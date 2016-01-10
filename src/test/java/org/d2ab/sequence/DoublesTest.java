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

import org.d2ab.primitive.doubles.DoubleIterator;
import org.d2ab.utils.MoreArrays;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DoublesTest {
	private final Doubles empty = Doubles.empty();

	private final Doubles a = Doubles.of(1.0);
	private final Doubles ab = Doubles.of(1.0, 2.0);
	private final Doubles abc = Doubles.of(1.0, 2.0, 3.0);
	private final Doubles abcd = Doubles.of(1.0, 2.0, 3.0, 4.0);
	private final Doubles abcde = Doubles.of(1.0, 2.0, 3.0, 4.0, 5.0);
	private final Doubles abcdefghi = Doubles.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);

	private final Doubles oneRandom = Doubles.of(17.0);
	private final Doubles twoRandom = Doubles.of(17.0, 32.0);
	private final Doubles nineRandom = Doubles.of(6.0, 6.0, 1.0, 7.0, 1.0, 2.0, 17.0, 5.0, 4.0);

	@Test
	public void ofOne() throws Exception {
		twice(() -> assertThat(a, contains(1.0)));
	}

	@Test
	public void ofMany() throws Exception {
		twice(() -> assertThat(abc, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void forLoop() throws Exception {
		twice(() -> {
			for (double ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			double expected = 1.0;
			for (double i : abc)
				assertThat(i, is(expected++));
		});
	}

	@Test
	public void forEach() throws Exception {
		twice(() -> {
			empty.forEachDouble(c -> fail("Should not get called"));
			a.forEachDouble(c -> assertThat(c, is(in(singletonList(1.0)))));
			ab.forEachDouble(c -> assertThat(c, is(in(Arrays.asList(1.0, 2.0)))));
			abc.forEachDouble(c -> assertThat(c, is(in(Arrays.asList(1.0, 2.0, 3.0)))));
		});
	}

	@Test
	public void iterator() throws Exception {
		twice(() -> {
			DoubleIterator iterator = abc.iterator();

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
		Doubles sequence = Doubles.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() throws Exception {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void fromSequence() throws Exception {
		Doubles fromSequence = Doubles.from(abc);

		twice(() -> assertThat(fromSequence, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void fromIterable() throws Exception {
		Iterable<Double> iterable = () -> Arrays.asList(1.0, 2.0, 3.0).iterator();

		Doubles sequenceFromIterable = Doubles.from(iterable);

		twice(() -> assertThat(sequenceFromIterable, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void fromStream() throws Exception {
		Doubles sequenceFromStream = Doubles.from(Arrays.asList(1.0, 2.0, 3.0).stream());

		assertThat(sequenceFromStream, contains(1.0, 2.0, 3.0));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromEmptyStream() throws Exception {
		Doubles sequenceFromStream = Doubles.from(Stream.of());

		assertThat(sequenceFromStream, is(emptyIterable()));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromIteratorSupplier() throws Exception {
		Supplier<DoubleIterator> iterators = () -> DoubleIterator.from(Arrays.asList(1.0, 2.0, 3.0));

		Doubles sequenceFromIterators = Doubles.from(iterators);

		twice(() -> assertThat(sequenceFromIterators, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void skip() {
		Doubles skipNone = abc.skip(0.0);
		twice(() -> assertThat(skipNone, contains(1.0, 2.0, 3.0)));

		Doubles skipOne = abc.skip(1.0);
		twice(() -> assertThat(skipOne, contains(2.0, 3.0)));

		Doubles skipTwo = abc.skip(2.0);
		twice(() -> assertThat(skipTwo, contains(3.0)));

		Doubles skipThree = abc.skip(3.0);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		Doubles skipFour = abc.skip(4.0);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		Doubles limitNone = abc.limit(0.0);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		Doubles limitOne = abc.limit(1.0);
		twice(() -> assertThat(limitOne, contains(1.0)));

		Doubles limitTwo = abc.limit(2.0);
		twice(() -> assertThat(limitTwo, contains(1.0, 2.0)));

		Doubles limitThree = abc.limit(3.0);
		twice(() -> assertThat(limitThree, contains(1.0, 2.0, 3.0)));

		Doubles limitFour = abc.limit(4.0);
		twice(() -> assertThat(limitFour, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void append() {
		Doubles appended = abc.append(Doubles.of(4.0, 5.0, 6.0)).append(Doubles.of(7.0, 8.0));

		twice(() -> assertThat(appended, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)));
	}

	@Test
	public void appendIterator() {
		Doubles appended = abc.append(MoreArrays.iterator(4.0, 5.0, 6.0)).append(MoreArrays.iterator(7.0, 8.0));

		assertThat(appended, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
		assertThat(appended, contains(1.0, 2.0, 3.0));
	}

	@Test
	public void appendStream() {
		Doubles appended = abc.append(Stream.of(4.0, 5.0, 6.0)).append(Stream.of(7.0, 8.0));

		assertThat(appended, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));

		DoubleIterator iterator = appended.iterator();
		assertThat(iterator.nextDouble(), is(1.0)); // First three are ok
		assertThat(iterator.nextDouble(), is(2.0));
		assertThat(iterator.nextDouble(), is(3.0));

		expecting(NoSuchElementException.class, iterator::nextDouble); // Hitting Stream that is exhausted
	}

	@Test
	public void appendArray() {
		Doubles appended = abc.append(4.0, 5.0, 6.0).append(7.0, 8.0);

		twice(() -> assertThat(appended, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)));
	}

	@Test
	public void appendIsLazy() {
		DoubleIterator first = DoubleIterator.from(Arrays.asList(1.0, 2.0, 3.0));
		DoubleIterator second = DoubleIterator.from(Arrays.asList(4.0, 5.0, 6.0));
		DoubleIterator third = DoubleIterator.from(Arrays.asList(7.0, 8.0));

		Doubles then = Doubles.from(first).append(() -> second).append(() -> third);

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

		Doubles sequence = Doubles.from(first).append(() -> second);

		// check delayed iteration
		DoubleIterator iterator = sequence.iterator();
		assertThat(iterator.next(), is(1.0));
		assertThat(iterator.next(), is(2.0));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		Doubles filtered = Doubles.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0).filter(i -> (i % 2.0) == 0.0);

		twice(() -> assertThat(filtered, contains(2.0, 4.0, 6.0)));
	}

	@Test
	public void map() {
		Doubles mapped = abc.map(c -> c + 1.0);
		twice(() -> assertThat(mapped, contains(2.0, 3.0, 4.0)));
	}

	@Test
	public void recurse() {
		Doubles recursive = Doubles.recurse(1.0, i -> i + 1.0);
		twice(() -> assertThat(recursive.limit(10.0), contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)));
	}

	@Test
	public void recurseUntil() {
		Doubles until = Doubles.recurse(1.0, c -> c + 1.0).until(7.0);
		twice(() -> assertThat(until, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void recurseEndingAt() {
		Doubles endingAt = Doubles.recurse(1.0, c -> c + 1.0).endingAt(7.0);
		twice(() -> assertThat(endingAt, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0)));
	}

	@Test
	public void collect() {
		twice(() -> {
			StringBuilder builder = abc.collect(StringBuilder::new, StringBuilder::append);
			assertThat(builder.toString(), is("1.02.03.0"));
		});
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(Arrays.equals(abc.toArray(), new double[]{1.0, 2.0, 3.0}), is(true)));
	}

	@Test
	public void join() {
		twice(() -> assertThat(abc.join(", "), is("1.0, 2.0, 3.0")));
	}

	@Test
	public void joinWithPrefixAndSuffix() {
		twice(() -> assertThat(abc.join("<", ", ", ">"), is("<1.0, 2.0, 3.0>")));
	}

	@Test
	public void reduce() {
		DoubleBinaryOperator secondDouble = (c1, c2) -> c2;
		twice(() -> {
			assertThat(empty.reduce(secondDouble), is(OptionalDouble.empty()));
			assertThat(a.reduce(secondDouble), is(OptionalDouble.of(1.0)));
			assertThat(ab.reduce(secondDouble), is(OptionalDouble.of(2.0)));
			assertThat(abc.reduce(secondDouble), is(OptionalDouble.of(3.0)));
		});
	}

	@Test
	public void reduceWithIdentity() {
		DoubleBinaryOperator secondDouble = (c1, c2) -> c2;
		twice(() -> {
			assertThat(empty.reduce(17.0, secondDouble), is(17.0));
			assertThat(a.reduce(17.0, secondDouble), is(1.0));
			assertThat(ab.reduce(17.0, secondDouble), is(2.0));
			assertThat(abc.reduce(17.0, secondDouble), is(3.0));
		});
	}

	@Test
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(OptionalDouble.empty()));
			assertThat(a.first(), is(OptionalDouble.of(1.0)));
			assertThat(ab.first(), is(OptionalDouble.of(1.0)));
			assertThat(abc.first(), is(OptionalDouble.of(1.0)));
		});
	}

	@Test
	public void second() {
		twice(() -> {
			assertThat(empty.second(), is(OptionalDouble.empty()));
			assertThat(a.second(), is(OptionalDouble.empty()));
			assertThat(ab.second(), is(OptionalDouble.of(2.0)));
			assertThat(abc.second(), is(OptionalDouble.of(2.0)));
			assertThat(abcd.second(), is(OptionalDouble.of(2.0)));
		});
	}

	@Test
	public void third() {
		twice(() -> {
			assertThat(empty.third(), is(OptionalDouble.empty()));
			assertThat(a.third(), is(OptionalDouble.empty()));
			assertThat(ab.third(), is(OptionalDouble.empty()));
			assertThat(abc.third(), is(OptionalDouble.of(3.0)));
			assertThat(abcd.third(), is(OptionalDouble.of(3.0)));
			assertThat(abcde.third(), is(OptionalDouble.of(3.0)));
		});
	}

	@Test
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(OptionalDouble.empty()));
			assertThat(a.last(), is(OptionalDouble.of(1.0)));
			assertThat(ab.last(), is(OptionalDouble.of(2.0)));
			assertThat(abc.last(), is(OptionalDouble.of(3.0)));
		});
	}

	@Test
	public void step() {
		Doubles stepThree = abcdefghi.step(3.0);
		twice(() -> assertThat(stepThree, contains(1.0, 4.0, 7.0)));
	}

	@Test
	public void sorted() {
		Doubles emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		Doubles oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, contains(17.0)));

		Doubles twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, contains(17.0, 32.0)));

		Doubles nineSorted = nineRandom.sorted();
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
		twice(() -> assertThat(a.count(), is(1.0)));
		twice(() -> assertThat(ab.count(), is(2.0)));
		twice(() -> assertThat(abcdefghi.count(), is(9.0)));
	}

	@Test
	public void any() {
		twice(() -> assertThat(abc.any(x -> x > 0.0), is(true)));
		twice(() -> assertThat(abc.any(x -> x > 2.0), is(true)));
		twice(() -> assertThat(abc.any(x -> x > 4.0), is(false)));
	}

	@Test
	public void all() {
		twice(() -> assertThat(abc.all(x -> x > 0.0), is(true)));
		twice(() -> assertThat(abc.all(x -> x > 2.0), is(false)));
		twice(() -> assertThat(abc.all(x -> x > 4.0), is(false)));
	}

	@Test
	public void none() {
		twice(() -> assertThat(abc.none(x -> x > 0.0), is(false)));
		twice(() -> assertThat(abc.none(x -> x > 2.0), is(false)));
		twice(() -> assertThat(abc.none(x -> x > 4.0), is(true)));
	}

	@Test
	public void peek() {
		Doubles peek = abc.peek(x -> assertThat(x, is(both(greaterThan(0.0)).and(lessThan(4.0)))));
		twice(() -> assertThat(peek, contains(1.0, 2.0, 3.0)));
	}

	@Test
	public void prefix() {
		Doubles prefixEmpty = empty.prefix(327.0);
		twice(() -> assertThat(prefixEmpty, contains(327.0)));

		Doubles prefix = abc.prefix(327.0);
		twice(() -> assertThat(prefix, contains(327.0, 1.0, 2.0, 3.0)));
	}

	@Test
	public void suffix() {
		Doubles suffixEmpty = empty.suffix(532.0);
		twice(() -> assertThat(suffixEmpty, contains(532.0)));

		Doubles suffix = abc.suffix(532.0);
		twice(() -> assertThat(suffix, contains(1.0, 2.0, 3.0, 532.0)));
	}

	@Test
	public void interleave() {
		assertThat(empty.interleave(empty), is(emptyIterable()));
		assertThat(abc.interleave(abcde), contains(1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 5.0));
		assertThat(abcde.interleave(abc), contains(1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void reverse() {
		Doubles emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		Doubles oneReversed = a.reverse();
		twice(() -> assertThat(oneReversed, contains(1.0)));

		Doubles twoReversed = ab.reverse();
		twice(() -> assertThat(twoReversed, contains(2.0, 1.0)));

		Doubles threeReversed = abc.reverse();
		twice(() -> assertThat(threeReversed, contains(3.0, 2.0, 1.0)));

		Doubles nineReversed = abcdefghi.reverse();
		twice(() -> assertThat(nineReversed, contains(9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0)));
	}

	@Test
	public void doubles() {
		assertThat(Doubles.positive().limit(3.0), contains(1.0, 2.0, 3.0));
		assertThat(Doubles.positive().limit(127).last(), is(OptionalDouble.of(127)));
	}

	@Test
	public void doublesStartingAt() {
		assertThat(Doubles.startingAt(1.0).limit(3.0), contains(1.0, 2.0, 3.0));
		assertThat(Doubles.startingAt(0x1400).limit(3).last(), is(OptionalDouble.of(0x1402)));
	}

	@Test
	public void doubleRange() {
		assertThat(Doubles.range(1.0, 6.0), contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0));
		assertThat(Doubles.range(6.0, 1.0), contains(6.0, 5.0, 4.0, 3.0, 2.0, 1.0));
		assertThat(Doubles.range(1.0, 6.0).count(), is(6.0));
	}
}
