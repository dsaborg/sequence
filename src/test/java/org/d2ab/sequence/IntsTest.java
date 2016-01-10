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

import org.d2ab.primitive.ints.IntIterator;
import org.d2ab.utils.MoreArrays;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class IntsTest {
	private final Ints empty = Ints.empty();

	private final Ints a = Ints.of(1);
	private final Ints ab = Ints.of(1, 2);
	private final Ints abc = Ints.of(1, 2, 3);
	private final Ints abcd = Ints.of(1, 2, 3, 4);
	private final Ints abcde = Ints.of(1, 2, 3, 4, 5);
	private final Ints abcdefghi = Ints.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

	private final Ints oneRandom = Ints.of(17);
	private final Ints twoRandom = Ints.of(17, 32);
	private final Ints nineRandom = Ints.of(6, 6, 1, 7, 1, 2, 17, 5, 4);

	@Test
	public void ofOne() throws Exception {
		twice(() -> assertThat(a, contains(1)));
	}

	@Test
	public void ofMany() throws Exception {
		twice(() -> assertThat(abc, contains(1, 2, 3)));
	}

	@Test
	public void forLoop() throws Exception {
		twice(() -> {
			for (int ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			int expected = 1;
			for (int i : abc)
				assertThat(i, is(expected++));
		});
	}

	@Test
	public void forEach() throws Exception {
		twice(() -> {
			empty.forEachInt(c -> fail("Should not get called"));
			a.forEachInt(c -> assertThat(c, is(in(singletonList(1)))));
			ab.forEachInt(c -> assertThat(c, is(in(Arrays.asList(1, 2)))));
			abc.forEachInt(c -> assertThat(c, is(in(Arrays.asList(1, 2, 3)))));
		});
	}

	@Test
	public void iterator() throws Exception {
		twice(() -> {
			IntIterator iterator = abc.iterator();

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
	public void ofNone() throws Exception {
		Ints sequence = Ints.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() throws Exception {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void fromSequence() throws Exception {
		Ints fromSequence = Ints.from(abc);

		twice(() -> assertThat(fromSequence, contains(1, 2, 3)));
	}

	@Test
	public void fromIterable() throws Exception {
		Iterable<Integer> iterable = () -> Arrays.asList(1, 2, 3).iterator();

		Ints sequenceFromIterable = Ints.from(iterable);

		twice(() -> assertThat(sequenceFromIterable, contains(1, 2, 3)));
	}

	@Test
	public void fromStream() throws Exception {
		Ints sequenceFromStream = Ints.from(Arrays.asList(1, 2, 3).stream());

		assertThat(sequenceFromStream, contains(1, 2, 3));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromEmptyStream() throws Exception {
		Ints sequenceFromStream = Ints.from(Stream.of());

		assertThat(sequenceFromStream, is(emptyIterable()));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromIteratorSupplier() throws Exception {
		Supplier<IntIterator> iterators = () -> IntIterator.from(Arrays.asList(1, 2, 3));

		Ints sequenceFromIterators = Ints.from(iterators);

		twice(() -> assertThat(sequenceFromIterators, contains(1, 2, 3)));
	}

	@Test
	public void skip() {
		Ints skipNone = abc.skip(0);
		twice(() -> assertThat(skipNone, contains(1, 2, 3)));

		Ints skipOne = abc.skip(1);
		twice(() -> assertThat(skipOne, contains(2, 3)));

		Ints skipTwo = abc.skip(2);
		twice(() -> assertThat(skipTwo, contains(3)));

		Ints skipThree = abc.skip(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		Ints skipFour = abc.skip(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		Ints limitNone = abc.limit(0);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		Ints limitOne = abc.limit(1);
		twice(() -> assertThat(limitOne, contains(1)));

		Ints limitTwo = abc.limit(2);
		twice(() -> assertThat(limitTwo, contains(1, 2)));

		Ints limitThree = abc.limit(3);
		twice(() -> assertThat(limitThree, contains(1, 2, 3)));

		Ints limitFour = abc.limit(4);
		twice(() -> assertThat(limitFour, contains(1, 2, 3)));
	}

	@Test
	public void append() {
		Ints appended = abc.append(Ints.of(4, 5, 6)).append(Ints.of(7, 8));

		twice(() -> assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIterator() {
		Ints appended = abc.append(MoreArrays.iterator(4, 5, 6)).append(MoreArrays.iterator(7, 8));

		assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, contains(1, 2, 3));
	}

	@Test
	public void appendStream() {
		Ints appended = abc.append(Stream.of(4, 5, 6)).append(Stream.of(7, 8));

		assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8));

		IntIterator iterator = appended.iterator();
		assertThat(iterator.nextInt(), is(1)); // First three are ok
		assertThat(iterator.nextInt(), is(2));
		assertThat(iterator.nextInt(), is(3));

		expecting(NoSuchElementException.class, iterator::nextInt); // Hitting Stream that is exhausted
	}

	@Test
	public void appendArray() {
		Ints appended = abc.append(4, 5, 6).append(7, 8);

		twice(() -> assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIsLazy() {
		IntIterator first = IntIterator.from(Arrays.asList(1, 2, 3));
		IntIterator second = IntIterator.from(Arrays.asList(4, 5, 6));
		IntIterator third = IntIterator.from(Arrays.asList(7, 8));

		Ints then = Ints.from(first).append(() -> second).append(() -> third);

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

		Ints sequence = Ints.from(first).append(() -> second);

		// check delayed iteration
		IntIterator iterator = sequence.iterator();
		assertThat(iterator.next(), is(1));
		assertThat(iterator.next(), is(2));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		Ints filtered = Ints.of(1, 2, 3, 4, 5, 6, 7).filter(i -> (i % 2) == 0);

		twice(() -> assertThat(filtered, contains(2, 4, 6)));
	}

	@Test
	public void map() {
		Ints mapped = abc.map(c -> c + 1);
		twice(() -> assertThat(mapped, contains(2, 3, 4)));
	}

	@Test
	public void recurse() {
		Ints recursive = Ints.recurse(1, i -> i + 1);
		twice(() -> assertThat(recursive.limit(10), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void recurseUntil() {
		Ints until = Ints.recurse(1, c -> c + 1).until(7);
		twice(() -> assertThat(until, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void recurseEndingAt() {
		Ints endingAt = Ints.recurse(1, c -> c + 1).endingAt(7);
		twice(() -> assertThat(endingAt, contains(1, 2, 3, 4, 5, 6, 7)));
	}

	@Test
	public void collect() {
		twice(() -> {
			StringBuilder builder = abc.collect(StringBuilder::new, StringBuilder::append);
			assertThat(builder.toString(), is("123"));
		});
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(Arrays.equals(abc.toArray(), new int[]{1, 2, 3}), is(true)));
	}

	@Test
	public void join() {
		twice(() -> assertThat(abc.join(", "), is("1, 2, 3")));
	}

	@Test
	public void joinWithPrefixAndSuffix() {
		twice(() -> assertThat(abc.join("<", ", ", ">"), is("<1, 2, 3>")));
	}

	@Test
	public void reduce() {
		IntBinaryOperator secondInt = (c1, c2) -> c2;
		twice(() -> {
			assertThat(empty.reduce(secondInt), is(OptionalInt.empty()));
			assertThat(a.reduce(secondInt), is(OptionalInt.of(1)));
			assertThat(ab.reduce(secondInt), is(OptionalInt.of(2)));
			assertThat(abc.reduce(secondInt), is(OptionalInt.of(3)));
		});
	}

	@Test
	public void reduceWithIdentity() {
		IntBinaryOperator secondInt = (c1, c2) -> c2;
		twice(() -> {
			assertThat(empty.reduce(17, secondInt), is(17));
			assertThat(a.reduce(17, secondInt), is(1));
			assertThat(ab.reduce(17, secondInt), is(2));
			assertThat(abc.reduce(17, secondInt), is(3));
		});
	}

	@Test
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(OptionalInt.empty()));
			assertThat(a.first(), is(OptionalInt.of(1)));
			assertThat(ab.first(), is(OptionalInt.of(1)));
			assertThat(abc.first(), is(OptionalInt.of(1)));
		});
	}

	@Test
	public void second() {
		twice(() -> {
			assertThat(empty.second(), is(OptionalInt.empty()));
			assertThat(a.second(), is(OptionalInt.empty()));
			assertThat(ab.second(), is(OptionalInt.of(2)));
			assertThat(abc.second(), is(OptionalInt.of(2)));
			assertThat(abcd.second(), is(OptionalInt.of(2)));
		});
	}

	@Test
	public void third() {
		twice(() -> {
			assertThat(empty.third(), is(OptionalInt.empty()));
			assertThat(a.third(), is(OptionalInt.empty()));
			assertThat(ab.third(), is(OptionalInt.empty()));
			assertThat(abc.third(), is(OptionalInt.of(3)));
			assertThat(abcd.third(), is(OptionalInt.of(3)));
			assertThat(abcde.third(), is(OptionalInt.of(3)));
		});
	}

	@Test
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(OptionalInt.empty()));
			assertThat(a.last(), is(OptionalInt.of(1)));
			assertThat(ab.last(), is(OptionalInt.of(2)));
			assertThat(abc.last(), is(OptionalInt.of(3)));
		});
	}

	@Test
	public void step() {
		Ints stepThree = abcdefghi.step(3);
		twice(() -> assertThat(stepThree, contains(1, 4, 7)));
	}

	@Test
	public void distinct() {
		Ints emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		Ints oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, contains(17)));

		Ints twoDuplicatesDistinct = Ints.of(17, 17).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, contains(17)));

		Ints nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, contains(6, 1, 7, 2, 17, 5, 4)));
	}

	@Test
	public void sorted() {
		Ints emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		Ints oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, contains(17)));

		Ints twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, contains(17, 32)));

		Ints nineSorted = nineRandom.sorted();
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
		twice(() -> assertThat(a.count(), is(1L)));
		twice(() -> assertThat(ab.count(), is(2L)));
		twice(() -> assertThat(abcdefghi.count(), is(9L)));
	}

	@Test
	public void any() {
		twice(() -> assertThat(abc.any(x -> x > 0), is(true)));
		twice(() -> assertThat(abc.any(x -> x > 2), is(true)));
		twice(() -> assertThat(abc.any(x -> x > 4), is(false)));
	}

	@Test
	public void all() {
		twice(() -> assertThat(abc.all(x -> x > 0), is(true)));
		twice(() -> assertThat(abc.all(x -> x > 2), is(false)));
		twice(() -> assertThat(abc.all(x -> x > 4), is(false)));
	}

	@Test
	public void none() {
		twice(() -> assertThat(abc.none(x -> x > 0), is(false)));
		twice(() -> assertThat(abc.none(x -> x > 2), is(false)));
		twice(() -> assertThat(abc.none(x -> x > 4), is(true)));
	}

	@Test
	public void peek() {
		Ints peek = abc.peek(x -> assertThat(x, is(both(greaterThan(0)).and(lessThan(4)))));
		twice(() -> assertThat(peek, contains(1, 2, 3)));
	}

	@Test
	public void prefix() {
		Ints prefixEmpty = empty.prefix(327);
		twice(() -> assertThat(prefixEmpty, contains(327)));

		Ints prefix = abc.prefix(327);
		twice(() -> assertThat(prefix, contains(327, 1, 2, 3)));
	}

	@Test
	public void suffix() {
		Ints suffixEmpty = empty.suffix(532);
		twice(() -> assertThat(suffixEmpty, contains(532)));

		Ints suffix = abc.suffix(532);
		twice(() -> assertThat(suffix, contains(1, 2, 3, 532)));
	}

	@Test
	public void interleave() {
		assertThat(empty.interleave(empty), is(emptyIterable()));
		assertThat(abc.interleave(abcde), contains(1, 1, 2, 2, 3, 3, 4, 5));
		assertThat(abcde.interleave(abc), contains(1, 1, 2, 2, 3, 3, 4, 5));
	}

	@Test
	public void reverse() {
		Ints emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		Ints oneReversed = a.reverse();
		twice(() -> assertThat(oneReversed, contains(1)));

		Ints twoReversed = ab.reverse();
		twice(() -> assertThat(twoReversed, contains(2, 1)));

		Ints threeReversed = abc.reverse();
		twice(() -> assertThat(threeReversed, contains(3, 2, 1)));

		Ints nineReversed = abcdefghi.reverse();
		twice(() -> assertThat(nineReversed, contains(9, 8, 7, 6, 5, 4, 3, 2, 1)));
	}

	@Test
	public void ints() {
		assertThat(Ints.positive().limit(3), contains(1, 2, 3));
		assertThat(Ints.positive().limit(127).last(), is(OptionalInt.of(127)));
	}

	@Test
	public void intsStartingAt() {
		assertThat(Ints.startingAt(1).limit(3), contains(1, 2, 3));
		assertThat(Ints.startingAt(0x1400).limit(3).last(), is(OptionalInt.of(0x1402)));
		assertThat(Ints.startingAt(Integer.MAX_VALUE), contains(Integer.MAX_VALUE));
		assertThat(Ints.startingAt(Integer.MAX_VALUE - 32767).count(), is(32768L));
	}

	@Test
	public void intRange() {
		assertThat(Ints.range(1, 6), contains(1, 2, 3, 4, 5, 6));
		assertThat(Ints.range(6, 1), contains(6, 5, 4, 3, 2, 1));
		assertThat(Ints.range(1, 6).count(), is(6L));
	}
}
