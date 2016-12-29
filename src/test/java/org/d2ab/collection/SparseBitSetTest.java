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

package org.d2ab.collection;

import org.d2ab.collection.longs.LongSortedSet;
import org.d2ab.iterator.longs.LongIterator;
import org.junit.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;

public class SparseBitSetTest {
	private final SparseBitSet empty = new SparseBitSet();
	private final SparseBitSet set = new SparseBitSet(0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486,
	                                                  Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE);

	@Test
	public void setGetClear() throws Exception {
		SparseBitSet set = new SparseBitSet();
		assertThat(set.get(17), is(false));

		assertThat(set.set(17), is(true));
		assertThat(set.get(17), is(true));

		assertThat(set.set(17), is(false));
		assertThat(set.get(17), is(true));

		assertThat(set.clear(17), is(true));
		assertThat(set.get(17), is(false));

		assertThat(set.clear(17), is(false));
		assertThat(set.get(17), is(false));

		assertThat(set.set(17, false), is(false));
		assertThat(set.get(17), is(false));

		assertThat(set.set(17, true), is(true));
		assertThat(set.get(17), is(true));

		assertThat(set.set(17, true), is(false));
		assertThat(set.get(17), is(true));

		assertThat(set.set(17, false), is(true));
		assertThat(set.get(17), is(false));

		expecting(IllegalArgumentException.class, () -> set.get(-1));
		expecting(IllegalArgumentException.class, () -> set.clear(-1));
		expecting(IllegalArgumentException.class, () -> set.set(-1, true));
	}

	@Test
	public void boundaries() throws Exception {
		SparseBitSet set = new SparseBitSet();

		assertThat(set.get(0), is(false));
		assertThat(set.set(0), is(true));
		assertThat(set.get(0), is(true));

		assertThat(set.get(Long.MAX_VALUE), is(false));
		assertThat(set.set(Long.MAX_VALUE), is(true));
		assertThat(set.get(Long.MAX_VALUE), is(true));
	}

	@Test
	public void fullWords() throws Exception {
		SparseBitSet set = new SparseBitSet();

		for (int i = 0; i < 128; i++)
			assertThat(set.set(i), is(true));

		for (int i = 0; i < 128; i++)
			assertThat(set.get(i), is(true));

		StringBuilder expectedToString = new StringBuilder("{");
		for (int i = 0; i < 128; i++)
			expectedToString.append(i > 0 ? ", " : "").append(i);
		expectedToString.append("}");
		assertThat(set.toString(), is(expectedToString.toString()));

		for (int i = 0; i < 128; i += 2)
			assertThat(set.clear(i), is(true));
		assertThat(set.bitCount(), is(64L));

		for (int i = 1; i < 128; i += 2)
			assertThat(set.clear(i), is(true));
		assertThat(set.bitCount(), is(0L));
	}

	@Test
	public void bitCount() {
		assertThat(empty.bitCount(), is(0L));
		assertThat(set.bitCount(), is(14L));
	}

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(set.size(), is(14));
	}

	@Test
	public void sizeOverIntegerMaxInt() {
		assertThat(new SparseBitSet() {
			@Override
			public long bitCount() {
				return Integer.MAX_VALUE;
			}
		}.size(), is(Integer.MAX_VALUE));

		expecting(IllegalStateException.class, () -> new SparseBitSet() {
			@Override
			public long bitCount() {
				return Integer.MAX_VALUE + 1L;
			}
		}.size());
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty.bitCount(), is(0L));

		set.clear();
		assertThat(set.bitCount(), is(0L));
	}

	@Test
	public void firstLong() {
		expecting(NoSuchElementException.class, empty::firstLong);
		assertThat(set.firstLong(), is(0L));
	}

	@Test
	public void lastLong() {
		expecting(NoSuchElementException.class, empty::lastLong);
		assertThat(set.lastLong(), is(Long.MAX_VALUE));
	}

	@Test
	public void iterator() {
		LongIterator iterator = set.iterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(0L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(1L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(2L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(3L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(17L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(42L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(73L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(1222L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(58723484L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(58723485L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(58723486L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(Long.MAX_VALUE - 2));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(Long.MAX_VALUE - 1));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(Long.MAX_VALUE));
		assertThat(iterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, iterator::next);
	}

	@Test
	public void iteratorRemove() {
		LongIterator iterator = set.iterator();

		expecting(IllegalStateException.class, iterator::remove);

		while (iterator.hasNext()) {
			iterator.nextLong();
			iterator.remove();
		}

		expecting(IllegalStateException.class, iterator::remove);

		assertThat(set.bitCount(), is(0L));
	}

	@Test
	public void iteratorFailFast() {
		LongIterator it1 = set.iterator();
		set.addLong(5);
		expecting(ConcurrentModificationException.class, it1::nextLong);

		LongIterator it2 = set.iterator();
		set.removeLong(5);
		expecting(ConcurrentModificationException.class, it2::nextLong);
	}

	@Test
	public void descendingIterator() {
		LongIterator iterator = set.descendingIterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(Long.MAX_VALUE));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(Long.MAX_VALUE - 1));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(Long.MAX_VALUE - 2));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(58723486L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(58723485L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(58723484L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(1222L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(73L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(42L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(17L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(3L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(2L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(1L));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(0L));
		assertThat(iterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, iterator::next);
	}

	@Test
	public void descendingIteratorRemove() {
		LongIterator iterator = set.descendingIterator();

		expecting(IllegalStateException.class, iterator::remove);

		while (iterator.hasNext()) {
			iterator.nextLong();
			iterator.remove();
		}

		expecting(IllegalStateException.class, iterator::remove);

		assertThat(set.bitCount(), is(0L));
	}

	@Test
	public void descendingIteratorFailFast() {
		LongIterator it1 = set.descendingIterator();
		set.addLong(5);
		expecting(ConcurrentModificationException.class, it1::nextLong);

		LongIterator it2 = set.descendingIterator();
		set.removeLong(5);
		expecting(ConcurrentModificationException.class, it2::nextLong);
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("{}"));
		assertThat(set.toString(), is("{0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486, " +
		                              "9223372036854775805, 9223372036854775806, 9223372036854775807}"));
	}

	@Test
	public void testEqualsHashCode() {
		SparseBitSet set1 = new SparseBitSet(20);
		SparseBitSet set2 = new SparseBitSet(10);

		set1.set(0);
		set1.set(17);
		set1.set(32);
		set1.set(73);

		set2.set(0);
		set2.set(17);
		set2.set(32);

		assertThat(set1, is(not(equalTo(set2))));
		assertThat(set2, is(not(equalTo(set1))));
		assertThat(set1.hashCode(), is(not(set2.hashCode())));

		set1.clear(73);
		assertThat(set1, is(equalTo(set2)));
		assertThat(set2, is(equalTo(set1)));
		assertThat(set1.hashCode(), is(set2.hashCode()));

		Set<Long> hashSet = new HashSet<>(asList(0L, 17L, 32L));
		assertThat(set1, is(equalTo(hashSet)));
		assertThat(set2, is(equalTo(hashSet)));
		assertThat(hashSet, is(equalTo(set1)));
		assertThat(hashSet, is(equalTo(set2)));
		assertThat(set1.hashCode(), is(hashSet.hashCode()));
		assertThat(set2.hashCode(), is(hashSet.hashCode()));
	}

	@Test
	public void subSet() {
		LongSortedSet subSet = set.subSet(3, 7777);
		assertThat(subSet, containsLongs(3, 17, 42, 73, 1222));
		assertThat(subSet.size(), is(5));
		assertThat(subSet.firstLong(), is(3L));
		assertThat(subSet.lastLong(), is(1222L));
		assertThat(subSet.containsLong(17), is(true));
		assertThat(subSet.containsLong(5), is(false));
		assertThat(subSet.toString(), is("[3, 17, 42, 73, 1222]"));

		Set<Long> equivalentSet = new HashSet<>(asList(3L, 17L, 42L, 73L, 1222L));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(subSet.removeLong(17), is(true));
		assertThat(subSet, containsLongs(3, 42, 73, 1222));
		assertThat(subSet.size(), is(4));
		assertThat(set, containsLongs(0, 1, 2, 3, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(subSet.removeLong(17), is(false));
		assertThat(subSet, containsLongs(3, 42, 73, 1222));
		assertThat(subSet.size(), is(4));
		assertThat(set, containsLongs(0, 1, 2, 3, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(subSet.addLong(17), is(true));
		assertThat(subSet, containsLongs(3, 17, 42, 73, 1222));
		assertThat(subSet.size(), is(5));
		assertThat(set, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(subSet.addLong(17), is(false));
		assertThat(subSet, containsLongs(3, 17, 42, 73, 1222));
		assertThat(subSet.size(), is(5));
		assertThat(set, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		expecting(IllegalArgumentException.class, () -> subSet.addLong(0));
		assertThat(subSet, containsLongs(3, 17, 42, 73, 1222));
		assertThat(subSet.size(), is(5));
		assertThat(set, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(set.addLong(5), is(true));
		assertThat(subSet, containsLongs(3, 5, 17, 42, 73, 1222));
		assertThat(subSet.size(), is(6));
		assertThat(set, containsLongs(0, 1, 2, 3, 5, 17, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		subSet.clear();
		assertThat(subSet, is(emptyIterable()));
		assertThat(subSet.size(), is(0));
		assertThat(set, containsLongs(0, 1, 2, 58723484, 58723485, 58723486, Long.MAX_VALUE - 2, Long.MAX_VALUE - 1,
		                              Long.MAX_VALUE));
	}

	@Test
	public void headSet() {
		LongSortedSet headSet = set.headSet(7777);
		assertThat(headSet, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222));
		assertThat(headSet.size(), is(8));
		assertThat(headSet.firstLong(), is(0L));
		assertThat(headSet.lastLong(), is(1222L));
		assertThat(headSet.containsLong(17), is(true));
		assertThat(headSet.containsLong(5), is(false));
		assertThat(headSet.toString(), is("[0, 1, 2, 3, 17, 42, 73, 1222]"));

		Set<Long> equivalentSet = new HashSet<>(asList(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(headSet.removeLong(17), is(true));
		assertThat(headSet, containsLongs(0, 1, 2, 3, 42, 73, 1222));
		assertThat(headSet.size(), is(7));
		assertThat(set, containsLongs(0, 1, 2, 3, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(headSet.removeLong(17), is(false));
		assertThat(headSet, containsLongs(0, 1, 2, 3, 42, 73, 1222));
		assertThat(headSet.size(), is(7));
		assertThat(set, containsLongs(0, 1, 2, 3, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(headSet.addLong(17), is(true));
		assertThat(headSet, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222));
		assertThat(headSet.size(), is(8));
		assertThat(set, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(headSet.addLong(17), is(false));
		assertThat(headSet, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222));
		assertThat(headSet.size(), is(8));
		assertThat(set, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		expecting(IllegalArgumentException.class, () -> headSet.addLong(10000));
		assertThat(headSet, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222));
		assertThat(headSet.size(), is(8));
		assertThat(set, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(set.addLong(5), is(true));
		assertThat(headSet, containsLongs(0, 1, 2, 3, 5, 17, 42, 73, 1222));
		assertThat(headSet.size(), is(9));
		assertThat(set, containsLongs(0, 1, 2, 3, 5, 17, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		headSet.clear();
		assertThat(headSet, is(emptyIterable()));
		assertThat(headSet.size(), is(0));
		assertThat(set, containsLongs(58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));
	}

	@Test
	public void tailSet() {
		LongSortedSet tailSet = set.tailSet(1222);
		assertThat(tailSet, containsLongs(1222, 58723484, 58723485, 58723486, Long.MAX_VALUE - 2,
		                                  Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet.size(), is(7));
		assertThat(tailSet.firstLong(), is(1222L));
		assertThat(tailSet.lastLong(), is(Long.MAX_VALUE));
		assertThat(tailSet.containsLong(58723484), is(true));
		assertThat(tailSet.containsLong(7777), is(false));
		assertThat(tailSet.toString(),
		           is("[1222, 58723484, 58723485, 58723486, 9223372036854775805, 9223372036854775806, " +
		              "9223372036854775807]"));

		Set<Long> equivalentSet = new HashSet<>(asList(1222L, 58723484L, 58723485L, 58723486L, Long.MAX_VALUE - 2,
		                                               Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(tailSet.removeLong(58723484), is(true));
		assertThat(tailSet, containsLongs(1222, 58723485, 58723486, Long.MAX_VALUE - 2,
		                                  Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet.size(), is(6));
		assertThat(set, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(tailSet.removeLong(58723484), is(false));
		assertThat(tailSet, containsLongs(1222, 58723485, 58723486, Long.MAX_VALUE - 2,
		                                  Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet.size(), is(6));
		assertThat(set, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(tailSet.addLong(58723484), is(true));
		assertThat(tailSet, containsLongs(1222, 58723484, 58723485, 58723486, Long.MAX_VALUE - 2,
		                                  Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet.size(), is(7));
		assertThat(set, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(tailSet.addLong(58723484), is(false));
		assertThat(tailSet, containsLongs(1222, 58723484, 58723485, 58723486, Long.MAX_VALUE - 2,
		                                  Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet.size(), is(7));
		assertThat(set, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		expecting(IllegalArgumentException.class, () -> tailSet.addLong(0));
		assertThat(tailSet, containsLongs(1222, 58723484, 58723485, 58723486, Long.MAX_VALUE - 2,
		                                  Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet.size(), is(7));
		assertThat(set, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(set.addLong(1223), is(true));
		assertThat(tailSet, containsLongs(1222, 1223, 58723484, 58723485, 58723486, Long.MAX_VALUE - 2,
		                                  Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet.size(), is(8));
		assertThat(set, containsLongs(0, 1, 2, 3, 17, 42, 73, 1222, 1223, 58723484, 58723485, 58723486,
		                              Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		tailSet.clear();
		assertThat(tailSet, is(emptyIterable()));
		assertThat(tailSet.size(), is(0));
		assertThat(set, containsLongs(0, 1, 2, 3, 17, 42, 73));
	}

	@Test
	public void fuzz() {
		long[] randomIndices = new long[1000];
		Random random = new Random();
		for (int i = 0; i < randomIndices.length; i++) {
			long randomIndex;
			do
				randomIndex = Math.abs(random.nextLong());
			while (Arrayz.contains(randomIndices, randomIndex));
			randomIndices[i] = randomIndex;
		}

		SparseBitSet set = new SparseBitSet();
		for (long randomIndex : randomIndices)
			assertThat(set.set(randomIndex), is(true));
		assertThat(set.bitCount(), is((long) randomIndices.length));

		for (long randomIndex : randomIndices)
			assertThat(set.set(randomIndex), is(false));

		for (long randomIndex : randomIndices)
			assertThat(set.get(randomIndex), is(true));

		Arrays.sort(randomIndices);
		StringBuilder expectedToString = new StringBuilder("{");
		for (int i = 0; i < randomIndices.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomIndices[i]);
		expectedToString.append("}");
		assertThat(set.toString(), is(expectedToString.toString()));

		for (long randomIndex : randomIndices)
			assertThat(set.clear(randomIndex), is(true));
		assertThat(set.toString(), is("{}"));
		assertThat(set.bitCount(), is(0L));

		for (long randomIndex : randomIndices)
			assertThat(set.clear(randomIndex), is(false));
	}
}
