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
import org.d2ab.collection.Lists;
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class DoubleSortedSetBoxingTest extends BaseBoxingTest {
	private final SortedSet<Double> empty = DoubleSortedSet.Base.create();
	private final SortedSet<Double> set = DoubleSortedSet.Base.create(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);

	@Test
	public void create() {
		assertThat(DoubleSortedSet.create(), is(emptyIterable()));
		assertThat(DoubleSortedSet.create(-2, -1, 0, 1), contains(-2.0, -1.0, 0.0, 1.0));
	}

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(set.size(), is(10));
	}

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
	public void comparator() {
		assertThat(empty.comparator(), is(nullValue()));
		assertThat(set.comparator(), is(nullValue()));
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
		assertThat(set.contains(new Object()), is(false));

		for (double x = -5; x <= 4; x++)
			assertThat(set.contains(x), is(true));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17.0), is(false));

		assertThat(set.remove(17.0), is(false));
		assertThat(set.remove(new Object()), is(false));

		for (double x = -5; x <= 4; x++)
			assertThat(set.remove(x), is(true));
		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(set.toString(), is("[-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0]"));
	}

	@Test
	public void testEqualsHashCodeAgainstSet() {
		Set<Double> set2 = new HashSet<>(Lists.of(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 17.0));
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.remove(17.0);

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void testEqualsHashCodeAgainstDoubleSet() {
		SortedListDoubleSet set2 = new SortedListDoubleSet(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0,
		                                                   17.0);
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.remove(17.0);

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void subSet() {
		SortedSet<Double> subSet = set.subSet(-3.0, 3.0);
		assertThat(subSet, contains(-3.0, -2.0, -1.0, 0.0, 1.0, 2.0));
		assertThat(subSet.size(), is(6));
		assertThat(subSet.first(), is(-3.0));
		assertThat(subSet.last(), is(2.0));
		assertThat(subSet.contains(1.0), is(true));
		assertThat(subSet.contains(3.0), is(false));
		assertThat(subSet.toString(), is("[-3.0, -2.0, -1.0, 0.0, 1.0, 2.0]"));

		Set<Double> equivalentSet = new HashSet<>(Lists.of(-3.0, -2.0, -1.0, 0.0, 1.0, 2.0));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(subSet.remove(0.0), is(true));
		assertThat(subSet, contains(-3.0, -2.0, -1.0, 1.0, 2.0));
		assertThat(subSet.size(), is(5));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 1.0, 2.0, 3.0, 4.0));

		assertThat(subSet.remove(0.0), is(false));
		assertThat(subSet, contains(-3.0, -2.0, -1.0, 1.0, 2.0));
		assertThat(subSet.size(), is(5));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 1.0, 2.0, 3.0, 4.0));

		assertThat(subSet.add(0.0), is(true));
		assertThat(subSet, contains(-3.0, -2.0, -1.0, 0.0, 1.0, 2.0));
		assertThat(subSet.size(), is(6));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0));

		assertThat(subSet.add(0.0), is(false));
		assertThat(subSet, contains(-3.0, -2.0, -1.0, 0.0, 1.0, 2.0));
		assertThat(subSet.size(), is(6));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0));

		expecting(IllegalArgumentException.class, () -> subSet.add(-17.0));
		assertThat(subSet, contains(-3.0, -2.0, -1.0, 0.0, 1.0, 2.0));
		assertThat(subSet.size(), is(6));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0));

		subSet.clear();
		assertThat(subSet, is(emptyIterable()));
		assertThat(subSet.size(), is(0));
		assertThat(set, contains(-5.0, -4.0, 3.0, 4.0));
	}

	@Test
	public void sparseSubSet() {
		SortedSet<Double> subSet = new SortedListDoubleSet(-5, -3, -1, 1, 3, 5).subSet(-2.0, 2.0);
		assertThat(subSet, contains(-1.0, 1.0));
		assertThat(subSet.size(), is(2));
		assertThat(subSet.first(), is(-1.0));
		assertThat(subSet.last(), is(1.0));
		assertThat(subSet.contains(1.0), is(true));
		assertThat(subSet.contains(-3.0), is(false));
		assertThat(subSet.toString(), is("[-1.0, 1.0]"));

		Set<Double> equivalentSet = new HashSet<>(Lists.of(-1.0, 1.0));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void headSet() {
		SortedSet<Double> headSet = set.headSet(0.0);
		assertThat(headSet, contains(-5.0, -4.0, -3.0, -2.0, -1.0));
		assertThat(headSet.size(), is(5));
		assertThat(headSet.first(), is(-5.0));
		assertThat(headSet.last(), is(-1.0));
		assertThat(headSet.contains(-3.0), is(true));
		assertThat(headSet.contains(0.0), is(false));
		assertThat(headSet.toString(), is("[-5.0, -4.0, -3.0, -2.0, -1.0]"));

		Set<Double> equivalentSet = new HashSet<>(Lists.of(-5.0, -4.0, -3.0, -2.0, -1.0));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(headSet.remove(-3.0), is(true));
		assertThat(headSet, contains(-5.0, -4.0, -2.0, -1.0));
		assertThat(headSet.size(), is(4));
		assertThat(set, contains(-5.0, -4.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0));

		assertThat(headSet.remove(-3.0), is(false));
		assertThat(headSet, contains(-5.0, -4.0, -2.0, -1.0));
		assertThat(headSet.size(), is(4));
		assertThat(set, contains(-5.0, -4.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0));

		assertThat(headSet.add(-17.0), is(true));
		assertThat(headSet, contains(-17.0, -5.0, -4.0, -2.0, -1.0));
		assertThat(headSet.size(), is(5));
		assertThat(set, contains(-17.0, -5.0, -4.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0));

		assertThat(headSet.add(-17.0), is(false));
		assertThat(headSet, contains(-17.0, -5.0, -4.0, -2.0, -1.0));
		assertThat(headSet.size(), is(5));
		assertThat(set, contains(-17.0, -5.0, -4.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0));

		expecting(IllegalArgumentException.class, () -> headSet.add(17.0));
		assertThat(headSet, contains(-17.0, -5.0, -4.0, -2.0, -1.0));
		assertThat(headSet.size(), is(5));
		assertThat(set, contains(-17.0, -5.0, -4.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0));

		assertThat(set.add(-6.0), is(true));
		assertThat(headSet, contains(-17.0, -6.0, -5.0, -4.0, -2.0, -1.0));
		assertThat(headSet.size(), is(6));
		assertThat(set, contains(-17.0, -6.0, -5.0, -4.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0));

		headSet.clear();
		assertThat(headSet, is(emptyIterable()));
		assertThat(headSet.size(), is(0));
		assertThat(set, contains(0.0, 1.0, 2.0, 3.0, 4.0));
	}

	@Test
	public void sparseHeadSet() {
		SortedSet<Double> headSet = new SortedListDoubleSet(-5, -3, -1, 1, 3, 5).headSet(0.0);
		assertThat(headSet, contains(-5.0, -3.0, -1.0));
		assertThat(headSet.size(), is(3));
		assertThat(headSet.first(), is(-5.0));
		assertThat(headSet.last(), is(-1.0));
		assertThat(headSet.contains(-3.0), is(true));
		assertThat(headSet.contains(1.0), is(false));
		assertThat(headSet.toString(), is("[-5.0, -3.0, -1.0]"));

		Set<Double> equivalentSet = new HashSet<>(Lists.of(-5.0, -3.0, -1.0));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void tailSet() {
		SortedSet<Double> tailSet = set.tailSet(0.0);
		assertThat(tailSet, contains(0.0, 1.0, 2.0, 3.0, 4.0));
		assertThat(tailSet.size(), is(5));
		assertThat(tailSet.first(), is(0.0));
		assertThat(tailSet.last(), is(4.0));
		assertThat(tailSet.contains(3.0), is(true));
		assertThat(tailSet.contains(-1.0), is(false));
		assertThat(tailSet.toString(), is("[0.0, 1.0, 2.0, 3.0, 4.0]"));

		Set<Double> equivalentSet = new HashSet<>(Lists.of(0.0, 1.0, 2.0, 3.0, 4.0));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(tailSet.remove(2.0), is(true));
		assertThat(tailSet, contains(0.0, 1.0, 3.0, 4.0));
		assertThat(tailSet.size(), is(4));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 3.0, 4.0));

		assertThat(tailSet.remove(2.0), is(false));
		assertThat(tailSet, contains(0.0, 1.0, 3.0, 4.0));
		assertThat(tailSet.size(), is(4));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 3.0, 4.0));

		assertThat(tailSet.add(17.0), is(true));
		assertThat(tailSet, contains(0.0, 1.0, 3.0, 4.0, 17.0));
		assertThat(tailSet.size(), is(5));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 3.0, 4.0, 17.0));

		assertThat(tailSet.add(17.0), is(false));
		assertThat(tailSet, contains(0.0, 1.0, 3.0, 4.0, 17.0));
		assertThat(tailSet.size(), is(5));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 3.0, 4.0, 17.0));

		expecting(IllegalArgumentException.class, () -> tailSet.add(-17.0));
		assertThat(tailSet, contains(0.0, 1.0, 3.0, 4.0, 17.0));
		assertThat(tailSet.size(), is(5));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 3.0, 4.0, 17.0));

		assertThat(set.add(5.0), is(true));
		assertThat(tailSet, contains(0.0, 1.0, 3.0, 4.0, 5.0, 17.0));
		assertThat(tailSet.size(), is(6));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 3.0, 4.0, 5.0, 17.0));

		tailSet.clear();
		assertThat(tailSet, is(emptyIterable()));
		assertThat(tailSet.size(), is(0));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0));
	}

	@Test
	public void sparseTailSet() {
		SortedSet<Double> tailSet = new SortedListDoubleSet(-5, -3, -1, 1, 3, 5).tailSet(0.0);
		assertThat(tailSet, contains(1.0, 3.0, 5.0));
		assertThat(tailSet.size(), is(3));
		assertThat(tailSet.first(), is(1.0));
		assertThat(tailSet.last(), is(5.0));
		assertThat(tailSet.contains(3.0), is(true));
		assertThat(tailSet.contains(-1.0), is(false));
		assertThat(tailSet.toString(), is("[1.0, 3.0, 5.0]"));

		Set<Double> equivalentSet = new HashSet<>(Lists.of(1.0, 3.0, 5.0));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));
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
	public void first() {
		expecting(NoSuchElementException.class, empty::first);
		assertThat(set.first(), is(-5.0));
	}

	@Test
	public void last() {
		expecting(NoSuchElementException.class, empty::last);
		assertThat(set.last(), is(4.0));
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
		assertThat(empty.removeAll(DoubleList.create(1.0, 2.0, 3.0)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(DoubleList.create(1.0, 2.0, 3.0)), is(true));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 4.0));
	}

	@Test
	public void retainAllDoubleCollection() {
		assertThat(empty.retainAll(DoubleList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(DoubleList.create(1, 2, 3)), is(true));
		assertThat(set, contains(1.0, 2.0, 3.0));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeIf(x -> x > 3), is(true));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0));
	}

	@Test
	public void containsAllDoubleCollection() {
		assertThat(empty.containsAll(DoubleList.create(1, 2, 3)), is(false));
		assertThat(set.containsAll(DoubleList.create(1, 2, 3)), is(true));
		assertThat(set.containsAll(DoubleList.create(1, 2, 3, 17.0)), is(false));
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
	public void addAll() {
		assertThat(empty.addAll(Lists.of(1.0, 2.0, 3.0)), is(true));
		assertThat(empty, contains(1.0, 2.0, 3.0));

		assertThat(set.addAll(Lists.of(3.0, 4.0, 5.0, 6.0, 7.0)), is(true));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(Lists.of(1.0, 2.0, 3.0)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(Lists.of(1.0, 2.0, 3.0)), is(true));
		assertThat(set, contains(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 4.0));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(Lists.of(1.0, 2.0, 3.0)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(Lists.of(1.0, 2.0, 3.0)), is(true));
		assertThat(set, contains(1.0, 2.0, 3.0));
	}

	@Test
	public void containsAllCollection() {
		assertThat(empty.containsAll(Lists.of(1.0, 2.0, 3.0)), is(false));
		assertThat(set.containsAll(Lists.of(1.0, 2.0, 3.0)), is(true));
		assertThat(set.containsAll(Lists.of(1.0, 2.0, 3.0, 17.0)), is(false));
	}

	@Test
	public void boundaries() {
		SortedListDoubleSet set = new SortedListDoubleSet();
		assertThat(set.add(Double.MIN_VALUE), is(true));
		assertThat(set.add(0.0), is(true));
		assertThat(set.add(Double.MAX_VALUE), is(true));

		assertThat(set, contains(0.0, Double.MIN_VALUE, Double.MAX_VALUE));

		assertThat(set.contains(Double.MIN_VALUE), is(true));
		assertThat(set.contains(0.0), is(true));
		assertThat(set.contains(Double.MAX_VALUE), is(true));

		assertThat(set.remove(Double.MIN_VALUE), is(true));
		assertThat(set.remove(0.0), is(true));
		assertThat(set.remove(Double.MAX_VALUE), is(true));

		assertThat(set, is(emptyIterable()));
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
		assertThat(empty.containsAll(Lists.of(randomValues)), is(true));

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
