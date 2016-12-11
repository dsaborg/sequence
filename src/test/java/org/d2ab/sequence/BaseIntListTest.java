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

import org.d2ab.collection.ints.IntList;
import org.d2ab.collection.ints.IntListIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.test.StrictIntIterator;
import org.junit.Test;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class BaseIntListTest {
	private final IntList backingEmpty = IntList.create();
	private final IntList empty = new IntList.Base() {
		@Override
		public IntIterator iterator() {
			return StrictIntIterator.from(backingEmpty.iterator());
		}

		@Override
		public int size() {
			return backingEmpty.size();
		}
	};

	private final IntList backingList = IntList.create(1, 2, 3, 4, 5, 1, 2, 3, 4, 5);
	private final IntList list = new IntList.Base() {
		@Override
		public IntIterator iterator() {
			return StrictIntIterator.from(backingList.iterator());
		}

		@Override
		public int size() {
			return backingList.size();
		}
	};

	@Test
	public void subList() {
		IntList subList = list.subList(2, 8);
		twice(() -> assertThat(subList, containsInts(3, 4, 5, 1, 2, 3)));

		assertThat(subList.remove(1), is(4));
		twice(() -> assertThat(subList, containsInts(3, 5, 1, 2, 3)));

		assertThat(subList.remove((Integer) 5), is(true));
		twice(() -> assertThat(subList, containsInts(3, 1, 2, 3)));

		IntIterator subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.next(), is(3));
		subListIterator.remove();
		twice(() -> assertThat(subList, containsInts(1, 2, 3)));

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, containsInts(1, 3)));

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
	public void containsElement() {
		assertThat(empty.contains(2), is(false));
		for (int i = 1; i < 5; i++)
			assertThat(list.contains(i), is(true));
		assertThat(list.contains(17), is(false));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void iteratorRemove() {
		IntIterator iterator = list.iterator();
		iterator.nextInt();
		iterator.nextInt();
		iterator.remove();
		iterator.nextInt();
		iterator.remove();

		assertThat(list, containsInts(1, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void toIntArray() {
		assertArrayEquals(new int[0], empty.toIntArray());
		assertArrayEquals(new int[]{1, 2, 3, 4, 5, 1, 2, 3, 4, 5}, list.toIntArray());
	}

	@Test
	public void add() {
		expecting(UnsupportedOperationException.class, () -> empty.add(1));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.add(6));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void remove() {
		assertThat(empty.remove((Integer) 17), is(false));

		assertThat(list.remove((Integer) 2), is(true));
		assertThat(list, containsInts(1, 3, 4, 5, 1, 2, 3, 4, 5));

		assertThat(list.remove((Integer) 17), is(false));
		assertThat(list, containsInts(1, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(asList(2, 3)), is(false));

		assertThat(list.containsAll(asList(2, 3)), is(true));
		assertThat(list.containsAll(asList(2, 17)), is(false));
	}

	@Test
	public void addAll() {
		assertThat(empty.addAll(emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAll(asList(1, 2)));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAll(asList(6, 7, 8)));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addAllAtIndex() {
		assertThat(empty.addAll(0, emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAll(0, asList(1, 2)));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAll(2, asList(17, 18, 19)));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(asList(1, 2)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(asList(1, 2, 5)), is(true));
		assertThat(list, containsInts(3, 4, 3, 4));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(asList(1, 2)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(asList(1, 2, 3)), is(true));
		assertThat(list, containsInts(1, 2, 3, 1, 2, 3));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAll(x -> x + 1));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void sort() {
		expecting(UnsupportedOperationException.class, () -> empty.sort(Comparator.naturalOrder()));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.sort(Comparator.naturalOrder()));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
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
		assertThat(empty.equals(emptyList()), is(true));
		assertThat(empty.equals(asList(1, 2)), is(false));

		assertThat(list.equals(asList(1, 2, 3, 4, 5, 1, 2, 3, 4, 5)), is(true));
		assertThat(list.equals(asList(5, 4, 3, 2, 1, 5, 4, 3, 2, 1)), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(empty.hashCode(), is(1));
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
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addAtIndex() {
		expecting(UnsupportedOperationException.class, () -> list.add(0, 17));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void indexOfInt() {
		assertThat(empty.indexOfInt(17), is(-1));

		assertThat(list.indexOfInt(1), is(0));
		assertThat(list.indexOfInt(3), is(2));
		assertThat(list.indexOfInt(5), is(4));
	}

	@Test
	public void lastIndexOfInt() {
		assertThat(empty.lastIndexOfInt(17), is(-1));

		assertThat(list.lastIndexOfInt(1), is(5));
		assertThat(list.lastIndexOfInt(3), is(7));
		assertThat(list.lastIndexOfInt(5), is(9));
	}

	@Test
	public void listIteratorEmpty() {
		IntListIterator emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(UnsupportedOperationException.class, () -> emptyIterator.add(17));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		IntListIterator listIterator = list.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));
		assertThat(listIterator.nextInt(), is(1));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.nextInt(), is(2));

		expecting(UnsupportedOperationException.class, () -> listIterator.add(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.nextInt(), is(3));

		expecting(UnsupportedOperationException.class, () -> listIterator.set(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.nextInt(), is(4));

		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		IntListIterator listIterator = list.listIterator();

		AtomicInteger i = new AtomicInteger(0);
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextInt(), is(i.get() % 5 + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
		});
	}

	@Test
	public void listIteratorRemoveAll() {
		IntIterator iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.nextInt(), is(i % 5 + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));

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
		assertThat(list, containsInts(2, 3, 4, 5, 2, 3, 4, 5));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger value = new AtomicInteger(0);
		list.forEach(x -> assertThat(x, is(value.getAndIncrement() % 5 + 1)));
		assertThat(value.get(), is(10));
	}
}
