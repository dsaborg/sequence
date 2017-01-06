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

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;

public class IntListForwardListIteratorTest {
	private final IntList empty = createIntList();
	private final IntList list = createIntList(1, 2, 3, 4, 5);

	private static IntList createIntList(int... items) {
		IntList backing = IntList.create(items);
		return new IntList.Base() {
			@Override
			public IntIterator iterator() {
				return backing.iterator();
			}

			@Override
			public int size() {
				return backing.size();
			}
		};
	}

	@Test
	public void listIteratorEmpty() {
		IntListIterator emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		expecting(UnsupportedOperationException.class, emptyIterator::hasPrevious);
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyIterator::nextInt);
		expecting(UnsupportedOperationException.class, emptyIterator::previousInt);

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIteratorAtEnd() {
		IntListIterator listIterator = list.listIterator(5);
		assertThat(listIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, listIterator::nextInt);
	}

	@Test
	public void listIteratorAfterEnd() {
		expecting(IndexOutOfBoundsException.class, () -> list.listIterator(6));
	}

	@Test
	public void listIterator() {
		IntListIterator listIterator = list.listIterator();

		expecting(IllegalStateException.class, listIterator::remove);
		expecting(UnsupportedOperationException.class, () -> listIterator.set(32));
		expecting(UnsupportedOperationException.class, listIterator::previousInt);
		assertThat(listIterator.hasNext(), is(true));
		expecting(UnsupportedOperationException.class, listIterator::hasPrevious);
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		expecting(UnsupportedOperationException.class, () -> listIterator.add(33));

		assertThat(listIterator.nextInt(), is(1));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextInt(), is(2));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.nextInt(), is(3));
		listIterator.remove();
		expecting(IllegalStateException.class, listIterator::remove);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		expecting(UnsupportedOperationException.class, listIterator::previousInt);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		expecting(UnsupportedOperationException.class, () -> listIterator.set(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.nextInt(), is(4));
		expecting(UnsupportedOperationException.class, () -> listIterator.add(18));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.nextInt(), is(5));
		assertThat(listIterator.hasNext(), is(false));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(list, containsInts(1, 2, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		twice(() -> {
			IntListIterator listIterator = list.listIterator();
			AtomicInteger i = new AtomicInteger();
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextInt(), is(i.get() + 1));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));
			expecting(NoSuchElementException.class, listIterator::nextInt);
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
		expecting(NoSuchElementException.class, iterator::nextInt);

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
		expecting(NoSuchElementException.class, listIterator::nextInt);

		assertThat(list, is(emptyIterable()));
	}
}
