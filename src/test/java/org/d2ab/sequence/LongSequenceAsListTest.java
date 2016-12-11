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

import org.d2ab.collection.longs.ArrayLongList;
import org.d2ab.collection.longs.LongList;
import org.d2ab.collection.longs.LongListIterator;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.test.StrictLongIterable;
import org.junit.Test;

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

public class LongSequenceAsListTest {
	private final LongSequence empty = LongSequence.from(StrictLongIterable.from(ArrayLongList.create()));
	private final LongList emptyList = empty.asList();

	private final LongSequence sequence = LongSequence.from(
			StrictLongIterable.from(ArrayLongList.create(1, 2, 3, 4, 5, 1, 2, 3, 4, 5)));
	private final LongList list = sequence.asList();

	@Test
	public void subList() {
		LongList subList = list.subList(2, 8);
		twice(() -> assertThat(subList, containsLongs(3, 4, 5, 1, 2, 3)));

		assertThat(subList.removeLongAt(1), is(4L));
		twice(() -> assertThat(subList, containsLongs(3, 5, 1, 2, 3)));
		twice(() -> assertThat(sequence, containsLongs(1, 2, 3, 5, 1, 2, 3, 4, 5)));

		assertThat(subList.removeLong(5), is(true));
		twice(() -> assertThat(subList, containsLongs(3, 1, 2, 3)));
		twice(() -> assertThat(sequence, containsLongs(1, 2, 3, 1, 2, 3, 4, 5)));

		LongIterator subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextLong(), is(3L));
		subListIterator.remove();
		twice(() -> assertThat(subList, containsLongs(1, 2, 3)));
		twice(() -> assertThat(sequence, containsLongs(1, 2, 1, 2, 3, 4, 5)));

		subList.removeLongsIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, containsLongs(1, 3)));
		twice(() -> assertThat(sequence, containsLongs(1, 2, 1, 3, 4, 5)));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(sequence, containsLongs(1, 2, 4, 5)));
	}

	@Test
	public void size() {
		assertThat(emptyList.size(), is(0));
		assertThat(list.size(), is(10));
	}

	@Test
	public void isEmpty() {
		assertThat(emptyList.isEmpty(), is(true));
		assertThat(list.isEmpty(), is(false));
	}

	@Test
	public void containsLong() {
		assertThat(emptyList.containsLong(2), is(false));
		for (long i = 1; i < 5; i++)
			assertThat(list.containsLong(i), is(true));
		assertThat(list.containsLong(17), is(false));
	}

	@Test
	public void iterator() {
		assertThat(emptyList, is(emptyIterable()));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void iteratorRemove() {
		LongIterator iterator = list.iterator();
		iterator.nextLong();
		iterator.nextLong();
		iterator.remove();
		iterator.nextLong();
		iterator.remove();

		assertThat(list, containsLongs(1, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, containsLongs(1, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void toLongArray() {
		assertArrayEquals(new long[0], emptyList.toLongArray());
		assertArrayEquals(new long[]{1, 2, 3, 4, 5, 1, 2, 3, 4, 5}, list.toLongArray());
	}

	@Test
	public void addLong() {
		expecting(UnsupportedOperationException.class, () -> emptyList.addLong(1));
		assertThat(emptyList, is(emptyIterable()));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addLong(6));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeLong() {
		assertThat(emptyList.removeLong(17), is(false));

		assertThat(list.removeLong(2), is(true));
		assertThat(list, containsLongs(1, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, containsLongs(1, 3, 4, 5, 1, 2, 3, 4, 5));

		assertThat(list.removeLong(17), is(false));
		assertThat(list, containsLongs(1, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, containsLongs(1, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void containsAllLongs() {
		assertThat(emptyList.containsAllLongs(LongList.create(2, 3)), is(false));

		assertThat(list.containsAllLongs(LongList.create(2, 3)), is(true));
		assertThat(list.containsAllLongs(LongList.create(2, 17)), is(false));
	}

	@Test
	public void addAllLongs() {
		assertThat(emptyList.addAllLongs(LongList.create()), is(false));
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> emptyList.addAllLongs(LongList.create(1, 2)));
		assertThat(emptyList, is(emptyIterable()));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAllLongs(LongList.create(6, 7, 8)));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addAllLongsAt() {
		assertThat(emptyList.addAllLongsAt(0, LongList.create()), is(false));
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> emptyList.addAllLongsAt(0, LongList.create(1, 2)));
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAllLongsAt(2, LongList.create(17, 18, 19)));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeAllLongs() {
		assertThat(emptyList.removeAllLongs(LongList.create(1, 2)), is(false));
		assertThat(emptyList, is(emptyIterable()));

		assertThat(list.removeAllLongs(LongList.create(1, 2, 5)), is(true));
		assertThat(list, containsLongs(3, 4, 3, 4));
		assertThat(sequence, containsLongs(3, 4, 3, 4));
	}

	@Test
	public void retainAllLongs() {
		assertThat(emptyList.retainAllLongs(LongList.create(1, 2)), is(false));
		assertThat(emptyList, is(emptyIterable()));

		assertThat(list.retainAllLongs(LongList.create(1, 2, 3)), is(true));
		assertThat(list, containsLongs(1, 2, 3, 1, 2, 3));
		assertThat(sequence, containsLongs(1, 2, 3, 1, 2, 3));
	}

	@Test
	public void replaceAllLongs() {
		emptyList.replaceAllLongs(x -> x + 1);
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAllLongs(x -> x + 1));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void sortLongs() {
		expecting(UnsupportedOperationException.class, emptyList::sortLongs);
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, list::sortLongs);
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
		assertThat(sequence, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void clear() {
		emptyList.clear();
		assertThat(emptyList, is(emptyIterable()));

		list.clear();
		assertThat(list, is(emptyIterable()));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void testEquals() {
		assertThat(emptyList.equals(emptyList()), is(true));
		assertThat(emptyList.equals(asList(1, 2)), is(false));

		assertThat(list.equals(asList(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L)), is(true));
		assertThat(list.equals(asList(5L, 4L, 3L, 2L, 1L, 5L, 4L, 3L, 2L, 1L)), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(emptyList.hashCode(), is(1));
		assertThat(list.hashCode(), is(-980763487));
	}

	@Test
	public void testToString() {
		assertThat(emptyList.toString(), is("[]"));
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
		assertThat(sequence, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addLongAt() {
		expecting(UnsupportedOperationException.class, () -> list.addLongAt(0, 17));
		assertThat(list, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void indexOfLong() {
		assertThat(emptyList.indexOfLong(17), is(-1));

		assertThat(list.indexOfLong(1), is(0));
		assertThat(list.indexOfLong(3), is(2));
		assertThat(list.indexOfLong(5), is(4));
	}

	@Test
	public void lastIndexOfLong() {
		assertThat(emptyList.lastIndexOfLong(17), is(-1));

		assertThat(list.lastIndexOfLong(1), is(5));
		assertThat(list.lastIndexOfLong(3), is(7));
		assertThat(list.lastIndexOfLong(5), is(9));
	}

	@Test
	public void listIteratorEmpty() {
		LongListIterator emptyIterator = emptyList.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(UnsupportedOperationException.class, () -> emptyIterator.add(17));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(emptyList, is(emptyIterable()));
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
		assertThat(sequence, containsLongs(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
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
	public void listIteratorRemoveAll() {
		LongIterator iterator = list.iterator();

		long i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.nextLong(), is(i % 5 + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10L));

		assertThat(list, is(emptyIterable()));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		LongListIterator listIterator = list.listIterator();

		long i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.nextLong(), is(i % 5 + 1));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			i++;
		}
		assertThat(i, is(10L));

		assertThat(list, is(emptyIterable()));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void stream() {
		assertThat(emptyList.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.stream().collect(Collectors.toList()), contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void parallelStream() {
		assertThat(emptyList.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.parallelStream().collect(Collectors.toList()), contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void removeLongsIf() {
		emptyList.removeLongsIf(x -> x == 1);
		assertThat(emptyList, is(emptyIterable()));

		list.removeLongsIf(x -> x == 1);
		assertThat(list, containsLongs(2, 3, 4, 5, 2, 3, 4, 5));
	}

	@Test
	public void forEachLong() {
		emptyList.forEachLong(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicLong value = new AtomicLong(0);
		list.forEachLong(x -> assertThat(x, is(value.getAndIncrement() % 5 + 1)));
		assertThat(value.get(), is(10L));
	}
}
