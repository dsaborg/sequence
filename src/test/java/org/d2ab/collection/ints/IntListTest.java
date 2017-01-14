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

import org.d2ab.collection.Lists;
import org.d2ab.collection.chars.CharList;
import org.d2ab.iterator.ints.IntIterator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class IntListTest {
	private final IntList empty = IntList.Base.create();
	private final IntList list = IntList.Base.create(1, 2, 3, 4, 5, 1, 2, 3, 4, 5);

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(list.size(), is(10));
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
	public void testAsList() {
		assertThat(empty.asList(), is(sameInstance(empty)));
		assertThat(list.asList(), is(sameInstance(list)));
	}

	@Test
	public void toIntArray() {
		assertArrayEquals(new int[0], empty.toIntArray());
		assertArrayEquals(new int[]{1, 2, 3, 4, 5, 1, 2, 3, 4, 5}, list.toIntArray());
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void listIteratorEmpty() {
		IntListIterator emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		emptyIterator.add(17);
		emptyIterator.add(18);
		expecting(IllegalStateException.class, () -> emptyIterator.set(19));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(true));
		assertThat(emptyIterator.nextIndex(), is(2));
		assertThat(emptyIterator.previousIndex(), is(1));

		assertThat(emptyIterator.previousInt(), is(18));
		assertThat(emptyIterator.hasNext(), is(true));
		assertThat(emptyIterator.hasPrevious(), is(true));
		assertThat(emptyIterator.nextIndex(), is(1));
		assertThat(emptyIterator.previousIndex(), is(0));

		emptyIterator.remove();
		expecting(IllegalStateException.class, () -> emptyIterator.set(19));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(true));
		assertThat(emptyIterator.nextIndex(), is(1));
		assertThat(emptyIterator.previousIndex(), is(0));

		assertThat(emptyIterator.previousInt(), is(17));
		emptyIterator.set(19);
		assertThat(emptyIterator.hasNext(), is(true));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(NoSuchElementException.class, emptyIterator::previousInt);
		assertThat(emptyIterator.hasNext(), is(true));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, containsInts(19));
	}

	@Test
	public void listIterator() {
		IntListIterator listIterator = list.listIterator();

		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set(32));
		expecting(NoSuchElementException.class, listIterator::previousInt);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.add(33);
		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set(34));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.previousInt(), is(33));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.set(35);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.remove();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		assertThat(listIterator.nextInt(), is(1));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextInt(), is(2));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.nextInt(), is(3));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.previousInt(), is(3));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.previousInt(), is(2));
		listIterator.set(17);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextInt(), is(17));
		listIterator.add(18);
		listIterator.add(19);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(listIterator.nextInt(), is(3));

		assertThat(list, containsInts(1, 17, 18, 19, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		twice(() -> {
			IntListIterator listIterator = list.listIterator();
			AtomicInteger i = new AtomicInteger();
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextInt(), is(i.get() % 5 + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
			expecting(NoSuchElementException.class, listIterator::nextInt);

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previousInt(), is(i.get() % 5 + 1));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
			expecting(NoSuchElementException.class, listIterator::previousInt);
		});
	}

	@Test
	public void iteratorRemoveAll() {
		IntIterator iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.nextInt(), is(i % 5 + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));
		expecting(NoSuchElementException.class, iterator::nextInt);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		IntListIterator listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.nextInt(), is(i % 5 + 1));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			i++;
		}
		assertThat(i, is(10));
		expecting(NoSuchElementException.class, listIterator::nextInt);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemoveBackwards() {
		int i = 10;
		IntListIterator listIterator = list.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previousInt(), is(i % 5 + 1));
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
		}
		assertThat(i, is(0));
		expecting(NoSuchElementException.class, listIterator::previousInt);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void iteratorFailFast() {
		IntIterator it1 = list.iterator();
		list.addInt(17);
		expecting(ConcurrentModificationException.class, it1::nextInt);

		IntIterator it2 = list.iterator();
		list.removeInt(17);
		expecting(ConcurrentModificationException.class, it2::nextInt);
	}

	@Test
	public void subList() {
		IntList list = IntList.Base.create(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(-1, 2));
		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(2, 11));

		IntList subList = list.subList(2, 8);
		assertThat(subList.size(), is(6));
		twice(() -> assertThat(subList, containsInts(3, 4, 5, 6, 7, 8)));

		IntList equivalentList = IntList.create(3, 4, 5, 6, 7, 8);
		assertThat(subList, is(equalTo(equivalentList)));
		assertThat(subList.hashCode(), is(equivalentList.hashCode()));
		assertThat(subList.toString(), is("[3, 4, 5, 6, 7, 8]"));

		assertThat(subList.removeIntAt(1), is(4));
		twice(() -> assertThat(subList, containsInts(3, 5, 6, 7, 8)));
		twice(() -> assertThat(list, containsInts(1, 2, 3, 5, 6, 7, 8, 9, 10)));

		assertThat(subList.removeInt(5), is(true));
		twice(() -> assertThat(subList, containsInts(3, 6, 7, 8)));
		twice(() -> assertThat(list, containsInts(1, 2, 3, 6, 7, 8, 9, 10)));

		IntIterator subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextInt(), is(3));
		subListIterator.remove();
		twice(() -> assertThat(subList, containsInts(6, 7, 8)));
		twice(() -> assertThat(list, containsInts(1, 2, 6, 7, 8, 9, 10)));

		subList.removeIntsIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, containsInts(7)));
		twice(() -> assertThat(list, containsInts(1, 2, 7, 9, 10)));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, containsInts(1, 2, 9, 10)));

		expecting(UnsupportedOperationException.class, () -> subList.addInt(17));
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, containsInts(1, 2, 9, 10)));
	}

	@Test
	public void sortInts() {
		IntList list = IntList.Base.create(5, 4, 3, 2, 1);
		expecting(UnsupportedOperationException.class, list::sortInts);
		assertThat(list, containsInts(5, 4, 3, 2, 1));
	}

	@Test
	public void binarySearch() {
		expecting(UnsupportedOperationException.class, () -> list.binarySearch(1));
	}

	@Test
	public void intStream() {
		assertThat(empty.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));

		assertThat(list.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void parallelIntStream() {
		assertThat(empty.parallelIntStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));

		assertThat(list.parallelIntStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(list.toString(), is("[1, 2, 3, 4, 5, 1, 2, 3, 4, 5]"));
	}

	@Test
	public void testEqualsHashCodeAgainstList() {
		assertThat(list, is(equalTo(list)));
		assertThat(list, is(not(equalTo(null))));
		assertThat(list, is(not(equalTo(new Object()))));
		assertThat(list, is(not(equalTo(new ArrayList<>(asList(1, 2, 3, 4, 5, 1, 2, 3, 4))))));

		List<Integer> list2 = new ArrayList<>(asList(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 17));
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));

		list2.remove((Integer) 17);

		assertThat(list, is(equalTo(list2)));
		assertThat(list.hashCode(), is(list2.hashCode()));

		Lists.reverse(list2);
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));
	}

	@Test
	public void testEqualsHashCodeAgainstIntList() {
		assertThat(list, is(not(equalTo(IntList.create(1, 2, 3, 4, 5, 1, 2, 3, 4)))));

		IntList list2 = IntList.create(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 17);
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));

		list2.removeInt(17);

		assertThat(list, is(equalTo(list2)));
		assertThat(list.hashCode(), is(list2.hashCode()));

		IntList list3 = IntList.create(5, 4, 3, 2, 1, 5, 4, 3, 2, 1);
		assertThat(list, is(not(equalTo(list3))));
		assertThat(list.hashCode(), is(not(list3.hashCode())));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(list.sequence(), containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void lastIndexOfInt() {
		assertThat(empty.lastIndexOfInt(17), is(-1));

		assertThat(list.lastIndexOfInt(17), is(-1));
		assertThat(list.lastIndexOfInt(1), is(5));
		assertThat(list.lastIndexOfInt(3), is(7));
		assertThat(list.lastIndexOfInt(5), is(9));
	}

	@Test
	public void indexOfInt() {
		assertThat(empty.indexOfInt(17), is(-1));

		assertThat(list.indexOfInt(17), is(-1));
		assertThat(list.indexOfInt(1), is(0));
		assertThat(list.indexOfInt(3), is(2));
		assertThat(list.indexOfInt(5), is(4));
	}

	@Test
	public void getInt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.getInt(2));
		expecting(IndexOutOfBoundsException.class, () -> empty.getInt(0));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.getInt(2), is(3));
		expecting(IndexOutOfBoundsException.class, () -> list.getInt(12));
		expecting(IndexOutOfBoundsException.class, () -> list.getInt(10));
	}

	@Test
	public void setInt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.setInt(2, 17));
		expecting(IndexOutOfBoundsException.class, () -> empty.setInt(0, 17));
		assertThat(empty, is(emptyIterable()));

		expecting(IndexOutOfBoundsException.class, () -> list.setInt(12, 17));
		expecting(IndexOutOfBoundsException.class, () -> list.setInt(10, 17));
		assertThat(list.setInt(2, 17), is(3));
		assertThat(list, containsInts(1, 2, 17, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addInt() {
		assertThat(empty.addInt(17), is(true));
		assertThat(empty, containsInts(17));

		assertThat(list.addInt(17), is(true));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 17));
	}

	@Test
	public void addIntAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.addIntAt(2, 17));
		assertThat(empty, is(emptyIterable()));

		empty.addIntAt(0, 17);
		assertThat(empty, containsInts(17));

		list.addIntAt(2, 17);
		assertThat(list, containsInts(1, 2, 17, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addAllIntsArray() {
		assertThat(empty.addAllInts(), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllInts(1, 2, 3), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(list.addAllInts(6, 7, 8), is(true));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllIntsIntCollection() {
		assertThat(empty.addAllInts(IntSortedSet.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllInts(IntSortedSet.create(1, 2, 3)), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(list.addAllInts(IntSortedSet.create(6, 7, 8)), is(true));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllIntsArrayIntList() {
		assertThat(empty.addAllInts(IntList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllInts(IntList.create(1, 2, 3)), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(list.addAllInts(IntList.create(6, 7, 8)), is(true));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllIntsAtAtArray() {
		assertThat(empty.addAllIntsAt(0), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllIntsAt(0, 1, 2, 3), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(list.addAllIntsAt(2, 17, 18, 19), is(true));
		assertThat(list, containsInts(1, 2, 17, 18, 19, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addAllIntsAtIntCollection() {
		assertThat(empty.addAllIntsAt(0, IntSortedSet.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllIntsAt(0, IntSortedSet.create(1, 2, 3)), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(list.addAllIntsAt(2, IntSortedSet.create(17, 18, 19)), is(true));
		assertThat(list, containsInts(1, 2, 17, 18, 19, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addAllIntsAtArrayIntList() {
		assertThat(empty.addAllIntsAt(0, IntList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllIntsAt(0, IntList.create(1, 2, 3)), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(list.addAllIntsAt(2, IntList.create(17, 18, 19)), is(true));
		assertThat(list, containsInts(1, 2, 17, 18, 19, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeInt() {
		assertThat(empty.removeInt(17), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeInt(17), is(false));
		assertThat(list.removeInt(2), is(true));
		assertThat(list, containsInts(1, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeIntAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.removeIntAt(0));
		expecting(IndexOutOfBoundsException.class, () -> empty.removeIntAt(2));
		assertThat(empty, is(emptyIterable()));

		expecting(IndexOutOfBoundsException.class, () -> list.removeIntAt(10));
		expecting(IndexOutOfBoundsException.class, () -> list.removeIntAt(12));
		assertThat(list.removeIntAt(2), is(3));
		assertThat(list, containsInts(1, 2, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void containsInt() {
		assertThat(empty.containsInt(17), is(false));

		assertThat(list.containsInt(17), is(false));
		assertThat(list.containsInt(2), is(true));
	}

	@Test
	public void containsAllIntsArray() {
		assertThat(empty.containsAllInts(17, 18, 19), is(false));

		assertThat(list.containsAllInts(17, 18, 19), is(false));
		assertThat(list.containsAllInts(1, 2, 3), is(true));
	}

	@Test
	public void containsAllIntsIntCollection() {
		assertThat(empty.containsAllInts(IntList.create(17, 18, 19)), is(false));

		assertThat(list.containsAllInts(IntList.create(17, 18, 19)), is(false));
		assertThat(list.containsAllInts(IntList.create(1, 2, 3)), is(true));
	}

	@Test
	public void containsAnyIntsArray() {
		assertThat(empty.containsAnyInts(17, 18, 19), is(false));

		assertThat(list.containsAnyInts(17, 18, 19), is(false));
		assertThat(list.containsAnyInts(1, 17, 3), is(true));
	}

	@Test
	public void containsAnyIntsIntCollection() {
		assertThat(empty.containsAnyInts(IntList.create(17, 18, 19)), is(false));

		assertThat(list.containsAnyInts(IntList.create(17, 18, 19)), is(false));
		assertThat(list.containsAnyInts(IntList.create(1, 17, 3)), is(true));
	}

	@Test
	public void removeAllIntsArray() {
		assertThat(empty.removeAllInts(1, 2, 3, 17), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllInts(17, 17, 19), is(false));
		assertThat(list.removeAllInts(1, 2, 3, 17), is(true));
		assertThat(list, containsInts(4, 5, 4, 5));
	}

	@Test
	public void removeAllIntsIntCollection() {
		assertThat(empty.removeAllInts(IntList.create(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllInts(IntList.create(17, 18, 19)), is(false));
		assertThat(list.removeAllInts(IntList.create(1, 2, 3, 17)), is(true));
		assertThat(list, containsInts(4, 5, 4, 5));
	}

	@Test
	public void removeIntsIf() {
		assertThat(empty.removeIntsIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeIntsIf(x -> x > 5), is(false));
		assertThat(list.removeIntsIf(x -> x > 3), is(true));
		assertThat(list, containsInts(1, 2, 3, 1, 2, 3));
	}

	@Test
	public void retainAllIntsArray() {
		assertThat(empty.retainAllInts(1, 2, 3, 17), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllInts(1, 2, 3, 17), is(true));
		assertThat(list, containsInts(1, 2, 3, 1, 2, 3));
	}

	@Test
	public void retainAllIntsIntCollection() {
		assertThat(empty.retainAllInts(IntList.create(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllInts(IntList.create(1, 2, 3, 17)), is(true));
		assertThat(list, containsInts(1, 2, 3, 1, 2, 3));
	}

	@Test
	public void replaceAllInts() {
		empty.replaceAllInts(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		list.replaceAllInts(x -> x + 1);
		assertThat(list, containsInts(2, 3, 4, 5, 6, 2, 3, 4, 5, 6));
	}

	@Test
	public void forEachInt() {
		empty.forEachInt(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger index = new AtomicInteger(0);
		list.forEachInt(x -> assertThat(x, is(index.getAndIncrement() % 5 + 1)));
		assertThat(index.get(), is(10));
	}

	@Test
	public void asChars() {
		CharList emptyAsChars = empty.asChars();
		twice(() -> assertThat(emptyAsChars, is(emptyIterable())));
		assertThat(emptyAsChars.size(), is(0));

		CharList listAsChars = IntList.Base.create('a', 'b', 'c', 'd', 'e').asChars();
		twice(() -> assertThat(listAsChars, containsChars('a', 'b', 'c', 'd', 'e')));
		assertThat(listAsChars.size(), is(5));
	}
}
