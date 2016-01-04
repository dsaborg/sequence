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

import org.d2ab.primitive.chars.CharBinaryOperator;
import org.d2ab.primitive.chars.CharIterator;
import org.d2ab.primitive.chars.OptionalChar;
import org.d2ab.sequence.PrimitiveSequence.Chars;
import org.d2ab.utils.MoreArrays;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class PrimitiveSequenceCharsTest {
	private final Chars empty = Chars.empty();
	private final Chars a = Chars.of('a');
	private final Chars ab = Chars.of('a', 'b');
	private final Chars abc = Chars.of('a', 'b', 'c');
	private final Chars abcd = Chars.of('a', 'b', 'c', 'd');
	private final Chars abcde = Chars.of('a', 'b', 'c', 'd', 'e');
	private final Chars abcdefghi = Chars.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i');
	private final Chars oneRandom = Chars.of('q');
	private final Chars twoRandom = Chars.of('q', 'w');
	private final Chars threeRandom = Chars.of('b', 'c', 'a');
	private final Chars nineRandom = Chars.of('f', 'f', 'a', 'g', 'a', 'b', 'q', 'e', 'd');

	@Test
	public void ofOne() throws Exception {
		twice(() -> assertThat(a, contains('a')));
	}

	@Test
	public void ofMany() throws Exception {
		twice(() -> assertThat(abc, contains('a', 'b', 'c')));
	}

	@Test
	public void forLoop() throws Exception {
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
	public void forEach() throws Exception {
		twice(() -> {
			empty.forEachChar(c -> fail("Should not get called"));
			a.forEachChar(c -> assertThat(c, is(in(singletonList('a')))));
			ab.forEachChar(c -> assertThat(c, is(in(Arrays.asList('a', 'b')))));
			abc.forEachChar(c -> assertThat(c, is(in(Arrays.asList('a', 'b', 'c')))));
		});
	}

	@Test
	public void iterator() throws Exception {
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
	public void ofNone() throws Exception {
		Chars sequence = Chars.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() throws Exception {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void fromSequence() throws Exception {
		Chars fromSequence = Chars.from(abc);

		twice(() -> assertThat(fromSequence, contains('a', 'b', 'c')));
	}

	@Test
	public void fromIterable() throws Exception {
		Iterable<Character> iterable = () -> Arrays.asList('a', 'b', 'c').iterator();

		Chars sequenceFromIterable = Chars.from(iterable);

		twice(() -> assertThat(sequenceFromIterable, contains('a', 'b', 'c')));
	}

	@Test
	public void fromStream() throws Exception {
		Chars sequenceFromStream = Chars.from(Arrays.asList('a', 'b', 'c').stream());

		assertThat(sequenceFromStream, contains('a', 'b', 'c'));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromEmptyStream() throws Exception {
		Chars sequenceFromStream = Chars.from(Stream.of());

		assertThat(sequenceFromStream, is(emptyIterable()));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromIteratorSupplier() throws Exception {
		Supplier<CharIterator> iterators = () -> CharIterator.from(Arrays.asList('a', 'b', 'c'));

		Chars sequenceFromIterators = Chars.from(iterators);

		twice(() -> assertThat(sequenceFromIterators, contains('a', 'b', 'c')));
	}

	@Test
	public void skip() {
		Chars skipNone = abc.skip(0);
		twice(() -> assertThat(skipNone, contains('a', 'b', 'c')));

		Chars skipOne = abc.skip(1);
		twice(() -> assertThat(skipOne, contains('b', 'c')));

		Chars skipTwo = abc.skip(2);
		twice(() -> assertThat(skipTwo, contains('c')));

		Chars skipThree = abc.skip(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		Chars skipFour = abc.skip(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		Chars limitNone = abc.limit(0);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		Chars limitOne = abc.limit(1);
		twice(() -> assertThat(limitOne, contains('a')));

		Chars limitTwo = abc.limit(2);
		twice(() -> assertThat(limitTwo, contains('a', 'b')));

		Chars limitThree = abc.limit(3);
		twice(() -> assertThat(limitThree, contains('a', 'b', 'c')));

		Chars limitFour = abc.limit(4);
		twice(() -> assertThat(limitFour, contains('a', 'b', 'c')));
	}

	@Test
	public void append() {
		Chars appended = abc.append(Chars.of('d', 'e', 'f')).append(Chars.of('g', 'h'));

		twice(() -> assertThat(appended, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')));
	}

	@Test
	public void appendIterator() {
		Chars appended = abc.append(MoreArrays.iterator('d', 'e', 'f')).append(MoreArrays.iterator('g', 'h'));

		assertThat(appended, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
		assertThat(appended, contains('a', 'b', 'c'));
	}

	@Test
	public void appendStream() {
		Chars appended = abc.append(Stream.of('d', 'e', 'f')).append(Stream.of('g', 'h'));

		assertThat(appended, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));

		CharIterator iterator = appended.iterator();
		assertThat(iterator.nextChar(), is('a')); // First three are ok
		assertThat(iterator.nextChar(), is('b'));
		assertThat(iterator.nextChar(), is('c'));

		expecting(NoSuchElementException.class, iterator::nextChar); // Hitting Stream that is exhausted
	}

	@Test
	public void appendArray() {
		Chars appended = abc.append('d', 'e', 'f').append('g', 'h');

		twice(() -> assertThat(appended, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')));
	}

	@Test
	public void appendIsLazy() {
		CharIterator first = CharIterator.from(Arrays.asList('a', 'b', 'c'));
		CharIterator second = CharIterator.from(Arrays.asList('d', 'e', 'f'));
		CharIterator third = CharIterator.from(Arrays.asList('g', 'h'));

		Chars then = Chars.from(first).append(() -> second).append(() -> third);

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

		Chars sequence = Chars.from(first).append(() -> second);

		// check delayed iteration
		CharIterator iterator = sequence.iterator();
		assertThat(iterator.next(), is('a'));
		assertThat(iterator.next(), is('b'));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		Chars filtered = Chars.of('a', 'b', 'c', 'd', 'e', 'f', 'g').filter(i -> (i % 2) == 0);

		twice(() -> assertThat(filtered, contains('b', 'd', 'f')));
	}

	@Test
	public void map() {
		Chars mapped = abc.map(c -> (char) (c + 1));
		twice(() -> assertThat(mapped, contains('b', 'c', 'd')));
	}

	@Test
	public void recurse() {
		Chars recursive = Chars.recurse('a', c -> (char) (c + 1));
		twice(() -> assertThat(recursive.limit(10), contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j')));
	}

	@Test
	public void recurseUntil() {
		Chars until = Chars.recurse('a', c -> (char) (c + 1)).until('g');
		twice(() -> assertThat(until, contains('a', 'b', 'c', 'd', 'e', 'f')));
	}

	@Test
	public void recurseEndingAt() {
		Chars endingAt = Chars.recurse('a', c -> (char) (c + 1)).endingAt('g');
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
		Chars stepThree = abcdefghi.step(3);
		twice(() -> assertThat(stepThree, contains('a', 'd', 'g')));
	}

	@Test
	public void distinct() {
		Chars emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		Chars oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, contains('q')));

		Chars twoDuplicatesDistinct = Chars.of('q', 'q').distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, contains('q')));

		Chars nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, contains('f', 'a', 'g', 'b', 'q', 'e', 'd')));
	}

	@Test
	public void sorted() {
		Chars emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		Chars oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, contains('q')));

		Chars twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, contains('q', 'w')));

		Chars nineSorted = nineRandom.sorted();
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
		Chars peek = abc.peek(x -> assertThat(x, is(both(greaterThan('@')).and(lessThan('d')))));
		twice(() -> assertThat(peek, contains('a', 'b', 'c')));
	}

	@Test
	public void prefix() {
		Chars prefixEmpty = empty.prefix('[');
		twice(() -> assertThat(prefixEmpty, contains('[')));

		Chars prefix = abc.prefix('[');
		twice(() -> assertThat(prefix, contains('[', 'a', 'b', 'c')));
	}

	@Test
	public void suffix() {
		Chars suffixEmpty = empty.suffix(']');
		twice(() -> assertThat(suffixEmpty, contains(']')));

		Chars suffix = abc.suffix(']');
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
		Chars emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		Chars oneReversed = a.reverse();
		twice(() -> assertThat(oneReversed, contains('a')));

		Chars twoReversed = ab.reverse();
		twice(() -> assertThat(twoReversed, contains('b', 'a')));

		Chars threeReversed = abc.reverse();
		twice(() -> assertThat(threeReversed, contains('c', 'b', 'a')));

		Chars nineReversed = abcdefghi.reverse();
		twice(() -> assertThat(nineReversed, contains('i', 'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a')));
	}

	@Test
	public void chars() {
		assertThat(Chars.all().limit(3), contains('\u0000', '\u0001', '\u0002'));
		assertThat(Chars.all().limit(0xC0).last(), is(OptionalChar.of('¿')));
		assertThat(Chars.all().count(), is(65536L));
	}

	@Test
	public void charsStartingAt() {
		assertThat(Chars.startingAt('A').limit(3), contains('A', 'B', 'C'));
		assertThat(Chars.startingAt('\u1400').limit(3).last(), is(OptionalChar.of('\u1402')));
		assertThat(Chars.startingAt(Character.MAX_VALUE), contains(Character.MAX_VALUE));
		assertThat(Chars.startingAt('\u8000').count(), is(32768L));
	}

	@Test
	public void charRange() {
		assertThat(Chars.range('A', 'F'), contains('A', 'B', 'C', 'D', 'E', 'F'));
		assertThat(Chars.range('F', 'A'), contains('F', 'E', 'D', 'C', 'B', 'A'));
		assertThat(Chars.range('A', 'F').count(), is(6L));
	}
}
