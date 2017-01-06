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
import org.d2ab.collection.doubles.*;
import org.d2ab.iterator.Iterators;
import org.d2ab.iterator.doubles.DelegatingTransformingDoubleIterator;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DoubleSequenceTest {
	private final DoubleSequence empty = DoubleSequence.empty();

	private final DoubleSequence _1 = DoubleSequence.from(DoubleList.create(1.0));
	private final DoubleSequence _12 = DoubleSequence.from(DoubleList.create(1.0, 2.0));
	private final DoubleSequence _123 = DoubleSequence.from(DoubleList.create(1.0, 2.0, 3.0));
	private final DoubleSequence _1234 = DoubleSequence.from(DoubleList.create(1.0, 2.0, 3.0, 4.0));
	private final DoubleSequence _12345 = DoubleSequence.from(DoubleList.create(1.0, 2.0, 3.0, 4.0, 5.0));
	private final DoubleSequence _123456789 =
			DoubleSequence.from(DoubleList.create(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0));

	private final DoubleSequence oneRandom = DoubleSequence.from(DoubleList.create(17.0));
	private final DoubleSequence twoRandom = DoubleSequence.from(DoubleList.create(17.0, 32.0));
	private final DoubleSequence threeRandom = DoubleSequence.from(DoubleList.create(17.0, 32.0, 12.0));
	private final DoubleSequence nineRandom =
			DoubleSequence.from(DoubleList.create(6.0, 6.0, 1.0, -7.0, 1.0, 2.0, 17.0, 5.0, 4.0));

	@Test
	public void ofOne() {
		twice(() -> assertThat(_1, containsDoubles(1.0)));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(_12345, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void fromArrayWithSize() {
		DoubleSequence sequence = DoubleSequence.from(new double[]{1, 2, 3, 4, 5}, 3);
		twice(() -> assertThat(sequence, containsDoubles(1, 2, 3)));
	}

	@Test
	public void fromArrayWithOffsetAndSize() {
		expecting(IndexOutOfBoundsException.class,
		          () -> DoubleSequence.from(new double[]{1, 2, 3, 4, 5}, -1, 3).iterator());
		expecting(IndexOutOfBoundsException.class,
		          () -> DoubleSequence.from(new double[]{1, 2, 3, 4, 5}, 6, 0).iterator());
		expecting(IndexOutOfBoundsException.class,
		          () -> DoubleSequence.from(new double[]{1, 2, 3, 4, 5}, 1, 5).iterator());
		expecting(IndexOutOfBoundsException.class,
		          () -> DoubleSequence.from(new double[]{1, 2, 3, 4, 5}, 1, -1).iterator());

		DoubleSequence sequence = DoubleSequence.from(new double[]{1, 2, 3, 4, 5}, 1, 3);
		twice(() -> assertThat(sequence, containsDoubles(2, 3, 4)));
	}

	@Test
	public void ofNone() {
		DoubleSequence sequence = DoubleSequence.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void fromIterable() {
		Iterable<Double> iterable = Iterables.of(1.0, 2.0, 3.0, 4.0, 5.0);

		DoubleSequence sequence = DoubleSequence.from(iterable);

		twice(() -> assertThat(sequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void fromDoubleIterable() {
		DoubleSequence sequence = DoubleSequence.from(DoubleIterable.of(1.0, 2.0, 3.0, 4.0, 5.0));

		twice(() -> assertThat(sequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void oncePrimitiveIteratorOfDouble() {
		DoubleSequence sequence = DoubleSequence.once(DoubleIterator.of(1.0, 2.0, 3.0, 4.0, 5.0));

		assertThat(sequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceIterator() {
		DoubleSequence sequence = DoubleSequence.once(Iterators.of(1.0, 2.0, 3.0, 4.0, 5.0));

		assertThat(sequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceDoubleStream() {
		DoubleSequence sequence = DoubleSequence.once(DoubleStream.of(1.0, 2.0, 3.0, 4.0, 5.0));

		assertThat(sequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceStream() {
		DoubleSequence sequence = DoubleSequence.once(Stream.of(1.0, 2.0, 3.0, 4.0, 5.0));

		assertThat(sequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceEmptyStream() {
		DoubleSequence sequence = DoubleSequence.once(Stream.of());

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void cachePrimitiveIteratorOfDouble() {
		DoubleSequence cached = DoubleSequence.cache(DoubleIterator.of(1.0, 2.0, 3.0, 4.0, 5.0));

		twice(() -> assertThat(cached, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void cacheIterator() {
		DoubleSequence cached = DoubleSequence.cache(Iterators.of(1.0, 2.0, 3.0, 4.0, 5.0));

		twice(() -> assertThat(cached, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void cacheDoubleIterable() {
		DoubleSequence cached = DoubleSequence.cache(DoubleIterable.of(1.0, 2.0, 3.0, 4.0, 5.0));

		twice(() -> assertThat(cached, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void cacheIterable() {
		DoubleSequence cached = DoubleSequence.cache(Iterables.of(1.0, 2.0, 3.0, 4.0, 5.0));

		twice(() -> assertThat(cached, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void cacheDoubleStream() {
		DoubleSequence cached = DoubleSequence.cache(DoubleStream.of(1.0, 2.0, 3.0, 4.0, 5.0));

		twice(() -> assertThat(cached, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void cacheStream() {
		DoubleSequence cached = DoubleSequence.cache(Stream.of(1.0, 2.0, 3.0, 4.0, 5.0));

		twice(() -> assertThat(cached, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void forEachDouble() {
		twice(() -> {
			empty.forEachDouble(c -> fail("Should not get called"));

			AtomicInteger value = new AtomicInteger(1);
			_1.forEachDouble(c -> assertThat(c, is((double) value.getAndIncrement())));

			value.set(1);
			_12.forEachDouble(c -> assertThat(c, is((double) value.getAndIncrement())));

			value.set(1);
			_123.forEachDouble(c -> assertThat(c, is((double) value.getAndIncrement())));
		});
	}

	@Test
	public void forEachDoubleIndexed() {
		twice(() -> {
			empty.forEachDoubleIndexed((e, i) -> fail("Should not get called"));

			AtomicInteger value = new AtomicInteger(1);
			AtomicInteger index = new AtomicInteger();
			_1.forEachDoubleIndexed((e, i) -> {
				assertThat(e, is((double) value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(1));

			value.set(1);
			index.set(0);
			_12.forEachDoubleIndexed((e, i) -> {
				assertThat(e, is((double) value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(2));

			value.set(1);
			index.set(0);
			_12345.forEachDoubleIndexed((e, i) -> {
				assertThat(e, is((double) value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(5));
		});
	}

	@Test
	public void iterator() {
		twice(() -> {
			DoubleIterator iterator = _123.iterator();

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.nextDouble(), is(1.0));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.nextDouble(), is(2.0));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.nextDouble(), is(3.0));

			assertThat(iterator.hasNext(), is(false));
			expecting(NoSuchElementException.class, iterator::nextDouble);
		});
	}

	@Test
	public void skip() {
		DoubleSequence threeSkipNone = _123.skip(0);
		twice(() -> assertThat(threeSkipNone, containsDoubles(1, 2, 3)));

		DoubleSequence threeSkipOne = _123.skip(1);
		twice(() -> assertThat(threeSkipOne, containsDoubles(2, 3)));

		DoubleSequence threeSkipTwo = _123.skip(2);
		twice(() -> assertThat(threeSkipTwo, containsDoubles(3)));

		DoubleSequence threeSkipThree = _123.skip(3);
		twice(() -> assertThat(threeSkipThree, is(emptyIterable())));

		DoubleSequence threeSkipFour = _123.skip(4);
		twice(() -> assertThat(threeSkipFour, is(emptyIterable())));

		expecting(NoSuchElementException.class, () -> threeSkipFour.iterator().nextDouble());

		assertThat(removeFirst(threeSkipNone), is(1.0));
		twice(() -> assertThat(threeSkipNone, containsDoubles(2, 3)));
		twice(() -> assertThat(_123, containsDoubles(2, 3)));
	}

	@Test
	public void skipTail() {
		DoubleSequence threeSkipTailNone = _123.skipTail(0);
		twice(() -> assertThat(threeSkipTailNone, containsDoubles(1, 2, 3)));

		DoubleSequence threeSkipTailOne = _123.skipTail(1);
		twice(() -> assertThat(threeSkipTailOne, containsDoubles(1, 2)));

		DoubleSequence threeSkipTailTwo = _123.skipTail(2);
		twice(() -> assertThat(threeSkipTailTwo, containsDoubles(1)));

		DoubleSequence threeSkipTailThree = _123.skipTail(3);
		twice(() -> assertThat(threeSkipTailThree, is(emptyIterable())));

		DoubleSequence threeSkipTailFour = _123.skipTail(4);
		twice(() -> assertThat(threeSkipTailFour, is(emptyIterable())));

		expecting(NoSuchElementException.class, () -> threeSkipTailFour.iterator().nextDouble());

		assertThat(removeFirst(threeSkipTailNone), is(1.0));
		twice(() -> assertThat(threeSkipTailNone, containsDoubles(2, 3)));
		twice(() -> assertThat(_123, containsDoubles(2, 3)));
	}

	@Test
	public void limit() {
		DoubleSequence threeLimitedToNone = _123.limit(0);
		twice(() -> assertThat(threeLimitedToNone, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> threeLimitedToNone.iterator().nextDouble());

		DoubleSequence threeLimitedToOne = _123.limit(1);
		twice(() -> assertThat(threeLimitedToOne, containsDoubles(1)));

		DoubleSequence threeLimitedToTwo = _123.limit(2);
		twice(() -> assertThat(threeLimitedToTwo, containsDoubles(1, 2)));

		DoubleSequence threeLimitedToThree = _123.limit(3);
		twice(() -> assertThat(threeLimitedToThree, containsDoubles(1, 2, 3)));

		DoubleSequence threeLimitedToFour = _123.limit(4);
		twice(() -> assertThat(threeLimitedToFour, containsDoubles(1, 2, 3)));

		assertThat(removeFirst(threeLimitedToFour), is(1.0));
		twice(() -> assertThat(threeLimitedToFour, containsDoubles(2, 3)));
		twice(() -> assertThat(_123, containsDoubles(2, 3)));
	}

	@Test
	public void appendEmptyArray() {
		DoubleSequence appendedEmpty = empty.append();
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextDouble());
	}

	@Test
	public void appendArray() {
		DoubleSequence appended = _123.append(4.0, 5.0, 6.0).append(7.0, 8.0);

		twice(() -> assertThat(appended, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)));
	}

	@Test
	public void appendEmptyDoubleIterable() {
		DoubleSequence appendedEmpty = empty.append(DoubleIterable.of());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextDouble());
	}

	@Test
	public void appendDoubleIterable() {
		DoubleSequence appended =
				_123.append(DoubleIterable.of(4.0, 5.0, 6.0)).append(DoubleIterable.of(7.0, 8.0));

		twice(() -> assertThat(appended, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)));
	}

	@Test
	public void appendIterable() {
		DoubleSequence appended = _123.append(Iterables.of(4.0, 5.0, 6.0)).append(Iterables.of(7.0, 8.0));

		twice(() -> assertThat(appended, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)));
	}

	@Test
	public void appendEmptyDoubleIterator() {
		DoubleSequence appendedEmpty = empty.append(DoubleIterator.of());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextDouble());
	}

	@Test
	public void appendDoubleIterator() {
		DoubleSequence appended = _123.append(DoubleIterator.of(4.0, 5.0, 6.0)).append(DoubleIterator.of(7.0, 8.0));

		assertThat(appended, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
		assertThat(appended, containsDoubles(1.0, 2.0, 3.0));
	}

	@Test
	public void appendDoubleIteratorAsIterator() {
		DoubleSequence appended = _123.append((Iterator<Double>) DoubleIterator.of(4, 5, 6))
		                              .append((Iterator<Double>) DoubleIterator.of(7, 8));

		assertThat(appended, containsDoubles(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, containsDoubles(1, 2, 3));
	}

	@Test
	public void appendIterator() {
		DoubleSequence appended = _123.append(Iterators.of(4.0, 5.0, 6.0)).append(Iterators.of(7.0, 8.0));

		assertThat(appended, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
		assertThat(appended, containsDoubles(1.0, 2.0, 3.0));
	}

	@Test
	public void appendEmptyDoubleStream() {
		DoubleSequence appendedEmpty = empty.append(DoubleStream.of());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().nextDouble());
	}

	@Test
	public void appendDoubleStream() {
		DoubleSequence appended = _123.append(DoubleStream.of(4.0, 5.0, 6.0)).append(DoubleStream.of(7.0, 8.0));

		assertThat(appended, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
		assertThat(appended, containsDoubles(1.0, 2.0, 3.0));
	}

	@Test
	public void appendStream() {
		DoubleSequence appended = _123.append(Stream.of(4.0, 5.0, 6.0)).append(Stream.of(7.0, 8.0));

		assertThat(appended, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
		assertThat(appended, containsDoubles(1.0, 2.0, 3.0));
	}

	@Test
	public void appendIsLazy() {
		DoubleIterator first = DoubleIterator.of(1.0, 2.0, 3.0);
		DoubleIterator second = DoubleIterator.of(4.0, 5.0, 6.0);
		DoubleIterator third = DoubleIterator.of(7.0, 8.0);

		DoubleSequence.once(first).append(second).append(third);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));
		assertThat(third.hasNext(), is(true));
	}

	@Test
	public void appendIsLazyWhenSkippingHasNext() {
		DoubleIterator first = DoubleIterator.of(1.0);
		DoubleIterator second = DoubleIterator.of(2.0);

		DoubleSequence sequence = DoubleSequence.once(first).append(() -> second);

		// check delayed iteration
		DoubleIterator iterator = sequence.iterator();
		assertThat(iterator.nextDouble(), is(1.0));
		assertThat(iterator.nextDouble(), is(2.0));
		assertThat(iterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, iterator::nextDouble);

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		DoubleSequence emptyFiltered = empty.filter(x -> (x % 2) == 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().nextDouble());

		DoubleSequence oneFiltered = _1.filter(x -> (x % 2) == 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		DoubleSequence twoFiltered = _12.filter(x -> (x % 2) == 0);
		twice(() -> assertThat(twoFiltered, containsDoubles(2)));

		assertThat(removeFirst(twoFiltered), is(2.0));
		twice(() -> assertThat(twoFiltered, is(emptyIterable())));
		twice(() -> assertThat(_12, containsDoubles(1)));

		DoubleSequence filtered = _123456789.filter(x -> (x % 2) == 0);
		twice(() -> assertThat(filtered, containsDoubles(2, 4, 6, 8)));
	}

	@Test
	public void filterIndexed() {
		DoubleSequence emptyFiltered = empty.filterIndexed((x, i) -> i > 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().nextDouble());

		DoubleSequence oneFiltered = _1.filterIndexed((x, i) -> i > 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		DoubleSequence twoFiltered = _12.filterIndexed((x, i) -> i > 0);
		twice(() -> assertThat(twoFiltered, containsDoubles(2)));

		assertThat(removeFirst(twoFiltered), is(2.0));
		twice(() -> assertThat(twoFiltered, is(emptyIterable())));
		twice(() -> assertThat(_12, containsDoubles(1)));

		DoubleSequence filtered = _123456789.filterIndexed((x, i) -> i > 3);
		twice(() -> assertThat(filtered, containsDoubles(5, 6, 7, 8, 9)));
	}

	@Test
	public void filterBack() {
		DoubleSequence emptyFilteredLess = empty.filterBack(117, (p, x) -> p < x);
		twice(() -> assertThat(emptyFilteredLess, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().nextDouble());

		DoubleSequence filteredLess = nineRandom.filterBack(117, (p, x) -> p < x);
		twice(() -> assertThat(filteredLess, containsDoubles(1, 2, 17)));

		DoubleSequence filteredGreater = nineRandom.filterBack(117, (p, x) -> p > x);
		twice(() -> assertThat(filteredGreater, containsDoubles(6, 1, -7, 5, 4)));

		assertThat(removeFirst(filteredGreater), is(6.0));
		twice(() -> assertThat(filteredGreater, containsDoubles(6, 1, -7, 5, 4)));
		twice(() -> assertThat(nineRandom, containsDoubles(6, 1, -7, 1, 2, 17, 5, 4)));
	}

	@Test
	public void filterForward() {
		DoubleSequence emptyFilteredLess = empty.filterForward(117, (x, n) -> x < n);
		twice(() -> assertThat(emptyFilteredLess, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().nextDouble());

		DoubleSequence filteredLess = nineRandom.filterForward(117, (x, n) -> n < x);
		twice(() -> assertThat(filteredLess, containsDoubles(6, 1, 17, 5)));

		DoubleSequence filteredGreater = nineRandom.filterForward(117, (x, n) -> n > x);
		twice(() -> assertThat(filteredGreater, containsDoubles(-7, 1, 2, 4)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(filteredGreater));
		twice(() -> assertThat(filteredGreater, containsDoubles(-7, 1, 2, 4)));
	}

	@Test
	public void includingExactlyArray() {
		DoubleSequence emptyIncluding = empty.includingExactly(1, 3, 5, 17);
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyIncluding.iterator().nextDouble());

		DoubleSequence including = _12345.includingExactly(1, 3, 5, 17);
		twice(() -> assertThat(including, containsDoubles(1, 3, 5)));

		DoubleSequence includingAll = _12345.includingExactly(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(includingAll, containsDoubles(1, 2, 3, 4, 5)));

		DoubleSequence includingNone = _12345.includingExactly();
		twice(() -> assertThat(includingNone, is(emptyIterable())));

		assertThat(removeFirst(including), is(1.0));
		twice(() -> assertThat(including, containsDoubles(3, 5)));
		twice(() -> assertThat(_12345, containsDoubles(2, 3, 4, 5)));
	}

	@Test
	public void includingArray() {
		DoubleSequence emptyIncluding = empty.including(new double[]{1, 3, 5, 17}, 0);
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyIncluding.iterator().nextDouble());

		DoubleSequence including = _12345.including(new double[]{1, 3, 5, 17}, 0);
		twice(() -> assertThat(including, containsDoubles(1, 3, 5)));

		DoubleSequence includingAll = _12345.including(new double[]{1, 2, 3, 4, 5, 17}, 0);
		twice(() -> assertThat(includingAll, containsDoubles(1, 2, 3, 4, 5)));

		DoubleSequence includingNone = _12345.including(new double[0], 0);
		twice(() -> assertThat(includingNone, is(emptyIterable())));

		assertThat(removeFirst(including), is(1.0));
		twice(() -> assertThat(including, containsDoubles(3, 5)));
		twice(() -> assertThat(_12345, containsDoubles(2, 3, 4, 5)));
	}

	@Test
	public void excludingExactlyArray() {
		DoubleSequence emptyExcluding = empty.excludingExactly(1, 3, 5, 17);
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyExcluding.iterator().nextDouble());

		DoubleSequence excluding = _12345.excludingExactly(1, 3, 5, 17);
		twice(() -> assertThat(excluding, containsDoubles(2, 4)));

		DoubleSequence excludingAll = _12345.excludingExactly(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		DoubleSequence excludingNone = _12345.excludingExactly();
		twice(() -> assertThat(excludingNone, containsDoubles(1, 2, 3, 4, 5)));

		assertThat(removeFirst(excluding), is(2.0));
		twice(() -> assertThat(excluding, containsDoubles(4)));
		twice(() -> assertThat(_12345, containsDoubles(1, 3, 4, 5)));
	}

	@Test
	public void excludingArray() {
		DoubleSequence emptyExcluding = empty.excluding(new double[]{1, 3, 5, 17}, 0);
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyExcluding.iterator().nextDouble());

		DoubleSequence excluding = _12345.excluding(new double[]{1, 3, 5, 17}, 0);
		twice(() -> assertThat(excluding, containsDoubles(2, 4)));

		DoubleSequence excludingAll = _12345.excluding(new double[]{1, 2, 3, 4, 5, 17}, 0);
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		DoubleSequence excludingNone = _12345.excluding(new double[0], 0);
		twice(() -> assertThat(excludingNone, containsDoubles(1, 2, 3, 4, 5)));

		assertThat(removeFirst(excluding), is(2.0));
		twice(() -> assertThat(excluding, containsDoubles(4)));
		twice(() -> assertThat(_12345, containsDoubles(1, 3, 4, 5)));
	}

	@Test
	public void map() {
		DoubleSequence emptyMapped = empty.map(l -> l + 1);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().nextDouble());

		DoubleSequence oneMapped = _1.map(l -> l + 1);
		twice(() -> assertThat(oneMapped, containsDoubles(2)));

		assertThat(removeFirst(oneMapped), is(2.0));
		twice(() -> assertThat(oneMapped, is(emptyIterable())));
		twice(() -> assertThat(_1, is(emptyIterable())));

		DoubleSequence twoMapped = _12.map(l -> l + 1);
		twice(() -> assertThat(twoMapped, containsDoubles(2, 3)));

		DoubleSequence fiveMapped = _12345.map(l -> l + 1);
		twice(() -> assertThat(fiveMapped, containsDoubles(2, 3, 4, 5, 6)));
	}

	@Test
	public void mapWithIndex() {
		DoubleSequence emptyMapped = empty.mapIndexed((i, x) -> i + x);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().nextDouble());

		DoubleSequence oneMapped = _1.mapIndexed((i, x) -> i + x);
		twice(() -> assertThat(oneMapped, containsDoubles(1)));

		assertThat(removeFirst(oneMapped), is(1.0));
		twice(() -> assertThat(oneMapped, is(emptyIterable())));
		twice(() -> assertThat(_1, is(emptyIterable())));

		DoubleSequence twoMapped = _12.mapIndexed((i, x) -> i + x);
		twice(() -> assertThat(twoMapped, containsDoubles(1, 3)));

		DoubleSequence mapped = _12345.mapIndexed((i, x) -> i + x);
		twice(() -> assertThat(mapped, containsDoubles(1, 3, 5, 7, 9)));
	}

	@Test
	public void recurse() {
		DoubleSequence recursive = DoubleSequence.recurse(1.0, i -> i + 1.0);
		twice(() -> assertThat(recursive.limit(10),
		                       containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)));
	}

	@Test
	public void untilTerminal() {
		DoubleSequence emptyUntil7 = empty.until(7, 0);
		twice(() -> assertThat(emptyUntil7, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntil7.iterator().nextDouble());

		DoubleSequence nineUntil7 = _123456789.until(7, 0);
		twice(() -> assertThat(nineUntil7, containsDoubles(1, 2, 3, 4, 5, 6)));

		assertThat(removeFirst(nineUntil7), is(1.0));
		twice(() -> assertThat(nineUntil7, containsDoubles(2, 3, 4, 5, 6)));
		twice(() -> assertThat(_123456789, containsDoubles(2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void untilPredicate() {
		DoubleSequence emptyUntilEqual7 = empty.until(i -> i == 7);
		twice(() -> assertThat(emptyUntilEqual7, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntilEqual7.iterator().nextDouble());

		DoubleSequence nineUntilEqual7 = _123456789.until(i -> i == 7);
		twice(() -> assertThat(nineUntilEqual7, containsDoubles(1, 2, 3, 4, 5, 6)));

		assertThat(removeFirst(nineUntilEqual7), is(1.0));
		twice(() -> assertThat(nineUntilEqual7, containsDoubles(2, 3, 4, 5, 6)));
		twice(() -> assertThat(_123456789, containsDoubles(2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void endingAtTerminal() {
		DoubleSequence emptyEndingAt7 = empty.endingAt(7, 0);
		twice(() -> assertThat(emptyEndingAt7, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAt7.iterator().nextDouble());

		DoubleSequence nineEndingAt7 = _123456789.endingAt(7, 0);
		twice(() -> assertThat(nineEndingAt7, containsDoubles(1, 2, 3, 4, 5, 6, 7)));

		assertThat(removeFirst(nineEndingAt7), is(1.0));
		twice(() -> assertThat(nineEndingAt7, containsDoubles(2, 3, 4, 5, 6, 7)));
		twice(() -> assertThat(_123456789, containsDoubles(2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void endingAtPredicate() {
		DoubleSequence emptyEndingAtEqual7 = empty.endingAt(i -> i == 7);
		twice(() -> assertThat(emptyEndingAtEqual7, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAtEqual7.iterator().nextDouble());

		DoubleSequence nineEndingAtEqual7 = _123456789.endingAt(i -> i == 7);
		twice(() -> assertThat(nineEndingAtEqual7, containsDoubles(1, 2, 3, 4, 5, 6, 7)));

		assertThat(removeFirst(nineEndingAtEqual7), is(1.0));
		twice(() -> assertThat(nineEndingAtEqual7, containsDoubles(2, 3, 4, 5, 6, 7)));
		twice(() -> assertThat(_123456789, containsDoubles(2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void startingAfter() {
		DoubleSequence emptyStartingAfter5 = empty.startingAfter(5, 0);
		twice(() -> assertThat(emptyStartingAfter5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingAfter5.iterator().nextDouble());

		DoubleSequence nineStartingAfter5 = _123456789.startingAfter(5, 0);
		twice(() -> assertThat(nineStartingAfter5, containsDoubles(6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingAfter5), is(6.0));
		twice(() -> assertThat(nineStartingAfter5, containsDoubles(7, 8, 9)));
		twice(() -> assertThat(_123456789, containsDoubles(1, 2, 3, 4, 5, 7, 8, 9)));

		DoubleSequence fiveStartingAfter10 = _12345.startingAfter(10, 0);
		twice(() -> assertThat(fiveStartingAfter10, is(emptyIterable())));
	}

	@Test
	public void startingAfterPredicate() {
		DoubleSequence emptyStartingAfterEqual5 = empty.startingAfter(x -> x == 5);
		twice(() -> assertThat(emptyStartingAfterEqual5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingAfterEqual5.iterator().nextDouble());

		DoubleSequence nineStartingAfterEqual5 = _123456789.startingAfter(x -> x == 5);
		twice(() -> assertThat(nineStartingAfterEqual5, containsDoubles(6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingAfterEqual5), is(6.0));
		twice(() -> assertThat(nineStartingAfterEqual5, containsDoubles(7, 8, 9)));
		twice(() -> assertThat(_123456789, containsDoubles(1, 2, 3, 4, 5, 7, 8, 9)));

		DoubleSequence fiveStartingAfterEqual10 = _12345.startingAfter(x -> x == 10);
		twice(() -> assertThat(fiveStartingAfterEqual10, is(emptyIterable())));
	}

	@Test
	public void startingFrom() {
		DoubleSequence emptyStartingFrom5 = empty.startingFrom(5, 0);
		twice(() -> assertThat(emptyStartingFrom5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingFrom5.iterator().nextDouble());

		DoubleSequence nineStartingFrom5 = _123456789.startingFrom(5, 0);
		twice(() -> assertThat(nineStartingFrom5, containsDoubles(5, 6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingFrom5), is(5.0));
		twice(() -> assertThat(nineStartingFrom5, is(emptyIterable())));
		twice(() -> assertThat(_123456789, containsDoubles(1, 2, 3, 4, 6, 7, 8, 9)));

		DoubleSequence fiveStartingFrom10 = _12345.startingFrom(10, 0);
		twice(() -> assertThat(fiveStartingFrom10, is(emptyIterable())));
	}

	@Test
	public void startingFromPredicate() {
		DoubleSequence emptyStartingFromEqual5 = empty.startingFrom(x -> x == 5);
		twice(() -> assertThat(emptyStartingFromEqual5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingFromEqual5.iterator().nextDouble());

		DoubleSequence nineStartingFromEqual5 = _123456789.startingFrom(x -> x == 5);
		twice(() -> assertThat(nineStartingFromEqual5, containsDoubles(5, 6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingFromEqual5), is(5.0));
		twice(() -> assertThat(nineStartingFromEqual5, is(emptyIterable())));
		twice(() -> assertThat(_123456789, containsDoubles(1, 2, 3, 4, 6, 7, 8, 9)));

		DoubleSequence fiveStartingFromEqual10 = _12345.startingFrom(x -> x == 10);
		twice(() -> assertThat(fiveStartingFromEqual10, is(emptyIterable())));
	}

	@Test
	public void toList() {
		twice(() -> {
			DoubleList list = _12345.toList();
			assertThat(list, is(instanceOf(ArrayDoubleList.class)));
			assertThat(list, containsDoubles(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSet() {
		twice(() -> {
			DoubleSet set = _12345.toSet();
			assertThat(set, instanceOf(RawDoubleSet.class));
			assertThat(set, containsDoubles(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSortedSet() {
		twice(() -> {
			DoubleSortedSet sortedSet = _12345.toSortedSet();
			assertThat(sortedSet, instanceOf(SortedListDoubleSet.class));
			assertThat(sortedSet, containsDoubles(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSetWithType() {
		twice(() -> {
			DoubleSet set = _12345.toSet(RawDoubleSet::new);
			assertThat(set, instanceOf(RawDoubleSet.class));
			assertThat(set, containsDoubles(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toCollection() {
		twice(() -> {
			DoubleList list = _12345.toCollection(DoubleList::create);
			assertThat(list, instanceOf(ArrayDoubleList.class));
			assertThat(list, containsDoubles(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void collectIntoDoubleCollection() {
		twice(() -> {
			DoubleList list = DoubleList.create();
			DoubleList result = _12345.collectInto(list);

			assertThat(result, is(sameInstance(list)));
			assertThat(result, containsDoubles(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void collect() {
		twice(() -> {
			StringBuilder builder = _123.collect(StringBuilder::new, StringBuilder::append);
			assertThat(builder.toString(), is("1.02.03.0"));
		});
	}

	@Test
	public void collectIntoContainer() {
		twice(() -> {
			DoubleList list = DoubleList.create();
			DoubleList result = _12345.collectInto(list, DoubleList::addDoubleExactly);

			assertThat(result, is(sameInstance(list)));
			assertThat(result, containsDoubles(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(Arrays.equals(_123.toDoubleArray(), new double[]{1.0, 2.0, 3.0}), is(true)));
	}

	@Test
	public void join() {
		twice(() -> assertThat(_123.join(", "), is("1.0, 2.0, 3.0")));
	}

	@Test
	public void joinWithPrefixAndSuffix() {
		twice(() -> assertThat(_123.join("<", ", ", ">"), is("<1.0, 2.0, 3.0>")));
	}

	@Test
	public void reduce() {
		DoubleBinaryOperator secondDouble = (c1, c2) -> c2;
		twice(() -> {
			assertThat(empty.reduce(secondDouble), is(OptionalDouble.empty()));
			assertThat(_1.reduce(secondDouble), is(OptionalDouble.of(1.0)));
			assertThat(_12.reduce(secondDouble), is(OptionalDouble.of(2.0)));
			assertThat(_123.reduce(secondDouble), is(OptionalDouble.of(3.0)));
		});
	}

	@Test
	public void reduceWithIdentity() {
		DoubleBinaryOperator secondDouble = (c1, c2) -> c2;
		twice(() -> {
			assertThat(empty.reduce(17.0, secondDouble), is(17.0));
			assertThat(_1.reduce(17.0, secondDouble), is(1.0));
			assertThat(_12.reduce(17.0, secondDouble), is(2.0));
			assertThat(_123.reduce(17.0, secondDouble), is(3.0));
		});
	}

	@Test
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(OptionalDouble.empty()));
			assertThat(_1.first(), is(OptionalDouble.of(1.0)));
			assertThat(_12.first(), is(OptionalDouble.of(1.0)));
			assertThat(_123.first(), is(OptionalDouble.of(1.0)));
		});
	}

	@Test
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(OptionalDouble.empty()));
			assertThat(_1.last(), is(OptionalDouble.of(1.0)));
			assertThat(_12.last(), is(OptionalDouble.of(2.0)));
			assertThat(_123.last(), is(OptionalDouble.of(3.0)));
		});
	}

	@Test
	public void at() {
		twice(() -> {
			assertThat(empty.at(0), is(OptionalDouble.empty()));
			assertThat(empty.at(17), is(OptionalDouble.empty()));

			assertThat(_1.at(0), is(OptionalDouble.of(1)));
			assertThat(_1.at(1), is(OptionalDouble.empty()));
			assertThat(_1.at(17), is(OptionalDouble.empty()));

			assertThat(_12345.at(0), is(OptionalDouble.of(1)));
			assertThat(_12345.at(1), is(OptionalDouble.of(2)));
			assertThat(_12345.at(4), is(OptionalDouble.of(5)));
			assertThat(_12345.at(17), is(OptionalDouble.empty()));
		});
	}

	@Test
	public void firstByPredicate() {
		twice(() -> {
			assertThat(empty.first(x -> x > 1), is(OptionalDouble.empty()));
			assertThat(_1.first(x -> x > 1), is(OptionalDouble.empty()));
			assertThat(_12.first(x -> x > 1), is(OptionalDouble.of(2)));
			assertThat(_12345.first(x -> x > 1), is(OptionalDouble.of(2)));
		});
	}

	@Test
	public void lastByPredicate() {
		twice(() -> {
			assertThat(empty.last(x -> x > 1), is(OptionalDouble.empty()));
			assertThat(_1.last(x -> x > 1), is(OptionalDouble.empty()));
			assertThat(_12.last(x -> x > 1), is(OptionalDouble.of(2)));
			assertThat(_12345.last(x -> x > 1), is(OptionalDouble.of(5)));
		});
	}

	@Test
	public void atByPredicate() {
		twice(() -> {
			assertThat(empty.at(0, x -> x > 1), is(OptionalDouble.empty()));
			assertThat(empty.at(17, x -> x > 1), is(OptionalDouble.empty()));

			assertThat(_1.at(0, x -> x > 1), is(OptionalDouble.empty()));
			assertThat(_1.at(17, x -> x > 1), is(OptionalDouble.empty()));

			assertThat(_12.at(0, x -> x > 1), is(OptionalDouble.of(2)));
			assertThat(_12.at(1, x -> x > 1), is(OptionalDouble.empty()));
			assertThat(_12.at(17, x -> x > 1), is(OptionalDouble.empty()));

			assertThat(_12345.at(0, x -> x > 1), is(OptionalDouble.of(2)));
			assertThat(_12345.at(1, x -> x > 1), is(OptionalDouble.of(3)));
			assertThat(_12345.at(3, x -> x > 1), is(OptionalDouble.of(5)));
			assertThat(_12345.at(4, x -> x > 1), is(OptionalDouble.empty()));
			assertThat(_12345.at(17, x -> x > 1), is(OptionalDouble.empty()));
		});
	}

	@Test
	public void step() {
		DoubleSequence emptyStep3 = empty.step(3);
		twice(() -> assertThat(emptyStep3, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStep3.iterator().nextDouble());

		DoubleSequence nineStep3 = _123456789.step(3);
		twice(() -> assertThat(nineStep3, containsDoubles(1, 4, 7)));

		DoubleIterator nineStep3Iterator = nineStep3.iterator();
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.hasNext(), is(true));
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.nextDouble(), is(1.0));
		nineStep3Iterator.remove();
		assertThat(nineStep3Iterator.hasNext(), is(true));
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.nextDouble(), is(4.0));
		nineStep3Iterator.remove();

		twice(() -> assertThat(nineStep3, containsDoubles(2, 6, 9)));
		twice(() -> assertThat(_123456789, containsDoubles(2, 3, 5, 6, 7, 8, 9)));
	}

	@Test
	public void distinct() {
		DoubleSequence emptyDistinct = empty.distinct(0.5);
		twice(() -> assertThat(emptyDistinct, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptyDistinct.iterator().nextDouble());

		DoubleSequence oneDistinct = oneRandom.distinct(0.5);
		twice(() -> assertThat(oneDistinct, containsDoubles(17)));

		DoubleSequence twoDuplicatesDistinct = DoubleSequence.from(DoubleIterable.of(17, 17.15, 17.3))
		                                                     .distinct(0.2);
		twice(() -> assertThat(twoDuplicatesDistinct, containsDoubles(17, 17.3)));

		DoubleSequence nineDistinct = nineRandom.distinct(0.5);
		twice(() -> assertThat(nineDistinct, containsDoubles(6L, 1L, -7L, 2L, 17L, 5L, 4L)));

		assertThat(removeFirst(nineDistinct), is(6.0));
		twice(() -> assertThat(nineDistinct, containsDoubles(6, 1, -7, 2, 17, 5, 4)));
		twice(() -> assertThat(nineRandom, containsDoubles(6, 1, -7, 1, 2, 17, 5, 4)));
	}

	@Test
	public void distinctExactly() {
		DoubleSequence emptyDistinct = empty.distinctExactly();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptyDistinct.iterator().nextDouble());

		DoubleSequence oneDistinct = oneRandom.distinctExactly();
		twice(() -> assertThat(oneDistinct, containsDoubles(17)));

		DoubleSequence twoDuplicatesDistinct = DoubleSequence.from(DoubleIterable.of(17, 17)).distinctExactly();
		twice(() -> assertThat(twoDuplicatesDistinct, containsDoubles(17)));

		DoubleSequence nineDistinct = nineRandom.distinctExactly();
		twice(() -> assertThat(nineDistinct, containsDoubles(6, 1, -7, 2, 17, 5, 4)));

		assertThat(removeFirst(nineDistinct), is(6.0));
		twice(() -> assertThat(nineDistinct, containsDoubles(6, 1, -7, 2, 17, 5, 4)));
		twice(() -> assertThat(nineRandom, containsDoubles(6, 1, -7, 1, 2, 17, 5, 4)));
	}

	@Test
	public void sorted() {
		DoubleSequence emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptySorted.iterator().nextDouble());

		DoubleSequence oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, containsDoubles(17)));

		DoubleSequence twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, containsDoubles(17, 32)));

		DoubleSequence nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, containsDoubles(-7, 1, 1, 2, 4, 5, 6, 6, 17)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSorted));
		twice(() -> assertThat(nineSorted, containsDoubles(-7, 1, 1, 2, 4, 5, 6, 6, 17)));
		twice(() -> assertThat(nineRandom, containsDoubles(6, 6, 1, -7, 1, 2, 17, 5, 4)));
	}

	@Test
	public void sortedWithUpdates() {
		List<Double> backing = new ArrayList<>(asList(2.0, 3.0, 1.0));
		DoubleSequence sorted = DoubleSequence.from(backing).sorted();

		backing.add(4.0);
		assertThat(sorted, containsDoubles(1.0, 2.0, 3.0, 4.0));
	}

	@Test
	public void min() {
		twice(() -> assertThat(empty.min(), is(OptionalDouble.empty())));
		twice(() -> assertThat(oneRandom.min(), is(OptionalDouble.of(17.0))));
		twice(() -> assertThat(twoRandom.min(), is(OptionalDouble.of(17.0))));
		twice(() -> assertThat(nineRandom.min(), is(OptionalDouble.of(-7.0))));
	}

	@Test
	public void max() {
		twice(() -> assertThat(empty.max(), is(OptionalDouble.empty())));
		twice(() -> assertThat(oneRandom.max(), is(OptionalDouble.of(17.0))));
		twice(() -> assertThat(twoRandom.max(), is(OptionalDouble.of(32.0))));
		twice(() -> assertThat(nineRandom.max(), is(OptionalDouble.of(17.0))));
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
		twice(() -> assertThat(_123.any(x -> x > 0.0), is(true)));
		twice(() -> assertThat(_123.any(x -> x > 2.0), is(true)));
		twice(() -> assertThat(_123.any(x -> x > 4.0), is(false)));
	}

	@Test
	public void all() {
		twice(() -> assertThat(_123.all(x -> x > 0.0), is(true)));
		twice(() -> assertThat(_123.all(x -> x > 2.0), is(false)));
		twice(() -> assertThat(_123.all(x -> x > 4.0), is(false)));
	}

	@Test
	public void none() {
		twice(() -> assertThat(_123.none(x -> x > 0.0), is(false)));
		twice(() -> assertThat(_123.none(x -> x > 2.0), is(false)));
		twice(() -> assertThat(_123.none(x -> x > 4.0), is(true)));
	}

	@Test
	public void peek() {
		DoubleSequence emptyPeeked = empty.peek(x -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyPeeked, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().nextDouble());

		AtomicInteger value = new AtomicInteger(1);
		DoubleSequence onePeeked = _1.peek(x -> assertThat(x, is((double) value.getAndIncrement())));
		twiceIndexed(value, 1, () -> assertThat(onePeeked, containsDoubles(1)));

		DoubleSequence twoPeeked = _12.peek(x -> assertThat(x, is((double) value.getAndIncrement())));
		twiceIndexed(value, 2, () -> assertThat(twoPeeked, containsDoubles(1, 2)));

		DoubleSequence fivePeeked = _12345.peek(x -> assertThat(x, is((double) value.getAndIncrement())));
		twiceIndexed(value, 5, () -> assertThat(fivePeeked, containsDoubles(1, 2, 3, 4, 5)));

		assertThat(removeFirst(fivePeeked), is(1.0));
		twiceIndexed(value, 4, () -> assertThat(fivePeeked, containsDoubles(2, 3, 4, 5)));
		twice(() -> assertThat(_12345, containsDoubles(2, 3, 4, 5)));
	}

	@Test
	public void peekIndexed() {
		DoubleSequence emptyPeeked = empty.peekIndexed((x, i) -> fail("Should not get called"));
		twice(() -> assertThat(emptyPeeked, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().nextDouble());

		AtomicInteger index = new AtomicInteger();
		AtomicInteger value = new AtomicInteger(1);
		DoubleSequence onePeeked = _1.peekIndexed((x, i) -> {
			assertThat(x, is((double) value.getAndIncrement()));
			assertThat(i, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(onePeeked, containsDoubles(1));

			assertThat(index.get(), is(1));
			assertThat(value.get(), is(2));
			index.set(0);
			value.set(1);
		});

		DoubleSequence twoPeeked = _12.peekIndexed((x, i) -> {
			assertThat(x, is((double) value.getAndIncrement()));
			assertThat(i, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(twoPeeked, containsDoubles(1, 2));

			assertThat(index.get(), is(2));
			assertThat(value.get(), is(3));
			index.set(0);
			value.set(1);
		});

		DoubleSequence fivePeeked = _12345.peekIndexed((x, i) -> {
			assertThat(x, is((double) value.getAndIncrement()));
			assertThat(i, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(fivePeeked, containsDoubles(1, 2, 3, 4, 5));

			assertThat(index.get(), is(5));
			assertThat(value.get(), is(6));
			index.set(0);
			value.set(1);
		});

		assertThat(removeFirst(fivePeeked), is(1.0));
		index.set(0);
		value.set(2);
		twice(() -> {
			assertThat(fivePeeked, containsDoubles(2, 3, 4, 5));
			assertThat(index.get(), is(4));
			assertThat(value.get(), is(6));
			index.set(0);
			value.set(2);
		});

		twice(() -> assertThat(_12345, containsDoubles(2, 3, 4, 5)));
	}

	@Test
	public void stream() {
		twice(() -> assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable())));
		twice(() -> assertThat(_12345.stream().collect(Collectors.toList()), contains(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void streamFromOnce() {
		DoubleSequence empty = DoubleSequence.once(DoubleIterator.of());
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));

		DoubleSequence sequence = DoubleSequence.once(DoubleIterator.of(1, 2, 3, 4, 5));
		assertThat(sequence.stream().collect(Collectors.toList()), contains(1.0, 2.0, 3.0, 4.0, 5.0));
		assertThat(sequence.stream().collect(Collectors.toList()), is(emptyIterable()));
	}

	@Test
	public void doubleStream() {
		twice(() -> assertThat(
				empty.doubleStream()
				     .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
				is(emptyIterable())));

		twice(() -> assertThat(
				_12345.doubleStream()
				      .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
				containsDoubles(1, 2, 3, 4, 5)));
	}

	@Test
	public void doubleStreamFromOnce() {
		DoubleSequence empty = DoubleSequence.once(DoubleIterator.of());
		assertThat(empty.doubleStream()
		                .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           is(emptyIterable()));
		assertThat(empty.doubleStream()
		                .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           is(emptyIterable()));

		DoubleSequence sequence = DoubleSequence.once(DoubleIterator.of(1, 2, 3, 4, 5));
		assertThat(
				sequence.doubleStream()
				        .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
				containsDoubles(1, 2, 3, 4, 5));
		assertThat(
				sequence.doubleStream()
				        .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
				is(emptyIterable()));
	}

	@Test
	public void prefix() {
		DoubleSequence emptyPrefixed = empty.prefix(327);
		twice(() -> assertThat(emptyPrefixed, containsDoubles(327)));

		DoubleIterator emptyIterator = emptyPrefixed.iterator();
		emptyIterator.nextDouble();
		expecting(NoSuchElementException.class, emptyIterator::nextDouble);

		DoubleSequence threePrefixed = _123.prefix(327);
		twice(() -> assertThat(threePrefixed, containsDoubles(327, 1, 2, 3)));

		DoubleIterator iterator = threePrefixed.iterator();
		expecting(UnsupportedOperationException.class, () -> {
			iterator.nextDouble();
			iterator.remove();
		});
		assertThat(iterator.nextDouble(), is(1.0));
		iterator.remove();
		twice(() -> assertThat(threePrefixed, containsDoubles(327, 2, 3)));
		twice(() -> assertThat(_123, containsDoubles(2, 3)));
	}

	@Test
	public void suffix() {
		DoubleSequence emptySuffixed = empty.suffix(532);
		twice(() -> assertThat(emptySuffixed, containsDoubles(532)));

		DoubleIterator emptyIterator = emptySuffixed.iterator();
		emptyIterator.nextDouble();
		expecting(NoSuchElementException.class, emptyIterator::nextDouble);

		DoubleSequence threeSuffixed = _123.suffix(532);
		twice(() -> assertThat(threeSuffixed, containsDoubles(1, 2, 3, 532)));

		assertThat(removeFirst(threeSuffixed), is(1.0));
		twice(() -> assertThat(threeSuffixed, containsDoubles(2, 3, 532)));
		twice(() -> assertThat(_123, containsDoubles(2, 3)));
	}

	@Test
	public void interleave() {
		DoubleSequence emptyInterleaved = empty.interleave(empty);
		twice(() -> assertThat(emptyInterleaved, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyInterleaved.iterator().nextDouble());

		DoubleSequence interleavedShortFirst = _123.interleave(_12345);
		twice(() -> assertThat(interleavedShortFirst, containsDoubles(1, 1, 2, 2, 3, 3, 4, 5)));

		DoubleSequence interleavedShortLast = _12345.interleave(_123);
		twice(() -> assertThat(interleavedShortLast, containsDoubles(1, 1, 2, 2, 3, 3, 4, 5)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(interleavedShortFirst));
		twice(() -> assertThat(interleavedShortLast, containsDoubles(1, 1, 2, 2, 3, 3, 4, 5)));
	}

	@Test
	public void reverse() {
		DoubleSequence emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyReversed.iterator().nextDouble());

		DoubleSequence oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, containsDoubles(1)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneReversed));
		twice(() -> assertThat(oneReversed, containsDoubles(1)));
		twice(() -> assertThat(_1, containsDoubles(1)));

		DoubleSequence twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, containsDoubles(2, 1)));

		DoubleSequence threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, containsDoubles(3, 2, 1)));

		DoubleSequence nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed, containsDoubles(9, 8, 7, 6, 5, 4, 3, 2, 1)));
	}

	@Test
	public void reverseWithUpdates() {
		List<Double> backing = new ArrayList<>(asList(1.0, 2.0, 3.0));
		DoubleSequence reversed = DoubleSequence.from(backing).reverse();

		backing.add(4.0);
		assertThat(reversed, containsDoubles(4.0, 3.0, 2.0, 1.0));
	}

	@Test
	public void steppingFrom() {
		assertThat(DoubleSequence.steppingFrom(1, 0.5).limit(3), containsDoubles(1.0, 1.5, 2.0));
		assertThat(DoubleSequence.steppingFrom(10000, 0.5).limit(3).last(), is(OptionalDouble.of(10001)));
	}

	@Test
	public void range() {
		assertThat(DoubleSequence.range(1, 6, 1, 0.1), containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0));
		assertThat(DoubleSequence.range(6, 1, 1, 0.1), containsDoubles(6.0, 5.0, 4.0, 3.0, 2.0, 1.0));
		expecting(IllegalArgumentException.class, () -> DoubleSequence.range(1, 6, -1, 0));
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

		IntSequence _0 = DoubleSequence.steppingFrom(0, 1).limit(5).toInts();
		twice(() -> assertThat(_0, containsInts(0, 1, 2, 3, 4)));

		IntSequence _0_5 = DoubleSequence.steppingFrom(0.5, 1).limit(5).toInts();
		twice(() -> assertThat(_0_5, containsInts(0, 1, 2, 3, 4)));

		IntSequence _0_9999 = DoubleSequence.steppingFrom(0.9999, 1).limit(5).toInts();
		twice(() -> assertThat(_0_9999, containsInts(0, 1, 2, 3, 4)));

		IntSequence _1 = DoubleSequence.steppingFrom(1, 1).limit(5).toInts();
		twice(() -> assertThat(_1, containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void toRoundedInts() {
		IntSequence emptyInts = empty.toRoundedInts();
		twice(() -> assertThat(emptyInts, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyInts.iterator().nextInt());

		IntSequence intSequence = _12345.toRoundedInts();
		twice(() -> assertThat(intSequence, containsInts(1, 2, 3, 4, 5)));

		assertThat(removeFirst(intSequence), is(1));
		twice(() -> assertThat(intSequence, containsInts(2, 3, 4, 5)));

		IntSequence _1 = DoubleSequence.steppingFrom(1, 1).limit(5).toRoundedInts();
		twice(() -> assertThat(_1, containsInts(1, 2, 3, 4, 5)));

		IntSequence _0_99999 = DoubleSequence.steppingFrom(0.99999, 1).limit(5).toRoundedInts();
		twice(() -> assertThat(_0_99999, containsInts(1, 2, 3, 4, 5)));

		IntSequence _0_5 = DoubleSequence.steppingFrom(0.5, 1).limit(5).toRoundedInts();
		twice(() -> assertThat(_0_5, containsInts(1, 2, 3, 4, 5)));

		IntSequence _0_49999 = DoubleSequence.steppingFrom(0.49999, 1).limit(5).toRoundedInts();
		twice(() -> assertThat(_0_49999, containsInts(0, 1, 2, 3, 4)));

		IntSequence _0 = DoubleSequence.steppingFrom(0, 1).limit(5).toRoundedInts();
		twice(() -> assertThat(_0, containsInts(0, 1, 2, 3, 4)));
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
	public void toLongs() {
		LongSequence emptyLongs = empty.toLongs();
		twice(() -> assertThat(emptyLongs, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyLongs.iterator().nextLong());

		LongSequence longSequence = _12345.toLongs();
		twice(() -> assertThat(longSequence, containsLongs(1, 2, 3, 4, 5)));

		assertThat(removeFirst(longSequence), is(1L));
		twice(() -> assertThat(longSequence, containsLongs(2, 3, 4, 5)));

		LongSequence _0 = DoubleSequence.steppingFrom(0, 1).limit(5).toLongs();
		twice(() -> assertThat(_0, containsLongs(0L, 1L, 2L, 3L, 4L)));

		LongSequence _0_5 = DoubleSequence.steppingFrom(0.5, 1).limit(5).toLongs();
		twice(() -> assertThat(_0_5, containsLongs(0L, 1L, 2L, 3L, 4L)));

		LongSequence _0_9999 = DoubleSequence.steppingFrom(0.9999, 1).limit(5).toLongs();
		twice(() -> assertThat(_0_9999, containsLongs(0L, 1L, 2L, 3L, 4L)));

		LongSequence _1 = DoubleSequence.steppingFrom(1, 1).limit(5).toLongs();
		twice(() -> assertThat(_1, containsLongs(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void toRoundedLongs() {
		LongSequence emptyLongs = empty.toRoundedLongs();
		twice(() -> assertThat(emptyLongs, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyLongs.iterator().nextLong());

		LongSequence longSequence = _12345.toRoundedLongs();
		twice(() -> assertThat(longSequence, containsLongs(1, 2, 3, 4, 5)));

		assertThat(removeFirst(longSequence), is(1L));
		twice(() -> assertThat(longSequence, containsLongs(2, 3, 4, 5)));

		LongSequence _1 = DoubleSequence.steppingFrom(1, 1).limit(5).toRoundedLongs();
		twice(() -> assertThat(_1, containsLongs(1L, 2L, 3L, 4L, 5L)));

		LongSequence _0_99999 = DoubleSequence.steppingFrom(0.99999, 1).limit(5).toRoundedLongs();
		twice(() -> assertThat(_0_99999, containsLongs(1L, 2L, 3L, 4L, 5L)));

		LongSequence _0_5 = DoubleSequence.steppingFrom(0.5, 1).limit(5).toRoundedLongs();
		twice(() -> assertThat(_0_5, containsLongs(1L, 2L, 3L, 4L, 5L)));

		LongSequence _0_49999 = DoubleSequence.steppingFrom(0.49999, 1).limit(5).toRoundedLongs();
		twice(() -> assertThat(_0_49999, containsLongs(0L, 1L, 2L, 3L, 4L)));

		LongSequence _0 = DoubleSequence.steppingFrom(0, 1).limit(5).toRoundedLongs();
		twice(() -> assertThat(_0, containsLongs(0L, 1L, 2L, 3L, 4L)));
	}

	@Test
	public void toLongsMapped() {
		LongSequence emptyLongs = empty.toLongs(d -> (long) (d * 2));
		twice(() -> assertThat(emptyLongs, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyLongs.iterator().nextLong());

		LongSequence doubledHalves = DoubleSequence.from(DoubleList.create(0.5, 1.0, 1.5))
		                                           .toLongs(d -> (long) (d * 2));
		twice(() -> assertThat(doubledHalves, containsLongs(1L, 2L, 3L)));

		assertThat(removeFirst(doubledHalves), is(1L));
		twice(() -> assertThat(doubledHalves, containsLongs(2L, 3L)));
	}

	@Test
	public void toSequence() {
		Sequence<Double> emptySequence = empty.toSequence(d -> d + 1);
		twice(() -> assertThat(emptySequence, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySequence.iterator().next());

		Sequence<Double> fiveSequence = _12345.toSequence(d -> d + 1);
		twice(() -> assertThat(fiveSequence, contains(2.0, 3.0, 4.0, 5.0, 6.0)));

		assertThat(removeFirst(fiveSequence), is(2.0));
		twice(() -> assertThat(fiveSequence, contains(3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void box() {
		Sequence<Double> emptyBoxed = empty.box();
		twice(() -> assertThat(emptyBoxed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBoxed.iterator().next());

		Sequence<Double> fiveBoxed = _12345.box();
		twice(() -> assertThat(fiveBoxed, contains(1.0, 2.0, 3.0, 4.0, 5.0)));

		assertThat(removeFirst(fiveBoxed), is(1.0));
		twice(() -> assertThat(fiveBoxed, contains(2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void repeat() {
		DoubleSequence emptyRepeated = empty.repeat();
		twice(() -> assertThat(emptyRepeated, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeated.iterator().nextDouble());

		DoubleSequence oneRepeated = _1.repeat();
		twice(() -> assertThat(oneRepeated.limit(3), containsDoubles(1, 1, 1)));

		DoubleSequence twoRepeated = _12.repeat();
		twice(() -> assertThat(twoRepeated.limit(5), containsDoubles(1, 2, 1, 2, 1)));

		DoubleSequence threeRepeated = _123.repeat();
		twice(() -> assertThat(threeRepeated.limit(8), containsDoubles(1, 2, 3, 1, 2, 3, 1, 2)));

		assertThat(removeFirst(threeRepeated), is(1.0));
		twice(() -> assertThat(threeRepeated.limit(6), containsDoubles(2, 3, 2, 3, 2, 3)));
		twice(() -> assertThat(_123, containsDoubles(2, 3)));

		DoubleSequence varyingLengthRepeated = DoubleSequence.from(new DoubleIterable() {
			private List<Double> list = asList(1.0, 2.0, 3.0);
			int end = list.size();

			@Override
			public DoubleIterator iterator() {
				List<Double> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Double> iterator = subList.iterator();
				return new DelegatingTransformingDoubleIterator<Double, Iterator<Double>>(iterator) {
					@Override
					public double nextDouble() {
						return iterator.next();
					}
				};
			}
		}).repeat();
		assertThat(varyingLengthRepeated, containsDoubles(1, 2, 3, 1, 2, 1));
	}

	@Test
	public void repeatTwice() {
		DoubleSequence emptyRepeatedTwice = empty.repeat(2);
		twice(() -> assertThat(emptyRepeatedTwice, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeatedTwice.iterator().nextDouble());

		DoubleSequence oneRepeatedTwice = _1.repeat(2);
		twice(() -> assertThat(oneRepeatedTwice, containsDoubles(1, 1)));

		DoubleSequence twoRepeatedTwice = _12.repeat(2);
		twice(() -> assertThat(twoRepeatedTwice, containsDoubles(1, 2, 1, 2)));

		DoubleSequence threeRepeatedTwice = _123.repeat(2);
		twice(() -> assertThat(threeRepeatedTwice, containsDoubles(1, 2, 3, 1, 2, 3)));

		assertThat(removeFirst(threeRepeatedTwice), is(1.0));
		twice(() -> assertThat(threeRepeatedTwice, containsDoubles(2, 3, 2, 3)));
		twice(() -> assertThat(_123, containsDoubles(2, 3)));

		DoubleSequence varyingLengthRepeatedTwice = DoubleSequence.from(new DoubleIterable() {
			private List<Double> list = asList(1.0, 2.0, 3.0);
			int end = list.size();

			@Override
			public DoubleIterator iterator() {
				List<Double> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Double> iterator = subList.iterator();
				return new DelegatingTransformingDoubleIterator<Double, Iterator<Double>>(iterator) {
					@Override
					public double nextDouble() {
						return iterator.next();
					}
				};
			}
		}).repeat(2);
		assertThat(varyingLengthRepeatedTwice, containsDoubles(1, 2, 3, 1, 2));
	}

	@Test
	public void repeatZero() {
		DoubleSequence emptyRepeatedZero = empty.repeat(0);
		twice(() -> assertThat(emptyRepeatedZero, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeatedZero.iterator().nextDouble());

		DoubleSequence oneRepeatedZero = _1.repeat(0);
		twice(() -> assertThat(oneRepeatedZero, is(emptyIterable())));

		DoubleSequence twoRepeatedZero = _12.repeat(0);
		twice(() -> assertThat(twoRepeatedZero, is(emptyIterable())));

		DoubleSequence threeRepeatedZero = _123.repeat(0);
		twice(() -> assertThat(threeRepeatedZero, is(emptyIterable())));
	}

	@Test
	public void generate() {
		Queue<Double> queue = new ArrayDeque<>(asList(1.0, 2.0, 3.0, 4.0, 5.0));
		DoubleSequence sequence = DoubleSequence.generate(queue::poll);

		DoubleIterator iterator = sequence.iterator();
		assertThat(iterator.nextDouble(), is(1.0));
		assertThat(iterator.nextDouble(), is(2.0));
		assertThat(iterator.nextDouble(), is(3.0));
		assertThat(iterator.nextDouble(), is(4.0));
		assertThat(iterator.nextDouble(), is(5.0));
		expecting(NullPointerException.class, iterator::nextDouble);

		DoubleIterator iterator2 = sequence.iterator();
		expecting(NullPointerException.class, iterator2::nextDouble);
	}

	@Test
	public void multiGenerate() {
		DoubleSequence sequence = DoubleSequence.multiGenerate(() -> {
			Queue<Double> queue = new ArrayDeque<>(asList(1.0, 2.0, 3.0, 4.0, 5.0));
			return queue::poll;
		});

		twice(() -> {
			DoubleIterator iterator = sequence.iterator();
			assertThat(iterator.nextDouble(), is(1.0));
			assertThat(iterator.nextDouble(), is(2.0));
			assertThat(iterator.nextDouble(), is(3.0));
			assertThat(iterator.nextDouble(), is(4.0));
			assertThat(iterator.nextDouble(), is(5.0));
			expecting(NullPointerException.class, iterator::nextDouble);
		});
	}

	@Test
	public void random() {
		DoubleSequence random = DoubleSequence.random();

		twice(() -> times(1000, random.iterator()::nextDouble));

		assertThat(random.limit(10), not(containsDoubles(random.limit(10))));
	}

	@Test
	public void randomWithSupplier() {
		DoubleSequence random = DoubleSequence.random(() -> new Random(17));

		twice(() -> assertThat(random.limit(5),
		                       containsDoubles(0.7323115139597316, 0.6973704783607497, 0.08295611145017068,
		                                       0.8162364511057306, 0.0443859375038691)));
	}

	@Test
	public void randomUpper() {
		DoubleSequence random = DoubleSequence.random(1000);

		twice(() -> {
			DoubleIterator iterator = random.iterator();
			times(1000, () -> assertThat(iterator.nextDouble(),
			                             is(both(greaterThanOrEqualTo(0.0)).and(lessThan(1000.0)))));
		});

		assertThat(random.limit(10), not(containsDoubles(random.limit(10))));
	}

	@Test
	public void randomUpperWithSupplier() {
		DoubleSequence random = DoubleSequence.random(() -> new Random(17), 1000);

		twice(() -> assertThat(random.limit(5),
		                       containsDoubles(732.3115139597315, 697.3704783607498, 82.95611145017068,
		                                       816.2364511057306, 44.3859375038691)));
	}

	@Test
	public void randomLowerUpper() {
		DoubleSequence random = DoubleSequence.random(1000, 2000);

		twice(() -> {
			DoubleIterator iterator = random.iterator();
			times(1000, () -> assertThat(iterator.nextDouble(),
			                             is(both(greaterThanOrEqualTo(1000.0)).and(lessThan(2000.0)))));
		});

		assertThat(random.limit(10), not(containsDoubles(random.limit(10))));
	}

	@Test
	public void randomLowerUpperWithSupplier() {
		DoubleSequence random = DoubleSequence.random(() -> new Random(17), 1000, 2000);

		twice(() -> assertThat(random.limit(5),
		                       containsDoubles(1732.3115139597317, 1697.3704783607498, 1082.95611145017068,
		                                       1816.2364511057306, 1044.3859375038691)));
	}

	@Test
	public void mapBack() {
		DoubleSequence emptyMappedBack = empty.mapBack(17, (p, x) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedBack, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedBack.iterator().nextDouble());

		DoubleSequence threeMappedBackToPrevious = _123.mapBack(17, (p, x) -> p);
		twice(() -> assertThat(threeMappedBackToPrevious, containsDoubles(17, 1, 2)));

		DoubleSequence threeMappedBackToCurrent = _123.mapBack(17, (p, x) -> x);
		twice(() -> assertThat(threeMappedBackToCurrent, containsDoubles(1, 2, 3)));

		assertThat(removeFirst(threeMappedBackToCurrent), is(1.0));
		twice(() -> assertThat(threeMappedBackToCurrent, containsDoubles(2, 3)));
		twice(() -> assertThat(_123, containsDoubles(2, 3)));
	}

	@Test
	public void mapForward() {
		DoubleSequence emptyMappedForward = empty.mapForward(17, (x, n) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedForward, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedForward.iterator().nextDouble());

		DoubleSequence threeMappedForwardToCurrent = _123.mapForward(17, (x, n) -> x);
		twice(() -> assertThat(threeMappedForwardToCurrent, containsDoubles(1, 2, 3)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(threeMappedForwardToCurrent));
		twice(() -> assertThat(threeMappedForwardToCurrent, containsDoubles(1, 2, 3)));

		DoubleSequence threeMappedForwardToNext = _123.mapForward(17, (x, n) -> n);
		twice(() -> assertThat(threeMappedForwardToNext, containsDoubles(2, 3, 17)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void window() {
		Sequence<DoubleSequence> emptyWindowed = empty.window(3);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<DoubleSequence> oneWindowed = _1.window(3);
		twice(() -> assertThat(oneWindowed, contains(containsDoubles(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(containsDoubles(1))));

		Sequence<DoubleSequence> twoWindowed = _12.window(3);
		twice(() -> assertThat(twoWindowed, contains(containsDoubles(1, 2))));

		Sequence<DoubleSequence> threeWindowed = _123.window(3);
		twice(() -> assertThat(threeWindowed, contains(containsDoubles(1, 2, 3))));

		Sequence<DoubleSequence> fourWindowed = _1234.window(3);
		twice(() -> assertThat(fourWindowed, contains(containsDoubles(1, 2, 3), containsDoubles(2, 3, 4))));

		Sequence<DoubleSequence> fiveWindowed = _12345.window(3);
		twice(() -> assertThat(fiveWindowed,
		                       contains(containsDoubles(1, 2, 3), containsDoubles(2, 3, 4), containsDoubles(3, 4, 5)
		                       )));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithSmallerStep() {
		Sequence<DoubleSequence> emptyWindowed = empty.window(3, 2);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<DoubleSequence> oneWindowed = _1.window(3, 2);
		twice(() -> assertThat(oneWindowed, contains(containsDoubles(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(containsDoubles(1))));

		Sequence<DoubleSequence> twoWindowed = _12.window(3, 2);
		twice(() -> assertThat(twoWindowed, contains(containsDoubles(1, 2))));

		Sequence<DoubleSequence> threeWindowed = _123.window(3, 2);
		twice(() -> assertThat(threeWindowed, contains(containsDoubles(1, 2, 3))));

		Sequence<DoubleSequence> fourWindowed = _1234.window(3, 2);
		twice(() -> assertThat(fourWindowed, contains(containsDoubles(1, 2, 3), containsDoubles(3, 4))));

		Sequence<DoubleSequence> fiveWindowed = _12345.window(3, 2);
		twice(() -> assertThat(fiveWindowed, contains(containsDoubles(1, 2, 3), containsDoubles(3, 4, 5))));

		Sequence<DoubleSequence> nineWindowed = _123456789.window(3, 2);
		twice(() -> assertThat(nineWindowed,
		                       contains(containsDoubles(1, 2, 3), containsDoubles(3, 4, 5), containsDoubles(5, 6, 7),
		                                containsDoubles(7, 8, 9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithLargerStep() {
		Sequence<DoubleSequence> emptyWindowed = empty.window(3, 4);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<DoubleSequence> oneWindowed = _1.window(3, 4);
		twice(() -> assertThat(oneWindowed, contains(containsDoubles(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(containsDoubles(1))));

		Sequence<DoubleSequence> twoWindowed = _12.window(3, 4);
		twice(() -> assertThat(twoWindowed, contains(containsDoubles(1, 2))));

		Sequence<DoubleSequence> threeWindowed = _123.window(3, 4);
		twice(() -> assertThat(threeWindowed, contains(containsDoubles(1, 2, 3))));

		Sequence<DoubleSequence> fourWindowed = _1234.window(3, 4);
		twice(() -> assertThat(fourWindowed, contains(containsDoubles(1, 2, 3))));

		Sequence<DoubleSequence> fiveWindowed = _12345.window(3, 4);
		twice(() -> assertThat(fiveWindowed, contains(containsDoubles(1, 2, 3), containsDoubles(5))));

		Sequence<DoubleSequence> nineWindowed = _123456789.window(3, 4);
		twice(() -> assertThat(nineWindowed,
		                       contains(containsDoubles(1, 2, 3), containsDoubles(5, 6, 7), containsDoubles(9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batch() {
		Sequence<DoubleSequence> emptyBatched = empty.batch(3);
		twice(() -> assertThat(emptyBatched, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<DoubleSequence> oneBatched = _1.batch(3);
		twice(() -> assertThat(oneBatched, contains(containsDoubles(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneBatched));
		twice(() -> assertThat(oneBatched, contains(containsDoubles(1))));

		Sequence<DoubleSequence> twoBatched = _12.batch(3);
		twice(() -> assertThat(twoBatched, contains(containsDoubles(1, 2))));

		Sequence<DoubleSequence> threeBatched = _123.batch(3);
		twice(() -> assertThat(threeBatched, contains(containsDoubles(1, 2, 3))));

		Sequence<DoubleSequence> fourBatched = _1234.batch(3);
		twice(() -> assertThat(fourBatched, contains(containsDoubles(1, 2, 3), containsDoubles(4))));

		Sequence<DoubleSequence> fiveBatched = _12345.batch(3);
		twice(() -> assertThat(fiveBatched, contains(containsDoubles(1, 2, 3), containsDoubles(4, 5))));

		Sequence<DoubleSequence> nineBatched = _123456789.batch(3);
		twice(() -> assertThat(nineBatched,
		                       contains(containsDoubles(1, 2, 3), containsDoubles(4, 5, 6), containsDoubles(7, 8, 9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnPredicate() {
		Sequence<DoubleSequence> emptyBatched = empty.batch((a, b) -> a > b);
		twice(() -> assertThat(emptyBatched, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<DoubleSequence> oneBatched = _1.batch((a, b) -> a > b);
		twice(() -> assertThat(oneBatched, contains(containsDoubles(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneBatched));
		twice(() -> assertThat(oneBatched, contains(containsDoubles(1))));

		Sequence<DoubleSequence> twoBatched = _12.batch((a, b) -> a > b);
		twice(() -> assertThat(twoBatched, contains(containsDoubles(1, 2))));

		Sequence<DoubleSequence> threeBatched = _123.batch((a, b) -> a > b);
		twice(() -> assertThat(threeBatched, contains(containsDoubles(1, 2, 3))));

		Sequence<DoubleSequence> threeRandomBatched = threeRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(threeRandomBatched, contains(containsDoubles(17, 32), containsDoubles(12))));

		Sequence<DoubleSequence> nineRandomBatched = nineRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(nineRandomBatched,
		                       contains(containsDoubles(6, 6), containsDoubles(1), containsDoubles(-7, 1, 2, 17),
		                                containsDoubles(5), containsDoubles(4))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void split() {
		Sequence<DoubleSequence> emptySplit = empty.split(3);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

		Sequence<DoubleSequence> oneSplit = _1.split(3);
		twice(() -> assertThat(oneSplit, contains(containsDoubles(1))));

		Sequence<DoubleSequence> twoSplit = _12.split(3);
		twice(() -> assertThat(twoSplit, contains(containsDoubles(1, 2))));

		Sequence<DoubleSequence> threeSplit = _123.split(3);
		twice(() -> assertThat(threeSplit, contains(containsDoubles(1, 2))));

		Sequence<DoubleSequence> fiveSplit = _12345.split(3);
		twice(() -> assertThat(fiveSplit, contains(containsDoubles(1, 2), containsDoubles(4, 5))));

		Sequence<DoubleSequence> nineSplit = _123456789.split(3);
		twice(() -> assertThat(nineSplit, contains(containsDoubles(1, 2), containsDoubles(4, 5, 6, 7, 8, 9))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSplit));
		twice(() -> assertThat(nineSplit, contains(containsDoubles(1, 2), containsDoubles(4, 5, 6, 7, 8, 9))));
		twice(() -> assertThat(_123456789, containsDoubles(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitDoublePredicate() {
		Sequence<DoubleSequence> emptySplit = empty.split(x -> x % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

		Sequence<DoubleSequence> oneSplit = _1.split(x -> x % 3 == 0);
		twice(() -> assertThat(oneSplit, contains(containsDoubles(1))));

		Sequence<DoubleSequence> twoSplit = _12.split(x -> x % 3 == 0);
		twice(() -> assertThat(twoSplit, contains(containsDoubles(1, 2))));

		Sequence<DoubleSequence> threeSplit = _123.split(x -> x % 3 == 0);
		twice(() -> assertThat(threeSplit, contains(containsDoubles(1, 2))));

		Sequence<DoubleSequence> fiveSplit = _12345.split(x -> x % 3 == 0);
		twice(() -> assertThat(fiveSplit, contains(containsDoubles(1, 2), containsDoubles(4, 5))));

		Sequence<DoubleSequence> nineSplit = _123456789.split(x -> x % 3 == 0);
		twice(() -> assertThat(nineSplit,
		                       contains(containsDoubles(1, 2), containsDoubles(4, 5), containsDoubles(7, 8))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSplit));
		twice(() -> assertThat(nineSplit,
		                       contains(containsDoubles(1, 2), containsDoubles(4, 5), containsDoubles(7, 8))));
		twice(() -> assertThat(_123456789, containsDoubles(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void filterClear() {
		List<Double> original = new ArrayList<>(asList(1.0, 2.0, 3.0, 4.0));

		DoubleSequence filtered = DoubleSequence.from(original).filter(x -> x % 2 != 0);
		filtered.clear();

		twice(() -> assertThat(filtered, is(emptyIterable())));
		assertThat(original, contains(2.0, 4.0));
	}

	@Test
	public void isEmpty() {
		twice(() -> assertThat(empty.isEmpty(), is(true)));
		twice(() -> assertThat(_1.isEmpty(), is(false)));
		twice(() -> assertThat(_12.isEmpty(), is(false)));
		twice(() -> assertThat(_12345.isEmpty(), is(false)));
	}

	@Test
	public void containsDouble() {
		assertThat(empty.containsDouble(17, 0.1), is(false));

		assertThat(_12345.containsDouble(1, 0.1), is(true));
		assertThat(_12345.containsDouble(3, 0.1), is(true));
		assertThat(_12345.containsDouble(5, 0.1), is(true));
		assertThat(_12345.containsDouble(17, 0.1), is(false));
	}

	@Test
	public void containsAllDoubles() {
		assertThat(empty.containsAllDoubles(new double[0], 0.5), is(true));
		assertThat(empty.containsAllDoubles(new double[]{17.1, 17.9, 19.1}, 0.5), is(false));

		assertThat(_12345.containsAllDoubles(new double[0], 0.5), is(true));
		assertThat(_12345.containsAllDoubles(new double[]{1.1}, 0.5), is(true));
		assertThat(_12345.containsAllDoubles(new double[]{1.1, 3.1, 5.1}, 0.5), is(true));
		assertThat(_12345.containsAllDoubles(new double[]{1.1, 1.9, 3.1, 3.9, 5.1}, 0.5), is(true));
		assertThat(_12345.containsAllDoubles(new double[]{1.1, 1.9, 3.1, 3.9, 5.1, 17.1}, 0.5), is(false));
		assertThat(_12345.containsAllDoubles(new double[]{17.1, 17.9, 19.1}, 0.5), is(false));
	}

	@Test
	public void containsAnyDoubles() {
		assertThat(empty.containsAnyDoubles(new double[0], 0.5), is(false));
		assertThat(empty.containsAnyDoubles(new double[]{17.1, 17.9, 18.1}, 0.5), is(false));

		assertThat(_12345.containsAnyDoubles(new double[0], 0.5), is(false));
		assertThat(_12345.containsAnyDoubles(new double[]{1.1}, 0.5), is(true));
		assertThat(_12345.containsAnyDoubles(new double[]{1.1, 3.1, 5.1}, 0.5), is(true));
		assertThat(_12345.containsAnyDoubles(new double[]{1.1, 1.9, 3.1, 3.9, 5.1}, 0.5), is(true));
		assertThat(_12345.containsAnyDoubles(new double[]{1.1, 1.9, 3.1, 3.9, 5.1, 17.1}, 0.5), is(true));
		assertThat(_12345.containsAnyDoubles(new double[]{17.1, 17.9, 19.1}, 0.5), is(false));
	}
}
