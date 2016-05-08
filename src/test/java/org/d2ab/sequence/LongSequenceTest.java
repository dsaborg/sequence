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
import org.d2ab.iterable.longs.LongIterable;
import org.d2ab.iterator.Iterators;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.iterator.longs.DelegatingLongIterator;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;
import java.util.stream.LongStream;
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

public class LongSequenceTest {
	private final LongSequence empty = LongSequence.empty();

	private final LongSequence _1 = LongSequence.from(StrictLongIterable.of(1L));
	private final LongSequence _12 = LongSequence.from(StrictLongIterable.of(1L, 2L));
	private final LongSequence _123 = LongSequence.from(StrictLongIterable.of(1L, 2L, 3L));
	private final LongSequence _1234 = LongSequence.from(StrictLongIterable.of(1L, 2L, 3L, 4L));
	private final LongSequence _12345 = LongSequence.from(StrictLongIterable.of(1L, 2L, 3L, 4L, 5L));
	private final LongSequence _123456789 =
			LongSequence.from(StrictLongIterable.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L));

	private final LongSequence oneRandom = LongSequence.from(StrictLongIterable.of(17L));
	private final LongSequence twoRandom = LongSequence.from(StrictLongIterable.of(17L, 32L));
	private final LongSequence threeRandom = LongSequence.from(StrictLongIterable.of(17L, 32L, 12L));
	private final LongSequence nineRandom =
			LongSequence.from(StrictLongIterable.of(6L, 6L, 1L, -7L, 1L, 2L, 17L, 5L, 4L));

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
		twice(() -> assertThat(_1, containsLongs(1L)));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(_123, containsLongs(1L, 2L, 3L)));
	}

	@Test
	public void forLoop() {
		twice(() -> {
			for (long ignored : empty)
				fail("Should not get called");
		});

		LongSequence sequence = LongSequence.of(1L, 2L, 3L, 4L, 5L);
		twice(() -> {
			long expected = 1;
			for (long i : sequence)
				assertThat(i, is(expected++));

			assertThat(expected, is(6L));
		});
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
			AtomicLong index = new AtomicLong();
			_1.forEachLongIndexed((e, i) -> {
				assertThat(e, is(value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(1L));

			value.set(1);
			index.set(0);
			_12.forEachLongIndexed((e, i) -> {
				assertThat(e, is(value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(2L));

			value.set(1);
			index.set(0);
			_12345.forEachLongIndexed((e, i) -> {
				assertThat(e, is(value.getAndIncrement()));
				assertThat(i, is(index.getAndIncrement()));
			});
			assertThat(index.get(), is(5L));
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
			assertThat(iterator.hasNext(), is(false));
		});
	}

	@Test
	public void fromLongIterable() {
		LongSequence sequence = LongSequence.from(LongIterable.of(1, 2, 3, 4, 5));

		twice(() -> assertThat(sequence, containsLongs(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void fromIterable() {
		LongSequence sequence = LongSequence.from(Iterables.of(1L, 2L, 3L, 4L, 5L));

		twice(() -> assertThat(sequence, containsLongs(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void oncePrimitiveIteratorOfLong() {
		LongSequence sequence = LongSequence.once(LongIterator.of(1, 2, 3, 4, 5));

		assertThat(sequence, containsLongs(1L, 2L, 3L, 4L, 5L));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceIterator() {
		LongSequence sequence = LongSequence.once(Iterators.of(1L, 2L, 3L, 4L, 5L));

		assertThat(sequence, containsLongs(1L, 2L, 3L, 4L, 5L));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceLongStream() {
		LongSequence sequence = LongSequence.once(LongStream.of(1L, 2L, 3L, 4L, 5L));

		assertThat(sequence, containsLongs(1L, 2L, 3L, 4L, 5L));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceStream() {
		LongSequence sequence = LongSequence.once(Stream.of(1L, 2L, 3L, 4L, 5L));

		assertThat(sequence, containsLongs(1L, 2L, 3L, 4L, 5L));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceEmptyStream() {
		LongSequence sequence = LongSequence.once(Stream.of());

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void cachePrimitiveIteratorOfLong() {
		LongSequence cached = LongSequence.cache(LongIterator.of(1L, 2L, 3L, 4L, 5L));

		twice(() -> assertThat(cached, containsLongs(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void cacheIterator() {
		LongSequence cached = LongSequence.cache(Iterators.of(1L, 2L, 3L, 4L, 5L));

		twice(() -> assertThat(cached, containsLongs(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void cacheLongIterable() {
		LongSequence cached = LongSequence.cache(LongIterable.of(1L, 2L, 3L, 4L, 5L));

		twice(() -> assertThat(cached, containsLongs(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void cacheIterable() {
		LongSequence cached = LongSequence.cache(Iterables.of(1L, 2L, 3L, 4L, 5L));

		twice(() -> assertThat(cached, containsLongs(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void cacheLongStream() {
		LongSequence cached = LongSequence.cache(LongStream.of(1L, 2L, 3L, 4L, 5L));

		twice(() -> assertThat(cached, containsLongs(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void cacheStream() {
		LongSequence cached = LongSequence.cache(Stream.of(1L, 2L, 3L, 4L, 5L));

		twice(() -> assertThat(cached, containsLongs(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void skip() {
		LongSequence skipNone = _123.skip(0L);
		twice(() -> assertThat(skipNone, containsLongs(1L, 2L, 3L)));

		LongSequence skipOne = _123.skip(1L);
		twice(() -> assertThat(skipOne, containsLongs(2L, 3L)));

		LongSequence skipTwo = _123.skip(2L);
		twice(() -> assertThat(skipTwo, containsLongs(3L)));

		LongSequence skipThree = _123.skip(3L);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		LongSequence skipFour = _123.skip(4L);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void skipTail() {
		LongSequence skipNone = _123.skipTail(0);
		twice(() -> assertThat(skipNone, containsLongs(1, 2, 3)));

		LongSequence skipOne = _123.skipTail(1);
		twice(() -> assertThat(skipOne, containsLongs(1, 2)));

		LongSequence skipTwo = _123.skipTail(2);
		twice(() -> assertThat(skipTwo, containsLongs(1)));

		LongSequence skipThree = _123.skipTail(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		LongSequence skipFour = _123.skipTail(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		LongSequence limitNone = _123.limit(0L);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		LongSequence limitOne = _123.limit(1L);
		twice(() -> assertThat(limitOne, containsLongs(1L)));

		LongSequence limitTwo = _123.limit(2L);
		twice(() -> assertThat(limitTwo, containsLongs(1L, 2L)));

		LongSequence limitThree = _123.limit(3L);
		twice(() -> assertThat(limitThree, containsLongs(1L, 2L, 3L)));

		LongSequence limitFour = _123.limit(4L);
		twice(() -> assertThat(limitFour, containsLongs(1L, 2L, 3L)));
	}

	@Test
	public void appendArray() {
		LongSequence appended = _123.append(4, 5, 6).append(7, 8);

		twice(() -> assertThat(appended, containsLongs(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)));
	}

	@Test
	public void appendLongIterable() {
		LongSequence appended = _123.append(LongIterable.of(4, 5, 6)).append(LongIterable.of(7, 8));

		twice(() -> assertThat(appended, containsLongs(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)));
	}

	@Test
	public void appendIterable() {
		LongSequence appended = _123.append(Iterables.of(4L, 5L, 6L)).append(Iterables.of(7L, 8L));

		twice(() -> assertThat(appended, containsLongs(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)));
	}

	@Test
	public void appendLongIterator() {
		LongSequence appended = _123.append(LongIterator.of(4, 5, 6)).append(LongIterator.of(7, 8));

		assertThat(appended, containsLongs(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
		assertThat(appended, containsLongs(1L, 2L, 3L));
	}

	@Test
	public void appendIterator() {
		LongSequence appended = _123.append(Iterators.of(4L, 5L, 6L)).append(Iterators.of(7L, 8L));

		assertThat(appended, containsLongs(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
		assertThat(appended, containsLongs(1L, 2L, 3L));
	}

	@Test
	public void appendStream() {
		LongSequence appended = _123.append(Stream.of(4L, 5L, 6L)).append(Stream.of(7L, 8L));

		assertThat(appended, containsLongs(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
		assertThat(appended, containsLongs(1L, 2L, 3L));
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
		assertThat(iterator.next(), is(1L));
		assertThat(iterator.next(), is(2L));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		LongSequence filtered =
				LongSequence.from(StrictLongIterable.of(1L, 2L, 3L, 4L, 5L, 6L, 7L)).filter(i -> (i % 2L) == 0L);

		twice(() -> assertThat(filtered, containsLongs(2L, 4L, 6L)));
	}

	@Test
	public void filterBack() {
		LongSequence filteredLess = nineRandom.filterBack(117, (p, i) -> p < i);
		twice(() -> assertThat(filteredLess, containsLongs(1L, 2L, 17L)));

		LongSequence filteredGreater = nineRandom.filterBack(117, (p, i) -> p > i);
		twice(() -> assertThat(filteredGreater, containsLongs(6L, 1L, -7L, 5L, 4L)));
	}

	@Test
	public void filterForward() {
		LongSequence filteredLess = nineRandom.filterForward(117, (i, f) -> f < i);
		twice(() -> assertThat(filteredLess, containsLongs(6L, 1L, 17L, 5L)));

		LongSequence filteredGreater = nineRandom.filterForward(117, (i, f) -> f > i);
		twice(() -> assertThat(filteredGreater, containsLongs(-7L, 1L, 2L, 4L)));
	}

	@Test
	public void includingArray() {
		LongSequence emptyIncluding = empty.including(1, 3, 5, 17);
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));

		LongSequence including = _12345.including(1, 3, 5, 17);
		twice(() -> assertThat(including, containsLongs(1, 3, 5)));

		LongSequence includingAll = _12345.including(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(includingAll, containsLongs(1, 2, 3, 4, 5)));

		LongSequence includingNone = _12345.including();
		twice(() -> assertThat(includingNone, is(emptyIterable())));
	}

	@Test
	public void excludingArray() {
		LongSequence emptyExcluding = empty.excluding(1, 3, 5, 17);
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));

		LongSequence excluding = _12345.excluding(1, 3, 5, 17);
		twice(() -> assertThat(excluding, containsLongs(2, 4)));

		LongSequence excludingAll = _12345.excluding(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		LongSequence excludingNone = _12345.excluding();
		twice(() -> assertThat(excludingNone, containsLongs(1, 2, 3, 4, 5)));
	}

	@Test
	public void map() {
		LongSequence mapped = _123.map(c -> c + 1L);
		twice(() -> assertThat(mapped, containsLongs(2L, 3L, 4L)));
	}

	@Test
	public void recurse() {
		LongSequence recursive = LongSequence.recurse(1L, i -> i + 1L);
		twice(() -> assertThat(recursive.limit(10), containsLongs(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)));
	}

	@Test
	public void untilTerminal() {
		LongSequence until = LongSequence.recurse(1, x -> x + 1).until(7L);
		twice(() -> assertThat(until, containsLongs(1L, 2L, 3L, 4L, 5L, 6L)));
	}

	@Test
	public void endingAtTerminal() {
		LongSequence endingAt = LongSequence.recurse(1, x -> x + 1).endingAt(7L);
		twice(() -> assertThat(endingAt, containsLongs(1L, 2L, 3L, 4L, 5L, 6L, 7L)));
	}

	@Test
	public void untilPredicate() {
		LongSequence until = LongSequence.recurse(1, x -> x + 1).until(i -> i == 7L);
		twice(() -> assertThat(until, containsLongs(1L, 2L, 3L, 4L, 5L, 6L)));
	}

	@Test
	public void endingAtPredicate() {
		LongSequence endingAt = LongSequence.recurse(1, x -> x + 1).endingAt(i -> i == 7L);
		twice(() -> assertThat(endingAt, containsLongs(1L, 2L, 3L, 4L, 5L, 6L, 7L)));
	}

	@Test
	public void startingAfter() {
		LongSequence startingEmpty = empty.startingAfter(5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		LongSequence sequence = _123456789.startingAfter(5);
		twice(() -> assertThat(sequence, containsLongs(6, 7, 8, 9)));

		LongSequence noStart = _12345.startingAfter(10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingAfterPredicate() {
		LongSequence startingEmpty = empty.startingAfter(i -> i == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		LongSequence sequence = _123456789.startingAfter(i -> i == 5);
		twice(() -> assertThat(sequence, containsLongs(6, 7, 8, 9)));

		LongSequence noStart = _12345.startingAfter(i -> i == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFrom() {
		LongSequence startingEmpty = empty.startingFrom(5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		LongSequence sequence = _123456789.startingFrom(5);
		twice(() -> assertThat(sequence, containsLongs(5, 6, 7, 8, 9)));

		LongSequence noStart = _12345.startingFrom(10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFromPredicate() {
		LongSequence startingEmpty = empty.startingFrom(i -> i == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		LongSequence sequence = _123456789.startingFrom(i -> i == 5);
		twice(() -> assertThat(sequence, containsLongs(5, 6, 7, 8, 9)));

		LongSequence noStart = _12345.startingFrom(i -> i == 10);
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
		twice(() -> assertThat(Arrays.equals(_123.toArray(), new long[]{1L, 2L, 3L}), is(true)));
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
		LongBinaryOperator secondLong = (c1, c2) -> c2;
		twice(() -> {
			assertThat(empty.reduce(secondLong), is(OptionalLong.empty()));
			assertThat(_1.reduce(secondLong), is(OptionalLong.of(1L)));
			assertThat(_12.reduce(secondLong), is(OptionalLong.of(2L)));
			assertThat(_123.reduce(secondLong), is(OptionalLong.of(3L)));
		});
	}

	@Test
	public void reduceWithIdentity() {
		LongBinaryOperator secondLong = (c1, c2) -> c2;
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
			assertThat(_1.first(), is(OptionalLong.of(1L)));
			assertThat(_12.first(), is(OptionalLong.of(1L)));
			assertThat(_123.first(), is(OptionalLong.of(1L)));
		});
	}

	@Test
	public void second() {
		twice(() -> {
			assertThat(empty.second(), is(OptionalLong.empty()));
			assertThat(_1.second(), is(OptionalLong.empty()));
			assertThat(_12.second(), is(OptionalLong.of(2L)));
			assertThat(_123.second(), is(OptionalLong.of(2L)));
			assertThat(_1234.second(), is(OptionalLong.of(2L)));
		});
	}

	@Test
	public void third() {
		twice(() -> {
			assertThat(empty.third(), is(OptionalLong.empty()));
			assertThat(_1.third(), is(OptionalLong.empty()));
			assertThat(_12.third(), is(OptionalLong.empty()));
			assertThat(_123.third(), is(OptionalLong.of(3L)));
			assertThat(_1234.third(), is(OptionalLong.of(3L)));
			assertThat(_12345.third(), is(OptionalLong.of(3L)));
		});
	}

	@Test
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(OptionalLong.empty()));
			assertThat(_1.last(), is(OptionalLong.of(1L)));
			assertThat(_12.last(), is(OptionalLong.of(2L)));
			assertThat(_123.last(), is(OptionalLong.of(3L)));
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
	public void secondByPredicate() {
		twice(() -> {
			assertThat(empty.second(x -> x > 1), is(OptionalLong.empty()));
			assertThat(_1.second(x -> x > 1), is(OptionalLong.empty()));
			assertThat(_12.second(x -> x > 1), is(OptionalLong.empty()));
			assertThat(_123.second(x -> x > 1), is(OptionalLong.of(3)));
			assertThat(_1234.second(x -> x > 1), is(OptionalLong.of(3)));
		});
	}

	@Test
	public void thirdByPredicate() {
		twice(() -> {
			assertThat(empty.third(x -> x > 1), is(OptionalLong.empty()));
			assertThat(_1.third(x -> x > 1), is(OptionalLong.empty()));
			assertThat(_12.third(x -> x > 1), is(OptionalLong.empty()));
			assertThat(_123.third(x -> x > 1), is(OptionalLong.empty()));
			assertThat(_1234.third(x -> x > 1), is(OptionalLong.of(4)));
			assertThat(_12345.third(x -> x > 1), is(OptionalLong.of(4)));
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
		LongSequence stepThree = _123456789.step(3L);
		twice(() -> assertThat(stepThree, containsLongs(1L, 4L, 7L)));
	}

	@Test
	public void distinct() {
		LongSequence emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		LongSequence oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, containsLongs(17L)));

		LongSequence twoDuplicatesDistinct = LongSequence.from(StrictLongIterable.of(17L, 17L)).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, containsLongs(17L)));

		LongSequence nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, containsLongs(6L, 1L, -7L, 2L, 17L, 5L, 4L)));
	}

	@Test
	public void sorted() {
		LongSequence emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		LongSequence oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, containsLongs(17L)));

		LongSequence twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, containsLongs(17L, 32L)));

		LongSequence nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, containsLongs(-7L, 1L, 1L, 2L, 4L, 5L, 6L, 6L, 17L)));
	}

	@Test
	public void sortedWithUpdates() {
		List<Long> backing = new ArrayList<>(asList(2L, 3L, 1L));
		LongSequence sorted = LongSequence.from(backing).sorted();

		backing.add(4L);
		assertThat(sorted, containsLongs(1L, 2L, 3L, 4L));
	}

	@Test
	public void min() {
		twice(() -> assertThat(empty.min(), is(OptionalLong.empty())));
		twice(() -> assertThat(oneRandom.min(), is(OptionalLong.of(17L))));
		twice(() -> assertThat(twoRandom.min(), is(OptionalLong.of(17L))));
		twice(() -> assertThat(nineRandom.min(), is(OptionalLong.of(-7L))));
	}

	@Test
	public void max() {
		twice(() -> assertThat(empty.max(), is(OptionalLong.empty())));
		twice(() -> assertThat(oneRandom.max(), is(OptionalLong.of(17L))));
		twice(() -> assertThat(twoRandom.max(), is(OptionalLong.of(32L))));
		twice(() -> assertThat(nineRandom.max(), is(OptionalLong.of(17L))));
	}

	@Test
	public void size() {
		twice(() -> assertThat(empty.size(), is(0L)));
		twice(() -> assertThat(_1.size(), is(1L)));
		twice(() -> assertThat(_12.size(), is(2L)));
		twice(() -> assertThat(_123456789.size(), is(9L)));
	}

	@Test
	public void any() {
		twice(() -> assertThat(_123.any(x -> x > 0L), is(true)));
		twice(() -> assertThat(_123.any(x -> x > 2L), is(true)));
		twice(() -> assertThat(_123.any(x -> x > 4L), is(false)));
	}

	@Test
	public void all() {
		twice(() -> assertThat(_123.all(x -> x > 0L), is(true)));
		twice(() -> assertThat(_123.all(x -> x > 2L), is(false)));
		twice(() -> assertThat(_123.all(x -> x > 4L), is(false)));
	}

	@Test
	public void none() {
		twice(() -> assertThat(_123.none(x -> x > 0L), is(false)));
		twice(() -> assertThat(_123.none(x -> x > 2L), is(false)));
		twice(() -> assertThat(_123.none(x -> x > 4L), is(true)));
	}

	@Test
	public void peek() {
		LongSequence peek = _123.peek(x -> assertThat(x, is(both(greaterThan(0L)).and(lessThan(4L)))));
		twice(() -> assertThat(peek, containsLongs(1L, 2L, 3L)));
	}

	@Test
	public void prefix() {
		LongSequence prefixEmpty = empty.prefix(327L);
		twice(() -> assertThat(prefixEmpty, containsLongs(327L)));

		LongSequence prefix = _123.prefix(327L);
		twice(() -> assertThat(prefix, containsLongs(327L, 1L, 2L, 3L)));
	}

	@Test
	public void suffix() {
		LongSequence suffixEmpty = empty.suffix(532L);
		twice(() -> assertThat(suffixEmpty, containsLongs(532L)));

		LongSequence suffix = _123.suffix(532L);
		twice(() -> assertThat(suffix, containsLongs(1L, 2L, 3L, 532L)));
	}

	@Test
	public void interleave() {
		assertThat(empty.interleave(empty), is(emptyIterable()));
		assertThat(_123.interleave(_12345), containsLongs(1L, 1L, 2L, 2L, 3L, 3L, 4L, 5L));
		assertThat(_12345.interleave(_123), containsLongs(1L, 1L, 2L, 2L, 3L, 3L, 4L, 5L));
	}

	@Test
	public void reverse() {
		LongSequence emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		LongSequence oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, containsLongs(1L)));

		LongSequence twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, containsLongs(2L, 1L)));

		LongSequence threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, containsLongs(3L, 2L, 1L)));

		LongSequence nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed, containsLongs(9L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L)));
	}

	@Test
	public void reverseWithUpdates() {
		List<Long> backing = new ArrayList<>(asList(1L, 2L, 3L));
		LongSequence reversed = LongSequence.from(backing).reverse();

		backing.add(4L);
		assertThat(reversed, containsLongs(4L, 3L, 2L, 1L));
	}

	@Test
	public void positive() {
		LongSequence positive = LongSequence.positive();
		twice(() -> assertThat(positive.limit(5), containsLongs(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void positiveFromZero() {
		LongSequence positiveFromZero = LongSequence.positiveFromZero();
		twice(() -> assertThat(positiveFromZero.limit(5), containsLongs(0L, 1L, 2L, 3L, 4L)));
	}

	@Test
	public void negative() {
		LongSequence negative = LongSequence.negative();
		twice(() -> assertThat(negative.limit(5), containsLongs(-1L, -2L, -3L, -4L, -5L)));
	}

	@Test
	public void negativeFromZero() {
		LongSequence negativeFromZero = LongSequence.negativeFromZero();
		twice(() -> assertThat(negativeFromZero.limit(5), containsLongs(0L, -1L, -2L, -3L, -4L)));
	}

	@Test
	public void decreasingFrom() {
		LongSequence decreasing = LongSequence.decreasingFrom(-10);
		twice(() -> assertThat(decreasing.limit(5), containsLongs(-10L, -11L, -12L, -13L, -14L)));

		LongSequence decreasingFrom2 = LongSequence.decreasingFrom(2);
		twice(() -> assertThat(decreasingFrom2.limit(5), containsLongs(2L, 1L, 0L, -1L, -2L)));

		LongSequence decreasingFromMinValue = LongSequence.decreasingFrom(Long.MIN_VALUE);
		twice(() -> assertThat(decreasingFromMinValue.limit(3),
		                       containsLongs(Long.MIN_VALUE, Long.MAX_VALUE, Long.MAX_VALUE - 1)));
	}

	@Test
	public void increasingFrom() {
		LongSequence increasingFrom10 = LongSequence.increasingFrom(10);
		twice(() -> assertThat(increasingFrom10.limit(5), containsLongs(10L, 11L, 12L, 13L, 14L)));

		LongSequence increasingFrom_2 = LongSequence.increasingFrom(-2);
		twice(() -> assertThat(increasingFrom_2.limit(5), containsLongs(-2L, -1L, 0L, 1L, 2L)));

		LongSequence increasingFromMaxValue = LongSequence.increasingFrom(Long.MAX_VALUE);
		twice(() -> assertThat(increasingFromMaxValue.limit(3),
		                       containsLongs(Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE + 1)));
	}

	@Test
	public void steppingFrom() {
		LongSequence steppingFrom0Step10 = LongSequence.steppingFrom(0, 10);
		twice(() -> assertThat(steppingFrom0Step10.limit(5), containsLongs(0L, 10L, 20L, 30L, 40L)));

		LongSequence steppingFrom0Step_10 = LongSequence.steppingFrom(0, -10);
		twice(() -> assertThat(steppingFrom0Step_10.limit(5), containsLongs(0L, -10L, -20L, -30L, -40L)));

		LongSequence steppingFromMaxValueStep10 = LongSequence.steppingFrom(Long.MAX_VALUE, 10);
		twice(() -> assertThat(steppingFromMaxValueStep10.limit(3),
		                       containsLongs(Long.MAX_VALUE, Long.MIN_VALUE + 9, Long.MIN_VALUE + 19)));
	}

	@Test
	public void range() {
		LongSequence range1to6 = LongSequence.range(1, 6);
		twice(() -> assertThat(range1to6, containsLongs(1L, 2L, 3L, 4L, 5L, 6L)));

		LongSequence range6to1 = LongSequence.range(6L, 1L);
		twice(() -> assertThat(range6to1, containsLongs(6L, 5L, 4L, 3L, 2L, 1L)));

		LongSequence range_2to2 = LongSequence.range(-2, 2);
		twice(() -> assertThat(range_2to2, containsLongs(-2L, -1L, 0L, 1L, 2L)));

		LongSequence range2to_2 = LongSequence.range(2, -2);
		twice(() -> assertThat(range2to_2, containsLongs(2L, 1L, 0L, -1L, -2L)));

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
		twice(() -> assertThat(range1to6step2, containsLongs(1L, 3L, 5L)));

		LongSequence range6to1step2 = LongSequence.range(6, 1, 2);
		twice(() -> assertThat(range6to1step2, containsLongs(6L, 4L, 2L)));

		LongSequence range_6to6step2 = LongSequence.range(-6, 6, 3);
		twice(() -> assertThat(range_6to6step2, containsLongs(-6L, -3L, 0L, 3L, 6L)));

		LongSequence range6to_6step2 = LongSequence.range(6, -6, 3);
		twice(() -> assertThat(range6to_6step2, containsLongs(6L, 3L, 0L, -3L, -6L)));

		LongSequence maxValue = LongSequence.range(Long.MAX_VALUE - 2, Long.MAX_VALUE, 2);
		twice(() -> assertThat(maxValue, containsLongs(Long.MAX_VALUE - 2, Long.MAX_VALUE)));

		LongSequence minValue = LongSequence.range(Long.MIN_VALUE + 2, Long.MIN_VALUE, 2);
		twice(() -> assertThat(minValue, containsLongs(Long.MIN_VALUE + 2, Long.MIN_VALUE)));

		LongSequence crossingMaxValue = LongSequence.range(Long.MAX_VALUE - 3, Long.MAX_VALUE, 2);
		twice(() -> assertThat(crossingMaxValue, containsLongs(Long.MAX_VALUE - 3, Long.MAX_VALUE - 1)));

		LongSequence crossingMinValue = LongSequence.range(Long.MIN_VALUE + 3, Long.MIN_VALUE, 2);
		twice(() -> assertThat(crossingMinValue, containsLongs(Long.MIN_VALUE + 3, Long.MIN_VALUE + 1)));
	}

	@Test
	public void toChars() {
		CharSeq charSeq = LongSequence.increasingFrom('a').limit(5).toChars();
		twice(() -> assertThat(charSeq, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void toInts() {
		IntSequence intSequence = _12345.toInts();
		twice(() -> assertThat(intSequence, containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void toDoubles() {
		DoubleSequence doubleSequence = _12345.toDoubles();
		twice(() -> assertThat(doubleSequence, containsDoubles(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void toCharsMapped() {
		CharSeq charSeq = _12345.toChars(l -> (char) (0x60 + l));
		twice(() -> assertThat(charSeq, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void toIntsMapped() {
		IntSequence intSequence =
				LongSequence.increasingFrom(Integer.MAX_VALUE + 1L).toInts(l -> (int) (l - Integer.MAX_VALUE));
		twice(() -> assertThat(intSequence.limit(5), containsInts(1, 2, 3, 4, 5)));
	}

	@Test
	public void toDoublesMapped() {
		DoubleSequence doubleSequence = _12345.toDoubles(l -> l / 2.0);
		twice(() -> assertThat(doubleSequence, containsDoubles(0.5, 1.0, 1.5, 2.0, 2.5)));
	}

	@Test
	public void toSequence() {
		Sequence<Long> emptySequence = empty.toSequence(l -> l + 1);
		twice(() -> assertThat(emptySequence, is(emptyIterable())));

		Sequence<Long> longs = _12345.toSequence(l -> l + 1);
		twice(() -> assertThat(longs, contains(2L, 3L, 4L, 5L, 6L)));
	}

	@Test
	public void box() {
		Sequence<Long> emptyBoxed = empty.box();
		twice(() -> assertThat(emptyBoxed, is(emptyIterable())));

		Sequence<Long> longs = _12345.box();
		twice(() -> assertThat(longs, contains(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void repeat() {
		LongSequence repeatEmpty = empty.repeat();
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		LongSequence repeatOne = _1.repeat();
		twice(() -> assertThat(repeatOne.limit(3), containsLongs(1L, 1L, 1L)));

		LongSequence repeatTwo = _12.repeat();
		twice(() -> assertThat(repeatTwo.limit(5), containsLongs(1L, 2L, 1L, 2L, 1L)));

		LongSequence repeatThree = _123.repeat();
		twice(() -> assertThat(repeatThree.limit(8), containsLongs(1L, 2L, 3L, 1L, 2L, 3L, 1L, 2L)));

		LongSequence repeatVarying = LongSequence.from(new LongIterable() {
			private List<Long> list = asList(1L, 2L, 3L);
			int end = list.size();

			@Override
			public LongIterator iterator() {
				List<Long> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Long> iterator = subList.iterator();
				return new DelegatingLongIterator<Long, Iterator<Long>>(iterator) {
					@Override
					public long nextLong() {
						return iterator.next();
					}
				};
			}
		}).repeat();
		assertThat(repeatVarying, containsLongs(1L, 2L, 3L, 1L, 2L, 1L));
	}

	@Test
	public void repeatTwice() {
		LongSequence repeatEmpty = empty.repeat(2);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		LongSequence repeatOne = _1.repeat(2);
		twice(() -> assertThat(repeatOne, containsLongs(1L, 1L)));

		LongSequence repeatTwo = _12.repeat(2);
		twice(() -> assertThat(repeatTwo, containsLongs(1L, 2L, 1L, 2L)));

		LongSequence repeatThree = _123.repeat(2);
		twice(() -> assertThat(repeatThree, containsLongs(1L, 2L, 3L, 1L, 2L, 3L)));

		LongSequence repeatVarying = LongSequence.from(new LongIterable() {
			private List<Long> list = asList(1L, 2L, 3L);
			int end = list.size();

			@Override
			public LongIterator iterator() {
				List<Long> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Long> iterator = subList.iterator();
				return new DelegatingLongIterator<Long, Iterator<Long>>(iterator) {
					@Override
					public long nextLong() {
						return iterator.next();
					}
				};
			}
		}).repeat(2);
		assertThat(repeatVarying, containsLongs(1L, 2L, 3L, 1L, 2L));
	}

	@Test
	public void repeatZero() {
		LongSequence repeatEmpty = empty.repeat(0);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		LongSequence repeatOne = _1.repeat(0);
		twice(() -> assertThat(repeatOne, is(emptyIterable())));

		LongSequence repeatTwo = _12.repeat(0);
		twice(() -> assertThat(repeatTwo, is(emptyIterable())));

		LongSequence repeatThree = _123.repeat(0);
		twice(() -> assertThat(repeatThree, is(emptyIterable())));
	}

	@Test
	public void generate() {
		Queue<Long> queue = new ArrayDeque<>(asList(1L, 2L, 3L, 4L, 5L));
		LongSequence sequence = LongSequence.generate(queue::poll);

		LongIterator iterator = sequence.iterator();
		assertThat(iterator.nextLong(), is(1L));
		assertThat(iterator.nextLong(), is(2L));
		assertThat(iterator.nextLong(), is(3L));
		assertThat(iterator.nextLong(), is(4L));
		assertThat(iterator.nextLong(), is(5L));
		expecting(NullPointerException.class, iterator::next);

		LongIterator iterator2 = sequence.iterator();
		expecting(NullPointerException.class, iterator2::next);
	}

	@Test
	public void multiGenerate() {
		LongSequence sequence = LongSequence.multiGenerate(() -> {
			Queue<Long> queue = new ArrayDeque<>(asList(1L, 2L, 3L, 4L, 5L));
			return queue::poll;
		});

		twice(() -> {
			LongIterator iterator = sequence.iterator();
			assertThat(iterator.nextLong(), is(1L));
			assertThat(iterator.nextLong(), is(2L));
			assertThat(iterator.nextLong(), is(3L));
			assertThat(iterator.nextLong(), is(4L));
			assertThat(iterator.nextLong(), is(5L));
			expecting(NullPointerException.class, iterator::next);
		});
	}

	@Test
	public void random() {
		LongSequence random = LongSequence.random();

		twice(() -> times(1000, random.iterator()::nextLong));

		assertThat(random.limit(10), not(contains(random.limit(10))));
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

		assertThat(random.limit(10), not(contains(random.limit(10))));
	}

	@Test
	public void randomUpperWithSupplier() {
		LongSequence random = LongSequence.random(() -> new Random(17), 1000);

		twice(() -> assertThat(random.limit(5),
		                       containsLongs(732L, 697L, 82L, 816L, 44L)));
	}

	@Test
	public void randomLowerUpper() {
		LongSequence random = LongSequence.random(1000, 2000);

		twice(() -> {
			LongIterator iterator = random.iterator();
			times(1000, () -> assertThat(iterator.nextLong(),
			                             is(both(greaterThanOrEqualTo(1000L)).and(lessThan(2000L)))));
		});

		assertThat(random.limit(10), not(contains(random.limit(10))));
	}

	@Test
	public void randomLowerUpperWithSupplier() {
		LongSequence random = LongSequence.random(() -> new Random(17), 1000, 2000);

		twice(() -> assertThat(random.limit(5),
		                       containsLongs(1732L, 1697L, 1082L, 1816L, 1044L)));
	}

	@Test
	public void mapBack() {
		twice(() -> assertThat(_123.mapBack(17L, (p, i) -> p), containsLongs(17L, 1L, 2L)));
		twice(() -> assertThat(_123.mapBack(17L, (p, i) -> i), containsLongs(1L, 2L, 3L)));
	}

	@Test
	public void mapForward() {
		twice(() -> assertThat(_123.mapForward(17L, (i, n) -> i), containsLongs(1L, 2L, 3L)));
		twice(() -> assertThat(_123.mapForward(17L, (i, n) -> n), containsLongs(2L, 3L, 17L)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void window() {
		twice(() -> assertThat(empty.window(3), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3), contains(containsLongs(1L))));
		twice(() -> assertThat(_12.window(3), contains(containsLongs(1L, 2L))));
		twice(() -> assertThat(_123.window(3), contains(containsLongs(1L, 2L, 3L))));
		twice(() -> assertThat(_1234.window(3), contains(containsLongs(1L, 2L, 3L), containsLongs(2L, 3L, 4L))));
		twice(() -> assertThat(_12345.window(3), contains(containsLongs(1L, 2L, 3L), containsLongs(2L, 3L, 4L),
		                                                  containsLongs(3L, 4L, 5L))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithStep() {
		twice(() -> assertThat(empty.window(3, 2), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3, 2), contains(containsLongs(1L))));
		twice(() -> assertThat(_12.window(3, 2), contains(containsLongs(1L, 2L))));
		twice(() -> assertThat(_123.window(3, 2), contains(containsLongs(1L, 2L, 3L))));
		twice(() -> assertThat(_1234.window(3, 2), contains(containsLongs(1L, 2L, 3L), containsLongs(3L, 4L))));
		twice(() -> assertThat(_12345.window(3, 2), contains(containsLongs(1L, 2L, 3L), containsLongs(3L, 4L, 5L))));
		twice(() -> assertThat(_123456789.window(3, 2),
		                       contains(containsLongs(1L, 2L, 3L), containsLongs(3L, 4L, 5L), containsLongs(5L, 6L,
		                                                                                                    7L),
		                                containsLongs(7L, 8L, 9L))));

		twice(() -> assertThat(empty.window(3, 4), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3, 4), contains(containsLongs(1L))));
		twice(() -> assertThat(_12.window(3, 4), contains(containsLongs(1L, 2L))));
		twice(() -> assertThat(_123.window(3, 4), contains(containsLongs(1L, 2L, 3L))));
		twice(() -> assertThat(_1234.window(3, 4), contains(containsLongs(1L, 2L, 3L))));
		twice(() -> assertThat(_12345.window(3, 4), contains(containsLongs(1L, 2L, 3L), containsLongs(5L))));
		twice(() -> assertThat(_123456789.window(3, 4),
		                       contains(containsLongs(1L, 2L, 3L), containsLongs(5L, 6L, 7L), containsLongs(9L))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batch() {
		twice(() -> assertThat(empty.batch(3), is(emptyIterable())));
		twice(() -> assertThat(_1.batch(3), contains(containsLongs(1L))));
		twice(() -> assertThat(_12.batch(3), contains(containsLongs(1L, 2L))));
		twice(() -> assertThat(_123.batch(3), contains(containsLongs(1L, 2L, 3L))));
		twice(() -> assertThat(_1234.batch(3), contains(containsLongs(1L, 2L, 3L), containsLongs(4L))));
		twice(() -> assertThat(_12345.batch(3), contains(containsLongs(1L, 2L, 3L), containsLongs(4L, 5L))));
		twice(() -> assertThat(_123456789.batch(3), contains(containsLongs(1L, 2L, 3L), containsLongs(4L, 5L, 6L),
		                                                     containsLongs(7L, 8L, 9L))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnPredicate() {
		Sequence<LongSequence> emptyPartitioned = empty.batch((a, b) -> a > b);
		twice(() -> assertThat(emptyPartitioned, is(emptyIterable())));

		Sequence<LongSequence> onePartitioned = _1.batch((a, b) -> a > b);
		twice(() -> assertThat(onePartitioned, contains(containsLongs(1L))));

		Sequence<LongSequence> twoPartitioned = _12.batch((a, b) -> a > b);
		twice(() -> assertThat(twoPartitioned, contains(containsLongs(1L, 2L))));

		Sequence<LongSequence> threePartitioned = _123.batch((a, b) -> a > b);
		twice(() -> assertThat(threePartitioned, contains(containsLongs(1L, 2L, 3L))));

		Sequence<LongSequence> threeRandomPartitioned = threeRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(threeRandomPartitioned, contains(containsLongs(17L, 32L), containsLongs(12L))));

		Sequence<LongSequence> nineRandomPartitioned = nineRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(nineRandomPartitioned,
		                       contains(containsLongs(6L, 6L), containsLongs(1L), containsLongs(-7L, 1L, 2L, 17L),
		                                containsLongs(5L), containsLongs(4L))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void split() {
		Sequence<LongSequence> emptySplit = empty.split(3);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

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
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitLongPredicate() {
		Sequence<LongSequence> emptySplit = empty.split(x -> x % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

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
	}

	@Test
	public void removeAllAfterFilter() {
		List<Long> original = new ArrayList<>(asList(1L, 2L, 3L, 4L));

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
	private interface StrictLongIterable extends LongIterable {
		static LongIterable from(LongIterable iterable) {
			return () -> StrictLongIterator.from(iterable.iterator());
		}

		static LongIterable of(long... values) {
			return () -> StrictLongIterator.of(values);
		}
	}

	private interface StrictLongIterator extends LongIterator {
		static LongIterator from(LongIterator iterator) {
			return new LongIterator() {
				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public long nextLong() {
					return iterator.nextLong();
				}

				@Override
				public Long next() {
					throw new UnsupportedOperationException();
				}
			};
		}

		static LongIterator of(long... values) {
			return from(LongIterator.of(values));
		}
	}
}
