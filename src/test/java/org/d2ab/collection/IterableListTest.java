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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class IterableListTest {
	private final Deque<Integer> originalEmpty = new ArrayDeque<>();
	private final List<Integer> listEmpty = IterableList.from(originalEmpty);

	private final Deque<Integer> original = new ArrayDeque<>(asList(1, 2, 3, 4, 5));
	private final List<Integer> list = IterableList.from(original);

	@Test
	public void size() {
		assertThat(listEmpty.size(), is(0));
		assertThat(list.size(), is(5));
	}

	@Test
	public void isEmpty() {
		assertThat(listEmpty.isEmpty(), is(true));
		assertThat(list.isEmpty(), is(false));
	}

	@Test
	public void containsElement() {
		assertThat(listEmpty.contains(2), is(false));

		for (int i = 1; i <= 5; i += 2)
			assertThat(list.contains(i), is(true));

		assertThat(list.contains(17), is(false));
	}

	@Test
	public void iterator() {
		assertThat(listEmpty, is(emptyIterable()));
		assertThat(list, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void iteratorRemove() {
		Iterator<Integer> iterator = list.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.remove();

		assertThat(list, contains(1, 4, 5));
		assertThat(original, contains(1, 4, 5));
	}

	@Test
	public void toArray() {
		assertThat(listEmpty.toArray(), is(emptyArray()));
		assertThat(list.toArray(), is(arrayContaining(1, 2, 3, 4, 5)));
	}

	@Test
	public void toArrayOfType() {
		assertThat(listEmpty.toArray(new Integer[0]), is(emptyArray()));
		assertThat(list.toArray(new Integer[5]), is(arrayContaining(1, 2, 3, 4, 5)));
	}

	@Test
	public void add() {
		expecting(UnsupportedOperationException.class, () -> listEmpty.add(3));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.add(3));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void remove() {
		assertThat(listEmpty.remove((Integer) 17), is(false));

		assertThat(list.remove((Integer) 2), is(true));
		assertThat(list, contains(1, 3, 4, 5));
		assertThat(original, contains(1, 3, 4, 5));

		assertThat(list.remove((Integer) 17), is(false));
		assertThat(list, contains(1, 3, 4, 5));
		assertThat(original, contains(1, 3, 4, 5));
	}

	@Test
	public void containsAll() {
		assertThat(listEmpty.containsAll(asList(17, 18)), is(false));

		assertThat(list.containsAll(asList(1, 3)), is(true));
		assertThat(list.containsAll(asList(1, 17)), is(false));
	}

	@Test
	public void addAll() {
		expecting(UnsupportedOperationException.class, () -> listEmpty.addAll(asList(1, 3)));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAll(asList(1, 3)));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void addAllAtIndex() {
		expecting(UnsupportedOperationException.class, () -> listEmpty.addAll(0, asList(1, 3)));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAll(0, asList(1, 3)));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void removeAll() {
		assertThat(listEmpty.removeAll(asList(1, 3)), is(false));
		assertThat(listEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		assertThat(list.removeAll(asList()), is(false));
		assertThat(original, contains(1, 2, 3, 4, 5));

		assertThat(list.removeAll(asList(1, 2, 3)), is(true));
		assertThat(list, contains(4, 5));
		assertThat(original, contains(4, 5));
	}

	@Test
	public void retainAll() {
		assertThat(listEmpty.retainAll(asList(1, 2)), is(false));
		assertThat(listEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		assertThat(list.retainAll(asList(1, 2, 3)), is(true));
		assertThat(list, contains(1, 2, 3));
		assertThat(original, contains(1, 2, 3));
	}

	@Test
	public void replaceAll() {
		listEmpty.replaceAll(x -> x + 2);
		assertThat(listEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAll(x -> x + 2));
		assertThat(list, contains(1, 2, 3, 4, 5));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void sort() {
		listEmpty.sort(Comparator.naturalOrder());
		assertThat(listEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.sort(Comparator.naturalOrder()));
		assertThat(list, contains(1, 2, 3, 4, 5));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void clear() {
		listEmpty.clear();
		assertThat(listEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		list.clear();
		assertThat(list, is(emptyIterable()));
		assertThat(original, is(emptyIterable()));
	}

	@Test
	public void testEquals() {
		assertThat(listEmpty.equals(asList()), is(true));
		assertThat(listEmpty.equals(asList(1, 3)), is(false));

		assertThat(list.equals(asList(1, 2, 3, 4, 5)), is(true));
		assertThat(list.equals(asList(1, 17, 3, 4, 5)), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(listEmpty.hashCode(), is(1));

		assertThat(list.hashCode(), is(29615266));
	}

	@Test
	public void get() {
		assertThat(list.get(0), is(1));
		assertThat(list.get(2), is(3));
		assertThat(list.get(4), is(5));
	}

	@Test
	public void set() {
		expecting(UnsupportedOperationException.class, () -> list.set(2, 17));
		assertThat(list, contains(1, 2, 3, 4, 5));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void addAtIndex() {
		expecting(UnsupportedOperationException.class, () -> listEmpty.add(0, 3));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.add(0, 3));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void indexOf() {
		assertThat(listEmpty.indexOf(17), is(-1));

		assertThat(list.indexOf(3), is(2));
	}

	@Test
	public void lastIndexOf() {
		assertThat(listEmpty.lastIndexOf(17), is(-1));

		original.add(3);
		assertThat(list.lastIndexOf(3), is(5));
		assertThat(list.lastIndexOf(17), is(-1));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Integer> emptyIterator = listEmpty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(UnsupportedOperationException.class, () -> emptyIterator.add(17));
		expecting(UnsupportedOperationException.class, () -> emptyIterator.set(17));

		assertThat(listEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		ListIterator<Integer> listIterator = list.listIterator();

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

		expecting(UnsupportedOperationException.class, () -> listIterator.set(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.next(), is(2));

		expecting(UnsupportedOperationException.class, () -> listIterator.add(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.next(), is(3));

		assertThat(list, contains(1, 2, 3, 4, 5));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		ListIterator<Integer> listIterator = list.listIterator();

		AtomicInteger i = new AtomicInteger();
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is(i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previous(), is(i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
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

		assertThat(list, is(emptyIterable()));
		assertThat(original, is(emptyIterable()));
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

		assertThat(list, is(emptyIterable()));
		assertThat(original, is(emptyIterable()));
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
			expecting(IllegalStateException.class, listIterator::remove);
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
		}
		assertThat(i, is(0));

		assertThat(list, contains(1, 2, 3, 4, 5));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void subList() {
		List<Integer> emptySubList = listEmpty.subList(0, 0);
		assertThat(emptySubList, is(emptyIterable()));

		List<Integer> subList = list.subList(2, 4);
		assertThat(subList, contains(3, 4));
	}

	@Test
	public void stream() {
		assertThat(listEmpty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.stream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5));
	}

	@Test
	public void parallelStream() {
		assertThat(listEmpty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.parallelStream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5));
	}

	@Test
	public void removeIf() {
		listEmpty.removeIf(x -> x == 1 || x == 2);
		assertThat(listEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		list.removeIf(x -> x == 1 || x == 2);
		assertThat(list, contains(3, 4, 5));
		assertThat(original, contains(3, 4, 5));
	}

	@Test
	public void forEach() {
		listEmpty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		list.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(6));
	}
}