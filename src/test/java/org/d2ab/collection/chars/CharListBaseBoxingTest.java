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

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.naturalOrder;
import static org.d2ab.collection.Arrayz.fill;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class CharListBaseBoxingTest {
	private final CharList backingEmpty = CharList.create();
	private final List<Character> empty = new CharList.Base() {
		@Override
		public CharIterator iterator() {
			return backingEmpty.iterator();
		}

		@Override
		public int size() {
			return backingEmpty.size();
		}
	};

	private final CharList backingList = CharList.create('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e');
	private final List<Character> list = new CharList.Base() {
		@Override
		public CharIterator iterator() {
			return backingList.iterator();
		}

		@Override
		public int size() {
			return backingList.size();
		}
	};

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
		assertThat(list.contains(new Object()), is(false));
		assertThat(list.contains(null), is(false));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		expecting(NoSuchElementException.class, () -> empty.iterator().next());

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
	public void add() {
		expecting(UnsupportedOperationException.class, () -> empty.add('a'));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.add('f'));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void remove() {
		assertThat(empty.remove((Character) 'q'), is(false));

		assertThat(list.remove((Character) 'b'), is(true));
		assertThat(list, contains('a', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));

		assertThat(list.remove((Character) 'q'), is(false));
		assertThat(list, contains('a', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));

		assertThat(list.remove(new Object()), is(false));
		assertThat(list, contains('a', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));

		assertThat(list.remove(null), is(false));
		assertThat(list, contains('a', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(asList('b', 'c')), is(false));

		assertThat(list.containsAll(asList('b', 'c')), is(true));
		assertThat(list.containsAll(asList('b', 'q')), is(false));
		assertThat(list.containsAll(singletonList(new Object())), is(false));
		assertThat(list.containsAll(singletonList(null)), is(false));
	}

	@Test
	public void addAll() {
		assertThat(empty.addAll(emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(CharList.of()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAll(asList('a', 'b')));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAll(asList('f', 'g', 'h')));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void addAllAt() {
		assertThat(empty.addAll(0, emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(0, CharList.of()), is(false));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> empty.addAll(0, asList('a', 'b')));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAll(2, asList('q', 'p', 'r')));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(asList('a', 'b')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.removeAll(asList('a', 'b', 'e')), is(true));
		assertThat(list, contains('c', 'd', 'c', 'd'));

		assertThat(list.removeAll(singletonList(new Object())), is(false));
		assertThat(list, contains('c', 'd', 'c', 'd'));

		assertThat(list.removeAll(singletonList(null)), is(false));
		assertThat(list, contains('c', 'd', 'c', 'd'));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(asList('a', 'b')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(list.retainAll(asList('a', 'b', 'c')), is(true));
		assertThat(list, contains('a', 'b', 'c', 'a', 'b', 'c'));
	}

	@Test
	public void retainAllObject() {
		assertThat(list.retainAll(singletonList(new Object())), is(true));
		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void retainAllNull() {
		assertThat(list.retainAll(singletonList(null)), is(true));
		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void replaceAll() {
		empty.replaceAll(x -> (char) (x + 1));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAll(x -> (char) (x + 1)));
		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void sort() {
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

		assertThat(list.indexOf('a'), is(0));
		assertThat(list.indexOf('c'), is(2));
		assertThat(list.indexOf('e'), is(4));
		assertThat(list.indexOf('q'), is(-1));
		assertThat(list.indexOf(new Object()), is(-1));
		assertThat(list.indexOf(null), is(-1));
	}

	@Test
	public void lastIndexOf() {
		assertThat(empty.lastIndexOf('q'), is(-1));

		assertThat(list.lastIndexOf('a'), is(5));
		assertThat(list.lastIndexOf('c'), is(7));
		assertThat(list.lastIndexOf('e'), is(9));
		assertThat(list.lastIndexOf('q'), is(-1));
		assertThat(list.lastIndexOf(new Object()), is(-1));
		assertThat(list.lastIndexOf(null), is(-1));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<Character> emptyIterator = empty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));
		expecting(NoSuchElementException.class, emptyIterator::next);
		expecting(UnsupportedOperationException.class, emptyIterator::previous);

		expecting(UnsupportedOperationException.class, () -> emptyIterator.add('q'));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(empty, is(emptyIterable()));
		expecting(IndexOutOfBoundsException.class, () -> empty.listIterator(1));
	}

	@Test
	public void listIterator() {
		ListIterator<Character> listIterator = list.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));
		assertThat(listIterator.next(), is('a'));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.next(), is('b'));

		expecting(UnsupportedOperationException.class, () -> listIterator.add('q'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.next(), is('c'));

		expecting(UnsupportedOperationException.class, () -> listIterator.set('q'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.next(), is('d'));

		assertThat(list, contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void exhaustiveListIterator() {
		twice(() -> {
			ListIterator<Character> listIterator = list.listIterator();
			AtomicInteger i = new AtomicInteger(0);
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is((char) (i.get() % 5 + 'a')));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
			expecting(NoSuchElementException.class, listIterator::next);
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
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.stream().collect(Collectors.toList()),
		           contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
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
