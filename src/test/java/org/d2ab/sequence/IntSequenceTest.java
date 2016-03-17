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

import org.d2ab.iterable.ints.IntIterable;
import org.d2ab.iterator.ints.DelegatingIntIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.util.Arrayz;
import org.junit.Test;

import java.util.*;
import java.util.function.IntBinaryOperator;
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

public class IntSequenceTest {
	private final IntSequence empty = IntSequence.empty();

	private final IntSequence _1 = IntSequence.of(1);
	private final IntSequence _12 = IntSequence.of(1, 2);
	private final IntSequence _123 = IntSequence.of(1, 2, 3);
	private final IntSequence _1234 = IntSequence.of(1, 2, 3, 4);
	private final IntSequence _12345 = IntSequence.of(1, 2, 3, 4, 5);
	private final IntSequence _123456789 = IntSequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

	private final IntSequence oneRandom = IntSequence.of(17);
	private final IntSequence twoRandom = IntSequence.of(17, 32);
	private final IntSequence nineRandom = IntSequence.of(6, 6, 1, 7, 1, 2, 17, 5, 4);

	@Test
	public void ofOne() {
		twice(() -> assertThat(_1, contains(1)));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(_123, contains(1, 2, 3)));
	}

	@Test
	public void forLoop() {
		twice(() -> {
			for (int ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			int expected = 1;
			for (int i : _123)
				assertThat(i, is(expected++));
		});
	}

	@Test
	public void forEach() {
		twice(() -> {
			empty.forEachInt(c -> fail("Should not get called"));
			_1.forEachInt(c -> assertThat(c, is(in(singletonList(1)))));
			_12.forEachInt(c -> assertThat(c, is(in(asList(1, 2)))));
			_123.forEachInt(c -> assertThat(c, is(in(asList(1, 2, 3)))));
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
	public void fromSequence() {
		IntSequence fromSequence = IntSequence.from(_123);

		twice(() -> assertThat(fromSequence, contains(1, 2, 3)));
	}

	@Test
	public void fromIterable() {
		Iterable<Integer> iterable = () -> asList(1, 2, 3).iterator();

		IntSequence sequenceFromIterable = IntSequence.from(iterable);

		twice(() -> assertThat(sequenceFromIterable, contains(1, 2, 3)));
	}

	@Test
	public void fromStream() {
		IntSequence sequenceFromStream = IntSequence.from(asList(1, 2, 3).stream());

		assertThat(sequenceFromStream, contains(1, 2, 3));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromEmptyStream() {
		IntSequence sequenceFromStream = IntSequence.from(Stream.of());

		assertThat(sequenceFromStream, is(emptyIterable()));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromIteratorSupplier() {
		Supplier<IntIterator> iterators = () -> IntIterator.from(asList(1, 2, 3));

		IntSequence sequenceFromIterators = IntSequence.from(iterators);

		twice(() -> assertThat(sequenceFromIterators, contains(1, 2, 3)));
	}

	@Test
	public void skip() {
		IntSequence skipNone = _123.skip(0);
		twice(() -> assertThat(skipNone, contains(1, 2, 3)));

		IntSequence skipOne = _123.skip(1);
		twice(() -> assertThat(skipOne, contains(2, 3)));

		IntSequence skipTwo = _123.skip(2);
		twice(() -> assertThat(skipTwo, contains(3)));

		IntSequence skipThree = _123.skip(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		IntSequence skipFour = _123.skip(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		IntSequence limitNone = _123.limit(0);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		IntSequence limitOne = _123.limit(1);
		twice(() -> assertThat(limitOne, contains(1)));

		IntSequence limitTwo = _123.limit(2);
		twice(() -> assertThat(limitTwo, contains(1, 2)));

		IntSequence limitThree = _123.limit(3);
		twice(() -> assertThat(limitThree, contains(1, 2, 3)));

		IntSequence limitFour = _123.limit(4);
		twice(() -> assertThat(limitFour, contains(1, 2, 3)));
	}

	@Test
	public void append() {
		IntSequence appended = _123.append(IntSequence.of(4, 5, 6)).append(IntSequence.of(7, 8));

		twice(() -> assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIterator() {
		IntSequence appended = _123.append(Arrayz.iterator(4, 5, 6)).append(Arrayz.iterator(7, 8));

		assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, contains(1, 2, 3));
	}

	@Test
	public void appendStream() {
		IntSequence appended = _123.append(Stream.of(4, 5, 6)).append(Stream.of(7, 8));

		assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8));

		IntIterator iterator = appended.iterator();
		assertThat(iterator.nextInt(), is(1)); // First three are ok
		assertThat(iterator.nextInt(), is(2));
		assertThat(iterator.nextInt(), is(3));

		expecting(NoSuchElementException.class, iterator::nextInt); // Hitting Stream that is exhausted
	}

	@Test
	public void appendArray() {
		IntSequence appended = _123.append(4, 5, 6).append(7, 8);

		twice(() -> assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIsLazy() {
		IntIterator first = IntIterator.from(asList(1, 2, 3));
		IntIterator second = IntIterator.from(asList(4, 5, 6));
		IntIterator third = IntIterator.from(asList(7, 8));

		IntSequence then = IntSequence.from(first).append(() -> second).append(() -> third);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));
		assertThat(third.hasNext(), is(true));

		assertThat(then, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(then, is(emptyIterable())); // iterators exhausted on second run
	}

	@Test
	public void thenIsLazyWhenSkippingHasNext() {
		IntIterator first = IntIterator.of(1);
		IntIterator second = IntIterator.of(2);

		IntSequence sequence = IntSequence.from(first).append(() -> second);

		// check delayed iteration
		IntIterator iterator = sequence.iterator();
		assertThat(iterator.next(), is(1));
		assertThat(iterator.next(), is(2));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		IntSequence filtered = IntSequence.of(1, 2, 3, 4, 5, 6, 7).filter(i -> (i % 2) == 0);

		twice(() -> assertThat(filtered, contains(2, 4, 6)));
	}

	@Test
	public void map() {
		IntSequence mapped = _123.map(c -> c + 1);
		twice(() -> assertThat(mapped, contains(2, 3, 4)));
	}

	@Test
	public void recurse() {
		IntSequence recursive = IntSequence.recurse(1, i -> i + 1);
		twice(() -> assertThat(recursive.limit(10), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void untilTerminal() {
		IntSequence until = IntSequence.recurse(1, x -> x + 1).until(7);
		twice(() -> assertThat(until, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void endingAtTerminal() {
		IntSequence endingAt = IntSequence.recurse(1, x -> x + 1).endingAt(7);
		twice(() -> assertThat(endingAt, contains(1, 2, 3, 4, 5, 6, 7)));
	}

	@Test
	public void untilPredicate() {
		IntSequence until = IntSequence.recurse(1, x -> x + 1).until(i -> i == 7);
		twice(() -> assertThat(until, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void endingAtPredicate() {
		IntSequence endingAt = IntSequence.recurse(1, x -> x + 1).endingAt(i -> i == 7);
		twice(() -> assertThat(endingAt, contains(1, 2, 3, 4, 5, 6, 7)));
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
		twice(() -> assertThat(stepThree, contains(1, 4, 7)));
	}

	@Test
	public void distinct() {
		IntSequence emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		IntSequence oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, contains(17)));

		IntSequence twoDuplicatesDistinct = IntSequence.of(17, 17).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, contains(17)));

		IntSequence nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, contains(6, 1, 7, 2, 17, 5, 4)));
	}

	@Test
	public void sorted() {
		IntSequence emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		IntSequence oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, contains(17)));

		IntSequence twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, contains(17, 32)));

		IntSequence nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, contains(1, 1, 2, 4, 5, 6, 6, 7, 17)));
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
		twice(() -> assertThat(nineMin, is(OptionalInt.of(1))));
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
		twice(() -> assertThat(peek, contains(1, 2, 3)));
	}

	@Test
	public void prefix() {
		IntSequence prefixEmpty = empty.prefix(327);
		twice(() -> assertThat(prefixEmpty, contains(327)));

		IntSequence prefix = _123.prefix(327);
		twice(() -> assertThat(prefix, contains(327, 1, 2, 3)));
	}

	@Test
	public void suffix() {
		IntSequence suffixEmpty = empty.suffix(532);
		twice(() -> assertThat(suffixEmpty, contains(532)));

		IntSequence suffix = _123.suffix(532);
		twice(() -> assertThat(suffix, contains(1, 2, 3, 532)));
	}

	@Test
	public void interleave() {
		assertThat(empty.interleave(empty), is(emptyIterable()));
		assertThat(_123.interleave(_12345), contains(1, 1, 2, 2, 3, 3, 4, 5));
		assertThat(_12345.interleave(_123), contains(1, 1, 2, 2, 3, 3, 4, 5));
	}

	@Test
	public void reverse() {
		IntSequence emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		IntSequence oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, contains(1)));

		IntSequence twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, contains(2, 1)));

		IntSequence threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, contains(3, 2, 1)));

		IntSequence nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed, contains(9, 8, 7, 6, 5, 4, 3, 2, 1)));
	}

	@Test
	public void positive() {
		IntSequence positive = IntSequence.positive();
		twice(() -> assertThat(positive.limit(3), contains(1, 2, 3)));
	}

	@Test
	public void positiveFromZero() {
		IntSequence positiveFromZero = IntSequence.positiveFromZero();
		twice(() -> assertThat(positiveFromZero.limit(3), contains(0, 1, 2)));
	}

	@Test
	public void negative() {
		IntSequence negative = IntSequence.negative();
		twice(() -> assertThat(negative.limit(3), contains(-1, -2, -3)));
	}

	@Test
	public void negativeFromZero() {
		IntSequence negativeFromZero = IntSequence.negativeFromZero();
		twice(() -> assertThat(negativeFromZero.limit(3), contains(0, -1, -2)));
	}

	@Test
	public void decreasingFrom() {
		IntSequence negative = IntSequence.decreasingFrom(-10);
		twice(() -> assertThat(negative.limit(5), contains(-10, -11, -12, -13, -14)));

		IntSequence decreasingFrom2 = IntSequence.decreasingFrom(2);
		twice(() -> assertThat(decreasingFrom2.limit(5), contains(2, 1, 0, -1, -2)));

		IntSequence decreasingFromMinValue = IntSequence.decreasingFrom(Integer.MIN_VALUE);
		twice(() -> assertThat(decreasingFromMinValue.limit(3),
		                       contains(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE - 1)));
	}

	@Test
	public void increasingFrom() {
		IntSequence increasingFrom10 = IntSequence.increasingFrom(10);
		twice(() -> assertThat(increasingFrom10.limit(3), contains(10, 11, 12)));

		IntSequence increasingFrom_2 = IntSequence.increasingFrom(-2);
		twice(() -> assertThat(increasingFrom_2.limit(5), contains(-2, -1, 0, 1, 2)));

		IntSequence increasingFromMaxValue = IntSequence.increasingFrom(Integer.MAX_VALUE);
		twice(() -> assertThat(increasingFromMaxValue.limit(3),
		                       contains(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE + 1)));
	}

	@Test
	public void steppingFrom() {
		IntSequence steppingFrom0Step10 = IntSequence.steppingFrom(0, 10);
		twice(() -> assertThat(steppingFrom0Step10.limit(3), contains(0, 10, 20)));

		IntSequence steppingFrom0Step_10 = IntSequence.steppingFrom(0, -10);
		twice(() -> assertThat(steppingFrom0Step_10.limit(3), contains(0, -10, -20)));

		IntSequence steppingFromMaxValueStep10 = IntSequence.steppingFrom(Integer.MAX_VALUE, 10);
		twice(() -> assertThat(steppingFromMaxValueStep10.limit(3),
		                       contains(Integer.MAX_VALUE, Integer.MIN_VALUE + 9, Integer.MIN_VALUE + 19)));
	}

	@Test
	public void range() {
		IntSequence range1to6 = IntSequence.range(1, 6);
		twice(() -> assertThat(range1to6, contains(1, 2, 3, 4, 5, 6)));

		IntSequence range6to1 = IntSequence.range(6, 1);
		twice(() -> assertThat(range6to1, contains(6, 5, 4, 3, 2, 1)));

		IntSequence range_2to2 = IntSequence.range(-2, 2);
		twice(() -> assertThat(range_2to2, contains(-2, -1, 0, 1, 2)));

		IntSequence range2to_2 = IntSequence.range(2, -2);
		twice(() -> assertThat(range2to_2, contains(2, 1, 0, -1, -2)));

		IntSequence maxValue = IntSequence.range(Integer.MAX_VALUE - 3, Integer.MAX_VALUE);
		twice(() -> assertThat(maxValue, contains(Integer.MAX_VALUE - 3, Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1,
		                                          Integer.MAX_VALUE)));

		IntSequence minValue = IntSequence.range(Integer.MIN_VALUE + 3, Integer.MIN_VALUE);
		twice(() -> assertThat(minValue, contains(Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 1,
		                                          Integer.MIN_VALUE)));
	}

	@Test
	public void rangeWithStep() {
		IntSequence range1to6step2 = IntSequence.range(1, 6, 2);
		twice(() -> assertThat(range1to6step2, contains(1, 3, 5)));

		IntSequence range6to1step2 = IntSequence.range(6, 1, 2);
		twice(() -> assertThat(range6to1step2, contains(6, 4, 2)));

		IntSequence range_6to6step2 = IntSequence.range(-6, 6, 3);
		twice(() -> assertThat(range_6to6step2, contains(-6, -3, 0, 3, 6)));

		IntSequence range6to_6step2 = IntSequence.range(6, -6, 3);
		twice(() -> assertThat(range6to_6step2, contains(6, 3, 0, -3, -6)));

		IntSequence maxValue = IntSequence.range(Integer.MAX_VALUE - 2, Integer.MAX_VALUE, 2);
		twice(() -> assertThat(maxValue, contains(Integer.MAX_VALUE - 2, Integer.MAX_VALUE)));

		IntSequence minValue = IntSequence.range(Integer.MIN_VALUE + 2, Integer.MIN_VALUE, 2);
		twice(() -> assertThat(minValue, contains(Integer.MIN_VALUE + 2, Integer.MIN_VALUE)));

		IntSequence crossingMaxValue = IntSequence.range(Integer.MAX_VALUE - 3, Integer.MAX_VALUE, 2);
		twice(() -> assertThat(crossingMaxValue, contains(Integer.MAX_VALUE - 3, Integer.MAX_VALUE - 1)));

		IntSequence crossingMinValue = IntSequence.range(Integer.MIN_VALUE + 3, Integer.MIN_VALUE, 2);
		twice(() -> assertThat(crossingMinValue, contains(Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 1)));
	}

	@Test
	public void toChars() {
		CharSeq charSeq = IntSequence.increasingFrom('a').toChars();
		twice(() -> assertThat(charSeq.limit(5), contains('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void toLongs() {
		LongSequence longSequence = IntSequence.positive().toLongs();
		twice(() -> assertThat(longSequence.limit(5), contains(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void toDoubles() {
		DoubleSequence doubleSequence = IntSequence.positive().toDoubles();
		twice(() -> assertThat(doubleSequence.limit(5), contains(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void toCharsMapped() {
		CharSeq charSeq = IntSequence.positiveFromZero().toChars(i -> (char) ('a' + i));
		twice(() -> assertThat(charSeq.limit(5), contains('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void toLongsMapped() {
		long maxInt = Integer.MAX_VALUE;

		LongSequence longSequence = IntSequence.positive().toLongs(i -> i * maxInt);
		twice(() -> assertThat(longSequence.limit(5),
		                       contains(maxInt, 2L * maxInt, 3L * maxInt, 4L * maxInt, 5L * maxInt)));
	}

	@Test
	public void toDoublesMapped() {
		DoubleSequence doubleSequence = IntSequence.positive().toDoubles(i -> i / 2.0);
		twice(() -> assertThat(doubleSequence.limit(5), contains(0.5, 1.0, 1.5, 2.0, 2.5)));
	}

	@Test
	public void toSequence() {
		Sequence<Integer> empty = IntSequence.empty().toSequence(i -> i + 1);
		twice(() -> assertThat(empty, is(emptyIterable())));

		Sequence<Integer> ints = IntSequence.positive().toSequence(i -> i + 1);
		twice(() -> assertThat(ints.limit(5), contains(2, 3, 4, 5, 6)));
	}

	@Test
	public void box() {
		Sequence<Integer> empty = IntSequence.empty().box();
		twice(() -> assertThat(empty, is(emptyIterable())));

		Sequence<Integer> ints = IntSequence.positive().box();
		twice(() -> assertThat(ints.limit(5), contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void repeat() {
		IntSequence repeatEmpty = empty.repeat();
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		IntSequence repeatOne = _1.repeat();
		twice(() -> assertThat(repeatOne.limit(3), contains(1, 1, 1)));

		IntSequence repeatTwo = _12.repeat();
		twice(() -> assertThat(repeatTwo.limit(5), contains(1, 2, 1, 2, 1)));

		IntSequence repeatThree = _123.repeat();
		twice(() -> assertThat(repeatThree.limit(8), contains(1, 2, 3, 1, 2, 3, 1, 2)));

		IntSequence repeatVarying = IntSequence.from(new IntIterable() {
			private List<Integer> list = asList(1, 2, 3);
			int end = list.size();

			@Override
			public IntIterator iterator() {
				List<Integer> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Integer> iterator = subList.iterator();
				return new DelegatingIntIterator<Integer, Iterator<Integer>>(iterator) {
					@Override
					public int nextInt() {
						return iterator.next();
					}
				};
			}
		}).repeat();
		assertThat(repeatVarying, contains(1, 2, 3, 1, 2, 1));
	}

	@Test
	public void repeatTwice() {
		IntSequence repeatEmpty = empty.repeat(2);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		IntSequence repeatOne = _1.repeat(2);
		twice(() -> assertThat(repeatOne, contains(1, 1)));

		IntSequence repeatTwo = _12.repeat(2);
		twice(() -> assertThat(repeatTwo, contains(1, 2, 1, 2)));

		IntSequence repeatThree = _123.repeat(2);
		twice(() -> assertThat(repeatThree, contains(1, 2, 3, 1, 2, 3)));

		IntSequence repeatVarying = IntSequence.from(new IntIterable() {
			private List<Integer> list = asList(1, 2, 3);
			int end = list.size();

			@Override
			public IntIterator iterator() {
				List<Integer> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Integer> iterator = subList.iterator();
				return new DelegatingIntIterator<Integer, Iterator<Integer>>(iterator) {
					@Override
					public int nextInt() {
						return iterator.next();
					}
				};
			}
		}).repeat(2);
		assertThat(repeatVarying, contains(1, 2, 3, 1, 2));
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
		IntSequence sequence = IntSequence.generate(queue::poll).endingAt(5);

		assertThat(sequence, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void mapBack() {
		twice(() -> assertThat(_123.mapBack(17, (p, i) -> p), contains(17, 1, 2)));
		twice(() -> assertThat(_123.mapBack(17, (p, i) -> i), contains(1, 2, 3)));
	}

	@Test
	public void mapForward() {
		twice(() -> assertThat(_123.mapForward(17, (i, n) -> i), contains(1, 2, 3)));
		twice(() -> assertThat(_123.mapForward(17, (i, n) -> n), contains(2, 3, 17)));
	}
}
