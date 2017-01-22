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
import org.d2ab.collection.Lists;
import org.d2ab.collection.longs.*;
import org.d2ab.iterator.Iterators;
import org.d2ab.iterator.longs.DelegatingTransformingLongIterator;
import org.d2ab.iterator.longs.LongIterator;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class LongSequenceTest {
	private final LongSequence empty = LongSequence.empty();

	private final LongSequence _1 = LongSequence.from(LongList.create(1));
	private final LongSequence _12 = LongSequence.from(LongList.create(1, 2));
	private final LongSequence _123 = LongSequence.from(LongList.create(1, 2, 3));
	private final LongSequence _1234 = LongSequence.from(LongList.create(1, 2, 3, 4));
	private final LongSequence _12345 = LongSequence.from(LongList.create(1, 2, 3, 4, 5));
	private final LongSequence _123456789 =
			LongSequence.from(LongList.create(1, 2, 3, 4, 5, 6, 7, 8, 9));

	private final LongSequence oneRandom = LongSequence.from(LongList.create(17));
	private final LongSequence twoRandom = LongSequence.from(LongList.create(17, 32));
	private final LongSequence threeRandom = LongSequence.from(LongList.create(17, 32, 12));
	private final LongSequence nineRandom =
			LongSequence.from(LongList.create(6, 6, 1, -7, 1, 2, 17, 5, 4));

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void ofNone() {
		LongSequence sequence = LongSequence.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void ofOne() {
		twice(() -> assertThat(_1, containsLongs(1)));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(_123, containsLongs(1, 2, 3)));
	}

	@Test
	public void fromArrayWithSize() {
		LongSequence sequence = LongSequence.from(new long[]{1, 2, 3, 4, 5}, 3);
		twice(() -> assertThat(sequence, containsLongs(1, 2, 3)));
	}

	@Test
	public void fromArrayWithOffsetAndSize() {
		expecting(IndexOutOfBoundsException.class,
		          () -> LongSequence.from(new long[]{1, 2, 3, 4, 5}, -1, 3).iterator());
		expecting(IndexOutOfBoundsException.class,
		          () -> LongSequence.from(new long[]{1, 2, 3, 4, 5}, 6, 0).iterator());
		expecting(IndexOutOfBoundsException.class,
		          () -> LongSequence.from(new long[]{1, 2, 3, 4, 5}, 1, 5).iterator());
		expecting(IndexOutOfBoundsException.class,
		          () -> LongSequence.from(new long[]{1, 2, 3, 4, 5}, 1, -1).iterator());

		LongSequence sequence = LongSequence.from(new long[]{1, 2, 3, 4, 5}, 1, 3);
		twice(() -> assertThat(sequence, containsLongs(2, 3, 4)));
	}

	@Test
	public void fromLongIterable() {
		LongSequence sequence = LongSequence.from(LongIterable.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(sequence, containsLongs(1, 2, 3, 4, 5)));
	}

	@Test
	public void fromLongIterableAsIterable() {
		LongSequence sequence = LongSequence.from((Iterable<Long>) LongIterable.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(sequence, containsLongs(1, 2, 3, 4, 5)));
	}

	@Test
	public void fromIterable() {
		LongSequence sequence = LongSequence.from(Iterables.of(1L, 2L, 3L, 4L, 5L));

		twice(() -> assertThat(sequence, containsLongs(1, 2, 3, 4, 5)));
	}

	@Test
	public void oncePrimitiveIteratorOfLong() {
		LongSequence sequence = LongSequence.once(LongIterator.of(1, 2, 3, 4, 5));

		assertThat(sequence, containsLongs(1, 2, 3, 4, 5));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceIterator() {
		LongSequence sequence = LongSequence.once(Iterators.of(1L, 2L, 3L, 4L, 5L));

		assertThat(sequence, containsLongs(1, 2, 3, 4, 5L));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceLongStream() {
		LongSequence sequence = LongSequence.once(LongStream.of(1, 2, 3, 4, 5));

		assertThat(sequence, containsLongs(1, 2, 3, 4, 5));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceStream() {
		LongSequence sequence = LongSequence.once(Stream.of(1L, 2L, 3L, 4L, 5L));

		assertThat(sequence, containsLongs(1, 2, 3, 4, 5));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceEmptyStream() {
		LongSequence sequence = LongSequence.once(Stream.of());

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void cachePrimitiveIteratorOfLong() {
		LongSequence cached = LongSequence.cache(LongIterator.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(cached, containsLongs(1, 2, 3, 4, 5)));
	}

	@Test
	public void cacheIterator() {
		LongSequence cached = LongSequence.cache(Iterators.of(1L, 2L, 3L, 4L, 5L));

		twice(() -> assertThat(cached, containsLongs(1, 2, 3, 4, 5)));
	}

	@Test
	public void cacheLongIterable() {
		LongSequence cached = LongSequence.cache(LongIterable.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(cached, containsLongs(1, 2, 3, 4, 5)));
	}

	@Test
	public void cacheIterable() {
		LongSequence cached = LongSequence.cache(Iterables.of(1L, 2L, 3L, 4L, 5L));

		twice(() -> assertThat(cached, containsLongs(1, 2, 3, 4, 5)));
	}

	@Test
	public void cacheLongStream() {
		LongSequence cached = LongSequence.cache(LongStream.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(cached, containsLongs(1, 2, 3, 4, 5)));
	}

	@Test
	public void cacheStream() {
		LongSequence cached = LongSequence.cache(Stream.of(1L, 2L, 3L, 4L, 5L));

		twice(() -> assertThat(cached, containsLongs(1, 2, 3, 4, 5)));
	}

	@Test
	public void forEachLong() {
		twice(() -> {
			empty.forEachLong(c -> fail("Should not get called"));

			AtomicLong value = new AtomicLong(1);
			_1.forEachLong(l -> assertThat(l, is(value.getAndIncrement())));

			value.set(1);
			_12.forEachLong(l -> assertThat(l, is(value.getAndIncrement())));

			value.set(1);
			_12345.forEachLong(l -> assertThat(l, is(value.getAndIncrement())));
		});
	}

	@Test
	public void forEachLongIndexed() {
		twice(() -> {
			empty.forEachLongIndexed((e, i) -> fail("Should not get called"));

			AtomicLong value = new AtomicLong(1);
			AtomicInteger index = new AtomicInteger();
			_1.forEachLongIndexed((e, i) -> {
				assertThat(e, is(value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(1));

			value.set(1);
			index.set(0);
			_12.forEachLongIndexed((e, i) -> {
				assertThat(e, is(value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(2));

			value.set(1);
			index.set(0);
			_12345.forEachLongIndexed((e, i) -> {
				assertThat(e, is(value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(5));
		});
	}

	@Test
	public void iterator() {
		twice(() -> {
			LongIterator iterator = _123.iterator();

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.nextLong(), is(1L));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.nextLong(), is(2L));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.nextLong(), is(3L));

			assertThat(iterator.hasNext(), is(false));
			expecting(NoSuchElementException.class, iterator::nextLong);
		});
	}

	@Test
	public void skip() {
		LongSequence threeSkipNone = _123.skip(0);
		twice(() -> assertThat(threeSkipNone, is(sameInstance(_123))));

		LongSequence threeSkipOne = _123.skip(1);
		twice(() -> assertThat(threeSkipOne, containsLongs(2, 3)));

		LongSequence threeSkipTwo = _123.skip(2);
		twice(() -> assertThat(threeSkipTwo, containsLongs(3)));

		LongSequence threeSkipThree = _123.skip(3);
		twice(() -> assertThat(threeSkipThree, is(emptyIterable())));

		LongSequence threeSkipFour = _123.skip(4);
		twice(() -> assertThat(threeSkipFour, is(emptyIterable())));

		expecting(NoSuchElementException.class, () -> threeSkipThree.iterator().nextLong());
		expecting(NoSuchElementException.class, () -> threeSkipFour.iterator().nextLong());

		assertThat(removeFirst(threeSkipOne), is(2L));
		twice(() -> assertThat(threeSkipOne, containsLongs(3)));
		twice(() -> assertThat(_123, containsLongs(1, 3)));
	}

	@Test
	public void skipTail() {
		LongSequence threeSkipTailNone = _123.skipTail(0);
		twice(() -> assertThat(threeSkipTailNone, is(sameInstance(_123))));

		LongSequence threeSkipTailOne = _123.skipTail(1);
		twice(() -> assertThat(threeSkipTailOne, containsLongs(1, 2)));

		LongSequence threeSkipTailTwo = _123.skipTail(2);
		twice(() -> assertThat(threeSkipTailTwo, containsLongs(1)));

		LongSequence threeSkipTailThree = _123.skipTail(3);
		twice(() -> assertThat(threeSkipTailThree, is(emptyIterable())));

		LongSequence threeSkipTailFour = _123.skipTail(4);
		twice(() -> assertThat(threeSkipTailFour, is(emptyIterable())));

		expecting(NoSuchElementException.class, () -> threeSkipTailThree.iterator().nextLong());
		expecting(NoSuchElementException.class, () -> threeSkipTailFour.iterator().nextLong());

		expecting(UnsupportedOperationException.class, () -> removeFirst(threeSkipTailOne));
		twice(() -> assertThat(threeSkipTailOne, containsLongs(1, 2)));
		twice(() -> assertThat(_123, containsLongs(1, 2, 3)));

		LongSequence nineSkipTailNone = _123456789.skipTail(0);
		twice(() -> assertThat(nineSkipTailNone, is(sameInstance(_123456789))));

		LongSequence nineSkipTailOne = _123456789.skipTail(1);
		twice(() -> assertThat(nineSkipTailOne, containsLongs(1, 2, 3, 4, 5, 6, 7, 8)));

		LongSequence nineSkipTailTwo = _123456789.skipTail(2);
		twice(() -> assertThat(nineSkipTailTwo, containsLongs(1, 2, 3, 4, 5, 6, 7)));

		LongSequence nineSkipTailThree = _123456789.skipTail(3);
		twice(() -> assertThat(nineSkipTailThree, containsLongs(1, 2, 3, 4, 5, 6)));

		LongSequence nineSkipTailFour = _123456789.skipTail(4);
		twice(() -> assertThat(nineSkipTailFour, containsLongs(1, 2, 3, 4, 5)));
	}

	@Test
	public void limit() {
		LongSequence threeLimitedToNone = _123.limit(0);
		twice(() -> assertThat(threeLimitedToNone, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> threeLimitedToNone.iterator().nextLong());

		LongSequence threeLimitedToOne = _123.limit(1);
		twice(() -> assertThat(threeLimitedToOne, containsLongs(1)));
		LongIterator iterator = threeLimitedToOne.iterator();
		iterator.nextLong();
		expecting(NoSuchElementException.class, iterator::nextLong);

		LongSequence threeLimitedToTwo = _123.limit(2);
		twice(() -> assertThat(threeLimitedToTwo, containsLongs(1, 2)));

		LongSequence threeLimitedToThree = _123.limit(3);
		twice(() -> assertThat(threeLimitedToThree, containsLongs(1, 2, 3)));

		LongSequence threeLimitedToFour = _123.limit(4);
		twice(() -> assertThat(threeLimitedToFour, containsLongs(1, 2, 3)));

		assertThat(removeFirst(threeLimitedToFour), is(1L));
		twice(() -> assertThat(threeLimitedToFour, containsLongs(2, 3)));
		twice(() -> assertThat(_123, containsLongs(2, 3)));
	}

	@Test
	public void limitTail() {
		LongSequence threeLimitTailToNone = _123.limitTail(0);
		twice(() -> assertThat(threeLimitTailToNone, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> threeLimitTailToNone.iterator().nextLong());

		LongSequence threeLimitTailToOne = _123.limitTail(1);
		twice(() -> assertThat(threeLimitTailToOne, containsLongs(3)));
		LongIterator iterator = threeLimitTailToOne.iterator();
		iterator.nextLong();
		expecting(NoSuchElementException.class, iterator::nextLong);

		LongSequence threeLimitTailToTwo = _123.limitTail(2);
		twice(() -> assertThat(threeLimitTailToTwo, containsLongs(2, 3)));

		LongSequence threeLimitTailToThree = _123.limitTail(3);
		twice(() -> assertThat(threeLimitTailToThree, containsLongs(1, 2, 3)));

		LongSequence threeLimitTailToFour = _123.limitTail(4);
		twice(() -> assertThat(threeLimitTailToFour, containsLongs(1, 2, 3)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(threeLimitTailToFour));
		twice(() -> assertThat(threeLimitTailToFour, containsLongs(1, 2, 3)));
		twice(() -> assertThat(_123, containsLongs(1, 2, 3)));

		LongSequence nineLimitTailToNone = _123456789.limitTail(0);
		twice(() -> assertThat(nineLimitTailToNone, is(emptyIterable())));

		LongSequence nineLimitTailToOne = _123456789.limitTail(1);
		twice(() -> assertThat(nineLimitTailToOne, containsLongs(9)));

		LongSequence nineLimitTailToTwo = _123456789.limitTail(2);
		twice(() -> assertThat(nineLimitTailToTwo, containsLongs(8, 9)));

		LongSequence nineLimitTailToThree = _123456789.limitTail(3);
		twice(() -> assertThat(nineLimitTailToThree, containsLongs(7, 8, 9)));

		LongSequence nineLimitTailToFour = _123456789.limitTail(4);
		twice(() -> assertThat(nineLimitTailToFour, containsLongs(6, 7, 8, 9)));
	}

	@Test
	public void appendEmptyArray() {
		LongSequence appendedEmpty = empty.append();
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextLong());
	}

	@Test
	public void appendArray() {
		LongSequence appended = _123.append(4, 5, 6).append(7, 8);

		twice(() -> assertThat(appended, containsLongs(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendEmptyLongIterable() {
		LongSequence appendedEmpty = empty.append(LongIterable.of());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextLong());
	}

	@Test
	public void appendLongIterable() {
		LongSequence appended = _123.append(LongIterable.of(4, 5, 6)).append(LongIterable.of(7, 8));

		twice(() -> assertThat(appended, containsLongs(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIterable() {
		LongSequence appended = _123.append(Iterables.of(4L, 5L, 6L)).append(Iterables.of(7L, 8L));

		twice(() -> assertThat(appended, containsLongs(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendEmptyLongIterator() {
		LongSequence appendedEmpty = empty.append(LongIterator.of());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextLong());
	}

	@Test
	public void appendLongIterator() {
		LongSequence appended = _123.append(LongIterator.of(4, 5, 6)).append(LongIterator.of(7, 8));

		assertThat(appended, containsLongs(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, containsLongs(1, 2, 3));
	}

	@Test
	public void appendLongIteratorAsIterator() {
		LongSequence appended = _123.append((Iterator<Long>) LongIterator.of(4, 5, 6))
		                            .append((Iterator<Long>) LongIterator.of(7, 8));

		assertThat(appended, containsLongs(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, containsLongs(1, 2, 3));
	}

	@Test
	public void appendIterator() {
		LongSequence appended = _123.append(Iterators.of(4L, 5L, 6L)).append(Iterators.of(7L, 8L));

		assertThat(appended, containsLongs(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, containsLongs(1, 2, 3));
	}

	@Test
	public void appendEmptyLongStream() {
		LongSequence appendedEmpty = empty.append(LongStream.of());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextLong());
	}

	@Test
	public void appendLongStream() {
		LongSequence appended = _123.append(LongStream.of(4, 5, 6)).append(LongStream.of(7, 8));

		assertThat(appended, containsLongs(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, containsLongs(1, 2, 3));
	}

	@Test
	public void appendStream() {
		LongSequence appended = _123.append(Stream.of(4L, 5L, 6L)).append(Stream.of(7L, 8L));

		assertThat(appended, containsLongs(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, containsLongs(1, 2, 3));
	}

	@Test
	public void appendIsLazy() {
		LongIterator first = LongIterator.of(1, 2, 3);
		LongIterator second = LongIterator.of(4, 5, 6);
		LongIterator third = LongIterator.of(7, 8);

		LongSequence.once(first).append(second).append(third);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));
		assertThat(third.hasNext(), is(true));
	}

	@Test
	public void appendIsLazyWhenSkippingHasNext() {
		LongIterator first = LongIterator.of(1);
		LongIterator second = LongIterator.of(2);

		LongSequence sequence = LongSequence.once(first).append(second);

		// check delayed iteration
		LongIterator iterator = sequence.iterator();
		assertThat(iterator.nextLong(), is(1L));
		assertThat(iterator.nextLong(), is(2L));
		assertThat(iterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, iterator::nextLong);

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		LongSequence emptyFiltered = empty.filter(x -> (x % 2) == 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().nextLong());

		LongSequence oneFiltered = _1.filter(x -> (x % 2) == 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		LongSequence twoFiltered = _12.filter(x -> (x % 2) == 0);
		twice(() -> assertThat(twoFiltered, containsLongs(2)));

		assertThat(removeFirst(twoFiltered), is(2L));
		twice(() -> assertThat(twoFiltered, is(emptyIterable())));
		twice(() -> assertThat(_12, containsLongs(1)));

		LongSequence filtered = _123456789.filter(x -> (x % 2) == 0);
		twice(() -> assertThat(filtered, containsLongs(2, 4, 6, 8)));
	}

	@Test
	public void filterIndexed() {
		LongSequence emptyFiltered = empty.filterIndexed((x, i) -> i > 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().nextLong());

		LongSequence oneFiltered = _1.filterIndexed((x, i) -> i > 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		LongSequence twoFiltered = _12.filterIndexed((x, i) -> i > 0);
		twice(() -> assertThat(twoFiltered, containsLongs(2)));

		assertThat(removeFirst(twoFiltered), is(2L));
		twice(() -> assertThat(twoFiltered, is(emptyIterable())));
		twice(() -> assertThat(_12, containsLongs(1)));

		LongSequence filtered = _123456789.filterIndexed((x, i) -> i > 3);
		twice(() -> assertThat(filtered, containsLongs(5, 6, 7, 8, 9)));
	}

	@Test
	public void filterBack() {
		LongSequence emptyFilteredLess = empty.filterBack(117, (p, x) -> p < x);
		twice(() -> assertThat(emptyFilteredLess, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().nextLong());

		LongSequence filteredLess = nineRandom.filterBack(117, (p, x) -> p < x);
		twice(() -> assertThat(filteredLess, containsLongs(1, 2, 17)));

		LongSequence filteredGreater = nineRandom.filterBack(117, (p, x) -> p > x);
		twice(() -> assertThat(filteredGreater, containsLongs(6, 1, -7, 5, 4)));

		assertThat(removeFirst(filteredGreater), is(6L));
		twice(() -> assertThat(filteredGreater, containsLongs(6, 1, -7, 5, 4)));
		twice(() -> assertThat(nineRandom, containsLongs(6, 1, -7, 1, 2, 17, 5, 4)));
	}

	@Test
	public void filterForward() {
		LongSequence emptyFilteredLess = empty.filterForward(117, (x, n) -> x < n);
		twice(() -> assertThat(emptyFilteredLess, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().nextLong());

		LongSequence filteredLess = nineRandom.filterForward(117, (x, n) -> n < x);
		twice(() -> assertThat(filteredLess, containsLongs(6, 1, 17, 5)));

		LongSequence filteredGreater = nineRandom.filterForward(117, (x, n) -> n > x);
		twice(() -> assertThat(filteredGreater, containsLongs(-7, 1, 2, 4)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(filteredGreater));
		twice(() -> assertThat(filteredGreater, containsLongs(-7, 1, 2, 4)));
	}

	@Test
	public void includingArray() {
		LongSequence emptyIncluding = empty.including(1, 3, 5, 17);
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyIncluding.iterator().nextLong());

		LongSequence including = _12345.including(1, 3, 5, 17);
		twice(() -> assertThat(including, containsLongs(1, 3, 5)));

		LongSequence includingAll = _12345.including(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(includingAll, containsLongs(1, 2, 3, 4, 5)));

		LongSequence includingNone = _12345.including();
		twice(() -> assertThat(includingNone, is(emptyIterable())));

		assertThat(removeFirst(including), is(1L));
		twice(() -> assertThat(including, containsLongs(3, 5)));
		twice(() -> assertThat(_12345, containsLongs(2, 3, 4, 5)));
	}

	@Test
	public void excludingArray() {
		LongSequence emptyExcluding = empty.excluding(1, 3, 5, 17);
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyExcluding.iterator().nextLong());

		LongSequence excluding = _12345.excluding(1, 3, 5, 17);
		twice(() -> assertThat(excluding, containsLongs(2, 4)));

		LongSequence excludingAll = _12345.excluding(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		LongSequence excludingNone = _12345.excluding();
		twice(() -> assertThat(excludingNone, containsLongs(1, 2, 3, 4, 5)));

		assertThat(removeFirst(excluding), is(2L));
		twice(() -> assertThat(excluding, containsLongs(4)));
		twice(() -> assertThat(_12345, containsLongs(1, 3, 4, 5)));
	}

	@Test
	public void map() {
		LongSequence emptyMapped = empty.map(l -> l + 1);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().nextLong());

		LongSequence oneMapped = _1.map(l -> l + 1);
		twice(() -> assertThat(oneMapped, containsLongs(2)));

		assertThat(removeFirst(oneMapped), is(2L));
		twice(() -> assertThat(oneMapped, is(emptyIterable())));
		twice(() -> assertThat(_1, is(emptyIterable())));

		LongSequence twoMapped = _12.map(l -> l + 1);
		twice(() -> assertThat(twoMapped, containsLongs(2, 3)));

		LongSequence fiveMapped = _12345.map(l -> l + 1);
		twice(() -> assertThat(fiveMapped, containsLongs(2, 3, 4, 5, 6)));
	}

	@Test
	public void mapWithIndex() {
		LongSequence emptyMapped = empty.mapIndexed((i, x) -> i + x);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().nextLong());

		LongSequence oneMapped = _1.mapIndexed((i, x) -> i + x);
		twice(() -> assertThat(oneMapped, containsLongs(1)));

		assertThat(removeFirst(oneMapped), is(1L));
		twice(() -> assertThat(oneMapped, is(emptyIterable())));
		twice(() -> assertThat(_1, is(emptyIterable())));

		LongSequence twoMapped = _12.mapIndexed((i, x) -> i + x);
		twice(() -> assertThat(twoMapped, containsLongs(1, 3)));

		LongSequence mapped = _12345.mapIndexed((i, x) -> i + x);
		twice(() -> assertThat(mapped, containsLongs(1, 3, 5, 7, 9)));
	}

	@Test
	public void recurse() {
		LongSequence recursive = LongSequence.recurse(1L, x -> x + 1);
		twice(() -> assertThat(recursive.limit(10), containsLongs(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void untilTerminal() {
		LongSequence emptyUntil7 = empty.until(7);
		twice(() -> assertThat(emptyUntil7, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntil7.iterator().nextLong());

		LongSequence nineUntil7 = _123456789.until(7);
		twice(() -> assertThat(nineUntil7, containsLongs(1, 2, 3, 4, 5, 6)));

		assertThat(removeFirst(nineUntil7), is(1L));
		twice(() -> assertThat(nineUntil7, containsLongs(2, 3, 4, 5, 6)));
		twice(() -> assertThat(_123456789, containsLongs(2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void untilPredicate() {
		LongSequence emptyUntilEqual7 = empty.until(i -> i == 7);
		twice(() -> assertThat(emptyUntilEqual7, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntilEqual7.iterator().nextLong());

		LongSequence nineUntilEqual7 = _123456789.until(i -> i == 7);
		twice(() -> assertThat(nineUntilEqual7, containsLongs(1, 2, 3, 4, 5, 6)));

		assertThat(removeFirst(nineUntilEqual7), is(1L));
		twice(() -> assertThat(nineUntilEqual7, containsLongs(2, 3, 4, 5, 6)));
		twice(() -> assertThat(_123456789, containsLongs(2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void endingAtTerminal() {
		LongSequence emptyEndingAt7 = empty.endingAt(7);
		twice(() -> assertThat(emptyEndingAt7, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAt7.iterator().nextLong());

		LongSequence nineEndingAt7 = _123456789.endingAt(7);
		twice(() -> assertThat(nineEndingAt7, containsLongs(1, 2, 3, 4, 5, 6, 7)));

		assertThat(removeFirst(nineEndingAt7), is(1L));
		twice(() -> assertThat(nineEndingAt7, containsLongs(2, 3, 4, 5, 6, 7)));
		twice(() -> assertThat(_123456789, containsLongs(2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void endingAtPredicate() {
		LongSequence emptyEndingAtEqual7 = empty.endingAt(i -> i == 7);
		twice(() -> assertThat(emptyEndingAtEqual7, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAtEqual7.iterator().nextLong());

		LongSequence nineEndingAtEqual7 = _123456789.endingAt(i -> i == 7);
		twice(() -> assertThat(nineEndingAtEqual7, containsLongs(1, 2, 3, 4, 5, 6, 7)));

		assertThat(removeFirst(nineEndingAtEqual7), is(1L));
		twice(() -> assertThat(nineEndingAtEqual7, containsLongs(2, 3, 4, 5, 6, 7)));
		twice(() -> assertThat(_123456789, containsLongs(2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void startingAfter() {
		LongSequence emptyStartingAfter5 = empty.startingAfter(5);
		twice(() -> assertThat(emptyStartingAfter5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingAfter5.iterator().nextLong());

		LongSequence nineStartingAfter5 = _123456789.startingAfter(5);
		twice(() -> assertThat(nineStartingAfter5, containsLongs(6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingAfter5), is(6L));
		twice(() -> assertThat(nineStartingAfter5, containsLongs(7, 8, 9)));
		twice(() -> assertThat(_123456789, containsLongs(1, 2, 3, 4, 5, 7, 8, 9)));

		LongSequence fiveStartingAfter10 = _12345.startingAfter(10);
		twice(() -> assertThat(fiveStartingAfter10, is(emptyIterable())));
	}

	@Test
	public void startingAfterPredicate() {
		LongSequence emptyStartingAfterEqual5 = empty.startingAfter(x -> x == 5);
		twice(() -> assertThat(emptyStartingAfterEqual5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingAfterEqual5.iterator().nextLong());

		LongSequence nineStartingAfterEqual5 = _123456789.startingAfter(x -> x == 5);
		twice(() -> assertThat(nineStartingAfterEqual5, containsLongs(6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingAfterEqual5), is(6L));
		twice(() -> assertThat(nineStartingAfterEqual5, containsLongs(7, 8, 9)));
		twice(() -> assertThat(_123456789, containsLongs(1, 2, 3, 4, 5, 7, 8, 9)));

		LongSequence fiveStartingAfterEqual10 = _12345.startingAfter(x -> x == 10);
		twice(() -> assertThat(fiveStartingAfterEqual10, is(emptyIterable())));
	}

	@Test
	public void startingFrom() {
		LongSequence emptyStartingFrom5 = empty.startingFrom(5);
		twice(() -> assertThat(emptyStartingFrom5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingFrom5.iterator().nextLong());

		LongSequence nineStartingFrom5 = _123456789.startingFrom(5);
		twice(() -> assertThat(nineStartingFrom5, containsLongs(5, 6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingFrom5), is(5L));
		twice(() -> assertThat(nineStartingFrom5, is(emptyIterable())));
		twice(() -> assertThat(_123456789, containsLongs(1, 2, 3, 4, 6, 7, 8, 9)));

		LongSequence fiveStartingFrom10 = _12345.startingFrom(10);
		twice(() -> assertThat(fiveStartingFrom10, is(emptyIterable())));
	}

	@Test
	public void startingFromPredicate() {
		LongSequence emptyStartingFromEqual5 = empty.startingFrom(x -> x == 5);
		twice(() -> assertThat(emptyStartingFromEqual5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingFromEqual5.iterator().nextLong());

		LongSequence nineStartingFromEqual5 = _123456789.startingFrom(x -> x == 5);
		twice(() -> assertThat(nineStartingFromEqual5, containsLongs(5, 6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingFromEqual5), is(5L));
		twice(() -> assertThat(nineStartingFromEqual5, is(emptyIterable())));
		twice(() -> assertThat(_123456789, containsLongs(1, 2, 3, 4, 6, 7, 8, 9)));

		LongSequence fiveStartingFromEqual10 = _12345.startingFrom(x -> x == 10);
		twice(() -> assertThat(fiveStartingFromEqual10, is(emptyIterable())));
	}

	@Test
	public void toList() {
		twice(() -> {
			LongList list = _12345.toList();
			assertThat(list, is(instanceOf(ArrayLongList.class)));
			assertThat(list, containsLongs(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSet() {
		twice(() -> {
			LongSet set = _12345.toSet();
			assertThat(set, instanceOf(BitLongSet.class));
			assertThat(set, containsLongs(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSortedSet() {
		twice(() -> {
			LongSortedSet sortedSet = _12345.toSortedSet();
			assertThat(sortedSet, instanceOf(BitLongSet.class));
			assertThat(sortedSet, containsLongs(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSetWithType() {
		twice(() -> {
			LongSet set = _12345.toSet(BitLongSet::new);
			assertThat(set, instanceOf(BitLongSet.class));
			assertThat(set, containsLongs(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toCollection() {
		twice(() -> {
			LongList list = _12345.toCollection(LongList::create);
			assertThat(list, instanceOf(ArrayLongList.class));
			assertThat(list, containsLongs(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void collectIntoLongCollection() {
		twice(() -> {
			LongList list = LongList.create();
			LongList result = _12345.collectInto(list);

			assertThat(result, is(sameInstance(list)));
			assertThat(result, containsLongs(1, 2, 3, 4, 5));
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
			LongList list = LongList.create();
			LongList result = _12345.collectInto(list, LongList::addLong);

			assertThat(result, is(sameInstance(list)));
			assertThat(result, containsLongs(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(Arrays.equals(_123.toLongArray(), new long[]{1, 2, 3}), is(true)));
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
		LongBinaryOperator secondLong = (x1, x2) -> x2;
		twice(() -> {
			assertThat(empty.reduce(secondLong), is(OptionalLong.empty()));
			assertThat(_1.reduce(secondLong), is(OptionalLong.of(1)));
			assertThat(_12.reduce(secondLong), is(OptionalLong.of(2)));
			assertThat(_123.reduce(secondLong), is(OptionalLong.of(3)));
		});
	}

	@Test
	public void reduceWithIdentity() {
		LongBinaryOperator secondLong = (x1, x2) -> x2;
		twice(() -> {
			assertThat(empty.reduce(17L, secondLong), is(17L));
			assertThat(_1.reduce(17L, secondLong), is(1L));
			assertThat(_12.reduce(17L, secondLong), is(2L));
			assertThat(_123.reduce(17L, secondLong), is(3L));
		});
	}

	@Test
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(OptionalLong.empty()));
			assertThat(_1.first(), is(OptionalLong.of(1)));
			assertThat(_12.first(), is(OptionalLong.of(1)));
			assertThat(_123.first(), is(OptionalLong.of(1)));
		});
	}

	@Test
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(OptionalLong.empty()));
			assertThat(_1.last(), is(OptionalLong.of(1)));
			assertThat(_12.last(), is(OptionalLong.of(2)));
			assertThat(_123.last(), is(OptionalLong.of(3)));
		});
	}

	@Test
	public void at() {
		twice(() -> {
			assertThat(empty.at(0), is(OptionalLong.empty()));
			assertThat(empty.at(17), is(OptionalLong.empty()));

			assertThat(_1.at(0), is(OptionalLong.of(1)));
			assertThat(_1.at(1), is(OptionalLong.empty()));
			assertThat(_1.at(17), is(OptionalLong.empty()));

			assertThat(_12345.at(0), is(OptionalLong.of(1)));
			assertThat(_12345.at(1), is(OptionalLong.of(2)));
			assertThat(_12345.at(4), is(OptionalLong.of(5)));
			assertThat(_12345.at(17), is(OptionalLong.empty()));
		});
	}

	@Test
	public void firstByPredicate() {
		twice(() -> {
			assertThat(empty.first(x -> x > 1), is(OptionalLong.empty()));
			assertThat(_1.first(x -> x > 1), is(OptionalLong.empty()));
			assertThat(_12.first(x -> x > 1), is(OptionalLong.of(2)));
			assertThat(_12345.first(x -> x > 1), is(OptionalLong.of(2)));
		});
	}

	@Test
	public void lastByPredicate() {
		twice(() -> {
			assertThat(empty.last(x -> x > 1), is(OptionalLong.empty()));
			assertThat(_1.last(x -> x > 1), is(OptionalLong.empty()));
			assertThat(_12.last(x -> x > 1), is(OptionalLong.of(2)));
			assertThat(_12345.last(x -> x > 1), is(OptionalLong.of(5)));
		});
	}

	@Test
	public void atByPredicate() {
		twice(() -> {
			assertThat(empty.at(0, x -> x > 1), is(OptionalLong.empty()));
			assertThat(empty.at(17, x -> x > 1), is(OptionalLong.empty()));

			assertThat(_1.at(0, x -> x > 1), is(OptionalLong.empty()));
			assertThat(_1.at(17, x -> x > 1), is(OptionalLong.empty()));

			assertThat(_12.at(0, x -> x > 1), is(OptionalLong.of(2)));
			assertThat(_12.at(1, x -> x > 1), is(OptionalLong.empty()));
			assertThat(_12.at(17, x -> x > 1), is(OptionalLong.empty()));

			assertThat(_12345.at(0, x -> x > 1), is(OptionalLong.of(2)));
			assertThat(_12345.at(1, x -> x > 1), is(OptionalLong.of(3)));
			assertThat(_12345.at(3, x -> x > 1), is(OptionalLong.of(5)));
			assertThat(_12345.at(4, x -> x > 1), is(OptionalLong.empty()));
			assertThat(_12345.at(17, x -> x > 1), is(OptionalLong.empty()));
		});
	}

	@Test
	public void step() {
		LongSequence emptyStep3 = empty.step(3);
		twice(() -> assertThat(emptyStep3, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStep3.iterator().nextLong());

		LongSequence nineStep3 = _123456789.step(3);
		twice(() -> assertThat(nineStep3, containsLongs(1, 4, 7)));

		LongIterator nineStep3Iterator = nineStep3.iterator();
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.hasNext(), is(true));
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.nextLong(), is(1L));
		nineStep3Iterator.remove();
		assertThat(nineStep3Iterator.hasNext(), is(true));
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.nextLong(), is(4L));
		nineStep3Iterator.remove();

		twice(() -> assertThat(nineStep3, containsLongs(2, 6, 9)));
		twice(() -> assertThat(_123456789, containsLongs(2, 3, 5, 6, 7, 8, 9)));
	}

	@Test
	public void distinct() {
		LongSequence emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptyDistinct.iterator().nextLong());

		LongSequence oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, containsLongs(17)));

		LongSequence twoDuplicatesDistinct = LongSequence.from(LongList.create(17, 17)).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, containsLongs(17)));

		LongSequence nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, containsLongs(6, 1, -7, 2, 17, 5, 4)));

		assertThat(removeFirst(nineDistinct), is(6L));
		twice(() -> assertThat(nineDistinct, containsLongs(6, 1, -7, 2, 17, 5, 4)));
		twice(() -> assertThat(nineRandom, containsLongs(6, 1, -7, 1, 2, 17, 5, 4)));
	}

	@Test
	public void sorted() {
		LongSequence emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptySorted.iterator().nextLong());

		LongSequence oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, containsLongs(17)));

		LongSequence twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, containsLongs(17, 32)));

		LongSequence nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, containsLongs(-7, 1, 1, 2, 4, 5, 6, 6, 17)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSorted));
		twice(() -> assertThat(nineSorted, containsLongs(-7, 1, 1, 2, 4, 5, 6, 6, 17)));
		twice(() -> assertThat(nineRandom, containsLongs(6, 6, 1, -7, 1, 2, 17, 5, 4)));
	}

	@Test
	public void sortedWithUpdates() {
		List<Long> backing = new ArrayList<>(Lists.of(2L, 3L, 1L));
		LongSequence sorted = LongSequence.from(backing).sorted();

		backing.add(4L);
		assertThat(sorted, containsLongs(1, 2, 3, 4));
	}

	@Test
	public void min() {
		twice(() -> assertThat(empty.min(), is(OptionalLong.empty())));
		twice(() -> assertThat(oneRandom.min(), is(OptionalLong.of(17))));
		twice(() -> assertThat(twoRandom.min(), is(OptionalLong.of(17))));
		twice(() -> assertThat(nineRandom.min(), is(OptionalLong.of(-7))));
	}

	@Test
	public void max() {
		twice(() -> assertThat(empty.max(), is(OptionalLong.empty())));
		twice(() -> assertThat(oneRandom.max(), is(OptionalLong.of(17))));
		twice(() -> assertThat(twoRandom.max(), is(OptionalLong.of(32))));
		twice(() -> assertThat(nineRandom.max(), is(OptionalLong.of(17))));
	}

	@Test
	public void sum() {
		twice(() -> assertThat(empty.sum(), is(0L)));
		twice(() -> assertThat(_12345.sum(), is(15L)));
		twice(() -> assertThat(oneRandom.sum(), is(17L)));
		twice(() -> assertThat(twoRandom.sum(), is(49L)));
		twice(() -> assertThat(nineRandom.sum(), is(35L)));

		LongSequence fiveMaxValues = LongSequence.of(Long.MAX_VALUE).repeat(5);
		twice(() -> assertThat(fiveMaxValues.sum(), is(Long.MAX_VALUE * 5)));
	}

	@Test
	public void average() {
		twice(() -> assertThat(empty.average(), is(OptionalDouble.empty())));
		twice(() -> assertThat(_12345.average(), is(OptionalDouble.of(3))));
		twice(() -> assertThat(oneRandom.average(), is(OptionalDouble.of(17))));
		twice(() -> assertThat(twoRandom.average(), is(OptionalDouble.of(24.5))));
		twice(() -> assertThat(nineRandom.average(), is(OptionalDouble.of(35.0 / 9))));

		LongSequence fiveMaxValues = LongSequence.of(Long.MAX_VALUE).repeat(5);
		twice(() -> assertThat(fiveMaxValues.average(), is(OptionalDouble.of(Long.MAX_VALUE))));
	}

	@Test
	public void statistics() {
		twice(() -> {
			LongSummaryStatistics emptyStatistics = empty.statistics();
			assertThat(emptyStatistics.getCount(), is(0L));
			assertThat(emptyStatistics.getMin(), is(Long.MAX_VALUE));
			assertThat(emptyStatistics.getMax(), is(Long.MIN_VALUE));
			assertThat(emptyStatistics.getAverage(), is(0.0));
		});

		twice(() -> {
			LongSummaryStatistics fiveStatistics = _12345.statistics();
			assertThat(fiveStatistics.getCount(), is(5L));
			assertThat(fiveStatistics.getMin(), is(1L));
			assertThat(fiveStatistics.getMax(), is(5L));
			assertThat(fiveStatistics.getAverage(), is(3.0));
		});

		twice(() -> {
			LongSummaryStatistics oneRandomStatistics = oneRandom.statistics();
			assertThat(oneRandomStatistics.getCount(), is(1L));
			assertThat(oneRandomStatistics.getMin(), is(17L));
			assertThat(oneRandomStatistics.getMax(), is(17L));
			assertThat(oneRandomStatistics.getAverage(), is(17.0));
		});

		twice(() -> {
			LongSummaryStatistics twoRandomStatistics = twoRandom.statistics();
			assertThat(twoRandomStatistics.getCount(), is(2L));
			assertThat(twoRandomStatistics.getMin(), is(17L));
			assertThat(twoRandomStatistics.getMax(), is(32L));
			assertThat(twoRandomStatistics.getAverage(), is(24.5));
		});

		twice(() -> {
			LongSummaryStatistics nineRandomStatistics = nineRandom.statistics();
			assertThat(nineRandomStatistics.getCount(), is(9L));
			assertThat(nineRandomStatistics.getMin(), is(-7L));
			assertThat(nineRandomStatistics.getMax(), is(17L));
			assertThat(nineRandomStatistics.getAverage(), is(35.0 / 9));
		});
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
		LongSequence emptyPeeked = empty.peek(x -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyPeeked, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().nextLong());

		AtomicLong value = new AtomicLong(1);
		LongSequence onePeeked = _1.peek(x -> assertThat(x, is(value.getAndIncrement())));
		twiceIndexed(value, 1, () -> assertThat(onePeeked, containsLongs(1)));

		LongSequence twoPeeked = _12.peek(x -> assertThat(x, is(value.getAndIncrement())));
		twiceIndexed(value, 2, () -> assertThat(twoPeeked, containsLongs(1, 2)));

		LongSequence fivePeeked = _12345.peek(x -> assertThat(x, is(value.getAndIncrement())));
		twiceIndexed(value, 5, () -> assertThat(fivePeeked, containsLongs(1, 2, 3, 4, 5)));

		assertThat(removeFirst(fivePeeked), is(1L));
		twiceIndexed(value, 4, () -> assertThat(fivePeeked, containsLongs(2, 3, 4, 5)));
		twice(() -> assertThat(_12345, containsLongs(2, 3, 4, 5)));
	}

	@Test
	public void peekIndexed() {
		LongSequence emptyPeeked = empty.peekIndexed((x, i) -> fail("Should not get called"));
		twice(() -> assertThat(emptyPeeked, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().nextLong());

		AtomicInteger index = new AtomicInteger();
		AtomicLong value = new AtomicLong(1);
		LongSequence onePeeked = _1.peekIndexed((x, i) -> {
			assertThat(x, is(value.getAndIncrement()));
			assertThat(i, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(onePeeked, containsLongs(1));

			assertThat(index.get(), is(1));
			assertThat(value.get(), is(2L));
			index.set(0);
			value.set(1);
		});

		LongSequence twoPeeked = _12.peekIndexed((x, i) -> {
			assertThat(x, is(value.getAndIncrement()));
			assertThat(i, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(twoPeeked, containsLongs(1, 2));

			assertThat(index.get(), is(2));
			assertThat(value.get(), is(3L));
			index.set(0);
			value.set(1);
		});

		LongSequence fivePeeked = _12345.peekIndexed((x, i) -> {
			assertThat(x, is(value.getAndIncrement()));
			assertThat(i, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(fivePeeked, containsLongs(1, 2, 3, 4, 5));

			assertThat(index.get(), is(5));
			assertThat(value.get(), is(6L));
			index.set(0);
			value.set(1);
		});

		assertThat(removeFirst(fivePeeked), is(1L));
		index.set(0);
		value.set(2);
		twice(() -> {
			assertThat(fivePeeked, containsLongs(2, 3, 4, 5));
			assertThat(index.get(), is(4));
			assertThat(value.get(), is(6L));
			index.set(0);
			value.set(2);
		});

		twice(() -> assertThat(_12345, containsLongs(2, 3, 4, 5)));
	}

	@Test
	public void stream() {
		twice(() -> assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable())));
		twice(() -> assertThat(_12345.stream().collect(Collectors.toList()), contains(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void streamFromOnce() {
		LongSequence empty = LongSequence.once(LongIterator.of());
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));

		LongSequence sequence = LongSequence.once(LongIterator.of(1, 2, 3, 4, 5));
		assertThat(sequence.stream().collect(Collectors.toList()), contains(1L, 2L, 3L, 4L, 5L));
		assertThat(sequence.stream().collect(Collectors.toList()), is(emptyIterable()));
	}

	@Test
	public void longStream() {
		twice(() -> assertThat(empty.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		                       is(emptyIterable())));

		twice(() -> assertThat(_12345.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		                       containsLongs(1, 2, 3, 4, 5)));
	}

	@Test
	public void longStreamFromOnce() {
		LongSequence empty = LongSequence.once(LongIterator.of());
		assertThat(empty.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           is(emptyIterable()));
		assertThat(empty.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           is(emptyIterable()));

		LongSequence sequence = LongSequence.once(LongIterator.of(1, 2, 3, 4, 5));
		assertThat(sequence.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           containsLongs(1, 2, 3, 4, 5));
		assertThat(sequence.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           is(emptyIterable()));
	}

	@Test
	public void prefix() {
		LongSequence emptyPrefixed = empty.prefix(327);
		twice(() -> assertThat(emptyPrefixed, containsLongs(327)));

		LongIterator emptyIterator = emptyPrefixed.iterator();
		emptyIterator.nextLong();
		expecting(NoSuchElementException.class, emptyIterator::nextLong);

		LongSequence threePrefixed = _123.prefix(327);
		twice(() -> assertThat(threePrefixed, containsLongs(327, 1, 2, 3)));

		LongIterator iterator = threePrefixed.iterator();
		expecting(UnsupportedOperationException.class, () -> {
			iterator.nextLong();
			iterator.remove();
		});
		assertThat(iterator.nextLong(), is(1L));
		iterator.remove();
		twice(() -> assertThat(threePrefixed, containsLongs(327, 2, 3)));
		twice(() -> assertThat(_123, containsLongs(2, 3)));
	}

	@Test
	public void suffix() {
		LongSequence emptySuffixed = empty.suffix(532);
		twice(() -> assertThat(emptySuffixed, containsLongs(532)));

		LongIterator emptyIterator = emptySuffixed.iterator();
		emptyIterator.nextLong();
		expecting(NoSuchElementException.class, emptyIterator::nextLong);

		LongSequence threeSuffixed = _123.suffix(532);
		twice(() -> assertThat(threeSuffixed, containsLongs(1, 2, 3, 532)));

		assertThat(removeFirst(threeSuffixed), is(1L));
		twice(() -> assertThat(threeSuffixed, containsLongs(2, 3, 532)));
		twice(() -> assertThat(_123, containsLongs(2, 3)));
	}

	@Test
	public void interleave() {
		LongSequence emptyInterleaved = empty.interleave(empty);
		twice(() -> assertThat(emptyInterleaved, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyInterleaved.iterator().nextLong());

		LongSequence interleavedShortFirst = _123.interleave(_12345);
		twice(() -> assertThat(interleavedShortFirst, containsLongs(1, 1, 2, 2, 3, 3, 4, 5)));

		LongSequence interleavedShortLast = _12345.interleave(_123);
		twice(() -> assertThat(interleavedShortLast, containsLongs(1, 1, 2, 2, 3, 3, 4, 5)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(interleavedShortFirst));
		twice(() -> assertThat(interleavedShortLast, containsLongs(1, 1, 2, 2, 3, 3, 4, 5)));
	}

	@Test
	public void reverse() {
		LongSequence emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyReversed.iterator().nextLong());

		LongSequence oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, containsLongs(1)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneReversed));
		twice(() -> assertThat(oneReversed, containsLongs(1)));
		twice(() -> assertThat(_1, containsLongs(1)));

		LongSequence twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, containsLongs(2, 1)));

		LongSequence threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, containsLongs(3, 2, 1)));

		LongSequence nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed, containsLongs(9, 8, 7, 6, 5, 4, 3, 2, 1)));
	}

	@Test
	public void reverseWithUpdates() {
		List<Long> backing = new ArrayList<>(Lists.of(1L, 2L, 3L));
		LongSequence reversed = LongSequence.from(backing).reverse();

		backing.add(4L);
		assertThat(reversed, containsLongs(4L, 3L, 2L, 1L));
	}

	@Test
	public void positive() {
		LongSequence positive = LongSequence.positive();
		twice(() -> assertThat(positive.limit(5), containsLongs(1, 2, 3, 4, 5)));
	}

	@Test
	public void positiveFromZero() {
		LongSequence positiveFromZero = LongSequence.positiveFromZero();
		twice(() -> assertThat(positiveFromZero.limit(5), containsLongs(0, 1, 2, 3, 4)));
	}

	@Test
	public void negative() {
		LongSequence negative = LongSequence.negative();
		twice(() -> assertThat(negative.limit(5), containsLongs(-1, -2, -3, -4, -5)));
	}

	@Test
	public void negativeFromZero() {
		LongSequence negativeFromZero = LongSequence.negativeFromZero();
		twice(() -> assertThat(negativeFromZero.limit(5), containsLongs(0, -1, -2, -3, -4)));
	}

	@Test
	public void decreasingFrom() {
		LongSequence decreasing = LongSequence.decreasingFrom(-10);
		twice(() -> assertThat(decreasing.limit(5), containsLongs(-10, -11, -12, -13, -14)));

		LongSequence decreasingFrom2 = LongSequence.decreasingFrom(2);
		twice(() -> assertThat(decreasingFrom2.limit(5), containsLongs(2, 1, 0, -1, -2)));

		LongSequence decreasingFromMinValue = LongSequence.decreasingFrom(Long.MIN_VALUE);
		twice(() -> assertThat(decreasingFromMinValue.limit(3),
		                       containsLongs(Long.MIN_VALUE, Long.MAX_VALUE, Long.MAX_VALUE - 1)));
	}

	@Test
	public void increasingFrom() {
		LongSequence increasingFrom10 = LongSequence.increasingFrom(10);
		twice(() -> assertThat(increasingFrom10.limit(5), containsLongs(10, 11, 12, 13, 14)));

		LongSequence increasingFrom_2 = LongSequence.increasingFrom(-2);
		twice(() -> assertThat(increasingFrom_2.limit(5), containsLongs(-2, -1, 0, 1, 2)));

		LongSequence increasingFromMaxValue = LongSequence.increasingFrom(Long.MAX_VALUE);
		twice(() -> assertThat(increasingFromMaxValue.limit(3),
		                       containsLongs(Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE + 1)));
	}

	@Test
	public void steppingFrom() {
		LongSequence steppingFrom0Step10 = LongSequence.steppingFrom(0, 10);
		twice(() -> assertThat(steppingFrom0Step10.limit(5), containsLongs(0, 10, 20, 30, 40)));

		LongSequence steppingFrom0Step_10 = LongSequence.steppingFrom(0, -10);
		twice(() -> assertThat(steppingFrom0Step_10.limit(5), containsLongs(0, -10, -20, -30, -40)));

		LongSequence steppingFromMaxValueStep10 = LongSequence.steppingFrom(Long.MAX_VALUE, 10);
		twice(() -> assertThat(steppingFromMaxValueStep10.limit(3),
		                       containsLongs(Long.MAX_VALUE, Long.MIN_VALUE + 9, Long.MIN_VALUE + 19)));
	}

	@Test
	public void range() {
		LongSequence range1to6 = LongSequence.range(1, 6);
		twice(() -> assertThat(range1to6, containsLongs(1, 2, 3, 4, 5, 6)));

		LongSequence range6to1 = LongSequence.range(6, 1);
		twice(() -> assertThat(range6to1, containsLongs(6, 5, 4, 3, 2, 1)));

		LongSequence range_2to2 = LongSequence.range(-2, 2);
		twice(() -> assertThat(range_2to2, containsLongs(-2, -1, 0, 1, 2)));

		LongSequence range2to_2 = LongSequence.range(2, -2);
		twice(() -> assertThat(range2to_2, containsLongs(2, 1, 0, -1, -2)));

		LongSequence maxValue = LongSequence.range(Long.MAX_VALUE - 3, Long.MAX_VALUE);
		twice(() -> assertThat(maxValue, containsLongs(Long.MAX_VALUE - 3, Long.MAX_VALUE - 2, Long.MAX_VALUE - 1,
		                                               Long.MAX_VALUE)));

		LongSequence minValue = LongSequence.range(Long.MIN_VALUE + 3, Long.MIN_VALUE);
		twice(() -> assertThat(minValue, containsLongs(Long.MIN_VALUE + 3, Long.MIN_VALUE + 2, Long.MIN_VALUE + 1,
		                                               Long.MIN_VALUE)));
	}

	@Test
	public void rangeWithStep() {
		LongSequence range1to6step2 = LongSequence.range(1, 6, 2);
		twice(() -> assertThat(range1to6step2, containsLongs(1, 3, 5)));

		LongSequence range6to1step2 = LongSequence.range(6, 1, 2);
		twice(() -> assertThat(range6to1step2, containsLongs(6, 4, 2)));

		LongSequence range_6to6step2 = LongSequence.range(-6, 6, 3);
		twice(() -> assertThat(range_6to6step2, containsLongs(-6, -3, 0, 3, 6)));

		LongSequence range6to_6step2 = LongSequence.range(6, -6, 3);
		twice(() -> assertThat(range6to_6step2, containsLongs(6, 3, 0, -3, -6)));

		LongSequence maxValue = LongSequence.range(Long.MAX_VALUE - 2, Long.MAX_VALUE, 2);
		twice(() -> assertThat(maxValue, containsLongs(Long.MAX_VALUE - 2, Long.MAX_VALUE)));

		LongSequence minValue = LongSequence.range(Long.MIN_VALUE + 2, Long.MIN_VALUE, 2);
		twice(() -> assertThat(minValue, containsLongs(Long.MIN_VALUE + 2, Long.MIN_VALUE)));

		LongSequence crossingMaxValue = LongSequence.range(Long.MAX_VALUE - 3, Long.MAX_VALUE, 2);
		twice(() -> assertThat(crossingMaxValue, containsLongs(Long.MAX_VALUE - 3, Long.MAX_VALUE - 1)));

		LongSequence crossingMinValue = LongSequence.range(Long.MIN_VALUE + 3, Long.MIN_VALUE, 2);
		twice(() -> assertThat(crossingMinValue, containsLongs(Long.MIN_VALUE + 3, Long.MIN_VALUE + 1)));

		expecting(IllegalArgumentException.class, () -> LongSequence.range(1, 6, -1));
	}

	@Test
	public void toChars() {
		CharSeq emptyChars = empty.toChars();
		twice(() -> assertThat(emptyChars, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyChars.iterator().nextChar());

		CharSeq charSeq = LongSequence.from(LongList.create('a', 'b', 'c', 'd', 'e')).toChars();
		twice(() -> assertThat(charSeq, containsChars('a', 'b', 'c', 'd', 'e')));

		assertThat(removeFirst(charSeq), is('a'));
		twice(() -> assertThat(charSeq, containsChars('b', 'c', 'd', 'e')));
	}

	@Test
	public void toCharsMapped() {
		CharSeq emptyChars = empty.toChars(x -> (char) (x + 'a' - 1));
		twice(() -> assertThat(emptyChars, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyChars.iterator().nextChar());

		CharSeq charSeq = _12345.toChars(x -> (char) ('a' + x - 1));
		twice(() -> assertThat(charSeq, containsChars('a', 'b', 'c', 'd', 'e')));

		assertThat(removeFirst(charSeq), is('a'));
		twice(() -> assertThat(charSeq, containsChars('b', 'c', 'd', 'e')));
	}

	@Test
	public void toInts() {
		IntSequence emptyInts = empty.toInts();
		twice(() -> assertThat(emptyInts, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyInts.iterator().nextInt());

		IntSequence intSequence = _12345.toInts();
		twice(() -> assertThat(intSequence, containsInts(1, 2, 3, 4, 5)));

		assertThat(removeFirst(intSequence), is(1));
		twice(() -> assertThat(intSequence, containsInts(2, 3, 4, 5)));
	}

	@Test
	public void toIntsMapped() {
		IntSequence emptyInts = empty.toInts(x -> (int) (x + 1));
		twice(() -> assertThat(emptyInts, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyInts.iterator().nextInt());

		IntSequence intSequence = _12345.toInts(x -> (int) (x + 1));
		twice(() -> assertThat(intSequence,
		                       containsInts(2, 3, 4, 5, 6)));

		assertThat(removeFirst(intSequence), is(2));
		twice(() -> assertThat(intSequence, containsInts(3, 4, 5, 6)));
	}

	@Test
	public void toDoubles() {
		DoubleSequence emptyDoubles = empty.toDoubles();
		twice(() -> assertThat(emptyDoubles, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyDoubles.iterator().nextDouble());

		DoubleSequence doubleSequence = _12345.toDoubles();
		twice(() -> assertThat(doubleSequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));

		assertThat(removeFirst(doubleSequence), is(1.0));
		twice(() -> assertThat(doubleSequence, containsDoubles(2, 3, 4, 5)));
	}

	@Test
	public void toDoublesMapped() {
		DoubleSequence emptyDoubles = empty.toDoubles(x -> x / 2.0);
		twice(() -> assertThat(emptyDoubles, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyDoubles.iterator().nextDouble());

		DoubleSequence doubleSequence = _12345.toDoubles(x -> x / 2.0);
		twice(() -> assertThat(doubleSequence, containsDoubles(0.5, 1.0, 1.5, 2.0, 2.5)));

		assertThat(removeFirst(doubleSequence), is(0.5));
		twice(() -> assertThat(doubleSequence, containsDoubles(1.0, 1.5, 2.0, 2.5)));
	}

	@Test
	public void toSequence() {
		Sequence<Long> emptySequence = empty.toSequence(l -> l + 1);
		twice(() -> assertThat(emptySequence, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySequence.iterator().next());

		Sequence<Long> fiveSequence = _12345.toSequence(l -> l + 1);
		twice(() -> assertThat(fiveSequence, contains(2L, 3L, 4L, 5L, 6L)));

		assertThat(removeFirst(fiveSequence), is(2L));
		twice(() -> assertThat(fiveSequence, contains(3L, 4L, 5L, 6L)));
	}

	@Test
	public void box() {
		Sequence<Long> emptyBoxed = empty.box();
		twice(() -> assertThat(emptyBoxed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBoxed.iterator().next());

		Sequence<Long> fiveBoxed = _12345.box();
		twice(() -> assertThat(fiveBoxed, contains(1L, 2L, 3L, 4L, 5L)));

		assertThat(removeFirst(fiveBoxed), is(1L));
		twice(() -> assertThat(fiveBoxed, contains(2L, 3L, 4L, 5L)));
	}

	@Test
	public void repeat() {
		LongSequence emptyRepeated = empty.repeat();
		twice(() -> assertThat(emptyRepeated, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeated.iterator().nextLong());

		LongSequence oneRepeated = _1.repeat();
		twice(() -> assertThat(oneRepeated.limit(3), containsLongs(1, 1, 1)));

		LongSequence twoRepeated = _12.repeat();
		twice(() -> assertThat(twoRepeated.limit(5), containsLongs(1, 2, 1, 2, 1)));

		LongSequence threeRepeated = _123.repeat();
		twice(() -> assertThat(threeRepeated.limit(8), containsLongs(1, 2, 3, 1, 2, 3, 1, 2)));

		assertThat(removeFirst(threeRepeated), is(1L));
		twice(() -> assertThat(threeRepeated.limit(6), containsLongs(2, 3, 2, 3, 2, 3)));
		twice(() -> assertThat(_123, containsLongs(2, 3)));

		LongSequence varyingLengthRepeated = LongSequence.from(new LongIterable() {
			private List<Long> list = Lists.of(1L, 2L, 3L);
			int end = list.size();

			@Override
			public LongIterator iterator() {
				List<Long> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Long> iterator = subList.iterator();
				return new DelegatingTransformingLongIterator<Long, Iterator<Long>>(iterator) {
					@Override
					public long nextLong() {
						return iterator.next();
					}
				};
			}
		}).repeat();
		assertThat(varyingLengthRepeated, containsLongs(1, 2, 3, 1, 2, 1));
	}

	@Test
	public void repeatTwice() {
		LongSequence emptyRepeatedTwice = empty.repeat(2);
		twice(() -> assertThat(emptyRepeatedTwice, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeatedTwice.iterator().nextLong());

		LongSequence oneRepeatedTwice = _1.repeat(2);
		twice(() -> assertThat(oneRepeatedTwice, containsLongs(1, 1)));

		LongSequence twoRepeatedTwice = _12.repeat(2);
		twice(() -> assertThat(twoRepeatedTwice, containsLongs(1, 2, 1, 2)));

		LongSequence threeRepeatedTwice = _123.repeat(2);
		twice(() -> assertThat(threeRepeatedTwice, containsLongs(1, 2, 3, 1, 2, 3)));

		assertThat(removeFirst(threeRepeatedTwice), is(1L));
		twice(() -> assertThat(threeRepeatedTwice, containsLongs(2, 3, 2, 3)));
		twice(() -> assertThat(_123, containsLongs(2, 3)));

		LongSequence varyingLengthRepeatedTwice = LongSequence.from(new LongIterable() {
			private List<Long> list = Lists.of(1L, 2L, 3L);
			int end = list.size();

			@Override
			public LongIterator iterator() {
				List<Long> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Long> iterator = subList.iterator();
				return new DelegatingTransformingLongIterator<Long, Iterator<Long>>(iterator) {
					@Override
					public long nextLong() {
						return iterator.next();
					}
				};
			}
		}).repeat(2);
		assertThat(varyingLengthRepeatedTwice, containsLongs(1, 2, 3, 1, 2));
	}

	@Test
	public void repeatZero() {
		LongSequence emptyRepeatedZero = empty.repeat(0);
		twice(() -> assertThat(emptyRepeatedZero, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeatedZero.iterator().nextLong());

		LongSequence oneRepeatedZero = _1.repeat(0);
		twice(() -> assertThat(oneRepeatedZero, is(emptyIterable())));

		LongSequence twoRepeatedZero = _12.repeat(0);
		twice(() -> assertThat(twoRepeatedZero, is(emptyIterable())));

		LongSequence threeRepeatedZero = _123.repeat(0);
		twice(() -> assertThat(threeRepeatedZero, is(emptyIterable())));
	}

	@Test
	public void generate() {
		Queue<Long> queue = new ArrayDeque<>(Lists.of(1L, 2L, 3L, 4L, 5L));
		LongSequence sequence = LongSequence.generate(queue::poll);

		LongIterator iterator = sequence.iterator();
		assertThat(iterator.nextLong(), is(1L));
		assertThat(iterator.nextLong(), is(2L));
		assertThat(iterator.nextLong(), is(3L));
		assertThat(iterator.nextLong(), is(4L));
		assertThat(iterator.nextLong(), is(5L));
		expecting(NullPointerException.class, iterator::nextLong);

		LongIterator iterator2 = sequence.iterator();
		expecting(NullPointerException.class, iterator2::nextLong);
	}

	@Test
	public void multiGenerate() {
		LongSequence sequence = LongSequence.multiGenerate(() -> {
			Queue<Long> queue = new ArrayDeque<>(Lists.of(1L, 2L, 3L, 4L, 5L));
			return queue::poll;
		});

		twice(() -> {
			LongIterator iterator = sequence.iterator();
			assertThat(iterator.nextLong(), is(1L));
			assertThat(iterator.nextLong(), is(2L));
			assertThat(iterator.nextLong(), is(3L));
			assertThat(iterator.nextLong(), is(4L));
			assertThat(iterator.nextLong(), is(5L));
			expecting(NullPointerException.class, iterator::nextLong);
		});
	}

	@Test
	public void random() {
		LongSequence random = LongSequence.random();

		twice(() -> times(1000, random.iterator()::nextLong));

		assertThat(random.limit(10), not(containsLongs(random.limit(10))));
	}

	@Test
	public void randomWithSupplier() {
		LongSequence random = LongSequence.random(() -> new Random(17));

		twice(() -> assertThat(random.limit(5),
		                       containsLongs(-4937981208836185383L, -5582529378488325032L, 1530270151771565451L,
		                                     -3389839389802268617L, 818775917343865025L)));
	}

	@Test
	public void randomUpper() {
		LongSequence random = LongSequence.random(1000);

		twice(() -> {
			LongIterator iterator = random.iterator();
			times(1000, () -> assertThat(iterator.nextLong(),
			                             is(both(greaterThanOrEqualTo(0L)).and(lessThan(1000L)))));
		});

		assertThat(random.limit(10), not(containsLongs(random.limit(10))));
	}

	@Test
	public void randomUpperWithSupplier() {
		LongSequence random = LongSequence.random(() -> new Random(17), 1000);

		twice(() -> assertThat(random.limit(5),
		                       containsLongs(732, 697, 82, 816, 44)));
	}

	@Test
	public void randomLowerUpper() {
		LongSequence random = LongSequence.random(1000, 2000);

		twice(() -> {
			LongIterator iterator = random.iterator();
			times(1000, () -> assertThat(iterator.nextLong(),
			                             is(both(greaterThanOrEqualTo(1000L)).and(lessThan(2000L)))));
		});

		assertThat(random.limit(10), not(containsLongs(random.limit(10))));
	}

	@Test
	public void randomLowerUpperWithSupplier() {
		LongSequence random = LongSequence.random(() -> new Random(17), 1000, 2000);

		twice(() -> assertThat(random.limit(5),
		                       containsLongs(1732, 1697, 1082, 1816, 1044)));
	}

	@Test
	public void mapBack() {
		LongSequence emptyMappedBack = empty.mapBack(17, (p, x) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedBack, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedBack.iterator().nextLong());

		LongSequence threeMappedBackToPrevious = _123.mapBack(17, (p, x) -> p);
		twice(() -> assertThat(threeMappedBackToPrevious, containsLongs(17, 1, 2)));

		LongSequence threeMappedBackToCurrent = _123.mapBack(17, (p, x) -> x);
		twice(() -> assertThat(threeMappedBackToCurrent, containsLongs(1, 2, 3)));

		assertThat(removeFirst(threeMappedBackToCurrent), is(1L));
		twice(() -> assertThat(threeMappedBackToCurrent, containsLongs(2, 3)));
		twice(() -> assertThat(_123, containsLongs(2, 3)));
	}

	@Test
	public void mapForward() {
		LongSequence emptyMappedForward = empty.mapForward(17, (x, n) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedForward, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedForward.iterator().nextLong());

		LongSequence threeMappedForwardToCurrent = _123.mapForward(17, (x, n) -> x);
		twice(() -> assertThat(threeMappedForwardToCurrent, containsLongs(1, 2, 3)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(threeMappedForwardToCurrent));
		twice(() -> assertThat(threeMappedForwardToCurrent, containsLongs(1, 2, 3)));

		LongSequence threeMappedForwardToNext = _123.mapForward(17, (x, n) -> n);
		twice(() -> assertThat(threeMappedForwardToNext, containsLongs(2, 3, 17)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void window() {
		Sequence<LongSequence> emptyWindowed = empty.window(3);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<LongSequence> oneWindowed = _1.window(3);
		twice(() -> assertThat(oneWindowed, contains(containsLongs(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(containsLongs(1))));

		Sequence<LongSequence> twoWindowed = _12.window(3);
		twice(() -> assertThat(twoWindowed, contains(containsLongs(1, 2))));

		Sequence<LongSequence> threeWindowed = _123.window(3);
		twice(() -> assertThat(threeWindowed, contains(containsLongs(1, 2, 3))));

		Sequence<LongSequence> fourWindowed = _1234.window(3);
		twice(() -> assertThat(fourWindowed, contains(containsLongs(1, 2, 3), containsLongs(2, 3, 4))));

		Sequence<LongSequence> fiveWindowed = _12345.window(3);
		twice(() -> assertThat(fiveWindowed,
		                       contains(containsLongs(1, 2, 3), containsLongs(2, 3, 4), containsLongs(3, 4, 5))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithSmallerStep() {
		Sequence<LongSequence> emptyWindowed = empty.window(3, 2);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<LongSequence> oneWindowed = _1.window(3, 2);
		twice(() -> assertThat(oneWindowed, contains(containsLongs(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(containsLongs(1))));

		Sequence<LongSequence> twoWindowed = _12.window(3, 2);
		twice(() -> assertThat(twoWindowed, contains(containsLongs(1, 2))));

		Sequence<LongSequence> threeWindowed = _123.window(3, 2);
		twice(() -> assertThat(threeWindowed, contains(containsLongs(1, 2, 3))));

		Sequence<LongSequence> fourWindowed = _1234.window(3, 2);
		twice(() -> assertThat(fourWindowed, contains(containsLongs(1, 2, 3), containsLongs(3, 4))));

		Sequence<LongSequence> fiveWindowed = _12345.window(3, 2);
		twice(() -> assertThat(fiveWindowed, contains(containsLongs(1, 2, 3), containsLongs(3, 4, 5))));

		Sequence<LongSequence> nineWindowed = _123456789.window(3, 2);
		twice(() -> assertThat(nineWindowed,
		                       contains(containsLongs(1, 2, 3), containsLongs(3, 4, 5), containsLongs(5, 6, 7),
		                                containsLongs(7, 8, 9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithLargerStep() {
		Sequence<LongSequence> emptyWindowed = empty.window(3, 4);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<LongSequence> oneWindowed = _1.window(3, 4);
		twice(() -> assertThat(oneWindowed, contains(containsLongs(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(containsLongs(1))));

		Sequence<LongSequence> twoWindowed = _12.window(3, 4);
		twice(() -> assertThat(twoWindowed, contains(containsLongs(1, 2))));

		Sequence<LongSequence> threeWindowed = _123.window(3, 4);
		twice(() -> assertThat(threeWindowed, contains(containsLongs(1, 2, 3))));

		Sequence<LongSequence> fourWindowed = _1234.window(3, 4);
		twice(() -> assertThat(fourWindowed, contains(containsLongs(1, 2, 3))));

		Sequence<LongSequence> fiveWindowed = _12345.window(3, 4);
		twice(() -> assertThat(fiveWindowed, contains(containsLongs(1, 2, 3), containsLongs(5))));

		Sequence<LongSequence> nineWindowed = _123456789.window(3, 4);
		twice(() -> assertThat(nineWindowed,
		                       contains(containsLongs(1, 2, 3), containsLongs(5, 6, 7), containsLongs(9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batch() {
		Sequence<LongSequence> emptyBatched = empty.batch(3);
		twice(() -> assertThat(emptyBatched, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<LongSequence> oneBatched = _1.batch(3);
		twice(() -> assertThat(oneBatched, contains(containsLongs(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneBatched));
		twice(() -> assertThat(oneBatched, contains(containsLongs(1))));

		Sequence<LongSequence> twoBatched = _12.batch(3);
		twice(() -> assertThat(twoBatched, contains(containsLongs(1, 2))));

		Sequence<LongSequence> threeBatched = _123.batch(3);
		twice(() -> assertThat(threeBatched, contains(containsLongs(1, 2, 3))));

		Sequence<LongSequence> fourBatched = _1234.batch(3);
		twice(() -> assertThat(fourBatched, contains(containsLongs(1, 2, 3), containsLongs(4))));

		Sequence<LongSequence> fiveBatched = _12345.batch(3);
		twice(() -> assertThat(fiveBatched, contains(containsLongs(1, 2, 3), containsLongs(4, 5))));

		Sequence<LongSequence> nineBatched = _123456789.batch(3);
		twice(() -> assertThat(nineBatched,
		                       contains(containsLongs(1, 2, 3), containsLongs(4, 5, 6), containsLongs(7, 8, 9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnPredicate() {
		Sequence<LongSequence> emptyBatched = empty.batch((a, b) -> a > b);
		twice(() -> assertThat(emptyBatched, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<LongSequence> oneBatched = _1.batch((a, b) -> a > b);
		twice(() -> assertThat(oneBatched, contains(containsLongs(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneBatched));
		twice(() -> assertThat(oneBatched, contains(containsLongs(1))));

		Sequence<LongSequence> twoBatched = _12.batch((a, b) -> a > b);
		twice(() -> assertThat(twoBatched, contains(containsLongs(1, 2))));

		Sequence<LongSequence> threeBatched = _123.batch((a, b) -> a > b);
		twice(() -> assertThat(threeBatched, contains(containsLongs(1, 2, 3))));

		Sequence<LongSequence> threeRandomBatched = threeRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(threeRandomBatched, contains(containsLongs(17, 32), containsLongs(12))));

		Sequence<LongSequence> nineRandomBatched = nineRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(nineRandomBatched,
		                       contains(containsLongs(6, 6), containsLongs(1), containsLongs(-7, 1, 2, 17),
		                                containsLongs(5), containsLongs(4))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void split() {
		Sequence<LongSequence> emptySplit = empty.split(3);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

		Sequence<LongSequence> oneSplit = _1.split(3);
		twice(() -> assertThat(oneSplit, contains(containsLongs(1))));

		Sequence<LongSequence> twoSplit = _12.split(3);
		twice(() -> assertThat(twoSplit, contains(containsLongs(1, 2))));

		Sequence<LongSequence> threeSplit = _123.split(3);
		twice(() -> assertThat(threeSplit, contains(containsLongs(1, 2))));

		Sequence<LongSequence> fiveSplit = _12345.split(3);
		twice(() -> assertThat(fiveSplit, contains(containsLongs(1, 2), containsLongs(4, 5))));

		Sequence<LongSequence> nineSplit = _123456789.split(3);
		twice(() -> assertThat(nineSplit, contains(containsLongs(1, 2), containsLongs(4, 5, 6, 7, 8, 9))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSplit));
		twice(() -> assertThat(nineSplit, contains(containsLongs(1, 2), containsLongs(4, 5, 6, 7, 8, 9))));
		twice(() -> assertThat(_123456789, containsLongs(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitLongPredicate() {
		Sequence<LongSequence> emptySplit = empty.split(x -> x % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

		Sequence<LongSequence> oneSplit = _1.split(x -> x % 3 == 0);
		twice(() -> assertThat(oneSplit, contains(containsLongs(1))));

		Sequence<LongSequence> twoSplit = _12.split(x -> x % 3 == 0);
		twice(() -> assertThat(twoSplit, contains(containsLongs(1, 2))));

		Sequence<LongSequence> threeSplit = _123.split(x -> x % 3 == 0);
		twice(() -> assertThat(threeSplit, contains(containsLongs(1, 2))));

		Sequence<LongSequence> fiveSplit = _12345.split(x -> x % 3 == 0);
		twice(() -> assertThat(fiveSplit, contains(containsLongs(1, 2), containsLongs(4, 5))));

		Sequence<LongSequence> nineSplit = _123456789.split(x -> x % 3 == 0);
		twice(() -> assertThat(nineSplit, contains(containsLongs(1, 2), containsLongs(4, 5), containsLongs(7, 8))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSplit));
		twice(() -> assertThat(nineSplit, contains(containsLongs(1, 2), containsLongs(4, 5), containsLongs(7, 8))));
		twice(() -> assertThat(_123456789, containsLongs(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void filterClear() {
		List<Long> original = new ArrayList<>(Lists.of(1L, 2L, 3L, 4L));

		LongSequence filtered = LongSequence.from(original).filter(x -> x % 2 != 0);
		filtered.clear();

		twice(() -> assertThat(filtered, is(emptyIterable())));
		assertThat(original, contains(2L, 4L));
	}

	@Test
	public void isEmpty() {
		twice(() -> assertThat(empty.isEmpty(), is(true)));
		twice(() -> assertThat(_1.isEmpty(), is(false)));
		twice(() -> assertThat(_12.isEmpty(), is(false)));
		twice(() -> assertThat(_12345.isEmpty(), is(false)));
	}

	@Test
	public void containsLong() {
		assertThat(empty.containsLong(17), is(false));

		assertThat(_12345.containsLong(1), is(true));
		assertThat(_12345.containsLong(3), is(true));
		assertThat(_12345.containsLong(5), is(true));
		assertThat(_12345.containsLong(17), is(false));
	}

	@Test
	public void containsAllLongs() {
		assertThat(empty.containsAllLongs(), is(true));
		assertThat(empty.containsAllLongs(17, 18, 19), is(false));

		assertThat(_12345.containsAllLongs(), is(true));
		assertThat(_12345.containsAllLongs(1), is(true));
		assertThat(_12345.containsAllLongs(1, 3, 5), is(true));
		assertThat(_12345.containsAllLongs(1, 2, 3, 4, 5), is(true));
		assertThat(_12345.containsAllLongs(1, 2, 3, 4, 5, 17), is(false));
		assertThat(_12345.containsAllLongs(17, 18, 19), is(false));
	}

	@Test
	public void containsAny() {
		assertThat(empty.containsAnyLongs(), is(false));
		assertThat(empty.containsAnyLongs(17, 18, 19), is(false));

		assertThat(_12345.containsAnyLongs(), is(false));
		assertThat(_12345.containsAnyLongs(1), is(true));
		assertThat(_12345.containsAnyLongs(1, 3, 5), is(true));
		assertThat(_12345.containsAnyLongs(1, 2, 3, 4, 5), is(true));
		assertThat(_12345.containsAnyLongs(1, 2, 3, 4, 5, 17), is(true));
		assertThat(_12345.containsAnyLongs(17, 18, 19), is(false));
	}
}
