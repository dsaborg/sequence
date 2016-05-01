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
import org.d2ab.iterable.ints.IntIterable;
import org.d2ab.iterator.Iterators;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.iterator.ints.MappedIntIterator;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
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

	private final IntSequence _1 = IntSequence.from(StrictIntIterable.of(1));
	private final IntSequence _12 = IntSequence.from(StrictIntIterable.of(1, 2));
	private final IntSequence _123 = IntSequence.from(StrictIntIterable.of(1, 2, 3));
	private final IntSequence _1234 = IntSequence.from(StrictIntIterable.of(1, 2, 3, 4));
	private final IntSequence _12345 = IntSequence.from(StrictIntIterable.of(1, 2, 3, 4, 5));
	private final IntSequence _123456789 = IntSequence.from(StrictIntIterable.of(1, 2, 3, 4, 5, 6, 7, 8, 9));

	private final IntSequence oneRandom = IntSequence.from(StrictIntIterable.of(17));
	private final IntSequence twoRandom = IntSequence.from(StrictIntIterable.of(17, 32));
	private final IntSequence threeRandom = IntSequence.from(StrictIntIterable.of(17, 32, 12));
	private final IntSequence nineRandom = IntSequence.from(StrictIntIterable.of(6, 6, 1, -7, 1, 2, 17, 5, 4));

	@Test
	public void ofOne() {
		twice(() -> assertThat(_1, containsInts(1)));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(_123, containsInts(1, 2, 3)));
	}

	@Test
	public void forLoop() {
		twice(() -> {
			for (int ignored : empty)
				fail("Should not get called");
		});

		IntSequence sequence = IntSequence.of(1, 2, 3, 4, 5);
		twice(() -> {
			int expected = 1;
			for (int i : sequence)
				assertThat(i, is(expected++));

			assertThat(expected, is(6));
		});
	}

	@Test
	public void forEach() {
		twice(() -> {
			empty.forEachInt(c -> fail("Should not get called"));

			AtomicInteger value = new AtomicInteger(1);
			_1.forEachInt(c -> assertThat(c, is(value.getAndIncrement())));

			value.set(1);
			_12.forEachInt(c -> assertThat(c, is(value.getAndIncrement())));

			value.set(1);
			_123.forEachInt(c -> assertThat(c, is(value.getAndIncrement())));
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
		IntSequence sequence = IntSequence.from(StrictIntIterable.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(sequence, containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void fromIterable() {
		IntSequence sequence = IntSequence.from(Iterables.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(sequence, containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void oncePrimitiveIteratorOfInt() {
		IntSequence sequence = IntSequence.once(StrictIntIterator.of(1, 2, 3, 4, 5));

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
	public void skip() {
		IntSequence skipNone = _123.skip(0);
		twice(() -> assertThat(skipNone, containsInts(1, 2, 3)));

		IntSequence skipOne = _123.skip(1);
		twice(() -> assertThat(skipOne, containsInts(2, 3)));

		IntSequence skipTwo = _123.skip(2);
		twice(() -> assertThat(skipTwo, containsInts(3)));

		IntSequence skipThree = _123.skip(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		IntSequence skipFour = _123.skip(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void skipTail() {
		IntSequence skipNone = _123.skipTail(0);
		twice(() -> assertThat(skipNone, containsInts(1, 2, 3)));

		IntSequence skipOne = _123.skipTail(1);
		twice(() -> assertThat(skipOne, containsInts(1, 2)));

		IntSequence skipTwo = _123.skipTail(2);
		twice(() -> assertThat(skipTwo, containsInts(1)));

		IntSequence skipThree = _123.skipTail(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		IntSequence skipFour = _123.skipTail(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		IntSequence limitNone = _123.limit(0);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		IntSequence limitOne = _123.limit(1);
		twice(() -> assertThat(limitOne, containsInts(1)));

		IntSequence limitTwo = _123.limit(2);
		twice(() -> assertThat(limitTwo, containsInts(1, 2)));

		IntSequence limitThree = _123.limit(3);
		twice(() -> assertThat(limitThree, containsInts(1, 2, 3)));

		IntSequence limitFour = _123.limit(4);
		twice(() -> assertThat(limitFour, containsInts(1, 2, 3)));
	}

	@Test
	public void appendArray() {
		IntSequence appended = _123.append(4, 5, 6).append(7, 8);

		twice(() -> assertThat(appended, containsInts(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIntIterable() {
		IntSequence appended = _123.append(StrictIntIterable.of(4, 5, 6)).append(StrictIntIterable.of(7, 8));

		twice(() -> assertThat(appended, containsInts(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIterable() {
		IntSequence appended = _123.append(Iterables.of(4, 5, 6)).append(Iterables.of(7, 8));

		twice(() -> assertThat(appended, containsInts(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIntIterator() {
		IntSequence appended = _123.append(IntIterator.of(4, 5, 6)).append(IntIterator.of(7, 8));

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
		assertThat(iterator.next(), is(1));
		assertThat(iterator.next(), is(2));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		IntSequence filtered = _123456789.filter(i -> (i % 2) == 0);

		twice(() -> assertThat(filtered, containsInts(2, 4, 6, 8)));
	}

	@Test
	public void filterBack() {
		IntSequence filteredLess = nineRandom.filterBack(117, (p, i) -> p < i);
		twice(() -> assertThat(filteredLess, containsInts(1, 2, 17)));

		IntSequence filteredGreater = nineRandom.filterBack(117, (p, i) -> p > i);
		twice(() -> assertThat(filteredGreater, containsInts(6, 1, -7, 5, 4)));
	}

	@Test
	public void filterForward() {
		IntSequence filteredLess = nineRandom.filterForward(117, (i, f) -> f < i);
		twice(() -> assertThat(filteredLess, containsInts(6, 1, 17, 5)));

		IntSequence filteredGreater = nineRandom.filterForward(117, (i, f) -> f > i);
		twice(() -> assertThat(filteredGreater, containsInts(-7, 1, 2, 4)));
	}

	@Test
	public void includingArray() {
		IntSequence emptyIncluding = empty.including(1, 3, 5, 17);
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));

		IntSequence including = _12345.including(1, 3, 5, 17);
		twice(() -> assertThat(including, containsInts(1, 3, 5)));

		IntSequence includingAll = _12345.including(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(includingAll, containsInts(1, 2, 3, 4, 5)));

		IntSequence includingNone = _12345.including();
		twice(() -> assertThat(includingNone, is(emptyIterable())));
	}

	@Test
	public void excludingArray() {
		IntSequence emptyExcluding = empty.excluding(1, 3, 5, 17);
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));

		IntSequence excluding = _12345.excluding(1, 3, 5, 17);
		twice(() -> assertThat(excluding, containsInts(2, 4)));

		IntSequence excludingAll = _12345.excluding(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		IntSequence excludingNone = _12345.excluding();
		twice(() -> assertThat(excludingNone, containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void map() {
		IntSequence mapped = _123.map(c -> c + 1);
		twice(() -> assertThat(mapped, containsInts(2, 3, 4)));
	}

	@Test
	public void recurse() {
		IntSequence recursive = IntSequence.recurse(1, i -> i + 1);
		twice(() -> assertThat(recursive.limit(10), containsInts(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void untilTerminal() {
		IntSequence until = IntSequence.recurse(1, x -> x + 1).until(7);
		twice(() -> assertThat(until, containsInts(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void endingAtTerminal() {
		IntSequence endingAt = IntSequence.recurse(1, x -> x + 1).endingAt(7);
		twice(() -> assertThat(endingAt, containsInts(1, 2, 3, 4, 5, 6, 7)));
	}

	@Test
	public void untilPredicate() {
		IntSequence until = IntSequence.recurse(1, x -> x + 1).until(i -> i == 7);
		twice(() -> assertThat(until, containsInts(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void endingAtPredicate() {
		IntSequence endingAt = IntSequence.recurse(1, x -> x + 1).endingAt(i -> i == 7);
		twice(() -> assertThat(endingAt, containsInts(1, 2, 3, 4, 5, 6, 7)));
	}

	@Test
	public void startingAfter() {
		IntSequence startingEmpty = empty.startingAfter(5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		IntSequence sequence = _123456789.startingAfter(5);
		twice(() -> assertThat(sequence, containsInts(6, 7, 8, 9)));

		IntSequence noStart = _12345.startingAfter(10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingAfterPredicate() {
		IntSequence startingEmpty = empty.startingAfter(i -> i == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		IntSequence sequence = _123456789.startingAfter(i -> i == 5);
		twice(() -> assertThat(sequence, containsInts(6, 7, 8, 9)));

		IntSequence noStart = _12345.startingAfter(i -> i == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFrom() {
		IntSequence startingEmpty = empty.startingFrom(5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		IntSequence sequence = _123456789.startingFrom(5);
		twice(() -> assertThat(sequence, containsInts(5, 6, 7, 8, 9)));

		IntSequence noStart = _12345.startingFrom(10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFromPredicate() {
		IntSequence startingEmpty = empty.startingFrom(i -> i == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		IntSequence sequence = _123456789.startingFrom(i -> i == 5);
		twice(() -> assertThat(sequence, containsInts(5, 6, 7, 8, 9)));

		IntSequence noStart = _12345.startingFrom(i -> i == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void collect() {
		twice(() -> {
			StringBuilder builder = _123.collect(StringBuilder::new, StringBuilder::append);
			assertThat(builder.toString(), is("123"));
		});
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(Arrays.equals(_123.toArray(), new int[]{1, 2, 3}), is(true)));
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
	public void second() {
		twice(() -> {
			assertThat(empty.second(), is(OptionalInt.empty()));
			assertThat(_1.second(), is(OptionalInt.empty()));
			assertThat(_12.second(), is(OptionalInt.of(2)));
			assertThat(_123.second(), is(OptionalInt.of(2)));
			assertThat(_1234.second(), is(OptionalInt.of(2)));
		});
	}

	@Test
	public void third() {
		twice(() -> {
			assertThat(empty.third(), is(OptionalInt.empty()));
			assertThat(_1.third(), is(OptionalInt.empty()));
			assertThat(_12.third(), is(OptionalInt.empty()));
			assertThat(_123.third(), is(OptionalInt.of(3)));
			assertThat(_1234.third(), is(OptionalInt.of(3)));
			assertThat(_12345.third(), is(OptionalInt.of(3)));
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
	public void step() {
		IntSequence stepThree = _123456789.step(3);
		twice(() -> assertThat(stepThree, containsInts(1, 4, 7)));
	}

	@Test
	public void distinct() {
		IntSequence emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		IntSequence oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, containsInts(17)));

		IntSequence twoDuplicatesDistinct = IntSequence.from(StrictIntIterable.of(17, 17)).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, containsInts(17)));

		IntSequence nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, containsInts(6, 1, -7, 2, 17, 5, 4)));
	}

	@Test
	public void sorted() {
		IntSequence emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		IntSequence oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, containsInts(17)));

		IntSequence twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, containsInts(17, 32)));

		IntSequence nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, containsInts(-7, 1, 1, 2, 4, 5, 6, 6, 17)));
	}

	@Test
	public void sortedWithUpdates() {
		List<Integer> backing = new ArrayList<>(asList(2, 3, 1));
		IntSequence sorted = IntSequence.from(backing).sorted();

		backing.add(4);
		assertThat(sorted, containsInts(1, 2, 3, 4));
	}

	@Test
	public void min() {
		OptionalInt emptyMin = empty.min();
		twice(() -> assertThat(emptyMin, is(OptionalInt.empty())));

		OptionalInt oneMin = oneRandom.min();
		twice(() -> assertThat(oneMin, is(OptionalInt.of(17))));

		OptionalInt twoMin = twoRandom.min();
		twice(() -> assertThat(twoMin, is(OptionalInt.of(17))));

		OptionalInt nineMin = nineRandom.min();
		twice(() -> assertThat(nineMin, is(OptionalInt.of(-7))));
	}

	@Test
	public void max() {
		OptionalInt emptyMax = empty.max();
		twice(() -> assertThat(emptyMax, is(OptionalInt.empty())));

		OptionalInt oneMax = oneRandom.max();
		twice(() -> assertThat(oneMax, is(OptionalInt.of(17))));

		OptionalInt twoMax = twoRandom.max();
		twice(() -> assertThat(twoMax, is(OptionalInt.of(32))));

		OptionalInt nineMax = nineRandom.max();
		twice(() -> assertThat(nineMax, is(OptionalInt.of(17))));
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
		IntSequence peek = _123.peek(x -> assertThat(x, is(both(greaterThan(0)).and(lessThan(4)))));
		twice(() -> assertThat(peek, containsInts(1, 2, 3)));
	}

	@Test
	public void prefix() {
		IntSequence prefixEmpty = empty.prefix(327);
		twice(() -> assertThat(prefixEmpty, containsInts(327)));

		IntSequence prefix = _123.prefix(327);
		twice(() -> assertThat(prefix, containsInts(327, 1, 2, 3)));
	}

	@Test
	public void suffix() {
		IntSequence suffixEmpty = empty.suffix(532);
		twice(() -> assertThat(suffixEmpty, containsInts(532)));

		IntSequence suffix = _123.suffix(532);
		twice(() -> assertThat(suffix, containsInts(1, 2, 3, 532)));
	}

	@Test
	public void interleave() {
		assertThat(empty.interleave(empty), is(emptyIterable()));
		assertThat(_123.interleave(_12345), containsInts(1, 1, 2, 2, 3, 3, 4, 5));
		assertThat(_12345.interleave(_123), containsInts(1, 1, 2, 2, 3, 3, 4, 5));
	}

	@Test
	public void reverse() {
		IntSequence emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		IntSequence oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, containsInts(1)));

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
	}

	@Test
	public void toChars() {
		CharSeq charSeq = IntSequence.increasingFrom('a').limit(5).toChars();
		twice(() -> assertThat(charSeq, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void toLongs() {
		LongSequence longSequence = _12345.toLongs();
		twice(() -> assertThat(longSequence, containsLongs(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void toDoubles() {
		DoubleSequence doubleSequence = _12345.toDoubles();
		twice(() -> assertThat(doubleSequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void toCharsMapped() {
		CharSeq charSeq = _12345.toChars(i -> (char) ('a' + i - 1));
		twice(() -> assertThat(charSeq, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void toLongsMapped() {
		long maxInt = Integer.MAX_VALUE;

		LongSequence longSequence = _12345.toLongs(i -> i * maxInt);
		twice(() -> assertThat(longSequence,
		                       containsLongs(maxInt, 2L * maxInt, 3L * maxInt, 4L * maxInt, 5L * maxInt)));
	}

	@Test
	public void toDoublesMapped() {
		DoubleSequence doubleSequence = _12345.toDoubles(i -> i / 2.0);
		twice(() -> assertThat(doubleSequence, containsDoubles(0.5, 1.0, 1.5, 2.0, 2.5)));
	}

	@Test
	public void toSequence() {
		Sequence<Integer> emptySequence = empty.toSequence(i -> i + 1);
		twice(() -> assertThat(emptySequence, is(emptyIterable())));

		Sequence<Integer> ints = _12345.toSequence(i -> i + 1);
		twice(() -> assertThat(ints, contains(2, 3, 4, 5, 6)));
	}

	@Test
	public void box() {
		Sequence<Integer> empty = IntSequence.empty().box();
		twice(() -> assertThat(empty, is(emptyIterable())));

		Sequence<Integer> ints = _12345.box();
		twice(() -> assertThat(ints, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void repeat() {
		IntSequence repeatEmpty = empty.repeat();
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		IntSequence repeatOne = _1.repeat();
		twice(() -> assertThat(repeatOne.limit(3), containsInts(1, 1, 1)));

		IntSequence repeatTwo = _12.repeat();
		twice(() -> assertThat(repeatTwo.limit(5), containsInts(1, 2, 1, 2, 1)));

		IntSequence repeatThree = _123.repeat();
		twice(() -> assertThat(repeatThree.limit(8), containsInts(1, 2, 3, 1, 2, 3, 1, 2)));

		IntSequence repeatVarying = IntSequence.from(new IntIterable() {
			private List<Integer> list = asList(1, 2, 3);
			int end = list.size();

			@Override
			public IntIterator iterator() {
				List<Integer> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Integer> iterator = subList.iterator();
				return new MappedIntIterator<Integer, Iterator<Integer>>(iterator) {
					@Override
					public int nextInt() {
						return iterator.next();
					}
				};
			}
		}).repeat();
		assertThat(repeatVarying, containsInts(1, 2, 3, 1, 2, 1));
	}

	@Test
	public void repeatTwice() {
		IntSequence repeatEmpty = empty.repeat(2);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		IntSequence repeatOne = _1.repeat(2);
		twice(() -> assertThat(repeatOne, containsInts(1, 1)));

		IntSequence repeatTwo = _12.repeat(2);
		twice(() -> assertThat(repeatTwo, containsInts(1, 2, 1, 2)));

		IntSequence repeatThree = _123.repeat(2);
		twice(() -> assertThat(repeatThree, containsInts(1, 2, 3, 1, 2, 3)));

		IntSequence repeatVarying = IntSequence.from(new IntIterable() {
			private List<Integer> list = asList(1, 2, 3);
			int end = list.size();

			@Override
			public IntIterator iterator() {
				List<Integer> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Integer> iterator = subList.iterator();
				return new MappedIntIterator<Integer, Iterator<Integer>>(iterator) {
					@Override
					public int nextInt() {
						return iterator.next();
					}
				};
			}
		}).repeat(2);
		assertThat(repeatVarying, containsInts(1, 2, 3, 1, 2));
	}

	@Test
	public void repeatZero() {
		IntSequence repeatEmpty = empty.repeat(0);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		IntSequence repeatOne = _1.repeat(0);
		twice(() -> assertThat(repeatOne, is(emptyIterable())));

		IntSequence repeatTwo = _12.repeat(0);
		twice(() -> assertThat(repeatTwo, is(emptyIterable())));

		IntSequence repeatThree = _123.repeat(0);
		twice(() -> assertThat(repeatThree, is(emptyIterable())));
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
		expecting(NullPointerException.class, iterator::next);

		IntIterator iterator2 = sequence.iterator();
		expecting(NullPointerException.class, iterator2::next);
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
			expecting(NullPointerException.class, iterator::next);
		});
	}

	@Test
	public void random() {
		IntSequence random = IntSequence.random();

		twice(() -> times(1000, random.iterator()::nextInt));

		assertThat(random.limit(10), not(contains(random.limit(10))));
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

		assertThat(random.limit(10), not(contains(random.limit(10))));
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

		assertThat(random.limit(10), not(contains(random.limit(10))));
	}

	@Test
	public void randomLowerUpperWithSupplier() {
		IntSequence random = IntSequence.random(() -> new Random(17), 1000, 2000);

		twice(() -> assertThat(random.limit(10),
		                       containsInts(1976, 1220, 1694, 1516, 1892, 1693, 1404, 1915, 1062, 1808)));
	}

	@Test
	public void mapBack() {
		twice(() -> assertThat(_123.mapBack(17, (p, i) -> p), containsInts(17, 1, 2)));
		twice(() -> assertThat(_123.mapBack(17, (p, i) -> i), containsInts(1, 2, 3)));
	}

	@Test
	public void mapForward() {
		twice(() -> assertThat(_123.mapForward(17, (i, n) -> i), containsInts(1, 2, 3)));
		twice(() -> assertThat(_123.mapForward(17, (i, n) -> n), containsInts(2, 3, 17)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void window() {
		twice(() -> assertThat(empty.window(3), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3), contains(containsInts(1))));
		twice(() -> assertThat(_12.window(3), contains(containsInts(1, 2))));
		twice(() -> assertThat(_123.window(3), contains(containsInts(1, 2, 3))));
		twice(() -> assertThat(_1234.window(3), contains(containsInts(1, 2, 3), containsInts(2, 3, 4))));
		twice(() -> assertThat(_12345.window(3),
		                       contains(containsInts(1, 2, 3), containsInts(2, 3, 4), containsInts(3, 4, 5))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithStep() {
		twice(() -> assertThat(empty.window(3, 2), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3, 2), contains(containsInts(1))));
		twice(() -> assertThat(_12.window(3, 2), contains(containsInts(1, 2))));
		twice(() -> assertThat(_123.window(3, 2), contains(containsInts(1, 2, 3))));
		twice(() -> assertThat(_1234.window(3, 2), contains(containsInts(1, 2, 3), containsInts(3, 4))));
		twice(() -> assertThat(_12345.window(3, 2), contains(containsInts(1, 2, 3), containsInts(3, 4, 5))));
		twice(() -> assertThat(_123456789.window(3, 2),
		                       contains(containsInts(1, 2, 3), containsInts(3, 4, 5), containsInts(5, 6, 7),
		                                containsInts(7, 8, 9))));

		twice(() -> assertThat(empty.window(3, 4), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3, 4), contains(containsInts(1))));
		twice(() -> assertThat(_12.window(3, 4), contains(containsInts(1, 2))));
		twice(() -> assertThat(_123.window(3, 4), contains(containsInts(1, 2, 3))));
		twice(() -> assertThat(_1234.window(3, 4), contains(containsInts(1, 2, 3))));
		twice(() -> assertThat(_12345.window(3, 4), contains(containsInts(1, 2, 3), containsInts(5))));
		twice(() -> assertThat(_123456789.window(3, 4),
		                       contains(containsInts(1, 2, 3), containsInts(5, 6, 7), containsInts(9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batch() {
		twice(() -> assertThat(empty.batch(3), is(emptyIterable())));
		twice(() -> assertThat(_1.batch(3), contains(containsInts(1))));
		twice(() -> assertThat(_12.batch(3), contains(containsInts(1, 2))));
		twice(() -> assertThat(_123.batch(3), contains(containsInts(1, 2, 3))));
		twice(() -> assertThat(_1234.batch(3), contains(containsInts(1, 2, 3), containsInts(4))));
		twice(() -> assertThat(_12345.batch(3), contains(containsInts(1, 2, 3), containsInts(4, 5))));
		twice(() -> assertThat(_123456789.batch(3),
		                       contains(containsInts(1, 2, 3), containsInts(4, 5, 6), containsInts(7, 8, 9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnPredicate() {
		Sequence<IntSequence> emptyPartitioned = empty.batch((a, b) -> a > b);
		twice(() -> assertThat(emptyPartitioned, is(emptyIterable())));

		Sequence<IntSequence> onePartitioned = _1.batch((a, b) -> a > b);
		twice(() -> assertThat(onePartitioned, contains(containsInts(1))));

		Sequence<IntSequence> twoPartitioned = _12.batch((a, b) -> a > b);
		twice(() -> assertThat(twoPartitioned, contains(containsInts(1, 2))));

		Sequence<IntSequence> threePartitioned = _123.batch((a, b) -> a > b);
		twice(() -> assertThat(threePartitioned, contains(containsInts(1, 2, 3))));

		Sequence<IntSequence> threeRandomPartitioned = threeRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(threeRandomPartitioned, contains(containsInts(17, 32), containsInts(12))));

		Sequence<IntSequence> nineRandomPartitioned = nineRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(nineRandomPartitioned,
		                       contains(containsInts(6, 6), containsInts(1), containsInts(-7, 1, 2, 17),
		                                containsInts(5), containsInts(4))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void split() {
		Sequence<IntSequence> emptySplit = empty.split(3);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

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
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitIntPredicate() {
		Sequence<IntSequence> emptySplit = empty.split(x -> x % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

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
	}

	@Test
	public void removeAllAfterFilter() {
		List<Integer> original = new ArrayList<>(asList(1, 2, 3, 4));

		IntSequence filtered = IntSequence.from(original).filter(x -> x % 2 != 0);
		filtered.removeAll();

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
		assertThat(empty.contains(17), is(false));

		assertThat(_12345.contains(1), is(true));
		assertThat(_12345.contains(3), is(true));
		assertThat(_12345.contains(5), is(true));
		assertThat(_12345.contains(17), is(false));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(), is(true));
		assertThat(empty.containsAll(17, 18, 19), is(false));

		assertThat(_12345.containsAll(), is(true));
		assertThat(_12345.containsAll(1), is(true));
		assertThat(_12345.containsAll(1, 3, 5), is(true));
		assertThat(_12345.containsAll(1, 2, 3, 4, 5), is(true));
		assertThat(_12345.containsAll(1, 2, 3, 4, 5, 17), is(false));
		assertThat(_12345.containsAll(17, 18, 19), is(false));
	}

	@Test
	public void containsAny() {
		assertThat(empty.containsAny(), is(false));
		assertThat(empty.containsAny(17, 18, 19), is(false));

		assertThat(_12345.containsAny(), is(false));
		assertThat(_12345.containsAny(1), is(true));
		assertThat(_12345.containsAny(1, 3, 5), is(true));
		assertThat(_12345.containsAny(1, 2, 3, 4, 5), is(true));
		assertThat(_12345.containsAny(1, 2, 3, 4, 5, 17), is(true));
		assertThat(_12345.containsAny(17, 18, 19), is(false));
	}

	@FunctionalInterface
	private interface StrictIntIterable extends IntIterable {
		static IntIterable from(IntIterable iterable) {
			return () -> StrictIntIterator.from(iterable.iterator());
		}

		static IntIterable of(int... values) {
			return () -> StrictIntIterator.of(values);
		}
	}

	private interface StrictIntIterator extends IntIterator {
		static IntIterator from(IntIterator iterator) {
			return new IntIterator() {
				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public int nextInt() {
					return iterator.nextInt();
				}

				@Override
				public Integer next() {
					throw new UnsupportedOperationException();
				}
			};
		}

		static IntIterator of(int... values) {
			return from(IntIterator.of(values));
		}
	}
}
