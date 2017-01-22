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

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CollectionIntListBoxingTest extends BaseBoxingTest {
	private final List<Integer> empty = CollectionIntList.from(IntList.create());
	private final List<Integer> list = CollectionIntList.from(IntList.create(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));

	@Test
	public void subList() {
		List<Integer> subList = list.subList(2, 8);
		twice(() -> assertThat(subList, contains(3, 4, 5, 1, 2, 3)));

		assertThat(subList.remove(1), is(4));
		twice(() -> assertThat(subList, contains(3, 5, 1, 2, 3)));

		assertThat(subList.remove((Integer) 5), is(true));
		twice(() -> assertThat(subList, contains(3, 1, 2, 3)));

		Iterator<Integer> subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.next(), is(3));
		subListIterator.remove();
		twice(() -> assertThat(subList, contains(1, 2, 3)));

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, contains(1, 3)));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
	}

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
	public void boxedContains() {
		assertThat(empty.contains(2), is(false));
		for (int i = 1; i < 5; i++)
			assertThat(list.contains(i), is(true));
		assertThat(list.contains(17), is(false));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
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
	}

	@Test
	public void add() {
		assertThat(empty.add(1), is(true));
		assertThat(empty, contains(1));

		assertThat(list.add(6), is(true));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6));
	}

	@Test
	public void remove() {
		assertThat(empty.remove((Integer) 17), is(false));

		assertThat(list.remove((Integer) 2), is(true));
		assertThat(list, contains(1, 3, 4, 5, 1, 2, 3, 4, 5));

		assertThat(list.remove((Integer) 17), is(false));
		assertThat(list, contains(1, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(IntList.create(2, 3)), is(false));

		assertThat(list.containsAll(IntList.create(2, 3)), is(true));
		assertThat(list.containsAll(IntList.create(2, 17)), is(false));
	}

	@Test
	public void addAll() {
		assertThat(empty.addAll(IntList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(IntList.create(1, 2)), is(true));
		assertThat(empty, contains(1, 2));

		assertThat(list.addAll(IntList.create(6, 7, 8)), is(true));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllAt() {
		assertThat(empty.addAll(0, IntList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAll(0, IntList.create(1, 2)));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAll(2, IntList.create(17, 18, 19)));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(IntList.create(1, 2)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(IntList.create(1, 2, 5)), is(true));
		assertThat(list, contains(3, 4, 3, 4));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(IntList.create(1, 2)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(IntList.create(1, 2, 3)), is(true));
		assertThat(list, contains(1, 2, 3, 1, 2, 3));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAll(x -> x + 1));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void sort() {
		expecting(UnsupportedOperationException.class, () -> empty.sort(naturalOrder()));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.sort(naturalOrder()));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty, is(emptyIterable()));

		list.clear();
		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void testEquals() {
		assertThat(empty.equals(Lists.of()), is(true));
		assertThat(empty.equals(Lists.of(1, 2)), is(false));

		assertThat(list.equals(Lists.of(1, 2, 3, 4, 5, 1, 2, 3, 4, 5)), is(true));
		assertThat(list.equals(Lists.of(5, 4, 3, 2, 1, 5, 4, 3, 2, 1)), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(empty.hashCode(), is(Lists.of().hashCode()));
		assertThat(list.hashCode(), is(Lists.of(1, 2, 3, 4, 5, 1, 2, 3, 4, 5).hashCode()));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(list.toString(), is("[1, 2, 3, 4, 5, 1, 2, 3, 4, 5]"));
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
	}

	@Test
	public void addAt() {
		expecting(UnsupportedOperationException.class, () -> list.add(0, 17));
		assertThat(list, contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void indexOf() {
		assertThat(empty.indexOf(17), is(-1));

		assertThat(list.indexOf(1), is(0));
		assertThat(list.indexOf(3), is(2));
		assertThat(list.indexOf(5), is(4));
	}

	@Test
	public void lastIndexOf() {
		assertThat(empty.lastIndexOf(17), is(-1));

		assertThat(list.lastIndexOf(1), is(5));
		assertThat(list.lastIndexOf(3), is(7));
		assertThat(list.lastIndexOf(5), is(9));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Integer> emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, emptyIterator::next);
		expecting(UnsupportedOperationException.class, emptyIterator::hasPrevious);
		expecting(UnsupportedOperationException.class, emptyIterator::previous);
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(UnsupportedOperationException.class, () -> emptyIterator.add(17));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIteratorAtEnd() {
		ListIterator<Integer> listIterator = list.listIterator(10);
		assertThat(listIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, listIterator::next);
	}

	@Test
	public void listIteratorAfterEnd() {
		expecting(IndexOutOfBoundsException.class, () -> list.listIterator(11));
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
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.stream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.parallelStream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeIf() {
		empty.removeIf(x -> x == 1);
		assertThat(empty, is(emptyIterable()));

		list.removeIf(x -> x == 1);
		assertThat(list, contains(2, 3, 4, 5, 2, 3, 4, 5));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger i = new AtomicInteger(0);
		list.forEach(x -> assertThat(x, is(i.getAndIncrement() % 5 + 1)));
		assertThat(i.get(), is(10));
	}
}
