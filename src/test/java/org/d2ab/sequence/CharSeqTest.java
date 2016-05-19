/*
 * Copyright 2016 Daniel Skogquist Åborg
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
import org.d2ab.iterable.Iterables;
import org.d2ab.iterable.chars.CharIterable;
import org.d2ab.iterator.Iterators;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.chars.DelegatingCharIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.util.primitive.OptionalChar;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class CharSeqTest {
	private final CharSeq empty = CharSeq.empty();

	private final CharSeq a = CharSeq.from(StrictCharIterable.of('a'));
	private final CharSeq ab = CharSeq.from(StrictCharIterable.of('a', 'b'));
	private final CharSeq abc = CharSeq.from(StrictCharIterable.of('a', 'b', 'c'));
	private final CharSeq abcd = CharSeq.from(StrictCharIterable.of('a', 'b', 'c', 'd'));
	private final CharSeq abcde = CharSeq.from(StrictCharIterable.of('a', 'b', 'c', 'd', 'e'));
	private final CharSeq abcdefghi = CharSeq.from(StrictCharIterable.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'));

	private final CharSeq oneRandom = CharSeq.from(StrictCharIterable.of('q'));
	private final CharSeq twoRandom = CharSeq.from(StrictCharIterable.of('q', 'w'));
	private final CharSeq threeRandom = CharSeq.from(StrictCharIterable.of('q', 'w', 'd'));
	private final CharSeq nineRandom = CharSeq.from(StrictCharIterable.of('f', 'f', 'a', 'g', 'a', 'b', 'q', 'e',
	                                                                      'd'));

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void ofNone() {
		CharSeq sequence = CharSeq.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void ofOne() {
		twice(() -> assertThat(a, containsChars('a')));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(abc, containsChars('a', 'b', 'c')));
	}

	@Test
	public void forLoop() {
		twice(() -> {
			for (int ignored : empty)
				fail("Should not get called");
		});

		CharSeq seq = CharSeq.of('a', 'b', 'c', 'd', 'e');
		twice(() -> {
			char expected = 'a';
			for (char c : seq)
				assertThat(c, is(expected++));

			assertThat(expected, is('f'));
		});
	}

	@Test
	public void forEachChar() {
		twice(() -> {
			empty.forEachChar(c -> fail("Should not get called"));

			AtomicInteger value = new AtomicInteger('a');
			a.forEachChar(c -> assertThat(c, is((char) value.getAndIncrement())));

			value.set('a');
			ab.forEachChar(c -> assertThat(c, is((char) value.getAndIncrement())));

			value.set('a');
			abcde.forEachChar(c -> assertThat(c, is((char) value.getAndIncrement())));
		});
	}

	@Test
	public void forEachCharIndexed() {
		twice(() -> {
			empty.forEachCharIndexed((e, i) -> fail("Should not get called"));

			AtomicInteger value = new AtomicInteger('a');
			AtomicLong index = new AtomicLong();
			a.forEachCharIndexed((e, i) -> {
				assertThat(e, is((char) value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(1L));

			value.set('a');
			index.set(0);
			ab.forEachCharIndexed((e, i) -> {
				assertThat(e, is((char) value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(2L));

			value.set('a');
			index.set(0);
			abcde.forEachCharIndexed((e, i) -> {
				assertThat(e, is((char) value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(5L));
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
	public void fromIterable() {
		CharSeq seq = CharSeq.from(Iterables.of('a', 'b', 'c', 'd', 'e'));

		twice(() -> assertThat(seq, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void fromCharIterable() {
		CharSeq seq = CharSeq.from(CharIterable.of('a', 'b', 'c', 'd', 'e'));

		twice(() -> assertThat(seq, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void onceIterator() {
		CharSeq seq = CharSeq.once(Iterators.of('a', 'b', 'c', 'd', 'e'));

		assertThat(seq, containsChars('a', 'b', 'c', 'd', 'e'));
		assertThat(seq, is(emptyIterable()));
	}

	@Test
	public void onceCharIterator() {
		CharSeq seq = CharSeq.once(CharIterator.of('a', 'b', 'c', 'd', 'e'));

		assertThat(seq, containsChars('a', 'b', 'c', 'd', 'e'));
		assertThat(seq, is(emptyIterable()));
	}

	@Test
	public void oncePrimitiveIteratorOfInt() {
		CharSeq seq = CharSeq.once(IntIterator.of('a', 'b', 'c', 'd', 'e'));

		assertThat(seq, containsChars('a', 'b', 'c', 'd', 'e'));
		assertThat(seq, is(emptyIterable()));
	}

	@Test
	public void onceStream() {
		CharSeq seq = CharSeq.once(Stream.of('a', 'b', 'c', 'd', 'e'));

		assertThat(seq, containsChars('a', 'b', 'c', 'd', 'e'));
		assertThat(seq, is(emptyIterable()));
	}

	@Test
	public void onceIntStream() {
		CharSeq seq = CharSeq.once(IntStream.of('a', 'b', 'c', 'd', 'e'));

		assertThat(seq, containsChars('a', 'b', 'c', 'd', 'e'));
		assertThat(seq, is(emptyIterable()));
	}

	@Test
	public void onceEmptyStream() {
		CharSeq seq = CharSeq.once(Stream.of());

		twice(() -> assertThat(seq, is(emptyIterable())));
	}

	@Test
	public void read() throws IOException {
		Reader reader = new StringReader("abcde");

		CharSeq seq = CharSeq.read(reader);
		twice(() -> assertThat(seq, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void readWithMark() throws IOException {
		Reader reader = new StringReader("abcde");
		assertThat((char) reader.read(), is('a'));

		reader.mark(0);

		CharSeq seq = CharSeq.read(reader);
		twice(() -> assertThat(seq, containsChars('b', 'c', 'd', 'e')));
	}

	@Test
	public void readAlreadyBegun() throws IOException {
		Reader reader = new StringReader("abcde");
		assertThat((char) reader.read(), is('a'));

		CharSeq seq = CharSeq.read(reader);
		assertThat(seq, containsChars('b', 'c', 'd', 'e'));
		assertThat(seq, containsChars('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void cachePrimitiveIteratorOfInt() {
		CharSeq cached = CharSeq.cache(IntIterator.of('a', 'b', 'c', 'd', 'e'));

		twice(() -> assertThat(cached, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void cacheCharIterator() {
		CharSeq cached = CharSeq.cache(CharIterator.of('a', 'b', 'c', 'd', 'e'));

		twice(() -> assertThat(cached, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void cacheIterator() {
		CharSeq cached = CharSeq.cache(Iterators.of('a', 'b', 'c', 'd', 'e'));

		twice(() -> assertThat(cached, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void cacheCharIterable() {
		CharSeq cached = CharSeq.cache(CharIterable.of('a', 'b', 'c', 'd', 'e'));

		twice(() -> assertThat(cached, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void cacheIterable() {
		CharSeq cached = CharSeq.cache(Iterables.of('a', 'b', 'c', 'd', 'e'));

		twice(() -> assertThat(cached, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void cacheIntStream() {
		CharSeq cached = CharSeq.cache(IntStream.of('a', 'b', 'c', 'd', 'e'));

		twice(() -> assertThat(cached, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void cacheStream() {
		CharSeq cached = CharSeq.cache(Stream.of('a', 'b', 'c', 'd', 'e'));

		twice(() -> assertThat(cached, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void skip() {
		CharSeq skipNone = abc.skip(0);
		twice(() -> assertThat(skipNone, containsChars('a', 'b', 'c')));

		CharSeq skipOne = abc.skip(1);
		twice(() -> assertThat(skipOne, containsChars('b', 'c')));

		CharSeq skipTwo = abc.skip(2);
		twice(() -> assertThat(skipTwo, containsChars('c')));

		CharSeq skipThree = abc.skip(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		CharSeq skipFour = abc.skip(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void skipTail() {
		CharSeq skipNone = abc.skipTail(0);
		twice(() -> assertThat(skipNone, containsChars('a', 'b', 'c')));

		CharSeq skipOne = abc.skipTail(1);
		twice(() -> assertThat(skipOne, containsChars('a', 'b')));

		CharSeq skipTwo = abc.skipTail(2);
		twice(() -> assertThat(skipTwo, containsChars('a')));

		CharSeq skipThree = abc.skipTail(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		CharSeq skipFour = abc.skipTail(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		CharSeq limitNone = abc.limit(0);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		CharSeq limitOne = abc.limit(1);
		twice(() -> assertThat(limitOne, containsChars('a')));

		CharSeq limitTwo = abc.limit(2);
		twice(() -> assertThat(limitTwo, containsChars('a', 'b')));

		CharSeq limitThree = abc.limit(3);
		twice(() -> assertThat(limitThree, containsChars('a', 'b', 'c')));

		CharSeq limitFour = abc.limit(4);
		twice(() -> assertThat(limitFour, containsChars('a', 'b', 'c')));
	}

	@Test
	public void appendArray() {
		CharSeq appended = abc.append('d', 'e', 'f').append('g', 'h');

		twice(() -> assertThat(appended, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')));
	}

	@Test
	public void appendCharIterable() {
		CharSeq appended = abc.append(CharIterable.of('d', 'e', 'f')).append(CharIterable.of('g', 'h'));

		twice(() -> assertThat(appended, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')));
	}

	@Test
	public void appendIterable() {
		CharSeq appended = abc.append(Iterables.of('d', 'e', 'f')).append(Iterables.of('g', 'h'));

		twice(() -> assertThat(appended, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')));
	}

	@Test
	public void appendCharIterator() {
		CharSeq appended = abc.append(CharIterator.of('d', 'e', 'f')).append(CharIterator.of('g', 'h'));

		assertThat(appended, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
		assertThat(appended, containsChars('a', 'b', 'c'));
	}

	@Test
	public void appendIterator() {
		CharSeq appended = abc.append(Iterators.of('d', 'e', 'f')).append(Iterators.of('g', 'h'));

		assertThat(appended, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
		assertThat(appended, containsChars('a', 'b', 'c'));
	}

	@Test
	public void appendStream() {
		CharSeq appended = abc.append(Stream.of('d', 'e', 'f')).append(Stream.of('g', 'h'));

		assertThat(appended, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
		assertThat(appended, containsChars('a', 'b', 'c'));
	}

	@Test
	public void appendIsLazy() {
		CharIterator first = CharIterator.of('a', 'b', 'c');
		CharIterator second = CharIterator.of('d', 'e', 'f');
		CharIterator third = CharIterator.of('g', 'h');

		CharSeq.once(first).append(second).append(third);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));
		assertThat(third.hasNext(), is(true));
	}

	@Test
	public void appendIsLazyWhenSkippingHasNext() {
		CharIterator first = CharIterator.of('a');
		CharIterator second = CharIterator.of('b');

		CharSeq sequence = CharSeq.once(first).append(second);

		// check delayed iteration
		CharIterator iterator = sequence.iterator();
		assertThat(iterator.next(), is('a'));
		assertThat(iterator.next(), is('b'));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		CharSeq emptyFiltered = empty.filter(c -> (c % 2) == 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));

		CharSeq oneFiltered = a.filter(c -> (c % 2) == 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		CharSeq twoFiltered = ab.filter(c -> (c % 2) == 0);
		twice(() -> assertThat(twoFiltered, containsChars('b')));

		CharSeq filtered = abcdefghi.filter(c -> (c % 2) == 0);
		twice(() -> assertThat(filtered, containsChars('b', 'd', 'f', 'h')));
	}

	@Test
	public void filterIndexed() {
		CharSeq emptyFiltered = empty.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));

		CharSeq oneFiltered = a.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		CharSeq twoFiltered = ab.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(twoFiltered, containsChars('b')));

		CharSeq filtered = abcdefghi.filterIndexed((i, x) -> x > 3);
		twice(() -> assertThat(filtered, containsChars('e', 'f', 'g', 'h', 'i')));
	}

	@Test
	public void filterBack() {
		CharSeq filteredLess = nineRandom.filterBack('z', (p, i) -> p < i);
		twice(() -> assertThat(filteredLess, containsChars('g', 'b', 'q')));

		CharSeq filteredGreater = nineRandom.filterBack('z', (p, i) -> p > i);
		twice(() -> assertThat(filteredGreater, containsChars('f', 'a', 'a', 'e', 'd')));
	}

	@Test
	public void filterForward() {
		CharSeq filteredLess = nineRandom.filterForward('z', (i, f) -> f < i);
		twice(() -> assertThat(filteredLess, containsChars('f', 'g', 'q', 'e')));

		CharSeq filteredGreater = nineRandom.filterForward('z', (i, f) -> f > i);
		twice(() -> assertThat(filteredGreater, containsChars('a', 'a', 'b', 'd')));
	}

	@Test
	public void includingArray() {
		CharSeq emptyIncluding = empty.including('a', 'c', 'e', 'q');
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));

		CharSeq including = abcde.including('a', 'c', 'e', 'q');
		twice(() -> assertThat(including, containsChars('a', 'c', 'e')));

		CharSeq includingAll = abcde.including('a', 'b', 'c', 'd', 'e', 'q');
		twice(() -> assertThat(includingAll, containsChars('a', 'b', 'c', 'd', 'e')));

		CharSeq includingNone = abcde.including();
		twice(() -> assertThat(includingNone, is(emptyIterable())));
	}

	@Test
	public void excludingArray() {
		CharSeq emptyExcluding = empty.excluding('a', 'c', 'e', 'q');
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));

		CharSeq excluding = abcde.excluding('a', 'c', 'e', 'q');
		twice(() -> assertThat(excluding, containsChars('b', 'd')));

		CharSeq excludingAll = abcde.excluding('a', 'b', 'c', 'd', 'e', 'q');
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		CharSeq excludingNone = abcde.excluding();
		twice(() -> assertThat(excludingNone, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void map() {
		CharSeq emptyMapped = empty.map(c -> (char) (c + 1));
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));

		CharSeq oneMapped = a.map(c -> (char) (c + 1));
		twice(() -> assertThat(oneMapped, containsChars('b')));

		CharSeq twoMapped = ab.map(c -> (char) (c + 1));
		twice(() -> assertThat(twoMapped, containsChars('b', 'c')));

		CharSeq fiveMapped = abcde.map(c -> (char) (c + 1));
		twice(() -> assertThat(fiveMapped, containsChars('b', 'c', 'd', 'e', 'f')));
	}

	@Test
	public void mapWithIndex() {
		CharSeq emptyMapped = empty.mapIndexed((c, x) -> (char) (c + x));
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));

		CharSeq oneMapped = a.mapIndexed((c, x) -> (char) (c + x));
		twice(() -> assertThat(oneMapped, containsChars('a')));

		CharSeq twoMapped = ab.mapIndexed((c, x) -> (char) (c + x));
		twice(() -> assertThat(twoMapped, containsChars('a', 'c')));

		CharSeq mapped = abcde.mapIndexed((c, x) -> (char) (c + x));
		twice(() -> assertThat(mapped, containsChars('a', 'c', 'e', 'g', 'i')));
	}

	@Test
	public void recurse() {
		CharSeq recursive = CharSeq.recurse('a', c -> (char) (c + 1));
		twice(() -> assertThat(recursive.limit(10), containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j')));
	}

	@Test
	public void untilTerminal() {
		CharSeq until = CharSeq.recurse('a', x -> (char) (x + 1)).until('g');
		twice(() -> assertThat(until, containsChars('a', 'b', 'c', 'd', 'e', 'f')));
	}

	@Test
	public void endingAtTerminal() {
		CharSeq endingAt = CharSeq.recurse('a', x -> (char) (x + 1)).endingAt('g');
		twice(() -> assertThat(endingAt, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g')));
	}

	@Test
	public void untilPredicate() {
		CharSeq until = CharSeq.recurse('a', x -> (char) (x + 1)).until(c -> c == 'g');
		twice(() -> assertThat(until, containsChars('a', 'b', 'c', 'd', 'e', 'f')));
	}

	@Test
	public void endingAtPredicate() {
		CharSeq endingAt = CharSeq.recurse('a', x -> (char) (x + 1)).endingAt(c -> c == 'g');
		twice(() -> assertThat(endingAt, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g')));
	}

	@Test
	public void startingAfter() {
		CharSeq startingEmpty = empty.startingAfter('e');
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		CharSeq sequence = abcdefghi.startingAfter('e');
		twice(() -> assertThat(sequence, containsChars('f', 'g', 'h', 'i')));

		CharSeq noStart = abcde.startingAfter('j');
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingAfterPredicate() {
		CharSeq startingEmpty = empty.startingAfter(c -> c == 'e');
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		CharSeq sequence = abcdefghi.startingAfter(c -> c == 'e');
		twice(() -> assertThat(sequence, containsChars('f', 'g', 'h', 'i')));

		CharSeq noStart = abcde.startingAfter(c -> c == 'j');
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFrom() {
		CharSeq startingEmpty = empty.startingFrom('e');
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		CharSeq sequence = abcdefghi.startingFrom('e');
		twice(() -> assertThat(sequence, containsChars('e', 'f', 'g', 'h', 'i')));

		CharSeq noStart = abcde.startingFrom('j');
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFromPredicate() {
		CharSeq startingEmpty = empty.startingFrom(c -> c == 'e');
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		CharSeq sequence = abcdefghi.startingFrom(c -> c == 'e');
		twice(() -> assertThat(sequence, containsChars('e', 'f', 'g', 'h', 'i')));

		CharSeq noStart = abcde.startingFrom(c -> c == 'j');
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void collect() {
		twice(() -> {
			StringBuilder builder = abc.collect(StringBuilder::new, StringBuilder::append);

			assertThat(builder.toString(), is("abc"));
		});
	}

	@Test
	public void collectInto() {
		twice(() -> {
			StringBuilder builder = new StringBuilder();
			StringBuilder result = abc.collectInto(builder, StringBuilder::append);

			assertThat(result, is(sameInstance(builder)));
			assertThat(result.toString(), is("abc"));
		});
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(Arrays.equals(abc.toArray(), new char[]{'a', 'b', 'c'}), is(true)));
	}

	@Test
	public void join() {
		twice(() -> assertThat(abc.join(), is("abc")));
	}

	@Test
	public void joinWithDelimiter() {
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
	public void at() {
		twice(() -> {
			assertThat(empty.at(0), is(OptionalChar.empty()));
			assertThat(empty.at(17), is(OptionalChar.empty()));

			assertThat(a.at(0), is(OptionalChar.of('a')));
			assertThat(a.at(1), is(OptionalChar.empty()));
			assertThat(a.at(17), is(OptionalChar.empty()));

			assertThat(abcde.at(0), is(OptionalChar.of('a')));
			assertThat(abcde.at(1), is(OptionalChar.of('b')));
			assertThat(abcde.at(4), is(OptionalChar.of('e')));
			assertThat(abcde.at(17), is(OptionalChar.empty()));
		});
	}

	@Test
	public void firstByPredicate() {
		twice(() -> {
			assertThat(empty.first(x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(a.first(x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(ab.first(x -> x > 'a'), is(OptionalChar.of('b')));
			assertThat(abcde.first(x -> x > 'a'), is(OptionalChar.of('b')));
		});
	}

	@Test
	public void secondByPredicate() {
		twice(() -> {
			assertThat(empty.second(x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(a.second(x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(ab.second(x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(abc.second(x -> x > 'a'), is(OptionalChar.of('c')));
			assertThat(abcd.second(x -> x > 'a'), is(OptionalChar.of('c')));
		});
	}

	@Test
	public void thirdByPredicate() {
		twice(() -> {
			assertThat(empty.third(x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(a.third(x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(ab.third(x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(abc.third(x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(abcd.third(x -> x > 'a'), is(OptionalChar.of('d')));
			assertThat(abcde.third(x -> x > 'a'), is(OptionalChar.of('d')));
		});
	}

	@Test
	public void lastByPredicate() {
		twice(() -> {
			assertThat(empty.last(x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(a.last(x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(ab.last(x -> x > 'a'), is(OptionalChar.of('b')));
			assertThat(abcde.last(x -> x > 'a'), is(OptionalChar.of('e')));
		});
	}

	@Test
	public void atByPredicate() {
		twice(() -> {
			assertThat(empty.at(0, x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(empty.at(17, x -> x > 'a'), is(OptionalChar.empty()));

			assertThat(a.at(0, x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(a.at(17, x -> x > 'a'), is(OptionalChar.empty()));

			assertThat(ab.at(0, x -> x > 'a'), is(OptionalChar.of('b')));
			assertThat(ab.at(1, x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(ab.at(17, x -> x > 'a'), is(OptionalChar.empty()));

			assertThat(abcde.at(0, x -> x > 'a'), is(OptionalChar.of('b')));
			assertThat(abcde.at(1, x -> x > 'a'), is(OptionalChar.of('c')));
			assertThat(abcde.at(3, x -> x > 'a'), is(OptionalChar.of('e')));
			assertThat(abcde.at(4, x -> x > 'a'), is(OptionalChar.empty()));
			assertThat(abcde.at(17, x -> x > 'a'), is(OptionalChar.empty()));
		});
	}

	@Test
	public void step() {
		CharSeq stepThree = abcdefghi.step(3);
		twice(() -> assertThat(stepThree, containsChars('a', 'd', 'g')));
	}

	@Test
	public void distinct() {
		CharSeq emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		CharSeq oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, containsChars('q')));

		CharSeq twoDuplicatesDistinct = CharSeq.from(StrictCharIterable.of('q', 'q')).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, containsChars('q')));

		CharSeq nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, containsChars('f', 'a', 'g', 'b', 'q', 'e', 'd')));
	}

	@Test
	public void sorted() {
		CharSeq emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		CharSeq oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, containsChars('q')));

		CharSeq twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, containsChars('q', 'w')));

		CharSeq nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, containsChars('a', 'a', 'b', 'd', 'e', 'f', 'f', 'g', 'q')));
	}

	@Test
	public void sortedWithUpdates() {
		List<Character> backing = new ArrayList<>(asList('b', 'c', 'a'));
		CharSeq sorted = CharSeq.from(backing).sorted();

		backing.add('d');
		assertThat(sorted, containsChars('a', 'b', 'c', 'd'));
	}

	@Test
	public void min() {
		twice(() -> assertThat(empty.min(), is(OptionalChar.empty())));
		twice(() -> assertThat(oneRandom.min(), is(OptionalChar.of('q'))));
		twice(() -> assertThat(twoRandom.min(), is(OptionalChar.of('q'))));
		twice(() -> assertThat(nineRandom.min(), is(OptionalChar.of('a'))));
	}

	@Test
	public void max() {
		twice(() -> assertThat(empty.max(), is(OptionalChar.empty())));
		twice(() -> assertThat(oneRandom.max(), is(OptionalChar.of('q'))));
		twice(() -> assertThat(twoRandom.max(), is(OptionalChar.of('w'))));
		twice(() -> assertThat(nineRandom.max(), is(OptionalChar.of('q'))));
	}

	@Test
	public void size() {
		twice(() -> assertThat(empty.size(), is(0L)));
		twice(() -> assertThat(a.size(), is(1L)));
		twice(() -> assertThat(ab.size(), is(2L)));
		twice(() -> assertThat(abcdefghi.size(), is(9L)));
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
		CharSeq peekEmpty = empty.peek(x -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(peekEmpty, is(emptyIterable())));

		AtomicInteger value = new AtomicInteger('a');
		CharSeq peekOne = a.peek(x -> assertThat(x, is((char) value.getAndIncrement())));
		twiceIndexed(value, 1, () -> assertThat(peekOne, containsChars('a')));

		CharSeq peekTwo = ab.peek(x -> assertThat(x, is((char) value.getAndIncrement())));
		twiceIndexed(value, 2, () -> assertThat(peekTwo, containsChars('a', 'b')));

		CharSeq peek = abcde.peek(x -> assertThat(x, is((char) value.getAndIncrement())));
		twiceIndexed(value, 5, () -> assertThat(peek, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void peekIndexed() {
		CharSeq peekEmpty = empty.peekIndexed((i, x) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(peekEmpty, is(emptyIterable())));

		AtomicLong index = new AtomicLong();
		CharSeq peekOne = a.peekIndexed((i, x) -> {
			assertThat(i, is((char) (index.get() + 'a')));
			assertThat(x, is(index.getAndIncrement()));
		});
		twiceIndexed(index, 1, () -> assertThat(peekOne, containsChars('a')));

		CharSeq peekTwo = ab.peekIndexed((i, x) -> {
			assertThat(i, is((char) (index.get() + 'a')));
			assertThat(x, is(index.getAndIncrement()));
		});
		twiceIndexed(index, 2, () -> assertThat(peekTwo, containsChars('a', 'b')));

		CharSeq peek = abcde.peekIndexed((i, x) -> {
			assertThat(i, is((char) (index.get() + 'a')));
			assertThat(x, is(index.getAndIncrement()));
		});
		twiceIndexed(index, 5, () -> assertThat(peek, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void prefix() {
		CharSeq prefixEmpty = empty.prefix('[');
		twice(() -> assertThat(prefixEmpty, containsChars('[')));

		CharSeq prefix = abc.prefix('[');
		twice(() -> assertThat(prefix, containsChars('[', 'a', 'b', 'c')));
	}

	@Test
	public void suffix() {
		CharSeq suffixEmpty = empty.suffix(']');
		twice(() -> assertThat(suffixEmpty, containsChars(']')));

		CharSeq suffix = abc.suffix(']');
		twice(() -> assertThat(suffix, containsChars('a', 'b', 'c', ']')));
	}

	@Test
	public void interleave() {
		assertThat(empty.interleave(empty), is(emptyIterable()));
		assertThat(abc.interleave(abcde), containsChars('a', 'a', 'b', 'b', 'c', 'c', 'd', 'e'));
		assertThat(abcde.interleave(abc), containsChars('a', 'a', 'b', 'b', 'c', 'c', 'd', 'e'));
	}

	@Test
	public void reverse() {
		CharSeq emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		CharSeq oneReversed = a.reverse();
		twice(() -> assertThat(oneReversed, containsChars('a')));

		CharSeq twoReversed = ab.reverse();
		twice(() -> assertThat(twoReversed, containsChars('b', 'a')));

		CharSeq threeReversed = abc.reverse();
		twice(() -> assertThat(threeReversed, containsChars('c', 'b', 'a')));

		CharSeq nineReversed = abcdefghi.reverse();
		twice(() -> assertThat(nineReversed, containsChars('i', 'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a')));
	}

	@Test
	public void reverseWithUpdates() {
		List<Character> backing = new ArrayList<>(asList('a', 'b', 'c'));
		CharSeq reversed = CharSeq.from(backing).reverse();

		backing.add('d');
		assertThat(reversed, containsChars('d', 'c', 'b', 'a'));
	}

	@Test
	public void chars() {
		assertThat(CharSeq.all().limit(5), containsChars('\u0000', '\u0001', '\u0002', '\u0003', '\u0004'));
		assertThat(CharSeq.all().limit(0xC0).last(), is(OptionalChar.of('¿')));
		assertThat(CharSeq.all().size(), is(65536L));
	}

	@Test
	public void charsStartingAt() {
		assertThat(CharSeq.startingAt('A').limit(5), containsChars('A', 'B', 'C', 'D', 'E'));
		assertThat(CharSeq.startingAt('\u1400').limit(3).last(), is(OptionalChar.of('\u1402')));
		assertThat(CharSeq.startingAt(Character.MAX_VALUE), containsChars(Character.MAX_VALUE));
		assertThat(CharSeq.startingAt('\u8000').size(), is(32768L));
	}

	@Test
	public void charRange() {
		assertThat(CharSeq.range('A', 'F'), containsChars('A', 'B', 'C', 'D', 'E', 'F'));
		assertThat(CharSeq.range('F', 'A'), containsChars('F', 'E', 'D', 'C', 'B', 'A'));
		assertThat(CharSeq.range('A', 'F').size(), is(6L));
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
		twice(() -> assertThat(intSequence, containsInts(0, 1, 2, 3, 4)));
	}

	@Test
	public void toIntsMapped() {
		IntSequence emptyIntSequence = empty.toInts(c -> c + 1);
		twice(() -> assertThat(emptyIntSequence, is(emptyIterable())));

		IntSequence intSequence = CharSeq.all().limit(5).toInts(c -> c + 1);
		twice(() -> assertThat(intSequence, containsInts(1, 2, 3, 4, 5)));
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
		twice(() -> assertThat(repeatOne.limit(3), containsChars('a', 'a', 'a')));

		CharSeq repeatTwo = ab.repeat();
		twice(() -> assertThat(repeatTwo.limit(5), containsChars('a', 'b', 'a', 'b', 'a')));

		CharSeq repeatThree = abc.repeat();
		twice(() -> assertThat(repeatThree.limit(8), containsChars('a', 'b', 'c', 'a', 'b', 'c', 'a', 'b')));

		CharSeq repeatVarying = CharSeq.from(new CharIterable() {
			private List<Character> list = asList('a', 'b', 'c');
			int end = list.size();

			@Override
			public CharIterator iterator() {
				List<Character> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Character> iterator = subList.iterator();
				return new DelegatingCharIterator<Character, Iterator<Character>>(iterator) {
					@Override
					public char nextChar() {
						return iterator.next();
					}
				};
			}
		}).repeat();
		assertThat(repeatVarying, containsChars('a', 'b', 'c', 'a', 'b', 'a'));
	}

	@Test
	public void repeatTwice() {
		CharSeq repeatEmpty = empty.repeat(2);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		CharSeq repeatOne = a.repeat(2);
		twice(() -> assertThat(repeatOne, containsChars('a', 'a')));

		CharSeq repeatTwo = ab.repeat(2);
		twice(() -> assertThat(repeatTwo, containsChars('a', 'b', 'a', 'b')));

		CharSeq repeatThree = abc.repeat(2);
		twice(() -> assertThat(repeatThree, containsChars('a', 'b', 'c', 'a', 'b', 'c')));

		CharSeq repeatVarying = CharSeq.from(new CharIterable() {
			private List<Character> list = asList('a', 'b', 'c');
			int end = list.size();

			@Override
			public CharIterator iterator() {
				List<Character> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Character> iterator = subList.iterator();
				return new DelegatingCharIterator<Character, Iterator<Character>>(iterator) {
					@Override
					public char nextChar() {
						return iterator.next();
					}
				};
			}
		}).repeat(2);
		assertThat(repeatVarying, containsChars('a', 'b', 'c', 'a', 'b'));
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
		CharSeq sequence = CharSeq.generate(queue::poll);

		CharIterator iterator = sequence.iterator();
		assertThat(iterator.nextChar(), is('a'));
		assertThat(iterator.nextChar(), is('b'));
		assertThat(iterator.nextChar(), is('c'));
		assertThat(iterator.nextChar(), is('d'));
		assertThat(iterator.nextChar(), is('e'));
		expecting(NullPointerException.class, iterator::next);

		CharIterator iterator2 = sequence.iterator();
		expecting(NullPointerException.class, iterator2::next);
	}

	@Test
	public void multiGenerate() {
		CharSeq sequence = CharSeq.multiGenerate(() -> {
			Queue<Character> queue = new ArrayDeque<>(asList('a', 'b', 'c', 'd', 'e'));
			return queue::poll;
		});

		CharIterator iterator = sequence.iterator();
		assertThat(iterator.nextChar(), is('a'));
		assertThat(iterator.nextChar(), is('b'));
		assertThat(iterator.nextChar(), is('c'));
		assertThat(iterator.nextChar(), is('d'));
		assertThat(iterator.nextChar(), is('e'));
		expecting(NullPointerException.class, iterator::next);

		CharIterator iterator2 = sequence.iterator();
		assertThat(iterator2.nextChar(), is('a'));
		assertThat(iterator2.nextChar(), is('b'));
		assertThat(iterator2.nextChar(), is('c'));
		assertThat(iterator2.nextChar(), is('d'));
		assertThat(iterator2.nextChar(), is('e'));
		expecting(NullPointerException.class, iterator2::next);
	}

	@Test
	public void random() {
		CharSeq random = CharSeq.random('a', 'z');

		twice(() -> {
			CharIterator iterator = random.iterator();
			times(1000, () -> assertThat(iterator.nextChar(),
			                             is(both(greaterThanOrEqualTo('a')).and(lessThanOrEqualTo('z')))));
		});

		assertThat(random.limit(10), not(contains(random.limit(10))));
	}

	@Test
	public void randomWithSupplier() {
		CharSeq random = CharSeq.random(() -> new Random(17), 'a', 'e');

		twice(() -> assertThat(random.limit(10), containsChars('b', 'a', 'e', 'b', 'c', 'd', 'e', 'a', 'c', 'd')));
	}

	@Test
	public void randomRanges() {
		CharSeq random = CharSeq.random("0-9", "A-F");

		twice(() -> {
			CharIterator randomIterator = random.iterator();
			times(1000, () -> assertThat(Integer.parseInt(String.valueOf(randomIterator.nextChar()), 16),
			                             is(both(greaterThanOrEqualTo(0)).and(lessThan(16)))));
		});

		assertThat(random.limit(10), not(contains(random.limit(10))));
	}

	@Test
	public void randomRangesWithSupplier() {
		CharSeq random = CharSeq.random(() -> new Random(17), "0-9", "A-F");

		twice(() -> assertThat(random.limit(10), containsChars('B', 'C', 'B', '9', '1', '6', 'D', 'F', '0', 'D')));
	}

	@Test
	public void mapBack() {
		twice(() -> assertThat(abc.mapBack('_', (p, c) -> p), containsChars('_', 'a', 'b')));
		twice(() -> assertThat(abc.mapBack('_', (p, c) -> c), containsChars('a', 'b', 'c')));
	}

	@Test
	public void mapForward() {
		twice(() -> assertThat(abc.mapForward('_', (c, n) -> c), containsChars('a', 'b', 'c')));
		twice(() -> assertThat(abc.mapForward('_', (c, n) -> n), containsChars('b', 'c', '_')));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void window() {
		twice(() -> assertThat(empty.window(3), is(emptyIterable())));
		twice(() -> assertThat(a.window(3), contains(containsChars('a'))));
		twice(() -> assertThat(ab.window(3), contains(containsChars('a', 'b'))));
		twice(() -> assertThat(abc.window(3), contains(containsChars('a', 'b', 'c'))));
		twice(() -> assertThat(abcd.window(3), contains(containsChars('a', 'b', 'c'), containsChars('b', 'c', 'd'))));
		twice(() -> assertThat(abcde.window(3), contains(containsChars('a', 'b', 'c'), containsChars('b', 'c', 'd'),
		                                                 containsChars('c', 'd', 'e'))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithStep() {
		twice(() -> assertThat(empty.window(3, 2), is(emptyIterable())));
		twice(() -> assertThat(a.window(3, 2), contains(containsChars('a'))));
		twice(() -> assertThat(ab.window(3, 2), contains(containsChars('a', 'b'))));
		twice(() -> assertThat(abc.window(3, 2), contains(containsChars('a', 'b', 'c'))));
		twice(() -> assertThat(abcd.window(3, 2), contains(containsChars('a', 'b', 'c'), containsChars('c', 'd'))));
		twice(() -> assertThat(abcde.window(3, 2),
		                       contains(containsChars('a', 'b', 'c'), containsChars('c', 'd', 'e'))));
		twice(() -> assertThat(abcdefghi.window(3, 2),
		                       contains(containsChars('a', 'b', 'c'), containsChars('c', 'd', 'e'),
		                                containsChars('e', 'f', 'g'), containsChars('g', 'h', 'i'))));

		twice(() -> assertThat(empty.window(3, 4), is(emptyIterable())));
		twice(() -> assertThat(a.window(3, 4), contains(containsChars('a'))));
		twice(() -> assertThat(ab.window(3, 4), contains(containsChars('a', 'b'))));
		twice(() -> assertThat(abc.window(3, 4), contains(containsChars('a', 'b', 'c'))));
		twice(() -> assertThat(abcd.window(3, 4), contains(containsChars('a', 'b', 'c'))));
		twice(() -> assertThat(abcde.window(3, 4), contains(containsChars('a', 'b', 'c'), containsChars('e'))));
		twice(() -> assertThat(abcdefghi.window(3, 4),
		                       contains(containsChars('a', 'b', 'c'), containsChars('e', 'f', 'g'),
		                                containsChars('i'))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batch() {
		twice(() -> assertThat(empty.batch(3), is(emptyIterable())));
		twice(() -> assertThat(a.batch(3), contains(containsChars('a'))));
		twice(() -> assertThat(ab.batch(3), contains(containsChars('a', 'b'))));
		twice(() -> assertThat(abc.batch(3), contains(containsChars('a', 'b', 'c'))));
		twice(() -> assertThat(abcd.batch(3), contains(containsChars('a', 'b', 'c'), containsChars('d'))));
		twice(() -> assertThat(abcde.batch(3), contains(containsChars('a', 'b', 'c'), containsChars('d', 'e'))));
		twice(() -> assertThat(abcdefghi.batch(3), contains(containsChars('a', 'b', 'c'), containsChars('d', 'e', 'f'),
		                                                    containsChars('g', 'h', 'i'))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnPredicate() {
		Sequence<CharSeq> emptyPartitioned = empty.batch((a, b) -> a > b);
		twice(() -> assertThat(emptyPartitioned, is(emptyIterable())));

		Sequence<CharSeq> onePartitioned = a.batch((a, b) -> a > b);
		twice(() -> assertThat(onePartitioned, contains(containsChars('a'))));

		Sequence<CharSeq> twoPartitioned = ab.batch((a, b) -> a > b);
		twice(() -> assertThat(twoPartitioned, contains(containsChars('a', 'b'))));

		Sequence<CharSeq> threePartitioned = abc.batch((a, b) -> a > b);
		twice(() -> assertThat(threePartitioned, contains(containsChars('a', 'b', 'c'))));

		Sequence<CharSeq> threeRandomPartitioned = threeRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(threeRandomPartitioned, contains(containsChars('q', 'w'), containsChars('d'))));

		Sequence<CharSeq> nineRandomPartitioned = nineRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(nineRandomPartitioned,
		                       contains(containsChars('f', 'f'), containsChars('a', 'g'), containsChars('a', 'b', 'q'),
		                                containsChars('e'), containsChars('d'))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void split() {
		Sequence<CharSeq> emptySplit = empty.split('c');
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

		Sequence<CharSeq> oneSplit = a.split('c');
		twice(() -> assertThat(oneSplit, contains(containsChars('a'))));

		Sequence<CharSeq> twoSplit = ab.split('c');
		twice(() -> assertThat(twoSplit, contains(containsChars('a', 'b'))));

		Sequence<CharSeq> threeSplit = abc.split('c');
		twice(() -> assertThat(threeSplit, contains(containsChars('a', 'b'))));

		Sequence<CharSeq> fiveSplit = abcde.split('c');
		twice(() -> assertThat(fiveSplit, contains(containsChars('a', 'b'), containsChars('d', 'e'))));

		Sequence<CharSeq> nineSplit = abcdefghi.split('c');
		twice(() -> assertThat(nineSplit,
		                       contains(containsChars('a', 'b'), containsChars('d', 'e', 'f', 'g', 'h', 'i'))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitCharPredicate() {
		Sequence<CharSeq> emptySplit = empty.split(x -> (x - 0x60) % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

		Sequence<CharSeq> oneSplit = a.split(x -> (x - 0x60) % 3 == 0);
		twice(() -> assertThat(oneSplit, contains(containsChars('a'))));

		Sequence<CharSeq> twoSplit = ab.split(x -> (x - 0x60) % 3 == 0);
		twice(() -> assertThat(twoSplit, contains(containsChars('a', 'b'))));

		Sequence<CharSeq> threeSplit = abc.split(x -> (x - 0x60) % 3 == 0);
		twice(() -> assertThat(threeSplit, contains(containsChars('a', 'b'))));

		Sequence<CharSeq> fiveSplit = abcde.split(x -> (x - 0x60) % 3 == 0);
		twice(() -> assertThat(fiveSplit, contains(containsChars('a', 'b'), containsChars('d', 'e'))));

		Sequence<CharSeq> nineSplit = abcdefghi.split(x -> (x - 0x60) % 3 == 0);
		twice(() -> assertThat(nineSplit,
		                       contains(containsChars('a', 'b'), containsChars('d', 'e'), containsChars('g', 'h'))));
	}

	@Test
	public void removeAllAfterFilter() {
		List<Character> original = new ArrayList<>(asList('a', 'b', 'c', 'd'));

		CharSeq filtered = CharSeq.from(original).filter(x -> x == 'b');
		filtered.clear();

		twice(() -> assertThat(filtered, is(emptyIterable())));
		assertThat(original, contains('a', 'c', 'd'));
	}

	@Test
	public void isEmpty() {
		twice(() -> assertThat(empty.isEmpty(), is(true)));
		twice(() -> assertThat(a.isEmpty(), is(false)));
		twice(() -> assertThat(ab.isEmpty(), is(false)));
		twice(() -> assertThat(abcde.isEmpty(), is(false)));
	}

	@Test
	public void containsChar() {
		assertThat(empty.containsChar('q'), is(false));

		assertThat(abcde.containsChar('a'), is(true));
		assertThat(abcde.containsChar('c'), is(true));
		assertThat(abcde.containsChar('e'), is(true));
		assertThat(abcde.containsChar('q'), is(false));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(), is(true));
		assertThat(empty.containsAll('p', 'q', 'r'), is(false));

		assertThat(abcde.containsAll(), is(true));
		assertThat(abcde.containsAll('a'), is(true));
		assertThat(abcde.containsAll('a', 'c', 'e'), is(true));
		assertThat(abcde.containsAll('a', 'b', 'c', 'd', 'e'), is(true));
		assertThat(abcde.containsAll('a', 'b', 'c', 'd', 'e', 'p'), is(false));
		assertThat(abcde.containsAll('p', 'q', 'r'), is(false));
	}

	@Test
	public void containsAny() {
		assertThat(empty.containsAny(), is(false));
		assertThat(empty.containsAny('p', 'q', 'r'), is(false));

		assertThat(abcde.containsAny(), is(false));
		assertThat(abcde.containsAny('a'), is(true));
		assertThat(abcde.containsAny('a', 'c', 'e'), is(true));
		assertThat(abcde.containsAny('a', 'b', 'c', 'd', 'e'), is(true));
		assertThat(abcde.containsAny('a', 'b', 'c', 'd', 'e', 'p'), is(true));
		assertThat(abcde.containsAny('p', 'q', 'r'), is(false));
	}

	@FunctionalInterface
	private interface StrictCharIterable extends CharIterable {
		static CharIterable from(CharIterable iterable) {
			return () -> StrictCharIterator.from(iterable.iterator());
		}

		static CharIterable of(char... values) {
			return () -> StrictCharIterator.of(values);
		}
	}

	private interface StrictCharIterator extends CharIterator {
		static CharIterator from(CharIterator iterator) {
			return new CharIterator() {
				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public char nextChar() {
					return iterator.nextChar();
				}

				@Override
				public Character next() {
					throw new UnsupportedOperationException();
				}
			};
		}

		static CharIterator of(char... values) {
			return from(CharIterator.of(values));
		}
	}
}
