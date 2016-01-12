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

import org.d2ab.primitive.ints.DelegatingIntIterator;
import org.d2ab.primitive.ints.IntIterable;
import org.d2ab.primitive.ints.IntIterator;
import org.d2ab.utils.MoreArrays;
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

public class IntSeqTest {
	private final IntSeq empty = IntSeq.empty();

	private final IntSeq _1 = IntSeq.of(1);
	private final IntSeq _12 = IntSeq.of(1, 2);
	private final IntSeq _123 = IntSeq.of(1, 2, 3);
	private final IntSeq _1234 = IntSeq.of(1, 2, 3, 4);
	private final IntSeq _12345 = IntSeq.of(1, 2, 3, 4, 5);
	private final IntSeq _123456789 = IntSeq.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

	private final IntSeq oneRandom = IntSeq.of(17);
	private final IntSeq twoRandom = IntSeq.of(17, 32);
	private final IntSeq nineRandom = IntSeq.of(6, 6, 1, 7, 1, 2, 17, 5, 4);

	@Test
	public void ofOne() throws Exception {
		twice(() -> assertThat(_1, contains(1)));
	}

	@Test
	public void ofMany() throws Exception {
		twice(() -> assertThat(_123, contains(1, 2, 3)));
	}

	@Test
	public void forLoop() throws Exception {
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
	public void forEach() throws Exception {
		twice(() -> {
			empty.forEachInt(c -> fail("Should not get called"));
			_1.forEachInt(c -> assertThat(c, is(in(singletonList(1)))));
			_12.forEachInt(c -> assertThat(c, is(in(asList(1, 2)))));
			_123.forEachInt(c -> assertThat(c, is(in(asList(1, 2, 3)))));
		});
	}

	@Test
	public void iterator() throws Exception {
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
	public void ofNone() throws Exception {
		IntSeq sequence = IntSeq.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() throws Exception {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void fromSequence() throws Exception {
		IntSeq fromSequence = IntSeq.from(_123);

		twice(() -> assertThat(fromSequence, contains(1, 2, 3)));
	}

	@Test
	public void fromIterable() throws Exception {
		Iterable<Integer> iterable = () -> asList(1, 2, 3).iterator();

		IntSeq sequenceFromIterable = IntSeq.from(iterable);

		twice(() -> assertThat(sequenceFromIterable, contains(1, 2, 3)));
	}

	@Test
	public void fromStream() throws Exception {
		IntSeq sequenceFromStream = IntSeq.from(asList(1, 2, 3).stream());

		assertThat(sequenceFromStream, contains(1, 2, 3));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromEmptyStream() throws Exception {
		IntSeq sequenceFromStream = IntSeq.from(Stream.of());

		assertThat(sequenceFromStream, is(emptyIterable()));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromIteratorSupplier() throws Exception {
		Supplier<IntIterator> iterators = () -> IntIterator.from(asList(1, 2, 3));

		IntSeq sequenceFromIterators = IntSeq.from(iterators);

		twice(() -> assertThat(sequenceFromIterators, contains(1, 2, 3)));
	}

	@Test
	public void skip() {
		IntSeq skipNone = _123.skip(0);
		twice(() -> assertThat(skipNone, contains(1, 2, 3)));

		IntSeq skipOne = _123.skip(1);
		twice(() -> assertThat(skipOne, contains(2, 3)));

		IntSeq skipTwo = _123.skip(2);
		twice(() -> assertThat(skipTwo, contains(3)));

		IntSeq skipThree = _123.skip(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		IntSeq skipFour = _123.skip(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		IntSeq limitNone = _123.limit(0);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		IntSeq limitOne = _123.limit(1);
		twice(() -> assertThat(limitOne, contains(1)));

		IntSeq limitTwo = _123.limit(2);
		twice(() -> assertThat(limitTwo, contains(1, 2)));

		IntSeq limitThree = _123.limit(3);
		twice(() -> assertThat(limitThree, contains(1, 2, 3)));

		IntSeq limitFour = _123.limit(4);
		twice(() -> assertThat(limitFour, contains(1, 2, 3)));
	}

	@Test
	public void append() {
		IntSeq appended = _123.append(IntSeq.of(4, 5, 6)).append(IntSeq.of(7, 8));

		twice(() -> assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIterator() {
		IntSeq appended = _123.append(MoreArrays.iterator(4, 5, 6)).append(MoreArrays.iterator(7, 8));

		assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, contains(1, 2, 3));
	}

	@Test
	public void appendStream() {
		IntSeq appended = _123.append(Stream.of(4, 5, 6)).append(Stream.of(7, 8));

		assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8));

		IntIterator iterator = appended.iterator();
		assertThat(iterator.nextInt(), is(1)); // First three are ok
		assertThat(iterator.nextInt(), is(2));
		assertThat(iterator.nextInt(), is(3));

		expecting(NoSuchElementException.class, iterator::nextInt); // Hitting Stream that is exhausted
	}

	@Test
	public void appendArray() {
		IntSeq appended = _123.append(4, 5, 6).append(7, 8);

		twice(() -> assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIsLazy() {
		IntIterator first = IntIterator.from(asList(1, 2, 3));
		IntIterator second = IntIterator.from(asList(4, 5, 6));
		IntIterator third = IntIterator.from(asList(7, 8));

		IntSeq then = IntSeq.from(first).append(() -> second).append(() -> third);

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

		IntSeq sequence = IntSeq.from(first).append(() -> second);

		// check delayed iteration
		IntIterator iterator = sequence.iterator();
		assertThat(iterator.next(), is(1));
		assertThat(iterator.next(), is(2));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		IntSeq filtered = IntSeq.of(1, 2, 3, 4, 5, 6, 7).filter(i -> (i % 2) == 0);

		twice(() -> assertThat(filtered, contains(2, 4, 6)));
	}

	@Test
	public void map() {
		IntSeq mapped = _123.map(c -> c + 1);
		twice(() -> assertThat(mapped, contains(2, 3, 4)));
	}

	@Test
	public void recurse() {
		IntSeq recursive = IntSeq.recurse(1, i -> i + 1);
		twice(() -> assertThat(recursive.limit(10), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void recurseUntil() {
		IntSeq until = IntSeq.recurse(1, c -> c + 1).until(7);
		twice(() -> assertThat(until, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void recurseEndingAt() {
		IntSeq endingAt = IntSeq.recurse(1, c -> c + 1).endingAt(7);
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
		IntSeq stepThree = _123456789.step(3);
		twice(() -> assertThat(stepThree, contains(1, 4, 7)));
	}

	@Test
	public void distinct() {
		IntSeq emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		IntSeq oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, contains(17)));

		IntSeq twoDuplicatesDistinct = IntSeq.of(17, 17).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, contains(17)));

		IntSeq nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, contains(6, 1, 7, 2, 17, 5, 4)));
	}

	@Test
	public void sorted() {
		IntSeq emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		IntSeq oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, contains(17)));

		IntSeq twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, contains(17, 32)));

		IntSeq nineSorted = nineRandom.sorted();
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
		IntSeq peek = _123.peek(x -> assertThat(x, is(both(greaterThan(0)).and(lessThan(4)))));
		twice(() -> assertThat(peek, contains(1, 2, 3)));
	}

	@Test
	public void prefix() {
		IntSeq prefixEmpty = empty.prefix(327);
		twice(() -> assertThat(prefixEmpty, contains(327)));

		IntSeq prefix = _123.prefix(327);
		twice(() -> assertThat(prefix, contains(327, 1, 2, 3)));
	}

	@Test
	public void suffix() {
		IntSeq suffixEmpty = empty.suffix(532);
		twice(() -> assertThat(suffixEmpty, contains(532)));

		IntSeq suffix = _123.suffix(532);
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
		IntSeq emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		IntSeq oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, contains(1)));

		IntSeq twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, contains(2, 1)));

		IntSeq threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, contains(3, 2, 1)));

		IntSeq nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed, contains(9, 8, 7, 6, 5, 4, 3, 2, 1)));
	}

	@Test
	public void ints() {
		assertThat(IntSeq.positive().limit(3), contains(1, 2, 3));
		assertThat(IntSeq.positive().limit(127).last(), is(OptionalInt.of(127)));
		assertThat(IntSeq.positive().limit(1000000).count(), is(1000000L));
	}

	@Test
	public void intsStartingAt() {
		assertThat(IntSeq.startingAt(1).limit(3), contains(1, 2, 3));
		assertThat(IntSeq.startingAt(0x1400).limit(3).last(), is(OptionalInt.of(0x1402)));
		assertThat(IntSeq.startingAt(Integer.MAX_VALUE), contains(Integer.MAX_VALUE));
		assertThat(IntSeq.startingAt(Integer.MAX_VALUE - 32767).count(), is(32768L));
	}

	@Test
	public void intRange() {
		assertThat(IntSeq.range(1, 6), contains(1, 2, 3, 4, 5, 6));
		assertThat(IntSeq.range(6, 1), contains(6, 5, 4, 3, 2, 1));
		assertThat(IntSeq.range(1, 6).count(), is(6L));
	}

	@Test
	public void toChars() {
		CharSeq charSeq = IntSeq.startingAt('a').limit(5).toChars();
		twice(() -> assertThat(charSeq, contains('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void toLongs() {
		LongSeq longSeq = IntSeq.positive().limit(5).toLongs();
		twice(() -> assertThat(longSeq, contains(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void toDoubles() {
		DoubleSeq doubleSeq = IntSeq.positive().limit(5).toDoubles();
		twice(() -> assertThat(doubleSeq, contains(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void toCharsMapped() {
		CharSeq charSeq = IntSeq.positive().limit(5).toChars(i -> (char) (0x60 + i));
		twice(() -> assertThat(charSeq, contains('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void toLongsMapped() {
		long maxInt = Integer.MAX_VALUE;

		LongSeq longSeq = IntSeq.positive().limit(5).toLongs(i -> i * maxInt);
		twice(() -> assertThat(longSeq, contains(maxInt, 2L * maxInt, 3L * maxInt, 4L * maxInt, 5L * maxInt)));
	}

	@Test
	public void toDoublesMapped() {
		DoubleSeq doubleSeq = IntSeq.positive().limit(5).toDoubles(i -> i / 2.0);
		twice(() -> assertThat(doubleSeq, contains(0.5, 1.0, 1.5, 2.0, 2.5)));
	}

	@Test
	public void toSequence() {
		Sequence<Integer> empty = IntSeq.empty().toSequence(i -> i + 1);
		twice(() -> assertThat(empty, is(emptyIterable())));

		Sequence<Integer> ints = IntSeq.positive().limit(5).toSequence(i -> i + 1);
		twice(() -> assertThat(ints, contains(2, 3, 4, 5, 6)));
	}

	@Test
	public void box() {
		Sequence<Integer> empty = IntSeq.empty().box();
		twice(() -> assertThat(empty, is(emptyIterable())));

		Sequence<Integer> ints = IntSeq.positive().limit(5).box();
		twice(() -> assertThat(ints, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void repeat() {
		IntSeq repeatEmpty = empty.repeat();
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		IntSeq repeatOne = _1.repeat();
		twice(() -> assertThat(repeatOne.limit(3), contains(1, 1, 1)));

		IntSeq repeatTwo = _12.repeat();
		twice(() -> assertThat(repeatTwo.limit(5), contains(1, 2, 1, 2, 1)));

		IntSeq repeatThree = _123.repeat();
		twice(() -> assertThat(repeatThree.limit(8), contains(1, 2, 3, 1, 2, 3, 1, 2)));

		IntSeq repeatVarying = IntSeq.from(new IntIterable() {
			private List<Integer> list = asList(1, 2, 3);
			int end = list.size();

			@Override
			public IntIterator iterator() {
				List<Integer> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				Iterator<Integer> iterator = subList.iterator();
				return new DelegatingIntIterator<Integer, Iterator<Integer>>() {
					@Override
					public int nextInt() {
						return iterator.next();
					}
				}.backedBy(iterator);
			}
		}).repeat();
		assertThat(repeatVarying, contains(1, 2, 3, 1, 2, 1));
	}

	@Test
	public void generate() {
		Queue<Integer> queue = new ArrayDeque<>(asList(1, 2, 3, 4, 5));
		IntSeq sequence = IntSeq.generate(queue::poll).endingAt(5);

		assertThat(sequence, contains(1, 2, 3, 4, 5));
	}
}
