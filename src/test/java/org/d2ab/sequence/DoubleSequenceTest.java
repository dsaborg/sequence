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

import org.d2ab.iterable.Iterables;
import org.d2ab.iterable.doubles.DoubleIterable;
import org.d2ab.iterator.Iterators;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.iterator.doubles.MappedDoubleIterator;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DoubleSequenceTest {
	private final DoubleSequence empty = DoubleSequence.empty();

	private final DoubleSequence _1 = DoubleSequence.from(StrictDoubleIterable.of(1.0));
	private final DoubleSequence _12 = DoubleSequence.from(StrictDoubleIterable.of(1.0, 2.0));
	private final DoubleSequence _123 = DoubleSequence.from(StrictDoubleIterable.of(1.0, 2.0, 3.0));
	private final DoubleSequence _1234 = DoubleSequence.from(StrictDoubleIterable.of(1.0, 2.0, 3.0, 4.0));
	private final DoubleSequence _12345 = DoubleSequence.from(StrictDoubleIterable.of(1.0, 2.0, 3.0, 4.0, 5.0));
	private final DoubleSequence _123456789 =
			DoubleSequence.from(StrictDoubleIterable.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0));

	private final DoubleSequence oneRandom = DoubleSequence.from(StrictDoubleIterable.of(17.0));
	private final DoubleSequence twoRandom = DoubleSequence.from(StrictDoubleIterable.of(17.0, 32.0));
	private final DoubleSequence threeRandom = DoubleSequence.from(StrictDoubleIterable.of(17.0, 32.0, 12.0));
	private final DoubleSequence nineRandom =
			DoubleSequence.from(StrictDoubleIterable.of(6.0, 6.0, 1.0, -7.0, 1.0, 2.0, 17.0, 5.0, 4.0));

	@Test
	public void ofOne() {
		twice(() -> assertThat(_1, containsDoubles(1.0)));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(_12345, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void forLoop() {
		twice(() -> {
			for (double ignored : empty)
				fail("Should not get called");
		});

		DoubleSequence sequence = DoubleSequence.of(1, 2, 3, 4, 5);
		twice(() -> {
			double expected = 1.0;
			for (double d : sequence)
				assertThat(d, is(expected++));

			assertThat(expected, is(6.0));
		});
	}

	@Test
	public void forEach() {
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
		Iterable<Double> iterable = List.of(1.0, 2.0, 3.0, 4.0, 5.0)::iterator;

		DoubleSequence sequence = DoubleSequence.from(iterable);

		twice(() -> assertThat(sequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void fromDoubleIterable() {
		DoubleSequence sequence = DoubleSequence.from(DoubleIterable.of(1.0, 2.0, 3.0, 4.0, 5.0));

		twice(() -> assertThat(sequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void fromPrimitiveIteratorOfDouble() {
		DoubleSequence sequence = DoubleSequence.from(DoubleIterator.of(1.0, 2.0, 3.0, 4.0, 5.0));

		assertThat(sequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void fromIterator() {
		DoubleSequence sequence = DoubleSequence.from(Iterators.of(1.0, 2.0, 3.0, 4.0, 5.0));

		assertThat(sequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void fromDoubleStream() {
		DoubleSequence sequence = DoubleSequence.from(DoubleStream.of(1.0, 2.0, 3.0, 4.0, 5.0));

		assertThat(sequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void fromStream() {
		DoubleSequence sequence = DoubleSequence.from(Stream.of(1.0, 2.0, 3.0, 4.0, 5.0));

		assertThat(sequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void fromEmptyStream() {
		DoubleSequence sequence = DoubleSequence.from(Stream.of());

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
		DoubleSequence appended = _123.append(StrictDoubleIterable.of(4.0, 5.0, 6.0))
		                              .append(StrictDoubleIterable.of(7.0, 8.0));

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

		DoubleSequence.from(first).append(second).append(third);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));
		assertThat(third.hasNext(), is(true));
	}

	@Test
	public void appendIsLazyWhenSkippingHasNext() {
		DoubleIterator first = DoubleIterator.of(1.0);
		DoubleIterator second = DoubleIterator.of(2.0);

		DoubleSequence sequence = DoubleSequence.from(first).append(() -> second);

		// check delayed iteration
		DoubleIterator iterator = sequence.iterator();
		assertThat(iterator.next(), is(1.0));
		assertThat(iterator.next(), is(2.0));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		DoubleSequence filtered = DoubleSequence.from(StrictDoubleIterable.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0))
		                                        .filter(i -> (i % 2.0) == 0.0);

		twice(() -> assertThat(filtered, containsDoubles(2.0, 4.0, 6.0)));
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
	public void map() {
		DoubleSequence mapped = _123.map(c -> c + 1.0);
		twice(() -> assertThat(mapped, containsDoubles(2.0, 3.0, 4.0)));
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
	public void collect() {
		twice(() -> {
			StringBuilder builder = _123.collect(StringBuilder::new, StringBuilder::append);
			assertThat(builder.toString(), is("1.02.03.0"));
		});
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(Arrays.equals(_123.toArray(), new double[]{1.0, 2.0, 3.0}), is(true)));
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
	public void second() {
		twice(() -> {
			assertThat(empty.second(), is(OptionalDouble.empty()));
			assertThat(_1.second(), is(OptionalDouble.empty()));
			assertThat(_12.second(), is(OptionalDouble.of(2.0)));
			assertThat(_123.second(), is(OptionalDouble.of(2.0)));
			assertThat(_1234.second(), is(OptionalDouble.of(2.0)));
		});
	}

	@Test
	public void third() {
		twice(() -> {
			assertThat(empty.third(), is(OptionalDouble.empty()));
			assertThat(_1.third(), is(OptionalDouble.empty()));
			assertThat(_12.third(), is(OptionalDouble.empty()));
			assertThat(_123.third(), is(OptionalDouble.of(3.0)));
			assertThat(_1234.third(), is(OptionalDouble.of(3.0)));
			assertThat(_12345.third(), is(OptionalDouble.of(3.0)));
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
	public void step() {
		DoubleSequence stepThree = _123456789.step(3);
		twice(() -> assertThat(stepThree, containsDoubles(1.0, 4.0, 7.0)));
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
		List<Double> backing = new ArrayList<>(List.of(2.0, 3.0, 1.0));
		DoubleSequence sorted = DoubleSequence.from(backing).sorted();

		backing.add(4.0);
		assertThat(sorted, containsDoubles(1.0, 2.0, 3.0, 4.0));
	}

	@Test
	public void min() {
		OptionalDouble emptyMin = empty.min();
		twice(() -> assertThat(emptyMin, is(OptionalDouble.empty())));

		OptionalDouble oneMin = oneRandom.min();
		twice(() -> assertThat(oneMin, is(OptionalDouble.of(17.0))));

		OptionalDouble twoMin = twoRandom.min();
		twice(() -> assertThat(twoMin, is(OptionalDouble.of(17.0))));

		OptionalDouble nineMin = nineRandom.min();
		twice(() -> assertThat(nineMin, is(OptionalDouble.of(-7.0))));
	}

	@Test
	public void max() {
		OptionalDouble emptyMax = empty.max();
		twice(() -> assertThat(emptyMax, is(OptionalDouble.empty())));

		OptionalDouble oneMax = oneRandom.max();
		twice(() -> assertThat(oneMax, is(OptionalDouble.of(17.0))));

		OptionalDouble twoMax = twoRandom.max();
		twice(() -> assertThat(twoMax, is(OptionalDouble.of(32.0))));

		OptionalDouble nineMax = nineRandom.max();
		twice(() -> assertThat(nineMax, is(OptionalDouble.of(17.0))));
	}

	@Test
	public void count() {
		twice(() -> assertThat(empty.count(), is(0L)));
		twice(() -> assertThat(_1.count(), is(1L)));
		twice(() -> assertThat(_12.count(), is(2L)));
		twice(() -> assertThat(_123456789.count(), is(9L)));
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
		DoubleSequence peek = _123.peek(x -> assertThat(x, is(both(greaterThan(0.0)).and(lessThan(4.0)))));
		twice(() -> assertThat(peek, containsDoubles(1.0, 2.0, 3.0)));
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
		List<Double> backing = new ArrayList<>(List.of(1.0, 2.0, 3.0));
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
			private List<Double> list = List.of(1.0, 2.0, 3.0);
			int end = list.size();

			@Override
			public DoubleIterator iterator() {
				List<Double> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Double> iterator = subList.iterator();
				return new MappedDoubleIterator<Double, Iterator<Double>>(iterator) {
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
			private List<Double> list = List.of(1.0, 2.0, 3.0);
			int end = list.size();

			@Override
			public DoubleIterator iterator() {
				List<Double> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Double> iterator = subList.iterator();
				return new MappedDoubleIterator<Double, Iterator<Double>>(iterator) {
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
		Queue<Double> queue = new ArrayDeque<>(List.of(1.0, 2.0, 3.0, 4.0, 5.0));
		DoubleSequence sequence = DoubleSequence.generate(queue::poll).endingAt(5.0, 0.1);

		assertThat(sequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0));
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
		Sequence<DoubleSequence> emptyPartitioned = empty.batch((a, b) -> a > b);
		twice(() -> assertThat(emptyPartitioned, is(emptyIterable())));

		Sequence<DoubleSequence> onePartitioned = _1.batch((a, b) -> a > b);
		twice(() -> assertThat(onePartitioned, contains(containsDoubles(1.0))));

		Sequence<DoubleSequence> twoPartitioned = _12.batch((a, b) -> a > b);
		twice(() -> assertThat(twoPartitioned, contains(containsDoubles(1.0, 2.0))));

		Sequence<DoubleSequence> threePartitioned = _123.batch((a, b) -> a > b);
		twice(() -> assertThat(threePartitioned, contains(containsDoubles(1.0, 2.0, 3.0))));

		Sequence<DoubleSequence> threeRandomPartitioned = threeRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(threeRandomPartitioned, contains(containsDoubles(17.0, 32.0), containsDoubles(12.0))));

		Sequence<DoubleSequence> nineRandomPartitioned = nineRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(nineRandomPartitioned, contains(containsDoubles(6.0, 6.0), containsDoubles(1.0),
		                                                       containsDoubles(-7.0, 1.0, 2.0, 17.0),
		                                                       containsDoubles(5.0), containsDoubles(4.0))));
	}

	@Test
	public void removeAllAfterFilter() {
		List<Double> original = new ArrayList<>(List.of(1.0, 2.0, 3.0, 4.0));

		DoubleSequence filtered = DoubleSequence.from(original).filter(x -> x % 2 != 0);
		filtered.removeAll();

		twice(() -> assertThat(filtered, is(emptyIterable())));
		assertThat(original, contains(2.0, 4.0));
	}

	@FunctionalInterface
	private interface StrictDoubleIterable extends DoubleIterable {
		static DoubleIterable from(DoubleIterable iterable) {
			return () -> StrictDoubleIterator.from(iterable.iterator());
		}

		static DoubleIterable of(double... values) {
			return () -> StrictDoubleIterator.of(values);
		}
	}

	private interface StrictDoubleIterator extends DoubleIterator {
		static DoubleIterator from(DoubleIterator iterator) {
			return new DoubleIterator() {
				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public double nextDouble() {
					return iterator.nextDouble();
				}

				@Override
				public Double next() {
					throw new UnsupportedOperationException();
				}
			};
		}

		static DoubleIterator of(double... values) {
			return from(DoubleIterator.of(values));
		}
	}
}
