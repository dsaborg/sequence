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

import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ArrayCharListBoxingTest extends BaseBoxingTest {
	private final List<Character> empty = new ArrayCharList();
	private final List<Character> list = new ArrayCharList(new char[]{'a', 'b', 'c', 'd', 'e'});

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
		assertThat(list, contains('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Character> emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		ListIterator<Character> listIterator = list.listIterator();

		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set('v'));
		expecting(NoSuchElementException.class, listIterator::previous);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.add('t');
		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set('v'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.previous(), is('t'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.set('u');
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.remove();
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		assertThat(listIterator.next(), is('a'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is('b'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.next(), is('c'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.previous(), is('c'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.previous(), is('b'));
		listIterator.set('q');
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.next(), is('q'));
		listIterator.add('r');
		listIterator.add('s');
		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set('v'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(listIterator.next(), is('c'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(5));
		assertThat(listIterator.previousIndex(), is(4));

		assertThat(list, contains('a', 'q', 'r', 's', 'c', 'd', 'e'));
	}

	@Test
	public void exhaustiveListIterator() {
		twice(() -> {
			ListIterator<Character> listIterator = list.listIterator();
			AtomicInteger i = new AtomicInteger();
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is((char) (i.get() + 'a')));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));
			expecting(NoSuchElementException.class, listIterator::next);

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previous(), is((char) (i.get() + 'a')));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
			expecting(NoSuchElementException.class, listIterator::previous);
		});
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Character> iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is((char) (i + 'a')));
			iterator.remove();
			i++;
		}
		assertThat(i, is(5));
		expecting(NoSuchElementException.class, iterator::next);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		ListIterator<Character> listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.next(), is((char) (i + 'a')));
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
		ListIterator<Character> listIterator = list.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previous(), is((char) (i + 'a')));
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
		Iterator<Character> it1 = list.iterator();
		list.add('q');
		expecting(ConcurrentModificationException.class, it1::next);

		Iterator<Character> it2 = list.iterator();
		list.remove((Character) 'q');
		expecting(ConcurrentModificationException.class, it2::next);
	}

	@Test
	public void subList() {
		List<Character> list = CharList.create('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j');

		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(-1, 2));
		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(2, 11));

		List<Character> subList = list.subList(2, 8);
		twice(() -> assertThat(subList, contains('c', 'd', 'e', 'f', 'g', 'h')));

		assertThat(subList.remove(1), is('d'));
		twice(() -> assertThat(subList, contains('c', 'e', 'f', 'g', 'h')));
		twice(() -> assertThat(list, contains('a', 'b', 'c', 'e', 'f', 'g', 'h', 'i', 'j')));

		assertThat(subList.remove((Character) 'e'), is(true));
		twice(() -> assertThat(subList, contains('c', 'f', 'g', 'h')));
		twice(() -> assertThat(list, contains('a', 'b', 'c', 'f', 'g', 'h', 'i', 'j')));

		Iterator<Character> subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.next(), is('c'));
		subListIterator.remove();
		twice(() -> assertThat(subList, contains('f', 'g', 'h')));
		twice(() -> assertThat(list, contains('a', 'b', 'f', 'g', 'h', 'i', 'j')));

		subList.removeIf(x -> x != 'g');
		twice(() -> assertThat(subList, contains('g')));
		twice(() -> assertThat(list, contains('a', 'b', 'g', 'i', 'j')));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, contains('a', 'b', 'i', 'j')));

		subList.add('q');
		twice(() -> assertThat(subList, contains('q')));
		twice(() -> assertThat(list, contains('a', 'b', 'q', 'i', 'j')));
	}

	@Test
	public void lastIndexOf() {
		assertThat(empty.lastIndexOf('q'), is(-1));

		assertThat(list.lastIndexOf('q'), is(-1));
		assertThat(list.lastIndexOf('b'), is(1));
	}

	@Test
	public void indexOf() {
		assertThat(empty.indexOf('q'), is(-1));

		assertThat(list.indexOf('q'), is(-1));
		assertThat(list.indexOf('b'), is(1));
	}

	@Test
	public void get() {
		expecting(IndexOutOfBoundsException.class, () -> empty.get(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.get(2), is('c'));
	}

	@Test
	public void set() {
		expecting(IndexOutOfBoundsException.class, () -> empty.set(2, 'q'));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.set(2, 'q'), is('c'));
		assertThat(list, contains('a', 'b', 'q', 'd', 'e'));

		expecting(NullPointerException.class, () -> list.set(2, null));
		assertThat(list, contains('a', 'b', 'q', 'd', 'e'));
	}

	@Test
	public void add() {
		assertThat(empty.add('q'), is(true));
		assertThat(empty, contains('q'));

		assertThat(list.add('q'), is(true));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'q'));

		expecting(NullPointerException.class, () -> list.add(null));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'q'));
	}

	@Test
	public void addAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.add(2, 'q'));
		assertThat(empty, is(emptyIterable()));

		empty.add(0, 'q');
		assertThat(empty, contains('q'));

		list.add(2, 'q');
		assertThat(list, contains('a', 'b', 'q', 'c', 'd', 'e'));

		expecting(NullPointerException.class, () -> list.add(2, null));
		assertThat(list, contains('a', 'b', 'q', 'c', 'd', 'e'));
	}

	@Test
	public void addAll() {
		assertThat(empty.addAll(emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(Arrays.asList('a', 'b', 'c')), is(true));
		assertThat(empty, contains('a', 'b', 'c'));

		assertThat(list.addAll(Arrays.asList('f', 'g', 'h')), is(true));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllCharsArrayCharList() {
		assertThat(empty.addAll(ArrayCharList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(ArrayCharList.create('a', 'b', 'c')), is(true));
		assertThat(empty, contains('a', 'b', 'c'));

		assertThat(list.addAll(ArrayCharList.create('f', 'g', 'h')), is(true));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllCharsCharCollection() {
		assertThat(empty.addAll(new BitCharSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(new BitCharSet('a', 'b', 'c')), is(true));
		assertThat(empty, contains('a', 'b', 'c'));

		assertThat(list.addAll(new BitCharSet('f', 'g', 'h')), is(true));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllAt() {
		assertThat(empty.addAll(0, emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, Arrays.asList('a', 'b', 'c')), is(true));
		assertThat(empty, contains('a', 'b', 'c'));

		assertThat(list.addAll(2, Arrays.asList('q', 'r', 's')), is(true));
		assertThat(list, contains('a', 'b', 'q', 'r', 's', 'c', 'd', 'e'));
	}

	@Test
	public void addAllCharsAtCharCollection() {
		assertThat(empty.addAll(0, new BitCharSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, new BitCharSet('a', 'b', 'c')), is(true));
		assertThat(empty, contains('a', 'b', 'c'));

		assertThat(list.addAll(2, new BitCharSet('q', 'r', 's')), is(true));
		assertThat(list, contains('a', 'b', 'q', 'r', 's', 'c', 'd', 'e'));
	}

	@Test
	public void addAllCharsAtArrayCharList() {
		empty.addAll(0, ArrayCharList.create());
		assertThat(empty, is(emptyIterable()));

		empty.addAll(0, ArrayCharList.create('a', 'b', 'c'));
		assertThat(empty, contains('a', 'b', 'c'));

		list.addAll(2, ArrayCharList.create('q', 'r', 's'));
		assertThat(list, contains('a', 'b', 'q', 'r', 's', 'c', 'd', 'e'));
	}

	@Test
	public void remove() {
		assertThat(empty.remove((Character) 'q'), is(false));
		assertThat(empty.remove(new Object()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove((Character) 'q'), is(false));
		assertThat(list.remove(new Object()), is(false));
		assertThat(list.remove((Character) 'b'), is(true));
		assertThat(list, contains('a', 'c', 'd', 'e'));
	}

	@Test
	public void removeAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.remove(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove(2), is('c'));
		assertThat(list, contains('a', 'b', 'd', 'e'));
	}

	@Test
	public void boxedContains() {
		assertThat(empty.contains('q'), is(false));
		assertThat(empty.contains(new Object()), is(false));

		assertThat(list.contains('q'), is(false));
		assertThat(list.contains(new Object()), is(false));
		assertThat(list.contains('b'), is(true));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(Arrays.asList('q', 'r', 's', new Object())), is(false));

		assertThat(list.containsAll(Arrays.asList('q', 'r', 's', new Object())), is(false));
		assertThat(list.containsAll(Arrays.asList('a', 'b', 'c')), is(true));
	}

	@Test
	public void containsAllCharsCollection() {
		assertThat(empty.containsAll(CharList.of('q', 'r', 's')), is(false));

		assertThat(list.containsAll(CharList.of('q', 'r', 's')), is(false));
		assertThat(list.containsAll(CharList.of('a', 'b', 'c')), is(true));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(Arrays.asList('a', 'b', 'c', 'q')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(Arrays.asList('q', 'r', 's')), is(false));
		assertThat(list.removeAll(Arrays.asList('a', 'b', 'c', 'q')), is(true));
		assertThat(list, contains('d', 'e'));
	}

	@Test
	public void removeAllCharsCollection() {
		assertThat(empty.removeAll(CharList.of('a', 'b', 'c', 'q')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(CharList.of('q', 'r', 's')), is(false));
		assertThat(list.removeAll(CharList.of('a', 'b', 'c', 'q')), is(true));
		assertThat(list, contains('d', 'e'));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 'c'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeIf(x -> x > 'e'), is(false));
		assertThat(list.removeIf(x -> x > 'c'), is(true));
		assertThat(list, contains('a', 'b', 'c'));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(Arrays.asList('a', 'b', 'c', 'q')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(Arrays.asList('a', 'b', 'c', 'q')), is(true));
		assertThat(list, contains('a', 'b', 'c'));
	}

	@Test
	public void retainAllCharsCollection() {
		assertThat(empty.retainAll(CharList.of('a', 'b', 'c', 'q')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(CharList.of('a', 'b', 'c', 'q')), is(true));
		assertThat(list, contains('a', 'b', 'c'));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> (char) (x + 1));
		assertThat(empty, is(emptyIterable()));

		list.replaceAll(x -> (char) (x + 1));
		assertThat(list, contains('b', 'c', 'd', 'e', 'f'));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(0);
		list.forEach(x -> assertThat(x, is((char) (value.getAndIncrement() + 'a'))));
		assertThat(value.get(), is(5));
	}

	@Test
	public void forEachLong() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(0);
		list.forEach(x -> assertThat(x, is((char) (value.getAndIncrement() + 'a'))));
		assertThat(value.get(), is(5));
	}
}
