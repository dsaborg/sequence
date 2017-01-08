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

import org.d2ab.iterator.longs.LongIterator;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class LongListOfTest {
	private final LongList empty = LongList.of();
	private final LongList list = LongList.of(1, 2, 3, 4, 5, 1, 2, 3, 4, 5);

	@Test
	public void subList() {
		LongList subList = list.subList(2, 8);
		twice(() -> assertThat(subList, containsLongs(3, 4, 5, 1, 2, 3)));

		expecting(UnsupportedOperationException.class, () -> subList.removeLongAt(1));
		twice(() -> assertThat(subList, containsLongs(3, 4, 5, 1, 2, 3)));

		expecting(UnsupportedOperationException.class, () -> subList.removeLong(5));
		twice(() -> assertThat(subList, containsLongs(3, 4, 5, 1, 2, 3)));

		LongIterator subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextLong(), is(3L));
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextLong(), is(4L));
		expecting(UnsupportedOperationException.class, subListIterator::remove);
		twice(() -> assertThat(subList, containsLongs(3, 4, 5, 1, 2, 3)));

		expecting(UnsupportedOperationException.class, () -> subList.removeLongsIf(x -> x % 2 == 0));
		twice(() -> assertThat(subList, containsLongs(3, 4, 5, 1, 2, 3)));

		expecting(UnsupportedOperationException.class, subList::clear);
		twice(() -> assertThat(subList, containsLongs(3, 4, 5, 1, 2, 3)));
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
	public void containsLong() {
		assertThat(empty.containsLong(2), is(false));
		for (long i = 1; i < 5; i++)
			assertThat(list.containsLong(i), is(true));
		assertThat(list.containsLong(17), is(false));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void iteratorRemove() {
		LongIterator iterator = list.iterator();
		assertThat(iterator.nextLong(), is(1L));
		assertThat(iterator.nextLong(), is(2L));
		expecting(UnsupportedOperationException.class, iterator::remove);
		assertThat(iterator.nextLong(), is(3L));
		expecting(UnsupportedOperationException.class, iterator::remove);

		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void toLongArray() {
		assertArrayEquals(new long[0], empty.toLongArray());
		assertArrayEquals(new long[]{1, 2, 3, 4, 5, 1, 2, 3, 4, 5}, list.toLongArray());
	}

	@Test
	public void addLong() {
		expecting(UnsupportedOperationException.class, () -> empty.addLong(1));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addLong(6));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeLong() {
		assertThat(empty.removeLong(17), is(false));

		expecting(UnsupportedOperationException.class, () -> assertThat(list.removeLong(2), is(true)));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));

		assertThat(list.removeLong(17), is(false));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void containsAllLongs() {
		assertThat(empty.containsAllLongs(LongList.create(2, 3)), is(false));

		assertThat(list.containsAllLongs(LongList.create(2, 3)), is(true));
		assertThat(list.containsAllLongs(LongList.create(2, 17)), is(false));
	}

	@Test
	public void addAllLongs() {
		assertThat(empty.addAllLongs(LongList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAllLongs(LongList.create(1, 2)));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAllLongs(LongList.create(6, 7, 8)));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addAllLongsAt() {
		assertThat(empty.addAllLongsAt(0, LongList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAllLongsAt(0, LongList.create(1, 2)));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAllLongsAt(2, LongList.create(17, 18, 19)));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeAllLongs() {
		assertThat(empty.removeAllLongs(LongList.create(1, 2)), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.removeAllLongs(LongList.create(1, 2, 5)));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void retainAllLongs() {
		assertThat(empty.retainAllLongs(LongList.create(1, 2)), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.retainAllLongs(LongList.create(1, 2, 3)));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void replaceAllLongs() {
		empty.replaceAllLongs(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAllLongs(x -> x + 1));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void sortLongs() {
		expecting(UnsupportedOperationException.class, empty::sortLongs);
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, list::sortLongs);
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, list::clear);
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void testEquals() {
		assertThat(empty.equals(emptyList()), is(true));
		assertThat(empty.equals(asList(1L, 2L)), is(false));

		assertThat(list.equals(asList(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L)), is(true));
		assertThat(list.equals(asList(5L, 4L, 3L, 2L, 1L, 5L, 4L, 3L, 2L, 1L)), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(empty.hashCode(), is(emptyList().hashCode()));
		assertThat(list.hashCode(), is(asList(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L).hashCode()));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(list.toString(), is("[1, 2, 3, 4, 5, 1, 2, 3, 4, 5]"));
	}

	@Test
	public void getLong() {
		assertThat(list.getLong(0), is(1L));
		assertThat(list.getLong(2), is(3L));
		assertThat(list.getLong(4), is(5L));
		assertThat(list.getLong(5), is(1L));
		assertThat(list.getLong(7), is(3L));
		assertThat(list.getLong(9), is(5L));
	}

	@Test
	public void setLong() {
		expecting(UnsupportedOperationException.class, () -> list.setLong(2, 17));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addLongAt() {
		expecting(UnsupportedOperationException.class, () -> list.addLongAt(0, 17));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void indexOfLong() {
		assertThat(empty.indexOfLong(17), is(-1));

		assertThat(list.indexOfLong(1), is(0));
		assertThat(list.indexOfLong(3), is(2));
		assertThat(list.indexOfLong(5), is(4));
	}

	@Test
	public void lastIndexOfLong() {
		assertThat(empty.lastIndexOfLong(17), is(-1));

		assertThat(list.lastIndexOfLong(1), is(5));
		assertThat(list.lastIndexOfLong(3), is(7));
		assertThat(list.lastIndexOfLong(5), is(9));
	}

	@Test
	public void listIteratorEmpty() {
		LongListIterator emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(NoSuchElementException.class, emptyIterator::nextLong);
		expecting(NoSuchElementException.class, emptyIterator::previousLong);

		expecting(UnsupportedOperationException.class, () -> emptyIterator.add(17));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(NoSuchElementException.class, emptyIterator::nextLong);
		expecting(NoSuchElementException.class, emptyIterator::previousLong);

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		LongListIterator listIterator = list.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));
		assertThat(listIterator.nextLong(), is(1L));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.nextLong(), is(2L));

		expecting(UnsupportedOperationException.class, () -> listIterator.add(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.nextLong(), is(3L));

		expecting(UnsupportedOperationException.class, () -> listIterator.set(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.nextLong(), is(4L));

		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		LongListIterator listIterator = list.listIterator();

		AtomicInteger i = new AtomicInteger(0);
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextLong(), is((long) (i.get() % 5 + 1)));
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
		assertThat(list.stream().collect(Collectors.toList()), contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.parallelStream().collect(Collectors.toList()),
		           contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void removeLongsIf() {
		empty.removeLongsIf(x -> x == 1);
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.removeLongsIf(x -> x == 1));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void forEachLong() {
		empty.forEachLong(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicLong value = new AtomicLong(0);
		list.forEachLong(x -> assertThat(x, is(value.getAndIncrement() % 5 + 1)));
		assertThat(value.get(), is(10L));
	}
}
