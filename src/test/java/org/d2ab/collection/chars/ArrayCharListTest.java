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

import org.d2ab.collection.ints.ArrayIntList;
import org.d2ab.iterator.chars.CharIterator;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class ArrayCharListTest {
	private final ArrayCharList empty = new ArrayCharList();
	private final ArrayCharList list = ArrayCharList.of('a', 'b', 'c', 'd', 'e');

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
	public void toCharArray() {
		assertArrayEquals(new char[0], empty.toCharArray());
		assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e'}, list.toCharArray());
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void listIteratorEmpty() {
		CharListIterator emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		CharListIterator listIterator = list.listIterator();

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

		listIterator.add('r');
		listIterator.add('s');
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(4));
		assertThat(listIterator.previousIndex(), is(3));
		assertThat(listIterator.nextChar(), is('c'));

		assertThat(list, containsChars('a', 'q', 'r', 's', 'c', 'd', 'e'));
	}

	@Test
	public void exhaustiveListIterator() {
		CharListIterator listIterator = list.listIterator();

		AtomicInteger i = new AtomicInteger();
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextChar(), is((char) (i.get() + 'a')));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previousChar(), is((char) (i.get() + 'a')));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
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

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemoveBackwards() {
		int i = 5;
		CharListIterator listIterator = list.listIterator(i);

		while (listIterator.hasPrevious()) {
			i--;
			assertThat(listIterator.previousChar(), is((char) (i + 'a')));
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(i));
			assertThat(listIterator.previousIndex(), is(i - 1));
		}
		assertThat(i, is(0));

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void subList() {
		CharList list = CharList.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j');

		CharList subList = list.subList(2, 8);
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

		subList.removeIf(x -> x != 'g');
		twice(() -> assertThat(subList, containsChars('g')));
		twice(() -> assertThat(list, containsChars('a', 'b', 'g', 'i', 'j')));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(list, containsChars('a', 'b', 'i', 'j')));
	}

	@Test
	public void intStream() {
		assertThat(empty.intStream().collect(ArrayIntList::new, ArrayIntList::addInt, ArrayIntList::addAllInts),
		           is(emptyIterable()));

		assertThat(list.intStream().collect(ArrayIntList::new, ArrayIntList::addInt, ArrayIntList::addAllInts),
		           containsInts('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void parallelIntStream() {
		assertThat(empty.parallelIntStream().collect(ArrayIntList::new, ArrayIntList::addInt,
		                                             ArrayIntList::addAllInts),
		           is(emptyIterable()));

		assertThat(list.parallelIntStream().collect(ArrayIntList::new, ArrayIntList::addInt, ArrayIntList::addAllInts),
		           containsInts('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(list.sequence(), containsChars('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void lastIndexOfBoxed() {
		assertThat(empty.lastIndexOf('q'), is(-1));

		assertThat(list.lastIndexOf('q'), is(-1));
		assertThat(list.lastIndexOf('b'), is(1));
	}

	@Test
	public void lastIndexOfChar() {
		assertThat(empty.lastIndexOfChar('q'), is(-1));

		assertThat(list.lastIndexOfChar('q'), is(-1));
		assertThat(list.lastIndexOfChar('b'), is(1));
	}

	@Test
	public void indexOfBoxed() {
		assertThat(empty.indexOf('q'), is(-1));

		assertThat(list.indexOf('q'), is(-1));
		assertThat(list.indexOf('b'), is(1));
	}

	@Test
	public void indexOfChar() {
		assertThat(empty.indexOfChar('q'), is(-1));

		assertThat(list.indexOfChar('q'), is(-1));
		assertThat(list.indexOfChar('b'), is(1));
	}

	@Test
	public void getBoxed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.get(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.get(2), is('c'));
	}

	@Test
	public void getChar() {
		expecting(IndexOutOfBoundsException.class, () -> empty.getChar(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.getChar(2), is('c'));
	}

	@Test
	public void setBoxed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.set(2, 'q'));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.set(2, 'q'), is('c'));
		assertThat(list, containsChars('a', 'b', 'q', 'd', 'e'));
	}

	@Test
	public void setChar() {
		expecting(IndexOutOfBoundsException.class, () -> empty.setChar(2, 'q'));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.setChar(2, 'q'), is('c'));
		assertThat(list, containsChars('a', 'b', 'q', 'd', 'e'));
	}

	@Test
	public void addBoxed() {
		empty.add('q');
		assertThat(empty, containsChars('q'));

		list.add('q');
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'q'));
	}

	@Test
	public void addChar() {
		empty.addChar('q');
		assertThat(empty, containsChars('q'));

		list.addChar('q');
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'q'));
	}

	@Test
	public void addAtBoxed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.add(2, 'q'));
		assertThat(empty, is(emptyIterable()));

		empty.add(0, 'q');
		assertThat(empty, containsChars('q'));

		list.add(2, 'q');
		assertThat(list, containsChars('a', 'b', 'q', 'c', 'd', 'e'));
	}

	@Test
	public void addCharAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.addCharAt(2, 'q'));
		assertThat(empty, is(emptyIterable()));

		empty.addCharAt(0, 'q');
		assertThat(empty, containsChars('q'));

		list.addCharAt(2, 'q');
		assertThat(list, containsChars('a', 'b', 'q', 'c', 'd', 'e'));
	}

	@Test
	public void addAllBoxed() {
		empty.addAll(Arrays.asList('a', 'b', 'c'));
		assertThat(empty, containsChars('a', 'b', 'c'));

		list.addAll(Arrays.asList('f', 'g', 'h'));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllCharsArray() {
		empty.addAllChars('a', 'b', 'c');
		assertThat(empty, containsChars('a', 'b', 'c'));

		list.addAllChars('f', 'g', 'h');
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllCharsCollection() {
		empty.addAllChars(ArrayCharList.of('a', 'b', 'c'));
		assertThat(empty, containsChars('a', 'b', 'c'));

		list.addAllChars(ArrayCharList.of('f', 'g', 'h'));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllAtBoxed() {
		empty.addAll(0, Arrays.asList('a', 'b', 'c'));
		assertThat(empty, containsChars('a', 'b', 'c'));

		list.addAll(2, Arrays.asList('q', 'r', 's'));
		assertThat(list, containsChars('a', 'b', 'q', 'r', 's', 'c', 'd', 'e'));
	}

	@Test
	public void addAllCharsAtAtArray() {
		empty.addAllCharsAt(0, 'a', 'b', 'c');
		assertThat(empty, containsChars('a', 'b', 'c'));

		list.addAllCharsAt(2, 'q', 'r', 's');
		assertThat(list, containsChars('a', 'b', 'q', 'r', 's', 'c', 'd', 'e'));
	}

	@Test
	public void addAllCharAtCollection() {
		empty.addAllCharsAt(0, ArrayCharList.of('a', 'b', 'c'));
		assertThat(empty, containsChars('a', 'b', 'c'));

		list.addAllCharsAt(2, 'q', 'r', 's');
		assertThat(list, containsChars('a', 'b', 'q', 'r', 's', 'c', 'd', 'e'));
	}

	@Test
	public void removeBoxed() {
		assertThat(empty.remove((Character) 'q'), is(false));
		assertThat(empty.remove(new Object()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove((Character) 'q'), is(false));
		assertThat(list.remove(new Object()), is(false));
		assertThat(list.remove((Character) 'b'), is(true));
		assertThat(list, containsChars('a', 'c', 'd', 'e'));
	}

	@Test
	public void removeChar() {
		assertThat(empty.removeChar('q'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeChar('q'), is(false));
		assertThat(list.removeChar('b'), is(true));
		assertThat(list, containsChars('a', 'c', 'd', 'e'));
	}

	@Test
	public void removeAtBoxed() {
		expecting(IndexOutOfBoundsException.class, () -> empty.remove(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.remove(2), is('c'));
		assertThat(list, containsChars('a', 'b', 'd', 'e'));
	}

	@Test
	public void removeCharAt() {
		expecting(IndexOutOfBoundsException.class, () -> empty.removeCharAt(2));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeCharAt(2), is('c'));
		assertThat(list, containsChars('a', 'b', 'd', 'e'));
	}

	@Test
	public void containsBoxed() {
		assertThat(empty.contains('q'), is(false));
		assertThat(empty.contains(new Object()), is(false));

		assertThat(list.contains('q'), is(false));
		assertThat(list.contains(new Object()), is(false));
		assertThat(list.contains('b'), is(true));
	}

	@Test
	public void containsChar() {
		assertThat(empty.containsChar('q'), is(false));

		assertThat(list.containsChar('q'), is(false));
		assertThat(list.containsChar('b'), is(true));
	}

	@Test
	public void containsAllBoxed() {
		assertThat(empty.containsAll(Arrays.asList('q', 'r', 's', new Object())), is(false));

		assertThat(list.containsAll(Arrays.asList('q', 'r', 's', new Object())), is(false));
		assertThat(list.containsAll(Arrays.asList('a', 'b', 'c')), is(true));
	}

	@Test
	public void containsAllCharsArray() {
		assertThat(empty.containsAllChars('q', 'r', 's'), is(false));

		assertThat(list.containsAllChars('q', 'r', 's'), is(false));
		assertThat(list.containsAllChars('a', 'b', 'c'), is(true));
	}

	@Test
	public void containsAllCharsCollection() {
		assertThat(empty.containsAllChars(ArrayCharList.of('q', 'r', 's')), is(false));

		assertThat(list.containsAllChars(ArrayCharList.of('q', 'r', 's')), is(false));
		assertThat(list.containsAllChars(ArrayCharList.of('a', 'b', 'c')), is(true));
	}

	@Test
	public void containsAnyCharsArray() {
		assertThat(empty.containsAnyChars('q', 'r', 's'), is(false));

		assertThat(list.containsAnyChars('q', 'r', 's'), is(false));
		assertThat(list.containsAnyChars('a', 'q', 'c'), is(true));
	}

	@Test
	public void containsAnyCharsCollection() {
		assertThat(empty.containsAnyChars(ArrayCharList.of('q', 'r', 's')), is(false));

		assertThat(list.containsAnyChars(ArrayCharList.of('q', 'r', 's')), is(false));
		assertThat(list.containsAnyChars(ArrayCharList.of('a', 'q', 'c')), is(true));
	}

	@Test
	public void removeAllBoxed() {
		assertThat(empty.removeAll(Arrays.asList('a', 'b', 'c', 'q')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(Arrays.asList('q', 'r', 's')), is(false));
		assertThat(list.removeAll(Arrays.asList('a', 'b', 'c', 'q')), is(true));
		assertThat(list, containsChars('d', 'e'));
	}

	@Test
	public void removeAllCharsArray() {
		assertThat(empty.removeAllChars('a', 'b', 'c', 'q'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllChars('q', 'q', 's'), is(false));
		assertThat(list.removeAllChars('a', 'b', 'c', 'q'), is(true));
		assertThat(list, containsChars('d', 'e'));
	}

	@Test
	public void removeAllCharsCollection() {
		assertThat(empty.removeAllChars(ArrayCharList.of('a', 'b', 'c', 'q')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAllChars(ArrayCharList.of('q', 'r', 's')), is(false));
		assertThat(list.removeAllChars(ArrayCharList.of('a', 'b', 'c', 'q')), is(true));
		assertThat(list, containsChars('d', 'e'));
	}

	@Test
	public void removeIfBoxed() {
		assertThat(empty.removeIf(x -> x > 'c'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeIf(x -> x > 'e'), is(false));
		assertThat(list.removeIf(x -> x > 'c'), is(true));
		assertThat(list, containsChars('a', 'b', 'c'));
	}

	@Test
	public void removeCharsIf() {
		assertThat(empty.removeCharsIf(x -> x > 'c'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeCharsIf(x -> x > 'e'), is(false));
		assertThat(list.removeCharsIf(x -> x > 'c'), is(true));
		assertThat(list, containsChars('a', 'b', 'c'));
	}

	@Test
	public void retainAllBoxed() {
		assertThat(empty.retainAll(Arrays.asList('a', 'b', 'c', 'q')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(Arrays.asList('a', 'b', 'c', 'q')), is(true));
		assertThat(list, containsChars('a', 'b', 'c'));
	}

	@Test
	public void retainAllCharsArray() {
		assertThat(empty.retainAllChars('a', 'b', 'c', 'q'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllChars('a', 'b', 'c', 'q'), is(true));
		assertThat(list, containsChars('a', 'b', 'c'));
	}

	@Test
	public void retainAllCharsCollection() {
		assertThat(empty.retainAllChars(ArrayCharList.of('a', 'b', 'c', 'q')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAllChars(ArrayCharList.of('a', 'b', 'c', 'q')), is(true));
		assertThat(list, containsChars('a', 'b', 'c'));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> (char) (x + 1));
		assertThat(empty, is(emptyIterable()));

		list.replaceAll(x -> (char) (x + 1));
		assertThat(list, containsChars('b', 'c', 'd', 'e', 'f'));
	}

	@Test
	public void replaceAllChars() {
		empty.replaceAllChars(x -> (char) (x + 1));
		assertThat(empty, is(emptyIterable()));

		list.replaceAllChars(x -> (char) (x + 1));
		assertThat(list, containsChars('b', 'c', 'd', 'e', 'f'));
	}

	@Test
	public void forEachBoxed() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(0);
		list.forEach(x -> assertThat(x, is((char) (value.getAndIncrement() + 'a'))));
		assertThat(value.get(), is(5));
	}

	@Test
	public void forEachLong() {
		empty.forEachChar(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(0);
		list.forEachChar(x -> assertThat(x, is((char) (value.getAndIncrement() + 'a'))));
		assertThat(value.get(), is(5));
	}
}