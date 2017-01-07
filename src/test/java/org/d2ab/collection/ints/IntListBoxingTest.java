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
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static java.util.Comparator.naturalOrder;
import static org.d2ab.collection.Arrayz.fill;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class IntListBoxingTest extends BaseBoxingTest {
	private final List<Integer> empty = IntList.Base.create();
	private final List<Integer> list = IntList.Base.create(1, 2, 3, 4, 5, 1, 2, 3, 4, 5);

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
	public void toArray() {
		assertArrayEquals(new Integer[0], empty.toArray());
		assertArrayEquals(new Integer[]{1, 2, 3, 4, 5, 1, 2, 3, 4, 5}, list.toArray());
	}

	@Test
	public void toArrayEmptyTarget() {
		Integer[] emptyTarget = new Integer[0];
		assertThat(empty.toArray(emptyTarget), is(sameInstance(emptyTarget)));
		assertArrayEquals(new Integer[]{1, 2, 3, 4, 5, 1, 2, 3, 4, 5}, list.toArray(new Integer[0]));
	}

	@Test
	public void toArraySmallerTarget() {
		assertArrayEquals(new Integer[]{1, 2, 3, 4, 5, 1, 2, 3, 4, 5}, list.toArray(new Integer[9]));
	}

	@Test
	public void toArrayBiggerTarget() {
		assertArrayEquals(new Integer[]{null, 17}, empty.toArray(fill(new Integer[2], 17)));
		assertArrayEquals(new Integer[]{1, 2, 3, 4, 5, 1, 2, 3, 4, 5, null, 17},
		                  list.toArray(fill(new Integer[12], 17)));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Integer> emptyIterator = empty.listIterator();
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

		assertThat(emptyIterator.previous(), is(18));
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

		assertThat(emptyIterator.previous(), is(17));
		emptyIterator.set(19);
		assertThat(emptyIterator.hasNext(), is(true));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(NoSuchElementException.class, emptyIterator::previous);
		assertThat(emptyIterator.hasNext(), is(true));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, contains(19));
	}

	@Test
	public void listIterator() {
		ListIterator<Integer> listIterator = list.listIterator();

		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set(32));
		expecting(NoSuchElementException.class, listIterator::previous);
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

		assertThat(listIterator.previous(), is(33));
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

		assertThat(listIterator.next(), is(1));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is(2));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.next(), is(3));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.previous(), is(3));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.previous(), is(2));
		listIterator.set(17);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is(17));
		listIterator.add(18);
		listIterator.add(19);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(listIterator.next(), is(3));

		assertThat(list, contains(1, 17, 18, 19, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		twice(() -> {
			ListIterator<Integer> listIterator = list.listIterator();
			AtomicInteger i = new AtomicInteger();
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is(i.get() % 5 + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
			expecting(NoSuchElementException.class, listIterator::next);

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previous(), is(i.get() % 5 + 1));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
			expecting(NoSuchElementException.class, listIterator::previous);
		});
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Integer> iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is(i % 5 + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));
		expecting(NoSuchElementException.class, iterator::next);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		ListIterator<Integer> listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.next(), is(i % 5 + 1));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			i++;
		}
		assertThat(i, is(10));
		expecting(NoSuchElementException.class, listIterator::next);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemoveBackwards() {
		int i = 10;
		ListIterator<Integer> listIterator = list.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previous(), is(i % 5 + 1));
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
		}
		assertThat(i, is(0));
		expecting(NoSuchElementException.class, listIterator::previous);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void iteratorFailFast() {
		Iterator<Integer> it1 = list.iterator();
		list.add(17);
		expecting(ConcurrentModificationException.class, it1::next);

		Iterator<Integer> it2 = list.iterator();
		list.remove((Integer) 17);
		expecting(ConcurrentModificationException.class, it2::next);
	}

	@Test
	public void subList() {
		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(-1, 2));
		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(2, 11));

		List<Integer> subList = list.subList(2, 8);
		assertThat(subList.size(), is(6));
		twice(() -> assertThat(subList, contains(3, 4, 5, 1, 2, 3)));

		assertThat(subList.remove(1), is(4));
		twice(() -> assertThat(subList, contains(3, 5, 1, 2, 3)));
		twice(() -> assertThat(list, contains(1, 2, 3, 5, 1, 2, 3, 4, 5)));

		assertThat(subList.remove((Integer) 5), is(true));
		twice(() -> assertThat(subList, contains(3, 1, 2, 3)));
		twice(() -> assertThat(list, contains(1, 2, 3, 1, 2, 3, 4, 5)));

		Iterator<Integer> subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.next(), is(3));
		subListIterator.remove();
		twice(() -> assertThat(subList, contains(1, 2, 3)));
		twice(() -> assertThat(list, contains(1, 2, 1, 2, 3, 4, 5)));

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, contains(1, 3)));
		twice(() -> assertThat(list, contains(1, 2, 1, 3, 4, 5)));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, contains(1, 2, 4, 5)));

		expecting(UnsupportedOperationException.class, () -> subList.add(17));
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, contains(1, 2, 4, 5)));
	}

	@Test
	public void sort() {
		Lists.reverse(list);
		expecting(UnsupportedOperationException.class, () -> list.sort(naturalOrder()));
		assertThat(list, contains(5, 4, 3, 2, 1, 5, 4, 3, 2, 1));
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
		assertThat(list, is(not(equalTo(new TreeSet<>(asList(1, 2, 3, 4, 5))))));
		assertThat(list, is(not(equalTo(new ArrayList<>(asList(1, 2, 3, 4))))));

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
		assertThat(list, is(not(equalTo(IntList.create(1, 2, 3, 4)))));

		List<Integer> list2 = IntList.create(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 17);
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
	public void lastIndexOf() {
		assertThat(empty.lastIndexOf(17), is(-1));

		assertThat(list.lastIndexOf(17), is(-1));
		assertThat(list.lastIndexOf(2), is(6));
	}

	@Test
	public void indexOf() {
		assertThat(empty.indexOf(17), is(-1));

		assertThat(list.indexOf(17), is(-1));
		assertThat(list.indexOf(2), is(1));
	}

	@Test
	public void get() {
		expecting(IndexOutOfBoundsException.class, () -> empty.get(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.get(2), is(3));
	}

	@Test
	public void set() {
		expecting(IndexOutOfBoundsException.class, () -> empty.set(2, 17));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.set(2, 17), is(3));
		assertThat(list, contains(1, 2, 17, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void add() {
		assertThat(empty.add(17), is(true));
		assertThat(empty, contains(17));

		assertThat(list.add(17), is(true));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 17));
	}

	@Test
	public void addAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.add(2, 17));
		assertThat(empty, is(emptyIterable()));

		empty.add(0, 17);
		assertThat(empty, contains(17));

		list.add(2, 17);
		assertThat(list, contains(1, 2, 17, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addAllIntsIntCollection() {
		assertThat(empty.addAll(IntSortedSet.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(IntSortedSet.create(1, 2, 3)), is(true));
		assertThat(empty, contains(1, 2, 3));

		assertThat(list.addAll(IntSortedSet.create(6, 7, 8)), is(true));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllIntsArrayIntList() {
		assertThat(empty.addAll(IntList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(IntList.create(1, 2, 3)), is(true));
		assertThat(empty, contains(1, 2, 3));

		assertThat(list.addAll(IntList.create(6, 7, 8)), is(true));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllIntsAtIntCollection() {
		assertThat(empty.addAll(0, IntSortedSet.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, IntSortedSet.create(1, 2, 3)), is(true));
		assertThat(empty, contains(1, 2, 3));

		assertThat(list.addAll(2, IntSortedSet.create(17, 18, 19)), is(true));
		assertThat(list, contains(1, 2, 17, 18, 19, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addAllIntsAtArrayIntList() {
		assertThat(empty.addAll(0, IntList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, IntList.create(1, 2, 3)), is(true));
		assertThat(empty, contains(1, 2, 3));

		assertThat(list.addAll(2, IntList.create(17, 18, 19)), is(true));
		assertThat(list, contains(1, 2, 17, 18, 19, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void remove() {
		assertThat(empty.remove((Integer) 17), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove((Integer) 17), is(false));
		assertThat(list.remove((Integer) 2), is(true));
		assertThat(list, contains(1, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.remove(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove(2), is(3));
		assertThat(list, contains(1, 2, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void boxedContains() {
		assertThat(empty.contains(17), is(false));

		assertThat(list.contains(17), is(false));
		assertThat(list.contains(2), is(true));
	}

	@Test
	public void containsAllIntsCollection() {
		assertThat(empty.containsAll(IntList.create(17, 18, 19)), is(false));

		assertThat(list.containsAll(IntList.create(17, 18, 19)), is(false));
		assertThat(list.containsAll(IntList.create(1, 2, 3)), is(true));
	}

	@Test
	public void removeAllIntsCollection() {
		assertThat(empty.removeAll(IntList.create(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(IntList.create(17, 18, 19)), is(false));
		assertThat(list.removeAll(IntList.create(1, 2, 3, 17)), is(true));
		assertThat(list, contains(4, 5, 4, 5));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeIf(x -> x > 5), is(false));
		assertThat(list.removeIf(x -> x > 3), is(true));
		assertThat(list, contains(1, 2, 3, 1, 2, 3));
	}

	@Test
	public void retainAllIntsCollection() {
		assertThat(empty.retainAll(IntList.create(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(IntList.create(1, 2, 3, 17)), is(true));
		assertThat(list, contains(1, 2, 3, 1, 2, 3));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		list.replaceAll(x -> x + 1);
		assertThat(list, contains(2, 3, 4, 5, 6, 2, 3, 4, 5, 6));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger index = new AtomicInteger(0);
		list.forEach(x -> assertThat(x, is(index.getAndIncrement() % 5 + 1)));
		assertThat(index.get(), is(10));
	}
}
