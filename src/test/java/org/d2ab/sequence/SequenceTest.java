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
import org.d2ab.iterator.Iterators;
import org.d2ab.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.reverseOrder;
import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.IsIterableBeginningWith.beginsWith;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class SequenceTest {
	private final Function<Object[], Sequence<?>> generator;

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

	@SuppressWarnings("UnusedParameters")
	public SequenceTest(String description, Function<Object[], Sequence<?>> generator) {
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
	}

	@SuppressWarnings("Convert2MethodRef")
	@Parameters(name = "{0}")
	public static Object[][] parameters() {
		return new Object[][]{
				{"Sequence",
				 (Function<Object[], Sequence<?>>) is -> newStandardSequence(is)},
				{"ListSequence",
				 (Function<Object[], Sequence<?>>) is -> newListSequence(is)},
				{"ChainedListSequence",
				 (Function<Object[], Sequence<?>>) is -> newChainedListSequence(is)},
				{"CollectionSequence",
				 (Function<Object[], Sequence<?>>) is -> newCollectionSequence(is)},
				};
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	private final <T> Sequence<T> newSequence(T... ts) {
		return (Sequence<T>) generator.apply(ts);
	}

	@SafeVarargs
	public static <T> Sequence<T> newChainedListSequence(T... is) {
		List<List<T>> lists = new ArrayList<>();
		List<T> current = new ArrayList<>();
		for (int i = 0; i < is.length; i++) {
			current.add(is[i]);
			if (i % 3 == 0) {
				lists.add(current);
				current = new ArrayList<>();
			}
		}
		lists.add(current);
		return ListSequence.concat(lists);
	}

	@SafeVarargs
	public static <T> Sequence<T> newListSequence(T... is) {
		return ListSequence.from(new ArrayList<>(asList(is)));
	}

	@SafeVarargs
	public static <T> Sequence<T> newCollectionSequence(T... is) {
		return CollectionSequence.from(new ArrayDeque<>(asList(is)));
	}

	@SafeVarargs
	public static <T> Sequence<T> newStandardSequence(T... is) {
		return Sequence.from(new ArrayDeque<>(asList(is))::iterator);
	}

	@Test
	public void empty() {
		Sequence<Integer> empty = Sequence.empty();
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void emptyImmutable() {
		List<Integer> list = Sequence.<Integer>empty().asList();
		expecting(UnsupportedOperationException.class, () -> list.add(1));
		expecting(UnsupportedOperationException.class, () -> list.add(0, 0));
		expecting(UnsupportedOperationException.class, () -> list.addAll(asList(1, 2)));
		expecting(UnsupportedOperationException.class, () -> list.addAll(0, asList(-1, 0)));
		expecting(UnsupportedOperationException.class, () -> list.remove(0));
	}

	@Test
	public void ofNone() {
		Sequence<Integer> sequence = Sequence.of();
		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void ofNoneImmutable() {
		List<Integer> list = Sequence.<Integer>of().asList();
		expecting(UnsupportedOperationException.class, () -> list.add(1));
		expecting(UnsupportedOperationException.class, () -> list.add(0, 0));
		expecting(UnsupportedOperationException.class, () -> list.addAll(asList(1, 2)));
		expecting(UnsupportedOperationException.class, () -> list.addAll(0, asList(-1, 0)));
		expecting(UnsupportedOperationException.class, () -> list.remove(0));
	}

	@Test
	public void ofOne() {
		Sequence<Integer> sequence = Sequence.of(1);
		twice(() -> assertThat(sequence, contains(1)));
	}

	@Test
	public void ofOneImmutable() {
		List<Integer> list = Sequence.of(1).asList();
		expecting(UnsupportedOperationException.class, () -> list.add(2));
		expecting(UnsupportedOperationException.class, () -> list.add(0, 0));
		expecting(UnsupportedOperationException.class, () -> list.addAll(asList(2, 3)));
		expecting(UnsupportedOperationException.class, () -> list.addAll(0, asList(-1, 0)));
		expecting(UnsupportedOperationException.class, () -> list.remove(0));
		expecting(UnsupportedOperationException.class, () -> list.set(0, 17));
	}

	@Test
	public void ofMany() {
		Sequence<Integer> sequence = Sequence.of(1, 2, 3, 4, 5);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void ofManyImmutable() {
		List<Integer> list = Sequence.of(1, 2, 3, 4, 5).asList();
		expecting(UnsupportedOperationException.class, () -> list.add(6));
		expecting(UnsupportedOperationException.class, () -> list.add(0, 0));
		expecting(UnsupportedOperationException.class, () -> list.addAll(asList(6, 7)));
		expecting(UnsupportedOperationException.class, () -> list.addAll(0, asList(-1, 0)));
		expecting(UnsupportedOperationException.class, () -> list.remove(0));
		expecting(UnsupportedOperationException.class, () -> list.set(0, 17));
	}

	@Test
	public void ofNulls() {
		Sequence<Integer> sequence = Sequence.of(1, null, 2, 3, null);
		twice(() -> assertThat(sequence, contains(1, null, 2, 3, null)));
	}

	@Test
	public void fromEmpty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void fromIterable() {
		Sequence<Integer> sequence = Sequence.from(Iterables.of(1, 2, 3));
		twice(() -> assertThat(sequence, contains(1, 2, 3)));
	}

	@Test
	public void onceIterator() {
		Sequence<Integer> sequence = Sequence.once(Iterators.of(1, 2, 3));

		assertThat(sequence, contains(1, 2, 3));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceStream() {
		Sequence<Integer> sequence = Sequence.once(Stream.of(1, 2, 3));

		assertThat(sequence, contains(1, 2, 3));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceEmptyStream() {
		Sequence<Integer> sequence = Sequence.once(Stream.of());

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void concatArrayOfIterables() {
		List<Integer> list1 = new ArrayList<>(asList(1, 2, 3));
		List<Integer> list2 = new ArrayList<>(asList(4, 5, 6));
		List<Integer> list3 = new ArrayList<>(asList(7, 8, 9));

		Sequence<Integer> sequence = Sequence.concat(list1::iterator, list2::iterator, list3::iterator);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		list1.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 17, 4, 5, 6, 7, 8, 9)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void concatArrayOfCollections() {
		ArrayDeque<Integer> collection1 = new ArrayDeque<>(asList(1, 2, 3));
		ArrayDeque<Integer> collection2 = new ArrayDeque<>(asList(4, 5, 6));
		ArrayDeque<Integer> collection3 = new ArrayDeque<>(asList(7, 8, 9));

		Sequence<Integer> sequence = Sequence.concat(collection1, collection2, collection3);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		collection1.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 17, 4, 5, 6, 7, 8, 9)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void concatArrayOfLists() {
		List<Integer> list1 = new ArrayList<>(asList(1, 2, 3));
		List<Integer> list2 = new ArrayList<>(asList(4, 5, 6));
		List<Integer> list3 = new ArrayList<>(asList(7, 8, 9));

		Sequence<Integer> sequence = Sequence.concat(list1, list2, list3);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		list1.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 17, 4, 5, 6, 7, 8, 9)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void concatArrayOfNoIterables() {
		Sequence<Integer> sequence = Sequence.concat();
		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void concatIterableOfIterables() {
		List<Integer> list1 = new ArrayList<>(asList(1, 2, 3));
		List<Integer> list2 = new ArrayList<>(asList(4, 5, 6));
		List<Integer> list3 = new ArrayList<>(asList(7, 8, 9));
		List<Iterable<Integer>> listList = new ArrayList<>(asList(list1::iterator, list2::iterator, list3::iterator));

		Sequence<Integer> sequence = Sequence.concat((Iterable<Iterable<Integer>>) listList::iterator);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		list1.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 17, 4, 5, 6, 7, 8, 9)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		listList.add(new ArrayList<>(asList(10, 11, 12)));
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}

	@Test
	public void concatIterableOfCollections() {
		ArrayDeque<Integer> collection1 = new ArrayDeque<>(asList(1, 2, 3));
		ArrayDeque<Integer> collection2 = new ArrayDeque<>(asList(4, 5, 6));
		ArrayDeque<Integer> collection3 = new ArrayDeque<>(asList(7, 8, 9));
		Collection<Iterable<Integer>> collectionCollection = new ArrayDeque<>(
				asList(collection1, collection2, collection3));

		Sequence<Integer> sequence = Sequence.concat((Iterable<Iterable<Integer>>) collectionCollection::iterator);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		collection1.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 17, 4, 5, 6, 7, 8, 9)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		collectionCollection.add(new ArrayDeque<>(asList(10, 11, 12)));
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}

	@Test
	public void concatIterableOfLists() {
		List<Integer> list1 = new ArrayList<>(asList(1, 2, 3));
		List<Integer> list2 = new ArrayList<>(asList(4, 5, 6));
		List<Integer> list3 = new ArrayList<>(asList(7, 8, 9));
		List<Iterable<Integer>> listList = new ArrayList<>(asList(list1, list2, list3));

		Sequence<Integer> sequence = Sequence.concat((Iterable<Iterable<Integer>>) listList::iterator);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		list1.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 17, 4, 5, 6, 7, 8, 9)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		listList.add(new ArrayList<>(asList(10, 11, 12)));
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}

	@Test
	public void concatIterableOfNoIterables() {
		Sequence<Integer> sequence = Sequence.concat(Iterables.empty());
		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void concatListOfLists() {
		List<Integer> list1 = new ArrayList<>(asList(1, 2, 3));
		List<Integer> list2 = new ArrayList<>(asList(4, 5, 6));
		List<Integer> list3 = new ArrayList<>(asList(7, 8, 9));
		List<Iterable<Integer>> listList = new ArrayList<>(asList(list1, list2, list3));

		Sequence<Integer> sequence = Sequence.concat(listList);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 17)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		listList.add(new ArrayList<>(asList(10, 11, 12)));
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}

	@Test
	public void concatCollectionOfCollections() {
		Collection<Integer> collection1 = new ArrayDeque<>(asList(1, 2, 3));
		Collection<Integer> collection2 = new ArrayDeque<>(asList(4, 5, 6));
		Collection<Integer> collection3 = new ArrayDeque<>(asList(7, 8, 9));
		Collection<Iterable<Integer>> collectionCollection = new ArrayDeque<>(
				asList(collection1, collection2, collection3));

		Sequence<Integer> sequence = Sequence.concat(collectionCollection);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 17)));

		sequence.remove(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		collectionCollection.add(new ArrayDeque<>(asList(10, 11, 12)));
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}

	@Test
	public void cacheCollection() {
		List<Integer> list = new ArrayList<>(asList(1, 2, 3, 4, 5));
		Sequence<Integer> cached = Sequence.cache(list);
		list.set(0, 17);

		twice(() -> assertThat(cached, contains(1, 2, 3, 4, 5)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void cacheIterable() {
		List<Integer> list = new ArrayList<>(asList(1, 2, 3, 4, 5));
		Sequence<Integer> cached = Sequence.cache(list::iterator);
		list.set(0, 17);

		twice(() -> assertThat(cached, contains(1, 2, 3, 4, 5)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void cacheIterator() {
		List<Integer> list = new ArrayList<>(asList(1, 2, 3, 4, 5));
		Sequence<Integer> cached = Sequence.cache(list.iterator());
		list.set(0, 17);

		twice(() -> assertThat(cached, contains(1, 2, 3, 4, 5)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void cacheStream() {
		List<Integer> list = new ArrayList<>(asList(1, 2, 3, 4, 5));
		Sequence<Integer> cached = Sequence.cache(list.stream());
		list.set(0, 17);

		twice(() -> assertThat(cached, contains(1, 2, 3, 4, 5)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void create() {
		Sequence<Integer> sequence = Sequence.create();
		twice(() -> assertThat(sequence, is(emptyIterable())));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(17)));
	}

	@Test
	public void withCapacity() {
		Sequence<Integer> sequence = Sequence.withCapacity(1);
		sequence.addAll(asList(1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void createOfNone() {
		Sequence<Integer> sequence = Sequence.createOf();
		twice(() -> assertThat(sequence, is(emptyIterable())));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(17)));
	}

	@Test
	public void createOfOne() {
		Sequence<Integer> sequence = Sequence.createOf(1);
		twice(() -> assertThat(sequence, contains(1)));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(1, 17)));
	}

	@Test
	public void createOfMany() {
		Sequence<Integer> sequence = Sequence.createOf(1, 2, 3, 4, 5);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5)));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 17)));
	}

	@Test
	public void createOfNulls() {
		Sequence<Integer> sequence = Sequence.createOf(1, null, 2, 3, null);
		twice(() -> assertThat(sequence, contains(1, null, 2, 3, null)));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(1, null, 2, 3, null, 17)));
	}

	@Test
	public void createFromCollectionAsIterable() {
		Collection<Integer> backing = new ArrayDeque<>(asList(1, 2, 3, 4, 5));
		Sequence<Integer> sequence = Sequence.createFrom((Iterable<Integer>) backing);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5)));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 17)));
		assertThat(backing, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void createFromIterable() {
		Collection<Integer> backing = new ArrayDeque<>(asList(1, 2, 3, 4, 5));
		Sequence<Integer> sequence = Sequence.createFrom(backing::iterator);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5)));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 17)));
		assertThat(backing, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void createFromCollection() {
		Collection<Integer> backing = new ArrayDeque<>(asList(1, 2, 3, 4, 5));
		Sequence<Integer> sequence = Sequence.createFrom(backing);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5)));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 17)));
		assertThat(backing, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void createFromIterator() {
		Sequence<Integer> sequence = Sequence.createFrom(Iterators.of(1, 2, 3, 4, 5));
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5)));

		sequence.add(17);
		twice(() -> assertThat(sequence, contains(1, 2, 3, 4, 5, 17)));
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
		Sequence<Integer> noneSkipped = _123.skip(0);
		twice(() -> assertThat(noneSkipped, contains(1, 2, 3)));

		Sequence<Integer> oneSkipped = _123.skip(1);
		twice(() -> assertThat(oneSkipped, contains(2, 3)));

		Sequence<Integer> twoSkipped = _123.skip(2);
		twice(() -> assertThat(twoSkipped, contains(3)));

		Sequence<Integer> threeSkipped = _123.skip(3);
		twice(() -> assertThat(threeSkipped, is(emptyIterable())));

		Sequence<Integer> fourSkipped = _123.skip(4);
		twice(() -> assertThat(fourSkipped, is(emptyIterable())));

		expecting(NoSuchElementException.class, () -> fourSkipped.iterator().next());

		assertThat(removeFirst(noneSkipped), is(1));
		twice(() -> assertThat(noneSkipped, contains(2, 3)));
		twice(() -> assertThat(_123, contains(2, 3)));
	}

	@Test
	public void skipTail() {
		Sequence<Integer> noneSkipped = _123.skipTail(0);
		twice(() -> assertThat(noneSkipped, contains(1, 2, 3)));

		Sequence<Integer> oneSkipped = _123.skipTail(1);
		twice(() -> assertThat(oneSkipped, contains(1, 2)));

		Sequence<Integer> twoSkipped = _123.skipTail(2);
		twice(() -> assertThat(twoSkipped, contains(1)));

		Sequence<Integer> threeSkipped = _123.skipTail(3);
		twice(() -> assertThat(threeSkipped, is(emptyIterable())));

		Sequence<Integer> fourSkipped = _123.skipTail(4);
		twice(() -> assertThat(fourSkipped, is(emptyIterable())));

		expecting(NoSuchElementException.class, () -> fourSkipped.iterator().next());

		assertThat(removeFirst(noneSkipped), is(1));
		twice(() -> assertThat(noneSkipped, contains(2, 3)));
		twice(() -> assertThat(_123, contains(2, 3)));
	}

	@Test
	public void limit() {
		Sequence<Integer> limitToNone = _123.limit(0);
		twice(() -> assertThat(limitToNone, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> limitToNone.iterator().next());

		Sequence<Integer> limitToOne = _123.limit(1);
		twice(() -> assertThat(limitToOne, contains(1)));

		Sequence<Integer> limitToTwo = _123.limit(2);
		twice(() -> assertThat(limitToTwo, contains(1, 2)));

		Sequence<Integer> limitToThree = _123.limit(3);
		twice(() -> assertThat(limitToThree, contains(1, 2, 3)));

		Sequence<Integer> limitToFour = _123.limit(4);
		twice(() -> assertThat(limitToFour, contains(1, 2, 3)));

		assertThat(removeFirst(limitToFour), is(1));
		twice(() -> assertThat(limitToFour, contains(2, 3)));
		twice(() -> assertThat(_123, contains(2, 3)));
	}

	@Test
	public void appendEmpty() {
		Sequence<Integer> appendedEmpty = empty.append(Iterables.empty());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().next());
	}

	@Test
	public void append() {
		Sequence<Integer> appended = _123.append(Iterables.of(4, 5, 6))
		                                 .append(Iterables.of(7, 8));

		twice(() -> assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8)));
	}

	@Test
	public void appendEmptyIterator() {
		Sequence<Integer> appendedEmpty = empty.append(Iterators.empty());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().next());
	}

	@Test
	public void appendIterator() {
		Sequence<Integer> appended = _123.append(Iterators.of(4, 5, 6)).append(Iterators.of(7, 8));

		assertThat(appended, contains(1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(appended, contains(1, 2, 3));
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
	}

	@Test
	public void appendEmptyArray() {
		Sequence<Integer> appendedEmpty = empty.append();
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().next());
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
		Sequence<Integer> emptyFiltered = empty.filter(i -> (i % 2) == 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().next());

		Sequence<Integer> oneFiltered = _1.filter(i -> (i % 2) == 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		Sequence<Integer> twoFiltered = _12.filter(i -> (i % 2) == 0);
		twice(() -> assertThat(twoFiltered, contains(2)));

		assertThat(removeFirst(twoFiltered), is(2));
		twice(() -> assertThat(twoFiltered, is(emptyIterable())));
		twice(() -> assertThat(_12, contains(1)));

		Sequence<Integer> nineFiltered = _123456789.filter(i -> (i % 2) == 0);
		twice(() -> assertThat(nineFiltered, contains(2, 4, 6, 8)));
	}

	@Test
	public void filterIndexed() {
		Sequence<Integer> emptyFiltered = empty.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().next());

		Sequence<Integer> oneFiltered = _1.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		Sequence<Integer> twoFiltered = _12.filterIndexed((i, x) -> x > 0);
		twice(() -> assertThat(twoFiltered, contains(2)));

		assertThat(removeFirst(twoFiltered), is(2));
		twice(() -> assertThat(twoFiltered, is(emptyIterable())));
		twice(() -> assertThat(_12, contains(1)));

		Sequence<Integer> nineFiltered = _123456789.filterIndexed((i, x) -> x > 3);
		twice(() -> assertThat(nineFiltered, contains(5, 6, 7, 8, 9)));
	}

	@Test
	public void filterInstanceOf() {
		Sequence<String> strings = mixed.filter(String.class);
		twice(() -> assertThat(strings, contains("1", "2", "3")));

		Sequence<Number> numbers = mixed.filter(Number.class);
		twice(() -> assertThat(numbers, contains(1, 1.0, 2, 2.0, 3, 3.0)));

		Sequence<Integer> integers = mixed.filter(Integer.class);
		twice(() -> assertThat(integers, contains(1, 2, 3)));

		Sequence<Double> doubles = mixed.filter(Double.class);
		twice(() -> assertThat(doubles, contains(1.0, 2.0, 3.0)));

		Sequence<Character> chars = mixed.filter(Character.class);
		twice(() -> assertThat(chars, contains('x', 'y', 'z')));

		assertThat(removeFirst(chars), is('x'));
		twice(() -> assertThat(chars, contains('y', 'z')));
		twice(() -> assertThat(mixed, contains("1", 1, 1.0, "2", 2, 'y', 2.0, "3", 3, 'z', 3.0)));
	}

	@Test
	public void filterBack() {
		Sequence<Integer> emptyFilteredLess = empty.filterBack((p, i) -> p == null || p < i);
		twice(() -> assertThat(emptyFilteredLess, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().next());

		Sequence<Integer> filteredLess = nineRandom.filterBack((p, i) -> p == null || p < i);
		twice(() -> assertThat(filteredLess, contains(67, 43, 5, 7, 24, 67)));

		Sequence<Integer> filteredGreater = nineRandom.filterBack((p, i) -> p == null || p > i);
		twice(() -> assertThat(filteredGreater, contains(67, 5, 3, 5)));

		assertThat(removeFirst(filteredGreater), is(67));
		twice(() -> assertThat(filteredGreater, contains(5, 3, 5)));
		twice(() -> assertThat(nineRandom, contains(5, 43, 3, 5, 7, 24, 5, 67)));
	}

	@Test
	public void filterBackWithReplacement() {
		Sequence<Integer> emptyFilteredLess = empty.filterBack(117, (p, i) -> p < i);
		twice(() -> assertThat(emptyFilteredLess, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().next());

		Sequence<Integer> filteredLess = nineRandom.filterBack(117, (p, i) -> p < i);
		twice(() -> assertThat(filteredLess, contains(43, 5, 7, 24, 67)));

		Sequence<Integer> filteredGreater = nineRandom.filterBack(117, (p, i) -> p > i);
		twice(() -> assertThat(filteredGreater, contains(67, 5, 3, 5)));

		assertThat(removeFirst(filteredGreater), is(67));
		twice(() -> assertThat(filteredGreater, contains(5, 3, 5)));
		twice(() -> assertThat(nineRandom, contains(5, 43, 3, 5, 7, 24, 5, 67)));
	}

	@Test
	public void filterForward() {
		Sequence<Integer> emptyFilteredLess = empty.filterForward((i, f) -> f == null || f < i);
		twice(() -> assertThat(emptyFilteredLess, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().next());

		Sequence<Integer> filteredLess = nineRandom.filterForward((i, f) -> f == null || f < i);
		twice(() -> assertThat(filteredLess, contains(67, 43, 24, 67)));

		Sequence<Integer> filteredGreater = nineRandom.filterForward((i, f) -> f == null || f > i);
		twice(() -> assertThat(filteredGreater, contains(5, 3, 5, 7, 5, 67)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(filteredGreater));
		twice(() -> assertThat(filteredGreater, contains(5, 3, 5, 7, 5, 67)));
	}

	@Test
	public void filterForwardWithReplacement() {
		Sequence<Integer> emptyFilteredLess = empty.filterForward(117, (i, f) -> f < i);
		twice(() -> assertThat(emptyFilteredLess, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFilteredLess.iterator().next());

		Sequence<Integer> filteredLess = nineRandom.filterForward(117, (i, f) -> f < i);
		twice(() -> assertThat(filteredLess, contains(67, 43, 24)));

		Sequence<Integer> filteredGreater = nineRandom.filterForward(117, (i, f) -> f > i);
		twice(() -> assertThat(filteredGreater, contains(5, 3, 5, 7, 5, 67)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(filteredGreater));
		twice(() -> assertThat(filteredGreater, contains(5, 3, 5, 7, 5, 67)));
	}

	@Test
	public void includingArray() {
		Sequence<Integer> emptyIncluding = empty.including(1, 3, 5, 17);
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyIncluding.iterator().next());

		Sequence<Integer> including = _12345.including(1, 3, 5, 17);
		twice(() -> assertThat(including, contains(1, 3, 5)));

		Sequence<Integer> includingAll = _12345.including(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(includingAll, contains(1, 2, 3, 4, 5)));

		Sequence<Integer> includingNone = _12345.including();
		twice(() -> assertThat(includingNone, is(emptyIterable())));

		assertThat(removeFirst(including), is(1));
		twice(() -> assertThat(including, contains(3, 5)));
		twice(() -> assertThat(_12345, contains(2, 3, 4, 5)));
	}

	@Test
	public void includingIterable() {
		Sequence<Integer> emptyIncluding = empty.including(Iterables.of(1, 3, 5, 17));
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyIncluding.iterator().next());

		Sequence<Integer> including = _12345.including(Iterables.of(1, 3, 5, 17));
		twice(() -> assertThat(including, contains(1, 3, 5)));

		Sequence<Integer> includingAll = _12345.including(Iterables.of(1, 2, 3, 4, 5, 17));
		twice(() -> assertThat(includingAll, contains(1, 2, 3, 4, 5)));

		Sequence<Integer> includingNone = _12345.including(Iterables.of());
		twice(() -> assertThat(includingNone, is(emptyIterable())));

		assertThat(removeFirst(including), is(1));
		twice(() -> assertThat(including, contains(3, 5)));
		twice(() -> assertThat(_12345, contains(2, 3, 4, 5)));
	}

	@Test
	public void excludingArray() {
		Sequence<Integer> emptyExcluding = empty.excluding(1, 3, 5, 17);
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyExcluding.iterator().next());

		Sequence<Integer> excluding = _12345.excluding(1, 3, 5, 17);
		twice(() -> assertThat(excluding, contains(2, 4)));

		Sequence<Integer> excludingAll = _12345.excluding(1, 2, 3, 4, 5, 17);
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		Sequence<Integer> excludingNone = _12345.excluding();
		twice(() -> assertThat(excludingNone, contains(1, 2, 3, 4, 5)));

		assertThat(removeFirst(excluding), is(2));
		twice(() -> assertThat(excluding, contains(4)));
		twice(() -> assertThat(_12345, contains(1, 3, 4, 5)));
	}

	@Test
	public void excludingIterable() {
		Sequence<Integer> emptyExcluding = empty.excluding(Iterables.of(1, 3, 5, 17));
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyExcluding.iterator().next());

		Sequence<Integer> excluding = _12345.excluding(Iterables.of(1, 3, 5, 17));
		twice(() -> assertThat(excluding, contains(2, 4)));

		Sequence<Integer> excludingAll = _12345.excluding(Iterables.of(1, 2, 3, 4, 5, 17));
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		Sequence<Integer> excludingNone = _12345.excluding(Iterables.of());
		twice(() -> assertThat(excludingNone, contains(1, 2, 3, 4, 5)));

		assertThat(removeFirst(excluding), is(2));
		twice(() -> assertThat(excluding, contains(4)));
		twice(() -> assertThat(_12345, contains(1, 3, 4, 5)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flatMapIterables() {
		Function<Iterable<Integer>, Iterable<Integer>> identity = Function.identity();

		Sequence<Integer> emptyFlatMap = this.<Iterable<Integer>>newSequence().flatten(identity);
		twice(() -> assertThat(emptyFlatMap, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFlatMap.iterator().next());

		Sequence<Integer> flatMap = newSequence(Iterables.of(1, 2), Iterables.of(3, 4), Iterables.of(5, 6))
				.flatten(identity);
		twice(() -> assertThat(flatMap, contains(1, 2, 3, 4, 5, 6)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(flatMap));
		twice(() -> assertThat(flatMap, contains(1, 2, 3, 4, 5, 6)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flatMapLazy() {
		Function<Iterable<Integer>, Iterable<Integer>> identity = Function.identity();

		Sequence<Integer> flatMap = newSequence(Iterables.of(1, 2), () -> {
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
		Sequence<Iterator<Integer>> sequence = newSequence(Iterators.of(1, 2), Iterators.of(3, 4), Iterators.of(5, 6));

		Sequence<Integer> flatMap = sequence.flatten(Iterables::once);

		assertThat(flatMap, contains(1, 2, 3, 4, 5, 6));
		assertThat(flatMap, is(emptyIterable()));
	}

	@Test
	public void flatMapArrays() {
		Sequence<Integer[]> sequence = newSequence(new Integer[]{1, 2}, new Integer[]{3, 4}, new Integer[]{5, 6});

		Sequence<Integer> flatMap = sequence.flatten(Iterables::of);

		twice(() -> assertThat(flatMap, contains(1, 2, 3, 4, 5, 6)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flattenIterables() {
		Sequence<Integer> emptyFlattened = newSequence().flatten();
		twice(() -> assertThat(emptyFlattened, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFlattened.iterator().next());

		Sequence<Integer> flattened = newSequence(Iterables.of(1, 2), Iterables.of(3, 4), Iterables.of(5, 6))
				.flatten();
		twice(() -> assertThat(flattened, contains(1, 2, 3, 4, 5, 6)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(flattened));
		twice(() -> assertThat(flattened, contains(1, 2, 3, 4, 5, 6)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flattenLazy() {
		Sequence<Integer> flattened = newSequence(Iterables.of(1, 2), () -> {
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
		Sequence<Iterator<Integer>> sequence = newSequence(Iterators.of(1, 2), Iterators.of(3, 4), Iterators.of(5, 6));
		Sequence<Integer> flattened = sequence.flatten();
		assertThat(flattened, contains(1, 2, 3, 4, 5, 6));
		assertThat(flattened, is(emptyIterable()));
	}

	@Test
	public void flattenStreams() {
		Sequence<Stream<Integer>> sequence = newSequence(Stream.of(1, 2), Stream.of(3, 4), Stream.of(5, 6));

		Sequence<Integer> flattened = sequence.flatten();
		assertThat(flattened, contains(1, 2, 3, 4, 5, 6));

		Iterator<Integer> iterator = flattened.iterator();
		expecting(IllegalStateException.class, iterator::next);
	}

	@Test
	public void flattenArrays() {
		Sequence<Integer[]> sequence = newSequence(new Integer[]{1, 2}, new Integer[]{3, 4}, new Integer[]{5, 6});

		Sequence<Integer> flattened = sequence.flatten();
		twice(() -> assertThat(flattened, contains(1, 2, 3, 4, 5, 6)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flattenPairs() {
		Sequence<Pair<String, Integer>> sequence = newSequence(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3));

		Sequence<?> flattened = sequence.flatten();
		twice(() -> assertThat(flattened, contains("1", 1, "2", 2, "3", 3)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flattenEntries() {
		Sequence<Entry<String, Integer>> sequence = newSequence(Maps.entry("1", 1), Maps.entry("2", 2),
		                                                        Maps.entry("3", 3));

		Sequence<?> flattened = sequence.flatten();
		twice(() -> assertThat(flattened, contains("1", 1, "2", 2, "3", 3)));
	}

	@Test
	public void flattenInvalid() {
		Iterator<?> iterator = _12345.flatten().iterator();
		expecting(ClassCastException.class, iterator::next);
	}

	@Test
	public void map() {
		Sequence<String> emptyMapped = empty.map(Object::toString);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().next());

		Sequence<String> oneMapped = _1.map(Object::toString);
		twice(() -> assertThat(oneMapped, contains("1")));

		assertThat(removeFirst(oneMapped), is("1"));
		twice(() -> assertThat(oneMapped, is(emptyIterable())));
		twice(() -> assertThat(_1, is(emptyIterable())));

		Sequence<String> twoMapped = _12.map(Object::toString);
		twice(() -> assertThat(twoMapped, contains("1", "2")));

		Sequence<String> fiveMapped = _12345.map(Object::toString);
		twice(() -> assertThat(fiveMapped, contains("1", "2", "3", "4", "5")));
	}

	@Test
	public void biMap() {
		Sequence<String> emptyMapped = empty.biMap(Object::toString, Integer::parseInt);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().next());

		Sequence<String> oneMapped = _1.biMap(Object::toString, Integer::parseInt);
		twice(() -> assertThat(oneMapped, contains("1")));

		assertThat(removeFirst(oneMapped), is("1"));
		twice(() -> assertThat(oneMapped, is(emptyIterable())));
		twice(() -> assertThat(_1, is(emptyIterable())));

		Sequence<String> twoMapped = _12.biMap(Object::toString, Integer::parseInt);
		twice(() -> assertThat(twoMapped, contains("1", "2")));

		Sequence<String> fiveMapped = _12345.biMap(Object::toString, Integer::parseInt);
		twice(() -> assertThat(fiveMapped, contains("1", "2", "3", "4", "5")));
	}

	@Test
	public void mapWithIndex() {
		Sequence<String> emptyMapped = empty.map(Object::toString);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().next());

		AtomicInteger index = new AtomicInteger();
		Sequence<String> oneMapped = _1.mapIndexed((e, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return String.valueOf(e);
		});
		twice(() -> {
			index.set(0);
			assertThat(oneMapped, contains("1"));
		});

		Sequence<String> twoMapped = _12.mapIndexed((e, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return String.valueOf(e);
		});
		twice(() -> {
			index.set(0);
			assertThat(twoMapped, contains("1", "2"));
		});

		Sequence<String> fiveMapped = _12345.mapIndexed((e, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return String.valueOf(e);
		});
		twice(() -> {
			index.set(0);
			assertThat(fiveMapped, contains("1", "2", "3", "4", "5"));
		});
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
		twice(() -> assertThat(emptyMappedBack, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedBack.iterator().next());

		Sequence<Integer> fiveMappedBackToPrevious = _12345.mapBack((p, c) -> p);
		twice(() -> assertThat(fiveMappedBackToPrevious, contains(null, 1, 2, 3, 4)));

		Sequence<Integer> fiveMappedBackToCurrent = _12345.mapBack((p, c) -> c);
		twice(() -> assertThat(fiveMappedBackToCurrent, contains(1, 2, 3, 4, 5)));

		assertThat(removeFirst(fiveMappedBackToCurrent), is(1));
		twice(() -> assertThat(fiveMappedBackToCurrent, contains(2, 3, 4, 5)));
		twice(() -> assertThat(_12345, contains(2, 3, 4, 5)));
	}

	@Test
	public void mapForward() {
		Sequence<Integer> emptyMappedForward = empty.mapForward((c, n) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedForward, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedForward.iterator().next());

		Sequence<Integer> fiveMappedForwardToCurrent = _12345.mapForward((c, n) -> c);
		twice(() -> assertThat(fiveMappedForwardToCurrent, contains(1, 2, 3, 4, 5)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(fiveMappedForwardToCurrent));
		twice(() -> assertThat(fiveMappedForwardToCurrent, contains(1, 2, 3, 4, 5)));

		Sequence<Integer> fiveMappedForwardToNext = _12345.mapForward((c, n) -> n);
		twice(() -> assertThat(fiveMappedForwardToNext, contains(2, 3, 4, 5, null)));
	}

	@Test
	public void mapBackWithReplacement() {
		Sequence<Integer> emptyMappedBack = empty.mapBack(117, (p, n) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedBack, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedBack.iterator().next());

		Sequence<Integer> fiveMappedBackToPrevious = _12345.mapBack(117, (p, n) -> p);
		twice(() -> assertThat(fiveMappedBackToPrevious, contains(117, 1, 2, 3, 4)));

		Sequence<Integer> fiveMappedBackToCurrent = _12345.mapBack(117, (p, n) -> n);
		twice(() -> assertThat(fiveMappedBackToCurrent, contains(1, 2, 3, 4, 5)));

		assertThat(removeFirst(fiveMappedBackToCurrent), is(1));
		twice(() -> assertThat(fiveMappedBackToCurrent, contains(2, 3, 4, 5)));
		twice(() -> assertThat(_12345, contains(2, 3, 4, 5)));
	}

	@Test
	public void mapForwardWithReplacement() {
		Sequence<Integer> emptyMappedForward = empty.mapForward(117, (c, n) -> {
			throw new IllegalStateException("should not get called");
		});
		twice(() -> assertThat(emptyMappedForward, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMappedForward.iterator().next());

		Sequence<Integer> fiveMappedForwardToCurrent = _12345.mapForward(117, (c, n) -> c);
		twice(() -> assertThat(fiveMappedForwardToCurrent, contains(1, 2, 3, 4, 5)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(fiveMappedForwardToCurrent));
		twice(() -> assertThat(fiveMappedForwardToCurrent, contains(1, 2, 3, 4, 5)));

		Sequence<Integer> fiveMappedForwardToNext = _12345.mapForward(117, (c, n) -> n);
		twice(() -> assertThat(fiveMappedForwardToNext, contains(2, 3, 4, 5, 117)));
	}

	@Test
	public void cast() {
		Sequence<Number> emptyCast = empty.cast(Number.class);
		twice(() -> assertThat(emptyCast, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyCast.iterator().next());

		Sequence<Number> oneCast = _1.cast(Number.class);
		twice(() -> assertThat(oneCast, contains(1)));

		assertThat(removeFirst(oneCast), is(1));
		twice(() -> assertThat(oneCast, is(emptyIterable())));

		Sequence<Number> twoCast = _12.cast(Number.class);
		twice(() -> assertThat(twoCast, contains(1, 2)));

		Sequence<Number> fiveCast = _12345.cast(Number.class);
		twice(() -> assertThat(fiveCast, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void peekBack() {
		Sequence<Integer> peekBackEmpty = empty.peekBack((previous, current) -> fail("should not get called"));
		twice(() -> assertThat(peekBackEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> peekBackEmpty.iterator().next());

		AtomicInteger value = new AtomicInteger(1);
		Sequence<Integer> fivePeekingBack = _12345.peekBack((p, n) -> {
			assertThat(n, is(value.getAndIncrement()));
			assertThat(p, is(n == 1 ? null : n - 1));
		});
		twice(() -> {
			assertThat(fivePeekingBack, contains(1, 2, 3, 4, 5));
			value.set(1);
		});

		assertThat(removeFirst(fivePeekingBack), is(1));
	}

	@Test
	public void peekForward() {
		Sequence<Integer> emptyPeekingForward = empty.peekForward((current, next) -> fail("should not get called"));
		twice(() -> assertThat(emptyPeekingForward, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeekingForward.iterator().next());

		AtomicInteger value = new AtomicInteger(1);
		Sequence<Integer> fivePeekingForward = _12345.peekForward((current, next) -> {
			assertThat(current, is(value.getAndIncrement()));
			assertThat(next, is(current == 5 ? null : current + 1));
		});
		twice(() -> {
			assertThat(fivePeekingForward, contains(1, 2, 3, 4, 5));
			value.set(1);
		});

		expecting(UnsupportedOperationException.class, () -> removeFirst(fivePeekingForward));
	}

	@Test
	public void peekBackWithReplacement() {
		Sequence<Integer> peekBackEmpty = empty.peekBack(117, (previous, current) -> fail("should not get called"));
		twice(() -> assertThat(peekBackEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> peekBackEmpty.iterator().next());

		AtomicInteger value = new AtomicInteger(1);
		Sequence<Integer> fivePeekingBack = _12345.peekBack(117, (previous, current) -> {
			assertThat(current, is(value.getAndIncrement()));
			assertThat(previous, is(current == 1 ? 117 : current - 1));
		});
		twice(() -> {
			assertThat(fivePeekingBack, contains(1, 2, 3, 4, 5));
			value.set(1);
		});

		assertThat(removeFirst(fivePeekingBack), is(1));
	}

	@Test
	public void peekForwardWithReplacement() {
		Sequence<Integer> peekForwardEmpty = empty.peekForward(117, (current, next) -> fail("should not get called"));
		twice(() -> assertThat(peekForwardEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> peekForwardEmpty.iterator().next());

		AtomicInteger value = new AtomicInteger(1);
		Sequence<Integer> fivePeekingForward = _12345.peekForward(117, (current, next) -> {
			assertThat(current, is(value.getAndIncrement()));
			assertThat(next, is(current == 5 ? 117 : current + 1));
		});
		twice(() -> {
			assertThat(fivePeekingForward, contains(1, 2, 3, 4, 5));
			value.set(1);
		});

		expecting(UnsupportedOperationException.class, () -> removeFirst(fivePeekingForward));
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
		Sequence<Integer> emptyUntil5 = empty.until(5);
		twice(() -> assertThat(emptyUntil5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntil5.iterator().next());

		Sequence<Integer> nineUntil5 = _123456789.until(5);
		twice(() -> assertThat(nineUntil5, contains(1, 2, 3, 4)));

		assertThat(removeFirst(nineUntil5), is(1));
		twice(() -> assertThat(nineUntil5, contains(2, 3, 4)));
		twice(() -> assertThat(_123456789, contains(2, 3, 4, 5, 6, 7, 8, 9)));

		Sequence<Integer> fiveUntil10 = _12345.until(10);
		twice(() -> assertThat(fiveUntil10, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void untilNull() {
		Sequence<Integer> emptyUntilNull = empty.untilNull();
		twice(() -> assertThat(emptyUntilNull, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntilNull.iterator().next());

		Sequence<Integer> nineUntilNull = Sequence.of(1, 2, 3, 4, 5, null, 7, 8, 9).untilNull();
		twice(() -> assertThat(nineUntilNull, contains(1, 2, 3, 4, 5)));

		Sequence<Integer> fiveUntilNull = _12345.untilNull();
		twice(() -> assertThat(fiveUntilNull, contains(1, 2, 3, 4, 5)));

		assertThat(removeFirst(fiveUntilNull), is(1));
		twice(() -> assertThat(fiveUntilNull, contains(2, 3, 4, 5)));
		twice(() -> assertThat(_12345, contains(2, 3, 4, 5)));
	}

	@Test
	public void untilPredicate() {
		Sequence<Integer> emptyUntilEqual5 = empty.until(i -> i == 5);
		twice(() -> assertThat(emptyUntilEqual5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntilEqual5.iterator().next());

		Sequence<Integer> nineUntilEqual5 = _123456789.until(i -> i == 5);
		twice(() -> assertThat(nineUntilEqual5, contains(1, 2, 3, 4)));

		assertThat(removeFirst(nineUntilEqual5), is(1));
		twice(() -> assertThat(nineUntilEqual5, contains(2, 3, 4)));
		twice(() -> assertThat(_123456789, contains(2, 3, 4, 5, 6, 7, 8, 9)));

		Sequence<Integer> fiveUntilEqual10 = _12345.until(i -> i == 10);
		twice(() -> assertThat(fiveUntilEqual10, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void endingAtTerminal() {
		Sequence<Integer> emptyEndingAt5 = empty.endingAt(5);
		twice(() -> assertThat(emptyEndingAt5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAt5.iterator().next());

		Sequence<Integer> nineEndingAt5 = _123456789.endingAt(5);
		twice(() -> assertThat(nineEndingAt5, contains(1, 2, 3, 4, 5)));

		assertThat(removeFirst(nineEndingAt5), is(1));
		twice(() -> assertThat(nineEndingAt5, contains(2, 3, 4, 5)));

		Sequence<Integer> fiveEndingAt10 = _12345.endingAt(10);
		twice(() -> assertThat(fiveEndingAt10, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void endingAtNull() {
		Sequence<Integer> emptyEndingAtNull = empty.endingAtNull();
		twice(() -> assertThat(emptyEndingAtNull, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAtNull.iterator().next());

		Sequence<Integer> nineEndingAtNull = Sequence.of(1, 2, 3, 4, 5, null, 7, 8, 9).endingAtNull();
		twice(() -> assertThat(nineEndingAtNull, contains(1, 2, 3, 4, 5, null)));

		Sequence<Integer> fiveEndingAtNull = _12345.endingAtNull();
		twice(() -> assertThat(fiveEndingAtNull, contains(1, 2, 3, 4, 5)));

		assertThat(removeFirst(fiveEndingAtNull), is(1));
		twice(() -> assertThat(fiveEndingAtNull, contains(2, 3, 4, 5)));
		twice(() -> assertThat(_12345, contains(2, 3, 4, 5)));
	}

	@Test
	public void endingAtPredicate() {
		Sequence<Integer> emptyEndingAtEqual5 = empty.endingAt(i -> i == 5);
		twice(() -> assertThat(emptyEndingAtEqual5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAtEqual5.iterator().next());

		Sequence<Integer> nineEndingAtEqual5 = _123456789.endingAt(i -> i == 5);
		twice(() -> assertThat(nineEndingAtEqual5, contains(1, 2, 3, 4, 5)));

		assertThat(removeFirst(nineEndingAtEqual5), is(1));
		twice(() -> assertThat(nineEndingAtEqual5, contains(2, 3, 4, 5)));
		twice(() -> assertThat(_123456789, contains(2, 3, 4, 5, 6, 7, 8, 9)));

		Sequence<Integer> fiveEndingAtEqual10 = _12345.endingAt(i -> i == 10);
		twice(() -> assertThat(fiveEndingAtEqual10, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void startingAfter() {
		Sequence<Integer> emptyStartingAfter5 = empty.startingAfter(5);
		twice(() -> assertThat(emptyStartingAfter5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingAfter5.iterator().next());

		Sequence<Integer> nineStartingAfter5 = _123456789.startingAfter(5);
		twice(() -> assertThat(nineStartingAfter5, contains(6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingAfter5), is(6));
		twice(() -> assertThat(nineStartingAfter5, contains(7, 8, 9)));
		twice(() -> assertThat(_123456789, contains(1, 2, 3, 4, 5, 7, 8, 9)));

		Sequence<Integer> fiveStartingAfter10 = _12345.startingAfter(10);
		twice(() -> assertThat(fiveStartingAfter10, is(emptyIterable())));
	}

	@Test
	public void startingAfterPredicate() {
		Sequence<Integer> emptyStartingAfterEqual5 = empty.startingAfter(i -> i == 5);
		twice(() -> assertThat(emptyStartingAfterEqual5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingAfterEqual5.iterator().next());

		Sequence<Integer> nineStartingAfterEqual5 = _123456789.startingAfter(i -> i == 5);
		twice(() -> assertThat(nineStartingAfterEqual5, contains(6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingAfterEqual5), is(6));
		twice(() -> assertThat(nineStartingAfterEqual5, contains(7, 8, 9)));
		twice(() -> assertThat(_123456789, contains(1, 2, 3, 4, 5, 7, 8, 9)));

		Sequence<Integer> fiveStartingAfterEqual10 = _12345.startingAfter(i -> i == 10);
		twice(() -> assertThat(fiveStartingAfterEqual10, is(emptyIterable())));
	}

	@Test
	public void startingFrom() {
		Sequence<Integer> emptyStartingFrom5 = empty.startingFrom(5);
		twice(() -> assertThat(emptyStartingFrom5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingFrom5.iterator().next());

		Sequence<Integer> nineStartingFrom5 = _123456789.startingFrom(5);
		twice(() -> assertThat(nineStartingFrom5, contains(5, 6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingFrom5), is(5));
		twice(() -> assertThat(nineStartingFrom5, is(emptyIterable())));
		twice(() -> assertThat(_123456789, contains(1, 2, 3, 4, 6, 7, 8, 9)));

		Sequence<Integer> fiveStartingFrom10 = _12345.startingFrom(10);
		twice(() -> assertThat(fiveStartingFrom10, is(emptyIterable())));
	}

	@Test
	public void startingFromPredicate() {
		Sequence<Integer> emptyStartingFromEqual5 = empty.startingFrom(i -> i == 5);
		twice(() -> assertThat(emptyStartingFromEqual5, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStartingFromEqual5.iterator().next());

		Sequence<Integer> nineStartingFromEqual5 = _123456789.startingFrom(i -> i == 5);
		twice(() -> assertThat(nineStartingFromEqual5, contains(5, 6, 7, 8, 9)));

		assertThat(removeFirst(nineStartingFromEqual5), is(5));
		twice(() -> assertThat(nineStartingFromEqual5, is(emptyIterable())));
		twice(() -> assertThat(_123456789, contains(1, 2, 3, 4, 6, 7, 8, 9)));

		Sequence<Integer> fiveStartingFromEqual10 = _12345.startingFrom(i -> i == 10);
		twice(() -> assertThat(fiveStartingFromEqual10, is(emptyIterable())));
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
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(Optional.empty()));
			assertThat(_1.first(), is(Optional.of(1)));
			assertThat(_12.first(), is(Optional.of(1)));
			assertThat(_12345.first(), is(Optional.of(1)));
		});
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
		});
	}

	@Test
	public void firstByPredicate() {
		twice(() -> {
			assertThat(empty.first(x -> x > 1), is(Optional.empty()));
			assertThat(_1.first(x -> x > 1), is(Optional.empty()));
			assertThat(_12.first(x -> x > 1), is(Optional.of(2)));
			assertThat(_12345.first(x -> x > 1), is(Optional.of(2)));
		});
	}

	@Test
	public void lastByPredicate() {
		twice(() -> {
			assertThat(empty.last(x -> x > 1), is(Optional.empty()));
			assertThat(_1.last(x -> x > 1), is(Optional.empty()));
			assertThat(_12.last(x -> x > 1), is(Optional.of(2)));
			assertThat(_12345.last(x -> x > 1), is(Optional.of(5)));
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

	@SuppressWarnings("unchecked")
	@Test
	public void entries() {
		Sequence<Entry<Integer, Integer>> emptyEntries = empty.entries();
		twice(() -> assertThat(emptyEntries, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEntries.iterator().next());

		Sequence<Entry<Integer, Integer>> oneEntries = _1.entries();
		twice(() -> assertThat(oneEntries, contains(Maps.entry(1, null))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneEntries));
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
		expecting(NoSuchElementException.class, () -> emptyPaired.iterator().next());

		Sequence<Pair<Integer, Integer>> onePaired = _1.pairs();
		twice(() -> assertThat(onePaired, contains(Pair.of(1, null))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(onePaired));
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
		expecting(NoSuchElementException.class, () -> emptyEntries.iterator().next());

		Sequence<Entry<Integer, Integer>> oneEntries = _1.adjacentEntries();
		twice(() -> assertThat(oneEntries, contains(Maps.entry(1, null))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneEntries));
		twice(() -> assertThat(oneEntries, contains(Maps.entry(1, null))));

		Sequence<Entry<Integer, Integer>> twoEntries = _12.adjacentEntries();
		twice(() -> assertThat(twoEntries, contains(Maps.entry(1, 2))));

		Sequence<Entry<Integer, Integer>> threeEntries = _123.adjacentEntries();
		twice(() -> assertThat(threeEntries, contains(Maps.entry(1, 2), Maps.entry(3, null))));

		Sequence<Entry<Integer, Integer>> fourEntries = _1234.adjacentEntries();
		twice(() -> assertThat(fourEntries, contains(Maps.entry(1, 2), Maps.entry(3, 4))));

		Sequence<Entry<Integer, Integer>> fiveEntries = _12345.adjacentEntries();
		twice(() -> assertThat(fiveEntries, contains(Maps.entry(1, 2), Maps.entry(3, 4), Maps.entry(5, null))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void adjacentPairs() {
		Sequence<Pair<Integer, Integer>> emptyPaired = empty.adjacentPairs();
		twice(() -> assertThat(emptyPaired, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPaired.iterator().next());

		Sequence<Pair<Integer, Integer>> onePaired = _1.adjacentPairs();
		twice(() -> assertThat(onePaired, contains(Pair.of(1, null))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(onePaired));
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
		expecting(NoSuchElementException.class, () -> emptyBiSequence.iterator().next());

		BiSequence<Integer, String> oneBiSequence = newSequence(Pair.of(1, "1")).toBiSequence();
		twice(() -> assertThat(oneBiSequence, contains(Pair.of(1, "1"))));

		assertThat(removeFirst(oneBiSequence), is(Pair.of(1, "1")));
		twice(() -> assertThat(oneBiSequence, is(emptyIterable())));

		BiSequence<Integer, String> twoBiSequence = newSequence(Pair.of(1, "1"), Pair.of(2, "2")).toBiSequence();
		twice(() -> assertThat(twoBiSequence, contains(Pair.of(1, "1"), Pair.of(2, "2"))));

		BiSequence<Integer, String> threeBiSequence = newSequence(Pair.of(1, "1"), Pair.of(2, "2"),
		                                                          Pair.of(3, "3")).toBiSequence();
		twice(() -> assertThat(threeBiSequence, contains(Pair.of(1, "1"), Pair.of(2, "2"), Pair.of(3, "3"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void entrySequence() {
		EntrySequence<Integer, String> emptyEntrySequence = empty.toEntrySequence();
		twice(() -> assertThat(emptyEntrySequence, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEntrySequence.iterator().next());

		EntrySequence<Integer, String> oneEntrySequence = newSequence(Maps.entry(1, "1")).toEntrySequence();
		twice(() -> assertThat(oneEntrySequence, contains(Maps.entry(1, "1"))));

		assertThat(removeFirst(oneEntrySequence), is(Maps.entry(1, "1")));
		twice(() -> assertThat(oneEntrySequence, is(emptyIterable())));

		EntrySequence<Integer, String> twoEntrySequence = newSequence(Maps.entry(1, "1"),
		                                                              Maps.entry(2, "2")).toEntrySequence();
		twice(() -> assertThat(twoEntrySequence, contains(Maps.entry(1, "1"), Maps.entry(2, "2"))));

		EntrySequence<Integer, String> threeEntrySequence = newSequence(Maps.entry(1, "1"), Maps.entry(2, "2"),
		                                                                Maps.entry(3, "3")).toEntrySequence();
		twice(() -> assertThat(threeEntrySequence,
		                       contains(Maps.entry(1, "1"), Maps.entry(2, "2"), Maps.entry(3, "3"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void window() {
		Sequence<Sequence<Integer>> emptyWindowed = empty.window(3);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<Sequence<Integer>> oneWindowed = _1.window(3);
		twice(() -> assertThat(oneWindowed, contains(contains(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(contains(1))));

		Sequence<Sequence<Integer>> twoWindowed = _12.window(3);
		twice(() -> assertThat(twoWindowed, contains(contains(1, 2))));

		Sequence<Sequence<Integer>> threeWindowed = _123.window(3);
		twice(() -> assertThat(threeWindowed, contains(contains(1, 2, 3))));

		Sequence<Sequence<Integer>> fourWindowed = _1234.window(3);
		twice(() -> assertThat(fourWindowed, contains(contains(1, 2, 3), contains(2, 3, 4))));

		Sequence<Sequence<Integer>> fiveWindowed = _12345.window(3);
		twice(() -> assertThat(fiveWindowed, contains(contains(1, 2, 3), contains(2, 3, 4), contains(3, 4, 5))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithSmallerStep() {
		Sequence<Sequence<Integer>> emptyWindowed = empty.window(3, 2);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<Sequence<Integer>> oneWindowed = _1.window(3, 2);
		twice(() -> assertThat(oneWindowed, contains(contains(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(contains(1))));

		Sequence<Sequence<Integer>> twoWindowed = _12.window(3, 2);
		twice(() -> assertThat(twoWindowed, contains(contains(1, 2))));

		Sequence<Sequence<Integer>> threeWindowed = _123.window(3, 2);
		twice(() -> assertThat(threeWindowed, contains(contains(1, 2, 3))));

		Sequence<Sequence<Integer>> fourWindowed = _1234.window(3, 2);
		twice(() -> assertThat(fourWindowed, contains(contains(1, 2, 3), contains(3, 4))));

		Sequence<Sequence<Integer>> fiveWindowed = _12345.window(3, 2);
		twice(() -> assertThat(fiveWindowed, contains(contains(1, 2, 3), contains(3, 4, 5))));

		Sequence<Sequence<Integer>> nineWindowed = _123456789.window(3, 2);
		twice(() -> assertThat(nineWindowed,
		                       contains(contains(1, 2, 3), contains(3, 4, 5), contains(5, 6, 7), contains(7, 8, 9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windowWithLargerStep() {
		Sequence<Sequence<Integer>> emptyWindowed = empty.window(3, 4);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<Sequence<Integer>> oneWindowed = _1.window(3, 4);
		twice(() -> assertThat(oneWindowed, contains(contains(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneWindowed));
		twice(() -> assertThat(oneWindowed, contains(contains(1))));

		Sequence<Sequence<Integer>> twoWindowed = _12.window(3, 4);
		twice(() -> assertThat(twoWindowed, contains(contains(1, 2))));

		Sequence<Sequence<Integer>> threeWindowed = _123.window(3, 4);
		twice(() -> assertThat(threeWindowed, contains(contains(1, 2, 3))));

		Sequence<Sequence<Integer>> fourWindowed = _1234.window(3, 4);
		twice(() -> assertThat(fourWindowed, contains(contains(1, 2, 3))));

		Sequence<Sequence<Integer>> fiveWindowed = _12345.window(3, 4);
		twice(() -> assertThat(fiveWindowed, contains(contains(1, 2, 3), contains(5))));

		Sequence<Sequence<Integer>> nineWindowed = _123456789.window(3, 4);
		twice(() -> assertThat(nineWindowed, contains(contains(1, 2, 3), contains(5, 6, 7), contains(9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batch() {
		Sequence<Sequence<Integer>> emptyBatched = empty.batch(3);
		twice(() -> assertThat(emptyBatched, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<Sequence<Integer>> oneBatched = _1.batch(3);
		twice(() -> assertThat(oneBatched, contains(contains(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneBatched));
		twice(() -> assertThat(oneBatched, contains(contains(1))));

		Sequence<Sequence<Integer>> twoBatched = _12.batch(3);
		twice(() -> assertThat(twoBatched, contains(contains(1, 2))));

		Sequence<Sequence<Integer>> threeBatched = _123.batch(3);
		twice(() -> assertThat(threeBatched, contains(contains(1, 2, 3))));

		Sequence<Sequence<Integer>> fourBatched = _1234.batch(3);
		twice(() -> assertThat(fourBatched, contains(contains(1, 2, 3), contains(4))));

		Sequence<Sequence<Integer>> fiveBatched = _12345.batch(3);
		twice(() -> assertThat(fiveBatched, contains(contains(1, 2, 3), contains(4, 5))));

		Sequence<Sequence<Integer>> nineBatched = _123456789.batch(3);
		twice(() -> assertThat(nineBatched, contains(contains(1, 2, 3), contains(4, 5, 6), contains(7, 8, 9))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnPredicate() {
		Sequence<Sequence<Integer>> emptyBatched = empty.batch((a, b) -> a > b);
		twice(() -> assertThat(emptyBatched, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<Sequence<Integer>> oneBatched = _1.batch((a, b) -> a > b);
		twice(() -> assertThat(oneBatched, contains(contains(1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneBatched));
		twice(() -> assertThat(oneBatched, contains(contains(1))));

		Sequence<Sequence<Integer>> twoBatched = _12.batch((a, b) -> a > b);
		twice(() -> assertThat(twoBatched, contains(contains(1, 2))));

		Sequence<Sequence<Integer>> threeBatched = _123.batch((a, b) -> a > b);
		twice(() -> assertThat(threeBatched, contains(contains(1, 2, 3))));

		Sequence<Sequence<Integer>> threeRandomBatched = threeRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(threeRandomBatched, contains(contains(2, 3), contains(1))));

		Sequence<Sequence<Integer>> nineRandomBatched = nineRandom.batch((a, b) -> a > b);
		twice(() -> assertThat(nineRandomBatched,
		                       contains(contains(67), contains(5, 43), contains(3, 5, 7, 24), contains(5, 67))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitAroundElement() {
		Sequence<Sequence<Integer>> emptySplit = empty.split(3);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

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

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSplit));
		twice(() -> assertThat(nineSplit, contains(contains(1, 2), contains(4, 5, 6, 7, 8, 9))));
		twice(() -> assertThat(_123456789, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitPredicate() {
		Sequence<Sequence<Integer>> emptySplit = empty.split(x -> x % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

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

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSplit));
		twice(() -> assertThat(nineSplit, contains(contains(1, 2), contains(4, 5), contains(7, 8))));
		twice(() -> assertThat(_123456789, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void step() {
		Sequence<Integer> emptyStep3 = empty.step(3);
		twice(() -> assertThat(emptyStep3, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStep3.iterator().next());

		Sequence<Integer> nineStep3 = _123456789.step(3);
		twice(() -> assertThat(nineStep3, contains(1, 4, 7)));

		Iterator<Integer> nineStep3Iterator = nineStep3.iterator();
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.hasNext(), is(true));
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.next(), is(1));
		nineStep3Iterator.remove();
		assertThat(nineStep3Iterator.hasNext(), is(true));
		expecting(IllegalStateException.class, nineStep3Iterator::remove);
		assertThat(nineStep3Iterator.next(), is(4));
		nineStep3Iterator.remove();

		twice(() -> assertThat(nineStep3, contains(2, 6, 9)));
		twice(() -> assertThat(_123456789, contains(2, 3, 5, 6, 7, 8, 9)));
	}

	@Test
	public void distinct() {
		Sequence<Integer> emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptyDistinct.iterator().next());

		Sequence<Integer> oneDistinct = oneRandom.distinct();
		twice(() -> assertThat(oneDistinct, contains(17)));

		Sequence<Integer> twoDuplicatesDistinct = newSequence(17, 17).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, contains(17)));

		Sequence<Integer> nineDistinct = nineRandom.distinct();
		twice(() -> assertThat(nineDistinct, contains(67, 5, 43, 3, 7, 24)));

		assertThat(removeFirst(nineDistinct), is(67));
		twice(() -> assertThat(nineDistinct, contains(5, 43, 3, 7, 24, 67)));
		twice(() -> assertThat(nineRandom, contains(5, 43, 3, 5, 7, 24, 5, 67)));
	}

	@Test
	public void sorted() {
		Sequence<Integer> emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptySorted.iterator().next());

		Sequence<Integer> oneSorted = oneRandom.sorted();
		twice(() -> assertThat(oneSorted, contains(17)));

		Sequence<Integer> twoSorted = twoRandom.sorted();
		twice(() -> assertThat(twoSorted, contains(17, 32)));

		Sequence<Integer> nineSorted = nineRandom.sorted();
		twice(() -> assertThat(nineSorted, contains(3, 5, 5, 5, 7, 24, 43, 67, 67)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSorted));
		twice(() -> assertThat(nineSorted, contains(3, 5, 5, 5, 7, 24, 43, 67, 67)));
		twice(() -> assertThat(nineRandom, contains(67, 5, 43, 3, 5, 7, 24, 5, 67)));
	}

	@Test
	public void sortedComparator() {
		Sequence<Integer> emptySorted = empty.sorted(reverseOrder());
		twice(() -> assertThat(emptySorted, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptySorted.iterator().next());

		Sequence<Integer> oneSorted = oneRandom.sorted(reverseOrder());
		twice(() -> assertThat(oneSorted, contains(17)));

		Sequence<Integer> twoSorted = twoRandom.sorted(reverseOrder());
		twice(() -> assertThat(twoSorted, contains(32, 17)));

		Sequence<Integer> nineSorted = nineRandom.sorted(reverseOrder());
		twice(() -> assertThat(nineSorted, contains(67, 67, 43, 24, 7, 5, 5, 5, 3)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineSorted));
		twice(() -> assertThat(nineSorted, contains(67, 67, 43, 24, 7, 5, 5, 5, 3)));
		twice(() -> assertThat(nineRandom, contains(67, 5, 43, 3, 5, 7, 24, 5, 67)));
	}

	@Test
	public void min() {
		twice(() -> assertThat(empty.min(), is(Optional.empty())));
		twice(() -> assertThat(oneRandom.min(), is(Optional.of(17))));
		twice(() -> assertThat(twoRandom.min(), is(Optional.of(17))));
		twice(() -> assertThat(nineRandom.min(), is(Optional.of(3))));
	}

	@Test
	public void max() {
		twice(() -> assertThat(empty.max(), is(Optional.empty())));
		twice(() -> assertThat(oneRandom.max(), is(Optional.of(17))));
		twice(() -> assertThat(twoRandom.max(), is(Optional.of(32))));
		twice(() -> assertThat(nineRandom.max(), is(Optional.of(67))));
	}

	@Test
	public void minByComparator() {
		twice(() -> assertThat(empty.min(reverseOrder()), is(Optional.empty())));
		twice(() -> assertThat(oneRandom.min(reverseOrder()), is(Optional.of(17))));
		twice(() -> assertThat(twoRandom.min(reverseOrder()), is(Optional.of(32))));
		twice(() -> assertThat(nineRandom.min(reverseOrder()), is(Optional.of(67))));
	}

	@Test
	public void maxByComparator() {
		twice(() -> assertThat(empty.max(reverseOrder()), is(Optional.empty())));
		twice(() -> assertThat(oneRandom.max(reverseOrder()), is(Optional.of(17))));
		twice(() -> assertThat(twoRandom.max(reverseOrder()), is(Optional.of(17))));
		twice(() -> assertThat(nineRandom.max(reverseOrder()), is(Optional.of(3))));
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
	public void peek() {
		Sequence<Integer> emptyPeeked = empty.peek(x -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyPeeked, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().next());

		AtomicInteger value = new AtomicInteger(1);
		Sequence<Integer> onePeeked = _1.peek(x -> assertThat(x, is(value.getAndIncrement())));
		twiceIndexed(value, 1, () -> assertThat(onePeeked, contains(1)));

		Sequence<Integer> twoPeeked = _12.peek(x -> assertThat(x, is(value.getAndIncrement())));
		twiceIndexed(value, 2, () -> assertThat(twoPeeked, contains(1, 2)));

		Sequence<Integer> fivePeeked = _12345.peek(x -> assertThat(x, is(value.getAndIncrement())));
		twiceIndexed(value, 5, () -> assertThat(fivePeeked, contains(1, 2, 3, 4, 5)));

		assertThat(removeFirst(fivePeeked), is(1));
		twiceIndexed(value, 4, () -> assertThat(fivePeeked, contains(2, 3, 4, 5)));
		twice(() -> assertThat(_12345, contains(2, 3, 4, 5)));
	}

	@Test
	public void peekIndexed() {
		Sequence<Integer> emptyPeeked = empty.peekIndexed((i, x) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyPeeked, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().next());

		AtomicInteger index = new AtomicInteger();
		AtomicInteger value = new AtomicInteger(1);
		Sequence<Integer> onePeeked = _1.peekIndexed((i, x) -> {
			assertThat(i, is(value.getAndIncrement()));
			assertThat(x, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(onePeeked, contains(1));

			assertThat(index.get(), is(1));
			assertThat(value.get(), is(2));
			index.set(0);
			value.set(1);
		});

		Sequence<Integer> twoPeeked = _12.peekIndexed((i, x) -> {
			assertThat(i, is(value.getAndIncrement()));
			assertThat(x, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(twoPeeked, contains(1, 2));

			assertThat(index.get(), is(2));
			assertThat(value.get(), is(3));
			index.set(0);
			value.set(1);
		});

		Sequence<Integer> fivePeeked = _12345.peekIndexed((i, x) -> {
			assertThat(i, is(value.getAndIncrement()));
			assertThat(x, is(index.getAndIncrement()));
		});
		twice(() -> {
			assertThat(fivePeeked, contains(1, 2, 3, 4, 5));

			assertThat(index.get(), is(5));
			assertThat(value.get(), is(6));
			index.set(0);
			value.set(1);
		});

		assertThat(removeFirst(fivePeeked), is(1));
		index.set(0);
		value.set(2);

		twice(() -> {
			assertThat(fivePeeked, contains(2, 3, 4, 5));
			assertThat(index.get(), is(4));
			assertThat(value.get(), is(6));
			index.set(0);
			value.set(2);
		});

		twice(() -> assertThat(_12345, contains(2, 3, 4, 5)));
	}

	@Test
	public void stream() {
		twice(() -> assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable())));
		twice(() -> assertThat(empty, is(emptyIterable())));

		twice(() -> assertThat(_12345.stream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5)));
		twice(() -> assertThat(_12345, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void parallelStream() {
		twice(() -> assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable())));
		twice(() -> assertThat(empty, is(emptyIterable())));

		twice(() -> assertThat(_12345.parallelStream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5)));
		twice(() -> assertThat(_12345, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void spliterator() {
		twice(() -> assertThat(StreamSupport.stream(empty.spliterator(), false).collect(Collectors.toList()),
		                       is(emptyIterable())));
		twice(() -> assertThat(empty, is(emptyIterable())));

		twice(() -> assertThat(StreamSupport.stream(_12345.spliterator(), false).collect(Collectors.toList()),
		                       contains(1, 2, 3, 4, 5)));
		twice(() -> assertThat(_12345, contains(1, 2, 3, 4, 5)));
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
		twice(() -> assertThat(emptyDelimited, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyDelimited.iterator().next());

		Sequence<?> oneDelimited = _1.delimit(", ");
		twice(() -> assertThat(oneDelimited, contains(1)));

		Iterator<?> oneIterator = oneDelimited.iterator();
		oneIterator.next();
		expecting(UnsupportedOperationException.class, oneIterator::remove);

		Sequence<?> twoDelimited = _12.delimit(", ");
		twice(() -> assertThat(twoDelimited, contains(1, ", ", 2)));

		Sequence<?> threeDelimited = _123.delimit(", ");
		twice(() -> assertThat(threeDelimited, contains(1, ", ", 2, ", ", 3)));

		Sequence<?> fourDelimited = _1234.delimit(", ");
		twice(() -> assertThat(fourDelimited, contains(1, ", ", 2, ", ", 3, ", ", 4)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(fourDelimited));
		twice(() -> assertThat(fourDelimited, contains(1, ", ", 2, ", ", 3, ", ", 4)));
		twice(() -> assertThat(_1234, contains(1, 2, 3, 4)));
	}

	@Test
	public void prefix() {
		Sequence<?> emptyPrefixed = empty.prefix("[");
		twice(() -> assertThat(emptyPrefixed, contains("[")));

		Iterator<?> emptyIterator = emptyPrefixed.iterator();
		emptyIterator.next();
		expecting(NoSuchElementException.class, emptyIterator::next);

		Sequence<?> onePrefixed = _1.prefix("[");
		twice(() -> assertThat(onePrefixed, contains("[", 1)));

		Sequence<?> threePrefixed = _123.prefix("[");
		twice(() -> assertThat(threePrefixed, contains("[", 1, 2, 3)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(threePrefixed));
		twice(() -> assertThat(threePrefixed, contains("[", 1, 2, 3)));
		twice(() -> assertThat(_123, contains(1, 2, 3)));
	}

	@Test
	public void suffix() {
		Sequence<?> emptySuffixed = empty.suffix("]");
		twice(() -> assertThat(emptySuffixed, contains("]")));

		Iterator<?> emptyIterator = emptySuffixed.iterator();
		emptyIterator.next();
		expecting(NoSuchElementException.class, emptyIterator::next);

		Sequence<?> oneSuffixed = _1.suffix("]");
		twice(() -> assertThat(oneSuffixed, contains(1, "]")));

		Sequence<?> threeSuffixed = _123.suffix("]");
		twice(() -> assertThat(threeSuffixed, contains(1, 2, 3, "]")));

		expecting(UnsupportedOperationException.class, () -> removeFirst(threeSuffixed));
		twice(() -> assertThat(threeSuffixed, contains(1, 2, 3, "]")));
		twice(() -> assertThat(_123, contains(1, 2, 3)));
	}

	@Test
	public void delimitPrefixSuffix() {
		Sequence<?> emptyDelimited = empty.delimit(", ").prefix("[").suffix("]");
		twice(() -> assertThat(emptyDelimited, contains("[", "]")));

		Iterator<?> emptyIterator = emptyDelimited.iterator();
		emptyIterator.next();
		emptyIterator.next();
		expecting(NoSuchElementException.class, emptyIterator::next);

		Sequence<?> threeDelimited = _123.delimit(", ").prefix("[").suffix("]");
		twice(() -> assertThat(threeDelimited, contains("[", 1, ", ", 2, ", ", 3, "]")));
	}

	@Test
	public void suffixPrefixDelimit() {
		Sequence<?> emptyDelimited = empty.suffix("]").prefix("[").delimit(", ");
		twice(() -> assertThat(emptyDelimited, contains("[", ", ", "]")));

		Iterator<?> emptyIterator = emptyDelimited.iterator();
		emptyIterator.next();
		emptyIterator.next();
		emptyIterator.next();
		expecting(NoSuchElementException.class, emptyIterator::next);

		Sequence<?> threeDelimited = _123.suffix("]").prefix("[").delimit(", ");
		twice(() -> assertThat(threeDelimited, contains("[", ", ", 1, ", ", 2, ", ", 3, ", ", "]")));
	}

	@Test
	public void surround() {
		Sequence<?> emptyDelimited = empty.delimit("[", ", ", "]");
		twice(() -> assertThat(emptyDelimited, contains("[", "]")));

		Iterator<?> emptyIterator = emptyDelimited.iterator();
		emptyIterator.next();
		emptyIterator.next();
		expecting(NoSuchElementException.class, emptyIterator::next);

		Sequence<?> threeDelimited = _123.delimit("[", ", ", "]");
		twice(() -> assertThat(threeDelimited, contains("[", 1, ", ", 2, ", ", 3, "]")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void interleave() {
		Sequence<Pair<Integer, Integer>> emptyInterleaved = empty.interleave(empty);
		twice(() -> assertThat(emptyInterleaved, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyInterleaved.iterator().next());

		Sequence<Pair<Integer, Integer>> interleavedShortFirst = _123.interleave(_12345);
		twice(() -> assertThat(interleavedShortFirst,
		                       contains(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3), Pair.of(null, 4),
		                                Pair.of(null, 5))));

		Sequence<Pair<Integer, Integer>> interleavedShortLast = _12345.interleave(_123);
		twice(() -> assertThat(interleavedShortLast,
		                       contains(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3), Pair.of(4, null),
		                                Pair.of(5, null))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(interleavedShortFirst));
		twice(() -> assertThat(interleavedShortLast,
		                       contains(Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3), Pair.of(4, null),
		                                Pair.of(5, null))));
	}

	@Test
	public void reverse() {
		Sequence<Integer> emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyReversed.iterator().next());

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
	public void reverseRemoval() {
		Sequence<Integer> reversed = _12345.reverse();
		assumeThat(reversed, is(not(instanceOf(ListSequence.class))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(reversed));
		twice(() -> assertThat(reversed, contains(5, 4, 3, 2, 1)));
		twice(() -> assertThat(_12345, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void shuffle() {
		Sequence<Integer> emptyShuffled = empty.shuffle();
		twice(() -> assertThat(emptyShuffled, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyShuffled.iterator().next());

		Sequence<Integer> oneShuffled = _1.shuffle();
		twice(() -> assertThat(oneShuffled, contains(1)));

		Sequence<Integer> twoShuffled = _12.shuffle();
		twice(() -> assertThat(twoShuffled, containsInAnyOrder(1, 2)));

		Sequence<Integer> threeShuffled = _123.shuffle();
		twice(() -> assertThat(threeShuffled, containsInAnyOrder(1, 2, 3)));

		Sequence<Integer> nineShuffled = _123456789.shuffle();
		twice(() -> assertThat(nineShuffled, containsInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineShuffled));
		twice(() -> assertThat(nineShuffled, containsInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9)));
		twice(() -> assertThat(_123456789, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void shuffleWithRandomSource() {
		Sequence<Integer> emptyShuffled = empty.shuffle(new Random(17));
		twice(() -> assertThat(emptyShuffled, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyShuffled.iterator().next());

		Sequence<Integer> oneShuffled = _1.shuffle(new Random(17));
		twice(() -> assertThat(oneShuffled, contains(1)));

		Sequence<Integer> twoShuffled = _12.shuffle(new Random(17));
		assertThat(twoShuffled, contains(1, 2));
		assertThat(twoShuffled, contains(1, 2));
		assertThat(twoShuffled, contains(1, 2));
		assertThat(twoShuffled, contains(1, 2));
		assertThat(twoShuffled, contains(2, 1));
		assertThat(twoShuffled, contains(2, 1));
		assertThat(twoShuffled, contains(1, 2));
		assertThat(twoShuffled, contains(1, 2));

		Sequence<Integer> threeShuffled = _123.shuffle(new Random(17));
		assertThat(threeShuffled, contains(3, 2, 1));
		assertThat(threeShuffled, contains(1, 3, 2));

		Sequence<Integer> nineShuffled = _123456789.shuffle(new Random(17));
		assertThat(nineShuffled, contains(1, 8, 4, 2, 6, 3, 5, 9, 7));
		assertThat(nineShuffled, contains(6, 3, 5, 2, 9, 4, 1, 7, 8));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineShuffled));
		twice(() -> assertThat(nineShuffled, containsInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9)));
		twice(() -> assertThat(_123456789, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
	}

	@Test
	public void shuffleWithRandomSupplier() {
		Sequence<Integer> emptyShuffled = empty.shuffle(() -> new Random(17));
		twice(() -> assertThat(emptyShuffled, is(emptyIterable())));

		Sequence<Integer> oneShuffled = _1.shuffle(() -> new Random(17));
		twice(() -> assertThat(oneShuffled, contains(1)));

		Sequence<Integer> twoShuffled = _12.shuffle(() -> new Random(17));
		twice(() -> assertThat(twoShuffled, contains(1, 2)));

		Sequence<Integer> threeShuffled = _123.shuffle(() -> new Random(17));
		twice(() -> assertThat(threeShuffled, contains(3, 2, 1)));

		Sequence<Integer> nineShuffled = _123456789.shuffle(() -> new Random(17));
		twice(() -> assertThat(nineShuffled, contains(1, 8, 4, 2, 6, 3, 5, 9, 7)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(nineShuffled));
		twice(() -> assertThat(nineShuffled, containsInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9)));
		twice(() -> assertThat(_123456789, contains(1, 2, 3, 4, 5, 6, 7, 8, 9)));
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
		expecting(NoSuchElementException.class, () -> emptyChars.iterator().next());

		CharSeq charSeq = _12345.toChars(x -> (char) (x + 'a' - 1));
		twice(() -> assertThat(charSeq, containsChars('a', 'b', 'c', 'd', 'e')));
	}

	@Test
	public void mapToInt() {
		IntSequence emptyInts = empty.toInts(x -> x + 1);
		twice(() -> assertThat(emptyInts, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyInts.iterator().next());

		IntSequence intSequence = _12345.toInts(x -> x + 1);
		twice(() -> assertThat(intSequence, containsInts(2, 3, 4, 5, 6)));
	}

	@Test
	public void mapToLong() {
		LongSequence emptyLongs = empty.toLongs(x -> x + 1);
		twice(() -> assertThat(emptyLongs, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyLongs.iterator().next());

		LongSequence longSequence = _12345.toLongs(x -> x + 1);
		twice(() -> assertThat(longSequence, containsLongs(2L, 3L, 4L, 5L, 6L)));
	}

	@Test
	public void mapToDouble() {
		DoubleSequence emptyDoubles = empty.toDoubles(x -> x + 1);
		twice(() -> assertThat(emptyDoubles, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyDoubles.iterator().next());

		DoubleSequence doubleSequence = _12345.toDoubles(x -> x + 1);
		twice(() -> assertThat(doubleSequence, containsDoubles(2.0, 3.0, 4.0, 5.0, 6.0)));
	}

	@Test
	public void repeat() {
		Sequence<Integer> emptyRepeated = empty.repeat();
		twice(() -> assertThat(emptyRepeated, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeated.iterator().next());

		Sequence<Integer> oneRepeated = _1.repeat();
		twice(() -> assertThat(oneRepeated, beginsWith(1, 1, 1)));

		Sequence<Integer> twoRepeated = _12.repeat();
		twice(() -> assertThat(twoRepeated, beginsWith(1, 2, 1, 2, 1)));

		Sequence<Integer> threeRepeated = _123.repeat();
		twice(() -> assertThat(threeRepeated, beginsWith(1, 2, 3, 1, 2, 3, 1, 2)));

		assertThat(removeFirst(threeRepeated), is(1));
		twice(() -> assertThat(threeRepeated, beginsWith(2, 3, 2, 3, 2, 3)));
		twice(() -> assertThat(_123, contains(2, 3)));

		Sequence<Integer> varyingLengthRepeated = Sequence.from(new Iterable<Integer>() {
			private List<Integer> list = asList(1, 2, 3);
			int end = list.size();

			@Override
			public Iterator<Integer> iterator() {
				List<Integer> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				return subList.iterator();
			}
		}).repeat();
		assertThat(varyingLengthRepeated, contains(1, 2, 3, 1, 2, 1));
	}

	@Test
	public void repeatTwice() {
		Sequence<Integer> emptyRepeatedTwice = empty.repeat(2);
		twice(() -> assertThat(emptyRepeatedTwice, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeatedTwice.iterator().next());

		Sequence<Integer> oneRepeatedTwice = _1.repeat(2);
		twice(() -> assertThat(oneRepeatedTwice, contains(1, 1)));

		Sequence<Integer> twoRepeatedTwice = _12.repeat(2);
		twice(() -> assertThat(twoRepeatedTwice, contains(1, 2, 1, 2)));

		Sequence<Integer> threeRepeatedTwice = _123.repeat(2);
		twice(() -> assertThat(threeRepeatedTwice, contains(1, 2, 3, 1, 2, 3)));

		assertThat(removeFirst(threeRepeatedTwice), is(1));
		twice(() -> assertThat(threeRepeatedTwice, contains(2, 3, 2, 3)));
		twice(() -> assertThat(_123, contains(2, 3)));

		Sequence<Integer> varyingLengthRepeatedTwice = Sequence.from(new Iterable<Integer>() {
			private List<Integer> list = asList(1, 2, 3);
			int end = list.size();

			@Override
			public Iterator<Integer> iterator() {
				List<Integer> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				return subList.iterator();
			}
		}).repeat(2);
		assertThat(varyingLengthRepeatedTwice, contains(1, 2, 3, 1, 2));
	}

	@Test
	public void repeatZero() {
		Sequence<Integer> emptyRepeatedZero = empty.repeat(0);
		twice(() -> assertThat(emptyRepeatedZero, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeatedZero.iterator().next());

		Sequence<Integer> oneRepeatedZero = _1.repeat(0);
		twice(() -> assertThat(oneRepeatedZero, is(emptyIterable())));

		Sequence<Integer> twoRepeatedZero = _12.repeat(0);
		twice(() -> assertThat(twoRepeatedZero, is(emptyIterable())));

		Sequence<Integer> threeRepeatedZero = _123.repeat(0);
		twice(() -> assertThat(threeRepeatedZero, is(emptyIterable())));
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
		Sequence<Integer> emptySwapTwoAndThree = empty.swap((a, b) -> a == 2 && b == 3);
		twice(() -> assertThat(emptySwapTwoAndThree, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySwapTwoAndThree.iterator().next());

		Sequence<Integer> swapTwoAndThree = _12345.swap((a, b) -> a == 2 && b == 3);
		twice(() -> assertThat(swapTwoAndThree, contains(1, 3, 2, 4, 5)));

		Sequence<Integer> swapTwoWithEverything = _12345.swap((a, b) -> a == 2);
		twice(() -> assertThat(swapTwoWithEverything, contains(1, 3, 4, 5, 2)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(swapTwoWithEverything));
		twice(() -> assertThat(swapTwoWithEverything, contains(1, 3, 4, 5, 2)));
		twice(() -> assertThat(_12345, contains(1, 2, 3, 4, 5)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void index() {
		BiSequence<Integer, Integer> emptyIndexed = empty.index();
		twice(() -> assertThat(emptyIndexed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyIndexed.iterator().next());

		BiSequence<Integer, Integer> fiveIndexed = _12345.index();
		twice(() -> assertThat(fiveIndexed, contains(Pair.of(0, 1), Pair.of(1, 2), Pair.of(2, 3), Pair.of(3, 4),
		                                             Pair.of(4, 5))));

		assertThat(removeFirst(fiveIndexed), is(Pair.of(0, 1)));
		twice(() -> assertThat(fiveIndexed, contains(Pair.of(0, 2), Pair.of(1, 3), Pair.of(2, 4), Pair.of(3, 5))));
		twice(() -> assertThat(_12345, contains(2, 3, 4, 5)));
	}

	@Test
	public void filterClear() {
		Sequence<Integer> filtered = _12345.filter(x -> x % 2 != 0);
		filtered.clear();

		twice(() -> assertThat(filtered, is(emptyIterable())));
		twice(() -> assertThat(_12345, contains(2, 4)));
	}

	@Test
	public void appendClear() {
		Sequence<Integer> appended = _1.append(new ArrayList<>(singletonList(2)));
		appended.clear();

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
		assertThat(empty.containsAll((Iterable<?>) emptyList()), is(true));
		assertThat(empty.containsAll((Iterable<?>) asList(17, 18, 19)), is(false));

		assertThat(_12345.containsAll(Iterables.of()), is(true));
		assertThat(_12345.containsAll((Iterable<?>) emptyList()), is(true));
		assertThat(_12345.containsAll(Iterables.of(1)), is(true));
		assertThat(_12345.containsAll(Iterables.of(1, 3, 5)), is(true));
		assertThat(_12345.containsAll((Iterable<?>) asList(1, 3, 5)), is(true));
		assertThat(_12345.containsAll(Iterables.of(1, 2, 3, 4, 5)), is(true));
		assertThat(_12345.containsAll(Iterables.of(1, 2, 3, 4, 5, 17)), is(false));
		assertThat(_12345.containsAll(Iterables.of(17, 18, 19)), is(false));
	}

	@Test
	public void containsAllCollection() {
		assertThat(empty.containsAll(emptyList()), is(true));
		assertThat(empty.containsAll(asList(17, 18, 19)), is(false));

		assertThat(_12345.containsAll(emptyList()), is(true));
		assertThat(_12345.containsAll(singletonList(1)), is(true));
		assertThat(_12345.containsAll(asList(1, 3, 5)), is(true));
		assertThat(_12345.containsAll(asList(1, 2, 3, 4, 5)), is(true));
		assertThat(_12345.containsAll(asList(1, 2, 3, 4, 5, 17)), is(false));
		assertThat(_12345.containsAll(asList(17, 18, 19)), is(false));
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
		assertThat(empty.containsAny(emptyList()), is(false));
		assertThat(empty.containsAny(asList(17, 18, 19)), is(false));

		assertThat(_12345.containsAny(emptyList()), is(false));
		assertThat(_12345.containsAny(singletonList(1)), is(true));
		assertThat(_12345.containsAny(asList(1, 3, 5)), is(true));
		assertThat(_12345.containsAny(asList(1, 2, 3, 4, 5)), is(true));
		assertThat(_12345.containsAny(asList(1, 2, 3, 4, 5, 17)), is(true));
		assertThat(_12345.containsAny(asList(17, 18, 19)), is(false));
	}

	@Test
	public void immutable() {
		Sequence<Integer> emptyImmutable = empty.immutable();
		twice(() -> assertThat(emptyImmutable, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyImmutable.iterator().next());

		Sequence<Integer> fiveImmutable = _12345.immutable();
		twice(() -> assertThat(fiveImmutable, contains(1, 2, 3, 4, 5)));

		Iterator<Integer> iterator = fiveImmutable.iterator();
		iterator.next();
		expecting(UnsupportedOperationException.class, iterator::remove);
		twice(() -> assertThat(fiveImmutable, contains(1, 2, 3, 4, 5)));

		expecting(UnsupportedOperationException.class, () -> fiveImmutable.add(17));
		twice(() -> assertThat(fiveImmutable, contains(1, 2, 3, 4, 5)));

		expecting(UnsupportedOperationException.class, () -> fiveImmutable.remove(3));
		twice(() -> assertThat(fiveImmutable, contains(1, 2, 3, 4, 5)));
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
	public void remove() {
		assertThat(empty.remove(3), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.remove(3), is(true));
		twice(() -> assertThat(_12345, contains(1, 2, 4, 5)));

		assertThat(_12345.remove(7), is(false));
		twice(() -> assertThat(_12345, contains(1, 2, 4, 5)));
	}

	@Test
	public void removeAllVarargs() {
		assertThat(empty.removeAll(3, 4), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.removeAll(3, 4, 7), is(true));
		twice(() -> assertThat(_12345, contains(1, 2, 5)));
	}

	@Test
	public void removeAllIterable() {
		assertThat(empty.removeAll(Iterables.of(3, 4)), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.removeAll(Iterables.of(3, 4, 7)), is(true));
		twice(() -> assertThat(_12345, contains(1, 2, 5)));
	}

	@Test
	public void removeAllCollection() {
		assertThat(empty.removeAll(asList(3, 4)), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.removeAll(asList(3, 4, 7)), is(true));
		twice(() -> assertThat(_12345, contains(1, 2, 5)));
	}

	@Test
	public void retainAllVarargs() {
		assertThat(empty.retainAll(3, 4), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.retainAll(3, 4, 7), is(true));
		twice(() -> assertThat(_12345, contains(3, 4)));
	}

	@Test
	public void retainAllIterable() {
		assertThat(empty.retainAll(Iterables.of(3, 4)), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.retainAll(Iterables.of(3, 4, 7)), is(true));
		twice(() -> assertThat(_12345, contains(3, 4)));
	}

	@Test
	public void retainAllCollection() {
		assertThat(empty.retainAll(asList(3, 4)), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.retainAll(asList(3, 4, 7)), is(true));
		twice(() -> assertThat(_12345, contains(3, 4)));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x == 3), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.removeIf(x -> x == 3), is(true));
		twice(() -> assertThat(_12345, contains(1, 2, 4, 5)));
	}

	@Test
	public void retainIf() {
		assertThat(empty.retainIf(x -> x == 3), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.retainIf(x -> x == 3), is(true));
		twice(() -> assertThat(_12345, contains(3)));
	}
}
