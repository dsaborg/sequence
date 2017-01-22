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

package org.d2ab.sequence;

import org.d2ab.collection.Lists;
import org.d2ab.collection.chars.CharList;
import org.d2ab.collection.chars.CharListIterator;
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

public class CharSeqAsListTest {
	private final CharSeq empty = CharSeq.from(CharList.create());
	private final CharList emptyList = empty.asList();

	private final CharSeq sequence = CharSeq.from(CharList.create('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	private final CharList list = sequence.asList();

	@Test
	public void subList() {
		CharList subList = list.subList(2, 8);
		twice(() -> assertThat(subList, containsChars('c', 'd', 'e', 'a', 'b', 'c')));

		assertThat(subList.removeCharAt(1), is('d'));
		twice(() -> assertThat(subList, containsChars('c', 'e', 'a', 'b', 'c')));
		twice(() -> assertThat(sequence, containsChars('a', 'b', 'c', 'e', 'a', 'b', 'c', 'd', 'e')));

		assertThat(subList.removeChar('e'), is(true));
		twice(() -> assertThat(subList, containsChars('c', 'a', 'b', 'c')));
		twice(() -> assertThat(sequence, containsChars('a', 'b', 'c', 'a', 'b', 'c', 'd', 'e')));

		CharIterator subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextChar(), is('c'));
		subListIterator.remove();
		twice(() -> assertThat(subList, containsChars('a', 'b', 'c')));
		twice(() -> assertThat(sequence, containsChars('a', 'b', 'a', 'b', 'c', 'd', 'e')));

		subList.removeCharsIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, containsChars('a', 'c')));
		twice(() -> assertThat(sequence, containsChars('a', 'b', 'a', 'c', 'd', 'e')));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
		twice(() -> assertThat(sequence, containsChars('a', 'b', 'd', 'e')));
	}

	@Test
	public void size() {
		assertThat(emptyList.size(), is(0));
		assertThat(list.size(), is(10));
	}

	@Test
	public void isEmpty() {
		assertThat(emptyList.isEmpty(), is(true));
		assertThat(list.isEmpty(), is(false));
	}

	@Test
	public void containsChar() {
		assertThat(emptyList.containsChar('b'), is(false));
		for (char x = 'a'; x <= 'e'; x++)
			assertThat(list.containsChar(x), is(true));
		assertThat(list.containsChar('q'), is(false));
	}

	@Test
	public void iterator() {
		assertThat(emptyList, is(emptyIterable()));
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
		assertThat(sequence, containsChars('a', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void toCharArray() {
		assertArrayEquals(new char[0], emptyList.toCharArray());
		assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'}, list.toCharArray());
	}

	@Test
	public void addChar() {
		expecting(UnsupportedOperationException.class, () -> emptyList.addChar('a'));
		assertThat(emptyList, is(emptyIterable()));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addChar('f'));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
		assertThat(sequence, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void removeChar() {
		assertThat(emptyList.removeChar('q'), is(false));

		assertThat(list.removeChar('b'), is(true));
		assertThat(list, containsChars('a', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
		assertThat(sequence, containsChars('a', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));

		assertThat(list.removeChar('q'), is(false));
		assertThat(list, containsChars('a', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
		assertThat(sequence, containsChars('a', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void containsAllChars() {
		assertThat(emptyList.containsAllChars(CharList.create('b', 'c')), is(false));

		assertThat(list.containsAllChars(CharList.create('b', 'c')), is(true));
		assertThat(list.containsAllChars(CharList.create('b', 'q')), is(false));
	}

	@Test
	public void addAllChars() {
		assertThat(emptyList.addAllChars(CharList.create()), is(false));
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> emptyList.addAllChars(CharList.create('a', 'b')));
		assertThat(emptyList, is(emptyIterable()));
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAllChars(CharList.create('f', 'g', 'h')));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
		assertThat(sequence, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void addAllCharsAt() {
		assertThat(emptyList.addAllCharsAt(0, CharList.create()), is(false));
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> emptyList.addAllCharsAt(0, CharList.create('a', 'b')));
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAllCharsAt(2, CharList.create('q', 'p', 'r')));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
		assertThat(sequence, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void removeAllChars() {
		assertThat(emptyList.removeAllChars(CharList.create('a', 'b')), is(false));
		assertThat(emptyList, is(emptyIterable()));

		assertThat(list.removeAllChars(CharList.create('a', 'b', 'e')), is(true));
		assertThat(list, containsChars('c', 'd', 'c', 'd'));
		assertThat(sequence, containsChars('c', 'd', 'c', 'd'));
	}

	@Test
	public void retainAllChars() {
		assertThat(emptyList.retainAllChars(CharList.create('a', 'b')), is(false));
		assertThat(emptyList, is(emptyIterable()));

		assertThat(list.retainAllChars(CharList.create('a', 'b', 'c')), is(true));
		assertThat(list, containsChars('a', 'b', 'c', 'a', 'b', 'c'));
		assertThat(sequence, containsChars('a', 'b', 'c', 'a', 'b', 'c'));
	}

	@Test
	public void replaceAllChars() {
		emptyList.replaceAllChars(x -> (char) (x + 1));
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAllChars(x -> (char) (x + 1)));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
		assertThat(sequence, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void sortChars() {
		expecting(UnsupportedOperationException.class, emptyList::sortChars);
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, list::sortChars);
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
		assertThat(sequence, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void clear() {
		emptyList.clear();
		assertThat(emptyList, is(emptyIterable()));

		list.clear();
		assertThat(list, is(emptyIterable()));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void testEquals() {
		assertThat(emptyList.equals(Lists.of()), is(true));
		assertThat(emptyList.equals(Lists.of('a', 'b')), is(false));

		assertThat(list.equals(Lists.of('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e')), is(true));
		assertThat(list.equals(Lists.of('e', 'd', 'c', 'b', 'a', 'e', 'd', 'c', 'b', 'a')), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(emptyList.hashCode(), is(Lists.of().hashCode()));
		assertThat(list.hashCode(), is(Lists.of('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e').hashCode()));
	}

	@Test
	public void testToString() {
		assertThat(emptyList.toString(), is("[]"));
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
		assertThat(sequence, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void addCharAt() {
		expecting(UnsupportedOperationException.class, () -> list.addCharAt(0, 'q'));
		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void indexOfChar() {
		assertThat(emptyList.indexOfChar('q'), is(-1));

		assertThat(list.indexOfChar('a'), is(0));
		assertThat(list.indexOfChar('c'), is(2));
		assertThat(list.indexOfChar('e'), is(4));
	}

	@Test
	public void lastIndexOfChar() {
		assertThat(emptyList.lastIndexOfChar('q'), is(-1));

		assertThat(list.lastIndexOfChar('a'), is(5));
		assertThat(list.lastIndexOfChar('c'), is(7));
		assertThat(list.lastIndexOfChar('e'), is(9));
	}

	@Test
	public void listIteratorEmpty() {
		CharListIterator emptyIterator = emptyList.listIterator();
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

		assertThat(emptyList, is(emptyIterable()));
		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		CharListIterator listIterator = list.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));
		assertThat(listIterator.nextChar(), is('a'));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.nextChar(), is('b'));

		expecting(UnsupportedOperationException.class, () -> listIterator.add('q'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.nextChar(), is('c'));

		expecting(UnsupportedOperationException.class, () -> listIterator.set('q'));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.nextChar(), is('d'));

		assertThat(list, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
		assertThat(sequence, containsChars('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
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
		assertThat(sequence, is(emptyIterable()));
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
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void stream() {
		assertThat(emptyList.intStream().mapToObj(x -> (char) x).collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.intStream().mapToObj(x -> (char) x).collect(Collectors.toList()),
		           contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void parallelStream() {
		assertThat(emptyList.parallelIntStream().mapToObj(x -> (char) x).collect(Collectors.toList()),
		           is(emptyIterable()));
		assertThat(list.parallelIntStream().mapToObj(x -> (char) x).collect(Collectors.toList()),
		           contains('a', 'b', 'c', 'd', 'e', 'a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void removeCharsIf() {
		emptyList.removeCharsIf(x -> x == 'a');
		assertThat(emptyList, is(emptyIterable()));

		list.removeCharsIf(x -> x == 'a');
		assertThat(list, containsChars('b', 'c', 'd', 'e', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void forEachChar() {
		emptyList.forEachChar(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger i = new AtomicInteger(0);
		list.forEachChar(x -> assertThat(x, is((char) (i.getAndIncrement() % 5 + 'a'))));
		assertThat(i.get(), is(10));
	}
}
