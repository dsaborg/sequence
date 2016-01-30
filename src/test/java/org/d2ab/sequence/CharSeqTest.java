/*
 * Copyright 2015 Daniel Skogquist Åborg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.d2ab.sequence;

import org.d2ab.function.chars.CharBinaryOperator;
import org.d2ab.iterable.chars.CharIterable;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.chars.DelegatingCharIterator;
import org.d2ab.util.Arrayz;
import org.d2ab.util.primitive.OptionalChar;
import org.junit.Test;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class CharSeqTest {
	private final CharSeq empty = CharSeq.empty();

	private final CharSeq a = CharSeq.of('a');
	private final CharSeq ab = CharSeq.of('a', 'b');
	private final CharSeq abc = CharSeq.of('a', 'b', 'c');
	private final CharSeq abcd = CharSeq.of('a', 'b', 'c', 'd');
	private final CharSeq abcde = CharSeq.of('a', 'b', 'c', 'd', 'e');
	private final CharSeq abcdefghi = CharSeq.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i');

	private final CharSeq oneRandom = CharSeq.of('q');
	private final CharSeq twoRandom = CharSeq.of('q', 'w');
	private final CharSeq nineRandom = CharSeq.of('f', 'f', 'a', 'g', 'a', 'b', 'q', 'e', 'd');

	@Test
	public void ofOne() {
		twice(() -> assertThat(a, contains('a')));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(abc, contains('a', 'b', 'c')));
	}

	@Test
	public void forLoop() {
		twice(() -> {
			for (int ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			int expected = 'a';
			for (int i : abc)
				assertThat(i, is(expected++));
		});
	}

	@Test
	public void forEach() {
		twice(() -> {
			empty.forEachChar(c -> fail("Should not get called"));
			a.forEachChar(c -> assertThat(c, is(in(singletonList('a')))));
			ab.forEachChar(c -> assertThat(c, is(in(asList('a', 'b')))));
			abc.forEachChar(c -> assertThat(c, is(in(asList('a', 'b', 'c')))));
		});
	}

	@Test
	public void iterator() {
		twice(() -> {
			CharIterator iterator = abc.iterator();

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.nextChar(), is('a'));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.nextChar(), is('b'));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.nextChar(), is('c'));

			assertThat(iterator.hasNext(), is(false));
			assertThat(iterator.hasNext(), is(false));
		});
	}

	@Test
	public void ofNone() {
		CharSeq sequence = CharSeq.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void fromSequence() {
		CharSeq fromSequence = CharSeq.from(abc);

		twice(() -> assertThat(fromSequence, contains('a', 'b', 'c')));
	}

	@Test
	public void fromIterable() {
		Iterable<Character> iterable = () -> asList('a', 'b', 'c').iterator();

		CharSeq sequenceFromIterable = CharSeq.from(iterable);

		twice(() -> assertThat(sequenceFromIterable, contains('a', 'b', 'c')));
	}

	@Test
	public void fromStream() {
		CharSeq sequenceFromStream = CharSeq.from(asList('a', 'b', 'c').stream());

		assertThat(sequenceFromStream, contains('a', 'b', 'c'));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromEmptyStream() {
		CharSeq sequenceFromStream = CharSeq.from(Stream.of());

		assertThat(sequenceFromStream, is(emptyIterable()));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromIteratorSupplier() {
		Supplier<CharIterator> iterators = () -> CharIterator.from(asList('a', 'b', 'c'));

		CharSeq sequenceFromIterators = CharSeq.from(iterators);

		twice(() -> assertThat(sequenceFromIterators, contains('a', 'b', 'c')));
	}

	@Test
	public void skip() {
		CharSeq skipNone = abc.skip(0);
		twice(() -> assertThat(skipNone, contains('a', 'b', 'c')));

		CharSeq skipOne = abc.skip(1);
		twice(() -> assertThat(skipOne, contains('b', 'c')));

		CharSeq skipTwo = abc.skip(2);
		twice(() -> assertThat(skipTwo, contains('c')));

		CharSeq skipThree = abc.skip(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		CharSeq skipFour = abc.skip(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		CharSeq limitNone = abc.limit(0);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		CharSeq limitOne = abc.limit(1);
		twice(() -> assertThat(limitOne, contains('a')));

		CharSeq limitTwo = abc.limit(2);
		twice(() -> assertThat(limitTwo, contains('a', 'b')));

		CharSeq limitThree = abc.limit(3);
		twice(() -> assertThat(limitThree, contains('a', 'b', 'c')));

		CharSeq limitFour = abc.limit(4);
		twice(() -> assertThat(limitFour, contains('a', 'b', 'c')));
	}

	@Test
	public void append() {
		CharSeq appended = abc.append(CharSeq.of('d', 'e', 'f')).append(CharSeq.of('g', 'h'));

		twice(() -> assertThat(appended, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')));
	}

	@Test
	public void appendIterator() {
		CharSeq appended = abc.append(Arrayz.iterator('d', 'e', 'f')).append(Arrayz.iterator('g', 'h'));

		assertThat(appended, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
		assertThat(appended, contains('a', 'b', 'c'));
	}

	@Test
	public void appendStream() {
		CharSeq appended = abc.append(Stream.of('d', 'e', 'f')).append(Stream.of('g', 'h'));

		assertThat(appended, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));

		CharIterator iterator = appended.iterator();
		assertThat(iterator.nextChar(), is('a')); // First three are ok
		assertThat(iterator.nextChar(), is('b'));
		assertThat(iterator.nextChar(), is('c'));

		expecting(NoSuchElementException.class, iterator::nextChar); // Hitting Stream that is exhausted
	}

	@Test
	public void appendArray() {
		CharSeq appended = abc.append('d', 'e', 'f').append('g', 'h');

		twice(() -> assertThat(appended, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')));
	}

	@Test
	public void appendIsLazy() {
		CharIterator first = CharIterator.from(asList('a', 'b', 'c'));
		CharIterator second = CharIterator.from(asList('d', 'e', 'f'));
		CharIterator third = CharIterator.from(asList('g', 'h'));

		CharSeq then = CharSeq.from(first).append(() -> second).append(() -> third);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));
		assertThat(third.hasNext(), is(true));

		assertThat(then, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
		assertThat(then, is(emptyIterable())); // iterators exhausted on second run
	}

	@Test
	public void thenIsLazyWhenSkippingHasNext() {
		CharIterator first = CharIterator.of('a');
		CharIterator second = CharIterator.of('b');

		CharSeq sequence = CharSeq.from(first).append(() -> second);

		// check delayed iteration
		CharIterator iterator = sequence.iterator();
		assertThat(iterator.next(), is('a'));
		assertThat(iterator.next(), is('b'));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		CharSeq filtered = CharSeq.of('a', 'b', 'c', 'd', 'e', 'f', 'g').filter(i -> (i % 2) == 0);

		twice(() -> assertThat(filtered, contains('b', 'd', 'f')));
	}

	@Test
	public void map() {
		CharSeq mapped = abc.map(c -> (char) (c + 1));
		twice(() -> assertThat(mapped, contains('b', 'c', 'd')));
	}

	@Test
	public void recurse() {
		CharSeq recursive = CharSeq.recurse('a', c -> (char) (c + 1));
		twice(() -> assertThat(recursive.limit(10), contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j')));
	}

	@Test
	public void untilTerminal() {
		CharSeq until = CharSeq.recurse('a', x -> (char) (x + 1)).until('g');
		twice(() -> assertThat(until, contains('a', 'b', 'c', 'd', 'e', 'f')));
	}

	@Test
	public void endingAtTerminal() {
		CharSeq endingAt = CharSeq.recurse('a', x -> (char) (x + 1)).endingAt('g');
		twice(() -> assertThat(endingAt, contains('a', 'b', 'c', 'd', 'e', 'f', 'g')));
	}

	@Test
	public void untilPredicate() {
		CharSeq until = CharSeq.recurse('a', x -> (char) (x + 1)).until(c -> c == 'g');
		twice(() -> assertThat(until, contains('a', 'b', 'c', 'd', 'e', 'f')));
	}

	@Test
	public void endingAtPredicate() {
		CharSeq endingAt = CharSeq.recurse('a', x -> (char) (x + 1)).endingAt(c -> c == 'g');
		twice(() -> assertThat(endingAt, contains('a', 'b', 'c', 'd', 'e', 'f', 'g')));
	}

	@Test
	public void collect() {
		twice(() -> {
			StringBuilder builder = abc.collect(StringBuilder::new, StringBuilder::append);
			assertThat(builder.toString(), is("abc"));
		});
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(Arrays.equals(abc.toArray(), new char[]{'a', 'b', 'c'}), is(true)));
	}

	@Test
	public void join() {
		twice(() -> assertThat(abc.join(", "), is("a, b, c")));
	}

	@Test
	public void joinWithPrefixAndSuffix() {
		twice(() -> assertThat(abc.join("<", ", ", ">"), is("<a, b, c>")));
	}

	@Test
	public void reduce() {
		CharBinaryOperator secondChar = (c1, c2) -> c2;
		twice(() -> {
			assertThat(empty.reduce(secondChar), is(OptionalChar.empty()));
			assertThat(a.reduce(secondChar), is(OptionalChar.of('a')));
			assertThat(ab.reduce(secondChar), is(OptionalChar.of('b')));
			assertThat(abc.reduce(secondChar), is(OptionalChar.of('c')));
		});
	}

	@Test
	public void reduceWithIdentity() {
		CharBinaryOperator secondChar = (c1, c2) -> c2;
		twice(() -> {
			assertThat(empty.reduce('q', secondChar), is('q'));
			assertThat(a.reduce('q', secondChar), is('a'));
			assertThat(ab.reduce('q', secondChar), is('b'));
			assertThat(abc.reduce('q', secondChar), is('c'));
		});
	}

	@Test
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(OptionalChar.empty()));
			assertThat(a.first(), is(OptionalChar.of('a')));
			assertThat(ab.first(), is(OptionalChar.of('a')));
			assertThat(abc.first(), is(OptionalChar.of('a')));
		});
	}

	@Test
	public void second() {
		twice(() -> {
			assertThat(empty.second(), is(OptionalChar.empty()));
			assertThat(a.second(), is(OptionalChar.empty()));
			assertThat(ab.second(), is(OptionalChar.of('b')));
			assertThat(abc.second(), is(OptionalChar.of('b')));
			assertThat(abcd.second(), is(OptionalChar.of('b')));
		});
	}

	@Test
	public void third() {
		twice(() -> {
			assertThat(empty.third(), is(OptionalChar.empty()));
			assertThat(a.third(), is(OptionalChar.empty()));
			assertThat(ab.third(), is(OptionalChar.empty()));
			assertThat(abc.third(), is(OptionalChar.of('c')));
			assertThat(abcd.third(), is(OptionalChar.of('c')));
			assertThat(abcde.third(), is(OptionalChar.of('c')));
		});
	}

	@Test
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(OptionalChar.empty()));
			assertThat(a.last(), is(OptionalChar.of('a')));
			assertThat(ab.last(), is(OptionalChar.of('b')));
			assertThat(abc.last(), is(OptionalChar.of('c')));
		});
	}

	@Test
	public void step() {
		CharSeq stepThree = abcdefghi.step(3);
		twice(() -> assertThat(stepThree, contains('a', 'd', 'g')));
	}

	@Test
	public void distinct() {
		CharSeq emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		CharSeq oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, contains('q')));

		CharSeq twoDuplicatesDistinct = CharSeq.of('q', 'q').distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, contains('q')));

		CharSeq nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, contains('f', 'a', 'g', 'b', 'q', 'e', 'd')));
	}

	@Test
	public void sorted() {
		CharSeq emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		CharSeq oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, contains('q')));

		CharSeq twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, contains('q', 'w')));

		CharSeq nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, contains('a', 'a', 'b', 'd', 'e', 'f', 'f', 'g', 'q')));
	}

	@Test
	public void min() {
		OptionalChar emptyMin = empty.min();
		twice(() -> assertThat(emptyMin, is(OptionalChar.empty())));

		OptionalChar oneMin = oneRandom.min();
		twice(() -> assertThat(oneMin, is(OptionalChar.of('q'))));

		OptionalChar twoMin = twoRandom.min();
		twice(() -> assertThat(twoMin, is(OptionalChar.of('q'))));

		OptionalChar nineMin = nineRandom.min();
		twice(() -> assertThat(nineMin, is(OptionalChar.of('a'))));
	}

	@Test
	public void max() {
		OptionalChar emptyMax = empty.max();
		twice(() -> assertThat(emptyMax, is(OptionalChar.empty())));

		OptionalChar oneMax = oneRandom.max();
		twice(() -> assertThat(oneMax, is(OptionalChar.of('q'))));

		OptionalChar twoMax = twoRandom.max();
		twice(() -> assertThat(twoMax, is(OptionalChar.of('w'))));

		OptionalChar nineMax = nineRandom.max();
		twice(() -> assertThat(nineMax, is(OptionalChar.of('q'))));
	}

	@Test
	public void count() {
		twice(() -> assertThat(empty.count(), is(0L)));
		twice(() -> assertThat(a.count(), is(1L)));
		twice(() -> assertThat(ab.count(), is(2L)));
		twice(() -> assertThat(abcdefghi.count(), is(9L)));
	}

	@Test
	public void any() {
		twice(() -> assertThat(abc.any(x -> x > '@'), is(true)));
		twice(() -> assertThat(abc.any(x -> x > 'b'), is(true)));
		twice(() -> assertThat(abc.any(x -> x > 'd'), is(false)));
	}

	@Test
	public void all() {
		twice(() -> assertThat(abc.all(x -> x > '@'), is(true)));
		twice(() -> assertThat(abc.all(x -> x > 'b'), is(false)));
		twice(() -> assertThat(abc.all(x -> x > 'd'), is(false)));
	}

	@Test
	public void none() {
		twice(() -> assertThat(abc.none(x -> x > '@'), is(false)));
		twice(() -> assertThat(abc.none(x -> x > 'b'), is(false)));
		twice(() -> assertThat(abc.none(x -> x > 'd'), is(true)));
	}

	@Test
	public void peek() {
		CharSeq peek = abc.peek(x -> assertThat(x, is(both(greaterThan('@')).and(lessThan('d')))));
		twice(() -> assertThat(peek, contains('a', 'b', 'c')));
	}

	@Test
	public void prefix() {
		CharSeq prefixEmpty = empty.prefix('[');
		twice(() -> assertThat(prefixEmpty, contains('[')));

		CharSeq prefix = abc.prefix('[');
		twice(() -> assertThat(prefix, contains('[', 'a', 'b', 'c')));
	}

	@Test
	public void suffix() {
		CharSeq suffixEmpty = empty.suffix(']');
		twice(() -> assertThat(suffixEmpty, contains(']')));

		CharSeq suffix = abc.suffix(']');
		twice(() -> assertThat(suffix, contains('a', 'b', 'c', ']')));
	}

	@Test
	public void interleave() {
		assertThat(empty.interleave(empty), is(emptyIterable()));
		assertThat(abc.interleave(abcde), contains('a', 'a', 'b', 'b', 'c', 'c', 'd', 'e'));
		assertThat(abcde.interleave(abc), contains('a', 'a', 'b', 'b', 'c', 'c', 'd', 'e'));
	}

	@Test
	public void reverse() {
		CharSeq emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		CharSeq oneReversed = a.reverse();
		twice(() -> assertThat(oneReversed, contains('a')));

		CharSeq twoReversed = ab.reverse();
		twice(() -> assertThat(twoReversed, contains('b', 'a')));

		CharSeq threeReversed = abc.reverse();
		twice(() -> assertThat(threeReversed, contains('c', 'b', 'a')));

		CharSeq nineReversed = abcdefghi.reverse();
		twice(() -> assertThat(nineReversed, contains('i', 'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a')));
	}

	@Test
	public void chars() {
		assertThat(CharSeq.all().limit(3), contains('\u0000', '\u0001', '\u0002'));
		assertThat(CharSeq.all().limit(0xC0).last(), is(OptionalChar.of('¿')));
		assertThat(CharSeq.all().count(), is(65536L));
	}

	@Test
	public void charsStartingAt() {
		assertThat(CharSeq.startingAt('A').limit(3), contains('A', 'B', 'C'));
		assertThat(CharSeq.startingAt('\u1400').limit(3).last(), is(OptionalChar.of('\u1402')));
		assertThat(CharSeq.startingAt(Character.MAX_VALUE), contains(Character.MAX_VALUE));
		assertThat(CharSeq.startingAt('\u8000').count(), is(32768L));
	}

	@Test
	public void charRange() {
		assertThat(CharSeq.range('A', 'F'), contains('A', 'B', 'C', 'D', 'E', 'F'));
		assertThat(CharSeq.range('F', 'A'), contains('F', 'E', 'D', 'C', 'B', 'A'));
		assertThat(CharSeq.range('A', 'F').count(), is(6L));
	}

	@Test
	public void asString() {
		twice(() -> assertThat(empty.asString(), is("")));
		twice(() -> assertThat(a.asString(), is("a")));
		twice(() -> assertThat(ab.asString(), is("ab")));
		twice(() -> assertThat(abc.asString(), is("abc")));
		twice(() -> assertThat(abcde.asString(), is("abcde")));
	}

	@Test
	public void toInts() {
		IntSequence emptyIntSequence = empty.toInts();
		twice(() -> assertThat(emptyIntSequence, is(emptyIterable())));

		IntSequence intSequence = CharSeq.all().limit(5).toInts();
		twice(() -> assertThat(intSequence, contains(0, 1, 2, 3, 4)));
	}

	@Test
	public void toSequence() {
		Sequence<Character> emptySequence = empty.toSequence(c -> (char) (c + 1));
		twice(() -> assertThat(emptySequence, is(emptyIterable())));

		Sequence<Character> charsSequence = abcde.toSequence(c -> (char) (c + 1));
		twice(() -> assertThat(charsSequence, contains('b', 'c', 'd', 'e', 'f')));
	}

	@Test
	public void box() {
		Sequence<Character> emptyBoxed = empty.box();
		twice(() -> assertThat(emptyBoxed, is(emptyIterable())));

		Sequence<Character> charsBoxed = abcde.box();
		twice(() -> assertThat(charsBoxed, contains('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void repeat() {
		CharSeq repeatEmpty = empty.repeat();
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		CharSeq repeatOne = a.repeat();
		twice(() -> assertThat(repeatOne.limit(3), contains('a', 'a', 'a')));

		CharSeq repeatTwo = ab.repeat();
		twice(() -> assertThat(repeatTwo.limit(5), contains('a', 'b', 'a', 'b', 'a')));

		CharSeq repeatThree = abc.repeat();
		twice(() -> assertThat(repeatThree.limit(8), contains('a', 'b', 'c', 'a', 'b', 'c', 'a', 'b')));

		CharSeq repeatVarying = CharSeq.from(new CharIterable() {
			private List<Character> list = asList('a', 'b', 'c');
			int end = list.size();

			@Override
			public CharIterator iterator() {
				List<Character> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Character> iterator = subList.iterator();
				return new DelegatingCharIterator<Character, Iterator<Character>>() {
					@Override
					public char nextChar() {
						return iterator.next();
					}
				}.backedBy(iterator);
			}
		}).repeat();
		assertThat(repeatVarying, contains('a', 'b', 'c', 'a', 'b', 'a'));
	}

	@Test
	public void repeatTwice() {
		CharSeq repeatEmpty = empty.repeat(2);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		CharSeq repeatOne = a.repeat(2);
		twice(() -> assertThat(repeatOne, contains('a', 'a')));

		CharSeq repeatTwo = ab.repeat(2);
		twice(() -> assertThat(repeatTwo, contains('a', 'b', 'a', 'b')));

		CharSeq repeatThree = abc.repeat(2);
		twice(() -> assertThat(repeatThree, contains('a', 'b', 'c', 'a', 'b', 'c')));

		CharSeq repeatVarying = CharSeq.from(new CharIterable() {
			private List<Character> list = asList('a', 'b', 'c');
			int end = list.size();

			@Override
			public CharIterator iterator() {
				List<Character> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Character> iterator = subList.iterator();
				return new DelegatingCharIterator<Character, Iterator<Character>>() {
					@Override
					public char nextChar() {
						return iterator.next();
					}
				}.backedBy(iterator);
			}
		}).repeat(2);
		assertThat(repeatVarying, contains('a', 'b', 'c', 'a', 'b'));
	}

	@Test
	public void repeatZero() {
		CharSeq repeatEmpty = empty.repeat(0);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		CharSeq repeatOne = a.repeat(0);
		twice(() -> assertThat(repeatOne, is(emptyIterable())));

		CharSeq repeatTwo = ab.repeat(0);
		twice(() -> assertThat(repeatTwo, is(emptyIterable())));

		CharSeq repeatThree = abc.repeat(0);
		twice(() -> assertThat(repeatThree, is(emptyIterable())));
	}

	@Test
	public void generate() {
		Queue<Character> queue = new ArrayDeque<>(asList('a', 'b', 'c', 'd', 'e'));
		CharSeq sequence = CharSeq.generate(queue::poll).endingAt('e');

		assertThat(sequence, contains('a', 'b', 'c', 'd', 'e'));
	}
}
