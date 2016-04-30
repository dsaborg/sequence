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

import org.d2ab.collection.Maps;
import org.d2ab.iterable.Iterables;
import org.d2ab.iterator.Iterators;
import org.d2ab.util.Pair;
import org.junit.Test;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.IsIterableBeginningWith.beginsWith;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SequenceTest {
	private final Sequence<Integer> empty = Sequence.from(new ArrayDeque<>());
	private final Sequence<Integer> _1 = Sequence.from(new ArrayDeque<>(asList(1)));
	private final Sequence<Integer> _12 = Sequence.from(new ArrayDeque<>(asList(1, 2)));
	private final Sequence<Integer> _123 = Sequence.from(new ArrayDeque<>(asList(1, 2, 3)));
	private final Sequence<Integer> _1234 = Sequence.from(new ArrayDeque<>(asList(1, 2, 3, 4)));
	private final Sequence<Integer> _12345 = Sequence.from(new ArrayDeque<>(asList(1, 2, 3, 4, 5)));
	private final Sequence<Integer> _123456789 = Sequence.from(new ArrayDeque<>(asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	private final Sequence<Integer> oneRandom = Sequence.from(new ArrayDeque<>(asList(17)));
	private final Sequence<Integer> twoRandom = Sequence.from(new ArrayDeque<>(asList(17, 32)));
	private final Sequence<Integer> threeRandom = Sequence.from(new ArrayDeque<>(asList(2, 3, 1)));
	private final Sequence<Integer> nineRandom = Sequence.from(new ArrayDeque<>(asList(67, 5, 43, 3, 5, 7, 24, 5,
	                                                                                   67)));

	@Test
	public void ofOne() {
		Sequence<Integer> sequence = Sequence.of(1);
		twice(() -> assertThat(sequence, contains(1)));
	}

	@Test
	public void ofMany() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void forLoop() {
		twice(() -> {
			for (int ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			int expected = 1;
			for (int i : _12345)
				assertThat(i, is(expected++));
		});
	}

	@Test
	public void forEach() {
		twice(() -> {
			empty.forEach(i -> fail("Should not get called"));

			AtomicInteger value = new AtomicInteger(1);
			_1.forEach(i -> assertThat(i, is(value.getAndIncrement())));

			value.set(1);
			_12.forEach(i -> assertThat(i, is(value.getAndIncrement())));

			value.set(1);
			_12345.forEach(i -> assertThat(i, is(value.getAndIncrement())));
		});
	}

	@Test
	public void iterator() {
		twice(() -> {
			Iterator iterator = _123.iterator();

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(1));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(2));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(3));

			assertThat(iterator.hasNext(), is(false));
			assertThat(iterator.hasNext(), is(false));
		});
	}

	@Test
	public void ofNone() {
		Sequence<Integer> sequence = Sequence.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void fromNulls() {
		Sequence<Integer> sequence = Sequence.from(Iterables.of(1, null, 2, 3, null));

		twice(() -> assertThat(sequence, contains(1, null, 2, 3, null)));
	}

	@Test
	public void fromIterable() {
		Sequence<Integer> sequenceFromIterable = Sequence.from(Iterables.of(1, 2, 3));

		twice(() -> assertThat(sequenceFromIterable, contains(1, 2, 3)));
	}

	@Test
	public void onceIterator() {
		Sequence<Integer> sequenceFromStream = Sequence.once(Iterators.of(1, 2, 3));

		assertThat(sequenceFromStream, contains(1, 2, 3));
		assertThat(sequenceFromStream, is(emptyIterable()));
	}

	@Test
	public void onceStream() {
		Sequence<Integer> sequenceFromStream = Sequence.once(Stream.of(1, 2, 3));

		assertThat(sequenceFromStream, contains(1, 2, 3));
		assertThat(sequenceFromStream, is(emptyIterable()));
	}

	@Test
	public void onceEmptyStream() {
		Sequence<Integer> sequenceFromStream = Sequence.once(Stream.of());

		twice(() -> assertThat(sequenceFromStream, is(emptyIterable())));
	}

	@Test
	public void concatArrayOfIterables() {
		Sequence<Integer> sequenceFromIterables = Sequence.concat(Iterables.of(1, 2, 3), Iterables.of(4, 5, 6),
		                                                          Iterables.of(7, 8, 9));

		twice(() -> assertThat(sequenceFromIterables, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void concatArrayOfLists() {
		Sequence<Integer> sequenceFromIterables = Sequence.concat(asList(1, 2, 3), asList(4, 5, 6), asList(7, 8, 9));

		twice(() -> assertThat(sequenceFromIterables, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void concatArrayOfNoIterables() {
		Sequence<Integer> sequenceFromNoIterables = Sequence.concat();

		twice(() -> assertThat(sequenceFromNoIterables, is(emptyIterable())));
	}

	@Test
	public void concatIterableOfIterables() {
		Sequence<Integer> sequenceFromIterables = Sequence.concat(Iterables.of(Iterables.of(1, 2, 3),
		                                                                       Iterables.of(4, 5, 6),
		                                                                       Iterables.of(7, 8, 9)));

		twice(() -> assertThat(sequenceFromIterables, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void concatIterableOfLists() {
		Sequence<Integer> sequenceFromIterables =
				Sequence.concat(Iterables.of(asList(1, 2, 3), asList(4, 5, 6), asList(7, 8, 9)));

		twice(() -> assertThat(sequenceFromIterables, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void concatIterableOfNoIterables() {
		Sequence<Integer> sequenceFromNoIterables = Sequence.concat(Iterables.of());

		twice(() -> assertThat(sequenceFromNoIterables, is(emptyIterable())));
	}

	@Test
	public void cacheCollection() {
		List<Integer> list = new ArrayList<>(asList(1, 2, 3, 4, 5));
		Sequence<Integer> cached = Sequence.cache(list);
		list.set(0, 17);

		twice(() -> assertThat(cached, contains(1, 2, 3, 4, 5)));

		cached.removeAll();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void cacheIterable() {
		List<Integer> list = new ArrayList<>(asList(1, 2, 3, 4, 5));
		Sequence<Integer> cached = Sequence.cache(list::iterator);
		list.set(0, 17);

		twice(() -> assertThat(cached, contains(1, 2, 3, 4, 5)));

		cached.removeAll();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void cacheIterator() {
		List<Integer> list = new ArrayList<>(asList(1, 2, 3, 4, 5));
		Sequence<Integer> cached = Sequence.cache(list.iterator());
		list.set(0, 17);

		twice(() -> assertThat(cached, contains(1, 2, 3, 4, 5)));

		cached.removeAll();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void cacheStream() {
		List<Integer> list = new ArrayList<>(asList(1, 2, 3, 4, 5));
		Sequence<Integer> cached = Sequence.cache(list.stream());
		list.set(0, 17);

		twice(() -> assertThat(cached, contains(1, 2, 3, 4, 5)));

		cached.removeAll();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void skip() {
		Sequence<Integer> skipNone = _123.skip(0);
		twice(() -> assertThat(skipNone, contains(1, 2, 3)));

		Sequence<Integer> skipOne = _123.skip(1);
		twice(() -> assertThat(skipOne, contains(2, 3)));

		Sequence<Integer> skipTwo = _123.skip(2);
		twice(() -> assertThat(skipTwo, contains(3)));

		Sequence<Integer> skipThree = _123.skip(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		Sequence<Integer> skipFour = _123.skip(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void skipTail() {
		Sequence<Integer> skipNone = _123.skipTail(0);
		twice(() -> assertThat(skipNone, contains(1, 2, 3)));

		Sequence<Integer> skipOne = _123.skipTail(1);
		twice(() -> assertThat(skipOne, contains(1, 2)));

		Sequence<Integer> skipTwo = _123.skipTail(2);
		twice(() -> assertThat(skipTwo, contains(1)));

		Sequence<Integer> skipThree = _123.skipTail(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		Sequence<Integer> skipFour = _123.skipTail(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		Sequence<Integer> limitNone = _123.limit(0);
		twice(() -> assertThat(limitNone, is(emptyIterable())));

		Sequence<Integer> limitOne = _123.limit(1);
		twice(() -> assertThat(limitOne, contains(1)));

		Sequence<Integer> limitTwo = _123.limit(2);
		twice(() -> assertThat(limitTwo, contains(1, 2)));

		Sequence<Integer> limitThree = _123.limit(3);
		twice(() -> assertThat(limitThree, contains(1, 2, 3)));

		Sequence<Integer> limitFour = _123.limit(4);
		twice(() -> assertThat(limitFour, contains(1, 2, 3)));
	}

	@Test
	public void append() {
		Sequence<Integer> appended = _123.append(Sequence.from(new ArrayDeque<>(asList(4, 5, 6))))
		                                 .append(Sequence.from(new ArrayDeque<>(asList(7, 8))));

		twice(() -> assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIterator() {
		Sequence<Integer> appended = _123.append(Iterators.of(4, 5, 6)).append(Iterators.of(7, 8));

		assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, contains(1, 2, 3));
	}

	@Test
	public void appendStream() {
		Sequence<Integer> appended = _123.append(Stream.of(4, 5, 6)).append(Stream.of(7, 8));

		assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, contains(1, 2, 3));
	}

	@Test
	public void appendArray() {
		Sequence<Integer> appended = _123.append(4, 5, 6).append(7, 8);

		twice(() -> assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIsLazy() {
		Iterator<Integer> first = Iterators.of(1, 2, 3);
		Iterator<Integer> second = Iterators.of(4, 5, 6);
		Iterator<Integer> third = Iterators.of(7, 8);

		Sequence.once(first).append(() -> second).append(() -> third);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));
		assertThat(third.hasNext(), is(true));
	}

	@Test
	public void appendIsLazyWhenSkippingHasNext() {
		Iterator<Integer> first = Iterators.of(1);
		Iterator<Integer> second = Iterators.of(2);

		Sequence<Integer> sequence = Sequence.once(first).append(() -> second);

		// check delayed iteration
		Iterator<Integer> iterator = sequence.iterator();
		assertThat(iterator.next(), is(1));
		assertThat(iterator.next(), is(2));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		Sequence<Integer> filtered = _123456789.filter(i -> (i % 2) == 0);

		twice(() -> assertThat(filtered, contains(2, 4, 6, 8)));
	}

	@Test
	public void filterBack() {
		Sequence<Integer> filteredLess = nineRandom.filterBack((p, i) -> p == null || p < i);
		twice(() -> assertThat(filteredLess, contains(67, 43, 5, 7, 24, 67)));

		Sequence<Integer> filteredGreater = nineRandom.filterBack((p, i) -> p == null || p > i);
		twice(() -> assertThat(filteredGreater, contains(67, 5, 3, 5)));
	}

	@Test
	public void filterBackWithReplacement() {
		Sequence<Integer> filteredLess = nineRandom.filterBack(117, (p, i) -> p < i);
		twice(() -> assertThat(filteredLess, contains(43, 5, 7, 24, 67)));

		Sequence<Integer> filteredGreater = nineRandom.filterBack(117, (p, i) -> p > i);
		twice(() -> assertThat(filteredGreater, contains(67, 5, 3, 5)));
	}

	@Test
	public void filterForward() {
		Sequence<Integer> filteredLess = nineRandom.filterForward((i, f) -> f == null || f < i);
		twice(() -> assertThat(filteredLess, contains(67, 43, 24, 67)));

		Sequence<Integer> filteredGreater = nineRandom.filterForward((i, f) -> f == null || f > i);
		twice(() -> assertThat(filteredGreater, contains(5, 3, 5, 7, 5, 67)));
	}

	@Test
	public void filterForwardWithReplacement() {
		Sequence<Integer> filteredLess = nineRandom.filterForward(117, (i, f) -> f < i);
		twice(() -> assertThat(filteredLess, contains(67, 43, 24)));

		Sequence<Integer> filteredGreater = nineRandom.filterForward(117, (i, f) -> f > i);
		twice(() -> assertThat(filteredGreater, contains(5, 3, 5, 7, 5, 67)));
	}

	@Test
	public void includingArray() {
		Sequence<Integer> emptyIncluding = empty.including(1, 3, 5, 17);
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));

		Sequence<Integer> including = _12345.including(1, 3, 5, 17);
		twice(() -> assertThat(including, contains(1, 3, 5)));

		Sequence<Integer> includingAll = _12345.including(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(includingAll, contains(1, 2, 3, 4, 5)));

		Sequence<Integer> includingNone = _12345.including();
		twice(() -> assertThat(includingNone, is(emptyIterable())));
	}

	@Test
	public void includingIterable() {
		Sequence<Integer> emptyIncluding = empty.including(Iterables.of(1, 3, 5, 17));
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));

		Sequence<Integer> including = _12345.including(Iterables.of(1, 3, 5, 17));
		twice(() -> assertThat(including, contains(1, 3, 5)));

		Sequence<Integer> includingAll = _12345.including(Iterables.of(1, 2, 3, 4, 5, 17));
		twice(() -> assertThat(includingAll, contains(1, 2, 3, 4, 5)));

		Sequence<Integer> includingNone = _12345.including(Iterables.of());
		twice(() -> assertThat(includingNone, is(emptyIterable())));
	}

	@Test
	public void excludingArray() {
		Sequence<Integer> emptyExcluding = empty.excluding(1, 3, 5, 17);
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));

		Sequence<Integer> excluding = _12345.excluding(1, 3, 5, 17);
		twice(() -> assertThat(excluding, contains(2, 4)));

		Sequence<Integer> excludingAll = _12345.excluding(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		Sequence<Integer> excludingNone = _12345.excluding();
		twice(() -> assertThat(excludingNone, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void excludingIterable() {
		Sequence<Integer> emptyExcluding = empty.excluding(Iterables.of(1, 3, 5, 17));
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));

		Sequence<Integer> excluding = _12345.excluding(Iterables.of(1, 3, 5, 17));
		twice(() -> assertThat(excluding, contains(2, 4)));

		Sequence<Integer> excludingAll = _12345.excluding(Iterables.of(1, 2, 3, 4, 5, 17));
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		Sequence<Integer> excludingNone = _12345.excluding(Iterables.of());
		twice(() -> assertThat(excludingNone, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void flatMapIterables() {
		Sequence<List<Integer>> sequence = Sequence.from(asList(asList(1, 2), asList(3, 4), asList(5, 6)));

		Function<List<Integer>, List<Integer>> identity = Function.identity();
		Sequence<Integer> flatMap = sequence.flatten(identity);

		twice(() -> assertThat(flatMap, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void flatMapLazy() {
		Function<Iterable<Integer>, Iterable<Integer>> identity = Function.identity();

		Sequence<Integer> flatMap = Sequence.from(new ArrayDeque<>(asList(asList(1, 2), (Iterable<Integer>) () -> {
			throw new IllegalStateException();
		}))).flatten(identity);

		twice(() -> {
			Iterator<Integer> iterator = flatMap.iterator(); // ISE if not lazy - expected later below
			assertThat(iterator.next(), is(1));
			assertThat(iterator.next(), is(2));
			expecting(IllegalStateException.class, iterator::next);
		});
	}

	@Test
	public void flatMapIterators() {
		Sequence<Iterator<Integer>> sequence =
				Sequence.from(new ArrayDeque<>(asList(Iterators.of(1, 2), Iterators.of(3, 4), Iterators.of(5, 6))));

		Sequence<Integer> flatMap = sequence.flatten(Sequence::once);

		assertThat(flatMap, contains(1, 2, 3, 4, 5, 6));
		assertThat(flatMap, is(emptyIterable()));
	}

	@Test
	public void flatMapArrays() {
		Sequence<Integer[]> sequence =
				Sequence.from(new ArrayDeque<>(asList(new Integer[]{1, 2}, new Integer[]{3, 4}, new Integer[]{5, 6})));

		Sequence<Integer> flatMap = sequence.flatten(Sequence::of);

		twice(() -> assertThat(flatMap, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void flattenIterables() {
		Sequence<Integer> flattened =
				Sequence.from(new ArrayDeque<>(asList(Iterables.of(1, 2), Iterables.of(3, 4), Iterables.of(5, 6))))
				        .flatten();

		twice(() -> assertThat(flattened, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void flattenLazy() {
		Sequence<Integer> flattened = Sequence.from(new ArrayDeque<>(asList(asList(1, 2), (Iterable<Integer>) () -> {
			throw new IllegalStateException();
		}))).flatten();

		twice(() -> {
			// IllegalStateException if not lazy - see below
			Iterator<Integer> iterator = flattened.iterator();
			assertThat(iterator.next(), is(1));
			assertThat(iterator.next(), is(2));
			expecting(IllegalStateException.class, iterator::next);
		});
	}

	@Test
	public void flattenIterators() {
		Sequence<Iterator<Integer>> sequence =
				Sequence.from(new ArrayDeque<>(asList(Iterators.of(1, 2), Iterators.of(3, 4), Iterators.of(5, 6))));
		Sequence<Integer> flattened = sequence.flatten();
		assertThat(flattened, contains(1, 2, 3, 4, 5, 6));
		assertThat(flattened, is(emptyIterable()));
	}

	@Test
	public void flattenArrays() {
		Sequence<Integer[]> sequence =
				Sequence.from(new ArrayDeque<>(asList(new Integer[]{1, 2}, new Integer[]{3, 4}, new Integer[]{5, 6})));

		Sequence<Integer> flattened = sequence.flatten();
		twice(() -> assertThat(flattened, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void flattenPairs() {
		Sequence<Pair<String, Integer>> sequence =
				Sequence.from(new ArrayDeque<>(asList(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));

		Sequence<Object> flattened = sequence.flatten();
		twice(() -> assertThat(flattened, contains("1", 1, "2", 2, "3", 3)));
	}

	@Test
	public void flattenEntries() {
		Sequence<Entry<String, Integer>> sequence =
				Sequence.from(new ArrayDeque<>(asList(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3))));

		Sequence<Object> flattened = sequence.flatten();
		twice(() -> assertThat(flattened, contains("1", 1, "2", 2, "3", 3)));
	}

	@Test
	public void map() {
		Sequence<String> mapped = _12345.map(Object::toString);
		twice(() -> assertThat(mapped, contains("1", "2", "3", "4", "5")));
	}

	@Test
	public void mapIsLazy() {
		Sequence<Integer> sequence = Sequence.from((Iterable<Integer>) () -> asList(1, null).iterator());

		Sequence<String> mapped = sequence.map(Object::toString);

		twice(() -> {
			Iterator<String> iterator = mapped.iterator(); // NPE here if not lazy
			assertThat(iterator.next(), is("1"));
			expecting(NullPointerException.class, iterator::next);
		});
	}

	@Test
	public void mapBack() {
		Sequence<Integer> mapBackPrevious = _12345.mapBack((p, n) -> p);
		twice(() -> assertThat(mapBackPrevious, contains(null, 1, 2, 3, 4)));

		Sequence<Integer> mapBack = _12345.mapBack((p, n) -> n);
		twice(() -> assertThat(mapBack, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void mapForward() {
		Sequence<Integer> mapForward = _12345.mapForward((c, n) -> c);
		twice(() -> assertThat(mapForward, contains(1, 2, 3, 4, 5)));

		Sequence<Integer> mapForwardNext = _12345.mapForward((c, n) -> n);
		twice(() -> assertThat(mapForwardNext, contains(2, 3, 4, 5, null)));
	}

	@Test
	public void mapBackWithReplacement() {
		Sequence<Integer> mapBackPrevious = _12345.mapBack(117, (p, n) -> p);
		twice(() -> assertThat(mapBackPrevious, contains(117, 1, 2, 3, 4)));

		Sequence<Integer> mapBack = _12345.mapBack(117, (p, n) -> n);
		twice(() -> assertThat(mapBack, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void mapForwardWithReplacement() {
		Sequence<Integer> mapForward = _12345.mapForward(117, (c, n) -> c);
		twice(() -> assertThat(mapForward, contains(1, 2, 3, 4, 5)));

		Sequence<Integer> mapForwardNext = _12345.mapForward(117, (c, n) -> n);
		twice(() -> assertThat(mapForwardNext, contains(2, 3, 4, 5, 117)));
	}

	@Test
	public void peekBack() {
		AtomicInteger value = new AtomicInteger();
		Sequence<Integer> peekBack = _12345.peekBack((p, n) -> {
			assertThat(n, is(value.getAndIncrement()));
			assertThat(p, is(n == 1 ? null : n - 1));
		});

		twice(() -> {
			value.set(1);
			assertThat(peekBack, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void peekForward() {
		AtomicInteger value = new AtomicInteger();
		Sequence<Integer> peekForward = _12345.peekForward((n, f) -> {
			assertThat(n, is(value.getAndIncrement()));
			assertThat(f, is(n == 5 ? null : n + 1));
		});

		twice(() -> {
			value.set(1);
			assertThat(peekForward, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void peekBackWithReplacement() {
		AtomicInteger value = new AtomicInteger();
		Sequence<Integer> peekBack = _12345.peekBack(117, (p, n) -> {
			assertThat(n, is(value.getAndIncrement()));
			assertThat(p, is(n == 1 ? 117 : n - 1));
		});

		twice(() -> {
			value.set(1);
			assertThat(peekBack, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void peekForwardWithReplacement() {
		AtomicInteger value = new AtomicInteger();
		Sequence<Integer> peekForward = _12345.peekForward(117, (n, f) -> {
			assertThat(n, is(value.getAndIncrement()));
			assertThat(f, is(n == 5 ? 117 : n + 1));
		});

		twice(() -> {
			value.set(1);
			assertThat(peekForward, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void recurse() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i + 1);
		twice(() -> assertThat(sequence, beginsWith(1, 2, 3, 4, 5)));
	}

	@Test
	public void recurseTwins() {
		Sequence<String> sequence = Sequence.recurse(1, Object::toString, s -> parseInt(s) + 1);
		twice(() -> assertThat(sequence, beginsWith("1", "2", "3", "4", "5")));
	}

	@Test
	public void untilTerminal() {
		Sequence<Integer> untilEmpty = empty.until(5);
		twice(() -> assertThat(untilEmpty, is(emptyIterable())));

		Sequence<Integer> sequence = _123456789.until(5);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4)));

		Sequence<Integer> noEnd = _12345.until(10);
		twice(() -> assertThat(noEnd, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void untilNull() {
		Sequence<Integer> untilEmpty = empty.untilNull();
		twice(() -> assertThat(untilEmpty, is(emptyIterable())));

		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5, null, 7, 8, 9).untilNull();
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5)));

		Sequence<Integer> noEnd = _12345.untilNull();
		twice(() -> assertThat(noEnd, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void untilPredicate() {
		Sequence<Integer> untilEmpty = empty.endingAt(5);
		twice(() -> assertThat(untilEmpty, is(emptyIterable())));

		Sequence<Integer> sequence = _123456789.until(i -> i == 5);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4)));

		Sequence<Integer> noEnd = _12345.until(i -> i == 10);
		twice(() -> assertThat(noEnd, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void endingAtTerminal() {
		Sequence<Integer> endingEmpty = empty.endingAt(5);
		twice(() -> assertThat(endingEmpty, is(emptyIterable())));

		Sequence<Integer> sequence = _123456789.endingAt(5);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5)));

		Sequence<Integer> noEnd = _12345.endingAt(10);
		twice(() -> assertThat(noEnd, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void endingAtNull() {
		Sequence<Integer> endingEmpty = empty.endingAtNull();
		twice(() -> assertThat(endingEmpty, is(emptyIterable())));

		Sequence<Integer> endingAtNull = Sequence.of(1, 2, 3, 4, 5, null, 7, 8, 9).endingAtNull();
		twice(() -> assertThat(endingAtNull, contains(1, 2, 3, 4, 5, null)));

		Sequence<Integer> noNullEnd = _12345.endingAtNull();
		twice(() -> assertThat(noNullEnd, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void endingAtPredicate() {
		Sequence<Integer> endingEmpty = empty.endingAt(i -> i == 5);
		twice(() -> assertThat(endingEmpty, is(emptyIterable())));

		Sequence<Integer> sequence = _123456789.endingAt(i -> i == 5);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5)));

		Sequence<Integer> noEnd = _12345.endingAt(i -> i == 10);
		twice(() -> assertThat(noEnd, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void startingAfter() {
		Sequence<Integer> startingEmpty = empty.startingAfter(5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		Sequence<Integer> sequence = _123456789.startingAfter(5);
		twice(() -> assertThat(sequence, contains(6, 7, 8, 9)));

		Sequence<Integer> noStart = _12345.startingAfter(10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingAfterPredicate() {
		Sequence<Integer> startingEmpty = empty.startingAfter(i -> i == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		Sequence<Integer> sequence = _123456789.startingAfter(i -> i == 5);
		twice(() -> assertThat(sequence, contains(6, 7, 8, 9)));

		Sequence<Integer> noStart = _12345.startingAfter(i -> i == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFrom() {
		Sequence<Integer> startingEmpty = empty.startingFrom(5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		Sequence<Integer> sequence = _123456789.startingFrom(5);
		twice(() -> assertThat(sequence, contains(5, 6, 7, 8, 9)));

		Sequence<Integer> noStart = _12345.startingFrom(10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFromPredicate() {
		Sequence<Integer> startingEmpty = empty.startingFrom(i -> i == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		Sequence<Integer> sequence = _123456789.startingFrom(i -> i == 5);
		twice(() -> assertThat(sequence, contains(5, 6, 7, 8, 9)));

		Sequence<Integer> noStart = _12345.startingFrom(i -> i == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void toList() {
		twice(() -> {
			List<Integer> list = _12345.toList();
			assertThat(list, is(instanceOf(ArrayList.class)));
			assertThat(list, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toLinkedList() {
		twice(() -> {
			List<Integer> list = _12345.toList(LinkedList::new);
			assertThat(list, instanceOf(LinkedList.class));
			assertThat(list, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSet() {
		twice(() -> {
			Set<Integer> set = _12345.toSet();
			assertThat(set, instanceOf(HashSet.class));
			assertThat(set, containsInAnyOrder(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSortedSet() {
		twice(() -> {
			SortedSet<Integer> sortedSet = _12345.toSortedSet();
			assertThat(sortedSet, instanceOf(TreeSet.class));
			assertThat(sortedSet, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSetWithType() {
		twice(() -> {
			Set<Integer> set = _12345.toSet(LinkedHashSet::new);
			assertThat(set, instanceOf(LinkedHashSet.class));
			assertThat(set, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toCollection() {
		twice(() -> {
			Deque<Integer> deque = _12345.toCollection(ArrayDeque::new);
			assertThat(deque, instanceOf(ArrayDeque.class));
			assertThat(deque, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void collectIntoCollection() {
		twice(() -> {
			Deque<Integer> deque = new ArrayDeque<>();
			Deque<Integer> result = _12345.collectInto(deque);
			assertThat(result, is(sameInstance(deque)));
			assertThat(result, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toMap() {
		Map<String, Integer> original = Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build();

		Sequence<Entry<String, Integer>> sequence = Sequence.from(original);

		twice(() -> {
			Map<String, Integer> map = sequence.toMap();
			assertThat(map, instanceOf(HashMap.class));
			assertThat(map, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build())));

			Map<String, Integer> linkedMap = sequence.toMap((Supplier<Map<String, Integer>>) LinkedHashMap::new);
			assertThat(linkedMap, instanceOf(HashMap.class));
			assertThat(linkedMap, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build())));
		});
	}

	@Test
	public void toMapWithMappers() {
		twice(() -> {
			Map<String, Integer> map = _123.toMap(Object::toString, Function.identity());

			assertThat(map, instanceOf(HashMap.class));
			assertThat(map, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).build())));
		});
	}

	@Test
	public void toMapWithType() {
		twice(() -> {
			Map<String, Integer> map = _123.toMap(LinkedHashMap::new, Object::toString, Function.identity());

			assertThat(map, instanceOf(LinkedHashMap.class));
			assertThat(map, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).build())));
		});
	}

	@Test
	public void toSortedMap() {
		Map<String, Integer> original = Maps.builder("3", 3).put("1", 1).put("4", 4).put("2", 2).build();

		Sequence<Entry<String, Integer>> sequence = Sequence.from(original);

		twice(() -> {
			Map<String, Integer> map = sequence.toSortedMap();
			assertThat(map, instanceOf(TreeMap.class));
			assertThat(map, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build())));
		});
	}

	@Test
	public void toSortedMapWithMappers() {
		twice(() -> {
			SortedMap<String, Integer> sortedMap = threeRandom.toSortedMap(Object::toString, Function.identity());

			assertThat(sortedMap, instanceOf(TreeMap.class));
			assertThat(sortedMap, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).build())));
		});
	}

	@Test
	public void collect() {
		twice(() -> {
			Deque<Integer> deque = _12345.collect(ArrayDeque::new, ArrayDeque::add);

			assertThat(deque, instanceOf(ArrayDeque.class));
			assertThat(deque, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void collectIntoContainer() {
		twice(() -> {
			Deque<Integer> deque = new ArrayDeque<>();
			Deque<Integer> result = _12345.collectInto(deque, Deque::add);

			assertThat(result, is(sameInstance(deque)));
			assertThat(result, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(_12345.toArray(), is(arrayContaining(1, 2, 3, 4, 5))));
	}

	@Test
	public void toArrayWithType() {
		twice(() -> assertThat(_12345.toArray(Integer[]::new), arrayContaining(1, 2, 3, 4, 5)));
	}

	@Test
	public void collector() {
		twice(() -> assertThat(_12345.collect(Collectors.toList()), contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void join() {
		twice(() -> assertThat(_12345.join(), is("12345")));
	}

	@Test
	public void joinWithDelimiter() {
		twice(() -> assertThat(_12345.join(", "), is("1, 2, 3, 4, 5")));
	}

	@Test
	public void joinWithPrefixAndSuffix() {
		twice(() -> assertThat(_12345.join("<", ", ", ">"), is("<1, 2, 3, 4, 5>")));
	}

	@Test
	public void reduce() {
		twice(() -> {
			assertThat(empty.reduce(Integer::sum), is(Optional.empty()));
			assertThat(_1.reduce(Integer::sum), is(Optional.of(1)));
			assertThat(_12.reduce(Integer::sum), is(Optional.of(3)));
			assertThat(_12345.reduce(Integer::sum), is(Optional.of(15)));
		});
	}

	@Test
	public void reduceWithIdentity() {
		twice(() -> {
			assertThat(empty.reduce(17, Integer::sum), is(17));
			assertThat(_1.reduce(17, Integer::sum), is(18));
			assertThat(_12.reduce(17, Integer::sum), is(20));
			assertThat(_12345.reduce(17, Integer::sum), is(32));
		});
	}

	@Test
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(Optional.empty()));
			assertThat(_1.first(), is(Optional.of(1)));
			assertThat(_12.first(), is(Optional.of(1)));
			assertThat(_12345.first(), is(Optional.of(1)));
		});
	}

	@Test
	public void second() {
		twice(() -> {
			assertThat(empty.second(), is(Optional.empty()));
			assertThat(_1.second(), is(Optional.empty()));
			assertThat(_12.second(), is(Optional.of(2)));
			assertThat(_123.second(), is(Optional.of(2)));
			assertThat(_1234.second(), is(Optional.of(2)));
		});
	}

	@Test
	public void third() {
		twice(() -> {
			assertThat(empty.third(), is(Optional.empty()));
			assertThat(_1.third(), is(Optional.empty()));
			assertThat(_12.third(), is(Optional.empty()));
			assertThat(_123.third(), is(Optional.of(3)));
			assertThat(_1234.third(), is(Optional.of(3)));
			assertThat(_12345.third(), is(Optional.of(3)));
		});
	}

	@Test
	public void get() {
		twice(() -> assertThat(empty.get(0), is(Optional.empty())));
		twice(() -> assertThat(empty.get(17), is(Optional.empty())));

		twice(() -> assertThat(_1.get(0), is(Optional.of(1))));
		twice(() -> assertThat(_1.get(1), is(Optional.empty())));
		twice(() -> assertThat(_1.get(17), is(Optional.empty())));

		twice(() -> assertThat(_12345.get(0), is(Optional.of(1))));
		twice(() -> assertThat(_12345.get(1), is(Optional.of(2))));
		twice(() -> assertThat(_12345.get(4), is(Optional.of(5))));
		twice(() -> assertThat(_12345.get(17), is(Optional.empty())));
	}

	@Test
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(Optional.empty()));
			assertThat(_1.last(), is(Optional.of(1)));
			assertThat(_12.last(), is(Optional.of(2)));
			assertThat(_12345.last(), is(Optional.of(5)));
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void entries() {
		Sequence<Entry<Integer, Integer>> emptyEntries = empty.entries();
		twice(() -> assertThat(emptyEntries, is(emptyIterable())));

		Sequence<Entry<Integer, Integer>> oneEntries = _1.entries();
		twice(() -> assertThat(oneEntries, contains(Maps.entry(1, null))));

		Sequence<Entry<Integer, Integer>> twoEntries = _12.entries();
		twice(() -> assertThat(twoEntries, contains(Maps.entry(1, 2))));

		Sequence<Entry<Integer, Integer>> threeEntries = _123.entries();
		twice(() -> assertThat(threeEntries, contains(Maps.entry(1, 2), Maps.entry(2, 3))));

		Sequence<Entry<Integer, Integer>> fiveEntries = _12345.entries();
		twice(() -> assertThat(fiveEntries,
		                       contains(Maps.entry(1, 2), Maps.entry(2, 3), Maps.entry(3, 4), Maps.entry(4, 5))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void pairs() {
		Sequence<Pair<Integer, Integer>> emptyPaired = empty.pairs();
		twice(() -> assertThat(emptyPaired, is(emptyIterable())));

		Sequence<Pair<Integer, Integer>> onePaired = _1.pairs();
		twice(() -> assertThat(onePaired, contains(Pair.of(1, null))));

		Sequence<Pair<Integer, Integer>> twoPaired = _12.pairs();
		twice(() -> assertThat(twoPaired, contains(Pair.of(1, 2))));

		Sequence<Pair<Integer, Integer>> threePaired = _123.pairs();
		twice(() -> assertThat(threePaired, contains(Pair.of(1, 2), Pair.of(2, 3))));

		Sequence<Pair<Integer, Integer>> fivePaired = _12345.pairs();
		twice(() -> assertThat(fivePaired, contains(Pair.of(1, 2), Pair.of(2, 3), Pair.of(3, 4), Pair.of(4, 5))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void adjacentEntries() {
		Sequence<Entry<Integer, Integer>> emptyEntries = empty.adjacentEntries();
		twice(() -> assertThat(emptyEntries, is(emptyIterable())));

		Sequence<Entry<Integer, Integer>> oneEntries = _1.adjacentEntries();
		twice(() -> assertThat(oneEntries, contains(Pair.of(1, null))));

		Sequence<Entry<Integer, Integer>> twoEntries = _12.adjacentEntries();
		twice(() -> assertThat(twoEntries, contains(Pair.of(1, 2))));

		Sequence<Entry<Integer, Integer>> threeEntries = _123.adjacentEntries();
		twice(() -> assertThat(threeEntries, contains(Pair.of(1, 2), Pair.of(3, null))));

		Sequence<Entry<Integer, Integer>> fourEntries = _1234.adjacentEntries();
		twice(() -> assertThat(fourEntries, contains(Pair.of(1, 2), Pair.of(3, 4))));

		Sequence<Entry<Integer, Integer>> fiveEntries = _12345.adjacentEntries();
		twice(() -> assertThat(fiveEntries, contains(Pair.of(1, 2), Pair.of(3, 4), Pair.of(5, null))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void adjacentPairs() {
		Sequence<Pair<Integer, Integer>> emptyPaired = empty.adjacentPairs();
		twice(() -> assertThat(emptyPaired, is(emptyIterable())));

		Sequence<Pair<Integer, Integer>> onePaired = _1.adjacentPairs();
		twice(() -> assertThat(onePaired, contains(Pair.of(1, null))));

		Sequence<Pair<Integer, Integer>> twoPaired = _12.adjacentPairs();
		twice(() -> assertThat(twoPaired, contains(Pair.of(1, 2))));

		Sequence<Pair<Integer, Integer>> threePaired = _123.adjacentPairs();
		twice(() -> assertThat(threePaired, contains(Pair.of(1, 2), Pair.of(3, null))));

		Sequence<Pair<Integer, Integer>> fourPaired = _1234.adjacentPairs();
		twice(() -> assertThat(fourPaired, contains(Pair.of(1, 2), Pair.of(3, 4))));

		Sequence<Pair<Integer, Integer>> fivePaired = _12345.adjacentPairs();
		twice(() -> assertThat(fivePaired, contains(Pair.of(1, 2), Pair.of(3, 4), Pair.of(5, null))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void biSequence() {
		BiSequence<Integer, String> emptyBiSequence = empty.toBiSequence();
		twice(() -> assertThat(emptyBiSequence, is(emptyIterable())));

		BiSequence<Integer, String> oneBiSequence =
				Sequence.from(new ArrayDeque<Pair>(asList(Pair.of(1, "1")))).toBiSequence();
		twice(() -> assertThat(oneBiSequence, contains(Pair.of(1, "1"))));

		BiSequence<Integer, String> twoBiSequence =
				Sequence.from(new ArrayDeque<Pair>(asList(Pair.of(1, "1"), Pair.of(2, "2")))).toBiSequence();
		twice(() -> assertThat(twoBiSequence, contains(Pair.of(1, "1"), Pair.of(2, "2"))));

		BiSequence<Integer, String> threeBiSequence =
				Sequence.from(new ArrayDeque<Pair>(asList(Pair.of(1, "1"), Pair.of(2, "2"), Pair.of(3, "3"))))
				        .toBiSequence();
		twice(() -> assertThat(threeBiSequence, contains(Pair.of(1, "1"), Pair.of(2, "2"), Pair.of(3, "3"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void entrySequence() {
		EntrySequence<Integer, String> emptyEntrySequence = empty.toEntrySequence();
		twice(() -> assertThat(emptyEntrySequence, is(emptyIterable())));

		EntrySequence<Integer, String> oneEntrySequence =
				Sequence.from(new ArrayDeque<Entry>(asList(Maps.entry(1, "1")))).toEntrySequence();
		twice(() -> assertThat(oneEntrySequence, contains(Maps.entry(1, "1"))));

		EntrySequence<Integer, String> twoEntrySequence =
				Sequence.from(new ArrayDeque<Entry>(asList(Maps.entry(1, "1"), Maps.entry(2, "2")))).toEntrySequence();
		twice(() -> assertThat(twoEntrySequence, contains(Maps.entry(1, "1"), Maps.entry(2, "2"))));

		EntrySequence<Integer, String> threeEntrySequence =
				Sequence.from(new ArrayDeque<Entry>(asList(Maps.entry(1, "1"), Maps.entry(2, "2"), Maps.entry(3,
				                                                                                              "3"))))
				        .toEntrySequence();
		twice(() -> assertThat(threeEntrySequence,
		                       contains(Maps.entry(1, "1"), Maps.entry(2, "2"), Maps.entry(3, "3"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void window() {
		twice(() -> assertThat(empty.window(3), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3), contains(contains(1))));
		twice(() -> assertThat(_12.window(3), contains(contains(1, 2))));
		twice(() -> assertThat(_123.window(3), contains(contains(1, 2, 3))));
		twice(() -> assertThat(_1234.window(3), contains(contains(1, 2, 3), contains(2, 3, 4))));
		twice(() -> assertThat(_12345.window(3), contains(contains(1, 2, 3), contains(2, 3, 4), contains(3, 4, 5))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithStep() {
		twice(() -> assertThat(empty.window(3, 2), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3, 2), contains(contains(1))));
		twice(() -> assertThat(_12.window(3, 2), contains(contains(1, 2))));
		twice(() -> assertThat(_123.window(3, 2), contains(contains(1, 2, 3))));
		twice(() -> assertThat(_1234.window(3, 2), contains(contains(1, 2, 3), contains(3, 4))));
		twice(() -> assertThat(_12345.window(3, 2), contains(contains(1, 2, 3), contains(3, 4, 5))));
		twice(() -> assertThat(_123456789.window(3, 2),
		                       contains(contains(1, 2, 3), contains(3, 4, 5), contains(5, 6, 7), contains(7, 8, 9))));

		twice(() -> assertThat(empty.window(3, 4), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3, 4), contains(contains(1))));
		twice(() -> assertThat(_12.window(3, 4), contains(contains(1, 2))));
		twice(() -> assertThat(_123.window(3, 4), contains(contains(1, 2, 3))));
		twice(() -> assertThat(_1234.window(3, 4), contains(contains(1, 2, 3))));
		twice(() -> assertThat(_12345.window(3, 4), contains(contains(1, 2, 3), contains(5))));
		twice(() -> assertThat(_123456789.window(3, 4), contains(contains(1, 2, 3), contains(5, 6, 7), contains(9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batch() {
		twice(() -> assertThat(empty.batch(3), is(emptyIterable())));
		twice(() -> assertThat(_1.batch(3), contains(contains(1))));
		twice(() -> assertThat(_12.batch(3), contains(contains(1, 2))));
		twice(() -> assertThat(_123.batch(3), contains(contains(1, 2, 3))));
		twice(() -> assertThat(_1234.batch(3), contains(contains(1, 2, 3), contains(4))));
		twice(() -> assertThat(_12345.batch(3), contains(contains(1, 2, 3), contains(4, 5))));
		twice(() -> assertThat(_123456789.batch(3), contains(contains(1, 2, 3), contains(4, 5, 6), contains(7, 8, 9)
		)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnPredicate() {
		Sequence<Sequence<Integer>> emptyPartitioned = empty.batch((a, b) -> a > b);
		twice(() -> assertThat(emptyPartitioned, is(emptyIterable())));

		Sequence<Sequence<Integer>> onePartitioned = _1.batch((a, b) -> a > b);
		twice(() -> assertThat(onePartitioned, contains(contains(1))));

		Sequence<Sequence<Integer>> twoPartitioned = _12.batch((a, b) -> a > b);
		twice(() -> assertThat(twoPartitioned, contains(contains(1, 2))));

		Sequence<Sequence<Integer>> threePartitioned = _123.batch((a, b) -> a > b);
		twice(() -> assertThat(threePartitioned, contains(contains(1, 2, 3))));

		Sequence<Sequence<Integer>> threeRandomPartitioned = threeRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(threeRandomPartitioned, contains(contains(2, 3), contains(1))));

		Sequence<Sequence<Integer>> nineRandomPartitioned = nineRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(nineRandomPartitioned,
		                       contains(contains(67), contains(5, 43), contains(3, 5, 7, 24), contains(5, 67))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitAroundElement() {
		Sequence<Sequence<Integer>> emptySplit = empty.split(3);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

		Sequence<Sequence<Integer>> oneSplit = _1.split(3);
		twice(() -> assertThat(oneSplit, contains(contains(1))));

		Sequence<Sequence<Integer>> twoSplit = _12.split(3);
		twice(() -> assertThat(twoSplit, contains(contains(1, 2))));

		Sequence<Sequence<Integer>> threeSplit = _123.split(3);
		twice(() -> assertThat(threeSplit, contains(contains(1, 2))));

		Sequence<Sequence<Integer>> fiveSplit = _12345.split(3);
		twice(() -> assertThat(fiveSplit, contains(contains(1, 2), contains(4, 5))));

		Sequence<Sequence<Integer>> nineSplit = _123456789.split(3);
		twice(() -> assertThat(nineSplit, contains(contains(1, 2), contains(4, 5, 6, 7, 8, 9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitPredicate() {
		Sequence<Sequence<Integer>> emptySplit = empty.split(x -> x % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

		Sequence<Sequence<Integer>> oneSplit = _1.split(x -> x % 3 == 0);
		twice(() -> assertThat(oneSplit, contains(contains(1))));

		Sequence<Sequence<Integer>> twoSplit = _12.split(x -> x % 3 == 0);
		twice(() -> assertThat(twoSplit, contains(contains(1, 2))));

		Sequence<Sequence<Integer>> threeSplit = _123.split(x -> x % 3 == 0);
		twice(() -> assertThat(threeSplit, contains(contains(1, 2))));

		Sequence<Sequence<Integer>> fiveSplit = _12345.split(x -> x % 3 == 0);
		twice(() -> assertThat(fiveSplit, contains(contains(1, 2), contains(4, 5))));

		Sequence<Sequence<Integer>> nineSplit = _123456789.split(x -> x % 3 == 0);
		twice(() -> assertThat(nineSplit, contains(contains(1, 2), contains(4, 5), contains(7, 8))));
	}

	@Test
	public void step() {
		twice(() -> assertThat(_123456789.step(3), contains(1, 4, 7)));
	}

	@Test
	public void distinct() {
		Sequence<Integer> emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		Sequence<Integer> oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, contains(17)));

		Sequence<Integer> twoDuplicatesDistinct = Sequence.from(new ArrayDeque<>(asList(17, 17))).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, contains(17)));

		Sequence<Integer> nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, contains(67, 5, 43, 3, 7, 24)));
	}

	@Test
	public void sorted() {
		Sequence<Integer> emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		Sequence<Integer> oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, contains(17)));

		Sequence<Integer> twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, contains(17, 32)));

		Sequence<Integer> nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, contains(3, 5, 5, 5, 7, 24, 43, 67, 67)));
	}

	@Test
	public void sortedComparator() {
		Sequence<Integer> emptySorted = empty.sorted(Comparator.reverseOrder());
		twice(() -> assertThat(emptySorted, emptyIterable()));

		Sequence<Integer> oneSorted = oneRandom.sorted(Comparator.reverseOrder());
		twice(() -> assertThat(oneSorted, contains(17)));

		Sequence<Integer> twoSorted = twoRandom.sorted(Comparator.reverseOrder());
		twice(() -> assertThat(twoSorted, contains(32, 17)));

		Sequence<Integer> nineSorted = nineRandom.sorted(Comparator.reverseOrder());
		twice(() -> assertThat(nineSorted, contains(67, 67, 43, 24, 7, 5, 5, 5, 3)));
	}

	@Test
	public void min() {
		Optional<Integer> emptyMin = empty.min(Comparator.naturalOrder());
		twice(() -> assertThat(emptyMin, is(Optional.empty())));

		Optional<Integer> oneMin = oneRandom.min(Comparator.naturalOrder());
		twice(() -> assertThat(oneMin, is(Optional.of(17))));

		Optional<Integer> twoMin = twoRandom.min(Comparator.naturalOrder());
		twice(() -> assertThat(twoMin, is(Optional.of(17))));

		Optional<Integer> nineMin = nineRandom.min(Comparator.naturalOrder());
		twice(() -> assertThat(nineMin, is(Optional.of(3))));
	}

	@Test
	public void max() {
		Optional<Integer> emptyMax = empty.max(Comparator.naturalOrder());
		twice(() -> assertThat(emptyMax, is(Optional.empty())));

		Optional<Integer> oneMax = oneRandom.max(Comparator.naturalOrder());
		twice(() -> assertThat(oneMax, is(Optional.of(17))));

		Optional<Integer> twoMax = twoRandom.max(Comparator.naturalOrder());
		twice(() -> assertThat(twoMax, is(Optional.of(32))));

		Optional<Integer> nineMax = nineRandom.max(Comparator.naturalOrder());
		twice(() -> assertThat(nineMax, is(Optional.of(67))));
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
		AtomicInteger value = new AtomicInteger();
		Sequence<Integer> peek = _123.peek(x -> assertThat(x, is(value.getAndIncrement())));

		twice(() -> {
			value.set(1);
			assertThat(peek, contains(1, 2, 3));
		});
	}

	@Test
	public void stream() {
		twice(() -> assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable())));
		twice(() -> assertThat(empty, is(emptyIterable())));

		twice(() -> assertThat(_12345.stream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5)));
		twice(() -> assertThat(_12345, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void delimit() {
		Sequence<Object> delimitedEmpty = empty.delimit(", ");
		twice(() -> assertThat(delimitedEmpty, is(emptyIterable())));

		Sequence<Object> delimited1 = _1.delimit(", ");
		twice(() -> assertThat(delimited1, contains(1)));

		Sequence<Object> delimited12 = _12.delimit(", ");
		twice(() -> assertThat(delimited12, contains(1, ", ", 2)));

		Sequence<Object> delimited123 = _123.delimit(", ");
		twice(() -> assertThat(delimited123, contains(1, ", ", 2, ", ", 3)));

		Sequence<Object> delimited1234 = _1234.delimit(", ");
		twice(() -> assertThat(delimited1234, contains(1, ", ", 2, ", ", 3, ", ", 4)));
	}

	@Test
	public void prefix() {
		Sequence<Object> delimitedEmpty = empty.prefix("[");
		twice(() -> assertThat(delimitedEmpty, contains("[")));

		Sequence<Object> delimited1 = _1.prefix("[");
		twice(() -> assertThat(delimited1, contains("[", 1)));

		Sequence<Object> delimited123 = _123.prefix("[");
		twice(() -> assertThat(delimited123, contains("[", 1, 2, 3)));
	}

	@Test
	public void suffix() {
		Sequence<Object> delimitedEmpty = empty.suffix("]");
		twice(() -> assertThat(delimitedEmpty, contains("]")));

		Sequence<Object> delimited1 = _1.suffix("]");
		twice(() -> assertThat(delimited1, contains(1, "]")));

		Sequence<Object> delimited123 = _123.suffix("]");
		twice(() -> assertThat(delimited123, contains(1, 2, 3, "]")));
	}

	@Test
	public void delimitPrefixSuffix() {
		Sequence<Object> delimitedEmpty = empty.delimit(", ").prefix("[").suffix("]");
		twice(() -> assertThat(delimitedEmpty, contains("[", "]")));

		Sequence<Object> delimited = _123.delimit(", ").prefix("[").suffix("]");
		twice(() -> assertThat(delimited, contains("[", 1, ", ", 2, ", ", 3, "]")));
	}

	@Test
	public void suffixPrefixDelimit() {
		Sequence<Object> delimitedEmpty = empty.suffix("]").prefix("[").delimit(", ");
		twice(() -> assertThat(delimitedEmpty, contains("[", ", ", "]")));

		Sequence<Object> delimited = _123.suffix("]").prefix("[").delimit(", ");
		twice(() -> assertThat(delimited, contains("[", ", ", 1, ", ", 2, ", ", 3, ", ", "]")));
	}

	@Test
	public void surround() {
		Sequence<Object> delimitedEmpty = empty.delimit("[", ", ", "]");
		twice(() -> assertThat(delimitedEmpty, contains("[", "]")));

		Sequence<Object> delimited = _123.delimit("[", ", ", "]");
		twice(() -> assertThat(delimited, contains("[", 1, ", ", 2, ", ", 3, "]")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void interleave() {
		Sequence<Pair<Integer, Integer>> emptyInterleaved = empty.interleave(empty);
		twice(() -> assertThat(emptyInterleaved, is(emptyIterable())));

		Sequence<Pair<Integer, Integer>> interleavedShortFirst = _123.interleave(_12345);
		twice(() -> assertThat(interleavedShortFirst,
		                       contains(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3), Pair.of(null, 4),
		                                Pair.of(null, 5))));

		Sequence<Pair<Integer, Integer>> interleavedShortLast = _12345.interleave(_123);
		twice(() -> assertThat(interleavedShortLast,
		                       contains(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3), Pair.of(4, null),
		                                Pair.of(5, null))));
	}

	@Test
	public void reverse() {
		Sequence<Integer> emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		Sequence<Integer> oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, contains(1)));

		Sequence<Integer> twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, contains(2, 1)));

		Sequence<Integer> threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, contains(3, 2, 1)));

		Sequence<Integer> nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed, contains(9, 8, 7, 6, 5, 4, 3, 2, 1)));
	}

	@Test
	public void shuffle() {
		assertThat(empty.shuffle(), is(emptyIterable()));
		assertThat(_1.shuffle(), contains(1));
		assertThat(_12.shuffle(), containsInAnyOrder(1, 2));
		assertThat(_123.shuffle(), containsInAnyOrder(1, 2, 3));
		assertThat(_123456789.shuffle(), containsInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9));
	}

	@Test
	public void shuffleWithRandomSource() {
		Random seed = new Random(17);

		assertThat(empty.shuffle(seed), is(emptyIterable()));
		assertThat(_1.shuffle(seed), contains(1));
		assertThat(_12.shuffle(seed), contains(1, 2));
		assertThat(_123.shuffle(seed), contains(3, 2, 1));
		assertThat(_123456789.shuffle(seed), contains(2, 9, 4, 6, 8, 7, 5, 1, 3));
	}

	@Test
	public void ints() {
		Sequence<Integer> ints = Sequence.ints();
		twice(() -> assertThat(ints, beginsWith(1, 2, 3, 4, 5)));
		twice(() -> assertThat(ints.limit(7777).last(), is(Optional.of(7777))));
	}

	@Test
	public void intsFromZero() {
		Sequence<Integer> intsFromZero = Sequence.intsFromZero();
		twice(() -> assertThat(intsFromZero, beginsWith(0, 1, 2, 3, 4)));
		twice(() -> assertThat(intsFromZero.limit(7777).last(), is(Optional.of(7776))));
	}

	@Test
	public void longs() {
		Sequence<Long> longs = Sequence.longs();
		twice(() -> assertThat(longs, beginsWith(1L, 2L, 3L, 4L, 5L)));
		twice(() -> assertThat(longs.limit(7777).last(), is(Optional.of(7777L))));
	}

	@Test
	public void longsFromZero() {
		Sequence<Long> longsFromZero = Sequence.longsFromZero();
		twice(() -> assertThat(longsFromZero, beginsWith(0L, 1L, 2L, 3L, 4L)));
		twice(() -> assertThat(longsFromZero.limit(7777).last(), is(Optional.of(7776L))));
	}

	@Test
	public void chars() {
		Sequence<Character> chars = Sequence.chars();
		twice(() -> assertThat(chars, beginsWith((char) 0, (char) 1, (char) 2, (char) 3, (char) 4)));
		twice(() -> assertThat(chars.limit(0x1400).last(), is(Optional.of('\u13FF'))));
		twice(() -> assertThat(chars.count(), is(65536L)));
		twice(() -> assertThat(chars.last(), is(Optional.of('\uFFFF'))));
	}

	@Test
	public void intsStartingAt() {
		Sequence<Integer> startingAtMinus17 = Sequence.intsFrom(-17);
		twice(() -> assertThat(startingAtMinus17, beginsWith(-17, -16, -15, -14, -13)));

		Sequence<Integer> startingAt17 = Sequence.intsFrom(17);
		twice(() -> assertThat(startingAt17, beginsWith(17, 18, 19, 20, 21)));

		Sequence<Integer> startingAt777 = Sequence.intsFrom(777);
		twice(() -> assertThat(startingAt777.limit(7000).last(), is(Optional.of(7776))));

		Sequence<Integer> startingAtMaxValue = Sequence.intsFrom(Integer.MAX_VALUE - 2);
		twice(() -> assertThat(startingAtMaxValue,
		                       contains(Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE)));
	}

	@Test
	public void longsStartingAt() {
		Sequence<Long> startingAtMinus17 = Sequence.longsFrom(-17);
		twice(() -> assertThat(startingAtMinus17, beginsWith(-17L, -16L, -15L, -14L, -13L)));

		Sequence<Long> startingAt17 = Sequence.longsFrom(17);
		twice(() -> assertThat(startingAt17, beginsWith(17L, 18L, 19L, 20L, 21L)));

		Sequence<Long> startingAt777 = Sequence.longsFrom(777);
		twice(() -> assertThat(startingAt777.limit(7000).last(), is(Optional.of(7776L))));

		Sequence<Long> startingAtMaxValue = Sequence.longsFrom(Long.MAX_VALUE - 2);
		twice(() -> assertThat(startingAtMaxValue, contains(Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE)));
	}

	@Test
	public void charsStartingAt() {
		Sequence<Character> startingAtA = Sequence.charsFrom('A');
		twice(() -> assertThat(startingAtA, beginsWith('A', 'B', 'C', 'D', 'E')));

		Sequence<Character> startingAt1400 = Sequence.charsFrom('\u1400');
		twice(() -> assertThat(startingAt1400.limit(256).last(), is(Optional.of('\u14FF'))));

		Sequence<Character> startingAtMaxValue = Sequence.charsFrom((char) (Character.MAX_VALUE - 2));
		twice(() -> assertThat(startingAtMaxValue,
		                       contains((char) (Character.MAX_VALUE - 2), (char) (Character.MAX_VALUE - 1),
		                                Character.MAX_VALUE)));
	}

	@Test
	public void intRange() {
		Sequence<Integer> range17to20 = Sequence.range(17, 20);
		twice(() -> assertThat(range17to20, contains(17, 18, 19, 20)));

		Sequence<Integer> range20to17 = Sequence.range(20, 17);
		twice(() -> assertThat(range20to17, contains(20, 19, 18, 17)));
	}

	@Test
	public void longRange() {
		Sequence<Long> range17to20 = Sequence.range(17L, 20L);
		twice(() -> assertThat(range17to20, contains(17L, 18L, 19L, 20L)));

		Sequence<Long> range20to17 = Sequence.range(20L, 17L);
		twice(() -> assertThat(range20to17, contains(20L, 19L, 18L, 17L)));
	}

	@Test
	public void charRange() {
		Sequence<Character> rangeAtoF = Sequence.range('A', 'F');
		twice(() -> assertThat(rangeAtoF, contains('A', 'B', 'C', 'D', 'E', 'F')));

		Sequence<Character> rangeFtoA = Sequence.range('F', 'A');
		twice(() -> assertThat(rangeFtoA, contains('F', 'E', 'D', 'C', 'B', 'A')));
	}

	@Test
	public void mapToChar() {
		CharSeq emptyChars = empty.toChars(x -> (char) (x + 'a' - 1));
		twice(() -> assertThat(emptyChars, is(emptyIterable())));

		CharSeq charSeq = _12345.toChars(x -> (char) (x + 'a' - 1));
		twice(() -> assertThat(charSeq, contains('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void mapToInt() {
		IntSequence emptyInts = empty.toInts(x -> x + 1);
		twice(() -> assertThat(emptyInts, is(emptyIterable())));

		IntSequence intSequence = _12345.toInts(x -> x + 1);
		twice(() -> assertThat(intSequence, containsInts(2, 3, 4, 5, 6)));
	}

	@Test
	public void mapToLong() {
		LongSequence emptyLongs = empty.toLongs(x -> x + 1);
		twice(() -> assertThat(emptyLongs, is(emptyIterable())));

		LongSequence longSequence = _12345.toLongs(x -> x + 1);
		twice(() -> assertThat(longSequence, containsLongs(2L, 3L, 4L, 5L, 6L)));
	}

	@Test
	public void mapToDouble() {
		DoubleSequence emptyDoubles = empty.toDoubles(x -> x + 1);
		twice(() -> assertThat(emptyDoubles, is(emptyIterable())));

		DoubleSequence doubleSequence = _12345.toDoubles(x -> x + 1);
		twice(() -> assertThat(doubleSequence, contains(2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void repeat() {
		Sequence<Integer> repeatEmpty = empty.repeat();
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		Sequence<Integer> repeatOne = _1.repeat();
		twice(() -> assertThat(repeatOne, beginsWith(1, 1, 1)));

		Sequence<Integer> repeatTwo = _12.repeat();
		twice(() -> assertThat(repeatTwo, beginsWith(1, 2, 1, 2, 1)));

		Sequence<Integer> repeatThree = _123.repeat();
		twice(() -> assertThat(repeatThree, beginsWith(1, 2, 3, 1, 2, 3, 1, 2)));

		Sequence<Integer> repeatVarying = Sequence.from(new Iterable<Integer>() {
			private List<Integer> list = asList(1, 2, 3);
			int end = list.size();

			@Override
			public Iterator<Integer> iterator() {
				List<Integer> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				return subList.iterator();
			}
		}).repeat();
		assertThat(repeatVarying, contains(1, 2, 3, 1, 2, 1));
	}

	@Test
	public void repeatTwice() {
		Sequence<Integer> repeatEmpty = empty.repeat(2);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		Sequence<Integer> repeatOne = _1.repeat(2);
		twice(() -> assertThat(repeatOne, contains(1, 1)));

		Sequence<Integer> repeatTwo = _12.repeat(2);
		twice(() -> assertThat(repeatTwo, contains(1, 2, 1, 2)));

		Sequence<Integer> repeatThree = _123.repeat(2);
		twice(() -> assertThat(repeatThree, contains(1, 2, 3, 1, 2, 3)));

		Sequence<Integer> repeatVarying = Sequence.from(new Iterable<Integer>() {
			private List<Integer> list = asList(1, 2, 3);
			int end = list.size();

			@Override
			public Iterator<Integer> iterator() {
				List<Integer> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				return subList.iterator();
			}
		}).repeat(2);
		assertThat(repeatVarying, contains(1, 2, 3, 1, 2));
	}

	@Test
	public void repeatZero() {
		Sequence<Integer> repeatEmpty = empty.repeat(0);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		Sequence<Integer> repeatOne = _1.repeat(0);
		twice(() -> assertThat(repeatOne, is(emptyIterable())));

		Sequence<Integer> repeatTwo = _12.repeat(0);
		twice(() -> assertThat(repeatTwo, is(emptyIterable())));

		Sequence<Integer> repeatThree = _123.repeat(0);
		twice(() -> assertThat(repeatThree, is(emptyIterable())));
	}

	@Test
	public void generate() {
		Queue<Integer> queue = new ArrayDeque<>(asList(1, 2, 3, 4, 5));
		Sequence<Integer> sequence = Sequence.generate(queue::poll);

		assertThat(sequence, beginsWith(1, 2, 3, 4, 5, null));
		assertThat(sequence, beginsWith((Integer) null));
	}

	@Test
	public void multiGenerate() {
		Sequence<Integer> sequence = Sequence.multiGenerate(() -> {
			Queue<Integer> queue = new ArrayDeque<>(asList(1, 2, 3, 4, 5));
			return queue::poll;
		});

		twice(() -> assertThat(sequence, beginsWith(1, 2, 3, 4, 5, null)));
	}

	@Test
	public void swap() {
		twice(() -> assertThat(_12345.swap((a, b) -> a == 2 && b == 3), contains(1, 3, 2, 4, 5)));
		twice(() -> assertThat(_12345.swap((a, b) -> a == 2), contains(1, 3, 4, 5, 2)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void index() {
		BiSequence<Long, Integer> indexed = _12345.index();
		twice(() -> assertThat(indexed, contains(Pair.of(0L, 1), Pair.of(1L, 2), Pair.of(2L, 3), Pair.of(3L, 4),
		                                         Pair.of(4L, 5))));
	}

	@Test
	public void iteratorRemove() {
		Iterator<Integer> iterator = _12345.iterator();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.next();
		iterator.remove();

		twice(() -> assertThat(_12345, contains(2, 4, 5)));
	}

	@Test
	public void removeAllAfterFilter() {
		Sequence<Integer> filtered = _12345.filter(x -> x % 2 != 0);
		filtered.removeAll();

		twice(() -> assertThat(filtered, is(emptyIterable())));
		twice(() -> assertThat(_12345, contains(2, 4)));
	}

	@Test
	public void removeAllAfterAppend() {
		Sequence<Integer> appended = _1.append(new ArrayList<>(asList(2)));
		appended.removeAll();

		twice(() -> assertThat(appended, is(emptyIterable())));
		twice(() -> assertThat(_1, is(emptyIterable())));
	}

	@Test
	public void isEmpty() {
		twice(() -> assertThat(empty.isEmpty(), is(true)));
		twice(() -> assertThat(_1.isEmpty(), is(false)));
		twice(() -> assertThat(_12.isEmpty(), is(false)));
		twice(() -> assertThat(_12345.isEmpty(), is(false)));
	}

	@Test
	public void containsObject() {
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
	public void containsAllIterable() {
		assertThat(empty.containsAll(Iterables.of()), is(true));
		assertThat(empty.containsAll(Iterables.of(17, 18, 19)), is(false));

		assertThat(_12345.containsAll(Iterables.of()), is(true));
		assertThat(_12345.containsAll(Iterables.of(1)), is(true));
		assertThat(_12345.containsAll(Iterables.of(1, 3, 5)), is(true));
		assertThat(_12345.containsAll(Iterables.of(1, 2, 3, 4, 5)), is(true));
		assertThat(_12345.containsAll(Iterables.of(1, 2, 3, 4, 5, 17)), is(false));
		assertThat(_12345.containsAll(Iterables.of(17, 18, 19)), is(false));
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

	@Test
	public void containsAnyIterable() {
		assertThat(empty.containsAny(Iterables.of()), is(false));
		assertThat(empty.containsAny(Iterables.of(17, 18, 19)), is(false));

		assertThat(_12345.containsAny(Iterables.of()), is(false));
		assertThat(_12345.containsAny(Iterables.of(1)), is(true));
		assertThat(_12345.containsAny(Iterables.of(1, 3, 5)), is(true));
		assertThat(_12345.containsAny(Iterables.of(1, 2, 3, 4, 5)), is(true));
		assertThat(_12345.containsAny(Iterables.of(1, 2, 3, 4, 5, 17)), is(true));
		assertThat(_12345.containsAny(Iterables.of(17, 18, 19)), is(false));
	}
}
