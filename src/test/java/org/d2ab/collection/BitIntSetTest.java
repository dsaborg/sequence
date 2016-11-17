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

import org.d2ab.iterator.ints.IntIterator;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;

public class BitIntSetTest {
	private final BitIntSet empty = new BitIntSet();
	private final BitIntSet intSet = new BitIntSet(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(intSet.size(), is(10));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(intSet, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void isEmpty() {
		assertThat(empty.isEmpty(), is(true));
		assertThat(intSet.isEmpty(), is(false));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty.isEmpty(), is(true));

		intSet.clear();
		assertThat(intSet.isEmpty(), is(true));
	}

	@Test
	public void addInt() {
		empty.addInt(17);
		assertThat(empty, containsInts(17));

		intSet.addInt(17);
		assertThat(intSet, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
	}

	@Test
	public void containsInt() {
		assertThat(empty.containsInt(17), is(false));

		assertThat(intSet.containsInt(17), is(false));
		for (int i = -5; i <= 4; i++)
			assertThat(intSet.containsInt(i), is(true));
	}

	@Test
	public void removeInt() {
		assertThat(empty.removeInt(17), is(false));

		assertThat(intSet.removeInt(17), is(false));
		for (int i = -5; i <= 4; i++)
			assertThat(intSet.removeInt(i), is(true));
		assertThat(intSet.isEmpty(), is(true));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(intSet.toString(), is("[-5, -4, -3, -2, -1, 0, 1, 2, 3, 4]"));
	}

	@Test
	public void testEqualsHashCode() {
		BitIntSet intSet2 = new BitIntSet(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17);
		assertThat(intSet, is(not(equalTo(intSet2))));
		assertThat(intSet.hashCode(), is(CoreMatchers.not(intSet2.hashCode())));

		intSet2.removeInt(17);

		assertThat(intSet, is(equalTo(intSet2)));
		assertThat(intSet.hashCode(), CoreMatchers.is(intSet2.hashCode()));
	}

	@Test
	public void addAllIntArray() {
		assertThat(empty.addAll(1, 2, 3), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(intSet.addAll(3, 4, 5, 6, 7), is(true));
		assertThat(intSet, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void addAllIntCollection() {
		assertThat(empty.addAll(IntList.of(1, 2, 3)), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(intSet.addAll(IntList.of(3, 4, 5, 6, 7)), is(true));
		assertThat(intSet, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(intSet.stream().collect(Collectors.toList()), contains(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void lastInt() {
		expecting(NoSuchElementException.class, empty::firstInt);
		assertThat(intSet.firstInt(), is(-5));
	}

	@Test
	public void firstInt() {
		expecting(NoSuchElementException.class, empty::lastInt);
		assertThat(intSet.lastInt(), is(4));
	}

	@Test
	public void iteratorRemoveAll() {
		IntIterator iterator = intSet.iterator();
		int value = -5;
		while (iterator.hasNext()) {
			assertThat(iterator.nextInt(), is(value));
			iterator.remove();
			value++;
		}
		assertThat(value, is(5));
		assertThat(intSet, is(emptyIterable()));
	}

	@Test
	public void removeAllIntArray() {
		assertThat(empty.removeAll(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(intSet.removeAll(1, 2, 3), is(true));
		assertThat(intSet, containsInts(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void removeAllIntCollection() {
		assertThat(empty.removeAll(IntList.of(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(intSet.removeAll(IntList.of(1, 2, 3)), is(true));
		assertThat(intSet, containsInts(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void retainAllIntArray() {
		assertThat(empty.retainAll(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(intSet.retainAll(1, 2, 3), is(true));
		assertThat(intSet, containsInts(1, 2, 3));
	}

	@Test
	public void retainAllIntCollection() {
		assertThat(empty.retainAll(IntList.of(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(intSet.retainAll(IntList.of(1, 2, 3)), is(true));
		assertThat(intSet, containsInts(1, 2, 3));
	}

	@Test
	public void removeIntsIf() {
		assertThat(empty.removeIntsIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(intSet.removeIntsIf(x -> x > 3), is(true));
		assertThat(intSet, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3));
	}

	@Test
	public void containsAllIntArray() {
		assertThat(empty.containsAll(1, 2, 3), is(false));
		assertThat(intSet.containsAll(1, 2, 3), is(true));
		assertThat(intSet.containsAll(1, 2, 3, 17), is(false));
	}

	@Test
	public void containsAllIntCollection() {
		assertThat(empty.containsAll(IntList.of(1, 2, 3)), is(false));
		assertThat(intSet.containsAll(IntList.of(1, 2, 3)), is(true));
		assertThat(intSet.containsAll(IntList.of(1, 2, 3, 17)), is(false));
	}

	@Test
	public void forEachInt() {
		empty.forEachInt(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(-5);
		intSet.forEachInt(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(5));
	}

	@Test
	public void add() {
		empty.add(17);
		assertThat(empty, containsInts(17));

		intSet.add(17);
		assertThat(intSet, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
	}

	@Test
	public void containsObject() {
		assertThat(empty.contains(17), is(false));

		assertThat(intSet.contains(17), is(false));
		assertThat(intSet.contains(new Object()), is(false));
		for (int i = -5; i <= 4; i++)
			assertThat(intSet.contains(i), is(true));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17), is(false));

		assertThat(intSet.remove(17), is(false));
		assertThat(intSet.remove(new Object()), is(false));
		for (int i = -5; i <= 4; i++)
			assertThat(intSet.remove(i), is(true));
		assertThat(intSet.isEmpty(), is(true));
	}

	@Test
	public void addAllCollection() {
		assertThat(empty.addAll(Arrays.asList(1, 2, 3)), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(intSet.addAll(Arrays.asList(3, 4, 5, 6, 7)), is(true));
		assertThat(intSet, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void last() {
		expecting(NoSuchElementException.class, empty::first);
		assertThat(intSet.first(), is(-5));
	}

	@Test
	public void first() {
		expecting(NoSuchElementException.class, empty::last);
		assertThat(intSet.last(), is(4));
	}

	@Test
	public void removeAllCollection() {
		assertThat(empty.removeAll(Arrays.asList(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(intSet.removeAll(Arrays.asList(1, 2, 3)), is(true));
		assertThat(intSet, containsInts(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void retainAllCollection() {
		assertThat(empty.retainAll(Arrays.asList(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(intSet.retainAll(Arrays.asList(1, 2, 3)), is(true));
		assertThat(intSet, containsInts(1, 2, 3));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(intSet.removeIf(x -> x > 3), is(true));
		assertThat(intSet, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3));
	}

	@Test
	public void containsIntCollection() {
		assertThat(empty.containsAll(Arrays.asList(1, 2, 3)), is(false));
		assertThat(intSet.containsAll(Arrays.asList(1, 2, 3)), is(true));
		assertThat(intSet.containsAll(Arrays.asList(1, 2, 3, 17)), is(false));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(-5);
		intSet.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(5));
	}

	@Test
	public void boundaries() {
		BitIntSet intSet = new BitIntSet();
		assertThat(intSet.addInt(Integer.MIN_VALUE), is(true));
		assertThat(intSet.addInt(0), is(true));
		assertThat(intSet.addInt(Integer.MAX_VALUE), is(true));

		assertThat(intSet, containsInts(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));

		assertThat(intSet.containsInt(Integer.MIN_VALUE), is(true));
		assertThat(intSet.containsInt(0), is(true));
		assertThat(intSet.containsInt(Integer.MAX_VALUE), is(true));

		assertThat(intSet.removeInt(Integer.MIN_VALUE), is(true));
		assertThat(intSet.removeInt(0), is(true));
		assertThat(intSet.removeInt(Integer.MAX_VALUE), is(true));

		assertThat(intSet, is(emptyIterable()));
	}

	@Test
	public void fuzz() {
		int[] randomValues = new int[10000];
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
			assertThat(empty.addInt(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		for (int randomValue : randomValues)
			assertThat(empty.addInt(randomValue), is(false));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAll(randomValues), is(true));

		for (int randomValue : randomValues)
			assertThat(empty.containsInt(randomValue), is(true));

		// toString
		Arrays.sort(randomValues);
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (int randomValue : randomValues)
			assertThat(empty.removeInt(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty.size(), is(0));

		for (int randomValue : randomValues)
			assertThat(empty.removeInt(randomValue), is(false));
	}
}
