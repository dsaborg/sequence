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

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.hamcrest.CoreMatchers.*;
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
		assertThat(intSet.hashCode(), is(not(intSet2.hashCode())));

		intSet2.removeInt(17);

		assertThat(intSet, is(equalTo(intSet2)));
		assertThat(intSet.hashCode(), is(intSet2.hashCode()));
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