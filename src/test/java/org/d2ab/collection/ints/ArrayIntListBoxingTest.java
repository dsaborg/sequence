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
import static java.util.Collections.emptyList;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ArrayIntListBoxingTest extends BaseBoxingTest {
	private final List<Integer> empty = ArrayIntList.create();
	private final List<Integer> list = ArrayIntList.create(1, 2, 3, 4, 5);

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
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Integer> emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
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
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(5));
		assertThat(listIterator.previousIndex(), is(4));

		assertThat(list, contains(1, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		twice(() -> {
			ListIterator<Integer> listIterator = list.listIterator();
			AtomicInteger i = new AtomicInteger();
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is(i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));
			expecting(NoSuchElementException.class, listIterator::next);

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previous(), is(i.get() + 1));
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
			assertThat(iterator.next(), is(i + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(5));
		expecting(NoSuchElementException.class, iterator::next);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		ListIterator<Integer> listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.next(), is(i + 1));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			i++;
		}
		assertThat(i, is(5));
		expecting(NoSuchElementException.class, listIterator::next);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemoveBackwards() {
		int i = 5;
		ListIterator<Integer> listIterator = list.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previous(), is(i + 1));
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
		List<Integer> list = IntList.create(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(-1, 2));
		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(2, 11));

		List<Integer> subList = list.subList(2, 8);
		twice(() -> assertThat(subList, contains(3, 4, 5, 6, 7, 8)));

		assertThat(subList.remove(1), is(4));
		twice(() -> assertThat(subList, contains(3, 5, 6, 7, 8)));
		twice(() -> assertThat(list, contains(1, 2, 3, 5, 6, 7, 8, 9, 10)));

		assertThat(subList.remove((Integer) 5), is(true));
		twice(() -> assertThat(subList, contains(3, 6, 7, 8)));
		twice(() -> assertThat(list, contains(1, 2, 3, 6, 7, 8, 9, 10)));

		Iterator<Integer> subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.next(), is(3));
		subListIterator.remove();
		twice(() -> assertThat(subList, contains(6, 7, 8)));
		twice(() -> assertThat(list, contains(1, 2, 6, 7, 8, 9, 10)));

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, contains(7)));
		twice(() -> assertThat(list, contains(1, 2, 7, 9, 10)));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, contains(1, 2, 9, 10)));

		subList.add(17);
		twice(() -> assertThat(subList, contains(17)));
		twice(() -> assertThat(list, contains(1, 2, 17, 9, 10)));
	}

	@Test
	public void sort() {
		List<Integer> list = ArrayIntList.create(32, 17, 5, 7, 19, 22);
		expecting(UnsupportedOperationException.class, () -> list.sort(naturalOrder()));
		assertThat(list, contains(32, 17, 5, 7, 19, 22));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(toList()), is(emptyIterable()));
		assertThat(list.stream().collect(toList()), contains(1, 2, 3, 4, 5));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(toList()), is(emptyIterable()));
		assertThat(list.parallelStream().collect(toList()), contains(1, 2, 3, 4, 5));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(list.toString(), is("[1, 2, 3, 4, 5]"));
	}

	@Test
	public void testEqualsHashCodeAgainstList() {
		List<Integer> list2 = new ArrayList<>(asList(1, 2, 3, 4, 5, 17));
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
		List<Integer> list2 = IntList.create(1, 2, 3, 4, 5, 17);
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
		assertThat(list.lastIndexOf(2), is(1));
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
		assertThat(list, contains(1, 2, 17, 4, 5));
	}

	@Test
	public void add() {
		assertThat(empty.add(17), is(true));
		assertThat(empty, contains(17));

		assertThat(list.add(17), is(true));
		assertThat(list, contains(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void addAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.add(-1, 17));
		expecting(IndexOutOfBoundsException.class, () -> empty.add(1, 17));
		expecting(IndexOutOfBoundsException.class, () -> empty.add(2, 17));
		assertThat(empty, is(emptyIterable()));

		empty.add(0, 17);
		assertThat(empty, contains(17));

		expecting(IndexOutOfBoundsException.class, () -> list.add(-1, 17));
		expecting(IndexOutOfBoundsException.class, () -> list.add(6, 17));
		expecting(IndexOutOfBoundsException.class, () -> list.add(7, 17));
		assertThat(list, contains(1, 2, 3, 4, 5));

		list.add(0, 16);
		assertThat(list, contains(16, 1, 2, 3, 4, 5));

		list.add(3, 17);
		assertThat(list, contains(16, 1, 2, 17, 3, 4, 5));

		list.add(7, 18);
		assertThat(list, contains(16, 1, 2, 17, 3, 4, 5, 18));
	}

	@Test
	public void addAll() {
		assertThat(empty.addAll(emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(Arrays.asList(1, 2, 3)), is(true));
		assertThat(empty, contains(1, 2, 3));

		assertThat(list.addAll(Arrays.asList(6, 7, 8)), is(true));
		assertThat(list, contains(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllArrayIntList() {
		assertThat(empty.addAll(ArrayIntList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(ArrayIntList.create(1, 2, 3)), is(true));
		assertThat(empty, contains(1, 2, 3));

		assertThat(list.addAll(ArrayIntList.create(6, 7, 8)), is(true));
		assertThat(list, contains(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllIntCollection() {
		assertThat(empty.addAll(new BitIntSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(new BitIntSet(1, 2, 3)), is(true));
		assertThat(empty, contains(1, 2, 3));

		assertThat(list.addAll(new BitIntSet(6, 7, 8)), is(true));
		assertThat(list, contains(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllAt() {
		assertThat(empty.addAll(0, emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, Arrays.asList(1, 2, 3)), is(true));
		assertThat(empty, contains(1, 2, 3));

		assertThat(list.addAll(2, Arrays.asList(17, 18, 19)), is(true));
		assertThat(list, contains(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void addAllAtArrayIntList() {
		assertThat(empty.addAll(0, ArrayIntList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, ArrayIntList.create(1, 2, 3)), is(true));
		assertThat(empty, contains(1, 2, 3));

		assertThat(list.addAll(2, ArrayIntList.create(17, 18, 19)), is(true));
		assertThat(list, contains(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void addAllAtIntCollection() {
		assertThat(empty.addAll(0, new BitIntSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, new BitIntSet(1, 2, 3)), is(true));
		assertThat(empty, contains(1, 2, 3));

		assertThat(list.addAll(2, new BitIntSet(17, 18, 19)), is(true));
		assertThat(list, contains(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void remove() {
		assertThat(empty.remove((Integer) 17), is(false));
		assertThat(empty.remove(new Object()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove((Integer) 17), is(false));
		assertThat(list.remove(new Object()), is(false));
		assertThat(list.remove((Integer) 2), is(true));
		assertThat(list, contains(1, 3, 4, 5));
	}

	@Test
	public void removeAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.remove(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove(2), is(3));
		assertThat(list, contains(1, 2, 4, 5));
	}

	@Test
	public void testContains() {
		assertThat(empty.contains(17), is(false));
		assertThat(empty.contains(new Object()), is(false));

		assertThat(list.contains(17), is(false));
		assertThat(list.contains(new Object()), is(false));
		assertThat(list.contains(2), is(true));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(Arrays.asList(17, 18, 19, new Object())), is(false));

		assertThat(list.containsAll(Arrays.asList(17, 18, 19, new Object())), is(false));
		assertThat(list.containsAll(Arrays.asList(1, 2, 3)), is(true));
	}

	@Test
	public void containsAllCollection() {
		assertThat(empty.containsAll(ArrayIntList.create(17, 18, 19)), is(false));

		assertThat(list.containsAll(ArrayIntList.create(17, 18, 19)), is(false));
		assertThat(list.containsAll(ArrayIntList.create(1, 2, 3)), is(true));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(Arrays.asList(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(Arrays.asList(17, 18, 19)), is(false));
		assertThat(list.removeAll(Arrays.asList(1, 2, 3, 17)), is(true));
		assertThat(list, contains(4, 5));
	}

	@Test
	public void removeAllCollection() {
		assertThat(empty.removeAll(ArrayIntList.create(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(ArrayIntList.create(17, 18, 19)), is(false));
		assertThat(list.removeAll(ArrayIntList.create(1, 2, 3, 17)), is(true));
		assertThat(list, contains(4, 5));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeIf(x -> x > 5), is(false));
		assertThat(list.removeIf(x -> x > 3), is(true));
		assertThat(list, contains(1, 2, 3));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(Arrays.asList(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(Arrays.asList(1, 2, 3, 17)), is(true));
		assertThat(list, contains(1, 2, 3));
	}

	@Test
	public void retainAllArrayIntList() {
		assertThat(empty.retainAll(ArrayIntList.create(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(ArrayIntList.create(1, 2, 3, 17)), is(true));
		assertThat(list, contains(1, 2, 3));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		list.replaceAll(x -> x + 1);
		assertThat(list, contains(2, 3, 4, 5, 6));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		list.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(6));
	}
}
