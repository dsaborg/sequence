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

package org.d2ab.collection.chars;

import org.d2ab.iterator.chars.CharIterator;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CharListForwardListIteratorTest {
	private final CharList empty = CharList.Base.from(new BitCharSet());
	private final CharList list = CharList.Base.from(new BitCharSet('a', 'b', 'c', 'd', 'e'));

	@Test
	public void listIteratorEmpty() {
		CharListIterator emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		expecting(UnsupportedOperationException.class, emptyIterator::hasPrevious);
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyIterator::nextChar);
		expecting(UnsupportedOperationException.class, emptyIterator::previousChar);

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIteratorAtEnd() {
		CharListIterator listIterator = list.listIterator(5);
		assertThat(listIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, listIterator::nextChar);
	}

	@Test
	public void listIteratorAfterEnd() {
		expecting(IndexOutOfBoundsException.class, () -> list.listIterator(6));
	}

	@Test
	public void listIterator() {
		CharListIterator listIterator = list.listIterator();

		expecting(IllegalStateException.class, listIterator::remove);
		expecting(UnsupportedOperationException.class, () -> listIterator.set('p'));
		expecting(UnsupportedOperationException.class, listIterator::previousChar);
		assertThat(listIterator.hasNext(), is(true));
		expecting(UnsupportedOperationException.class, listIterator::hasPrevious);
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		expecting(UnsupportedOperationException.class, () -> listIterator.add('q'));

		assertThat(listIterator.nextChar(), is('a'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextChar(), is('b'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.nextChar(), is('c'));
		listIterator.remove();
		expecting(IllegalStateException.class, listIterator::remove);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		expecting(UnsupportedOperationException.class, listIterator::previousChar);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		expecting(UnsupportedOperationException.class, () -> listIterator.set('r'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.nextChar(), is('d'));
		expecting(UnsupportedOperationException.class, () -> listIterator.add('s'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.nextChar(), is('e'));
		assertThat(listIterator.hasNext(), is(false));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(list, containsChars('a', 'b', 'd', 'e'));
	}

	@Test
	public void exhaustiveListIterator() {
		twice(() -> {
			CharListIterator listIterator = list.listIterator();
			AtomicInteger i = new AtomicInteger();
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextChar(), is((char) (i.get() + 'a')));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));
			expecting(NoSuchElementException.class, listIterator::nextChar);
		});
	}

	@Test
	public void iteratorRemoveAll() {
		CharIterator iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.nextChar(), is((char) (i + 'a')));
			iterator.remove();
			i++;
		}
		assertThat(i, is(5));
		expecting(NoSuchElementException.class, iterator::nextChar);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		CharListIterator listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.nextChar(), is((char) (i + 'a')));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			i++;
		}
		assertThat(i, is(5));
		expecting(NoSuchElementException.class, listIterator::nextChar);

		assertThat(list, is(emptyIterable()));
	}
}
