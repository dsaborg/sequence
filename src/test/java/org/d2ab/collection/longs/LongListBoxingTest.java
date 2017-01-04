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
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.naturalOrder;
import static org.d2ab.collection.Arrayz.fill;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class LongListBoxingTest extends BaseBoxingTest {
	private final LongList backingEmpty = LongList.create();
	private final List<Long> empty = new LongList.Base() {
		@Override
		public LongIterator iterator() {
			return backingEmpty.iterator();
		}

		@Override
		public int size() {
			return backingEmpty.size();
		}
	};

	private final LongList backingList = LongList.create(1, 2, 3, 4, 5, 1, 2, 3, 4, 5);
	private final List<Long> list = new LongList.Base() {
		@Override
		public LongIterator iterator() {
			return backingList.iterator();
		}

		@Override
		public int size() {
			return backingList.size();
		}
	};

	@Test
	public void subList() {
		List<Long> subList = list.subList(2, 8);
		twice(() -> assertThat(subList, contains(3L, 4L, 5L, 1L, 2L, 3L)));

		assertThat(subList.remove(1), is(4L));
		twice(() -> assertThat(subList, contains(3L, 5L, 1L, 2L, 3L)));

		assertThat(subList.remove(5L), is(true));
		twice(() -> assertThat(subList, contains(3L, 1L, 2L, 3L)));

		Iterator<Long> subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.next(), is(3L));
		subListIterator.remove();
		twice(() -> assertThat(subList, contains(1L, 2L, 3L)));

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, contains(1L, 3L)));

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

		for (long i = 1; i < 5; i++)
			assertThat(list.contains(i), is(true));

		assertThat(list.contains(17), is(false));

		assertThat(list.contains(new Object()), is(false));

		assertThat(list.contains(null), is(false));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		expecting(NoSuchElementException.class, () -> empty.iterator().next());

		assertThat(list, contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void iteratorRemove() {
		Iterator<Long> iterator = list.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.remove();

		assertThat(list, contains(1L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void toArray() {
		assertArrayEquals(new Long[0], empty.toArray());
		assertArrayEquals(new Long[]{1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L}, list.toArray());
	}

	@Test
	public void toArrayEmptyTarget() {
		Long[] emptyTarget = new Long[0];
		assertThat(empty.toArray(emptyTarget), is(sameInstance(emptyTarget)));
		assertArrayEquals(new Long[]{1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L}, list.toArray(new Long[0]));
	}

	@Test
	public void toArraySmallerTarget() {
		assertArrayEquals(new Long[]{1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L}, list.toArray(new Long[9]));
	}

	@Test
	public void toArrayBiggerTarget() {
		assertArrayEquals(new Long[]{null, 17L}, empty.toArray(fill(new Long[2], 17L)));
		assertArrayEquals(new Long[]{1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L, null, 17L},
		                  list.toArray(fill(new Long[12], 17L)));
	}

	@Test
	public void addAt() {
		expecting(UnsupportedOperationException.class, () -> empty.add(1L));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.add(6L));
		assertThat(list, contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17L), is(false));

		assertThat(list.remove(2L), is(true));
		assertThat(list, contains(1L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));

		assertThat(list.remove(17L), is(false));
		assertThat(list, contains(1L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));

		assertThat(list.remove(new Object()), is(false));
		assertThat(list, contains(1L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));

		assertThat(list.remove(null), is(false));
		assertThat(list, contains(1L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(asList(2L, 3L)), is(false));

		assertThat(list.containsAll(asList(2L, 3L)), is(true));
		assertThat(list.containsAll(asList(2L, 17L)), is(false));
		assertThat(list.containsAll(singletonList(new Object())), is(false));
		assertThat(list.containsAll(singletonList(null)), is(false));
	}

	@Test
	public void addAllAt() {
		assertThat(empty.addAll(emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(LongList.of()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAll(asList(1L, 2L)));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAll(asList(6L, 7L, 8L)));
		assertThat(list, contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void addAll() {
		assertThat(empty.addAll(0, emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, LongList.of()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAll(0, asList(1L, 2L)));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAll(2, asList(17L, 18L, 19L)));
		assertThat(list, contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(asList(1L, 2L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(asList(1L, 2L, 5L)), is(true));
		assertThat(list, contains(3L, 4L, 3L, 4L));

		assertThat(list.removeAll(singletonList(new Object())), is(false));
		assertThat(list, contains(3L, 4L, 3L, 4L));

		assertThat(list.removeAll(singletonList(null)), is(false));
		assertThat(list, contains(3L, 4L, 3L, 4L));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(asList(1L, 2L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(asList(1L, 2L, 3L)), is(true));
		assertThat(list, contains(1L, 2L, 3L, 1L, 2L, 3L));
	}

	@Test
	public void retainAllObject() {
		assertThat(list.retainAll(singletonList(new Object())), is(true));
		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void retainAllNull() {
		assertThat(list.retainAll(singletonList(new Object())), is(true));
		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAll(x -> x + 1));
		assertThat(list, contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void sort() {
		expecting(UnsupportedOperationException.class, () -> empty.sort(naturalOrder()));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.sort(naturalOrder()));
		assertThat(list, contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty, is(emptyIterable()));

		list.clear();
		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void get() {
		assertThat(list.get(0), is(1L));
		assertThat(list.get(2), is(3L));
		assertThat(list.get(4), is(5L));
		assertThat(list.get(5), is(1L));
		assertThat(list.get(7), is(3L));
		assertThat(list.get(9), is(5L));
		expecting(IndexOutOfBoundsException.class, () -> list.get(10));
		expecting(IndexOutOfBoundsException.class, () -> list.get(11));
	}

	@Test
	public void set() {
		expecting(UnsupportedOperationException.class, () -> list.set(2, 17L));
		assertThat(list, contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void add() {
		expecting(UnsupportedOperationException.class, () -> list.add(0, 17L));
		assertThat(list, contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void indexOf() {
		assertThat(empty.indexOf(17L), is(-1));

		assertThat(list.indexOf(1L), is(0));
		assertThat(list.indexOf(3L), is(2));
		assertThat(list.indexOf(5L), is(4));
		assertThat(list.indexOf(17L), is(-1));
		assertThat(list.indexOf(new Object()), is(-1));
		assertThat(list.indexOf(null), is(-1));
	}

	@Test
	public void lastIndexOf() {
		assertThat(empty.lastIndexOf(17L), is(-1));

		assertThat(list.lastIndexOf(1L), is(5));
		assertThat(list.lastIndexOf(3L), is(7));
		assertThat(list.lastIndexOf(5L), is(9));
		assertThat(list.lastIndexOf(17L), is(-1));
		assertThat(list.lastIndexOf(new Object()), is(-1));
		assertThat(list.lastIndexOf(null), is(-1));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Long> emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyIterator::next);
		expecting(UnsupportedOperationException.class, emptyIterator::previous);

		expecting(UnsupportedOperationException.class, () -> emptyIterator.add(17L));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
		expecting(IndexOutOfBoundsException.class, () -> empty.listIterator(1));
	}

	@Test
	public void listIterator() {
		ListIterator<Long> listIterator = list.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));
		assertThat(listIterator.next(), is(1L));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.next(), is(2L));

		expecting(UnsupportedOperationException.class, () -> listIterator.add(17L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.next(), is(3L));

		expecting(UnsupportedOperationException.class, () -> listIterator.set(17L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.next(), is(4L));

		assertThat(list, contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void exhaustiveListIterator() {
		ListIterator<Long> listIterator = list.listIterator();

		AtomicInteger i = new AtomicInteger(0);
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is((long) (i.get() % 5 + 1)));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
			expecting(NoSuchElementException.class, listIterator::next);
		});
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Long> iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is((long) (i % 5 + 1)));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemoveAll() {
		ListIterator<Long> listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.next(), is((long) (i % 5 + 1)));
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
		assertThat(list.stream().collect(Collectors.toList()), contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.parallelStream().collect(Collectors.toList()),
		           contains(1L, 2L, 3L, 4L, 5L, 1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void removeIf() {
		empty.removeIf(x -> x == 1);
		assertThat(empty, is(emptyIterable()));

		list.removeIf(x -> x == 1);
		assertThat(list, contains(2L, 3L, 4L, 5L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicLong value = new AtomicLong(0);
		list.forEach(x -> assertThat(x, is(value.getAndIncrement() % 5 + 1)));
		assertThat(value.get(), is(10L));
	}
}
