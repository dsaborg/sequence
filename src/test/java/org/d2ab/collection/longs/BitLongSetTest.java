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
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;

public class BitLongSetTest {
	private final BitLongSet empty = new BitLongSet();
	private final BitLongSet set = new BitLongSet(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);

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
	public void testEqualsHashCode() {
		BitLongSet set2 = new BitLongSet(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17);
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.removeLong(17);

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
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
	public void addBoxed() {
		empty.add(17L);
		assertThat(empty, containsLongs(17));

		set.add(17L);
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
	}

	@Test
	public void containsBoxed() {
		assertThat(empty.contains(17L), is(false));

		assertThat(set.contains(17L), is(false));
		assertThat(set.contains(new Object()), is(false));
		for (long x = -5; x <= 4; x++)
			assertThat(set.contains(x), is(true));
	}

	@Test
	public void removeBoxed() {
		assertThat(empty.remove(17), is(false));

		assertThat(set.remove(17), is(false));
		assertThat(set.remove(new Object()), is(false));
		for (long x = -5; x <= 4; x++)
			assertThat(set.remove(x), is(true));
		assertThat(set.isEmpty(), is(true));
	}

	@Test
	public void addAllBoxed() {
		assertThat(empty.addAll(Arrays.asList(1L, 2L, 3L)), is(true));
		assertThat(empty, containsLongs(1, 2, 3));

		assertThat(set.addAll(Arrays.asList(3L, 4L, 5L, 6L, 7L)), is(true));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void firstBoxed() {
		expecting(NoSuchElementException.class, empty::first);
		assertThat(set.first(), is(-5L));
	}

	@Test
	public void lastBoxed() {
		expecting(NoSuchElementException.class, empty::last);
		assertThat(set.last(), is(4L));
	}

	@Test
	public void removeAllBoxed() {
		assertThat(empty.removeAll(Arrays.asList(1L, 2L, 3L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(Arrays.asList(1L, 2L, 3L)), is(true));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void retainAllBoxed() {
		assertThat(empty.retainAll(Arrays.asList(1L, 2L, 3L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(Arrays.asList(1L, 2L, 3L)), is(true));
		assertThat(set, containsLongs(1, 2, 3));
	}

	@Test
	public void removeIfBoxed() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeIf(x -> x > 3), is(true));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3));
	}

	@Test
	public void containsLongCollection() {
		assertThat(empty.containsAll(Arrays.asList(1L, 2L, 3L)), is(false));
		assertThat(set.containsAll(Arrays.asList(1L, 2L, 3L)), is(true));
		assertThat(set.containsAll(Arrays.asList(1L, 2L, 3L, 17L)), is(false));
	}

	@Test
	public void forEachBoxed() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicLong value = new AtomicLong(-5);
		set.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(5L));
	}

	@Test
	public void boundaries() {
		BitLongSet intSet = new BitLongSet();
		assertThat(intSet.addLong(Long.MIN_VALUE), is(true));
		assertThat(intSet.addLong(0), is(true));
		assertThat(intSet.addLong(Long.MAX_VALUE), is(true));

		assertThat(intSet, containsLongs(Long.MIN_VALUE, 0, Long.MAX_VALUE));

		assertThat(intSet.containsLong(Long.MIN_VALUE), is(true));
		assertThat(intSet.containsLong(0), is(true));
		assertThat(intSet.containsLong(Long.MAX_VALUE), is(true));

		assertThat(intSet.removeLong(Long.MIN_VALUE), is(true));
		assertThat(intSet.removeLong(0), is(true));
		assertThat(intSet.removeLong(Long.MAX_VALUE), is(true));

		assertThat(intSet, is(emptyIterable()));
	}

	@Test
	public void fuzz() {
		long[] randomValues = new long[10000];
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