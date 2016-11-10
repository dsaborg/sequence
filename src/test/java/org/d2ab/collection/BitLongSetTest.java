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

import org.d2ab.iterator.longs.LongIterator;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;

public class BitLongSetTest {
	private final BitLongSet empty = new BitLongSet();
	private final BitLongSet longSet = new BitLongSet(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(longSet.size(), is(10));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(longSet, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void isEmpty() {
		assertThat(empty.isEmpty(), is(true));
		assertThat(longSet.isEmpty(), is(false));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty.isEmpty(), is(true));

		longSet.clear();
		assertThat(longSet.isEmpty(), is(true));
	}

	@Test
	public void addLong() {
		empty.addLong(17);
		assertThat(empty, containsLongs(17));

		longSet.addLong(17);
		assertThat(longSet, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
	}

	@Test
	public void containsLong() {
		assertThat(empty.containsLong(17), is(false));

		assertThat(longSet.containsLong(17), is(false));
		for (int i = -5; i <= 4; i++)
			assertThat(longSet.containsLong(i), is(true));
	}

	@Test
	public void removeLong() {
		assertThat(empty.removeLong(17), is(false));

		assertThat(longSet.removeLong(17), is(false));
		for (int i = -5; i <= 4; i++)
			assertThat(longSet.removeLong(i), is(true));
		assertThat(longSet.isEmpty(), is(true));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(longSet.toString(), is("[-5, -4, -3, -2, -1, 0, 1, 2, 3, 4]"));
	}

	@Test
	public void testEqualsHashCode() {
		BitLongSet intSet2 = new BitLongSet(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17);
		assertThat(longSet, is(not(equalTo(intSet2))));
		assertThat(longSet.hashCode(), is(CoreMatchers.not(intSet2.hashCode())));

		intSet2.removeLong(17);

		assertThat(longSet, is(equalTo(intSet2)));
		assertThat(longSet.hashCode(), CoreMatchers.is(intSet2.hashCode()));
	}

	@Test
	public void iteratorRemoveAll() {
		LongIterator iterator = longSet.iterator();
		long value = -5;
		while (iterator.hasNext()) {
			assertThat(iterator.nextLong(), is(value));
			iterator.remove();
			value++;
		}
		assertThat(value, is(5L));
		assertThat(longSet, is(emptyIterable()));
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
		assertThat(empty.containsAll(randomValues), is(true));

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