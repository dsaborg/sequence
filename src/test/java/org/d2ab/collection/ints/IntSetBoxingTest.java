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

import static org.d2ab.collection.Arrayz.fill;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class IntSetBoxingTest extends BaseBoxingTest {
	private final Set<Integer> empty = IntSet.Base.create();
	private final Set<Integer> set = IntSet.Base.create(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);

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
		for (int x = -5; x <= 4; x++)
			assertThat(set.contains(x), is(true));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17), is(false));

		assertThat(set.remove(17), is(false));
		for (int x = -5; x <= 4; x++)
			assertThat(set.remove(x), is(true));
		assertThat(set.isEmpty(), is(true));
	}

	@Test
	public void toArray() {
		assertArrayEquals(new Integer[0], empty.toArray());
		assertArrayEquals(new Integer[]{-5, -4, -3, -2, -1, 0, 1, 2, 3, 4}, set.toArray());
	}

	@Test
	public void toArrayEmptyTarget() {
		Integer[] emptyTarget = new Integer[0];
		assertThat(empty.toArray(emptyTarget), is(sameInstance(emptyTarget)));
		assertArrayEquals(new Integer[]{-5, -4, -3, -2, -1, 0, 1, 2, 3, 4}, set.toArray(new Integer[0]));
	}

	@Test
	public void toArraySmallerTarget() {
		assertArrayEquals(new Integer[]{-5, -4, -3, -2, -1, 0, 1, 2, 3, 4}, set.toArray(new Integer[9]));
	}

	@Test
	public void toArrayBiggerTarget() {
		assertArrayEquals(new Integer[]{null, 17}, empty.toArray(fill(new Integer[2], 17)));
		assertArrayEquals(new Integer[]{-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, null, 17},
		                  set.toArray(fill(new Integer[12], 17)));
	}

	@Test
	public void equalsHashCodeAgainstTreeSet() {
		Set<Integer> larger = new TreeSet<>(Lists.of(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
		assertThat(set, is(not(equalTo(larger))));
		assertThat(set.hashCode(), is(not(larger.hashCode())));

		Set<Integer> smaller = new TreeSet<>(Lists.of(-5, -4, -3, -2, -1, 0, 1, 2, 3));
		assertThat(set, is(not(equalTo(smaller))));
		assertThat(set.hashCode(), is(not(smaller.hashCode())));

		Set<Integer> dissimilar = new TreeSet<>(Lists.of(-5, -4, -3, -2, -1, 0, 1, 2, 3, 5));
		assertThat(set, is(not(equalTo(dissimilar))));
		assertThat(set.hashCode(), is(not(dissimilar.hashCode())));

		Set<Integer> same = new TreeSet<>(Lists.of(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
		assertThat(set, is(equalTo(same)));
		assertThat(set.hashCode(), is(same.hashCode()));
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
		assertThat(empty.removeAll(IntList.create(-2, -1, 0, 1)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(IntList.create(-2, -1, 0, 1)), is(true));
		assertThat(set, contains(-5, -4, -3, 2, 3, 4));
	}

	@Test
	public void retainAllIntCollection() {
		assertThat(empty.retainAll(IntList.create(-2, -1, 0, 1)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(IntList.create(-2, -1, 0, 1)), is(true));
		assertThat(set, contains(-2, -1, 0, 1));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeIf(x -> x >= -3 && x < 3), is(true));
		assertThat(set, contains(-5, -4, 3, 4));
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
	public void boundaries() {
		assertThat(empty.add(Integer.MIN_VALUE), is(true));
		assertThat(empty.add(0), is(true));
		assertThat(empty.add(Integer.MAX_VALUE), is(true));

		assertThat(empty, contains(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));

		assertThat(empty.contains(Integer.MIN_VALUE), is(true));
		assertThat(empty.contains(0), is(true));
		assertThat(empty.contains(Integer.MAX_VALUE), is(true));

		assertThat(empty.remove(Integer.MIN_VALUE), is(true));
		assertThat(empty.remove(0), is(true));
		assertThat(empty.remove(Integer.MAX_VALUE), is(true));

		assertThat(empty, is(emptyIterable()));
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
