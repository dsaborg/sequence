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
import org.d2ab.collection.ints.*;
import org.d2ab.iterator.Iterators;
import org.d2ab.iterator.ints.DelegatingTransformingIntIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class IntSequenceTest {
	private final IntSequence empty = IntSequence.empty();

	private final IntSequence _1 = IntSequence.from(IntList.create(1));
	private final IntSequence _12 = IntSequence.from(IntList.create(1, 2));
	private final IntSequence _123 = IntSequence.from(IntList.create(1, 2, 3));
	private final IntSequence _1234 = IntSequence.from(IntList.create(1, 2, 3, 4));
	private final IntSequence _12345 = IntSequence.from(IntList.create(1, 2, 3, 4, 5));
	private final IntSequence _123456789 = IntSequence.from(IntList.create(1, 2, 3, 4, 5, 6, 7, 8, 9));

	private final IntSequence oneRandom = IntSequence.from(IntList.create(17));
	private final IntSequence twoRandom = IntSequence.from(IntList.create(17, 32));
	private final IntSequence threeRandom = IntSequence.from(IntList.create(17, 32, 12));
	private final IntSequence nineRandom = IntSequence.from(IntList.create(6, 6, 1, -7, 1, 2, 17, 5, 4));

	@Test
	public void ofOne() {
		twice(() -> assertThat(_1, containsInts(1)));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(_123, containsInts(1, 2, 3)));
	}

	@Test
	public void fromArrayWithSize() {
		IntSequence sequence = IntSequence.from(new int[]{1, 2, 3, 4, 5}, 3);
		twice(() -> assertThat(sequence, containsInts(1, 2, 3)));
	}

	@Test
	public void fromArrayWithOffsetAndSize() {
		expecting(IndexOutOfBoundsException.class,
		          () -> IntSequence.from(new int[]{1, 2, 3, 4, 5}, -1, 3).iterator());
		expecting(IndexOutOfBoundsException.class,
		          () -> IntSequence.from(new int[]{1, 2, 3, 4, 5}, 6, 0).iterator());
		expecting(IndexOutOfBoundsException.class,
		          () -> IntSequence.from(new int[]{1, 2, 3, 4, 5}, 1, 5).iterator());
		expecting(IndexOutOfBoundsException.class,
		          () -> IntSequence.from(new int[]{1, 2, 3, 4, 5}, 1, -1).iterator());

		IntSequence sequence = IntSequence.from(new int[]{1, 2, 3, 4, 5}, 1, 3);
		twice(() -> assertThat(sequence, containsInts(2, 3, 4)));
	}

	@Test
	public void ofNone() {
		IntSequence sequence = IntSequence.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void fromIntIterable() {
		IntSequence sequence = IntSequence.of(1, 2, 3, 4, 5);

		twice(() -> assertThat(sequence, containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void fromIterable() {
		IntSequence sequence = IntSequence.from(Iterables.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(sequence, containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void oncePrimitiveIteratorOfInt() {
		IntSequence sequence = IntSequence.once(IntIterator.of(1, 2, 3, 4, 5));

		assertThat(sequence, containsInts(1, 2, 3, 4, 5));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceIterator() {
		IntSequence sequence = IntSequence.once(Iterators.of(1, 2, 3, 4, 5));

		assertThat(sequence, containsInts(1, 2, 3, 4, 5));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceIntStream() {
		IntSequence sequence = IntSequence.once(IntStream.of(1, 2, 3, 4, 5));

		assertThat(sequence, containsInts(1, 2, 3, 4, 5));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceStream() {
		IntSequence sequence = IntSequence.once(Stream.of(1, 2, 3, 4, 5));

		assertThat(sequence, containsInts(1, 2, 3, 4, 5));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void fromEmptyStream() {
		IntSequence sequence = IntSequence.once(Stream.of());

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void read() {
		InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5});

		IntSequence seq = IntSequence.read(inputStream);
		twice(() -> assertThat(seq, containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void readNegatives() {
		InputStream inputStream = new ByteArrayInputStream(new byte[]{-1, -2, -3, -4, -5});

		IntSequence seq = IntSequence.read(inputStream);
		twice(() -> assertThat(seq, containsInts(255, 254, 253, 252, 251)));
	}

	@Test
	public void readAlreadyBegun() throws IOException {
		InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5});
		assertThat(inputStream.read(), is(1));

		IntSequence seq = IntSequence.read(inputStream);
		assertThat(seq, containsInts(2, 3, 4, 5));
		assertThat(seq, containsInts(1, 2, 3, 4, 5));
	}

	@Test
	public void readWithMark() throws IOException {
		InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5});
		assertThat(inputStream.read(), is(1));
		inputStream.mark(0);

		IntSequence seq = IntSequence.read(inputStream);
		assertThat(seq, containsInts(2, 3, 4, 5));
		assertThat(seq, containsInts(2, 3, 4, 5));
	}

	@Test
	public void cachePrimitiveIteratorOfInt() {
		IntSequence cached = IntSequence.cache(IntIterator.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(cached, containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void cacheIterator() {
		IntSequence cached = IntSequence.cache(Iterators.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(cached, containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void cacheIntIterable() {
		IntSequence cached = IntSequence.cache(IntIterable.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(cached, containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void cacheIterable() {
		IntSequence cached = IntSequence.cache(Iterables.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(cached, containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void cacheIntStream() {
		IntSequence cached = IntSequence.cache(IntStream.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(cached, containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void cacheStream() {
		IntSequence cached = IntSequence.cache(Stream.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(cached, containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void forEachInt() {
		twice(() -> {
			empty.forEachInt(i -> fail("Should not get called"));

			AtomicInteger value = new AtomicInteger(1);
			_1.forEachInt(i -> assertThat(i, is(value.getAndIncrement())));

			value.set(1);
			_12.forEachInt(i -> assertThat(i, is(value.getAndIncrement())));

			value.set(1);
			_123.forEachInt(i -> assertThat(i, is(value.getAndIncrement())));
		});
	}

	@Test
	public void forEachIntIndexed() {
		twice(() -> {
			empty.forEachIntIndexed((e, i) -> fail("Should not get called"));

			AtomicInteger value = new AtomicInteger(1);
			AtomicInteger index = new AtomicInteger();
			_1.forEachIntIndexed((e, i) -> {
				assertThat(e, is(value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(1));

			value.set(1);
			index.set(0);
			_12.forEachIntIndexed((e, i) -> {
				assertThat(e, is(value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(2));

			value.set(1);
			index.set(0);
			_12345.forEachIntIndexed((e, i) -> {
				assertThat(e, is(value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(5));
		});
	}

	@Test
	public void iterator() {
		twice(() -> {
			IntIterator iterator = _123.iterator();

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.nextInt(), is(1));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.nextInt(), is(2));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.nextInt(), is(3));

			assertThat(iterator.hasNext(), is(false));
			assertThat(iterator.hasNext(), is(false));
		});
	}

	@Test
	public void skip() {
		IntSequence threeSkipNone = _123.skip(0);
		twice(() -> assertThat(threeSkipNone, containsInts(1, 2, 3)));

		IntSequence threeSkipOne = _123.skip(1);
		twice(() -> assertThat(threeSkipOne, containsInts(2, 3)));

		IntSequence threeSkipTwo = _123.skip(2);
		twice(() -> assertThat(threeSkipTwo, containsInts(3)));

		IntSequence threeSkipThree = _123.skip(3);
		twice(() -> assertThat(threeSkipThree, is(emptyIterable())));

		IntSequence threeSkipFour = _123.skip(4);
		twice(() -> assertThat(threeSkipFour, is(emptyIterable())));

		expecting(NoSuchElementException.class, () -> threeSkipFour.iterator().nextInt());

		assertThat(removeFirst(threeSkipNone), is(1));
		twice(() -> assertThat(threeSkipNone, containsInts(2, 3)));
		twice(() -> assertThat(_123, containsInts(2, 3)));
	}

	@Test
	public void skipTail() {
		IntSequence threeSkipTailNone = _123.skipTail(0);
		twice(() -> assertThat(threeSkipTailNone, containsInts(1, 2, 3)));

		IntSequence threeSkipTailOne = _123.skipTail(1);
		twice(() -> assertThat(threeSkipTailOne, containsInts(1, 2)));

		IntSequence threeSkipTailTwo = _123.skipTail(2);
		twice(() -> assertThat(threeSkipTailTwo, containsInts(1)));

		IntSequence threeSkipTailThree = _123.skipTail(3);
		twice(() -> assertThat(threeSkipTailThree, is(emptyIterable())));

		IntSequence threeSkipTailFour = _123.skipTail(4);
		twice(() -> assertThat(threeSkipTailFour, is(emptyIterable())));

		expecting(NoSuchElementException.class, () -> threeSkipTailFour.iterator().nextInt());

		assertThat(removeFirst(threeSkipTailNone), is(1));
		twice(() -> assertThat(threeSkipTailNone, containsInts(2, 3)));
		twice(() -> assertThat(_123, containsInts(2, 3)));
	}

	@Test
	public void limit() {
		IntSequence threeLimitedToNone = _123.limit(0);
		twice(() -> assertThat(threeLimitedToNone, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> threeLimitedToNone.iterator().nextInt());

		IntSequence threeLimitedToOne = _123.limit(1);
		twice(() -> assertThat(threeLimitedToOne, containsInts(1)));

		IntSequence threeLimitedToTwo = _123.limit(2);
		twice(() -> assertThat(threeLimitedToTwo, containsInts(1, 2)));

		IntSequence threeLimitedToThree = _123.limit(3);
		twice(() -> assertThat(threeLimitedToThree, containsInts(1, 2, 3)));

		IntSequence threeLimitedToFour = _123.limit(4);
		twice(() -> assertThat(threeLimitedToFour, containsInts(1, 2, 3)));

		assertThat(removeFirst(threeLimitedToFour), is(1));
		twice(() -> assertThat(threeLimitedToFour, containsInts(2, 3)));
		twice(() -> assertThat(_123, containsInts(2, 3)));
	}

	@Test
	public void appendEmptyArray() {
		IntSequence appendedEmpty = empty.append();
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextInt());
	}

	@Test
	public void appendArray() {
		IntSequence appended = _123.append(4, 5, 6).append(7, 8);

		twice(() -> assertThat(appended, containsInts(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendEmptyIntIterable() {
		IntSequence appendedEmpty = empty.append(IntIterable.of());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextInt());
	}

	@Test
	public void appendIntIterable() {
		IntSequence appended = _123.append(IntIterable.of(4, 5, 6)).append(IntIterable.of(7, 8));

		twice(() -> assertThat(appended, containsInts(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIterable() {
		IntSequence appended = _123.append(Iterables.of(4, 5, 6)).append(Iterables.of(7, 8));

		twice(() -> assertThat(appended, containsInts(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendEmptyIntIterator() {
		IntSequence appendedEmpty = empty.append(IntIterator.of());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextInt());
	}

	@Test
	public void appendIntIterator() {
		IntSequence appended = _123.append(IntIterator.of(4, 5, 6)).append(IntIterator.of(7, 8));

		assertThat(appended, containsInts(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, containsInts(1, 2, 3));
	}

	@Test
	public void appendIntIteratorAsIterator() {
		IntSequence appended = _123.append((Iterator<Integer>) IntIterator.of(4, 5, 6))
		                           .append((Iterator<Integer>) IntIterator.of(7, 8));

		assertThat(appended, containsInts(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, containsInts(1, 2, 3));
	}

	@Test
	public void appendIterator() {
		IntSequence appended = _123.append(Iterators.of(4, 5, 6)).append(Iterators.of(7, 8));

		assertThat(appended, containsInts(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, containsInts(1, 2, 3));
	}

	@Test
	public void appendEmptyIntStream() {
		IntSequence appendedEmpty = empty.append(IntStream.of());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextInt());
	}

	@Test
	public void appendIntStream() {
		IntSequence appended = _123.append(IntStream.of(4, 5, 6)).append(IntStream.of(7, 8));

		assertThat(appended, containsInts(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, containsInts(1, 2, 3));
	}

	@Test
	public void appendStream() {
		IntSequence appended = _123.append(Stream.of(4, 5, 6)).append(Stream.of(7, 8));

		assertThat(appended, containsInts(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, containsInts(1, 2, 3));
	}

	@Test
	public void appendIsLazy() {
		IntIterator first = IntIterator.of(1, 2, 3);
		IntIterator second = IntIterator.of(4, 5, 6);
		IntIterator third = IntIterator.of(7, 8);

		IntSequence.once(first).append(second).append(third);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));
		assertThat(third.hasNext(), is(true));
	}

	@Test
	public void appendIsLazyWhenSkippingHasNext() {
		IntIterator first = IntIterator.of(1);
		IntIterator second = IntIterator.of(2);

		IntSequence sequence = IntSequence.once(first).append(second);

		// check delayed iteration
		IntIterator iterator = sequence.iterator();
		assertThat(iterator.nextInt(), is(1));
		assertThat(iterator.nextInt(), is(2));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		IntSequence emptyFiltered = empty.filter(i -> (i % 2) == 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().nextInt());

		IntSequence oneFiltered = _1.filter(i -> (i % 2) == 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		IntSequence twoFiltered = _12.filter(i -> (i % 2) == 0);
		twice(() -> assertThat(twoFiltered, containsInts(2)));

		assertThat(removeFirst(twoFiltered), is(2));
		twice(() -> assertThat(twoFiltered, is(emptyIterable())));
		twice(() -> assertThat(_12, containsInts(1)));

		IntSequence filtered = _123456789.filter(i -> (i % 2) == 0);
		twice(() -> assertThat(filtered, containsInts(2, 4, 6, 8)));
	}

	@Test
	public void filterIndexed() {
		IntSequence emptyFiltered = empty.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().nextInt());

		IntSequence oneFiltered = _1.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		IntSequence twoFiltered = _12.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(twoFiltered, containsInts(2)));

		assertThat(removeFirst(twoFiltered), is(2));
		twice(() -> assertThat(twoFiltered, is(emptyIterable())));
		twice(() -> assertThat(_12, containsInts(1)));

		IntSequence filtered = _123456789.filterIndexed((i, x) -> x > 3);
		twice(() -> assertThat(filtered, containsInts(5, 6, 7, 8, 9)));
	}

	@Test
	public void filterBack() {
		IntSequence emptyFilteredLess = empty.filterBack(117, (p, i) -> p < i);
		twice(() -> assertThat(emptyFilteredLess, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().nextInt());

		IntSequence filteredLess = nineRandom.filterBack(117, (p, i) -> p < i);
		twice(() -> assertThat(filteredLess, containsInts(1, 2, 17)));

		IntSequence filteredGreater = nineRandom.filterBack(117, (p, i) -> p > i);
		twice(() -> assertThat(filteredGreater, containsInts(6, 1, -7, 5, 4)));

		assertThat(removeFirst(filteredGreater), is(6));
		twice(() -> assertThat(filteredGreater, containsInts(6, 1, -7, 5, 4)));
		twice(() -> assertThat(nineRandom, containsInts(6, 1, -7, 1, 2, 17, 5, 4)));
	}

	@Test
	public void filterForward() {
		IntSequence emptyFilteredLess = empty.filterForward(117, (i, n) -> i < n);
		twice(() -> assertThat(emptyFilteredLess, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().nextInt());

		IntSequence filteredLess = nineRandom.filterForward(117, (i, n) -> n < i);
		twice(() -> assertThat(filteredLess, containsInts(6, 1, 17, 5)));

		IntSequence filteredGreater = nineRandom.filterForward(117, (i, n) -> n > i);
		twice(() -> assertThat(filteredGreater, containsInts(-7, 1, 2, 4)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(filteredGreater));
		twice(() -> assertThat(filteredGreater, containsInts(-7, 1, 2, 4)));
	}

	@Test
	public void includingArray() {
		IntSequence emptyIncluding = empty.including(1, 3, 5, 17);
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyIncluding.iterator().nextInt());

		IntSequence including = _12345.including(1, 3, 5, 17);
		twice(() -> assertThat(including, containsInts(1, 3, 5)));

		IntSequence includingAll = _12345.including(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(includingAll, containsInts(1, 2, 3, 4, 5)));

		IntSequence includingNone = _12345.including();
		twice(() -> assertThat(includingNone, is(emptyIterable())));

		assertThat(removeFirst(including), is(1));
		twice(() -> assertThat(including, containsInts(3, 5)));
		twice(() -> assertThat(_12345, containsInts(2, 3, 4, 5)));
	}

	@Test
	public void excludingArray() {
		IntSequence emptyExcluding = empty.excluding(1, 3, 5, 17);
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyExcluding.iterator().nextInt());

		IntSequence excluding = _12345.excluding(1, 3, 5, 17);
		twice(() -> assertThat(excluding, containsInts(2, 4)));

		IntSequence excludingAll = _12345.excluding(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		IntSequence excludingNone = _12345.excluding();
		twice(() -> assertThat(excludingNone, containsInts(1, 2, 3, 4, 5)));

		assertThat(removeFirst(excluding), is(2));
		twice(() -> assertThat(excluding, containsInts(4)));
		twice(() -> assertThat(_12345, containsInts(1, 3, 4, 5)));
	}

	@Test
	public void map() {
		IntSequence emptyMapped = empty.map(i -> i + 1);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().nextInt());

		IntSequence oneMapped = _1.map(i -> i + 1);
		twice(() -> assertThat(oneMapped, containsInts(2)));

		assertThat(removeFirst(oneMapped), is(2));
		twice(() -> assertThat(oneMapped, is(emptyIterable())));
		twice(() -> assertThat(_1, is(emptyIterable())));

		IntSequence twoMapped = _12.map(i -> i + 1);
		twice(() -> assertThat(twoMapped, containsInts(2, 3)));

		IntSequence mapped = _12345.map(i -> i + 1);
		twice(() -> assertThat(mapped, containsInts(2, 3, 4, 5, 6)));
	}

	@Test
	public void mapWithIndex() {
		IntSequence emptyMapped = empty.mapIndexed((i, x) -> i + x);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().nextInt());

		IntSequence oneMapped = _1.mapIndexed((i, x) -> i + x);
		twice(() -> assertThat(oneMapped, containsInts(1)));

		assertThat(removeFirst(oneMapped), is(1));
		twice(() -> assertThat(oneMapped, is(emptyIterable())));

		IntSequence twoMapped = _12.mapIndexed((i, x) -> i + x);
		twice(() -> assertThat(twoMapped, containsInts(1, 3)));

		IntSequence mapped = _12345.mapIndexed((i, x) -> i + x);
		twice(() -> assertThat(mapped, containsInts(1, 3, 5, 7, 9)));
	}

	@Test
	public void recurse() {
		IntSequence recursive = IntSequence.recurse(1, i -> i + 1);
		twice(() -> assertThat(recursive.limit(10), containsInts(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void untilTerminal() {
		IntSequence emptyUntil7 = empty.until(7);
		twice(() -> assertThat(emptyUntil7, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntil7.iterator().nextInt());

		IntSequence nineUntil7 = _123456789.until(7);
		twice(() -> assertThat(nineUntil7, containsInts(1, 2, 3, 4, 5, 6)));

		assertThat(removeFirst(nineUntil7), is(1));
		twice(() -> assertThat(nineUntil7, containsInts(2, 3, 4, 5, 6)));
		twice(() -> assertThat(_123456789, containsInts(2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void untilPredicate() {
		IntSequence emptyUntilEqual7 = empty.until(i -> i == 7);
		twice(() -> assertThat(emptyUntilEqual7, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntilEqual7.iterator().nextInt());

		IntSequence nineUntilEqual7 = _123456789.until(i -> i == 7);
		twice(() -> assertThat(nineUntilEqual7, containsInts(1, 2, 3, 4, 5, 6)));

		assertThat(removeFirst(nineUntilEqual7), is(1));
		twice(() -> assertThat(nineUntilEqual7, containsInts(2, 3, 4, 5, 6)));
		twice(() -> assertThat(_123456789, containsInts(2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void endingAtTerminal() {
		IntSequence emptyEndingAt7 = empty.endingAt(7);
		twice(() -> assertThat(emptyEndingAt7, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAt7.iterator().nextInt());

		IntSequence nineEndingAt7 = _123456789.endingAt(7);
		twice(() -> assertThat(nineEndingAt7, containsInts(1, 2, 3, 4, 5, 6, 7)));

		assertThat(removeFirst(nineEndingAt7), is(1));
		twice(() -> assertThat(nineEndingAt7, containsInts(2, 3, 4, 5, 6, 7)));
		twice(() -> assertThat(_123456789, containsInts(2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void endingAtPredicate() {
		IntSequence emptyEndingAtEqual7 = empty.endingAt(i -> i == 7);
		twice(() -> assertThat(emptyEndingAtEqual7, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAtEqual7.iterator().nextInt());

		IntSequence nineEndingAtEqual7 = _123456789.endingAt(i -> i == 7);
		twice(() -> assertThat(nineEndingAtEqual7, containsInts(1, 2, 3, 4, 5, 6, 7)));

		assertThat(removeFirst(nineEndingAtEqual7), is(1));
		twice(() -> assertThat(nineEndingAtEqual7, containsInts(2, 3, 4, 5, 6, 7)));
		twice(() -> assertThat(_123456789, containsInts(2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void startingAfter() {
		IntSequence emptyStartingAfter5 = empty.startingAfter(5);
		twice(() -> assertThat(emptyStartingAfter5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingAfter5.iterator().nextInt());

		IntSequence nineStartingAfter5 = _123456789.startingAfter(5);
		twice(() -> assertThat(nineStartingAfter5, containsInts(6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingAfter5), is(6));
		twice(() -> assertThat(nineStartingAfter5, containsInts(7, 8, 9)));
		twice(() -> assertThat(_123456789, containsInts(1, 2, 3, 4, 5, 7, 8, 9)));

		IntSequence fiveStartingAfter10 = _12345.startingAfter(10);
		twice(() -> assertThat(fiveStartingAfter10, is(emptyIterable())));
	}

	@Test
	public void startingAfterPredicate() {
		IntSequence emptyStartingAfterEqual5 = empty.startingAfter(i -> i == 5);
		twice(() -> assertThat(emptyStartingAfterEqual5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingAfterEqual5.iterator().nextInt());

		IntSequence nineStartingAfterEqual5 = _123456789.startingAfter(i -> i == 5);
		twice(() -> assertThat(nineStartingAfterEqual5, containsInts(6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingAfterEqual5), is(6));
		twice(() -> assertThat(nineStartingAfterEqual5, containsInts(7, 8, 9)));
		twice(() -> assertThat(_123456789, containsInts(1, 2, 3, 4, 5, 7, 8, 9)));

		IntSequence fiveStartingAfterEqual10 = _12345.startingAfter(i -> i == 10);
		twice(() -> assertThat(fiveStartingAfterEqual10, is(emptyIterable())));
	}

	@Test
	public void startingFrom() {
		IntSequence emptyStartingFrom5 = empty.startingFrom(5);
		twice(() -> assertThat(emptyStartingFrom5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingFrom5.iterator().nextInt());

		IntSequence nineStartingFrom5 = _123456789.startingFrom(5);
		twice(() -> assertThat(nineStartingFrom5, containsInts(5, 6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingFrom5), is(5));
		twice(() -> assertThat(nineStartingFrom5, is(emptyIterable())));
		twice(() -> assertThat(_123456789, containsInts(1, 2, 3, 4, 6, 7, 8, 9)));

		IntSequence fiveStartingFrom10 = _12345.startingFrom(10);
		twice(() -> assertThat(fiveStartingFrom10, is(emptyIterable())));
	}

	@Test
	public void startingFromPredicate() {
		IntSequence emptyStartingFromEqual5 = empty.startingFrom(i -> i == 5);
		twice(() -> assertThat(emptyStartingFromEqual5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingFromEqual5.iterator().nextInt());

		IntSequence nineStartingFromEqual5 = _123456789.startingFrom(i -> i == 5);
		twice(() -> assertThat(nineStartingFromEqual5, containsInts(5, 6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingFromEqual5), is(5));
		twice(() -> assertThat(nineStartingFromEqual5, is(emptyIterable())));
		twice(() -> assertThat(_123456789, containsInts(1, 2, 3, 4, 6, 7, 8, 9)));

		IntSequence fiveStartingFromEqual10 = _12345.startingFrom(i -> i == 10);
		twice(() -> assertThat(fiveStartingFromEqual10, is(emptyIterable())));
	}

	@Test
	public void toList() {
		twice(() -> {
			IntList list = _12345.toList();
			assertThat(list, is(instanceOf(ArrayIntList.class)));
			assertThat(list, containsInts(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSet() {
		twice(() -> {
			IntSet set = _12345.toSet();
			assertThat(set, instanceOf(BitIntSet.class));
			assertThat(set, containsInts(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSortedSet() {
		twice(() -> {
			IntSortedSet sortedSet = _12345.toSortedSet();
			assertThat(sortedSet, instanceOf(BitIntSet.class));
			assertThat(sortedSet, containsInts(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSetWithType() {
		twice(() -> {
			IntSet set = _12345.toSet(BitIntSet::new);
			assertThat(set, instanceOf(BitIntSet.class));
			assertThat(set, containsInts(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toCollection() {
		twice(() -> {
			IntList list = _12345.toCollection(IntList::create);
			assertThat(list, instanceOf(ArrayIntList.class));
			assertThat(list, containsInts(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void collectIntoIntCollection() {
		twice(() -> {
			IntList list = IntList.create();
			IntList result = _12345.collectInto(list);

			assertThat(result, is(sameInstance(list)));
			assertThat(result, containsInts(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void collect() {
		twice(() -> {
			StringBuilder builder = _123.collect(StringBuilder::new, StringBuilder::append);
			assertThat(builder.toString(), is("123"));
		});
	}

	@Test
	public void collectIntoContainer() {
		twice(() -> {
			IntList list = IntList.create();
			IntList result = _12345.collectInto(list, IntList::addInt);

			assertThat(result, is(sameInstance(list)));
			assertThat(result, containsInts(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(Arrays.equals(_123.toIntArray(), new int[]{1, 2, 3}), is(true)));
	}

	@Test
	public void join() {
		twice(() -> assertThat(_123.join(", "), is("1, 2, 3")));
	}

	@Test
	public void joinWithPrefixAndSuffix() {
		twice(() -> assertThat(_123.join("<", ", ", ">"), is("<1, 2, 3>")));
	}

	@Test
	public void reduce() {
		IntBinaryOperator secondInt = (c1, c2) -> c2;
		twice(() -> {
			assertThat(empty.reduce(secondInt), is(OptionalInt.empty()));
			assertThat(_1.reduce(secondInt), is(OptionalInt.of(1)));
			assertThat(_12.reduce(secondInt), is(OptionalInt.of(2)));
			assertThat(_123.reduce(secondInt), is(OptionalInt.of(3)));
		});
	}

	@Test
	public void reduceWithIdentity() {
		IntBinaryOperator secondInt = (c1, c2) -> c2;
		twice(() -> {
			assertThat(empty.reduce(17, secondInt), is(17));
			assertThat(_1.reduce(17, secondInt), is(1));
			assertThat(_12.reduce(17, secondInt), is(2));
			assertThat(_123.reduce(17, secondInt), is(3));
		});
	}

	@Test
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(OptionalInt.empty()));
			assertThat(_1.first(), is(OptionalInt.of(1)));
			assertThat(_12.first(), is(OptionalInt.of(1)));
			assertThat(_123.first(), is(OptionalInt.of(1)));
		});
	}

	@Test
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(OptionalInt.empty()));
			assertThat(_1.last(), is(OptionalInt.of(1)));
			assertThat(_12.last(), is(OptionalInt.of(2)));
			assertThat(_123.last(), is(OptionalInt.of(3)));
		});
	}

	@Test
	public void at() {
		twice(() -> {
			assertThat(empty.at(0), is(OptionalInt.empty()));
			assertThat(empty.at(17), is(OptionalInt.empty()));

			assertThat(_1.at(0), is(OptionalInt.of(1)));
			assertThat(_1.at(1), is(OptionalInt.empty()));
			assertThat(_1.at(17), is(OptionalInt.empty()));

			assertThat(_12345.at(0), is(OptionalInt.of(1)));
			assertThat(_12345.at(1), is(OptionalInt.of(2)));
			assertThat(_12345.at(4), is(OptionalInt.of(5)));
			assertThat(_12345.at(17), is(OptionalInt.empty()));
		});
	}

	@Test
	public void firstByPredicate() {
		twice(() -> {
			assertThat(empty.first(x -> x > 1), is(OptionalInt.empty()));
			assertThat(_1.first(x -> x > 1), is(OptionalInt.empty()));
			assertThat(_12.first(x -> x > 1), is(OptionalInt.of(2)));
			assertThat(_12345.first(x -> x > 1), is(OptionalInt.of(2)));
		});
	}

	@Test
	public void lastByPredicate() {
		twice(() -> {
			assertThat(empty.last(x -> x > 1), is(OptionalInt.empty()));
			assertThat(_1.last(x -> x > 1), is(OptionalInt.empty()));
			assertThat(_12.last(x -> x > 1), is(OptionalInt.of(2)));
			assertThat(_12345.last(x -> x > 1), is(OptionalInt.of(5)));
		});
	}

	@Test
	public void atByPredicate() {
		twice(() -> {
			assertThat(empty.at(0, x -> x > 1), is(OptionalInt.empty()));
			assertThat(empty.at(17, x -> x > 1), is(OptionalInt.empty()));

			assertThat(_1.at(0, x -> x > 1), is(OptionalInt.empty()));
			assertThat(_1.at(17, x -> x > 1), is(OptionalInt.empty()));

			assertThat(_12.at(0, x -> x > 1), is(OptionalInt.of(2)));
			assertThat(_12.at(1, x -> x > 1), is(OptionalInt.empty()));
			assertThat(_12.at(17, x -> x > 1), is(OptionalInt.empty()));

			assertThat(_12345.at(0, x -> x > 1), is(OptionalInt.of(2)));
			assertThat(_12345.at(1, x -> x > 1), is(OptionalInt.of(3)));
			assertThat(_12345.at(3, x -> x > 1), is(OptionalInt.of(5)));
			assertThat(_12345.at(4, x -> x > 1), is(OptionalInt.empty()));
			assertThat(_12345.at(17, x -> x > 1), is(OptionalInt.empty()));
		});
	}

	@Test
	public void step() {
		IntSequence emptyStep3 = empty.step(3);
		twice(() -> assertThat(emptyStep3, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStep3.iterator().nextInt());

		IntSequence nineStep3 = _123456789.step(3);
		twice(() -> assertThat(nineStep3, containsInts(1, 4, 7)));

		IntIterator nineStep3Iterator = nineStep3.iterator();
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.hasNext(), is(true));
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.nextInt(), is(1));
		nineStep3Iterator.remove();
		assertThat(nineStep3Iterator.hasNext(), is(true));
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.nextInt(), is(4));
		nineStep3Iterator.remove();

		twice(() -> assertThat(nineStep3, containsInts(2, 6, 9)));
		twice(() -> assertThat(_123456789, containsInts(2, 3, 5, 6, 7, 8, 9)));
	}

	@Test
	public void distinct() {
		IntSequence emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptyDistinct.iterator().nextInt());

		IntSequence oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, containsInts(17)));

		IntSequence twoDuplicatesDistinct = IntSequence.of(17, 17).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, containsInts(17)));

		IntSequence nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, containsInts(6, 1, -7, 2, 17, 5, 4)));

		assertThat(removeFirst(nineDistinct), is(6));
		twice(() -> assertThat(nineDistinct, containsInts(6, 1, -7, 2, 17, 5, 4)));
		twice(() -> assertThat(nineRandom, containsInts(6, 1, -7, 1, 2, 17, 5, 4)));
	}

	@Test
	public void sorted() {
		IntSequence emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptySorted.iterator().nextInt());

		IntSequence oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, containsInts(17)));

		IntSequence twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, containsInts(17, 32)));

		IntSequence nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, containsInts(-7, 1, 1, 2, 4, 5, 6, 6, 17)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSorted));
		twice(() -> assertThat(nineSorted, containsInts(-7, 1, 1, 2, 4, 5, 6, 6, 17)));
		twice(() -> assertThat(nineRandom, contains(6, 6, 1, -7, 1, 2, 17, 5, 4)));
	}

	@Test
	public void sortedWithUpdates() {
		List<Integer> backing = new ArrayList<>(asList(2, 3, 1));
		IntSequence sorted = IntSequence.from(backing).sorted();

		backing.add(4);
		twice(() -> assertThat(sorted, containsInts(1, 2, 3, 4)));
	}

	@Test
	public void min() {
		twice(() -> assertThat(empty.min(), is(OptionalInt.empty())));
		twice(() -> assertThat(oneRandom.min(), is(OptionalInt.of(17))));
		twice(() -> assertThat(twoRandom.min(), is(OptionalInt.of(17))));
		twice(() -> assertThat(nineRandom.min(), is(OptionalInt.of(-7))));
	}

	@Test
	public void max() {
		twice(() -> assertThat(empty.max(), is(OptionalInt.empty())));
		twice(() -> assertThat(oneRandom.max(), is(OptionalInt.of(17))));
		twice(() -> assertThat(twoRandom.max(), is(OptionalInt.of(32))));
		twice(() -> assertThat(nineRandom.max(), is(OptionalInt.of(17))));
	}

	@Test
	public void size() {
		twice(() -> assertThat(empty.size(), is(0)));
		twice(() -> assertThat(_1.size(), is(1)));
		twice(() -> assertThat(_12.size(), is(2)));
		twice(() -> assertThat(_123456789.size(), is(9)));
	}

	@Test
	public void any() {
		twice(() -> assertThat(_123.any(x -> x > 0), is(true)));
		twice(() -> assertThat(_123.any(x -> x > 2), is(true)));
		twice(() -> assertThat(_123.any(x -> x > 4), is(false)));
	}

	@Test
	public void all() {
		twice(() -> assertThat(_123.all(x -> x > 0), is(true)));
		twice(() -> assertThat(_123.all(x -> x > 2), is(false)));
		twice(() -> assertThat(_123.all(x -> x > 4), is(false)));
	}

	@Test
	public void none() {
		twice(() -> assertThat(_123.none(x -> x > 0), is(false)));
		twice(() -> assertThat(_123.none(x -> x > 2), is(false)));
		twice(() -> assertThat(_123.none(x -> x > 4), is(true)));
	}

	@Test
	public void peek() {
		IntSequence emptyPeeked = empty.peek(x -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyPeeked, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().nextInt());

		AtomicInteger value = new AtomicInteger(1);
		IntSequence onePeeked = _1.peek(x -> assertThat(x, is(value.getAndIncrement())));
		twiceIndexed(value, 1, () -> assertThat(onePeeked, containsInts(1)));

		IntSequence twoPeeked = _12.peek(x -> assertThat(x, is(value.getAndIncrement())));
		twiceIndexed(value, 2, () -> assertThat(twoPeeked, containsInts(1, 2)));

		IntSequence fivePeeked = _12345.peek(x -> assertThat(x, is(value.getAndIncrement())));
		twiceIndexed(value, 5, () -> assertThat(fivePeeked, containsInts(1, 2, 3, 4, 5)));

		assertThat(removeFirst(fivePeeked), is(1));
		twiceIndexed(value, 4, () -> assertThat(fivePeeked, containsInts(2, 3, 4, 5)));
		twice(() -> assertThat(_12345, containsInts(2, 3, 4, 5)));
	}

	@Test
	public void peekIndexed() {
		IntSequence emptyPeeked = empty.peekIndexed((i, x) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyPeeked, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().nextInt());

		AtomicInteger index = new AtomicInteger();
		AtomicInteger value = new AtomicInteger(1);
		IntSequence onePeeked = _1.peekIndexed((i, x) -> {
			assertThat(i, is(value.getAndIncrement()));
			assertThat(x, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(onePeeked, containsInts(1));

			assertThat(index.get(), is(1));
			assertThat(value.get(), is(2));
			index.set(0);
			value.set(1);
		});

		IntSequence twoPeeked = _12.peekIndexed((i, x) -> {
			assertThat(i, is(value.getAndIncrement()));
			assertThat(x, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(twoPeeked, containsInts(1, 2));

			assertThat(index.get(), is(2));
			assertThat(value.get(), is(3));
			index.set(0);
			value.set(1);
		});

		IntSequence fivePeeked = _12345.peekIndexed((i, x) -> {
			assertThat(i, is(value.getAndIncrement()));
			assertThat(x, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(fivePeeked, containsInts(1, 2, 3, 4, 5));

			assertThat(index.get(), is(5));
			assertThat(value.get(), is(6));
			index.set(0);
			value.set(1);
		});

		assertThat(removeFirst(fivePeeked), is(1));
		index.set(0);
		value.set(2);

		twice(() -> {
			assertThat(fivePeeked, containsInts(2, 3, 4, 5));
			assertThat(index.get(), is(4));
			assertThat(value.get(), is(6));
			index.set(0);
			value.set(2);
		});

		twice(() -> assertThat(_12345, containsInts(2, 3, 4, 5)));
	}

	@Test
	public void stream() {
		IntSequence empty = IntSequence.empty();
		twice(() -> assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable())));

		IntSequence sequence = IntSequence.of(1, 2, 3, 4, 5);
		twice(() -> assertThat(sequence.stream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void streamFromOnce() {
		IntSequence empty = IntSequence.once(IntIterator.of());
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));

		IntSequence sequence = IntSequence.once(IntIterator.of(1, 2, 3, 4, 5));
		assertThat(sequence.stream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5));
		assertThat(sequence.stream().collect(Collectors.toList()), is(emptyIterable()));
	}

	@Test
	public void intStream() {
		twice(() -> assertThat(empty.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		                       is(emptyIterable())));

		twice(() -> assertThat(_12345.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		                       containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void intStreamFromOnce() {
		IntSequence empty = IntSequence.once(IntIterator.of());
		assertThat(empty.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));
		assertThat(empty.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));

		IntSequence sequence = IntSequence.once(IntIterator.of(1, 2, 3, 4, 5));
		assertThat(sequence.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts(1, 2, 3, 4, 5));
		assertThat(sequence.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));
	}

	@Test
	public void prefix() {
		IntSequence emptyPrefixed = empty.prefix(327);
		twice(() -> assertThat(emptyPrefixed, containsInts(327)));

		IntIterator emptyIterator = emptyPrefixed.iterator();
		emptyIterator.nextInt();
		expecting(NoSuchElementException.class, emptyIterator::nextInt);

		IntSequence threePrefixed = _123.prefix(327);
		twice(() -> assertThat(threePrefixed, containsInts(327, 1, 2, 3)));

		IntIterator iterator = threePrefixed.iterator();
		expecting(UnsupportedOperationException.class, () -> {
			iterator.nextInt();
			iterator.remove();
		});
		assertThat(iterator.nextInt(), is(1));
		iterator.remove();
		twice(() -> assertThat(threePrefixed, containsInts(327, 2, 3)));
		twice(() -> assertThat(_123, containsInts(2, 3)));
	}

	@Test
	public void suffix() {
		IntSequence emptySuffixed = empty.suffix(532);
		twice(() -> assertThat(emptySuffixed, containsInts(532)));

		IntIterator emptyIterator = emptySuffixed.iterator();
		emptyIterator.nextInt();
		expecting(NoSuchElementException.class, emptyIterator::nextInt);

		IntSequence threeSuffixed = _123.suffix(532);
		twice(() -> assertThat(threeSuffixed, containsInts(1, 2, 3, 532)));

		assertThat(removeFirst(threeSuffixed), is(1));
		twice(() -> assertThat(threeSuffixed, containsInts(2, 3, 532)));
		twice(() -> assertThat(_123, containsInts(2, 3)));
	}

	@Test
	public void interleave() {
		IntSequence emptyInterleaved = empty.interleave(empty);
		twice(() -> assertThat(emptyInterleaved, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyInterleaved.iterator().nextInt());

		IntSequence interleavedShortFirst = _123.interleave(_12345);
		twice(() -> assertThat(interleavedShortFirst, containsInts(1, 1, 2, 2, 3, 3, 4, 5)));

		IntSequence interleavedShortLast = _12345.interleave(_123);
		twice(() -> assertThat(interleavedShortLast, containsInts(1, 1, 2, 2, 3, 3, 4, 5)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(interleavedShortFirst));
		twice(() -> assertThat(interleavedShortLast, containsInts(1, 1, 2, 2, 3, 3, 4, 5)));
	}

	@Test
	public void reverse() {
		IntSequence emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyReversed.iterator().nextInt());

		IntSequence oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, containsInts(1)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneReversed));
		twice(() -> assertThat(oneReversed, containsInts(1)));
		twice(() -> assertThat(_1, containsInts(1)));

		IntSequence twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, containsInts(2, 1)));

		IntSequence threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, containsInts(3, 2, 1)));

		IntSequence nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed, containsInts(9, 8, 7, 6, 5, 4, 3, 2, 1)));
	}

	@Test
	public void reverseWithUpdates() {
		List<Integer> backing = new ArrayList<>(asList(1, 2, 3));
		IntSequence reversed = IntSequence.from(backing).reverse();

		backing.add(4);
		assertThat(reversed, containsInts(4, 3, 2, 1));
	}

	@Test
	public void positive() {
		IntSequence positive = IntSequence.positive();
		twice(() -> assertThat(positive.limit(5), containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void positiveFromZero() {
		IntSequence positiveFromZero = IntSequence.positiveFromZero();
		twice(() -> assertThat(positiveFromZero.limit(5), containsInts(0, 1, 2, 3, 4)));
	}

	@Test
	public void negative() {
		IntSequence negative = IntSequence.negative();
		twice(() -> assertThat(negative.limit(5), containsInts(-1, -2, -3, -4, -5)));
	}

	@Test
	public void negativeFromZero() {
		IntSequence negativeFromZero = IntSequence.negativeFromZero();
		twice(() -> assertThat(negativeFromZero.limit(5), containsInts(0, -1, -2, -3, -4)));
	}

	@Test
	public void decreasingFrom() {
		IntSequence negative = IntSequence.decreasingFrom(-10);
		twice(() -> assertThat(negative.limit(5), containsInts(-10, -11, -12, -13, -14)));

		IntSequence decreasingFrom2 = IntSequence.decreasingFrom(2);
		twice(() -> assertThat(decreasingFrom2.limit(5), containsInts(2, 1, 0, -1, -2)));

		IntSequence decreasingFromMinValue = IntSequence.decreasingFrom(Integer.MIN_VALUE);
		twice(() -> assertThat(decreasingFromMinValue.limit(3),
		                       containsInts(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE - 1)));
	}

	@Test
	public void increasingFrom() {
		IntSequence increasingFrom10 = IntSequence.increasingFrom(10);
		twice(() -> assertThat(increasingFrom10.limit(5), containsInts(10, 11, 12, 13, 14)));

		IntSequence increasingFrom_2 = IntSequence.increasingFrom(-2);
		twice(() -> assertThat(increasingFrom_2.limit(5), containsInts(-2, -1, 0, 1, 2)));

		IntSequence increasingFromMaxValue = IntSequence.increasingFrom(Integer.MAX_VALUE);
		twice(() -> assertThat(increasingFromMaxValue.limit(3),
		                       containsInts(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE + 1)));
	}

	@Test
	public void steppingFrom() {
		IntSequence steppingFrom0Step10 = IntSequence.steppingFrom(0, 10);
		twice(() -> assertThat(steppingFrom0Step10.limit(5), containsInts(0, 10, 20, 30, 40)));

		IntSequence steppingFrom0Step_10 = IntSequence.steppingFrom(0, -10);
		twice(() -> assertThat(steppingFrom0Step_10.limit(5), containsInts(0, -10, -20, -30, -40)));

		IntSequence steppingFromMaxValueStep10 = IntSequence.steppingFrom(Integer.MAX_VALUE, 10);
		twice(() -> assertThat(steppingFromMaxValueStep10.limit(3),
		                       containsInts(Integer.MAX_VALUE, Integer.MIN_VALUE + 9, Integer.MIN_VALUE + 19)));
	}

	@Test
	public void range() {
		IntSequence range1to6 = IntSequence.range(1, 6);
		twice(() -> assertThat(range1to6, containsInts(1, 2, 3, 4, 5, 6)));

		IntSequence range6to1 = IntSequence.range(6, 1);
		twice(() -> assertThat(range6to1, containsInts(6, 5, 4, 3, 2, 1)));

		IntSequence range_2to2 = IntSequence.range(-2, 2);
		twice(() -> assertThat(range_2to2, containsInts(-2, -1, 0, 1, 2)));

		IntSequence range2to_2 = IntSequence.range(2, -2);
		twice(() -> assertThat(range2to_2, containsInts(2, 1, 0, -1, -2)));

		IntSequence maxValue = IntSequence.range(Integer.MAX_VALUE - 3, Integer.MAX_VALUE);
		twice(() -> assertThat(maxValue,
		                       containsInts(Integer.MAX_VALUE - 3, Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1,
		                                    Integer.MAX_VALUE)));

		IntSequence minValue = IntSequence.range(Integer.MIN_VALUE + 3, Integer.MIN_VALUE);
		twice(() -> assertThat(minValue,
		                       containsInts(Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 1,
		                                    Integer.MIN_VALUE)));
	}

	@Test
	public void rangeWithStep() {
		IntSequence range1to6step2 = IntSequence.range(1, 6, 2);
		twice(() -> assertThat(range1to6step2, containsInts(1, 3, 5)));

		IntSequence range6to1step2 = IntSequence.range(6, 1, 2);
		twice(() -> assertThat(range6to1step2, containsInts(6, 4, 2)));

		IntSequence range_6to6step2 = IntSequence.range(-6, 6, 3);
		twice(() -> assertThat(range_6to6step2, containsInts(-6, -3, 0, 3, 6)));

		IntSequence range6to_6step2 = IntSequence.range(6, -6, 3);
		twice(() -> assertThat(range6to_6step2, containsInts(6, 3, 0, -3, -6)));

		IntSequence maxValue = IntSequence.range(Integer.MAX_VALUE - 2, Integer.MAX_VALUE, 2);
		twice(() -> assertThat(maxValue, containsInts(Integer.MAX_VALUE - 2, Integer.MAX_VALUE)));

		IntSequence minValue = IntSequence.range(Integer.MIN_VALUE + 2, Integer.MIN_VALUE, 2);
		twice(() -> assertThat(minValue, containsInts(Integer.MIN_VALUE + 2, Integer.MIN_VALUE)));

		IntSequence crossingMaxValue = IntSequence.range(Integer.MAX_VALUE - 3, Integer.MAX_VALUE, 2);
		twice(() -> assertThat(crossingMaxValue, containsInts(Integer.MAX_VALUE - 3, Integer.MAX_VALUE - 1)));

		IntSequence crossingMinValue = IntSequence.range(Integer.MIN_VALUE + 3, Integer.MIN_VALUE, 2);
		twice(() -> assertThat(crossingMinValue, containsInts(Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 1)));

		expecting(IllegalArgumentException.class, () -> IntSequence.range(1, 6, -1));
	}

	@Test
	public void toChars() {
		CharSeq emptyChars = empty.toChars();
		twice(() -> assertThat(emptyChars, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyChars.iterator().nextChar());

		CharSeq charSeq = IntSequence.from(IntList.create('a', 'b', 'c', 'd', 'e')).toChars();
		twice(() -> assertThat(charSeq, containsChars('a', 'b', 'c', 'd', 'e')));

		assertThat(removeFirst(charSeq), is('a'));
		twice(() -> assertThat(charSeq, containsChars('b', 'c', 'd', 'e')));
	}

	@Test
	public void toLongs() {
		LongSequence emptyLongs = empty.toLongs();
		twice(() -> assertThat(emptyLongs, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyLongs.iterator().nextLong());

		LongSequence longSequence = _12345.toLongs();
		twice(() -> assertThat(longSequence, containsLongs(1, 2, 3, 4, 5)));

		assertThat(removeFirst(longSequence), is(1L));
		twice(() -> assertThat(longSequence, containsLongs(2, 3, 4, 5)));
	}

	@Test
	public void toDoubles() {
		DoubleSequence emptyDoubles = empty.toDoubles(x -> x + 1);
		twice(() -> assertThat(emptyDoubles, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyDoubles.iterator().nextDouble());

		DoubleSequence doubleSequence = _12345.toDoubles();
		twice(() -> assertThat(doubleSequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));

		assertThat(removeFirst(doubleSequence), is(1.0));
		twice(() -> assertThat(doubleSequence, containsDoubles(2, 3, 4, 5)));
	}

	@Test
	public void toCharsMapped() {
		CharSeq emptyChars = empty.toChars(x -> (char) (x + 'a' - 1));
		twice(() -> assertThat(emptyChars, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyChars.iterator().nextChar());

		CharSeq charSeq = _12345.toChars(i -> (char) ('a' + i - 1));
		twice(() -> assertThat(charSeq, containsChars('a', 'b', 'c', 'd', 'e')));

		assertThat(removeFirst(charSeq), is('a'));
		twice(() -> assertThat(charSeq, containsChars('b', 'c', 'd', 'e')));
	}

	@Test
	public void toLongsMapped() {
		long maxInt = Integer.MAX_VALUE;

		LongSequence emptyLongs = empty.toLongs(i -> i + maxInt);
		twice(() -> assertThat(emptyLongs, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyLongs.iterator().nextLong());

		LongSequence longSequence = _12345.toLongs(i -> i + maxInt);
		twice(() -> assertThat(longSequence,
		                       containsLongs(1L + maxInt, 2L + maxInt, 3L + maxInt, 4L + maxInt, 5L + maxInt)));

		assertThat(removeFirst(longSequence), is(1L + maxInt));
		twice(() -> assertThat(longSequence, containsLongs(2L + maxInt, 3L + maxInt, 4L + maxInt, 5L + maxInt)));
	}

	@Test
	public void toDoublesMapped() {
		DoubleSequence emptyDoubles = empty.toDoubles(i -> i / 2.0);
		twice(() -> assertThat(emptyDoubles, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyDoubles.iterator().nextDouble());

		DoubleSequence doubleSequence = _12345.toDoubles(i -> i / 2.0);
		twice(() -> assertThat(doubleSequence, containsDoubles(0.5, 1.0, 1.5, 2.0, 2.5)));

		assertThat(removeFirst(doubleSequence), is(0.5));
		twice(() -> assertThat(doubleSequence, containsDoubles(1.0, 1.5, 2.0, 2.5)));
	}

	@Test
	public void toSequence() {
		Sequence<Integer> emptySequence = empty.toSequence(i -> i + 1);
		twice(() -> assertThat(emptySequence, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySequence.iterator().next());

		Sequence<Integer> fiveSequence = _12345.toSequence(i -> i + 1);
		twice(() -> assertThat(fiveSequence, contains(2, 3, 4, 5, 6)));

		assertThat(removeFirst(fiveSequence), is(2));
		twice(() -> assertThat(fiveSequence, contains(3, 4, 5, 6)));
	}

	@Test
	public void box() {
		Sequence<Integer> emptyBoxed = empty.box();
		twice(() -> assertThat(emptyBoxed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBoxed.iterator().next());

		Sequence<Integer> fiveBoxed = _12345.box();
		twice(() -> assertThat(fiveBoxed, contains(1, 2, 3, 4, 5)));

		assertThat(removeFirst(fiveBoxed), is(1));
		twice(() -> assertThat(fiveBoxed, contains(2, 3, 4, 5)));
	}

	@Test
	public void repeat() {
		IntSequence emptyRepeated = empty.repeat();
		twice(() -> assertThat(emptyRepeated, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeated.iterator().nextInt());

		IntSequence oneRepeated = _1.repeat();
		twice(() -> assertThat(oneRepeated.limit(3), containsInts(1, 1, 1)));

		IntSequence twoRepeated = _12.repeat();
		twice(() -> assertThat(twoRepeated.limit(5), containsInts(1, 2, 1, 2, 1)));

		IntSequence threeRepeated = _123.repeat();
		twice(() -> assertThat(threeRepeated.limit(8), containsInts(1, 2, 3, 1, 2, 3, 1, 2)));

		assertThat(removeFirst(threeRepeated), is(1));
		twice(() -> assertThat(threeRepeated.limit(6), containsInts(2, 3, 2, 3, 2, 3)));
		twice(() -> assertThat(_123, containsInts(2, 3)));

		IntSequence varyingLengthRepeated = IntSequence.from(new IntIterable() {
			private List<Integer> list = asList(1, 2, 3);
			int end = list.size();

			@Override
			public IntIterator iterator() {
				List<Integer> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Integer> iterator = subList.iterator();
				return new DelegatingTransformingIntIterator<Integer, Iterator<Integer>>(iterator) {
					@Override
					public int nextInt() {
						return iterator.next();
					}
				};
			}
		}).repeat();
		assertThat(varyingLengthRepeated, containsInts(1, 2, 3, 1, 2, 1));
	}

	@Test
	public void repeatTwice() {
		IntSequence emptyRepeatedTwice = empty.repeat(2);
		twice(() -> assertThat(emptyRepeatedTwice, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeatedTwice.iterator().nextInt());

		IntSequence oneRepeatedTwice = _1.repeat(2);
		twice(() -> assertThat(oneRepeatedTwice, containsInts(1, 1)));

		IntSequence twoRepeatedTwice = _12.repeat(2);
		twice(() -> assertThat(twoRepeatedTwice, containsInts(1, 2, 1, 2)));

		IntSequence threeRepeatedTwice = _123.repeat(2);
		twice(() -> assertThat(threeRepeatedTwice, containsInts(1, 2, 3, 1, 2, 3)));

		assertThat(removeFirst(threeRepeatedTwice), is(1));
		twice(() -> assertThat(threeRepeatedTwice, containsInts(2, 3, 2, 3)));
		twice(() -> assertThat(_123, containsInts(2, 3)));

		IntSequence varyingLengthRepeatedTwice = IntSequence.from(new IntIterable() {
			private List<Integer> list = asList(1, 2, 3);
			int end = list.size();

			@Override
			public IntIterator iterator() {
				List<Integer> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Integer> iterator = subList.iterator();
				return new DelegatingTransformingIntIterator<Integer, Iterator<Integer>>(iterator) {
					@Override
					public int nextInt() {
						return iterator.next();
					}
				};
			}
		}).repeat(2);
		assertThat(varyingLengthRepeatedTwice, containsInts(1, 2, 3, 1, 2));
	}

	@Test
	public void repeatZero() {
		IntSequence emptyRepeatedZero = empty.repeat(0);
		twice(() -> assertThat(emptyRepeatedZero, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeatedZero.iterator().nextInt());

		IntSequence oneRepeatedZero = _1.repeat(0);
		twice(() -> assertThat(oneRepeatedZero, is(emptyIterable())));

		IntSequence twoRepeatedZero = _12.repeat(0);
		twice(() -> assertThat(twoRepeatedZero, is(emptyIterable())));

		IntSequence threeRepeatedZero = _123.repeat(0);
		twice(() -> assertThat(threeRepeatedZero, is(emptyIterable())));
	}

	@Test
	public void generate() {
		Queue<Integer> queue = new ArrayDeque<>(asList(1, 2, 3, 4, 5));
		IntSequence sequence = IntSequence.generate(queue::poll);

		IntIterator iterator = sequence.iterator();
		assertThat(iterator.nextInt(), is(1));
		assertThat(iterator.nextInt(), is(2));
		assertThat(iterator.nextInt(), is(3));
		assertThat(iterator.nextInt(), is(4));
		assertThat(iterator.nextInt(), is(5));
		expecting(NullPointerException.class, iterator::nextInt);

		IntIterator iterator2 = sequence.iterator();
		expecting(NullPointerException.class, iterator2::nextInt);
	}

	@Test
	public void multiGenerate() {
		IntSequence sequence = IntSequence.multiGenerate(() -> {
			Queue<Integer> queue = new ArrayDeque<>(asList(1, 2, 3, 4, 5));
			return queue::poll;
		});

		twice(() -> {
			IntIterator iterator = sequence.iterator();
			assertThat(iterator.nextInt(), is(1));
			assertThat(iterator.nextInt(), is(2));
			assertThat(iterator.nextInt(), is(3));
			assertThat(iterator.nextInt(), is(4));
			assertThat(iterator.nextInt(), is(5));
			expecting(NullPointerException.class, iterator::nextInt);
		});
	}

	@Test
	public void random() {
		IntSequence random = IntSequence.random();

		twice(() -> times(1000, random.iterator()::nextInt));

		assertThat(random.limit(10), not(containsInts(random.limit(10))));
	}

	@Test
	public void randomWithSupplier() {
		IntSequence random = IntSequence.random(() -> new Random(17));

		twice(() -> assertThat(random.limit(10),
		                       containsInts(-1149713343, -876354855, -1299783908, -1761252264, 356293784, 1723477387,
		                                    -789258487, -46827465, 190636124, -672335679)));
	}

	@Test
	public void randomUpper() {
		IntSequence random = IntSequence.random(1000);

		twice(() -> {
			IntIterator iterator = random.iterator();
			times(1000, () -> assertThat(iterator.nextInt(),
			                             is(both(greaterThanOrEqualTo(0)).and(lessThan(1000)))));
		});

		assertThat(random.limit(10), not(containsInts(random.limit(10))));
	}

	@Test
	public void randomUpperWithSupplier() {
		IntSequence random = IntSequence.random(() -> new Random(17), 1000);

		twice(() -> assertThat(random.limit(10),
		                       containsInts(976, 220, 694, 516, 892, 693, 404, 915, 62, 808)));
	}

	@Test
	public void randomLowerUpper() {
		IntSequence random = IntSequence.random(1000, 2000);

		twice(() -> {
			IntIterator iterator = random.iterator();
			times(1000, () -> assertThat(iterator.nextInt(),
			                             is(both(greaterThanOrEqualTo(1000)).and(lessThan(2000)))));
		});

		assertThat(random.limit(10), not(containsInts(random.limit(10))));
	}

	@Test
	public void randomLowerUpperWithSupplier() {
		IntSequence random = IntSequence.random(() -> new Random(17), 1000, 2000);

		twice(() -> assertThat(random.limit(10),
		                       containsInts(1976, 1220, 1694, 1516, 1892, 1693, 1404, 1915, 1062, 1808)));
	}

	@Test
	public void mapBack() {
		IntSequence emptyMappedBack = empty.mapBack(17, (p, x) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedBack, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedBack.iterator().nextInt());

		IntSequence threeMappedBackToPrevious = _123.mapBack(17, (p, x) -> p);
		twice(() -> assertThat(threeMappedBackToPrevious, containsInts(17, 1, 2)));

		IntSequence threeMappedBackToCurrent = _123.mapBack(17, (p, x) -> x);
		twice(() -> assertThat(threeMappedBackToCurrent, containsInts(1, 2, 3)));

		assertThat(removeFirst(threeMappedBackToCurrent), is(1));
		twice(() -> assertThat(threeMappedBackToCurrent, containsInts(2, 3)));
		twice(() -> assertThat(_123, containsInts(2, 3)));
	}

	@Test
	public void mapForward() {
		IntSequence emptyMappedForward = empty.mapForward(17, (x, n) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedForward, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedForward.iterator().nextInt());

		IntSequence threeMappedForwardToCurrent = _123.mapForward(17, (x, n) -> x);
		twice(() -> assertThat(threeMappedForwardToCurrent, containsInts(1, 2, 3)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(threeMappedForwardToCurrent));
		twice(() -> assertThat(threeMappedForwardToCurrent, containsInts(1, 2, 3)));

		IntSequence threeMappedForwardToNext = _123.mapForward(17, (x, n) -> n);
		twice(() -> assertThat(threeMappedForwardToNext, containsInts(2, 3, 17)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void window() {
		Sequence<IntSequence> emptyWindowed = empty.window(3);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<IntSequence> oneWindowed = _1.window(3);
		twice(() -> assertThat(oneWindowed, contains(containsInts(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(containsInts(1))));

		Sequence<IntSequence> twoWindowed = _12.window(3);
		twice(() -> assertThat(twoWindowed, contains(containsInts(1, 2))));

		Sequence<IntSequence> threeWindowed = _123.window(3);
		twice(() -> assertThat(threeWindowed, contains(containsInts(1, 2, 3))));

		Sequence<IntSequence> fourWindowed = _1234.window(3);
		twice(() -> assertThat(fourWindowed, contains(containsInts(1, 2, 3), containsInts(2, 3, 4))));

		Sequence<IntSequence> fiveWindowed = _12345.window(3);
		twice(() -> assertThat(fiveWindowed,
		                       contains(containsInts(1, 2, 3), containsInts(2, 3, 4), containsInts(3, 4, 5))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithSmallerStep() {
		Sequence<IntSequence> emptyWindowed = empty.window(3, 2);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<IntSequence> oneWindowed = _1.window(3, 2);
		twice(() -> assertThat(oneWindowed, contains(containsInts(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(containsInts(1))));

		Sequence<IntSequence> twoWindowed = _12.window(3, 2);
		twice(() -> assertThat(twoWindowed, contains(containsInts(1, 2))));

		Sequence<IntSequence> threeWindowed = _123.window(3, 2);
		twice(() -> assertThat(threeWindowed, contains(containsInts(1, 2, 3))));

		Sequence<IntSequence> fourWindowed = _1234.window(3, 2);
		twice(() -> assertThat(fourWindowed, contains(containsInts(1, 2, 3), containsInts(3, 4))));

		Sequence<IntSequence> fiveWindowed = _12345.window(3, 2);
		twice(() -> assertThat(fiveWindowed, contains(containsInts(1, 2, 3), containsInts(3, 4, 5))));

		Sequence<IntSequence> nineWindowed = _123456789.window(3, 2);
		twice(() -> assertThat(nineWindowed,
		                       contains(containsInts(1, 2, 3), containsInts(3, 4, 5), containsInts(5, 6, 7),
		                                containsInts(7, 8, 9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithLargerStep() {
		Sequence<IntSequence> emptyWindowed = empty.window(3, 4);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<IntSequence> oneWindowed = _1.window(3, 4);
		twice(() -> assertThat(oneWindowed, contains(containsInts(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(containsInts(1))));

		Sequence<IntSequence> twoWindowed = _12.window(3, 4);
		twice(() -> assertThat(twoWindowed, contains(containsInts(1, 2))));

		Sequence<IntSequence> threeWindowed = _123.window(3, 4);
		twice(() -> assertThat(threeWindowed, contains(containsInts(1, 2, 3))));

		Sequence<IntSequence> fourWindowed = _1234.window(3, 4);
		twice(() -> assertThat(fourWindowed, contains(containsInts(1, 2, 3))));

		Sequence<IntSequence> fiveWindowed = _12345.window(3, 4);
		twice(() -> assertThat(fiveWindowed, contains(containsInts(1, 2, 3), containsInts(5))));

		Sequence<IntSequence> nineWindowed = _123456789.window(3, 4);
		twice(() -> assertThat(nineWindowed,
		                       contains(containsInts(1, 2, 3), containsInts(5, 6, 7), containsInts(9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batch() {
		Sequence<IntSequence> emptyBatched = empty.batch(3);
		twice(() -> assertThat(emptyBatched, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<IntSequence> oneBatched = _1.batch(3);
		twice(() -> assertThat(oneBatched, contains(containsInts(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneBatched));
		twice(() -> assertThat(oneBatched, contains(containsInts(1))));

		Sequence<IntSequence> twoBatched = _12.batch(3);
		twice(() -> assertThat(twoBatched, contains(containsInts(1, 2))));

		Sequence<IntSequence> threeBatched = _123.batch(3);
		twice(() -> assertThat(threeBatched, contains(containsInts(1, 2, 3))));

		Sequence<IntSequence> fourBatched = _1234.batch(3);
		twice(() -> assertThat(fourBatched, contains(containsInts(1, 2, 3), containsInts(4))));

		Sequence<IntSequence> fiveBatched = _12345.batch(3);
		twice(() -> assertThat(fiveBatched, contains(containsInts(1, 2, 3), containsInts(4, 5))));

		Sequence<IntSequence> nineBatched = _123456789.batch(3);
		twice(() -> assertThat(nineBatched,
		                       contains(containsInts(1, 2, 3), containsInts(4, 5, 6), containsInts(7, 8, 9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnPredicate() {
		Sequence<IntSequence> emptyBatched = empty.batch((a, b) -> a > b);
		twice(() -> assertThat(emptyBatched, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<IntSequence> oneBatched = _1.batch((a, b) -> a > b);
		twice(() -> assertThat(oneBatched, contains(containsInts(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneBatched));
		twice(() -> assertThat(oneBatched, contains(containsInts(1))));

		Sequence<IntSequence> twoBatched = _12.batch((a, b) -> a > b);
		twice(() -> assertThat(twoBatched, contains(containsInts(1, 2))));

		Sequence<IntSequence> threeBatched = _123.batch((a, b) -> a > b);
		twice(() -> assertThat(threeBatched, contains(containsInts(1, 2, 3))));

		Sequence<IntSequence> threeRandomBatched = threeRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(threeRandomBatched, contains(containsInts(17, 32), containsInts(12))));

		Sequence<IntSequence> nineRandomBatched = nineRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(nineRandomBatched,
		                       contains(containsInts(6, 6), containsInts(1), containsInts(-7, 1, 2, 17),
		                                containsInts(5), containsInts(4))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void split() {
		Sequence<IntSequence> emptySplit = empty.split(3);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

		Sequence<IntSequence> oneSplit = _1.split(3);
		twice(() -> assertThat(oneSplit, contains(containsInts(1))));

		Sequence<IntSequence> twoSplit = _12.split(3);
		twice(() -> assertThat(twoSplit, contains(containsInts(1, 2))));

		Sequence<IntSequence> threeSplit = _123.split(3);
		twice(() -> assertThat(threeSplit, contains(containsInts(1, 2))));

		Sequence<IntSequence> fiveSplit = _12345.split(3);
		twice(() -> assertThat(fiveSplit, contains(containsInts(1, 2), containsInts(4, 5))));

		Sequence<IntSequence> nineSplit = _123456789.split(3);
		twice(() -> assertThat(nineSplit, contains(containsInts(1, 2), containsInts(4, 5, 6, 7, 8, 9))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSplit));
		twice(() -> assertThat(nineSplit, contains(containsInts(1, 2), containsInts(4, 5, 6, 7, 8, 9))));
		twice(() -> assertThat(_123456789, containsInts(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitIntPredicate() {
		Sequence<IntSequence> emptySplit = empty.split(x -> x % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

		Sequence<IntSequence> oneSplit = _1.split(x -> x % 3 == 0);
		twice(() -> assertThat(oneSplit, contains(containsInts(1))));

		Sequence<IntSequence> twoSplit = _12.split(x -> x % 3 == 0);
		twice(() -> assertThat(twoSplit, contains(containsInts(1, 2))));

		Sequence<IntSequence> threeSplit = _123.split(x -> x % 3 == 0);
		twice(() -> assertThat(threeSplit, contains(containsInts(1, 2))));

		Sequence<IntSequence> fiveSplit = _12345.split(x -> x % 3 == 0);
		twice(() -> assertThat(fiveSplit, contains(containsInts(1, 2), containsInts(4, 5))));

		Sequence<IntSequence> nineSplit = _123456789.split(x -> x % 3 == 0);
		twice(() -> assertThat(nineSplit, contains(containsInts(1, 2), containsInts(4, 5), containsInts(7, 8))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSplit));
		twice(() -> assertThat(nineSplit, contains(containsInts(1, 2), containsInts(4, 5), containsInts(7, 8))));
		twice(() -> assertThat(_123456789, containsInts(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void filterClear() {
		List<Integer> original = new ArrayList<>(asList(1, 2, 3, 4));

		IntSequence filtered = IntSequence.from(original).filter(x -> x % 2 != 0);
		filtered.clear();

		twice(() -> assertThat(filtered, is(emptyIterable())));
		assertThat(original, contains(2, 4));
	}

	@Test
	public void isEmpty() {
		twice(() -> assertThat(empty.isEmpty(), is(true)));
		twice(() -> assertThat(_1.isEmpty(), is(false)));
		twice(() -> assertThat(_12.isEmpty(), is(false)));
		twice(() -> assertThat(_12345.isEmpty(), is(false)));
	}

	@Test
	public void containsInt() {
		assertThat(empty.containsInt(17), is(false));

		assertThat(_12345.containsInt(1), is(true));
		assertThat(_12345.containsInt(3), is(true));
		assertThat(_12345.containsInt(5), is(true));
		assertThat(_12345.containsInt(17), is(false));
	}

	@Test
	public void containsAllInts() {
		assertThat(empty.containsAllInts(), is(true));
		assertThat(empty.containsAllInts(17, 18, 19), is(false));

		assertThat(_12345.containsAllInts(), is(true));
		assertThat(_12345.containsAllInts(1), is(true));
		assertThat(_12345.containsAllInts(1, 3, 5), is(true));
		assertThat(_12345.containsAllInts(1, 2, 3, 4, 5), is(true));
		assertThat(_12345.containsAllInts(1, 2, 3, 4, 5, 17), is(false));
		assertThat(_12345.containsAllInts(17, 18, 19), is(false));
	}

	@Test
	public void containsAnyInts() {
		assertThat(empty.containsAnyInts(), is(false));
		assertThat(empty.containsAnyInts(17, 18, 19), is(false));

		assertThat(_12345.containsAnyInts(), is(false));
		assertThat(_12345.containsAnyInts(1), is(true));
		assertThat(_12345.containsAnyInts(1, 3, 5), is(true));
		assertThat(_12345.containsAnyInts(1, 2, 3, 4, 5), is(true));
		assertThat(_12345.containsAnyInts(1, 2, 3, 4, 5, 17), is(true));
		assertThat(_12345.containsAnyInts(17, 18, 19), is(false));
	}
}
