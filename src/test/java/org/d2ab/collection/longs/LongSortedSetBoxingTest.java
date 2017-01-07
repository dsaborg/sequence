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

package org.d2ab.collection.longs;

import org.d2ab.collection.Arrayz;
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class LongSortedSetBoxingTest extends BaseBoxingTest {
	private final SortedSet<Long> empty = LongSortedSet.Base.create();
	private final SortedSet<Long> set = LongSortedSet.Base.create(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);

	@Test
	public void create() {
		assertThat(LongSortedSet.create(), is(emptyIterable()));
		assertThat(LongSortedSet.create(-2, -1, 0, 1), contains(-2L, -1L, 0L, 1L));
	}

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(set.size(), is(10));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L));
	}

	@Test
	public void iteratorFailFastPositives() {
		Iterator<Long> it1 = set.iterator();
		assertThat(it1.hasNext(), is(true));
		assertThat(set.add(17L), is(true));
		expecting(ConcurrentModificationException.class, it1::next);

		Iterator<Long> it2 = set.iterator();
		assertThat(it2.hasNext(), is(true));
		assertThat(set.remove(17L), is(true));
		expecting(ConcurrentModificationException.class, it2::next);
	}

	@Test
	public void iteratorFailFastNegatives() {
		Iterator<Long> it1 = set.iterator();
		assertThat(it1.hasNext(), is(true));
		assertThat(set.add(-17L), is(true));
		expecting(ConcurrentModificationException.class, it1::next);

		Iterator<Long> it2 = set.iterator();
		assertThat(it2.hasNext(), is(true));
		assertThat(set.remove(-17L), is(true));
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
		empty.add(17L);
		assertThat(empty, contains(17L));

		set.add(17L);
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L, 17L));
	}

	@Test
	public void boxedContains() {
		assertThat(empty.contains(17L), is(false));

		assertThat(set.contains(17L), is(false));
		assertThat(set.contains(new Object()), is(false));

		for (long x = -5; x <= 4; x++)
			assertThat(set.contains(x), is(true));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17L), is(false));

		assertThat(set.remove(17L), is(false));
		assertThat(set.remove(new Object()), is(false));

		for (long x = -5; x <= 4; x++)
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
		Set<Long> set2 = new HashSet<>(asList(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L, 17L));
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.remove(17L);

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void testEqualsHashCodeAgainstLongSet() {
		BitLongSet set2 = new BitLongSet(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L, 17L);
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.remove(17L);

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void subSet() {
		SortedSet<Long> subSet = set.subSet(-3L, 3L);
		assertThat(subSet, contains(-3L, -2L, -1L, 0L, 1L, 2L));
		assertThat(subSet.size(), is(6));
		assertThat(subSet.first(), is(-3L));
		assertThat(subSet.last(), is(2L));
		assertThat(subSet.contains(1L), is(true));
		assertThat(subSet.contains(3L), is(false));
		assertThat(subSet.toString(), is("[-3, -2, -1, 0, 1, 2]"));

		Set<Long> equivalentSet = new HashSet<>(asList(-3L, -2L, -1L, 0L, 1L, 2L));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(subSet.remove(0L), is(true));
		assertThat(subSet, contains(-3L, -2L, -1L, 1L, 2L));
		assertThat(subSet.size(), is(5));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 1L, 2L, 3L, 4L));

		assertThat(subSet.remove(0L), is(false));
		assertThat(subSet, contains(-3L, -2L, -1L, 1L, 2L));
		assertThat(subSet.size(), is(5));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 1L, 2L, 3L, 4L));

		assertThat(subSet.add(0L), is(true));
		assertThat(subSet, contains(-3L, -2L, -1L, 0L, 1L, 2L));
		assertThat(subSet.size(), is(6));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L));

		assertThat(subSet.add(0L), is(false));
		assertThat(subSet, contains(-3L, -2L, -1L, 0L, 1L, 2L));
		assertThat(subSet.size(), is(6));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L));

		expecting(IllegalArgumentException.class, () -> subSet.add(-17L));
		assertThat(subSet, contains(-3L, -2L, -1L, 0L, 1L, 2L));
		assertThat(subSet.size(), is(6));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L));

		subSet.clear();
		assertThat(subSet, is(emptyIterable()));
		assertThat(subSet.size(), is(0));
		assertThat(set, contains(-5L, -4L, 3L, 4L));
	}

	@Test
	public void sparseSubSet() {
		SortedSet<Long> subSet = new BitLongSet(-5, -3, -1, 1, 3, 5).subSet(-2, 2);
		assertThat(subSet, contains(-1L, 1L));
		assertThat(subSet.size(), is(2));
		assertThat(subSet.first(), is(-1L));
		assertThat(subSet.last(), is(1L));
		assertThat(subSet.contains(1L), is(true));
		assertThat(subSet.contains(-3L), is(false));
		assertThat(subSet.toString(), is("[-1, 1]"));

		Set<Long> equivalentSet = new HashSet<>(asList(-1L, 1L));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void headSet() {
		SortedSet<Long> headSet = set.headSet(0L);
		assertThat(headSet, contains(-5L, -4L, -3L, -2L, -1L));
		assertThat(headSet.size(), is(5));
		assertThat(headSet.first(), is(-5L));
		assertThat(headSet.last(), is(-1L));
		assertThat(headSet.contains(-3L), is(true));
		assertThat(headSet.contains(0L), is(false));
		assertThat(headSet.toString(), is("[-5, -4, -3, -2, -1]"));

		Set<Long> equivalentSet = new HashSet<>(asList(-5L, -4L, -3L, -2L, -1L));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(headSet.remove(-3L), is(true));
		assertThat(headSet, contains(-5L, -4L, -2L, -1L));
		assertThat(headSet.size(), is(4));
		assertThat(set, contains(-5L, -4L, -2L, -1L, 0L, 1L, 2L, 3L, 4L));

		assertThat(headSet.remove(-3L), is(false));
		assertThat(headSet, contains(-5L, -4L, -2L, -1L));
		assertThat(headSet.size(), is(4));
		assertThat(set, contains(-5L, -4L, -2L, -1L, 0L, 1L, 2L, 3L, 4L));

		assertThat(headSet.add(-17L), is(true));
		assertThat(headSet, contains(-17L, -5L, -4L, -2L, -1L));
		assertThat(headSet.size(), is(5));
		assertThat(set, contains(-17L, -5L, -4L, -2L, -1L, 0L, 1L, 2L, 3L, 4L));

		assertThat(headSet.add(-17L), is(false));
		assertThat(headSet, contains(-17L, -5L, -4L, -2L, -1L));
		assertThat(headSet.size(), is(5));
		assertThat(set, contains(-17L, -5L, -4L, -2L, -1L, 0L, 1L, 2L, 3L, 4L));

		expecting(IllegalArgumentException.class, () -> headSet.add(17L));
		assertThat(headSet, contains(-17L, -5L, -4L, -2L, -1L));
		assertThat(headSet.size(), is(5));
		assertThat(set, contains(-17L, -5L, -4L, -2L, -1L, 0L, 1L, 2L, 3L, 4L));

		assertThat(set.add(-6L), is(true));
		assertThat(headSet, contains(-17L, -6L, -5L, -4L, -2L, -1L));
		assertThat(headSet.size(), is(6));
		assertThat(set, contains(-17L, -6L, -5L, -4L, -2L, -1L, 0L, 1L, 2L, 3L, 4L));

		headSet.clear();
		assertThat(headSet, is(emptyIterable()));
		assertThat(headSet.size(), is(0));
		assertThat(set, contains(0L, 1L, 2L, 3L, 4L));
	}

	@Test
	public void sparseHeadSet() {
		SortedSet<Long> headSet = new BitLongSet(-5, -3, -1, 1, 3, 5).headSet(0);
		assertThat(headSet, contains(-5L, -3L, -1L));
		assertThat(headSet.size(), is(3));
		assertThat(headSet.first(), is(-5L));
		assertThat(headSet.last(), is(-1L));
		assertThat(headSet.contains(-3L), is(true));
		assertThat(headSet.contains(1L), is(false));
		assertThat(headSet.toString(), is("[-5, -3, -1]"));

		Set<Long> equivalentSet = new HashSet<>(asList(-5L, -3L, -1L));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void tailSet() {
		SortedSet<Long> tailSet = set.tailSet(0L);
		assertThat(tailSet, contains(0L, 1L, 2L, 3L, 4L));
		assertThat(tailSet.size(), is(5));
		assertThat(tailSet.first(), is(0L));
		assertThat(tailSet.last(), is(4L));
		assertThat(tailSet.contains(3L), is(true));
		assertThat(tailSet.contains(-1L), is(false));
		assertThat(tailSet.toString(), is("[0, 1, 2, 3, 4]"));

		Set<Long> equivalentSet = new HashSet<>(asList(0L, 1L, 2L, 3L, 4L));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(tailSet.remove(2L), is(true));
		assertThat(tailSet, contains(0L, 1L, 3L, 4L));
		assertThat(tailSet.size(), is(4));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 3L, 4L));

		assertThat(tailSet.remove(2L), is(false));
		assertThat(tailSet, contains(0L, 1L, 3L, 4L));
		assertThat(tailSet.size(), is(4));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 3L, 4L));

		assertThat(tailSet.add(17L), is(true));
		assertThat(tailSet, contains(0L, 1L, 3L, 4L, 17L));
		assertThat(tailSet.size(), is(5));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 3L, 4L, 17L));

		assertThat(tailSet.add(17L), is(false));
		assertThat(tailSet, contains(0L, 1L, 3L, 4L, 17L));
		assertThat(tailSet.size(), is(5));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 3L, 4L, 17L));

		expecting(IllegalArgumentException.class, () -> tailSet.add(-17L));
		assertThat(tailSet, contains(0L, 1L, 3L, 4L, 17L));
		assertThat(tailSet.size(), is(5));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 3L, 4L, 17L));

		assertThat(set.add(5L), is(true));
		assertThat(tailSet, contains(0L, 1L, 3L, 4L, 5L, 17L));
		assertThat(tailSet.size(), is(6));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 3L, 4L, 5L, 17L));

		tailSet.clear();
		assertThat(tailSet, is(emptyIterable()));
		assertThat(tailSet.size(), is(0));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L));
	}

	@Test
	public void sparseTailSet() {
		SortedSet<Long> tailSet = new BitLongSet(-5, -3, -1, 1, 3, 5).tailSet(0);
		assertThat(tailSet, contains(1L, 3L, 5L));
		assertThat(tailSet.size(), is(3));
		assertThat(tailSet.first(), is(1L));
		assertThat(tailSet.last(), is(5L));
		assertThat(tailSet.contains(3L), is(true));
		assertThat(tailSet.contains(-1L), is(false));
		assertThat(tailSet.toString(), is("[1, 3, 5]"));

		Set<Long> equivalentSet = new HashSet<>(asList(1L, 3L, 5L));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void addAllLongCollection() {
		assertThat(empty.addAll(LongList.create(1, 2, 3)), is(true));
		assertThat(empty, contains(1L, 2L, 3L));

		assertThat(set.addAll(LongList.create(3, 4, 5, 6, 7)), is(true));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(set.stream().collect(Collectors.toList()), contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(set.parallelStream().collect(Collectors.toList()),
		           contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L));
	}

	@Test
	public void first() {
		expecting(NoSuchElementException.class, empty::first);
		assertThat(set.first(), is(-5L));
	}

	@Test
	public void last() {
		expecting(NoSuchElementException.class, empty::last);
		assertThat(set.last(), is(4L));
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Long> iterator = set.iterator();
		long value = -5;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is(value));
			iterator.remove();
			value++;
		}
		assertThat(value, is(5L));
		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void removeAllLongCollection() {
		assertThat(empty.removeAll(LongList.create(1L, 2L, 3L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(LongList.create(1L, 2L, 3L)), is(true));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 4L));
	}

	@Test
	public void retainAllLongCollection() {
		assertThat(empty.retainAll(LongList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(LongList.create(1, 2, 3)), is(true));
		assertThat(set, contains(1L, 2L, 3L));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeIf(x -> x > 3), is(true));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L));
	}

	@Test
	public void containsAllLongCollection() {
		assertThat(empty.containsAll(LongList.create(1, 2, 3)), is(false));
		assertThat(set.containsAll(LongList.create(1, 2, 3)), is(true));
		assertThat(set.containsAll(LongList.create(1, 2, 3, 17L)), is(false));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicLong value = new AtomicLong(-5);
		set.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(5L));
	}

	@Test
	public void addAll() {
		assertThat(empty.addAll(asList(1L, 2L, 3L)), is(true));
		assertThat(empty, contains(1L, 2L, 3L));

		assertThat(set.addAll(asList(3L, 4L, 5L, 6L, 7L)), is(true));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(asList(1L, 2L, 3L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(asList(1L, 2L, 3L)), is(true));
		assertThat(set, contains(-5L, -4L, -3L, -2L, -1L, 0L, 4L));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(asList(1L, 2L, 3L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(asList(1L, 2L, 3L)), is(true));
		assertThat(set, contains(1L, 2L, 3L));
	}

	@Test
	public void containsAllCollection() {
		assertThat(empty.containsAll(asList(1L, 2L, 3L)), is(false));
		assertThat(set.containsAll(asList(1L, 2L, 3L)), is(true));
		assertThat(set.containsAll(asList(1L, 2L, 3L, 17L)), is(false));
	}

	@Test
	public void boundaries() {
		BitLongSet set = new BitLongSet();
		assertThat(set.add(Long.MIN_VALUE), is(true));
		assertThat(set.add(0L), is(true));
		assertThat(set.add(Long.MAX_VALUE), is(true));

		assertThat(set, contains(Long.MIN_VALUE, 0L, Long.MAX_VALUE));

		assertThat(set.contains(Long.MIN_VALUE), is(true));
		assertThat(set.contains(0L), is(true));
		assertThat(set.contains(Long.MAX_VALUE), is(true));

		assertThat(set.remove(Long.MIN_VALUE), is(true));
		assertThat(set.remove(0L), is(true));
		assertThat(set.remove(Long.MAX_VALUE), is(true));

		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void fuzz() {
		Long[] randomValues = new Long[1000];
		Random random = new Random();
		for (int i = 0; i < randomValues.length; i++) {
			long randomValue;
			do
				randomValue = random.nextLong();
			while (Arrayz.contains(randomValues, randomValue));
			randomValues[i] = randomValue;
		}

		// Adding
		for (long randomValue : randomValues)
			assertThat(empty.add(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		for (long randomValue : randomValues)
			assertThat(empty.add(randomValue), is(false));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAll(asList(randomValues)), is(true));

		for (long randomValue : randomValues)
			assertThat(empty.contains(randomValue), is(true));

		// toString
		Arrays.sort(randomValues);
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (long randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty.size(), is(0));

		for (long randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(false));
	}
}
