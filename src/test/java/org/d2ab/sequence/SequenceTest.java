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
import org.d2ab.iterator.Iterators;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
			empty.forEach(i -> fail("Should not get called"));
			_1.forEach(i -> assertThat(i, is(in(Arrays.asList(1, 2, 3)))));
			_12.forEach(i -> assertThat(i, is(in(Arrays.asList(1, 2, 3)))));
			_123.forEach(i -> assertThat(i, is(in(Arrays.asList(1, 2, 3)))));
		});
	}

	@Test
	public void iterator() throws Exception {
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
	public void ofNone() throws Exception {
		Sequence<Integer> sequence = Sequence.<Integer>of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void empty() throws Exception {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void ofNulls() throws Exception {
		Sequence<Integer> sequence = Sequence.of(1, null, 2, 3, null);

		twice(() -> assertThat(sequence, contains(1, null, 2, 3, null)));
	}

	@Test
	public void fromSequence() throws Exception {
		Sequence<Integer> fromSequence = Sequence.from(_123);

		twice(() -> assertThat(fromSequence, contains(1, 2, 3)));
	}

	@Test
	public void fromIterable() throws Exception {
		Iterable<Integer> iterable = () -> Arrays.asList(1, 2, 3).iterator();

		Sequence<Integer> sequenceFromIterable = Sequence.from(iterable);

		twice(() -> assertThat(sequenceFromIterable, contains(1, 2, 3)));
	}

	@Test
	public void fromStream() throws Exception {
		Sequence<Integer> sequenceFromStream = Sequence.from(Arrays.asList(1, 2, 3).stream());

		assertThat(sequenceFromStream, contains(1, 2, 3));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromEmptyStream() throws Exception {
		Sequence<Integer> sequenceFromStream = Sequence.from(Collections.<Integer>emptyList().stream());

		assertThat(sequenceFromStream, is(emptyIterable()));
		expecting(IllegalStateException.class, sequenceFromStream::iterator);
	}

	@Test
	public void fromIteratorSupplier() throws Exception {
		Supplier<Iterator<Integer>> iterators = () -> Arrays.asList(1, 2, 3).iterator();

		Sequence<Integer> sequenceFromIterators = Sequence.from(iterators);

		twice(() -> assertThat(sequenceFromIterators, contains(1, 2, 3)));
	}

	@Test
	public void fromIterables() throws Exception {
		Iterable<Integer> first = Arrays.asList(1, 2, 3);
		Iterable<Integer> second = Arrays.asList(4, 5, 6);
		Iterable<Integer> third = Arrays.asList(7, 8, 9);

		Sequence<Integer> sequenceFromIterables = Sequence.from(first, second, third);

		twice(() -> assertThat(sequenceFromIterables, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void fromNoIterables() throws Exception {
		Sequence<Integer> sequenceFromNoIterables = Sequence.from(new Iterable[]{});

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
		Sequence<Integer> appended = _123.append(Iterators.of(4, 5, 6)).append(Iterators.of(7, 8));

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
		Iterator<Integer> first = Arrays.asList(1, 2, 3).iterator();
		Iterator<Integer> second = Arrays.asList(4, 5, 6).iterator();
		Iterator<Integer> third = Arrays.asList(7, 8).iterator();

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
		Iterator<Integer> first = Collections.singletonList(1).iterator();
		Iterator<Integer> second = Collections.singletonList(2).iterator();

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
		Sequence<Integer> filtered = Sequence.of(1, 2, 3, 4, 5, 6, 7).filter(i -> i % 2 == 0);

		twice(() -> assertThat(filtered, contains(2, 4, 6)));
	}

	@Test
	public void filterAndMap() {
		List<String> evens = Sequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
		                             .filter(x -> x % 2 == 0)
		                             .map(Objects::toString)
		                             .toList();

		twice(() -> assertThat(evens, contains("2", "4", "6", "8")));
	}

	@Test
	public void reuseOfSequence() {
		Sequence<Integer> singulars = Sequence.recurse(1, i -> i + 1).limit(10);

		// using sequence of ints 1..10 first time
		int x = 0, odds[] = {1, 3, 5, 7, 9};
		for (int odd : singulars.step(2))
			assertThat(odd, is(odds[x++]));

		// re-using the same sequence again
		int y = 0, squares[] = {16, 25, 36, 49, 64};
		for (int square : singulars.map(i -> i * i).skip(3).limit(5))
			assertThat(square, is(squares[y++]));
	}

	@Test
	public void flatMapIterables() {
		Sequence<List<Integer>> sequence = Sequence.of(Arrays.asList(1, 2), Arrays.asList(3, 4), Arrays.asList(5, 6));

		Sequence<Integer> flatMap = sequence.flatMap(Function.identity());

		twice(() -> assertThat(flatMap, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void flatMapLazy() {
		Sequence<Integer> flatMap = Sequence.of(Arrays.asList(1, 2), null).flatMap(Function.identity());

		twice(() -> {
			Iterator<Integer> iterator = flatMap.iterator(); // NPE if not lazy - expected later below
			assertThat(iterator.next(), is(1));
			assertThat(iterator.next(), is(2));
			expecting(NullPointerException.class, iterator::next);
		});
	}

	@Test
	public void flatMapIterators() {
		Sequence<Iterator<Integer>> sequence = Sequence.of(Arrays.asList(1, 2).iterator(),
		                                                   Arrays.asList(3, 4).iterator(),
		                                                   Arrays.asList(5, 6).iterator());
		Sequence<Integer> flatMap = sequence.flatMap(Sequence::from);
		assertThat(flatMap, contains(1, 2, 3, 4, 5, 6));
		assertThat(flatMap, is(emptyIterable()));
	}

	@Test
	public void flatMapArrays() {
		Sequence<Integer[]> sequence = Sequence.of(new Integer[]{1, 2}, new Integer[]{3, 4}, new Integer[]{5, 6});

		Sequence<Integer> flatMap = sequence.flatMap(Sequence::of);

		twice(() -> assertThat(flatMap, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void flattenIterables() {
		Sequence<Integer> flattened = Sequence.of(Arrays.asList(1, 2), Arrays.asList(3, 4), Arrays.asList(5, 6))
		                                      .flatten();

		twice(() -> assertThat(flattened, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void flattenLazy() {
		Sequence<Integer> flattened = Sequence.of(Arrays.asList(1, 2), (Iterable<Integer>) () -> {
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
		Sequence<Iterator<Integer>> sequence = Sequence.of(Arrays.asList(1, 2).iterator(),
		                                                   Arrays.asList(3, 4).iterator(),
		                                                   Arrays.asList(5, 6).iterator());
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
	public void recurseUntilNull() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i < 10 ? i + 1 : null).untilNull();
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void recurseUntil() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i + 1).until(7);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void recurseThrowableCause() {
		Exception e = new IllegalStateException(new IllegalArgumentException(new NullPointerException()));

		Sequence<Throwable> sequence = Sequence.recurse(e, Throwable::getCause).until(null);

		twice(() -> {
			Iterator<Throwable> iterator = sequence.iterator();
			assertThat(iterator.next(), is(instanceOf(IllegalStateException.class)));
			assertThat(iterator.next(), is(instanceOf(IllegalArgumentException.class)));
			assertThat(iterator.next(), is(instanceOf(NullPointerException.class)));
		});
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
	public void toMapFromPairs() {
		Map<String, Integer> original = Maps.put("1", 1).put("2", 2).put("3", 3).put("4", 4).build();

		Sequence<Pair<String, Integer>> sequence = Sequence.from(original)
		                                                   .filter(p -> p.test((s, i) -> i != 2))
		                                                   .map(p -> p.map((s, i) -> Pair.of(s + " x 2", i * 2)));

		twice(() -> {
			Map<String, Integer> map = sequence.pairsToMap(Function.identity());
			assertThat(map, instanceOf(HashMap.class));
			assertThat(map, is(equalTo(Maps.builder().put("1 x 2", 2).put("3 x 2", 6).put("4 x 2", 8).build())));
		});
	}

	@Test
	public void toMapWithTypeFromPairs() {
		Map<String, Integer> original = Maps.put("1", 1).put("2", 2).put("3", 3).put("4", 4).build();

		Sequence<Pair<String, Integer>> sequence = Sequence.from(original)
		                                                   .filter(p -> p.test((s, i) -> i != 2))
		                                                   .map(p -> p.map((s, i) -> Pair.of(s + " x 2", i * 2)));

		twice(() -> {
			Map<String, Integer> map = sequence.pairsToMap(LinkedHashMap::new, Function.identity());

			assertThat(map, instanceOf(HashMap.class));
			assertThat(map, is(equalTo(Maps.builder().put("1 x 2", 2).put("3 x 2", 6).put("4 x 2", 8).build())));
		});
	}

	@Test
	public void toMap() {
		twice(() -> {
			Map<String, Integer> map = _123.toMap(Object::toString, Function.identity());

			assertThat(map, instanceOf(HashMap.class));
			assertThat(map, is(equalTo(Maps.builder().put("1", 1).put("2", 2).put("3", 3).build())));
		});
	}

	@Test
	public void toMapWithType() {
		twice(() -> {
			Map<String, Integer> map = _123.toMap(LinkedHashMap::new, Object::toString, Function.identity());

			assertThat(map, instanceOf(LinkedHashMap.class));
			assertThat(map, is(equalTo(Maps.builder().put("1", 1).put("2", 2).put("3", 3).build())));
		});
	}

	@Test
	public void toSortedMap() {
		twice(() -> {
			SortedMap<String, Integer> sortedMap = threeRandom.toSortedMap(Object::toString, Function.identity());

			assertThat(sortedMap, instanceOf(TreeMap.class));
			assertThat(sortedMap, is(equalTo(Maps.builder().put("1", 1).put("2", 2).put("3", 3).build())));
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

	@Test
	public void fibonacci() {
		Sequence<Integer> fibonacci = Sequence.recurse(Pair.of(0, 1),
		                                               p -> Pair.of(p.getSecond(), p.apply(Integer::sum)))
		                                      .map(Pair::getFirst);
		twice(() -> assertThat(fibonacci.limit(10), contains(0, 1, 1, 2, 3, 5, 8, 13, 21, 34)));
	}

	@Test
	public void pairs() {
		twice(() -> {
			assertThat(empty.pair(), is(emptyIterable()));
			assertThat(_1.pair(), is(emptyIterable()));
			assertThat(_12.pair(), contains(Pair.of(1, 2)));
			assertThat(_123.pair(), contains(Pair.of(1, 2), Pair.of(2, 3)));
			assertThat(_12345.pair(), contains(Pair.of(1, 2), Pair.of(2, 3), Pair.of(3, 4), Pair.of(4, 5)));
		});
	}

	@Test
	public void partition() {
		twice(() -> assertThat(_12345.partition(3),
		                       contains(Arrays.asList(1, 2, 3), Arrays.asList(2, 3, 4), Arrays.asList(3, 4, 5))));
	}

	@Test
	public void step() {
		twice(() -> assertThat(_123456789.step(3), contains(1, 4, 7)));
	}

	@Test
	public void partitionAndStep() {
		Sequence<List<Integer>> partitionAndStep = _12345.partition(3).step(2);
		twice(() -> assertThat(partitionAndStep, contains(Arrays.asList(1, 2, 3), Arrays.asList(3, 4, 5))));

		Sequence<List<Integer>> partitioned = _12345.partition(3);
		twice(() -> assertThat(partitioned.step(2), contains(Arrays.asList(1, 2, 3), Arrays.asList(3, 4, 5))));
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
		twice(() -> assertThat(empty.count(), is(0)));
		twice(() -> assertThat(_1.count(), is(1)));
		twice(() -> assertThat(_12.count(), is(2)));
		twice(() -> assertThat(_123456789.count(), is(9)));
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
	public void streamToSequenceAndBack() {
		Stream<String> abcd = Arrays.asList("a", "b", "c", "d").stream();
		Stream<String> abbccd = Sequence.from(abcd).pair().<String>flatten().stream();
		assertThat(abbccd.collect(Collectors.toList()), contains("a", "b", "b", "c", "c", "d"));
	}

	@Test
	public void delimit() {
		Sequence<Object> delimited = _123.delimit(", ");
		twice(() -> assertThat(delimited, contains(1, ", ", 2, ", ", 3)));
	}

	@Test
	public void prefix() {
		Sequence<Object> delimitedEmpty = empty.prefix("[");
		twice(() -> assertThat(delimitedEmpty, contains("[")));

		Sequence<Object> delimited = _123.prefix("[");
		twice(() -> assertThat(delimited, contains("[", 1, 2, 3)));
	}

	@Test
	public void suffix() {
		Sequence<Object> delimitedEmpty = empty.suffix("]");
		twice(() -> assertThat(delimitedEmpty, contains("]")));

		Sequence<Object> delimited = _123.suffix("]");
		twice(() -> assertThat(delimited, contains(1, 2, 3, "]")));
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

	@Test
	public void interleave() {
		assertThat(empty.interleave(empty), is(emptyIterable()));
		assertThat(_123.interleave(_12345), contains(1, 1, 2, 2, 3, 3, 4, 5));
		assertThat(_12345.interleave(_123), contains(1, 1, 2, 2, 3, 3, 4, 5));
	}
}
