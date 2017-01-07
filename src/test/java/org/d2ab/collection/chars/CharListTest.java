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
import org.d2ab.collection.ints.IntList;
import org.d2ab.iterator.chars.CharIterator;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class CharListTest {
	private final CharList empty = CharList.Base.create();
	private final CharList list = CharList.Base.create('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e');

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
	public void testAsList() {
		assertThat(empty.asList(), is(sameInstance(empty)));
		assertThat(list.asList(), is(sameInstance(list)));
	}

	@Test
	public void toCharArray() {
		assertArrayEquals(new char[0], empty.toCharArray());
		assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'}, list.toCharArray());
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void listIteratorEmpty() {
		CharListIterator emptyIterator = empty.listIterator();
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

		assertThat(emptyIterator.previousChar(), is('p'));
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

		assertThat(emptyIterator.previousChar(), is('q'));
		emptyIterator.set('r');
		assertThat(emptyIterator.hasNext(), is(true));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(NoSuchElementException.class, emptyIterator::previousChar);
		assertThat(emptyIterator.hasNext(), is(true));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, containsChars('r'));
	}

	@Test
	public void listIterator() {
		CharListIterator listIterator = list.listIterator();

		expecting(IllegalStateException.class, listIterator::remove);
		expecting(IllegalStateException.class, () -> listIterator.set('z'));
		expecting(NoSuchElementException.class, listIterator::previousChar);
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

		assertThat(listIterator.previousChar(), is('x'));
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

		assertThat(listIterator.nextChar(), is('a'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextChar(), is('b'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.nextChar(), is('c'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));

		assertThat(listIterator.previousChar(), is('c'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));

		assertThat(listIterator.previousChar(), is('b'));
		listIterator.set('q');
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));

		assertThat(listIterator.nextChar(), is('q'));
		listIterator.add('p');
		listIterator.add('r');
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));

		assertThat(listIterator.nextChar(), is('c'));

		assertThat(list, containsChars('a', 'q', 'p', 'r', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void exhaustiveListIterator() {
		twice(() -> {
			CharListIterator listIterator = list.listIterator();
			AtomicInteger i = new AtomicInteger();
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextChar(), is((char) (i.get() % 5 + 'a')));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
			expecting(NoSuchElementException.class, listIterator::nextChar);

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previousChar(), is((char) (i.get() % 5 + 'a')));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
			expecting(NoSuchElementException.class, listIterator::previousChar);
		});
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
		expecting(NoSuchElementException.class, listIterator::nextChar);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemoveBackwards() {
		int i = 10;
		CharListIterator listIterator = list.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previousChar(), is((char) (i % 5 + 'a')));
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
		}
		assertThat(i, is(0));
		expecting(NoSuchElementException.class, listIterator::previousChar);

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void iteratorFailFast() {
		CharIterator it1 = list.iterator();
		list.addChar('q');
		expecting(ConcurrentModificationException.class, it1::nextChar);

		CharIterator it2 = list.iterator();
		list.removeChar('q');
		expecting(ConcurrentModificationException.class, it2::nextChar);
	}

	@Test
	public void subList() {
		CharList list = CharList.Base.create('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j');

		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(-1, 2));
		expecting(ArrayIndexOutOfBoundsException.class, () -> list.subList(2, 11));

		CharList subList = list.subList(2, 8);
		assertThat(subList.size(), is(6));
		twice(() -> assertThat(subList, containsChars('c', 'd', 'e', 'f', 'g', 'h')));

		assertThat(subList.removeCharAt(1), is('d'));
		twice(() -> assertThat(subList, containsChars('c', 'e', 'f', 'g', 'h')));
		twice(() -> assertThat(list, containsChars('a', 'b', 'c', 'e', 'f', 'g', 'h', 'i', 'j')));

		assertThat(subList.removeChar('e'), is(true));
		twice(() -> assertThat(subList, containsChars('c', 'f', 'g', 'h')));
		twice(() -> assertThat(list, containsChars('a', 'b', 'c', 'f', 'g', 'h', 'i', 'j')));

		CharIterator subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextChar(), is('c'));
		subListIterator.remove();
		twice(() -> assertThat(subList, containsChars('f', 'g', 'h')));
		twice(() -> assertThat(list, containsChars('a', 'b', 'f', 'g', 'h', 'i', 'j')));

		subList.removeIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, containsChars('g')));
		twice(() -> assertThat(list, containsChars('a', 'b', 'g', 'i', 'j')));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, containsChars('a', 'b', 'i', 'j')));

		expecting(UnsupportedOperationException.class, () -> subList.addChar('q'));
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, containsChars('a', 'b', 'i', 'j')));
	}

	@Test
	public void sortChars() {
		Lists.reverse(list);
		expecting(UnsupportedOperationException.class, list::sortChars);
		assertThat(list, containsChars('e', 'd', 'c', 'b', 'a', 'e', 'd', 'c', 'b', 'a'));
	}

	@Test
	public void binarySearch() {
		expecting(UnsupportedOperationException.class, () -> list.binarySearch('a'));
	}

	@Test
	public void intStream() {
		assertThat(empty.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));

		assertThat(list.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void parallelIntStream() {
		assertThat(empty.parallelIntStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));

		assertThat(list.parallelIntStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
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

		list2.removeChar('q');

		assertThat(list, is(equalTo(list2)));
		assertThat(list.hashCode(), is(list2.hashCode()));

		Lists.reverse(list2);
		assertThat(list, is(not(equalTo(list2))));
		assertThat(list.hashCode(), is(not(list2.hashCode())));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(list.sequence(), containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void lastIndexOfChar() {
		assertThat(empty.lastIndexOfChar('q'), is(-1));

		assertThat(list.lastIndexOfChar('q'), is(-1));
		assertThat(list.lastIndexOfChar('b'), is(6));
	}

	@Test
	public void indexOfChar() {
		assertThat(empty.indexOfChar('q'), is(-1));

		assertThat(list.indexOfChar('q'), is(-1));
		assertThat(list.indexOfChar('b'), is(1));
	}

	@Test
	public void getChar() {
		expecting(IndexOutOfBoundsException.class, () -> empty.getChar(2));
		expecting(IndexOutOfBoundsException.class, () -> empty.getChar(0));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.getChar(2), is('c'));
		expecting(IndexOutOfBoundsException.class, () -> list.getChar(12));
		expecting(IndexOutOfBoundsException.class, () -> list.getChar(10));
	}

	@Test
	public void setChar() {
		expecting(IndexOutOfBoundsException.class, () -> empty.setChar(2, 'q'));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.setChar(2, 'q'), is('c'));
		assertThat(list, containsChars('a', 'b', 'q', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void addChar() {
		assertThat(empty.addChar('q'), is(true));
		assertThat(empty, containsChars('q'));

		assertThat(list.addChar('q'), is(true));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'q'));
	}

	@Test
	public void addCharAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.addCharAt(2, 'q'));
		assertThat(empty, is(emptyIterable()));

		empty.addCharAt(0, 'q');
		assertThat(empty, containsChars('q'));

		list.addCharAt(2, 'q');
		assertThat(list, containsChars('a', 'b', 'q', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void addAllCharsArray() {
		assertThat(empty.addAllChars(), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllChars('a', 'b', 'c'), is(true));
		assertThat(empty, containsChars('a', 'b', 'c'));

		assertThat(list.addAllChars('f', 'g', 'h'), is(true));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllCharsCharCollection() {
		assertThat(empty.addAllChars(new BitCharSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllChars(new BitCharSet('a', 'b', 'c')), is(true));
		assertThat(empty, containsChars('a', 'b', 'c'));

		assertThat(list.addAllChars(new BitCharSet('f', 'g', 'h')), is(true));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllCharsArrayCharList() {
		assertThat(empty.addAllChars(ArrayCharList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllChars(ArrayCharList.create('a', 'b', 'c')), is(true));
		assertThat(empty, containsChars('a', 'b', 'c'));

		assertThat(list.addAllChars(ArrayCharList.create('f', 'g', 'h')), is(true));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllCharsAtAtArray() {
		assertThat(empty.addAllCharsAt(0), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllCharsAt(0, 'a', 'b', 'c'), is(true));
		assertThat(empty, containsChars('a', 'b', 'c'));

		assertThat(list.addAllCharsAt(2, 'q', 'p', 'r'), is(true));
		assertThat(list, containsChars('a', 'b', 'q', 'p', 'r', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void addAllCharsAtCharCollection() {
		assertThat(empty.addAllCharsAt(0, new BitCharSet()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllCharsAt(0, new BitCharSet('a', 'b', 'c')), is(true));
		assertThat(empty, containsChars('a', 'b', 'c'));

		assertThat(list.addAllCharsAt(2, new BitCharSet('p', 'q', 'r')), is(true));
		assertThat(list, containsChars('a', 'b', 'p', 'q', 'r', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void addAllCharsAtArrayCharList() {
		assertThat(empty.addAllCharsAt(0, ArrayCharList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllCharsAt(0, ArrayCharList.create('a', 'b', 'c')), is(true));
		assertThat(empty, containsChars('a', 'b', 'c'));

		assertThat(list.addAllCharsAt(2, ArrayCharList.create('q', 'p', 'r')), is(true));
		assertThat(list, containsChars('a', 'b', 'q', 'p', 'r', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void removeChar() {
		assertThat(empty.removeChar('q'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeChar('q'), is(false));
		assertThat(list.removeChar('b'), is(true));
		assertThat(list, containsChars('a', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void removeCharAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.removeCharAt(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeCharAt(2), is('c'));
		assertThat(list, containsChars('a', 'b', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void containsChar() {
		assertThat(empty.containsChar('q'), is(false));

		assertThat(list.containsChar('q'), is(false));
		assertThat(list.containsChar('b'), is(true));
	}

	@Test
	public void containsAllCharsArray() {
		assertThat(empty.containsAllChars('q', 'p', 'r'), is(false));

		assertThat(list.containsAllChars('q', 'p', 'r'), is(false));
		assertThat(list.containsAllChars('a', 'b', 'c'), is(true));
	}

	@Test
	public void containsAllCharsCollection() {
		assertThat(empty.containsAllChars(ArrayCharList.create('q', 'p', 'r')), is(false));

		assertThat(list.containsAllChars(ArrayCharList.create('q', 'p', 'r')), is(false));
		assertThat(list.containsAllChars(ArrayCharList.create('a', 'b', 'c')), is(true));
	}

	@Test
	public void containsAnyCharsArray() {
		assertThat(empty.containsAnyChars('q', 'p', 'r'), is(false));

		assertThat(list.containsAnyChars('q', 'p', 'r'), is(false));
		assertThat(list.containsAnyChars('a', 'q', 'c'), is(true));
	}

	@Test
	public void containsAnyCharsCollection() {
		assertThat(empty.containsAnyChars(ArrayCharList.create('q', 'p', 'r')), is(false));

		assertThat(list.containsAnyChars(ArrayCharList.create('q', 'p', 'r')), is(false));
		assertThat(list.containsAnyChars(ArrayCharList.create('a', 'q', 'c')), is(true));
	}

	@Test
	public void removeAllCharsArray() {
		assertThat(empty.removeAllChars('a', 'b', 'c', 'q'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllChars('q', 'q', 'r'), is(false));
		assertThat(list.removeAllChars('a', 'b', 'c', 'q'), is(true));
		assertThat(list, containsChars('d', 'e', 'd', 'e'));
	}

	@Test
	public void removeAllCharsCollection() {
		assertThat(empty.removeAllChars(ArrayCharList.create('a', 'b', 'c', 'q')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllChars(ArrayCharList.create('q', 'p', 'r')), is(false));
		assertThat(list.removeAllChars(ArrayCharList.create('a', 'b', 'c', 'q')), is(true));
		assertThat(list, containsChars('d', 'e', 'd', 'e'));
	}

	@Test
	public void removeCharsIf() {
		assertThat(empty.removeCharsIf(x -> x > 'c'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeCharsIf(x -> x > 'e'), is(false));
		assertThat(list.removeCharsIf(x -> x > 'c'), is(true));
		assertThat(list, containsChars('a', 'b', 'c', 'a', 'b', 'c'));
	}

	@Test
	public void retainAllCharsArray() {
		assertThat(empty.retainAllChars('a', 'b', 'c', 'q'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllChars('a', 'b', 'c', 'q'), is(true));
		assertThat(list, containsChars('a', 'b', 'c', 'a', 'b', 'c'));
	}

	@Test
	public void retainAllCharsCharList() {
		assertThat(empty.retainAllChars(CharList.create('a', 'b', 'c', 'q')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllChars(CharList.create('a', 'b', 'c', 'q')), is(true));
		assertThat(list, containsChars('a', 'b', 'c', 'a', 'b', 'c'));
	}

	@Test
	public void replaceAllChars() {
		empty.replaceAllChars(x -> (char) (x + 1));
		assertThat(empty, is(emptyIterable()));

		list.replaceAllChars(x -> (char) (x + 1));
		assertThat(list, containsChars('b', 'c', 'd', 'e', 'f', 'b', 'c', 'd', 'e', 'f'));
	}

	@Test
	public void forEachChar() {
		empty.forEachChar(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger index = new AtomicInteger(0);
		list.forEachChar(x -> assertThat(x, is((char) (index.getAndIncrement() % 5 + 'a'))));
		assertThat(index.get(), is(10));
	}
}
