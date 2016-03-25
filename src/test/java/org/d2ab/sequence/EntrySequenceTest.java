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
import org.d2ab.util.Arrayz;
import org.d2ab.util.Entries;
import org.d2ab.util.Pair;
import org.junit.Test;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@SuppressWarnings("unchecked")
public class EntrySequenceTest {
	private final EntrySequence<String, Integer> empty = EntrySequence.empty();
	private final EntrySequence<String, Integer> _1 = EntrySequence.of(Entries.of("1", 1));
	private final EntrySequence<String, Integer> _12 = EntrySequence.of(Entries.of("1", 1), Entries.of("2", 2));
	private final EntrySequence<String, Integer> _123 =
			EntrySequence.of(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3));
	private final EntrySequence<String, Integer> _1234 =
			EntrySequence.of(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3), Entries.of("4", 4));
	private final EntrySequence<String, Integer> _12345 =
			EntrySequence.of(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3), Entries.of("4", 4),
			                 Entries.of("5", 5));
	private final EntrySequence<String, Integer> _123456789 =
			EntrySequence.of(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3), Entries.of("4", 4),
			                 Entries.of("5", 5), Entries.of("6", 6), Entries.of("7", 7), Entries.of("8", 8),
			                 Entries.of("9", 9));
	private final EntrySequence<String, Integer> random1 = EntrySequence.of(Entries.of("17", 17));
	private final EntrySequence<String, Integer> random2 =
			EntrySequence.of(Entries.of("17", 17), Entries.of("32", 32));
	private final EntrySequence<String, Integer> random3 =
			EntrySequence.of(Entries.of("4", 4), Entries.of("2", 2), Entries.of("3", 3));
	private final EntrySequence<String, Integer> random9 =
			EntrySequence.of(Entries.of("67", 67), Entries.of("5", 5), Entries.of("43", 43), Entries.of("3", 3),
			                 Entries.of("5", 5), Entries.of("7", 7), Entries.of("24", 24), Entries.of("5", 5),
			                 Entries.of("67", 67));
	private final Entry<String, Integer>[] entries123 =
			new Entry[]{Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3)};
	private final Entry<String, Integer>[] entries12345 =
			new Entry[]{Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3), Entries.of("4", 4),
			            Entries.of("5", 5)};
	private final Entry<String, Integer>[] entries456 =
			new Entry[]{Entries.of("4", 4), Entries.of("5", 5), Entries.of("6", 6)};
	private final Entry<String, Integer>[] entries789 =
			new Entry[]{Entries.of("7", 7), Entries.of("8", 8), Entries.of("9", 9)};

	@Test
	public void ofOne() {
		twice(() -> assertThat(_1, contains(Entries.of("1", 1))));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(_123, contains(entries123)));
	}

	@Test
	public void ofPair() {
		assertThat(EntrySequence.ofEntry("1", 1), contains(Entries.of("1", 1)));
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
			for (Entry<String, Integer> i : _123)
				assertThat(i, is(Entries.of(String.valueOf(expected), expected++)));
		});
	}

	@Test
	public void forEach() {
		twice(() -> {
			empty.forEach(i -> fail("Should not get called"));
			_1.forEach(i -> assertThat(i, is(in(entries123))));
			_12.forEach(i -> assertThat(i, is(in(entries123))));
			_123.forEach(i -> assertThat(i, is(in(entries123))));
		});
	}

	@Test
	public void iterator() {
		twice(() -> {
			Iterator iterator = _123.iterator();

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(Entries.of("1", 1)));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(Entries.of("2", 2)));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(Entries.of("3", 3)));

			assertThat(iterator.hasNext(), is(false));
			assertThat(iterator.hasNext(), is(false));
		});
	}

	@Test
	public void ofNone() {
		EntrySequence<String, Integer> sequence = EntrySequence.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void ofWithNulls() {
		EntrySequence<String, Integer> sequence =
				EntrySequence.of(Entries.of("1", 1), Entries.of(null, 2), Entries.of("3", 3), Entries.of("4",
				                                                                                         null),
				                 Entries.of(null, null));

		twice(() -> assertThat(sequence, contains(Entries.of("1", 1), Entries.of(null, 2), Entries.of("3", 3),
		                                          Entries.of("4", null), Entries.of(null, null))));
	}

	@Test
	public void fromBiSequence() {
		EntrySequence<String, Integer> fromEntrySequence = EntrySequence.from(_123);

		twice(() -> assertThat(fromEntrySequence, contains(entries123)));
	}

	@Test
	public void fromIterable() {
		Iterable<Entry<String, Integer>> iterable = asList(entries123);

		EntrySequence<String, Integer> sequenceFromIterable = EntrySequence.from(iterable);

		twice(() -> assertThat(sequenceFromIterable, contains(entries123)));
	}

	@Test
	public void fromIterables() {
		Iterable<Entry<String, Integer>> first = asList(entries123);
		Iterable<Entry<String, Integer>> second = asList(entries456);
		Iterable<Entry<String, Integer>> third = asList(entries789);

		EntrySequence<String, Integer> sequenceFromIterables = EntrySequence.from(first, second, third);

		twice(() -> assertThat(sequenceFromIterables,
		                       contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3),
		                                Entries.of("4", 4), Entries.of("5", 5), Entries.of("6", 6),
		                                Entries.of("7", 7), Entries.of("8", 8), Entries.of("9", 9))));
	}

	@Test
	public void fromEntryIterables() {
		Iterable<Entry<String, Integer>> first = asList(entries123);
		Iterable<Entry<String, Integer>> second = asList(entries456);
		Iterable<Entry<String, Integer>> third = asList(entries789);

		EntrySequence<String, Integer> sequenceFromIterables = EntrySequence.from(first, second, third);

		twice(() -> assertThat(sequenceFromIterables,
		                       contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3),
		                                Entries.of("4", 4), Entries.of("5", 5), Entries.of("6", 6),
		                                Entries.of("7", 7), Entries.of("8", 8), Entries.of("9", 9))));
	}

	@Test
	public void fromNoIterables() {
		EntrySequence<String, Integer> sequenceFromNoIterables = EntrySequence.from(new Iterable[]{});

		twice(() -> assertThat(sequenceFromNoIterables, is(emptyIterable())));
	}

	@Test
	public void skip() {
		EntrySequence<String, Integer> skipNone = _123.skip(0);
		twice(() -> assertThat(skipNone, contains(entries123)));

		EntrySequence<String, Integer> skipOne = _123.skip(1);
		twice(() -> assertThat(skipOne, contains(Entries.of("2", 2), Entries.of("3", 3))));

		EntrySequence<String, Integer> skipTwo = _123.skip(2);
		twice(() -> assertThat(skipTwo, contains(Entries.of("3", 3))));

		EntrySequence<String, Integer> skipThree = _123.skip(3);
		twice(() -> assertThat(skipThree, is(emptyIterable())));

		EntrySequence<String, Integer> skipFour = _123.skip(4);
		twice(() -> assertThat(skipFour, is(emptyIterable())));
	}

	@Test
	public void limit() {
		EntrySequence<String, Integer> limitZero = _123.limit(0);
		twice(() -> assertThat(limitZero, is(emptyIterable())));

		EntrySequence<String, Integer> limitOne = _123.limit(1);
		twice(() -> assertThat(limitOne, contains(Entries.of("1", 1))));

		EntrySequence<String, Integer> limitTwo = _123.limit(2);
		twice(() -> assertThat(limitTwo, contains(Entries.of("1", 1), Entries.of("2", 2))));

		EntrySequence<String, Integer> limitThree = _123.limit(3);
		twice(() -> assertThat(limitThree, contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3))));

		EntrySequence<String, Integer> limitFour = _123.limit(4);
		twice(() -> assertThat(limitFour, contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3))));
	}

	@Test
	public void append() {
		EntrySequence<String, Integer> appended =
				_123.append(EntrySequence.of(entries456)).append(EntrySequence.of(entries789));

		twice(() -> assertThat(appended, contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3),
		                                          Entries.of("4", 4), Entries.of("5", 5), Entries.of("6", 6),
		                                          Entries.of("7", 7), Entries.of("8", 8), Entries.of("9", 9))));
	}

	@Test
	public void appendIsLazy() {
		Iterator<Entry<String, Integer>> first = Arrayz.iterator(entries123);
		Iterator<Entry<String, Integer>> second = Arrayz.iterator(entries456);

		EntrySequence<String, Integer> appended = EntrySequence.from(first).append(() -> second);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));

		assertThat(appended,
		           contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3), Entries.of("4", 4),
		                    Entries.of("5", 5), Entries.of("6", 6)));
		assertThat(appended, is(emptyIterable())); // iterators exhausted on second run
	}

	@Test
	public void appendIsLazyWhenSkippingHasNext() {
		Iterator<Entry<String, Integer>> first = Arrayz.iterator(Entries.of("1", 1));
		Iterator<Entry<String, Integer>> second = Arrayz.iterator(Entries.of("2", 2));

		EntrySequence<String, Integer> sequence = EntrySequence.from(first).append(EntrySequence.from(second));

		// check delayed iteration
		Iterator<Entry<String, Integer>> iterator = sequence.iterator();
		assertThat(iterator.next(), is(Entries.of("1", 1)));
		assertThat(iterator.next(), is(Entries.of("2", 2)));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		EntrySequence<String, Integer> filtered = _123456789.filter((s, i) -> i % 2 == 0);

		twice(() -> assertThat(filtered, contains(Entries.of("2", 2), Entries.of("4", 4), Entries.of("6", 6),
		                                          Entries.of("8", 8))));
	}

	@Test
	public void filterAndMap() {
		EntrySequence<Integer, String> evens =
				_123456789.filter((s, x) -> x % 2 == 0).map(Integer::parseInt, Object::toString);

		twice(() -> assertThat(evens, contains(Entries.of(2, "2"), Entries.of(4, "4"), Entries.of(6, "6"),
		                                       Entries.of(8, "8"))));
	}

	@Test
	public void mapBiFunction() {
		EntrySequence<Integer, String> mapped = _123.map((s, i) -> Entries.of(parseInt(s), i.toString()));
		twice(() -> assertThat(mapped, contains(Entries.of(1, "1"), Entries.of(2, "2"), Entries.of(3, "3"))));
	}

	@Test
	public void mapTwoFunctions() {
		EntrySequence<Integer, String> mapped = _123.map(Integer::parseInt, Object::toString);
		twice(() -> assertThat(mapped, contains(Entries.of(1, "1"), Entries.of(2, "2"), Entries.of(3, "3"))));
	}

	@Test
	public void mapEntryFunction() {
		EntrySequence<Integer, String> mapped =
				_123.map(p -> Entries.of(parseInt(p.getKey()), p.getValue().toString()));
		twice(() -> assertThat(mapped, contains(Entries.of(1, "1"), Entries.of(2, "2"), Entries.of(3, "3"))));
	}

	@Test
	public void mapIsLazy() {
		EntrySequence<Integer, String> mapped = EntrySequence.of(Entries.of("1", 1), null) // null will be hit on map
		                                                     .map((s, i) -> Entries.of(parseInt(s), i.toString()));

		twice(() -> {
			// NPE here if not lazy
			Iterator<Entry<Integer, String>> iterator = mapped.iterator();

			assertThat(iterator.next(), is(Entries.of(1, "1")));

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
		EntrySequence<String, Integer> sequence =
				EntrySequence.recurse("1", 1, (k, v) -> Entries.of(String.valueOf(v + 1), v + 1));
		twice(() -> assertThat(sequence.limit(3),
		                       contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3))));
	}

	@Test
	public void recurseTwins() {
		EntrySequence<String, Integer> sequence = EntrySequence.recurse(1, "1", (k, v) -> Entries.of(v, k),
		                                                                (k, v) -> Entries.of(v + 1,
		                                                                                     String.valueOf(v + 1)));
		twice(() -> assertThat(sequence.limit(3),
		                       contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3))));
	}

	@Test
	public void until() {
		EntrySequence<String, Integer> sequence = EntrySequence.from(_12345).until(Entries.of("4", 4));
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void endingAt() {
		EntrySequence<String, Integer> sequence = EntrySequence.from(_12345).endingAt(Entries.of("3", 3));
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void untilPredicate() {
		EntrySequence<String, Integer> sequence = EntrySequence.from(_12345).until(e -> e.equals(Entries.of("4", 4)));
		twice(() -> assertThat(sequence, contains(entries123)));
	}

	@Test
	public void endingAtPredicate() {
		EntrySequence<String, Integer> sequence =
				EntrySequence.from(_12345).endingAt(e -> e.equals(Entries.of("3", 3)));
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
				(r, e) -> Entries.of(r.getKey() + e.getKey(), r.getValue() + e.getValue());

		twice(() -> {
			assertThat(empty.reduce(sumEntry), is(Optional.empty()));
			assertThat(_1.reduce(sumEntry), is(Optional.of(Entries.of("1", 1))));
			assertThat(_12.reduce(sumEntry), is(Optional.of(Entries.of("12", 3))));
			assertThat(_123.reduce(sumEntry), is(Optional.of(Entries.of("123", 6))));
		});
	}

	@Test
	public void reduceQuaternary() {
		QuaternaryFunction<String, Integer, String, Integer, Entry<String, Integer>> sumEntry =
				(rk, rv, ek, ev) -> Entries.of(rk + ek, rv + ev);

		twice(() -> {
			assertThat(empty.reduce(sumEntry), is(Optional.empty()));
			assertThat(_1.reduce(sumEntry), is(Optional.of(Entries.of("1", 1))));
			assertThat(_12.reduce(sumEntry), is(Optional.of(Entries.of("12", 3))));
			assertThat(_123.reduce(sumEntry), is(Optional.of(Entries.of("123", 6))));
		});
	}

	@Test
	public void reduceWithIdentity() {
		BinaryOperator<Entry<String, Integer>> sumEntry =
				(r, e) -> Entries.of(r.getKey() + e.getKey(), r.getValue() + e.getValue());
		twice(() -> {
			assertThat(empty.reduce(Entries.of("17", 17), sumEntry), is(Entries.of("17", 17)));
			assertThat(_1.reduce(Entries.of("17", 17), sumEntry), is(Entries.of("171", 18)));
			assertThat(_12.reduce(Entries.of("17", 17), sumEntry), is(Entries.of("1712", 20)));
			assertThat(_123.reduce(Entries.of("17", 17), sumEntry), is(Entries.of("17123", 23)));
		});
	}

	@Test
	public void reduceQuaternaryWithIdentity() {
		QuaternaryFunction<String, Integer, String, Integer, Entry<String, Integer>> sumEntry =
				(rk, rv, ek, ev) -> Entries.of(rk + ek, rv + ev);

		twice(() -> {
			assertThat(empty.reduce("17", 17, sumEntry), is(Entries.of("17", 17)));
			assertThat(_1.reduce("17", 17, sumEntry), is(Entries.of("171", 18)));
			assertThat(_12.reduce("17", 17, sumEntry), is(Entries.of("1712", 20)));
			assertThat(_123.reduce("17", 17, sumEntry), is(Entries.of("17123", 23)));
		});
	}

	@Test
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(Optional.empty()));
			assertThat(_1.first(), is(Optional.of(Entries.of("1", 1))));
			assertThat(_12.first(), is(Optional.of(Entries.of("1", 1))));
			assertThat(_123.first(), is(Optional.of(Entries.of("1", 1))));
		});
	}

	@Test
	public void second() {
		twice(() -> {
			assertThat(empty.second(), is(Optional.empty()));
			assertThat(_1.second(), is(Optional.empty()));
			assertThat(_12.second(), is(Optional.of(Entries.of("2", 2))));
			assertThat(_123.second(), is(Optional.of(Entries.of("2", 2))));
			assertThat(_1234.second(), is(Optional.of(Entries.of("2", 2))));
		});
	}

	@Test
	public void third() {
		twice(() -> {
			assertThat(empty.third(), is(Optional.empty()));
			assertThat(_1.third(), is(Optional.empty()));
			assertThat(_12.third(), is(Optional.empty()));
			assertThat(_123.third(), is(Optional.of(Entries.of("3", 3))));
			assertThat(_1234.third(), is(Optional.of(Entries.of("3", 3))));
			assertThat(_12345.third(), is(Optional.of(Entries.of("3", 3))));
		});
	}

	@Test
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(Optional.empty()));
			assertThat(_1.last(), is(Optional.of(Entries.of("1", 1))));
			assertThat(_12.last(), is(Optional.of(Entries.of("2", 2))));
			assertThat(_123.last(), is(Optional.of(Entries.of("3", 3))));
		});
	}

	@Test
	public void window() {
		twice(() -> assertThat(_12345.window(3),
		                       contains(contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3)),
		                                contains(Entries.of("2", 2), Entries.of("3", 3), Entries.of("4", 4)),
		                                contains(Entries.of("3", 3), Entries.of("4", 4), Entries.of("5", 5)))));
	}

	@Test
	public void windowWithStep() {
		twice(() -> assertThat(_12345.window(3, 2),
		                       contains(contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3)),
		                                contains(Entries.of("3", 3), Entries.of("4", 4), Entries.of("5", 5)))));
	}

	@Test
	public void batch() {
		twice(() -> assertThat(_12345.batch(3),
		                       contains(contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3)),
		                                contains(Entries.of("4", 4), Entries.of("5", 5)))));
	}

	@SuppressWarnings("uncheckeed")
	@Test
	public void batchOnPredicate() {
		Sequence<EntrySequence<String, Integer>> emptyPartitioned = empty.batch((a, b) -> a.getValue() > b.getValue());
		twice(() -> assertThat(emptyPartitioned, is(emptyIterable())));

		Sequence<EntrySequence<String, Integer>> onePartitioned = _1.batch((a, b) -> a.getValue() > b.getValue());
		twice(() -> assertThat(onePartitioned, contains(contains(Pair.of("1", 1)))));

		Sequence<EntrySequence<String, Integer>> twoPartitioned = _12.batch((a, b) -> a.getValue() > b.getValue());
		twice(() -> assertThat(twoPartitioned, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<EntrySequence<String, Integer>> threePartitioned = _123.batch((a, b) -> a.getValue() > b.getValue());
		twice(() -> assertThat(threePartitioned,
		                       contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)))));

		Sequence<EntrySequence<String, Integer>> threeRandomPartitioned =
				random3.batch((a, b) -> a.getValue() > b.getValue());
		twice(() -> assertThat(threeRandomPartitioned,
		                       contains(contains(Pair.of("4", 4)), contains(Pair.of("2", 2), Pair.of("3", 3)))));

		Sequence<EntrySequence<String, Integer>> nineRandomPartitioned =
				random9.batch((a, b) -> a.getValue() > b.getValue());
		twice(() -> assertThat(nineRandomPartitioned,
		                       contains(contains(Pair.of("67", 67)), contains(Pair.of("5", 5), Pair.of("43", 43)),
		                                contains(Pair.of("3", 3), Pair.of("5", 5), Pair.of("7", 7), Pair.of("24", 24)),
		                                contains(Pair.of("5", 5), Pair.of("67", 67)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnQuaternaryPredicate() {
		Sequence<EntrySequence<String, Integer>> emptyPartitioned = empty.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(emptyPartitioned, is(emptyIterable())));

		Sequence<EntrySequence<String, Integer>> onePartitioned = _1.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(onePartitioned, contains(contains(Pair.of("1", 1)))));

		Sequence<EntrySequence<String, Integer>> twoPartitioned = _12.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(twoPartitioned, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<EntrySequence<String, Integer>> threePartitioned = _123.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(threePartitioned,
		                       contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)))));

		Sequence<EntrySequence<String, Integer>> threeRandomPartitioned = random3.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(threeRandomPartitioned,
		                       contains(contains(Pair.of("4", 4)), contains(Pair.of("2", 2), Pair.of("3", 3)))));

		Sequence<EntrySequence<String, Integer>> nineRandomPartitioned = random9.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(nineRandomPartitioned,
		                       contains(contains(Pair.of("67", 67)), contains(Pair.of("5", 5), Pair.of("43", 43)),
		                                contains(Pair.of("3", 3), Pair.of("5", 5), Pair.of("7", 7), Pair.of("24", 24)),
		                                contains(Pair.of("5", 5), Pair.of("67", 67)))));
	}

	@Test
	public void step() {
		twice(() -> assertThat(_123456789.step(3),
		                       contains(Entries.of("1", 1), Entries.of("4", 4), Entries.of("7", 7))));
	}

	@Test
	public void distinct() {
		EntrySequence<String, Integer> emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));

		EntrySequence<String, Integer> oneDistinct = random1.distinct();
		twice(() -> assertThat(oneDistinct, contains(Entries.of("17", 17))));

		EntrySequence<String, Integer> twoDuplicatesDistinct =
				EntrySequence.of(Entries.of("17", 17), Entries.of("17", 17)).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, contains(Entries.of("17", 17))));

		EntrySequence<String, Integer> nineDistinct = random9.distinct();
		twice(() -> assertThat(nineDistinct, contains(Entries.of("67", 67), Entries.of("5", 5), Entries.of("43",
		                                                                                                   43),
		                                              Entries.of("3", 3), Entries.of("7", 7),
		                                              Entries.of("24", 24))));
	}

	@Test
	public void sorted() {
		EntrySequence<String, Integer> emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));

		EntrySequence<String, Integer> oneSorted = random1.sorted();
		twice(() -> assertThat(oneSorted, contains(Entries.of("17", 17))));

		EntrySequence<String, Integer> twoSorted = random2.sorted();
		twice(() -> assertThat(twoSorted, contains(Entries.of("17", 17), Entries.of("32", 32))));

		EntrySequence<String, Integer> nineSorted = random9.sorted();
		twice(() -> assertThat(nineSorted, // String sorting on first item
		                       contains(Entries.of("24", 24), Entries.of("3", 3), Entries.of("43", 43),
		                                Entries.of("5", 5), Entries.of("5", 5), Entries.of("5", 5),
		                                Entries.of("67", 67), Entries.of("67", 67), Entries.of("7", 7))));
	}

	@Test
	public void sortedComparator() {
		EntrySequence<String, Integer> emptySorted = empty.sorted((Comparator) Comparator.reverseOrder());
		twice(() -> assertThat(emptySorted, emptyIterable()));

		EntrySequence<String, Integer> oneSorted = random1.sorted((Comparator) Comparator.reverseOrder());
		twice(() -> assertThat(oneSorted, contains(Entries.of("17", 17))));

		EntrySequence<String, Integer> twoSorted = random2.sorted((Comparator) Comparator.reverseOrder());
		twice(() -> assertThat(twoSorted, contains(Entries.of("32", 32), Entries.of("17", 17))));

		EntrySequence<String, Integer> nineSorted = random9.sorted((Comparator) Comparator.reverseOrder());
		twice(() -> assertThat(nineSorted, // String sorting on first item reverse
		                       contains(Entries.of("7", 7), Entries.of("67", 67), Entries.of("67", 67),
		                                Entries.of("5", 5), Entries.of("5", 5), Entries.of("5", 5),
		                                Entries.of("43", 43), Entries.of("3", 3), Entries.of("24", 24))));
	}

	@Test
	public void min() {
		Optional<Entry<String, Integer>> emptyMin = empty.min((Comparator) Comparator.naturalOrder());
		twice(() -> assertThat(emptyMin, is(Optional.empty())));

		Optional<Entry<String, Integer>> oneMin = random1.min((Comparator) Comparator.naturalOrder());
		twice(() -> assertThat(oneMin, is(Optional.of(Entries.of("17", 17)))));

		Optional<Entry<String, Integer>> twoMin = random2.min((Comparator) Comparator.naturalOrder());
		twice(() -> assertThat(twoMin, is(Optional.of(Entries.of("17", 17)))));

		Optional<Entry<String, Integer>> nineMin = random9.min((Comparator) Comparator.naturalOrder());
		twice(() -> assertThat(nineMin, is(Optional.of(Entries.of("24", 24)))));
	}

	@Test
	public void max() {
		Optional<Entry<String, Integer>> emptyMax = empty.max((Comparator) Comparator.naturalOrder());
		twice(() -> assertThat(emptyMax, is(Optional.empty())));

		Optional<Entry<String, Integer>> oneMax = random1.max((Comparator) Comparator.naturalOrder());
		twice(() -> assertThat(oneMax, is(Optional.of(Entries.of("17", 17)))));

		Optional<Entry<String, Integer>> twoMax = random2.max((Comparator) Comparator.naturalOrder());
		twice(() -> assertThat(twoMax, is(Optional.of(Entries.of("32", 32)))));

		Optional<Entry<String, Integer>> nineMax = random9.max((Comparator) Comparator.naturalOrder());
		twice(() -> assertThat(nineMax, is(Optional.of(Entries.of("7", 7)))));
	}

	@Test
	public void count() {
		twice(() -> assertThat(empty.count(), is(0)));
		twice(() -> assertThat(_1.count(), is(1)));
		twice(() -> assertThat(_12.count(), is(2)));
		twice(() -> assertThat(_123456789.count(), is(9)));
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
		EntrySequence<String, Integer> peek = _123.peek((s, x) -> assertThat(x, is(both(greaterThanOrEqualTo(1)).and(
				lessThanOrEqualTo(3)).and(equalTo(parseInt(s))))));
		twice(() -> assertThat(peek, contains(entries123)));
	}

	@Test
	public void repeat() {
		EntrySequence<String, Integer> repeatEmpty = empty.repeat();
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		EntrySequence<String, Integer> repeatOne = _1.repeat();
		twice(() -> assertThat(repeatOne.limit(3),
		                       contains(Entries.of("1", 1), Entries.of("1", 1), Entries.of("1", 1))));

		EntrySequence<String, Integer> repeatTwo = _12.repeat();
		twice(() -> assertThat(repeatTwo.limit(5),
		                       contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("1", 1),
		                                Entries.of("2", 2), Entries.of("1", 1))));

		EntrySequence<String, Integer> repeatThree = _123.repeat();
		twice(() -> assertThat(repeatThree.limit(8),
		                       contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3),
		                                Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3),
		                                Entries.of("1", 1), Entries.of("2", 2))));

		EntrySequence<String, Integer> repeatVarying = EntrySequence.from(new Iterable<Entry<String, Integer>>() {
			private List<Entry<String, Integer>> list =
					asList(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3));
			int end = list.size();

			@Override
			public Iterator<Entry<String, Integer>> iterator() {
				List<Entry<String, Integer>> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				return subList.iterator();
			}
		}).repeat();
		assertThat(repeatVarying,
		           contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3), Entries.of("1", 1),
		                    Entries.of("2", 2), Entries.of("1", 1)));
	}

	@Test
	public void repeatTwice() {
		EntrySequence<String, Integer> repeatEmpty = empty.repeat(2);
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		EntrySequence<String, Integer> repeatOne = _1.repeat(2);
		twice(() -> assertThat(repeatOne, contains(Entries.of("1", 1), Entries.of("1", 1))));

		EntrySequence<String, Integer> repeatTwo = _12.repeat(2);
		twice(() -> assertThat(repeatTwo, contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("1", 1),
		                                           Entries.of("2", 2))));

		EntrySequence<String, Integer> repeatThree = _123.repeat(2);
		twice(() -> assertThat(repeatThree, contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3),
		                                             Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3))));

		EntrySequence<String, Integer> repeatVarying = EntrySequence.from(new Iterable<Entry<String, Integer>>() {
			private List<Entry<String, Integer>> list =
					asList(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3));
			int end = list.size();

			@Override
			public Iterator<Entry<String, Integer>> iterator() {
				List<Entry<String, Integer>> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				return subList.iterator();
			}
		}).repeat(2);
		assertThat(repeatVarying,
		           contains(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3), Entries.of("1", 1),
		                    Entries.of("2", 2)));
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
	public void reverse() {
		EntrySequence<String, Integer> emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));

		EntrySequence<String, Integer> oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, contains(Entries.of("1", 1))));

		EntrySequence<String, Integer> twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, contains(Entries.of("2", 2), Entries.of("1", 1))));

		EntrySequence<String, Integer> threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, contains(Entries.of("3", 3), Entries.of("2", 2), Entries.of("1", 1)
		)));

		EntrySequence<String, Integer> nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed, contains(Entries.of("9", 9), Entries.of("8", 8), Entries.of("7", 7),
		                                              Entries.of("6", 6), Entries.of("5", 5), Entries.of("4", 4),
		                                              Entries.of("3", 3), Entries.of("2", 2), Entries.of("1", 1))));
	}

	@Test
	public void shuffle() {
		assertThat(empty.shuffle(), is(emptyIterable()));
		assertThat(_1.shuffle(), contains(Entries.of("1", 1)));
		assertThat(_12.shuffle(), containsInAnyOrder(Entries.of("1", 1), Entries.of("2", 2)));
		assertThat(_123.shuffle(), containsInAnyOrder(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3)));
		assertThat(_123456789.shuffle(),
		           containsInAnyOrder(Entries.of("1", 1), Entries.of("2", 2), Entries.of("3", 3),
		                              Entries.of("4", 4), Entries.of("5", 5), Entries.of("6", 6),
		                              Entries.of("7", 7), Entries.of("8", 8), Entries.of("9", 9)));
	}

	@Test
	public void shuffleWithRandomSource() {
		Random seed = new Random(17);

		assertThat(empty.shuffle(seed), is(emptyIterable()));
		assertThat(_1.shuffle(seed), contains(Entries.of("1", 1)));
		assertThat(_12.shuffle(seed), contains(Entries.of("1", 1), Entries.of("2", 2)));
		assertThat(_123.shuffle(seed), contains(Entries.of("3", 3), Entries.of("2", 2), Entries.of("1", 1)));
		assertThat(_123456789.shuffle(seed),
		           contains(Entries.of("2", 2), Entries.of("9", 9), Entries.of("4", 4), Entries.of("6", 6),
		                    Entries.of("8", 8), Entries.of("7", 7), Entries.of("5", 5), Entries.of("1", 1),
		                    Entries.of("3", 3)));
	}

	@Test
	public void flattenKeys() {
		EntrySequence<String, Integer> flattened =
				EntrySequence.<List<String>, Integer>ofEntries(asList("1", "2", "3"), 1, emptyList(), "4",
				                                               asList("5", "6", "7"), 3).flattenKeys(Entry::getKey);
		twice(() -> assertThat(flattened, contains(Entries.of("1", 1), Entries.of("2", 1), Entries.of("3", 1),
		                                           Entries.of("5", 3), Entries.of("6", 3), Entries.of("7", 3))));
	}

	@Test
	public void flattenValues() {
		EntrySequence<String, Integer> flattened =
				EntrySequence.<String, List<Integer>>ofEntries("1", asList(1, 2, 3), "2", emptyList(), "3",
				                                               asList(2, 3, 4)).flattenValues(Entry::getValue);
		twice(() -> assertThat(flattened, contains(Entries.of("1", 1), Entries.of("1", 2), Entries.of("1", 3),
		                                           Entries.of("3", 2), Entries.of("3", 3), Entries.of("3", 4))));
	}
}
