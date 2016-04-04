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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ReverseListTest {
	private final List<Integer> originalEmpty = new ArrayList<>();
	private final List<Integer> reverseEmpty = ReverseList.from(originalEmpty);

	private final List<Integer> original = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
	private final List<Integer> reverse = ReverseList.from(original);

	@Test
	public void size() {
		assertThat(reverseEmpty.size(), is(0));
		assertThat(reverse.size(), is(6));
	}

	@Test
	public void isEmpty() {
		assertThat(reverseEmpty.isEmpty(), is(true));
		assertThat(reverse.isEmpty(), is(false));
	}

	@Test
	public void containsElement() {
		assertThat(reverseEmpty.contains(2), is(false));
		for (int i = 1; i < 6; i++)
			assertThat(reverse.contains(i), is(true));
		assertThat(reverse.contains(7), is(false));
	}

	@Test
	public void iterator() {
		assertThat(reverseEmpty, is(emptyIterable()));
		assertThat(reverse, contains(6, 5, 4, 3, 2, 1));
	}

	@Test
	public void iteratorRemove() {
		Iterator<Integer> iterator = reverse.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.remove();

		assertThat(reverse, contains(6, 3, 2, 1));
		assertThat(original, contains(1, 2, 3, 6));
	}

	@Test
	public void toArray() {
		assertThat(reverseEmpty.toArray(), is(emptyArray()));
		assertThat(reverse.toArray(), is(arrayContaining(6, 5, 4, 3, 2, 1)));
	}

	@Test
	public void toArrayOfType() {
		assertThat(reverseEmpty.toArray(new Integer[0]), is(emptyArray()));
		assertThat(reverse.toArray(new Integer[6]), is(arrayContaining(6, 5, 4, 3, 2, 1)));
	}

	@Test
	public void add() {
		assertThat(reverseEmpty.add(1), is(true));
		assertThat(reverseEmpty.add(2), is(true));
		assertThat(reverseEmpty, contains(1, 2));
		assertThat(originalEmpty, contains(2, 1));

		assertThat(reverse.add(0), is(true));
		assertThat(reverse.add(-1), is(true));
		assertThat(reverse, contains(6, 5, 4, 3, 2, 1, 0, -1));
		assertThat(original, contains(-1, 0, 1, 2, 3, 4, 5, 6));
	}

	@Test
	public void remove() {
		assertThat(reverseEmpty.remove((Integer) 17), is(false));

		original.add(5, 2);

		assertThat(reverse.remove((Integer) 2), is(true));
		assertThat(reverse, contains(6, 5, 4, 3, 2, 1));
		assertThat(original, contains(1, 2, 3, 4, 5, 6));

		assertThat(reverse.remove((Integer) 17), is(false));
		assertThat(reverse, contains(6, 5, 4, 3, 2, 1));
		assertThat(original, contains(1, 2, 3, 4, 5, 6));
	}

	@Test
	public void containsAll() {
		assertThat(reverseEmpty.containsAll(List.of(2, 3)), is(false));

		assertThat(reverse.containsAll(List.of(2, 3)), is(true));
		assertThat(reverse.containsAll(List.of(2, 17)), is(false));
	}

	@Test
	public void addAll() {
		assertThat(reverseEmpty.addAll(List.of()), is(false));
		assertThat(reverseEmpty, is(emptyIterable()));

		assertThat(reverseEmpty.addAll(List.of(1, 2)), is(true));
		assertThat(reverseEmpty, contains(1, 2));
		assertThat(originalEmpty, contains(2, 1));

		assertThat(reverse.addAll(List.of(0, -1, -2)), is(true));
		assertThat(reverse, contains(6, 5, 4, 3, 2, 1, 0, -1, -2));
		assertThat(original, contains(-2, -1, 0, 1, 2, 3, 4, 5, 6));
	}

	@Test
	public void addAllAtIndex() {
		assertThat(reverseEmpty.addAll(0, List.of()), is(false));
		assertThat(reverseEmpty, is(emptyIterable()));

		assertThat(reverseEmpty.addAll(0, List.of(1, 2)), is(true));
		assertThat(reverseEmpty, contains(1, 2));
		assertThat(originalEmpty, contains(2, 1));

		assertThat(reverse.addAll(2, List.of(17, 18, 19)), is(true));
		assertThat(reverse, contains(6, 5, 17, 18, 19, 4, 3, 2, 1));
		assertThat(original, contains(1, 2, 3, 4, 19, 18, 17, 5, 6));
	}

	@Test
	public void removeAll() {
		assertThat(reverseEmpty.removeAll(List.of(1, 2)), is(false));
		assertThat(reverseEmpty, is(emptyIterable()));

		original.addAll(4, List.of(1, 2));

		assertThat(reverse.removeAll(List.of(1, 2, 5)), is(true));
		assertThat(reverse, contains(6, 4, 3));
		assertThat(original, contains(3, 4, 6));
	}

	@Test
	public void retainAll() {
		assertThat(reverseEmpty.retainAll(List.of(1, 2)), is(false));
		assertThat(reverseEmpty, is(emptyIterable()));

		original.addAll(4, List.of(1, 2));

		assertThat(reverse.retainAll(List.of(1, 2, 3)), is(true));
		assertThat(reverse, contains(2, 1, 3, 2, 1));
		assertThat(original, contains(1, 2, 3, 1, 2));
	}

	@Test
	public void replaceAll() {
		reverseEmpty.replaceAll(x -> x + 1);
		assertThat(reverseEmpty, is(emptyIterable()));

		reverse.replaceAll(x -> x + 1);
		assertThat(reverse, contains(7, 6, 5, 4, 3, 2));
		assertThat(original, contains(2, 3, 4, 5, 6, 7));
	}

	@Test
	public void sort() {
		reverseEmpty.sort(Comparator.naturalOrder());
		assertThat(reverseEmpty, is(emptyIterable()));

		reverse.sort(Comparator.naturalOrder());
		assertThat(reverse, contains(1, 2, 3, 4, 5, 6));
		assertThat(original, contains(6, 5, 4, 3, 2, 1));
	}

	@Test
	public void clear() {
		reverseEmpty.clear();
		assertThat(reverseEmpty, is(emptyIterable()));

		reverse.clear();
		assertThat(reverse, is(emptyIterable()));
		assertThat(original, is(emptyIterable()));
	}

	@Test
	public void testEquals() {
		assertThat(reverseEmpty.equals(List.of()), is(true));
		assertThat(reverseEmpty.equals(List.of(1, 2)), is(false));

		assertThat(reverse.equals(List.of(6, 5, 4, 3, 2, 1)), is(true));
		assertThat(reverse.equals(List.of(1, 2, 3, 4, 5, 6)), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(reverse.hashCode(), is(1064018302));
		assertThat(original.hashCode(), is(918073252));
	}

	@Test
	public void get() {
		assertThat(reverse.get(0), is(6));
		assertThat(reverse.get(2), is(4));
		assertThat(reverse.get(5), is(1));
	}

	@Test
	public void set() {
		reverse.set(2, 17);
		assertThat(reverse, contains(6, 5, 17, 3, 2, 1));
	}

	@Test
	public void addAtIndex() {
		reverse.add(0, 17);
		reverse.add(3, 18);
		reverse.add(8, 19);

		assertThat(reverse, contains(17, 6, 5, 18, 4, 3, 2, 1, 19));
	}

	@Test
	public void indexOf() {
		assertThat(reverseEmpty.indexOf(17), is(-1));

		original.addAll(4, List.of(1, 2));

		assertThat(reverse.indexOf(1), is(3));
	}

	@Test
	public void lastIndexOf() {
		assertThat(reverseEmpty.lastIndexOf(17), is(-1));

		original.addAll(4, List.of(1, 2));

		assertThat(reverse.lastIndexOf(1), is(7));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Integer> emptyIterator = reverseEmpty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		emptyIterator.add(17);
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(true));
		assertThat(emptyIterator.nextIndex(), is(1));
		assertThat(emptyIterator.previousIndex(), is(0));
		assertThat(emptyIterator.previous(), is(17));

		emptyIterator.set(18);
		assertThat(emptyIterator.hasNext(), is(true));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));
		assertThat(emptyIterator.next(), is(18));

		assertThat(reverseEmpty, contains(18));
		assertThat(originalEmpty, contains(18));
	}

	@Test
	public void listIterator() {
		ListIterator<Integer> listIterator = reverse.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));
		assertThat(listIterator.next(), is(6));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.next(), is(5));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.next(), is(4));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.previous(), is(4));

		listIterator.add(17);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.next(), is(4));

		listIterator.set(18);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));
		assertThat(listIterator.next(), is(3));

		assertThat(reverse, contains(6, 5, 17, 18, 3, 2, 1));
		assertThat(original, contains(1, 2, 3, 18, 17, 5, 6));
	}

	@Test
	public void subList() {
		List<Integer> emptySubList = reverseEmpty.subList(0, 0);
		emptySubList.addAll(List.of(1, 2));
		assertThat(reverseEmpty, contains(1, 2));
		assertThat(originalEmpty, contains(2, 1));

		List<Integer> reverseSubList = reverse.subList(2, 4);
		assertThat(reverseSubList, contains(4, 3));

		reverseSubList.addAll(List.of(17, 18));
		assertThat(reverseSubList, contains(4, 3, 17, 18));
		assertThat(reverse, contains(6, 5, 4, 3, 17, 18, 2, 1));
	}

	@Test
	public void stream() {
		assertThat(reverseEmpty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(reverse.stream().collect(Collectors.toList()), contains(6, 5, 4, 3, 2, 1));
	}

	@Test
	public void parallelStream() {
		assertThat(reverseEmpty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(reverse.parallelStream().collect(Collectors.toList()), contains(6, 5, 4, 3, 2, 1));
	}

	@Test
	public void removeIf() {
		reverseEmpty.removeIf(x -> x == 1);
		assertThat(reverseEmpty, is(emptyIterable()));

		original.addAll(4, List.of(1, 2));

		reverse.removeIf(x -> x == 1);
		assertThat(reverse, contains(6, 5, 2, 4, 3, 2));
	}

	@Test
	public void forEach() {
		reverseEmpty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger value = new AtomicInteger(6);
		reverse.forEach(x -> assertThat(x, is(value.getAndDecrement())));
	}
}