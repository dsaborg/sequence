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

import org.d2ab.collection.Iterables;
import org.d2ab.collection.Maps;
import org.d2ab.function.QuaternaryFunction;
import org.d2ab.iterator.Iterators;
import org.d2ab.util.Pair;
import org.junit.Test;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.IsIterableBeginningWith.beginsWith;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.twice;
import static org.d2ab.test.Tests.twiceIndexed;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@SuppressWarnings("unchecked")
public class EntrySequenceTest {
	private final EntrySequence<String, Integer> empty = EntrySequence.empty();
	private final EntrySequence<String, Integer> _1 = EntrySequence.of(Maps.entry("1", 1));
	private final EntrySequence<String, Integer> _12 = EntrySequence.of(Maps.entry("1", 1), Maps.entry("2", 2));
	private final EntrySequence<String, Integer> _123 =
			EntrySequence.of(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3));
	private final EntrySequence<String, Integer> _1234 =
			EntrySequence.of(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4));
	private final EntrySequence<String, Integer> _12345 =
			EntrySequence.of(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
			                 Maps.entry("5", 5));
	private final EntrySequence<String, Integer> _123456789 =
			EntrySequence.of(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
			                 Maps.entry("5", 5), Maps.entry("6", 6), Maps.entry("7", 7), Maps.entry("8", 8),
			                 Maps.entry("9", 9));
	private final EntrySequence<String, Integer> random1 = EntrySequence.of(Maps.entry("17", 17));
	private final EntrySequence<String, Integer> random2 = EntrySequence.of(Maps.entry("17", 17), Maps.entry("32",
	                                                                                                         32));
	private final EntrySequence<String, Integer> random3 =
			EntrySequence.of(Maps.entry("4", 4), Maps.entry("2", 2), Maps.entry("3", 3));
	private final EntrySequence<String, Integer> random9 =
			EntrySequence.of(Maps.entry("67", 67), Maps.entry("5", 5), Maps.entry("43", 43), Maps.entry("3", 3),
			                 Maps.entry("5", 5), Maps.entry("7", 7), Maps.entry("24", 24), Maps.entry("5", 5),
			                 Maps.entry("67", 67));
	private final Entry<String, Integer>[] entries123 =
			new Entry[]{Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3)};
	private final Entry<String, Integer>[] entries12345 =
			new Entry[]{Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
			            Maps.entry("5", 5)};
	private final Entry<String, Integer>[] entries456 =
			new Entry[]{Maps.entry("4", 4), Maps.entry("5", 5), Maps.entry("6", 6)};
	private final Entry<String, Integer>[] entries789 =
			new Entry[]{Maps.entry("7", 7), Maps.entry("8", 8), Maps.entry("9", 9)};

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void ofNone() {
		EntrySequence<String, Integer> sequence = EntrySequence.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void ofWithNulls() {
		EntrySequence<String, Integer> sequence =
				EntrySequence.of(Maps.entry("1", 1), Maps.entry(null, 2), null, Maps.entry("4", null),
				                 Maps.entry(null, null));

		twice(() -> assertThat(sequence, contains(Maps.entry("1", 1), Maps.entry(null, 2), null, Maps.entry("4", null),
		                                          Maps.entry(null, null))));
	}

	@Test
	public void ofOne() {
		twice(() -> assertThat(_1, contains(Maps.entry("1", 1))));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(_123, contains(entries123)));
	}

	@Test
	public void ofPair() {
		assertThat(EntrySequence.ofEntry("1", 1), contains(Maps.entry("1", 1)));
	}

	@Test
	public void ofPairs() {
		assertThat(EntrySequence.ofEntries("1", 1, "2", 2, "3", 3), contains(entries123));
	}

	@Test
	public void forLoop() {
		twice(() -> {
			for (Entry<String, Integer> ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			int expected = 1;
			for (Entry<String, Integer> e : _12345)
				assertThat(e, is(Maps.entry(String.valueOf(expected), expected++)));

			assertThat(expected, is(6));
		});
	}

	@Test
	public void forEach() {
		twice(() -> {
			empty.forEach(e -> fail("Should not get called"));

			AtomicInteger value = new AtomicInteger(1);
			_1.forEach(e -> assertThat(e, is(Maps.entry(String.valueOf(value.get()), value.getAndIncrement()))));

			value.set(1);
			_12.forEach(e -> assertThat(e, is(Maps.entry(String.valueOf(value.get()), value.getAndIncrement()))));

			value.set(1);
			_12345.forEach(e -> assertThat(e, is(Maps.entry(String.valueOf(value.get()), value.getAndIncrement()))));
		});
	}

	@Test
	public void forEachBiConsumer() {
		twice(() -> {
			empty.forEach((k, v) -> fail("Should not get called"));

			AtomicInteger value = new AtomicInteger(1);
			_1.forEach((k, v) -> {
				assertThat(k, is(String.valueOf(value.get())));
				assertThat(v, is(value.getAndIncrement()));
			});

			value.set(1);
			_12.forEach((k, v) -> {
				assertThat(k, is(String.valueOf(value.get())));
				assertThat(v, is(value.getAndIncrement()));
			});

			value.set(1);
			_12345.forEach((k, v) -> {
				assertThat(k, is(String.valueOf(value.get())));
				assertThat(v, is(value.getAndIncrement()));
			});
		});
	}

	@Test
	public void iterator() {
		twice(() -> {
			Iterator iterator = _123.iterator();

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(Maps.entry("1", 1)));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(Maps.entry("2", 2)));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(Maps.entry("3", 3)));

			assertThat(iterator.hasNext(), is(false));
			assertThat(iterator.hasNext(), is(false));
		});
	}

	@Test
	public void fromEntrySequence() {
		EntrySequence<String, Integer> sequence = EntrySequence.from(_12345);

		twice(() -> assertThat(sequence, contains(entries12345)));
	}

	@Test
	public void fromIterable() {
		EntrySequence<String, Integer> sequence = EntrySequence.from(Iterables.of(entries12345));

		twice(() -> assertThat(sequence, contains(entries12345)));
	}

	@Test
	public void fromIterables() {
		Iterable<Entry<String, Integer>> first = Iterables.of(entries123);
		Iterable<Entry<String, Integer>> second = Iterables.of(entries456);
		Iterable<Entry<String, Integer>> third = Iterables.of(entries789);

		EntrySequence<String, Integer> sequence = EntrySequence.from(first, second, third);

		twice(() -> assertThat(sequence,
		                       contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
		                                Maps.entry("5", 5), Maps.entry("6", 6), Maps.entry("7", 7), Maps.entry("8", 8),
		                                Maps.entry("9", 9))));
	}

	@Test
	public void fromNoIterables() {
		EntrySequence<String, Integer> sequence = EntrySequence.from(new Iterable[0]);

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void onceIterator() {
		EntrySequence<String, Integer> sequence = EntrySequence.once(Iterators.of(entries12345));

		assertThat(sequence, contains(entries12345));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceStream() {
		EntrySequence<String, Integer> sequence = EntrySequence.once(Stream.of(entries12345));

		assertThat(sequence, contains(entries12345));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void cacheCollection() {
		List<Entry<String, Integer>> list = new ArrayList<>(asList(entries12345));
		EntrySequence<String, Integer> cached = EntrySequence.cache(list);
		list.set(0, Maps.entry("17", 17));

		twice(() -> assertThat(cached, contains(entries12345)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void cacheIterable() {
		List<Entry<String, Integer>> list = new ArrayList<>(asList(entries12345));
		EntrySequence<String, Integer> cached = EntrySequence.cache(list::iterator);
		list.set(0, Maps.entry("17", 17));

		twice(() -> assertThat(cached, contains(entries12345)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void cacheIterator() {
		List<Entry<String, Integer>> list = new ArrayList<>(asList(entries12345));
		EntrySequence<String, Integer> cached = EntrySequence.cache(list.iterator());
		list.set(0, Maps.entry("17", 17));

		twice(() -> assertThat(cached, contains(entries12345)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void cacheStream() {
		List<Entry<String, Integer>> list = new ArrayList<>(asList(entries12345));
		EntrySequence<String, Integer> cached = EntrySequence.cache(list.stream());
		list.set(0, Maps.entry("17", 17));

		twice(() -> assertThat(cached, contains(entries12345)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void skip() {
		EntrySequence<String, Integer> skipNone = _123.skip(0);
		twice(() -> assertThat(skipNone, contains(entries123)));

		EntrySequence<String, Integer> skipOne = _123.skip(1);
		twice(() -> assertThat(skipOne, contains(Maps.entry("2", 2), Maps.entry("3", 3))));

		EntrySequence<String, Integer> skipTwo = _123.skip(2);
		twice(() -> assertThat(skipTwo, contains(Maps.entry("3", 3))));

		EntrySequence<String, Integer> skipThree = _123.skip(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		EntrySequence<String, Integer> skipFour = _123.skip(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void skipTail() {
		EntrySequence<String, Integer> skipNone = _123.skipTail(0);
		twice(() -> assertThat(skipNone, contains(entries123)));

		EntrySequence<String, Integer> skipOne = _123.skipTail(1);
		twice(() -> assertThat(skipOne, contains(Maps.entry("1", 1), Maps.entry("2", 2))));

		EntrySequence<String, Integer> skipTwo = _123.skipTail(2);
		twice(() -> assertThat(skipTwo, contains(Maps.entry("1", 1))));

		EntrySequence<String, Integer> skipThree = _123.skipTail(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		EntrySequence<String, Integer> skipFour = _123.skipTail(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		EntrySequence<String, Integer> limitZero = _123.limit(0);
		twice(() -> assertThat(limitZero, is(emptyIterable())));

		EntrySequence<String, Integer> limitOne = _123.limit(1);
		twice(() -> assertThat(limitOne, contains(Maps.entry("1", 1))));

		EntrySequence<String, Integer> limitTwo = _123.limit(2);
		twice(() -> assertThat(limitTwo, contains(Maps.entry("1", 1), Maps.entry("2", 2))));

		EntrySequence<String, Integer> limitThree = _123.limit(3);
		twice(() -> assertThat(limitThree, contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3))));

		EntrySequence<String, Integer> limitFour = _123.limit(4);
		twice(() -> assertThat(limitFour, contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3))));
	}

	@Test
	public void append() {
		EntrySequence<String, Integer> appended =
				_123.append(EntrySequence.of(entries456)).append(EntrySequence.of(entries789));

		twice(() -> assertThat(appended,
		                       contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
		                                Maps.entry("5", 5), Maps.entry("6", 6), Maps.entry("7", 7), Maps.entry("8", 8),
		                                Maps.entry("9", 9))));
	}

	@Test
	public void appendEntry() {
		EntrySequence<String, Integer> appended = _123.appendEntry("4", 4);
		twice(() -> assertThat(appended, contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3),
		                                          Maps.entry("4", 4))));
	}

	@Test
	public void appendIsLazy() {
		Iterator<Entry<String, Integer>> first = Iterators.of(entries123);
		Iterator<Entry<String, Integer>> second = Iterators.of(entries456);

		EntrySequence<String, Integer> appended = EntrySequence.once(first).append(() -> second);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));

		assertThat(appended, contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
		                              Maps.entry("5", 5), Maps.entry("6", 6)));
		assertThat(appended, is(emptyIterable())); // iterators exhausted on second run
	}

	@Test
	public void appendIsLazyWhenSkippingHasNext() {
		Iterator<Entry<String, Integer>> first = Iterators.of(Maps.entry("1", 1));
		Iterator<Entry<String, Integer>> second = Iterators.of(Maps.entry("2", 2));

		EntrySequence<String, Integer> sequence = EntrySequence.once(first).append(EntrySequence.once(second));

		// check delayed iteration
		Iterator<Entry<String, Integer>> iterator = sequence.iterator();
		assertThat(iterator.next(), is(Maps.entry("1", 1)));
		assertThat(iterator.next(), is(Maps.entry("2", 2)));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void toSequence() {
		Sequence<Entry<String, Integer>> emptySequence = empty.toSequence();
		twice(() -> assertThat(emptySequence, is(emptyIterable())));

		Sequence<Entry<String, Integer>> sequence = _12345.toSequence();
		twice(() -> assertThat(sequence, contains(entries12345)));
	}

	@Test
	public void toSequenceKeyValueMapper() {
		Sequence<String> emptyKeySequence = empty.toSequence((k, v) -> k);
		twice(() -> assertThat(emptyKeySequence, is(emptyIterable())));

		Sequence<Integer> emptyValueSequence = empty.toSequence((k, v) -> v);
		twice(() -> assertThat(emptyValueSequence, is(emptyIterable())));

		Sequence<String> keySequence = _12345.toSequence((k, v) -> k);
		twice(() -> assertThat(keySequence, contains("1", "2", "3", "4", "5")));

		Sequence<Integer> valueSequence = _12345.toSequence((k, v) -> v);
		twice(() -> assertThat(valueSequence, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void toSequenceEntryMapper() {
		Sequence<String> emptyKeySequence = empty.toSequence(Entry::getKey);
		twice(() -> assertThat(emptyKeySequence, is(emptyIterable())));

		Sequence<Integer> emptyValueSequence = empty.toSequence(Entry::getValue);
		twice(() -> assertThat(emptyValueSequence, is(emptyIterable())));

		Sequence<String> keySequence = _12345.toSequence(Entry::getKey);
		twice(() -> assertThat(keySequence, contains("1", "2", "3", "4", "5")));

		Sequence<Integer> valueSequence = _12345.toSequence(Entry::getValue);
		twice(() -> assertThat(valueSequence, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void toBiSequence() {
		BiSequence<String, Integer> emptySequence = empty.toBiSequence();
		twice(() -> assertThat(emptySequence, is(emptyIterable())));

		BiSequence<String, Integer> sequence = _12345.toBiSequence();
		twice(() -> assertThat(sequence, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                          Pair.of("5", 5))));
	}

	@Test
	public void filter() {
		EntrySequence<String, Integer> emptyFiltered = empty.filter((s, i) -> parseInt(s) == i && i % 2 == 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));

		EntrySequence<String, Integer> oneFiltered = _1.filter((s, i) -> parseInt(s) == i && i % 2 == 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		EntrySequence<String, Integer> twoFiltered = _12.filter((s, i) -> parseInt(s) == i && i % 2 == 0);
		twice(() -> assertThat(twoFiltered, contains(Maps.entry("2", 2))));

		EntrySequence<String, Integer> filtered = _123456789.filter((s, i) -> parseInt(s) == i && i % 2 == 0);
		twice(() -> assertThat(filtered, contains(Maps.entry("2", 2), Maps.entry("4", 4), Maps.entry("6", 6),
		                                          Maps.entry("8", 8))));
	}

	@Test
	public void filterIndexed() {
		EntrySequence<String, Integer> emptyFiltered = empty.filterIndexed((k, v, x) -> parseInt(k) == v && x > 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));

		EntrySequence<String, Integer> oneFiltered = _1.filterIndexed((k, v, x) -> parseInt(k) == v && x > 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		EntrySequence<String, Integer> twoFiltered = _12.filterIndexed((k, v, x) -> parseInt(k) == v && x > 0);
		twice(() -> assertThat(twoFiltered, contains(Maps.entry("2", 2))));

		EntrySequence<String, Integer> filtered = _123456789.filterIndexed((k, v, x) -> parseInt(k) == v && x > 3);
		twice(() -> assertThat(filtered,
		                       contains(Maps.entry("5", 5), Maps.entry("6", 6), Maps.entry("7", 7), Maps.entry("8", 8),
		                                Maps.entry("9", 9))));
	}

	@Test
	public void filterEntryIndexed() {
		EntrySequence<String, Integer> emptyFiltered = empty.filterIndexed(
				(e, x) -> parseInt(e.getKey()) == e.getValue() && x > 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));

		EntrySequence<String, Integer> oneFiltered = _1.filterIndexed(
				(e, x) -> parseInt(e.getKey()) == e.getValue() && x > 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		EntrySequence<String, Integer> twoFiltered = _12.filterIndexed(
				(e, x) -> parseInt(e.getKey()) == e.getValue() && x > 0);
		twice(() -> assertThat(twoFiltered, contains(Maps.entry("2", 2))));

		EntrySequence<String, Integer> filtered = _123456789.filterIndexed(
				(e, x) -> parseInt(e.getKey()) == e.getValue() && x > 3);
		twice(() -> assertThat(filtered,
		                       contains(Maps.entry("5", 5), Maps.entry("6", 6), Maps.entry("7", 7), Maps.entry("8", 8),
		                                Maps.entry("9", 9))));
	}

	@Test
	public void includingArray() {
		EntrySequence<String, Integer> emptyIncluding = empty.including(Maps.entry("1", 1), Maps.entry("3", 3),
		                                                                Maps.entry("5", 5), Maps.entry("17", 17));
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));

		EntrySequence<String, Integer> including = _12345.including(Maps.entry("1", 1), Maps.entry("3", 3),
		                                                            Maps.entry("5", 5), Maps.entry("17", 17));
		twice(() -> assertThat(including, contains(Maps.entry("1", 1), Maps.entry("3", 3), Maps.entry("5", 5))));

		EntrySequence<String, Integer> includingAll = _12345.including(Maps.entry("1", 1), Maps.entry("2", 2),
		                                                               Maps.entry("3", 3), Maps.entry("4", 4),
		                                                               Maps.entry("5", 5), Maps.entry("17", 17));
		twice(() -> assertThat(includingAll, contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3),
		                                              Maps.entry("4", 4), Maps.entry("5", 5))));

		EntrySequence<String, Integer> includingNone = _12345.including();
		twice(() -> assertThat(includingNone, is(emptyIterable())));
	}

	@Test
	public void includingIterable() {
		EntrySequence<String, Integer> emptyIncluding = empty.including(
				Iterables.of(Maps.entry("1", 1), Maps.entry("3", 3), Maps.entry("5", 5), Maps.entry("17", 17)));
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));

		EntrySequence<String, Integer> including = _12345.including(
				Iterables.of(Maps.entry("1", 1), Maps.entry("3", 3), Maps.entry("5", 5), Maps.entry("17", 17)));
		twice(() -> assertThat(including, contains(Maps.entry("1", 1), Maps.entry("3", 3), Maps.entry("5", 5))));

		EntrySequence<String, Integer> includingAll = _12345.including(
				Iterables.of(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
				             Maps.entry("5", 5), Maps.entry("17", 17)));
		twice(() -> assertThat(includingAll, contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3),
		                                              Maps.entry("4", 4), Maps.entry("5", 5))));

		EntrySequence<String, Integer> includingNone = _12345.including(Iterables.of());
		twice(() -> assertThat(includingNone, is(emptyIterable())));
	}

	@Test
	public void excludingArray() {
		EntrySequence<String, Integer> emptyExcluding = empty.excluding(Maps.entry("1", 1), Maps.entry("3", 3),
		                                                                Maps.entry("5", 5), Maps.entry("17", 17));
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));

		EntrySequence<String, Integer> excluding = _12345.excluding(Maps.entry("1", 1), Maps.entry("3", 3),
		                                                            Maps.entry("5", 5), Maps.entry("17", 17));
		twice(() -> assertThat(excluding, contains(Maps.entry("2", 2), Maps.entry("4", 4))));

		EntrySequence<String, Integer> excludingAll = _12345.excluding(Maps.entry("1", 1), Maps.entry("2", 2),
		                                                               Maps.entry("3", 3), Maps.entry("4", 4),
		                                                               Maps.entry("5", 5), Maps.entry("17", 17));
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		EntrySequence<String, Integer> excludingNone = _12345.excluding();
		twice(() -> assertThat(excludingNone, contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3),
		                                               Maps.entry("4", 4), Maps.entry("5", 5))));
	}

	@Test
	public void excludingIterable() {
		EntrySequence<String, Integer> emptyExcluding = empty.excluding(
				Iterables.of(Maps.entry("1", 1), Maps.entry("3", 3), Maps.entry("5", 5), Maps.entry("17", 17)));
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));

		EntrySequence<String, Integer> excluding = _12345.excluding(
				Iterables.of(Maps.entry("1", 1), Maps.entry("3", 3), Maps.entry("5", 5), Maps.entry("17", 17)));
		twice(() -> assertThat(excluding, contains(Maps.entry("2", 2), Maps.entry("4", 4))));

		EntrySequence<String, Integer> excludingAll = _12345.excluding(
				Iterables.of(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
				             Maps.entry("5", 5), Maps.entry("17", 17)));
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		EntrySequence<String, Integer> excludingNone = _12345.excluding(Iterables.of());
		twice(() -> assertThat(excludingNone, contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3),
		                                               Maps.entry("4", 4), Maps.entry("5", 5))));
	}

	@Test
	public void filterAndMap() {
		EntrySequence<Integer, String> evens =
				_123456789.filter((s, x) -> x % 2 == 0).map(Integer::parseInt, Object::toString);

		twice(() -> assertThat(evens, contains(Maps.entry(2, "2"), Maps.entry(4, "4"), Maps.entry(6, "6"),
		                                       Maps.entry(8, "8"))));
	}

	@Test
	public void mapBiFunction() {
		EntrySequence<Integer, String> mapped = _123.map((s, i) -> Maps.entry(parseInt(s), i.toString()));
		twice(() -> assertThat(mapped, contains(Maps.entry(1, "1"), Maps.entry(2, "2"), Maps.entry(3, "3"))));
	}

	@Test
	public void mapTwoFunctions() {
		EntrySequence<Integer, String> mapped = _123.map(Integer::parseInt, Object::toString);
		twice(() -> assertThat(mapped, contains(Maps.entry(1, "1"), Maps.entry(2, "2"), Maps.entry(3, "3"))));
	}

	@Test
	public void mapEntryFunction() {
		EntrySequence<Integer, String> mapped =
				_123.map(p -> Maps.entry(parseInt(p.getKey()), p.getValue().toString()));
		twice(() -> assertThat(mapped, contains(Maps.entry(1, "1"), Maps.entry(2, "2"), Maps.entry(3, "3"))));
	}

	@Test
	public void mapIsLazy() {
		EntrySequence<Integer, String> mapped = EntrySequence.of(Maps.entry("1", 1), null) // null will be hit on map
		                                                     .map((s, i) -> Maps.entry(parseInt(s), i.toString()));

		twice(() -> {
			// NPE here if not lazy
			Iterator<Entry<Integer, String>> iterator = mapped.iterator();

			assertThat(iterator.next(), is(Maps.entry(1, "1")));

			try {
				iterator.next();
				fail("Expected NPE");
			} catch (NullPointerException ignored) {
				// expected
			}
		});
	}

	@Test
	public void mapWithIndex() {
		EntrySequence<Integer, String> mappedEmpty = empty.mapIndexed((p, i) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(mappedEmpty, is(emptyIterable())));

		AtomicInteger index = new AtomicInteger();
		EntrySequence<Integer, String> oneMapped = _1.mapIndexed((e, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return Maps.entry(e.getValue(), e.getKey());
		});
		twice(() -> {
			index.set(0);
			assertThat(oneMapped, contains(Maps.entry(1, "1")));
		});

		EntrySequence<Integer, String> twoMapped = _12.mapIndexed((e, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return Maps.entry(e.getValue(), e.getKey());
		});
		twice(() -> {
			index.set(0);
			assertThat(twoMapped, contains(Maps.entry(1, "1"), Maps.entry(2, "2")));
		});

		EntrySequence<Integer, String> fiveMapped = _12345.mapIndexed((e, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return Maps.entry(e.getValue(), e.getKey());
		});
		twice(() -> {
			index.set(0);
			assertThat(fiveMapped,
			           contains(Maps.entry(1, "1"), Maps.entry(2, "2"), Maps.entry(3, "3"), Maps.entry(4, "4"),
			                    Maps.entry(5, "5")));
		});
	}

	@Test
	public void mapBiFunctionWithIndex() {
		EntrySequence<Integer, String> mappedEmpty = empty.mapIndexed((k, v, i) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(mappedEmpty, is(emptyIterable())));

		AtomicInteger index = new AtomicInteger();
		EntrySequence<Integer, String> oneMapped = _1.mapIndexed((k, v, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return Maps.entry(v, k);
		});
		twiceIndexed(index, 1, () -> {
			assertThat(oneMapped, contains(Maps.entry(1, "1")));
		});

		EntrySequence<Integer, String> twoMapped = _12.mapIndexed((k, v, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return Maps.entry(v, k);
		});
		twiceIndexed(index, 2, () -> {
			assertThat(twoMapped, contains(Maps.entry(1, "1"), Maps.entry(2, "2")));
		});

		EntrySequence<Integer, String> fiveMapped = _12345.mapIndexed((k, v, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return Maps.entry(v, k);
		});
		twiceIndexed(index, 5, () -> assertThat(fiveMapped,
		                                        contains(Maps.entry(1, "1"), Maps.entry(2, "2"), Maps.entry(3, "3"),
		                                                 Maps.entry(4, "4"), Maps.entry(5, "5"))));
	}

	@Test
	public void recurse() {
		EntrySequence<String, Integer> sequence =
				EntrySequence.recurse("1", 1, (k, v) -> Maps.entry(String.valueOf(v + 1), v + 1));
		twice(() -> assertThat(sequence.limit(3),
		                       contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3))));
	}

	@Test
	public void recurseTwins() {
		EntrySequence<String, Integer> sequence = EntrySequence.recurse(1, "1", (k, v) -> Maps.entry(v, k),
		                                                                (k, v) -> Maps.entry(v + 1,
		                                                                                     String.valueOf(v + 1)));
		twice(() -> assertThat(sequence.limit(3),
		                       contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3))));
	}

	@Test
	public void until() {
		EntrySequence<String, Integer> sequence = EntrySequence.from(_12345).until(Maps.entry("4", 4));
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void endingAt() {
		EntrySequence<String, Integer> sequence = EntrySequence.from(_12345).endingAt(Maps.entry("3", 3));
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void untilPredicate() {
		EntrySequence<String, Integer> sequence = EntrySequence.from(_12345).until(e -> e.equals(Maps.entry("4", 4)));
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void endingAtPredicate() {
		EntrySequence<String, Integer> sequence =
				EntrySequence.from(_12345).endingAt(e -> e.equals(Maps.entry("3", 3)));
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void untilBinary() {
		EntrySequence<String, Integer> sequence = EntrySequence.from(_12345).until("4", 4);
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void endingAtBinary() {
		EntrySequence<String, Integer> sequence = EntrySequence.from(_12345).endingAt("3", 3);
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void untilBinaryPredicate() {
		EntrySequence<String, Integer> sequence = EntrySequence.from(_12345).until((k, v) -> k.equals("4") && v == 4);
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void endingAtBinaryPredicate() {
		EntrySequence<String, Integer> sequence =
				EntrySequence.from(_12345).endingAt((k, v) -> k.equals("3") && v == 3);
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void startingAfter() {
		EntrySequence<String, Integer> startingEmpty = empty.startingAfter(Maps.entry("5", 5));
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		EntrySequence<String, Integer> sequence = _123456789.startingAfter(Maps.entry("5", 5));
		twice(() -> assertThat(sequence, contains(Maps.entry("6", 6), Maps.entry("7", 7), Maps.entry("8", 8),
		                                          Maps.entry("9", 9))));

		EntrySequence<String, Integer> noStart = _12345.startingAfter(Maps.entry("10", 10));
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingAfterPredicate() {
		EntrySequence<String, Integer> startingEmpty = empty.startingAfter(e -> e.getValue() == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		EntrySequence<String, Integer> sequence = _123456789.startingAfter(e -> e.getValue() == 5);
		twice(() -> assertThat(sequence, contains(Maps.entry("6", 6), Maps.entry("7", 7), Maps.entry("8", 8),
		                                          Maps.entry("9", 9))));

		EntrySequence<String, Integer> noStart = _12345.startingAfter(e -> e.getValue() == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingAfterBiPredicate() {
		EntrySequence<String, Integer> startingEmpty = empty.startingAfter((k, v) -> v == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		EntrySequence<String, Integer> sequence = _123456789.startingAfter((k, v) -> v == 5);
		twice(() -> assertThat(sequence, contains(Maps.entry("6", 6), Maps.entry("7", 7), Maps.entry("8", 8),
		                                          Maps.entry("9", 9))));

		EntrySequence<String, Integer> noStart = _12345.startingAfter((k, v) -> v == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFrom() {
		EntrySequence<String, Integer> startingEmpty = empty.startingFrom(Maps.entry("5", 5));
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		EntrySequence<String, Integer> sequence = _123456789.startingFrom(Maps.entry("5", 5));
		twice(() -> assertThat(sequence,
		                       contains(Maps.entry("5", 5), Maps.entry("6", 6), Maps.entry("7", 7), Maps.entry("8", 8),
		                                Maps.entry("9", 9))));

		EntrySequence<String, Integer> noStart = _12345.startingFrom(Maps.entry("10", 10));
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFromPredicate() {
		EntrySequence<String, Integer> startingEmpty = empty.startingFrom(e -> e.getValue() == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		EntrySequence<String, Integer> sequence = _123456789.startingFrom(e -> e.getValue() == 5);
		twice(() -> assertThat(sequence,
		                       contains(Maps.entry("5", 5), Maps.entry("6", 6), Maps.entry("7", 7), Maps.entry("8", 8),
		                                Maps.entry("9", 9))));

		EntrySequence<String, Integer> noStart = _12345.startingFrom(e -> e.getValue() == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFromBiPredicate() {
		EntrySequence<String, Integer> startingEmpty = empty.startingFrom((k, v) -> v == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		EntrySequence<String, Integer> sequence = _123456789.startingFrom((k, v) -> v == 5);
		twice(() -> assertThat(sequence,
		                       contains(Maps.entry("5", 5), Maps.entry("6", 6), Maps.entry("7", 7), Maps.entry("8", 8),
		                                Maps.entry("9", 9))));

		EntrySequence<String, Integer> noStart = _12345.startingFrom((k, v) -> v == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void toList() {
		EntrySequence<String, Integer> sequence = _12345;

		twice(() -> {
			List<Entry<String, Integer>> list = sequence.toList();
			assertThat(list, instanceOf(ArrayList.class));
			assertThat(list, contains(entries12345));
		});
	}

	@Test
	public void toLinkedList() {
		EntrySequence<String, Integer> sequence = _12345;

		twice(() -> {
			List<Entry<String, Integer>> list = sequence.toList(LinkedList::new);
			assertThat(list, instanceOf(LinkedList.class));
			assertThat(list, contains(entries12345));
		});
	}

	@Test
	public void toSet() {
		EntrySequence<String, Integer> sequence = _12345;

		twice(() -> {
			Set<Entry<String, Integer>> set = sequence.toSet();
			assertThat(set, instanceOf(HashSet.class));
			assertThat(set, containsInAnyOrder(entries12345));
		});
	}

	@Test
	public void toSortedSet() {
		EntrySequence<String, Integer> sequence = _12345;

		twice(() -> {
			SortedSet<Entry<String, Integer>> sortedSet = sequence.toSortedSet();
			assertThat(sortedSet, instanceOf(TreeSet.class));
			assertThat(sortedSet, contains(entries12345));
		});
	}

	@Test
	public void toSetWithType() {
		EntrySequence<String, Integer> sequence = _12345;

		twice(() -> {
			Set<Entry<String, Integer>> set = sequence.toSet(LinkedHashSet::new);
			assertThat(set, instanceOf(LinkedHashSet.class));
			assertThat(set, contains(entries12345));
		});
	}

	@Test
	public void toCollection() {
		EntrySequence<String, Integer> sequence = _12345;

		twice(() -> {
			Deque<Entry<String, Integer>> deque = sequence.toCollection(ArrayDeque::new);
			assertThat(deque, instanceOf(ArrayDeque.class));
			assertThat(deque, contains(entries12345));
		});
	}

	@Test
	public void toMapFromEntries() {
		Map<String, Integer> original = Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build();

		EntrySequence<String, Integer> sequence = EntrySequence.from(original);

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
	public void toSortedMap() {
		twice(() -> {
			SortedMap<String, Integer> sortedMap = random3.toSortedMap();

			assertThat(sortedMap, instanceOf(TreeMap.class));
			assertThat(sortedMap, is(equalTo(Maps.builder("2", 2).put("3", 3).put("4", 4).build())));
		});
	}

	@Test
	public void collect() {
		twice(() -> {
			Deque<Entry<String, Integer>> deque = _123.collect(ArrayDeque::new, ArrayDeque::add);

			assertThat(deque, instanceOf(ArrayDeque.class));
			assertThat(deque, contains(entries123));
		});
	}

	@Test
	public void collectInto() {
		twice(() -> {
			ArrayDeque<Entry<String, Integer>> original = new ArrayDeque<>();
			Deque<Entry<String, Integer>> deque = _123.collectInto(original, ArrayDeque::add);

			assertThat(deque, is(sameInstance(original)));
			assertThat(deque, contains(entries123));
		});
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(_123.toArray(), is(arrayContaining(entries123))));
	}

	@Test
	public void toArrayWithType() {
		twice(() -> assertThat(_123.toArray(Entry[]::new), arrayContaining(entries123)));
	}

	@Test
	public void collector() {
		twice(() -> assertThat(_123.collect(Collectors.toList()), contains(entries123)));
	}

	@Test
	public void join() {
		twice(() -> assertThat(_123.join(", "), is("<1, 1>, <2, 2>, <3, 3>")));
	}

	@Test
	public void joinWithPrefixAndSuffix() {
		twice(() -> assertThat(_123.join("<", ", ", ">"), is("<<1, 1>, <2, 2>, <3, 3>>")));
	}

	@Test
	public void reduce() {
		BinaryOperator<Entry<String, Integer>> sumEntry =
				(r, e) -> Maps.entry(r.getKey() + e.getKey(), r.getValue() + e.getValue());

		twice(() -> {
			assertThat(empty.reduce(sumEntry), is(Optional.empty()));
			assertThat(_1.reduce(sumEntry), is(Optional.of(Maps.entry("1", 1))));
			assertThat(_12.reduce(sumEntry), is(Optional.of(Maps.entry("12", 3))));
			assertThat(_123.reduce(sumEntry), is(Optional.of(Maps.entry("123", 6))));
		});
	}

	@Test
	public void reduceQuaternary() {
		QuaternaryFunction<String, Integer, String, Integer, Entry<String, Integer>> sumEntry =
				(rk, rv, ek, ev) -> Maps.entry(rk + ek, rv + ev);

		twice(() -> {
			assertThat(empty.reduce(sumEntry), is(Optional.empty()));
			assertThat(_1.reduce(sumEntry), is(Optional.of(Maps.entry("1", 1))));
			assertThat(_12.reduce(sumEntry), is(Optional.of(Maps.entry("12", 3))));
			assertThat(_123.reduce(sumEntry), is(Optional.of(Maps.entry("123", 6))));
		});
	}

	@Test
	public void reduceWithIdentity() {
		BinaryOperator<Entry<String, Integer>> sumEntry =
				(r, e) -> Maps.entry(r.getKey() + e.getKey(), r.getValue() + e.getValue());
		twice(() -> {
			assertThat(empty.reduce(Maps.entry("17", 17), sumEntry), is(Maps.entry("17", 17)));
			assertThat(_1.reduce(Maps.entry("17", 17), sumEntry), is(Maps.entry("171", 18)));
			assertThat(_12.reduce(Maps.entry("17", 17), sumEntry), is(Maps.entry("1712", 20)));
			assertThat(_123.reduce(Maps.entry("17", 17), sumEntry), is(Maps.entry("17123", 23)));
		});
	}

	@Test
	public void reduceQuaternaryWithIdentity() {
		QuaternaryFunction<String, Integer, String, Integer, Entry<String, Integer>> sumEntry =
				(rk, rv, ek, ev) -> Maps.entry(rk + ek, rv + ev);

		twice(() -> {
			assertThat(empty.reduce("17", 17, sumEntry), is(Maps.entry("17", 17)));
			assertThat(_1.reduce("17", 17, sumEntry), is(Maps.entry("171", 18)));
			assertThat(_12.reduce("17", 17, sumEntry), is(Maps.entry("1712", 20)));
			assertThat(_123.reduce("17", 17, sumEntry), is(Maps.entry("17123", 23)));
		});
	}

	@Test
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(Optional.empty()));
			assertThat(_1.first(), is(Optional.of(Maps.entry("1", 1))));
			assertThat(_12.first(), is(Optional.of(Maps.entry("1", 1))));
			assertThat(_123.first(), is(Optional.of(Maps.entry("1", 1))));
		});
	}

	@Test
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(Optional.empty()));
			assertThat(_1.last(), is(Optional.of(Maps.entry("1", 1))));
			assertThat(_12.last(), is(Optional.of(Maps.entry("2", 2))));
			assertThat(_123.last(), is(Optional.of(Maps.entry("3", 3))));
		});
	}

	@Test
	public void at() {
		twice(() -> assertThat(empty.at(0), is(Optional.empty())));
		twice(() -> assertThat(empty.at(17), is(Optional.empty())));
		twice(() -> assertThat(_1.at(0), is(Optional.of(Maps.entry("1", 1)))));
		twice(() -> assertThat(_1.at(1), is(Optional.empty())));
		twice(() -> assertThat(_1.at(17), is(Optional.empty())));
		twice(() -> assertThat(_12345.at(0), is(Optional.of(Maps.entry("1", 1)))));
		twice(() -> assertThat(_12345.at(1), is(Optional.of(Maps.entry("2", 2)))));
		twice(() -> assertThat(_12345.at(4), is(Optional.of(Maps.entry("5", 5)))));
		twice(() -> assertThat(_12345.at(17), is(Optional.empty())));
	}

	@Test
	public void firstByPredicate() {
		twice(() -> {
			assertThat(empty.first(e -> e.getValue() > 1), is(Optional.empty()));
			assertThat(_1.first(e -> e.getValue() > 1), is(Optional.empty()));
			assertThat(_12.first(e -> e.getValue() > 1), is(Optional.of(Maps.entry("2", 2))));
			assertThat(_123.first(e -> e.getValue() > 1), is(Optional.of(Maps.entry("2", 2))));
		});
	}

	@Test
	public void lastByPredicate() {
		twice(() -> {
			assertThat(empty.last(e -> e.getValue() > 1), is(Optional.empty()));
			assertThat(_1.last(e -> e.getValue() > 1), is(Optional.empty()));
			assertThat(_12.last(e -> e.getValue() > 1), is(Optional.of(Maps.entry("2", 2))));
			assertThat(_123.last(e -> e.getValue() > 1), is(Optional.of(Maps.entry("3", 3))));
		});
	}

	@Test
	public void atByPredicate() {
		twice(() -> {
			assertThat(empty.at(0, e -> e.getValue() > 1), is(Optional.empty()));
			assertThat(empty.at(17, e -> e.getValue() > 1), is(Optional.empty()));

			assertThat(_1.at(0, e -> e.getValue() > 1), is(Optional.empty()));
			assertThat(_1.at(17, e -> e.getValue() > 1), is(Optional.empty()));

			assertThat(_12.at(0, e -> e.getValue() > 1), is(Optional.of(Maps.entry("2", 2))));
			assertThat(_12.at(1, e -> e.getValue() > 1), is(Optional.empty()));
			assertThat(_12.at(17, e -> e.getValue() > 1), is(Optional.empty()));

			assertThat(_12345.at(0, e -> e.getValue() > 1), is(Optional.of(Maps.entry("2", 2))));
			assertThat(_12345.at(1, e -> e.getValue() > 1), is(Optional.of(Maps.entry("3", 3))));
			assertThat(_12345.at(3, e -> e.getValue() > 1), is(Optional.of(Maps.entry("5", 5))));
			assertThat(_12345.at(4, e -> e.getValue() > 1), is(Optional.empty()));
			assertThat(_12345.at(17, e -> e.getValue() > 1), is(Optional.empty()));
		});
	}

	@Test
	public void firstByBiPredicate() {
		twice(() -> {
			assertThat(empty.first((k, v) -> v > 1), is(Optional.empty()));
			assertThat(_1.first((k, v) -> v > 1), is(Optional.empty()));
			assertThat(_12.first((k, v) -> v > 1), is(Optional.of(Maps.entry("2", 2))));
			assertThat(_123.first((k, v) -> v > 1), is(Optional.of(Maps.entry("2", 2))));
		});
	}

	@Test
	public void lastByBiPredicate() {
		twice(() -> {
			assertThat(empty.last((k, v) -> v > 1), is(Optional.empty()));
			assertThat(_1.last((k, v) -> v > 1), is(Optional.empty()));
			assertThat(_12.last((k, v) -> v > 1), is(Optional.of(Maps.entry("2", 2))));
			assertThat(_123.last((k, v) -> v > 1), is(Optional.of(Maps.entry("3", 3))));
		});
	}

	@Test
	public void atByBiPredicate() {
		twice(() -> {
			assertThat(empty.at(0, (k, v) -> v > 1), is(Optional.empty()));
			assertThat(empty.at(17, (k, v) -> v > 1), is(Optional.empty()));

			assertThat(_1.at(0, (k, v) -> v > 1), is(Optional.empty()));
			assertThat(_1.at(17, (k, v) -> v > 1), is(Optional.empty()));

			assertThat(_12.at(0, (k, v) -> v > 1), is(Optional.of(Maps.entry("2", 2))));
			assertThat(_12.at(1, (k, v) -> v > 1), is(Optional.empty()));
			assertThat(_12.at(17, (k, v) -> v > 1), is(Optional.empty()));

			assertThat(_12345.at(0, (k, v) -> v > 1), is(Optional.of(Maps.entry("2", 2))));
			assertThat(_12345.at(1, (k, v) -> v > 1), is(Optional.of(Maps.entry("3", 3))));
			assertThat(_12345.at(3, (k, v) -> v > 1), is(Optional.of(Maps.entry("5", 5))));
			assertThat(_12345.at(4, (k, v) -> v > 1), is(Optional.empty()));
			assertThat(_12345.at(17, (k, v) -> v > 1), is(Optional.empty()));
		});
	}

	@Test
	public void window() {
		twice(() -> assertThat(_12345.window(3),
		                       contains(contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3)),
		                                contains(Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4)),
		                                contains(Maps.entry("3", 3), Maps.entry("4", 4), Maps.entry("5", 5)))));
	}

	@Test
	public void windowWithStep() {
		twice(() -> assertThat(_12345.window(3, 2),
		                       contains(contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3)),
		                                contains(Maps.entry("3", 3), Maps.entry("4", 4), Maps.entry("5", 5)))));
	}

	@Test
	public void batch() {
		twice(() -> assertThat(_12345.batch(3),
		                       contains(contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3)),
		                                contains(Maps.entry("4", 4), Maps.entry("5", 5)))));
	}

	@SuppressWarnings("uncheckeed")
	@Test
	public void batchOnPredicate() {
		Sequence<EntrySequence<String, Integer>> emptyPartitioned = empty.batch((a, b) -> a.getValue() > b.getValue());
		twice(() -> assertThat(emptyPartitioned, is(emptyIterable())));

		Sequence<EntrySequence<String, Integer>> onePartitioned = _1.batch((a, b) -> a.getValue() > b.getValue());
		twice(() -> assertThat(onePartitioned, contains(contains(Maps.entry("1", 1)))));

		Sequence<EntrySequence<String, Integer>> twoPartitioned = _12.batch((a, b) -> a.getValue() > b.getValue());
		twice(() -> assertThat(twoPartitioned, contains(contains(Maps.entry("1", 1), Maps.entry("2", 2)))));

		Sequence<EntrySequence<String, Integer>> threePartitioned = _123.batch((a, b) -> a.getValue() > b.getValue());
		twice(() -> assertThat(threePartitioned,
		                       contains(contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3)))));

		Sequence<EntrySequence<String, Integer>> threeRandomPartitioned =
				random3.batch((a, b) -> a.getValue() > b.getValue());
		twice(() -> assertThat(threeRandomPartitioned, contains(contains(Maps.entry("4", 4)),
		                                                        contains(Maps.entry("2", 2), Maps.entry("3", 3)))));

		Sequence<EntrySequence<String, Integer>> nineRandomPartitioned =
				random9.batch((a, b) -> a.getValue() > b.getValue());
		twice(() -> assertThat(nineRandomPartitioned, contains(contains(Maps.entry("67", 67)),
		                                                       contains(Maps.entry("5", 5), Maps.entry("43", 43)),
		                                                       contains(Maps.entry("3", 3), Maps.entry("5", 5),
		                                                                Maps.entry("7", 7), Maps.entry("24", 24)),
		                                                       contains(Maps.entry("5", 5), Maps.entry("67", 67)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnQuaternaryPredicate() {
		Sequence<EntrySequence<String, Integer>> emptyPartitioned = empty.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(emptyPartitioned, is(emptyIterable())));

		Sequence<EntrySequence<String, Integer>> onePartitioned = _1.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(onePartitioned, contains(contains(Maps.entry("1", 1)))));

		Sequence<EntrySequence<String, Integer>> twoPartitioned = _12.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(twoPartitioned, contains(contains(Maps.entry("1", 1), Maps.entry("2", 2)))));

		Sequence<EntrySequence<String, Integer>> threePartitioned = _123.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(threePartitioned,
		                       contains(contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3)))));

		Sequence<EntrySequence<String, Integer>> threeRandomPartitioned = random3.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(threeRandomPartitioned, contains(contains(Maps.entry("4", 4)),
		                                                        contains(Maps.entry("2", 2), Maps.entry("3", 3)))));

		Sequence<EntrySequence<String, Integer>> nineRandomPartitioned = random9.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(nineRandomPartitioned, contains(contains(Maps.entry("67", 67)),
		                                                       contains(Maps.entry("5", 5), Maps.entry("43", 43)),
		                                                       contains(Maps.entry("3", 3), Maps.entry("5", 5),
		                                                                Maps.entry("7", 7), Maps.entry("24", 24)),
		                                                       contains(Maps.entry("5", 5), Maps.entry("67", 67)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void split() {
		Sequence<EntrySequence<String, Integer>> emptySplit = empty.split(Maps.entry("3", 3));
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

		Sequence<EntrySequence<String, Integer>> oneSplit = _1.split(Maps.entry("3", 3));
		twice(() -> assertThat(oneSplit, contains(contains(Maps.entry("1", 1)))));

		Sequence<EntrySequence<String, Integer>> twoSplit = _12.split(Maps.entry("3", 3));
		twice(() -> assertThat(twoSplit, contains(contains(Maps.entry("1", 1), Maps.entry("2", 2)))));

		Sequence<EntrySequence<String, Integer>> threeSplit = _123.split(Maps.entry("3", 3));
		twice(() -> assertThat(threeSplit, contains(contains(Maps.entry("1", 1), Maps.entry("2", 2)))));

		Sequence<EntrySequence<String, Integer>> fiveSplit = _12345.split(Maps.entry("3", 3));
		twice(() -> assertThat(fiveSplit, contains(contains(Maps.entry("1", 1), Maps.entry("2", 2)),
		                                           contains(Maps.entry("4", 4), Maps.entry("5", 5)))));

		Sequence<EntrySequence<String, Integer>> nineSplit = _123456789.split(Maps.entry("3", 3));
		twice(() -> assertThat(nineSplit, contains(contains(Maps.entry("1", 1), Maps.entry("2", 2)),
		                                           contains(Maps.entry("4", 4), Maps.entry("5", 5), Maps.entry("6", 6),
		                                                    Maps.entry("7", 7), Maps.entry("8", 8),
		                                                    Maps.entry("9", 9)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitPredicate() {
		Sequence<EntrySequence<String, Integer>> emptySplit = empty.split(x -> x.getValue() % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

		Sequence<EntrySequence<String, Integer>> oneSplit = _1.split(x -> x.getValue() % 3 == 0);
		twice(() -> assertThat(oneSplit, contains(contains(Maps.entry("1", 1)))));

		Sequence<EntrySequence<String, Integer>> twoSplit = _12.split(x -> x.getValue() % 3 == 0);
		twice(() -> assertThat(twoSplit, contains(contains(Maps.entry("1", 1), Maps.entry("2", 2)))));

		Sequence<EntrySequence<String, Integer>> threeSplit = _123.split(x -> x.getValue() % 3 == 0);
		twice(() -> assertThat(threeSplit, contains(contains(Maps.entry("1", 1), Maps.entry("2", 2)))));

		Sequence<EntrySequence<String, Integer>> fiveSplit = _12345.split(x -> x.getValue() % 3 == 0);
		twice(() -> assertThat(fiveSplit, contains(contains(Maps.entry("1", 1), Maps.entry("2", 2)),
		                                           contains(Maps.entry("4", 4), Maps.entry("5", 5)))));

		Sequence<EntrySequence<String, Integer>> nineSplit = _123456789.split(x -> x.getValue() % 3 == 0);
		twice(() -> assertThat(nineSplit, contains(contains(Maps.entry("1", 1), Maps.entry("2", 2)),
		                                           contains(Maps.entry("4", 4), Maps.entry("5", 5)),
		                                           contains(Maps.entry("7", 7), Maps.entry("8", 8)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitKeyValue() {
		Sequence<EntrySequence<String, Integer>> emptySplit = empty.split((k, v) -> v % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

		Sequence<EntrySequence<String, Integer>> oneSplit = _1.split((k, v) -> v % 3 == 0);
		twice(() -> assertThat(oneSplit, contains(contains(Maps.entry("1", 1)))));

		Sequence<EntrySequence<String, Integer>> twoSplit = _12.split((k, v) -> v % 3 == 0);
		twice(() -> assertThat(twoSplit, contains(contains(Maps.entry("1", 1), Maps.entry("2", 2)))));

		Sequence<EntrySequence<String, Integer>> threeSplit = _123.split((k, v) -> v % 3 == 0);
		twice(() -> assertThat(threeSplit, contains(contains(Maps.entry("1", 1), Maps.entry("2", 2)))));

		Sequence<EntrySequence<String, Integer>> fiveSplit = _12345.split((k, v) -> v % 3 == 0);
		twice(() -> assertThat(fiveSplit, contains(contains(Maps.entry("1", 1), Maps.entry("2", 2)),
		                                           contains(Maps.entry("4", 4), Maps.entry("5", 5)))));

		Sequence<EntrySequence<String, Integer>> nineSplit = _123456789.split((k, v) -> v % 3 == 0);
		twice(() -> assertThat(nineSplit, contains(contains(Maps.entry("1", 1), Maps.entry("2", 2)),
		                                           contains(Maps.entry("4", 4), Maps.entry("5", 5)),
		                                           contains(Maps.entry("7", 7), Maps.entry("8", 8)))));
	}

	@Test
	public void step() {
		twice(() -> assertThat(_123456789.step(3),
		                       contains(Maps.entry("1", 1), Maps.entry("4", 4), Maps.entry("7", 7))));
	}

	@Test
	public void distinct() {
		EntrySequence<String, Integer> emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		EntrySequence<String, Integer> oneDistinct = random1.distinct();
		twice(() -> assertThat(oneDistinct, contains(Maps.entry("17", 17))));

		EntrySequence<String, Integer> twoDuplicatesDistinct =
				EntrySequence.of(Maps.entry("17", 17), Maps.entry("17", 17)).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, contains(Maps.entry("17", 17))));

		EntrySequence<String, Integer> nineDistinct = random9.distinct();
		twice(() -> assertThat(nineDistinct, contains(Maps.entry("67", 67), Maps.entry("5", 5), Maps.entry("43", 43),
		                                              Maps.entry("3", 3), Maps.entry("7", 7), Maps.entry("24", 24))));
	}

	@Test
	public void sorted() {
		EntrySequence<String, Integer> emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		EntrySequence<String, Integer> oneSorted = random1.sorted();
		twice(() -> assertThat(oneSorted, contains(Maps.entry("17", 17))));

		EntrySequence<String, Integer> twoSorted = random2.sorted();
		twice(() -> assertThat(twoSorted, contains(Maps.entry("17", 17), Maps.entry("32", 32))));

		EntrySequence<String, Integer> nineSorted = random9.sorted();
		twice(() -> assertThat(nineSorted, // String sorting on first item
		                       contains(Maps.entry("24", 24), Maps.entry("3", 3), Maps.entry("43", 43),
		                                Maps.entry("5", 5), Maps.entry("5", 5), Maps.entry("5", 5),
		                                Maps.entry("67", 67), Maps.entry("67", 67), Maps.entry("7", 7))));
	}

	@Test
	public void sortedComparator() {
		EntrySequence<String, Integer> emptySorted = empty.sorted((Comparator) Comparator.reverseOrder());
		twice(() -> assertThat(emptySorted, emptyIterable()));

		EntrySequence<String, Integer> oneSorted = random1.sorted((Comparator) Comparator.reverseOrder());
		twice(() -> assertThat(oneSorted, contains(Maps.entry("17", 17))));

		EntrySequence<String, Integer> twoSorted = random2.sorted((Comparator) Comparator.reverseOrder());
		twice(() -> assertThat(twoSorted, contains(Maps.entry("32", 32), Maps.entry("17", 17))));

		EntrySequence<String, Integer> nineSorted = random9.sorted((Comparator) Comparator.reverseOrder());
		twice(() -> assertThat(nineSorted, // String sorting on first item reverse
		                       contains(Maps.entry("7", 7), Maps.entry("67", 67), Maps.entry("67", 67),
		                                Maps.entry("5", 5), Maps.entry("5", 5), Maps.entry("5", 5),
		                                Maps.entry("43", 43), Maps.entry("3", 3), Maps.entry("24", 24))));
	}

	@Test
	public void min() {
		twice(() -> assertThat(empty.min(), is(Optional.empty())));
		twice(() -> assertThat(random1.min(), is(Optional.of(Maps.entry("17", 17)))));
		twice(() -> assertThat(random2.min(), is(Optional.of(Maps.entry("17", 17)))));
		twice(() -> assertThat(random9.min(), is(Optional.of(Maps.entry("24", 24)))));
	}

	@Test
	public void max() {
		twice(() -> assertThat(empty.max(), is(Optional.empty())));
		twice(() -> assertThat(random1.max(), is(Optional.of(Maps.entry("17", 17)))));
		twice(() -> assertThat(random2.max(), is(Optional.of(Maps.entry("32", 32)))));
		twice(() -> assertThat(random9.max(), is(Optional.of(Maps.entry("7", 7)))));
	}

	@Test
	public void minByComparator() {
		twice(() -> assertThat(empty.min((Comparator) Comparator.reverseOrder()), is(Optional.empty())));
		twice(() -> assertThat(random1.min((Comparator) Comparator.reverseOrder()),
		                       is(Optional.of(Maps.entry("17", 17)))));
		twice(() -> assertThat(random2.min((Comparator) Comparator.reverseOrder()),
		                       is(Optional.of(Maps.entry("32", 32)))));
		twice(() -> assertThat(random9.min((Comparator) Comparator.reverseOrder()),
		                       is(Optional.of(Maps.entry("7", 7)))));
	}

	@Test
	public void maxByComparator() {
		twice(() -> assertThat(empty.max((Comparator) Comparator.reverseOrder()), is(Optional.empty())));
		twice(() -> assertThat(random1.max((Comparator) Comparator.reverseOrder()),
		                       is(Optional.of(Maps.entry("17", 17)))));
		twice(() -> assertThat(random2.max((Comparator) Comparator.reverseOrder()),
		                       is(Optional.of(Maps.entry("17", 17)))));
		twice(() -> assertThat(random9.max((Comparator) Comparator.reverseOrder()),
		                       is(Optional.of(Maps.entry("24", 24)))));
	}

	@Test
	public void size() {
		twice(() -> assertThat(empty.size(), is(0)));
		twice(() -> assertThat(_1.size(), is(1)));
		twice(() -> assertThat(_12.size(), is(2)));
		twice(() -> assertThat(_123456789.size(), is(9)));
	}

	@Test
	public void any() {
		twice(() -> assertThat(_123.any((s, x) -> x > 0 && x == parseInt(s)), is(true)));
		twice(() -> assertThat(_123.any((s, x) -> x > 2 && x == parseInt(s)), is(true)));
		twice(() -> assertThat(_123.any((s, x) -> x > 4 && x == parseInt(s)), is(false)));
	}

	@Test
	public void all() {
		twice(() -> assertThat(_123.all((s, x) -> x > 0 && x == parseInt(s)), is(true)));
		twice(() -> assertThat(_123.all((s, x) -> x > 2 && x == parseInt(s)), is(false)));
		twice(() -> assertThat(_123.all((s, x) -> x > 4 && x == parseInt(s)), is(false)));
	}

	@Test
	public void none() {
		twice(() -> assertThat(_123.none((s, x) -> x > 0 && x == parseInt(s)), is(false)));
		twice(() -> assertThat(_123.none((s, x) -> x > 2 && x == parseInt(s)), is(false)));
		twice(() -> assertThat(_123.none((s, x) -> x > 4 && x == parseInt(s)), is(true)));
	}

	@Test
	public void peek() {
		EntrySequence<String, Integer> peekEmpty = empty.peek((l, r) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(peekEmpty, is(emptyIterable())));

		AtomicInteger index = new AtomicInteger();
		EntrySequence<String, Integer> peekOne = _1.peek((l, r) -> {
			index.getAndIncrement();
			assertThat(l, is(String.valueOf(index.get())));
			assertThat(r, is(index.get()));
		});
		twiceIndexed(index, 1, () -> assertThat(peekOne, contains(Maps.entry("1", 1))));

		EntrySequence<String, Integer> peekTwo = _12.peek((l, r) -> {
			index.getAndIncrement();
			assertThat(l, is(String.valueOf(index.get())));
			assertThat(r, is(index.get()));
		});
		twiceIndexed(index, 2, () -> assertThat(peekTwo, contains(Maps.entry("1", 1), Maps.entry("2", 2))));

		EntrySequence<String, Integer> peek = _12345.peek((l, r) -> {
			index.getAndIncrement();
			assertThat(l, is(String.valueOf(index.get())));
			assertThat(r, is(index.get()));
		});
		twiceIndexed(index, 5, () -> assertThat(peek, contains(Maps.entry("1", 1), Maps.entry("2", 2),
		                                                       Maps.entry("3", 3), Maps.entry("4", 4),
		                                                       Maps.entry("5", 5))));
	}

	@Test
	public void peekIndexed() {
		EntrySequence<String, Integer> peekEmpty = empty.peekIndexed((l, r, x) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(peekEmpty, is(emptyIterable())));

		AtomicInteger index = new AtomicInteger();
		EntrySequence<String, Integer> peekOne = _1.peekIndexed((l, r, x) -> {
			assertThat(x, is(index.getAndIncrement()));
			assertThat(l, is(String.valueOf(index.get())));
			assertThat(r, is((int) index.get()));
		});
		twiceIndexed(index, 1, () -> assertThat(peekOne, contains(Maps.entry("1", 1))));

		EntrySequence<String, Integer> peekTwo = _12.peekIndexed((l, r, x) -> {
			assertThat(x, is(index.getAndIncrement()));
			assertThat(l, is(String.valueOf(index.get())));
			assertThat(r, is((int) index.get()));
		});
		twiceIndexed(index, 2, () -> assertThat(peekTwo, contains(Maps.entry("1", 1), Maps.entry("2", 2))));

		EntrySequence<String, Integer> peek = _12345.peekIndexed((l, r, x) -> {
			assertThat(x, is(index.getAndIncrement()));
			assertThat(l, is(String.valueOf(index.get())));
			assertThat(r, is((int) index.get()));
		});
		twiceIndexed(index, 5, () -> assertThat(peek, contains(Maps.entry("1", 1), Maps.entry("2", 2),
		                                                       Maps.entry("3", 3), Maps.entry("4", 4),
		                                                       Maps.entry("5", 5))));
	}

	@Test
	public void peekEntry() {
		EntrySequence<String, Integer> peekEmpty = empty.peek(p -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(peekEmpty, is(emptyIterable())));

		AtomicInteger value = new AtomicInteger(1);
		EntrySequence<String, Integer> peekOne = _1.peek(p -> {
			assertThat(p, is(Maps.entry(String.valueOf(value.get()), value.getAndIncrement())));
		});
		twiceIndexed(value, 1, () -> assertThat(peekOne, contains(Maps.entry("1", 1))));

		EntrySequence<String, Integer> peekTwo = _12.peek(p -> {
			assertThat(p, is(Maps.entry(String.valueOf(value.get()), value.getAndIncrement())));
		});
		twiceIndexed(value, 2, () -> assertThat(peekTwo, contains(Maps.entry("1", 1), Maps.entry("2", 2))));

		EntrySequence<String, Integer> peek = _12345.peek(p -> {
			assertThat(p, is(Maps.entry(String.valueOf(value.get()), value.getAndIncrement())));
		});
		twiceIndexed(value, 5, () -> assertThat(peek, contains(Maps.entry("1", 1), Maps.entry("2", 2),
		                                                       Maps.entry("3", 3), Maps.entry("4", 4),
		                                                       Maps.entry("5", 5))));
	}

	@Test
	public void peekIndexedEntry() {
		EntrySequence<String, Integer> peekEmpty = empty.peekIndexed((p, x) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(peekEmpty, is(emptyIterable())));

		AtomicInteger index = new AtomicInteger();
		EntrySequence<String, Integer> peekOne = _1.peekIndexed((p, x) -> {
			assertThat(x, is(index.getAndIncrement()));
			assertThat(p, is(Maps.entry(String.valueOf(index.get()), (int) index.get())));
		});
		twiceIndexed(index, 1, () -> assertThat(peekOne, contains(Maps.entry("1", 1))));

		EntrySequence<String, Integer> peekTwo = _12.peekIndexed((p, x) -> {
			assertThat(x, is(index.getAndIncrement()));
			assertThat(p, is(Maps.entry(String.valueOf(index.get()), (int) index.get())));
		});
		twiceIndexed(index, 2, () -> assertThat(peekTwo, contains(Maps.entry("1", 1), Maps.entry("2", 2))));

		EntrySequence<String, Integer> peek = _12345.peekIndexed((p, x) -> {
			assertThat(x, is(index.getAndIncrement()));
			assertThat(p, is(Maps.entry(String.valueOf(index.get()), (int) index.get())));
		});
		twiceIndexed(index, 5, () -> assertThat(peek, contains(Maps.entry("1", 1), Maps.entry("2", 2),
		                                                       Maps.entry("3", 3), Maps.entry("4", 4),
		                                                       Maps.entry("5", 5))));
	}

	@Test
	public void mapToChar() {
		CharSeq emptyChars = empty.toChars((k, v) -> (char) (v + 'a' - 1));
		twice(() -> assertThat(emptyChars, is(emptyIterable())));

		CharSeq charSeq = _12345.toChars((k, v) -> (char) (v + 'a' - 1));
		twice(() -> assertThat(charSeq, contains('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void mapToInt() {
		IntSequence emptyInts = empty.toInts((k, v) -> v + 1);
		twice(() -> assertThat(emptyInts, is(emptyIterable())));

		IntSequence intSequence = _12345.toInts((k, v) -> v + 1);
		twice(() -> assertThat(intSequence, containsInts(2, 3, 4, 5, 6)));
	}

	@Test
	public void mapToLong() {
		LongSequence emptyLongs = empty.toLongs((k, v) -> v + 1);
		twice(() -> assertThat(emptyLongs, is(emptyIterable())));

		LongSequence longSequence = _12345.toLongs((k, v) -> v + 1);
		twice(() -> assertThat(longSequence, containsLongs(2L, 3L, 4L, 5L, 6L)));
	}

	@Test
	public void mapToDouble() {
		DoubleSequence emptyDoubles = empty.toDoubles((k, v) -> v + 1);
		twice(() -> assertThat(emptyDoubles, is(emptyIterable())));

		DoubleSequence doubleSequence = _12345.toDoubles((k, v) -> v + 1);
		twice(() -> assertThat(doubleSequence, contains(2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void mapToCharFunction() {
		CharSeq emptyChars = empty.toChars(e -> (char) (e.getValue() + 'a' - 1));
		twice(() -> assertThat(emptyChars, is(emptyIterable())));

		CharSeq charSeq = _12345.toChars(e -> (char) (e.getValue() + 'a' - 1));
		twice(() -> assertThat(charSeq, contains('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void mapToIntFunction() {
		IntSequence emptyInts = empty.toInts(e -> e.getValue() + 1);
		twice(() -> assertThat(emptyInts, is(emptyIterable())));

		IntSequence intSequence = _12345.toInts(e -> e.getValue() + 1);
		twice(() -> assertThat(intSequence, containsInts(2, 3, 4, 5, 6)));
	}

	@Test
	public void mapToLongFunction() {
		LongSequence emptyLongs = empty.toLongs(e -> e.getValue() + 1);
		twice(() -> assertThat(emptyLongs, is(emptyIterable())));

		LongSequence longSequence = _12345.toLongs(e -> e.getValue() + 1);
		twice(() -> assertThat(longSequence, containsLongs(2L, 3L, 4L, 5L, 6L)));
	}

	@Test
	public void mapToDoubleFunction() {
		DoubleSequence emptyDoubles = empty.toDoubles(e -> e.getValue() + 1);
		twice(() -> assertThat(emptyDoubles, is(emptyIterable())));

		DoubleSequence doubleSequence = _12345.toDoubles(e -> e.getValue() + 1);
		twice(() -> assertThat(doubleSequence, contains(2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void repeat() {
		EntrySequence<String, Integer> repeatEmpty = empty.repeat();
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		EntrySequence<String, Integer> repeatOne = _1.repeat();
		twice(() -> assertThat(repeatOne.limit(3),
		                       contains(Maps.entry("1", 1), Maps.entry("1", 1), Maps.entry("1", 1))));

		EntrySequence<String, Integer> repeatTwo = _12.repeat();
		twice(() -> assertThat(repeatTwo.limit(5),
		                       contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("1", 1), Maps.entry("2", 2),
		                                Maps.entry("1", 1))));

		EntrySequence<String, Integer> repeatThree = _123.repeat();
		twice(() -> assertThat(repeatThree.limit(8),
		                       contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("1", 1),
		                                Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("1", 1),
		                                Maps.entry("2", 2))));

		EntrySequence<String, Integer> repeatVarying = EntrySequence.from(new Iterable<Entry<String, Integer>>() {
			private List<Entry<String, Integer>> list =
					asList(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3));
			int end = list.size();

			@Override
			public Iterator<Entry<String, Integer>> iterator() {
				List<Entry<String, Integer>> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				return subList.iterator();
			}
		}).repeat();
		assertThat(repeatVarying,
		           contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("1", 1),
		                    Maps.entry("2", 2), Maps.entry("1", 1)));
	}

	@Test
	public void repeatTwice() {
		EntrySequence<String, Integer> repeatEmpty = empty.repeat(2);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		EntrySequence<String, Integer> repeatOne = _1.repeat(2);
		twice(() -> assertThat(repeatOne, contains(Maps.entry("1", 1), Maps.entry("1", 1))));

		EntrySequence<String, Integer> repeatTwo = _12.repeat(2);
		twice(() -> assertThat(repeatTwo, contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("1", 1),
		                                           Maps.entry("2", 2))));

		EntrySequence<String, Integer> repeatThree = _123.repeat(2);
		twice(() -> assertThat(repeatThree,
		                       contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("1", 1),
		                                Maps.entry("2", 2), Maps.entry("3", 3))));

		EntrySequence<String, Integer> repeatVarying = EntrySequence.from(new Iterable<Entry<String, Integer>>() {
			private List<Entry<String, Integer>> list =
					asList(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3));
			int end = list.size();

			@Override
			public Iterator<Entry<String, Integer>> iterator() {
				List<Entry<String, Integer>> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				return subList.iterator();
			}
		}).repeat(2);
		assertThat(repeatVarying,
		           contains(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("1", 1),
		                    Maps.entry("2", 2)));
	}

	@Test
	public void repeatZero() {
		EntrySequence<String, Integer> repeatEmpty = empty.repeat(0);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		EntrySequence<String, Integer> repeatOne = _1.repeat(0);
		twice(() -> assertThat(repeatOne, is(emptyIterable())));

		EntrySequence<String, Integer> repeatTwo = _12.repeat(0);
		twice(() -> assertThat(repeatTwo, is(emptyIterable())));

		EntrySequence<String, Integer> repeatThree = _123.repeat(0);
		twice(() -> assertThat(repeatThree, is(emptyIterable())));
	}

	@Test
	public void generate() {
		Queue<Entry<String, Integer>> queue = new ArrayDeque<>(asList(Maps.entry("1", 1), Maps.entry("2", 2),
		                                                              Maps.entry("3", 3), Maps.entry("4", 4),
		                                                              Maps.entry("5", 5)));
		EntrySequence<String, Integer> sequence = EntrySequence.generate(queue::poll);

		assertThat(sequence, beginsWith(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
		                                Maps.entry("5", 5), null));
		assertThat(sequence, beginsWith((Entry<String, Integer>) null));
	}

	@Test
	public void multiGenerate() {
		EntrySequence<String, Integer> sequence = EntrySequence.multiGenerate(() -> {
			Queue<Entry<String, Integer>> queue = new ArrayDeque<>(asList(Maps.entry("1", 1), Maps.entry("2", 2),
			                                                              Maps.entry("3", 3), Maps.entry("4", 4),
			                                                              Maps.entry("5", 5)));
			return queue::poll;
		});

		twice(() -> assertThat(sequence, beginsWith(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3),
		                                            Maps.entry("4", 4), Maps.entry("5", 5), null)));
	}

	@Test
	public void reverse() {
		EntrySequence<String, Integer> emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		EntrySequence<String, Integer> oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, contains(Maps.entry("1", 1))));

		EntrySequence<String, Integer> twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, contains(Maps.entry("2", 2), Maps.entry("1", 1))));

		EntrySequence<String, Integer> threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, contains(Maps.entry("3", 3), Maps.entry("2", 2), Maps.entry("1", 1))));

		EntrySequence<String, Integer> nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed,
		                       contains(Maps.entry("9", 9), Maps.entry("8", 8), Maps.entry("7", 7), Maps.entry("6", 6),
		                                Maps.entry("5", 5), Maps.entry("4", 4), Maps.entry("3", 3), Maps.entry("2", 2),
		                                Maps.entry("1", 1))));
	}

	@Test
	public void shuffle() {
		EntrySequence<String, Integer> emptyShuffled = empty.shuffle();
		twice(() -> assertThat(emptyShuffled, is(emptyIterable())));

		EntrySequence<String, Integer> oneShuffled = _1.shuffle();
		twice(() -> assertThat(oneShuffled, contains(Maps.entry("1", 1))));

		EntrySequence<String, Integer> twoShuffled = _12.shuffle();
		twice(() -> assertThat(twoShuffled, containsInAnyOrder(Maps.entry("1", 1), Maps.entry("2", 2))));

		EntrySequence<String, Integer> threeShuffled = _123.shuffle();
		twice(() -> assertThat(threeShuffled,
		                       containsInAnyOrder(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3))));

		EntrySequence<String, Integer> nineShuffled = _123456789.shuffle();
		twice(() -> assertThat(nineShuffled,
		                       containsInAnyOrder(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3),
		                                          Maps.entry("4", 4), Maps.entry("5", 5), Maps.entry("6", 6),
		                                          Maps.entry("7", 7), Maps.entry("8", 8), Maps.entry("9", 9))));
	}

	@Test
	public void shuffleWithRandomSource() {
		EntrySequence<String, Integer> emptyShuffled = empty.shuffle(new Random(17));
		twice(() -> assertThat(emptyShuffled, is(emptyIterable())));

		EntrySequence<String, Integer> oneShuffled = _1.shuffle(new Random(17));
		twice(() -> assertThat(oneShuffled, contains(Maps.entry("1", 1))));

		EntrySequence<String, Integer> twoShuffled = _12.shuffle(new Random(17));
		assertThat(twoShuffled, contains(Maps.entry("1", 1), Maps.entry("2", 2)));
		assertThat(twoShuffled, contains(Maps.entry("1", 1), Maps.entry("2", 2)));
		assertThat(twoShuffled, contains(Maps.entry("1", 1), Maps.entry("2", 2)));
		assertThat(twoShuffled, contains(Maps.entry("1", 1), Maps.entry("2", 2)));
		assertThat(twoShuffled, contains(Maps.entry("2", 2), Maps.entry("1", 1)));
		assertThat(twoShuffled, contains(Maps.entry("2", 2), Maps.entry("1", 1)));
		assertThat(twoShuffled, contains(Maps.entry("1", 1), Maps.entry("2", 2)));
		assertThat(twoShuffled, contains(Maps.entry("1", 1), Maps.entry("2", 2)));

		EntrySequence<String, Integer> threeShuffled = _123.shuffle(new Random(17));
		assertThat(threeShuffled, contains(Maps.entry("3", 3), Maps.entry("2", 2), Maps.entry("1", 1)));
		assertThat(threeShuffled, contains(Maps.entry("1", 1), Maps.entry("3", 3), Maps.entry("2", 2)));

		EntrySequence<String, Integer> nineShuffled = _123456789.shuffle(new Random(17));
		assertThat(nineShuffled,
		           contains(Maps.entry("1", 1), Maps.entry("8", 8), Maps.entry("4", 4), Maps.entry("2", 2),
		                    Maps.entry("6", 6), Maps.entry("3", 3), Maps.entry("5", 5), Maps.entry("9", 9),
		                    Maps.entry("7", 7)));
		assertThat(nineShuffled,
		           contains(Maps.entry("6", 6), Maps.entry("3", 3), Maps.entry("5", 5), Maps.entry("2", 2),
		                    Maps.entry("9", 9), Maps.entry("4", 4), Maps.entry("1", 1), Maps.entry("7", 7),
		                    Maps.entry("8", 8)));
	}

	@Test
	public void shuffleWithRandomSupplier() {
		EntrySequence<String, Integer> emptyShuffled = empty.shuffle(() -> new Random(17));
		twice(() -> assertThat(emptyShuffled, is(emptyIterable())));

		EntrySequence<String, Integer> oneShuffled = _1.shuffle(() -> new Random(17));
		twice(() -> assertThat(oneShuffled, contains(Maps.entry("1", 1))));

		EntrySequence<String, Integer> twoShuffled = _12.shuffle(() -> new Random(17));
		twice(() -> assertThat(twoShuffled, contains(Maps.entry("1", 1), Maps.entry("2", 2))));

		EntrySequence<String, Integer> threeShuffled = _123.shuffle(() -> new Random(17));
		twice(() -> assertThat(threeShuffled, contains(Maps.entry("3", 3), Maps.entry("2", 2), Maps.entry("1", 1))));

		EntrySequence<String, Integer> nineShuffled = _123456789.shuffle(() -> new Random(17));
		twice(() -> assertThat(nineShuffled,
		                       contains(Maps.entry("1", 1), Maps.entry("8", 8), Maps.entry("4", 4), Maps.entry("2", 2),
		                                Maps.entry("6", 6), Maps.entry("3", 3), Maps.entry("5", 5), Maps.entry("9", 9),
		                                Maps.entry("7", 7))));
	}

	@Test
	public void flatten() {
		Sequence<Object> emptyFlattened = empty.flatten();
		twice(() -> assertThat(emptyFlattened, is(emptyIterable())));

		Sequence<Object> oneFlattened = _1.flatten();
		twice(() -> assertThat(oneFlattened, contains("1", 1)));

		Sequence<Object> twoFlattened = _12.flatten();
		twice(() -> assertThat(twoFlattened, contains("1", 1, "2", 2)));

		Sequence<Object> fiveFlattened = _12345.flatten();
		twice(() -> assertThat(fiveFlattened, contains("1", 1, "2", 2, "3", 3, "4", 4, "5", 5)));
	}

	@Test
	public void flattenFunction() {
		EntrySequence<String, Integer> flattened = _123.flatten(entry -> Iterables.of(entry, Maps.entry("0", 0)));
		twice(() -> assertThat(flattened,
		                       contains(Maps.entry("1", 1), Maps.entry("0", 0), Maps.entry("2", 2), Maps.entry("0", 0),
		                                Maps.entry("3", 3), Maps.entry("0", 0))));
	}

	@Test
	public void flattenBiFunction() {
		EntrySequence<String, Integer> flattened =
				_123.flatten((k, v) -> Iterables.of(Maps.entry(k, v), Maps.entry("0", 0)));
		twice(() -> assertThat(flattened,
		                       contains(Maps.entry("1", 1), Maps.entry("0", 0), Maps.entry("2", 2), Maps.entry("0", 0),
		                                Maps.entry("3", 3), Maps.entry("0", 0))));
	}

	@Test
	public void flattenKeys() {
		EntrySequence<String, Integer> flattened = EntrySequence.<Iterable<String>, Integer>ofEntries(
				Iterables.of("1", "2", "3"), 1, Iterables.empty(), 2, Iterables.of("5", "6", "7"), 3)
				.flattenKeys(Entry::getKey);
		twice(() -> assertThat(flattened,
		                       contains(Maps.entry("1", 1), Maps.entry("2", 1), Maps.entry("3", 1), Maps.entry("5", 3),
		                                Maps.entry("6", 3), Maps.entry("7", 3))));
	}

	@Test
	public void flattenValues() {
		EntrySequence<String, Integer> flattened = EntrySequence.<String, Iterable<Integer>>ofEntries(
				"1", Iterables.of(1, 2, 3), "2", Iterables.empty(), "3", Iterables.of(2, 3, 4))
				.flattenValues(Entry::getValue);
		twice(() -> assertThat(flattened,
		                       contains(Maps.entry("1", 1), Maps.entry("1", 2), Maps.entry("1", 3), Maps.entry("3", 2),
		                                Maps.entry("3", 3), Maps.entry("3", 4))));
	}

	@Test
	public void clear() {
		Map<String, Integer> original = Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build();

		EntrySequence<String, Integer> filtered = EntrySequence.from(original).filter((k, v) -> v % 2 != 0);
		filtered.clear();

		twice(() -> assertThat(filtered, is(emptyIterable())));
		twice(() -> assertThat(original.entrySet(), contains(Maps.entry("2", 2), Maps.entry("4", 4))));
	}

	@Test
	public void isEmpty() {
		twice(() -> assertThat(empty.isEmpty(), is(true)));
		twice(() -> assertThat(_1.isEmpty(), is(false)));
		twice(() -> assertThat(_12.isEmpty(), is(false)));
		twice(() -> assertThat(_12345.isEmpty(), is(false)));
	}

	@Test
	public void containsEntry() {
		assertThat(empty.contains(Maps.entry("17", 17)), is(false));

		assertThat(_12345.contains(Maps.entry("1", 1)), is(true));
		assertThat(_12345.contains(Maps.entry("3", 3)), is(true));
		assertThat(_12345.contains(Maps.entry("5", 5)), is(true));
		assertThat(_12345.contains(Maps.entry("17", 17)), is(false));
	}

	@Test
	public void containsEntryComponents() {
		assertThat(empty.contains("17", 17), is(false));

		assertThat(_12345.contains("1", 1), is(true));
		assertThat(_12345.contains("3", 3), is(true));
		assertThat(_12345.contains("5", 5), is(true));
		assertThat(_12345.contains("17", 17), is(false));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(), is(true));
		assertThat(empty.containsAll(Maps.entry("17", 17), Maps.entry("18", 18), Maps.entry("19", 19)), is(false));

		assertThat(_12345.containsAll(), is(true));
		assertThat(_12345.containsAll(Maps.entry("1", 1)), is(true));
		assertThat(_12345.containsAll(Maps.entry("1", 1), Maps.entry("3", 3), Maps.entry("5", 5)), is(true));
		assertThat(_12345.containsAll(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
		                              Maps.entry("5", 5)), is(true));
		assertThat(_12345.containsAll(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
		                              Maps.entry("5", 5), Maps.entry("17", 17)), is(false));
		assertThat(_12345.containsAll(Maps.entry("17", 17), Maps.entry("18", 18), Maps.entry("19", 19)), is(false));
	}

	@Test
	public void containsAny() {
		assertThat(empty.containsAny(), is(false));
		assertThat(empty.containsAny(Maps.entry("17", 17), Maps.entry("18", 18), Maps.entry("19", 19)), is(false));

		assertThat(_12345.containsAny(), is(false));
		assertThat(_12345.containsAny(Maps.entry("1", 1)), is(true));
		assertThat(_12345.containsAny(Maps.entry("1", 1), Maps.entry("3", 3), Maps.entry("5", 5)), is(true));
		assertThat(_12345.containsAny(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
		                              Maps.entry("5", 5)), is(true));
		assertThat(_12345.containsAny(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
		                              Maps.entry("5", 5), Maps.entry("17", 17)), is(true));
		assertThat(_12345.containsAny(Maps.entry("17", 17), Maps.entry("18", 18), Maps.entry("19", 19)), is(false));
	}

	@Test
	public void containsAllIterable() {
		assertThat(empty.containsAll(Iterables.of()), is(true));
		assertThat(empty.containsAll(Iterables.of(Maps.entry("17", 17), Maps.entry("18", 18), Maps.entry("19", 19))),
		           is(false));

		assertThat(_12345.containsAll(Iterables.of()), is(true));
		assertThat(_12345.containsAll(Iterables.of(Maps.entry("1", 1))), is(true));
		assertThat(_12345.containsAll(Iterables.of(Maps.entry("1", 1), Maps.entry("3", 3), Maps.entry("5", 5))),
		           is(true));
		assertThat(_12345.containsAll(
				Iterables.of(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
				             Maps.entry("5", 5))), is(true));
		assertThat(_12345.containsAll(
				Iterables.of(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
				             Maps.entry("5", 5), Maps.entry("17", 17))), is(false));
		assertThat(_12345.containsAll(Iterables.of(Maps.entry("17", 17), Maps.entry("18", 18), Maps.entry("19", 19))),
		           is(false));
	}

	@Test
	public void containsAnyIterable() {
		assertThat(empty.containsAny(Iterables.of()), is(false));
		assertThat(empty.containsAny(Iterables.of(Maps.entry("17", 17), Maps.entry("18", 18), Maps.entry("19", 19))),
		           is(false));

		assertThat(_12345.containsAny(Iterables.of()), is(false));
		assertThat(_12345.containsAny(Iterables.of(Maps.entry("1", 1))), is(true));
		assertThat(_12345.containsAny(Iterables.of(Maps.entry("1", 1), Maps.entry("3", 3), Maps.entry("5", 5))),
		           is(true));
		assertThat(_12345.containsAny(
				Iterables.of(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
				             Maps.entry("5", 5))), is(true));
		assertThat(_12345.containsAny(
				Iterables.of(Maps.entry("1", 1), Maps.entry("2", 2), Maps.entry("3", 3), Maps.entry("4", 4),
				             Maps.entry("5", 5), Maps.entry("17", 17))), is(true));
		assertThat(_12345.containsAny(Iterables.of(Maps.entry("17", 17), Maps.entry("18", 18), Maps.entry("19", 19))),
		           is(false));
	}
}
