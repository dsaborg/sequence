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
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CharSetTest {
	private final CharSet empty = CharSet.Base.create();
	private final CharSet set = CharSet.Base.create('a', 'b', 'c', 'd', 'e');

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
