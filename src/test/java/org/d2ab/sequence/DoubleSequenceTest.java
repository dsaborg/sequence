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

import org.d2ab.iterable.doubles.DoubleIterable;
import org.d2ab.iterator.doubles.DelegatingDoubleIterator;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.util.Arrayz;
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

public class DoubleSequenceTest {
	private final DoubleSequence empty = DoubleSequence.empty();

	private final DoubleSequence _1 = DoubleSequence.of(1.0);
	private final DoubleSequence _12 = DoubleSequence.of(1.0, 2.0);
	private final DoubleSequence _123 = DoubleSequence.of(1.0, 2.0, 3.0);
	private final DoubleSequence _1234 = DoubleSequence.of(1.0, 2.0, 3.0, 4.0);
	private final DoubleSequence _12345 = DoubleSequence.of(1.0, 2.0, 3.0, 4.0, 5.0);
	private final DoubleSequence _123456789 = DoubleSequence.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);

	private final DoubleSequence oneRandom = DoubleSequence.of(17.0);
	private final DoubleSequence twoRandom = DoubleSequence.of(17.0, 32.0);
	private final DoubleSequence nineRandom = DoubleSequence.of(6.0, 6.0, 1.0, 7.0, 1.0, 2.0, 17.0, 5.0, 4.0);

	@Test
	public void ofOne() {
		twice(() -> assertThat(_1, contains(1.0)));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(_123, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void forLoop() {
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
	public void forEach() {
		twice(() -> {
			empty.forEachDouble(c -> fail("Should not get called"));
			_1.forEachDouble(c -> assertThat(c, is(in(singletonList(1.0)))));
			_12.forEachDouble(c -> assertThat(c, is(in(asList(1.0, 2.0)))));
			_123.forEachDouble(c -> assertThat(c, is(in(asList(1.0, 2.0, 3.0)))));
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
	public void fromSequence() {
		DoubleSequence fromSequence = DoubleSequence.from(_123);

		twice(() -> assertThat(fromSequence, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void fromIterable() {
		Iterable<Double> iterable = () -> asList(1.0, 2.0, 3.0).iterator();

		DoubleSequence sequenceFromIterable = DoubleSequence.from(iterable);

		twice(() -> assertThat(sequenceFromIterable, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void fromStream() {
		DoubleSequence sequenceFromStream = DoubleSequence.from(asList(1.0, 2.0, 3.0).stream());

		assertThat(sequenceFromStream, contains(1.0, 2.0, 3.0));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromEmptyStream() {
		DoubleSequence sequenceFromStream = DoubleSequence.from(Stream.of());

		assertThat(sequenceFromStream, is(emptyIterable()));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromIteratorSupplier() {
		Supplier<DoubleIterator> iterators = () -> DoubleIterator.from(asList(1.0, 2.0, 3.0));

		DoubleSequence sequenceFromIterators = DoubleSequence.from(iterators);

		twice(() -> assertThat(sequenceFromIterators, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void skip() {
		DoubleSequence skipNone = _123.skip(0);
		twice(() -> assertThat(skipNone, contains(1.0, 2.0, 3.0)));

		DoubleSequence skipOne = _123.skip(1);
		twice(() -> assertThat(skipOne, contains(2.0, 3.0)));

		DoubleSequence skipTwo = _123.skip(2);
		twice(() -> assertThat(skipTwo, contains(3.0)));

		DoubleSequence skipThree = _123.skip(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		DoubleSequence skipFour = _123.skip(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		DoubleSequence limitNone = _123.limit(0);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		DoubleSequence limitOne = _123.limit(1);
		twice(() -> assertThat(limitOne, contains(1.0)));

		DoubleSequence limitTwo = _123.limit(2);
		twice(() -> assertThat(limitTwo, contains(1.0, 2.0)));

		DoubleSequence limitThree = _123.limit(3);
		twice(() -> assertThat(limitThree, contains(1.0, 2.0, 3.0)));

		DoubleSequence limitFour = _123.limit(4);
		twice(() -> assertThat(limitFour, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void append() {
		DoubleSequence appended = _123.append(DoubleSequence.of(4.0, 5.0, 6.0)).append(DoubleSequence.of(7.0, 8.0));

		twice(() -> assertThat(appended, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)));
	}

	@Test
	public void appendIterator() {
		DoubleSequence appended = _123.append(Arrayz.iterator(4.0, 5.0, 6.0)).append(Arrayz.iterator(7.0, 8.0));

		assertThat(appended, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
		assertThat(appended, contains(1.0, 2.0, 3.0));
	}

	@Test
	public void appendStream() {
		DoubleSequence appended = _123.append(Stream.of(4.0, 5.0, 6.0)).append(Stream.of(7.0, 8.0));

		assertThat(appended, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));

		DoubleIterator iterator = appended.iterator();
		assertThat(iterator.nextDouble(), is(1.0)); // First three are ok
		assertThat(iterator.nextDouble(), is(2.0));
		assertThat(iterator.nextDouble(), is(3.0));

		expecting(NoSuchElementException.class, iterator::nextDouble); // Hitting Stream that is exhausted
	}

	@Test
	public void appendArray() {
		DoubleSequence appended = _123.append(4.0, 5.0, 6.0).append(7.0, 8.0);

		twice(() -> assertThat(appended, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)));
	}

	@Test
	public void appendIsLazy() {
		DoubleIterator first = DoubleIterator.from(asList(1.0, 2.0, 3.0));
		DoubleIterator second = DoubleIterator.from(asList(4.0, 5.0, 6.0));
		DoubleIterator third = DoubleIterator.from(asList(7.0, 8.0));

		DoubleSequence then = DoubleSequence.from(first).append(() -> second).append(() -> third);

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
		DoubleSequence filtered = DoubleSequence.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0).filter(i -> (i % 2.0) == 0.0);

		twice(() -> assertThat(filtered, contains(2.0, 4.0, 6.0)));
	}

	@Test
	public void map() {
		DoubleSequence mapped = _123.map(c -> c + 1.0);
		twice(() -> assertThat(mapped, contains(2.0, 3.0, 4.0)));
	}

	@Test
	public void recurse() {
		DoubleSequence recursive = DoubleSequence.recurse(1.0, i -> i + 1.0);
		twice(() -> assertThat(recursive.limit(10), contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)));
	}

	@Test
	public void untilTerminal() {
		DoubleSequence until = DoubleSequence.recurse(1, x -> x + 1).until(7.0, 0.1);
		twice(() -> assertThat(until, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void endingAtTerminal() {
		DoubleSequence endingAt = DoubleSequence.recurse(1, x -> x + 1).endingAt(7.0, 0.1);
		twice(() -> assertThat(endingAt, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0)));
	}

	@Test
	public void untilPredicate() {
		DoubleSequence until = DoubleSequence.recurse(1, x -> x + 1).until(d -> d == 7.0);
		twice(() -> assertThat(until, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void endingAtPredicate() {
		DoubleSequence endingAt = DoubleSequence.recurse(1, x -> x + 1).endingAt(d -> d == 7.0);
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
		DoubleSequence stepThree = _123456789.step(3);
		twice(() -> assertThat(stepThree, contains(1.0, 4.0, 7.0)));
	}

	@Test
	public void sorted() {
		DoubleSequence emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		DoubleSequence oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, contains(17.0)));

		DoubleSequence twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, contains(17.0, 32.0)));

		DoubleSequence nineSorted = nineRandom.sorted();
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
		twice(() -> assertThat(peek, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void prefix() {
		DoubleSequence prefixEmpty = empty.prefix(327.0);
		twice(() -> assertThat(prefixEmpty, contains(327.0)));

		DoubleSequence prefix = _123.prefix(327.0);
		twice(() -> assertThat(prefix, contains(327.0, 1.0, 2.0, 3.0)));
	}

	@Test
	public void suffix() {
		DoubleSequence suffixEmpty = empty.suffix(532.0);
		twice(() -> assertThat(suffixEmpty, contains(532.0)));

		DoubleSequence suffix = _123.suffix(532.0);
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
		DoubleSequence emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		DoubleSequence oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, contains(1.0)));

		DoubleSequence twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, contains(2.0, 1.0)));

		DoubleSequence threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, contains(3.0, 2.0, 1.0)));

		DoubleSequence nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed, contains(9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0)));
	}

	@Test
	public void steppingFrom() {
		assertThat(DoubleSequence.steppingFrom(1, 0.5).limit(3), contains(1.0, 1.5, 2.0));
		assertThat(DoubleSequence.steppingFrom(10000, 0.5).limit(3).last(), is(OptionalDouble.of(10001)));
	}

	@Test
	public void range() {
		assertThat(DoubleSequence.range(1, 6, 1, 0.1), contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0));
		assertThat(DoubleSequence.range(6, 1, 1, 0.1), contains(6.0, 5.0, 4.0, 3.0, 2.0, 1.0));
	}

	@Test
	public void toInts() {
		IntSequence empty = DoubleSequence.empty().toInts();
		twice(() -> assertThat(empty, is(emptyIterable())));

		IntSequence _0 = DoubleSequence.steppingFrom(0, 1).limit(5).toInts();
		twice(() -> assertThat(_0, contains(0, 1, 2, 3, 4)));

		IntSequence _0_5 = DoubleSequence.steppingFrom(0.5, 1).limit(5).toInts();
		twice(() -> assertThat(_0_5, contains(0, 1, 2, 3, 4)));

		IntSequence _0_9999 = DoubleSequence.steppingFrom(0.9999, 1).limit(5).toInts();
		twice(() -> assertThat(_0_9999, contains(0, 1, 2, 3, 4)));

		IntSequence _1 = DoubleSequence.steppingFrom(1, 1).limit(5).toInts();
		twice(() -> assertThat(_1, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void toLongs() {
		LongSequence empty = DoubleSequence.empty().toLongs();
		twice(() -> assertThat(empty, is(emptyIterable())));

		LongSequence _0 = DoubleSequence.steppingFrom(0, 1).limit(5).toLongs();
		twice(() -> assertThat(_0, contains(0L, 1L, 2L, 3L, 4L)));

		LongSequence _0_5 = DoubleSequence.steppingFrom(0.5, 1).limit(5).toLongs();
		twice(() -> assertThat(_0_5, contains(0L, 1L, 2L, 3L, 4L)));

		LongSequence _0_9999 = DoubleSequence.steppingFrom(0.9999, 1).limit(5).toLongs();
		twice(() -> assertThat(_0_9999, contains(0L, 1L, 2L, 3L, 4L)));

		LongSequence _1 = DoubleSequence.steppingFrom(1, 1).limit(5).toLongs();
		twice(() -> assertThat(_1, contains(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void toRoundedInts() {
		IntSequence empty = DoubleSequence.empty().toRoundedInts();
		twice(() -> assertThat(empty, is(emptyIterable())));

		IntSequence _1 = DoubleSequence.steppingFrom(1, 1).limit(5).toRoundedInts();
		twice(() -> assertThat(_1, contains(1, 2, 3, 4, 5)));

		IntSequence _0_99999 = DoubleSequence.steppingFrom(0.99999, 1).limit(5).toRoundedInts();
		twice(() -> assertThat(_0_99999, contains(1, 2, 3, 4, 5)));

		IntSequence _0_5 = DoubleSequence.steppingFrom(0.5, 1).limit(5).toRoundedInts();
		twice(() -> assertThat(_0_5, contains(1, 2, 3, 4, 5)));

		IntSequence _0_49999 = DoubleSequence.steppingFrom(0.49999, 1).limit(5).toRoundedInts();
		twice(() -> assertThat(_0_49999, contains(0, 1, 2, 3, 4)));

		IntSequence _0 = DoubleSequence.steppingFrom(0, 1).limit(5).toRoundedInts();
		twice(() -> assertThat(_0, contains(0, 1, 2, 3, 4)));
	}

	@Test
	public void toRoundedLongs() {
		LongSequence empty = DoubleSequence.empty().toRoundedLongs();
		twice(() -> assertThat(empty, is(emptyIterable())));

		LongSequence _1 = DoubleSequence.steppingFrom(1, 1).limit(5).toRoundedLongs();
		twice(() -> assertThat(_1, contains(1L, 2L, 3L, 4L, 5L)));

		LongSequence _0_99999 = DoubleSequence.steppingFrom(0.99999, 1).limit(5).toRoundedLongs();
		twice(() -> assertThat(_0_99999, contains(1L, 2L, 3L, 4L, 5L)));

		LongSequence _0_5 = DoubleSequence.steppingFrom(0.5, 1).limit(5).toRoundedLongs();
		twice(() -> assertThat(_0_5, contains(1L, 2L, 3L, 4L, 5L)));

		LongSequence _0_49999 = DoubleSequence.steppingFrom(0.49999, 1).limit(5).toRoundedLongs();
		twice(() -> assertThat(_0_49999, contains(0L, 1L, 2L, 3L, 4L)));

		LongSequence _0 = DoubleSequence.steppingFrom(0, 1).limit(5).toRoundedLongs();
		twice(() -> assertThat(_0, contains(0L, 1L, 2L, 3L, 4L)));
	}

	@Test
	public void toIntsMapped() {
		IntSequence empty = DoubleSequence.empty().toInts(d -> (int) (d * 2));
		twice(() -> assertThat(empty, is(emptyIterable())));

		IntSequence doubledHalves = DoubleSequence.range(0.5, 1.5, 0.5, 0.1).toInts(d -> (int) (d * 2));
		twice(() -> assertThat(doubledHalves, contains(1, 2, 3)));
	}

	@Test
	public void toLongsMapped() {
		LongSequence empty = DoubleSequence.empty().toLongs(d -> (long) (d * 2));
		twice(() -> assertThat(empty, is(emptyIterable())));

		LongSequence doubledHalves = DoubleSequence.range(0.5, 1.5, 0.5, 0.1).toLongs(d -> (long) (d * 2));
		twice(() -> assertThat(doubledHalves, contains(1L, 2L, 3L)));
	}

	@Test
	public void toSequence() {
		Sequence<Double> empty = DoubleSequence.empty().toSequence(d -> d + 1);
		twice(() -> assertThat(empty, is(emptyIterable())));

		Sequence<Double> doubles = DoubleSequence.steppingFrom(1, 1).limit(5).toSequence(d -> d + 1);
		twice(() -> assertThat(doubles, contains(2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void box() {
		Sequence<Double> empty = DoubleSequence.empty().box();
		twice(() -> assertThat(empty, is(emptyIterable())));

		Sequence<Double> doubles = DoubleSequence.steppingFrom(1, 1).limit(5).box();
		twice(() -> assertThat(doubles, contains(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void repeat() {
		DoubleSequence repeatEmpty = empty.repeat();
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		DoubleSequence repeatOne = _1.repeat();
		twice(() -> assertThat(repeatOne.limit(3), contains(1.0, 1.0, 1.0)));

		DoubleSequence repeatTwo = _12.repeat();
		twice(() -> assertThat(repeatTwo.limit(5), contains(1.0, 2.0, 1.0, 2.0, 1.0)));

		DoubleSequence repeatThree = _123.repeat();
		twice(() -> assertThat(repeatThree.limit(8), contains(1.0, 2.0, 3.0, 1.0, 2.0, 3.0, 1.0, 2.0)));

		DoubleSequence repeatVarying = DoubleSequence.from(new DoubleIterable() {
			private List<Double> list = asList(1.0, 2.0, 3.0);
			int end = list.size();

			@Override
			public DoubleIterator iterator() {
				List<Double> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Double> iterator = subList.iterator();
				return new DelegatingDoubleIterator<Double, Iterator<Double>>(iterator) {
					@Override
					public double nextDouble() {
						return iterator.next();
					}
				};
			}
		}).repeat();
		assertThat(repeatVarying, contains(1.0, 2.0, 3.0, 1.0, 2.0, 1.0));
	}

	@Test
	public void repeatTwice() {
		DoubleSequence repeatEmpty = empty.repeat(2);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		DoubleSequence repeatOne = _1.repeat(2);
		twice(() -> assertThat(repeatOne, contains(1.0, 1.0)));

		DoubleSequence repeatTwo = _12.repeat(2);
		twice(() -> assertThat(repeatTwo, contains(1.0, 2.0, 1.0, 2.0)));

		DoubleSequence repeatThree = _123.repeat(2);
		twice(() -> assertThat(repeatThree, contains(1.0, 2.0, 3.0, 1.0, 2.0, 3.0)));

		DoubleSequence repeatVarying = DoubleSequence.from(new DoubleIterable() {
			private List<Double> list = asList(1.0, 2.0, 3.0);
			int end = list.size();

			@Override
			public DoubleIterator iterator() {
				List<Double> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Double> iterator = subList.iterator();
				return new DelegatingDoubleIterator<Double, Iterator<Double>>(iterator) {
					@Override
					public double nextDouble() {
						return iterator.next();
					}
				};
			}
		}).repeat(2);
		assertThat(repeatVarying, contains(1.0, 2.0, 3.0, 1.0, 2.0));
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
		DoubleSequence sequence = DoubleSequence.generate(queue::poll).endingAt(5.0, 0.1);

		assertThat(sequence, contains(1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void mapBack() {
		twice(() -> assertThat(_123.mapBack(17.0, (p, x) -> p), contains(17.0, 1.0, 2.0)));
		twice(() -> assertThat(_123.mapBack(17.0, (p, x) -> x), contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void mapForward() {
		twice(() -> assertThat(_123.mapForward(17.0, (x, n) -> x), contains(1.0, 2.0, 3.0)));
		twice(() -> assertThat(_123.mapForward(17.0, (x, n) -> n), contains(2.0, 3.0, 17.0)));
	}
}
