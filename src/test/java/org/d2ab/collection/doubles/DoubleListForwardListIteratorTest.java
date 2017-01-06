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

import org.d2ab.iterator.doubles.DoubleIterator;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DoubleListForwardListIteratorTest {
	private final DoubleList empty = DoubleList.Base.from(new SortedListDoubleSet());
	private final DoubleList list = DoubleList.Base.from(new SortedListDoubleSet(1, 2, 3, 4, 5));

	@Test
	public void listIteratorEmpty() {
		DoubleListIterator emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		expecting(UnsupportedOperationException.class, emptyIterator::hasPrevious);
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyIterator::nextDouble);
		expecting(UnsupportedOperationException.class, emptyIterator::previousDouble);

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIteratorAtEnd() {
		DoubleListIterator listIterator = list.listIterator(5);
		assertThat(listIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, listIterator::nextDouble);
	}

	@Test
	public void listIteratorAfterEnd() {
		expecting(IndexOutOfBoundsException.class, () -> list.listIterator(6));
	}

	@Test
	public void listIterator() {
		DoubleListIterator listIterator = list.listIterator();

		expecting(IllegalStateException.class, listIterator::remove);
		expecting(UnsupportedOperationException.class, () -> listIterator.set('p'));
		expecting(UnsupportedOperationException.class, listIterator::previousDouble);
		assertThat(listIterator.hasNext(), is(true));
		expecting(UnsupportedOperationException.class, listIterator::hasPrevious);
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		expecting(UnsupportedOperationException.class, () -> listIterator.add('q'));

		assertThat(listIterator.nextDouble(), is(1.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextDouble(), is(2.0));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.nextDouble(), is(3.0));
		listIterator.remove();
		expecting(IllegalStateException.class, listIterator::remove);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		expecting(UnsupportedOperationException.class, listIterator::previousDouble);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		expecting(UnsupportedOperationException.class, () -> listIterator.set('r'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.nextDouble(), is(4.0));
		expecting(UnsupportedOperationException.class, () -> listIterator.add('s'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.nextDouble(), is(5.0));
		assertThat(listIterator.hasNext(), is(false));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(list, containsDoubles(1, 2, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		twice(() -> {
			DoubleListIterator listIterator = list.listIterator();
			AtomicInteger i = new AtomicInteger();
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextDouble(), is((double) (i.get() + 1)));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));
			expecting(NoSuchElementException.class, listIterator::nextDouble);
		});
	}

	@Test
	public void iteratorRemoveAll() {
		DoubleIterator iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.nextDouble(), is((double) (i + 1)));
			iterator.remove();
			i++;
		}
		assertThat(i, is(5));
		expecting(NoSuchElementException.class, iterator::nextDouble);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		DoubleListIterator listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.nextDouble(), is((double) (i + 1)));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			i++;
		}
		assertThat(i, is(5));
		expecting(NoSuchElementException.class, listIterator::nextDouble);

		assertThat(list, is(emptyIterable()));
	}
}
