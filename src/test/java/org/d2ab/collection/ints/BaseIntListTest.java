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

import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.test.StrictIntIterator;
import org.junit.Test;

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

		assertThat(subList.removeIntAt(1), is(4));
		twice(() -> assertThat(subList, containsInts(3, 5, 1, 2, 3)));

		assertThat(subList.removeInt(5), is(true));
		twice(() -> assertThat(subList, containsInts(3, 1, 2, 3)));

		IntIterator subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextInt(), is(3));
		subListIterator.remove();
		twice(() -> assertThat(subList, containsInts(1, 2, 3)));

		subList.removeIntsIf(x -> x % 2 == 0);
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
	public void containsInt() {
		assertThat(empty.containsInt(2), is(false));
		for (int i = 1; i < 5; i++)
			assertThat(list.containsInt(i), is(true));
		assertThat(list.containsInt(17), is(false));
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
	public void addInt() {
		expecting(UnsupportedOperationException.class, () -> empty.addInt(1));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addInt(6));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeInt() {
		assertThat(empty.removeInt(17), is(false));

		assertThat(list.removeInt(2), is(true));
		assertThat(list, containsInts(1, 3, 4, 5, 1, 2, 3, 4, 5));

		assertThat(list.removeInt(17), is(false));
		assertThat(list, containsInts(1, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void containsAllInts() {
		assertThat(empty.containsAllInts(IntList.create(2, 3)), is(false));

		assertThat(list.containsAllInts(IntList.create(2, 3)), is(true));
		assertThat(list.containsAllInts(IntList.create(2, 17)), is(false));
	}

	@Test
	public void addAllInts() {
		assertThat(empty.addAllInts(IntList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAllInts(IntList.create(1, 2)));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAllInts(IntList.create(6, 7, 8)));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addAllIntsAt() {
		assertThat(empty.addAllIntsAt(0, IntList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAllIntsAt(0, IntList.create(1, 2)));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAllIntsAt(2, IntList.create(17, 18, 19)));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeAllInts() {
		assertThat(empty.removeAllInts(IntList.create(1, 2)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllInts(IntList.create(1, 2, 5)), is(true));
		assertThat(list, containsInts(3, 4, 3, 4));
	}

	@Test
	public void retainAllInts() {
		assertThat(empty.retainAllInts(IntList.create(1, 2)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllInts(IntList.create(1, 2, 3)), is(true));
		assertThat(list, containsInts(1, 2, 3, 1, 2, 3));
	}

	@Test
	public void replaceAllInts() {
		empty.replaceAllInts(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAllInts(x -> x + 1));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void sortInts() {
		expecting(UnsupportedOperationException.class, empty::sortInts);
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, list::sortInts);
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
		assertThat(empty.hashCode(), is(emptyList().hashCode()));
		assertThat(list.hashCode(), is(asList(1, 2, 3, 4, 5, 1, 2, 3, 4, 5).hashCode()));
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
	public void setInt() {
		expecting(UnsupportedOperationException.class, () -> list.setInt(2, 17));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addIntAt() {
		expecting(UnsupportedOperationException.class, () -> list.addIntAt(0, 17));
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
	public void removeIntsIf() {
		empty.removeIntsIf(x -> x == 1);
		assertThat(empty, is(emptyIterable()));

		list.removeIntsIf(x -> x == 1);
		assertThat(list, containsInts(2, 3, 4, 5, 2, 3, 4, 5));
	}

	@Test
	public void forEachInt() {
		empty.forEachInt(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger value = new AtomicInteger(0);
		list.forEachInt(x -> assertThat(x, is(value.getAndIncrement() % 5 + 1)));
		assertThat(value.get(), is(10));
	}
}
