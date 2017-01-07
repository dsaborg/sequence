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
import org.d2ab.collection.chars.CharSet;
import org.d2ab.iterator.ints.IntIterator;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class IntSetTest {
	private final IntSet empty = IntSet.Base.create();
	private final IntSet set = IntSet.Base.create(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(set.size(), is(10));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void iteratorFailFast() {
		IntIterator it1 = set.iterator();
		set.addInt(17);
		expecting(ConcurrentModificationException.class, it1::nextInt);

		IntIterator it2 = set.iterator();
		set.removeInt(17);
		expecting(ConcurrentModificationException.class, it2::nextInt);
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
	public void addInt() {
		empty.addInt(17);
		assertThat(empty, containsInts(17));

		set.addInt(17);
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
	}

	@Test
	public void containsInt() {
		assertThat(empty.containsInt(17), is(false));

		assertThat(set.containsInt(17), is(false));
		for (int x = -5; x <= 4; x++)
			assertThat(set.containsInt(x), is(true));
	}

	@Test
	public void removeInt() {
		assertThat(empty.removeInt(17), is(false));

		assertThat(set.removeInt(17), is(false));
		for (int x = -5; x <= 4; x++)
			assertThat(set.removeInt(x), is(true));
		assertThat(set.isEmpty(), is(true));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(set.toString(), is("[-5, -4, -3, -2, -1, 0, 1, 2, 3, 4]"));
	}

	@Test
	public void equalsEdgeCases() {
		assertThat(set, is(equalTo(set)));
		assertThat(set, is(not(equalTo(null))));
		assertThat(set, is(not(equalTo(new Object()))));
	}

	@Test
	public void equalsHashCodeAgainstSet() {
		Set<Integer> set2 = new HashSet<>(asList(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.remove(17);

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void equalsHashCodeAgainstIntSet() {
		IntSet set2 = IntSet.Base.create(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17);
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.removeInt(17);

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void addAllIntArray() {
		assertThat(empty.addAllInts(1, 2, 3), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(set.addAllInts(3, 4, 5, 6, 7), is(true));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void addAllIntCollection() {
		assertThat(empty.addAllInts(IntList.create(1, 2, 3)), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(set.addAllInts(IntList.create(3, 4, 5, 6, 7)), is(true));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(set.stream().collect(Collectors.toList()), contains(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(set.parallelStream().collect(Collectors.toList()), contains(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void intStream() {
		assertThat(empty.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));

		assertThat(set.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void parallelIntStream() {
		assertThat(empty.parallelIntStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));

		assertThat(set.parallelIntStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(set.sequence(), containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void iteratorRemoveAll() {
		IntIterator iterator = set.iterator();
		int value = -5;
		while (iterator.hasNext()) {
			assertThat(iterator.nextInt(), is(value));
			iterator.remove();
			value++;
		}
		assertThat(value, is(5));
		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void removeAllIntArray() {
		assertThat(empty.removeAllInts(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAllInts(1, 2, 3), is(true));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void removeAllIntCollection() {
		assertThat(empty.removeAll(IntList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(IntList.create(1, 2, 3)), is(true));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void retainAllIntArray() {
		assertThat(empty.retainAllInts(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAllInts(1, 2, 3), is(true));
		assertThat(set, containsInts(1, 2, 3));
	}

	@Test
	public void retainAllIntCollection() {
		assertThat(empty.retainAll(IntList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(IntList.create(1, 2, 3)), is(true));
		assertThat(set, containsInts(1, 2, 3));
	}

	@Test
	public void removeIntsIf() {
		assertThat(empty.removeIntsIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeIntsIf(x -> x > 3), is(true));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3));
	}

	@Test
	public void containsAllIntArray() {
		assertThat(empty.containsAllInts(1, 2, 3), is(false));
		assertThat(set.containsAllInts(1, 2, 3), is(true));
		assertThat(set.containsAllInts(1, 2, 3, 17), is(false));
	}

	@Test
	public void containsAllIntCollection() {
		assertThat(empty.containsAll(IntList.create(1, 2, 3)), is(false));
		assertThat(set.containsAll(IntList.create(1, 2, 3)), is(true));
		assertThat(set.containsAll(IntList.create(1, 2, 3, 17)), is(false));
	}

	@Test
	public void forEachInt() {
		empty.forEachInt(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(-5);
		set.forEachInt(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(5));
	}

	@Test
	public void containsIntCollection() {
		assertThat(empty.containsAll(asList(1, 2, 3)), is(false));
		assertThat(set.containsAll(asList(1, 2, 3)), is(true));
		assertThat(set.containsAll(asList(1, 2, 3, 17)), is(false));
	}

	@Test
	public void asChars() {
		CharSet emptyAsChars = empty.asChars();
		twice(() -> assertThat(emptyAsChars, is(emptyIterable())));
		assertThat(emptyAsChars.size(), is(0));

		CharSet intSetAsChars = IntSet.Base.create('a', 'b', 'c', 'd', 'e').asChars();
		twice(() -> assertThat(intSetAsChars, containsChars('a', 'b', 'c', 'd', 'e')));
		assertThat(intSetAsChars.size(), is(5));
	}

	@Test
	public void boundaries() {
		assertThat(empty.addInt(Integer.MIN_VALUE), is(true));
		assertThat(empty.addInt(0), is(true));
		assertThat(empty.addInt(Integer.MAX_VALUE), is(true));

		assertThat(empty, containsInts(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));

		assertThat(empty.containsInt(Integer.MIN_VALUE), is(true));
		assertThat(empty.containsInt(0), is(true));
		assertThat(empty.containsInt(Integer.MAX_VALUE), is(true));

		assertThat(empty.removeInt(Integer.MIN_VALUE), is(true));
		assertThat(empty.removeInt(0), is(true));
		assertThat(empty.removeInt(Integer.MAX_VALUE), is(true));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void fuzz() {
		int[] randomValues = new int[1000];
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
		assertThat(empty.containsAllInts(randomValues), is(true));

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
