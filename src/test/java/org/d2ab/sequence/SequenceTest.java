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

import org.d2ab.collection.Maps;
import org.d2ab.util.Arrayz;
import org.d2ab.util.Entries;
import org.d2ab.util.Pair;
import org.junit.Test;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SequenceTest {
	private final Sequence<Integer> empty = Sequence.<Integer>empty();
	private final Sequence<Integer> _1 = Sequence.of(1);
	private final Sequence<Integer> _12 = Sequence.of(1, 2);
	private final Sequence<Integer> _123 = Sequence.of(1, 2, 3);
	private final Sequence<Integer> _1234 = Sequence.of(1, 2, 3, 4);
	private final Sequence<Integer> _12345 = Sequence.of(1, 2, 3, 4, 5);
	private final Sequence<Integer> _123456789 = Sequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
	private final Sequence<Integer> oneRandom = Sequence.of(17);
	private final Sequence<Integer> twoRandom = Sequence.of(17, 32);
	private final Sequence<Integer> threeRandom = Sequence.of(2, 3, 1);
	private final Sequence<Integer> nineRandom = Sequence.of(67, 5, 43, 3, 5, 7, 24, 5, 67);

	@Test
	public void ofOne() {
		twice(() -> assertThat(_1, contains(1)));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(_123, contains(1, 2, 3)));
	}

	@Test
	public void forLoop() {
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
	public void forEach() {
		twice(() -> {
			empty.forEach(i -> fail("Should not get called"));
			_1.forEach(i -> assertThat(i, is(in(singletonList(1)))));
			_12.forEach(i -> assertThat(i, is(in(asList(1, 2)))));
			_123.forEach(i -> assertThat(i, is(in(asList(1, 2, 3)))));
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
		Sequence<Integer> sequence = Sequence.<Integer>of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void ofNulls() {
		Sequence<Integer> sequence = Sequence.of(1, null, 2, 3, null);

		twice(() -> assertThat(sequence, contains(1, null, 2, 3, null)));
	}

	@Test
	public void fromSequence() {
		Sequence<Integer> fromSequence = Sequence.from(_123);

		twice(() -> assertThat(fromSequence, contains(1, 2, 3)));
	}

	@Test
	public void fromIterable() {
		Iterable<Integer> iterable = () -> asList(1, 2, 3).iterator();

		Sequence<Integer> sequenceFromIterable = Sequence.from(iterable);

		twice(() -> assertThat(sequenceFromIterable, contains(1, 2, 3)));
	}

	@Test
	public void fromStream() {
		Sequence<Integer> sequenceFromStream = Sequence.from(asList(1, 2, 3).stream());

		assertThat(sequenceFromStream, contains(1, 2, 3));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromEmptyStream() {
		Sequence<Integer> sequenceFromStream = Sequence.from(Stream.of());

		assertThat(sequenceFromStream, is(emptyIterable()));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromIteratorSupplier() {
		Supplier<Iterator<Integer>> iterators = () -> asList(1, 2, 3).iterator();

		Sequence<Integer> sequenceFromIterators = Sequence.from(iterators);

		twice(() -> assertThat(sequenceFromIterators, contains(1, 2, 3)));
	}

	@Test
	public void fromIterables() {
		Iterable<Integer> first = asList(1, 2, 3);
		Iterable<Integer> second = asList(4, 5, 6);
		Iterable<Integer> third = asList(7, 8, 9);

		Sequence<Integer> sequenceFromIterables = Sequence.from(first, second, third);

		twice(() -> assertThat(sequenceFromIterables, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void fromNoIterables() {
		Sequence<Integer> sequenceFromNoIterables = Sequence.<Integer>from();

		twice(() -> assertThat(sequenceFromNoIterables, is(emptyIterable())));
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
		Sequence<Integer> appended = _123.append(Sequence.of(4, 5, 6)).append(Sequence.of(7, 8));

		twice(() -> assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIterator() {
		Sequence<Integer> appended = _123.append(Arrayz.iterator(4, 5, 6)).append(Arrayz.iterator(7, 8));

		assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, contains(1, 2, 3));
	}

	@Test
	public void appendStream() {
		Sequence<Integer> appended = _123.append(Stream.of(4, 5, 6)).append(Stream.of(7, 8));

		assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8));

		Iterator<Integer> iterator = appended.iterator();
		assertThat(iterator.next(), is(1)); // First three are ok
		assertThat(iterator.next(), is(2));
		assertThat(iterator.next(), is(3));

		expecting(IllegalStateException.class, iterator::next); // Hitting Stream that is exhausted
	}

	@Test
	public void appendArray() {
		Sequence<Integer> appended = _123.append(4, 5, 6).append(7, 8);

		twice(() -> assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendIsLazy() {
		Iterator<Integer> first = asList(1, 2, 3).iterator();
		Iterator<Integer> second = asList(4, 5, 6).iterator();
		Iterator<Integer> third = asList(7, 8).iterator();

		Sequence<Integer> then = Sequence.from(first).append(() -> second).append(() -> third);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));
		assertThat(third.hasNext(), is(true));

		assertThat(then, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(then, is(emptyIterable())); // iterators exhausted on second run
	}

	@Test
	public void thenIsLazyWhenSkippingHasNext() {
		Iterator<Integer> first = singletonList(1).iterator();
		Iterator<Integer> second = singletonList(2).iterator();

		Sequence<Integer> sequence = Sequence.from(first).append(() -> second);

		// check delayed iteration
		Iterator<Integer> iterator = sequence.iterator();
		assertThat(iterator.next(), is(1));
		assertThat(iterator.next(), is(2));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void filter() {
		Sequence<Integer> filtered = Sequence.of(1, 2, 3, 4, 5, 6, 7).filter(i -> (i % 2) == 0);

		twice(() -> assertThat(filtered, contains(2, 4, 6)));
	}

	@Test
	public void flatMapIterables() {
		Sequence<List<Integer>> sequence = Sequence.of(asList(1, 2), asList(3, 4), asList(5, 6));

		Sequence<Integer> flatMap = sequence.flatten(Function.identity());

		twice(() -> assertThat(flatMap, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void flatMapLazy() {
		Sequence<Integer> flatMap = Sequence.of(asList(1, 2), (Iterable<Integer>) () -> {
			throw new IllegalStateException();
		}).flatten(Function.identity());

		twice(() -> {
			Iterator<Integer> iterator = flatMap.iterator(); // NPE if not lazy - expected later below
			assertThat(iterator.next(), is(1));
			assertThat(iterator.next(), is(2));
			expecting(IllegalStateException.class, iterator::next);
		});
	}

	@Test
	public void flatMapIterators() {
		Sequence<Iterator<Integer>> sequence =
				Sequence.of(asList(1, 2).iterator(), asList(3, 4).iterator(), asList(5, 6).iterator());
		Sequence<Integer> flatMap = sequence.flatten(Sequence::from);
		assertThat(flatMap, contains(1, 2, 3, 4, 5, 6));
		assertThat(flatMap, is(emptyIterable()));
	}

	@Test
	public void flatMapArrays() {
		Sequence<Integer[]> sequence = Sequence.of(new Integer[]{1, 2}, new Integer[]{3, 4}, new Integer[]{5, 6});

		Sequence<Integer> flatMap = sequence.flatten(Sequence::of);

		twice(() -> assertThat(flatMap, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void flattenIterables() {
		Sequence<Integer> flattened = Sequence.of(asList(1, 2), asList(3, 4), asList(5, 6)).flatten();

		twice(() -> assertThat(flattened, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void flattenLazy() {
		Sequence<Integer> flattened = Sequence.of(asList(1, 2), (Iterable<Integer>) () -> {
			throw new IllegalStateException();
		}).flatten();

		twice(() -> {
			// NPE if not lazy - see below
			Iterator<Integer> iterator = flattened.iterator();
			assertThat(iterator.next(), is(1));
			assertThat(iterator.next(), is(2));
			expecting(IllegalStateException.class, iterator::next);
		});
	}

	@Test
	public void flattenIterators() {
		Sequence<Iterator<Integer>> sequence =
				Sequence.of(asList(1, 2).iterator(), asList(3, 4).iterator(), asList(5, 6).iterator());
		Sequence<Integer> flattened = sequence.flatten();
		assertThat(flattened, contains(1, 2, 3, 4, 5, 6));
		assertThat(flattened, is(emptyIterable()));
	}

	@Test
	public void flattenArrays() {
		Sequence<Integer[]> sequence = Sequence.of(new Integer[]{1, 2}, new Integer[]{3, 4}, new Integer[]{5, 6});

		Sequence<Integer> flattened = sequence.flatten();
		twice(() -> assertThat(flattened, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void map() {
		Sequence<String> mapped = _123.map(Object::toString);
		twice(() -> assertThat(mapped, contains("1", "2", "3")));
	}

	@Test
	public void mapIsLazy() {
		Sequence<Integer> sequence = Sequence.of(1, null);

		twice(() -> {
			Iterator<String> iterator = sequence.map(Object::toString).iterator(); // NPE here if not lazy
			assertThat(iterator.next(), is("1"));
			expecting(NullPointerException.class, iterator::next);
		});
	}

	@Test
	public void mapBack() {
		twice(() -> assertThat(_123.mapBack((p, n) -> p), contains(null, 1, 2)));
		twice(() -> assertThat(_123.mapBack((p, n) -> n), contains(1, 2, 3)));
	}

	@Test
	public void mapForward() {
		twice(() -> assertThat(_123.mapForward((n, f) -> n), contains(1, 2, 3)));
		twice(() -> assertThat(_123.mapForward((n, f) -> f), contains(2, 3, null)));
	}

	@Test
	public void peekBack() {
		twice(() -> assertThat(_123.peekBack((p, n) -> {
			assertThat(n, is(oneOf(1, 2, 3)));
			assertThat(p, is(n == 1 ? null : n - 1));
		}), contains(1, 2, 3)));
	}

	@Test
	public void peekForward() {
		twice(() -> assertThat(_123.peekForward((n, f) -> {
			assertThat(n, is(oneOf(1, 2, 3)));
			assertThat(f, is(n == 3 ? null : n + 1));
		}), contains(1, 2, 3)));
	}

	@Test
	public void recurse() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i + 1);
		twice(() -> assertThat(sequence.limit(10), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void recurseTwins() {
		Sequence<String> sequence = Sequence.recurse(1, Object::toString, s -> Integer.parseInt(s) + 1);
		twice(() -> assertThat(sequence.limit(10), contains("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")));
	}

	@Test
	public void untilTerminal() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i + 1).until(7);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void untilNull() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> (i < 10) ? (i + 1) : null).untilNull();
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void untilPredicate() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i + 1).until(i -> i == 7);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void endingAtTerminal() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i + 1).endingAt(7);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7)));
	}

	@Test
	public void endingAtNull() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> (i < 10) ? (i + 1) : null).endingAtNull();
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, null)));
	}

	@Test
	public void endingAtPredicate() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i + 1).endingAt(i -> i == 7);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7)));
	}

	@Test
	public void toList() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5, 6, 7);

		twice(() -> {
			List<Integer> list = sequence.toList();
			assertThat(list, instanceOf(ArrayList.class));
			assertThat(list, contains(1, 2, 3, 4, 5, 6, 7));
		});
	}

	@Test
	public void toLinkedList() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5, 6, 7);

		twice(() -> {
			List<Integer> list = sequence.toList(LinkedList::new);
			assertThat(list, instanceOf(LinkedList.class));
			assertThat(list, contains(1, 2, 3, 4, 5, 6, 7));
		});
	}

	@Test
	public void toSet() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5, 6, 7);

		twice(() -> {
			Set<Integer> set = sequence.toSet();
			assertThat(set, instanceOf(HashSet.class));
			assertThat(set, containsInAnyOrder(1, 2, 3, 4, 5, 6, 7));
		});
	}

	@Test
	public void toSortedSet() {
		Sequence<Integer> sequence = Sequence.of(1, 5, 2, 6, 3, 4, 7);

		twice(() -> {
			SortedSet<Integer> sortedSet = sequence.toSortedSet();
			assertThat(sortedSet, instanceOf(TreeSet.class));
			assertThat(sortedSet, contains(1, 2, 3, 4, 5, 6, 7));
		});
	}

	@Test
	public void toSetWithType() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5, 6, 7);

		twice(() -> {
			Set<Integer> set = sequence.toSet(LinkedHashSet::new);
			assertThat(set, instanceOf(LinkedHashSet.class));
			assertThat(set, contains(1, 2, 3, 4, 5, 6, 7));
		});
	}

	@Test
	public void toCollection() {
		Sequence<Integer> sequence = _123;

		twice(() -> {
			Deque<Integer> deque = sequence.toCollection(ArrayDeque::new);
			assertThat(deque, instanceOf(ArrayDeque.class));
			assertThat(deque, contains(1, 2, 3));
		});
	}

	@Test
	public void collectIntoCollection() {
		Sequence<Integer> sequence = _123;

		twice(() -> {
			Deque<Integer> deque = new ArrayDeque<>();
			Deque<Integer> result = sequence.collectInto(deque);
			assertThat(result, is(sameInstance(deque)));
			assertThat(result, contains(1, 2, 3));
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
	public void toMapWithWithMapper() {
		Function<Integer, Entry<Integer, String>> mapper = i -> Entries.of(i, String.valueOf(i));

		twice(() -> {
			Map<Integer, String> map = _123.toMap(mapper);
			assertThat(map, instanceOf(HashMap.class));
			assertThat(map, is(equalTo(Maps.builder(1, "1").put(2, "2").put(3, "3").build())));

			Map<Integer, String> linkedMap = _123.toMap((Supplier<Map<Integer, String>>) LinkedHashMap::new, mapper);
			assertThat(linkedMap, instanceOf(HashMap.class));
			assertThat(linkedMap, is(equalTo(Maps.builder(1, "1").put(2, "2").put(3, "3").build())));
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
	public void toSortedMapWithMapper() {
		twice(() -> {
			SortedMap<String, Integer> sortedMap = threeRandom.toSortedMap(x -> Entries.of(x.toString(), x));

			assertThat(sortedMap, instanceOf(TreeMap.class));
			assertThat(sortedMap, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).build())));
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
			Deque<Integer> deque = _123.collect(ArrayDeque::new, ArrayDeque::add);

			assertThat(deque, instanceOf(ArrayDeque.class));
			assertThat(deque, contains(1, 2, 3));
		});
	}

	@Test
	public void collectIntoContainer() {
		twice(() -> {
			Deque<Integer> deque = new ArrayDeque<>();
			Deque<Integer> result = _123.collectInto(deque, Deque::add);

			assertThat(result, is(sameInstance(deque)));
			assertThat(result, contains(1, 2, 3));
		});
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(_123.toArray(), is(arrayContaining(1, 2, 3))));
	}

	@Test
	public void toArrayWithType() {
		twice(() -> assertThat(_123.toArray(Integer[]::new), arrayContaining(1, 2, 3)));
	}

	@Test
	public void collector() {
		twice(() -> assertThat(_123.collect(Collectors.toList()), contains(1, 2, 3)));
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
		twice(() -> {
			assertThat(empty.reduce(Integer::sum), is(Optional.empty()));
			assertThat(_1.reduce(Integer::sum), is(Optional.of(1)));
			assertThat(_12.reduce(Integer::sum), is(Optional.of(3)));
			assertThat(_123.reduce(Integer::sum), is(Optional.of(6)));
		});
	}

	@Test
	public void reduceWithIdentity() {
		twice(() -> {
			assertThat(empty.reduce(17, Integer::sum), is(17));
			assertThat(_1.reduce(17, Integer::sum), is(18));
			assertThat(_12.reduce(17, Integer::sum), is(20));
			assertThat(_123.reduce(17, Integer::sum), is(23));
		});
	}

	@Test
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(Optional.empty()));
			assertThat(_1.first(), is(Optional.of(1)));
			assertThat(_12.first(), is(Optional.of(1)));
			assertThat(_123.first(), is(Optional.of(1)));
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
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(Optional.empty()));
			assertThat(_1.last(), is(Optional.of(1)));
			assertThat(_12.last(), is(Optional.of(2)));
			assertThat(_123.last(), is(Optional.of(3)));
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void entries() {
		Sequence<Entry<Integer, Integer>> emptyEntries = empty.entries();
		twice(() -> assertThat(emptyEntries, is(emptyIterable())));

		Sequence<Entry<Integer, Integer>> oneEntries = _1.entries();
		twice(() -> assertThat(oneEntries, contains(Entries.of(1, null))));

		Sequence<Entry<Integer, Integer>> twoEntries = _12.entries();
		twice(() -> assertThat(twoEntries, contains(Entries.of(1, 2))));

		Sequence<Entry<Integer, Integer>> threeEntries = _123.entries();
		twice(() -> assertThat(threeEntries, contains(Entries.of(1, 2), Entries.of(2, 3))));

		Sequence<Entry<Integer, Integer>> fiveEntries = _12345.entries();
		twice(() -> assertThat(fiveEntries,
		                       contains(Entries.of(1, 2), Entries.of(2, 3), Entries.of(3, 4), Entries.of(4, 5))));
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

		BiSequence<Integer, String> oneBiSequence = Sequence.of(Pair.of(1, "1")).toBiSequence();
		twice(() -> assertThat(oneBiSequence, contains(Pair.of(1, "1"))));

		BiSequence<Integer, String> twoBiSequence = Sequence.of(Pair.of(1, "1"), Pair.of(2, "2")).toBiSequence();
		twice(() -> assertThat(twoBiSequence, contains(Pair.of(1, "1"), Pair.of(2, "2"))));

		BiSequence<Integer, String> threeBiSequence =
				Sequence.of(Pair.of(1, "1"), Pair.of(2, "2"), Pair.of(3, "3")).toBiSequence();
		twice(() -> assertThat(threeBiSequence, contains(Pair.of(1, "1"), Pair.of(2, "2"), Pair.of(3, "3"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void entrySequence() {
		EntrySequence<Integer, String> emptyEntrySequence = empty.toEntrySequence();
		twice(() -> assertThat(emptyEntrySequence, is(emptyIterable())));

		EntrySequence<Integer, String> oneEntrySequence = Sequence.of(Entries.of(1, "1")).toEntrySequence();
		twice(() -> assertThat(oneEntrySequence, contains(Entries.of(1, "1"))));

		EntrySequence<Integer, String> twoEntrySequence =
				Sequence.of(Entries.of(1, "1"), Entries.of(2, "2")).toEntrySequence();
		twice(() -> assertThat(twoEntrySequence, contains(Entries.of(1, "1"), Entries.of(2, "2"))));

		EntrySequence<Integer, String> threeEntrySequence =
				Sequence.of(Entries.of(1, "1"), Entries.of(2, "2"), Entries.of(3, "3")).toEntrySequence();
		twice(() -> assertThat(threeEntrySequence,
		                       contains(Entries.of(1, "1"), Entries.of(2, "2"), Entries.of(3, "3"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void window() {
		twice(() -> assertThat(empty.window(3), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3), contains(singletonList(1))));
		twice(() -> assertThat(_12.window(3), contains(asList(1, 2))));
		twice(() -> assertThat(_123.window(3), contains(asList(1, 2, 3))));
		twice(() -> assertThat(_1234.window(3), contains(asList(1, 2, 3), asList(2, 3, 4))));
		twice(() -> assertThat(_12345.window(3), contains(asList(1, 2, 3), asList(2, 3, 4), asList(3, 4, 5))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithStep() {
		twice(() -> assertThat(empty.window(3, 2), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3, 2), contains(singletonList(1))));
		twice(() -> assertThat(_12.window(3, 2), contains(asList(1, 2))));
		twice(() -> assertThat(_123.window(3, 2), contains(asList(1, 2, 3))));
		twice(() -> assertThat(_1234.window(3, 2), contains(asList(1, 2, 3), asList(3, 4))));
		twice(() -> assertThat(_12345.window(3, 2), contains(asList(1, 2, 3), asList(3, 4, 5))));
		twice(() -> assertThat(_123456789.window(3, 2),
		                       contains(asList(1, 2, 3), asList(3, 4, 5), asList(5, 6, 7), asList(7, 8, 9))));

		twice(() -> assertThat(empty.window(3, 4), is(emptyIterable())));
		twice(() -> assertThat(_1.window(3, 4), contains(singletonList(1))));
		twice(() -> assertThat(_12.window(3, 4), contains(asList(1, 2))));
		twice(() -> assertThat(_123.window(3, 4), contains(asList(1, 2, 3))));
		twice(() -> assertThat(_1234.window(3, 4), contains(asList(1, 2, 3))));
		twice(() -> assertThat(_12345.window(3, 4), contains(asList(1, 2, 3), singletonList(5))));
		twice(() -> assertThat(_123456789.window(3, 4), contains(asList(1, 2, 3), asList(5, 6, 7), singletonList(9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void partition() {
		twice(() -> assertThat(empty.partition(3), is(emptyIterable())));
		twice(() -> assertThat(_1.partition(3), contains(singletonList(1))));
		twice(() -> assertThat(_12.partition(3), contains(asList(1, 2))));
		twice(() -> assertThat(_123.partition(3), contains(asList(1, 2, 3))));
		twice(() -> assertThat(_1234.partition(3), contains(asList(1, 2, 3), singletonList(4))));
		twice(() -> assertThat(_12345.partition(3), contains(asList(1, 2, 3), asList(4, 5))));
		twice(() -> assertThat(_123456789.partition(3), contains(asList(1, 2, 3), asList(4, 5, 6), asList(7, 8, 9))));
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

		Sequence<Integer> twoDuplicatesDistinct = Sequence.of(17, 17).distinct();
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
		Sequence<Integer> peek = _123.peek(x -> assertThat(x, is(both(greaterThan(0)).and(lessThan(4)))));
		twice(() -> assertThat(peek, contains(1, 2, 3)));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(empty, is(emptyIterable()));

		assertThat(_123.stream().collect(Collectors.toList()), contains(1, 2, 3));
		assertThat(_123, contains(1, 2, 3));
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
		assertThat(Sequence.ints().limit(3), contains(1, 2, 3));
		assertThat(Sequence.ints().limit(7777).last(), is(Optional.of(7777)));
	}

	@Test
	public void longs() {
		assertThat(Sequence.longs().limit(3), contains(1L, 2L, 3L));
		assertThat(Sequence.longs().limit(7777).last(), is(Optional.of(7777L)));
	}

	@Test
	public void chars() {
		assertThat(Sequence.chars().limit(3), contains('\u0000', '\u0001', '\u0002'));
		assertThat(Sequence.chars().limit(0xC0).last(), is(Optional.of('¿')));
		assertThat(Sequence.chars().count(), is(65536L));
	}

	@Test
	public void intsStartingAt() {
		assertThat(Sequence.ints(17).limit(3), contains(17, 18, 19));
		assertThat(Sequence.ints(777).limit(7000).last(), is(Optional.of(7776)));
		assertThat(Sequence.ints(Integer.MAX_VALUE), contains(Integer.MAX_VALUE));
	}

	@Test
	public void longsStartingAt() {
		assertThat(Sequence.longs(17).limit(3), contains(17L, 18L, 19L));
		assertThat(Sequence.longs(777).limit(7000).last(), is(Optional.of(7776L)));
		assertThat(Sequence.longs(Long.MAX_VALUE), contains(Long.MAX_VALUE));
	}

	@Test
	public void charsStartingAt() {
		assertThat(Sequence.chars('A').limit(3), contains('A', 'B', 'C'));
		assertThat(Sequence.chars('\u1400').limit(3).last(), is(Optional.of('\u1402')));
		assertThat(Sequence.chars(Character.MAX_VALUE), contains(Character.MAX_VALUE));
	}

	@Test
	public void intRange() {
		assertThat(Sequence.range(17, 20), contains(17, 18, 19, 20));
		assertThat(Sequence.range(20, 17), contains(20, 19, 18, 17));
	}

	@Test
	public void longRange() {
		assertThat(Sequence.range(17L, 20L), contains(17L, 18L, 19L, 20L));
		assertThat(Sequence.range(20L, 17L), contains(20L, 19L, 18L, 17L));
	}

	@Test
	public void charRange() {
		assertThat(Sequence.range('A', 'F'), contains('A', 'B', 'C', 'D', 'E', 'F'));
		assertThat(Sequence.range('F', 'A'), contains('F', 'E', 'D', 'C', 'B', 'A'));
	}

	@Test
	public void mapToChar() {
		CharSeq empty = Sequence.<Integer>empty().toChars(x -> (char) x.intValue());
		twice(() -> assertThat(empty, is(emptyIterable())));

		CharSeq charSeq = Sequence.ints(0x61).limit(5).toChars(x -> (char) x.intValue());
		twice(() -> assertThat(charSeq, contains('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void mapToInt() {
		IntSequence empty = Sequence.<Integer>empty().toInts(Integer::intValue);
		twice(() -> assertThat(empty, is(emptyIterable())));

		IntSequence intSequence = Sequence.ints().limit(5).toInts(Integer::intValue);
		twice(() -> assertThat(intSequence, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void mapToLong() {
		LongSequence empty = Sequence.<Long>empty().toLongs(Long::longValue);
		twice(() -> assertThat(empty, is(emptyIterable())));

		LongSequence longSequence = Sequence.ints().limit(5).toLongs(i -> (long) i);
		twice(() -> assertThat(longSequence, contains(1L, 2L, 3L, 4L, 5L)));
	}

	@Test
	public void mapToDouble() {
		DoubleSequence empty = Sequence.<Double>empty().toDoubles(Double::doubleValue);
		twice(() -> assertThat(empty, is(emptyIterable())));

		DoubleSequence doubleSequence = Sequence.ints().limit(5).toDoubles(i -> (double) i);
		twice(() -> assertThat(doubleSequence, contains(1.0, 2.0, 3.0, 4.0, 5.0)));
	}

	@Test
	public void repeat() {
		Sequence<Integer> repeatEmpty = empty.repeat();
		twice(() -> assertThat(repeatEmpty, is(emptyIterable())));

		Sequence<Integer> repeatOne = _1.repeat();
		twice(() -> assertThat(repeatOne.limit(3), contains(1, 1, 1)));

		Sequence<Integer> repeatTwo = _12.repeat();
		twice(() -> assertThat(repeatTwo.limit(5), contains(1, 2, 1, 2, 1)));

		Sequence<Integer> repeatThree = _123.repeat();
		twice(() -> assertThat(repeatThree.limit(8), contains(1, 2, 3, 1, 2, 3, 1, 2)));

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
		Sequence<Integer> sequence = Sequence.generate(queue::poll).untilNull();

		assertThat(sequence, contains(1, 2, 3, 4, 5));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void swap() {
		twice(() -> assertThat(_12345.swap((a, b) -> a == 2 && b == 3), contains(1, 3, 2, 4, 5)));
		twice(() -> assertThat(_12345.swap((a, b) -> a == 2), contains(1, 3, 4, 5, 2)));
	}
}
