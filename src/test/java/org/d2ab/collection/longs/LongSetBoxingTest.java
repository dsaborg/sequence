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
import static org.d2ab.collection.Arrayz.fill;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class LongSetBoxingTest extends BaseBoxingTest {
	private final Set<Long> empty = LongSet.Base.create();
	private final Set<Long> set = LongSet.Base.create(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);

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
		for (long x = -5; x <= 4; x++)
			assertThat(set.contains(x), is(true));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17L), is(false));

		assertThat(set.remove(17L), is(false));
		for (long x = -5; x <= 4; x++)
			assertThat(set.remove(x), is(true));
		assertThat(set.isEmpty(), is(true));
	}

	@Test
	public void toArray() {
		assertArrayEquals(new Long[0], empty.toArray());
		assertArrayEquals(new Long[]{-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L}, set.toArray());
	}

	@Test
	public void toArrayEmptyTarget() {
		Long[] emptyTarget = new Long[0];
		assertThat(empty.toArray(emptyTarget), is(sameInstance(emptyTarget)));
		assertArrayEquals(new Long[]{-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L}, set.toArray(new Long[0]));
	}

	@Test
	public void toArraySmallerTarget() {
		assertArrayEquals(new Long[]{-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L}, set.toArray(new Long[9]));
	}

	@Test
	public void toArrayBiggerTarget() {
		assertArrayEquals(new Long[]{null, 17L}, empty.toArray(fill(new Long[2], 17L)));
		assertArrayEquals(new Long[]{-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L, null, 17L},
		                  set.toArray(fill(new Long[12], 17L)));
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
		assertThat(empty.removeAll(LongList.create(-2, -1, 0, 1)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(LongList.create(-2, -1, 0, 1)), is(true));
		assertThat(set, contains(-5L, -4L, -3L, 2L, 3L, 4L));
	}

	@Test
	public void retainAllLongCollection() {
		assertThat(empty.retainAll(LongList.create(-2, -1, 0, 1)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(LongList.create(-2, -1, 0, 1)), is(true));
		assertThat(set, contains(-2L, -1L, 0L, 1L));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeIf(x -> x >= -3 && x < 3), is(true));
		assertThat(set, contains(-5L, -4L, 3L, 4L));
	}

	@Test
	public void containsAllLongCollection() {
		assertThat(empty.containsAll(LongList.create(1, 2, 3)), is(false));
		assertThat(set.containsAll(LongList.create(1, 2, 3)), is(true));
		assertThat(set.containsAll(LongList.create(1, 2, 3, 17)), is(false));
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
	public void boundaries() {
		assertThat(empty.add(Long.MIN_VALUE), is(true));
		assertThat(empty.add(0L), is(true));
		assertThat(empty.add(Long.MAX_VALUE), is(true));

		assertThat(empty, contains(Long.MIN_VALUE, 0L, Long.MAX_VALUE));

		assertThat(empty.contains(Long.MIN_VALUE), is(true));
		assertThat(empty.contains(0L), is(true));
		assertThat(empty.contains(Long.MAX_VALUE), is(true));

		assertThat(empty.remove(Long.MIN_VALUE), is(true));
		assertThat(empty.remove(0L), is(true));
		assertThat(empty.remove(Long.MAX_VALUE), is(true));

		assertThat(empty, is(emptyIterable()));
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
