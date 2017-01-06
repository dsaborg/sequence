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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DoubleSequenceTest {
	private final DoubleSequence empty = DoubleSequence.empty();

	private final DoubleSequence _1 = DoubleSequence.from(DoubleIterable.of(1.0));
	private final DoubleSequence _12 = DoubleSequence.from(DoubleIterable.of(1.0, 2.0));
	private final DoubleSequence _123 = DoubleSequence.from(DoubleIterable.of(1.0, 2.0, 3.0));
	private final DoubleSequence _1234 = DoubleSequence.from(DoubleIterable.of(1.0, 2.0, 3.0, 4.0));
	private final DoubleSequence _12345 = DoubleSequence.from(DoubleIterable.of(1.0, 2.0, 3.0, 4.0, 5.0));
	private final DoubleSequence _123456789 =
			DoubleSequence.from(DoubleIterable.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0));

	private final DoubleSequence oneRandom = DoubleSequence.from(DoubleIterable.of(17.0));
	private final DoubleSequence twoRandom = DoubleSequence.from(DoubleIterable.of(17.0, 32.0));
	private final DoubleSequence threeRandom = DoubleSequence.from(DoubleIterable.of(17.0, 32.0, 12.0));
	private final DoubleSequence nineRandom =
			DoubleSequence.from(DoubleIterable.of(6.0, 6.0, 1.0, -7.0, 1.0, 2.0, 17.0, 5.0, 4.0));

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
			assertThat(iterator.hasNext(), is(false));
		});
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
	public void skip() {
		DoubleSequence skipNone = _123.skip(0);
		twice(() -> assertThat(skipNone, containsDoubles(1.0, 2.0, 3.0)));

		DoubleSequence skipOne = _123.skip(1);
		twice(() -> assertThat(skipOne, containsDoubles(2.0, 3.0)));

		DoubleSequence skipTwo = _123.skip(2);
		twice(() -> assertThat(skipTwo, containsDoubles(3.0)));

		DoubleSequence skipThree = _123.skip(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		DoubleSequence skipFour = _123.skip(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void skipTail() {
		DoubleSequence skipNone = _123.skipTail(0);
		twice(() -> assertThat(skipNone, containsDoubles(1, 2, 3)));

		DoubleSequence skipOne = _123.skipTail(1);
		twice(() -> assertThat(skipOne, containsDoubles(1, 2)));

		DoubleSequence skipTwo = _123.skipTail(2);
		twice(() -> assertThat(skipTwo, containsDoubles(1)));

		DoubleSequence skipThree = _123.skipTail(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		DoubleSequence skipFour = _123.skipTail(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		DoubleSequence limitNone = _123.limit(0);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		DoubleSequence limitOne = _123.limit(1);
		twice(() -> assertThat(limitOne, containsDoubles(1.0)));

		DoubleSequence limitTwo = _123.limit(2);
		twice(() -> assertThat(limitTwo, containsDoubles(1.0, 2.0)));

		DoubleSequence limitThree = _123.limit(3);
		twice(() -> assertThat(limitThree, containsDoubles(1.0, 2.0, 3.0)));

		DoubleSequence limitFour = _123.limit(4);
		twice(() -> assertThat(limitFour, containsDoubles(1.0, 2.0, 3.0)));
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
	public void appendPrimitiveIteratorOfDouble() {
		DoubleSequence appended = _123.append(DoubleIterator.of(4.0, 5.0, 6.0)).append(DoubleIterator.of(7.0, 8.0));

		assertThat(appended, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
		assertThat(appended, containsDoubles(1.0, 2.0, 3.0));
	}

	@Test
	public void appendIterator() {
		DoubleSequence appended = _123.append(Iterators.of(4.0, 5.0, 6.0)).append(Iterators.of(7.0, 8.0));

		assertThat(appended, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
		assertThat(appended, containsDoubles(1.0, 2.0, 3.0));
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
	public void appendArray() {
		DoubleSequence appended = _123.append(4.0, 5.0, 6.0).append(7.0, 8.0);

		twice(() -> assertThat(appended, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)));
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
		DoubleSequence emptyFiltered = empty.filter(i -> (i % 2) == 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));

		DoubleSequence oneFiltered = _1.filter(i -> (i % 2) == 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		DoubleSequence twoFiltered = _12.filter(i -> (i % 2) == 0);
		twice(() -> assertThat(twoFiltered, containsDoubles(2)));

		DoubleSequence filtered = _123456789.filter(i -> (i % 2) == 0);
		twice(() -> assertThat(filtered, containsDoubles(2, 4, 6, 8)));
	}

	@Test
	public void filterIndexed() {
		DoubleSequence emptyFiltered = empty.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));

		DoubleSequence oneFiltered = _1.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		DoubleSequence twoFiltered = _12.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(twoFiltered, containsDoubles(2)));

		DoubleSequence filtered = _123456789.filterIndexed((i, x) -> x > 3);
		twice(() -> assertThat(filtered, containsDoubles(5, 6, 7, 8, 9)));
	}

	@Test
	public void filterBack() {
		DoubleSequence filteredLess = nineRandom.filterBack(117, (p, i) -> p < i);
		twice(() -> assertThat(filteredLess, containsDoubles(1.0, 2.0, 17.0)));

		DoubleSequence filteredGreater = nineRandom.filterBack(117, (p, i) -> p > i);
		twice(() -> assertThat(filteredGreater, containsDoubles(6.0, 1.0, -7.0, 5.0, 4.0)));
	}

	@Test
	public void filterForward() {
		DoubleSequence filteredLess = nineRandom.filterForward(117, (i, f) -> f < i);
		twice(() -> assertThat(filteredLess, containsDoubles(6.0, 1.0, 17.0, 5.0)));

		DoubleSequence filteredGreater = nineRandom.filterForward(117, (i, f) -> f > i);
		twice(() -> assertThat(filteredGreater, containsDoubles(-7.0, 1.0, 2.0, 4.0)));
	}

	@Test
	public void includingExactlyArray() {
		DoubleSequence emptyIncluding = empty.includingExactly(1, 3, 5, 17);
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));

		DoubleSequence including = _12345.includingExactly(1, 3, 5, 17);
		twice(() -> assertThat(including, containsDoubles(1, 3, 5)));

		DoubleSequence includingAll = _12345.includingExactly(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(includingAll, containsDoubles(1, 2, 3, 4, 5)));

		DoubleSequence includingNone = _12345.includingExactly();
		twice(() -> assertThat(includingNone, is(emptyIterable())));
	}

	@Test
	public void includingArray() {
		DoubleSequence emptyIncluding = empty.including(new double[]{1.1, 2.9, 5.1, 17.1}, 0.5);
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));

		DoubleSequence including = _12345.including(new double[]{1.1, 2.9, 5.1, 17.1}, 0.5);
		twice(() -> assertThat(including, containsDoubles(1, 3, 5)));

		DoubleSequence includingAll = _12345.including(new double[]{1.1, 1.9, 3.1, 3.9, 5.1, 17.1}, 0.5);
		twice(() -> assertThat(includingAll, containsDoubles(1, 2, 3, 4, 5)));

		DoubleSequence includingNone = _12345.including(new double[0], 0.5);
		twice(() -> assertThat(includingNone, is(emptyIterable())));
	}

	@Test
	public void excludingExactlyArray() {
		DoubleSequence emptyExcluding = empty.excludingExactly(1, 3, 5, 17);
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));

		DoubleSequence excluding = _12345.excludingExactly(1, 3, 5, 17);
		twice(() -> assertThat(excluding, containsDoubles(2, 4)));

		DoubleSequence excludingAll = _12345.excludingExactly(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		DoubleSequence excludingNone = _12345.excludingExactly();
		twice(() -> assertThat(excludingNone, containsDoubles(1, 2, 3, 4, 5)));
	}

	@Test
	public void excludingArray() {
		DoubleSequence emptyExcluding = empty.excluding(new double[]{1.1, 2.9, 5.1, 17.1}, 0.5);
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));

		DoubleSequence excluding = _12345.excluding(new double[]{1.1, 2.9, 5.1, 17.1}, 0.5);
		twice(() -> assertThat(excluding, containsDoubles(2, 4)));

		DoubleSequence excludingAll = _12345.excluding(new double[]{1.1, 1.9, 3.1, 3.9, 5.1, 17.1}, 0.5);
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		DoubleSequence excludingNone = _12345.excluding(new double[0], 0.5);
		twice(() -> assertThat(excludingNone, containsDoubles(1, 2, 3, 4, 5)));
	}

	@Test
	public void map() {
		DoubleSequence emptyMapped = empty.map(d -> d + 1);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));

		DoubleSequence oneMapped = _1.map(d -> d + 1);
		twice(() -> assertThat(oneMapped, containsDoubles(2)));

		DoubleSequence twoMapped = _12.map(d -> d + 1);
		twice(() -> assertThat(twoMapped, containsDoubles(2, 3)));

		DoubleSequence fiveMapped = _12345.map(d -> d + 1);
		twice(() -> assertThat(fiveMapped, containsDoubles(2, 3, 4, 5, 6)));
	}

	@Test
	public void mapWithIndex() {
		DoubleSequence emptyMapped = empty.mapIndexed((i, x) -> i + x);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));

		DoubleSequence oneMapped = _1.mapIndexed((i, x) -> i + x);
		twice(() -> assertThat(oneMapped, containsDoubles(1)));

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
		DoubleSequence until = DoubleSequence.recurse(1, x -> x + 1).until(7.0, 0.1);
		twice(() -> assertThat(until, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void endingAtTerminal() {
		DoubleSequence endingAt = DoubleSequence.recurse(1, x -> x + 1).endingAt(7.0, 0.1);
		twice(() -> assertThat(endingAt, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0)));
	}

	@Test
	public void untilPredicate() {
		DoubleSequence until = DoubleSequence.recurse(1, x -> x + 1).until(d -> d == 7.0);
		twice(() -> assertThat(until, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void endingAtPredicate() {
		DoubleSequence endingAt = DoubleSequence.recurse(1, x -> x + 1).endingAt(d -> d == 7.0);
		twice(() -> assertThat(endingAt, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0)));
	}

	@Test
	public void startingAfter() {
		DoubleSequence startingEmpty = empty.startingAfter(5, 0.1);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		DoubleSequence sequence = _123456789.startingAfter(5, 0.1);
		twice(() -> assertThat(sequence, containsDoubles(6, 7, 8, 9)));

		DoubleSequence noStart = _12345.startingAfter(10, 0.1);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingAfterPredicate() {
		DoubleSequence startingEmpty = empty.startingAfter(i -> i == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		DoubleSequence sequence = _123456789.startingAfter(i -> i == 5);
		twice(() -> assertThat(sequence, containsDoubles(6, 7, 8, 9)));

		DoubleSequence noStart = _12345.startingAfter(i -> i == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFrom() {
		DoubleSequence startingEmpty = empty.startingFrom(5, 0.1);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		DoubleSequence sequence = _123456789.startingFrom(5, 0.1);
		twice(() -> assertThat(sequence, containsDoubles(5, 6, 7, 8, 9)));

		DoubleSequence noStart = _12345.startingFrom(10, 0.1);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFromPredicate() {
		DoubleSequence startingEmpty = empty.startingFrom(i -> i == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		DoubleSequence sequence = _123456789.startingFrom(i -> i == 5);
		twice(() -> assertThat(sequence, containsDoubles(5, 6, 7, 8, 9)));

		DoubleSequence noStart = _12345.startingFrom(i -> i == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
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
		DoubleSequence stepThree = _123456789.step(3);
		twice(() -> assertThat(stepThree, containsDoubles(1.0, 4.0, 7.0)));
	}

	@Test
	public void distinct() {
		DoubleSequence emptyDistinct = empty.distinct(0.5);
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		DoubleSequence oneDistinct = oneRandom.distinct(0.5);
		twice(() -> assertThat(oneDistinct, containsDoubles(17)));

		DoubleSequence twoDuplicatesDistinct = DoubleSequence.from(DoubleIterable.of(17, 17.15, 17.3))
		                                                     .distinct(0.2);
		twice(() -> assertThat(twoDuplicatesDistinct, containsDoubles(17, 17.3)));

		DoubleSequence nineDistinct = nineRandom.distinct(0.5);
		twice(() -> assertThat(nineDistinct, containsDoubles(6, 1, -7, 2, 17, 5, 4)));
	}

	@Test
	public void distinctExactly() {
		DoubleSequence emptyDistinct = empty.distinctExactly();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		DoubleSequence oneDistinct = oneRandom.distinctExactly();
		twice(() -> assertThat(oneDistinct, containsDoubles(17)));

		DoubleSequence twoDuplicatesDistinct = DoubleSequence.from(DoubleIterable.of(17, 17)).distinctExactly();
		twice(() -> assertThat(twoDuplicatesDistinct, containsDoubles(17)));

		DoubleSequence nineDistinct = nineRandom.distinctExactly();
		twice(() -> assertThat(nineDistinct, containsDoubles(6, 1, -7, 2, 17, 5, 4)));
	}

	@Test
	public void sorted() {
		DoubleSequence emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		DoubleSequence oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, containsDoubles(17.0)));

		DoubleSequence twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, containsDoubles(17.0, 32.0)));

		DoubleSequence nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, containsDoubles(-7.0, 1.0, 1.0, 2.0, 4.0, 5.0, 6.0, 6.0, 17.0)));
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
		DoubleSequence peekEmpty = empty.peek(x -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(peekEmpty, is(emptyIterable())));

		AtomicInteger value = new AtomicInteger(1);
		DoubleSequence peekOne = _1.peek(x -> assertThat(x, is((double) value.getAndIncrement())));
		twiceIndexed(value, 1, () -> assertThat(peekOne, containsDoubles(1)));

		DoubleSequence peekTwo = _12.peek(x -> assertThat(x, is((double) value.getAndIncrement())));
		twiceIndexed(value, 2, () -> assertThat(peekTwo, containsDoubles(1, 2)));

		DoubleSequence peek = _12345.peek(x -> assertThat(x, is((double) value.getAndIncrement())));
		twiceIndexed(value, 5, () -> assertThat(peek, containsDoubles(1, 2, 3, 4, 5)));
	}

	@Test
	public void peekIndexed() {
		DoubleSequence peekEmpty = empty.peekIndexed((i, x) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(peekEmpty, is(emptyIterable())));

		AtomicInteger index = new AtomicInteger();
		DoubleSequence peekOne = _1.peekIndexed((i, x) -> {
			assertThat(i, is((double) (index.get() + 1)));
			assertThat(x, is(index.getAndIncrement()));
		});
		twiceIndexed(index, 1, () -> assertThat(peekOne, containsDoubles(1)));

		DoubleSequence peekTwo = _12.peekIndexed((i, x) -> {
			assertThat(i, is((double) (index.get() + 1)));
			assertThat(x, is(index.getAndIncrement()));
		});
		twiceIndexed(index, 2, () -> assertThat(peekTwo, containsDoubles(1, 2)));

		DoubleSequence peek = _12345.peekIndexed((i, x) -> {
			assertThat(i, is((double) (index.get() + 1)));
			assertThat(x, is(index.getAndIncrement()));
		});
		twiceIndexed(index, 5, () -> assertThat(peek, containsDoubles(1, 2, 3, 4, 5)));
	}

	@Test
	public void stream() {
		DoubleSequence empty = DoubleSequence.empty();
		twice(() -> assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable())));

		DoubleSequence sequence = DoubleSequence.of(1, 2, 3, 4, 5);
		twice(() -> assertThat(sequence.stream().collect(Collectors.toList()), contains(1.0, 2.0, 3.0, 4.0, 5.0)));
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
		DoubleSequence prefixEmpty = empty.prefix(327.0);
		twice(() -> assertThat(prefixEmpty, containsDoubles(327.0)));

		DoubleSequence prefix = _123.prefix(327.0);
		twice(() -> assertThat(prefix, containsDoubles(327.0, 1.0, 2.0, 3.0)));
	}

	@Test
	public void suffix() {
		DoubleSequence suffixEmpty = empty.suffix(532.0);
		twice(() -> assertThat(suffixEmpty, containsDoubles(532.0)));

		DoubleSequence suffix = _123.suffix(532.0);
		twice(() -> assertThat(suffix, containsDoubles(1.0, 2.0, 3.0, 532.0)));
	}

	@Test
	public void interleave() {
		assertThat(empty.interleave(empty), is(emptyIterable()));
		assertThat(_123.interleave(_12345), containsDoubles(1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 5.0));
		assertThat(_12345.interleave(_123), containsDoubles(1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void reverse() {
		DoubleSequence emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		DoubleSequence oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, containsDoubles(1.0)));

		DoubleSequence twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, containsDoubles(2.0, 1.0)));

		DoubleSequence threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, containsDoubles(3.0, 2.0, 1.0)));

		DoubleSequence nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed, containsDoubles(9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0)));
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
		IntSequence empty = DoubleSequence.empty().toInts();
		twice(() -> assertThat(empty, is(emptyIterable())));

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
	public void toLongs() {
		LongSequence empty = DoubleSequence.empty().toLongs();
		twice(() -> assertThat(empty, is(emptyIterable())));

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
	public void toRoundedInts() {
		IntSequence empty = DoubleSequence.empty().toRoundedInts();
		twice(() -> assertThat(empty, is(emptyIterable())));

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
	public void toRoundedLongs() {
		LongSequence empty = DoubleSequence.empty().toRoundedLongs();
		twice(() -> assertThat(empty, is(emptyIterable())));

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
	public void toIntsMapped() {
		IntSequence empty = DoubleSequence.empty().toInts(d -> (int) (d * 2));
		twice(() -> assertThat(empty, is(emptyIterable())));

		IntSequence doubledHalves = DoubleSequence.range(0.5, 1.5, 0.5, 0.1).toInts(d -> (int) (d * 2));
		twice(() -> assertThat(doubledHalves, containsInts(1, 2, 3)));
	}

	@Test
	public void toLongsMapped() {
		LongSequence empty = DoubleSequence.empty().toLongs(d -> (long) (d * 2));
		twice(() -> assertThat(empty, is(emptyIterable())));

		LongSequence doubledHalves = DoubleSequence.range(0.5, 1.5, 0.5, 0.1).toLongs(d -> (long) (d * 2));
		twice(() -> assertThat(doubledHalves, containsLongs(1L, 2L, 3L)));
	}

	@Test
	public void toSequence() {
		Sequence<Double> empty = DoubleSequence.empty().toSequence(d -> d + 1);
		twice(() -> assertThat(empty, is(emptyIterable())));

		Sequence<Double> doubles = _12345.toSequence(d -> d + 1);
		twice(() -> assertThat(doubles, contains(2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void box() {
		Sequence<Double> empty = DoubleSequence.empty().box();
		twice(() -> assertThat(empty, is(emptyIterable())));

		Sequence<Double> doubles = _12345.box();
		twice(() -> assertThat(doubles, contains(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void repeat() {
		DoubleSequence repeatEmpty = empty.repeat();
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		DoubleSequence repeatOne = _1.repeat();
		twice(() -> assertThat(repeatOne.limit(3), containsDoubles(1.0, 1.0, 1.0)));

		DoubleSequence repeatTwo = _12.repeat();
		twice(() -> assertThat(repeatTwo.limit(5), containsDoubles(1.0, 2.0, 1.0, 2.0, 1.0)));

		DoubleSequence repeatThree = _123.repeat();
		twice(() -> assertThat(repeatThree.limit(8), containsDoubles(1.0, 2.0, 3.0, 1.0, 2.0, 3.0, 1.0, 2.0)));

		DoubleSequence repeatVarying = DoubleSequence.from(new DoubleIterable() {
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
		assertThat(repeatVarying, containsDoubles(1.0, 2.0, 3.0, 1.0, 2.0, 1.0));
	}

	@Test
	public void repeatTwice() {
		DoubleSequence repeatEmpty = empty.repeat(2);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		DoubleSequence repeatOne = _1.repeat(2);
		twice(() -> assertThat(repeatOne, containsDoubles(1.0, 1.0)));

		DoubleSequence repeatTwo = _12.repeat(2);
		twice(() -> assertThat(repeatTwo, containsDoubles(1.0, 2.0, 1.0, 2.0)));

		DoubleSequence repeatThree = _123.repeat(2);
		twice(() -> assertThat(repeatThree, containsDoubles(1.0, 2.0, 3.0, 1.0, 2.0, 3.0)));

		DoubleSequence repeatVarying = DoubleSequence.from(new DoubleIterable() {
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
		assertThat(repeatVarying, containsDoubles(1.0, 2.0, 3.0, 1.0, 2.0));
	}

	@Test
	public void repeatZero() {
		DoubleSequence repeatEmpty = empty.repeat(0);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		DoubleSequence repeatOne = _1.repeat(0);
		twice(() -> assertThat(repeatOne, is(emptyIterable())));

		DoubleSequence repeatTwo = _12.repeat(0);
		twice(() -> assertThat(repeatTwo, is(emptyIterable())));

		DoubleSequence repeatThree = _123.repeat(0);
		twice(() -> assertThat(repeatThree, is(emptyIterable())));
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
		twice(() -> assertThat(_123.mapBack(17.0, (p, x) -> p), containsDoubles(17.0, 1.0, 2.0)));
		twice(() -> assertThat(_123.mapBack(17.0, (p, x) -> x), containsDoubles(1.0, 2.0, 3.0)));
	}

	@Test
	public void mapForward() {
		twice(() -> assertThat(_123.mapForward(17.0, (x, n) -> x), containsDoubles(1.0, 2.0, 3.0)));
		twice(() -> assertThat(_123.mapForward(17.0, (x, n) -> n), containsDoubles(2.0, 3.0, 17.0)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void window() {
		twice(() -> assertThat(empty.window(3), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3), contains(containsDoubles(1.0))));
		twice(() -> assertThat(_12.window(3), contains(containsDoubles(1.0, 2.0))));
		twice(() -> assertThat(_123.window(3), contains(containsDoubles(1.0, 2.0, 3.0))));
		twice(() -> assertThat(_1234.window(3),
		                       contains(containsDoubles(1.0, 2.0, 3.0), containsDoubles(2.0, 3.0, 4.0))));
		twice(() -> assertThat(_12345.window(3),
		                       contains(containsDoubles(1.0, 2.0, 3.0), containsDoubles(2.0, 3.0, 4.0),
		                                containsDoubles(3.0, 4.0, 5.0))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithStep() {
		twice(() -> assertThat(empty.window(3, 2), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3, 2), contains(containsDoubles(1.0))));
		twice(() -> assertThat(_12.window(3, 2), contains(containsDoubles(1.0, 2.0))));
		twice(() -> assertThat(_123.window(3, 2), contains(containsDoubles(1.0, 2.0, 3.0))));
		twice(() -> assertThat(_1234.window(3, 2),
		                       contains(containsDoubles(1.0, 2.0, 3.0), containsDoubles(3.0, 4.0))));
		twice(() -> assertThat(_12345.window(3, 2),
		                       contains(containsDoubles(1.0, 2.0, 3.0), containsDoubles(3.0, 4.0, 5.0))));
		twice(() -> assertThat(_123456789.window(3, 2),
		                       contains(containsDoubles(1.0, 2.0, 3.0), containsDoubles(3.0, 4.0, 5.0),
		                                containsDoubles(5.0, 6.0, 7.0), containsDoubles(7.0, 8.0, 9.0))));

		twice(() -> assertThat(empty.window(3, 4), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3, 4), contains(containsDoubles(1.0))));
		twice(() -> assertThat(_12.window(3, 4), contains(containsDoubles(1.0, 2.0))));
		twice(() -> assertThat(_123.window(3, 4), contains(containsDoubles(1.0, 2.0, 3.0))));
		twice(() -> assertThat(_1234.window(3, 4), contains(containsDoubles(1.0, 2.0, 3.0))));
		twice(() -> assertThat(_12345.window(3, 4), contains(containsDoubles(1.0, 2.0, 3.0), containsDoubles(5.0))));
		twice(() -> assertThat(_123456789.window(3, 4),
		                       contains(containsDoubles(1.0, 2.0, 3.0), containsDoubles(5.0, 6.0, 7.0),
		                                containsDoubles(9.0))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batch() {
		twice(() -> assertThat(empty.batch(3), is(emptyIterable())));
		twice(() -> assertThat(_1.batch(3), contains(containsDoubles(1.0))));
		twice(() -> assertThat(_12.batch(3), contains(containsDoubles(1.0, 2.0))));
		twice(() -> assertThat(_123.batch(3), contains(containsDoubles(1.0, 2.0, 3.0))));
		twice(() -> assertThat(_1234.batch(3), contains(containsDoubles(1.0, 2.0, 3.0), containsDoubles(4.0))));
		twice(() -> assertThat(_12345.batch(3), contains(containsDoubles(1.0, 2.0, 3.0), containsDoubles(4.0, 5.0))));
		twice(() -> assertThat(_123456789.batch(3),
		                       contains(containsDoubles(1.0, 2.0, 3.0), containsDoubles(4.0, 5.0, 6.0),
		                                containsDoubles(7.0, 8.0, 9.0))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnPredicate() {
		Sequence<DoubleSequence> emptyBatched = empty.batch((a, b) -> a > b);
		twice(() -> assertThat(emptyBatched, is(emptyIterable())));

		Sequence<DoubleSequence> oneBatched = _1.batch((a, b) -> a > b);
		twice(() -> assertThat(oneBatched, contains(containsDoubles(1.0))));

		Sequence<DoubleSequence> twoBatched = _12.batch((a, b) -> a > b);
		twice(() -> assertThat(twoBatched, contains(containsDoubles(1.0, 2.0))));

		Sequence<DoubleSequence> threeBatched = _123.batch((a, b) -> a > b);
		twice(() -> assertThat(threeBatched, contains(containsDoubles(1.0, 2.0, 3.0))));

		Sequence<DoubleSequence> threeRandomBatched = threeRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(threeRandomBatched, contains(containsDoubles(17.0, 32.0), containsDoubles(12.0))));

		Sequence<DoubleSequence> nineRandomBatched = nineRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(nineRandomBatched, contains(containsDoubles(6.0, 6.0), containsDoubles(1.0),
		                                                       containsDoubles(-7.0, 1.0, 2.0, 17.0),
		                                                       containsDoubles(5.0), containsDoubles(4.0))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void split() {
		Sequence<DoubleSequence> emptySplit = empty.split(3);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

		Sequence<DoubleSequence> oneSplit = _1.split(3);
		twice(() -> assertThat(oneSplit, contains(containsDoubles(1))));

		Sequence<DoubleSequence> twoSplit = _12.split(3);
		twice(() -> assertThat(twoSplit, contains(containsDoubles(1, 2))));

		Sequence<DoubleSequence> threeSplit = _123.split(3);
		twice(() -> assertThat(threeSplit, contains(containsDoubles(1, 2))));

		Sequence<DoubleSequence> fiveSplit = _12345.split(3);
		twice(() -> assertThat(fiveSplit, contains(containsDoubles(1, 2), containsDoubles(4, 5))));

		Sequence<DoubleSequence> nineSplit = _123456789.split(3);
		twice(() -> assertThat(nineSplit,
		                       contains(containsDoubles(1, 2), containsDoubles(4, 5, 6, 7, 8, 9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitDoublePredicate() {
		Sequence<DoubleSequence> emptySplit = empty.split(x -> x % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

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
	}

	@Test
	public void removeAllAfterFilter() {
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
