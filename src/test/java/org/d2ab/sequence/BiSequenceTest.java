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
import org.d2ab.function.QuaternaryFunction;
import org.d2ab.iterable.Iterables;
import org.d2ab.iterator.Iterators;
import org.d2ab.util.Pair;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.IsIterableBeginningWith.beginsWith;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@SuppressWarnings("unchecked")
public class BiSequenceTest {
	private final BiSequence<String, Integer> empty = BiSequence.empty();
	private final BiSequence<String, Integer> _1 = BiSequence.of(Pair.of("1", 1));
	private final BiSequence<String, Integer> _12 = BiSequence.of(Pair.of("1", 1), Pair.of("2", 2));
	private final BiSequence<String, Integer> _123 = BiSequence.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3));
	private final BiSequence<String, Integer> _1234 =
			BiSequence.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4));
	private final BiSequence<String, Integer> _12345 =
			BiSequence.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5));
	private final BiSequence<String, Integer> _123456789 =
			BiSequence.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5),
			              Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8), Pair.of("9", 9));
	private final BiSequence<String, Integer> random1 = BiSequence.of(Pair.of("17", 17));
	private final BiSequence<String, Integer> random2 = BiSequence.of(Pair.of("17", 17), Pair.of("32", 32));
	private final BiSequence<String, Integer> random3 =
			BiSequence.of(Pair.of("4", 4), Pair.of("2", 2), Pair.of("3", 3));
	private final BiSequence<String, Integer> random9 =
			BiSequence.of(Pair.of("67", 67), Pair.of("5", 5), Pair.of("43", 43), Pair.of("3", 3), Pair.of("5", 5),
			              Pair.of("7", 7), Pair.of("24", 24), Pair.of("5", 5), Pair.of("67", 67));
	private final Pair<String, Integer>[] entries123 = new Pair[]{Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)};
	private final Pair<String, Integer>[] entries12345 =
			new Pair[]{Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5)};
	private final Pair<String, Integer>[] entries456 = new Pair[]{Pair.of("4", 4), Pair.of("5", 5), Pair.of("6", 6)};
	private final Pair<String, Integer>[] entries789 = new Pair[]{Pair.of("7", 7), Pair.of("8", 8), Pair.of("9", 9)};

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void ofNone() {
		BiSequence<String, Integer> sequence = BiSequence.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void ofWithNulls() {
		BiSequence<String, Integer> sequence =
				BiSequence.of(Pair.of("1", 1), Pair.of(null, 2), Pair.of("3", 3), Pair.of("4", null),
				              Pair.of(null, null));

		twice(() -> assertThat(sequence,
		                       contains(Pair.of("1", 1), Pair.of(null, 2), Pair.of("3", 3), Pair.of("4", null),
		                                Pair.of(null, null))));
	}

	@Test
	public void ofOne() {
		twice(() -> assertThat(_1, contains(Pair.of("1", 1))));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(_123, contains(entries123)));
	}

	@Test
	public void ofPair() {
		assertThat(BiSequence.ofPair("1", 1), contains(Pair.of("1", 1)));
	}

	@Test
	public void ofPairs() {
		assertThat(BiSequence.ofPairs("1", 1, "2", 2, "3", 3), contains(entries123));
	}

	@Test
	public void forLoop() {
		twice(() -> {
			for (Pair<String, Integer> ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			int expected = 1;
			for (Pair<String, Integer> i : _12345)
				assertThat(i, is(Pair.of(String.valueOf(expected), expected++)));

			assertThat(expected, is(6));
		});
	}

	@Test
	public void forEach() {
		twice(() -> {
			empty.forEach(p -> fail("Should not get called"));

			AtomicInteger value = new AtomicInteger(1);
			_1.forEach(p -> assertThat(p, is(Pair.of(String.valueOf(value.get()), value.getAndIncrement()))));

			value.set(1);
			_12.forEach(p -> assertThat(p, is(Pair.of(String.valueOf(value.get()), value.getAndIncrement()))));

			value.set(1);
			_12345.forEach(p -> assertThat(p, is(Pair.of(String.valueOf(value.get()), value.getAndIncrement()))));
		});
	}

	@Test
	public void forEachBiConsumer() {
		twice(() -> {
			empty.forEach((l, r) -> fail("Should not get called"));

			AtomicInteger value = new AtomicInteger(1);
			_1.forEach((l, r) -> {
				assertThat(l, is(String.valueOf(value.get())));
				assertThat(r, is(value.getAndIncrement()));
			});

			value.set(1);
			_12.forEach((l, r) -> {
				assertThat(l, is(String.valueOf(value.get())));
				assertThat(r, is(value.getAndIncrement()));
			});

			value.set(1);
			_12345.forEach((l, r) -> {
				assertThat(l, is(String.valueOf(value.get())));
				assertThat(r, is(value.getAndIncrement()));
			});
		});
	}

	@Test
	public void iterator() {
		twice(() -> {
			Iterator iterator = _123.iterator();

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(Pair.of("1", 1)));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(Pair.of("2", 2)));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(Pair.of("3", 3)));

			assertThat(iterator.hasNext(), is(false));
			assertThat(iterator.hasNext(), is(false));
		});
	}

	@Test
	public void fromBiSequence() {
		BiSequence<String, Integer> sequence = BiSequence.from(_12345);

		twice(() -> assertThat(sequence, contains(entries12345)));
	}

	@Test
	public void fromIterable() {
		BiSequence<String, Integer> sequence = BiSequence.from(Iterables.of(entries12345));

		twice(() -> assertThat(sequence, contains(entries12345)));
	}

	@Test
	public void fromIterables() {
		Iterable<Pair<String, Integer>> first = Iterables.of(entries123);
		Iterable<Pair<String, Integer>> second = Iterables.of(entries456);
		Iterable<Pair<String, Integer>> third = Iterables.of(entries789);

		BiSequence<String, Integer> sequence = BiSequence.from(first, second, third);

		twice(() -> assertThat(sequence, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                          Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));
	}

	@Test
	public void fromNoIterables() {
		BiSequence<String, Integer> sequence = BiSequence.from(new Iterable[0]);

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void onceIterator() {
		BiSequence<String, Integer> sequence = BiSequence.once(Iterators.of(entries12345));

		assertThat(sequence, contains(entries12345));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceStream() {
		BiSequence<String, Integer> sequence = BiSequence.once(Stream.of(entries12345));

		assertThat(sequence, contains(entries12345));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void cacheIterable() {
		List<Pair<String, Integer>> list = new ArrayList<>(asList(entries12345));
		BiSequence<String, Integer> cached = BiSequence.cache(list::iterator);
		list.set(0, Pair.of("17", 17));

		twice(() -> assertThat(cached, contains(entries12345)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void cacheIterator() {
		List<Pair<String, Integer>> list = new ArrayList<>(asList(entries12345));
		BiSequence<String, Integer> cached = BiSequence.cache(list.iterator());
		list.set(0, Pair.of("17", 17));

		twice(() -> assertThat(cached, contains(entries12345)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void cacheStream() {
		List<Pair<String, Integer>> list = new ArrayList<>(asList(entries12345));
		BiSequence<String, Integer> cached = BiSequence.cache(list.stream());
		list.set(0, Pair.of("17", 17));

		twice(() -> assertThat(cached, contains(entries12345)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void skip() {
		BiSequence<String, Integer> skipNone = _123.skip(0);
		twice(() -> assertThat(skipNone, contains(entries123)));

		BiSequence<String, Integer> skipOne = _123.skip(1);
		twice(() -> assertThat(skipOne, contains(Pair.of("2", 2), Pair.of("3", 3))));

		BiSequence<String, Integer> skipTwo = _123.skip(2);
		twice(() -> assertThat(skipTwo, contains(Pair.of("3", 3))));

		BiSequence<String, Integer> skipThree = _123.skip(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		BiSequence<String, Integer> skipFour = _123.skip(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void skipTail() {
		BiSequence<String, Integer> skipNone = _123.skipTail(0);
		twice(() -> assertThat(skipNone, contains(entries123)));

		BiSequence<String, Integer> skipOne = _123.skipTail(1);
		twice(() -> assertThat(skipOne, contains(Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> skipTwo = _123.skipTail(2);
		twice(() -> assertThat(skipTwo, contains(Pair.of("1", 1))));

		BiSequence<String, Integer> skipThree = _123.skipTail(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		BiSequence<String, Integer> skipFour = _123.skipTail(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		BiSequence<String, Integer> limitZero = _123.limit(0);
		twice(() -> assertThat(limitZero, is(emptyIterable())));

		BiSequence<String, Integer> limitOne = _123.limit(1);
		twice(() -> assertThat(limitOne, contains(Pair.of("1", 1))));

		BiSequence<String, Integer> limitTwo = _123.limit(2);
		twice(() -> assertThat(limitTwo, contains(Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> limitThree = _123.limit(3);
		twice(() -> assertThat(limitThree, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));

		BiSequence<String, Integer> limitFour = _123.limit(4);
		twice(() -> assertThat(limitFour, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void append() {
		BiSequence<String, Integer> appended = _123.append(BiSequence.of(entries456)).append(BiSequence.of
				(entries789));

		twice(() -> assertThat(appended, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                          Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));
	}

	@Test
	public void appendPair() {
		BiSequence<String, Integer> appended = _123.appendPair("4", 4);
		twice(() -> assertThat(appended, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4)
		)));
	}

	@Test
	public void appendIsLazy() {
		Iterator<Pair<String, Integer>> first = Iterators.of(entries123);
		Iterator<Pair<String, Integer>> second = Iterators.of(entries456);

		BiSequence<String, Integer> then = BiSequence.once(first).append(() -> second);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));

		assertThat(then, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5),
		                          Pair.of("6", 6)));
		assertThat(then, is(emptyIterable())); // iterators exhausted on second run
	}

	@Test
	public void appendIsLazyWhenSkippingHasNext() {
		Iterator<Pair<String, Integer>> first = Iterators.of(Pair.of("1", 1));
		Iterator<Pair<String, Integer>> second = Iterators.of(Pair.of("2", 2));

		BiSequence<String, Integer> sequence = BiSequence.once(first).append(BiSequence.once(second));

		// check delayed iteration
		Iterator<Pair<String, Integer>> iterator = sequence.iterator();
		assertThat(iterator.next(), is(Pair.of("1", 1)));
		assertThat(iterator.next(), is(Pair.of("2", 2)));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void toSequence() {
		Sequence<Pair<String, Integer>> emptySequence = empty.toSequence();
		twice(() -> assertThat(emptySequence, is(emptyIterable())));

		Sequence<Pair<String, Integer>> sequence = _12345.toSequence();
		twice(() -> assertThat(sequence, contains(entries12345)));
	}

	@Test
	public void toSequenceKeyValueMapper() {
		Sequence<String> emptyKeySequence = empty.toSequence((l, r) -> l);
		twice(() -> assertThat(emptyKeySequence, is(emptyIterable())));

		Sequence<Integer> emptyValueSequence = empty.toSequence((l, r) -> r);
		twice(() -> assertThat(emptyValueSequence, is(emptyIterable())));

		Sequence<String> keySequence = _12345.toSequence((l, r) -> l);
		twice(() -> assertThat(keySequence, contains("1", "2", "3", "4", "5")));

		Sequence<Integer> valueSequence = _12345.toSequence((l, r) -> r);
		twice(() -> assertThat(valueSequence, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void toSequenceEntryMapper() {
		Sequence<String> emptyKeySequence = empty.toSequence(Pair::getLeft);
		twice(() -> assertThat(emptyKeySequence, is(emptyIterable())));

		Sequence<Integer> emptyValueSequence = empty.toSequence(Pair::getRight);
		twice(() -> assertThat(emptyValueSequence, is(emptyIterable())));

		Sequence<String> keySequence = _12345.toSequence(Pair::getLeft);
		twice(() -> assertThat(keySequence, contains("1", "2", "3", "4", "5")));

		Sequence<Integer> valueSequence = _12345.toSequence(Pair::getRight);
		twice(() -> assertThat(valueSequence, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void toBiSequence() {
		EntrySequence<String, Integer> emptySequence = empty.toEntrySequence();
		twice(() -> assertThat(emptySequence, is(emptyIterable())));

		EntrySequence<String, Integer> sequence = _12345.toEntrySequence();
		twice(() -> assertThat(sequence, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                          Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void filter() {
		BiSequence<String, Integer> filtered = _123456789.filter((s, i) -> i % 2 == 0);

		twice(() -> assertThat(filtered, contains(Pair.of("2", 2), Pair.of("4", 4), Pair.of("6", 6),
		                                          Pair.of("8", 8))));
	}

	@Test
	public void includingArray() {
		BiSequence<String, Integer> emptyIncluding = empty.including(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5),
		                                                             Pair.of("17", 17));
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));

		BiSequence<String, Integer> including = _12345.including(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5),
		                                                         Pair.of("17", 17));
		twice(() -> assertThat(including, contains(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5))));

		BiSequence<String, Integer> includingAll = _12345.including(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                                            Pair.of("4", 4), Pair.of("5", 5),
		                                                            Pair.of("17", 17));
		twice(() -> assertThat(includingAll, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                              Pair.of("4", 4), Pair.of("5", 5))));

		BiSequence<String, Integer> includingNone = _12345.including();
		twice(() -> assertThat(includingNone, is(emptyIterable())));
	}

	@Test
	public void includingIterable() {
		BiSequence<String, Integer> emptyIncluding = empty.including(
				Iterables.of(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5), Pair.of("17", 17)));
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));

		BiSequence<String, Integer> including = _12345.including(
				Iterables.of(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5), Pair.of("17", 17)));
		twice(() -> assertThat(including, contains(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5))));

		BiSequence<String, Integer> includingAll = _12345.including(
				Iterables.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5),
				             Pair.of("17", 17)));
		twice(() -> assertThat(includingAll, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                              Pair.of("4", 4), Pair.of("5", 5))));

		BiSequence<String, Integer> includingNone = _12345.including(Iterables.of());
		twice(() -> assertThat(includingNone, is(emptyIterable())));
	}

	@Test
	public void excludingArray() {
		BiSequence<String, Integer> emptyExcluding = empty.excluding(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5),
		                                                             Pair.of("17", 17));
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));

		BiSequence<String, Integer> excluding = _12345.excluding(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5),
		                                                         Pair.of("17", 17));
		twice(() -> assertThat(excluding, contains(Pair.of("2", 2), Pair.of("4", 4))));

		BiSequence<String, Integer> excludingAll = _12345.excluding(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                                            Pair.of("4", 4), Pair.of("5", 5),
		                                                            Pair.of("17", 17));
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		BiSequence<String, Integer> excludingNone = _12345.excluding();
		twice(() -> assertThat(excludingNone, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                               Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void excludingIterable() {
		BiSequence<String, Integer> emptyExcluding = empty.excluding(
				Iterables.of(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5), Pair.of("17", 17)));
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));

		BiSequence<String, Integer> excluding = _12345.excluding(
				Iterables.of(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5), Pair.of("17", 17)));
		twice(() -> assertThat(excluding, contains(Pair.of("2", 2), Pair.of("4", 4))));

		BiSequence<String, Integer> excludingAll = _12345.excluding(
				Iterables.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5),
				             Pair.of("17", 17)));
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		BiSequence<String, Integer> excludingNone = _12345.excluding(Iterables.of());
		twice(() -> assertThat(excludingNone, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                               Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void filterAndMap() {
		BiSequence<Integer, String> evens =
				_123456789.filter((s, x) -> x % 2 == 0).map(Integer::parseInt, Object::toString);

		twice(() -> assertThat(evens, contains(Pair.of(2, "2"), Pair.of(4, "4"), Pair.of(6, "6"), Pair.of(8, "8"))));
	}

	@Test
	public void mapBiFunction() {
		BiSequence<Integer, String> mapped = _123.map((s, i) -> Pair.of(parseInt(s), i.toString()));
		twice(() -> assertThat(mapped, contains(Pair.of(1, "1"), Pair.of(2, "2"), Pair.of(3, "3"))));
	}

	@Test
	public void mapTwoFunctions() {
		BiSequence<Integer, String> mapped = _123.map(Integer::parseInt, Object::toString);
		twice(() -> assertThat(mapped, contains(Pair.of(1, "1"), Pair.of(2, "2"), Pair.of(3, "3"))));
	}

	@Test
	public void mapPairFunction() {
		BiSequence<Integer, String> mapped = _123.map(p -> Pair.of(parseInt(p.getLeft()), p.getRight().toString()));
		twice(() -> assertThat(mapped, contains(Pair.of(1, "1"), Pair.of(2, "2"), Pair.of(3, "3"))));
	}

	@Test
	public void mapIsLazy() {
		BiSequence<Integer, String> mapped = BiSequence.of(Pair.of("1", 1), null) // null will be hit when mapping
		                                               .map((s, i) -> Pair.of(parseInt(s), i.toString()));

		twice(() -> {
			// NPE here if not lazy
			Iterator<Pair<Integer, String>> iterator = mapped.iterator();

			assertThat(iterator.next(), is(Pair.of(1, "1")));

			try {
				iterator.next();
				fail("Expected NPE");
			} catch (NullPointerException ignored) {
				// expected
			}
		});
	}

	@Test
	public void recurse() {
		BiSequence<String, Integer> sequence =
				BiSequence.recurse("1", 1, (k, v) -> Pair.of(String.valueOf(v + 1), v + 1));
		twice(() -> assertThat(sequence.limit(3), contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void recurseTwins() {
		BiSequence<String, Integer> sequence =
				BiSequence.recurse(1, "1", (k, v) -> Pair.of(v, k), (k, v) -> Pair.of(v + 1, String.valueOf(v + 1)));
		twice(() -> assertThat(sequence.limit(3), contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void until() {
		BiSequence<String, Integer> sequence = BiSequence.from(_12345).until(Pair.of("4", 4));
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void endingAt() {
		BiSequence<String, Integer> sequence = BiSequence.from(_12345).endingAt(Pair.of("3", 3));
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void untilPredicate() {
		BiSequence<String, Integer> sequence = BiSequence.from(_12345).until(e -> e.equals(Pair.of("4", 4)));
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void endingAtPredicate() {
		BiSequence<String, Integer> sequence = BiSequence.from(_12345).endingAt(e -> e.equals(Pair.of("3", 3)));
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void untilBinary() {
		BiSequence<String, Integer> sequence = BiSequence.from(_12345).until("4", 4);
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void endingAtBinary() {
		BiSequence<String, Integer> sequence = BiSequence.from(_12345).endingAt("3", 3);
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void untilBinaryPredicate() {
		BiSequence<String, Integer> sequence = BiSequence.from(_12345).until((l, r) -> l.equals("4") && r == 4);
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void endingAtBinaryPredicate() {
		BiSequence<String, Integer> sequence = BiSequence.from(_12345).endingAt((l, r) -> l.equals("3") && r == 3);
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void startingAfter() {
		BiSequence<String, Integer> startingEmpty = empty.startingAfter(Pair.of("5", 5));
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		BiSequence<String, Integer> sequence = _123456789.startingAfter(Pair.of("5", 5));
		twice(() -> assertThat(sequence, contains(Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8), Pair.of("9", 9)
		)));

		BiSequence<String, Integer> noStart = _12345.startingAfter(Pair.of("10", 10));
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingAfterPredicate() {
		BiSequence<String, Integer> startingEmpty = empty.startingAfter(p -> p.getRight() == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		BiSequence<String, Integer> sequence = _123456789.startingAfter(p -> p.getRight() == 5);
		twice(() -> assertThat(sequence, contains(Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8), Pair.of("9", 9)
		)));

		BiSequence<String, Integer> noStart = _12345.startingAfter(p -> p.getRight() == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingAfterBiPredicate() {
		BiSequence<String, Integer> startingEmpty = empty.startingAfter((l, r) -> r == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		BiSequence<String, Integer> sequence = _123456789.startingAfter((l, r) -> r == 5);
		twice(() -> assertThat(sequence, contains(Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8), Pair.of("9", 9)
		)));

		BiSequence<String, Integer> noStart = _12345.startingAfter((l, r) -> r == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFrom() {
		BiSequence<String, Integer> startingEmpty = empty.startingFrom(Pair.of("5", 5));
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		BiSequence<String, Integer> sequence = _123456789.startingFrom(Pair.of("5", 5));
		twice(() -> assertThat(sequence, contains(Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));

		BiSequence<String, Integer> noStart = _12345.startingFrom(Pair.of("10", 10));
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFromPredicate() {
		BiSequence<String, Integer> startingEmpty = empty.startingFrom(p -> p.getRight() == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		BiSequence<String, Integer> sequence = _123456789.startingFrom(p -> p.getRight() == 5);
		twice(() -> assertThat(sequence, contains(Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));

		BiSequence<String, Integer> noStart = _12345.startingFrom(p -> p.getRight() == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFromBiPredicate() {
		BiSequence<String, Integer> startingEmpty = empty.startingFrom((l, r) -> r == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));

		BiSequence<String, Integer> sequence = _123456789.startingFrom((l, r) -> r == 5);
		twice(() -> assertThat(sequence, contains(Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));

		BiSequence<String, Integer> noStart = _12345.startingFrom((l, r) -> r == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void toList() {
		BiSequence<String, Integer> sequence = _12345;

		twice(() -> {
			List<Pair<String, Integer>> list = sequence.toList();
			assertThat(list, instanceOf(ArrayList.class));
			assertThat(list, contains(entries12345));
		});
	}

	@Test
	public void toLinkedList() {
		BiSequence<String, Integer> sequence = _12345;

		twice(() -> {
			List<Pair<String, Integer>> list = sequence.toList(LinkedList::new);
			assertThat(list, instanceOf(LinkedList.class));
			assertThat(list, contains(entries12345));
		});
	}

	@Test
	public void toSet() {
		BiSequence<String, Integer> sequence = _12345;

		twice(() -> {
			Set<Pair<String, Integer>> set = sequence.toSet();
			assertThat(set, instanceOf(HashSet.class));
			assertThat(set, containsInAnyOrder(entries12345));
		});
	}

	@Test
	public void toSortedSet() {
		BiSequence<String, Integer> sequence = _12345;

		twice(() -> {
			SortedSet<Pair<String, Integer>> sortedSet = sequence.toSortedSet();
			assertThat(sortedSet, instanceOf(TreeSet.class));
			assertThat(sortedSet, contains(entries12345));
		});
	}

	@Test
	public void toSetWithType() {
		BiSequence<String, Integer> sequence = _12345;

		twice(() -> {
			Set<Pair<String, Integer>> set = sequence.toSet(LinkedHashSet::new);
			assertThat(set, instanceOf(LinkedHashSet.class));
			assertThat(set, contains(entries12345));
		});
	}

	@Test
	public void toCollection() {
		BiSequence<String, Integer> sequence = _12345;

		twice(() -> {
			Deque<Pair<String, Integer>> deque = sequence.toCollection(ArrayDeque::new);
			assertThat(deque, instanceOf(ArrayDeque.class));
			assertThat(deque, contains(entries12345));
		});
	}

	@Test
	public void toMapFromPairs() {
		Map<String, Integer> original = Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build();

		BiSequence<String, Integer> sequence = BiSequence.from(original);

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
			Deque<Pair<String, Integer>> deque = _123.collect(ArrayDeque::new, ArrayDeque::add);

			assertThat(deque, instanceOf(ArrayDeque.class));
			assertThat(deque, contains(entries123));
		});
	}

	@Test
	public void collectInto() {
		twice(() -> {
			ArrayDeque<Pair<String, Integer>> original = new ArrayDeque<>();
			Deque<Pair<String, Integer>> deque = _123.collectInto(original, ArrayDeque::add);

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
		twice(() -> assertThat(_123.toArray(Pair[]::new), arrayContaining(entries123)));
	}

	@Test
	public void collector() {
		twice(() -> assertThat(_123.collect(Collectors.toList()), contains(entries123)));
	}

	@Test
	public void join() {
		twice(() -> assertThat(_123.join(", "), is("(\"1\", 1), (\"2\", 2), (\"3\", 3)")));
	}

	@Test
	public void joinWithPrefixAndSuffix() {
		twice(() -> assertThat(_123.join("<", ", ", ">"), is("<(\"1\", 1), (\"2\", 2), (\"3\", 3)>")));
	}

	@Test
	public void reduce() {
		BinaryOperator<Pair<String, Integer>> sumPair =
				(r, e) -> Pair.of(r.getLeft() + e.getLeft(), r.getRight() + e.getRight());

		twice(() -> {
			assertThat(empty.reduce(sumPair), is(Optional.empty()));
			assertThat(_1.reduce(sumPair), is(Optional.of(Pair.of("1", 1))));
			assertThat(_12.reduce(sumPair), is(Optional.of(Pair.of("12", 3))));
			assertThat(_123.reduce(sumPair), is(Optional.of(Pair.of("123", 6))));
		});
	}

	@Test
	public void reduceQuaternary() {
		QuaternaryFunction<String, Integer, String, Integer, Pair<String, Integer>> sumPair =
				(rk, rv, ek, ev) -> Pair.of(rk + ek, rv + ev);

		twice(() -> {
			assertThat(empty.reduce(sumPair), is(Optional.empty()));
			assertThat(_1.reduce(sumPair), is(Optional.of(Pair.of("1", 1))));
			assertThat(_12.reduce(sumPair), is(Optional.of(Pair.of("12", 3))));
			assertThat(_123.reduce(sumPair), is(Optional.of(Pair.of("123", 6))));
		});
	}

	@Test
	public void reduceWithIdentity() {
		BinaryOperator<Pair<String, Integer>> sumPair =
				(r, e) -> Pair.of(r.getLeft() + e.getLeft(), r.getRight() + e.getRight());
		twice(() -> {
			assertThat(empty.reduce(Pair.of("17", 17), sumPair), is(Pair.of("17", 17)));
			assertThat(_1.reduce(Pair.of("17", 17), sumPair), is(Pair.of("171", 18)));
			assertThat(_12.reduce(Pair.of("17", 17), sumPair), is(Pair.of("1712", 20)));
			assertThat(_123.reduce(Pair.of("17", 17), sumPair), is(Pair.of("17123", 23)));
		});
	}

	@Test
	public void reduceQuaternaryWithIdentity() {
		QuaternaryFunction<String, Integer, String, Integer, Pair<String, Integer>> sumPair =
				(rk, rv, ek, ev) -> Pair.of(rk + ek, rv + ev);

		twice(() -> {
			assertThat(empty.reduce("17", 17, sumPair), is(Pair.of("17", 17)));
			assertThat(_1.reduce("17", 17, sumPair), is(Pair.of("171", 18)));
			assertThat(_12.reduce("17", 17, sumPair), is(Pair.of("1712", 20)));
			assertThat(_123.reduce("17", 17, sumPair), is(Pair.of("17123", 23)));
		});
	}

	@Test
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(Optional.empty()));
			assertThat(_1.first(), is(Optional.of(Pair.of("1", 1))));
			assertThat(_12.first(), is(Optional.of(Pair.of("1", 1))));
			assertThat(_123.first(), is(Optional.of(Pair.of("1", 1))));
		});
	}

	@Test
	public void second() {
		twice(() -> {
			assertThat(empty.second(), is(Optional.empty()));
			assertThat(_1.second(), is(Optional.empty()));
			assertThat(_12.second(), is(Optional.of(Pair.of("2", 2))));
			assertThat(_123.second(), is(Optional.of(Pair.of("2", 2))));
			assertThat(_1234.second(), is(Optional.of(Pair.of("2", 2))));
		});
	}

	@Test
	public void third() {
		twice(() -> {
			assertThat(empty.third(), is(Optional.empty()));
			assertThat(_1.third(), is(Optional.empty()));
			assertThat(_12.third(), is(Optional.empty()));
			assertThat(_123.third(), is(Optional.of(Pair.of("3", 3))));
			assertThat(_1234.third(), is(Optional.of(Pair.of("3", 3))));
			assertThat(_12345.third(), is(Optional.of(Pair.of("3", 3))));
		});
	}

	@Test
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(Optional.empty()));
			assertThat(_1.last(), is(Optional.of(Pair.of("1", 1))));
			assertThat(_12.last(), is(Optional.of(Pair.of("2", 2))));
			assertThat(_123.last(), is(Optional.of(Pair.of("3", 3))));
		});
	}

	@Test
	public void at() {
		twice(() -> {
			assertThat(empty.at(0), is(Optional.empty()));
			assertThat(empty.at(17), is(Optional.empty()));

			assertThat(_1.at(0), is(Optional.of(Pair.of("1", 1))));
			assertThat(_1.at(1), is(Optional.empty()));
			assertThat(_1.at(17), is(Optional.empty()));

			assertThat(_12345.at(0), is(Optional.of(Pair.of("1", 1))));
			assertThat(_12345.at(1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_12345.at(4), is(Optional.of(Pair.of("5", 5))));
			assertThat(_12345.at(17), is(Optional.empty()));
		});
	}

	@Test
	public void firstByPredicate() {
		twice(() -> {
			assertThat(empty.first(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_1.first(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_12.first(p -> p.getRight() > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_123.first(p -> p.getRight() > 1), is(Optional.of(Pair.of("2", 2))));
		});
	}

	@Test
	public void secondByPredicate() {
		twice(() -> {
			assertThat(empty.second(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_1.second(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_12.second(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_123.second(p -> p.getRight() > 1), is(Optional.of(Pair.of("3", 3))));
			assertThat(_1234.second(p -> p.getRight() > 1), is(Optional.of(Pair.of("3", 3))));
		});
	}

	@Test
	public void thirdByPredicate() {
		twice(() -> {
			assertThat(empty.third(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_1.third(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_12.third(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_123.third(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_1234.third(p -> p.getRight() > 1), is(Optional.of(Pair.of("4", 4))));
			assertThat(_12345.third(p -> p.getRight() > 1), is(Optional.of(Pair.of("4", 4))));
		});
	}

	@Test
	public void lastByPredicate() {
		twice(() -> {
			assertThat(empty.last(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_1.last(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_12.last(p -> p.getRight() > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_123.last(p -> p.getRight() > 1), is(Optional.of(Pair.of("3", 3))));
		});
	}

	@Test
	public void atByPredicate() {
		twice(() -> {
			assertThat(empty.at(0, p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(empty.at(17, p -> p.getRight() > 1), is(Optional.empty()));

			assertThat(_1.at(0, p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_1.at(17, p -> p.getRight() > 1), is(Optional.empty()));

			assertThat(_12.at(0, p -> p.getRight() > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_12.at(1, p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_12.at(17, p -> p.getRight() > 1), is(Optional.empty()));

			assertThat(_12345.at(0, p -> p.getRight() > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_12345.at(1, p -> p.getRight() > 1), is(Optional.of(Pair.of("3", 3))));
			assertThat(_12345.at(3, p -> p.getRight() > 1), is(Optional.of(Pair.of("5", 5))));
			assertThat(_12345.at(4, p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_12345.at(17, p -> p.getRight() > 1), is(Optional.empty()));
		});
	}

	@Test
	public void firstByBiPredicate() {
		twice(() -> {
			assertThat(empty.first((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_1.first((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_12.first((l, r) -> r > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_123.first((l, r) -> r > 1), is(Optional.of(Pair.of("2", 2))));
		});
	}

	@Test
	public void secondByBiPredicate() {
		twice(() -> {
			assertThat(empty.second((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_1.second((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_12.second((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_123.second((l, r) -> r > 1), is(Optional.of(Pair.of("3", 3))));
			assertThat(_1234.second((l, r) -> r > 1), is(Optional.of(Pair.of("3", 3))));
		});
	}

	@Test
	public void thirdByBiPredicate() {
		twice(() -> {
			assertThat(empty.third((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_1.third((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_12.third((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_123.third((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_1234.third((l, r) -> r > 1), is(Optional.of(Pair.of("4", 4))));
			assertThat(_12345.third((l, r) -> r > 1), is(Optional.of(Pair.of("4", 4))));
		});
	}

	@Test
	public void lastByBiPredicate() {
		twice(() -> {
			assertThat(empty.last((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_1.last((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_12.last((l, r) -> r > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_123.last((l, r) -> r > 1), is(Optional.of(Pair.of("3", 3))));
		});
	}

	@Test
	public void atByBiPredicate() {
		twice(() -> {
			assertThat(empty.at(0, (l, r) -> r > 1), is(Optional.empty()));
			assertThat(empty.at(17, (l, r) -> r > 1), is(Optional.empty()));

			assertThat(_1.at(0, (l, r) -> r > 1), is(Optional.empty()));
			assertThat(_1.at(17, (l, r) -> r > 1), is(Optional.empty()));

			assertThat(_12.at(0, (l, r) -> r > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_12.at(1, (l, r) -> r > 1), is(Optional.empty()));
			assertThat(_12.at(17, (l, r) -> r > 1), is(Optional.empty()));

			assertThat(_12345.at(0, (l, r) -> r > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_12345.at(1, (l, r) -> r > 1), is(Optional.of(Pair.of("3", 3))));
			assertThat(_12345.at(3, (l, r) -> r > 1), is(Optional.of(Pair.of("5", 5))));
			assertThat(_12345.at(4, (l, r) -> r > 1), is(Optional.empty()));
			assertThat(_12345.at(17, (l, r) -> r > 1), is(Optional.empty()));
		});
	}

	@Test
	public void window() {
		Sequence<BiSequence<String, Integer>> windowed = _12345.window(3);
		twice(() -> assertThat(windowed,
		                       contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)),
		                                contains(Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4)),
		                                contains(Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5)))));
	}

	@Test
	public void windowWithStep() {
		Sequence<BiSequence<String, Integer>> windowed = _12345.window(3, 2);
		twice(() -> assertThat(windowed,
		                       contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)),
		                                contains(Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5)))));
	}

	@Test
	public void batch() {
		Sequence<BiSequence<String, Integer>> batched = _12345.batch(3);
		twice(() -> assertThat(batched, contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)),
		                                         contains(Pair.of("4", 4), Pair.of("5", 5)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnPredicate() {
		Sequence<BiSequence<String, Integer>> emptyPartitioned = empty.batch((a, b) -> a.getRight() > b.getRight());
		twice(() -> assertThat(emptyPartitioned, is(emptyIterable())));

		Sequence<BiSequence<String, Integer>> onePartitioned = _1.batch((a, b) -> a.getRight() > b.getRight());
		twice(() -> assertThat(onePartitioned, contains(contains(Pair.of("1", 1)))));

		Sequence<BiSequence<String, Integer>> twoPartitioned = _12.batch((a, b) -> a.getRight() > b.getRight());
		twice(() -> assertThat(twoPartitioned, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> threePartitioned = _123.batch((a, b) -> a.getRight() > b.getRight());
		twice(() -> assertThat(threePartitioned,
		                       contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)))));

		Sequence<BiSequence<String, Integer>> threeRandomPartitioned =
				random3.batch((a, b) -> a.getRight() > b.getRight());
		twice(() -> assertThat(threeRandomPartitioned,
		                       contains(contains(Pair.of("4", 4)), contains(Pair.of("2", 2), Pair.of("3", 3)))));

		Sequence<BiSequence<String, Integer>> nineRandomPartitioned =
				random9.batch((a, b) -> a.getRight() > b.getRight());
		twice(() -> assertThat(nineRandomPartitioned,
		                       contains(contains(Pair.of("67", 67)), contains(Pair.of("5", 5), Pair.of("43", 43)),
		                                contains(Pair.of("3", 3), Pair.of("5", 5), Pair.of("7", 7), Pair.of("24", 24)),
		                                contains(Pair.of("5", 5), Pair.of("67", 67)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnQuaternaryPredicate() {
		Sequence<BiSequence<String, Integer>> emptyPartitioned = empty.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(emptyPartitioned, is(emptyIterable())));

		Sequence<BiSequence<String, Integer>> onePartitioned = _1.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(onePartitioned, contains(contains(Pair.of("1", 1)))));

		Sequence<BiSequence<String, Integer>> twoPartitioned = _12.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(twoPartitioned, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> threePartitioned = _123.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(threePartitioned,
		                       contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)))));

		Sequence<BiSequence<String, Integer>> threeRandomPartitioned = random3.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(threeRandomPartitioned,
		                       contains(contains(Pair.of("4", 4)), contains(Pair.of("2", 2), Pair.of("3", 3)))));

		Sequence<BiSequence<String, Integer>> nineRandomPartitioned = random9.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(nineRandomPartitioned,
		                       contains(contains(Pair.of("67", 67)), contains(Pair.of("5", 5), Pair.of("43", 43)),
		                                contains(Pair.of("3", 3), Pair.of("5", 5), Pair.of("7", 7), Pair.of("24", 24)),
		                                contains(Pair.of("5", 5), Pair.of("67", 67)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void split() {
		Sequence<BiSequence<String, Integer>> emptySplit = empty.split(Pair.of("3", 3));
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

		Sequence<BiSequence<String, Integer>> oneSplit = _1.split(Pair.of("3", 3));
		twice(() -> assertThat(oneSplit, contains(contains(Pair.of("1", 1)))));

		Sequence<BiSequence<String, Integer>> twoSplit = _12.split(Pair.of("3", 3));
		twice(() -> assertThat(twoSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> threeSplit = _123.split(Pair.of("3", 3));
		twice(() -> assertThat(threeSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> fiveSplit = _12345.split(Pair.of("3", 3));
		twice(() -> assertThat(fiveSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)),
		                                           contains(Pair.of("4", 4), Pair.of("5", 5)))));

		Sequence<BiSequence<String, Integer>> nineSplit = _123456789.split(Pair.of("3", 3));
		twice(() -> assertThat(nineSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)),
		                                           contains(Pair.of("4", 4), Pair.of("5", 5), Pair.of("6", 6),
		                                                    Pair.of("7", 7), Pair.of("8", 8), Pair.of("9", 9)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitPredicate() {
		Sequence<BiSequence<String, Integer>> emptySplit = empty.split(x -> x.getRight() % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

		Sequence<BiSequence<String, Integer>> oneSplit = _1.split(x -> x.getRight() % 3 == 0);
		twice(() -> assertThat(oneSplit, contains(contains(Pair.of("1", 1)))));

		Sequence<BiSequence<String, Integer>> twoSplit = _12.split(x -> x.getRight() % 3 == 0);
		twice(() -> assertThat(twoSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> threeSplit = _123.split(x -> x.getRight() % 3 == 0);
		twice(() -> assertThat(threeSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> fiveSplit = _12345.split(x -> x.getRight() % 3 == 0);
		twice(() -> assertThat(fiveSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)),
		                                           contains(Pair.of("4", 4), Pair.of("5", 5)))));

		Sequence<BiSequence<String, Integer>> nineSplit = _123456789.split(x -> x.getRight() % 3 == 0);
		twice(() -> assertThat(nineSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)),
		                                           contains(Pair.of("4", 4), Pair.of("5", 5)),
		                                           contains(Pair.of("7", 7), Pair.of("8", 8)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitLeftRight() {
		Sequence<BiSequence<String, Integer>> emptySplit = empty.split((l, r) -> r % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));

		Sequence<BiSequence<String, Integer>> oneSplit = _1.split((l, r) -> r % 3 == 0);
		twice(() -> assertThat(oneSplit, contains(contains(Pair.of("1", 1)))));

		Sequence<BiSequence<String, Integer>> twoSplit = _12.split((l, r) -> r % 3 == 0);
		twice(() -> assertThat(twoSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> threeSplit = _123.split((l, r) -> r % 3 == 0);
		twice(() -> assertThat(threeSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> fiveSplit = _12345.split((l, r) -> r % 3 == 0);
		twice(() -> assertThat(fiveSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)),
		                                           contains(Pair.of("4", 4), Pair.of("5", 5)))));

		Sequence<BiSequence<String, Integer>> nineSplit = _123456789.split((l, r) -> r % 3 == 0);
		twice(() -> assertThat(nineSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)),
		                                           contains(Pair.of("4", 4), Pair.of("5", 5)),
		                                           contains(Pair.of("7", 7), Pair.of("8", 8)))));
	}

	@Test
	public void step() {
		twice(() -> assertThat(_123456789.step(3), contains(Pair.of("1", 1), Pair.of("4", 4), Pair.of("7", 7))));
	}

	@Test
	public void distinct() {
		BiSequence<String, Integer> emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		BiSequence<String, Integer> oneDistinct = random1.distinct();
		twice(() -> assertThat(oneDistinct, contains(Pair.of("17", 17))));

		BiSequence<String, Integer> twoDuplicatesDistinct =
				BiSequence.of(Pair.of("17", 17), Pair.of("17", 17)).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, contains(Pair.of("17", 17))));

		BiSequence<String, Integer> nineDistinct = random9.distinct();
		twice(() -> assertThat(nineDistinct,
		                       contains(Pair.of("67", 67), Pair.of("5", 5), Pair.of("43", 43), Pair.of("3", 3),
		                                Pair.of("7", 7), Pair.of("24", 24))));
	}

	@Test
	public void sorted() {
		BiSequence<String, Integer> emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		BiSequence<String, Integer> oneSorted = random1.sorted();
		twice(() -> assertThat(oneSorted, contains(Pair.of("17", 17))));

		BiSequence<String, Integer> twoSorted = random2.sorted();
		twice(() -> assertThat(twoSorted, contains(Pair.of("17", 17), Pair.of("32", 32))));

		BiSequence<String, Integer> nineSorted = random9.sorted();
		twice(() -> assertThat(nineSorted, // String sorting on first item
		                       contains(Pair.of("24", 24), Pair.of("3", 3), Pair.of("43", 43), Pair.of("5", 5),
		                                Pair.of("5", 5), Pair.of("5", 5), Pair.of("67", 67), Pair.of("67", 67),
		                                Pair.of("7", 7))));
	}

	@Test
	public void sortedComparator() {
		BiSequence<String, Integer> emptySorted = empty.sorted((Comparator) Comparator.reverseOrder());
		twice(() -> assertThat(emptySorted, emptyIterable()));

		BiSequence<String, Integer> oneSorted = random1.sorted((Comparator) Comparator.reverseOrder());
		twice(() -> assertThat(oneSorted, contains(Pair.of("17", 17))));

		BiSequence<String, Integer> twoSorted = random2.sorted((Comparator) Comparator.reverseOrder());
		twice(() -> assertThat(twoSorted, contains(Pair.of("32", 32), Pair.of("17", 17))));

		BiSequence<String, Integer> nineSorted = random9.sorted((Comparator) Comparator.reverseOrder());
		twice(() -> assertThat(nineSorted, // String sorting on first item reverse
		                       contains(Pair.of("7", 7), Pair.of("67", 67), Pair.of("67", 67), Pair.of("5", 5),
		                                Pair.of("5", 5), Pair.of("5", 5), Pair.of("43", 43), Pair.of("3", 3),
		                                Pair.of("24", 24))));
	}

	@Test
	public void min() {
		twice(() -> assertThat(empty.min(), is(Optional.empty())));
		twice(() -> assertThat(random1.min(), is(Optional.of(Pair.of("17", 17)))));
		twice(() -> assertThat(random2.min(), is(Optional.of(Pair.of("17", 17)))));
		twice(() -> assertThat(random9.min(), is(Optional.of(Pair.of("24", 24)))));
	}

	@Test
	public void max() {
		twice(() -> assertThat(empty.max(), is(Optional.empty())));
		twice(() -> assertThat(random1.max(), is(Optional.of(Pair.of("17", 17)))));
		twice(() -> assertThat(random2.max(), is(Optional.of(Pair.of("32", 32)))));
		twice(() -> assertThat(random9.max(), is(Optional.of(Pair.of("7", 7)))));
	}

	@Test
	public void minByComparator() {
		twice(() -> assertThat(empty.min(Comparator.reverseOrder()), is(Optional.empty())));
		twice(() -> assertThat(random1.min(Comparator.reverseOrder()), is(Optional.of(Pair.of("17", 17)))));
		twice(() -> assertThat(random2.min(Comparator.reverseOrder()), is(Optional.of(Pair.of("32", 32)))));
		twice(() -> assertThat(random9.min(Comparator.reverseOrder()), is(Optional.of(Pair.of("7", 7)))));
	}

	@Test
	public void maxByComparator() {
		twice(() -> assertThat(empty.max(Comparator.reverseOrder()), is(Optional.empty())));
		twice(() -> assertThat(random1.max(Comparator.reverseOrder()), is(Optional.of(Pair.of("17", 17)))));
		twice(() -> assertThat(random2.max(Comparator.reverseOrder()), is(Optional.of(Pair.of("17", 17)))));
		twice(() -> assertThat(random9.max(Comparator.reverseOrder()), is(Optional.of(Pair.of("24", 24)))));
	}

	@Test
	public void size() {
		twice(() -> assertThat(empty.size(), is(0L)));
		twice(() -> assertThat(_1.size(), is(1L)));
		twice(() -> assertThat(_12.size(), is(2L)));
		twice(() -> assertThat(_123456789.size(), is(9L)));
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
		BiSequence<String, Integer> peek = _123.peek((s, x) -> assertThat(x, is(both(greaterThanOrEqualTo(1)).and(
				lessThanOrEqualTo(3)).and(equalTo(parseInt(s))))));
		twice(() -> assertThat(peek, contains(entries123)));
	}

	@Test
	public void mapToChar() {
		CharSeq emptyChars = empty.toChars((l, r) -> (char) (r + 'a' - 1));
		twice(() -> assertThat(emptyChars, is(emptyIterable())));

		CharSeq charSeq = _12345.toChars((l, r) -> (char) (r + 'a' - 1));
		twice(() -> assertThat(charSeq, contains('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void mapToInt() {
		IntSequence emptyInts = empty.toInts((l, r) -> r + 1);
		twice(() -> assertThat(emptyInts, is(emptyIterable())));

		IntSequence intSequence = _12345.toInts((l, r) -> r + 1);
		twice(() -> assertThat(intSequence, containsInts(2, 3, 4, 5, 6)));
	}

	@Test
	public void mapToLong() {
		LongSequence emptyLongs = empty.toLongs((l, r) -> r + 1);
		twice(() -> assertThat(emptyLongs, is(emptyIterable())));

		LongSequence longSequence = _12345.toLongs((l, r) -> r + 1);
		twice(() -> assertThat(longSequence, containsLongs(2L, 3L, 4L, 5L, 6L)));
	}

	@Test
	public void mapToDouble() {
		DoubleSequence emptyDoubles = empty.toDoubles((l, r) -> r + 1);
		twice(() -> assertThat(emptyDoubles, is(emptyIterable())));

		DoubleSequence doubleSequence = _12345.toDoubles((l, r) -> r + 1);
		twice(() -> assertThat(doubleSequence, contains(2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void mapToCharFunction() {
		CharSeq emptyChars = empty.toChars(p -> (char) (p.getRight() + 'a' - 1));
		twice(() -> assertThat(emptyChars, is(emptyIterable())));

		CharSeq charSeq = _12345.toChars(p -> (char) (p.getRight() + 'a' - 1));
		twice(() -> assertThat(charSeq, contains('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void mapToIntFunction() {
		IntSequence emptyInts = empty.toInts(p -> p.getRight() + 1);
		twice(() -> assertThat(emptyInts, is(emptyIterable())));

		IntSequence intSequence = _12345.toInts(p -> p.getRight() + 1);
		twice(() -> assertThat(intSequence, containsInts(2, 3, 4, 5, 6)));
	}

	@Test
	public void mapToLongFunction() {
		LongSequence emptyLongs = empty.toLongs(p -> p.getRight() + 1);
		twice(() -> assertThat(emptyLongs, is(emptyIterable())));

		LongSequence longSequence = _12345.toLongs(p -> p.getRight() + 1);
		twice(() -> assertThat(longSequence, containsLongs(2L, 3L, 4L, 5L, 6L)));
	}

	@Test
	public void mapToDoubleFunction() {
		DoubleSequence emptyDoubles = empty.toDoubles(p -> p.getRight() + 1);
		twice(() -> assertThat(emptyDoubles, is(emptyIterable())));

		DoubleSequence doubleSequence = _12345.toDoubles(p -> p.getRight() + 1);
		twice(() -> assertThat(doubleSequence, contains(2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void repeat() {
		BiSequence<String, Integer> repeatEmpty = empty.repeat();
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		BiSequence<String, Integer> repeatOne = _1.repeat();
		twice(() -> assertThat(repeatOne.limit(3), contains(Pair.of("1", 1), Pair.of("1", 1), Pair.of("1", 1))));

		BiSequence<String, Integer> repeatTwo = _12.repeat();
		twice(() -> assertThat(repeatTwo.limit(5),
		                       contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("1", 1), Pair.of("2", 2),
		                                Pair.of("1", 1))));

		BiSequence<String, Integer> repeatThree = _123.repeat();
		twice(() -> assertThat(repeatThree.limit(8),
		                       contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("1", 1),
		                                Pair.of("2", 2), Pair.of("3", 3), Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> repeatVarying = BiSequence.from(new Iterable<Pair<String, Integer>>() {
			private List<Pair<String, Integer>> list = asList(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3));
			int end = list.size();

			@Override
			public Iterator<Pair<String, Integer>> iterator() {
				List<Pair<String, Integer>> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				return subList.iterator();
			}
		}).repeat();
		assertThat(repeatVarying,
		           contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("1", 1), Pair.of("2", 2),
		                    Pair.of("1", 1)));
	}

	@Test
	public void repeatTwice() {
		BiSequence<String, Integer> repeatEmpty = empty.repeat(2);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		BiSequence<String, Integer> repeatOne = _1.repeat(2);
		twice(() -> assertThat(repeatOne, contains(Pair.of("1", 1), Pair.of("1", 1))));

		BiSequence<String, Integer> repeatTwo = _12.repeat(2);
		twice(() -> assertThat(repeatTwo,
		                       contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> repeatThree = _123.repeat(2);
		twice(() -> assertThat(repeatThree, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("1",
		                                                                                                        1),
		                                             Pair.of("2", 2), Pair.of("3", 3))));
		BiSequence<String, Integer> repeatVarying = BiSequence.from(new Iterable<Pair<String, Integer>>() {
			private List<Pair<String, Integer>> list = asList(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3));
			int end = list.size();

			@Override
			public Iterator<Pair<String, Integer>> iterator() {
				List<Pair<String, Integer>> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				return subList.iterator();
			}
		}).repeat(2);
		assertThat(repeatVarying,
		           contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("1", 1), Pair.of("2", 2)));
	}

	@Test
	public void repeatZero() {
		BiSequence<String, Integer> repeatEmpty = empty.repeat(0);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		BiSequence<String, Integer> repeatOne = _1.repeat(0);
		twice(() -> assertThat(repeatOne, is(emptyIterable())));

		BiSequence<String, Integer> repeatTwo = _12.repeat(0);
		twice(() -> assertThat(repeatTwo, is(emptyIterable())));

		BiSequence<String, Integer> repeatThree = _123.repeat(0);
		twice(() -> assertThat(repeatThree, is(emptyIterable())));
	}

	@Test
	public void generate() {
		Queue<Pair<String, Integer>> queue = new ArrayDeque<>(asList(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                                             Pair.of("4", 4), Pair.of("5", 5)));
		BiSequence<String, Integer> sequence = BiSequence.generate(queue::poll);

		assertThat(sequence,
		           beginsWith(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5),
		                      null));
		assertThat(sequence, beginsWith((Pair<String, Integer>) null));
	}

	@Test
	public void multiGenerate() {
		BiSequence<String, Integer> sequence = BiSequence.multiGenerate(() -> {
			Queue<Pair<String, Integer>> queue = new ArrayDeque<>(
					asList(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5)));
			return queue::poll;
		});

		twice(() -> assertThat(sequence, beginsWith(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                            Pair.of("5", 5), null)));
	}

	@Test
	public void reverse() {
		BiSequence<String, Integer> emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		BiSequence<String, Integer> oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, contains(Pair.of("1", 1))));

		BiSequence<String, Integer> twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, contains(Pair.of("2", 2), Pair.of("1", 1))));

		BiSequence<String, Integer> threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, contains(Pair.of("3", 3), Pair.of("2", 2), Pair.of("1", 1))));

		BiSequence<String, Integer> nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed,
		                       contains(Pair.of("9", 9), Pair.of("8", 8), Pair.of("7", 7), Pair.of("6", 6),
		                                Pair.of("5", 5), Pair.of("4", 4), Pair.of("3", 3), Pair.of("2", 2),
		                                Pair.of("1", 1))));
	}

	@Test
	public void shuffle() {
		BiSequence<String, Integer> emptyShuffled = empty.shuffle();
		twice(() -> assertThat(emptyShuffled, is(emptyIterable())));

		BiSequence<String, Integer> oneShuffled = _1.shuffle();
		twice(() -> assertThat(oneShuffled, contains(Pair.of("1", 1))));

		BiSequence<String, Integer> twoShuffled = _12.shuffle();
		twice(() -> assertThat(twoShuffled, containsInAnyOrder(Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> threeShuffled = _123.shuffle();
		twice(() -> assertThat(threeShuffled, containsInAnyOrder(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));

		BiSequence<String, Integer> nineShuffled = _123456789.shuffle();
		twice(() -> assertThat(nineShuffled,
		                       containsInAnyOrder(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                          Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));
	}

	@Test
	public void shuffleWithRandomSource() {
		BiSequence<String, Integer> emptyShuffled = empty.shuffle(new Random(17));
		twice(() -> assertThat(emptyShuffled, is(emptyIterable())));

		BiSequence<String, Integer> oneShuffled = _1.shuffle(new Random(17));
		twice(() -> assertThat(oneShuffled, contains(Pair.of("1", 1))));

		BiSequence<String, Integer> twoShuffled = _12.shuffle(new Random(17));
		assertThat(twoShuffled, contains(Pair.of("1", 1), Pair.of("2", 2)));
		assertThat(twoShuffled, contains(Pair.of("1", 1), Pair.of("2", 2)));
		assertThat(twoShuffled, contains(Pair.of("1", 1), Pair.of("2", 2)));
		assertThat(twoShuffled, contains(Pair.of("1", 1), Pair.of("2", 2)));
		assertThat(twoShuffled, contains(Pair.of("2", 2), Pair.of("1", 1)));
		assertThat(twoShuffled, contains(Pair.of("2", 2), Pair.of("1", 1)));
		assertThat(twoShuffled, contains(Pair.of("1", 1), Pair.of("2", 2)));
		assertThat(twoShuffled, contains(Pair.of("1", 1), Pair.of("2", 2)));

		BiSequence<String, Integer> threeShuffled = _123.shuffle(new Random(17));
		assertThat(threeShuffled, contains(Pair.of("3", 3), Pair.of("2", 2), Pair.of("1", 1)));
		assertThat(threeShuffled, contains(Pair.of("1", 1), Pair.of("3", 3), Pair.of("2", 2)));

		BiSequence<String, Integer> nineShuffled = _123456789.shuffle(new Random(17));
		assertThat(nineShuffled,
		           contains(Pair.of("1", 1), Pair.of("8", 8), Pair.of("4", 4), Pair.of("2", 2), Pair.of("6", 6),
		                    Pair.of("3", 3), Pair.of("5", 5), Pair.of("9", 9), Pair.of("7", 7)));
		assertThat(nineShuffled,
		           contains(Pair.of("6", 6), Pair.of("3", 3), Pair.of("5", 5), Pair.of("2", 2), Pair.of("9", 9),
		                    Pair.of("4", 4), Pair.of("1", 1), Pair.of("7", 7), Pair.of("8", 8)));
	}

	@Test
	public void shuffleWithRandomSupplier() {
		BiSequence<String, Integer> emptyShuffled = empty.shuffle(() -> new Random(17));
		twice(() -> assertThat(emptyShuffled, is(emptyIterable())));

		BiSequence<String, Integer> oneShuffled = _1.shuffle(() -> new Random(17));
		twice(() -> assertThat(oneShuffled, contains(Pair.of("1", 1))));

		BiSequence<String, Integer> twoShuffled = _12.shuffle(() -> new Random(17));
		twice(() -> assertThat(twoShuffled, contains(Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> threeShuffled = _123.shuffle(() -> new Random(17));
		twice(() -> assertThat(threeShuffled, contains(Pair.of("3", 3), Pair.of("2", 2), Pair.of("1", 1))));

		BiSequence<String, Integer> nineShuffled = _123456789.shuffle(() -> new Random(17));
		twice(() -> assertThat(nineShuffled,
		                       contains(Pair.of("1", 1), Pair.of("8", 8), Pair.of("4", 4), Pair.of("2", 2),
		                                Pair.of("6", 6), Pair.of("3", 3), Pair.of("5", 5), Pair.of("9", 9),
		                                Pair.of("7", 7))));
	}

	@Test
	public void flatten() {
		BiSequence<String, Integer> flattened = _123.flatten(pair -> Iterables.of(pair, Pair.of("0", 0)));
		twice(() -> assertThat(flattened, contains(Pair.of("1", 1), Pair.of("0", 0), Pair.of("2", 2), Pair.of("0", 0),
		                                           Pair.of("3", 3), Pair.of("0", 0))));
	}

	@Test
	public void flattenBiFunction() {
		BiSequence<String, Integer> flattened = _123.flatten((l, r) -> Iterables.of(Pair.of(l, r), Pair.of("0", 0)));
		twice(() -> assertThat(flattened, contains(Pair.of("1", 1), Pair.of("0", 0), Pair.of("2", 2), Pair.of("0", 0),
		                                           Pair.of("3", 3), Pair.of("0", 0))));
	}

	@Test
	public void flattenLeft() {
		BiSequence<String, Integer> flattened =
				BiSequence.<List<String>, Integer>ofPairs(Iterables.of("1", "2", "3"), 1, emptyList(), "4",
				                                          Iterables.of("5", "6", "7"), 3).flattenLeft(Pair::getLeft);
		twice(() -> assertThat(flattened, contains(Pair.of("1", 1), Pair.of("2", 1), Pair.of("3", 1), Pair.of("5", 3),
		                                           Pair.of("6", 3), Pair.of("7", 3))));
	}

	@Test
	public void flattenRight() {
		BiSequence<String, Integer> flattened =
				BiSequence.<String, List<Integer>>ofPairs("1", Iterables.of(1, 2, 3), "2", emptyList(), "3",
				                                          Iterables.of(2, 3, 4)).flattenRight(Pair::getRight);
		twice(() -> assertThat(flattened, contains(Pair.of("1", 1), Pair.of("1", 2), Pair.of("1", 3), Pair.of("3", 2),
		                                           Pair.of("3", 3), Pair.of("3", 4))));
	}

	@Test
	public void clear() {
		List<Pair<String, Integer>> original =
				new ArrayList<>(asList(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4)));

		BiSequence<String, Integer> filtered = BiSequence.from(original).filter((l, r) -> r % 2 != 0);
		filtered.clear();

		twice(() -> assertThat(filtered, is(emptyIterable())));
		twice(() -> assertThat(original, contains(Pair.of("2", 2), Pair.of("4", 4))));
	}

	@Test
	public void isEmpty() {
		twice(() -> assertThat(empty.isEmpty(), is(true)));
		twice(() -> assertThat(_1.isEmpty(), is(false)));
		twice(() -> assertThat(_12.isEmpty(), is(false)));
		twice(() -> assertThat(_12345.isEmpty(), is(false)));
	}

	@Test
	public void containsPair() {
		assertThat(empty.contains(Pair.of("17", 17)), is(false));

		assertThat(_12345.contains(Pair.of("1", 1)), is(true));
		assertThat(_12345.contains(Pair.of("3", 3)), is(true));
		assertThat(_12345.contains(Pair.of("5", 5)), is(true));
		assertThat(_12345.contains(Pair.of("17", 17)), is(false));
	}

	@Test
	public void containsPairComponents() {
		assertThat(empty.contains("17", 17), is(false));

		assertThat(_12345.contains("1", 1), is(true));
		assertThat(_12345.contains("3", 3), is(true));
		assertThat(_12345.contains("5", 5), is(true));
		assertThat(_12345.contains("17", 17), is(false));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(), is(true));
		assertThat(empty.containsAll(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19)), is(false));

		assertThat(_12345.containsAll(), is(true));
		assertThat(_12345.containsAll(Pair.of("1", 1)), is(true));
		assertThat(_12345.containsAll(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5)), is(true));
		assertThat(_12345.containsAll(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                              Pair.of("5", 5)), is(true));
		assertThat(_12345.containsAll(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                              Pair.of("5", 5), Pair.of("17", 17)), is(false));
		assertThat(_12345.containsAll(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19)), is(false));
	}

	@Test
	public void containsAny() {
		assertThat(empty.containsAny(), is(false));
		assertThat(empty.containsAny(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19)), is(false));

		assertThat(_12345.containsAny(), is(false));
		assertThat(_12345.containsAny(Pair.of("1", 1)), is(true));
		assertThat(_12345.containsAny(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5)), is(true));
		assertThat(_12345.containsAny(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                              Pair.of("5", 5)), is(true));
		assertThat(_12345.containsAny(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                              Pair.of("5", 5), Pair.of("17", 17)), is(true));
		assertThat(_12345.containsAny(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19)), is(false));
	}

	@Test
	public void containsAllIterable() {
		assertThat(empty.containsAll(Iterables.of()), is(true));
		assertThat(empty.containsAll(Iterables.of(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19))), is
				(false));

		assertThat(_12345.containsAll(Iterables.of()), is(true));
		assertThat(_12345.containsAll(Iterables.of(Pair.of("1", 1))), is(true));
		assertThat(_12345.containsAll(Iterables.of(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5))), is(true));
		assertThat(_12345.containsAll(Iterables.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                           Pair.of("5", 5))), is(true));
		assertThat(_12345.containsAll(Iterables.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                           Pair.of("5", 5), Pair.of("17", 17))), is(false));
		assertThat(_12345.containsAll(Iterables.of(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19))),
		           is(false));
	}

	@Test
	public void containsAnyIterable() {
		assertThat(empty.containsAny(Iterables.of()), is(false));
		assertThat(empty.containsAny(Iterables.of(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19))), is
				(false));

		assertThat(_12345.containsAny(Iterables.of()), is(false));
		assertThat(_12345.containsAny(Iterables.of(Pair.of("1", 1))), is(true));
		assertThat(_12345.containsAny(Iterables.of(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5))), is(true));
		assertThat(_12345.containsAny(Iterables.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                           Pair.of("5", 5))), is(true));
		assertThat(_12345.containsAny(Iterables.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                           Pair.of("5", 5), Pair.of("17", 17))), is(true));
		assertThat(_12345.containsAny(Iterables.of(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19))),
		           is(false));
	}
}
