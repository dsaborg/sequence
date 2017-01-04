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

import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class SparseBitSetBoxingTest extends BaseBoxingTest {
	private final SortedSet<Long> empty = new SparseBitSet();
	private final SortedSet<Long> set = new SparseBitSet(0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486,
	                                                     Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE);

	@Test
	public void setGetClear() throws Exception {
		assertThat(empty.contains(17L), is(false));

		assertThat(empty.add(17L), is(true));
		assertThat(empty.contains(17L), is(true));

		assertThat(empty.add(17L), is(false));
		assertThat(empty.contains(17L), is(true));

		assertThat(empty.remove(17L), is(true));
		assertThat(empty.contains(17L), is(false));

		assertThat(empty.remove(17L), is(false));
		assertThat(empty.contains(17L), is(false));

		expecting(IllegalArgumentException.class, () -> empty.contains(-1L));
		expecting(IllegalArgumentException.class, () -> empty.remove(-1L));
		expecting(IllegalArgumentException.class, () -> empty.add(-1L));
	}

	@Test
	public void boundaries() throws Exception {
		SortedSet<Long> set = new SparseBitSet();

		assertThat(set.contains(0L), is(false));
		assertThat(set.add(0L), is(true));
		assertThat(set.contains(0L), is(true));

		assertThat(set.contains(Long.MAX_VALUE), is(false));
		assertThat(set.add(Long.MAX_VALUE), is(true));
		assertThat(set.contains(Long.MAX_VALUE), is(true));
	}

	@Test
	public void fullWords() throws Exception {
		SortedSet<Long> set = new SparseBitSet();

		for (long i = 0; i < 128; i++)
			assertThat(set.add(i), is(true));

		for (long i = 0; i < 128; i++)
			assertThat(set.contains(i), is(true));

		StringBuilder expectedToString = new StringBuilder("{");
		for (long i = 0; i < 128; i++)
			expectedToString.append(i > 0 ? ", " : "").append(i);
		expectedToString.append("}");
		assertThat(set.toString(), is(expectedToString.toString()));

		for (long i = 0; i < 128; i += 2)
			assertThat(set.remove(i), is(true));
		assertThat(set.size(), is(64));

		for (long i = 1; i < 128; i += 2)
			assertThat(set.remove(i), is(true));
		assertThat(set.size(), is(0));
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
		assertThat(empty.size(), is(0));

		set.clear();
		assertThat(set.size(), is(0));
	}

	@Test
	public void first() {
		expecting(NoSuchElementException.class, empty::first);
		assertThat(set.first(), is(0L));
	}

	@Test
	public void last() {
		expecting(NoSuchElementException.class, empty::last);
		assertThat(set.last(), is(Long.MAX_VALUE));
	}

	@Test
	public void iterator() {
		Iterator<Long> iterator = set.iterator();
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
		Iterator<Long> iterator = set.iterator();

		expecting(IllegalStateException.class, iterator::remove);

		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}

		expecting(IllegalStateException.class, iterator::remove);

		assertThat(set.size(), is(0));
	}

	@Test
	public void iteratorFailFast() {
		Iterator<Long> it1 = set.iterator();
		set.add(5L);
		expecting(ConcurrentModificationException.class, it1::next);

		Iterator<Long> it2 = set.iterator();
		set.remove(5L);
		expecting(ConcurrentModificationException.class, it2::next);
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("{}"));
		assertThat(set.toString(), is("{0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486, " +
		                              "9223372036854775805, 9223372036854775806, 9223372036854775807}"));
	}

	@Test
	public void testEqualsHashCode() {
		SortedSet<Long> set1 = new SparseBitSet(20);
		SortedSet<Long> set2 = new SparseBitSet(10);

		set1.add(0L);
		set1.add(17L);
		set1.add(32L);
		set1.add(73L);

		set2.add(0L);
		set2.add(17L);
		set2.add(32L);

		assertThat(set1, is(not(equalTo(set2))));
		assertThat(set2, is(not(equalTo(set1))));
		assertThat(set1.hashCode(), is(not(set2.hashCode())));

		set1.remove(73L);
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
		SortedSet<Long> subSet = set.subSet(3L, 7777L);
		assertThat(subSet, contains(3L, 17L, 42L, 73L, 1222L));
		assertThat(subSet.size(), is(5));
		assertThat(subSet.first(), is(3L));
		assertThat(subSet.last(), is(1222L));
		assertThat(subSet.contains(17L), is(true));
		assertThat(subSet.contains(5L), is(false));
		assertThat(subSet.toString(), is("[3, 17, 42, 73, 1222]"));

		Set<Long> equivalentSet = new HashSet<>(asList(3L, 17L, 42L, 73L, 1222L));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(subSet.remove(17L), is(true));
		assertThat(subSet, contains(3L, 42L, 73L, 1222L));
		assertThat(subSet.size(), is(4));
		assertThat(set, contains(0L, 1L, 2L, 3L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(subSet.remove(17L), is(false));
		assertThat(subSet, contains(3L, 42L, 73L, 1222L));
		assertThat(subSet.size(), is(4));
		assertThat(set, contains(0L, 1L, 2L, 3L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(subSet.add(17L), is(true));
		assertThat(subSet, contains(3L, 17L, 42L, 73L, 1222L));
		assertThat(subSet.size(), is(5));
		assertThat(set, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(subSet.add(17L), is(false));
		assertThat(subSet, contains(3L, 17L, 42L, 73L, 1222L));
		assertThat(subSet.size(), is(5));
		assertThat(set, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		expecting(IllegalArgumentException.class, () -> subSet.add(0L));
		assertThat(subSet, contains(3L, 17L, 42L, 73L, 1222L));
		assertThat(subSet.size(), is(5));
		assertThat(set, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(set.add(5L), is(true));
		assertThat(subSet, contains(3L, 5L, 17L, 42L, 73L, 1222L));
		assertThat(subSet.size(), is(6));
		assertThat(set, contains(0L, 1L, 2L, 3L, 5L, 17L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		subSet.clear();
		assertThat(subSet, is(emptyIterable()));
		assertThat(subSet.size(), is(0));
		assertThat(set, contains(0L, 1L, 2L, 58723484L, 58723485L, 58723486L, Long.MAX_VALUE - 2, Long.MAX_VALUE - 1,
		                         Long.MAX_VALUE));
	}

	@Test
	public void headSet() {
		SortedSet<Long> headSet = set.headSet(7777L);
		assertThat(headSet, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L));
		assertThat(headSet.size(), is(8));
		assertThat(headSet.first(), is(0L));
		assertThat(headSet.last(), is(1222L));
		assertThat(headSet.contains(17L), is(true));
		assertThat(headSet.contains(5L), is(false));
		assertThat(headSet.toString(), is("[0, 1, 2, 3, 17, 42, 73, 1222]"));

		Set<Long> equivalentSet = new HashSet<>(asList(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(headSet.remove(17L), is(true));
		assertThat(headSet, contains(0L, 1L, 2L, 3L, 42L, 73L, 1222L));
		assertThat(headSet.size(), is(7));
		assertThat(set, contains(0L, 1L, 2L, 3L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(headSet.remove(17L), is(false));
		assertThat(headSet, contains(0L, 1L, 2L, 3L, 42L, 73L, 1222L));
		assertThat(headSet.size(), is(7));
		assertThat(set, contains(0L, 1L, 2L, 3L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(headSet.add(17L), is(true));
		assertThat(headSet, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L));
		assertThat(headSet.size(), is(8));
		assertThat(set, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(headSet.add(17L), is(false));
		assertThat(headSet, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L));
		assertThat(headSet.size(), is(8));
		assertThat(set, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		expecting(IllegalArgumentException.class, () -> headSet.add(10000L));
		assertThat(headSet, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L));
		assertThat(headSet.size(), is(8));
		assertThat(set, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(set.add(5L), is(true));
		assertThat(headSet, contains(0L, 1L, 2L, 3L, 5L, 17L, 42L, 73L, 1222L));
		assertThat(headSet.size(), is(9));
		assertThat(set, contains(0L, 1L, 2L, 3L, 5L, 17L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		headSet.clear();
		assertThat(headSet, is(emptyIterable()));
		assertThat(headSet.size(), is(0));
		assertThat(set, contains(58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));
	}

	@Test
	public void tailSet() {
		SortedSet<Long> tailSet = set.tailSet(1222L);
		assertThat(tailSet, contains(1222L, 58723484L, 58723485L, 58723486L, Long.MAX_VALUE - 2,
		                             Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet.size(), is(7));
		assertThat(tailSet.first(), is(1222L));
		assertThat(tailSet.last(), is(Long.MAX_VALUE));
		assertThat(tailSet.contains(58723484L), is(true));
		assertThat(tailSet.contains(7777L), is(false));
		assertThat(tailSet.toString(),
		           is("[1222, 58723484, 58723485, 58723486, 9223372036854775805, 9223372036854775806, " +
		              "9223372036854775807]"));

		Set<Long> equivalentSet = new HashSet<>(asList(1222L, 58723484L, 58723485L, 58723486L,
		                                               Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(tailSet.remove(58723484L), is(true));
		assertThat(tailSet, contains(1222L, 58723485L, 58723486L, Long.MAX_VALUE - 2,
		                             Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet.size(), is(6));
		assertThat(set, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(tailSet.remove(58723484L), is(false));
		assertThat(tailSet, contains(1222L, 58723485L, 58723486L, Long.MAX_VALUE - 2,
		                             Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet.size(), is(6));
		assertThat(set, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(tailSet.add(58723484L), is(true));
		assertThat(tailSet, contains(1222L, 58723484L, 58723485L, 58723486L, Long.MAX_VALUE - 2,
		                             Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet.size(), is(7));
		assertThat(set, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(tailSet.add(58723484L), is(false));
		assertThat(tailSet, contains(1222L, 58723484L, 58723485L, 58723486L, Long.MAX_VALUE - 2,
		                             Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet.size(), is(7));
		assertThat(set, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		expecting(IllegalArgumentException.class, () -> tailSet.add(0L));
		assertThat(tailSet, contains(1222L, 58723484L, 58723485L, 58723486L, Long.MAX_VALUE - 2,
		                             Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet.size(), is(7));
		assertThat(set, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		assertThat(set.add(1223L), is(true));
		assertThat(tailSet, contains(1222L, 1223L, 58723484L, 58723485L, 58723486L, Long.MAX_VALUE - 2,
		                             Long.MAX_VALUE - 1, Long.MAX_VALUE));
		assertThat(tailSet.size(), is(8));
		assertThat(set, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L, 1222L, 1223L, 58723484L, 58723485L, 58723486L,
		                         Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE));

		tailSet.clear();
		assertThat(tailSet, is(emptyIterable()));
		assertThat(tailSet.size(), is(0));
		assertThat(set, contains(0L, 1L, 2L, 3L, 17L, 42L, 73L));
	}

	@Test
	public void fuzz() {
		Long[] randomIndices = new Long[1000];
		Random random = new Random();
		for (int i = 0; i < randomIndices.length; i++) {
			long randomIndex;
			do
				randomIndex = Math.abs(random.nextLong());
			while (Arrayz.contains(randomIndices, randomIndex));
			randomIndices[i] = randomIndex;
		}

		SortedSet<Long> set = new SparseBitSet();
		for (long randomIndex : randomIndices)
			assertThat(set.add(randomIndex), is(true));
		assertThat(set.size(), is(randomIndices.length));

		for (long randomIndex : randomIndices)
			assertThat(set.add(randomIndex), is(false));

		for (long randomIndex : randomIndices)
			assertThat(set.contains(randomIndex), is(true));

		Arrays.sort(randomIndices);
		StringBuilder expectedToString = new StringBuilder("{");
		for (int i = 0; i < randomIndices.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomIndices[i]);
		expectedToString.append("}");
		assertThat(set.toString(), is(expectedToString.toString()));

		for (long randomIndex : randomIndices)
			assertThat(set.remove(randomIndex), is(true));
		assertThat(set.toString(), is("{}"));
		assertThat(set.size(), is(0));

		for (long randomIndex : randomIndices)
			assertThat(set.remove(randomIndex), is(false));
	}
}
