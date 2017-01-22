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
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CollectionCharListBoxingTest extends BaseBoxingTest {
	private final List<Character> empty = CollectionCharList.from(CharList.create());
	private final List<Character> list = CollectionCharList.from(
			CharList.create('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));

	@Test
	public void subList() {
		List<Character> subList = list.subList(2, 8);
		twice(() -> assertThat(subList, contains('c', 'd', 'e', 'a', 'b', 'c')));

		assertThat(subList.remove(1), is('d'));
		twice(() -> assertThat(subList, contains('c', 'e', 'a', 'b', 'c')));

		assertThat(subList.remove((Character) 'e'), is(true));
		twice(() -> assertThat(subList, contains('c', 'a', 'b', 'c')));

		Iterator<Character> subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.next(), is('c'));
		subListIterator.remove();
		twice(() -> assertThat(subList, contains('a', 'b', 'c')));

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, contains('a', 'c')));

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
		assertThat(empty.contains('b'), is(false));
		for (char x = 'a'; x <= 'e'; x++)
			assertThat(list.contains(x), is(true));
		assertThat(list.contains('q'), is(false));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void iteratorRemove() {
		Iterator<Character> iterator = list.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.remove();

		assertThat(list, contains('a', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void add() {
		assertThat(empty.add('a'), is(true));
		assertThat(empty, contains('a'));

		assertThat(list.add('f'), is(true));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'f'));
	}

	@Test
	public void remove() {
		assertThat(empty.remove((Character) 'q'), is(false));

		assertThat(list.remove((Character) 'b'), is(true));
		assertThat(list, contains('a', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));

		assertThat(list.remove((Character) 'q'), is(false));
		assertThat(list, contains('a', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(CharList.create('b', 'c')), is(false));

		assertThat(list.containsAll(CharList.create('b', 'c')), is(true));
		assertThat(list.containsAll(CharList.create('b', 'q')), is(false));
	}

	@Test
	public void addAllCharsCharList() {
		assertThat(empty.addAll(CharList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(CharList.create('a', 'b')), is(true));
		assertThat(empty, contains('a', 'b'));

		assertThat(list.addAll(CharList.create('f', 'g', 'h')), is(true));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllCollection() {
		assertThat(empty.addAll(Lists.of()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(Lists.of('a', 'b')), is(true));
		assertThat(empty, contains('a', 'b'));

		assertThat(list.addAll(Lists.of('f', 'g', 'h')), is(true));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllAt() {
		assertThat(empty.addAll(0, CharList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAll(0, CharList.create('a', 'b')));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAll(2, CharList.create('q', 'p', 'r')));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(CharList.create('a', 'b')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(CharList.create('a', 'b', 'e')), is(true));
		assertThat(list, contains('c', 'd', 'c', 'd'));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(CharList.create('a', 'b')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(list, contains('a', 'b', 'c', 'a', 'b', 'c'));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> (char) (x + 1));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAll(x -> (char) (x + 1)));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void sortChars() {
		expecting(UnsupportedOperationException.class, () -> empty.sort(naturalOrder()));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.sort(naturalOrder()));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
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
	public void get() {
		assertThat(list.get(0), is('a'));
		assertThat(list.get(2), is('c'));
		assertThat(list.get(4), is('e'));
		assertThat(list.get(5), is('a'));
		assertThat(list.get(7), is('c'));
		assertThat(list.get(9), is('e'));
	}

	@Test
	public void set() {
		expecting(UnsupportedOperationException.class, () -> list.set(2, 'q'));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void addAt() {
		expecting(UnsupportedOperationException.class, () -> list.add(0, 'q'));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void indexOf() {
		assertThat(empty.indexOf('q'), is(-1));

		assertThat(list.indexOf('q'), is(-1));
		assertThat(list.indexOf('a'), is(0));
		assertThat(list.indexOf('c'), is(2));
		assertThat(list.indexOf('e'), is(4));
	}

	@Test
	public void lastIndexOf() {
		assertThat(empty.lastIndexOf('q'), is(-1));

		assertThat(list.lastIndexOf('q'), is(-1));
		assertThat(list.lastIndexOf('a'), is(5));
		assertThat(list.lastIndexOf('c'), is(7));
		assertThat(list.lastIndexOf('e'), is(9));
	}

	@Test
	public void listIteratorAtEnd() {
		ListIterator<Character> listIterator = list.listIterator(10);
		assertThat(listIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, listIterator::next);
	}

	@Test
	public void listIteratorAfterEnd() {
		expecting(IndexOutOfBoundsException.class, () -> list.listIterator(11));
	}

	@Test
	public void listIterator() {
		ListIterator<Character> listIterator = list.listIterator();

		expecting(IllegalStateException.class, listIterator::remove);
		expecting(UnsupportedOperationException.class, () -> listIterator.set('p'));
		expecting(UnsupportedOperationException.class, listIterator::previous);
		assertThat(listIterator.hasNext(), is(true));
		expecting(UnsupportedOperationException.class, listIterator::hasPrevious);
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		expecting(UnsupportedOperationException.class, () -> listIterator.add('q'));

		assertThat(listIterator.next(), is('a'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is('b'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.next(), is('c'));
		listIterator.remove();
		expecting(IllegalStateException.class, listIterator::remove);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		expecting(UnsupportedOperationException.class, listIterator::previous);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		expecting(UnsupportedOperationException.class, () -> listIterator.set('r'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.next(), is('d'));
		expecting(UnsupportedOperationException.class, () -> listIterator.add('s'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.next(), is('e'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(list, contains('a', 'b', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Character> iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is((char) (i % 5 + 'a')));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));
		expecting(NoSuchElementException.class, iterator::next);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Character> emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, emptyIterator::next);
		expecting(UnsupportedOperationException.class, emptyIterator::hasPrevious);
		expecting(UnsupportedOperationException.class, emptyIterator::previous);
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
		ListIterator<Character> listIterator = list.listIterator();

		AtomicInteger i = new AtomicInteger(0);
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is((char) (i.get() % 5 + 'a')));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
		});
	}

	@Test
	public void listIteratorRemoveAll() {
		Iterator<Character> iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is((char) (i % 5 + 'a')));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		ListIterator<Character> listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.next(), is((char) (i % 5 + 'a')));
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
		           contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()),
		           is(emptyIterable()));
		assertThat(list.parallelStream().collect(Collectors.toList()),
		           contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void removeIf() {
		empty.removeIf(x -> x == 'a');
		assertThat(empty, is(emptyIterable()));

		list.removeIf(x -> x == 'a');
		assertThat(list, contains('b', 'c', 'd', 'e', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger i = new AtomicInteger(0);
		list.forEach(x -> assertThat(x, is((char) (i.getAndIncrement() % 5 + 'a'))));
		assertThat(i.get(), is(10));
	}
}
