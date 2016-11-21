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
import org.junit.Test;

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
	private final org.d2ab.collection.ints.ArrayIntList empty = new org.d2ab.collection.ints.ArrayIntList();
	private final org.d2ab.collection.ints.ArrayIntList list = org.d2ab.collection.ints.ArrayIntList.of(1, 2, 3, 4, 5);

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
		org.d2ab.collection.ints.IntListIterator emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		org.d2ab.collection.ints.IntListIterator listIterator = list.listIterator();

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
		org.d2ab.collection.ints.IntListIterator listIterator = list.listIterator();

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
		org.d2ab.collection.ints.IntListIterator listIterator = list.listIterator();

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
	public void intStream() {
		assertThat(empty.intStream().collect(ArrayIntList::new, ArrayIntList::addInt, ArrayIntList::addAllInts),
		           is(emptyIterable()));

		assertThat(list.intStream().collect(ArrayIntList::new, ArrayIntList::addInt, ArrayIntList::addAllInts),
		           containsInts(1, 2, 3, 4, 5));
	}

	@Test
	public void parallelIntStream() {
		assertThat(empty.parallelIntStream().collect(ArrayIntList::new, ArrayIntList::addInt, ArrayIntList::addAllInts),
		           is(emptyIterable()));

		assertThat(list.parallelIntStream().collect(ArrayIntList::new, ArrayIntList::addInt, ArrayIntList::addAllInts),
		           containsInts(1, 2, 3, 4, 5));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(list.sequence(), containsInts(1, 2, 3, 4, 5));
	}

	@Test
	public void lastIndexOfBoxed() {
		assertThat(empty.lastIndexOf(17), is(-1));

		assertThat(list.lastIndexOf(17), is(-1));
		assertThat(list.lastIndexOf(2), is(1));
	}

	@Test
	public void lastIndexOfInt() {
		assertThat(empty.lastIndexOfInt(17), is(-1));

		assertThat(list.lastIndexOfInt(17), is(-1));
		assertThat(list.lastIndexOfInt(2), is(1));
	}

	@Test
	public void indexOfBoxed() {
		assertThat(empty.indexOf(17), is(-1));

		assertThat(list.indexOf(17), is(-1));
		assertThat(list.indexOf(2), is(1));
	}

	@Test
	public void indexOfInt() {
		assertThat(empty.indexOfInt(17), is(-1));

		assertThat(list.indexOfInt(17), is(-1));
		assertThat(list.indexOfInt(2), is(1));
	}

	@Test
	public void getBoxed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.get(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.get(2), is(3));
	}

	@Test
	public void getInt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.getInt(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.getInt(2), is(3));
	}

	@Test
	public void setBoxed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.set(2, 17));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.set(2, 17), is(3));
		assertThat(list, containsInts(1, 2, 17, 4, 5));
	}

	@Test
	public void setInt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.setInt(2, 17));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.setInt(2, 17), is(3));
		assertThat(list, containsInts(1, 2, 17, 4, 5));
	}

	@Test
	public void addBoxed() {
		empty.add(17);
		assertThat(empty, containsInts(17));

		list.add(17);
		assertThat(list, containsInts(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void addInt() {
		empty.addInt(17);
		assertThat(empty, containsInts(17));

		list.addInt(17);
		assertThat(list, containsInts(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void addAtBoxed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.add(2, 17));
		assertThat(empty, is(emptyIterable()));

		empty.add(0, 17);
		assertThat(empty, containsInts(17));

		list.add(2, 17);
		assertThat(list, containsInts(1, 2, 17, 3, 4, 5));
	}

	@Test
	public void addIntAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.addIntAt(2, 17));
		assertThat(empty, is(emptyIterable()));

		empty.addIntAt(0, 17);
		assertThat(empty, containsInts(17));

		list.addIntAt(2, 17);
		assertThat(list, containsInts(1, 2, 17, 3, 4, 5));
	}

	@Test
	public void addAllBoxed() {
		empty.addAll(Arrays.asList(1, 2, 3));
		assertThat(empty, containsInts(1, 2, 3));

		list.addAll(Arrays.asList(6, 7, 8));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllIntsArray() {
		empty.addAllInts(1, 2, 3);
		assertThat(empty, containsInts(1, 2, 3));

		list.addAllInts(6, 7, 8);
		assertThat(list, containsInts(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllIntsCollection() {
		empty.addAllInts(org.d2ab.collection.ints.ArrayIntList.of(1, 2, 3));
		assertThat(empty, containsInts(1, 2, 3));

		list.addAllInts(org.d2ab.collection.ints.ArrayIntList.of(6, 7, 8));
		assertThat(list, containsInts(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllAtBoxed() {
		empty.addAll(0, Arrays.asList(1, 2, 3));
		assertThat(empty, containsInts(1, 2, 3));

		list.addAll(2, Arrays.asList(17, 18, 19));
		assertThat(list, containsInts(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void addAllIntsAtAtArray() {
		empty.addAllIntsAt(0, 1, 2, 3);
		assertThat(empty, containsInts(1, 2, 3));

		list.addAllIntsAt(2, 17, 18, 19);
		assertThat(list, containsInts(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void addAllIntAtCollection() {
		empty.addAllIntsAt(0, org.d2ab.collection.ints.ArrayIntList.of(1, 2, 3));
		assertThat(empty, containsInts(1, 2, 3));

		list.addAllIntsAt(2, 17, 18, 19);
		assertThat(list, containsInts(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void removeBoxed() {
		assertThat(empty.remove((Integer) 17), is(false));
		assertThat(empty.remove(new Object()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove((Integer) 17), is(false));
		assertThat(list.remove(new Object()), is(false));
		assertThat(list.remove((Integer) 2), is(true));
		assertThat(list, containsInts(1, 3, 4, 5));
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
	public void removeAtBoxed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.remove(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove(2), is(3));
		assertThat(list, containsInts(1, 2, 4, 5));
	}

	@Test
	public void removeIntAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.removeIntAt(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeIntAt(2), is(3));
		assertThat(list, containsInts(1, 2, 4, 5));
	}

	@Test
	public void containsBoxed() {
		assertThat(empty.contains(17), is(false));
		assertThat(empty.contains(new Object()), is(false));

		assertThat(list.contains(17), is(false));
		assertThat(list.contains(new Object()), is(false));
		assertThat(list.contains(2), is(true));
	}

	@Test
	public void containsInt() {
		assertThat(empty.containsInt(17), is(false));

		assertThat(list.containsInt(17), is(false));
		assertThat(list.containsInt(2), is(true));
	}

	@Test
	public void containsAllBoxed() {
		assertThat(empty.containsAll(Arrays.asList(17, 18, 19, new Object())), is(false));

		assertThat(list.containsAll(Arrays.asList(17, 18, 19, new Object())), is(false));
		assertThat(list.containsAll(Arrays.asList(1, 2, 3)), is(true));
	}

	@Test
	public void containsAllIntsArray() {
		assertThat(empty.containsAllInts(17, 18, 19), is(false));

		assertThat(list.containsAllInts(17, 18, 19), is(false));
		assertThat(list.containsAllInts(1, 2, 3), is(true));
	}

	@Test
	public void containsAllIntsCollection() {
		assertThat(empty.containsAllInts(org.d2ab.collection.ints.ArrayIntList.of(17, 18, 19)), is(false));

		assertThat(list.containsAllInts(org.d2ab.collection.ints.ArrayIntList.of(17, 18, 19)), is(false));
		assertThat(list.containsAllInts(org.d2ab.collection.ints.ArrayIntList.of(1, 2, 3)), is(true));
	}

	@Test
	public void containsAnyIntsArray() {
		assertThat(empty.containsAnyInts(17, 18, 19), is(false));

		assertThat(list.containsAnyInts(17, 18, 19), is(false));
		assertThat(list.containsAnyInts(1, 17, 3), is(true));
	}

	@Test
	public void containsAnyIntsCollection() {
		assertThat(empty.containsAnyInts(org.d2ab.collection.ints.ArrayIntList.of(17, 18, 19)), is(false));

		assertThat(list.containsAnyInts(org.d2ab.collection.ints.ArrayIntList.of(17, 18, 19)), is(false));
		assertThat(list.containsAnyInts(org.d2ab.collection.ints.ArrayIntList.of(1, 17, 3)), is(true));
	}

	@Test
	public void removeAllBoxed() {
		assertThat(empty.removeAll(Arrays.asList(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(Arrays.asList(17, 18, 19)), is(false));
		assertThat(list.removeAll(Arrays.asList(1, 2, 3, 17)), is(true));
		assertThat(list, containsInts(4, 5));
	}

	@Test
	public void removeAllIntsArray() {
		assertThat(empty.removeAllInts(1, 2, 3, 17), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllInts(17, 17, 19), is(false));
		assertThat(list.removeAllInts(1, 2, 3, 17), is(true));
		assertThat(list, containsInts(4, 5));
	}

	@Test
	public void removeAllIntsCollection() {
		assertThat(empty.removeAllInts(org.d2ab.collection.ints.ArrayIntList.of(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllInts(org.d2ab.collection.ints.ArrayIntList.of(17, 18, 19)), is(false));
		assertThat(list.removeAllInts(org.d2ab.collection.ints.ArrayIntList.of(1, 2, 3, 17)), is(true));
		assertThat(list, containsInts(4, 5));
	}

	@Test
	public void removeIfBoxed() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeIf(x -> x > 5), is(false));
		assertThat(list.removeIf(x -> x > 3), is(true));
		assertThat(list, containsInts(1, 2, 3));
	}

	@Test
	public void removeIntsIf() {
		assertThat(empty.removeIntsIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeIntsIf(x -> x > 5), is(false));
		assertThat(list.removeIntsIf(x -> x > 3), is(true));
		assertThat(list, containsInts(1, 2, 3));
	}

	@Test
	public void retainAllBoxed() {
		assertThat(empty.retainAll(Arrays.asList(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(Arrays.asList(1, 2, 3, 17)), is(true));
		assertThat(list, containsInts(1, 2, 3));
	}

	@Test
	public void retainAllIntsArray() {
		assertThat(empty.retainAllInts(1, 2, 3, 17), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllInts(1, 2, 3, 17), is(true));
		assertThat(list, containsInts(1, 2, 3));
	}

	@Test
	public void retainAllIntsCollection() {
		assertThat(empty.retainAllInts(org.d2ab.collection.ints.ArrayIntList.of(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllInts(org.d2ab.collection.ints.ArrayIntList.of(1, 2, 3, 17)), is(true));
		assertThat(list, containsInts(1, 2, 3));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		list.replaceAll(x -> x + 1);
		assertThat(list, containsInts(2, 3, 4, 5, 6));
	}

	@Test
	public void replaceAllInts() {
		empty.replaceAllInts(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		list.replaceAllInts(x -> x + 1);
		assertThat(list, containsInts(2, 3, 4, 5, 6));
	}

	@Test
	public void forEachBoxed() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		list.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(6));
	}

	@Test
	public void forEachLong() {
		empty.forEachInt(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		list.forEachInt(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(6));
	}
}