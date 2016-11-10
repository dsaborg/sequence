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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class FilteredListTest {
	private final Predicate<Integer> predicate = e -> e % 2 == 1;

	private final List<Integer> originalEmpty = new ArrayList<>();
	private final List<Integer> filteredEmpty = FilteredList.from(originalEmpty, predicate);

	private final List<Integer> original = new ArrayList<>(asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
	private final List<Integer> filtered = FilteredList.from(original, predicate);

	@Test
	public void size() {
		assertThat(filteredEmpty.size(), is(0));
		assertThat(filtered.size(), is(5));
	}

	@Test
	public void isEmpty() {
		assertThat(filteredEmpty.isEmpty(), is(true));
		assertThat(filtered.isEmpty(), is(false));
	}

	@Test
	public void containsElement() {
		assertThat(filteredEmpty.contains(2), is(false));

		for (int i = 1; i <= 9; i += 2)
			assertThat(filtered.contains(i), is(true));

		assertThat(filtered.contains(17), is(false));
	}

	@Test
	public void iterator() {
		assertThat(filteredEmpty, is(emptyIterable()));
		assertThat(filtered, contains(1, 3, 5, 7, 9));
	}

	@Test
	public void iteratorRemove() {
		Iterator<Integer> iterator = filtered.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.remove();

		assertThat(filtered, contains(1, 7, 9));
		assertThat(original, contains(1, 2, 4, 6, 7, 8, 9, 10));
	}

	@Test
	public void toArray() {
		assertThat(filteredEmpty.toArray(), is(emptyArray()));
		assertThat(filtered.toArray(), is(arrayContaining(1, 3, 5, 7, 9)));
	}

	@Test
	public void toArrayOfType() {
		assertThat(filteredEmpty.toArray(new Integer[0]), is(emptyArray()));
		assertThat(filtered.toArray(new Integer[5]), is(arrayContaining(1, 3, 5, 7, 9)));
	}

	@Test
	public void add() {
		expecting(UnsupportedOperationException.class, () -> filteredEmpty.add(3));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> filtered.add(3));
		assertThat(original, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void remove() {
		assertThat(filteredEmpty.remove((Integer) 17), is(false));

		assertThat(filtered.remove((Integer) 2), is(true));
		assertThat(filtered, contains(1, 3, 5, 7, 9));
		assertThat(original, contains(1, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(filtered.remove((Integer) 17), is(false));
		assertThat(filtered, contains(1, 3, 5, 7, 9));
		assertThat(original, contains(1, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void containsAll() {
		assertThat(filteredEmpty.containsAll(asList(17, 18)), is(false));

		assertThat(filtered.containsAll(asList(2, 3)), is(false));
		assertThat(filtered.containsAll(asList(1, 3)), is(true));
		assertThat(filtered.containsAll(asList(1, 17)), is(false));
	}

	@Test
	public void addAll() {
		expecting(UnsupportedOperationException.class, () -> filteredEmpty.addAll(asList(1, 3)));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> filtered.addAll(asList(1, 3)));
		assertThat(original, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void addAllAtIndex() {
		expecting(UnsupportedOperationException.class, () -> filteredEmpty.addAll(0, asList(1, 3)));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> filtered.addAll(0, asList(1, 3)));
		assertThat(original, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void removeAll() {
		assertThat(filteredEmpty.removeAll(asList(1, 3)), is(false));
		assertThat(filteredEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		assertThat(filtered.removeAll(asList()), is(false));
		assertThat(original, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(filtered.removeAll(asList(1, 2, 3, 4, 5)), is(true));
		assertThat(filtered, contains(7, 9));
		assertThat(original, contains(2, 4, 6, 7, 8, 9, 10));
	}

	@Test
	public void retainAll() {
		assertThat(filteredEmpty.retainAll(asList(1, 2)), is(false));
		assertThat(filteredEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		assertThat(filtered.retainAll(asList(1, 2, 3)), is(true));
		assertThat(filtered, contains(1, 3));
		assertThat(original, contains(1, 2, 3, 4, 6, 8, 10));
	}

	@Test
	public void replaceAll() {
		filteredEmpty.replaceAll(x -> x + 2);
		assertThat(filteredEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		filtered.replaceAll(x -> x + 2);
		assertThat(filtered, contains(3, 5, 7, 9, 11));
		assertThat(original, contains(3, 2, 5, 4, 7, 6, 9, 8, 11, 10));
	}

	@Test
	public void sort() {
		filteredEmpty.sort(Comparator.naturalOrder());
		assertThat(filteredEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		filtered.sort(Comparator.naturalOrder());
		assertThat(filtered, contains(1, 3, 5, 7, 9));
		assertThat(original, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		filtered.sort(Comparator.reverseOrder());
		assertThat(filtered, contains(9, 7, 5, 3, 1));
		assertThat(original, contains(9, 2, 7, 4, 5, 6, 3, 8, 1, 10));
	}

	@Test
	public void clear() {
		filteredEmpty.clear();
		assertThat(filteredEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		filtered.clear();
		assertThat(filtered, is(emptyIterable()));
		assertThat(original, contains(2, 4, 6, 8, 10));
	}

	@Test
	public void testEquals() {
		assertThat(filteredEmpty.equals(asList()), is(true));
		assertThat(filteredEmpty.equals(asList(1, 3)), is(false));

		assertThat(filtered.equals(asList(1, 3, 5, 7, 9)), is(true));
		assertThat(filtered.equals(asList(1, 17, 5, 7, 9)), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(filteredEmpty.hashCode(), is(1));

		assertThat(filtered.hashCode(), is(29647076));
	}

	@Test
	public void get() {
		assertThat(filtered.get(0), is(1));
		assertThat(filtered.get(2), is(5));
		assertThat(filtered.get(4), is(9));
	}

	@Test
	public void set() {
		expecting(IllegalArgumentException.class, () -> filtered.set(2, 18));
		assertThat(original, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		filtered.set(2, 17);
		assertThat(filtered, contains(1, 3, 17, 7, 9));
		assertThat(original, contains(1, 2, 3, 4, 17, 6, 7, 8, 9, 10));
	}

	@Test
	public void addAtIndex() {
		expecting(UnsupportedOperationException.class, () -> filteredEmpty.add(0, 3));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> filtered.add(0, 3));
		assertThat(original, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void indexOf() {
		assertThat(filteredEmpty.indexOf(17), is(-1));

		assertThat(filtered.indexOf(3), is(1));
	}

	@Test
	public void lastIndexOf() {
		assertThat(filteredEmpty.lastIndexOf(17), is(-1));

		assertThat(filtered.lastIndexOf(3), is(1));
		assertThat(filtered.lastIndexOf(17), is(-1));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Integer> emptyIterator = filteredEmpty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(UnsupportedOperationException.class, () -> emptyIterator.add(17));

		expecting(IllegalStateException.class, () -> emptyIterator.set(17));

		assertThat(filteredEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		ListIterator<Integer> listIterator = filtered.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));
		assertThat(listIterator.next(), is(1));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.next(), is(3));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.next(), is(5));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.previous(), is(5));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.previous(), is(3));

		listIterator.set(17);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.next(), is(17));

		expecting(UnsupportedOperationException.class, () -> listIterator.add(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.next(), is(5));

		assertThat(filtered, contains(1, 17, 5, 7, 9));
		assertThat(original, contains(1, 2, 17, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void exhaustiveListIterator() {
		ListIterator<Integer> listIterator = filtered.listIterator();

		AtomicInteger i = new AtomicInteger();
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is(i.get() * 2 + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previous(), is(i.get() * 2 + 1));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
		});
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Integer> iterator = filtered.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is(i * 2 + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(5));

		assertThat(filtered, is(emptyIterable()));
		assertThat(original, contains(2, 4, 6, 8, 10));
	}

	@Test
	public void listIteratorRemove() {
		ListIterator<Integer> listIterator = filtered.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.next(), is(i * 2 + 1));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			i++;
		}
		assertThat(i, is(5));

		assertThat(filtered, is(emptyIterable()));
		assertThat(original, contains(2, 4, 6, 8, 10));
	}

	@Test
	public void listIteratorRemoveBackwards() {
		int i = 5;
		ListIterator<Integer> listIterator = filtered.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previous(), is(i * 2 + 1));
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
		}
		assertThat(i, is(0));

		assertThat(filtered, is(emptyIterable()));
		assertThat(original, contains(2, 4, 6, 8, 10));
	}

	@Test
	public void subList() {
		List<Integer> emptySubList = filteredEmpty.subList(0, 0);
		assertThat(emptySubList, is(emptyIterable()));

		List<Integer> filteredSubList = filtered.subList(2, 4);
		assertThat(filteredSubList, contains(5, 7));
	}

	@Test
	public void stream() {
		assertThat(filteredEmpty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(filtered.stream().collect(Collectors.toList()), contains(1, 3, 5, 7, 9));
	}

	@Test
	public void parallelStream() {
		assertThat(filteredEmpty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(filtered.parallelStream().collect(Collectors.toList()), contains(1, 3, 5, 7, 9));
	}

	@Test
	public void removeIf() {
		filteredEmpty.removeIf(x -> x == 1 || x == 2);
		assertThat(filteredEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		filtered.removeIf(x -> x == 1 || x == 2);
		assertThat(filtered, contains(3, 5, 7, 9));
		assertThat(original, contains(2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void forEach() {
		filteredEmpty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		filtered.forEach(x -> assertThat(x, is(value.getAndAdd(2))));
		assertThat(value.get(), is(11));
	}
}