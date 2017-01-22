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

package org.d2ab.collection.ints;

import org.d2ab.collection.Arrayz;
import org.d2ab.collection.Lists;
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class IntSortedSetBoxingTest extends BaseBoxingTest {
	private final SortedSet<Integer> empty = IntSortedSet.Base.create();
	private final SortedSet<Integer> set = IntSortedSet.Base.create(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);

	@Test
	public void create() {
		assertThat(IntSortedSet.create(), is(emptyIterable()));
		assertThat(IntSortedSet.create(-2, -1, 0, 1), contains(-2, -1, 0, 1));
	}

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(set.size(), is(10));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void iteratorFailFastPositives() {
		Iterator<Integer> it1 = set.iterator();
		assertThat(it1.hasNext(), is(true));
		assertThat(set.add(17), is(true));
		expecting(ConcurrentModificationException.class, it1::next);

		Iterator<Integer> it2 = set.iterator();
		assertThat(it2.hasNext(), is(true));
		assertThat(set.remove(17), is(true));
		expecting(ConcurrentModificationException.class, it2::next);
	}

	@Test
	public void iteratorFailFastNegatives() {
		Iterator<Integer> it1 = set.iterator();
		assertThat(it1.hasNext(), is(true));
		assertThat(set.add(-17), is(true));
		expecting(ConcurrentModificationException.class, it1::next);

		Iterator<Integer> it2 = set.iterator();
		assertThat(it2.hasNext(), is(true));
		assertThat(set.remove(-17), is(true));
		expecting(ConcurrentModificationException.class, it2::next);
	}

	@Test
	public void isEmpty() {
		assertThat(empty.isEmpty(), is(true));
		assertThat(set.isEmpty(), is(false));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty.isEmpty(), is(true));

		set.clear();
		assertThat(set.isEmpty(), is(true));
	}

	@Test
	public void comparator() {
		assertThat(empty.comparator(), is(nullValue()));
		assertThat(set.comparator(), is(nullValue()));
	}

	@Test
	public void add() {
		empty.add(17);
		assertThat(empty, contains(17));

		set.add(17);
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
	}

	@Test
	public void boxedContains() {
		assertThat(empty.contains(17), is(false));

		assertThat(set.contains(17), is(false));
		assertThat(set.contains(new Object()), is(false));

		for (int x = -5; x <= 4; x++)
			assertThat(set.contains(x), is(true));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17), is(false));

		assertThat(set.remove(17), is(false));
		assertThat(set.remove(new Object()), is(false));

		for (int x = -5; x <= 4; x++)
			assertThat(set.remove(x), is(true));
		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(set.toString(), is("[-5, -4, -3, -2, -1, 0, 1, 2, 3, 4]"));
	}

	@Test
	public void testEqualsHashCodeAgainstSet() {
		Set<Integer> set2 = new HashSet<>(Lists.of(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.remove(17);

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void testEqualsHashCodeAgainstIntSet() {
		BitIntSet set2 = new BitIntSet(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17);
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.remove(17);

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void subSet() {
		SortedSet<Integer> subSet = set.subSet(-3, 3);
		assertThat(subSet, contains(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(subSet.first(), is(-3));
		assertThat(subSet.last(), is(2));
		assertThat(subSet.contains(1), is(true));
		assertThat(subSet.contains(3), is(false));
		assertThat(subSet.toString(), is("[-3, -2, -1, 0, 1, 2]"));

		Set<Integer> equivalentSet = new HashSet<>(Lists.of(-3, -2, -1, 0, 1, 2));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(subSet.remove(0), is(true));
		assertThat(subSet, contains(-3, -2, -1, 1, 2));
		assertThat(subSet.size(), is(5));
		assertThat(set, contains(-5, -4, -3, -2, -1, 1, 2, 3, 4));

		assertThat(subSet.remove(0), is(false));
		assertThat(subSet, contains(-3, -2, -1, 1, 2));
		assertThat(subSet.size(), is(5));
		assertThat(set, contains(-5, -4, -3, -2, -1, 1, 2, 3, 4));

		assertThat(subSet.add(0), is(true));
		assertThat(subSet, contains(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));

		assertThat(subSet.add(0), is(false));
		assertThat(subSet, contains(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));

		expecting(IllegalArgumentException.class, () -> subSet.add(-17));
		assertThat(subSet, contains(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));

		subSet.clear();
		assertThat(subSet, is(emptyIterable()));
		assertThat(subSet.size(), is(0));
		assertThat(set, contains(-5, -4, 3, 4));
	}

	@Test
	public void sparseSubSet() {
		SortedSet<Integer> subSet = new BitIntSet(-5, -3, -1, 1, 3, 5).subSet(-2, 2);
		assertThat(subSet, contains(-1, 1));
		assertThat(subSet.size(), is(2));
		assertThat(subSet.first(), is(-1));
		assertThat(subSet.last(), is(1));
		assertThat(subSet.contains(1), is(true));
		assertThat(subSet.contains(-3), is(false));
		assertThat(subSet.toString(), is("[-1, 1]"));

		Set<Integer> equivalentSet = new HashSet<>(Lists.of(-1, 1));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void headSet() {
		SortedSet<Integer> headSet = set.headSet(0);
		assertThat(headSet, contains(-5, -4, -3, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(headSet.first(), is(-5));
		assertThat(headSet.last(), is(-1));
		assertThat(headSet.contains(-3), is(true));
		assertThat(headSet.contains(0), is(false));
		assertThat(headSet.toString(), is("[-5, -4, -3, -2, -1]"));

		Set<Integer> equivalentSet = new HashSet<>(Lists.of(-5, -4, -3, -2, -1));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(headSet.remove(-3), is(true));
		assertThat(headSet, contains(-5, -4, -2, -1));
		assertThat(headSet.size(), is(4));
		assertThat(set, contains(-5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(headSet.remove(-3), is(false));
		assertThat(headSet, contains(-5, -4, -2, -1));
		assertThat(headSet.size(), is(4));
		assertThat(set, contains(-5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(headSet.add(-17), is(true));
		assertThat(headSet, contains(-17, -5, -4, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(set, contains(-17, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(headSet.add(-17), is(false));
		assertThat(headSet, contains(-17, -5, -4, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(set, contains(-17, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		expecting(IllegalArgumentException.class, () -> headSet.add(17));
		assertThat(headSet, contains(-17, -5, -4, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(set, contains(-17, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(set.add(-6), is(true));
		assertThat(headSet, contains(-17, -6, -5, -4, -2, -1));
		assertThat(headSet.size(), is(6));
		assertThat(set, contains(-17, -6, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		headSet.clear();
		assertThat(headSet, is(emptyIterable()));
		assertThat(headSet.size(), is(0));
		assertThat(set, contains(0, 1, 2, 3, 4));
	}

	@Test
	public void sparseHeadSet() {
		SortedSet<Integer> headSet = new BitIntSet(-5, -3, -1, 1, 3, 5).headSet(0);
		assertThat(headSet, contains(-5, -3, -1));
		assertThat(headSet.size(), is(3));
		assertThat(headSet.first(), is(-5));
		assertThat(headSet.last(), is(-1));
		assertThat(headSet.contains(-3), is(true));
		assertThat(headSet.contains(1), is(false));
		assertThat(headSet.toString(), is("[-5, -3, -1]"));

		Set<Integer> equivalentSet = new HashSet<>(Lists.of(-5, -3, -1));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void tailSet() {
		SortedSet<Integer> tailSet = set.tailSet(0);
		assertThat(tailSet, contains(0, 1, 2, 3, 4));
		assertThat(tailSet.size(), is(5));
		assertThat(tailSet.first(), is(0));
		assertThat(tailSet.last(), is(4));
		assertThat(tailSet.contains(3), is(true));
		assertThat(tailSet.contains(-1), is(false));
		assertThat(tailSet.toString(), is("[0, 1, 2, 3, 4]"));

		Set<Integer> equivalentSet = new HashSet<>(Lists.of(0, 1, 2, 3, 4));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(tailSet.remove(2), is(true));
		assertThat(tailSet, contains(0, 1, 3, 4));
		assertThat(tailSet.size(), is(4));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 1, 3, 4));

		assertThat(tailSet.remove(2), is(false));
		assertThat(tailSet, contains(0, 1, 3, 4));
		assertThat(tailSet.size(), is(4));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 1, 3, 4));

		assertThat(tailSet.add(17), is(true));
		assertThat(tailSet, contains(0, 1, 3, 4, 17));
		assertThat(tailSet.size(), is(5));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 1, 3, 4, 17));

		assertThat(tailSet.add(17), is(false));
		assertThat(tailSet, contains(0, 1, 3, 4, 17));
		assertThat(tailSet.size(), is(5));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 1, 3, 4, 17));

		expecting(IllegalArgumentException.class, () -> tailSet.add(-17));
		assertThat(tailSet, contains(0, 1, 3, 4, 17));
		assertThat(tailSet.size(), is(5));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 1, 3, 4, 17));

		assertThat(set.add(5), is(true));
		assertThat(tailSet, contains(0, 1, 3, 4, 5, 17));
		assertThat(tailSet.size(), is(6));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 1, 3, 4, 5, 17));

		tailSet.clear();
		assertThat(tailSet, is(emptyIterable()));
		assertThat(tailSet.size(), is(0));
		assertThat(set, contains(-5, -4, -3, -2, -1));
	}

	@Test
	public void sparseTailSet() {
		SortedSet<Integer> tailSet = new BitIntSet(-5, -3, -1, 1, 3, 5).tailSet(0);
		assertThat(tailSet, contains(1, 3, 5));
		assertThat(tailSet.size(), is(3));
		assertThat(tailSet.first(), is(1));
		assertThat(tailSet.last(), is(5));
		assertThat(tailSet.contains(3), is(true));
		assertThat(tailSet.contains(-1), is(false));
		assertThat(tailSet.toString(), is("[1, 3, 5]"));

		Set<Integer> equivalentSet = new HashSet<>(Lists.of(1, 3, 5));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void addAllIntCollection() {
		assertThat(empty.addAll(IntList.create(1, 2, 3)), is(true));
		assertThat(empty, contains(1, 2, 3));

		assertThat(set.addAll(IntList.create(3, 4, 5, 6, 7)), is(true));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(set.stream().collect(Collectors.toList()), contains(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(set.parallelStream().collect(Collectors.toList()),
		           contains(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void first() {
		expecting(NoSuchElementException.class, empty::first);
		assertThat(set.first(), is(-5));
	}

	@Test
	public void last() {
		expecting(NoSuchElementException.class, empty::last);
		assertThat(set.last(), is(4));
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Integer> iterator = set.iterator();
		int value = -5;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is(value));
			iterator.remove();
			value++;
		}
		assertThat(value, is(5));
		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void removeAllIntCollection() {
		assertThat(empty.removeAll(IntList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(IntList.create(1, 2, 3)), is(true));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void retainAllIntCollection() {
		assertThat(empty.retainAll(IntList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(IntList.create(1, 2, 3)), is(true));
		assertThat(set, contains(1, 2, 3));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeIf(x -> x > 3), is(true));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 1, 2, 3));
	}

	@Test
	public void containsAllIntCollection() {
		assertThat(empty.containsAll(IntList.create(1, 2, 3)), is(false));
		assertThat(set.containsAll(IntList.create(1, 2, 3)), is(true));
		assertThat(set.containsAll(IntList.create(1, 2, 3, 17)), is(false));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(-5);
		set.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(5));
	}

	@Test
	public void addAll() {
		assertThat(empty.addAll(Lists.of(1, 2, 3)), is(true));
		assertThat(empty, contains(1, 2, 3));

		assertThat(set.addAll(Lists.of(3, 4, 5, 6, 7)), is(true));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(Lists.of(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(Lists.of(1, 2, 3)), is(true));
		assertThat(set, contains(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(Lists.of(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(Lists.of(1, 2, 3)), is(true));
		assertThat(set, contains(1, 2, 3));
	}

	@Test
	public void containsAllCollection() {
		assertThat(empty.containsAll(Lists.of(1, 2, 3)), is(false));
		assertThat(set.containsAll(Lists.of(1, 2, 3)), is(true));
		assertThat(set.containsAll(Lists.of(1, 2, 3, 17)), is(false));
	}

	@Test
	public void boundaries() {
		BitIntSet set = new BitIntSet();
		assertThat(set.add(Integer.MIN_VALUE), is(true));
		assertThat(set.add(0), is(true));
		assertThat(set.add(Integer.MAX_VALUE), is(true));

		assertThat(set, contains(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));

		assertThat(set.contains(Integer.MIN_VALUE), is(true));
		assertThat(set.contains(0), is(true));
		assertThat(set.contains(Integer.MAX_VALUE), is(true));

		assertThat(set.remove(Integer.MIN_VALUE), is(true));
		assertThat(set.remove(0), is(true));
		assertThat(set.remove(Integer.MAX_VALUE), is(true));

		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void fuzz() {
		Integer[] randomValues = new Integer[1000];
		Random random = new Random();
		for (int i = 0; i < randomValues.length; i++) {
			int randomValue;
			do
				randomValue = random.nextInt();
			while (Arrayz.contains(randomValues, randomValue));
			randomValues[i] = randomValue;
		}

		// Adding
		for (int randomValue : randomValues)
			assertThat(empty.add(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		for (int randomValue : randomValues)
			assertThat(empty.add(randomValue), is(false));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAll(Lists.of(randomValues)), is(true));

		for (int randomValue : randomValues)
			assertThat(empty.contains(randomValue), is(true));

		// toString
		Arrays.sort(randomValues);
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (int randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty.size(), is(0));

		for (int randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(false));
	}
}
