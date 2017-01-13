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

import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Comparator.naturalOrder;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CollectionDoubleListBoxingTest extends BaseBoxingTest {
	private final List<Double> empty = CollectionDoubleList.from(DoubleList.create());
	private final List<Double> list = CollectionDoubleList.from(DoubleList.create(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));

	@Test
	public void subList() {
		List<Double> subList = list.subList(2, 8);
		twice(() -> assertThat(subList, contains(3.0, 4.0, 5.0, 1.0, 2.0, 3.0)));

		assertThat(subList.remove(1), is(4.0));
		twice(() -> assertThat(subList, contains(3.0, 5.0, 1.0, 2.0, 3.0)));

		assertThat(subList.remove(5.0), is(true));
		twice(() -> assertThat(subList, contains(3.0, 1.0, 2.0, 3.0)));

		Iterator<Double> subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.next(), is(3.0));
		subListIterator.remove();
		twice(() -> assertThat(subList, contains(1.0, 2.0, 3.0)));

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, contains(1.0, 3.0)));

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
		for (double i = 1; i < 5; i++)
			assertThat(list.contains(i), is(true));
		assertThat(list.contains(17), is(false));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void iteratorRemove() {
		Iterator<Double> iterator = list.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.remove();

		assertThat(list, contains(1.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void add() {
		assertThat(empty.add(1.0), is(true));
		assertThat(empty, contains(1.0));

		assertThat(list.add(6.0), is(true));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17.0), is(false));

		assertThat(list.remove(2.0), is(true));
		assertThat(list, contains(1.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0));

		assertThat(list.remove(17.0), is(false));
		assertThat(list, contains(1.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(DoubleList.create(2, 3)), is(false));

		assertThat(list.containsAll(DoubleList.create(2, 3)), is(true));
		assertThat(list.containsAll(DoubleList.create(2, 17)), is(false));
	}

	@Test
	public void addAllDoubleList() {
		assertThat(empty.addAll(DoubleList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(DoubleList.create(1, 2)), is(true));
		assertThat(empty, contains(1.0, 2.0));

		assertThat(list.addAll(DoubleList.create(6, 7, 8)), is(true));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
	}

	@Test
	public void addAllCollection() {
		assertThat(empty.addAll(DoubleList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(DoubleList.create(1, 2)), is(true));
		assertThat(empty, contains(1.0, 2.0));

		assertThat(list.addAll(DoubleList.create(6, 7, 8)), is(true));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
	}

	@Test
	public void addAll() {
		assertThat(empty.addAll(0, DoubleList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAll(0, DoubleList.create(1, 2)));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAll(2, DoubleList.create(17, 18, 19)));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(DoubleList.create(1, 2)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(DoubleList.create(1, 2, 5)), is(true));
		assertThat(list, contains(3.0, 4.0, 3.0, 4.0));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(DoubleList.create(1, 2)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(DoubleList.create(1, 2, 3)), is(true));
		assertThat(list, contains(1.0, 2.0, 3.0, 1.0, 2.0, 3.0));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAll(x -> x + 1));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void sort() {
		expecting(UnsupportedOperationException.class, () -> empty.sort(naturalOrder()));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.sort(naturalOrder()));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0));
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
		assertThat(empty.equals(asList(1.0, 2.0)), is(false));

		assertThat(list.equals(asList(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0)), is(true));
		assertThat(list.equals(asList(5.0, 4.0, 3.0, 2.0, 1.0, 5.0, 4.0, 3.0, 2.0, 1.0)), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(empty.hashCode(), is(emptyList().hashCode()));
		assertThat(list.hashCode(), is(asList(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0).hashCode()));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(list.toString(), is("[1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0]"));
	}

	@Test
	public void get() {
		assertThat(list.get(0), is(1.0));
		assertThat(list.get(2), is(3.0));
		assertThat(list.get(4), is(5.0));
		assertThat(list.get(5), is(1.0));
		assertThat(list.get(7), is(3.0));
		assertThat(list.get(9), is(5.0));
	}

	@Test
	public void set() {
		expecting(UnsupportedOperationException.class, () -> list.set(2, 17.0));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void addAt() {
		expecting(UnsupportedOperationException.class, () -> list.add(0, 17.0));
		assertThat(list, contains(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void indexOf() {
		assertThat(empty.indexOf(17.0), is(-1));

		assertThat(list.indexOf(17.0), is(-1));
		assertThat(list.indexOf(1.0), is(0));
		assertThat(list.indexOf(3.0), is(2));
		assertThat(list.indexOf(5.0), is(4));
	}

	@Test
	public void lastIndexOf() {
		assertThat(empty.lastIndexOf(17.0), is(-1));

		assertThat(list.lastIndexOf(17.0), is(-1));
		assertThat(list.lastIndexOf(1.0), is(5));
		assertThat(list.lastIndexOf(3.0), is(7));
		assertThat(list.lastIndexOf(5.0), is(9));
	}

	@Test
	public void listIteratorAtEnd() {
		ListIterator<Double> listIterator = list.listIterator(10);
		assertThat(listIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, listIterator::next);
	}

	@Test
	public void listIteratorAfterEnd() {
		expecting(IndexOutOfBoundsException.class, () -> list.listIterator(11));
	}

	@Test
	public void listIterator() {
		ListIterator<Double> listIterator = list.listIterator();

		expecting(IllegalStateException.class, listIterator::remove);
		expecting(UnsupportedOperationException.class, () -> listIterator.set(17.0));
		expecting(UnsupportedOperationException.class, listIterator::previous);
		assertThat(listIterator.hasNext(), is(true));
		expecting(UnsupportedOperationException.class, listIterator::hasPrevious);
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		expecting(UnsupportedOperationException.class, () -> listIterator.add(18.0));

		assertThat(listIterator.next(), is(1.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is(2.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.next(), is(3.0));
		listIterator.remove();
		expecting(IllegalStateException.class, listIterator::remove);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		expecting(UnsupportedOperationException.class, listIterator::previous);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		expecting(UnsupportedOperationException.class, () -> listIterator.set(19.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.next(), is(4.0));
		expecting(UnsupportedOperationException.class, () -> listIterator.add(20.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.next(), is(5.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(list, contains(1.0, 2.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Double> iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is((double) (i % 5 + 1)));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));
		expecting(NoSuchElementException.class, iterator::next);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Double> emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, emptyIterator::next);
		expecting(UnsupportedOperationException.class, emptyIterator::hasPrevious);
		expecting(UnsupportedOperationException.class, emptyIterator::previous);
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(UnsupportedOperationException.class, () -> emptyIterator.add(17.0));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void exhaustiveListIterator() {
		ListIterator<Double> listIterator = list.listIterator();

		AtomicInteger i = new AtomicInteger(0);
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is((double) (i.get() % 5 + 1)));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
		});
	}

	@Test
	public void listIteratorRemoveAll() {
		Iterator<Double> iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is((double) (i % 5 + 1)));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		ListIterator<Double> listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.next(), is((double) (i % 5 + 1)));
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
		assertThat(list.stream().collect(Collectors.toList()),
		           contains(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.parallelStream().collect(Collectors.toList()),
		           contains(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void removeIf() {
		empty.removeIf(x -> x == 1);
		assertThat(empty, is(emptyIterable()));

		list.removeIf(x -> x == 1);
		assertThat(list, contains(2.0, 3.0, 4.0, 5.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger i = new AtomicInteger(0);
		list.forEach(x -> assertThat(x, is((double) (i.getAndIncrement() % 5 + 1))));
		assertThat(i.get(), is(10));
	}
}
