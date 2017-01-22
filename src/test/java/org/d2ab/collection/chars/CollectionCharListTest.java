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

import org.d2ab.collection.Lists;
import org.d2ab.iterator.chars.CharIterator;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class CollectionCharListTest {
	private final CharList empty = CollectionCharList.from(CharList.create());
	private final CharList list = CollectionCharList.from(
			CharList.create('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));

	@Test
	public void subList() {
		CharList subList = list.subList(2, 8);
		twice(() -> assertThat(subList, containsChars('c', 'd', 'e', 'a', 'b', 'c')));

		assertThat(subList.removeCharAt(1), is('d'));
		twice(() -> assertThat(subList, containsChars('c', 'e', 'a', 'b', 'c')));

		assertThat(subList.removeChar('e'), is(true));
		twice(() -> assertThat(subList, containsChars('c', 'a', 'b', 'c')));

		CharIterator subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextChar(), is('c'));
		subListIterator.remove();
		twice(() -> assertThat(subList, containsChars('a', 'b', 'c')));

		subList.removeCharsIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, containsChars('a', 'c')));

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
	public void containsChar() {
		assertThat(empty.containsChar('b'), is(false));
		for (char x = 'a'; x <= 'e'; x++)
			assertThat(list.containsChar(x), is(true));
		assertThat(list.containsChar('q'), is(false));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void iteratorRemove() {
		CharIterator iterator = list.iterator();
		iterator.nextChar();
		iterator.nextChar();
		iterator.remove();
		iterator.nextChar();
		iterator.remove();

		assertThat(list, containsChars('a', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void toCharArray() {
		assertArrayEquals(new char[0], empty.toCharArray());
		assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'}, list.toCharArray());
	}

	@Test
	public void addChar() {
		assertThat(empty.addChar('a'), is(true));
		assertThat(empty, containsChars('a'));

		assertThat(list.addChar('f'), is(true));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'f'));
	}

	@Test
	public void removeChar() {
		assertThat(empty.removeChar('q'), is(false));

		assertThat(list.removeChar('b'), is(true));
		assertThat(list, containsChars('a', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));

		assertThat(list.removeChar('q'), is(false));
		assertThat(list, containsChars('a', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void containsAllChars() {
		assertThat(empty.containsAllChars(CharList.create('b', 'c')), is(false));

		assertThat(list.containsAllChars(CharList.create('b', 'c')), is(true));
		assertThat(list.containsAllChars(CharList.create('b', 'q')), is(false));
	}

	@Test
	public void addAllCharsVarargs() {
		assertThat(empty.addAllChars(), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllChars('a', 'b'), is(true));
		assertThat(empty, containsChars('a', 'b'));

		assertThat(list.addAllChars('f', 'g', 'h'), is(true));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllCharsCharList() {
		assertThat(empty.addAllChars(CharList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllChars(CharList.create('a', 'b')), is(true));
		assertThat(empty, containsChars('a', 'b'));

		assertThat(list.addAllChars(CharList.create('f', 'g', 'h')), is(true));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllCharsAt() {
		assertThat(empty.addAllCharsAt(0, CharList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAllCharsAt(0, CharList.create('a', 'b')));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAllCharsAt(2, CharList.create('q', 'p', 'r')));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void removeAllChars() {
		assertThat(empty.removeAllChars(CharList.create('a', 'b')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllChars(CharList.create('a', 'b', 'e')), is(true));
		assertThat(list, containsChars('c', 'd', 'c', 'd'));
	}

	@Test
	public void retainAllChars() {
		assertThat(empty.retainAllChars(CharList.create('a', 'b')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllChars(CharList.create('a', 'b', 'c')), is(true));
		assertThat(list, containsChars('a', 'b', 'c', 'a', 'b', 'c'));
	}

	@Test
	public void replaceAllChars() {
		empty.replaceAllChars(x -> (char) (x + 1));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAllChars(x -> (char) (x + 1)));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void sortChars() {
		expecting(UnsupportedOperationException.class, empty::sortChars);
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, list::sortChars);
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
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
		assertThat(empty.equals(Lists.of()), is(true));
		assertThat(empty.equals(Lists.of('a', 'b')), is(false));

		assertThat(list.equals(Lists.of('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e')), is(true));
		assertThat(list.equals(Lists.of('e', 'd', 'c', 'b', 'a', 'e', 'd', 'c', 'b', 'a')), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(empty.hashCode(), is(Lists.of().hashCode()));
		assertThat(list.hashCode(), is(Lists.of('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e').hashCode()));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(list.toString(), is("[a, b, c, d, e, a, b, c, d, e]"));
	}

	@Test
	public void getChar() {
		assertThat(list.getChar(0), is('a'));
		assertThat(list.getChar(2), is('c'));
		assertThat(list.getChar(4), is('e'));
		assertThat(list.getChar(5), is('a'));
		assertThat(list.getChar(7), is('c'));
		assertThat(list.getChar(9), is('e'));
	}

	@Test
	public void setChar() {
		expecting(UnsupportedOperationException.class, () -> list.setChar(2, 'q'));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void addCharAt() {
		expecting(UnsupportedOperationException.class, () -> list.addCharAt(0, 'q'));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void indexOfChar() {
		assertThat(empty.indexOfChar('q'), is(-1));

		assertThat(list.indexOfChar('q'), is(-1));
		assertThat(list.indexOfChar('a'), is(0));
		assertThat(list.indexOfChar('c'), is(2));
		assertThat(list.indexOfChar('e'), is(4));
	}

	@Test
	public void lastIndexOfChar() {
		assertThat(empty.lastIndexOfChar('q'), is(-1));

		assertThat(list.lastIndexOfChar('q'), is(-1));
		assertThat(list.lastIndexOfChar('a'), is(5));
		assertThat(list.lastIndexOfChar('c'), is(7));
		assertThat(list.lastIndexOfChar('e'), is(9));
	}

	@Test
	public void listIteratorAtEnd() {
		CharListIterator listIterator = list.listIterator(10);
		assertThat(listIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, listIterator::nextChar);
	}

	@Test
	public void listIteratorAfterEnd() {
		expecting(IndexOutOfBoundsException.class, () -> list.listIterator(11));
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
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(list, containsChars('a', 'b', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void iteratorRemoveAll() {
		CharIterator iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.nextChar(), is((char) (i % 5 + 'a')));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));
		expecting(NoSuchElementException.class, iterator::nextChar);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorEmpty() {
		CharListIterator emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, emptyIterator::nextChar);
		expecting(UnsupportedOperationException.class, emptyIterator::hasPrevious);
		expecting(UnsupportedOperationException.class, emptyIterator::previousChar);
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(UnsupportedOperationException.class, () -> emptyIterator.add('q'));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void exhaustiveListIterator() {
		CharListIterator listIterator = list.listIterator();

		AtomicInteger i = new AtomicInteger(0);
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextChar(), is((char) (i.get() % 5 + 'a')));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
		});
	}

	@Test
	public void listIteratorRemoveAll() {
		CharIterator iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.nextChar(), is((char) (i % 5 + 'a')));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		CharListIterator listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.nextChar(), is((char) (i % 5 + 'a')));
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
		assertThat(empty.intStream().mapToObj(x -> (char) x).collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.intStream().mapToObj(x -> (char) x).collect(Collectors.toList()),
		           contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelIntStream().mapToObj(x -> (char) x).collect(Collectors.toList()),
		           is(emptyIterable()));
		assertThat(list.parallelIntStream().mapToObj(x -> (char) x).collect(Collectors.toList()),
		           contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void removeCharsIf() {
		empty.removeCharsIf(x -> x == 'a');
		assertThat(empty, is(emptyIterable()));

		list.removeCharsIf(x -> x == 'a');
		assertThat(list, containsChars('b', 'c', 'd', 'e', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void forEachChar() {
		empty.forEachChar(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger i = new AtomicInteger(0);
		list.forEachChar(x -> assertThat(x, is((char) (i.getAndIncrement() % 5 + 'a'))));
		assertThat(i.get(), is(10));
	}
}
