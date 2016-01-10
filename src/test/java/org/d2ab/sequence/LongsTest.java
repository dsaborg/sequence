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

import org.d2ab.primitive.longs.LongIterator;
import org.d2ab.utils.MoreArrays;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.OptionalLong;
import java.util.function.LongBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class LongsTest {
	private final Longs empty = Longs.empty();

	private final Longs a = Longs.of(1L);
	private final Longs ab = Longs.of(1L, 2L);
	private final Longs abc = Longs.of(1L, 2L, 3L);
	private final Longs abcd = Longs.of(1L, 2L, 3L, 4L);
	private final Longs abcde = Longs.of(1L, 2L, 3L, 4L, 5L);
	private final Longs abcdefghi = Longs.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);

	private final Longs oneRandom = Longs.of(17L);
	private final Longs twoRandom = Longs.of(17L, 32L);
	private final Longs nineRandom = Longs.of(6L, 6L, 1L, 7L, 1L, 2L, 17L, 5L, 4L);

	@Test
	public void ofOne() throws Exception {
		twice(() -> assertThat(a, contains(1L)));
	}

	@Test
	public void ofMany() throws Exception {
		twice(() -> assertThat(abc, contains(1L, 2L, 3L)));
	}

	@Test
	public void forLoop() throws Exception {
		twice(() -> {
			for (long ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			long expected = 1L;
			for (long i : abc)
				assertThat(i, is(expected++));
		});
	}

	@Test
	public void forEach() throws Exception {
		twice(() -> {
			empty.forEachLong(c -> fail("Should not get called"));
			a.forEachLong(c -> assertThat(c, is(in(singletonList(1L)))));
			ab.forEachLong(c -> assertThat(c, is(in(Arrays.asList(1L, 2L)))));
			abc.forEachLong(c -> assertThat(c, is(in(Arrays.asList(1L, 2L, 3L)))));
		});
	}

	@Test
	public void iterator() throws Exception {
		twice(() -> {
			LongIterator iterator = abc.iterator();

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
	public void ofNone() throws Exception {
		Longs sequence = Longs.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() throws Exception {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void fromSequence() throws Exception {
		Longs fromSequence = Longs.from(abc);

		twice(() -> assertThat(fromSequence, contains(1L, 2L, 3L)));
	}

	@Test
	public void fromIterable() throws Exception {
		Iterable<Long> iterable = () -> Arrays.asList(1L, 2L, 3L).iterator();

		Longs sequenceFromIterable = Longs.from(iterable);

		twice(() -> assertThat(sequenceFromIterable, contains(1L, 2L, 3L)));
	}

	@Test
	public void fromStream() throws Exception {
		Longs sequenceFromStream = Longs.from(Arrays.asList(1L, 2L, 3L).stream());

		assertThat(sequenceFromStream, contains(1L, 2L, 3L));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromEmptyStream() throws Exception {
		Longs sequenceFromStream = Longs.from(Stream.of());

		assertThat(sequenceFromStream, is(emptyIterable()));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromIteratorSupplier() throws Exception {
		Supplier<LongIterator> iterators = () -> LongIterator.from(Arrays.asList(1L, 2L, 3L));

		Longs sequenceFromIterators = Longs.from(iterators);

		twice(() -> assertThat(sequenceFromIterators, contains(1L, 2L, 3L)));
	}

	@Test
	public void skip() {
		Longs skipNone = abc.skip(0L);
		twice(() -> assertThat(skipNone, contains(1L, 2L, 3L)));

		Longs skipOne = abc.skip(1L);
		twice(() -> assertThat(skipOne, contains(2L, 3L)));

		Longs skipTwo = abc.skip(2L);
		twice(() -> assertThat(skipTwo, contains(3L)));

		Longs skipThree = abc.skip(3L);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		Longs skipFour = abc.skip(4L);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		Longs limitNone = abc.limit(0L);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		Longs limitOne = abc.limit(1L);
		twice(() -> assertThat(limitOne, contains(1L)));

		Longs limitTwo = abc.limit(2L);
		twice(() -> assertThat(limitTwo, contains(1L, 2L)));

		Longs limitThree = abc.limit(3L);
		twice(() -> assertThat(limitThree, contains(1L, 2L, 3L)));

		Longs limitFour = abc.limit(4L);
		twice(() -> assertThat(limitFour, contains(1L, 2L, 3L)));
	}

	@Test
	public void append() {
		Longs appended = abc.append(Longs.of(4L, 5L, 6L)).append(Longs.of(7L, 8L));

		twice(() -> assertThat(appended, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)));
	}

	@Test
	public void appendIterator() {
		Longs appended = abc.append(MoreArrays.iterator(4L, 5L, 6L)).append(MoreArrays.iterator(7L, 8L));

		assertThat(appended, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
		assertThat(appended, contains(1L, 2L, 3L));
	}

	@Test
	public void appendStream() {
		Longs appended = abc.append(Stream.of(4L, 5L, 6L)).append(Stream.of(7L, 8L));

		assertThat(appended, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));

		LongIterator iterator = appended.iterator();
		assertThat(iterator.nextLong(), is(1L)); // First three are ok
		assertThat(iterator.nextLong(), is(2L));
		assertThat(iterator.nextLong(), is(3L));

		expecting(NoSuchElementException.class, iterator::nextLong); // Hitting Stream that is exhausted
	}

	@Test
	public void appendArray() {
		Longs appended = abc.append(4L, 5L, 6L).append(7L, 8L);

		twice(() -> assertThat(appended, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)));
	}

	@Test
	public void appendIsLazy() {
		LongIterator first = LongIterator.from(Arrays.asList(1L, 2L, 3L));
		LongIterator second = LongIterator.from(Arrays.asList(4L, 5L, 6L));
		LongIterator third = LongIterator.from(Arrays.asList(7L, 8L));

		Longs then = Longs.from(first).append(() -> second).append(() -> third);

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

		Longs sequence = Longs.from(first).append(() -> second);

		// check delayed iteration
		LongIterator iterator = sequence.iterator();
		assertThat(iterator.next(), is(1L));
		assertThat(iterator.next(), is(2L));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		Longs filtered = Longs.of(1L, 2L, 3L, 4L, 5L, 6L, 7L).filter(i -> (i % 2L) == 0L);

		twice(() -> assertThat(filtered, contains(2L, 4L, 6L)));
	}

	@Test
	public void map() {
		Longs mapped = abc.map(c -> c + 1L);
		twice(() -> assertThat(mapped, contains(2L, 3L, 4L)));
	}

	@Test
	public void recurse() {
		Longs recursive = Longs.recurse(1L, i -> i + 1L);
		twice(() -> assertThat(recursive.limit(10L), contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)));
	}

	@Test
	public void recurseUntil() {
		Longs until = Longs.recurse(1L, c -> c + 1L).until(7L);
		twice(() -> assertThat(until, contains(1L, 2L, 3L, 4L, 5L, 6L)));
	}

	@Test
	public void recurseEndingAt() {
		Longs endingAt = Longs.recurse(1L, c -> c + 1L).endingAt(7L);
		twice(() -> assertThat(endingAt, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L)));
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
		twice(() -> assertThat(Arrays.equals(abc.toArray(), new long[]{1L, 2L, 3L}), is(true)));
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
		LongBinaryOperator secondLong = (c1, c2) -> c2;
		twice(() -> {
			assertThat(empty.reduce(secondLong), is(OptionalLong.empty()));
			assertThat(a.reduce(secondLong), is(OptionalLong.of(1L)));
			assertThat(ab.reduce(secondLong), is(OptionalLong.of(2L)));
			assertThat(abc.reduce(secondLong), is(OptionalLong.of(3L)));
		});
	}

	@Test
	public void reduceWithIdentity() {
		LongBinaryOperator secondLong = (c1, c2) -> c2;
		twice(() -> {
			assertThat(empty.reduce(17L, secondLong), is(17L));
			assertThat(a.reduce(17L, secondLong), is(1L));
			assertThat(ab.reduce(17L, secondLong), is(2L));
			assertThat(abc.reduce(17L, secondLong), is(3L));
		});
	}

	@Test
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(OptionalLong.empty()));
			assertThat(a.first(), is(OptionalLong.of(1L)));
			assertThat(ab.first(), is(OptionalLong.of(1L)));
			assertThat(abc.first(), is(OptionalLong.of(1L)));
		});
	}

	@Test
	public void second() {
		twice(() -> {
			assertThat(empty.second(), is(OptionalLong.empty()));
			assertThat(a.second(), is(OptionalLong.empty()));
			assertThat(ab.second(), is(OptionalLong.of(2L)));
			assertThat(abc.second(), is(OptionalLong.of(2L)));
			assertThat(abcd.second(), is(OptionalLong.of(2L)));
		});
	}

	@Test
	public void third() {
		twice(() -> {
			assertThat(empty.third(), is(OptionalLong.empty()));
			assertThat(a.third(), is(OptionalLong.empty()));
			assertThat(ab.third(), is(OptionalLong.empty()));
			assertThat(abc.third(), is(OptionalLong.of(3L)));
			assertThat(abcd.third(), is(OptionalLong.of(3L)));
			assertThat(abcde.third(), is(OptionalLong.of(3L)));
		});
	}

	@Test
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(OptionalLong.empty()));
			assertThat(a.last(), is(OptionalLong.of(1L)));
			assertThat(ab.last(), is(OptionalLong.of(2L)));
			assertThat(abc.last(), is(OptionalLong.of(3L)));
		});
	}

	@Test
	public void step() {
		Longs stepThree = abcdefghi.step(3L);
		twice(() -> assertThat(stepThree, contains(1L, 4L, 7L)));
	}

	@Test
	public void distinct() {
		Longs emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		Longs oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, contains(17L)));

		Longs twoDuplicatesDistinct = Longs.of(17L, 17L).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, contains(17L)));

		Longs nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, contains(6L, 1L, 7L, 2L, 17L, 5L, 4L)));
	}

	@Test
	public void sorted() {
		Longs emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		Longs oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, contains(17L)));

		Longs twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, contains(17L, 32L)));

		Longs nineSorted = nineRandom.sorted();
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
		twice(() -> assertThat(a.count(), is(1L)));
		twice(() -> assertThat(ab.count(), is(2L)));
		twice(() -> assertThat(abcdefghi.count(), is(9L)));
	}

	@Test
	public void any() {
		twice(() -> assertThat(abc.any(x -> x > 0L), is(true)));
		twice(() -> assertThat(abc.any(x -> x > 2L), is(true)));
		twice(() -> assertThat(abc.any(x -> x > 4L), is(false)));
	}

	@Test
	public void all() {
		twice(() -> assertThat(abc.all(x -> x > 0L), is(true)));
		twice(() -> assertThat(abc.all(x -> x > 2L), is(false)));
		twice(() -> assertThat(abc.all(x -> x > 4L), is(false)));
	}

	@Test
	public void none() {
		twice(() -> assertThat(abc.none(x -> x > 0L), is(false)));
		twice(() -> assertThat(abc.none(x -> x > 2L), is(false)));
		twice(() -> assertThat(abc.none(x -> x > 4L), is(true)));
	}

	@Test
	public void peek() {
		Longs peek = abc.peek(x -> assertThat(x, is(both(greaterThan(0L)).and(lessThan(4L)))));
		twice(() -> assertThat(peek, contains(1L, 2L, 3L)));
	}

	@Test
	public void prefix() {
		Longs prefixEmpty = empty.prefix(327L);
		twice(() -> assertThat(prefixEmpty, contains(327L)));

		Longs prefix = abc.prefix(327L);
		twice(() -> assertThat(prefix, contains(327L, 1L, 2L, 3L)));
	}

	@Test
	public void suffix() {
		Longs suffixEmpty = empty.suffix(532L);
		twice(() -> assertThat(suffixEmpty, contains(532L)));

		Longs suffix = abc.suffix(532L);
		twice(() -> assertThat(suffix, contains(1L, 2L, 3L, 532L)));
	}

	@Test
	public void interleave() {
		assertThat(empty.interleave(empty), is(emptyIterable()));
		assertThat(abc.interleave(abcde), contains(1L, 1L, 2L, 2L, 3L, 3L, 4L, 5L));
		assertThat(abcde.interleave(abc), contains(1L, 1L, 2L, 2L, 3L, 3L, 4L, 5L));
	}

	@Test
	public void reverse() {
		Longs emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		Longs oneReversed = a.reverse();
		twice(() -> assertThat(oneReversed, contains(1L)));

		Longs twoReversed = ab.reverse();
		twice(() -> assertThat(twoReversed, contains(2L, 1L)));

		Longs threeReversed = abc.reverse();
		twice(() -> assertThat(threeReversed, contains(3L, 2L, 1L)));

		Longs nineReversed = abcdefghi.reverse();
		twice(() -> assertThat(nineReversed, contains(9L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L)));
	}

	@Test
	public void longs() {
		assertThat(Longs.all().limit(3L), contains(1L, 2L, 3L));
		assertThat(Longs.all().limit(127).last(), is(OptionalLong.of(127)));
	}

	@Test
	public void longsStartingAt() {
		assertThat(Longs.startingAt(1L).limit(3L), contains(1L, 2L, 3L));
		assertThat(Longs.startingAt('\u1400').limit(3L).last(), is(OptionalLong.of('\u1402')));
		assertThat(Longs.startingAt(Long.MAX_VALUE), contains(Long.MAX_VALUE));
		assertThat(Longs.startingAt(Long.MAX_VALUE - 32767).count(), is(32768L));
	}

	@Test
	public void longRange() {
		assertThat(Longs.range(1L, 6L), contains(1L, 2L, 3L, 4L, 5L, 6L));
		assertThat(Longs.range(6L, 1L), contains(6L, 5L, 4L, 3L, 2L, 1L));
		assertThat(Longs.range(1L, 6L).count(), is(6L));
	}
}
