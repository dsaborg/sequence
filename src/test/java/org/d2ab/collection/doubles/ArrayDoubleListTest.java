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

import org.d2ab.collection.Lists;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class ArrayDoubleListTest {
	private final ArrayDoubleList empty = ArrayDoubleList.create();
	private final ArrayDoubleList list = ArrayDoubleList.create(1, 2, 3, 4, 5);

	@Test
	public void withCapacity() {
		DoubleList list = ArrayDoubleList.withCapacity(3);
		twice(() -> assertThat(list, is(emptyIterable())));

		list.addAllDoubles(1, 2, 3, 4, 5);
		twice(() -> assertThat(list, containsDoubles(1, 2, 3, 4, 5)));
	}

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(list.size(), is(5));
	}

	@Test
	public void isEmpty() {
		assertThat(empty.isEmpty(), is(true));
		assertThat(list.isEmpty(), is(false));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty, is(emptyIterable()));

		list.clear();
		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void toDoubleArray() {
		assertArrayEquals(new double[0], empty.toDoubleArray(), 0);
		assertArrayEquals(new double[]{1, 2, 3, 4, 5}, list.toDoubleArray(), 0);
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5));
	}

	@Test
	public void listIteratorEmpty() {
		DoubleListIterator emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		DoubleListIterator listIterator = list.listIterator();

		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set(32.0));
		expecting(NoSuchElementException.class, listIterator::previousDouble);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.add(33.0);
		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set(34.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.previousDouble(), is(33.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.set(35.0);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.remove();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		assertThat(listIterator.nextDouble(), is(1.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextDouble(), is(2.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.nextDouble(), is(3.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.previousDouble(), is(3.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.previousDouble(), is(2.0));
		listIterator.set(17);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextDouble(), is(17.0));
		listIterator.add(18);
		listIterator.add(19);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(listIterator.nextDouble(), is(3.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(5));
		assertThat(listIterator.previousIndex(), is(4));

		assertThat(list, containsDoubles(1, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		twice(() -> {
			DoubleListIterator listIterator = list.listIterator();
			AtomicInteger i = new AtomicInteger();
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextDouble(), is((double) i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));
			expecting(NoSuchElementException.class, listIterator::nextDouble);

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previousDouble(), is((double) i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
			expecting(NoSuchElementException.class, listIterator::previousDouble);
		});
	}

	@Test
	public void iteratorRemoveAll() {
		DoubleIterator iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.nextDouble(), is((double) i + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(5));
		expecting(NoSuchElementException.class, iterator::nextDouble);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		DoubleListIterator listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.nextDouble(), is((double) i + 1));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			i++;
		}
		assertThat(i, is(5));
		expecting(NoSuchElementException.class, listIterator::nextDouble);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemoveBackwards() {
		int i = 5;
		DoubleListIterator listIterator = list.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previousDouble(), is((double) i + 1));
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
		}
		assertThat(i, is(0));
		expecting(NoSuchElementException.class, listIterator::previousDouble);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void iteratorFailFast() {
		DoubleIterator it1 = list.iterator();
		list.addDoubleExactly(17);
		expecting(ConcurrentModificationException.class, it1::nextDouble);

		DoubleIterator it2 = list.iterator();
		list.removeDoubleExactly(17);
		expecting(ConcurrentModificationException.class, it2::nextDouble);
	}

	@Test
	public void subList() {
		DoubleList list = DoubleList.create(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(-1, 2));
		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(2, 11));

		DoubleList subList = list.subList(2, 8);
		twice(() -> assertThat(subList, containsDoubles(3, 4, 5, 6, 7, 8)));

		assertThat(subList.removeDoubleAt(1), is(4.0));
		twice(() -> assertThat(subList, containsDoubles(3, 5, 6, 7, 8)));
		twice(() -> assertThat(list, containsDoubles(1, 2, 3, 5, 6, 7, 8, 9, 10)));

		assertThat(subList.removeDoubleExactly(5), is(true));
		twice(() -> assertThat(subList, containsDoubles(3, 6, 7, 8)));
		twice(() -> assertThat(list, containsDoubles(1, 2, 3, 6, 7, 8, 9, 10)));

		DoubleIterator subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextDouble(), is(3.0));
		subListIterator.remove();
		twice(() -> assertThat(subList, containsDoubles(6, 7, 8)));
		twice(() -> assertThat(list, containsDoubles(1, 2, 6, 7, 8, 9, 10)));

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, containsDoubles(7)));
		twice(() -> assertThat(list, containsDoubles(1, 2, 7, 9, 10)));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, containsDoubles(1, 2, 9, 10)));

		subList.addDoubleExactly(17);
		twice(() -> assertThat(subList, containsDoubles(17)));
		twice(() -> assertThat(list, containsDoubles(1, 2, 17, 9, 10)));
	}

	@Test
	public void sortDoubles() {
		DoubleList list = ArrayDoubleList.create(32, 17, 5, 7, 19, 22);
		list.sortDoubles();
		assertThat(list, containsDoubles(5, 7, 17, 19, 22, 32));
	}

	@Test
	public void binarySearch() {
		DoubleList list = ArrayDoubleList.create(1, 3, 5, 6, 7, 8, 32);
		assertThat(list.binarySearchExactly(1), is(0));
		assertThat(list.binarySearchExactly(5), is(2));
		assertThat(list.binarySearchExactly(7), is(4));
		assertThat(list.binarySearchExactly(32), is(6));
		assertThat(list.binarySearchExactly(0), is(-1));
		assertThat(list.binarySearchExactly(2), is(-2));
		assertThat(list.binarySearchExactly(4), is(-3));
		assertThat(list.binarySearchExactly(31), is(-7));
		assertThat(list.binarySearchExactly(33), is(-8));
	}

	@Test
	public void doubleStream() {
		assertThat(empty.doubleStream()
		                .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           is(emptyIterable()));

		assertThat(list.doubleStream()
		               .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           containsDoubles(1, 2, 3, 4, 5));
	}

	@Test
	public void parallelDoubleStream() {
		assertThat(empty.parallelDoubleStream()
		                .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           is(emptyIterable()));

		assertThat(list.parallelDoubleStream()
		               .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           containsDoubles(1, 2, 3, 4, 5));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(list.toString(), is("[1.0, 2.0, 3.0, 4.0, 5.0]"));
	}

	@Test
	public void testEqualsHashCodeAgainstList() {
		List<Double> list2 = new ArrayList<>(asList(1.0, 2.0, 3.0, 4.0, 5.0, 17.0));
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));

		list2.remove(17.0);

		assertThat(list, is(equalTo(list2)));
		assertThat(list.hashCode(), is(list2.hashCode()));

		Lists.reverse(list2);
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));
	}

	@Test
	public void testEqualsHashCodeAgainstDoubleList() {
		DoubleList list2 = DoubleList.create(1, 2, 3, 4, 5, 17);
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));

		list2.removeDoubleExactly(17);

		assertThat(list, is(equalTo(list2)));
		assertThat(list.hashCode(), is(list2.hashCode()));

		Lists.reverse(list2);
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(list.sequence(), containsDoubles(1, 2, 3, 4, 5));
	}

	@Test
	public void lastIndexOfBoxed() {
		assertThat(empty.lastIndexOf(17.0), is(-1));

		assertThat(list.lastIndexOf(17.0), is(-1));
		assertThat(list.lastIndexOf(2.0), is(1));
	}

	@Test
	public void lastIndexOfDoubleExactly() {
		assertThat(empty.lastIndexOfDoubleExactly(17), is(-1));

		assertThat(list.lastIndexOfDoubleExactly(17), is(-1));
		assertThat(list.lastIndexOfDoubleExactly(2), is(1));
	}

	@Test
	public void lastIndexOfDouble() {
		assertThat(empty.lastIndexOfDouble(17.1, 0.5), is(-1));

		assertThat(list.lastIndexOfDouble(17.1, 0.5), is(-1));
		assertThat(list.lastIndexOfDouble(2.1, 0.5), is(1));
	}

	@Test
	public void indexOfBoxed() {
		assertThat(empty.indexOf(17.0), is(-1));

		assertThat(list.indexOf(17.0), is(-1));
		assertThat(list.indexOf(2.0), is(1));
	}

	@Test
	public void indexOfDoubleExactly() {
		assertThat(empty.indexOfDoubleExactly(17), is(-1));

		assertThat(list.indexOfDoubleExactly(17), is(-1));
		assertThat(list.indexOfDoubleExactly(2), is(1));
	}

	@Test
	public void indexOfDouble() {
		assertThat(empty.indexOfDouble(17.1, 0.5), is(-1));

		assertThat(list.indexOfDouble(17.1, 0.5), is(-1));
		assertThat(list.indexOfDouble(2.1, 0.5), is(1));
	}

	@Test
	public void getBoxed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.get(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.get(2), is(3.0));
	}

	@Test
	public void getDouble() {
		expecting(IndexOutOfBoundsException.class, () -> empty.getDouble(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.getDouble(2), is(3.0));
	}

	@Test
	public void setBoxed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.set(2, 17.0));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.set(2, 17.0), is(3.0));
		assertThat(list, containsDoubles(1, 2, 17, 4, 5));
	}

	@Test
	public void setDouble() {
		expecting(IndexOutOfBoundsException.class, () -> empty.setDouble(2, 17));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.setDouble(2, 17), is(3.0));
		assertThat(list, containsDoubles(1, 2, 17, 4, 5));
	}

	@Test
	public void addBoxed() {
		assertThat(empty.add(17.0), is(true));
		assertThat(empty, containsDoubles(17));

		assertThat(list.add(17.0), is(true));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void addDoubleExactly() {
		assertThat(empty.addDoubleExactly(17), is(true));
		assertThat(empty, containsDoubles(17));

		assertThat(list.addDoubleExactly(17), is(true));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void addDouble() {
		assertThat(empty.addDouble(17, 0.5), is(true));
		assertThat(empty, containsDoubles(17));

		assertThat(list.addDouble(17, 0.5), is(true));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void addAtBoxed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.add(2, 17.0));
		assertThat(empty, is(emptyIterable()));

		empty.add(0, 17.0);
		assertThat(empty, containsDoubles(17));

		list.add(2, 17.0);
		assertThat(list, containsDoubles(1, 2, 17, 3, 4, 5));
	}

	@Test
	public void addDoubleAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.addDoubleAt(2, 17));
		assertThat(empty, is(emptyIterable()));

		empty.addDoubleAt(0, 17);
		assertThat(empty, containsDoubles(17));

		list.addDoubleAt(2, 17);
		assertThat(list, containsDoubles(1, 2, 17, 3, 4, 5));
	}

	@Test
	public void addAllBoxed() {
		assertThat(empty.addAll(emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(asList(1.0, 2.0, 3.0)), is(true));
		assertThat(empty, containsDoubles(1, 2, 3));

		assertThat(list.addAll(asList(6.0, 7.0, 8.0)), is(true));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllDoublesArray() {
		assertThat(empty.addAllDoubles(), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllDoubles(1, 2, 3), is(true));
		assertThat(empty, containsDoubles(1, 2, 3));

		assertThat(list.addAllDoubles(6, 7, 8), is(true));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllDoublesDoubleCollection() {
		assertThat(empty.addAllDoubles(new SortedListDoubleSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllDoubles(new SortedListDoubleSet(1, 2, 3)), is(true));
		assertThat(empty, containsDoubles(1, 2, 3));

		assertThat(list.addAllDoubles(new SortedListDoubleSet(6, 7, 8)), is(true));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllDoublesArrayDoubleList() {
		assertThat(empty.addAllDoubles(ArrayDoubleList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllDoubles(ArrayDoubleList.create(1, 2, 3)), is(true));
		assertThat(empty, containsDoubles(1, 2, 3));

		assertThat(list.addAllDoubles(ArrayDoubleList.create(6, 7, 8)), is(true));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllAtBoxed() {
		assertThat(empty.addAll(0, emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, asList(1.0, 2.0, 3.0)), is(true));
		assertThat(empty, containsDoubles(1, 2, 3));

		assertThat(list.addAll(2, asList(17.0, 18.0, 19.0)), is(true));
		assertThat(list, containsDoubles(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void addAllDoublesAtAtArray() {
		assertThat(empty.addAllDoublesAt(0), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllDoublesAt(0, 1, 2, 3), is(true));
		assertThat(empty, containsDoubles(1, 2, 3));

		assertThat(list.addAllDoublesAt(2, 17, 18, 19), is(true));
		assertThat(list, containsDoubles(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void addAllDoublesAtDoubleCollection() {
		assertThat(empty.addAllDoublesAt(0, new SortedListDoubleSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllDoublesAt(0, new SortedListDoubleSet(1, 2, 3)), is(true));
		assertThat(empty, containsDoubles(1, 2, 3));

		assertThat(list.addAllDoublesAt(2, new SortedListDoubleSet(17, 18, 19)), is(true));
		assertThat(list, containsDoubles(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void addAllDoublesAtArrayDoubleList() {
		assertThat(empty.addAllDoublesAt(0, ArrayDoubleList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllDoublesAt(0, ArrayDoubleList.create(1, 2, 3)), is(true));
		assertThat(empty, containsDoubles(1, 2, 3));

		assertThat(list.addAllDoublesAt(2, ArrayDoubleList.create(17, 18, 19)), is(true));
		assertThat(list, containsDoubles(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void removeBoxed() {
		assertThat(empty.remove(17.0), is(false));
		assertThat(empty.remove(new Object()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove(17.0), is(false));
		assertThat(list.remove(new Object()), is(false));
		assertThat(list.remove(2.0), is(true));
		assertThat(list, containsDoubles(1, 3, 4, 5));
	}

	@Test
	public void removeDoubleExactly() {
		assertThat(empty.removeDoubleExactly(17), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeDoubleExactly(17), is(false));
		assertThat(list.removeDoubleExactly(2), is(true));
		assertThat(list, containsDoubles(1, 3, 4, 5));
	}

	@Test
	public void removeDouble() {
		assertThat(empty.removeDouble(17.1, 0.5), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeDouble(17.1, 0.5), is(false));
		assertThat(list.removeDouble(2.1, 0.5), is(true));
		assertThat(list, containsDoubles(1, 3, 4, 5));
	}

	@Test
	public void removeAtBoxed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.remove(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove(2), is(3.0));
		assertThat(list, containsDoubles(1, 2, 4, 5));
	}

	@Test
	public void removeDoubleAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.removeDoubleAt(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeDoubleAt(2), is(3.0));
		assertThat(list, containsDoubles(1, 2, 4, 5));
	}

	@Test
	public void containsBoxed() {
		assertThat(empty.contains(17.0), is(false));
		assertThat(empty.contains(new Object()), is(false));

		assertThat(list.contains(17.0), is(false));
		assertThat(list.contains(new Object()), is(false));
		assertThat(list.contains(2.0), is(true));
	}

	@Test
	public void containsDoubleExactly() {
		assertThat(empty.containsDoubleExactly(17), is(false));

		assertThat(list.containsDoubleExactly(17), is(false));
		assertThat(list.containsDoubleExactly(2), is(true));
	}

	@Test
	public void containsDouble() {
		assertThat(empty.containsDouble(17.1, 0.5), is(false));

		assertThat(list.containsDouble(17.1, 0.5), is(false));
		assertThat(list.containsDouble(2.1, 0.5), is(true));
	}

	@Test
	public void containsAllBoxed() {
		assertThat(empty.containsAll(asList(17.0, 18.0, 19.0, new Object())), is(false));

		assertThat(list.containsAll(asList(17.0, 18.0, 19.0, new Object())), is(false));
		assertThat(list.containsAll(asList(1.0, 2.0, 3.0)), is(true));
	}

	@Test
	public void containsAllDoublesExactlyArray() {
		assertThat(empty.containsAllDoublesExactly(17, 18, 19), is(false));

		assertThat(list.containsAllDoublesExactly(17, 18, 19), is(false));
		assertThat(list.containsAllDoublesExactly(1, 2, 3), is(true));
	}

	@Test
	public void containsAllDoublesArray() {
		assertThat(empty.containsAllDoubles(new double[]{17.1, 17.9, 19.1}, 0.5), is(false));

		assertThat(list.containsAllDoubles(new double[]{17.1, 17.9, 19.1}, 0.5), is(false));
		assertThat(list.containsAllDoubles(new double[]{1.1, 1.9, 3.1}, 0.5), is(true));
	}

	@Test
	public void containsAllDoublesExactlyCollection() {
		assertThat(empty.containsAllDoublesExactly(ArrayDoubleList.create(17, 18, 19)), is(false));

		assertThat(list.containsAllDoublesExactly(ArrayDoubleList.create(17, 18, 19)), is(false));
		assertThat(list.containsAllDoublesExactly(ArrayDoubleList.create(1, 2, 3)), is(true));
	}

	@Test
	public void containsAllDoublesCollection() {
		assertThat(empty.containsAllDoubles(ArrayDoubleList.create(17.1, 17.9, 19.1), 0.5), is(false));

		assertThat(list.containsAllDoubles(ArrayDoubleList.create(17.1, 17.9, 19.1), 0.5), is(false));
		assertThat(list.containsAllDoubles(ArrayDoubleList.create(1.1, 1.9, 3.1), 0.5), is(true));
	}

	@Test
	public void containsAnyDoublesExactlyArray() {
		assertThat(empty.containsAnyDoublesExactly(17, 18, 19), is(false));

		assertThat(list.containsAnyDoublesExactly(17, 18, 19), is(false));
		assertThat(list.containsAnyDoublesExactly(1, 17, 3), is(true));
	}

	@Test
	public void containsAnyDoublesArray() {
		assertThat(empty.containsAnyDoubles(new double[]{17.1, 17.9, 19.1}, 0.5), is(false));

		assertThat(list.containsAnyDoubles(new double[]{17.1, 17.9, 19.1}, 0.5), is(false));
		assertThat(list.containsAnyDoubles(new double[]{1.1, 17.1, 3.1}, 0.5), is(true));
	}

	@Test
	public void containsAnyDoublesExactlyCollection() {
		assertThat(empty.containsAnyDoublesExactly(ArrayDoubleList.create(17, 18, 19)), is(false));

		assertThat(list.containsAnyDoublesExactly(ArrayDoubleList.create(17, 18, 19)), is(false));
		assertThat(list.containsAnyDoublesExactly(ArrayDoubleList.create(1, 17, 3)), is(true));
	}

	@Test
	public void containsAnyDoublesCollection() {
		assertThat(empty.containsAnyDoubles(ArrayDoubleList.create(17.1, 17.9, 19.1), 0.5), is(false));

		assertThat(list.containsAnyDoubles(ArrayDoubleList.create(17.1, 17.9, 19.1), 0.5), is(false));
		assertThat(list.containsAnyDoubles(ArrayDoubleList.create(1.1, 17.1, 3.1), 0.5), is(true));
	}

	@Test
	public void removeAllBoxed() {
		assertThat(empty.removeAll(asList(1.0, 2.0, 3.0, 17.0)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(asList(17.0, 18.0, 19.0)), is(false));
		assertThat(list.removeAll(asList(1.0, 2.0, 3.0, 17.0)), is(true));
		assertThat(list, containsDoubles(4, 5));
	}

	@Test
	public void removeAllDoublesExactlyArray() {
		assertThat(empty.removeAllDoublesExactly(1, 2, 3, 17), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllDoublesExactly(17, 18, 19), is(false));
		assertThat(list.removeAllDoublesExactly(1, 2, 3, 17), is(true));
		assertThat(list, containsDoubles(4, 5));
	}

	@Test
	public void removeAllDoublesArray() {
		assertThat(empty.removeAllDoubles(new double[]{1.1, 1.9, 3.1, 17.1}, 0.5), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllDoubles(new double[]{17.1, 17.9, 19.1}, 0.5), is(false));
		assertThat(list.removeAllDoubles(new double[]{1.1, 1.9, 3.1, 17.1}, 0.5), is(true));
		assertThat(list, containsDoubles(4, 5));
	}

	@Test
	public void removeAllDoublesExactlyCollection() {
		assertThat(empty.removeAllDoublesExactly(ArrayDoubleList.create(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllDoublesExactly(ArrayDoubleList.create(17, 18, 19)), is(false));
		assertThat(list.removeAllDoublesExactly(ArrayDoubleList.create(1, 2, 3, 17)), is(true));
		assertThat(list, containsDoubles(4, 5));
	}

	@Test
	public void removeAllDoublesCollection() {
		assertThat(empty.removeAllDoubles(ArrayDoubleList.create(1.1, 1.9, 3.1, 17.1), 0.5), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllDoubles(ArrayDoubleList.create(17.1, 17.9, 19.1), 0.5), is(false));
		assertThat(list.removeAllDoubles(ArrayDoubleList.create(1.1, 1.9, 3.1, 17.1), 0.5), is(true));
		assertThat(list, containsDoubles(4, 5));
	}

	@Test
	public void removeIfBoxed() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeIf(x -> x > 5), is(false));
		assertThat(list.removeIf(x -> x > 3), is(true));
		assertThat(list, containsDoubles(1, 2, 3));
	}

	@Test
	public void removeDoublesIf() {
		assertThat(empty.removeDoublesIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeDoublesIf(x -> x > 5), is(false));
		assertThat(list.removeDoublesIf(x -> x > 3), is(true));
		assertThat(list, containsDoubles(1, 2, 3));
	}

	@Test
	public void retainAllBoxed() {
		assertThat(empty.retainAll(asList(1.0, 2.0, 3.0, 17.0)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(asList(1.0, 2.0, 3.0, 17.0)), is(true));
		assertThat(list, containsDoubles(1, 2, 3));
	}

	@Test
	public void retainAllDoublesExactlyArray() {
		assertThat(empty.retainAllDoublesExactly(1, 2, 3, 17), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllDoublesExactly(1, 2, 3, 17), is(true));
		assertThat(list, containsDoubles(1, 2, 3));
	}

	@Test
	public void retainAllDoublesArray() {
		assertThat(empty.retainAllDoubles(new double[]{1.1, 1.9, 3.1, 17.1}, 0.5), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllDoubles(new double[]{1.1, 1.9, 3.1, 17.1}, 0.5), is(true));
		assertThat(list, containsDoubles(1, 2, 3));
	}

	@Test
	public void retainAllDoublesExactlyCollection() {
		assertThat(empty.retainAllDoublesExactly(ArrayDoubleList.create(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllDoublesExactly(ArrayDoubleList.create(1, 2, 3, 17)), is(true));
		assertThat(list, containsDoubles(1, 2, 3));
	}

	@Test
	public void retainAllDoublesCollection() {
		assertThat(empty.retainAllDoubles(ArrayDoubleList.create(1.1, 1.9, 3.1, 17.1), 0.5), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllDoubles(ArrayDoubleList.create(1.1, 1.9, 3.1, 17.1), 0.5), is(true));
		assertThat(list, containsDoubles(1, 2, 3));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		list.replaceAll(x -> x + 1);
		assertThat(list, containsDoubles(2, 3, 4, 5, 6));
	}

	@Test
	public void replaceAllDoubles() {
		empty.replaceAllDoubles(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		list.replaceAllDoubles(x -> x + 1);
		assertThat(list, containsDoubles(2, 3, 4, 5, 6));
	}

	@Test
	public void forEachDouble() {
		empty.forEachDouble(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		list.forEachDouble(x -> assertThat(x, is((double) value.getAndIncrement())));
		assertThat(value.get(), is(6));
	}
}
