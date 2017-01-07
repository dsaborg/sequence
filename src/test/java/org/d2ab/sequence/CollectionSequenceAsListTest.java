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

import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CollectionSequenceAsListTest {
	private final Sequence<Integer> empty = CollectionSequence.from(new ArrayDeque<>());
	private final List<Integer> emptyList = empty.asList();

	private final Sequence<Integer> sequence = CollectionSequence.from(
			new ArrayDeque<>(Arrays.asList(1, 2, 3, 4, 5, 1, 2, 3, 4, 5)));
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
		expecting(UnsupportedOperationException.class, () -> emptyList.add(1));
		assertThat(emptyList, is(emptyIterable()));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.add(6));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void remove() {
		assertThat(emptyList.remove((Integer) 17), is(false));

		assertThat(list.remove((Integer) 2), is(true));
		assertThat(list, contains(1, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 3, 4, 5, 1, 2, 3, 4, 5));

		assertThat(list.remove((Integer) 17), is(false));
		assertThat(list, contains(1, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void containsAll() {
		assertThat(emptyList.containsAll(asList(2, 3)), is(false));

		assertThat(list.containsAll(asList(2, 3)), is(true));
		assertThat(list.containsAll(asList(2, 17)), is(false));
	}

	@Test
	public void addAll() {
		emptyList.addAll(emptyList());
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> emptyList.addAll(asList(1, 2)));
		assertThat(emptyList, is(emptyIterable()));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAll(asList(6, 7, 8)));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addAllAtIndex() {
		assertThat(emptyList.addAll(0, emptyList()), is(false));
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> emptyList.addAll(0, asList(1, 2)));
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAll(2, asList(17, 18, 19)));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeAll() {
		assertThat(emptyList.removeAll(asList(1, 2)), is(false));
		assertThat(emptyList, is(emptyIterable()));

		assertThat(list.removeAll(asList(1, 2, 5)), is(true));
		assertThat(list, contains(3, 4, 3, 4));
		assertThat(sequence, contains(3, 4, 3, 4));
	}

	@Test
	public void retainAll() {
		assertThat(emptyList.retainAll(asList(1, 2)), is(false));
		assertThat(emptyList, is(emptyIterable()));

		assertThat(list.retainAll(asList(1, 2, 3)), is(true));
		assertThat(list, contains(1, 2, 3, 1, 2, 3));
		assertThat(sequence, contains(1, 2, 3, 1, 2, 3));
	}

	@Test
	public void replaceAll() {
		emptyList.replaceAll(x -> x + 1);
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAll(x -> x + 1));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void sort() {
		emptyList.sort(Comparator.naturalOrder());
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.sort(Comparator.naturalOrder()));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void clear() {
		emptyList.clear();
		assertThat(emptyList, is(emptyIterable()));

		list.clear();
		assertThat(list, is(emptyIterable()));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void testEquals() {
		assertThat(emptyList.equals(emptyList()), is(true));
		assertThat(emptyList.equals(asList(1, 2)), is(false));

		assertThat(list.equals(asList(1, 2, 3, 4, 5, 1, 2, 3, 4, 5)), is(true));
		assertThat(list.equals(asList(5, 4, 3, 2, 1, 5, 4, 3, 2, 1)), is(false));
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
		expecting(UnsupportedOperationException.class, () -> list.set(2, 17));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addAtIndex() {
		expecting(UnsupportedOperationException.class, () -> list.add(0, 17));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
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
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(UnsupportedOperationException.class, () -> emptyIterator.add(17));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(emptyList, is(emptyIterable()));
		assertThat(empty, is(emptyIterable()));
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

		expecting(UnsupportedOperationException.class, () -> listIterator.add(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.next(), is(3));

		expecting(UnsupportedOperationException.class, () -> listIterator.set(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.next(), is(4));

		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
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

		list.removeIf(x -> x == 1);
		assertThat(list, contains(2, 3, 4, 5, 2, 3, 4, 5));
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
