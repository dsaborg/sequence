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
import org.d2ab.collection.Lists;
import org.d2ab.collection.Maps;
import org.d2ab.collection.SizedIterable;
import org.d2ab.iterator.Iterators;
import org.d2ab.test.SequentialCollector;
import org.d2ab.test.Tests;
import org.d2ab.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Comparator.reverseOrder;
import static org.d2ab.collection.Lists.sort;
import static org.d2ab.collection.SizedIterable.SizeType.*;
import static org.d2ab.test.HasSizeCharacteristics.*;
import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.IsIterableBeginningWith.beginsWith;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class SequenceTest {
	private final BiFunction<Function<Object[], List<Object>>, Object[], Sequence<?>> generator;

	private final Sequence<Integer> empty;
	private final Sequence<Integer> _1;
	private final Sequence<Integer> _12;
	private final Sequence<Integer> _123;
	private final Sequence<Integer> _1234;
	private final Sequence<Integer> _12345;
	private final Sequence<Integer> _123456789;
	private final Sequence<Integer> oneRandom;
	private final Sequence<Integer> twoRandom;
	private final Sequence<Integer> threeRandom;
	private final Sequence<Integer> nineRandom;
	private final Sequence<?> mixed;
	private final Sequence<Integer> mutableFive;

	private final Sequence<Integer> sizePassThrough = new Sequence<Integer>() {
		@Override
		public Iterator<Integer> iterator() {
			throw new IllegalStateException();
		}

		@Override
		public int size() {
			return 10;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}
	};

	private final Sequence<Integer> emptySizePassThrough = new Sequence<Integer>() {
		@Override
		public Iterator<Integer> iterator() {
			throw new IllegalStateException();
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}
	};

	@SuppressWarnings("UnusedParameters")
	public SequenceTest(String description,
	                    BiFunction<Function<Object[], List<Object>>, Object[], Sequence<?>> generator) {
		this.generator = generator;

		empty = newSequence();
		_1 = newSequence(1);
		_12 = newSequence(1, 2);
		_123 = newSequence(1, 2, 3);
		_1234 = newSequence(1, 2, 3, 4);
		_12345 = newSequence(1, 2, 3, 4, 5);
		_123456789 = newSequence(1, 2, 3, 4, 5, 6, 7, 8, 9);
		oneRandom = newSequence(17);
		twoRandom = newSequence(17, 32);
		threeRandom = newSequence(2, 3, 1);
		nineRandom = newSequence(67, 5, 43, 3, 5, 7, 24, 5, 67);
		mixed = newSequence("1", 1, 'x', 1.0, "2", 2, 'y', 2.0, "3", 3, 'z', 3.0);
		mutableFive = newMutableSequence(1, 2, 3, 4, 5);
	}

	@SuppressWarnings("Convert2MethodRef")
	@Parameters(name = "{0}")
	public static Object[][] parameters() {
		return new Object[][]{
				{"Sequence",
				 (BiFunction<Function<Object[], List<Object>>, Object[], Sequence<?>>) (lm, xs) ->
						 newStandardSequence(lm, xs)},
				{"ListSequence",
				 (BiFunction<Function<Object[], List<Object>>, Object[], Sequence<?>>) (lm, xs) ->
						 newListSequence(lm, xs)},
				{"ChainedListSequence",
				 (BiFunction<Function<Object[], List<Object>>, Object[], Sequence<?>>) (lm, xs) ->
						 newChainedListSequence(lm, xs)},
				{"CollectionSequence",
				 (BiFunction<Function<Object[], List<Object>>, Object[], Sequence<?>>) (lm, xs) ->
						 newCollectionSequence(lm, xs)},
				{"SortedSequence",
				 (BiFunction<Function<Object[], List<Object>>, Object[], Sequence<?>>) (lm, xs) ->
						 newSortedSequence(lm, xs)},
				{"ReverseSequence",
				 (BiFunction<Function<Object[], List<Object>>, Object[], Sequence<?>>) (lm, xs) ->
						 newReverseSequence(lm, xs)},
				};
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	private final <T> Sequence<T> newSequence(T... ts) {
		return (Sequence<T>) generator.apply(Lists::of, ts);
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	private final <T> Sequence<T> newMutableSequence(T... ts) {
		return (Sequence<T>) generator.apply(xs -> new ArrayList<>(Lists.of(xs)), ts);
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> newChainedListSequence(Function<T[], List<T>> listMaker, T[] xs) {
		List<List<T>> lists = new ArrayList<>();
		List<T> current = new ArrayList<>();
		for (int i = 0; i < xs.length; i++) {
			current.add(xs[i]);
			if (i % 3 == 0) {
				lists.add(listMaker.apply((T[]) current.toArray()));
				current = new ArrayList<>();
			}
		}
		lists.add(listMaker.apply((T[]) current.toArray()));
		return ListSequence.concat(lists.toArray(new List[lists.size()]));
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> newListSequence(Function<T[], List<T>> listMaker, T[] xs) {
		return ListSequence.from(listMaker.apply(xs));
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> newCollectionSequence(Function<T[], List<T>> listMaker, T[] xs) {
		return CollectionSequence.from(listMaker.apply(xs));
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> newStandardSequence(Function<T[], List<T>> listMaker, T[] xs) {
		return Sequence.from(SizedIterable.from(listMaker.apply(xs)));
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> newSortedSequence(Function<T[], List<T>> listMaker, T[] xs) {
		List<T> list = listMaker.apply(xs);
		if (!(list instanceof ArrayList) && !list.contains(null)) {
			Sequence<T> sequence = Sequence.from(list);
			if (sequence.all(Comparable.class) && sequence.map(Object::getClass).distinct().size() <= 1) {
				List<T> copy = new ArrayList<>(list);
				if (list.equals(sort(copy, null))) {
					return Sequence.from(listMaker.apply((T[]) Lists.reverse(copy).toArray())).sorted();
				}
			}
		}

		return Sequence.from(SizedIterable.from(list));
	}

	@SuppressWarnings("unchecked")
	public static <T> Sequence<T> newReverseSequence(Function<T[], List<T>> listMaker, T[] xs) {
		List<T> list = listMaker.apply(xs);
		if (list instanceof ArrayList) {
			return Sequence.from(SizedIterable.from(list));
		}

		List<T> copy = Lists.reverse(new ArrayList<>(list));
		return Sequence.from(listMaker.apply((T[]) copy.toArray())).reverse();
	}

	@Test
	public void empty() {
		Sequence<Integer> empty = Sequence.empty();
		twice(() -> assertThat(empty, is(emptyFixedIterable())));
	}

	@Test
	public void emptyIsImmutable() {
		List<Integer> list = Sequence.<Integer>empty().asList();
		expecting(UnsupportedOperationException.class, () -> list.add(1));
		expecting(UnsupportedOperationException.class, () -> list.add(0, 0));
		expecting(UnsupportedOperationException.class, () -> list.addAll(Lists.of(1, 2)));
		expecting(UnsupportedOperationException.class, () -> list.addAll(0, Lists.of(-1, 0)));
		expecting(IndexOutOfBoundsException.class, () -> list.remove(0));
	}

	@Test
	public void ofNone() {
		Sequence<Integer> sequence = Sequence.of();
		twice(() -> assertThat(sequence, is(emptyFixedIterable())));
	}

	@Test
	public void ofNoneIsImmutable() {
		List<Integer> list = Sequence.<Integer>of().asList();
		expecting(UnsupportedOperationException.class, () -> list.add(1));
		expecting(UnsupportedOperationException.class, () -> list.add(0, 0));
		expecting(UnsupportedOperationException.class, () -> list.addAll(Lists.of(1, 2)));
		expecting(UnsupportedOperationException.class, () -> list.addAll(0, Lists.of(-1, 0)));
		expecting(UnsupportedOperationException.class, () -> list.remove(0));
	}

	@Test
	public void ofOne() {
		Sequence<Integer> sequence = Sequence.of(1);
		twice(() -> assertThat(sequence, containsFixed(1)));
	}

	@Test
	public void ofOneIsImmutable() {
		List<Integer> list = Sequence.of(1).asList();
		expecting(UnsupportedOperationException.class, () -> list.add(2));
		expecting(UnsupportedOperationException.class, () -> list.add(0, 0));
		expecting(UnsupportedOperationException.class, () -> list.addAll(Lists.of(2, 3)));
		expecting(UnsupportedOperationException.class, () -> list.addAll(0, Lists.of(-1, 0)));
		expecting(UnsupportedOperationException.class, () -> list.remove(0));
		expecting(UnsupportedOperationException.class, () -> list.set(0, 17));
	}

	@Test
	public void ofMany() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5);
		twice(() -> assertThat(sequence, containsFixed(1, 2, 3, 4, 5)));
	}

	@Test
	public void ofManyIsImmutable() {
		List<Integer> list = Sequence.of(1, 2, 3, 4, 5).asList();
		expecting(UnsupportedOperationException.class, () -> list.add(6));
		expecting(UnsupportedOperationException.class, () -> list.add(0, 0));
		expecting(UnsupportedOperationException.class, () -> list.addAll(Lists.of(6, 7)));
		expecting(UnsupportedOperationException.class, () -> list.addAll(0, Lists.of(-1, 0)));
		expecting(UnsupportedOperationException.class, () -> list.remove(0));
		expecting(UnsupportedOperationException.class, () -> list.set(0, 17));
	}

	@Test
	public void ofNulls() {
		Sequence<Integer> sequence = Sequence.of(1, null, 2, 3, null);
		twice(() -> assertThat(sequence, containsFixed(1, null, 2, 3, null)));
	}

	@Test
	public void fromEmpty() {
		twice(() -> assertThat(empty, is(emptyFixedIterable())));
	}

	@Test
	public void fromIterable() {
		Sequence<Integer> sequence = Sequence.from(Iterables.of(1, 2, 3)::iterator);
		twice(() -> assertThat(sequence, containsUnsized(1, 2, 3)));
	}

	@Test
	public void fromSizedIterable() {
		Sequence<Integer> sequence = Sequence.from(Iterables.of(1, 2, 3));
		twice(() -> assertThat(sequence, containsFixed(1, 2, 3)));
	}

	@Test
	public void fromSizedIterableAsIterable() {
		Sequence<Integer> sequence = Sequence.from((Iterable<Integer>) Iterables.of(1, 2, 3));
		twice(() -> assertThat(sequence, containsFixed(1, 2, 3)));
	}

	@Test
	public void onceIterator() {
		Sequence<Integer> sequence = Sequence.once(Iterators.of(1, 2, 3));
		assertThat(sequence, contains(1, 2, 3));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceIteratorSize() {
		Sequence<Integer> sequence = Sequence.once(Iterators.of(1, 2, 3));
		assertThat(sequence.size(), is(3));
		assertThat(sequence.size(), is(0));
	}

	@Test
	public void onceIteratorSizeType() {
		Sequence<Integer> sequence = Sequence.once(Iterators.of(1, 2, 3));
		twice(() -> assertThat(sequence.sizeType(), is(UNAVAILABLE)));
	}

	@Test
	public void onceIteratorIsEmpty() {
		Sequence<Integer> sequence = Sequence.once(Iterators.of(1, 2, 3));
		twice(() -> assertThat(sequence.isEmpty(), is(false)));
	}

	@Test
	public void onceStream() {
		Sequence<Integer> sequence = Sequence.once(Stream.of(1, 2, 3));
		assertThat(sequence, contains(1, 2, 3));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceStreamSize() {
		Sequence<Integer> sequence = Sequence.once(Stream.of(1, 2, 3));
		assertThat(sequence.size(), is(3));
		assertThat(sequence.size(), is(0));
	}

	@Test
	public void onceStreamSizeType() {
		Sequence<Integer> sequence = Sequence.once(Stream.of(1, 2, 3));
		twice(() -> assertThat(sequence.sizeType(), is(UNAVAILABLE)));
	}

	@Test
	public void onceStreamIsEmpty() {
		Sequence<Integer> sequence = Sequence.once(Stream.of(1, 2, 3));
		twice(() -> assertThat(sequence.isEmpty(), is(false)));
	}

	@Test
	public void onceEmptyStream() {
		Sequence<Integer> sequence = Sequence.once(Stream.of());
		twice(() -> assertThat(sequence, is(emptyUnsizedIterable())));
	}

	@Test
	public void concatArrayOfIterables() {
		List<Integer> list1 = new ArrayList<>(Lists.of(1, 2, 3));
		List<Integer> list2 = new ArrayList<>(Lists.of(4, 5, 6));
		List<Integer> list3 = new ArrayList<>(Lists.of(7, 8, 9));

		Sequence<Integer> sequence = Sequence.concat(list1::iterator, list2::iterator, list3::iterator);
		twice(() -> assertThat(sequence, containsUnsized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		list1.add(17);
		twice(() -> assertThat(sequence, containsUnsized(1, 2, 3, 17, 4, 5, 6, 7, 8, 9)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, containsUnsized(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void concatArrayOfCollections() {
		ArrayDeque<Integer> collection1 = new ArrayDeque<>(Lists.of(1, 2, 3));
		ArrayDeque<Integer> collection2 = new ArrayDeque<>(Lists.of(4, 5, 6));
		ArrayDeque<Integer> collection3 = new ArrayDeque<>(Lists.of(7, 8, 9));

		Sequence<Integer> sequence = Sequence.concat(collection1, collection2, collection3);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		collection1.add(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 17, 4, 5, 6, 7, 8, 9)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void concatArrayOfLists() {
		List<Integer> list1 = new ArrayList<>(Lists.of(1, 2, 3));
		List<Integer> list2 = new ArrayList<>(Lists.of(4, 5, 6));
		List<Integer> list3 = new ArrayList<>(Lists.of(7, 8, 9));

		Sequence<Integer> sequence = Sequence.concat(list1, list2, list3);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		list1.add(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 17, 4, 5, 6, 7, 8, 9)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void concatArrayOfFixedLists() {
		Sequence<Integer> sequence = Sequence.concat(Lists.of(1, 2, 3), Lists.of(4, 5, 6), Lists.of(7, 8, 9));
		twice(() -> assertThat(sequence, containsFixed(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		expecting(UnsupportedOperationException.class, () -> sequence.add(17));
		twice(() -> assertThat(sequence, containsFixed(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		expecting(UnsupportedOperationException.class, () -> sequence.remove(1));
		twice(() -> assertThat(sequence, containsFixed(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void concatArrayOfNoIterables() {
		Sequence<Integer> sequence = Sequence.concat();
		twice(() -> assertThat(sequence, is(emptyFixedIterable())));
	}

	@Test
	public void concatIterableOfIterables() {
		List<Integer> list1 = new ArrayList<>(Lists.of(1, 2, 3));
		List<Integer> list2 = new ArrayList<>(Lists.of(4, 5, 6));
		List<Integer> list3 = new ArrayList<>(Lists.of(7, 8, 9));
		List<Iterable<Integer>> listList = new ArrayList<>(Lists.of(list1::iterator, list2::iterator,
		                                                            list3::iterator));

		Sequence<Integer> sequence = Sequence.concat((Iterable<Iterable<Integer>>) listList::iterator);
		twice(() -> assertThat(sequence, containsUnsized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		list1.add(17);
		twice(() -> assertThat(sequence, containsUnsized(1, 2, 3, 17, 4, 5, 6, 7, 8, 9)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, containsUnsized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		listList.add(new ArrayList<>(Lists.of(10, 11, 12)));
		twice(() -> assertThat(sequence, containsUnsized(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}

	@Test
	public void concatIterableOfCollections() {
		ArrayDeque<Integer> collection1 = new ArrayDeque<>(Lists.of(1, 2, 3));
		ArrayDeque<Integer> collection2 = new ArrayDeque<>(Lists.of(4, 5, 6));
		ArrayDeque<Integer> collection3 = new ArrayDeque<>(Lists.of(7, 8, 9));
		Collection<Iterable<Integer>> collectionCollection = new ArrayDeque<>(
				Lists.of(collection1, collection2, collection3));

		Sequence<Integer> sequence = Sequence.concat((Iterable<Iterable<Integer>>) collectionCollection::iterator);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		collection1.add(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 17, 4, 5, 6, 7, 8, 9)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		collectionCollection.add(new ArrayDeque<>(Lists.of(10, 11, 12)));
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}

	@Test
	public void concatIterableOfLists() {
		List<Integer> list1 = new ArrayList<>(Lists.of(1, 2, 3));
		List<Integer> list2 = new ArrayList<>(Lists.of(4, 5, 6));
		List<Integer> list3 = new ArrayList<>(Lists.of(7, 8, 9));
		List<Iterable<Integer>> listList = new ArrayList<>(Lists.of(list1, list2, list3));

		Sequence<Integer> sequence = Sequence.concat((Iterable<Iterable<Integer>>) listList::iterator);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		list1.add(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 17, 4, 5, 6, 7, 8, 9)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		listList.add(new ArrayList<>(Lists.of(10, 11, 12)));
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}

	@Test
	public void concatIterableOfNoIterables() {
		Sequence<Integer> sequence = Sequence.concat(Iterables.empty());
		twice(() -> assertThat(sequence, is(emptyFixedIterable())));
	}

	@Test
	public void concatListOfLists() {
		List<Integer> list1 = new ArrayList<>(Lists.of(1, 2, 3));
		List<Integer> list2 = new ArrayList<>(Lists.of(4, 5, 6));
		List<Integer> list3 = new ArrayList<>(Lists.of(7, 8, 9));
		List<Iterable<Integer>> listList = new ArrayList<>(Lists.of(list1, list2, list3));

		Sequence<Integer> sequence = Sequence.concat(listList);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		sequence.add(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9, 17)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		listList.add(new ArrayList<>(Lists.of(10, 11, 12)));
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}

	@Test
	public void concatCollectionOfLists() {
		List<Integer> list1 = new ArrayList<>(Lists.of(1, 2, 3));
		List<Integer> list2 = new ArrayList<>(Lists.of(4, 5, 6));
		List<Integer> list3 = new ArrayList<>(Lists.of(7, 8, 9));
		Collection<Iterable<Integer>> listCollection = new ArrayDeque<>(Lists.of(list1, list2, list3));

		Sequence<Integer> sequence = Sequence.concat(listCollection);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		sequence.add(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9, 17)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		listCollection.add(new ArrayList<>(Lists.of(10, 11, 12)));
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}

	@Test
	public void concatCollectionOfCollections() {
		Collection<Integer> collection1 = new ArrayDeque<>(Lists.of(1, 2, 3));
		Collection<Integer> collection2 = new ArrayDeque<>(Lists.of(4, 5, 6));
		Collection<Integer> collection3 = new ArrayDeque<>(Lists.of(7, 8, 9));
		Collection<Iterable<Integer>> collectionCollection = new ArrayDeque<>(
				Lists.of(collection1, collection2, collection3));

		Sequence<Integer> sequence = Sequence.concat(collectionCollection);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		sequence.add(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9, 17)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		collectionCollection.add(new ArrayDeque<>(Lists.of(10, 11, 12)));
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}

	@Test
	public void cacheCollection() {
		List<Integer> list = new ArrayList<>(Lists.of(1, 2, 3, 4, 5));
		Sequence<Integer> cached = Sequence.cache(list);
		list.set(0, 17);

		twice(() -> assertThat(cached, containsSized(1, 2, 3, 4, 5)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptySizedIterable())));
	}

	@Test
	public void cacheIterable() {
		List<Integer> list = new ArrayList<>(Lists.of(1, 2, 3, 4, 5));
		Sequence<Integer> cached = Sequence.cache(list::iterator);
		list.set(0, 17);

		twice(() -> assertThat(cached, containsSized(1, 2, 3, 4, 5)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptySizedIterable())));
	}

	@Test
	public void cacheIterator() {
		List<Integer> list = new ArrayList<>(Lists.of(1, 2, 3, 4, 5));
		Sequence<Integer> cached = Sequence.cache(list.iterator());
		list.set(0, 17);

		twice(() -> assertThat(cached, containsSized(1, 2, 3, 4, 5)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptySizedIterable())));
	}

	@Test
	public void cacheStream() {
		List<Integer> list = new ArrayList<>(Lists.of(1, 2, 3, 4, 5));
		Sequence<Integer> cached = Sequence.cache(list.stream());
		list.set(0, 17);

		twice(() -> assertThat(cached, containsSized(1, 2, 3, 4, 5)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptySizedIterable())));
	}

	@Test
	public void create() {
		Sequence<Integer> sequence = Sequence.create();
		twice(() -> assertThat(sequence, is(emptySizedIterable())));

		sequence.add(17);
		twice(() -> assertThat(sequence, containsSized(17)));
	}

	@Test
	public void withCapacity() {
		Sequence<Integer> sequence = Sequence.withCapacity(1);
		twice(() -> assertThat(sequence, is(emptySizedIterable())));

		sequence.addAll(Lists.of(1, 2, 3, 4, 5));
		assertThat(sequence, containsSized(1, 2, 3, 4, 5));
	}

	@Test
	public void createOfNone() {
		Sequence<Integer> sequence = Sequence.createOf();
		twice(() -> assertThat(sequence, is(emptySizedIterable())));

		sequence.add(17);
		twice(() -> assertThat(sequence, containsSized(17)));
	}

	@Test
	public void createOfOne() {
		Sequence<Integer> sequence = Sequence.createOf(1);
		twice(() -> assertThat(sequence, containsSized(1)));

		sequence.add(17);
		twice(() -> assertThat(sequence, containsSized(1, 17)));
	}

	@Test
	public void createOfMany() {
		Sequence<Integer> sequence = Sequence.createOf(1, 2, 3, 4, 5);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5)));

		sequence.add(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 17)));
	}

	@Test
	public void createOfNulls() {
		Sequence<Integer> sequence = Sequence.createOf(1, null, 2, 3, null);
		twice(() -> assertThat(sequence, containsSized(1, null, 2, 3, null)));

		sequence.add(17);
		twice(() -> assertThat(sequence, containsSized(1, null, 2, 3, null, 17)));
	}

	@Test
	public void createFromCollectionAsIterable() {
		Collection<Integer> backing = new ArrayDeque<>(Lists.of(1, 2, 3, 4, 5));
		Sequence<Integer> sequence = Sequence.createFrom((Iterable<Integer>) backing);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5)));

		sequence.add(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 17)));
		assertThat(backing, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void createFromIterable() {
		Collection<Integer> backing = new ArrayDeque<>(Lists.of(1, 2, 3, 4, 5));
		Sequence<Integer> sequence = Sequence.createFrom(backing::iterator);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5)));

		sequence.add(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 17)));
		assertThat(backing, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void createFromCollection() {
		Collection<Integer> backing = new ArrayDeque<>(Lists.of(1, 2, 3, 4, 5));
		Sequence<Integer> sequence = Sequence.createFrom(backing);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5)));

		sequence.add(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 17)));
		assertThat(backing, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void createFromIterator() {
		Sequence<Integer> sequence = Sequence.createFrom(Iterators.of(1, 2, 3, 4, 5));
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5)));

		sequence.add(17);
		twice(() -> assertThat(sequence, containsSized(1, 2, 3, 4, 5, 17)));
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
		empty.forEach(i -> fail("Should not get called"));

		AtomicInteger value = new AtomicInteger(1);
		_1.forEach(i -> assertThat(i, is(value.getAndIncrement())));

		value.set(1);
		_12.forEach(i -> assertThat(i, is(value.getAndIncrement())));

		value.set(1);
		_12345.forEach(i -> assertThat(i, is(value.getAndIncrement())));
	}

	@Test
	public void forEachIndexed() {
		empty.forEachIndexed((e, i) -> fail("Should not get called"));

		AtomicInteger index = new AtomicInteger();
		_1.forEachIndexed((e, i) -> {
			assertThat(e, is(index.get() + 1));
			assertThat(i, is(index.getAndIncrement()));
		});
		assertThat(index.get(), is(1));

		index.set(0);
		_12.forEachIndexed((e, i) -> {
			assertThat(e, is(index.get() + 1));
			assertThat(i, is(index.getAndIncrement()));
		});
		assertThat(index.get(), is(2));

		index.set(0);
		_12345.forEachIndexed((e, i) -> {
			assertThat(e, is(index.get() + 1));
			assertThat(i, is(index.getAndIncrement()));
		});
		assertThat(index.get(), is(5));
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
			expecting(NoSuchElementException.class, iterator::next);
		});
	}

	@Test
	public void skip() {
		Sequence<Integer> threeSkipNone = _123.skip(0);
		twice(() -> assertThat(threeSkipNone, is(sameInstance(_123))));

		Sequence<Integer> threeSkipOne = _123.skip(1);
		twice(() -> assertThat(threeSkipOne, containsFixed(2, 3)));

		Sequence<Integer> threeSkipTwo = _123.skip(2);
		twice(() -> assertThat(threeSkipTwo, containsFixed(3)));

		Sequence<Integer> threeSkipThree = _123.skip(3);
		twice(() -> assertThat(threeSkipThree, is(emptyFixedIterable())));

		Sequence<Integer> threeSkipFour = _123.skip(4);
		twice(() -> assertThat(threeSkipFour, is(emptyFixedIterable())));

		expecting(NoSuchElementException.class, () -> threeSkipThree.iterator().next());
		expecting(NoSuchElementException.class, () -> threeSkipFour.iterator().next());

		Sequence<Integer> mutableSkipOne = mutableFive.skip(1);
		twice(() -> assertThat(mutableSkipOne, containsSized(2, 3, 4, 5)));

		assertThat(Tests.removeFirst(mutableSkipOne), is(2));
		twice(() -> assertThat(mutableSkipOne, containsSized(3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(1, 3, 4, 5)));

		Sequence<Integer> infiniteSkipFive = Sequence.recurse(1, x -> x + 1).skip(5);
		twice(() -> assertThat(infiniteSkipFive, infiniteBeginningWith(6, 7, 8, 9, 10)));
	}

	@Test
	public void skipTail() {
		Sequence<Integer> threeSkipTailNone = _123.skipTail(0);
		twice(() -> assertThat(threeSkipTailNone, is(sameInstance(_123))));

		Sequence<Integer> threeSkipTailOne = _123.skipTail(1);
		twice(() -> assertThat(threeSkipTailOne, containsFixed(1, 2)));

		Sequence<Integer> threeSkipTailTwo = _123.skipTail(2);
		twice(() -> assertThat(threeSkipTailTwo, containsFixed(1)));

		Sequence<Integer> threeSkipTailThree = _123.skipTail(3);
		twice(() -> assertThat(threeSkipTailThree, is(emptyFixedIterable())));

		Sequence<Integer> threeSkipTailFour = _123.skipTail(4);
		twice(() -> assertThat(threeSkipTailFour, is(emptyFixedIterable())));

		expecting(NoSuchElementException.class, () -> threeSkipTailThree.iterator().next());
		expecting(NoSuchElementException.class, () -> threeSkipTailFour.iterator().next());

		Sequence<Integer> mutableSkipTailOne = mutableFive.skipTail(1);
		twice(() -> assertThat(mutableSkipTailOne, containsSized(1, 2, 3, 4)));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutableSkipTailOne));
		twice(() -> assertThat(mutableSkipTailOne, containsSized(1, 2, 3, 4)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));

		Sequence<Integer> nineSkipTailNone = _123456789.skipTail(0);
		twice(() -> assertThat(nineSkipTailNone, is(sameInstance(_123456789))));

		Sequence<Integer> nineSkipTailOne = _123456789.skipTail(1);
		twice(() -> assertThat(nineSkipTailOne, containsFixed(1, 2, 3, 4, 5, 6, 7, 8)));

		Sequence<Integer> nineSkipTailTwo = _123456789.skipTail(2);
		twice(() -> assertThat(nineSkipTailTwo, containsFixed(1, 2, 3, 4, 5, 6, 7)));

		Sequence<Integer> nineSkipTailThree = _123456789.skipTail(3);
		twice(() -> assertThat(nineSkipTailThree, containsFixed(1, 2, 3, 4, 5, 6)));

		Sequence<Integer> nineSkipTailFour = _123456789.skipTail(4);
		twice(() -> assertThat(nineSkipTailFour, containsFixed(1, 2, 3, 4, 5)));

		Sequence<Integer> infiniteSkipTailFive = Sequence.recurse(1, x -> x + 1).skipTail(5);
		twice(() -> assertThat(infiniteSkipTailFive, infiniteBeginningWith(1, 2, 3, 4, 5)));
	}

	@Test
	public void limit() {
		Sequence<Integer> threeLimitedToNone = _123.limit(0);
		twice(() -> assertThat(threeLimitedToNone, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> threeLimitedToNone.iterator().next());

		Sequence<Integer> threeLimitedToOne = _123.limit(1);
		twice(() -> assertThat(threeLimitedToOne, containsFixed(1)));
		Iterator<Integer> iterator = threeLimitedToOne.iterator();
		iterator.next();
		expecting(NoSuchElementException.class, iterator::next);

		Sequence<Integer> threeLimitedToTwo = _123.limit(2);
		twice(() -> assertThat(threeLimitedToTwo, containsFixed(1, 2)));

		Sequence<Integer> threeLimitedToThree = _123.limit(3);
		twice(() -> assertThat(threeLimitedToThree, containsFixed(1, 2, 3)));

		Sequence<Integer> threeLimitedToFour = _123.limit(4);
		twice(() -> assertThat(threeLimitedToFour, containsFixed(1, 2, 3)));

		Sequence<Integer> mutableLimitedToThree = mutableFive.limit(3);
		assertThat(Tests.removeFirst(mutableLimitedToThree), is(1));
		twice(() -> assertThat(mutableLimitedToThree, containsSized(2, 3, 4)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<Integer> infiniteLimitedToFive = Sequence.recurse(1, x -> x + 1).limit(5);
		twice(() -> assertThat(infiniteLimitedToFive, containsSized(1, 2, 3, 4, 5)));
	}

	@Test
	public void limitTail() {
		Sequence<Integer> threeLimitTailToNone = _123.limitTail(0);
		twice(() -> assertThat(threeLimitTailToNone, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> threeLimitTailToNone.iterator().next());

		Sequence<Integer> threeLimitTailToOne = _123.limitTail(1);
		twice(() -> assertThat(threeLimitTailToOne, containsFixed(3)));
		Iterator<Integer> iterator = threeLimitTailToOne.iterator();
		iterator.next();
		expecting(NoSuchElementException.class, iterator::next);

		Sequence<Integer> threeLimitTailToTwo = _123.limitTail(2);
		twice(() -> assertThat(threeLimitTailToTwo, containsFixed(2, 3)));

		Sequence<Integer> threeLimitTailToThree = _123.limitTail(3);
		twice(() -> assertThat(threeLimitTailToThree, containsFixed(1, 2, 3)));

		Sequence<Integer> threeLimitTailToFour = _123.limitTail(4);
		twice(() -> assertThat(threeLimitTailToFour, containsFixed(1, 2, 3)));

		Sequence<Integer> nineLimitTailToNone = _123456789.limitTail(0);
		twice(() -> assertThat(nineLimitTailToNone, is(emptyFixedIterable())));

		Sequence<Integer> nineLimitTailToOne = _123456789.limitTail(1);
		twice(() -> assertThat(nineLimitTailToOne, containsFixed(9)));

		Sequence<Integer> nineLimitTailToTwo = _123456789.limitTail(2);
		twice(() -> assertThat(nineLimitTailToTwo, containsFixed(8, 9)));

		Sequence<Integer> nineLimitTailToThree = _123456789.limitTail(3);
		twice(() -> assertThat(nineLimitTailToThree, containsFixed(7, 8, 9)));

		Sequence<Integer> nineLimitTailToFour = _123456789.limitTail(4);
		twice(() -> assertThat(nineLimitTailToFour, containsFixed(6, 7, 8, 9)));

		Sequence<Integer> mutableLimitTailToFour = mutableFive.limitTail(4);
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutableLimitTailToFour));
		twice(() -> assertThat(mutableLimitTailToFour, containsSized(2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));

		expecting(IllegalStateException.class, () -> Sequence.recurse(1, x -> x + 1).limitTail(5));
	}

	@Test
	public void appendEmptyIterable() {
		Sequence<Integer> appendedEmpty = empty.append(Iterables.empty());
		twice(() -> assertThat(appendedEmpty, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().next());
	}

	@Test
	public void appendIterable() {
		Sequence<Integer> appended = _123.append(Iterables.of(4, 5, 6))
		                                 .append(Iterables.of(7, 8));
		twice(() -> assertThat(appended, containsFixed(1, 2, 3, 4, 5, 6, 7, 8)));

		Sequence<Integer> mutableAppended = mutableFive.append(Iterables.of(6, 7)).append(Iterables.of(8, 9));
		assertThat(Tests.removeFirst(mutableAppended), is(1));
		twice(() -> assertThat(mutableAppended, containsSized(2, 3, 4, 5, 6, 7, 8, 9)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));
	}

	@Test
	public void appendCollection() {
		Sequence<Integer> appended = _123.append(new ArrayList<>(Lists.of(4, 5, 6)))
		                                 .append(new ArrayList<>(Lists.of(7, 8)));
		twice(() -> assertThat(appended, containsSized(1, 2, 3, 4, 5, 6, 7, 8)));

		Iterator<Integer> iterator = appended.iterator();
		Iterators.skip(iterator, 3);
		iterator.next();
		iterator.remove();
		twice(() -> assertThat(appended, containsSized(1, 2, 3, 5, 6, 7, 8)));

		Sequence<Integer> mutableAppended = mutableFive.append(new ArrayList<>(Lists.of(6, 7)))
		                                               .append(new ArrayList<>(Lists.of(8, 9)));
		assertThat(Tests.removeFirst(mutableAppended), is(1));
		twice(() -> assertThat(mutableAppended, containsSized(2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void appendEmptyIterator() {
		Sequence<Integer> appendedEmpty = empty.append(Iterators.empty());
		twice(() -> assertThat(appendedEmpty, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().next());
	}

	@Test
	public void appendIterator() {
		Sequence<Integer> appended = _123.append(Iterators.of(4, 5, 6)).append(Iterators.of(7, 8));
		assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, contains(1, 2, 3));
		assertThat(_123, containsFixed(1, 2, 3));
	}

	@Test
	public void appendIteratorSize() {
		Sequence<Integer> appended = _123.append(Iterators.of(4, 5, 6)).append(Iterators.of(7, 8));
		assertThat(appended.size(), is(8));
		assertThat(appended.size(), is(3));
		assertThat(_123, containsFixed(1, 2, 3));
	}

	@Test
	public void appendIteratorIsEmpty() {
		Sequence<Integer> appended = _123.append(Iterators.of(4, 5, 6)).append(Iterators.of(7, 8));
		twice(() -> assertThat(appended.isEmpty(), is(false)));
		assertThat(_123, containsFixed(1, 2, 3));
	}

	@Test
	public void appendCollectionIterator() {
		List<Integer> _456 = new ArrayList<>(Lists.of(4, 5, 6));
		List<Integer> _78 = new ArrayList<>(Lists.of(7, 8));

		Sequence<Integer> appended = _123.append(_456.iterator()).append(_78.iterator());
		assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, contains(1, 2, 3));

		Sequence<Integer> appended2 = _123.append(_456.iterator()).append(_78.iterator());
		assertThat(appended2.size(), is(8));
		assertThat(appended2.size(), is(3));

		Sequence<Integer> appended3 = _123.append(_456.iterator()).append(_78.iterator());
		twice(() -> assertThat(appended3.isEmpty(), is(false)));

		Sequence<Integer> appended4 = _123.append(_456.iterator()).append(_78.iterator());
		Iterator<Integer> iterator = appended4.iterator();
		Iterators.skip(iterator, 4);
		iterator.remove();
		Iterators.skip(iterator, 4);
		iterator.remove();
		assertThat(appended4, contains(1, 2, 3));
		assertThat(_456, contains(5, 6));
		assertThat(_78, contains(7));
	}

	@Test
	public void appendEmptyStream() {
		Sequence<Integer> appendedEmpty = empty.append(Stream.of());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().next());
	}

	@Test
	public void appendStream() {
		Sequence<Integer> appended = _123.append(Stream.of(4, 5, 6)).append(Stream.of(7, 8));
		assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, contains(1, 2, 3));
		assertThat(_123, containsFixed(1, 2, 3));
	}

	@Test
	public void appendStreamSize() {
		Sequence<Integer> appended = _123.append(Stream.of(4, 5, 6)).append(Stream.of(7, 8));
		assertThat(appended.size(), is(8));
		assertThat(appended.size(), is(3));
		assertThat(_123, containsFixed(1, 2, 3));
	}

	@Test
	public void appendStreamIsEmpty() {
		Sequence<Integer> appended = _123.append(Stream.of(4, 5, 6)).append(Stream.of(7, 8));
		twice(() -> assertThat(appended.isEmpty(), is(false)));
		assertThat(_123, containsFixed(1, 2, 3));
	}

	@Test
	public void appendEmptyArray() {
		Sequence<Integer> appendedEmpty = empty.append();
		twice(() -> assertThat(appendedEmpty, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().next());
	}

	@Test
	public void appendArray() {
		Sequence<Integer> appended = _123.append(4, 5, 6).append(7, 8);
		twice(() -> assertThat(appended, containsFixed(1, 2, 3, 4, 5, 6, 7, 8)));
		assertThat(_123, containsFixed(1, 2, 3));
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
		Sequence<Integer> emptyFiltered = empty.filter(x -> (x % 2) == 0);
		twice(() -> assertThat(emptyFiltered, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().next());

		Sequence<Integer> oneFiltered = _1.filter(x -> (x % 2) == 0);
		twice(() -> assertThat(oneFiltered, is(emptyUnsizedIterable())));

		Sequence<Integer> twoFiltered = _12.filter(x -> (x % 2) == 0);
		twice(() -> assertThat(twoFiltered, containsUnsized(2)));

		Sequence<Integer> mutableFiltered = mutableFive.filter(x -> (x % 2) == 0);
		assertThat(Tests.removeFirst(mutableFiltered), is(2));
		twice(() -> assertThat(mutableFiltered, containsUnsized(4)));
		twice(() -> assertThat(mutableFive, containsSized(1, 3, 4, 5)));

		Sequence<Integer> nineFiltered = _123456789.filter(x -> (x % 2) == 0);
		twice(() -> assertThat(nineFiltered, containsUnsized(2, 4, 6, 8)));
	}

	@Test
	public void filterIndexed() {
		Sequence<Integer> emptyFiltered = empty.filterIndexed((x, i) -> i > 0);
		twice(() -> assertThat(emptyFiltered, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().next());

		Sequence<Integer> oneFiltered = _1.filterIndexed((x, i) -> i > 0);
		twice(() -> assertThat(oneFiltered, is(emptyUnsizedIterable())));

		Sequence<Integer> twoFiltered = _12.filterIndexed((x, i) -> i > 0);
		twice(() -> assertThat(twoFiltered, containsUnsized(2)));

		Sequence<Integer> mutableFiltered = mutableFive.filterIndexed((x, i) -> i > 0);
		assertThat(Tests.removeFirst(mutableFiltered), is(2));
		twice(() -> assertThat(mutableFiltered, containsUnsized(3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(1, 3, 4, 5)));

		Sequence<Integer> nineFiltered = _123456789.filterIndexed((x, i) -> i > 3);
		twice(() -> assertThat(nineFiltered, containsUnsized(5, 6, 7, 8, 9)));
	}

	@Test
	public void filterInstanceOf() {
		Sequence<String> strings = mixed.filter(String.class);
		twice(() -> assertThat(strings, containsUnsized("1", "2", "3")));

		Sequence<Number> numbers = mixed.filter(Number.class);
		twice(() -> assertThat(numbers, containsUnsized(1, 1.0, 2, 2.0, 3, 3.0)));

		Sequence<Integer> integers = mixed.filter(Integer.class);
		twice(() -> assertThat(integers, containsUnsized(1, 2, 3)));

		Sequence<Double> doubles = mixed.filter(Double.class);
		twice(() -> assertThat(doubles, containsUnsized(1.0, 2.0, 3.0)));

		Sequence<Character> chars = mixed.filter(Character.class);
		twice(() -> assertThat(chars, containsUnsized('x', 'y', 'z')));

		Sequence<Integer> mutable = mutableFive.filter(Integer.class);
		assertThat(Tests.removeFirst(mutable), is(1));
		twice(() -> assertThat(mutable, containsUnsized(2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));
	}

	@Test
	public void filterBack() {
		Sequence<Integer> emptyFilteredLess = empty.filterBack((p, x) -> p == null || p < x);
		twice(() -> assertThat(emptyFilteredLess, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().next());

		Sequence<Integer> filteredLess = nineRandom.filterBack((p, x) -> p == null || p < x);
		twice(() -> assertThat(filteredLess, containsUnsized(67, 43, 5, 7, 24, 67)));

		Sequence<Integer> filteredGreater = nineRandom.filterBack((p, x) -> p == null || p > x);
		twice(() -> assertThat(filteredGreater, containsUnsized(67, 5, 3, 5)));

		Sequence<Integer> filteredMutable = mutableFive.filterBack((p, x) -> p == null || p < x);
		assertThat(Tests.removeFirst(filteredMutable), is(1));
		twice(() -> assertThat(filteredMutable, containsUnsized(2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));
	}

	@Test
	public void filterBackWithReplacement() {
		Sequence<Integer> emptyFilteredLess = empty.filterBack(117, (p, x) -> p < x);
		twice(() -> assertThat(emptyFilteredLess, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().next());

		Sequence<Integer> filteredLess = nineRandom.filterBack(117, (p, x) -> p < x);
		twice(() -> assertThat(filteredLess, containsUnsized(43, 5, 7, 24, 67)));

		Sequence<Integer> filteredGreater = nineRandom.filterBack(117, (p, x) -> p > x);
		twice(() -> assertThat(filteredGreater, containsUnsized(67, 5, 3, 5)));

		Sequence<Integer> filteredMutable = mutableFive.filterBack(0, (p, x) -> p < x);
		assertThat(Tests.removeFirst(filteredMutable), is(1));
		twice(() -> assertThat(filteredMutable, containsUnsized(2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));
	}

	@Test
	public void filterForward() {
		Sequence<Integer> emptyFilteredLess = empty.filterForward((x, n) -> n == null || n < x);
		twice(() -> assertThat(emptyFilteredLess, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().next());

		Sequence<Integer> filteredLess = nineRandom.filterForward((x, n) -> n == null || n < x);
		twice(() -> assertThat(filteredLess, containsUnsized(67, 43, 24, 67)));

		Sequence<Integer> filteredGreater = nineRandom.filterForward((x, n) -> n == null || n > x);
		twice(() -> assertThat(filteredGreater, containsUnsized(5, 3, 5, 7, 5, 67)));

		Sequence<Integer> filteredMutable = mutableFive.filterForward((x, n) -> n == null || x < n);
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(filteredMutable));
		twice(() -> assertThat(filteredMutable, containsUnsized(1, 2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));
	}

	@Test
	public void filterForwardWithReplacement() {
		Sequence<Integer> emptyFilteredLess = empty.filterForward(117, (x, n) -> n < x);
		twice(() -> assertThat(emptyFilteredLess, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().next());

		Sequence<Integer> filteredLess = nineRandom.filterForward(117, (x, n) -> n < x);
		twice(() -> assertThat(filteredLess, containsUnsized(67, 43, 24)));

		Sequence<Integer> filteredGreater = nineRandom.filterForward(117, (x, n) -> n > x);
		twice(() -> assertThat(filteredGreater, containsUnsized(5, 3, 5, 7, 5, 67)));

		Sequence<Integer> filteredMutable = mutableFive.filterForward(117, (x, n) -> x < n);
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(filteredMutable));
		twice(() -> assertThat(filteredMutable, containsUnsized(1, 2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));
	}

	@Test
	public void includingArray() {
		Sequence<Integer> emptyIncluding = empty.including(1, 3, 5, 17);
		twice(() -> assertThat(emptyIncluding, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyIncluding.iterator().next());

		Sequence<Integer> includingSome = _12345.including(1, 3, 5, 17);
		twice(() -> assertThat(includingSome, containsUnsized(1, 3, 5)));

		Sequence<Integer> includingAll = _12345.including(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(includingAll, containsUnsized(1, 2, 3, 4, 5)));

		Sequence<Integer> includingNone = _12345.including();
		twice(() -> assertThat(includingNone, is(emptyUnsizedIterable())));

		Sequence<Integer> mutableIncludingSome = mutableFive.including(1, 3, 5, 17);
		assertThat(Tests.removeFirst(mutableIncludingSome), is(1));
		twice(() -> assertThat(mutableIncludingSome, containsUnsized(3, 5)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));
	}

	@Test
	public void includingIterable() {
		Sequence<Integer> emptyIncluding = empty.including(Iterables.of(1, 3, 5, 17));
		twice(() -> assertThat(emptyIncluding, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyIncluding.iterator().next());

		Sequence<Integer> includingSome = _12345.including(Iterables.of(1, 3, 5, 17));
		twice(() -> assertThat(includingSome, containsUnsized(1, 3, 5)));

		Sequence<Integer> includingAll = _12345.including(Iterables.of(1, 2, 3, 4, 5, 17));
		twice(() -> assertThat(includingAll, containsUnsized(1, 2, 3, 4, 5)));

		Sequence<Integer> includingNone = _12345.including(Iterables.of());
		twice(() -> assertThat(includingNone, is(emptyUnsizedIterable())));

		Sequence<Integer> includingMutable = mutableFive.including(Iterables.of(1, 3, 5, 17));
		assertThat(Tests.removeFirst(includingMutable), is(1));
		twice(() -> assertThat(includingMutable, containsUnsized(3, 5)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));
	}

	@Test
	public void excludingArray() {
		Sequence<Integer> emptyExcluding = empty.excluding(1, 3, 5, 17);
		twice(() -> assertThat(emptyExcluding, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyExcluding.iterator().next());

		Sequence<Integer> excludingSome = _12345.excluding(1, 3, 5, 17);
		twice(() -> assertThat(excludingSome, containsUnsized(2, 4)));

		Sequence<Integer> excludingAll = _12345.excluding(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(excludingAll, is(emptyUnsizedIterable())));

		Sequence<Integer> excludingNone = _12345.excluding();
		twice(() -> assertThat(excludingNone, containsUnsized(1, 2, 3, 4, 5)));

		Sequence<Integer> excludingMutable = mutableFive.excluding(1, 3, 5, 17);
		assertThat(Tests.removeFirst(excludingMutable), is(2));
		twice(() -> assertThat(excludingMutable, containsUnsized(4)));
		twice(() -> assertThat(mutableFive, containsSized(1, 3, 4, 5)));
	}

	@Test
	public void excludingIterable() {
		Sequence<Integer> emptyExcluding = empty.excluding(Iterables.of(1, 3, 5, 17));
		twice(() -> assertThat(emptyExcluding, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyExcluding.iterator().next());

		Sequence<Integer> excludingSome = _12345.excluding(Iterables.of(1, 3, 5, 17));
		twice(() -> assertThat(excludingSome, containsUnsized(2, 4)));

		Sequence<Integer> excludingAll = _12345.excluding(Iterables.of(1, 2, 3, 4, 5, 17));
		twice(() -> assertThat(excludingAll, is(emptyUnsizedIterable())));

		Sequence<Integer> excludingNone = _12345.excluding(Iterables.of());
		twice(() -> assertThat(excludingNone, containsUnsized(1, 2, 3, 4, 5)));

		Sequence<Integer> excludingMutable = mutableFive.excluding(Iterables.of(1, 3, 5, 17));
		assertThat(Tests.removeFirst(excludingMutable), is(2));
		twice(() -> assertThat(excludingMutable, containsUnsized(4)));
		twice(() -> assertThat(mutableFive, containsSized(1, 3, 4, 5)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flatMapIterables() {
		Function<Integer, Iterable<Integer>> appendZero = x -> Iterables.of(x, 0);

		Sequence<Integer> emptyFlatMapped = empty.flatten(appendZero);
		twice(() -> assertThat(emptyFlatMapped, is(emptySizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyFlatMapped.iterator().next());

		Sequence<Integer> fiveFlatMapped = _12345.flatten(appendZero);
		twice(() -> assertThat(fiveFlatMapped, containsSized(1, 0, 2, 0, 3, 0, 4, 0, 5, 0)));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(fiveFlatMapped));
		twice(() -> assertThat(fiveFlatMapped, containsSized(1, 0, 2, 0, 3, 0, 4, 0, 5, 0)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flatMapCollections() {
		Function<Integer, List<Integer>> appendZero = x -> Lists.of(x, 0);

		Sequence<Integer> emptyFlatMapped = empty.flatten(appendZero);
		twice(() -> assertThat(emptyFlatMapped, is(emptySizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyFlatMapped.iterator().next());

		Sequence<Integer> fiveFlatMapped = _12345.flatten(appendZero);
		twice(() -> assertThat(fiveFlatMapped, containsSized(1, 0, 2, 0, 3, 0, 4, 0, 5, 0)));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(fiveFlatMapped));
		twice(() -> assertThat(fiveFlatMapped, containsSized(1, 0, 2, 0, 3, 0, 4, 0, 5, 0)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flatMapLazy() {
		Function<Iterable<Integer>, Iterable<Integer>> identity = Function.identity();

		Sequence<Integer> flatMap = newSequence((Iterable<Integer>) Iterables.of(1, 2), () -> {
			throw new IllegalStateException();
		}).flatten(identity);

		twice(() -> {
			Iterator<Integer> iterator = flatMap.iterator(); // ISE if not lazy - expected later below
			assertThat(iterator.next(), is(1));
			assertThat(iterator.next(), is(2));
			expecting(IllegalStateException.class, iterator::next);
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flatMapIterators() {
		Sequence<Iterator<Integer>> sequence = newSequence(Iterators.of(1, 2), Iterators.of(3, 4),
		                                                   Iterators.of(5, 6));

		Sequence<Integer> flatMap = sequence.flatten(Iterables::once);
		assertThat(flatMap, contains(1, 2, 3, 4, 5, 6));
		assertThat(flatMap, is(emptyIterable()));
	}

	@Test
	public void flatMapArrays() {
		Sequence<Integer[]> sequence = newSequence(new Integer[]{1, 2}, new Integer[]{3, 4}, new Integer[]{5, 6});

		Sequence<Integer> flatMap = sequence.flatten(Iterables::of);
		twice(() -> assertThat(flatMap, containsSized(1, 2, 3, 4, 5, 6)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flattenIterables() {
		Sequence<Integer> emptyFlattened = newSequence().flatten();
		twice(() -> assertThat(emptyFlattened, is(emptySizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyFlattened.iterator().next());

		Sequence<Integer> flattened = newSequence(Iterables.of(1, 2), Iterables.of(3, 4), Iterables.of(5, 6))
				.flatten();
		twice(() -> assertThat(flattened, containsSized(1, 2, 3, 4, 5, 6)));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(flattened));
		twice(() -> assertThat(flattened, containsSized(1, 2, 3, 4, 5, 6)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flattenLazy() {
		Sequence<Integer> flattened = newSequence((Iterable<Integer>) Iterables.of(1, 2), () -> {
			throw new IllegalStateException();
		}).flatten();

		twice(() -> {
			// IllegalStateException if not lazy - see below
			Iterator<Integer> iterator = flattened.iterator();
			assertThat(iterator.next(), is(1));
			assertThat(iterator.next(), is(2));
			expecting(IllegalStateException.class, iterator::next);
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flattenIterators() {
		Sequence<Iterator<Integer>> sequence = newSequence(Iterators.of(1, 2), Iterators.of(3, 4),
		                                                   Iterators.of(5, 6));
		Sequence<Integer> flattened = sequence.flatten();
		assertThat(flattened, contains(1, 2, 3, 4, 5, 6));
		assertThat(flattened, is(emptyIterable()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flattenIteratorsSize() {
		Sequence<Iterator<Integer>> sequence = newSequence(Iterators.of(1, 2), Iterators.of(3, 4),
		                                                   Iterators.of(5, 6));
		Sequence<Integer> flattened = sequence.flatten();
		assertThat(flattened.size(), is(6));
		assertThat(flattened.size(), is(0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flattenIteratorsIsEmpty() {
		Sequence<Iterator<Integer>> sequence = newSequence(Iterators.of(1, 2), Iterators.of(3, 4),
		                                                   Iterators.of(5, 6));
		Sequence<Integer> flattened = sequence.flatten();
		twice(() -> assertThat(flattened.isEmpty(), is(false)));
	}

	@Test
	public void flattenStreams() {
		Sequence<Stream<Integer>> sequence = newSequence(Stream.of(1, 2), Stream.of(3, 4), Stream.of(5, 6));

		Sequence<Integer> flattened = sequence.flatten();
		assertThat(flattened, contains(1, 2, 3, 4, 5, 6));

		expecting(IllegalStateException.class, flattened.iterator()::next);
	}

	@Test
	public void flattenStreamsSize() {
		Sequence<Stream<Integer>> sequence = newSequence(Stream.of(1, 2), Stream.of(3, 4), Stream.of(5, 6));

		Sequence<Integer> flattened = sequence.flatten();
		assertThat(flattened.size(), is(6));

		expecting(IllegalStateException.class, flattened::size);
	}

	@Test
	public void flattenStreamsIsEmpty() {
		Sequence<Stream<Integer>> sequence = newSequence(Stream.of(1, 2), Stream.of(3, 4), Stream.of(5, 6));

		Sequence<Integer> flattened = sequence.flatten();
		assertThat(flattened.isEmpty(), is(false));

		expecting(IllegalStateException.class, flattened::isEmpty);
	}

	@Test
	public void flattenArrays() {
		Sequence<Integer[]> sequence = newSequence(new Integer[]{1, 2}, new Integer[]{3, 4}, new Integer[]{5, 6});

		Sequence<Integer> flattened = sequence.flatten();
		twice(() -> assertThat(flattened, containsSized(1, 2, 3, 4, 5, 6)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flattenPairs() {
		Sequence<Pair<String, Integer>> sequence = newSequence(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3));

		Sequence<?> flattened = sequence.flatten();
		twice(() -> assertThat(flattened, containsSized("1", 1, "2", 2, "3", 3)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flattenEntries() {
		Sequence<Entry<String, Integer>> sequence = newSequence(Maps.entry("1", 1), Maps.entry("2", 2),
		                                                        Maps.entry("3", 3));

		Sequence<?> flattened = sequence.flatten();
		twice(() -> assertThat(flattened, containsSized("1", 1, "2", 2, "3", 3)));
	}

	@Test
	public void flattenInvalid() {
		Iterator<?> iterator = _12345.flatten().iterator();
		expecting(ClassCastException.class, iterator::next);
	}

	@Test
	public void map() {
		Sequence<String> emptyMapped = empty.map(Object::toString);
		twice(() -> assertThat(emptyMapped, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().next());

		Sequence<String> oneMapped = _1.map(Object::toString);
		twice(() -> assertThat(oneMapped, containsFixed("1")));

		Sequence<String> twoMapped = _12.map(Object::toString);
		twice(() -> assertThat(twoMapped, containsFixed("1", "2")));

		Sequence<String> fiveMapped = _12345.map(Object::toString);
		twice(() -> assertThat(fiveMapped, containsFixed("1", "2", "3", "4", "5")));

		Sequence<String> mutableMapped = mutableFive.map(Object::toString);
		assertThat(Tests.removeFirst(mutableMapped), is("1"));
		twice(() -> assertThat(mutableMapped, containsSized("2", "3", "4", "5")));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<String> mappedSizePassThrough = sizePassThrough.map(Object::toString);
		twice(() -> assertThat(mappedSizePassThrough.size(), is(10)));
		twice(() -> assertThat(mappedSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void biMap() {
		Sequence<String> emptyMapped = empty.biMap(Object::toString, Integer::parseInt);
		twice(() -> assertThat(emptyMapped, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().next());

		Sequence<String> oneMapped = _1.biMap(Object::toString, Integer::parseInt);
		twice(() -> assertThat(oneMapped, containsFixed("1")));

		Sequence<String> twoMapped = _12.biMap(Object::toString, Integer::parseInt);
		twice(() -> assertThat(twoMapped, containsFixed("1", "2")));

		Sequence<String> fiveMapped = _12345.biMap(Object::toString, Integer::parseInt);
		twice(() -> assertThat(fiveMapped, containsFixed("1", "2", "3", "4", "5")));

		Sequence<String> mutableMapped = mutableFive.biMap(Object::toString, Integer::parseInt);
		assertThat(Tests.removeFirst(mutableMapped), is("1"));
		twice(() -> assertThat(mutableMapped, containsSized("2", "3", "4", "5")));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<String> mappedSizePassThrough = sizePassThrough.biMap(Object::toString, Integer::parseInt);
		twice(() -> assertThat(mappedSizePassThrough.size(), is(10)));
		twice(() -> assertThat(mappedSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void mapWithIndex() {
		Sequence<String> emptyMapped = empty.map(Object::toString);
		twice(() -> assertThat(emptyMapped, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().next());

		AtomicInteger index = new AtomicInteger();
		Sequence<String> oneMapped = _1.mapIndexed((x, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return String.valueOf(x);
		});
		twice(() -> {
			index.set(0);
			assertThat(oneMapped, containsFixed("1"));
		});

		Sequence<String> twoMapped = _12.mapIndexed((x, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return String.valueOf(x);
		});
		twice(() -> {
			index.set(0);
			assertThat(twoMapped, containsFixed("1", "2"));
		});

		Sequence<String> fiveMapped = _12345.mapIndexed((x, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return String.valueOf(x);
		});
		twice(() -> {
			index.set(0);
			assertThat(fiveMapped, containsFixed("1", "2", "3", "4", "5"));
		});

		Sequence<String> mutableMapped = mutableFive.mapIndexed((x, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return String.valueOf(x);
		});
		index.set(0);
		assertThat(Tests.removeFirst(mutableMapped), is("1"));
		twice(() -> {
			index.set(0);
			assertThat(mutableMapped, containsSized("2", "3", "4", "5"));
		});
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<String> mappedSizePassThrough = sizePassThrough.mapIndexed((x, i) -> String.valueOf(x));
		twice(() -> assertThat(mappedSizePassThrough.size(), is(10)));
		twice(() -> assertThat(mappedSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void mapIsLazy() {
		Sequence<Integer> sequence = Sequence.concat(Iterables.of(1), () -> {
			throw new IllegalStateException();
		});

		Sequence<String> mapped = sequence.map(Object::toString);

		twice(() -> {
			Iterator<String> iterator = mapped.iterator(); // ISE here if not lazy
			assertThat(iterator.next(), is("1"));
			expecting(IllegalStateException.class, iterator::next);
		});
	}

	@Test
	public void mapBack() {
		Sequence<Integer> emptyMappedBack = empty.mapBack((p, c) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedBack, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedBack.iterator().next());

		Sequence<Integer> fiveMappedBackToPrevious = _12345.mapBack((p, c) -> p);
		twice(() -> assertThat(fiveMappedBackToPrevious, containsFixed(null, 1, 2, 3, 4)));

		Sequence<Integer> fiveMappedBackToCurrent = _12345.mapBack((p, c) -> c);
		twice(() -> assertThat(fiveMappedBackToCurrent, containsFixed(1, 2, 3, 4, 5)));

		Sequence<Integer> mutableMappedBackToCurrent = mutableFive.mapBack((p, c) -> c);
		assertThat(Tests.removeFirst(mutableMappedBackToCurrent), is(1));
		twice(() -> assertThat(mutableMappedBackToCurrent, containsSized(2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<Integer> mappedBackSizePassThrough = sizePassThrough.mapBack((p, c) -> c);
		twice(() -> assertThat(mappedBackSizePassThrough.size(), is(10)));
		twice(() -> assertThat(mappedBackSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void mapForward() {
		Sequence<Integer> emptyMappedForward = empty.mapForward((c, n) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedForward, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedForward.iterator().next());

		Sequence<Integer> fiveMappedForwardToCurrent = _12345.mapForward((c, n) -> c);
		twice(() -> assertThat(fiveMappedForwardToCurrent, containsFixed(1, 2, 3, 4, 5)));

		Sequence<Integer> fiveMappedForwardToNext = _12345.mapForward((c, n) -> n);
		twice(() -> assertThat(fiveMappedForwardToNext, containsFixed(2, 3, 4, 5, null)));

		Sequence<Integer> mutableMappedForwardToCurrent = mutableFive.mapForward((c, n) -> c);
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutableMappedForwardToCurrent));
		twice(() -> assertThat(mutableMappedForwardToCurrent, containsSized(1, 2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));

		Sequence<Integer> mappedForwardSizePassThrough = sizePassThrough.mapForward((c, n) -> c);
		twice(() -> assertThat(mappedForwardSizePassThrough.size(), is(10)));
		twice(() -> assertThat(mappedForwardSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void mapBackWithReplacement() {
		Sequence<Integer> emptyMappedBack = empty.mapBack(117, (p, n) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedBack, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedBack.iterator().next());

		Sequence<Integer> fiveMappedBackToPrevious = _12345.mapBack(117, (p, n) -> p);
		twice(() -> assertThat(fiveMappedBackToPrevious, containsFixed(117, 1, 2, 3, 4)));

		Sequence<Integer> fiveMappedBackToCurrent = _12345.mapBack(117, (p, n) -> n);
		twice(() -> assertThat(fiveMappedBackToCurrent, containsFixed(1, 2, 3, 4, 5)));

		Sequence<Integer> mutableMappedBackToCurrent = mutableFive.mapBack(117, (p, n) -> n);
		assertThat(Tests.removeFirst(mutableMappedBackToCurrent), is(1));
		twice(() -> assertThat(mutableMappedBackToCurrent, containsSized(2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<Integer> mappedBackSizePassThrough = sizePassThrough.mapBack(117, (p, c) -> c);
		twice(() -> assertThat(mappedBackSizePassThrough.size(), is(10)));
		twice(() -> assertThat(mappedBackSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void mapForwardWithReplacement() {
		Sequence<Integer> emptyMappedForward = empty.mapForward(117, (c, n) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedForward, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedForward.iterator().next());

		Sequence<Integer> fiveMappedForwardToCurrent = _12345.mapForward(117, (c, n) -> c);
		twice(() -> assertThat(fiveMappedForwardToCurrent, containsFixed(1, 2, 3, 4, 5)));

		Sequence<Integer> fiveMappedForwardToNext = _12345.mapForward(117, (c, n) -> n);
		twice(() -> assertThat(fiveMappedForwardToNext, containsFixed(2, 3, 4, 5, 117)));

		Sequence<Integer> mutableMappedForwardToCurrent = mutableFive.mapForward(117, (c, n) -> c);
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutableMappedForwardToCurrent));
		twice(() -> assertThat(mutableMappedForwardToCurrent, containsSized(1, 2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));

		Sequence<Integer> mappedForwardSizePassThrough = sizePassThrough.mapForward(117, (c, n) -> c);
		twice(() -> assertThat(mappedForwardSizePassThrough.size(), is(10)));
		twice(() -> assertThat(mappedForwardSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void cast() {
		Sequence<Number> emptyCast = empty.cast(Number.class);
		twice(() -> assertThat(emptyCast, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyCast.iterator().next());

		Sequence<Number> oneCast = _1.cast(Number.class);
		twice(() -> assertThat(oneCast, containsFixed(1)));

		Sequence<Number> twoCast = _12.cast(Number.class);
		twice(() -> assertThat(twoCast, containsFixed(1, 2)));

		Sequence<Number> fiveCast = _12345.cast(Number.class);
		twice(() -> assertThat(fiveCast, containsFixed(1, 2, 3, 4, 5)));

		Sequence<Number> mutableCast = mutableFive.cast(Number.class);
		assertThat(Tests.removeFirst(mutableCast), is(1));
		twice(() -> assertThat(mutableCast, containsSized(2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<Number> castSizePassThrough = sizePassThrough.cast(Number.class);
		twice(() -> assertThat(castSizePassThrough.size(), is(10)));
		twice(() -> assertThat(castSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void peek() {
		Sequence<Integer> emptyPeeked = empty.peek(x -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyPeeked, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().next());

		AtomicInteger value = new AtomicInteger(1);
		Sequence<Integer> onePeeked = _1.peek(x -> assertThat(x, is(value.getAndIncrement())));
		twiceIndexed(value, 1, () -> assertThat(onePeeked, containsFixed(1)));

		Sequence<Integer> twoPeeked = _12.peek(x -> assertThat(x, is(value.getAndIncrement())));
		twiceIndexed(value, 2, () -> assertThat(twoPeeked, containsFixed(1, 2)));

		Sequence<Integer> fivePeeked = _12345.peek(x -> assertThat(x, is(value.getAndIncrement())));
		twiceIndexed(value, 5, () -> assertThat(fivePeeked, containsFixed(1, 2, 3, 4, 5)));

		Sequence<Integer> mutablePeeked = mutableFive.peek(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(Tests.removeFirst(mutablePeeked), is(1));
		twiceIndexed(value, 4, () -> assertThat(mutablePeeked, containsSized(2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<Integer> sizePassThroughPeeked = sizePassThrough.peek(x -> {});
		assertThat(sizePassThroughPeeked.size(), is(10));
		assertThat(sizePassThroughPeeked.isEmpty(), is(false));
	}

	@Test
	public void peekIndexed() {
		Sequence<Integer> emptyPeeked = empty.peekIndexed((x, i) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyPeeked, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().next());

		AtomicInteger index = new AtomicInteger();
		AtomicInteger value = new AtomicInteger(1);
		Sequence<Integer> onePeeked = _1.peekIndexed((x, i) -> {
			assertThat(x, is(value.getAndIncrement()));
			assertThat(i, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(onePeeked, containsFixed(1));

			assertThat(index.get(), is(1));
			assertThat(value.get(), is(2));
			index.set(0);
			value.set(1);
		});

		Sequence<Integer> twoPeeked = _12.peekIndexed((x, i) -> {
			assertThat(x, is(value.getAndIncrement()));
			assertThat(i, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(twoPeeked, containsFixed(1, 2));

			assertThat(index.get(), is(2));
			assertThat(value.get(), is(3));
			index.set(0);
			value.set(1);
		});

		Sequence<Integer> fivePeeked = _12345.peekIndexed((x, i) -> {
			assertThat(x, is(value.getAndIncrement()));
			assertThat(i, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(fivePeeked, containsFixed(1, 2, 3, 4, 5));

			assertThat(index.get(), is(5));
			assertThat(value.get(), is(6));
			index.set(0);
			value.set(1);
		});

		Sequence<Integer> mutablePeeked = mutableFive.peekIndexed((x, i) -> {
			assertThat(x, is(value.getAndIncrement()));
			assertThat(i, is(index.getAndIncrement()));
		});
		assertThat(Tests.removeFirst(mutablePeeked), is(1));
		index.set(0);
		value.set(2);

		twice(() -> {
			assertThat(mutablePeeked, containsSized(2, 3, 4, 5));
			assertThat(index.get(), is(4));
			assertThat(value.get(), is(6));
			index.set(0);
			value.set(2);
		});
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<Integer> sizePassThroughPeeked = sizePassThrough.peekIndexed((x, i) -> {});
		assertThat(sizePassThroughPeeked.size(), is(10));
		assertThat(sizePassThroughPeeked.isEmpty(), is(false));
	}

	@Test
	public void peekBack() {
		Sequence<Integer> emptyPeekingBack = empty.peekBack((previous, current) -> fail("should not get called"));
		twice(() -> assertThat(emptyPeekingBack, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeekingBack.iterator().next());

		AtomicInteger index = new AtomicInteger(0);
		AtomicInteger value = new AtomicInteger(1);
		Sequence<Integer> fivePeekingBack = _12345.peekBack((p, n) -> {
			assertThat(n, is(value.getAndIncrement()));
			assertThat(p, is(index.getAndIncrement() == 0 ? null : n - 1));
		});
		twice(() -> {
			index.set(0);
			value.set(1);
			assertThat(fivePeekingBack, containsFixed(1, 2, 3, 4, 5));
		});

		index.set(0);
		value.set(1);
		Sequence<Integer> mutablePeekingBack = mutableFive.peekBack((p, n) -> {
			assertThat(n, is(value.getAndIncrement()));
			assertThat(p, is(index.getAndIncrement() == 0 ? null : n - 1));
		});
		assertThat(Tests.removeFirst(mutablePeekingBack), is(1));
		assertThat(mutablePeekingBack.size(), is(4));
		twice(() -> {
			index.set(0);
			value.set(2);
			assertThat(mutablePeekingBack, containsSized(2, 3, 4, 5));
		});
		assertThat(mutableFive, containsSized(2, 3, 4, 5));

		Sequence<Integer> mappedForwardSizePassThrough = sizePassThrough.peekBack((p, c) -> {});
		twice(() -> assertThat(mappedForwardSizePassThrough.size(), is(10)));
		twice(() -> assertThat(mappedForwardSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void peekForward() {
		Sequence<Integer> emptyPeekingForward = empty.peekForward((current, next) -> fail("should not get called"));
		twice(() -> assertThat(emptyPeekingForward, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeekingForward.iterator().next());

		AtomicInteger value = new AtomicInteger(1);
		Sequence<Integer> fivePeekingForward = _12345.peekForward((current, next) -> {
			assertThat(current, is(value.getAndIncrement()));
			assertThat(next, is(current == 5 ? null : current + 1));
		});
		twice(() -> {
			value.set(1);
			assertThat(fivePeekingForward, containsFixed(1, 2, 3, 4, 5));
		});

		value.set(1);
		Sequence<Integer> mutablePeekingForward = mutableFive.peekForward((current, next) -> {
			assertThat(current, is(value.getAndIncrement()));
			assertThat(next, is(current == 5 ? null : current + 1));
		});
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutablePeekingForward));
		twice(() -> {
			value.set(1);
			assertThat(mutablePeekingForward, containsSized(1, 2, 3, 4, 5));
		});
		assertThat(mutableFive, containsSized(1, 2, 3, 4, 5));

		Sequence<Integer> mappedForwardSizePassThrough = sizePassThrough.peekForward((p, c) -> {});
		twice(() -> assertThat(mappedForwardSizePassThrough.size(), is(10)));
		twice(() -> assertThat(mappedForwardSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void peekBackWithReplacement() {
		Sequence<Integer> emptyPeekingBack = empty.peekBack(117, (previous, current) -> fail("should not get called"));
		twice(() -> assertThat(emptyPeekingBack, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeekingBack.iterator().next());

		AtomicInteger index = new AtomicInteger(0);
		AtomicInteger value = new AtomicInteger(1);
		Sequence<Integer> fivePeekingBack = _12345.peekBack(117, (p, n) -> {
			assertThat(n, is(value.getAndIncrement()));
			assertThat(p, is(index.getAndIncrement() == 0 ? 117 : n - 1));
		});
		twice(() -> {
			index.set(0);
			value.set(1);
			assertThat(fivePeekingBack, containsFixed(1, 2, 3, 4, 5));
		});

		index.set(0);
		value.set(1);
		Sequence<Integer> mutablePeekingBack = mutableFive.peekBack(117, (p, n) -> {
			assertThat(n, is(value.getAndIncrement()));
			assertThat(p, is(index.getAndIncrement() == 0 ? 117 : n - 1));
		});
		assertThat(Tests.removeFirst(mutablePeekingBack), is(1));
		assertThat(mutablePeekingBack.size(), is(4));
		twice(() -> {
			index.set(0);
			value.set(2);
			assertThat(mutablePeekingBack, containsSized(2, 3, 4, 5));
		});
		assertThat(mutableFive, containsSized(2, 3, 4, 5));

		Sequence<Integer> mappedForwardSizePassThrough = sizePassThrough.peekBack(117, (p, c) -> {});
		twice(() -> assertThat(mappedForwardSizePassThrough.size(), is(10)));
		twice(() -> assertThat(mappedForwardSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void peekForwardWithReplacement() {
		Sequence<Integer> emptyPeekingForward = empty.peekForward(117, (current, next) ->
				fail("should not get called"));
		twice(() -> assertThat(emptyPeekingForward, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeekingForward.iterator().next());

		AtomicInteger value = new AtomicInteger(1);
		Sequence<Integer> fivePeekingForward = _12345.peekForward(117, (current, next) -> {
			assertThat(current, is(value.getAndIncrement()));
			assertThat(next, is(current == 5 ? 117 : current + 1));
		});
		twice(() -> {
			value.set(1);
			assertThat(fivePeekingForward, containsFixed(1, 2, 3, 4, 5));
		});

		value.set(1);
		Sequence<Integer> mutablePeekingForward = mutableFive.peekForward(117, (current, next) -> {
			assertThat(current, is(value.getAndIncrement()));
			assertThat(next, is(current == 5 ? 117 : current + 1));
		});
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutablePeekingForward));
		twice(() -> {
			value.set(1);
			assertThat(mutablePeekingForward, containsSized(1, 2, 3, 4, 5));
		});
		assertThat(mutableFive, containsSized(1, 2, 3, 4, 5));

		Sequence<Integer> mappedForwardSizePassThrough = sizePassThrough.peekForward(117, (p, c) -> {});
		twice(() -> assertThat(mappedForwardSizePassThrough.size(), is(10)));
		twice(() -> assertThat(mappedForwardSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void recurse() {
		Sequence<Integer> sequence = Sequence.recurse(1, i -> i + 1);
		twice(() -> assertThat(sequence, infiniteBeginningWith(1, 2, 3, 4, 5)));
		expecting(UnsupportedOperationException.class, sequence::size);
		twice(() -> assertThat(sequence.isEmpty(), is(false)));
	}

	@Test
	public void recurseTwins() {
		Sequence<String> sequence = Sequence.recurse(1, Object::toString, s -> parseInt(s) + 1);
		twice(() -> assertThat(sequence, infiniteBeginningWith("1", "2", "3", "4", "5")));
		expecting(UnsupportedOperationException.class, sequence::size);
		twice(() -> assertThat(sequence.isEmpty(), is(false)));
	}

	@Test
	public void untilTerminal() {
		Sequence<Integer> emptyUntil5 = empty.until(5);
		twice(() -> assertThat(emptyUntil5, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntil5.iterator().next());

		Sequence<Integer> nineUntil5 = _123456789.until(5);
		twice(() -> assertThat(nineUntil5, containsUnsized(1, 2, 3, 4)));

		Sequence<Integer> mutableUntil3 = mutableFive.until(3);
		assertThat(Tests.removeFirst(mutableUntil3), is(1));
		twice(() -> assertThat(mutableUntil3, containsUnsized(2)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<Integer> fiveUntil10 = _12345.until(10);
		twice(() -> assertThat(fiveUntil10, containsUnsized(1, 2, 3, 4, 5)));
	}

	@Test
	public void untilNull() {
		Sequence<Integer> emptyUntilNull = empty.untilNull();
		twice(() -> assertThat(emptyUntilNull, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntilNull.iterator().next());

		Sequence<Integer> nineUntilNull = newSequence(1, 2, 3, 4, 5, null, 7, 8, 9).untilNull();
		twice(() -> assertThat(nineUntilNull, containsUnsized(1, 2, 3, 4, 5)));

		Sequence<Integer> fiveUntilNull = _12345.untilNull();
		twice(() -> assertThat(fiveUntilNull, containsUnsized(1, 2, 3, 4, 5)));

		Sequence<Integer> mutableUntilNull = mutableFive.untilNull();
		assertThat(Tests.removeFirst(mutableUntilNull), is(1));
		twice(() -> assertThat(mutableUntilNull, containsUnsized(2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));
	}

	@Test
	public void untilPredicate() {
		Sequence<Integer> emptyUntilEqual5 = empty.until(i -> i == 5);
		twice(() -> assertThat(emptyUntilEqual5, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntilEqual5.iterator().next());

		Sequence<Integer> nineUntilEqual5 = _123456789.until(i -> i == 5);
		twice(() -> assertThat(nineUntilEqual5, containsUnsized(1, 2, 3, 4)));

		Sequence<Integer> mutableUntilEqual3 = mutableFive.until(i -> i == 3);
		assertThat(Tests.removeFirst(mutableUntilEqual3), is(1));
		twice(() -> assertThat(mutableUntilEqual3, containsUnsized(2)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<Integer> fiveUntilEqual10 = _12345.until(i -> i == 10);
		twice(() -> assertThat(fiveUntilEqual10, containsUnsized(1, 2, 3, 4, 5)));
	}

	@Test
	public void endingAtTerminal() {
		Sequence<Integer> emptyEndingAt5 = empty.endingAt(5);
		twice(() -> assertThat(emptyEndingAt5, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAt5.iterator().next());

		Sequence<Integer> nineEndingAt5 = _123456789.endingAt(5);
		twice(() -> assertThat(nineEndingAt5, containsUnsized(1, 2, 3, 4, 5)));

		Sequence<Integer> fiveEndingAt10 = _12345.endingAt(10);
		twice(() -> assertThat(fiveEndingAt10, containsUnsized(1, 2, 3, 4, 5)));

		Sequence<Integer> mutableEndingAt3 = mutableFive.endingAt(3);
		assertThat(Tests.removeFirst(mutableEndingAt3), is(1));
		twice(() -> assertThat(mutableEndingAt3, containsUnsized(2, 3)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));
	}

	@Test
	public void endingAtNull() {
		Sequence<Integer> emptyEndingAtNull = empty.endingAtNull();
		twice(() -> assertThat(emptyEndingAtNull, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAtNull.iterator().next());

		Sequence<Integer> nineEndingAtNull = newSequence(1, 2, 3, 4, 5, null, 7, 8, 9).endingAtNull();
		twice(() -> assertThat(nineEndingAtNull, containsUnsized(1, 2, 3, 4, 5, null)));

		Sequence<Integer> fiveEndingAtNull = _12345.endingAtNull();
		twice(() -> assertThat(fiveEndingAtNull, containsUnsized(1, 2, 3, 4, 5)));

		Sequence<Integer> mutableEndingAtNull = mutableFive.endingAtNull();
		assertThat(Tests.removeFirst(mutableEndingAtNull), is(1));
		twice(() -> assertThat(mutableEndingAtNull, containsUnsized(2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));
	}

	@Test
	public void endingAtPredicate() {
		Sequence<Integer> emptyEndingAtEqual5 = empty.endingAt(i -> i == 5);
		twice(() -> assertThat(emptyEndingAtEqual5, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAtEqual5.iterator().next());

		Sequence<Integer> nineEndingAtEqual5 = _123456789.endingAt(i -> i == 5);
		twice(() -> assertThat(nineEndingAtEqual5, containsUnsized(1, 2, 3, 4, 5)));

		Sequence<Integer> mutableEndingAtEqual3 = mutableFive.endingAt(i -> i == 3);
		assertThat(Tests.removeFirst(mutableEndingAtEqual3), is(1));
		twice(() -> assertThat(mutableEndingAtEqual3, containsUnsized(2, 3)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<Integer> fiveEndingAtEqual10 = _12345.endingAt(i -> i == 10);
		twice(() -> assertThat(fiveEndingAtEqual10, containsUnsized(1, 2, 3, 4, 5)));
	}

	@Test
	public void startingAfter() {
		Sequence<Integer> emptyStartingAfter5 = empty.startingAfter(5);
		twice(() -> assertThat(emptyStartingAfter5, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingAfter5.iterator().next());

		Sequence<Integer> nineStartingAfter5 = _123456789.startingAfter(5);
		twice(() -> assertThat(nineStartingAfter5, containsUnsized(6, 7, 8, 9)));

		Sequence<Integer> mutableStartingAfter3 = mutableFive.startingAfter(3);
		assertThat(Tests.removeFirst(mutableStartingAfter3), is(4));
		twice(() -> assertThat(mutableStartingAfter3, containsUnsized(5)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 5)));

		Sequence<Integer> fiveStartingAfter10 = _12345.startingAfter(10);
		twice(() -> assertThat(fiveStartingAfter10, is(emptyUnsizedIterable())));
	}

	@Test
	public void startingAfterPredicate() {
		Sequence<Integer> emptyStartingAfterEqual5 = empty.startingAfter(i -> i == 5);
		twice(() -> assertThat(emptyStartingAfterEqual5, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingAfterEqual5.iterator().next());

		Sequence<Integer> nineStartingAfterEqual5 = _123456789.startingAfter(i -> i == 5);
		twice(() -> assertThat(nineStartingAfterEqual5, containsUnsized(6, 7, 8, 9)));

		Sequence<Integer> mutableStartingAfterEqual3 = mutableFive.startingAfter(i -> i == 3);
		assertThat(Tests.removeFirst(mutableStartingAfterEqual3), is(4));
		twice(() -> assertThat(mutableStartingAfterEqual3, containsUnsized(5)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 5)));

		Sequence<Integer> fiveStartingAfterEqual10 = _12345.startingAfter(i -> i == 10);
		twice(() -> assertThat(fiveStartingAfterEqual10, is(emptyUnsizedIterable())));
	}

	@Test
	public void startingFrom() {
		Sequence<Integer> emptyStartingFrom5 = empty.startingFrom(5);
		twice(() -> assertThat(emptyStartingFrom5, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingFrom5.iterator().next());

		Sequence<Integer> nineStartingFrom5 = _123456789.startingFrom(5);
		twice(() -> assertThat(nineStartingFrom5, containsUnsized(5, 6, 7, 8, 9)));

		Sequence<Integer> mutableStartingFrom3 = mutableFive.startingFrom(3);
		assertThat(Tests.removeFirst(mutableStartingFrom3), is(3));
		twice(() -> assertThat(mutableStartingFrom3, is(emptyUnsizedIterable())));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 4, 5)));

		Sequence<Integer> fiveStartingFrom10 = _12345.startingFrom(10);
		twice(() -> assertThat(fiveStartingFrom10, is(emptyUnsizedIterable())));
	}

	@Test
	public void startingFromPredicate() {
		Sequence<Integer> emptyStartingFromEqual5 = empty.startingFrom(i -> i == 5);
		twice(() -> assertThat(emptyStartingFromEqual5, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingFromEqual5.iterator().next());

		Sequence<Integer> nineStartingFromEqual5 = _123456789.startingFrom(i -> i == 5);
		twice(() -> assertThat(nineStartingFromEqual5, containsUnsized(5, 6, 7, 8, 9)));

		Sequence<Integer> mutableStartingFromEqual3 = mutableFive.startingFrom(i -> i == 3);
		assertThat(Tests.removeFirst(mutableStartingFromEqual3), is(3));
		twice(() -> assertThat(mutableStartingFromEqual3, is(emptyUnsizedIterable())));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 4, 5)));

		Sequence<Integer> fiveStartingFromEqual10 = _12345.startingFrom(i -> i == 10);
		twice(() -> assertThat(fiveStartingFromEqual10, is(emptyUnsizedIterable())));
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
			assertThat(list, is(instanceOf(LinkedList.class)));
			assertThat(list, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSet() {
		twice(() -> {
			Set<Integer> set = _12345.toSet();
			assertThat(set, is(instanceOf(HashSet.class)));
			assertThat(set, containsInAnyOrder(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSortedSet() {
		twice(() -> {
			SortedSet<Integer> sortedSet = _12345.toSortedSet();
			assertThat(sortedSet, is(instanceOf(TreeSet.class)));
			assertThat(sortedSet, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSortedSetWithNullComparator() {
		twice(() -> {
			SortedSet<Integer> sortedSet = _12345.toSortedSet(null);
			assertThat(sortedSet, is(instanceOf(TreeSet.class)));
			assertThat(sortedSet, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toSortedSetWithComparator() {
		twice(() -> {
			SortedSet<Integer> sortedSet = _12345.toSortedSet(reverseOrder());
			assertThat(sortedSet, is(instanceOf(TreeSet.class)));
			assertThat(sortedSet, contains(5, 4, 3, 2, 1));
		});
	}

	@Test
	public void toSetWithType() {
		twice(() -> {
			Set<Integer> set = _12345.toSet(LinkedHashSet::new);
			assertThat(set, is(instanceOf(LinkedHashSet.class)));
			assertThat(set, contains(1, 2, 3, 4, 5));
		});
	}

	@Test
	public void toCollection() {
		twice(() -> {
			Deque<Integer> deque = _12345.toCollection(ArrayDeque::new);
			assertThat(deque, is(instanceOf(ArrayDeque.class)));
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
			assertThat(map, is(instanceOf(HashMap.class)));
			assertThat(map, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build())));
		});
	}

	@Test
	public void toMapWithConstructor() {
		Map<String, Integer> original = Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build();
		Sequence<Entry<String, Integer>> sequence = Sequence.from(original);

		twice(() -> {
			Map<String, Integer> linkedMap = sequence.toMap(LinkedHashMap::new);
			assertThat(linkedMap, is(instanceOf(HashMap.class)));
			assertThat(linkedMap, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build())));
		});
	}

	@Test
	public void toMapWithMappers() {
		twice(() -> {
			Map<String, Integer> map = _123.toMap(Object::toString, Function.identity());

			assertThat(map, is(instanceOf(HashMap.class)));
			assertThat(map, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).build())));
		});
	}

	@Test
	public void toMapWithMappersAndConstructor() {
		twice(() -> {
			Map<String, Integer> map = _123.toMap(LinkedHashMap::new, Object::toString, Function.identity());

			assertThat(map, is(instanceOf(LinkedHashMap.class)));
			assertThat(map, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).build())));
		});
	}

	@Test
	public void toMergedMapWithMappers() {
		twice(() -> {
			Map<String, Integer> map = _12345.toMergedMap(x -> String.valueOf(x % 3), Function.identity(),
			                                              (old, value) -> old);

			assertThat(map, is(instanceOf(HashMap.class)));
			assertThat(map, is(equalTo(Maps.builder("1", 1).put("2", 2).put("0", 3).build())));
		});
	}

	@Test
	public void toMergedMapWithMappersAndConstructor() {
		twice(() -> {
			Map<String, Integer> map = _12345.toMergedMap(x -> String.valueOf(x % 3), Function.identity(),
			                                              (old, value) -> value);

			assertThat(map, is(instanceOf(HashMap.class)));
			assertThat(map, is(equalTo(Maps.builder("0", 3).put("1", 4).put("2", 5).build())));
		});
	}

	@Test
	public void groupBy() {
		twice(() -> assertThat(empty.groupBy(x -> x), is(emptyMap())));
		twice(() -> assertThat(_1.groupBy(x -> x), is(singletonMap(1, Lists.of(1)))));
		twice(() -> assertThat(_12.groupBy(x -> x), is(Maps.builder()
		                                                   .put(1, Lists.of(1))
		                                                   .put(2, Lists.of(2))
		                                                   .build())));

		twice(() -> assertThat(empty.groupBy(x -> x / 3), is(emptyMap())));
		twice(() -> assertThat(_1.groupBy(x -> x / 3), is(singletonMap(0, Lists.of(1)))));
		twice(() -> assertThat(_12.groupBy(x -> x / 3), is(singletonMap(0, Lists.of(1, 2)))));
		twice(() -> assertThat(_12345.groupBy(x -> x / 3), is(Maps.builder()
		                                                          .put(0, Lists.of(1, 2))
		                                                          .put(1, Lists.of(3, 4, 5))
		                                                          .build())));

		twice(() -> {
			Map<Integer, List<Integer>> map = _123456789.groupBy(i -> i % 3 == 0 ? null : i % 3);

			assertThat(map, is(instanceOf(HashMap.class)));
			assertThat(map, is(equalTo(Maps.builder()
			                               .put(1, Lists.of(1, 4, 7))
			                               .put(2, Lists.of(2, 5, 8))
			                               .put(null, Lists.of(3, 6, 9))
			                               .build())));
		});
	}

	@Test
	public void groupByWithMapConstructor() {
		Supplier<Map<Integer, List<Integer>>> createLinkedHashMap = LinkedHashMap::new;

		twice(() -> assertThat(empty.groupBy(x -> x, createLinkedHashMap), is(emptyMap())));
		twice(() -> assertThat(_1.groupBy(x -> x, createLinkedHashMap),
		                       is(singletonMap(1, Lists.of(1)))));
		twice(() -> assertThat(_12.groupBy(x -> x, createLinkedHashMap), is(Maps.builder()
		                                                                        .put(1, Lists.of(1))
		                                                                        .put(2, Lists.of(2))
		                                                                        .build())));

		twice(() -> assertThat(empty.groupBy(x -> x / 3, createLinkedHashMap),
		                       is(emptyMap())));
		twice(() -> assertThat(_1.groupBy(x -> x / 3, createLinkedHashMap),
		                       is(singletonMap(0, Lists.of(1)))));
		twice(() -> assertThat(_12.groupBy(x -> x / 3, createLinkedHashMap),
		                       is(singletonMap(0, Lists.of(1, 2)))));
		twice(() -> assertThat(_12345.groupBy(x -> x / 3, createLinkedHashMap),
		                       is(Maps.builder()
		                              .put(0, Lists.of(1, 2))
		                              .put(1, Lists.of(3, 4, 5))
		                              .build())));

		twice(() -> {
			Map<Integer, List<Integer>> map = _123456789.groupBy(i -> i % 3 == 0 ? null : i % 3, LinkedHashMap::new);

			assertThat(map, is(instanceOf(LinkedHashMap.class)));
			assertThat(map, is(equalTo(Maps.builder()
			                               .put(1, Lists.of(1, 4, 7))
			                               .put(2, Lists.of(2, 5, 8))
			                               .put(null, Lists.of(3, 6, 9))
			                               .build())));

			// check order
			assertThat(map.keySet(), contains(1, 2, null));
			//noinspection unchecked
			assertThat(map.values(), contains(contains(1, 4, 7), contains(2, 5, 8), contains(3, 6, 9)));
		});
	}

	@Test
	public void groupByWithMapConstructorAndGroupConstructor() {
		Supplier<Map<Integer, List<Integer>>> createLinkedHashMap = LinkedHashMap::new;

		twice(() -> assertThat(empty.groupBy(x -> x, createLinkedHashMap, LinkedList::new), is(emptyMap())));
		twice(() -> assertThat(_1.groupBy(x -> x, createLinkedHashMap, LinkedList::new),
		                       is(singletonMap(1, Lists.of(1)))));
		twice(() -> assertThat(_12.groupBy(x -> x, createLinkedHashMap, LinkedList::new),
		                       is(Maps.builder()
		                              .put(1, Lists.of(1))
		                              .put(2, Lists.of(2))
		                              .build())));

		twice(() -> assertThat(empty.groupBy(x -> x / 3, createLinkedHashMap, LinkedList::new),
		                       is(emptyMap())));
		twice(() -> assertThat(_1.groupBy(x -> x / 3, createLinkedHashMap, LinkedList::new),
		                       is(singletonMap(0, Lists.of(1)))));
		twice(() -> assertThat(_12.groupBy(x -> x / 3, createLinkedHashMap, LinkedList::new),
		                       is(singletonMap(0, Lists.of(1, 2)))));
		twice(() -> assertThat(_12345.groupBy(x -> x / 3, createLinkedHashMap, LinkedList::new),
		                       is(Maps.builder()
		                              .put(0, Lists.of(1, 2))
		                              .put(1, Lists.of(3, 4, 5))
		                              .build())));

		twice(() -> {
			Map<Integer, SortedSet<Integer>> map = _123456789.groupBy(i -> i % 3 == 0 ? null : i % 3,
			                                                          LinkedHashMap::new, TreeSet::new);

			assertThat(map, is(instanceOf(LinkedHashMap.class)));
			assertThat(map, is(Maps.builder()
			                       .put(1, new TreeSet<>(Lists.of(1, 4, 7)))
			                       .put(2, new TreeSet<>(Lists.of(2, 5, 8)))
			                       .put(null, new TreeSet<>(Lists.of(3, 6, 9)))
			                       .build()));

			assertThat(map.get(1), is(instanceOf(TreeSet.class)));
			assertThat(map.get(2), is(instanceOf(TreeSet.class)));
			assertThat(map.get(null), is(instanceOf(TreeSet.class)));

			// check order
			assertThat(map.keySet(), contains(1, 2, null));
			//noinspection unchecked
			assertThat(map.values(), contains(contains(1, 4, 7), contains(2, 5, 8), contains(3, 6, 9)));
		});
	}

	@Test
	public void groupByWithMapConstructorAndCollector() {
		Supplier<Map<Integer, List<Integer>>> createLinkedHashMap = LinkedHashMap::new;
		Collector<Integer, ?, List<Integer>> toLinkedList = Collectors.toCollection(LinkedList::new);

		twice(() -> assertThat(empty.groupBy(x -> x, createLinkedHashMap, toLinkedList), is(emptyMap())));
		twice(() -> assertThat(_1.groupBy(x -> x, createLinkedHashMap, toLinkedList),
		                       is(singletonMap(1, Lists.of(1)))));
		twice(() -> assertThat(_12.groupBy(x -> x, createLinkedHashMap, toLinkedList),
		                       is(Maps.builder()
		                              .put(1, Lists.of(1))
		                              .put(2, Lists.of(2))
		                              .build())));

		twice(() -> assertThat(empty.groupBy(x -> x / 3, createLinkedHashMap, toLinkedList),
		                       is(emptyMap())));
		twice(() -> assertThat(_1.groupBy(x -> x / 3, createLinkedHashMap, toLinkedList),
		                       is(singletonMap(0, Lists.of(1)))));
		twice(() -> assertThat(_12.groupBy(x -> x / 3, createLinkedHashMap, toLinkedList),
		                       is(singletonMap(0, Lists.of(1, 2)))));
		twice(() -> assertThat(_12345.groupBy(x -> x / 3, createLinkedHashMap, toLinkedList),
		                       is(Maps.builder()
		                              .put(0, Lists.of(1, 2))
		                              .put(1, Lists.of(3, 4, 5))
		                              .build())));

		twice(() -> {
			Map<Integer, List<Integer>> map = _123456789.groupBy(i -> i % 3 == 0 ? null : i % 3, LinkedHashMap::new,
			                                                     toLinkedList);

			assertThat(map, is(instanceOf(LinkedHashMap.class)));
			assertThat(map, is(Maps.builder()
			                       .put(1, Lists.of(1, 4, 7))
			                       .put(2, Lists.of(2, 5, 8))
			                       .put(null, Lists.of(3, 6, 9))
			                       .build()));

			assertThat(map.get(1), is(instanceOf(LinkedList.class)));
			assertThat(map.get(2), is(instanceOf(LinkedList.class)));
			assertThat(map.get(null), is(instanceOf(LinkedList.class)));

			// check order
			assertThat(map.keySet(), contains(1, 2, null));
			//noinspection unchecked
			assertThat(map.values(), contains(contains(1, 4, 7), contains(2, 5, 8), contains(3, 6, 9)));
		});
	}

	@Test
	public void groupByWithMapConstructorAndCollectorWithFinisher() {
		Supplier<Map<Integer, String>> createLinkedHashMap = LinkedHashMap::new;
		Collector<Integer, StringBuilder, String> toStringWithBuilder = new SequentialCollector<>(
				StringBuilder::new, StringBuilder::append, StringBuilder::toString);

		twice(() -> assertThat(empty.groupBy(x -> x, createLinkedHashMap, toStringWithBuilder),
		                       is(emptyMap())));
		twice(() -> assertThat(_1.groupBy(x -> x, createLinkedHashMap, toStringWithBuilder),
		                       is(singletonMap(1, "1"))));
		twice(() -> assertThat(_12.groupBy(x -> x, createLinkedHashMap, toStringWithBuilder),
		                       is(Maps.builder()
		                              .put(1, "1")
		                              .put(2, "2")
		                              .build())));

		twice(() -> assertThat(empty.groupBy(x -> x / 3, createLinkedHashMap, toStringWithBuilder),
		                       is(emptyMap())));
		twice(() -> assertThat(_1.groupBy(x -> x / 3, createLinkedHashMap, toStringWithBuilder),
		                       is(singletonMap(0, "1"))));
		twice(() -> assertThat(_12.groupBy(x -> x / 3, createLinkedHashMap, toStringWithBuilder),
		                       is(singletonMap(0, "12"))));
		twice(() -> assertThat(_12345.groupBy(x -> x / 3, createLinkedHashMap, toStringWithBuilder),
		                       is(Maps.builder()
		                              .put(0, "12")
		                              .put(1, "345")
		                              .build())));

		twice(() -> {
			Map<Integer, String> map = _123456789.groupBy(x -> x % 3 == 0 ? null : x % 3, LinkedHashMap::new,
			                                              toStringWithBuilder);

			assertThat(map, is(instanceOf(LinkedHashMap.class)));
			assertThat(map, is(Maps.builder().put(1, "147").put(2, "258").put(null, "369").build()));

			// check order
			assertThat(map.keySet(), contains(1, 2, null));
			//noinspection unchecked
			assertThat(map.values(), contains("147", "258", "369"));
		});
	}

	@Test
	public void toSortedMap() {
		Map<String, Integer> original = Maps.builder("3", 3).put("1", 1).put("4", 4).put("2", 2).build();

		Sequence<Entry<String, Integer>> sequence = Sequence.from(original);

		twice(() -> {
			Map<String, Integer> map = sequence.toSortedMap();
			assertThat(map, is(instanceOf(TreeMap.class)));
			assertThat(map, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build())));
		});
	}

	@Test
	public void toSortedMapWithMappers() {
		twice(() -> {
			SortedMap<String, Integer> sortedMap = threeRandom.toSortedMap(Object::toString, Function.identity());

			assertThat(sortedMap, is(instanceOf(TreeMap.class)));
			assertThat(sortedMap, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).build())));
		});
	}

	@Test
	public void toSortedMapWithNullComparator() {
		Map<String, Integer> original = Maps.builder("3", 3).put("1", 1).put("4", 4).put("2", 2).build();

		Sequence<Entry<String, Integer>> sequence = Sequence.from(original);

		twice(() -> {
			Map<String, Integer> map = sequence.toSortedMap(null);
			assertThat(map, is(instanceOf(TreeMap.class)));
			assertThat(map, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build())));
		});
	}

	@Test
	public void toSortedMapWithNullComparatorAndMappers() {
		twice(() -> {
			SortedMap<String, Integer> sortedMap = threeRandom.toSortedMap(null,
			                                                               Object::toString, Function.identity());

			assertThat(sortedMap, is(instanceOf(TreeMap.class)));
			assertThat(sortedMap, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).build())));
		});
	}

	@Test
	public void toSortedMapWithComparator() {
		Map<String, Integer> original = Maps.builder("3", 3).put("1", 1).put("4", 4).put("2", 2).build();

		Sequence<Entry<String, Integer>> sequence = Sequence.from(original);

		twice(() -> {
			Map<String, Integer> map = sequence.toSortedMap(reverseOrder());
			assertThat(map, is(instanceOf(TreeMap.class)));
			assertThat(map, is(equalTo(Maps.builder("4", 4).put("3", 3).put("2", 2).put("1", 1).build())));
		});
	}

	@Test
	public void toSortedMapWithComparatorAndMappers() {
		twice(() -> {
			SortedMap<String, Integer> sortedMap = threeRandom.toSortedMap(reverseOrder(),
			                                                               Object::toString, Function.identity());

			assertThat(sortedMap, is(instanceOf(TreeMap.class)));
			assertThat(sortedMap, is(equalTo(Maps.builder("3", 3).put("2", 2).put("1", 1).build())));
		});
	}

	@Test
	public void collect() {
		twice(() -> {
			Deque<Integer> deque = _12345.collect(ArrayDeque::new, ArrayDeque::add);

			assertThat(deque, is(instanceOf(ArrayDeque.class)));
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
		twice(() -> assertThat(_12345.toArray(Number[]::new), arrayContaining(1, 2, 3, 4, 5)));
	}

	@Test
	public void toArrayWithExistingArray() {
		twice(() -> assertThat(_12345.toArray(new Integer[0]), arrayContaining(1, 2, 3, 4, 5)));
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
	public void fold() {
		twice(() -> {
			assertThat(empty.fold("", (s, i) -> s + i), is(""));
			assertThat(_1.fold("", (s, i) -> s + i), is("1"));
			assertThat(_12.fold("", (s, i) -> s + i), is("12"));
			assertThat(_12345.fold("", (s, i) -> s + i), is("12345"));
		});
	}

	@Test
	public void arbitrary() {
		twice(() -> {
			assertThat(empty.arbitrary(), is(Optional.empty()));
			assertThat(_1.arbitrary(), is(Optional.of(1)));
			assertThat(_12.arbitrary().get(),
			           is(both(greaterThanOrEqualTo(1)).and(lessThanOrEqualTo(2))));
			assertThat(_12345.arbitrary().get(),
			           is(both(greaterThanOrEqualTo(1)).and(lessThanOrEqualTo(5))));
			assertThat(mutableFive.arbitrary(), is(Optional.of(1)));
		});
	}

	@Test
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(Optional.empty()));
			assertThat(_1.first(), is(Optional.of(1)));
			assertThat(_12.first(), is(Optional.of(1)));
			assertThat(_12345.first(), is(Optional.of(1)));
			assertThat(mutableFive.first(), is(Optional.of(1)));
		});
	}

	@Test
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(Optional.empty()));
			assertThat(_1.last(), is(Optional.of(1)));
			assertThat(_12.last(), is(Optional.of(2)));
			assertThat(_12345.last(), is(Optional.of(5)));
			assertThat(mutableFive.last(), is(Optional.of(5)));
		});
	}

	@Test
	public void at() {
		twice(() -> {
			assertThat(empty.at(0), is(Optional.empty()));
			assertThat(empty.at(17), is(Optional.empty()));

			assertThat(_1.at(0), is(Optional.of(1)));
			assertThat(_1.at(1), is(Optional.empty()));
			assertThat(_1.at(17), is(Optional.empty()));

			assertThat(_12345.at(0), is(Optional.of(1)));
			assertThat(_12345.at(1), is(Optional.of(2)));
			assertThat(_12345.at(4), is(Optional.of(5)));
			assertThat(_12345.at(17), is(Optional.empty()));

			assertThat(mutableFive.at(0), is(Optional.of(1)));
			assertThat(mutableFive.at(1), is(Optional.of(2)));
			assertThat(mutableFive.at(4), is(Optional.of(5)));
			assertThat(mutableFive.at(17), is(Optional.empty()));
		});
	}

	@Test
	public void arbitraryByPredicate() {
		twice(() -> {
			assertThat(empty.arbitrary(x -> x > 1), is(Optional.empty()));
			assertThat(_1.arbitrary(x -> x > 1), is(Optional.empty()));
			assertThat(_12.arbitrary(x -> x > 1), is(Optional.of(2)));
			assertThat(_12345.arbitrary(x -> x > 1).get(),
			           is(both(greaterThanOrEqualTo(2)).and(lessThanOrEqualTo(5))));
			assertThat(mutableFive.arbitrary(x -> x > 1), is(Optional.of(2)));
		});
	}

	@Test
	public void firstByPredicate() {
		twice(() -> {
			assertThat(empty.first(x -> x > 1), is(Optional.empty()));
			assertThat(_1.first(x -> x > 1), is(Optional.empty()));
			assertThat(_12.first(x -> x > 1), is(Optional.of(2)));
			assertThat(_12345.first(x -> x > 1), is(Optional.of(2)));
			assertThat(mutableFive.first(x -> x > 1), is(Optional.of(2)));
		});
	}

	@Test
	public void lastByPredicate() {
		twice(() -> {
			assertThat(empty.last(x -> x > 1), is(Optional.empty()));
			assertThat(_1.last(x -> x > 1), is(Optional.empty()));
			assertThat(_12.last(x -> x > 1), is(Optional.of(2)));
			assertThat(_12345.last(x -> x > 1), is(Optional.of(5)));
			assertThat(mutableFive.last(x -> x > 1), is(Optional.of(5)));
		});
	}

	@Test
	public void atByPredicate() {
		twice(() -> {
			assertThat(empty.at(0, x -> x > 1), is(Optional.empty()));
			assertThat(empty.at(17, x -> x > 1), is(Optional.empty()));

			assertThat(_1.at(0, x -> x > 1), is(Optional.empty()));
			assertThat(_1.at(17, x -> x > 1), is(Optional.empty()));

			assertThat(_12.at(0, x -> x > 1), is(Optional.of(2)));
			assertThat(_12.at(1, x -> x > 1), is(Optional.empty()));
			assertThat(_12.at(17, x -> x > 1), is(Optional.empty()));

			assertThat(_12345.at(0, x -> x > 1), is(Optional.of(2)));
			assertThat(_12345.at(1, x -> x > 1), is(Optional.of(3)));
			assertThat(_12345.at(3, x -> x > 1), is(Optional.of(5)));
			assertThat(_12345.at(4, x -> x > 1), is(Optional.empty()));
			assertThat(_12345.at(17, x -> x > 1), is(Optional.empty()));

			assertThat(mutableFive.at(0, x -> x > 1), is(Optional.of(2)));
			assertThat(mutableFive.at(1, x -> x > 1), is(Optional.of(3)));
			assertThat(mutableFive.at(3, x -> x > 1), is(Optional.of(5)));
			assertThat(mutableFive.at(4, x -> x > 1), is(Optional.empty()));
			assertThat(mutableFive.at(17, x -> x > 1), is(Optional.empty()));
		});
	}

	@Test
	public void arbitraryByClass() {
		twice(() -> {
			assertThat(mixed.arbitrary(Long.class), is(Optional.empty()));
			assertThat(mixed.arbitrary(String.class), is(Optional.of("1")));
			assertThat(mixed.arbitrary(Number.class), is(Optional.of(1)));
			assertThat(mixed.arbitrary(Integer.class), is(Optional.of(1)));
			assertThat(mixed.arbitrary(Double.class), is(Optional.of(1.0)));
		});
	}

	@Test
	public void firstByClass() {
		twice(() -> {
			assertThat(mixed.first(Long.class), is(Optional.empty()));
			assertThat(mixed.first(String.class), is(Optional.of("1")));
			assertThat(mixed.first(Number.class), is(Optional.of(1)));
			assertThat(mixed.first(Integer.class), is(Optional.of(1)));
			assertThat(mixed.first(Double.class), is(Optional.of(1.0)));
		});
	}

	@Test
	public void lastByClass() {
		twice(() -> {
			assertThat(mixed.last(Long.class), is(Optional.empty()));
			assertThat(mixed.last(String.class), is(Optional.of("3")));
			assertThat(mixed.last(Number.class), is(Optional.of(3.0)));
			assertThat(mixed.last(Integer.class), is(Optional.of(3)));
			assertThat(mixed.last(Double.class), is(Optional.of(3.0)));
		});
	}

	@Test
	public void atByClass() {
		twice(() -> {
			assertThat(mixed.at(0, Long.class), is(Optional.empty()));
			assertThat(mixed.at(1, Long.class), is(Optional.empty()));

			assertThat(mixed.at(0, String.class), is(Optional.of("1")));
			assertThat(mixed.at(1, String.class), is(Optional.of("2")));
			assertThat(mixed.at(2, String.class), is(Optional.of("3")));
			assertThat(mixed.at(3, String.class), is(Optional.empty()));

			assertThat(mixed.at(0, Number.class), is(Optional.of(1)));
			assertThat(mixed.at(1, Number.class), is(Optional.of(1.0)));
			assertThat(mixed.at(2, Number.class), is(Optional.of(2)));
			assertThat(mixed.at(3, Number.class), is(Optional.of(2.0)));
			assertThat(mixed.at(4, Number.class), is(Optional.of(3)));
			assertThat(mixed.at(5, Number.class), is(Optional.of(3.0)));
			assertThat(mixed.at(6, Number.class), is(Optional.empty()));
		});
	}

	@Test
	public void removeArbitrary() {
		assertThat(mutableFive.removeArbitrary().get(),
		           is(both(greaterThanOrEqualTo(1)).and(lessThanOrEqualTo(5))));
		assertThat(mutableFive.size(), is(4));

		assertThat(mutableFive.removeArbitrary().get(),
		           is(both(greaterThanOrEqualTo(1)).and(lessThanOrEqualTo(5))));
		assertThat(mutableFive.size(), is(3));
	}

	@Test
	public void removeFirst() {
		assertThat(mutableFive.removeFirst(Integer.class), is(Optional.of(1)));
		assertThat(mutableFive, contains(2, 3, 4, 5));

		assertThat(mutableFive.removeFirst(Integer.class), is(Optional.of(2)));
		assertThat(mutableFive, contains(3, 4, 5));
	}

	@Test
	public void removeLast() {
		assertThat(mutableFive.removeLast(), is(Optional.of(5)));
		assertThat(mutableFive, contains(1, 2, 3, 4));

		assertThat(mutableFive.removeLast(), is(Optional.of(4)));
		assertThat(mutableFive, contains(1, 2, 3));
	}

	@Test
	public void removeAt() {
		assertThat(mutableFive.removeAt(0), is(Optional.of(1)));
		assertThat(mutableFive, contains(2, 3, 4, 5));

		assertThat(mutableFive.removeAt(1), is(Optional.of(3)));
		assertThat(mutableFive, contains(2, 4, 5));

		assertThat(mutableFive.removeAt(2), is(Optional.of(5)));
		assertThat(mutableFive, contains(2, 4));

		assertThat(mutableFive.removeAt(3), is(Optional.empty()));
		assertThat(mutableFive, contains(2, 4));
	}

	@Test
	public void removeArbitraryByPredicate() {
		assertThat(mutableFive.removeArbitrary(x -> x > 5), is(Optional.empty()));
		assertThat(mutableFive, contains(1, 2, 3, 4, 5));

		assertThat(mutableFive.removeArbitrary(x -> x > 1).get(),
		           is(both(greaterThanOrEqualTo(2)).and(lessThanOrEqualTo(5))));
		assertThat(mutableFive.size(), is(4));

		assertThat(mutableFive.removeArbitrary(x -> x > 1).get(),
		           is(both(greaterThanOrEqualTo(2)).and(lessThanOrEqualTo(5))));
		assertThat(mutableFive.size(), is(3));
	}

	@Test
	public void removeFirstByPredicate() {
		assertThat(mutableFive.removeFirst(Double.class), is(Optional.empty()));
		assertThat(mutableFive, contains(1, 2, 3, 4, 5));

		assertThat(mutableFive.removeFirst(Integer.class), is(Optional.of(1)));
		assertThat(mutableFive, contains(2, 3, 4, 5));

		assertThat(mutableFive.removeFirst(Integer.class), is(Optional.of(2)));
		assertThat(mutableFive, contains(3, 4, 5));
	}

	@Test
	public void removeLastByPredicate() {
		assertThat(mutableFive.removeLast(Double.class), is(Optional.empty()));
		assertThat(mutableFive, contains(1, 2, 3, 4, 5));

		assertThat(mutableFive.removeLast(Integer.class), is(Optional.of(5)));
		assertThat(mutableFive, contains(1, 2, 3, 4));

		assertThat(mutableFive.removeLast(Integer.class), is(Optional.of(4)));
		assertThat(mutableFive, contains(1, 2, 3));
	}

	@Test
	public void removeAtByPredicate() {
		assertThat(mutableFive.removeAt(0, x -> x > 5), is(Optional.empty()));
		assertThat(mutableFive, contains(1, 2, 3, 4, 5));

		assertThat(mutableFive.removeAt(0, x -> x > 1), is(Optional.of(2)));
		assertThat(mutableFive, contains(1, 3, 4, 5));

		assertThat(mutableFive.removeAt(1, x -> x > 1), is(Optional.of(4)));
		assertThat(mutableFive, contains(1, 3, 5));

		assertThat(mutableFive.removeAt(2, x -> x > 1), is(Optional.empty()));
		assertThat(mutableFive, contains(1, 3, 5));
	}

	@Test
	public void removeArbitraryByClass() {
		assertThat(mutableFive.removeArbitrary(Double.class), is(Optional.empty()));
		assertThat(mutableFive, contains(1, 2, 3, 4, 5));

		assertThat(mutableFive.removeArbitrary(Integer.class).get(),
		           is(both(greaterThanOrEqualTo(1)).and(lessThanOrEqualTo(5))));
		assertThat(mutableFive.size(), is(4));

		assertThat(mutableFive.removeArbitrary(Integer.class).get(),
		           is(both(greaterThanOrEqualTo(1)).and(lessThanOrEqualTo(5))));
		assertThat(mutableFive.size(), is(3));
	}

	@Test
	public void removeFirstByClass() {
		assertThat(mutableFive.removeFirst(Double.class), is(Optional.empty()));
		assertThat(mutableFive, contains(1, 2, 3, 4, 5));

		assertThat(mutableFive.removeFirst(Integer.class), is(Optional.of(1)));
		assertThat(mutableFive, contains(2, 3, 4, 5));

		assertThat(mutableFive.removeFirst(Integer.class), is(Optional.of(2)));
		assertThat(mutableFive, contains(3, 4, 5));
	}

	@Test
	public void removeLastByClass() {
		assertThat(mutableFive.removeLast(Double.class), is(Optional.empty()));
		assertThat(mutableFive, contains(1, 2, 3, 4, 5));

		assertThat(mutableFive.removeLast(Integer.class), is(Optional.of(5)));
		assertThat(mutableFive, contains(1, 2, 3, 4));

		assertThat(mutableFive.removeLast(Integer.class), is(Optional.of(4)));
		assertThat(mutableFive, contains(1, 2, 3));
	}

	@Test
	public void removeAtByClass() {
		assertThat(mutableFive.removeAt(0, Double.class), is(Optional.empty()));
		assertThat(mutableFive, contains(1, 2, 3, 4, 5));

		assertThat(mutableFive.removeAt(0, Integer.class), is(Optional.of(1)));
		assertThat(mutableFive, contains(2, 3, 4, 5));

		assertThat(mutableFive.removeAt(1, Integer.class), is(Optional.of(3)));
		assertThat(mutableFive, contains(2, 4, 5));

		assertThat(mutableFive.removeAt(2, Integer.class), is(Optional.of(5)));
		assertThat(mutableFive, contains(2, 4));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void entries() {
		Sequence<Entry<Integer, Integer>> emptyEntries = empty.entries();
		twice(() -> assertThat(emptyEntries, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyEntries.iterator().next());

		Sequence<Entry<Integer, Integer>> oneEntries = _1.entries();
		twice(() -> assertThat(oneEntries, containsFixed(Maps.entry(1, null))));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(oneEntries));
		twice(() -> assertThat(oneEntries, containsFixed(Maps.entry(1, null))));

		Sequence<Entry<Integer, Integer>> twoEntries = _12.entries();
		twice(() -> assertThat(twoEntries, containsFixed(Maps.entry(1, 2))));

		Sequence<Entry<Integer, Integer>> threeEntries = _123.entries();
		twice(() -> assertThat(threeEntries, containsFixed(Maps.entry(1, 2), Maps.entry(2, 3))));

		Sequence<Entry<Integer, Integer>> fiveEntries = _12345.entries();
		twice(() -> assertThat(fiveEntries,
		                       containsFixed(Maps.entry(1, 2), Maps.entry(2, 3), Maps.entry(3, 4), Maps.entry(4, 5))));

		Sequence<Entry<Integer, Integer>> sizePassThroughEntries = sizePassThrough.entries();
		twice(() -> assertThat(sizePassThroughEntries.size(), is(9)));
		twice(() -> assertThat(sizePassThroughEntries.isEmpty(), is(false)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void pairs() {
		Sequence<Pair<Integer, Integer>> emptyPaired = empty.pairs();
		twice(() -> assertThat(emptyPaired, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyPaired.iterator().next());

		Sequence<Pair<Integer, Integer>> onePaired = _1.pairs();
		twice(() -> assertThat(onePaired, containsFixed(Pair.of(1, null))));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(onePaired));
		twice(() -> assertThat(onePaired, containsFixed(Pair.of(1, null))));

		Sequence<Pair<Integer, Integer>> twoPaired = _12.pairs();
		twice(() -> assertThat(twoPaired, containsFixed(Pair.of(1, 2))));

		Sequence<Pair<Integer, Integer>> threePaired = _123.pairs();
		twice(() -> assertThat(threePaired, containsFixed(Pair.of(1, 2), Pair.of(2, 3))));

		Sequence<Pair<Integer, Integer>> fivePaired = _12345.pairs();
		twice(() -> assertThat(fivePaired, containsFixed(Pair.of(1, 2), Pair.of(2, 3), Pair.of(3, 4), Pair.of(4, 5))));

		Sequence<Pair<Integer, Integer>> sizePassThroughPaired = sizePassThrough.pairs();
		twice(() -> assertThat(sizePassThroughPaired.size(), is(9)));
		twice(() -> assertThat(sizePassThroughPaired.isEmpty(), is(false)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void adjacentEntries() {
		Sequence<Entry<Integer, Integer>> emptyEntries = empty.adjacentEntries();
		twice(() -> assertThat(emptyEntries, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyEntries.iterator().next());

		Sequence<Entry<Integer, Integer>> oneEntries = _1.adjacentEntries();
		twice(() -> assertThat(oneEntries, containsFixed(Maps.entry(1, null))));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(oneEntries));
		twice(() -> assertThat(oneEntries, containsFixed(Maps.entry(1, null))));

		Sequence<Entry<Integer, Integer>> twoEntries = _12.adjacentEntries();
		twice(() -> assertThat(twoEntries, containsFixed(Maps.entry(1, 2))));

		Sequence<Entry<Integer, Integer>> threeEntries = _123.adjacentEntries();
		twice(() -> assertThat(threeEntries, containsFixed(Maps.entry(1, 2), Maps.entry(3, null))));

		Sequence<Entry<Integer, Integer>> fourEntries = _1234.adjacentEntries();
		twice(() -> assertThat(fourEntries, containsFixed(Maps.entry(1, 2), Maps.entry(3, 4))));

		Sequence<Entry<Integer, Integer>> fiveEntries = _12345.adjacentEntries();
		twice(() -> assertThat(fiveEntries, containsFixed(Maps.entry(1, 2), Maps.entry(3, 4), Maps.entry(5, null))));

		Sequence<Entry<Integer, Integer>> sizePassThroughEntries = sizePassThrough.adjacentEntries();
		twice(() -> assertThat(sizePassThroughEntries.size(), is(5)));
		twice(() -> assertThat(sizePassThroughEntries.isEmpty(), is(false)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void adjacentPairs() {
		Sequence<Pair<Integer, Integer>> emptyPaired = empty.adjacentPairs();
		twice(() -> assertThat(emptyPaired, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyPaired.iterator().next());

		Sequence<Pair<Integer, Integer>> onePaired = _1.adjacentPairs();
		twice(() -> assertThat(onePaired, containsFixed(Pair.of(1, null))));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(onePaired));
		twice(() -> assertThat(onePaired, containsFixed(Pair.of(1, null))));

		Sequence<Pair<Integer, Integer>> twoPaired = _12.adjacentPairs();
		twice(() -> assertThat(twoPaired, containsFixed(Pair.of(1, 2))));

		Sequence<Pair<Integer, Integer>> threePaired = _123.adjacentPairs();
		twice(() -> assertThat(threePaired, containsFixed(Pair.of(1, 2), Pair.of(3, null))));

		Sequence<Pair<Integer, Integer>> fourPaired = _1234.adjacentPairs();
		twice(() -> assertThat(fourPaired, containsFixed(Pair.of(1, 2), Pair.of(3, 4))));

		Sequence<Pair<Integer, Integer>> fivePaired = _12345.adjacentPairs();
		twice(() -> assertThat(fivePaired, containsFixed(Pair.of(1, 2), Pair.of(3, 4), Pair.of(5, null))));

		Sequence<Pair<Integer, Integer>> sizePassThroughPaired = sizePassThrough.adjacentPairs();
		twice(() -> assertThat(sizePassThroughPaired.size(), is(5)));
		twice(() -> assertThat(sizePassThroughPaired.isEmpty(), is(false)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void biSequence() {
		BiSequence<Integer, String> emptyBiSequence = empty.toBiSequence();
		twice(() -> assertThat(emptyBiSequence, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyBiSequence.iterator().next());

		BiSequence<Integer, String> oneBiSequence = newSequence(Pair.of(1, "1")).toBiSequence();
		twice(() -> assertThat(oneBiSequence, containsFixed(Pair.of(1, "1"))));

		BiSequence<Integer, String> twoBiSequence = newSequence(Pair.of(1, "1"), Pair.of(2, "2")).toBiSequence();
		twice(() -> assertThat(twoBiSequence, containsFixed(Pair.of(1, "1"), Pair.of(2, "2"))));

		BiSequence<Integer, String> threeBiSequence = newSequence(Pair.of(1, "1"), Pair.of(2, "2"),
		                                                          Pair.of(3, "3")).toBiSequence();
		twice(() -> assertThat(threeBiSequence, containsFixed(Pair.of(1, "1"), Pair.of(2, "2"), Pair.of(3, "3"))));

		BiSequence<Integer, String> mutableBiSequence = newMutableSequence(Pair.of(1, "1")).toBiSequence();
		assertThat(Tests.removeFirst(mutableBiSequence), is(Pair.of(1, "1")));
		twice(() -> assertThat(mutableBiSequence, is(emptySizedIterable())));

		BiSequence<Integer, Integer> sizePassThroughBiSequence = sizePassThrough.toBiSequence();
		twice(() -> assertThat(sizePassThroughBiSequence.size(), is(10)));
		twice(() -> assertThat(sizePassThroughBiSequence.isEmpty(), is(false)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void entrySequence() {
		EntrySequence<Integer, String> emptyEntrySequence = empty.toEntrySequence();
		twice(() -> assertThat(emptyEntrySequence, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyEntrySequence.iterator().next());

		EntrySequence<Integer, String> oneEntrySequence = newSequence(Maps.entry(1, "1")).toEntrySequence();
		twice(() -> assertThat(oneEntrySequence, containsFixed(Maps.entry(1, "1"))));

		EntrySequence<Integer, String> twoEntrySequence = newSequence(Maps.entry(1, "1"),
		                                                              Maps.entry(2, "2")).toEntrySequence();
		twice(() -> assertThat(twoEntrySequence, containsFixed(Maps.entry(1, "1"), Maps.entry(2, "2"))));

		EntrySequence<Integer, String> threeEntrySequence = newSequence(Maps.entry(1, "1"), Maps.entry(2, "2"),
		                                                                Maps.entry(3, "3")).toEntrySequence();
		twice(() -> assertThat(threeEntrySequence,
		                       containsFixed(Maps.entry(1, "1"), Maps.entry(2, "2"), Maps.entry(3, "3"))));

		EntrySequence<Integer, String> mutableEntrySequence = newMutableSequence(Maps.entry(1, "1")).toEntrySequence();
		assertThat(Tests.removeFirst(mutableEntrySequence), is(Maps.entry(1, "1")));
		twice(() -> assertThat(mutableEntrySequence, is(emptySizedIterable())));

		EntrySequence<Integer, Integer> sizePassThroughEntrySequence = sizePassThrough.toEntrySequence();
		twice(() -> assertThat(sizePassThroughEntrySequence.size(), is(10)));
		twice(() -> assertThat(sizePassThroughEntrySequence.isEmpty(), is(false)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void window() {
		Sequence<Sequence<Integer>> emptyWindowed = empty.window(3);
		twice(() -> assertThat(emptyWindowed, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<Sequence<Integer>> oneWindowed = _1.window(3);
		twice(() -> assertThat(oneWindowed, containsFixed(containsSized(1))));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, containsFixed(containsSized(1))));

		Sequence<Sequence<Integer>> twoWindowed = _12.window(3);
		twice(() -> assertThat(twoWindowed, containsFixed(containsSized(1, 2))));

		Sequence<Sequence<Integer>> threeWindowed = _123.window(3);
		twice(() -> assertThat(threeWindowed, containsFixed(containsSized(1, 2, 3))));

		Sequence<Sequence<Integer>> fourWindowed = _1234.window(3);
		twice(() -> assertThat(fourWindowed, containsFixed(containsSized(1, 2, 3), containsSized(2, 3, 4))));

		Sequence<Sequence<Integer>> fiveWindowed = _12345.window(3);
		twice(() -> assertThat(fiveWindowed,
		                       containsFixed(containsSized(1, 2, 3), containsSized(2, 3, 4), containsSized(3, 4, 5))));

		Sequence<Sequence<Integer>> sizePassThroughWindowed = sizePassThrough.window(3);
		twice(() -> assertThat(sizePassThroughWindowed.size(), is(8)));
		twice(() -> assertThat(sizePassThroughWindowed.isEmpty(), is(false)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithSmallerStep() {
		Sequence<Sequence<Integer>> emptyWindowed = empty.window(3, 2);
		twice(() -> assertThat(emptyWindowed, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<Sequence<Integer>> oneWindowed = _1.window(3, 2);
		twice(() -> assertThat(oneWindowed, containsUnsized(containsSized(1))));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, containsUnsized(containsSized(1))));

		Sequence<Sequence<Integer>> twoWindowed = _12.window(3, 2);
		twice(() -> assertThat(twoWindowed, containsUnsized(containsSized(1, 2))));

		Sequence<Sequence<Integer>> threeWindowed = _123.window(3, 2);
		twice(() -> assertThat(threeWindowed, containsUnsized(containsSized(1, 2, 3))));

		Sequence<Sequence<Integer>> fourWindowed = _1234.window(3, 2);
		twice(() -> assertThat(fourWindowed, containsUnsized(containsSized(1, 2, 3), containsSized(3, 4))));

		Sequence<Sequence<Integer>> fiveWindowed = _12345.window(3, 2);
		twice(() -> assertThat(fiveWindowed, containsUnsized(containsSized(1, 2, 3), containsSized(3, 4, 5))));

		Sequence<Sequence<Integer>> nineWindowed = _123456789.window(3, 2);
		twice(() -> assertThat(nineWindowed,
		                       containsUnsized(containsSized(1, 2, 3), containsSized(3, 4, 5), containsSized(5, 6, 7),
		                                       containsSized(7, 8, 9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithLargerStep() {
		Sequence<Sequence<Integer>> emptyWindowed = empty.window(3, 4);
		twice(() -> assertThat(emptyWindowed, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<Sequence<Integer>> oneWindowed = _1.window(3, 4);
		twice(() -> assertThat(oneWindowed, containsUnsized(containsSized(1))));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, containsUnsized(containsSized(1))));

		Sequence<Sequence<Integer>> twoWindowed = _12.window(3, 4);
		twice(() -> assertThat(twoWindowed, containsUnsized(containsSized(1, 2))));

		Sequence<Sequence<Integer>> threeWindowed = _123.window(3, 4);
		twice(() -> assertThat(threeWindowed, containsUnsized(containsSized(1, 2, 3))));

		Sequence<Sequence<Integer>> fourWindowed = _1234.window(3, 4);
		twice(() -> assertThat(fourWindowed, containsUnsized(containsSized(1, 2, 3))));

		Sequence<Sequence<Integer>> fiveWindowed = _12345.window(3, 4);
		twice(() -> assertThat(fiveWindowed, containsUnsized(containsSized(1, 2, 3), containsSized(5))));

		Sequence<Sequence<Integer>> nineWindowed = _123456789.window(3, 4);
		twice(() -> assertThat(nineWindowed,
		                       containsUnsized(containsSized(1, 2, 3), containsSized(5, 6, 7), containsSized(9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batch() {
		Sequence<Sequence<Integer>> emptyBatched = empty.batch(3);
		twice(() -> assertThat(emptyBatched, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<Sequence<Integer>> oneBatched = _1.batch(3);
		twice(() -> assertThat(oneBatched, containsFixed(containsSized(1))));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(oneBatched));
		twice(() -> assertThat(oneBatched, containsFixed(containsSized(1))));

		Sequence<Sequence<Integer>> twoBatched = _12.batch(3);
		twice(() -> assertThat(twoBatched, containsFixed(containsSized(1, 2))));

		Sequence<Sequence<Integer>> threeBatched = _123.batch(3);
		twice(() -> assertThat(threeBatched, containsFixed(containsSized(1, 2, 3))));

		Sequence<Sequence<Integer>> fourBatched = _1234.batch(3);
		twice(() -> assertThat(fourBatched, containsFixed(containsSized(1, 2, 3), containsSized(4))));

		Sequence<Sequence<Integer>> fiveBatched = _12345.batch(3);
		twice(() -> assertThat(fiveBatched, containsFixed(containsSized(1, 2, 3), containsSized(4, 5))));

		Sequence<Sequence<Integer>> nineBatched = _123456789.batch(3);
		twice(() -> assertThat(nineBatched,
		                       containsFixed(containsSized(1, 2, 3), containsSized(4, 5, 6), containsSized(7, 8, 9))));

		Sequence<Sequence<Integer>> sizePassThroughBatched = sizePassThrough.batch(3);
		twice(() -> assertThat(sizePassThroughBatched.size(), is(4)));
		twice(() -> assertThat(sizePassThroughBatched.isEmpty(), is(false)));

		Sequence<Sequence<Integer>> emptySizePassThroughBatched = emptySizePassThrough.batch(3);
		twice(() -> assertThat(emptySizePassThroughBatched.size(), is(0)));
		twice(() -> assertThat(emptySizePassThroughBatched.isEmpty(), is(true)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnPredicate() {
		Sequence<Sequence<Integer>> emptyBatched = empty.batch((a, b) -> a > b);
		twice(() -> assertThat(emptyBatched, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<Sequence<Integer>> oneBatched = _1.batch((a, b) -> a > b);
		twice(() -> assertThat(oneBatched, containsUnsized(containsSized(1))));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(oneBatched));
		twice(() -> assertThat(oneBatched, containsUnsized(containsSized(1))));

		Sequence<Sequence<Integer>> twoBatched = _12.batch((a, b) -> a > b);
		twice(() -> assertThat(twoBatched, containsUnsized(containsSized(1, 2))));

		Sequence<Sequence<Integer>> threeBatched = _123.batch((a, b) -> a > b);
		twice(() -> assertThat(threeBatched, containsUnsized(containsSized(1, 2, 3))));

		Sequence<Sequence<Integer>> threeRandomBatched = threeRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(threeRandomBatched, containsUnsized(containsSized(2, 3), containsSized(1))));

		Sequence<Sequence<Integer>> nineRandomBatched = nineRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(nineRandomBatched,
		                       containsUnsized(containsSized(67), containsSized(5, 43), containsSized(3, 5, 7, 24),
		                                       containsSized(5, 67))));

		Sequence<Sequence<Integer>> sizePassThroughBatched = sizePassThrough.batch((a, b) -> a > b);
		twice(() -> assertThat(sizePassThroughBatched.isEmpty(), is(false)));

		Sequence<Sequence<Integer>> emptySizePassThroughBatched = emptySizePassThrough.batch((a, b) -> a > b);
		twice(() -> assertThat(emptySizePassThroughBatched.isEmpty(), is(true)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitAroundElement() {
		Sequence<Sequence<Integer>> emptySplit = empty.split(3);
		twice(() -> assertThat(emptySplit, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

		Sequence<Sequence<Integer>> oneSplit = _1.split(3);
		twice(() -> assertThat(oneSplit, containsUnsized(containsSized(1))));

		Sequence<Sequence<Integer>> oneSplitOnFirst = _1.split(1);
		twice(() -> assertThat(oneSplitOnFirst, containsUnsized(emptySizedIterable())));

		Sequence<Sequence<Integer>> splitOnAll = newSequence(17, 17, 17).split(17);
		twice(() -> assertThat(splitOnAll, containsUnsized(emptySizedIterable(), emptySizedIterable(),
		                                                   emptySizedIterable())));

		Sequence<Sequence<Integer>> twoSplit = _12.split(3);
		twice(() -> assertThat(twoSplit, containsUnsized(containsSized(1, 2))));

		Sequence<Sequence<Integer>> twoSplitOnFirst = _12.split(1);
		twice(() -> assertThat(twoSplitOnFirst, containsUnsized(emptySizedIterable(), containsSized(2))));

		Sequence<Sequence<Integer>> threeSplit = _123.split(3);
		twice(() -> assertThat(threeSplit, containsUnsized(containsSized(1, 2))));

		Sequence<Sequence<Integer>> fiveSplit = _12345.split(3);
		twice(() -> assertThat(fiveSplit, containsUnsized(containsSized(1, 2), containsSized(4, 5))));

		Sequence<Sequence<Integer>> nineSplit = _123456789.split(3);
		twice(() -> assertThat(nineSplit, containsUnsized(containsSized(1, 2), containsSized(4, 5, 6, 7, 8, 9))));

		Sequence<Sequence<Integer>> mutableSplit = mutableFive.split(3);
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutableSplit));
		twice(() -> assertThat(mutableSplit, containsUnsized(containsSized(1, 2), containsSized(4, 5))));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitOnPredicate() {
		Sequence<Sequence<Integer>> emptySplit = empty.split(x -> x % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

		Sequence<Sequence<Integer>> oneSplit = _1.split(x -> x % 3 == 0);
		twice(() -> assertThat(oneSplit, containsUnsized(containsSized(1))));

		Sequence<Sequence<Integer>> oneSplitOnFirst = _1.split(x -> x % 3 == 1);
		twice(() -> assertThat(oneSplitOnFirst, containsUnsized(emptySizedIterable())));

		Sequence<Sequence<Integer>> splitOnAll = newSequence(17, 17, 17).split(x -> x == 17);
		twice(() -> assertThat(splitOnAll, containsUnsized(emptySizedIterable(), emptySizedIterable(),
		                                                   emptySizedIterable())));

		Sequence<Sequence<Integer>> twoSplit = _12.split(x -> x % 3 == 0);
		twice(() -> assertThat(twoSplit, containsUnsized(containsSized(1, 2))));

		Sequence<Sequence<Integer>> twoSplitOnFirst = _12.split(x -> x % 3 == 1);
		twice(() -> assertThat(twoSplitOnFirst, containsUnsized(emptySizedIterable(), containsSized(2))));

		Sequence<Sequence<Integer>> threeSplit = _123.split(x -> x % 3 == 0);
		twice(() -> assertThat(threeSplit, containsUnsized(containsSized(1, 2))));

		Sequence<Sequence<Integer>> fiveSplit = _12345.split(x -> x % 3 == 0);
		twice(() -> assertThat(fiveSplit, containsUnsized(containsSized(1, 2), containsSized(4, 5))));

		Sequence<Sequence<Integer>> nineSplit = _123456789.split(x -> x % 3 == 0);
		twice(() -> assertThat(nineSplit,
		                       containsUnsized(containsSized(1, 2), containsSized(4, 5), containsSized(7, 8))));

		Sequence<Sequence<Integer>> mutableSplit = mutableFive.split(x -> x % 3 == 0);
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutableSplit));
		twice(() -> assertThat(mutableSplit, containsUnsized(containsSized(1, 2), containsSized(4, 5))));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));
	}

	@Test
	public void step() {
		Sequence<Integer> emptyStep3 = empty.step(3);
		twice(() -> assertThat(emptyStep3, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyStep3.iterator().next());

		Sequence<Integer> oneStep3 = _1.step(3);
		twice(() -> assertThat(oneStep3, containsFixed(1)));

		Sequence<Integer> fourStep3 = _1234.step(3);
		twice(() -> assertThat(fourStep3, containsFixed(1, 4)));

		Sequence<Integer> nineStep3 = _123456789.step(3);
		twice(() -> assertThat(nineStep3, containsFixed(1, 4, 7)));

		Sequence<Integer> mutableStep2 = mutableFive.step(2);
		Iterator<Integer> mutableStep2Iterator = mutableStep2.iterator();
		expecting(IllegalStateException.class, mutableStep2Iterator::remove);
		assertThat(mutableStep2Iterator.hasNext(), is(true));
		expecting(IllegalStateException.class, mutableStep2Iterator::remove);
		assertThat(mutableStep2Iterator.next(), is(1));
		mutableStep2Iterator.remove();
		assertThat(mutableStep2Iterator.hasNext(), is(true));
		expecting(IllegalStateException.class, mutableStep2Iterator::remove);
		assertThat(mutableStep2Iterator.next(), is(3));
		mutableStep2Iterator.remove();

		twice(() -> assertThat(mutableStep2, containsSized(2, 5)));
		twice(() -> assertThat(mutableFive, containsSized(2, 4, 5)));

		Sequence<Integer> sizePassThroughStep3 = sizePassThrough.step(3);
		assertThat(sizePassThroughStep3.size(), is(4));
		assertThat(sizePassThroughStep3.isEmpty(), is(false));
	}

	@Test
	public void distinct() {
		Sequence<Integer> emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, is(emptyUnsizedIterable())));
		expecting(NoSuchElementException.class, () -> emptyDistinct.iterator().next());

		Sequence<Integer> oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, containsUnsized(17)));

		Sequence<Integer> twoDuplicatesDistinct = newSequence(17, 17).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, containsUnsized(17)));

		Sequence<Integer> nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, containsUnsized(67, 5, 43, 3, 7, 24)));

		Sequence<Integer> mutableDistinct = mutableFive.distinct();
		assertThat(Tests.removeFirst(mutableDistinct), is(1));
		twice(() -> assertThat(mutableDistinct, containsUnsized(2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<Integer> sizePassThroughDistinct = sizePassThrough.distinct();
		assertThat(sizePassThroughDistinct.isEmpty(), is(false));

		Sequence<Integer> emptySizePassThroughDistinct = emptySizePassThrough.distinct();
		assertThat(emptySizePassThroughDistinct.isEmpty(), is(true));
	}

	@Test
	public void sorted() {
		Sequence<Integer> emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptySorted.iterator().next());

		Sequence<Integer> oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, containsFixed(17)));

		Sequence<Integer> twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, containsFixed(17, 32)));

		Sequence<Integer> nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, containsFixed(3, 5, 5, 5, 7, 24, 43, 67, 67)));

		Sequence<Integer> mutableSorted = mutableFive.sorted();
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutableSorted));
		twice(() -> assertThat(mutableSorted, containsSized(1, 2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));

		Sequence<Integer> sizePassThroughSorted = sizePassThrough.sorted();
		assertThat(sizePassThroughSorted.size(), is(10));
		assertThat(sizePassThroughSorted.isEmpty(), is(false));
	}

	@Test
	public void sortedComparator() {
		Sequence<Integer> emptySorted = empty.sorted(reverseOrder());
		twice(() -> assertThat(emptySorted, emptyFixedIterable()));
		expecting(NoSuchElementException.class, () -> emptySorted.iterator().next());

		Sequence<Integer> oneSorted = oneRandom.sorted(reverseOrder());
		twice(() -> assertThat(oneSorted, containsFixed(17)));

		Sequence<Integer> twoSorted = twoRandom.sorted(reverseOrder());
		twice(() -> assertThat(twoSorted, containsFixed(32, 17)));

		Sequence<Integer> nineSorted = nineRandom.sorted(reverseOrder());
		twice(() -> assertThat(nineSorted, containsFixed(67, 67, 43, 24, 7, 5, 5, 5, 3)));

		Sequence<Integer> mutableSorted = mutableFive.sorted(reverseOrder());
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutableSorted));
		twice(() -> assertThat(mutableSorted, containsSized(5, 4, 3, 2, 1)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));

		Sequence<Integer> sizePassThroughSorted = sizePassThrough.sorted(reverseOrder());
		assertThat(sizePassThroughSorted.size(), is(10));
		assertThat(sizePassThroughSorted.isEmpty(), is(false));
	}

	@Test
	public void min() {
		twice(() -> assertThat(empty.min(), is(Optional.empty())));
		twice(() -> assertThat(oneRandom.min(), is(Optional.of(17))));
		twice(() -> assertThat(twoRandom.min(), is(Optional.of(17))));
		twice(() -> assertThat(nineRandom.min(), is(Optional.of(3))));
		twice(() -> assertThat(mutableFive.min(), is(Optional.of(1))));
	}

	@Test
	public void max() {
		twice(() -> assertThat(empty.max(), is(Optional.empty())));
		twice(() -> assertThat(oneRandom.max(), is(Optional.of(17))));
		twice(() -> assertThat(twoRandom.max(), is(Optional.of(32))));
		twice(() -> assertThat(nineRandom.max(), is(Optional.of(67))));
		twice(() -> assertThat(mutableFive.max(), is(Optional.of(5))));
	}

	@Test
	public void minByComparator() {
		twice(() -> assertThat(empty.min(reverseOrder()), is(Optional.empty())));
		twice(() -> assertThat(oneRandom.min(reverseOrder()), is(Optional.of(17))));
		twice(() -> assertThat(twoRandom.min(reverseOrder()), is(Optional.of(32))));
		twice(() -> assertThat(nineRandom.min(reverseOrder()), is(Optional.of(67))));
		twice(() -> assertThat(mutableFive.min(reverseOrder()), is(Optional.of(5))));
	}

	@Test
	public void maxByComparator() {
		twice(() -> assertThat(empty.max(reverseOrder()), is(Optional.empty())));
		twice(() -> assertThat(oneRandom.max(reverseOrder()), is(Optional.of(17))));
		twice(() -> assertThat(twoRandom.max(reverseOrder()), is(Optional.of(17))));
		twice(() -> assertThat(nineRandom.max(reverseOrder()), is(Optional.of(3))));
		twice(() -> assertThat(mutableFive.max(reverseOrder()), is(Optional.of(1))));
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
	public void anyInstanceOf() {
		twice(() -> assertThat(mixed.any(String.class), is(true)));
		twice(() -> assertThat(mixed.any(Number.class), is(true)));
		twice(() -> assertThat(mixed.any(Integer.class), is(true)));
		twice(() -> assertThat(mixed.any(Double.class), is(true)));
		twice(() -> assertThat(mixed.any(Character.class), is(true)));
		twice(() -> assertThat(mixed.any(Object.class), is(true)));
		twice(() -> assertThat(mixed.any(Long.class), is(false)));
	}

	@Test
	public void allInstanceOf() {
		twice(() -> assertThat(mixed.all(String.class), is(false)));
		twice(() -> assertThat(mixed.all(Number.class), is(false)));
		twice(() -> assertThat(mixed.all(Integer.class), is(false)));
		twice(() -> assertThat(mixed.all(Double.class), is(false)));
		twice(() -> assertThat(mixed.all(Character.class), is(false)));
		twice(() -> assertThat(mixed.all(Object.class), is(true)));
		twice(() -> assertThat(mixed.all(Long.class), is(false)));
	}

	@Test
	public void noneInstanceOf() {
		twice(() -> assertThat(mixed.none(String.class), is(false)));
		twice(() -> assertThat(mixed.none(Number.class), is(false)));
		twice(() -> assertThat(mixed.none(Integer.class), is(false)));
		twice(() -> assertThat(mixed.none(Double.class), is(false)));
		twice(() -> assertThat(mixed.none(Character.class), is(false)));
		twice(() -> assertThat(mixed.none(Object.class), is(false)));
		twice(() -> assertThat(mixed.none(Long.class), is(true)));
	}

	@Test
	public void stream() {
		twice(() -> assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable())));
		twice(() -> assertThat(empty, is(emptyFixedIterable())));

		twice(() -> assertThat(_12345.stream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5)));
		twice(() -> assertThat(_12345, containsFixed(1, 2, 3, 4, 5)));
	}

	@Test
	public void parallelStream() {
		twice(() -> assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable())));
		twice(() -> assertThat(empty, is(emptyFixedIterable())));

		twice(() -> assertThat(_12345.parallelStream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5)));
		twice(() -> assertThat(_12345, containsFixed(1, 2, 3, 4, 5)));
	}

	@Test
	public void spliterator() {
		twice(() -> assertThat(StreamSupport.stream(empty.spliterator(), false).collect(Collectors.toList()),
		                       is(emptyIterable())));
		twice(() -> assertThat(empty, is(emptyFixedIterable())));

		twice(() -> assertThat(StreamSupport.stream(_12345.spliterator(), false).collect(Collectors.toList()),
		                       contains(1, 2, 3, 4, 5)));
		twice(() -> assertThat(_12345, containsFixed(1, 2, 3, 4, 5)));
	}

	@Test
	public void streamFromOnce() {
		Sequence<Integer> empty = Sequence.once(Iterators.empty());
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));

		Sequence<Integer> sequence = Sequence.once(Iterators.of(1, 2, 3, 4, 5));
		assertThat(sequence.stream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5));
		assertThat(sequence.stream().collect(Collectors.toList()), is(emptyIterable()));
	}

	@Test
	public void delimit() {
		Sequence<?> emptyDelimited = empty.delimit(", ");
		twice(() -> assertThat(emptyDelimited, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyDelimited.iterator().next());

		Sequence<?> oneDelimited = _1.delimit(", ");
		twice(() -> assertThat(oneDelimited, containsFixed(1)));

		Iterator<?> oneIterator = oneDelimited.iterator();
		oneIterator.next();
		expecting(UnsupportedOperationException.class, oneIterator::remove);

		Sequence<?> twoDelimited = _12.delimit(", ");
		twice(() -> assertThat(twoDelimited, containsFixed(1, ", ", 2)));

		Sequence<?> threeDelimited = _123.delimit(", ");
		twice(() -> assertThat(threeDelimited, containsFixed(1, ", ", 2, ", ", 3)));

		Sequence<?> fourDelimited = _1234.delimit(", ");
		twice(() -> assertThat(fourDelimited, containsFixed(1, ", ", 2, ", ", 3, ", ", 4)));

		Sequence<?> mutableDelimited = mutableFive.delimit(", ");
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutableDelimited));
		twice(() -> assertThat(mutableDelimited, containsSized(1, ", ", 2, ", ", 3, ", ", 4, ", ", 5)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));

		Sequence<Integer> sizePassThroughDelimited = sizePassThrough.delimit(", ");
		assertThat(sizePassThroughDelimited.size(), is(19));
		assertThat(sizePassThroughDelimited.isEmpty(), is(false));
	}

	@Test
	public void prefix() {
		Sequence<?> emptyPrefixed = empty.prefix("[");
		twice(() -> assertThat(emptyPrefixed, containsFixed("[")));

		Iterator<?> emptyIterator = emptyPrefixed.iterator();
		emptyIterator.next();
		expecting(NoSuchElementException.class, emptyIterator::next);

		Sequence<?> onePrefixed = _1.prefix("[");
		twice(() -> assertThat(onePrefixed, containsFixed("[", 1)));

		Sequence<?> threePrefixed = _123.prefix("[");
		twice(() -> assertThat(threePrefixed, containsFixed("[", 1, 2, 3)));

		Sequence<?> mutablePrefixed = mutableFive.prefix("[");
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutablePrefixed));
		twice(() -> assertThat(mutablePrefixed, containsSized("[", 1, 2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));

		Sequence<Integer> sizePassThroughPrefixed = sizePassThrough.prefix("[");
		assertThat(sizePassThroughPrefixed.size(), is(11));
		assertThat(sizePassThroughPrefixed.isEmpty(), is(false));

		Sequence<Integer> emptySizePassThroughPrefixed = emptySizePassThrough.prefix("[");
		assertThat(emptySizePassThroughPrefixed.size(), is(1));
		assertThat(emptySizePassThroughPrefixed.isEmpty(), is(false));
	}

	@Test
	public void suffix() {
		Sequence<?> emptySuffixed = empty.suffix("]");
		twice(() -> assertThat(emptySuffixed, containsFixed("]")));

		Iterator<?> emptyIterator = emptySuffixed.iterator();
		emptyIterator.next();
		expecting(NoSuchElementException.class, emptyIterator::next);

		Sequence<?> oneSuffixed = _1.suffix("]");
		twice(() -> assertThat(oneSuffixed, containsFixed(1, "]")));

		Sequence<?> threeSuffixed = _123.suffix("]");
		twice(() -> assertThat(threeSuffixed, containsFixed(1, 2, 3, "]")));

		Sequence<?> mutableSuffixed = mutableFive.suffix("]");
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutableSuffixed));
		twice(() -> assertThat(mutableSuffixed, containsSized(1, 2, 3, 4, 5, "]")));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));

		Sequence<Integer> sizePassThroughSuffixed = sizePassThrough.suffix("]");
		assertThat(sizePassThroughSuffixed.size(), is(11));
		assertThat(sizePassThroughSuffixed.isEmpty(), is(false));

		Sequence<Integer> emptySizePassThroughSuffixed = emptySizePassThrough.suffix("]");
		assertThat(emptySizePassThroughSuffixed.size(), is(1));
		assertThat(emptySizePassThroughSuffixed.isEmpty(), is(false));
	}

	@Test
	public void delimitPrefixSuffix() {
		Sequence<?> emptyDelimited = empty.delimit(", ").prefix("[").suffix("]");
		twice(() -> assertThat(emptyDelimited, containsFixed("[", "]")));

		Iterator<?> emptyIterator = emptyDelimited.iterator();
		emptyIterator.next();
		emptyIterator.next();
		expecting(NoSuchElementException.class, emptyIterator::next);

		Sequence<?> threeDelimited = _123.delimit(", ").prefix("[").suffix("]");
		twice(() -> assertThat(threeDelimited, containsFixed("[", 1, ", ", 2, ", ", 3, "]")));

		Sequence<Integer> sizePassThroughDelimited = sizePassThrough.delimit(", ").prefix("[").suffix("]");
		assertThat(sizePassThroughDelimited.size(), is(21));
		assertThat(sizePassThroughDelimited.isEmpty(), is(false));

		Sequence<Integer> emptySizePassThroughDelimited = emptySizePassThrough.delimit(", ").prefix("[").suffix("]");
		assertThat(emptySizePassThroughDelimited.size(), is(2));
		assertThat(emptySizePassThroughDelimited.isEmpty(), is(false));
	}

	@Test
	public void suffixPrefixDelimit() {
		Sequence<?> emptyDelimited = empty.suffix("]").prefix("[").delimit(", ");
		twice(() -> assertThat(emptyDelimited, containsFixed("[", ", ", "]")));

		Iterator<?> emptyIterator = emptyDelimited.iterator();
		emptyIterator.next();
		emptyIterator.next();
		emptyIterator.next();
		expecting(NoSuchElementException.class, emptyIterator::next);

		Sequence<?> threeDelimited = _123.suffix("]").prefix("[").delimit(", ");
		twice(() -> assertThat(threeDelimited, containsFixed("[", ", ", 1, ", ", 2, ", ", 3, ", ", "]")));

		Sequence<?> mutableDelimited = mutableFive.suffix("]").prefix("[").delimit(", ");
		Iterator<?> mutableIterator = mutableDelimited.iterator();
		mutableIterator.next();
		expecting(UnsupportedOperationException.class, mutableIterator::remove);
		mutableIterator.next();
		expecting(UnsupportedOperationException.class, mutableIterator::remove);
		assertThat(mutableDelimited, containsSized("[", ", ", 1, ", ", 2, ", ", 3, ", ", 4, ", ", 5, ", ", "]"));
		assertThat(mutableFive, containsSized(1, 2, 3, 4, 5));

		Sequence<Integer> sizePassThroughDelimited = sizePassThrough.suffix("]").prefix("[").delimit(", ");
		assertThat(sizePassThroughDelimited.size(), is(23));
		assertThat(sizePassThroughDelimited.isEmpty(), is(false));

		Sequence<Integer> emptySizePassThroughDelimited = emptySizePassThrough.suffix("]").prefix("[").delimit(", ");
		assertThat(emptySizePassThroughDelimited.size(), is(3));
		assertThat(emptySizePassThroughDelimited.isEmpty(), is(false));
	}

	@Test
	public void surround() {
		Sequence<?> emptyDelimited = empty.delimit("[", ", ", "]");
		twice(() -> assertThat(emptyDelimited, containsFixed("[", "]")));

		Iterator<?> emptyIterator = emptyDelimited.iterator();
		emptyIterator.next();
		emptyIterator.next();
		expecting(NoSuchElementException.class, emptyIterator::next);

		Sequence<?> threeDelimited = _123.delimit("[", ", ", "]");
		twice(() -> assertThat(threeDelimited, containsFixed("[", 1, ", ", 2, ", ", 3, "]")));

		Sequence<?> mutableDelimited = mutableFive.delimit("[", ", ", "]");
		Iterator<?> mutableIterator = mutableDelimited.iterator();
		mutableIterator.next();
		expecting(UnsupportedOperationException.class, mutableIterator::remove);
		mutableIterator.next();
		expecting(UnsupportedOperationException.class, mutableIterator::remove);
		assertThat(mutableDelimited, containsSized("[", 1, ", ", 2, ", ", 3, ", ", 4, ", ", 5, "]"));
		assertThat(mutableFive, containsSized(1, 2, 3, 4, 5));

		Sequence<Integer> sizePassThroughDelimited = sizePassThrough.delimit("[", ", ", "]");
		assertThat(sizePassThroughDelimited.size(), is(21));
		assertThat(sizePassThroughDelimited.isEmpty(), is(false));

		Sequence<Integer> emptySizePassThroughDelimited = emptySizePassThrough.delimit("[", ", ", "]");
		assertThat(emptySizePassThroughDelimited.size(), is(2));
		assertThat(emptySizePassThroughDelimited.isEmpty(), is(false));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void interleaveWithSequence() {
		Sequence<Pair<Integer, Integer>> emptyInterleaved = empty.interleave(empty);
		twice(() -> assertThat(emptyInterleaved, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyInterleaved.iterator().next());

		Sequence<Pair<Integer, Integer>> interleavedShortFirst = _123.interleave(_12345);
		twice(() -> assertThat(interleavedShortFirst,
		                       containsFixed(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3), Pair.of(null, 4),
		                                     Pair.of(null, 5))));

		Sequence<Pair<Integer, Integer>> interleavedShortLast = _12345.interleave(_123);
		twice(() -> assertThat(interleavedShortLast,
		                       containsFixed(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3), Pair.of(4, null),
		                                     Pair.of(5, null))));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(interleavedShortFirst));
		twice(() -> assertThat(interleavedShortLast,
		                       containsFixed(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3), Pair.of(4, null),
		                                     Pair.of(5, null))));

		Sequence<Pair<Integer, Integer>> interleavedSizePassThroughShorter = sizePassThrough.interleave(_123);
		twice(() -> assertThat(interleavedSizePassThroughShorter.size(), is(10)));
		twice(() -> assertThat(interleavedSizePassThroughShorter.isEmpty(), is(false)));

		Sequence<Pair<Integer, Integer>> interleavedSizePassThroughLonger = _123.interleave(sizePassThrough);
		twice(() -> assertThat(interleavedSizePassThroughLonger.size(), is(10)));
		twice(() -> assertThat(interleavedSizePassThroughLonger.isEmpty(), is(false)));

		Sequence<Pair<Integer, Integer>> interleavedEmptySizePassThrough1 = emptySizePassThrough.interleave(_123);
		twice(() -> assertThat(interleavedEmptySizePassThrough1.size(), is(3)));
		twice(() -> assertThat(interleavedEmptySizePassThrough1.isEmpty(), is(false)));

		Sequence<Pair<Integer, Integer>> interleavedEmptySizePassThrough2 = _123.interleave(emptySizePassThrough);
		twice(() -> assertThat(interleavedEmptySizePassThrough2.size(), is(3)));
		twice(() -> assertThat(interleavedEmptySizePassThrough2.isEmpty(), is(false)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void interleaveWithIterable() {
		Sequence<Pair<Integer, Integer>> emptyInterleaved = empty.interleave(empty);
		twice(() -> assertThat(emptyInterleaved, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyInterleaved.iterator().next());

		Sequence<Pair<Integer, Integer>> interleavedShortFirst = _123.interleave(Iterables.of(1, 2, 3, 4, 5));
		twice(() -> assertThat(interleavedShortFirst,
		                       containsFixed(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3), Pair.of(null, 4),
		                                     Pair.of(null, 5))));

		Sequence<Pair<Integer, Integer>> interleavedShortLast = _12345.interleave(Iterables.of(1, 2, 3));
		twice(() -> assertThat(interleavedShortLast,
		                       containsFixed(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3), Pair.of(4, null),
		                                     Pair.of(5, null))));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(interleavedShortFirst));
		twice(() -> assertThat(interleavedShortLast,
		                       containsFixed(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3), Pair.of(4, null),
		                                     Pair.of(5, null))));

		Sequence<Pair<Integer, Integer>> interleavedSizePassThroughShorter = sizePassThrough.interleave(
				Iterables.of(1, 2, 3));
		twice(() -> assertThat(interleavedSizePassThroughShorter.size(), is(10)));
		twice(() -> assertThat(interleavedSizePassThroughShorter.isEmpty(), is(false)));

		Sequence<Pair<Integer, Integer>> interleavedSizePassThroughLonger = sizePassThrough.interleave(
				Iterables.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
		twice(() -> assertThat(interleavedSizePassThroughLonger.size(), is(12)));
		twice(() -> assertThat(interleavedSizePassThroughLonger.isEmpty(), is(false)));

		Sequence<Pair<Integer, Integer>> interleavedEmptySizePassThrough = emptySizePassThrough.interleave(
				Iterables.of(1, 2, 3));
		twice(() -> assertThat(interleavedEmptySizePassThrough.size(), is(3)));
		twice(() -> assertThat(interleavedEmptySizePassThrough.isEmpty(), is(false)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void interleaveShortWithSequence() {
		Sequence<Pair<Integer, Integer>> emptyInterleaved = empty.interleaveShort(empty);
		twice(() -> assertThat(emptyInterleaved, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyInterleaved.iterator().next());

		Sequence<Pair<Integer, Integer>> interleavedShortFirst = _123.interleaveShort(_12345);
		twice(() -> assertThat(interleavedShortFirst,
		                       containsFixed(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3))));

		Sequence<Pair<Integer, Integer>> interleavedShortLast = _12345.interleaveShort(_123);
		twice(() -> assertThat(interleavedShortLast,
		                       containsFixed(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3))));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(interleavedShortFirst));
		twice(() -> assertThat(interleavedShortLast,
		                       containsFixed(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3))));

		Sequence<Pair<Integer, Integer>> interleavedSizePassThroughShorter = sizePassThrough.interleaveShort(_123);
		twice(() -> assertThat(interleavedSizePassThroughShorter.size(), is(3)));
		twice(() -> assertThat(interleavedSizePassThroughShorter.isEmpty(), is(false)));

		Sequence<Pair<Integer, Integer>> interleavedSizePassThroughLonger = _123.interleaveShort(sizePassThrough);
		twice(() -> assertThat(interleavedSizePassThroughLonger.size(), is(3)));
		twice(() -> assertThat(interleavedSizePassThroughLonger.isEmpty(), is(false)));

		Sequence<Pair<Integer, Integer>> interleavedEmptySizePassThrough1 = emptySizePassThrough.interleaveShort(_123);
		twice(() -> assertThat(interleavedEmptySizePassThrough1.size(), is(0)));
		twice(() -> assertThat(interleavedEmptySizePassThrough1.isEmpty(), is(true)));

		Sequence<Pair<Integer, Integer>> interleavedEmptySizePassThrough2 = _123.interleaveShort(emptySizePassThrough);
		twice(() -> assertThat(interleavedEmptySizePassThrough2.size(), is(0)));
		twice(() -> assertThat(interleavedEmptySizePassThrough2.isEmpty(), is(true)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void interleaveShortWithIterable() {
		Sequence<Pair<Integer, Integer>> emptyInterleaved = empty.interleaveShort(empty);
		twice(() -> assertThat(emptyInterleaved, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyInterleaved.iterator().next());

		Sequence<Pair<Integer, Integer>> interleavedShortFirst = _123.interleaveShort(Iterables.of(1, 2, 3, 4, 5));
		twice(() -> assertThat(interleavedShortFirst,
		                       containsFixed(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3))));

		Sequence<Pair<Integer, Integer>> interleavedShortLast = _12345.interleaveShort(Iterables.of(1, 2, 3));
		twice(() -> assertThat(interleavedShortLast,
		                       containsFixed(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3))));

		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(interleavedShortFirst));
		twice(() -> assertThat(interleavedShortLast,
		                       containsFixed(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3))));

		Sequence<Pair<Integer, Integer>> interleavedSizePassThroughShorter = sizePassThrough.interleaveShort(
				Iterables.of(1, 2, 3));
		twice(() -> assertThat(interleavedSizePassThroughShorter.size(), is(3)));
		twice(() -> assertThat(interleavedSizePassThroughShorter.isEmpty(), is(false)));

		Sequence<Pair<Integer, Integer>> interleavedSizePassThroughLonger = sizePassThrough.interleaveShort(
				Iterables.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
		twice(() -> assertThat(interleavedSizePassThroughLonger.size(), is(10)));
		twice(() -> assertThat(interleavedSizePassThroughLonger.isEmpty(), is(false)));

		Sequence<Pair<Integer, Integer>> interleavedEmptySizePassThrough = emptySizePassThrough.interleaveShort(
				Iterables.of(1, 2, 3));
		twice(() -> assertThat(interleavedEmptySizePassThrough.size(), is(0)));
		twice(() -> assertThat(interleavedEmptySizePassThrough.isEmpty(), is(true)));
	}

	@Test
	public void reverse() {
		Sequence<Integer> emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyReversed.iterator().next());

		Sequence<Integer> oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, containsFixed(1)));

		Sequence<Integer> twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, containsFixed(2, 1)));

		Sequence<Integer> threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, containsFixed(3, 2, 1)));

		Sequence<Integer> nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed, containsFixed(9, 8, 7, 6, 5, 4, 3, 2, 1)));

		Sequence<Integer> reversedSizePassThrough = sizePassThrough.reverse();
		twice(() -> assertThat(reversedSizePassThrough.size(), is(10)));
		twice(() -> assertThat(reversedSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void reverseRemoval() {
		Sequence<Integer> reversed = mutableFive.reverse();
		if (reversed instanceof ListSequence) {
			// covered by ListSequenceTest
		} else {
			expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(reversed));
			twice(() -> assertThat(reversed, containsSized(5, 4, 3, 2, 1)));
			twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));
		}
	}

	@Test
	public void shuffle() {
		Sequence<Integer> emptyShuffled = empty.shuffle();
		twice(() -> assertThat(emptyShuffled, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyShuffled.iterator().next());

		Sequence<Integer> oneShuffled = _1.shuffle();
		twice(() -> assertThat(oneShuffled, containsFixed(1)));

		Sequence<Integer> twoShuffled = _12.shuffle();
		twice(() -> assertThat(twoShuffled, containsInAnyOrder(1, 2)));
		twice(() -> assertThat(twoShuffled.sizeType(), is(FIXED)));
		twice(() -> assertThat(twoShuffled.size(), is(2)));
		twice(() -> assertThat(twoShuffled.isEmpty(), is(false)));

		Sequence<Integer> threeShuffled = _123.shuffle();
		twice(() -> assertThat(threeShuffled, containsInAnyOrder(1, 2, 3)));
		twice(() -> assertThat(threeShuffled.sizeType(), is(FIXED)));
		twice(() -> assertThat(threeShuffled.size(), is(3)));
		twice(() -> assertThat(threeShuffled.isEmpty(), is(false)));

		Sequence<Integer> nineShuffled = _123456789.shuffle();
		twice(() -> assertThat(nineShuffled, containsInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9)));
		twice(() -> assertThat(nineShuffled.sizeType(), is(FIXED)));
		twice(() -> assertThat(nineShuffled.size(), is(9)));
		twice(() -> assertThat(nineShuffled.isEmpty(), is(false)));

		Sequence<Integer> mutableShuffled = mutableFive.shuffle();
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutableShuffled));
		twice(() -> assertThat(mutableShuffled, containsInAnyOrder(1, 2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));

		Sequence<Integer> shuffledSizePassThrough = sizePassThrough.shuffle();
		twice(() -> assertThat(shuffledSizePassThrough.size(), is(10)));
		twice(() -> assertThat(shuffledSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void shuffleWithRandomSource() {
		Sequence<Integer> emptyShuffled = empty.shuffle(new Random(17));
		twice(() -> assertThat(emptyShuffled, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyShuffled.iterator().next());

		Sequence<Integer> oneShuffled = _1.shuffle(new Random(17));
		twice(() -> assertThat(oneShuffled, containsFixed(1)));

		Sequence<Integer> twoShuffled = _12.shuffle(new Random(17));
		twice(() -> assertThat(twoShuffled, containsInAnyOrder(1, 2)));
		twice(() -> assertThat(twoShuffled.sizeType(), is(FIXED)));
		twice(() -> assertThat(twoShuffled.size(), is(2)));
		twice(() -> assertThat(twoShuffled.isEmpty(), is(false)));

		Sequence<Integer> threeShuffled = _123.shuffle(new Random(17));
		twice(() -> assertThat(threeShuffled, containsInAnyOrder(1, 2, 3)));
		twice(() -> assertThat(threeShuffled.sizeType(), is(FIXED)));
		twice(() -> assertThat(threeShuffled.size(), is(3)));
		twice(() -> assertThat(threeShuffled.isEmpty(), is(false)));

		Sequence<Integer> nineShuffled = _123456789.shuffle(new Random(17));
		twice(() -> assertThat(nineShuffled, containsInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9)));
		twice(() -> assertThat(nineShuffled.sizeType(), is(FIXED)));
		twice(() -> assertThat(nineShuffled.size(), is(9)));
		twice(() -> assertThat(nineShuffled.isEmpty(), is(false)));

		Sequence<Integer> mutableShuffled = mutableFive.shuffle(new Random(17));
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutableShuffled));
		twice(() -> assertThat(mutableShuffled, containsInAnyOrder(1, 2, 3, 4, 5)));
		twice(() -> assertThat(mutableShuffled.sizeType(), is(AVAILABLE)));
		twice(() -> assertThat(mutableShuffled.size(), is(5)));
		twice(() -> assertThat(mutableShuffled.isEmpty(), is(false)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));

		Sequence<Integer> shuffledSizePassThrough = sizePassThrough.shuffle(new Random(17));
		twice(() -> assertThat(shuffledSizePassThrough.size(), is(10)));
		twice(() -> assertThat(shuffledSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void shuffleWithRandomSupplier() {
		Sequence<Integer> emptyShuffled = empty.shuffle(() -> new Random(17));
		twice(() -> assertThat(emptyShuffled, is(emptyFixedIterable())));

		Sequence<Integer> oneShuffled = _1.shuffle(() -> new Random(17));
		twice(() -> assertThat(oneShuffled, containsFixed(1)));

		Sequence<Integer> twoShuffled = _12.shuffle(() -> new Random(17));
		twice(() -> assertThat(twoShuffled, containsInAnyOrder(1, 2)));
		twice(() -> assertThat(twoShuffled.sizeType(), is(FIXED)));
		twice(() -> assertThat(twoShuffled.size(), is(2)));
		twice(() -> assertThat(twoShuffled.isEmpty(), is(false)));

		Sequence<Integer> threeShuffled = _123.shuffle(() -> new Random(17));
		twice(() -> assertThat(threeShuffled, containsInAnyOrder(1, 2, 3)));
		twice(() -> assertThat(threeShuffled.sizeType(), is(FIXED)));
		twice(() -> assertThat(threeShuffled.size(), is(3)));
		twice(() -> assertThat(threeShuffled.isEmpty(), is(false)));

		Sequence<Integer> nineShuffled = _123456789.shuffle(() -> new Random(17));
		twice(() -> assertThat(nineShuffled, containsInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9)));
		twice(() -> assertThat(nineShuffled.sizeType(), is(FIXED)));
		twice(() -> assertThat(nineShuffled.size(), is(9)));
		twice(() -> assertThat(nineShuffled.isEmpty(), is(false)));

		Sequence<Integer> mutableShuffled = mutableFive.shuffle(() -> new Random(17));
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutableShuffled));
		twice(() -> assertThat(mutableShuffled, containsInAnyOrder(1, 2, 3, 4, 5)));
		twice(() -> assertThat(mutableShuffled.sizeType(), is(AVAILABLE)));
		twice(() -> assertThat(mutableShuffled.size(), is(5)));
		twice(() -> assertThat(mutableShuffled.isEmpty(), is(false)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));

		Sequence<Integer> shuffledSizePassThrough = sizePassThrough.shuffle(() -> new Random(17));
		twice(() -> assertThat(shuffledSizePassThrough.size(), is(10)));
		twice(() -> assertThat(shuffledSizePassThrough.isEmpty(), is(false)));
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
		twice(() -> assertThat(chars.size(), is(65536)));
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
		                       containsUnsized(Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE)));
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
		twice(() -> assertThat(startingAtMaxValue,
		                       containsUnsized(Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE)));
	}

	@Test
	public void charsStartingAt() {
		Sequence<Character> startingAtA = Sequence.charsFrom('A');
		twice(() -> assertThat(startingAtA, beginsWith('A', 'B', 'C', 'D', 'E')));

		Sequence<Character> startingAt1400 = Sequence.charsFrom('\u1400');
		twice(() -> assertThat(startingAt1400.limit(256).last(), is(Optional.of('\u14FF'))));

		Sequence<Character> startingAtMaxValue = Sequence.charsFrom((char) (Character.MAX_VALUE - 2));
		twice(() -> assertThat(startingAtMaxValue,
		                       containsUnsized((char) (Character.MAX_VALUE - 2), (char) (Character.MAX_VALUE - 1),
		                                       Character.MAX_VALUE)));
	}

	@Test
	public void intRange() {
		Sequence<Integer> range17to20 = Sequence.range(17, 20);
		twice(() -> assertThat(range17to20, containsUnsized(17, 18, 19, 20)));

		Sequence<Integer> range20to17 = Sequence.range(20, 17);
		twice(() -> assertThat(range20to17, containsUnsized(20, 19, 18, 17)));
	}

	@Test
	public void longRange() {
		Sequence<Long> range17to20 = Sequence.range(17L, 20L);
		twice(() -> assertThat(range17to20, containsUnsized(17L, 18L, 19L, 20L)));

		Sequence<Long> range20to17 = Sequence.range(20L, 17L);
		twice(() -> assertThat(range20to17, containsUnsized(20L, 19L, 18L, 17L)));
	}

	@Test
	public void charRange() {
		Sequence<Character> rangeAtoF = Sequence.range('A', 'F');
		twice(() -> assertThat(rangeAtoF, containsUnsized('A', 'B', 'C', 'D', 'E', 'F')));

		Sequence<Character> rangeFtoA = Sequence.range('F', 'A');
		twice(() -> assertThat(rangeFtoA, containsUnsized('F', 'E', 'D', 'C', 'B', 'A')));
	}

	@Test
	public void toChars() {
		CharSeq emptyChars = empty.toChars(x -> (char) (x + 'a' - 1));
		twice(() -> assertThat(emptyChars, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyChars.iterator().nextChar());

		CharSeq charSeq = _12345.toChars(x -> (char) (x + 'a' - 1));
		twice(() -> assertThat(charSeq, containsChars('a', 'b', 'c', 'd', 'e')));
		twice(() -> assertThat(charSeq.size(), is(5)));
		twice(() -> assertThat(charSeq.isEmpty(), is(false)));

		CharSeq mutableCharSeq = mutableFive.toChars(x -> (char) (x + 'a' - 1));
		assertThat(Tests.removeFirst(mutableCharSeq), is('a'));
		twice(() -> assertThat(mutableCharSeq, containsChars('b', 'c', 'd', 'e')));
		twice(() -> assertThat(mutableCharSeq.size(), is(4)));
		twice(() -> assertThat(mutableCharSeq.isEmpty(), is(false)));
	}

	@Test
	public void toInts() {
		IntSequence emptyInts = empty.toInts(x -> x + 1);
		twice(() -> assertThat(emptyInts, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyInts.iterator().nextInt());

		IntSequence intSequence = _12345.toInts(x -> x + 1);
		twice(() -> assertThat(intSequence, containsInts(2, 3, 4, 5, 6)));
		twice(() -> assertThat(intSequence.size(), is(5)));
		twice(() -> assertThat(intSequence.isEmpty(), is(false)));

		IntSequence mutableIntSequence = mutableFive.toInts(x -> x + 1);
		assertThat(Tests.removeFirst(mutableIntSequence), is(2));
		twice(() -> assertThat(mutableIntSequence, containsInts(3, 4, 5, 6)));
		twice(() -> assertThat(mutableIntSequence.size(), is(4)));
		twice(() -> assertThat(mutableIntSequence.isEmpty(), is(false)));
	}

	@Test
	public void toLongs() {
		LongSequence emptyLongs = empty.toLongs(x -> x + 1);
		twice(() -> assertThat(emptyLongs, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyLongs.iterator().nextLong());

		LongSequence longSequence = _12345.toLongs(x -> x + 1);
		twice(() -> assertThat(longSequence, containsLongs(2, 3, 4, 5, 6)));
		twice(() -> assertThat(longSequence.size(), is(5)));
		twice(() -> assertThat(longSequence.isEmpty(), is(false)));

		LongSequence mutableLongSequence = mutableFive.toLongs(x -> x + 1);
		assertThat(Tests.removeFirst(mutableLongSequence), is(2L));
		twice(() -> assertThat(mutableLongSequence, containsLongs(3, 4, 5, 6)));
		twice(() -> assertThat(mutableLongSequence.size(), is(4)));
		twice(() -> assertThat(mutableLongSequence.isEmpty(), is(false)));
	}

	@Test
	public void toDoubles() {
		DoubleSequence emptyDoubles = empty.toDoubles(x -> x + 1);
		twice(() -> assertThat(emptyDoubles, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyDoubles.iterator().nextDouble());

		DoubleSequence doubleSequence = _12345.toDoubles(x -> x + 1);
		twice(() -> assertThat(doubleSequence, containsDoubles(2, 3, 4, 5, 6)));
		twice(() -> assertThat(doubleSequence.size(), is(5)));
		twice(() -> assertThat(doubleSequence.isEmpty(), is(false)));

		DoubleSequence mutableDoubleSequence = mutableFive.toDoubles(x -> x + 1);
		assertThat(Tests.removeFirst(mutableDoubleSequence), is(2.0));
		twice(() -> assertThat(mutableDoubleSequence, containsDoubles(3, 4, 5, 6)));
		twice(() -> assertThat(mutableDoubleSequence.size(), is(4)));
		twice(() -> assertThat(mutableDoubleSequence.isEmpty(), is(false)));
	}

	@Test
	public void repeat() {
		Sequence<Integer> emptyRepeated = empty.repeat();
		twice(() -> assertThat(emptyRepeated, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeated.iterator().next());

		Sequence<Integer> oneRepeated = _1.repeat();
		twice(() -> assertThat(oneRepeated, is(infiniteBeginningWith(1, 1, 1))));

		Sequence<Integer> twoRepeated = _12.repeat();
		twice(() -> assertThat(twoRepeated, is(infiniteBeginningWith(1, 2, 1, 2, 1))));

		Sequence<Integer> threeRepeated = _123.repeat();
		twice(() -> assertThat(threeRepeated, is(infiniteBeginningWith(1, 2, 3, 1, 2, 3, 1, 2))));

		Sequence<Integer> mutableRepeated = mutableFive.repeat();
		assertThat(Tests.removeFirst(mutableRepeated), is(1));
		twice(() -> assertThat(mutableRepeated, beginsWith(2, 3, 4, 5, 2, 3, 4, 5)));
		twice(() -> assertThat(mutableRepeated.sizeType(), is(UNAVAILABLE)));
		twice(() -> assertThat(mutableRepeated.isEmpty(), is(false)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<Integer> varyingLengthRepeated = Sequence.from(new Iterable<Integer>() {
			private List<Integer> list = Lists.of(1, 2, 3);
			int end = list.size();

			@Override
			public Iterator<Integer> iterator() {
				List<Integer> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				return subList.iterator();
			}
		}).repeat();
		assertThat(varyingLengthRepeated, contains(1, 2, 3, 1, 2, 1));

		Sequence<Integer> infiniteRepeated = Sequence.recurse(1, x -> x + 1).repeat();
		twice(() -> assertThat(infiniteRepeated, is(infiniteBeginningWith(1, 2, 3, 4, 5, 6))));
	}

	@Test
	public void repeatTwice() {
		Sequence<Integer> emptyRepeatedTwice = empty.repeat(2);
		twice(() -> assertThat(emptyRepeatedTwice, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeatedTwice.iterator().next());

		Sequence<Integer> oneRepeatedTwice = _1.repeat(2);
		twice(() -> assertThat(oneRepeatedTwice, containsFixed(1, 1)));

		Sequence<Integer> twoRepeatedTwice = _12.repeat(2);
		twice(() -> assertThat(twoRepeatedTwice, containsFixed(1, 2, 1, 2)));

		Sequence<Integer> threeRepeatedTwice = _123.repeat(2);
		twice(() -> assertThat(threeRepeatedTwice, containsFixed(1, 2, 3, 1, 2, 3)));

		Sequence<Integer> mutableRepeatedTwice = mutableFive.repeat(2);
		assertThat(Tests.removeFirst(mutableRepeatedTwice), is(1));
		twice(() -> assertThat(mutableRepeatedTwice, containsUnsized(2, 3, 4, 5, 2, 3, 4, 5)));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		Sequence<Integer> varyingLengthRepeatedTwice = Sequence.from(new Iterable<Integer>() {
			private List<Integer> list = Lists.of(1, 2, 3);
			int end = list.size();

			@Override
			public Iterator<Integer> iterator() {
				List<Integer> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				return subList.iterator();
			}
		}).repeat(2);
		assertThat(varyingLengthRepeatedTwice, contains(1, 2, 3, 1, 2));

		Sequence<Integer> infiniteRepeatedTwice = Sequence.recurse(1, x -> x + 1).repeat(2);
		twice(() -> assertThat(infiniteRepeatedTwice, is(infiniteBeginningWith(1, 2, 3, 4, 5, 6))));
	}

	@Test
	public void repeatZero() {
		Sequence<Integer> emptyRepeatedZero = empty.repeat(0);
		twice(() -> assertThat(emptyRepeatedZero, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeatedZero.iterator().next());

		Sequence<Integer> oneRepeatedZero = _1.repeat(0);
		twice(() -> assertThat(oneRepeatedZero, is(emptyFixedIterable())));

		Sequence<Integer> twoRepeatedZero = _12.repeat(0);
		twice(() -> assertThat(twoRepeatedZero, is(emptyFixedIterable())));

		Sequence<Integer> threeRepeatedZero = _123.repeat(0);
		twice(() -> assertThat(threeRepeatedZero, is(emptyFixedIterable())));
	}

	@Test
	public void generate() {
		Queue<Integer> queue = new ArrayDeque<>(Lists.of(1, 2, 3, 4, 5));
		Sequence<Integer> sequence = Sequence.generate(queue::poll);
		assertThat(sequence, infiniteBeginningWith(1, 2, 3, 4, 5, null));
		assertThat(sequence, infiniteBeginningWith((Integer) null));
	}

	@Test
	public void multiGenerate() {
		Sequence<Integer> sequence = Sequence.multiGenerate(() -> {
			Queue<Integer> queue = new ArrayDeque<>(Lists.of(1, 2, 3, 4, 5));
			return queue::poll;
		});
		twice(() -> assertThat(sequence, infiniteBeginningWith(1, 2, 3, 4, 5, null)));
	}

	@Test
	public void swap() {
		Sequence<Integer> emptySwapTwoAndThree = empty.swap((a, b) -> a == 2 && b == 3);
		twice(() -> assertThat(emptySwapTwoAndThree, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptySwapTwoAndThree.iterator().next());

		Sequence<Integer> swapTwoAndThree = _12345.swap((a, b) -> a == 2 && b == 3);
		twice(() -> assertThat(swapTwoAndThree, containsFixed(1, 3, 2, 4, 5)));

		Sequence<Integer> swapTwoWithEverything = _12345.swap((a, b) -> a == 2);
		twice(() -> assertThat(swapTwoWithEverything, containsFixed(1, 3, 4, 5, 2)));

		Sequence<Integer> mutableSwapTwoWithEverything = mutableFive.swap((a, b) -> a == 2);
		expecting(UnsupportedOperationException.class, () -> Tests.removeFirst(mutableSwapTwoWithEverything));
		twice(() -> assertThat(mutableSwapTwoWithEverything, containsSized(1, 3, 4, 5, 2)));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 3, 4, 5)));

		Sequence<Integer> sizePassThroughSwapped = sizePassThrough.swap((a, b) -> a == 2);
		assertThat(sizePassThroughSwapped.size(), is(10));
		assertThat(sizePassThroughSwapped.isEmpty(), is(false));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void index() {
		BiSequence<Integer, Integer> emptyIndexed = empty.index();
		twice(() -> assertThat(emptyIndexed, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyIndexed.iterator().next());

		BiSequence<Integer, Integer> fiveIndexed = _12345.index();
		twice(() -> assertThat(fiveIndexed, containsFixed(Pair.of(0, 1), Pair.of(1, 2), Pair.of(2, 3), Pair.of(3, 4),
		                                                  Pair.of(4, 5))));

		BiSequence<Integer, Integer> mutableIndexed = mutableFive.index();
		assertThat(Tests.removeFirst(mutableIndexed), is(Pair.of(0, 1)));
		twice(() -> assertThat(mutableIndexed, containsSized(Pair.of(0, 2), Pair.of(1, 3), Pair.of(2, 4),
		                                                     Pair.of(3, 5))));
		twice(() -> assertThat(mutableFive, containsSized(2, 3, 4, 5)));

		BiSequence<Integer, Integer> sizePassThroughIndexed = sizePassThrough.index();
		assertThat(sizePassThroughIndexed.size(), is(10));
		assertThat(sizePassThroughIndexed.isEmpty(), is(false));
	}

	@Test
	public void filterClear() {
		Sequence<Integer> filtered = mutableFive.filter(x -> x % 2 != 0);
		filtered.clear();

		twice(() -> assertThat(filtered, is(emptyUnsizedIterable())));
		twice(() -> assertThat(mutableFive, containsSized(2, 4)));
	}

	@Test
	public void appendClear() {
		Sequence<Integer> appended = mutableFive.append(new ArrayList<>(Lists.of(2)));
		appended.clear();

		twice(() -> assertThat(appended, is(emptySizedIterable())));
		twice(() -> assertThat(mutableFive, is(emptySizedIterable())));
	}

	@Test
	public void isEmpty() {
		twice(() -> assertThat(empty.isEmpty(), is(true)));
		twice(() -> assertThat(_1.isEmpty(), is(false)));
		twice(() -> assertThat(_12.isEmpty(), is(false)));
		twice(() -> assertThat(_12345.isEmpty(), is(false)));
		twice(() -> assertThat(mutableFive.isEmpty(), is(false)));
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
	public void containsAllVarargs() {
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
		assertThat(empty.containsAll((Iterable<?>) Lists.of()), is(true));
		assertThat(empty.containsAll((Iterable<?>) Lists.of(17, 18, 19)), is(false));

		assertThat(_12345.containsAll(Iterables.of()), is(true));
		assertThat(_12345.containsAll((Iterable<?>) Lists.of()), is(true));
		assertThat(_12345.containsAll(Iterables.of(1)), is(true));
		assertThat(_12345.containsAll(Iterables.of(1, 3, 5)), is(true));
		assertThat(_12345.containsAll((Iterable<?>) Lists.of(1, 3, 5)), is(true));
		assertThat(_12345.containsAll(Iterables.of(1, 2, 3, 4, 5)), is(true));
		assertThat(_12345.containsAll(Iterables.of(1, 2, 3, 4, 5, 17)), is(false));
		assertThat(_12345.containsAll(Iterables.of(17, 18, 19)), is(false));
	}

	@Test
	public void containsAllCollection() {
		assertThat(empty.containsAll(Lists.of()), is(true));
		assertThat(empty.containsAll(Lists.of(17, 18, 19)), is(false));

		assertThat(_12345.containsAll(Lists.of()), is(true));
		assertThat(_12345.containsAll(Lists.of(1)), is(true));
		assertThat(_12345.containsAll(Lists.of(1, 3, 5)), is(true));
		assertThat(_12345.containsAll(Lists.of(1, 2, 3, 4, 5)), is(true));
		assertThat(_12345.containsAll(Lists.of(1, 2, 3, 4, 5, 17)), is(false));
		assertThat(_12345.containsAll(Lists.of(17, 18, 19)), is(false));
	}

	@Test
	public void containsAnyVarargs() {
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

	@Test
	public void containsAnyCollection() {
		assertThat(empty.containsAny(Lists.of()), is(false));
		assertThat(empty.containsAny(Lists.of(17, 18, 19)), is(false));

		assertThat(_12345.containsAny(Lists.of()), is(false));
		assertThat(_12345.containsAny(Lists.of(1)), is(true));
		assertThat(_12345.containsAny(Lists.of(1, 3, 5)), is(true));
		assertThat(_12345.containsAny(Lists.of(1, 2, 3, 4, 5)), is(true));
		assertThat(_12345.containsAny(Lists.of(1, 2, 3, 4, 5, 17)), is(true));
		assertThat(_12345.containsAny(Lists.of(17, 18, 19)), is(false));
	}

	@Test
	public void immutable() {
		Sequence<Integer> emptyImmutable = empty.immutable();
		twice(() -> assertThat(emptyImmutable, is(emptyFixedIterable())));
		expecting(NoSuchElementException.class, () -> emptyImmutable.iterator().next());

		Sequence<Integer> fiveImmutable = mutableFive.immutable();
		twice(() -> assertThat(fiveImmutable, containsSized(1, 2, 3, 4, 5)));

		Iterator<Integer> iterator = fiveImmutable.iterator();
		iterator.next();
		expecting(UnsupportedOperationException.class, iterator::remove);
		twice(() -> assertThat(fiveImmutable, containsSized(1, 2, 3, 4, 5)));

		expecting(UnsupportedOperationException.class, () -> fiveImmutable.add(17));
		twice(() -> assertThat(fiveImmutable, containsSized(1, 2, 3, 4, 5)));

		expecting(UnsupportedOperationException.class, () -> fiveImmutable.remove(3));
		twice(() -> assertThat(fiveImmutable, containsSized(1, 2, 3, 4, 5)));

		Sequence<Integer> immutableSizePassThrough = sizePassThrough.immutable();
		twice(() -> assertThat(immutableSizePassThrough.size(), is(10)));
		twice(() -> assertThat(immutableSizePassThrough.isEmpty(), is(false)));
	}

	@Test
	public void iteratorRemove() {
		Iterator<Integer> iterator = mutableFive.iterator();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.next();
		iterator.remove();

		twice(() -> assertThat(mutableFive, containsSized(2, 4, 5)));
	}

	@Test
	public void remove() {
		Sequence<Integer> mutableEmpty = newMutableSequence();
		assertThat(mutableEmpty.remove(3), is(false));
		twice(() -> assertThat(mutableEmpty, is(emptySizedIterable())));

		assertThat(mutableFive.remove(3), is(true));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 4, 5)));

		assertThat(mutableFive.remove(7), is(false));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 4, 5)));
	}

	@Test
	public void removeAllVarargs() {
		assertThat(empty.removeAll(3, 4), is(false));
		twice(() -> assertThat(empty, is(emptyFixedIterable())));

		assertThat(mutableFive.removeAll(3, 4, 7), is(true));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 5)));
	}

	@Test
	public void removeAllIterable() {
		assertThat(empty.removeAll(Iterables.of(3, 4)), is(false));
		twice(() -> assertThat(empty, is(emptyFixedIterable())));

		assertThat(mutableFive.removeAll(Iterables.of(3, 4, 7)), is(true));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 5)));
	}

	@Test
	public void removeAllCollection() {
		Sequence<Integer> mutableEmpty = newMutableSequence();
		assertThat(mutableEmpty.removeAll(Lists.of(3, 4)), is(false));
		twice(() -> assertThat(mutableEmpty, is(emptySizedIterable())));

		assertThat(mutableFive.removeAll(Lists.of(3, 4, 7)), is(true));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 5)));
	}

	@Test
	public void retainAllVarargs() {
		assertThat(empty.retainAll(3, 4), is(false));
		twice(() -> assertThat(empty, is(emptyFixedIterable())));

		assertThat(mutableFive.retainAll(3, 4, 7), is(true));
		twice(() -> assertThat(mutableFive, containsSized(3, 4)));
	}

	@Test
	public void retainAllIterable() {
		assertThat(empty.retainAll(Iterables.of(3, 4)), is(false));
		twice(() -> assertThat(empty, is(emptyFixedIterable())));

		assertThat(mutableFive.retainAll(Iterables.of(3, 4, 7)), is(true));
		twice(() -> assertThat(mutableFive, containsSized(3, 4)));
	}

	@Test
	public void retainAllCollection() {
		Sequence<Integer> mutableEmpty = newMutableSequence();
		assertThat(mutableEmpty.retainAll(Lists.of(3, 4)), is(false));
		twice(() -> assertThat(mutableEmpty, is(emptySizedIterable())));

		assertThat(mutableFive.retainAll(Lists.of(3, 4, 7)), is(true));
		twice(() -> assertThat(mutableFive, containsSized(3, 4)));
	}

	@Test
	public void removeIf() {
		Sequence<Integer> mutableEmpty = newMutableSequence();
		assertThat(mutableEmpty.removeIf(x -> x == 3), is(false));
		twice(() -> assertThat(mutableEmpty, is(emptySizedIterable())));

		assertThat(mutableFive.removeIf(x -> x == 3), is(true));
		twice(() -> assertThat(mutableFive, containsSized(1, 2, 4, 5)));
	}

	@Test
	public void retainIf() {
		assertThat(empty.retainIf(x -> x == 3), is(false));
		twice(() -> assertThat(empty, is(emptyFixedIterable())));

		assertThat(mutableFive.retainIf(x -> x == 3), is(true));
		twice(() -> assertThat(mutableFive, containsSized(3)));
	}
}
