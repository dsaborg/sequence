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
import org.d2ab.iterator.ints.IntIterator;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class IntListOfTest {
	private final IntList empty = IntList.of();
	private final IntList list = IntList.of(1, 2, 3, 4, 5, 1, 2, 3, 4, 5);

	@Test
	public void subList() {
		IntList subList = list.subList(2, 8);
		twice(() -> assertThat(subList, containsInts(3, 4, 5, 1, 2, 3)));

		expecting(UnsupportedOperationException.class, () -> subList.removeIntAt(1));
		twice(() -> assertThat(subList, containsInts(3, 4, 5, 1, 2, 3)));

		expecting(UnsupportedOperationException.class, () -> subList.removeInt(5));
		twice(() -> assertThat(subList, containsInts(3, 4, 5, 1, 2, 3)));

		IntIterator subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextInt(), is(3));
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextInt(), is(4));
		expecting(UnsupportedOperationException.class, subListIterator::remove);
		twice(() -> assertThat(subList, containsInts(3, 4, 5, 1, 2, 3)));

		expecting(UnsupportedOperationException.class, () -> subList.removeIntsIf(x -> x % 2 == 0));
		twice(() -> assertThat(subList, containsInts(3, 4, 5, 1, 2, 3)));

		expecting(UnsupportedOperationException.class, subList::clear);
		twice(() -> assertThat(subList, containsInts(3, 4, 5, 1, 2, 3)));
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
		assertThat(iterator.nextInt(), is(1));
		assertThat(iterator.nextInt(), is(2));
		expecting(UnsupportedOperationException.class, iterator::remove);
		assertThat(iterator.nextInt(), is(3));
		expecting(UnsupportedOperationException.class, iterator::remove);

		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
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

		expecting(UnsupportedOperationException.class, () -> list.removeInt(2));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));

		assertThat(list.removeInt(17), is(false));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
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

		expecting(UnsupportedOperationException.class, () -> list.removeAllInts(IntList.create(1, 2, 5)));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void retainAllInts() {
		assertThat(empty.retainAllInts(IntList.create(1, 2)), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.retainAllInts(IntList.create(1, 2, 3)));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
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

		expecting(UnsupportedOperationException.class, list::clear);
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
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
		assertThat(list.getInt(0), is(1));
		assertThat(list.getInt(2), is(3));
		assertThat(list.getInt(4), is(5));
		assertThat(list.getInt(5), is(1));
		assertThat(list.getInt(7), is(3));
		assertThat(list.getInt(9), is(5));
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
	public void listIteratorAtEnd() {
		IntListIterator listIterator = list.listIterator(10);
		assertThat(listIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, listIterator::nextInt);
	}

	@Test
	public void listIteratorEdgeCases() {
		expecting(IndexOutOfBoundsException.class, () -> list.listIterator(-1));
		expecting(IndexOutOfBoundsException.class, () -> list.listIterator(11));
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

		expecting(UnsupportedOperationException.class, () -> list.removeIntsIf(x -> x == 1));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
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
