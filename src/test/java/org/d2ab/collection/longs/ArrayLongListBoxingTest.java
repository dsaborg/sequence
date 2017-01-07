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
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ArrayLongListBoxingTest extends BaseBoxingTest {
	private final List<Long> empty = ArrayLongList.create();
	private final List<Long> list = ArrayLongList.create(1, 2, 3, 4, 5);

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
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, contains(1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Long> emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyIterator::next);
		expecting(NoSuchElementException.class, emptyIterator::previous);

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		ListIterator<Long> listIterator = list.listIterator();

		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set(32L));
		expecting(NoSuchElementException.class, listIterator::previous);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.add(33L);
		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set(34L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.previous(), is(33L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.set(35L);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.remove();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		assertThat(listIterator.next(), is(1L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is(2L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.next(), is(3L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.previous(), is(3L));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.previous(), is(2L));
		listIterator.set(17L);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is(17L));
		listIterator.add(18L);
		listIterator.add(19L);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(listIterator.next(), is(3L));

		assertThat(list, contains(1L, 17L, 18L, 19L, 3L, 4L, 5L));
	}

	@Test
	public void exhaustiveListIterator() {
		twice(() -> {
			ListIterator<Long> listIterator = list.listIterator();
			AtomicInteger i = new AtomicInteger();
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is((long) i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));
			expecting(NoSuchElementException.class, listIterator::next);

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previous(), is((long) i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
			expecting(NoSuchElementException.class, listIterator::previous);
		});
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Long> iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is((long) i + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(5));
		expecting(NoSuchElementException.class, iterator::next);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		ListIterator<Long> listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.next(), is((long) i + 1));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			i++;
		}
		assertThat(i, is(5));
		expecting(NoSuchElementException.class, listIterator::next);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemoveBackwards() {
		int i = 5;
		ListIterator<Long> listIterator = list.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previous(), is((long) i + 1));
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
		}
		assertThat(i, is(0));
		expecting(NoSuchElementException.class, listIterator::previous);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void iteratorFailFast() {
		Iterator<Long> it1 = list.iterator();
		list.add(17L);
		expecting(ConcurrentModificationException.class, it1::next);

		Iterator<Long> it2 = list.iterator();
		list.remove(17L);
		expecting(ConcurrentModificationException.class, it2::next);
	}

	@Test
	public void subList() {
		List<Long> list = LongList.create(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(-1, 2));
		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(2, 11));

		List<Long> subList = list.subList(2, 8);
		assertThat(subList.size(), is(6));
		twice(() -> assertThat(subList, contains(3L, 4L, 5L, 6L, 7L, 8L)));

		assertThat(subList.remove(1), is(4L));
		twice(() -> assertThat(subList, contains(3L, 5L, 6L, 7L, 8L)));
		twice(() -> assertThat(list, contains(1L, 2L, 3L, 5L, 6L, 7L, 8L, 9L, 10L)));

		assertThat(subList.remove(5L), is(true));
		twice(() -> assertThat(subList, contains(3L, 6L, 7L, 8L)));
		twice(() -> assertThat(list, contains(1L, 2L, 3L, 6L, 7L, 8L, 9L, 10L)));

		Iterator<Long> subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.next(), is(3L));
		subListIterator.remove();
		twice(() -> assertThat(subList, contains(6L, 7L, 8L)));
		twice(() -> assertThat(list, contains(1L, 2L, 6L, 7L, 8L, 9L, 10L)));

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, contains(7L)));
		twice(() -> assertThat(list, contains(1L, 2L, 7L, 9L, 10L)));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, contains(1L, 2L, 9L, 10L)));

		subList.add(17L);
		twice(() -> assertThat(subList, contains(17L)));
		twice(() -> assertThat(list, contains(1L, 2L, 17L, 9L, 10L)));
	}

	@Test
	public void sort() {
		List<Long> list = ArrayLongList.create(32, 17, 5, 7, 19, 22);
		expecting(UnsupportedOperationException.class, () -> list.sort(naturalOrder()));
		assertThat(list, contains(32L, 17L, 5L, 7L, 19L, 22L));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(toList()), is(emptyIterable()));
		assertThat(list.stream().collect(toList()), contains(1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void parallelLongStream() {
		assertThat(empty.parallelStream().collect(toList()), is(emptyIterable()));
		assertThat(list.parallelStream().collect(toList()), contains(1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(list.toString(), is("[1, 2, 3, 4, 5]"));
	}

	@Test
	public void testEqualsHashCodeAgainstList() {
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
		List<Long> list2 = LongList.create(1, 2, 3, 4, 5, 17);
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
	public void lastIndexOf() {
		assertThat(empty.lastIndexOf(17L), is(-1));

		assertThat(list.lastIndexOf(17L), is(-1));
		assertThat(list.lastIndexOf(2L), is(1));
	}

	@Test
	public void indexOf() {
		assertThat(empty.indexOf(17L), is(-1));

		assertThat(list.indexOf(17L), is(-1));
		assertThat(list.indexOf(2L), is(1));
	}

	@Test
	public void get() {
		expecting(IndexOutOfBoundsException.class, () -> empty.get(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.get(2), is(3L));
	}

	@Test
	public void set() {
		expecting(IndexOutOfBoundsException.class, () -> empty.set(2, 17L));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.set(2, 17L), is(3L));
		assertThat(list, contains(1L, 2L, 17L, 4L, 5L));
	}

	@Test
	public void add() {
		assertThat(empty.add(17L), is(true));
		assertThat(empty, contains(17L));

		assertThat(list.add(17L), is(true));
		assertThat(list, contains(1L, 2L, 3L, 4L, 5L, 17L));
	}

	@Test
	public void addAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.add(2, 17L));
		assertThat(empty, is(emptyIterable()));

		empty.add(0, 17L);
		assertThat(empty, contains(17L));

		list.add(2, 17L);
		assertThat(list, contains(1L, 2L, 17L, 3L, 4L, 5L));
	}

	@Test
	public void addAll() {
		assertThat(empty.addAll(emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(Arrays.asList(1L, 2L, 3L)), is(true));
		assertThat(empty, contains(1L, 2L, 3L));

		assertThat(list.addAll(Arrays.asList(6L, 7L, 8L)), is(true));
		assertThat(list, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
	}

	@Test
	public void addAllLongsLongCollection() {
		assertThat(empty.addAll(new BitLongSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(new BitLongSet(1, 2, 3)), is(true));
		assertThat(empty, contains(1L, 2L, 3L));

		assertThat(list.addAll(new BitLongSet(6, 7, 8)), is(true));
		assertThat(list, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
	}

	@Test
	public void addAllLongsArrayLongList() {
		assertThat(empty.addAll(ArrayLongList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(ArrayLongList.create(1, 2, 3)), is(true));
		assertThat(empty, contains(1L, 2L, 3L));

		assertThat(list.addAll(ArrayLongList.create(6, 7, 8)), is(true));
		assertThat(list, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
	}

	@Test
	public void addAllAt() {
		assertThat(empty.addAll(0, emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, Arrays.asList(1L, 2L, 3L)), is(true));
		assertThat(empty, contains(1L, 2L, 3L));

		assertThat(list.addAll(2, Arrays.asList(17L, 18L, 19L)), is(true));
		assertThat(list, contains(1L, 2L, 17L, 18L, 19L, 3L, 4L, 5L));
	}

	@Test
	public void addAllLongsAtLongCollection() {
		assertThat(empty.addAll(0, new BitLongSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, new BitLongSet(1, 2, 3)), is(true));
		assertThat(empty, contains(1L, 2L, 3L));

		assertThat(list.addAll(2, new BitLongSet(17, 18, 19)), is(true));
		assertThat(list, contains(1L, 2L, 17L, 18L, 19L, 3L, 4L, 5L));
	}

	@Test
	public void addAllLongsAtArrayLongList() {
		assertThat(empty.addAll(0, ArrayLongList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, ArrayLongList.create(1, 2, 3)), is(true));
		assertThat(empty, contains(1L, 2L, 3L));

		assertThat(list.addAll(2, ArrayLongList.create(17, 18, 19)), is(true));
		assertThat(list, contains(1L, 2L, 17L, 18L, 19L, 3L, 4L, 5L));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17L), is(false));
		assertThat(empty.remove(new Object()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove(17L), is(false));
		assertThat(list.remove(new Object()), is(false));
		assertThat(list.remove(2L), is(true));
		assertThat(list, contains(1L, 3L, 4L, 5L));
	}

	@Test
	public void removeAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.remove(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove(2), is(3L));
		assertThat(list, contains(1L, 2L, 4L, 5L));
	}

	@Test
	public void boxedContains() {
		assertThat(empty.contains(17L), is(false));
		assertThat(empty.contains(new Object()), is(false));

		assertThat(list.contains(17L), is(false));
		assertThat(list.contains(new Object()), is(false));
		assertThat(list.contains(2L), is(true));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(Arrays.asList(17L, 18L, 19L, new Object())), is(false));

		assertThat(list.containsAll(Arrays.asList(17L, 18L, 19L, new Object())), is(false));
		assertThat(list.containsAll(Arrays.asList(1L, 2L, 3L)), is(true));
	}

	@Test
	public void containsAllLongsCollection() {
		assertThat(empty.containsAll(ArrayLongList.create(17, 18, 19)), is(false));

		assertThat(list.containsAll(ArrayLongList.create(17, 18, 19)), is(false));
		assertThat(list.containsAll(ArrayLongList.create(1, 2, 3)), is(true));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(Arrays.asList(1L, 2L, 3L, 17L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(Arrays.asList(17L, 18L, 19L)), is(false));
		assertThat(list.removeAll(Arrays.asList(1L, 2L, 3L, 17L)), is(true));
		assertThat(list, contains(4L, 5L));
	}

	@Test
	public void removeAllLongsCollection() {
		assertThat(empty.removeAll(ArrayLongList.create(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(ArrayLongList.create(17, 18, 19)), is(false));
		assertThat(list.removeAll(ArrayLongList.create(1, 2, 3, 17)), is(true));
		assertThat(list, contains(4L, 5L));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeIf(x -> x > 5), is(false));
		assertThat(list.removeIf(x -> x > 3), is(true));
		assertThat(list, contains(1L, 2L, 3L));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(Arrays.asList(1L, 2L, 3L, 17L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(Arrays.asList(1L, 2L, 3L, 17L)), is(true));
		assertThat(list, contains(1L, 2L, 3L));
	}

	@Test
	public void retainAllLongsCollection() {
		assertThat(empty.retainAll(ArrayLongList.create(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(ArrayLongList.create(1, 2, 3, 17)), is(true));
		assertThat(list, contains(1L, 2L, 3L));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		list.replaceAll(x -> x + 1);
		assertThat(list, contains(2L, 3L, 4L, 5L, 6L));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicLong value = new AtomicLong(1);
		list.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(6L));
	}
}
