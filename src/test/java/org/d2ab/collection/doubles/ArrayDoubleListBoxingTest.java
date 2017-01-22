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

package org.d2ab.collection.doubles;

import org.d2ab.collection.Lists;
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ArrayDoubleListBoxingTest extends BaseBoxingTest {
	private final List<Double> empty = ArrayDoubleList.create();
	private final List<Double> list = ArrayDoubleList.create(1, 2, 3, 4, 5);

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
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Double> emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		ListIterator<Double> listIterator = list.listIterator();

		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set(32.0));
		expecting(NoSuchElementException.class, listIterator::previous);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.add(33.0);
		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set(34.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.previous(), is(33.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.set(35.0);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.remove();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		assertThat(listIterator.next(), is(1.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is(2.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.next(), is(3.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.previous(), is(3.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.previous(), is(2.0));
		listIterator.set(17.0);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is(17.0));
		listIterator.add(18.0);
		listIterator.add(19.0);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(listIterator.next(), is(3.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(5));
		assertThat(listIterator.previousIndex(), is(4));

		assertThat(list, contains(1.0, 17.0, 18.0, 19.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void exhaustiveListIterator() {
		twice(() -> {
			ListIterator<Double> listIterator = list.listIterator();
			AtomicInteger i = new AtomicInteger();
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is((double) i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));
			expecting(NoSuchElementException.class, listIterator::next);

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previous(), is((double) i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
			expecting(NoSuchElementException.class, listIterator::previous);
		});
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Double> iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is((double) i + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(5));
		expecting(NoSuchElementException.class, iterator::next);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		ListIterator<Double> listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.next(), is((double) i + 1));
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
		ListIterator<Double> listIterator = list.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previous(), is((double) i + 1));
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
		Iterator<Double> it1 = list.iterator();
		list.add(17.0);
		expecting(ConcurrentModificationException.class, it1::next);

		Iterator<Double> it2 = list.iterator();
		list.remove(17.0);
		expecting(ConcurrentModificationException.class, it2::next);
	}

	@Test
	public void subList() {
		List<Double> list = DoubleList.create(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(-1, 2));
		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(2, 11));

		List<Double> subList = list.subList(2, 8);
		twice(() -> assertThat(subList, contains(3.0, 4.0, 5.0, 6.0, 7.0, 8.0)));

		assertThat(subList.remove(1), is(4.0));
		twice(() -> assertThat(subList, contains(3.0, 5.0, 6.0, 7.0, 8.0)));
		twice(() -> assertThat(list, contains(1.0, 2.0, 3.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)));

		assertThat(subList.remove(5.0), is(true));
		twice(() -> assertThat(subList, contains(3.0, 6.0, 7.0, 8.0)));
		twice(() -> assertThat(list, contains(1.0, 2.0, 3.0, 6.0, 7.0, 8.0, 9.0, 10.0)));

		Iterator<Double> subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.next(), is(3.0));
		subListIterator.remove();
		twice(() -> assertThat(subList, contains(6.0, 7.0, 8.0)));
		twice(() -> assertThat(list, contains(1.0, 2.0, 6.0, 7.0, 8.0, 9.0, 10.0)));

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, contains(7.0)));
		twice(() -> assertThat(list, contains(1.0, 2.0, 7.0, 9.0, 10.0)));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, contains(1.0, 2.0, 9.0, 10.0)));

		subList.add(17.0);
		twice(() -> assertThat(subList, contains(17.0)));
		twice(() -> assertThat(list, contains(1.0, 2.0, 17.0, 9.0, 10.0)));
	}

	@Test
	public void sort() {
		List<Double> list = ArrayDoubleList.create(32, 17, 5, 7, 19, 22);
		expecting(UnsupportedOperationException.class, () -> list.sort(naturalOrder()));
		assertThat(list, contains(32.0, 17.0, 5.0, 7.0, 19.0, 22.0));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(toList()), is(emptyIterable()));
		assertThat(list.stream().collect(toList()), contains(1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void parallelDoubleStream() {
		assertThat(empty.parallelStream().collect(toList()), is(emptyIterable()));
		assertThat(list.parallelStream().collect(toList()), contains(1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(list.toString(), is("[1.0, 2.0, 3.0, 4.0, 5.0]"));
	}

	@Test
	public void testEqualsHashCodeAgainstList() {
		List<Double> list2 = new ArrayList<>(Lists.of(1.0, 2.0, 3.0, 4.0, 5.0, 17.0));
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));

		list2.remove(17.0);

		assertThat(list, is(equalTo(list2)));
		assertThat(list.hashCode(), is(list2.hashCode()));

		Lists.reverse(list2);
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));
	}

	@Test
	public void testEqualsHashCodeAgainstDoubleList() {
		List<Double> list2 = DoubleList.create(1, 2, 3, 4, 5, 17);
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));

		list2.remove(17.0);

		assertThat(list, is(equalTo(list2)));
		assertThat(list.hashCode(), is(list2.hashCode()));

		Lists.reverse(list2);
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));
	}

	@Test
	public void lastIndexOf() {
		assertThat(empty.lastIndexOf(17.0), is(-1));

		assertThat(list.lastIndexOf(17.0), is(-1));
		assertThat(list.lastIndexOf(2.0), is(1));
	}

	@Test
	public void indexOf() {
		assertThat(empty.indexOf(17.0), is(-1));

		assertThat(list.indexOf(17.0), is(-1));
		assertThat(list.indexOf(2.0), is(1));
	}

	@Test
	public void get() {
		expecting(IndexOutOfBoundsException.class, () -> empty.get(-1));
		expecting(IndexOutOfBoundsException.class, () -> empty.get(0));
		expecting(IndexOutOfBoundsException.class, () -> empty.get(1));
		assertThat(empty, is(emptyIterable()));

		expecting(IndexOutOfBoundsException.class, () -> list.get(-1));
		expecting(IndexOutOfBoundsException.class, () -> list.get(5));
		expecting(IndexOutOfBoundsException.class, () -> list.get(6));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0));

		assertThat(list.get(0), is(1.0));
		assertThat(list.get(2), is(3.0));
		assertThat(list.get(4), is(5.0));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void set() {
		expecting(IndexOutOfBoundsException.class, () -> empty.set(2, 17.0));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.set(2, 17.0), is(3.0));
		assertThat(list, contains(1.0, 2.0, 17.0, 4.0, 5.0));
	}

	@Test
	public void add() {
		assertThat(empty.add(17.0), is(true));
		assertThat(empty, contains(17.0));

		assertThat(list.add(17.0), is(true));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0, 17.0));
	}

	@Test
	public void addAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.add(-1, 17.0));
		expecting(IndexOutOfBoundsException.class, () -> empty.add(1, 17.0));
		expecting(IndexOutOfBoundsException.class, () -> empty.add(2, 17.0));
		assertThat(empty, is(emptyIterable()));

		empty.add(0, 17.0);
		assertThat(empty, contains(17.0));

		expecting(IndexOutOfBoundsException.class, () -> list.add(-1, 17.0));
		expecting(IndexOutOfBoundsException.class, () -> list.add(6, 17.0));
		expecting(IndexOutOfBoundsException.class, () -> list.add(7, 17.0));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0));

		list.add(0, 16.0);
		assertThat(list, contains(16.0, 1.0, 2.0, 3.0, 4.0, 5.0));

		list.add(3, 17.0);
		assertThat(list, contains(16.0, 1.0, 2.0, 17.0, 3.0, 4.0, 5.0));

		list.add(7, 18.0);
		assertThat(list, contains(16.0, 1.0, 2.0, 17.0, 3.0, 4.0, 5.0, 18.0));
	}

	@Test
	public void addAll() {
		assertThat(empty.addAll(Lists.of()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(Lists.of(1.0, 2.0, 3.0)), is(true));
		assertThat(empty, contains(1.0, 2.0, 3.0));

		assertThat(list.addAll(Lists.of(6.0, 7.0, 8.0)), is(true));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
	}

	@Test
	public void addAllDoubleCollection() {
		assertThat(empty.addAll(new SortedListDoubleSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(new SortedListDoubleSet(1, 2, 3)), is(true));
		assertThat(empty, contains(1.0, 2.0, 3.0));

		assertThat(list.addAll(new SortedListDoubleSet(6, 7, 8)), is(true));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
	}

	@Test
	public void addAllArrayDoubleList() {
		assertThat(empty.addAll(ArrayDoubleList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(ArrayDoubleList.create(1, 2, 3)), is(true));
		assertThat(empty, contains(1.0, 2.0, 3.0));

		assertThat(list.addAll(ArrayDoubleList.create(6, 7, 8)), is(true));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
	}

	@Test
	public void addAllAt() {
		assertThat(empty.addAll(0, Lists.of()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, Lists.of(1.0, 2.0, 3.0)), is(true));
		assertThat(empty, contains(1.0, 2.0, 3.0));

		assertThat(list.addAll(2, Lists.of(17.0, 18.0, 19.0)), is(true));
		assertThat(list, contains(1.0, 2.0, 17.0, 18.0, 19.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void addAllAtDoubleCollection() {
		assertThat(empty.addAll(0, new SortedListDoubleSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, new SortedListDoubleSet(1, 2, 3)), is(true));
		assertThat(empty, contains(1.0, 2.0, 3.0));

		assertThat(list.addAll(2, new SortedListDoubleSet(17, 18, 19)), is(true));
		assertThat(list, contains(1.0, 2.0, 17.0, 18.0, 19.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void addAllAtArrayDoubleList() {
		assertThat(empty.addAll(0, ArrayDoubleList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, ArrayDoubleList.create(1, 2, 3)), is(true));
		assertThat(empty, contains(1.0, 2.0, 3.0));

		assertThat(list.addAll(2, ArrayDoubleList.create(17, 18, 19)), is(true));
		assertThat(list, contains(1.0, 2.0, 17.0, 18.0, 19.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17.0), is(false));
		assertThat(empty.remove(new Object()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove(17.0), is(false));
		assertThat(list.remove(new Object()), is(false));
		assertThat(list.remove(2.0), is(true));
		assertThat(list, contains(1.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void removeAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.remove(-1));
		expecting(IndexOutOfBoundsException.class, () -> empty.remove(0));
		expecting(IndexOutOfBoundsException.class, () -> empty.remove(1));
		assertThat(empty, is(emptyIterable()));

		expecting(IndexOutOfBoundsException.class, () -> list.remove(-1));
		expecting(IndexOutOfBoundsException.class, () -> list.remove(5));
		expecting(IndexOutOfBoundsException.class, () -> list.remove(6));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0));

		assertThat(list.remove(0), is(1.0));
		assertThat(list, contains(2.0, 3.0, 4.0, 5.0));

		assertThat(list.remove(1), is(3.0));
		assertThat(list, contains(2.0, 4.0, 5.0));

		assertThat(list.remove(2), is(5.0));
		assertThat(list, contains(2.0, 4.0));
	}

	@Test
	public void boxedContains() {
		assertThat(empty.contains(17.0), is(false));
		assertThat(empty.contains(new Object()), is(false));

		assertThat(list.contains(17.0), is(false));
		assertThat(list.contains(new Object()), is(false));
		assertThat(list.contains(2.0), is(true));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(Lists.of(17.0, 18.0, 19.0, new Object())), is(false));

		assertThat(list.containsAll(Lists.of(17.0, 18.0, 19.0, new Object())), is(false));
		assertThat(list.containsAll(Lists.of(1.0, 2.0, 3.0)), is(true));
	}

	@Test
	public void containsAllArrayDoubleList() {
		assertThat(empty.containsAll(ArrayDoubleList.create(17, 18, 19)), is(false));

		assertThat(list.containsAll(ArrayDoubleList.create(17, 18, 19)), is(false));
		assertThat(list.containsAll(ArrayDoubleList.create(1, 2, 3)), is(true));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(Lists.of(1.0, 2.0, 3.0, 17.0)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(Lists.of(17.0, 18.0, 19.0)), is(false));
		assertThat(list.removeAll(Lists.of(1.0, 2.0, 3.0, 17.0)), is(true));
		assertThat(list, contains(4.0, 5.0));
	}

	@Test
	public void removeAllArrayDoubleList() {
		assertThat(empty.removeAll(ArrayDoubleList.create(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(ArrayDoubleList.create(17, 18, 19)), is(false));
		assertThat(list.removeAll(ArrayDoubleList.create(1, 2, 3, 17)), is(true));
		assertThat(list, contains(4.0, 5.0));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeIf(x -> x > 5), is(false));
		assertThat(list.removeIf(x -> x > 3), is(true));
		assertThat(list, contains(1.0, 2.0, 3.0));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(Lists.of(1.0, 2.0, 3.0, 17.0)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(Lists.of(1.0, 2.0, 3.0, 17.0)), is(true));
		assertThat(list, contains(1.0, 2.0, 3.0));
	}

	@Test
	public void retainAllArrayDoubleList() {
		assertThat(empty.retainAll(ArrayDoubleList.create(1, 2, 3, 17)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(ArrayDoubleList.create(1, 2, 3, 17)), is(true));
		assertThat(list, contains(1.0, 2.0, 3.0));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		list.replaceAll(x -> x + 1);
		assertThat(list, contains(2.0, 3.0, 4.0, 5.0, 6.0));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		list.forEach(x -> assertThat(x, is((double) value.getAndIncrement())));
		assertThat(value.get(), is(6));
	}
}
