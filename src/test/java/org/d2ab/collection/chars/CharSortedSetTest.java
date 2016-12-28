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

import org.d2ab.collection.Arrayz;
import org.d2ab.collection.ints.IntList;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.test.StrictCharIterator;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CharSortedSetTest {
	private final CharSet backingEmpty = new BitCharSet();
	private final CharSortedSet empty = new CharSortedSet.Base() {
		@Override
		public CharIterator iterator() {
			return StrictCharIterator.from(backingEmpty.iterator());
		}

		@Override
		public int size() {
			return backingEmpty.size();
		}

		@Override
		public boolean addChar(char x) {
			return backingEmpty.addChar(x);
		}
	};

	private final CharSet backing = new BitCharSet('a', 'b', 'c', 'd', 'e');
	private final CharSortedSet set = new CharSortedSet.Base() {
		@Override
		public CharIterator iterator() {
			return StrictCharIterator.from(backing.iterator());
		}

		@Override
		public int size() {
			return backing.size();
		}

		@Override
		public boolean addChar(char x) {
			return backing.addChar(x);
		}
	};

	@Test
	public void createEmpty() {
		CharSortedSet set = CharSortedSet.create();
		assertThat(set, is(emptyIterable()));

		set.addChar('b');
		set.addChar('c');
		set.addChar('a');
		assertThat(set, containsChars('a', 'b', 'c'));
	}

	@Test
	public void create() {
		CharSortedSet set = CharSortedSet.create('b', 'c', 'a');
		assertThat(set, containsChars('a', 'b', 'c'));

		set.addChar('e');
		set.addChar('d');
		assertThat(set, containsChars('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(set.size(), is(5));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(set, containsChars('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void iteratorFailFast() {
		CharIterator it1 = set.iterator();
		set.addChar('q');
		expecting(ConcurrentModificationException.class, it1::nextChar);

		CharIterator it2 = set.iterator();
		set.removeChar('q');
		expecting(ConcurrentModificationException.class, it2::nextChar);
	}

	@Test
	public void isEmpty() {
		assertThat(empty.isEmpty(), is(true));
		assertThat(set.isEmpty(), is(false));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty.isEmpty(), is(true));

		set.clear();
		assertThat(set.isEmpty(), is(true));
	}

	@Test
	public void addChar() {
		empty.addChar('q');
		assertThat(empty, containsChars('q'));

		set.addChar('q');
		assertThat(set, containsChars('a', 'b', 'c', 'd', 'e', 'q'));
	}

	@Test
	public void containsChar() {
		assertThat(empty.containsChar('q'), is(false));

		assertThat(set.containsChar('q'), is(false));
		for (char x = 'a'; x <= 'e'; x++)
			assertThat(set.containsChar(x), is(true));
	}

	@Test
	public void removeChar() {
		assertThat(empty.removeChar('q'), is(false));

		assertThat(set.removeChar('q'), is(false));
		for (char x = 'a'; x <= 'e'; x++)
			assertThat(set.removeChar(x), is(true));
		assertThat(set.isEmpty(), is(true));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(set.toString(), is("[a, b, c, d, e]"));
	}

	@Test
	public void equalsEdgeCases() {
		assertThat(set, is(equalTo(set)));
		assertThat(set, is(not(equalTo(null))));
		assertThat(set, is(not(equalTo(new Object()))));
		assertThat(set, is(not(equalTo(asList('a', 'b', 'c', 'd', 'e')))));
		assertThat(set, is(not(equalTo(CharList.create('a', 'b', 'c', 'd', 'e')))));
	}

	@Test
	public void equalsHashCodeAgainstSet() {
		Set<Character> set2 = new HashSet<>(asList('a', 'b', 'c', 'd', 'e', 'q'));
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.remove('q');

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void equalsHashCodeAgainstCharSet() {
		BitCharSet set2 = new BitCharSet('a', 'b', 'c', 'd', 'e', 'q');
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.removeChar('q');

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void subSet() {
		CharSortedSet subSet = set.subSet('b', 'e');
		assertThat(subSet, containsChars('b', 'c', 'd'));
		assertThat(subSet.size(), is(3));
		assertThat(subSet.firstChar(), is('b'));
		assertThat(subSet.lastChar(), is('d'));
		assertThat(subSet.containsChar('b'), is(true));
		assertThat(subSet.containsChar('e'), is(false));
		assertThat(subSet.toString(), is("[b, c, d]"));

		Set<Character> equivalentSet = new HashSet<>(asList('b', 'c', 'd'));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(subSet.removeChar('b'), is(true));
		assertThat(subSet, containsChars('c', 'd'));
		assertThat(subSet.size(), is(2));
		assertThat(set, containsChars('a', 'c', 'd', 'e'));

		assertThat(subSet.removeChar('b'), is(false));
		assertThat(subSet, containsChars('c', 'd'));
		assertThat(subSet.size(), is(2));
		assertThat(set, containsChars('a', 'c', 'd', 'e'));

		assertThat(subSet.addChar('b'), is(true));
		assertThat(subSet, containsChars('b', 'c', 'd'));
		assertThat(subSet.size(), is(3));
		assertThat(set, containsChars('a', 'b', 'c', 'd', 'e'));

		assertThat(subSet.addChar('b'), is(false));
		assertThat(subSet, containsChars('b', 'c', 'd'));
		assertThat(subSet.size(), is(3));
		assertThat(set, containsChars('a', 'b', 'c', 'd', 'e'));

		expecting(IllegalArgumentException.class, () -> subSet.addChar('f'));
		assertThat(subSet, containsChars('b', 'c', 'd'));
		assertThat(subSet.size(), is(3));
		assertThat(set, containsChars('a', 'b', 'c', 'd', 'e'));

		subSet.clear();
		assertThat(subSet, is(emptyIterable()));
		assertThat(subSet.size(), is(0));
		assertThat(set, containsChars('a', 'e'));
	}

	@Test
	public void sparseSubSet() {
		BitCharSet set = new BitCharSet('b', 'd', 'f', 'h');
		CharSortedSet subSet = set.subSet('c', 'g');
		assertThat(subSet, containsChars('d', 'f'));
		assertThat(subSet.size(), is(2));
		assertThat(subSet.firstChar(), is('d'));
		assertThat(subSet.lastChar(), is('f'));
		assertThat(subSet.containsChar('d'), is(true));
		assertThat(subSet.containsChar('h'), is(false));
		assertThat(subSet.toString(), is("[d, f]"));

		Set<Character> equivalentSet = new HashSet<>(asList('d', 'f'));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void headSet() {
		CharSortedSet headSet = set.headSet('d');
		assertThat(headSet, containsChars('a', 'b', 'c'));
		assertThat(headSet.size(), is(3));
		assertThat(headSet.firstChar(), is('a'));
		assertThat(headSet.lastChar(), is('c'));
		assertThat(headSet.containsChar('b'), is(true));
		assertThat(headSet.containsChar('d'), is(false));
		assertThat(headSet.toString(), is("[a, b, c]"));

		Set<Character> equivalentSet = new HashSet<>(asList('a', 'b', 'c'));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(headSet.removeChar('b'), is(true));
		assertThat(headSet, containsChars('a', 'c'));
		assertThat(headSet.size(), is(2));
		assertThat(set, containsChars('a', 'c', 'd', 'e'));

		assertThat(headSet.removeChar('b'), is(false));
		assertThat(headSet, containsChars('a', 'c'));
		assertThat(headSet.size(), is(2));
		assertThat(set, containsChars('a', 'c', 'd', 'e'));

		assertThat(headSet.addChar('b'), is(true));
		assertThat(headSet, containsChars('a', 'b', 'c'));
		assertThat(headSet.size(), is(3));
		assertThat(set, containsChars('a', 'b', 'c', 'd', 'e'));

		assertThat(headSet.addChar('b'), is(false));
		assertThat(headSet, containsChars('a', 'b', 'c'));
		assertThat(headSet.size(), is(3));
		assertThat(set, containsChars('a', 'b', 'c', 'd', 'e'));

		expecting(IllegalArgumentException.class, () -> headSet.addChar('q'));
		assertThat(headSet, containsChars('a', 'b', 'c'));
		assertThat(headSet.size(), is(3));
		assertThat(set, containsChars('a', 'b', 'c', 'd', 'e'));

		headSet.clear();
		assertThat(headSet, is(emptyIterable()));
		assertThat(headSet.size(), is(0));
		assertThat(set, containsChars('d', 'e'));
	}

	@Test
	public void sparseHeadSet() {
		BitCharSet set = new BitCharSet('b', 'd', 'f', 'h');
		CharSortedSet headSet = set.headSet('e');
		assertThat(headSet, containsChars('b', 'd'));
		assertThat(headSet.size(), is(2));
		assertThat(headSet.firstChar(), is('b'));
		assertThat(headSet.lastChar(), is('d'));
		assertThat(headSet.containsChar('b'), is(true));
		assertThat(headSet.containsChar('f'), is(false));
		assertThat(headSet.toString(), is("[b, d]"));

		Set<Character> equivalentSet = new HashSet<>(asList('b', 'd'));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void tailSet() {
		CharSortedSet tailSet = set.tailSet('c');
		assertThat(tailSet, containsChars('c', 'd', 'e'));
		assertThat(tailSet.size(), is(3));
		assertThat(tailSet.firstChar(), is('c'));
		assertThat(tailSet.lastChar(), is('e'));
		assertThat(tailSet.containsChar('d'), is(true));
		assertThat(tailSet.containsChar('a'), is(false));
		assertThat(tailSet.toString(), is("[c, d, e]"));

		Set<Character> equivalentSet = new HashSet<>(asList('c', 'd', 'e'));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(tailSet.removeChar('d'), is(true));
		assertThat(tailSet, containsChars('c', 'e'));
		assertThat(tailSet.size(), is(2));
		assertThat(set, containsChars('a', 'b', 'c', 'e'));

		assertThat(tailSet.removeChar('d'), is(false));
		assertThat(tailSet, containsChars('c', 'e'));
		assertThat(tailSet.size(), is(2));
		assertThat(set, containsChars('a', 'b', 'c', 'e'));

		assertThat(tailSet.addChar('q'), is(true));
		assertThat(tailSet, containsChars('c', 'e', 'q'));
		assertThat(tailSet.size(), is(3));
		assertThat(set, containsChars('a', 'b', 'c', 'e', 'q'));

		assertThat(tailSet.addChar('q'), is(false));
		assertThat(tailSet, containsChars('c', 'e', 'q'));
		assertThat(tailSet.size(), is(3));
		assertThat(set, containsChars('a', 'b', 'c', 'e', 'q'));

		expecting(IllegalArgumentException.class, () -> tailSet.addChar('a'));
		assertThat(tailSet, containsChars('c', 'e', 'q'));
		assertThat(tailSet.size(), is(3));
		assertThat(set, containsChars('a', 'b', 'c', 'e', 'q'));

		assertThat(set.addChar('d'), is(true));
		assertThat(tailSet, containsChars('c', 'd', 'e', 'q'));
		assertThat(tailSet.size(), is(4));
		assertThat(set, containsChars('a', 'b', 'c', 'd', 'e', 'q'));

		tailSet.clear();
		assertThat(tailSet, is(emptyIterable()));
		assertThat(tailSet.size(), is(0));
		assertThat(set, containsChars('a', 'b'));
	}

	@Test
	public void sparseTailSet() {
		BitCharSet set = new BitCharSet('b', 'd', 'f', 'h');
		CharSortedSet tailSet = set.tailSet('e');
		assertThat(tailSet, containsChars('f', 'h'));
		assertThat(tailSet.size(), is(2));
		assertThat(tailSet.firstChar(), is('f'));
		assertThat(tailSet.lastChar(), is('h'));
		assertThat(tailSet.containsChar('f'), is(true));
		assertThat(tailSet.containsChar('d'), is(false));
		assertThat(tailSet.toString(), is("[f, h]"));

		Set<Character> equivalentSet = new HashSet<>(asList('f', 'h'));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void addAllCharArray() {
		assertThat(empty.addAllChars('a', 'b', 'c'), is(true));
		assertThat(empty, containsChars('a', 'b', 'c'));

		assertThat(set.addAllChars('c', 'd', 'e', 'f', 'g'), is(true));
		assertThat(set, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g'));
	}

	@Test
	public void addAllCharCollection() {
		assertThat(empty.addAllChars(CharList.create('a', 'b', 'c')), is(true));
		assertThat(empty, containsChars('a', 'b', 'c'));

		assertThat(set.addAllChars(CharList.create('c', 'd', 'e', 'f', 'g')), is(true));
		assertThat(set, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g'));
	}

	@Test
	public void intStream() {
		assertThat(empty.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));

		assertThat(set.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void parallelIntStream() {
		assertThat(empty.parallelIntStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));

		assertThat(set.parallelIntStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(set.sequence(), containsChars('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void firstChar() {
		expecting(NoSuchElementException.class, empty::firstChar);
		assertThat(set.firstChar(), is('a'));
	}

	@Test
	public void lastChar() {
		expecting(NoSuchElementException.class, empty::lastChar);
		assertThat(set.lastChar(), is('e'));
	}

	@Test
	public void iteratorRemoveAll() {
		CharIterator iterator = set.iterator();
		char value = 'a';
		while (iterator.hasNext()) {
			assertThat(iterator.nextChar(), is(value));
			iterator.remove();
			value++;
		}
		assertThat(value, is('f'));
		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void removeAllCharArray() {
		assertThat(empty.removeAllChars('a', 'b', 'c'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAllChars('a', 'b', 'c'), is(true));
		assertThat(set, containsChars('d', 'e'));
	}

	@Test
	public void removeAllCharCollection() {
		assertThat(empty.removeAll(CharList.create('a', 'b', 'c')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(set, containsChars('d', 'e'));
	}

	@Test
	public void retainAllCharArray() {
		assertThat(empty.retainAllChars('a', 'b', 'c'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAllChars('a', 'b', 'c'), is(true));
		assertThat(set, containsChars('a', 'b', 'c'));
	}

	@Test
	public void retainAllCharCollection() {
		assertThat(empty.retainAll(CharList.create('a', 'b', 'c')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(set, containsChars('a', 'b', 'c'));
	}

	@Test
	public void removeCharsIf() {
		assertThat(empty.removeCharsIf(x -> x > 'c'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeCharsIf(x -> x > 'c'), is(true));
		assertThat(set, containsChars('a', 'b', 'c'));
	}

	@Test
	public void containsAllCharArray() {
		assertThat(empty.containsAllChars('a', 'b', 'c'), is(false));
		assertThat(set.containsAllChars('a', 'b', 'c'), is(true));
		assertThat(set.containsAllChars('a', 'b', 'c', 'q'), is(false));
	}

	@Test
	public void containsAllCharCollection() {
		assertThat(empty.containsAll(CharList.create('a', 'b', 'c')), is(false));
		assertThat(set.containsAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(set.containsAll(CharList.create('a', 'b', 'c', 'q')), is(false));
	}

	@Test
	public void forEachChar() {
		empty.forEachChar(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger('a');
		set.forEachChar(x -> assertThat(x, is((char) value.getAndIncrement())));
		assertThat(value.get(), is((int) 'f'));
	}

	@Test
	public void containsCharCollection() {
		assertThat(empty.containsAll(asList('a', 'b', 'c')), is(false));
		assertThat(set.containsAll(asList('a', 'b', 'c')), is(true));
		assertThat(set.containsAll(asList('a', 'b', 'c', 'q')), is(false));
	}

	@Test
	public void boundaries() {
		BitCharSet charSet = new BitCharSet();
		assertThat(charSet.addChar(Character.MIN_VALUE), is(true));
		assertThat(charSet.addChar(Character.MAX_VALUE), is(true));

		assertThat(charSet, containsChars(Character.MIN_VALUE, Character.MAX_VALUE));

		assertThat(charSet.containsChar(Character.MIN_VALUE), is(true));
		assertThat(charSet.containsChar(Character.MAX_VALUE), is(true));

		assertThat(charSet.removeChar(Character.MIN_VALUE), is(true));
		assertThat(charSet.removeChar(Character.MAX_VALUE), is(true));

		assertThat(charSet, is(emptyIterable()));
	}

	@Test
	public void fuzz() {
		char[] randomValues = new char[1000];
		Random random = new Random();
		for (int i = 0; i < randomValues.length; i++) {
			char randomValue;
			do
				randomValue = (char) random.nextInt(Character.MAX_VALUE + 1);
			while (Arrayz.contains(randomValues, randomValue));
			randomValues[i] = randomValue;
		}

		// Adding
		for (char randomValue : randomValues)
			assertThat(empty.addChar(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		for (char randomValue : randomValues)
			assertThat(empty.addChar(randomValue), is(false));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAllChars(randomValues), is(true));

		for (char randomValue : randomValues)
			assertThat(empty.containsChar(randomValue), is(true));

		// toString
		Arrays.sort(randomValues);
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (char randomValue : randomValues)
			assertThat(empty.removeChar(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty.size(), is(0));

		for (char randomValue : randomValues)
			assertThat(empty.removeChar(randomValue), is(false));
	}
}
