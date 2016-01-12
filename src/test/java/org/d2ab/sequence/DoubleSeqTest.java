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

import org.d2ab.primitive.doubles.DelegatingDoubleIterator;
import org.d2ab.primitive.doubles.DoubleIterable;
import org.d2ab.primitive.doubles.DoubleIterator;
import org.d2ab.utils.Arrayz;
import org.junit.Test;

import java.util.*;
import java.util.function.DoubleBinaryOperator;
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

public class DoubleSeqTest {
	private final DoubleSeq empty = DoubleSeq.empty();

	private final DoubleSeq _1 = DoubleSeq.of(1.0);
	private final DoubleSeq _12 = DoubleSeq.of(1.0, 2.0);
	private final DoubleSeq _123 = DoubleSeq.of(1.0, 2.0, 3.0);
	private final DoubleSeq _1234 = DoubleSeq.of(1.0, 2.0, 3.0, 4.0);
	private final DoubleSeq _12345 = DoubleSeq.of(1.0, 2.0, 3.0, 4.0, 5.0);
	private final DoubleSeq _123456789 = DoubleSeq.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);

	private final DoubleSeq oneRandom = DoubleSeq.of(17.0);
	private final DoubleSeq twoRandom = DoubleSeq.of(17.0, 32.0);
	private final DoubleSeq nineRandom = DoubleSeq.of(6.0, 6.0, 1.0, 7.0, 1.0, 2.0, 17.0, 5.0, 4.0);

	@Test
	public void ofOne() throws Exception {
		twice(() -> assertThat(_1, contains(1.0)));
	}

	@Test
	public void ofMany() throws Exception {
		twice(() -> assertThat(_123, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void forLoop() throws Exception {
		twice(() -> {
			for (double ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			double expected = 1.0;
			for (double i : _123)
				assertThat(i, is(expected++));
		});
	}

	@Test
	public void forEach() throws Exception {
		twice(() -> {
			empty.forEachDouble(c -> fail("Should not get called"));
			_1.forEachDouble(c -> assertThat(c, is(in(singletonList(1.0)))));
			_12.forEachDouble(c -> assertThat(c, is(in(asList(1.0, 2.0)))));
			_123.forEachDouble(c -> assertThat(c, is(in(asList(1.0, 2.0, 3.0)))));
		});
	}

	@Test
	public void iterator() throws Exception {
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
	public void ofNone() throws Exception {
		DoubleSeq sequence = DoubleSeq.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() throws Exception {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void fromSequence() throws Exception {
		DoubleSeq fromSequence = DoubleSeq.from(_123);

		twice(() -> assertThat(fromSequence, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void fromIterable() throws Exception {
		Iterable<Double> iterable = () -> asList(1.0, 2.0, 3.0).iterator();

		DoubleSeq sequenceFromIterable = DoubleSeq.from(iterable);

		twice(() -> assertThat(sequenceFromIterable, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void fromStream() throws Exception {
		DoubleSeq sequenceFromStream = DoubleSeq.from(asList(1.0, 2.0, 3.0).stream());

		assertThat(sequenceFromStream, contains(1.0, 2.0, 3.0));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromEmptyStream() throws Exception {
		DoubleSeq sequenceFromStream = DoubleSeq.from(Stream.of());

		assertThat(sequenceFromStream, is(emptyIterable()));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromIteratorSupplier() throws Exception {
		Supplier<DoubleIterator> iterators = () -> DoubleIterator.from(asList(1.0, 2.0, 3.0));

		DoubleSeq sequenceFromIterators = DoubleSeq.from(iterators);

		twice(() -> assertThat(sequenceFromIterators, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void skip() {
		DoubleSeq skipNone = _123.skip(0.0);
		twice(() -> assertThat(skipNone, contains(1.0, 2.0, 3.0)));

		DoubleSeq skipOne = _123.skip(1.0);
		twice(() -> assertThat(skipOne, contains(2.0, 3.0)));

		DoubleSeq skipTwo = _123.skip(2.0);
		twice(() -> assertThat(skipTwo, contains(3.0)));

		DoubleSeq skipThree = _123.skip(3.0);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		DoubleSeq skipFour = _123.skip(4.0);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		DoubleSeq limitNone = _123.limit(0.0);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		DoubleSeq limitOne = _123.limit(1.0);
		twice(() -> assertThat(limitOne, contains(1.0)));

		DoubleSeq limitTwo = _123.limit(2.0);
		twice(() -> assertThat(limitTwo, contains(1.0, 2.0)));

		DoubleSeq limitThree = _123.limit(3.0);
		twice(() -> assertThat(limitThree, contains(1.0, 2.0, 3.0)));

		DoubleSeq limitFour = _123.limit(4.0);
		twice(() -> assertThat(limitFour, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void append() {
		DoubleSeq appended = _123.append(DoubleSeq.of(4.0, 5.0, 6.0)).append(DoubleSeq.of(7.0, 8.0));

		twice(() -> assertThat(appended, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)));
	}

	@Test
	public void appendIterator() {
		DoubleSeq appended = _123.append(Arrayz.iterator(4.0, 5.0, 6.0)).append(Arrayz.iterator(7.0, 8.0));

		assertThat(appended, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
		assertThat(appended, contains(1.0, 2.0, 3.0));
	}

	@Test
	public void appendStream() {
		DoubleSeq appended = _123.append(Stream.of(4.0, 5.0, 6.0)).append(Stream.of(7.0, 8.0));

		assertThat(appended, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));

		DoubleIterator iterator = appended.iterator();
		assertThat(iterator.nextDouble(), is(1.0)); // First three are ok
		assertThat(iterator.nextDouble(), is(2.0));
		assertThat(iterator.nextDouble(), is(3.0));

		expecting(NoSuchElementException.class, iterator::nextDouble); // Hitting Stream that is exhausted
	}

	@Test
	public void appendArray() {
		DoubleSeq appended = _123.append(4.0, 5.0, 6.0).append(7.0, 8.0);

		twice(() -> assertThat(appended, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)));
	}

	@Test
	public void appendIsLazy() {
		DoubleIterator first = DoubleIterator.from(asList(1.0, 2.0, 3.0));
		DoubleIterator second = DoubleIterator.from(asList(4.0, 5.0, 6.0));
		DoubleIterator third = DoubleIterator.from(asList(7.0, 8.0));

		DoubleSeq then = DoubleSeq.from(first).append(() -> second).append(() -> third);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));
		assertThat(third.hasNext(), is(true));

		assertThat(then, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
		assertThat(then, is(emptyIterable())); // iterators exhausted on second run
	}

	@Test
	public void thenIsLazyWhenSkippingHasNext() {
		DoubleIterator first = DoubleIterator.of(1.0);
		DoubleIterator second = DoubleIterator.of(2.0);

		DoubleSeq sequence = DoubleSeq.from(first).append(() -> second);

		// check delayed iteration
		DoubleIterator iterator = sequence.iterator();
		assertThat(iterator.next(), is(1.0));
		assertThat(iterator.next(), is(2.0));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		DoubleSeq filtered = DoubleSeq.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0).filter(i -> (i % 2.0) == 0.0);

		twice(() -> assertThat(filtered, contains(2.0, 4.0, 6.0)));
	}

	@Test
	public void map() {
		DoubleSeq mapped = _123.map(c -> c + 1.0);
		twice(() -> assertThat(mapped, contains(2.0, 3.0, 4.0)));
	}

	@Test
	public void recurse() {
		DoubleSeq recursive = DoubleSeq.recurse(1.0, i -> i + 1.0);
		twice(() -> assertThat(recursive.limit(10.0), contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)));
	}

	@Test
	public void recurseUntil() {
		DoubleSeq until = DoubleSeq.recurse(1.0, c -> c + 1.0).until(7.0);
		twice(() -> assertThat(until, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void recurseEndingAt() {
		DoubleSeq endingAt = DoubleSeq.recurse(1.0, c -> c + 1.0).endingAt(7.0);
		twice(() -> assertThat(endingAt, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0)));
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
		DoubleSeq stepThree = _123456789.step(3.0);
		twice(() -> assertThat(stepThree, contains(1.0, 4.0, 7.0)));
	}

	@Test
	public void sorted() {
		DoubleSeq emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		DoubleSeq oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, contains(17.0)));

		DoubleSeq twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, contains(17.0, 32.0)));

		DoubleSeq nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, contains(1.0, 1.0, 2.0, 4.0, 5.0, 6.0, 6.0, 7.0, 17.0)));
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
		twice(() -> assertThat(nineMin, is(OptionalDouble.of(1.0))));
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
		twice(() -> assertThat(empty.count(), is(0.0)));
		twice(() -> assertThat(_1.count(), is(1.0)));
		twice(() -> assertThat(_12.count(), is(2.0)));
		twice(() -> assertThat(_123456789.count(), is(9.0)));
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
		DoubleSeq peek = _123.peek(x -> assertThat(x, is(both(greaterThan(0.0)).and(lessThan(4.0)))));
		twice(() -> assertThat(peek, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void prefix() {
		DoubleSeq prefixEmpty = empty.prefix(327.0);
		twice(() -> assertThat(prefixEmpty, contains(327.0)));

		DoubleSeq prefix = _123.prefix(327.0);
		twice(() -> assertThat(prefix, contains(327.0, 1.0, 2.0, 3.0)));
	}

	@Test
	public void suffix() {
		DoubleSeq suffixEmpty = empty.suffix(532.0);
		twice(() -> assertThat(suffixEmpty, contains(532.0)));

		DoubleSeq suffix = _123.suffix(532.0);
		twice(() -> assertThat(suffix, contains(1.0, 2.0, 3.0, 532.0)));
	}

	@Test
	public void interleave() {
		assertThat(empty.interleave(empty), is(emptyIterable()));
		assertThat(_123.interleave(_12345), contains(1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 5.0));
		assertThat(_12345.interleave(_123), contains(1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void reverse() {
		DoubleSeq emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		DoubleSeq oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, contains(1.0)));

		DoubleSeq twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, contains(2.0, 1.0)));

		DoubleSeq threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, contains(3.0, 2.0, 1.0)));

		DoubleSeq nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed, contains(9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0)));
	}

	@Test
	public void doubles() {
		assertThat(DoubleSeq.positive().limit(3.0), contains(1.0, 2.0, 3.0));
		assertThat(DoubleSeq.positive().limit(127).last(), is(OptionalDouble.of(127)));
	}

	@Test
	public void doublesStartingAt() {
		assertThat(DoubleSeq.startingAt(1.0).limit(3.0), contains(1.0, 2.0, 3.0));
		assertThat(DoubleSeq.startingAt(0x1400).limit(3).last(), is(OptionalDouble.of(0x1402)));
	}

	@Test
	public void doubleRange() {
		assertThat(DoubleSeq.range(1.0, 6.0), contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0));
		assertThat(DoubleSeq.range(6.0, 1.0), contains(6.0, 5.0, 4.0, 3.0, 2.0, 1.0));
		assertThat(DoubleSeq.range(1.0, 6.0).count(), is(6.0));
	}

	@Test
	public void toInts() {
		IntSeq empty = DoubleSeq.empty().toInts();
		twice(() -> assertThat(empty, is(emptyIterable())));

		IntSeq _0 = DoubleSeq.startingAt(0).limit(5).toInts();
		twice(() -> assertThat(_0, contains(0, 1, 2, 3, 4)));

		IntSeq _0_5 = DoubleSeq.startingAt(0.5).limit(5).toInts();
		twice(() -> assertThat(_0_5, contains(0, 1, 2, 3, 4)));

		IntSeq _0_9999 = DoubleSeq.startingAt(0.9999).limit(5).toInts();
		twice(() -> assertThat(_0_9999, contains(0, 1, 2, 3, 4)));

		IntSeq _1 = DoubleSeq.startingAt(1).limit(5).toInts();
		twice(() -> assertThat(_1, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void toLongs() {
		LongSeq empty = DoubleSeq.empty().toLongs();
		twice(() -> assertThat(empty, is(emptyIterable())));

		LongSeq _0 = DoubleSeq.startingAt(0).limit(5).toLongs();
		twice(() -> assertThat(_0, contains(0L, 1L, 2L, 3L, 4L)));

		LongSeq _0_5 = DoubleSeq.startingAt(0.5).limit(5).toLongs();
		twice(() -> assertThat(_0_5, contains(0L, 1L, 2L, 3L, 4L)));

		LongSeq _0_9999 = DoubleSeq.startingAt(0.9999).limit(5).toLongs();
		twice(() -> assertThat(_0_9999, contains(0L, 1L, 2L, 3L, 4L)));

		LongSeq _1 = DoubleSeq.startingAt(1).limit(5).toLongs();
		twice(() -> assertThat(_1, contains(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void toRoundedInts() {
		IntSeq empty = DoubleSeq.empty().toRoundedInts();
		twice(() -> assertThat(empty, is(emptyIterable())));

		IntSeq _1 = DoubleSeq.startingAt(1).limit(5).toRoundedInts();
		twice(() -> assertThat(_1, contains(1, 2, 3, 4, 5)));

		IntSeq _0_99999 = DoubleSeq.startingAt(0.99999).limit(5).toRoundedInts();
		twice(() -> assertThat(_0_99999, contains(1, 2, 3, 4, 5)));

		IntSeq _0_5 = DoubleSeq.startingAt(0.5).limit(5).toRoundedInts();
		twice(() -> assertThat(_0_5, contains(1, 2, 3, 4, 5)));

		IntSeq _0_49999 = DoubleSeq.startingAt(0.49999).limit(5).toRoundedInts();
		twice(() -> assertThat(_0_49999, contains(0, 1, 2, 3, 4)));

		IntSeq _0 = DoubleSeq.startingAt(0).limit(5).toRoundedInts();
		twice(() -> assertThat(_0, contains(0, 1, 2, 3, 4)));
	}

	@Test
	public void toRoundedLongs() {
		LongSeq empty = DoubleSeq.empty().toRoundedLongs();
		twice(() -> assertThat(empty, is(emptyIterable())));

		LongSeq _1 = DoubleSeq.startingAt(1).limit(5).toRoundedLongs();
		twice(() -> assertThat(_1, contains(1L, 2L, 3L, 4L, 5L)));

		LongSeq _0_99999 = DoubleSeq.startingAt(0.99999).limit(5).toRoundedLongs();
		twice(() -> assertThat(_0_99999, contains(1L, 2L, 3L, 4L, 5L)));

		LongSeq _0_5 = DoubleSeq.startingAt(0.5).limit(5).toRoundedLongs();
		twice(() -> assertThat(_0_5, contains(1L, 2L, 3L, 4L, 5L)));

		LongSeq _0_49999 = DoubleSeq.startingAt(0.49999).limit(5).toRoundedLongs();
		twice(() -> assertThat(_0_49999, contains(0L, 1L, 2L, 3L, 4L)));

		LongSeq _0 = DoubleSeq.startingAt(0).limit(5).toRoundedLongs();
		twice(() -> assertThat(_0, contains(0L, 1L, 2L, 3L, 4L)));
	}

	@Test
	public void toIntsMapped() {
		IntSeq empty = DoubleSeq.empty().toInts(d -> (int) (d * 2));
		twice(() -> assertThat(empty, is(emptyIterable())));

		IntSeq doubledHalves = DoubleSeq.range(0.5, 1.5, 0.5).toInts(d -> (int) (d * 2));
		twice(() -> assertThat(doubledHalves, contains(1, 2, 3)));
	}

	@Test
	public void toLongsMapped() {
		LongSeq empty = DoubleSeq.empty().toLongs(d -> (long) (d * 2));
		twice(() -> assertThat(empty, is(emptyIterable())));

		LongSeq doubledHalves = DoubleSeq.range(0.5, 1.5, 0.5).toLongs(d -> (long) (d * 2));
		twice(() -> assertThat(doubledHalves, contains(1L, 2L, 3L)));
	}

	@Test
	public void toSequence() {
		Sequence<Double> empty = DoubleSeq.empty().toSequence(d -> d + 1);
		twice(() -> assertThat(empty, is(emptyIterable())));

		Sequence<Double> doubles = DoubleSeq.startingAt(1).limit(5).toSequence(d -> d + 1);
		twice(() -> assertThat(doubles, contains(2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void box() {
		Sequence<Double> empty = DoubleSeq.empty().box();
		twice(() -> assertThat(empty, is(emptyIterable())));

		Sequence<Double> doubles = DoubleSeq.startingAt(1).limit(5).box();
		twice(() -> assertThat(doubles, contains(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void repeat() {
		DoubleSeq repeatEmpty = empty.repeat();
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		DoubleSeq repeatOne = _1.repeat();
		twice(() -> assertThat(repeatOne.limit(3), contains(1.0, 1.0, 1.0)));

		DoubleSeq repeatTwo = _12.repeat();
		twice(() -> assertThat(repeatTwo.limit(5), contains(1.0, 2.0, 1.0, 2.0, 1.0)));

		DoubleSeq repeatThree = _123.repeat();
		twice(() -> assertThat(repeatThree.limit(8), contains(1.0, 2.0, 3.0, 1.0, 2.0, 3.0, 1.0, 2.0)));

		DoubleSeq repeatVarying = DoubleSeq.from(new DoubleIterable() {
			private List<Double> list = asList(1.0, 2.0, 3.0);
			int end = list.size();

			@Override
			public DoubleIterator iterator() {
				List<Double> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Double> iterator = subList.iterator();
				return new DelegatingDoubleIterator<Double, Iterator<Double>>() {
					@Override
					public double nextDouble() {
						return iterator.next();
					}
				}.backedBy(iterator);
			}
		}).repeat();
		assertThat(repeatVarying, contains(1.0, 2.0, 3.0, 1.0, 2.0, 1.0));
	}

	@Test
	public void generate() {
		Queue<Double> queue = new ArrayDeque<>(asList(1.0, 2.0, 3.0, 4.0, 5.0));
		DoubleSeq sequence = DoubleSeq.generate(queue::poll).endingAt(5.0);

		assertThat(sequence, contains(1.0, 2.0, 3.0, 4.0, 5.0));
	}
}
