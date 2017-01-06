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

package org.d2ab.collection.longs;

import org.d2ab.collection.Lists;
import org.d2ab.iterator.longs.LongIterator;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Arrays.asList;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class LongListTest {
	private final LongList empty = LongList.Base.create();
	private final LongList list = LongList.Base.create(1, 2, 3, 4, 5);

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
	public void testAsList() {
		assertThat(empty.asList(), is(sameInstance(empty)));
		assertThat(list.asList(), is(sameInstance(list)));
	}

	@Test
	public void toLongArray() {
		assertArrayEquals(new long[0], empty.toLongArray());
		assertArrayEquals(new long[]{1, 2, 3, 4, 5}, list.toLongArray());
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, containsLongs(1, 2, 3, 4, 5));
	}

	@Test
	public void listIteratorEmpty() {
		LongListIterator emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		emptyIterator.add(17);
		emptyIterator.add(18);
		expecting(IllegalStateException.class, () -> emptyIterator.set(19));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(true));
		assertThat(emptyIterator.nextIndex(), is(2));
		assertThat(emptyIterator.previousIndex(), is(1));

		assertThat(emptyIterator.previousLong(), is(18L));
		assertThat(emptyIterator.hasNext(), is(true));
		assertThat(emptyIterator.hasPrevious(), is(true));
		assertThat(emptyIterator.nextIndex(), is(1));
		assertThat(emptyIterator.previousIndex(), is(0));

		emptyIterator.remove();
		expecting(IllegalStateException.class, () -> emptyIterator.set(19));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(true));
		assertThat(emptyIterator.nextIndex(), is(1));
		assertThat(emptyIterator.previousIndex(), is(0));

		assertThat(emptyIterator.previousLong(), is(17L));
		emptyIterator.set(19);
		assertThat(emptyIterator.hasNext(), is(true));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(NoSuchElementException.class, emptyIterator::previousLong);
		assertThat(emptyIterator.hasNext(), is(true));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, containsLongs(19));
	}

	@Test
	public void listIterator() {
		LongListIterator listIterator = list.listIterator();

		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set(32));
		expecting(NoSuchElementException.class, listIterator::previousLong);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.add(33);
		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set(34));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.previousLong(), is(33L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.set(35);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.remove();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		assertThat(listIterator.nextLong(), is(1L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextLong(), is(2L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.nextLong(), is(3L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.previousLong(), is(3L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.previousLong(), is(2L));
		listIterator.set(17);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextLong(), is(17L));
		listIterator.add(18);
		listIterator.add(19);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(listIterator.nextLong(), is(3L));

		assertThat(list, containsLongs(1, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		twice(() -> {
			LongListIterator listIterator = list.listIterator();
			AtomicInteger i = new AtomicInteger();
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextLong(), is((long) i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));
			expecting(NoSuchElementException.class, listIterator::nextLong);

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previousLong(), is((long) i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
			expecting(NoSuchElementException.class, listIterator::previousLong);
		});
	}

	@Test
	public void iteratorRemoveAll() {
		LongIterator iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.nextLong(), is((long) i + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(5));
		expecting(NoSuchElementException.class, iterator::nextLong);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		LongListIterator listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.nextLong(), is((long) i + 1));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			i++;
		}
		assertThat(i, is(5));
		expecting(NoSuchElementException.class, listIterator::nextLong);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemoveBackwards() {
		int i = 5;
		LongListIterator listIterator = list.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previousLong(), is((long) i + 1));
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
		}
		assertThat(i, is(0));
		expecting(NoSuchElementException.class, listIterator::previousLong);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void iteratorFailFast() {
		LongIterator it1 = list.iterator();
		list.addLong(17);
		expecting(ConcurrentModificationException.class, it1::nextLong);

		LongIterator it2 = list.iterator();
		list.removeLong(17);
		expecting(ConcurrentModificationException.class, it2::nextLong);
	}

	@Test
	public void subList() {
		LongList list = LongList.Base.create(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(-1, 2));
		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(2, 11));

		LongList subList = list.subList(2, 8);
		assertThat(subList.size(), is(6));
		twice(() -> assertThat(subList, containsLongs(3, 4, 5, 6, 7, 8)));

		assertThat(subList.removeLongAt(1), is(4L));
		twice(() -> assertThat(subList, containsLongs(3, 5, 6, 7, 8)));
		twice(() -> assertThat(list, containsLongs(1, 2, 3, 5, 6, 7, 8, 9, 10)));

		assertThat(subList.removeLong(5), is(true));
		twice(() -> assertThat(subList, containsLongs(3, 6, 7, 8)));
		twice(() -> assertThat(list, containsLongs(1, 2, 3, 6, 7, 8, 9, 10)));

		LongIterator subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextLong(), is(3L));
		subListIterator.remove();
		twice(() -> assertThat(subList, containsLongs(6, 7, 8)));
		twice(() -> assertThat(list, containsLongs(1, 2, 6, 7, 8, 9, 10)));

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, containsLongs(7)));
		twice(() -> assertThat(list, containsLongs(1, 2, 7, 9, 10)));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, containsLongs(1, 2, 9, 10)));

		expecting(UnsupportedOperationException.class, () -> subList.addLong(17));
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, containsLongs(1, 2, 9, 10)));
	}

	@Test
	public void sortLongs() {
		Lists.reverse(list);
		expecting(UnsupportedOperationException.class, list::sortLongs);
		assertThat(list, containsLongs(5, 4, 3, 2, 1));
	}

	@Test
	public void binarySearch() {
		expecting(UnsupportedOperationException.class, () -> list.binarySearch(1));
	}

	@Test
	public void longStream() {
		assertThat(empty.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           is(emptyIterable()));

		assertThat(list.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           containsLongs(1, 2, 3, 4, 5));
	}

	@Test
	public void parallelLongStream() {
		assertThat(empty.parallelLongStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           is(emptyIterable()));

		assertThat(list.parallelLongStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           containsLongs(1, 2, 3, 4, 5));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(list.toString(), is("[1, 2, 3, 4, 5]"));
	}

	@Test
	public void testEqualsHashCodeAgainstList() {
		assertThat(list, is(equalTo(list)));
		assertThat(list, is(not(equalTo(null))));
		assertThat(list, is(not(equalTo(new Object()))));
		assertThat(list, is(not(equalTo(new TreeSet<>(asList(1L, 2L, 3L, 4L, 5L))))));
		assertThat(list, is(not(equalTo(new ArrayList<>(asList(1L, 2L, 3L, 4L))))));

		List<Long> list2 = new ArrayList<>(asList(1L, 2L, 3L, 4L, 5L, 17L));
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));

		list2.remove(17L);

		assertThat(list, is(equalTo(list2)));
		assertThat(list.hashCode(), is(list2.hashCode()));

		Lists.reverse(list2);
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));
	}

	@Test
	public void testEqualsHashCodeAgainstLongList() {
		assertThat(list, is(not(equalTo(LongList.create(1, 2, 3, 4)))));

		LongList list2 = LongList.create(1, 2, 3, 4, 5, 17);
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));

		list2.removeLong(17);

		assertThat(list, is(equalTo(list2)));
		assertThat(list.hashCode(), is(list2.hashCode()));

		Lists.reverse(list2);
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(list.sequence(), containsLongs(1, 2, 3, 4, 5));
	}

	@Test
	public void lastIndexOfLong() {
		assertThat(empty.lastIndexOfLong(17), is(-1));

		assertThat(list.lastIndexOfLong(17), is(-1));
		assertThat(list.lastIndexOfLong(2), is(1));
	}

	@Test
	public void indexOfLong() {
		assertThat(empty.indexOfLong(17), is(-1));

		assertThat(list.indexOfLong(17), is(-1));
		assertThat(list.indexOfLong(2), is(1));
	}

	@Test
	public void getLong() {
		expecting(IndexOutOfBoundsException.class, () -> empty.getLong(2));
		expecting(IndexOutOfBoundsException.class, () -> empty.getLong(0));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.getLong(2), is(3L));
		expecting(IndexOutOfBoundsException.class, () -> list.getLong(7));
		expecting(IndexOutOfBoundsException.class, () -> list.getLong(5));
	}

	@Test
	public void setLong() {
		expecting(IndexOutOfBoundsException.class, () -> empty.setLong(2, 17));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.setLong(2, 17), is(3L));
		assertThat(list, containsLongs(1, 2, 17, 4, 5));
	}

	@Test
	public void addLong() {
		assertThat(empty.addLong(17), is(true));
		assertThat(empty, containsLongs(17));

		assertThat(list.addLong(17), is(true));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void addLongAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.addLongAt(2, 17));
		assertThat(empty, is(emptyIterable()));

		empty.addLongAt(0, 17);
		assertThat(empty, containsLongs(17));

		list.addLongAt(2, 17);
		assertThat(list, containsLongs(1, 2, 17, 3, 4, 5));
	}

	@Test
	public void addAllLongsArray() {
		assertThat(empty.addAllLongs(), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllLongs(1, 2, 3), is(true));
		assertThat(empty, containsLongs(1, 2, 3));

		assertThat(list.addAllLongs(6, 7, 8), is(true));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllLongsLongCollection() {
		assertThat(empty.addAllLongs(new BitLongSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllLongs(new BitLongSet(1, 2, 3)), is(true));
		assertThat(empty, containsLongs(1, 2, 3));

		assertThat(list.addAllLongs(new BitLongSet(6, 7, 8)), is(true));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllLongsArrayLongList() {
		assertThat(empty.addAllLongs(ArrayLongList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllLongs(ArrayLongList.create(1, 2, 3)), is(true));
		assertThat(empty, containsLongs(1, 2, 3));

		assertThat(list.addAllLongs(ArrayLongList.create(6, 7, 8)), is(true));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllLongsAtAtArray() {
		assertThat(empty.addAllLongsAt(0), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllLongsAt(0, 1, 2, 3), is(true));
		assertThat(empty, containsLongs(1, 2, 3));

		assertThat(list.addAllLongsAt(2, 17, 18, 19), is(true));
		assertThat(list, containsLongs(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void addAllLongsAtLongCollection() {
		assertThat(empty.addAllLongsAt(0, new BitLongSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllLongsAt(0, new BitLongSet(1, 2, 3)), is(true));
		assertThat(empty, containsLongs(1, 2, 3));

		assertThat(list.addAllLongsAt(2, new BitLongSet(17, 18, 19)), is(true));
		assertThat(list, containsLongs(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void addAllLongsAtArrayLongList() {
		assertThat(empty.addAllLongsAt(0, ArrayLongList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllLongsAt(0, ArrayLongList.create(1, 2, 3)), is(true));
		assertThat(empty, containsLongs(1, 2, 3));

		assertThat(list.addAllLongsAt(2, ArrayLongList.create(17, 18, 19)), is(true));
		assertThat(list, containsLongs(1, 2, 17, 18, 19, 3, 4, 5));
	}

	@Test
	public void removeLong() {
		assertThat(empty.removeLong(17), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeLong(17), is(false));
		assertThat(list.removeLong(2), is(true));
		assertThat(list, containsLongs(1, 3, 4, 5));
	}

	@Test
	public void removeLongAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.removeLongAt(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeLongAt(2), is(3L));
		assertThat(list, containsLongs(1, 2, 4, 5));
	}

	@Test
	public void containsLong() {
		assertThat(empty.containsLong(17), is(false));

		assertThat(list.containsLong(17), is(false));
		assertThat(list.containsLong(2), is(true));
	}

	@Test
	public void containsAllLongsArray() {
		assertThat(empty.containsAllLongs(17, 18, 19), is(false));

		assertThat(list.containsAllLongs(17, 18, 19), is(false));
		assertThat(list.containsAllLongs(1, 2, 3), is(true));
	}

	@Test
	public void containsAllLongsCollection() {
		assertThat(empty.containsAllLongs(ArrayLongList.create(17, 18, 19)), is(false));

		assertThat(list.containsAllLongs(ArrayLongList.create(17, 18, 19)), is(false));
		assertThat(list.containsAllLongs(ArrayLongList.create(1, 2, 3)), is(true));
	}

	@Test
	public void containsAnyLongsArray() {
		assertThat(empty.containsAnyLongs(17, 18, 19), is(false));

		assertThat(list.containsAnyLongs(17, 18, 19), is(false));
		assertThat(list.containsAnyLongs(1, 17, 3), is(true));
	}

	@Test
	public void containsAnyLongsCollection() {
		assertThat(empty.containsAnyLongs(ArrayLongList.create(17, 18, 19)), is(false));

		assertThat(list.containsAnyLongs(ArrayLongList.create(17, 18, 19)), is(false));
		assertThat(list.containsAnyLongs(ArrayLongList.create(1, 17, 3)), is(true));
	}

	@Test
	public void removeAllLongsArray() {
		assertThat(empty.removeAllLongs(1, 2, 3, 17), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllLongs(17, 17, 19), is(false));
		assertThat(list.removeAllLongs(1, 2, 3, 17), is(true));
		assertThat(list, containsLongs(4, 5));
	}

	@Test
	public void removeAllLongsCollection() {
		assertThat(empty.removeAllLongs(ArrayLongList.create(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllLongs(ArrayLongList.create(17, 18, 19)), is(false));
		assertThat(list.removeAllLongs(ArrayLongList.create(1, 2, 3, 17)), is(true));
		assertThat(list, containsLongs(4, 5));
	}

	@Test
	public void removeLongsIf() {
		assertThat(empty.removeLongsIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeLongsIf(x -> x > 5), is(false));
		assertThat(list.removeLongsIf(x -> x > 3), is(true));
		assertThat(list, containsLongs(1, 2, 3));
	}

	@Test
	public void retainAllLongsArray() {
		assertThat(empty.retainAllLongs(1, 2, 3, 17), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllLongs(1, 2, 3, 17), is(true));
		assertThat(list, containsLongs(1, 2, 3));
	}

	@Test
	public void retainAllLongsCollection() {
		assertThat(empty.retainAllLongs(LongList.create(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllLongs(LongList.create(1, 2, 3, 17)), is(true));
		assertThat(list, containsLongs(1, 2, 3));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		list.replaceAll(x -> x + 1);
		assertThat(list, containsLongs(2, 3, 4, 5, 6));
	}

	@Test
	public void replaceAllLongs() {
		empty.replaceAllLongs(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		list.replaceAllLongs(x -> x + 1);
		assertThat(list, containsLongs(2, 3, 4, 5, 6));
	}

	@Test
	public void forEachLong() {
		empty.forEachLong(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicLong value = new AtomicLong(1);
		list.forEachLong(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(6L));
	}
}
