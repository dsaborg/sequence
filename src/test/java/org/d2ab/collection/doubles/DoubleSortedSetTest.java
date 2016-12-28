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
import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.test.StrictDoubleIterator;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;

public class DoubleSortedSetTest {
	private final DoubleSortedSet backingEmpty = new SortedListDoubleSet();
	private final DoubleSortedSet empty = new DoubleSortedSet.Base() {
		@Override
		public DoubleIterator iterator() {
			return StrictDoubleIterator.from(backingEmpty.iterator());
		}

		@Override
		public int size() {
			return backingEmpty.size();
		}

		@Override
		public boolean addDoubleExactly(double x) {
			return backingEmpty.addDoubleExactly(x);
		}
	};

	private final DoubleSortedSet backing = new SortedListDoubleSet(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);
	private final DoubleSortedSet set = new DoubleSortedSet.Base() {
		@Override
		public DoubleIterator iterator() {
			return StrictDoubleIterator.from(backing.iterator());
		}

		@Override
		public int size() {
			return backing.size();
		}

		@Override
		public boolean addDoubleExactly(double x) {
			return backing.addDoubleExactly(x);
		}
	};

	@Test
	public void create() {
		assertThat(DoubleSortedSet.create(), is(emptyIterable()));
		assertThat(DoubleSortedSet.create(-2, -1, 0, 1), containsDoubles(-2, -1, 0, 1));
	}

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(set.size(), is(10));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void iteratorFailFast() {
		DoubleIterator it1 = set.iterator();
		set.addDoubleExactly(17);
		expecting(ConcurrentModificationException.class, it1::nextDouble);

		DoubleIterator it2 = set.iterator();
		set.removeDoubleExactly(17);
		expecting(ConcurrentModificationException.class, it2::nextDouble);
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
	public void addDoubleExactly() {
		assertThat(empty.addDoubleExactly(17), is(true));
		assertThat(empty, containsDoubles(17));

		assertThat(empty.addDoubleExactly(17), is(false));
		assertThat(empty, containsDoubles(17));

		assertThat(set.addDoubleExactly(17), is(true));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));

		assertThat(set.addDoubleExactly(17), is(false));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
	}

	@Test
	public void addDouble() {
		assertThat(empty.addDouble(17, 0.5), is(true));
		assertThat(empty, containsDoubles(17));

		assertThat(empty.addDouble(17.1, 0.5), is(false));
		assertThat(empty, containsDoubles(17));

		assertThat(set.addDouble(17, 0.5), is(true));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));

		assertThat(set.addDouble(17.1, 0.5), is(false));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
	}

	@Test
	public void containsDoubleExactly() {
		assertThat(empty.containsDoubleExactly(17), is(false));

		assertThat(set.containsDoubleExactly(17), is(false));
		for (double x = -5; x <= 4; x++)
			assertThat(set.containsDoubleExactly(x), is(true));
	}

	@Test
	public void removeDoubleExactly() {
		assertThat(empty.removeDoubleExactly(17), is(false));

		assertThat(set.removeDoubleExactly(17), is(false));
		for (double x = -5; x <= 4; x++)
			assertThat(set.removeDoubleExactly(x), is(true));
		assertThat(set.isEmpty(), is(true));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(set.toString(), is("[-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0]"));
	}

	@Test
	public void testEqualsHashCodeAgainstSet() {
		Set<Double> set2 = new HashSet<>(Arrays.asList(-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 17.0));
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.remove(17.0);

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void testEqualsHashCodeAgainstDoubleSet() {
		DoubleSet set2 = new SortedListDoubleSet(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17);
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.removeDoubleExactly(17);

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void subSetExactly() {
		DoubleSortedSet subSet = set.subSetExactly(-3.0, 3.0);
		assertThat(subSet, containsDoubles(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(subSet.firstDouble(), is(-3.0));
		assertThat(subSet.lastDouble(), is(2.0));
		assertThat(subSet.containsDoubleExactly(1), is(true));
		assertThat(subSet.containsDoubleExactly(3), is(false));
		assertThat(subSet.toString(), is("[-3.0, -2.0, -1.0, 0.0, 1.0, 2.0]"));

		Set<Double> equivalentSet = new HashSet<>(asList(-3.0, -2.0, -1.0, 0.0, 1.0, 2.0));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(subSet.removeDoubleExactly(0), is(true));
		assertThat(subSet, containsDoubles(-3, -2, -1, 1, 2));
		assertThat(subSet.size(), is(5));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 1, 2, 3, 4));

		assertThat(subSet.removeDoubleExactly(0), is(false));
		assertThat(subSet, containsDoubles(-3, -2, -1, 1, 2));
		assertThat(subSet.size(), is(5));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 1, 2, 3, 4));

		assertThat(subSet.addDoubleExactly(0), is(true));
		assertThat(subSet, containsDoubles(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));

		assertThat(subSet.addDoubleExactly(0), is(false));
		assertThat(subSet, containsDoubles(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));

		expecting(IllegalArgumentException.class, () -> subSet.addDoubleExactly(-17));
		assertThat(subSet, containsDoubles(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));

		subSet.clear();
		assertThat(subSet, is(emptyIterable()));
		assertThat(subSet.size(), is(0));
		assertThat(set, containsDoubles(-5, -4, 3, 4));
	}

	@Test
	public void sparseSubSetExactly() {
		SortedListDoubleSet set = new SortedListDoubleSet(-5, -3, -1, 1, 3, 5);
		DoubleSortedSet subSet = set.subSetExactly(-2, 2);
		assertThat(subSet, containsDoubles(-1, 1));
		assertThat(subSet.size(), is(2));
		assertThat(subSet.firstDouble(), is(-1.0));
		assertThat(subSet.lastDouble(), is(1.0));
		assertThat(subSet.containsDoubleExactly(1), is(true));
		assertThat(subSet.containsDoubleExactly(-3), is(false));
		assertThat(subSet.toString(), is("[-1.0, 1.0]"));

		Set<Double> equivalentSet = new HashSet<>(asList(-1.0, 1.0));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void subSet() {
		SortedListDoubleSet set = new SortedListDoubleSet(-5, -3, -1, 1, 3, 5);
		DoubleSortedSet subSet = set.subSet(-0.8, 0.8, 0.5);
		assertThat(subSet, containsDoubles(-1, 1));
		assertThat(subSet.size(), is(2));
		assertThat(subSet.firstDouble(), is(-1.0));
		assertThat(subSet.lastDouble(), is(1.0));
		assertThat(subSet.containsDouble(1.2, 0.5), is(true));
		assertThat(subSet.containsDouble(-3.2, 0.5), is(false));
		assertThat(subSet.toString(), is("[-1.0, 1.0]"));

		Set<Double> equivalentSet = new HashSet<>(asList(-1.0, 1.0));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(subSet.removeDouble(1.2, 0.5), is(true));
		assertThat(subSet.removeDouble(-3.2, 0.5), is(false));
		assertThat(subSet, containsDoubles(-1));
		assertThat(set, containsDoubles(-5, -3, -1, 3, 5));

		assertThat(subSet.addDouble(1.2, 0.5), is(true));
		assertThat(subSet.addDouble(1.25, 0.5), is(false));
		expecting(IllegalArgumentException.class, () -> subSet.addDouble(1.3, 0.5));
		assertThat(subSet, containsDoubles(-1, 1.2));
		assertThat(set, containsDoubles(-5, -3, -1, 1.2, 3, 5));
	}

	@Test
	public void headSetExactly() {
		DoubleSortedSet headSet = set.headSetExactly(0);
		assertThat(headSet, containsDoubles(-5, -4, -3, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(headSet.firstDouble(), is(-5.0));
		assertThat(headSet.lastDouble(), is(-1.0));
		assertThat(headSet.containsDoubleExactly(-3), is(true));
		assertThat(headSet.containsDoubleExactly(0), is(false));
		assertThat(headSet.toString(), is("[-5.0, -4.0, -3.0, -2.0, -1.0]"));

		Set<Double> equivalentSet = new HashSet<>(asList(-5.0, -4.0, -3.0, -2.0, -1.0));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(headSet.removeDoubleExactly(-3), is(true));
		assertThat(headSet, containsDoubles(-5, -4, -2, -1));
		assertThat(headSet.size(), is(4));
		assertThat(set, containsDoubles(-5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(headSet.removeDoubleExactly(-3), is(false));
		assertThat(headSet, containsDoubles(-5, -4, -2, -1));
		assertThat(headSet.size(), is(4));
		assertThat(set, containsDoubles(-5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(headSet.addDoubleExactly(-17), is(true));
		assertThat(headSet, containsDoubles(-17, -5, -4, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(set, containsDoubles(-17, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(headSet.addDoubleExactly(-17), is(false));
		assertThat(headSet, containsDoubles(-17, -5, -4, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(set, containsDoubles(-17, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		expecting(IllegalArgumentException.class, () -> headSet.addDoubleExactly(17));
		assertThat(headSet, containsDoubles(-17, -5, -4, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(set, containsDoubles(-17, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(set.addDoubleExactly(-6), is(true));
		assertThat(headSet, containsDoubles(-17, -6, -5, -4, -2, -1));
		assertThat(headSet.size(), is(6));
		assertThat(set, containsDoubles(-17, -6, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		headSet.clear();
		assertThat(headSet, is(emptyIterable()));
		assertThat(headSet.size(), is(0));
		assertThat(set, containsDoubles(0, 1, 2, 3, 4));
	}

	@Test
	public void sparseHeadSetExactly() {
		SortedListDoubleSet set = new SortedListDoubleSet(-5, -3, -1, 1, 3, 5);
		DoubleSortedSet headSet = set.headSetExactly(0);
		assertThat(headSet, containsDoubles(-5, -3, -1));
		assertThat(headSet.size(), is(3));
		assertThat(headSet.firstDouble(), is(-5.0));
		assertThat(headSet.lastDouble(), is(-1.0));
		assertThat(headSet.containsDoubleExactly(-3), is(true));
		assertThat(headSet.containsDoubleExactly(1), is(false));
		assertThat(headSet.toString(), is("[-5.0, -3.0, -1.0]"));

		Set<Double> equivalentSet = new HashSet<>(asList(-5.0, -3.0, -1.0));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void headSet() {
		SortedListDoubleSet set = new SortedListDoubleSet(-5, -3, -1, 1, 3, 5);
		DoubleSortedSet headSet = set.headSet(-1.2, 0.5);
		assertThat(headSet, containsDoubles(-5, -3, -1));
		assertThat(headSet.size(), is(3));
		assertThat(headSet.firstDouble(), is(-5.0));
		assertThat(headSet.lastDouble(), is(-1.0));
		assertThat(headSet.containsDouble(-3.2, 0.5), is(true));
		assertThat(headSet.containsDouble(1.2, 0.5), is(false));
		assertThat(headSet.toString(), is("[-5.0, -3.0, -1.0]"));

		Set<Double> equivalentSet = new HashSet<>(asList(-5.0, -3.0, -1.0));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(headSet.removeDouble(-3.2, 0.5), is(true));
		assertThat(headSet.removeDouble(1.2, 0.5), is(false));
		assertThat(headSet, containsDoubles(-5, -1));
		assertThat(set, containsDoubles(-5, -1, 1, 3, 5));

		assertThat(headSet.addDouble(-3.2, 0.5), is(true));
		assertThat(headSet.addDouble(-3.25, 0.5), is(false));
		expecting(IllegalArgumentException.class, () -> headSet.addDouble(-0.7, 0.5));
		assertThat(headSet, containsDoubles(-5, -3.2, -1));
		assertThat(set, containsDoubles(-5, -3.2, -1, 1, 3, 5));
	}

	@Test
	public void tailSetExactly() {
		DoubleSortedSet tailSet = set.tailSetExactly(0);
		assertThat(tailSet, containsDoubles(0, 1, 2, 3, 4));
		assertThat(tailSet.size(), is(5));
		assertThat(tailSet.firstDouble(), is(0.0));
		assertThat(tailSet.lastDouble(), is(4.0));
		assertThat(tailSet.containsDoubleExactly(3), is(true));
		assertThat(tailSet.containsDoubleExactly(-1), is(false));
		assertThat(tailSet.toString(), is("[0.0, 1.0, 2.0, 3.0, 4.0]"));

		Set<Double> equivalentSet = new HashSet<>(asList(0.0, 1.0, 2.0, 3.0, 4.0));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(tailSet.removeDoubleExactly(2), is(true));
		assertThat(tailSet, containsDoubles(0, 1, 3, 4));
		assertThat(tailSet.size(), is(4));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 3, 4));

		assertThat(tailSet.removeDoubleExactly(2), is(false));
		assertThat(tailSet, containsDoubles(0, 1, 3, 4));
		assertThat(tailSet.size(), is(4));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 3, 4));

		assertThat(tailSet.addDoubleExactly(17), is(true));
		assertThat(tailSet, containsDoubles(0, 1, 3, 4, 17));
		assertThat(tailSet.size(), is(5));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 3, 4, 17));

		assertThat(tailSet.addDoubleExactly(17), is(false));
		assertThat(tailSet, containsDoubles(0, 1, 3, 4, 17));
		assertThat(tailSet.size(), is(5));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 3, 4, 17));

		expecting(IllegalArgumentException.class, () -> tailSet.addDoubleExactly(-17));
		assertThat(tailSet, containsDoubles(0, 1, 3, 4, 17));
		assertThat(tailSet.size(), is(5));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 3, 4, 17));

		assertThat(set.addDoubleExactly(5), is(true));
		assertThat(tailSet, containsDoubles(0, 1, 3, 4, 5, 17));
		assertThat(tailSet.size(), is(6));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 3, 4, 5, 17));

		tailSet.clear();
		assertThat(tailSet, is(emptyIterable()));
		assertThat(tailSet.size(), is(0));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1));
	}

	@Test
	public void sparseTailSetExactly() {
		SortedListDoubleSet set = new SortedListDoubleSet(-5, -3, -1, 1, 3, 5);
		DoubleSortedSet tailSet = set.tailSetExactly(0);
		assertThat(tailSet, containsDoubles(1, 3, 5));
		assertThat(tailSet.size(), is(3));
		assertThat(tailSet.firstDouble(), is(1.0));
		assertThat(tailSet.lastDouble(), is(5.0));
		assertThat(tailSet.containsDouble(3.2, 0.5), is(true));
		assertThat(tailSet.containsDouble(-1.2, 0.5), is(false));
		assertThat(tailSet.toString(), is("[1.0, 3.0, 5.0]"));

		Set<Double> equivalentSet = new HashSet<>(asList(1.0, 3.0, 5.0));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void tailSet() {
		SortedListDoubleSet set = new SortedListDoubleSet(-5, -3, -1, 1, 3, 5);
		DoubleSortedSet tailSet = set.tailSet(1.2, 0.5);
		assertThat(tailSet, containsDoubles(1, 3, 5));
		assertThat(tailSet.size(), is(3));
		assertThat(tailSet.firstDouble(), is(1.0));
		assertThat(tailSet.lastDouble(), is(5.0));
		assertThat(tailSet.containsDoubleExactly(3), is(true));
		assertThat(tailSet.containsDoubleExactly(-1), is(false));
		assertThat(tailSet.toString(), is("[1.0, 3.0, 5.0]"));

		Set<Double> equivalentSet = new HashSet<>(asList(1.0, 3.0, 5.0));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(tailSet.removeDouble(3.2, 0.5), is(true));
		assertThat(tailSet.removeDouble(-1.2, 0.5), is(false));
		assertThat(tailSet, containsDoubles(1, 5));
		assertThat(set, containsDoubles(-5, -3, -1, 1, 5));

		assertThat(tailSet.addDouble(3.2, 0.5), is(true));
		assertThat(tailSet.addDouble(3.25, 0.5), is(false));
		expecting(IllegalArgumentException.class, () -> tailSet.addDouble(0.69, 0.5));
		assertThat(tailSet, containsDoubles(1, 3.2, 5));
		assertThat(set, containsDoubles(-5, -3, -1, 1, 3.2, 5));
	}

	@Test
	public void addAllDoubleArray() {
		assertThat(empty.addAllDoubles(1, 2, 3), is(true));
		assertThat(empty, containsDoubles(1, 2, 3));

		assertThat(set.addAllDoubles(3, 4, 5, 6, 7), is(true));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void addAllDoubleCollection() {
		assertThat(empty.addAllDoubles(DoubleList.create(1, 2, 3)), is(true));
		assertThat(empty, containsDoubles(1, 2, 3));

		assertThat(set.addAllDoubles(DoubleList.create(3, 4, 5, 6, 7)), is(true));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
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
	public void doubleStream() {
		assertThat(empty.doubleStream().collect(DoubleList::create, DoubleList::addDoubleExactly,
		                                        DoubleList::addAllDoubles),
		           is(emptyIterable()));

		assertThat(set.doubleStream().collect(DoubleList::create, DoubleList::addDoubleExactly,
		                                      DoubleList::addAllDoubles),
		           containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void parallelDoubleStream() {
		assertThat(empty.parallelDoubleStream()
		                .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           is(emptyIterable()));

		assertThat(set.parallelDoubleStream()
		              .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(set.sequence(), containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void firstDouble() {
		expecting(NoSuchElementException.class, empty::firstDouble);
		assertThat(set.firstDouble(), is(-5.0));
	}

	@Test
	public void lastDouble() {
		expecting(NoSuchElementException.class, empty::lastDouble);
		assertThat(set.lastDouble(), is(4.0));
	}

	@Test
	public void iteratorRemoveAll() {
		DoubleIterator iterator = set.iterator();
		double value = -5;
		while (iterator.hasNext()) {
			assertThat(iterator.nextDouble(), is(value));
			iterator.remove();
			value++;
		}
		assertThat(value, is(5.0));
		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void removeAllDoubleArray() {
		assertThat(empty.removeAllDoublesExactly(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAllDoublesExactly(1, 2, 3), is(true));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void removeAllDoubleCollection() {
		assertThat(empty.removeAll(DoubleList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(DoubleList.create(1, 2, 3)), is(true));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void retainAllDoubleArray() {
		assertThat(empty.retainAllDoublesExactly(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAllDoublesExactly(1, 2, 3), is(true));
		assertThat(set, containsDoubles(1, 2, 3));
	}

	@Test
	public void retainAllDoubleCollection() {
		assertThat(empty.retainAll(DoubleList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(DoubleList.create(1, 2, 3)), is(true));
		assertThat(set, containsDoubles(1, 2, 3));
	}

	@Test
	public void removeDoublesIf() {
		assertThat(empty.removeDoublesIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeDoublesIf(x -> x > 3), is(true));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3));
	}

	@Test
	public void containsAllDoubleArray() {
		assertThat(empty.containsAllDoublesExactly(1, 2, 3), is(false));
		assertThat(set.containsAllDoublesExactly(1, 2, 3), is(true));
		assertThat(set.containsAllDoublesExactly(1, 2, 3, 17), is(false));
	}

	@Test
	public void containsAllDoubleCollection() {
		assertThat(empty.containsAll(DoubleList.create(1, 2, 3)), is(false));
		assertThat(set.containsAll(DoubleList.create(1, 2, 3)), is(true));
		assertThat(set.containsAll(DoubleList.create(1, 2, 3, 17)), is(false));
	}

	@Test
	public void forEachDouble() {
		empty.forEachDouble(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(-5);
		set.forEachDouble(x -> assertThat(x, is((double) value.getAndIncrement())));
		assertThat(value.get(), is(5));
	}

	@Test
	public void addBoxed() {
		empty.add(17.0);
		assertThat(empty, containsDoubles(17));

		set.add(17.0);
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
	}

	@Test
	public void containsBoxed() {
		assertThat(empty.contains(17.0), is(false));

		assertThat(set.contains(17.0), is(false));
		assertThat(set.contains(new Object()), is(false));
		for (double x = -5; x <= 4; x++)
			assertThat(set.contains(x), is(true));
	}

	@Test
	public void removeBoxed() {
		assertThat(empty.remove(17), is(false));

		assertThat(set.remove(17), is(false));
		assertThat(set.remove(new Object()), is(false));
		for (double x = -5; x <= 4; x++)
			assertThat(set.remove(x), is(true));
		assertThat(set.isEmpty(), is(true));
	}

	@Test
	public void addAllBoxed() {
		assertThat(empty.addAll(Arrays.asList(1.0, 2.0, 3.0)), is(true));
		assertThat(empty, containsDoubles(1, 2, 3));

		assertThat(set.addAll(Arrays.asList(3.0, 4.0, 5.0, 6.0, 7.0)), is(true));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void firstBoxed() {
		expecting(NoSuchElementException.class, empty::first);
		assertThat(set.first(), is(-5.0));
	}

	@Test
	public void lastBoxed() {
		expecting(NoSuchElementException.class, empty::last);
		assertThat(set.last(), is(4.0));
	}

	@Test
	public void removeAllBoxed() {
		assertThat(empty.removeAll(Arrays.asList(1.0, 2.0, 3.0)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(Arrays.asList(1.0, 2.0, 3.0)), is(true));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void retainAllBoxed() {
		assertThat(empty.retainAll(Arrays.asList(1.0, 2.0, 3.0)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(Arrays.asList(1.0, 2.0, 3.0)), is(true));
		assertThat(set, containsDoubles(1, 2, 3));
	}

	@Test
	public void removeIfBoxed() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeIf(x -> x > 3), is(true));
		assertThat(set, containsDoubles(-5, -4, -3, -2, -1, 0, 1, 2, 3));
	}

	@Test
	public void containsDoubleCollection() {
		assertThat(empty.containsAll(Arrays.asList(1.0, 2.0, 3.0)), is(false));
		assertThat(set.containsAll(Arrays.asList(1.0, 2.0, 3.0)), is(true));
		assertThat(set.containsAll(Arrays.asList(1.0, 2.0, 3.0, 17.0)), is(false));
	}

	@Test
	public void forEachBoxed() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(-5);
		set.forEach(x -> assertThat(x, is((double) value.getAndIncrement())));
		assertThat(value.get(), is(5));
	}

	@Test
	public void boundaries() {
		SortedListDoubleSet intSet = new SortedListDoubleSet();
		assertThat(intSet.addDoubleExactly(0), is(true));
		assertThat(intSet.addDoubleExactly(Double.MIN_VALUE), is(true));
		assertThat(intSet.addDoubleExactly(Double.MAX_VALUE), is(true));

		assertThat(intSet, containsDoubles(0, Double.MIN_VALUE, Double.MAX_VALUE));

		assertThat(intSet.containsDoubleExactly(0), is(true));
		assertThat(intSet.containsDoubleExactly(Double.MIN_VALUE), is(true));
		assertThat(intSet.containsDoubleExactly(Double.MAX_VALUE), is(true));

		assertThat(intSet.removeDoubleExactly(0), is(true));
		assertThat(intSet.removeDoubleExactly(Double.MIN_VALUE), is(true));
		assertThat(intSet.removeDoubleExactly(Double.MAX_VALUE), is(true));

		assertThat(intSet, is(emptyIterable()));
	}

	@Test
	public void fuzz() {
		double[] randomValues = new double[10000];
		Random random = new Random();
		for (int i = 0; i < randomValues.length; i++) {
			double randomValue;
			do
				randomValue = random.nextDouble() * 1000000000;
			while (Arrayz.containsExactly(randomValues, randomValue));
			randomValues[i] = randomValue;
		}

		// Adding
		for (double randomValue : randomValues)
			assertThat(empty.addDoubleExactly(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		for (double randomValue : randomValues)
			assertThat(empty.addDoubleExactly(randomValue), is(false));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAllDoublesExactly(randomValues), is(true));

		for (double randomValue : randomValues)
			assertThat(empty.containsDoubleExactly(randomValue), is(true));

		// toString
		Arrays.sort(randomValues);
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (double randomValue : randomValues)
			assertThat(empty.removeDoubleExactly(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty.size(), is(0));

		for (double randomValue : randomValues)
			assertThat(empty.removeDoubleExactly(randomValue), is(false));
	}
}