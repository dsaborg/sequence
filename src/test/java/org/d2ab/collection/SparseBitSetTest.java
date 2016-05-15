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

import org.d2ab.collection.iterator.LongIterator;
import org.junit.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class SparseBitSetTest {

	private final SparseBitSet empty = new SparseBitSet();
	private final SparseBitSet bitSet = new SparseBitSet(0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486,
	                                                     Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE);

	@Test
	public void setGetClear() throws Exception {
		SparseBitSet bitSet = new SparseBitSet();
		assertThat(bitSet.get(17), is(false));

		assertThat(bitSet.set(17), is(true));
		assertThat(bitSet.get(17), is(true));

		assertThat(bitSet.set(17), is(false));
		assertThat(bitSet.get(17), is(true));

		assertThat(bitSet.clear(17), is(true));
		assertThat(bitSet.get(17), is(false));

		assertThat(bitSet.clear(17), is(false));
		assertThat(bitSet.get(17), is(false));

		assertThat(bitSet.set(17, false), is(false));
		assertThat(bitSet.get(17), is(false));

		assertThat(bitSet.set(17, true), is(true));
		assertThat(bitSet.get(17), is(true));

		assertThat(bitSet.set(17, true), is(false));
		assertThat(bitSet.get(17), is(true));

		assertThat(bitSet.set(17, false), is(true));
		assertThat(bitSet.get(17), is(false));
	}

	@Test
	public void boundaries() throws Exception {
		SparseBitSet bitSet = new SparseBitSet();

		assertThat(bitSet.get(0), is(false));
		assertThat(bitSet.set(0), is(true));
		assertThat(bitSet.get(0), is(true));

		assertThat(bitSet.get(Long.MAX_VALUE), is(false));
		assertThat(bitSet.set(Long.MAX_VALUE), is(true));
		assertThat(bitSet.get(Long.MAX_VALUE), is(true));
	}

	@Test
	public void fullWords() throws Exception {
		SparseBitSet bitSet = new SparseBitSet();

		for (int i = 0; i < 128; i++)
			assertThat(bitSet.set(i), is(true));

		for (int i = 0; i < 128; i++)
			assertThat(bitSet.get(i), is(true));

		StringBuilder expectedToString = new StringBuilder("{");
		for (int i = 0; i < 128; i++)
			expectedToString.append(i > 0 ? ", " : "").append(i);
		expectedToString.append("}");
		assertThat(bitSet.toString(), is(expectedToString.toString()));

		for (int i = 0; i < 128; i += 2)
			assertThat(bitSet.clear(i), is(true));
		assertThat(bitSet.bitCount(), is(64L));

		for (int i = 1; i < 128; i += 2)
			assertThat(bitSet.clear(i), is(true));
		assertThat(bitSet.bitCount(), is(0L));
	}

	@Test
	public void bitCount() {
		assertThat(empty.bitCount(), is(0L));
		assertThat(bitSet.bitCount(), is(14L));
	}

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(bitSet.size(), is(14));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty.bitCount(), is(0L));

		bitSet.clear();
		assertThat(bitSet.bitCount(), is(0L));
	}

	@Test
	public void firstLong() {
		expecting(NoSuchElementException.class, empty::firstLong);
		assertThat(bitSet.firstLong(), is(0L));
	}

	@Test
	public void lastLong() {
		expecting(NoSuchElementException.class, empty::lastLong);
		assertThat(bitSet.lastLong(), is(Long.MAX_VALUE));
	}

	@Test
	public void iterator() {
		LongIterator iterator = bitSet.iterator();
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
	}

	@Test
	public void iteratorRemove() {
		LongIterator iterator = bitSet.iterator();

		expecting(IllegalStateException.class, iterator::remove);

		while (iterator.hasNext()) {
			iterator.nextLong();
			iterator.remove();
		}

		expecting(IllegalStateException.class, iterator::remove);

		assertThat(bitSet.bitCount(), is(0L));
	}

	@Test
	public void descendingIterator() {
		LongIterator iterator = bitSet.descendingIterator();
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
	}

	@Test
	public void descendingRemove() {
		LongIterator iterator = bitSet.descendingIterator();

		expecting(IllegalStateException.class, iterator::remove);

		while (iterator.hasNext()) {
			iterator.nextLong();
			iterator.remove();
		}

		expecting(IllegalStateException.class, iterator::remove);

		assertThat(bitSet.bitCount(), is(0L));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("{}"));
		assertThat(bitSet.toString(), is("{0, 1, 2, 3, 17, 42, 73, 1222, 58723484, 58723485, 58723486, " +
		                                 "9223372036854775805, 9223372036854775806, 9223372036854775807}"));
	}

	@Test
	public void testEqualsHashCode() {
		SparseBitSet bitSet1 = new SparseBitSet(20);
		SparseBitSet bitSet2 = new SparseBitSet(10);

		bitSet1.set(0);
		bitSet1.set(17);
		bitSet1.set(32);
		bitSet1.set(73);

		bitSet2.set(0);
		bitSet2.set(17);
		bitSet2.set(32);

		assertThat(bitSet1, is(not(equalTo(bitSet2))));
		assertThat(bitSet2, is(not(equalTo(bitSet1))));
		assertThat(bitSet1.hashCode(), is(not(bitSet2.hashCode())));

		bitSet1.clear(73);
		assertThat(bitSet1, is(equalTo(bitSet2)));
		assertThat(bitSet2, is(equalTo(bitSet1)));
		assertThat(bitSet1.hashCode(), is(bitSet2.hashCode()));

		Set<Long> hashSet = new HashSet<>(asList(0L, 17L, 32L));
		assertThat(bitSet1, is(equalTo(hashSet)));
		assertThat(bitSet2, is(equalTo(hashSet)));
		assertThat(hashSet, is(equalTo(bitSet1)));
		assertThat(hashSet, is(equalTo(bitSet2)));
		assertThat(bitSet1.hashCode(), is(hashSet.hashCode()));
		assertThat(bitSet2.hashCode(), is(hashSet.hashCode()));
	}

	@Test
	public void fuzz() {
		long[] randomIndices = new long[10000];
		Random random = new Random();
		for (int i = 0; i < randomIndices.length; i++) {
			long randomIndex;
			do
				randomIndex = Math.abs(random.nextLong());
			while (Arrayz.contains(randomIndices, randomIndex));
			randomIndices[i] = randomIndex;
		}

		SparseBitSet bitSet = new SparseBitSet();
		for (long randomIndex : randomIndices)
			assertThat(bitSet.set(randomIndex), is(true));
		assertThat(bitSet.bitCount(), is((long) randomIndices.length));

		for (long randomIndex : randomIndices)
			assertThat(bitSet.set(randomIndex), is(false));

		for (long randomIndex : randomIndices)
			assertThat(bitSet.get(randomIndex), is(true));

		Arrays.sort(randomIndices);
		StringBuilder expectedToString = new StringBuilder("{");
		for (int i = 0; i < randomIndices.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomIndices[i]);
		expectedToString.append("}");
		assertThat(bitSet.toString(), is(expectedToString.toString()));

		for (long randomIndex : randomIndices)
			assertThat(bitSet.clear(randomIndex), is(true));
		assertThat(bitSet.toString(), is("{}"));
		assertThat(bitSet.bitCount(), is(0L));

		for (long randomIndex : randomIndices)
			assertThat(bitSet.clear(randomIndex), is(false));
	}
}