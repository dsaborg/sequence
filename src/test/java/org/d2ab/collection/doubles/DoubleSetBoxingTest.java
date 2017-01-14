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

package org.d2ab.collection.doubles;

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

public class DoubleSetBoxingTest extends BaseBoxingTest {
	private final Set<Double> empty = DoubleSet.Base.create();
	private final Set<Double> set = DoubleSet.Base.create(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0);

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0));
	}

	@Test
	public void iteratorFailFastPositives() {
		Iterator<Double> it1 = set.iterator();
		assertThat(it1.hasNext(), is(true));
		assertThat(set.add(17.0), is(true));
		expecting(ConcurrentModificationException.class, it1::next);

		Iterator<Double> it2 = set.iterator();
		assertThat(it2.hasNext(), is(true));
		assertThat(set.remove(17.0), is(true));
		expecting(ConcurrentModificationException.class, it2::next);
	}

	@Test
	public void iteratorFailFastNegatives() {
		Iterator<Double> it1 = set.iterator();
		assertThat(it1.hasNext(), is(true));
		assertThat(set.add(-17.0), is(true));
		expecting(ConcurrentModificationException.class, it1::next);

		Iterator<Double> it2 = set.iterator();
		assertThat(it2.hasNext(), is(true));
		assertThat(set.remove(-17.0), is(true));
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
		empty.add(17.0);
		assertThat(empty, contains(17.0));

		set.add(17.0);
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 17.0));
	}

	@Test
	public void boxedContains() {
		assertThat(empty.contains(17.0), is(false));

		assertThat(set.contains(17.0), is(false));
		for (double x = -5; x <= 4; x++)
			assertThat(set.contains(x), is(true));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17.0), is(false));

		assertThat(set.remove(17.0), is(false));
		for (double x = -5; x <= 4; x++)
			assertThat(set.remove(x), is(true));
		assertThat(set.isEmpty(), is(true));
	}

	@Test
	public void toArray() {
		assertArrayEquals(new Double[0], empty.toArray());
		assertArrayEquals(new Double[]{-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0}, set.toArray());
	}

	@Test
	public void toArrayEmptyTarget() {
		Double[] emptyTarget = new Double[0];
		assertThat(empty.toArray(emptyTarget), is(sameInstance(emptyTarget)));
		assertArrayEquals(new Double[]{-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0},
		                  set.toArray(new Double[0]));
	}

	@Test
	public void toArraySmallerTarget() {
		assertArrayEquals(new Double[]{-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0},
		                  set.toArray(new Double[9]));
	}

	@Test
	public void toArrayBiggerTarget() {
		assertArrayEquals(new Double[]{null, 17.0}, empty.toArray(fill(new Double[2], 17.0)));
		assertArrayEquals(new Double[]{-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, null, 17.0},
		                  set.toArray(fill(new Double[12], 17.0)));
	}

	@Test
	public void equalsHashCodeAgainstTreeSet() {
		Set<Double> larger = new TreeSet<>(asList(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 17.0));
		assertThat(set, is(not(equalTo(larger))));
		assertThat(set.hashCode(), is(not(larger.hashCode())));

		Set<Double> smaller = new TreeSet<>(asList(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0));
		assertThat(set, is(not(equalTo(smaller))));
		assertThat(set.hashCode(), is(not(smaller.hashCode())));

		Set<Double> dissimilar = new TreeSet<>(asList(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 5.0));
		assertThat(set, is(not(equalTo(dissimilar))));
		assertThat(set.hashCode(), is(not(dissimilar.hashCode())));

		Set<Double> same = new TreeSet<>(asList(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0));
		assertThat(set, is(equalTo(same)));
		assertThat(set.hashCode(), is(same.hashCode()));
	}

	@Test
	public void addAllDoubleCollection() {
		assertThat(empty.addAll(DoubleList.create(1, 2, 3)), is(true));
		assertThat(empty, contains(1.0, 2.0, 3.0));

		assertThat(set.addAll(DoubleList.create(3, 4, 5, 6, 7)), is(true));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(set.stream().collect(Collectors.toList()),
		           contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(set.parallelStream().collect(Collectors.toList()),
		           contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0));
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Double> iterator = set.iterator();
		double value = -5;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is(value));
			iterator.remove();
			value++;
		}
		assertThat(value, is(5.0));
		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void removeAllDoubleCollection() {
		assertThat(empty.removeAll(DoubleList.create(-2, -1, 0, 1)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(DoubleList.create(-2, -1, 0, 1)), is(true));
		assertThat(set, contains(-5.0, -4.0, -3.0, 2.0, 3.0, 4.0));
	}

	@Test
	public void retainAllDoubleCollection() {
		assertThat(empty.retainAll(DoubleList.create(-2, -1, 0, 1)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(DoubleList.create(-2, -1, 0, 1)), is(true));
		assertThat(set, contains(-2.0, -1.0, 0.0, 1.0));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeIf(x -> x >= -3 && x < 3), is(true));
		assertThat(set, contains(-5.0, -4.0, 3.0, 4.0));
	}

	@Test
	public void containsAllDoubleCollection() {
		assertThat(empty.containsAll(DoubleList.create(1, 2, 3)), is(false));
		assertThat(set.containsAll(DoubleList.create(1, 2, 3)), is(true));
		assertThat(set.containsAll(DoubleList.create(1, 2, 3, 17)), is(false));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicLong value = new AtomicLong(-5);
		set.forEach(x -> assertThat(x, is((double) value.getAndIncrement())));
		assertThat(value.get(), is(5L));
	}

	@Test
	public void boundaries() {
		assertThat(empty.add(Double.MIN_VALUE), is(true));
		assertThat(empty.add(0.0), is(true));
		assertThat(empty.add(Double.MAX_VALUE), is(true));

		assertThat(empty, contains(0.0, Double.MIN_VALUE, Double.MAX_VALUE));

		assertThat(empty.contains(Double.MIN_VALUE), is(true));
		assertThat(empty.contains(0.0), is(true));
		assertThat(empty.contains(Double.MAX_VALUE), is(true));

		assertThat(empty.remove(Double.MIN_VALUE), is(true));
		assertThat(empty.remove(0.0), is(true));
		assertThat(empty.remove(Double.MAX_VALUE), is(true));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void fuzz() {
		Double[] randomValues = new Double[1000];
		Random random = new Random();
		for (int i = 0; i < randomValues.length; i++) {
			double randomValue;
			do
				randomValue = random.nextDouble();
			while (Arrayz.contains(randomValues, randomValue));
			randomValues[i] = randomValue;
		}

		// Adding
		for (double randomValue : randomValues)
			assertThat(empty.add(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		for (double randomValue : randomValues)
			assertThat(empty.add(randomValue), is(false));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAll(asList(randomValues)), is(true));

		for (double randomValue : randomValues)
			assertThat(empty.contains(randomValue), is(true));

		// toString
		Arrays.sort(randomValues);
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (double randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty.size(), is(0));

		for (double randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(false));
	}
}
