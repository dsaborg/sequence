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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static java.util.Comparator.naturalOrder;
import static org.d2ab.collection.Arrayz.fill;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class CharListBoxingTest extends BaseBoxingTest {
	private final List<Character> empty = CharList.Base.create();
	private final List<Character> list = CharList.Base.create('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e');

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
	public void clear() {
		empty.clear();
		assertThat(empty, is(emptyIterable()));

		list.clear();
		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void toArray() {
		assertArrayEquals(new Character[0], empty.toArray());
		assertArrayEquals(new Character[]{'a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'}, list.toArray());
	}

	@Test
	public void toArrayEmptyTarget() {
		Character[] emptyTarget = new Character[0];
		assertThat(empty.toArray(emptyTarget), is(sameInstance(emptyTarget)));
		assertArrayEquals(new Character[]{'a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'},
		                  list.toArray(new Character[0]));
	}

	@Test
	public void toArraySmallerTarget() {
		assertArrayEquals(new Character[]{'a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'},
		                  list.toArray(new Character[9]));
	}

	@Test
	public void toArrayBiggerTarget() {
		assertArrayEquals(new Character[]{null, 'q'}, empty.toArray(fill(new Character[2], 'q')));
		assertArrayEquals(new Character[]{'a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', null, 'q'},
		                  list.toArray(fill(new Character[12], 'q')));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Character> emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		emptyIterator.add('q');
		emptyIterator.add('p');
		expecting(IllegalStateException.class, () -> emptyIterator.set('r'));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(true));
		assertThat(emptyIterator.nextIndex(), is(2));
		assertThat(emptyIterator.previousIndex(), is(1));

		assertThat(emptyIterator.previous(), is('p'));
		assertThat(emptyIterator.hasNext(), is(true));
		assertThat(emptyIterator.hasPrevious(), is(true));
		assertThat(emptyIterator.nextIndex(), is(1));
		assertThat(emptyIterator.previousIndex(), is(0));

		emptyIterator.remove();
		expecting(IllegalStateException.class, () -> emptyIterator.set('r'));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(true));
		assertThat(emptyIterator.nextIndex(), is(1));
		assertThat(emptyIterator.previousIndex(), is(0));

		assertThat(emptyIterator.previous(), is('q'));
		emptyIterator.set('r');
		assertThat(emptyIterator.hasNext(), is(true));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(NoSuchElementException.class, emptyIterator::previous);
		assertThat(emptyIterator.hasNext(), is(true));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, contains('r'));
	}

	@Test
	public void listIterator() {
		ListIterator<Character> listIterator = list.listIterator();

		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set('z'));
		expecting(NoSuchElementException.class, listIterator::previous);
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.add('x');
		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set('y'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.previous(), is('x'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));

		listIterator.set('w');
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
		listIterator.add('p');
		listIterator.add('r');
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(listIterator.next(), is('c'));

		assertThat(list, contains('a', 'q', 'p', 'r', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void exhaustiveListIterator() {
		twice(() -> {
			ListIterator<Character> listIterator = list.listIterator();
			AtomicInteger i = new AtomicInteger();
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is((char) (i.get() % 5 + 'a')));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
			expecting(NoSuchElementException.class, listIterator::next);

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previous(), is((char) (i.get() % 5 + 'a')));
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
			assertThat(iterator.next(), is((char) (i % 5 + 'a')));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));
		expecting(NoSuchElementException.class, iterator::next);

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
		expecting(NoSuchElementException.class, listIterator::next);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemoveBackwards() {
		int i = 10;
		ListIterator<Character> listIterator = list.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previous(), is((char) (i % 5 + 'a')));
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
		CharList list = CharList.Base.create('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j');

		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(-1, 2));
		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(2, 11));

		CharList subList = list.subList(2, 8);
		assertThat(subList.size(), is(6));
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

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, contains('g')));
		twice(() -> assertThat(list, contains('a', 'b', 'g', 'i', 'j')));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, contains('a', 'b', 'i', 'j')));

		expecting(UnsupportedOperationException.class, () -> subList.add('q'));
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, contains('a', 'b', 'i', 'j')));
	}

	@Test
	public void sort() {
		Lists.reverse(list);
		expecting(UnsupportedOperationException.class, () -> list.sort(naturalOrder()));
		assertThat(list, contains('e', 'd', 'c', 'b', 'a', 'e', 'd', 'c', 'b', 'a'));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(list.toString(), is("[a, b, c, d, e, a, b, c, d, e]"));
	}

	@Test
	public void testEqualsHashCodeAgainstList() {
		assertThat(list, is(equalTo(list)));
		assertThat(list, is(not(equalTo(null))));
		assertThat(list, is(not(equalTo(new Object()))));
		assertThat(list, is(not(equalTo(new TreeSet<>(asList('a', 'b', 'c', 'd', 'e'))))));
		assertThat(list, is(not(equalTo(new ArrayList<>(asList('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd'))))));

		List<Character> list2 = new ArrayList<>(asList('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'q'));
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));

		list2.remove((Character) 'q');

		assertThat(list, is(equalTo(list2)));
		assertThat(list.hashCode(), is(list2.hashCode()));

		Lists.reverse(list2);
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));
	}

	@Test
	public void testEqualsHashCodeAgainstCharList() {
		assertThat(list, is(not(equalTo(CharList.create('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd')))));

		CharList list2 = CharList.create('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'q');
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));

		list2.remove((Character) 'q');

		assertThat(list, is(equalTo(list2)));
		assertThat(list.hashCode(), is(list2.hashCode()));

		Lists.reverse(list2);
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));
	}

	@Test
	public void lastIndexOf() {
		assertThat(empty.lastIndexOf('q'), is(-1));

		assertThat(list.lastIndexOf('q'), is(-1));
		assertThat(list.lastIndexOf('b'), is(6));
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
		expecting(IndexOutOfBoundsException.class, () -> empty.get(0));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.get(2), is('c'));
		expecting(IndexOutOfBoundsException.class, () -> list.get(12));
		expecting(IndexOutOfBoundsException.class, () -> list.get(10));
	}

	@Test
	public void set() {
		expecting(IndexOutOfBoundsException.class, () -> empty.set(2, 'q'));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.set(2, 'q'), is('c'));
		assertThat(list, contains('a', 'b', 'q', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void add() {
		assertThat(empty.add('q'), is(true));
		assertThat(empty, contains('q'));

		assertThat(list.add('q'), is(true));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'q'));
	}

	@Test
	public void addAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.add(2, 'q'));
		assertThat(empty, is(emptyIterable()));

		empty.add(0, 'q');
		assertThat(empty, contains('q'));

		list.add(2, 'q');
		assertThat(list, contains('a', 'b', 'q', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void addAllCharCollection() {
		assertThat(empty.addAll(new BitCharSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(new BitCharSet('a', 'b', 'c')), is(true));
		assertThat(empty, contains('a', 'b', 'c'));

		assertThat(list.addAll(new BitCharSet('f', 'g', 'h')), is(true));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllArrayCharList() {
		assertThat(empty.addAll(ArrayCharList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(ArrayCharList.create('a', 'b', 'c')), is(true));
		assertThat(empty, contains('a', 'b', 'c'));

		assertThat(list.addAll(ArrayCharList.create('f', 'g', 'h')), is(true));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllAtCharCollection() {
		assertThat(empty.addAll(0, new BitCharSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, new BitCharSet('a', 'b', 'c')), is(true));
		assertThat(empty, contains('a', 'b', 'c'));

		assertThat(list.addAll(2, new BitCharSet('p', 'q', 'r')), is(true));
		assertThat(list, contains('a', 'b', 'p', 'q', 'r', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void addAllAtArrayCharList() {
		assertThat(empty.addAll(0, ArrayCharList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, ArrayCharList.create('a', 'b', 'c')), is(true));
		assertThat(empty, contains('a', 'b', 'c'));

		assertThat(list.addAll(2, ArrayCharList.create('q', 'p', 'r')), is(true));
		assertThat(list, contains('a', 'b', 'q', 'p', 'r', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void remove() {
		assertThat(empty.remove((Character) 'q'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove((Character) 'q'), is(false));
		assertThat(list.remove((Character) 'b'), is(true));
		assertThat(list, contains('a', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void removeAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.remove(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove(2), is('c'));
		assertThat(list, contains('a', 'b', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void boxedContains() {
		assertThat(empty.contains('q'), is(false));

		assertThat(list.contains('q'), is(false));
		assertThat(list.contains('b'), is(true));
	}

	@Test
	public void containsAllCollection() {
		assertThat(empty.containsAll(ArrayCharList.create('q', 'p', 'r')), is(false));

		assertThat(list.containsAll(ArrayCharList.create('q', 'p', 'r')), is(false));
		assertThat(list.containsAll(ArrayCharList.create('a', 'b', 'c')), is(true));
	}

	@Test
	public void removeAllCollection() {
		assertThat(empty.removeAll(ArrayCharList.create('a', 'b', 'c', 'q')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(ArrayCharList.create('q', 'p', 'r')), is(false));
		assertThat(list.removeAll(ArrayCharList.create('a', 'b', 'c', 'q')), is(true));
		assertThat(list, contains('d', 'e', 'd', 'e'));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 'c'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeIf(x -> x > 'e'), is(false));
		assertThat(list.removeIf(x -> x > 'c'), is(true));
		assertThat(list, contains('a', 'b', 'c', 'a', 'b', 'c'));
	}

	@Test
	public void retainAllCharList() {
		assertThat(empty.retainAll(CharList.create('a', 'b', 'c', 'q')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(CharList.create('a', 'b', 'c', 'q')), is(true));
		assertThat(list, contains('a', 'b', 'c', 'a', 'b', 'c'));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> (char) (x + 1));
		assertThat(empty, is(emptyIterable()));

		list.replaceAll(x -> (char) (x + 1));
		assertThat(list, contains('b', 'c', 'd', 'e', 'f', 'b', 'c', 'd', 'e', 'f'));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger index = new AtomicInteger(0);
		list.forEach(x -> assertThat(x, is((char) (index.getAndIncrement() % 5 + 'a'))));
		assertThat(index.get(), is(10));
	}
}
