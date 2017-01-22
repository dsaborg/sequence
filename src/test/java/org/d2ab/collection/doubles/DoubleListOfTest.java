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
import org.d2ab.iterator.doubles.DoubleIterator;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class DoubleListOfTest {
	private final DoubleList empty = DoubleList.of();
	private final DoubleList list = DoubleList.of(1, 2, 3, 4, 5, 1, 2, 3, 4, 5);

	@Test
	public void subList() {
		DoubleList subList = list.subList(2, 8);
		twice(() -> assertThat(subList, containsDoubles(3, 4, 5, 1, 2, 3)));

		expecting(UnsupportedOperationException.class, () -> subList.removeDoubleAt(1));
		twice(() -> assertThat(subList, containsDoubles(3, 4, 5, 1, 2, 3)));

		expecting(UnsupportedOperationException.class, () -> subList.removeDoubleExactly(5));
		twice(() -> assertThat(subList, containsDoubles(3, 4, 5, 1, 2, 3)));

		DoubleIterator subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextDouble(), is(3.0));
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextDouble(), is(4.0));
		expecting(UnsupportedOperationException.class, subListIterator::remove);
		twice(() -> assertThat(subList, containsDoubles(3, 4, 5, 1, 2, 3)));

		expecting(UnsupportedOperationException.class, () -> subList.removeDoublesIf(x -> x % 2 == 0));
		twice(() -> assertThat(subList, containsDoubles(3, 4, 5, 1, 2, 3)));

		expecting(UnsupportedOperationException.class, subList::clear);
		twice(() -> assertThat(subList, containsDoubles(3, 4, 5, 1, 2, 3)));
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
	public void containsDoubleExactly() {
		assertThat(empty.containsDoubleExactly(2), is(false));
		for (double i = 1; i < 5; i++)
			assertThat(list.containsDoubleExactly(i), is(true));
		assertThat(list.containsDoubleExactly(17), is(false));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void iteratorRemove() {
		DoubleIterator iterator = list.iterator();
		assertThat(iterator.nextDouble(), is(1.0));
		assertThat(iterator.nextDouble(), is(2.0));
		expecting(UnsupportedOperationException.class, iterator::remove);
		assertThat(iterator.nextDouble(), is(3.0));
		expecting(UnsupportedOperationException.class, iterator::remove);

		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void toDoubleArray() {
		assertArrayEquals(new double[0], empty.toDoubleArray(), 0.0);
		assertArrayEquals(new double[]{1, 2, 3, 4, 5, 1, 2, 3, 4, 5}, list.toDoubleArray(), 0.0);
	}

	@Test
	public void addDouble() {
		expecting(UnsupportedOperationException.class, () -> empty.addDoubleExactly(1));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addDoubleExactly(6));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeDoubleExactly() {
		assertThat(empty.removeDoubleExactly(17), is(false));

		expecting(UnsupportedOperationException.class, () -> list.removeDoubleExactly(2));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));

		assertThat(list.removeDoubleExactly(17), is(false));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void containsAllDoubles() {
		assertThat(empty.containsAllDoublesExactly(DoubleList.create(2, 3)), is(false));

		assertThat(list.containsAllDoublesExactly(DoubleList.create(2, 3)), is(true));
		assertThat(list.containsAllDoublesExactly(DoubleList.create(2, 17)), is(false));
	}

	@Test
	public void addAllDoubles() {
		assertThat(empty.addAllDoubles(DoubleList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAllDoubles(DoubleList.create(1, 2)));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAllDoubles(DoubleList.create(6, 7, 8)));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addAllDoublesAt() {
		assertThat(empty.addAllDoublesAt(0, DoubleList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAllDoublesAt(0, DoubleList.create(1, 2)));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAllDoublesAt(2, DoubleList.create(17, 18, 19)));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeAllDoublesExactly() {
		assertThat(empty.removeAllDoublesExactly(DoubleList.create(1, 2)), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.removeAllDoublesExactly(DoubleList.create(1, 2, 5)));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void retainAllDoublesExactly() {
		assertThat(empty.retainAllDoublesExactly(DoubleList.create(1, 2)), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.retainAllDoublesExactly(DoubleList.create(1, 2, 3)));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void replaceAllDoubles() {
		empty.replaceAllDoubles(x -> x + 1);
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAllDoubles(x -> x + 1));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void sortDoubles() {
		expecting(UnsupportedOperationException.class, empty::sortDoubles);
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, list::sortDoubles);
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, list::clear);
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void testEquals() {
		assertThat(empty.equals(Lists.of()), is(true));
		assertThat(empty.equals(Lists.of(1.0, 2.0)), is(false));

		assertThat(list.equals(Lists.of(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0)), is(true));
		assertThat(list.equals(Lists.of(5.0, 4.0, 3.0, 2.0, 1.0, 5.0, 4.0, 3.0, 2.0, 1.0)), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(empty.hashCode(), is(Lists.of().hashCode()));
		assertThat(list.hashCode(), is(Lists.of(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0).hashCode()));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(list.toString(), is("[1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0]"));
	}

	@Test
	public void getDouble() {
		assertThat(list.getDouble(0), is(1.0));
		assertThat(list.getDouble(2), is(3.0));
		assertThat(list.getDouble(4), is(5.0));
		assertThat(list.getDouble(5), is(1.0));
		assertThat(list.getDouble(7), is(3.0));
		assertThat(list.getDouble(9), is(5.0));
	}

	@Test
	public void setDouble() {
		expecting(UnsupportedOperationException.class, () -> list.setDouble(2, 17));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addDoubleAt() {
		expecting(UnsupportedOperationException.class, () -> list.addDoubleAt(0, 17));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void indexOfDouble() {
		assertThat(empty.indexOfDoubleExactly(17), is(-1));

		assertThat(list.indexOfDoubleExactly(1), is(0));
		assertThat(list.indexOfDoubleExactly(3), is(2));
		assertThat(list.indexOfDoubleExactly(5), is(4));
	}

	@Test
	public void lastIndexOfDouble() {
		assertThat(empty.lastIndexOfDoubleExactly(17), is(-1));

		assertThat(list.lastIndexOfDoubleExactly(1), is(5));
		assertThat(list.lastIndexOfDoubleExactly(3), is(7));
		assertThat(list.lastIndexOfDoubleExactly(5), is(9));
	}

	@Test
	public void listIteratorAtEnd() {
		DoubleListIterator listIterator = list.listIterator(10);
		assertThat(listIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, listIterator::nextDouble);
	}

	@Test
	public void listIteratorEdgeCases() {
		expecting(IndexOutOfBoundsException.class, () -> list.listIterator(-1));
		expecting(IndexOutOfBoundsException.class, () -> list.listIterator(11));
	}

	@Test
	public void listIteratorEmpty() {
		DoubleListIterator emptyIterator = empty.listIterator();
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
		DoubleListIterator listIterator = list.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));
		assertThat(listIterator.nextDouble(), is(1.0));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.nextDouble(), is(2.0));

		expecting(UnsupportedOperationException.class, () -> listIterator.add(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.nextDouble(), is(3.0));

		expecting(UnsupportedOperationException.class, () -> listIterator.set(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.nextDouble(), is(4.0));

		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		DoubleListIterator listIterator = list.listIterator();

		AtomicInteger i = new AtomicInteger(0);
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextDouble(), is((double) (i.get() % 5 + 1)));
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
	public void removeDoublesIf() {
		empty.removeDoublesIf(x -> x == 1);
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.removeDoublesIf(x -> x == 1));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void forEachDouble() {
		empty.forEachDouble(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger i = new AtomicInteger(0);
		list.forEachDouble(x -> assertThat(x, is((double) (i.getAndIncrement() % 5 + 1))));
		assertThat(i.get(), is(10));
	}
}
