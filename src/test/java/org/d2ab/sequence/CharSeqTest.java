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

import org.d2ab.collection.Iterables;
import org.d2ab.collection.chars.*;
import org.d2ab.collection.ints.IntList;
import org.d2ab.function.CharBinaryOperator;
import org.d2ab.iterator.Iterators;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.chars.DelegatingTransformingCharIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.util.OptionalChar;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class CharSeqTest {
	private final CharSeq empty = CharSeq.empty();

	private final CharSeq a = CharSeq.from(CharList.create('a'));
	private final CharSeq ab = CharSeq.from(CharList.create('a', 'b'));
	private final CharSeq abc = CharSeq.from(CharList.create('a', 'b', 'c'));
	private final CharSeq abcd = CharSeq.from(CharList.create('a', 'b', 'c', 'd'));
	private final CharSeq abcde = CharSeq.from(CharList.create('a', 'b', 'c', 'd', 'e'));
	private final CharSeq abcdefghi = CharSeq.from(CharList.create('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'));

	private final CharSeq oneRandom = CharSeq.from(CharList.create('q'));
	private final CharSeq twoRandom = CharSeq.from(CharList.create('q', 'w'));
	private final CharSeq threeRandom = CharSeq.from(CharList.create('q', 'w', 'd'));
	private final CharSeq nineRandom = CharSeq.from(CharList.create('f', 'f', 'a', 'g', 'a', 'b', 'q', 'e', 'd'));

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
	public void fromArrayWithSize() {
		CharSeq sequence = CharSeq.from(new char[]{'a', 'b', 'c', 'd', 'e'}, 3);
		twice(() -> assertThat(sequence, containsChars('a', 'b', 'c')));
	}

	@Test
	public void fromArrayWithOffsetAndSize() {
		expecting(IndexOutOfBoundsException.class,
		          () -> CharSeq.from(new char[]{'a', 'b', 'c', 'd', 'e'}, -1, 3).iterator());
		expecting(IndexOutOfBoundsException.class,
		          () -> CharSeq.from(new char[]{'a', 'b', 'c', 'd', 'e'}, 6, 0).iterator());
		expecting(IndexOutOfBoundsException.class,
		          () -> CharSeq.from(new char[]{'a', 'b', 'c', 'd', 'e'}, 1, 5).iterator());
		expecting(IndexOutOfBoundsException.class,
		          () -> CharSeq.from(new char[]{'a', 'b', 'c', 'd', 'e'}, 1, -1).iterator());

		CharSeq sequence = CharSeq.from(new char[]{'a', 'b', 'c', 'd', 'e'}, 1, 3);
		twice(() -> assertThat(sequence, containsChars('b', 'c', 'd')));
	}

	@Test
	public void fromCharSequence() {
		CharSeq empty = CharSeq.from("");
		twice(() -> assertThat(empty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> empty.iterator().nextChar());

		CharSeq sequence = CharSeq.from("abcde");
		twice(() -> assertThat(sequence, containsChars('a', 'b', 'c', 'd', 'e')));
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
			AtomicInteger index = new AtomicInteger();
			a.forEachCharIndexed((e, i) -> {
				assertThat(e, is((char) value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(1));

			value.set('a');
			index.set(0);
			ab.forEachCharIndexed((e, i) -> {
				assertThat(e, is((char) value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(2));

			value.set('a');
			index.set(0);
			abcde.forEachCharIndexed((e, i) -> {
				assertThat(e, is((char) value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(5));
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
			expecting(NoSuchElementException.class, iterator::nextChar);
		});
	}

	@Test
	public void skip() {
		CharSeq threeSkipNone = abc.skip(0);
		twice(() -> assertThat(threeSkipNone, containsChars('a', 'b', 'c')));

		CharSeq threeSkipOne = abc.skip(1);
		twice(() -> assertThat(threeSkipOne, containsChars('b', 'c')));

		CharSeq threeSkipTwo = abc.skip(2);
		twice(() -> assertThat(threeSkipTwo, containsChars('c')));

		CharSeq threeSkipThree = abc.skip(3);
		twice(() -> assertThat(threeSkipThree, is(emptyIterable())));

		CharSeq threeSkipFour = abc.skip(4);
		twice(() -> assertThat(threeSkipFour, is(emptyIterable())));

		expecting(NoSuchElementException.class, () -> threeSkipFour.iterator().nextChar());

		assertThat(removeFirst(threeSkipNone), is('a'));
		twice(() -> assertThat(threeSkipNone, containsChars('b', 'c')));
		twice(() -> assertThat(abc, containsChars('b', 'c')));
	}

	@Test
	public void skipTail() {
		CharSeq threeSkipTailNone = abc.skipTail(0);
		twice(() -> assertThat(threeSkipTailNone, containsChars('a', 'b', 'c')));

		CharSeq threeSkipTailOne = abc.skipTail(1);
		twice(() -> assertThat(threeSkipTailOne, containsChars('a', 'b')));

		CharSeq threeSkipTailTwo = abc.skipTail(2);
		twice(() -> assertThat(threeSkipTailTwo, containsChars('a')));

		CharSeq threeSkipTailThree = abc.skipTail(3);
		twice(() -> assertThat(threeSkipTailThree, is(emptyIterable())));

		CharSeq threeSkipTailFour = abc.skipTail(4);
		twice(() -> assertThat(threeSkipTailFour, is(emptyIterable())));

		expecting(NoSuchElementException.class, () -> threeSkipTailFour.iterator().nextChar());

		assertThat(removeFirst(threeSkipTailNone), is('a'));
		twice(() -> assertThat(threeSkipTailNone, containsChars('b', 'c')));
		twice(() -> assertThat(abc, containsChars('b', 'c')));
	}

	@Test
	public void limit() {
		CharSeq threeLimitedToNone = abc.limit(0);
		twice(() -> assertThat(threeLimitedToNone, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> threeLimitedToNone.iterator().nextChar());

		CharSeq threeLimitedToOne = abc.limit(1);
		twice(() -> assertThat(threeLimitedToOne, containsChars('a')));

		CharSeq threeLimitedToTwo = abc.limit(2);
		twice(() -> assertThat(threeLimitedToTwo, containsChars('a', 'b')));

		CharSeq threeLimitedToThree = abc.limit(3);
		twice(() -> assertThat(threeLimitedToThree, containsChars('a', 'b', 'c')));

		CharSeq threeLimitedToFour = abc.limit(4);
		twice(() -> assertThat(threeLimitedToFour, containsChars('a', 'b', 'c')));

		assertThat(removeFirst(threeLimitedToFour), is('a'));
		twice(() -> assertThat(threeLimitedToFour, containsChars('b', 'c')));
		twice(() -> assertThat(abc, containsChars('b', 'c')));
	}

	@Test
	public void appendEmptyArray() {
		CharSeq appendedEmpty = empty.append();
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextChar());
	}

	@Test
	public void appendArray() {
		CharSeq appended = abc.append('d', 'e', 'f').append('g', 'h');

		twice(() -> assertThat(appended, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')));
	}

	@Test
	public void appendEmptyCharIterable() {
		CharSeq appendedEmpty = empty.append(CharIterable.of());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextChar());
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
	public void appendEmptyCharIterator() {
		CharSeq appendedEmpty = empty.append(CharIterator.of());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextChar());
	}

	@Test
	public void appendCharIterator() {
		CharSeq appended = abc.append(CharIterator.of('d', 'e', 'f')).append(CharIterator.of('g', 'h'));

		assertThat(appended, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
		assertThat(appended, containsChars('a', 'b', 'c'));
	}

	@Test
	public void appendCharIteratorAsIterator() {
		CharSeq appended = abc.append((Iterator<Character>) CharIterator.of('d', 'e', 'f'))
		                      .append((Iterator<Character>) CharIterator.of('g', 'h'));

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
	public void appendEmptyIntStream() {
		CharSeq appendedEmpty = empty.append(IntStream.of());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextChar());
	}

	@Test
	public void appendIntStream() {
		CharSeq appended = abc.append(IntStream.of('d', 'e', 'f')).append(IntStream.of('g', 'h'));

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
		assertThat(iterator.nextChar(), is('a'));
		assertThat(iterator.nextChar(), is('b'));
		assertThat(iterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, iterator::nextChar);

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		CharSeq emptyFiltered = empty.filter(c -> (c % 2) == 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().nextChar());

		CharSeq oneFiltered = a.filter(c -> (c % 2) == 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		CharSeq twoFiltered = ab.filter(c -> (c % 2) == 0);
		twice(() -> assertThat(twoFiltered, containsChars('b')));

		assertThat(removeFirst(twoFiltered), is('b'));
		twice(() -> assertThat(twoFiltered, is(emptyIterable())));
		twice(() -> assertThat(ab, containsChars('a')));

		CharSeq filtered = abcdefghi.filter(c -> (c % 2) == 0);
		twice(() -> assertThat(filtered, containsChars('b', 'd', 'f', 'h')));
	}

	@Test
	public void filterIndexed() {
		CharSeq emptyFiltered = empty.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().nextChar());

		CharSeq oneFiltered = a.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		CharSeq twoFiltered = ab.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(twoFiltered, containsChars('b')));

		assertThat(removeFirst(twoFiltered), is('b'));
		twice(() -> assertThat(twoFiltered, is(emptyIterable())));
		twice(() -> assertThat(ab, containsChars('a')));

		CharSeq filtered = abcdefghi.filterIndexed((i, x) -> x > 3);
		twice(() -> assertThat(filtered, containsChars('e', 'f', 'g', 'h', 'i')));
	}

	@Test
	public void filterBack() {
		CharSeq emptyFilteredLess = empty.filterBack('z', (p, x) -> p < x);
		twice(() -> assertThat(emptyFilteredLess, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().nextChar());

		CharSeq filteredLess = nineRandom.filterBack('z', (p, x) -> p < x);
		twice(() -> assertThat(filteredLess, containsChars('g', 'b', 'q')));

		CharSeq filteredGreater = nineRandom.filterBack('z', (p, x) -> p > x);
		twice(() -> assertThat(filteredGreater, containsChars('f', 'a', 'a', 'e', 'd')));

		assertThat(removeFirst(filteredGreater), is('f'));
		twice(() -> assertThat(filteredGreater, containsChars('f', 'a', 'a', 'e', 'd')));
		twice(() -> assertThat(nineRandom, containsChars('f', 'a', 'g', 'a', 'b', 'q', 'e', 'd')));
	}

	@Test
	public void filterForward() {
		CharSeq emptyFilteredLess = empty.filterForward('z', (x, n) -> x < n);
		twice(() -> assertThat(emptyFilteredLess, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().nextChar());

		CharSeq filteredLess = nineRandom.filterForward('z', (x, n) -> n < x);
		twice(() -> assertThat(filteredLess, containsChars('f', 'g', 'q', 'e')));

		CharSeq filteredGreater = nineRandom.filterForward('z', (x, n) -> n > x);
		twice(() -> assertThat(filteredGreater, containsChars('a', 'a', 'b', 'd')));

		expecting(UnsupportedOperationException.class, () -> removeFirst(filteredGreater));
		twice(() -> assertThat(filteredGreater, containsChars('a', 'a', 'b', 'd')));
	}

	@Test
	public void includingArray() {
		CharSeq emptyIncluding = empty.including('a', 'c', 'e', 'q');
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyIncluding.iterator().nextChar());

		CharSeq including = abcde.including('a', 'c', 'e', 'q');
		twice(() -> assertThat(including, containsChars('a', 'c', 'e')));

		CharSeq includingAll = abcde.including('a', 'b', 'c', 'd', 'e', 'q');
		twice(() -> assertThat(includingAll, containsChars('a', 'b', 'c', 'd', 'e')));

		CharSeq includingNone = abcde.including();
		twice(() -> assertThat(includingNone, is(emptyIterable())));

		assertThat(removeFirst(including), is('a'));
		twice(() -> assertThat(including, containsChars('c', 'e')));
		twice(() -> assertThat(abcde, containsChars('b', 'c', 'd', 'e')));
	}

	@Test
	public void excludingArray() {
		CharSeq emptyExcluding = empty.excluding('a', 'c', 'e', 'q');
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyExcluding.iterator().nextChar());

		CharSeq excluding = abcde.excluding('a', 'c', 'e', 'q');
		twice(() -> assertThat(excluding, containsChars('b', 'd')));

		CharSeq excludingAll = abcde.excluding('a', 'b', 'c', 'd', 'e', 'q');
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		CharSeq excludingNone = abcde.excluding();
		twice(() -> assertThat(excludingNone, containsChars('a', 'b', 'c', 'd', 'e')));

		assertThat(removeFirst(excluding), is('b'));
		twice(() -> assertThat(excluding, containsChars('d')));
		twice(() -> assertThat(abcde, containsChars('a', 'c', 'd', 'e')));
	}

	@Test
	public void map() {
		CharSeq emptyMapped = empty.map(x -> (char) (x + 1));
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().nextChar());

		CharSeq oneMapped = a.map(x -> (char) (x + 1));
		twice(() -> assertThat(oneMapped, containsChars('b')));

		assertThat(removeFirst(oneMapped), is('b'));
		twice(() -> assertThat(oneMapped, is(emptyIterable())));
		twice(() -> assertThat(a, is(emptyIterable())));

		CharSeq twoMapped = ab.map(x -> (char) (x + 1));
		twice(() -> assertThat(twoMapped, containsChars('b', 'c')));

		CharSeq fiveMapped = abcde.map(x -> (char) (x + 1));
		twice(() -> assertThat(fiveMapped, containsChars('b', 'c', 'd', 'e', 'f')));
	}

	@Test
	public void mapWithIndex() {
		CharSeq emptyMapped = empty.mapIndexed((x, i) -> (char) (x + i));
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().nextChar());

		CharSeq oneMapped = a.mapIndexed((x, i) -> (char) (x + i));
		twice(() -> assertThat(oneMapped, containsChars('a')));

		assertThat(removeFirst(oneMapped), is('a'));
		twice(() -> assertThat(oneMapped, is(emptyIterable())));
		twice(() -> assertThat(a, is(emptyIterable())));

		CharSeq twoMapped = ab.mapIndexed((x, i) -> (char) (x + i));
		twice(() -> assertThat(twoMapped, containsChars('a', 'c')));

		CharSeq mapped = abcde.mapIndexed((x, i) -> (char) (x + i));
		twice(() -> assertThat(mapped, containsChars('a', 'c', 'e', 'g', 'i')));
	}

	@Test
	public void recurse() {
		CharSeq recursive = CharSeq.recurse('a', x -> (char) (x + 1));
		twice(() -> assertThat(recursive.limit(10), containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j')));
	}

	@Test
	public void untilTerminal() {
		CharSeq emptyUntilG = empty.until('g');
		twice(() -> assertThat(emptyUntilG, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntilG.iterator().nextChar());

		CharSeq nineUntilG = abcdefghi.until('g');
		twice(() -> assertThat(nineUntilG, containsChars('a', 'b', 'c', 'd', 'e', 'f')));

		assertThat(removeFirst(nineUntilG), is('a'));
		twice(() -> assertThat(nineUntilG, containsChars('b', 'c', 'd', 'e', 'f')));
		twice(() -> assertThat(abcdefghi, containsChars('b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')));
	}

	@Test
	public void untilPredicate() {
		CharSeq emptyUntilEqualG = empty.until(x -> x == 'g');
		twice(() -> assertThat(emptyUntilEqualG, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntilEqualG.iterator().nextChar());

		CharSeq nineUntilEqualG = abcdefghi.until(x -> x == 'g');
		twice(() -> assertThat(nineUntilEqualG, containsChars('a', 'b', 'c', 'd', 'e', 'f')));

		assertThat(removeFirst(nineUntilEqualG), is('a'));
		twice(() -> assertThat(nineUntilEqualG, containsChars('b', 'c', 'd', 'e', 'f')));
		twice(() -> assertThat(abcdefghi, containsChars('b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')));
	}

	@Test
	public void endingAtTerminal() {
		CharSeq emptyEndingAtG = empty.endingAt('g');
		twice(() -> assertThat(emptyEndingAtG, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAtG.iterator().nextChar());

		CharSeq nineEndingAtG = abcdefghi.endingAt('g');
		twice(() -> assertThat(nineEndingAtG, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g')));

		assertThat(removeFirst(nineEndingAtG), is('a'));
		twice(() -> assertThat(nineEndingAtG, containsChars('b', 'c', 'd', 'e', 'f', 'g')));
		twice(() -> assertThat(abcdefghi, containsChars('b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')));
	}

	@Test
	public void endingAtPredicate() {
		CharSeq emptyEndingAtEqualG = empty.endingAt(x -> x == 'g');
		twice(() -> assertThat(emptyEndingAtEqualG, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAtEqualG.iterator().nextChar());

		CharSeq nineEndingAtEqualG = abcdefghi.endingAt(x -> x == 'g');
		twice(() -> assertThat(nineEndingAtEqualG, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g')));

		assertThat(removeFirst(nineEndingAtEqualG), is('a'));
		twice(() -> assertThat(nineEndingAtEqualG, containsChars('b', 'c', 'd', 'e', 'f', 'g')));
		twice(() -> assertThat(abcdefghi, containsChars('b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')));
	}

	@Test
	public void startingAfter() {
		CharSeq emptyStartingAfterE = empty.startingAfter('e');
		twice(() -> assertThat(emptyStartingAfterE, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingAfterE.iterator().nextChar());

		CharSeq nineStartingAfterE = abcdefghi.startingAfter('e');
		twice(() -> assertThat(nineStartingAfterE, containsChars('f', 'g', 'h', 'i')));

		assertThat(removeFirst(nineStartingAfterE), is('f'));
		twice(() -> assertThat(nineStartingAfterE, containsChars('g', 'h', 'i')));
		twice(() -> assertThat(abcdefghi, containsChars('a', 'b', 'c', 'd', 'e', 'g', 'h', 'i')));

		CharSeq fiveStartingAfterJ = abcde.startingAfter('j');
		twice(() -> assertThat(fiveStartingAfterJ, is(emptyIterable())));
	}

	@Test
	public void startingAfterPredicate() {
		CharSeq emptyStartingAfterEqualE = empty.startingAfter(x -> x == 'e');
		twice(() -> assertThat(emptyStartingAfterEqualE, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingAfterEqualE.iterator().nextChar());

		CharSeq nineStartingAfterEqualE = abcdefghi.startingAfter(x -> x == 'e');
		twice(() -> assertThat(nineStartingAfterEqualE, containsChars('f', 'g', 'h', 'i')));

		assertThat(removeFirst(nineStartingAfterEqualE), is('f'));
		twice(() -> assertThat(nineStartingAfterEqualE, containsChars('g', 'h', 'i')));
		twice(() -> assertThat(abcdefghi, containsChars('a', 'b', 'c', 'd', 'e', 'g', 'h', 'i')));

		CharSeq fiveStartingAfterEqualJ = abcde.startingAfter(x -> x == 'j');
		twice(() -> assertThat(fiveStartingAfterEqualJ, is(emptyIterable())));
	}

	@Test
	public void startingFrom() {
		CharSeq emptyStartingFromE = empty.startingFrom('e');
		twice(() -> assertThat(emptyStartingFromE, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingFromE.iterator().nextChar());

		CharSeq nineStartingFromE = abcdefghi.startingFrom('e');
		twice(() -> assertThat(nineStartingFromE, containsChars('e', 'f', 'g', 'h', 'i')));

		assertThat(removeFirst(nineStartingFromE), is('e'));
		twice(() -> assertThat(nineStartingFromE, is(emptyIterable())));
		twice(() -> assertThat(abcdefghi, containsChars('a', 'b', 'c', 'd', 'f', 'g', 'h', 'i')));

		CharSeq fiveStartingFromJ = abcde.startingFrom('j');
		twice(() -> assertThat(fiveStartingFromJ, is(emptyIterable())));
	}

	@Test
	public void startingFromPredicate() {
		CharSeq emptyStartingFromEqualE = empty.startingFrom(x -> x == 'e');
		twice(() -> assertThat(emptyStartingFromEqualE, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingFromEqualE.iterator().nextChar());

		CharSeq nineStartingFromEqualE = abcdefghi.startingFrom(x -> x == 'e');
		twice(() -> assertThat(nineStartingFromEqualE, containsChars('e', 'f', 'g', 'h', 'i')));

		assertThat(removeFirst(nineStartingFromEqualE), is('e'));
		twice(() -> assertThat(nineStartingFromEqualE, is(emptyIterable())));
		twice(() -> assertThat(abcdefghi, containsChars('a', 'b', 'c', 'd', 'f', 'g', 'h', 'i')));

		CharSeq fiveStartingFromEqualJ = abcde.startingFrom(x -> x == 'j');
		twice(() -> assertThat(fiveStartingFromEqualJ, is(emptyIterable())));
	}

	@Test
	public void toList() {
		twice(() -> {
			CharList list = abcde.toList();
			assertThat(list, is(instanceOf(ArrayCharList.class)));
			assertThat(list, containsChars('a', 'b', 'c', 'd', 'e'));
		});
	}

	@Test
	public void toSet() {
		twice(() -> {
			CharSet set = abcde.toSet();
			assertThat(set, instanceOf(BitCharSet.class));
			assertThat(set, containsChars('a', 'b', 'c', 'd', 'e'));
		});
	}

	@Test
	public void toSortedSet() {
		twice(() -> {
			CharSortedSet sortedSet = abcde.toSortedSet();
			assertThat(sortedSet, instanceOf(BitCharSet.class));
			assertThat(sortedSet, containsChars('a', 'b', 'c', 'd', 'e'));
		});
	}

	@Test
	public void toSetWithType() {
		twice(() -> {
			CharSet set = abcde.toSet(BitCharSet::new);
			assertThat(set, instanceOf(BitCharSet.class));
			assertThat(set, containsChars('a', 'b', 'c', 'd', 'e'));
		});
	}

	@Test
	public void toCollection() {
		twice(() -> {
			CharList list = abcde.toCollection(CharList::create);
			assertThat(list, instanceOf(ArrayCharList.class));
			assertThat(list, containsChars('a', 'b', 'c', 'd', 'e'));
		});
	}

	@Test
	public void collectIntoCharCollection() {
		twice(() -> {
			CharList list = CharList.create();
			CharList result = abcde.collectInto(list);

			assertThat(result, is(sameInstance(list)));
			assertThat(result, containsChars('a', 'b', 'c', 'd', 'e'));
		});
	}

	@Test
	public void collect() {
		twice(() -> {
			StringBuilder builder = abc.collect(StringBuilder::new, StringBuilder::append);
			assertThat(builder.toString(), is("abc"));
		});
	}

	@Test
	public void collectIntoContainer() {
		twice(() -> {
			CharList list = CharList.create();
			CharList result = abcde.collectInto(list, CharList::addChar);

			assertThat(result, is(sameInstance(list)));
			assertThat(result, containsChars('a', 'b', 'c', 'd', 'e'));
		});
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(Arrays.equals(abc.toCharArray(), new char[]{'a', 'b', 'c'}), is(true)));
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
		CharSeq emptyStep3 = empty.step(3);
		twice(() -> assertThat(emptyStep3, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStep3.iterator().nextChar());

		CharSeq nineStep3 = abcdefghi.step(3);
		twice(() -> assertThat(nineStep3, containsChars('a', 'd', 'g')));

		CharIterator nineStep3Iterator = nineStep3.iterator();
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.hasNext(), is(true));
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.nextChar(), is('a'));
		nineStep3Iterator.remove();
		assertThat(nineStep3Iterator.hasNext(), is(true));
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.nextChar(), is('d'));
		nineStep3Iterator.remove();

		twice(() -> assertThat(nineStep3, containsChars('b', 'f', 'i')));
		twice(() -> assertThat(abcdefghi, containsChars('b', 'c', 'e', 'f', 'g', 'h', 'i')));
	}

	@Test
	public void distinct() {
		CharSeq emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptyDistinct.iterator().nextChar());

		CharSeq oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, containsChars('q')));

		CharSeq twoDuplicatesDistinct = CharSeq.from(CharList.create('q', 'q')).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, containsChars('q')));

		CharSeq nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, containsChars('f', 'a', 'g', 'b', 'q', 'e', 'd')));

		assertThat(removeFirst(nineDistinct), is('f'));
		twice(() -> assertThat(nineDistinct, containsChars('f', 'a', 'g', 'b', 'q', 'e', 'd')));
		twice(() -> assertThat(nineRandom, containsChars('f', 'a', 'g', 'a', 'b', 'q', 'e', 'd')));
	}

	@Test
	public void sorted() {
		CharSeq emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptySorted.iterator().nextChar());

		CharSeq oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, containsChars('q')));

		CharSeq twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, containsChars('q', 'w')));

		CharSeq nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, containsChars('a', 'a', 'b', 'd', 'e', 'f', 'f', 'g', 'q')));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSorted));
		twice(() -> assertThat(nineSorted, containsChars('a', 'a', 'b', 'd', 'e', 'f', 'f', 'g', 'q')));
		twice(() -> assertThat(nineRandom, containsChars('f', 'f', 'a', 'g', 'a', 'b', 'q', 'e', 'd')));
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
		twice(() -> assertThat(empty.size(), is(0)));
		twice(() -> assertThat(a.size(), is(1)));
		twice(() -> assertThat(ab.size(), is(2)));
		twice(() -> assertThat(abcdefghi.size(), is(9)));
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
		CharSeq emptyPeeked = empty.peek(x -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyPeeked, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().nextChar());

		AtomicInteger value = new AtomicInteger('a');
		CharSeq onePeeked = a.peek(x -> assertThat(x, is((char) value.getAndIncrement())));
		twiceIndexed(value, 1, () -> assertThat(onePeeked, containsChars('a')));

		CharSeq twoPeeked = ab.peek(x -> assertThat(x, is((char) value.getAndIncrement())));
		twiceIndexed(value, 2, () -> assertThat(twoPeeked, containsChars('a', 'b')));

		CharSeq fivePeeked = abcde.peek(x -> assertThat(x, is((char) value.getAndIncrement())));
		twiceIndexed(value, 5, () -> assertThat(fivePeeked, containsChars('a', 'b', 'c', 'd', 'e')));

		assertThat(removeFirst(fivePeeked), is('a'));
		twiceIndexed(value, 4, () -> assertThat(fivePeeked, containsChars('b', 'c', 'd', 'e')));
		twice(() -> assertThat(abcde, containsChars('b', 'c', 'd', 'e')));
	}

	@Test
	public void peekIndexed() {
		CharSeq emptyPeeked = empty.peekIndexed((x, i) -> fail("Should not get called"));
		twice(() -> assertThat(emptyPeeked, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().nextChar());

		AtomicInteger index = new AtomicInteger();
		AtomicInteger value = new AtomicInteger('a');
		CharSeq onePeeked = a.peekIndexed((x, i) -> {
			assertThat(x, is((char) value.getAndIncrement()));
			assertThat(i, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(onePeeked, containsChars('a'));

			assertThat(index.get(), is(1));
			assertThat((char) value.get(), is('b'));
			index.set(0);
			value.set('a');
		});

		CharSeq twoPeeked = ab.peekIndexed((x, i) -> {
			assertThat(x, is((char) value.getAndIncrement()));
			assertThat(i, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(twoPeeked, containsChars('a', 'b'));

			assertThat(index.get(), is(2));
			assertThat((char) value.get(), is('c'));
			index.set(0);
			value.set('a');
		});

		CharSeq fivePeeked = abcde.peekIndexed((x, i) -> {
			assertThat(x, is((char) value.getAndIncrement()));
			assertThat(i, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(fivePeeked, containsChars('a', 'b', 'c', 'd', 'e'));

			assertThat(index.get(), is(5));
			assertThat((char) value.get(), is('f'));
			index.set(0);
			value.set('a');
		});

		assertThat(removeFirst(fivePeeked), is('a'));
		index.set(0);
		value.set('b');
		twice(() -> {
			assertThat(fivePeeked, containsChars('b', 'c', 'd', 'e'));
			assertThat(index.get(), is(4));
			assertThat((char) value.get(), is('f'));
			index.set(0);
			value.set('b');
		});

		twice(() -> assertThat(abcde, containsChars('b', 'c', 'd', 'e')));
	}

	@Test
	public void intStream() {
		twice(() -> assertThat(
				empty.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts).asChars(),
				is(emptyIterable())));

		twice(() -> assertThat(
				abcde.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts).asChars(),
				containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void intStreamFromOnce() {
		CharSeq empty = CharSeq.once(CharIterator.of());
		assertThat(empty.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts).asChars(),
		           is(emptyIterable()));
		assertThat(empty.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts).asChars(),
		           is(emptyIterable()));

		CharSeq sequence = CharSeq.once(CharIterator.of('a', 'b', 'c', 'd', 'e'));
		assertThat(sequence.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts).asChars(),
		           containsChars('a', 'b', 'c', 'd', 'e'));
		assertThat(sequence.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts).asChars(),
		           is(emptyIterable()));
	}

	@Test
	public void prefix() {
		CharSeq emptyPrefixed = empty.prefix('[');
		twice(() -> assertThat(emptyPrefixed, containsChars('[')));

		CharIterator emptyIterator = emptyPrefixed.iterator();
		emptyIterator.nextChar();
		expecting(NoSuchElementException.class, emptyIterator::nextChar);

		CharSeq threePrefixed = abc.prefix('[');
		twice(() -> assertThat(threePrefixed, containsChars('[', 'a', 'b', 'c')));

		CharIterator iterator = threePrefixed.iterator();
		expecting(UnsupportedOperationException.class, () -> {
			iterator.nextChar();
			iterator.remove();
		});
		assertThat(iterator.nextChar(), is('a'));
		iterator.remove();
		twice(() -> assertThat(threePrefixed, containsChars('[', 'b', 'c')));
		twice(() -> assertThat(abc, containsChars('b', 'c')));
	}

	@Test
	public void suffix() {
		CharSeq emptySuffixed = empty.suffix(']');
		twice(() -> assertThat(emptySuffixed, containsChars(']')));

		CharIterator emptyIterator = emptySuffixed.iterator();
		emptyIterator.nextChar();
		expecting(NoSuchElementException.class, emptyIterator::nextChar);

		CharSeq threeSuffixed = abc.suffix(']');
		twice(() -> assertThat(threeSuffixed, containsChars('a', 'b', 'c', ']')));

		assertThat(removeFirst(threeSuffixed), is('a'));
		twice(() -> assertThat(threeSuffixed, containsChars('b', 'c', ']')));
		twice(() -> assertThat(abc, containsChars('b', 'c')));
	}

	@Test
	public void interleave() {
		CharSeq emptyInterleaved = empty.interleave(empty);
		twice(() -> assertThat(emptyInterleaved, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyInterleaved.iterator().nextChar());

		CharSeq interleavedShortFirst = abc.interleave(abcde);
		twice(() -> assertThat(interleavedShortFirst, containsChars('a', 'a', 'b', 'b', 'c', 'c', 'd', 'e')));

		CharSeq interleavedShortLast = abcde.interleave(abc);
		twice(() -> assertThat(interleavedShortLast, containsChars('a', 'a', 'b', 'b', 'c', 'c', 'd', 'e')));

		expecting(UnsupportedOperationException.class, () -> removeFirst(interleavedShortFirst));
		twice(() -> assertThat(interleavedShortLast, containsChars('a', 'a', 'b', 'b', 'c', 'c', 'd', 'e')));
	}

	@Test
	public void reverse() {
		CharSeq emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyReversed.iterator().nextChar());

		CharSeq oneReversed = a.reverse();
		twice(() -> assertThat(oneReversed, containsChars('a')));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneReversed));
		twice(() -> assertThat(oneReversed, containsChars('a')));
		twice(() -> assertThat(a, containsChars('a')));

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
		assertThat(CharSeq.all().size(), is(65536));
	}

	@Test
	public void charsStartingAt() {
		assertThat(CharSeq.startingAt('A').limit(5), containsChars('A', 'B', 'C', 'D', 'E'));
		assertThat(CharSeq.startingAt('\u1400').limit(3).last(), is(OptionalChar.of('\u1402')));
		assertThat(CharSeq.startingAt(Character.MAX_VALUE), containsChars(Character.MAX_VALUE));
		assertThat(CharSeq.startingAt('\u8000').size(), is(32768));
	}

	@Test
	public void charRange() {
		assertThat(CharSeq.range('A', 'F'), containsChars('A', 'B', 'C', 'D', 'E', 'F'));
		assertThat(CharSeq.range('F', 'A'), containsChars('F', 'E', 'D', 'C', 'B', 'A'));
		assertThat(CharSeq.range('A', 'F').size(), is(6));
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
		IntSequence emptyInts = empty.toInts();
		twice(() -> assertThat(emptyInts, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyInts.iterator().nextInt());

		IntSequence intSequence = abcde.toInts();
		twice(() -> assertThat(intSequence, containsInts('a', 'b', 'c', 'd', 'e')));

		assertThat(removeFirst(intSequence), is((int) 'a'));
		twice(() -> assertThat(intSequence, containsInts('b', 'c', 'd', 'e')));
	}

	@Test
	public void toIntsMapped() {
		IntSequence emptyInts = empty.toInts(x -> x + 1);
		twice(() -> assertThat(emptyInts, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyInts.iterator().nextInt());

		IntSequence intSequence = abcde.toInts(x -> x + 1);
		twice(() -> assertThat(intSequence, containsInts('b', 'c', 'd', 'e', 'f')));

		assertThat(removeFirst(intSequence), is((int) 'b'));
		twice(() -> assertThat(intSequence, containsInts('c', 'd', 'e', 'f')));
	}

	@Test
	public void toSequence() {
		Sequence<Character> emptySequence = empty.toSequence(x -> (char) (x + 1));
		twice(() -> assertThat(emptySequence, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySequence.iterator().next());

		Sequence<Character> fiveSequence = abcde.toSequence(x -> (char) (x + 1));
		twice(() -> assertThat(fiveSequence, contains('b', 'c', 'd', 'e', 'f')));

		assertThat(removeFirst(fiveSequence), is('b'));
		twice(() -> assertThat(fiveSequence, contains('c', 'd', 'e', 'f')));
	}

	@Test
	public void box() {
		Sequence<Character> emptyBoxed = empty.box();
		twice(() -> assertThat(emptyBoxed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBoxed.iterator().next());

		Sequence<Character> fiveBoxed = abcde.box();
		twice(() -> assertThat(fiveBoxed, contains('a', 'b', 'c', 'd', 'e')));

		assertThat(removeFirst(fiveBoxed), is('a'));
		twice(() -> assertThat(fiveBoxed, contains('b', 'c', 'd', 'e')));
	}

	@Test
	public void repeat() {
		CharSeq emptyRepeated = empty.repeat();
		twice(() -> assertThat(emptyRepeated, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeated.iterator().nextChar());

		CharSeq oneRepeated = a.repeat();
		twice(() -> assertThat(oneRepeated.limit(3), containsChars('a', 'a', 'a')));

		CharSeq twoRepeated = ab.repeat();
		twice(() -> assertThat(twoRepeated.limit(5), containsChars('a', 'b', 'a', 'b', 'a')));

		CharSeq threeRepeated = abc.repeat();
		twice(() -> assertThat(threeRepeated.limit(8), containsChars('a', 'b', 'c', 'a', 'b', 'c', 'a', 'b')));

		assertThat(removeFirst(threeRepeated), is('a'));
		twice(() -> assertThat(threeRepeated.limit(6), containsChars('b', 'c', 'b', 'c', 'b', 'c')));
		twice(() -> assertThat(abc, containsChars('b', 'c')));

		CharSeq varyingLengthRepeated = CharSeq.from(new CharIterable() {
			private List<Character> list = asList('a', 'b', 'c');
			int end = list.size();

			@Override
			public CharIterator iterator() {
				List<Character> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Character> iterator = subList.iterator();
				return new DelegatingTransformingCharIterator<Character, Iterator<Character>>(iterator) {
					@Override
					public char nextChar() {
						return iterator.next();
					}
				};
			}
		}).repeat();
		assertThat(varyingLengthRepeated, containsChars('a', 'b', 'c', 'a', 'b', 'a'));
	}

	@Test
	public void repeatTwice() {
		CharSeq emptyRepeatedTwice = empty.repeat(2);
		twice(() -> assertThat(emptyRepeatedTwice, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeatedTwice.iterator().nextChar());

		CharSeq oneRepeatedTwice = a.repeat(2);
		twice(() -> assertThat(oneRepeatedTwice, containsChars('a', 'a')));

		CharSeq twoRepeatedTwice = ab.repeat(2);
		twice(() -> assertThat(twoRepeatedTwice, containsChars('a', 'b', 'a', 'b')));

		CharSeq threeRepeatedTwice = abc.repeat(2);
		twice(() -> assertThat(threeRepeatedTwice, containsChars('a', 'b', 'c', 'a', 'b', 'c')));

		assertThat(removeFirst(threeRepeatedTwice), is('a'));
		twice(() -> assertThat(threeRepeatedTwice, containsChars('b', 'c', 'b', 'c')));
		twice(() -> assertThat(abc, containsChars('b', 'c')));

		CharSeq varyingLengthRepeatedTwice = CharSeq.from(new CharIterable() {
			private List<Character> list = asList('a', 'b', 'c');
			int end = list.size();

			@Override
			public CharIterator iterator() {
				List<Character> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Character> iterator = subList.iterator();
				return new DelegatingTransformingCharIterator<Character, Iterator<Character>>(iterator) {
					@Override
					public char nextChar() {
						return iterator.next();
					}
				};
			}
		}).repeat(2);
		assertThat(varyingLengthRepeatedTwice, containsChars('a', 'b', 'c', 'a', 'b'));
	}

	@Test
	public void repeatZero() {
		CharSeq emptyRepeatedZero = empty.repeat(0);
		twice(() -> assertThat(emptyRepeatedZero, is(emptyIterable())));

		CharSeq oneRepeatedZero = a.repeat(0);
		twice(() -> assertThat(oneRepeatedZero, is(emptyIterable())));

		CharSeq twoRepeatedZero = ab.repeat(0);
		twice(() -> assertThat(twoRepeatedZero, is(emptyIterable())));

		CharSeq threeRepeatedZero = abc.repeat(0);
		twice(() -> assertThat(threeRepeatedZero, is(emptyIterable())));
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
		expecting(NullPointerException.class, iterator::nextChar);

		CharIterator iterator2 = sequence.iterator();
		expecting(NullPointerException.class, iterator2::nextChar);
	}

	@Test
	public void multiGenerate() {
		CharSeq sequence = CharSeq.multiGenerate(() -> {
			Queue<Character> queue = new ArrayDeque<>(asList('a', 'b', 'c', 'd', 'e'));
			return queue::poll;
		});

		twice(() -> {
			CharIterator iterator = sequence.iterator();
			assertThat(iterator.nextChar(), is('a'));
			assertThat(iterator.nextChar(), is('b'));
			assertThat(iterator.nextChar(), is('c'));
			assertThat(iterator.nextChar(), is('d'));
			assertThat(iterator.nextChar(), is('e'));
			expecting(NullPointerException.class, iterator::nextChar);
		});
	}

	@Test
	public void random() {
		CharSeq random = CharSeq.random('a', 'z');

		twice(() -> {
			CharIterator iterator = random.iterator();
			times(1000, () -> assertThat(iterator.nextChar(),
			                             is(both(greaterThanOrEqualTo('a')).and(lessThanOrEqualTo('z')))));
		});

		assertThat(random.limit(10), not(containsChars(random.limit(10))));
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

		assertThat(random.limit(10), not(containsChars(random.limit(10))));
	}

	@Test
	public void randomRangesWithSupplier() {
		CharSeq random = CharSeq.random(() -> new Random(17), "0-9", "A-F");

		twice(() -> assertThat(random.limit(10), containsChars('B', 'C', 'B', '9', '1', '6', 'D', 'F', '0', 'D')));
	}

	@Test
	public void randomRangesEmpty() {
		CharSeq random = CharSeq.random();

		twice(() -> assertThat(random, is(emptyIterable())));
	}

	@Test
	public void randomRangesEmptyWithSupplier() {
		CharSeq random = CharSeq.random(() -> new Random(17));

		twice(() -> assertThat(random, is(emptyIterable())));
	}

	@Test
	public void randomRangesWithSupplierInvalidState() {
		CharSeq invalidRandom = CharSeq.random(() -> new Random(17) {
			@Override
			public int nextInt(int bound) {
				return bound;
			}
		}, "0-9", "A-F");

		expecting(IllegalStateException.class, () -> invalidRandom.iterator().nextChar());
	}

	@Test
	public void mapBack() {
		CharSeq emptyMappedBack = empty.mapBack('_', (p, x) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedBack, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedBack.iterator().nextChar());

		CharSeq threeMappedBackToPrevious = abc.mapBack('_', (p, x) -> p);
		twice(() -> assertThat(threeMappedBackToPrevious, containsChars('_', 'a', 'b')));

		CharSeq threeMappedBackToCurrent = abc.mapBack('_', (p, x) -> x);
		twice(() -> assertThat(threeMappedBackToCurrent, containsChars('a', 'b', 'c')));

		assertThat(removeFirst(threeMappedBackToCurrent), is('a'));
		twice(() -> assertThat(threeMappedBackToCurrent, containsChars('b', 'c')));
		twice(() -> assertThat(abc, containsChars('b', 'c')));
	}

	@Test
	public void mapForward() {
		CharSeq emptyMappedForward = empty.mapForward('_', (x, n) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedForward, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedForward.iterator().nextChar());

		CharSeq threeMappedForwardToCurrent = abc.mapForward('_', (x, n) -> x);
		twice(() -> assertThat(threeMappedForwardToCurrent, containsChars('a', 'b', 'c')));

		expecting(UnsupportedOperationException.class, () -> removeFirst(threeMappedForwardToCurrent));
		twice(() -> assertThat(threeMappedForwardToCurrent, containsChars('a', 'b', 'c')));

		CharSeq threeMappedForwardToNext = abc.mapForward('_', (x, n) -> n);
		twice(() -> assertThat(threeMappedForwardToNext, containsChars('b', 'c', '_')));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void window() {
		Sequence<CharSeq> emptyWindowed = empty.window(3);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<CharSeq> oneWindowed = a.window(3);
		twice(() -> assertThat(oneWindowed, contains(containsChars('a'))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(containsChars('a'))));

		Sequence<CharSeq> twoWindowed = ab.window(3);
		twice(() -> assertThat(twoWindowed, contains(containsChars('a', 'b'))));

		Sequence<CharSeq> threeWindowed = abc.window(3);
		twice(() -> assertThat(threeWindowed, contains(containsChars('a', 'b', 'c'))));

		Sequence<CharSeq> fourWindowed = abcd.window(3);
		twice(() -> assertThat(fourWindowed, contains(containsChars('a', 'b', 'c'), containsChars('b', 'c', 'd'))));

		Sequence<CharSeq> fiveWindowed = abcde.window(3);
		twice(() -> assertThat(fiveWindowed, contains(containsChars('a', 'b', 'c'), containsChars('b', 'c', 'd'),
		                                              containsChars('c', 'd', 'e'))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithSmallerStep() {
		Sequence<CharSeq> emptyWindowed = empty.window(3, 2);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<CharSeq> oneWindowed = a.window(3, 2);
		twice(() -> assertThat(oneWindowed, contains(containsChars('a'))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(containsChars('a'))));

		Sequence<CharSeq> twoWindowed = ab.window(3, 2);
		twice(() -> assertThat(twoWindowed, contains(containsChars('a', 'b'))));

		Sequence<CharSeq> threeWindowed = abc.window(3, 2);
		twice(() -> assertThat(threeWindowed, contains(containsChars('a', 'b', 'c'))));

		Sequence<CharSeq> fourWindowed = abcd.window(3, 2);
		twice(() -> assertThat(fourWindowed, contains(containsChars('a', 'b', 'c'), containsChars('c', 'd'))));

		Sequence<CharSeq> fiveWindowed = abcde.window(3, 2);
		twice(() -> assertThat(fiveWindowed,
		                       contains(containsChars('a', 'b', 'c'), containsChars('c', 'd', 'e'))));

		Sequence<CharSeq> nineWindowed = abcdefghi.window(3, 2);
		twice(() -> assertThat(nineWindowed,
		                       contains(containsChars('a', 'b', 'c'), containsChars('c', 'd', 'e'),
		                                containsChars('e', 'f', 'g'), containsChars('g', 'h', 'i'))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithLargerStep() {
		Sequence<CharSeq> emptyWindowed = empty.window(3, 4);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<CharSeq> oneWindowed = a.window(3, 4);
		twice(() -> assertThat(oneWindowed, contains(containsChars('a'))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(containsChars('a'))));

		Sequence<CharSeq> twoWindowed = ab.window(3, 4);
		twice(() -> assertThat(twoWindowed, contains(containsChars('a', 'b'))));

		Sequence<CharSeq> threeWindowed = abc.window(3, 4);
		twice(() -> assertThat(threeWindowed, contains(containsChars('a', 'b', 'c'))));

		Sequence<CharSeq> fourWindowed = abcd.window(3, 4);
		twice(() -> assertThat(fourWindowed, contains(containsChars('a', 'b', 'c'))));

		Sequence<CharSeq> fiveWindowed = abcde.window(3, 4);
		twice(() -> assertThat(fiveWindowed, contains(containsChars('a', 'b', 'c'), containsChars('e'))));

		Sequence<CharSeq> nineWindowed = abcdefghi.window(3, 4);
		twice(() -> assertThat(nineWindowed,
		                       contains(containsChars('a', 'b', 'c'), containsChars('e', 'f', 'g'),
		                                containsChars('i'))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batch() {
		Sequence<CharSeq> emptyBatched = empty.batch(3);
		twice(() -> assertThat(emptyBatched, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<CharSeq> oneBatched = a.batch(3);
		twice(() -> assertThat(oneBatched, contains(containsChars('a'))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneBatched));
		twice(() -> assertThat(oneBatched, contains(containsChars('a'))));

		Sequence<CharSeq> twoBatched = ab.batch(3);
		twice(() -> assertThat(twoBatched, contains(containsChars('a', 'b'))));

		Sequence<CharSeq> threeBatched = abc.batch(3);
		twice(() -> assertThat(threeBatched, contains(containsChars('a', 'b', 'c'))));

		Sequence<CharSeq> fourBatched = abcd.batch(3);
		twice(() -> assertThat(fourBatched, contains(containsChars('a', 'b', 'c'), containsChars('d'))));

		Sequence<CharSeq> fiveBatched = abcde.batch(3);
		twice(() -> assertThat(fiveBatched, contains(containsChars('a', 'b', 'c'), containsChars('d', 'e'))));

		Sequence<CharSeq> nineBatched = abcdefghi.batch(3);
		twice(() -> assertThat(nineBatched, contains(containsChars('a', 'b', 'c'), containsChars('d', 'e', 'f'),
		                                             containsChars('g', 'h', 'i'))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnPredicate() {
		Sequence<CharSeq> emptyBatched = empty.batch((a, b) -> a > b);
		twice(() -> assertThat(emptyBatched, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<CharSeq> oneBatched = a.batch((a, b) -> a > b);
		twice(() -> assertThat(oneBatched, contains(containsChars('a'))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneBatched));
		twice(() -> assertThat(oneBatched, contains(containsChars('a'))));

		Sequence<CharSeq> twoBatched = ab.batch((a, b) -> a > b);
		twice(() -> assertThat(twoBatched, contains(containsChars('a', 'b'))));

		Sequence<CharSeq> threeBatched = abc.batch((a, b) -> a > b);
		twice(() -> assertThat(threeBatched, contains(containsChars('a', 'b', 'c'))));

		Sequence<CharSeq> threeRandomBatched = threeRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(threeRandomBatched, contains(containsChars('q', 'w'), containsChars('d'))));

		Sequence<CharSeq> nineRandomBatched = nineRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(nineRandomBatched,
		                       contains(containsChars('f', 'f'), containsChars('a', 'g'), containsChars('a', 'b', 'q'),
		                                containsChars('e'), containsChars('d'))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void split() {
		Sequence<CharSeq> emptySplit = empty.split('c');
		twice(() -> assertThat(emptySplit, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

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

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSplit));
		twice(() -> assertThat(nineSplit,
		                       contains(containsChars('a', 'b'), containsChars('d', 'e', 'f', 'g', 'h', 'i'))));
		twice(() -> assertThat(abcdefghi, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitCharPredicate() {
		Sequence<CharSeq> emptySplit = empty.split(x -> (x - 0x60) % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

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

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSplit));
		twice(() -> assertThat(nineSplit,
		                       contains(containsChars('a', 'b'), containsChars('d', 'e'), containsChars('g', 'h'))));
		twice(() -> assertThat(abcdefghi, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')));
	}

	@Test
	public void filterClear() {
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
	public void containsAllChars() {
		assertThat(empty.containsAllChars(), is(true));
		assertThat(empty.containsAllChars('p', 'q', 'r'), is(false));

		assertThat(abcde.containsAllChars(), is(true));
		assertThat(abcde.containsAllChars('a'), is(true));
		assertThat(abcde.containsAllChars('a', 'c', 'e'), is(true));
		assertThat(abcde.containsAllChars('a', 'b', 'c', 'd', 'e'), is(true));
		assertThat(abcde.containsAllChars('a', 'b', 'c', 'd', 'e', 'p'), is(false));
		assertThat(abcde.containsAllChars('p', 'q', 'r'), is(false));
	}

	@Test
	public void containsAnyChars() {
		assertThat(empty.containsAnyChars(), is(false));
		assertThat(empty.containsAnyChars('p', 'q', 'r'), is(false));

		assertThat(abcde.containsAnyChars(), is(false));
		assertThat(abcde.containsAnyChars('a'), is(true));
		assertThat(abcde.containsAnyChars('a', 'c', 'e'), is(true));
		assertThat(abcde.containsAnyChars('a', 'b', 'c', 'd', 'e'), is(true));
		assertThat(abcde.containsAnyChars('a', 'b', 'c', 'd', 'e', 'p'), is(true));
		assertThat(abcde.containsAnyChars('p', 'q', 'r'), is(false));
	}
}
