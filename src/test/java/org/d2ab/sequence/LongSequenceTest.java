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

import org.d2ab.iterable.longs.LongIterable;
import org.d2ab.iterator.longs.DelegatingLongIterator;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.util.Arrayz;
import org.junit.Test;

import java.util.*;
import java.util.function.LongBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class LongSequenceTest {
	private final LongSequence empty = LongSequence.empty();

	private final LongSequence _1 = LongSequence.of(1L);
	private final LongSequence _12 = LongSequence.of(1L, 2L);
	private final LongSequence _123 = LongSequence.of(1L, 2L, 3L);
	private final LongSequence _1234 = LongSequence.of(1L, 2L, 3L, 4L);
	private final LongSequence _12345 = LongSequence.of(1L, 2L, 3L, 4L, 5L);
	private final LongSequence _123456789 = LongSequence.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);

	private final LongSequence oneRandom = LongSequence.of(17L);
	private final LongSequence twoRandom = LongSequence.of(17L, 32L);
	private final LongSequence nineRandom = LongSequence.of(6L, 6L, 1L, 7L, 1L, 2L, 17L, 5L, 4L);

	@Test
	public void ofOne() {
		twice(() -> assertThat(_1, contains(1L)));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(_123, contains(1L, 2L, 3L)));
	}

	@Test
	public void forLoop() {
		twice(() -> {
			for (long ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			long expected = 1L;
			for (long i : _123)
				assertThat(i, is(expected++));
		});
	}

	@Test
	public void forEach() {
		twice(() -> {
			empty.forEachLong(c -> fail("Should not get called"));
			_1.forEachLong(c -> assertThat(c, is(in(singletonList(1L)))));
			_12.forEachLong(c -> assertThat(c, is(in(asList(1L, 2L)))));
			_123.forEachLong(c -> assertThat(c, is(in(asList(1L, 2L, 3L)))));
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
	public void ofNone() {
		LongSequence sequence = LongSequence.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void fromSequence() {
		LongSequence fromSequence = LongSequence.from(_123);

		twice(() -> assertThat(fromSequence, contains(1L, 2L, 3L)));
	}

	@Test
	public void fromIterable() {
		Iterable<Long> iterable = () -> asList(1L, 2L, 3L).iterator();

		LongSequence sequenceFromIterable = LongSequence.from(iterable);

		twice(() -> assertThat(sequenceFromIterable, contains(1L, 2L, 3L)));
	}

	@Test
	public void fromStream() {
		LongSequence sequenceFromStream = LongSequence.from(asList(1L, 2L, 3L).stream());

		assertThat(sequenceFromStream, contains(1L, 2L, 3L));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromEmptyStream() {
		LongSequence sequenceFromStream = LongSequence.from(Stream.of());

		assertThat(sequenceFromStream, is(emptyIterable()));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromIteratorSupplier() {
		Supplier<LongIterator> iterators = () -> LongIterator.from(asList(1L, 2L, 3L));

		LongSequence sequenceFromIterators = LongSequence.from(iterators);

		twice(() -> assertThat(sequenceFromIterators, contains(1L, 2L, 3L)));
	}

	@Test
	public void skip() {
		LongSequence skipNone = _123.skip(0L);
		twice(() -> assertThat(skipNone, contains(1L, 2L, 3L)));

		LongSequence skipOne = _123.skip(1L);
		twice(() -> assertThat(skipOne, contains(2L, 3L)));

		LongSequence skipTwo = _123.skip(2L);
		twice(() -> assertThat(skipTwo, contains(3L)));

		LongSequence skipThree = _123.skip(3L);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		LongSequence skipFour = _123.skip(4L);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		LongSequence limitNone = _123.limit(0L);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		LongSequence limitOne = _123.limit(1L);
		twice(() -> assertThat(limitOne, contains(1L)));

		LongSequence limitTwo = _123.limit(2L);
		twice(() -> assertThat(limitTwo, contains(1L, 2L)));

		LongSequence limitThree = _123.limit(3L);
		twice(() -> assertThat(limitThree, contains(1L, 2L, 3L)));

		LongSequence limitFour = _123.limit(4L);
		twice(() -> assertThat(limitFour, contains(1L, 2L, 3L)));
	}

	@Test
	public void append() {
		LongSequence appended = _123.append(LongSequence.of(4L, 5L, 6L)).append(LongSequence.of(7L, 8L));

		twice(() -> assertThat(appended, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)));
	}

	@Test
	public void appendIterator() {
		LongSequence appended = _123.append(Arrayz.iterator(4L, 5L, 6L)).append(Arrayz.iterator(7L, 8L));

		assertThat(appended, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
		assertThat(appended, contains(1L, 2L, 3L));
	}

	@Test
	public void appendStream() {
		LongSequence appended = _123.append(Stream.of(4L, 5L, 6L)).append(Stream.of(7L, 8L));

		assertThat(appended, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));

		LongIterator iterator = appended.iterator();
		assertThat(iterator.nextLong(), is(1L)); // First three are ok
		assertThat(iterator.nextLong(), is(2L));
		assertThat(iterator.nextLong(), is(3L));

		expecting(NoSuchElementException.class, iterator::nextLong); // Hitting Stream that is exhausted
	}

	@Test
	public void appendArray() {
		LongSequence appended = _123.append(4L, 5L, 6L).append(7L, 8L);

		twice(() -> assertThat(appended, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)));
	}

	@Test
	public void appendIsLazy() {
		LongIterator first = LongIterator.from(asList(1L, 2L, 3L));
		LongIterator second = LongIterator.from(asList(4L, 5L, 6L));
		LongIterator third = LongIterator.from(asList(7L, 8L));

		LongSequence then = LongSequence.from(first).append(() -> second).append(() -> third);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));
		assertThat(third.hasNext(), is(true));

		assertThat(then, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
		assertThat(then, is(emptyIterable())); // iterators exhausted on second run
	}

	@Test
	public void thenIsLazyWhenSkippingHasNext() {
		LongIterator first = LongIterator.of(1L);
		LongIterator second = LongIterator.of(2L);

		LongSequence sequence = LongSequence.from(first).append(() -> second);

		// check delayed iteration
		LongIterator iterator = sequence.iterator();
		assertThat(iterator.next(), is(1L));
		assertThat(iterator.next(), is(2L));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		LongSequence filtered = LongSequence.of(1L, 2L, 3L, 4L, 5L, 6L, 7L).filter(i -> (i % 2L) == 0L);

		twice(() -> assertThat(filtered, contains(2L, 4L, 6L)));
	}

	@Test
	public void map() {
		LongSequence mapped = _123.map(c -> c + 1L);
		twice(() -> assertThat(mapped, contains(2L, 3L, 4L)));
	}

	@Test
	public void recurse() {
		LongSequence recursive = LongSequence.recurse(1L, i -> i + 1L);
		twice(() -> assertThat(recursive.limit(10L), contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)));
	}

	@Test
	public void untilTerminal() {
		LongSequence until = LongSequence.recurse(1, x -> x + 1).until(7L);
		twice(() -> assertThat(until, contains(1L, 2L, 3L, 4L, 5L, 6L)));
	}

	@Test
	public void endingAtTerminal() {
		LongSequence endingAt = LongSequence.recurse(1, x -> x + 1).endingAt(7L);
		twice(() -> assertThat(endingAt, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L)));
	}

	@Test
	public void untilPredicate() {
		LongSequence until = LongSequence.recurse(1, x -> x + 1).until(i -> i == 7L);
		twice(() -> assertThat(until, contains(1L, 2L, 3L, 4L, 5L, 6L)));
	}

	@Test
	public void endingAtPredicate() {
		LongSequence endingAt = LongSequence.recurse(1, x -> x + 1).endingAt(i -> i == 7L);
		twice(() -> assertThat(endingAt, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L)));
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
	public void step() {
		LongSequence stepThree = _123456789.step(3L);
		twice(() -> assertThat(stepThree, contains(1L, 4L, 7L)));
	}

	@Test
	public void distinct() {
		LongSequence emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		LongSequence oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, contains(17L)));

		LongSequence twoDuplicatesDistinct = LongSequence.of(17L, 17L).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, contains(17L)));

		LongSequence nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, contains(6L, 1L, 7L, 2L, 17L, 5L, 4L)));
	}

	@Test
	public void sorted() {
		LongSequence emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		LongSequence oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, contains(17L)));

		LongSequence twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, contains(17L, 32L)));

		LongSequence nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, contains(1L, 1L, 2L, 4L, 5L, 6L, 6L, 7L, 17L)));
	}

	@Test
	public void min() {
		OptionalLong emptyMin = empty.min();
		twice(() -> assertThat(emptyMin, is(OptionalLong.empty())));

		OptionalLong oneMin = oneRandom.min();
		twice(() -> assertThat(oneMin, is(OptionalLong.of(17L))));

		OptionalLong twoMin = twoRandom.min();
		twice(() -> assertThat(twoMin, is(OptionalLong.of(17L))));

		OptionalLong nineMin = nineRandom.min();
		twice(() -> assertThat(nineMin, is(OptionalLong.of(1L))));
	}

	@Test
	public void max() {
		OptionalLong emptyMax = empty.max();
		twice(() -> assertThat(emptyMax, is(OptionalLong.empty())));

		OptionalLong oneMax = oneRandom.max();
		twice(() -> assertThat(oneMax, is(OptionalLong.of(17L))));

		OptionalLong twoMax = twoRandom.max();
		twice(() -> assertThat(twoMax, is(OptionalLong.of(32L))));

		OptionalLong nineMax = nineRandom.max();
		twice(() -> assertThat(nineMax, is(OptionalLong.of(17L))));
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
		twice(() -> assertThat(peek, contains(1L, 2L, 3L)));
	}

	@Test
	public void prefix() {
		LongSequence prefixEmpty = empty.prefix(327L);
		twice(() -> assertThat(prefixEmpty, contains(327L)));

		LongSequence prefix = _123.prefix(327L);
		twice(() -> assertThat(prefix, contains(327L, 1L, 2L, 3L)));
	}

	@Test
	public void suffix() {
		LongSequence suffixEmpty = empty.suffix(532L);
		twice(() -> assertThat(suffixEmpty, contains(532L)));

		LongSequence suffix = _123.suffix(532L);
		twice(() -> assertThat(suffix, contains(1L, 2L, 3L, 532L)));
	}

	@Test
	public void interleave() {
		assertThat(empty.interleave(empty), is(emptyIterable()));
		assertThat(_123.interleave(_12345), contains(1L, 1L, 2L, 2L, 3L, 3L, 4L, 5L));
		assertThat(_12345.interleave(_123), contains(1L, 1L, 2L, 2L, 3L, 3L, 4L, 5L));
	}

	@Test
	public void reverse() {
		LongSequence emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		LongSequence oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, contains(1L)));

		LongSequence twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, contains(2L, 1L)));

		LongSequence threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, contains(3L, 2L, 1L)));

		LongSequence nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed, contains(9L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L)));
	}

	@Test
	public void positive() {
		LongSequence positive = LongSequence.positive();
		twice(() -> assertThat(positive.limit(3), contains(1L, 2L, 3L)));
	}

	@Test
	public void positiveFromZero() {
		LongSequence positiveFromZero = LongSequence.positiveFromZero();
		twice(() -> assertThat(positiveFromZero.limit(3), contains(0L, 1L, 2L)));
	}

	@Test
	public void negative() {
		LongSequence negative = LongSequence.negative();
		twice(() -> assertThat(negative.limit(3), contains(-1L, -2L, -3L)));
	}

	@Test
	public void negativeFromZero() {
		LongSequence negativeFromZero = LongSequence.negativeFromZero();
		twice(() -> assertThat(negativeFromZero.limit(3), contains(0L, -1L, -2L)));
	}

	@Test
	public void decreasingFrom() {
		LongSequence decreasing = LongSequence.decreasingFrom(-10);
		twice(() -> assertThat(decreasing.limit(3), contains(-10L, -11L, -12L)));

		LongSequence decreasingFrom2 = LongSequence.decreasingFrom(2);
		twice(() -> assertThat(decreasingFrom2.limit(5), contains(2L, 1L, 0L, -1L, -2L)));

		LongSequence decreasingFromMinValue = LongSequence.decreasingFrom(Long.MIN_VALUE);
		twice(() -> assertThat(decreasingFromMinValue.limit(3),
		                       contains(Long.MIN_VALUE, Long.MAX_VALUE, Long.MAX_VALUE - 1)));
	}

	@Test
	public void increasingFrom() {
		LongSequence increasingFrom10 = LongSequence.increasingFrom(10);
		twice(() -> assertThat(increasingFrom10.limit(3), contains(10L, 11L, 12L)));

		LongSequence increasingFrom_2 = LongSequence.increasingFrom(-2);
		twice(() -> assertThat(increasingFrom_2.limit(5), contains(-2L, -1L, 0L, 1L, 2L)));

		LongSequence increasingFromMaxValue = LongSequence.increasingFrom(Long.MAX_VALUE);
		twice(() -> assertThat(increasingFromMaxValue.limit(3),
		                       contains(Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE + 1)));
	}

	@Test
	public void steppingFrom() {
		LongSequence steppingFrom0Step10 = LongSequence.steppingFrom(0, 10);
		twice(() -> assertThat(steppingFrom0Step10.limit(3), contains(0L, 10L, 20L)));

		LongSequence steppingFrom0Step_10 = LongSequence.steppingFrom(0, -10);
		twice(() -> assertThat(steppingFrom0Step_10.limit(3), contains(0L, -10L, -20L)));

		LongSequence steppingFromMaxValueStep10 = LongSequence.steppingFrom(Long.MAX_VALUE, 10);
		twice(() -> assertThat(steppingFromMaxValueStep10.limit(3),
		                       contains(Long.MAX_VALUE, Long.MIN_VALUE + 9, Long.MIN_VALUE + 19)));
	}

	@Test
	public void range() {
		LongSequence range1to6 = LongSequence.range(1, 6);
		twice(() -> assertThat(range1to6, contains(1L, 2L, 3L, 4L, 5L, 6L)));

		LongSequence range6to1 = LongSequence.range(6L, 1L);
		twice(() -> assertThat(range6to1, contains(6L, 5L, 4L, 3L, 2L, 1L)));

		LongSequence range_2to2 = LongSequence.range(-2, 2);
		twice(() -> assertThat(range_2to2, contains(-2L, -1L, 0L, 1L, 2L)));

		LongSequence range2to_2 = LongSequence.range(2, -2);
		twice(() -> assertThat(range2to_2, contains(2L, 1L, 0L, -1L, -2L)));

		LongSequence maxValue = LongSequence.range(Long.MAX_VALUE - 3, Long.MAX_VALUE);
		twice(() -> assertThat(maxValue,
		                       contains(Long.MAX_VALUE - 3, Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE)));

		LongSequence minValue = LongSequence.range(Long.MIN_VALUE + 3, Long.MIN_VALUE);
		twice(() -> assertThat(minValue,
		                       contains(Long.MIN_VALUE + 3, Long.MIN_VALUE + 2, Long.MIN_VALUE + 1, Long.MIN_VALUE)));
	}

	@Test
	public void rangeWithStep() {
		LongSequence range1to6step2 = LongSequence.range(1, 6, 2);
		twice(() -> assertThat(range1to6step2, contains(1L, 3L, 5L)));

		LongSequence range6to1step2 = LongSequence.range(6, 1, 2);
		twice(() -> assertThat(range6to1step2, contains(6L, 4L, 2L)));

		LongSequence range_6to6step2 = LongSequence.range(-6, 6, 3);
		twice(() -> assertThat(range_6to6step2, contains(-6L, -3L, 0L, 3L, 6L)));

		LongSequence range6to_6step2 = LongSequence.range(6, -6, 3);
		twice(() -> assertThat(range6to_6step2, contains(6L, 3L, 0L, -3L, -6L)));

		LongSequence maxValue = LongSequence.range(Long.MAX_VALUE - 2, Long.MAX_VALUE, 2);
		twice(() -> assertThat(maxValue, contains(Long.MAX_VALUE - 2, Long.MAX_VALUE)));

		LongSequence minValue = LongSequence.range(Long.MIN_VALUE + 2, Long.MIN_VALUE, 2);
		twice(() -> assertThat(minValue, contains(Long.MIN_VALUE + 2, Long.MIN_VALUE)));

		LongSequence crossingMaxValue = LongSequence.range(Long.MAX_VALUE - 3, Long.MAX_VALUE, 2);
		twice(() -> assertThat(crossingMaxValue, contains(Long.MAX_VALUE - 3, Long.MAX_VALUE - 1)));

		LongSequence crossingMinValue = LongSequence.range(Long.MIN_VALUE + 3, Long.MIN_VALUE, 2);
		twice(() -> assertThat(crossingMinValue, contains(Long.MIN_VALUE + 3, Long.MIN_VALUE + 1)));
	}

	@Test
	public void toChars() {
		CharSeq charSeq = LongSequence.increasingFrom('a').limit(5).toChars();
		twice(() -> assertThat(charSeq, contains('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void toInts() {
		IntSequence intSequence = LongSequence.positive().limit(5).toInts();
		twice(() -> assertThat(intSequence, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void toDoubles() {
		DoubleSequence doubleSequence = LongSequence.positive().limit(5).toDoubles();
		twice(() -> assertThat(doubleSequence, contains(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void toCharsMapped() {
		CharSeq charSeq = LongSequence.positive().limit(5).toChars(l -> (char) (0x60 + l));
		twice(() -> assertThat(charSeq, contains('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void toIntsMapped() {
		IntSequence intSequence = LongSequence.increasingFrom(Integer.MAX_VALUE + 1L)
		                                      .toInts(l -> (int) (l - Integer.MAX_VALUE));
		twice(() -> assertThat(intSequence.limit(5), contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void toDoublesMapped() {
		DoubleSequence doubleSequence = LongSequence.positive().toDoubles(l -> l / 2.0);
		twice(() -> assertThat(doubleSequence.limit(5), contains(0.5, 1.0, 1.5, 2.0, 2.5)));
	}

	@Test
	public void toSequence() {
		Sequence<Long> empty = LongSequence.empty().toSequence(l -> l + 1);
		twice(() -> assertThat(empty, is(emptyIterable())));

		Sequence<Long> longs = LongSequence.positive().toSequence(l -> l + 1);
		twice(() -> assertThat(longs.limit(5), contains(2L, 3L, 4L, 5L, 6L)));
	}

	@Test
	public void box() {
		Sequence<Long> empty = LongSequence.empty().box();
		twice(() -> assertThat(empty, is(emptyIterable())));

		Sequence<Long> longs = LongSequence.positive().box();
		twice(() -> assertThat(longs.limit(5), contains(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void repeat() {
		LongSequence repeatEmpty = empty.repeat();
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		LongSequence repeatOne = _1.repeat();
		twice(() -> assertThat(repeatOne.limit(3), contains(1L, 1L, 1L)));

		LongSequence repeatTwo = _12.repeat();
		twice(() -> assertThat(repeatTwo.limit(5), contains(1L, 2L, 1L, 2L, 1L)));

		LongSequence repeatThree = _123.repeat();
		twice(() -> assertThat(repeatThree.limit(8), contains(1L, 2L, 3L, 1L, 2L, 3L, 1L, 2L)));

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
		assertThat(repeatVarying, contains(1L, 2L, 3L, 1L, 2L, 1L));
	}

	@Test
	public void repeatTwice() {
		LongSequence repeatEmpty = empty.repeat(2);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		LongSequence repeatOne = _1.repeat(2);
		twice(() -> assertThat(repeatOne, contains(1L, 1L)));

		LongSequence repeatTwo = _12.repeat(2);
		twice(() -> assertThat(repeatTwo, contains(1L, 2L, 1L, 2L)));

		LongSequence repeatThree = _123.repeat(2);
		twice(() -> assertThat(repeatThree, contains(1L, 2L, 3L, 1L, 2L, 3L)));

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
		assertThat(repeatVarying, contains(1L, 2L, 3L, 1L, 2L));
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
		LongSequence sequence = LongSequence.generate(queue::poll).endingAt(5L);

		assertThat(sequence, contains(1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void mapBack() {
		twice(() -> assertThat(_123.mapBack(17L, (p, i) -> p), contains(17L, 1L, 2L)));
		twice(() -> assertThat(_123.mapBack(17L, (p, i) -> i), contains(1L, 2L, 3L)));
	}

	@Test
	public void mapForward() {
		twice(() -> assertThat(_123.mapForward(17L, (i, n) -> i), contains(1L, 2L, 3L)));
		twice(() -> assertThat(_123.mapForward(17L, (i, n) -> n), contains(2L, 3L, 17L)));
	}
}
