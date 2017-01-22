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

package org.d2ab.sequence;

import org.d2ab.collection.Lists;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ChainedListSequenceAsListTest {
	private final ArrayList<Integer> emptyBackingList1 = new ArrayList<>();
	private final ArrayList<Integer> emptyBackingList2 = new ArrayList<>();
	private final Sequence<Integer> empty = ListSequence.concat(emptyBackingList1, emptyBackingList2);
	private final List<Integer> emptyList = empty.asList();

	private final List<Integer> backingList1 = Lists.create(1, 2, 3, 4, 5);
	private final List<Integer> backingList2 = Lists.create(1, 2, 3, 4, 5);
	private final Sequence<Integer> sequence = ListSequence.concat(backingList1, backingList2);
	private final List<Integer> list = sequence.asList();

	@Test
	public void subList() {
		List<Integer> subList = list.subList(2, 8);
		twice(() -> assertThat(subList, contains(3, 4, 5, 1, 2, 3)));

		assertThat(subList.remove(1), is(4));
		twice(() -> assertThat(subList, contains(3, 5, 1, 2, 3)));
		twice(() -> assertThat(sequence, contains(1, 2, 3, 5, 1, 2, 3, 4, 5)));

		assertThat(subList.remove((Integer) 5), is(true));
		twice(() -> assertThat(subList, contains(3, 1, 2, 3)));
		twice(() -> assertThat(sequence, contains(1, 2, 3, 1, 2, 3, 4, 5)));

		Iterator<Integer> subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.next(), is(3));
		subListIterator.remove();
		twice(() -> assertThat(subList, contains(1, 2, 3)));
		twice(() -> assertThat(sequence, contains(1, 2, 1, 2, 3, 4, 5)));

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, contains(1, 3)));
		twice(() -> assertThat(sequence, contains(1, 2, 1, 3, 4, 5)));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(sequence, contains(1, 2, 4, 5)));
	}

	@Test
	public void size() {
		assertThat(emptyList.size(), is(0));
		assertThat(list.size(), is(10));
	}

	@Test
	public void isEmpty() {
		assertThat(emptyList.isEmpty(), is(true));
		assertThat(list.isEmpty(), is(false));
	}

	@Test
	public void containsElement() {
		assertThat(emptyList.contains(2), is(false));
		for (int i = 1; i < 5; i++)
			assertThat(list.contains(i), is(true));
		assertThat(list.contains(17), is(false));
	}

	@Test
	public void iterator() {
		assertThat(emptyList, is(emptyIterable()));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void iteratorRemove() {
		Iterator<Integer> iterator = list.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.remove();

		assertThat(list, contains(1, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 4, 5, 1, 2, 3, 4, 5));
		assertThat(backingList1, contains(1, 4, 5));
		assertThat(backingList2, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void toArray() {
		assertThat(emptyList.toArray(), is(emptyArray()));
		assertThat(list.toArray(), is(arrayContaining(1, 2, 3, 4, 5, 1, 2, 3, 4, 5)));
	}

	@Test
	public void toArrayOfType() {
		assertThat(emptyList.toArray(new Integer[0]), is(emptyArray()));
		assertThat(list.toArray(new Integer[5]), is(arrayContaining(1, 2, 3, 4, 5, 1, 2, 3, 4, 5)));
	}

	@Test
	public void add() {
		assertThat(emptyList.add(1), is(true));
		assertThat(emptyList.add(2), is(true));
		assertThat(emptyList, contains(1, 2));
		assertThat(empty, contains(1, 2));
		assertThat(emptyBackingList1, contains(1, 2));
		assertThat(emptyBackingList2, is(emptyIterable()));

		assertThat(list.add(6), is(true));
		assertThat(list.add(7), is(true));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6, 7));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6, 7));
		assertThat(backingList1, contains(1, 2, 3, 4, 5));
		assertThat(backingList2, contains(1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void remove() {
		assertThat(emptyList.remove((Integer) 17), is(false));

		assertThat(list.remove((Integer) 2), is(true));
		assertThat(list, contains(1, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(backingList1, contains(1, 3, 4, 5));
		assertThat(backingList2, contains(1, 2, 3, 4, 5));

		assertThat(list.remove((Integer) 17), is(false));
		assertThat(list, contains(1, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(backingList1, contains(1, 3, 4, 5));
		assertThat(backingList2, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void containsAll() {
		assertThat(emptyList.containsAll(Lists.of(2, 3)), is(false));

		assertThat(list.containsAll(Lists.of(2, 3)), is(true));
		assertThat(list.containsAll(Lists.of(2, 17)), is(false));
	}

	@Test
	public void addAll() {
		assertThat(emptyList.addAll(Lists.of()), is(false));
		assertThat(emptyList, is(emptyIterable()));
		assertThat(emptyBackingList1, is(emptyIterable()));
		assertThat(emptyBackingList2, is(emptyIterable()));

		assertThat(emptyList.addAll(Lists.of(1, 2)), is(true));
		assertThat(emptyList, contains(1, 2));
		assertThat(empty, contains(1, 2));
		assertThat(emptyBackingList1, contains(1, 2));
		assertThat(emptyBackingList2, is(emptyIterable()));

		assertThat(list.addAll(Lists.of(6, 7, 8)), is(true));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6, 7, 8));
		assertThat(backingList1, contains(1, 2, 3, 4, 5));
		assertThat(backingList2, contains(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllAtIndex() {
		assertThat(emptyList.addAll(0, Lists.of()), is(false));
		assertThat(emptyList, is(emptyIterable()));
		assertThat(emptyBackingList1, is(emptyIterable()));
		assertThat(emptyBackingList2, is(emptyIterable()));

		assertThat(emptyList.addAll(0, Lists.of(1, 2)), is(true));
		assertThat(emptyList, contains(1, 2));
		assertThat(emptyBackingList1, contains(1, 2));
		assertThat(emptyBackingList2, is(emptyIterable()));

		assertThat(list.addAll(2, Lists.of(17, 18, 19)), is(true));
		assertThat(list, contains(1, 2, 17, 18, 19, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 2, 17, 18, 19, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(backingList1, contains(1, 2, 17, 18, 19, 3, 4, 5));
		assertThat(backingList2, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void removeAll() {
		assertThat(emptyList.removeAll(Lists.of(1, 2)), is(false));
		assertThat(emptyList, is(emptyIterable()));
		assertThat(emptyBackingList1, is(emptyIterable()));
		assertThat(emptyBackingList2, is(emptyIterable()));

		assertThat(list.removeAll(Lists.of(1, 2, 5)), is(true));
		assertThat(list, contains(3, 4, 3, 4));
		assertThat(sequence, contains(3, 4, 3, 4));
		assertThat(backingList1, contains(3, 4));
		assertThat(backingList2, contains(3, 4));
	}

	@Test
	public void retainAll() {
		assertThat(emptyList.retainAll(Lists.of(1, 2)), is(false));
		assertThat(emptyList, is(emptyIterable()));
		assertThat(emptyBackingList1, is(emptyIterable()));
		assertThat(emptyBackingList2, is(emptyIterable()));

		assertThat(list.retainAll(Lists.of(1, 2, 3)), is(true));
		assertThat(list, contains(1, 2, 3, 1, 2, 3));
		assertThat(sequence, contains(1, 2, 3, 1, 2, 3));
		assertThat(backingList1, contains(1, 2, 3));
		assertThat(backingList2, contains(1, 2, 3));
	}

	@Test
	public void replaceAll() {
		emptyList.replaceAll(x -> x + 1);
		assertThat(emptyList, is(emptyIterable()));
		assertThat(emptyBackingList1, is(emptyIterable()));
		assertThat(emptyBackingList2, is(emptyIterable()));

		list.replaceAll(x -> x + 1);
		assertThat(list, contains(2, 3, 4, 5, 6, 2, 3, 4, 5, 6));
		assertThat(sequence, contains(2, 3, 4, 5, 6, 2, 3, 4, 5, 6));
		assertThat(backingList1, contains(2, 3, 4, 5, 6));
		assertThat(backingList2, contains(2, 3, 4, 5, 6));
	}

	@Test
	public void sort() {
		emptyList.sort(Comparator.naturalOrder());
		assertThat(emptyList, is(emptyIterable()));
		assertThat(emptyBackingList1, is(emptyIterable()));
		assertThat(emptyBackingList2, is(emptyIterable()));

		list.sort(Comparator.naturalOrder());
		assertThat(list, contains(1, 1, 2, 2, 3, 3, 4, 4, 5, 5));
		assertThat(sequence, contains(1, 1, 2, 2, 3, 3, 4, 4, 5, 5));
		assertThat(backingList1, contains(1, 1, 2, 2, 3));
		assertThat(backingList2, contains(3, 4, 4, 5, 5));
	}

	@Test
	public void clear() {
		emptyList.clear();
		assertThat(emptyList, is(emptyIterable()));
		assertThat(emptyBackingList1, is(emptyIterable()));
		assertThat(emptyBackingList2, is(emptyIterable()));

		list.clear();
		assertThat(list, is(emptyIterable()));
		assertThat(sequence, is(emptyIterable()));
		assertThat(backingList1, is(emptyIterable()));
		assertThat(backingList2, is(emptyIterable()));
	}

	@Test
	public void testEquals() {
		assertThat(emptyList.equals(Lists.of()), is(true));
		assertThat(emptyList.equals(Lists.of(1, 2)), is(false));

		assertThat(list.equals(Lists.of(1, 2, 3, 4, 5, 1, 2, 3, 4, 5)), is(true));
		assertThat(list.equals(Lists.of(5, 4, 3, 2, 1, 5, 4, 3, 2, 1)), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(emptyList.hashCode(), is(1));
		assertThat(list.hashCode(), is(-980763487));
	}

	@Test
	public void get() {
		assertThat(list.get(0), is(1));
		assertThat(list.get(2), is(3));
		assertThat(list.get(4), is(5));
		assertThat(list.get(5), is(1));
		assertThat(list.get(7), is(3));
		assertThat(list.get(9), is(5));
	}

	@Test
	public void set() {
		list.set(2, 17);
		assertThat(list, contains(1, 2, 17, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 2, 17, 4, 5, 1, 2, 3, 4, 5));
		assertThat(backingList1, contains(1, 2, 17, 4, 5));
		assertThat(backingList2, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void addAtIndex() {
		list.add(0, 17);
		assertThat(list, contains(17, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(17, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(backingList1, contains(17, 1, 2, 3, 4, 5));
		assertThat(backingList2, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void indexOf() {
		assertThat(emptyList.indexOf(17), is(-1));

		assertThat(list.indexOf(1), is(0));
		assertThat(list.indexOf(3), is(2));
		assertThat(list.indexOf(5), is(4));
	}

	@Test
	public void lastIndexOf() {
		assertThat(emptyList.lastIndexOf(17), is(-1));

		assertThat(list.lastIndexOf(1), is(5));
		assertThat(list.lastIndexOf(3), is(7));
		assertThat(list.lastIndexOf(5), is(9));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Integer> emptyIterator = emptyList.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		emptyIterator.add(17);
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(true));
		assertThat(emptyIterator.nextIndex(), is(1));
		assertThat(emptyIterator.previousIndex(), is(0));

		assertThat(emptyList, contains(17));
		assertThat(empty, contains(17));
	}

	@Test
	public void listIterator() {
		ListIterator<Integer> listIterator = list.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));
		assertThat(listIterator.next(), is(1));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.next(), is(2));

		listIterator.add(17);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.next(), is(3));

		listIterator.set(18);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));
		assertThat(listIterator.next(), is(4));

		assertThat(list, contains(1, 2, 17, 18, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 2, 17, 18, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		ListIterator<Integer> listIterator = list.listIterator();

		AtomicInteger i = new AtomicInteger(0);
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is(i.get() % 5 + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
		});
	}

	@Test
	public void listIteratorRemoveAll() {
		Iterator<Integer> iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is(i % 5 + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));

		assertThat(list, is(emptyIterable()));
		assertThat(sequence, is(emptyIterable()));
		assertThat(backingList1, is(emptyIterable()));
		assertThat(backingList2, is(emptyIterable()));
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

		assertThat(list, is(emptyIterable()));
		assertThat(sequence, is(emptyIterable()));
		assertThat(backingList1, is(emptyIterable()));
		assertThat(backingList2, is(emptyIterable()));
	}

	@Test
	public void stream() {
		assertThat(emptyList.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.stream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void parallelStream() {
		assertThat(emptyList.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.parallelStream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeIf() {
		emptyList.removeIf(x -> x == 1);
		assertThat(emptyList, is(emptyIterable()));
		assertThat(emptyBackingList1, is(emptyIterable()));
		assertThat(emptyBackingList2, is(emptyIterable()));

		list.removeIf(x -> x == 1);
		assertThat(list, contains(2, 3, 4, 5, 2, 3, 4, 5));
		assertThat(backingList1, contains(2, 3, 4, 5));
		assertThat(backingList2, contains(2, 3, 4, 5));
	}

	@Test
	public void forEach() {
		emptyList.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger value = new AtomicInteger(0);
		list.forEach(x -> assertThat(x, is(value.getAndIncrement() % 5 + 1)));
		assertThat(value.get(), is(10));
	}
}
