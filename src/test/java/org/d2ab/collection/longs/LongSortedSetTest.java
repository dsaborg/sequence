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
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.test.StrictLongIterator;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;

public class LongSortedSetTest {
	private final LongSet backingEmpty = new BitLongSet();
	private final LongSortedSet empty = new LongSortedSet.Base() {
		@Override
		public LongIterator iterator() {
			return StrictLongIterator.from(backingEmpty.iterator());
		}

		@Override
		public int size() {
			return backingEmpty.size();
		}

		@Override
		public boolean addLong(long x) {
			return backingEmpty.addLong(x);
		}
	};

	private final LongSet backing = new BitLongSet(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);
	private final LongSortedSet set = new LongSortedSet.Base() {
		@Override
		public LongIterator iterator() {
			return StrictLongIterator.from(backing.iterator());
		}

		@Override
		public int size() {
			return backing.size();
		}

		@Override
		public boolean addLong(long x) {
			return backing.addLong(x);
		}
	};

	@Test
	public void create() {
		assertThat(LongSortedSet.create(), is(emptyIterable()));
		assertThat(LongSortedSet.create(-2, -1, 0, 1), containsLongs(-2, -1, 0, 1));
	}

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(set.size(), is(10));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void iteratorFailFastPositives() {
		LongIterator it1 = set.iterator();
		assertThat(it1.hasNext(), is(true));
		assertThat(set.addLong(17), is(true));
		expecting(ConcurrentModificationException.class, it1::nextLong);

		LongIterator it2 = set.iterator();
		assertThat(it2.hasNext(), is(true));
		assertThat(set.removeLong(17), is(true));
		expecting(ConcurrentModificationException.class, it2::nextLong);
	}

	@Test
	public void iteratorFailFastNegatives() {
		LongIterator it1 = set.iterator();
		assertThat(it1.hasNext(), is(true));
		assertThat(set.addLong(-17), is(true));
		expecting(ConcurrentModificationException.class, it1::nextLong);

		LongIterator it2 = set.iterator();
		assertThat(it2.hasNext(), is(true));
		assertThat(set.removeLong(-17), is(true));
		expecting(ConcurrentModificationException.class, it2::nextLong);
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
	public void addLong() {
		empty.addLong(17);
		assertThat(empty, containsLongs(17));

		set.addLong(17);
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
	}

	@Test
	public void containsLong() {
		assertThat(empty.containsLong(17), is(false));

		assertThat(set.containsLong(17), is(false));
		for (long x = -5; x <= 4; x++)
			assertThat(set.containsLong(x), is(true));
	}

	@Test
	public void removeLong() {
		assertThat(empty.removeLong(17), is(false));

		assertThat(set.removeLong(17), is(false));
		for (long x = -5; x <= 4; x++)
			assertThat(set.removeLong(x), is(true));
		assertThat(set.isEmpty(), is(true));
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
		BitLongSet set2 = new BitLongSet(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17);
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.removeLong(17);

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void subSet() {
		LongSortedSet subSet = set.subSet(-3, 3);
		assertThat(subSet, containsLongs(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(subSet.firstLong(), is(-3L));
		assertThat(subSet.lastLong(), is(2L));
		assertThat(subSet.containsLong(1), is(true));
		assertThat(subSet.containsLong(3), is(false));
		assertThat(subSet.toString(), is("[-3, -2, -1, 0, 1, 2]"));

		Set<Long> equivalentSet = new HashSet<>(asList(-3L, -2L, -1L, 0L, 1L, 2L));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(subSet.removeLong(0), is(true));
		assertThat(subSet, containsLongs(-3, -2, -1, 1, 2));
		assertThat(subSet.size(), is(5));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 1, 2, 3, 4));

		assertThat(subSet.removeLong(0), is(false));
		assertThat(subSet, containsLongs(-3, -2, -1, 1, 2));
		assertThat(subSet.size(), is(5));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 1, 2, 3, 4));

		assertThat(subSet.addLong(0), is(true));
		assertThat(subSet, containsLongs(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));

		assertThat(subSet.addLong(0), is(false));
		assertThat(subSet, containsLongs(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));

		expecting(IllegalArgumentException.class, () -> subSet.addLong(-17));
		assertThat(subSet, containsLongs(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));

		subSet.clear();
		assertThat(subSet, is(emptyIterable()));
		assertThat(subSet.size(), is(0));
		assertThat(set, containsLongs(-5, -4, 3, 4));
	}

	@Test
	public void sparseSubSet() {
		LongSortedSet subSet = new BitLongSet(-5, -3, -1, 1, 3, 5).subSet(-2, 2);
		assertThat(subSet, containsLongs(-1, 1));
		assertThat(subSet.size(), is(2));
		assertThat(subSet.firstLong(), is(-1L));
		assertThat(subSet.lastLong(), is(1L));
		assertThat(subSet.containsLong(1), is(true));
		assertThat(subSet.containsLong(-3), is(false));
		assertThat(subSet.toString(), is("[-1, 1]"));

		Set<Long> equivalentSet = new HashSet<>(asList(-1L, 1L));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void headSet() {
		LongSortedSet headSet = set.headSet(0);
		assertThat(headSet, containsLongs(-5, -4, -3, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(headSet.firstLong(), is(-5L));
		assertThat(headSet.lastLong(), is(-1L));
		assertThat(headSet.containsLong(-3), is(true));
		assertThat(headSet.containsLong(0), is(false));
		assertThat(headSet.toString(), is("[-5, -4, -3, -2, -1]"));

		Set<Long> equivalentSet = new HashSet<>(asList(-5L, -4L, -3L, -2L, -1L));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(headSet.removeLong(-3), is(true));
		assertThat(headSet, containsLongs(-5, -4, -2, -1));
		assertThat(headSet.size(), is(4));
		assertThat(set, containsLongs(-5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(headSet.removeLong(-3), is(false));
		assertThat(headSet, containsLongs(-5, -4, -2, -1));
		assertThat(headSet.size(), is(4));
		assertThat(set, containsLongs(-5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(headSet.addLong(-17), is(true));
		assertThat(headSet, containsLongs(-17, -5, -4, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(set, containsLongs(-17, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(headSet.addLong(-17), is(false));
		assertThat(headSet, containsLongs(-17, -5, -4, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(set, containsLongs(-17, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		expecting(IllegalArgumentException.class, () -> headSet.addLong(17));
		assertThat(headSet, containsLongs(-17, -5, -4, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(set, containsLongs(-17, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(set.addLong(-6), is(true));
		assertThat(headSet, containsLongs(-17, -6, -5, -4, -2, -1));
		assertThat(headSet.size(), is(6));
		assertThat(set, containsLongs(-17, -6, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		headSet.clear();
		assertThat(headSet, is(emptyIterable()));
		assertThat(headSet.size(), is(0));
		assertThat(set, containsLongs(0, 1, 2, 3, 4));
	}

	@Test
	public void sparseHeadSet() {
		LongSortedSet headSet = new BitLongSet(-5, -3, -1, 1, 3, 5).headSet(0);
		assertThat(headSet, containsLongs(-5, -3, -1));
		assertThat(headSet.size(), is(3));
		assertThat(headSet.firstLong(), is(-5L));
		assertThat(headSet.lastLong(), is(-1L));
		assertThat(headSet.containsLong(-3), is(true));
		assertThat(headSet.containsLong(1), is(false));
		assertThat(headSet.toString(), is("[-5, -3, -1]"));

		Set<Long> equivalentSet = new HashSet<>(asList(-5L, -3L, -1L));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void tailSet() {
		LongSortedSet tailSet = set.tailSet(0);
		assertThat(tailSet, containsLongs(0, 1, 2, 3, 4));
		assertThat(tailSet.size(), is(5));
		assertThat(tailSet.firstLong(), is(0L));
		assertThat(tailSet.lastLong(), is(4L));
		assertThat(tailSet.containsLong(3), is(true));
		assertThat(tailSet.containsLong(-1), is(false));
		assertThat(tailSet.toString(), is("[0, 1, 2, 3, 4]"));

		Set<Long> equivalentSet = new HashSet<>(asList(0L, 1L, 2L, 3L, 4L));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(tailSet.removeLong(2), is(true));
		assertThat(tailSet, containsLongs(0, 1, 3, 4));
		assertThat(tailSet.size(), is(4));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 3, 4));

		assertThat(tailSet.removeLong(2), is(false));
		assertThat(tailSet, containsLongs(0, 1, 3, 4));
		assertThat(tailSet.size(), is(4));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 3, 4));

		assertThat(tailSet.addLong(17), is(true));
		assertThat(tailSet, containsLongs(0, 1, 3, 4, 17));
		assertThat(tailSet.size(), is(5));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 3, 4, 17));

		assertThat(tailSet.addLong(17), is(false));
		assertThat(tailSet, containsLongs(0, 1, 3, 4, 17));
		assertThat(tailSet.size(), is(5));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 3, 4, 17));

		expecting(IllegalArgumentException.class, () -> tailSet.addLong(-17));
		assertThat(tailSet, containsLongs(0, 1, 3, 4, 17));
		assertThat(tailSet.size(), is(5));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 3, 4, 17));

		assertThat(set.addLong(5), is(true));
		assertThat(tailSet, containsLongs(0, 1, 3, 4, 5, 17));
		assertThat(tailSet.size(), is(6));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 3, 4, 5, 17));

		tailSet.clear();
		assertThat(tailSet, is(emptyIterable()));
		assertThat(tailSet.size(), is(0));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1));
	}

	@Test
	public void sparseTailSet() {
		LongSortedSet tailSet = new BitLongSet(-5, -3, -1, 1, 3, 5).tailSet(0);
		assertThat(tailSet, containsLongs(1, 3, 5));
		assertThat(tailSet.size(), is(3));
		assertThat(tailSet.firstLong(), is(1L));
		assertThat(tailSet.lastLong(), is(5L));
		assertThat(tailSet.containsLong(3), is(true));
		assertThat(tailSet.containsLong(-1), is(false));
		assertThat(tailSet.toString(), is("[1, 3, 5]"));

		Set<Long> equivalentSet = new HashSet<>(asList(1L, 3L, 5L));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void addAllLongArray() {
		assertThat(empty.addAllLongs(1, 2, 3), is(true));
		assertThat(empty, containsLongs(1, 2, 3));

		assertThat(set.addAllLongs(3, 4, 5, 6, 7), is(true));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void addAllLongCollection() {
		assertThat(empty.addAllLongs(LongList.create(1, 2, 3)), is(true));
		assertThat(empty, containsLongs(1, 2, 3));

		assertThat(set.addAllLongs(LongList.create(3, 4, 5, 6, 7)), is(true));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
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
	public void longStream() {
		assertThat(empty.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           is(emptyIterable()));

		assertThat(set.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void parallelLongStream() {
		assertThat(empty.parallelLongStream()
		                .collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           is(emptyIterable()));

		assertThat(set.parallelLongStream()
		              .collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(set.sequence(), containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void firstLong() {
		expecting(NoSuchElementException.class, empty::firstLong);
		assertThat(set.firstLong(), is(-5L));
	}

	@Test
	public void lastLong() {
		expecting(NoSuchElementException.class, empty::lastLong);
		assertThat(set.lastLong(), is(4L));
	}

	@Test
	public void iteratorRemoveAll() {
		LongIterator iterator = set.iterator();
		long value = -5;
		while (iterator.hasNext()) {
			assertThat(iterator.nextLong(), is(value));
			iterator.remove();
			value++;
		}
		assertThat(value, is(5L));
		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void removeAllLongArray() {
		assertThat(empty.removeAllLongs(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAllLongs(1, 2, 3), is(true));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void removeAllLongCollection() {
		assertThat(empty.removeAll(LongList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(LongList.create(1, 2, 3)), is(true));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void retainAllLongArray() {
		assertThat(empty.retainAllLongs(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAllLongs(1, 2, 3), is(true));
		assertThat(set, containsLongs(1, 2, 3));
	}

	@Test
	public void retainAllLongCollection() {
		assertThat(empty.retainAll(LongList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(LongList.create(1, 2, 3)), is(true));
		assertThat(set, containsLongs(1, 2, 3));
	}

	@Test
	public void removeLongsIf() {
		assertThat(empty.removeLongsIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeLongsIf(x -> x > 3), is(true));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3));
	}

	@Test
	public void containsAllLongArray() {
		assertThat(empty.containsAllLongs(1, 2, 3), is(false));
		assertThat(set.containsAllLongs(1, 2, 3), is(true));
		assertThat(set.containsAllLongs(1, 2, 3, 17), is(false));
	}

	@Test
	public void containsAllLongCollection() {
		assertThat(empty.containsAll(LongList.create(1, 2, 3)), is(false));
		assertThat(set.containsAll(LongList.create(1, 2, 3)), is(true));
		assertThat(set.containsAll(LongList.create(1, 2, 3, 17)), is(false));
	}

	@Test
	public void forEachLong() {
		empty.forEachLong(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicLong value = new AtomicLong(-5);
		set.forEachLong(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(5L));
	}

	@Test
	public void boundaries() {
		BitLongSet set = new BitLongSet();
		assertThat(set.addLong(Long.MIN_VALUE), is(true));
		assertThat(set.addLong(0), is(true));
		assertThat(set.addLong(Long.MAX_VALUE), is(true));

		assertThat(set, containsLongs(Long.MIN_VALUE, 0, Long.MAX_VALUE));

		assertThat(set.containsLong(Long.MIN_VALUE), is(true));
		assertThat(set.containsLong(0), is(true));
		assertThat(set.containsLong(Long.MAX_VALUE), is(true));

		assertThat(set.removeLong(Long.MIN_VALUE), is(true));
		assertThat(set.removeLong(0), is(true));
		assertThat(set.removeLong(Long.MAX_VALUE), is(true));

		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void fuzz() {
		long[] randomValues = new long[1000];
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
			assertThat(empty.addLong(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		for (long randomValue : randomValues)
			assertThat(empty.addLong(randomValue), is(false));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAllLongs(randomValues), is(true));

		for (long randomValue : randomValues)
			assertThat(empty.containsLong(randomValue), is(true));

		// toString
		Arrays.sort(randomValues);
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (long randomValue : randomValues)
			assertThat(empty.removeLong(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty.size(), is(0));

		for (long randomValue : randomValues)
			assertThat(empty.removeLong(randomValue), is(false));
	}
}
