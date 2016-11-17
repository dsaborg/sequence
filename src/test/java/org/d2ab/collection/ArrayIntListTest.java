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

import org.d2ab.iterator.ints.IntIterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class ArrayIntListTest {
	private final ArrayIntList empty = new ArrayIntList();
	private final ArrayIntList list = ArrayIntList.of(1, 2, 3, 4, 5);

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
	public void toIntArray() {
		assertArrayEquals(new int[0], empty.toIntArray());
		assertArrayEquals(new int[]{1, 2, 3, 4, 5}, list.toIntArray());
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, containsInts(1, 2, 3, 4, 5));
	}

	@Test
	public void listIteratorEmpty() {
		IntListIterator emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		IntListIterator listIterator = list.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));
		assertThat(listIterator.nextInt(), is(1));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.nextInt(), is(2));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.nextInt(), is(3));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.previousInt(), is(3));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.previousInt(), is(2));

		listIterator.set(17);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.nextInt(), is(17));

		listIterator.add(18);
		listIterator.add(19);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));
		assertThat(listIterator.nextInt(), is(3));

		assertThat(list, containsInts(1, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		IntListIterator listIterator = list.listIterator();

		AtomicInteger i = new AtomicInteger();
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextInt(), is(i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previousInt(), is(i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
		});
	}

	@Test
	public void iteratorRemoveAll() {
		IntIterator iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.nextInt(), is(i + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(5));

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		IntListIterator listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.nextInt(), is(i + 1));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			i++;
		}
		assertThat(i, is(5));

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemoveBackwards() {
		int i = 5;
		IntListIterator listIterator = list.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previousInt(), is(i + 1));
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
		}
		assertThat(i, is(0));

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void subList() {
		expecting(UnsupportedOperationException.class, () -> list.subList(1, 2));
	}

	@Test
	public void replaceAllInts() {
		empty.replaceAllInts(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		list.replaceAllInts(x -> x + 1);
		assertThat(list, containsInts(2, 3, 4, 5, 6));
	}

	@Test
	public void getAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.getAt(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.getAt(2), is(3));
	}

	@Test
	public void setAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.setAt(2, 17));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.setAt(2, 17), is(3));
		assertThat(list, containsInts(1, 2, 17, 4, 5));
	}

	@Test
	public void addAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.addAt(2, 17));
		assertThat(empty, is(emptyIterable()));

		empty.addAt(0, 17);
		assertThat(empty, containsInts(17));

		list.addAt(2, 17);
		assertThat(list, containsInts(1, 2, 17, 3, 4, 5));
	}

	@Test
	public void removeAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.removeAt(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAt(2), is(3));
		assertThat(list, containsInts(1, 2, 4, 5));
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
	public void addInt() {
		empty.addInt(17);
		assertThat(empty, containsInts(17));

		list.addInt(17);
		assertThat(list, containsInts(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void addAllIntArray() {
		empty.addAll(1, 2, 3);
		assertThat(empty, containsInts(1, 2, 3));

		list.addAll(6, 7, 8);
		assertThat(list, containsInts(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllIntCollection() {
		empty.addAll(ArrayIntList.of(1, 2, 3));
		assertThat(empty, containsInts(1, 2, 3));

		list.addAll(ArrayIntList.of(6, 7, 8));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllAtIntArray() {
		empty.addAllAt(0, 1, 2, 3);
		assertThat(empty, containsInts(1, 2, 3));

		list.addAllAt(2, 17, 18, 19);
		assertThat(list, containsInts(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void addAllAtIntCollection() {
		empty.addAllAt(0, ArrayIntList.of(1, 2, 3));
		assertThat(empty, containsInts(1, 2, 3));

		list.addAllAt(2, 17, 18, 19);
		assertThat(list, containsInts(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void containsAllIntArray() {
		assertThat(empty.containsAll(17, 18, 19), is(false));

		assertThat(list.containsAll(17, 18, 19), is(false));
		assertThat(list.containsAll(1, 2, 3), is(true));
	}

	@Test
	public void containsAllIntCollection() {
		assertThat(empty.containsAll(ArrayIntList.of(17, 18, 19)), is(false));

		assertThat(list.containsAll(ArrayIntList.of(17, 18, 19)), is(false));
		assertThat(list.containsAll(ArrayIntList.of(1, 2, 3)), is(true));
	}

	@Test
	public void removeInt() {
		assertThat(empty.removeInt(17), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeInt(17), is(false));
		assertThat(list.removeInt(2), is(true));
		assertThat(list, containsInts(1, 3, 4, 5));
	}

	@Test
	public void containsInt() {
		assertThat(empty.containsInt(17), is(false));

		assertThat(list.containsInt(17), is(false));
		assertThat(list.containsInt(2), is(true));
	}

	@Test
	public void removeAllIntArray() {
		assertThat(empty.removeAll(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(1, 2, 3), is(true));
		assertThat(list, containsInts(4, 5));
	}

	@Test
	public void removeAllIntCollection() {
		assertThat(empty.removeAll(ArrayIntList.of(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(ArrayIntList.of(1, 2, 3)), is(true));
		assertThat(list, containsInts(4, 5));
	}

	@Test
	public void retainAllIntArray() {
		assertThat(empty.retainAll(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(1, 2, 3), is(true));
		assertThat(list, containsInts(1, 2, 3));
	}

	@Test
	public void retainAllIntCollection() {
		assertThat(empty.retainAll(ArrayIntList.of(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(ArrayIntList.of(1, 2, 3)), is(true));
		assertThat(list, containsInts(1, 2, 3));
	}

	@Test
	public void removeIntsIf() {
		assertThat(empty.removeIntsIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeIntsIf(x -> x > 3), is(true));
		assertThat(list, containsInts(1, 2, 3));
	}

	@Test
	public void forEachInt() {
		empty.forEachInt(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		list.forEachInt(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(6));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		list.replaceAll(x -> x + 1);
		assertThat(list, containsInts(2, 3, 4, 5, 6));
	}

	@Test
	public void getIndexed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.get(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.get(2), is(3));
	}

	@Test
	public void setIndexed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.set(2, 17));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.set(2, 17), is(3));
		assertThat(list, containsInts(1, 2, 17, 4, 5));
	}

	@Test
	public void addIndexed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.add(2, 17));
		assertThat(empty, is(emptyIterable()));

		empty.add(0, 17);
		assertThat(empty, containsInts(17));

		list.add(2, 17);
		assertThat(list, containsInts(1, 2, 17, 3, 4, 5));
	}

	@Test
	public void removeIndexed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.remove(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove(2), is(3));
		assertThat(list, containsInts(1, 2, 4, 5));
	}

	@Test
	public void lastIndexOfBoxed() {
		assertThat(empty.lastIndexOf((Integer) 17), is(-1));

		assertThat(list.lastIndexOf((Integer) 17), is(-1));
		assertThat(list.lastIndexOf((Integer) 2), is(1));
	}

	@Test
	public void indexOfBoxed() {
		assertThat(empty.indexOf((Integer) 17), is(-1));

		assertThat(list.indexOf((Integer) 17), is(-1));
		assertThat(list.indexOf((Integer) 2), is(1));
	}

	@Test
	public void add() {
		empty.add(17);
		assertThat(empty, containsInts(17));

		list.add(17);
		assertThat(list, containsInts(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void addAll() {
		empty.addAll(Arrays.asList(1, 2, 3));
		assertThat(empty, containsInts(1, 2, 3));

		list.addAll(Arrays.asList(6, 7, 8));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllIndexed() {
		empty.addAll(0, Arrays.asList(1, 2, 3));
		assertThat(empty, containsInts(1, 2, 3));

		list.addAll(2, Arrays.asList(17, 18, 19));
		assertThat(list, containsInts(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(Arrays.asList(17, 18, 19)), is(false));

		assertThat(list.containsAll(Arrays.asList(17, 18, 19)), is(false));
		assertThat(list.containsAll(Arrays.asList(1, 2, 3)), is(true));
	}

	@Test
	public void remove() {
		assertThat(empty.remove((Integer) 17), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove((Integer) 17), is(false));
		assertThat(list.remove((Integer) 2), is(true));
		assertThat(list, containsInts(1, 3, 4, 5));
	}

	@Test
	public void contains() {
		assertThat(empty.contains(17), is(false));

		assertThat(list.contains(17), is(false));
		assertThat(list.contains(2), is(true));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(Arrays.asList(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(Arrays.asList(1, 2, 3)), is(true));
		assertThat(list, containsInts(4, 5));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(Arrays.asList(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(Arrays.asList(1, 2, 3)), is(true));
		assertThat(list, containsInts(1, 2, 3));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeIf(x -> x > 3), is(true));
		assertThat(list, containsInts(1, 2, 3));
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