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
import org.d2ab.iterator.chars.CharIterator;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class CharSetBoxingTest {
	private final CharSet backingEmpty = new BitCharSet();
	private final Set<Character> empty = new CharSet.Base() {
		@Override
		public CharIterator iterator() {
			return backingEmpty.iterator();
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
	private final Set<Character> set = new CharSet.Base() {
		@Override
		public CharIterator iterator() {
			return backing.iterator();
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
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(set.size(), is(5));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(set, contains('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void iteratorFailFast() {
		Iterator<Character> it1 = set.iterator();
		set.add('q');
		expecting(ConcurrentModificationException.class, it1::next);

		Iterator<Character> it2 = set.iterator();
		set.remove('q');
		expecting(ConcurrentModificationException.class, it2::next);
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
	public void toArray() {
		assertArrayEquals(new Character[0], empty.toArray());
		assertArrayEquals(new Character[]{'a', 'b', 'c', 'd', 'e'}, set.toArray());
	}

	@Test
	public void toArrayWithType() {
		assertArrayEquals(new Character[0], empty.toArray(new Character[0]));
		assertArrayEquals(new Character[]{'a', 'b', 'c', 'd', 'e'}, set.toArray(new Character[0]));
	}

	@Test
	public void add() {
		empty.add('q');
		assertThat(empty, contains('q'));

		set.add('q');
		assertThat(set, contains('a', 'b', 'c', 'd', 'e', 'q'));
	}

	@Test
	public void boxedContains() {
		assertThat(empty.contains('q'), is(false));

		assertThat(set.contains('q'), is(false));
		for (char x = 'a'; x <= 'e'; x++)
			assertThat(set.contains(x), is(true));
	}

	@Test
	public void remove() {
		assertThat(empty.remove('q'), is(false));

		assertThat(set.remove('q'), is(false));
		for (char x = 'a'; x <= 'e'; x++)
			assertThat(set.remove(x), is(true));
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

		set2.remove('q');

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void addAllCharCollection() {
		assertThat(empty.addAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(empty, contains('a', 'b', 'c'));

		assertThat(set.addAll(CharList.create('c', 'd', 'e', 'f', 'g')), is(true));
		assertThat(set, contains('a', 'b', 'c', 'd', 'e', 'f', 'g'));
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Character> iterator = set.iterator();
		char value = 'a';
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is(value));
			iterator.remove();
			value++;
		}
		assertThat(value, is('f'));
		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void removeAllCharCollection() {
		assertThat(empty.removeAll(CharList.create('a', 'b', 'c')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(set, contains('d', 'e'));
	}

	@Test
	public void retainAllCharCollection() {
		assertThat(empty.retainAll(CharList.create('a', 'b', 'c')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(set, contains('a', 'b', 'c'));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 'c'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeIf(x -> x > 'c'), is(true));
		assertThat(set, contains('a', 'b', 'c'));
	}

	@Test
	public void containsAllCharCollection() {
		assertThat(empty.containsAll(CharList.create('a', 'b', 'c')), is(false));
		assertThat(set.containsAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(set.containsAll(CharList.create('a', 'b', 'c', 'q')), is(false));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger('a');
		set.forEach(x -> assertThat(x, is((char) value.getAndIncrement())));
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
		Set<Character> charSet = new BitCharSet();
		assertThat(charSet.add(Character.MIN_VALUE), is(true));
		assertThat(charSet.add(Character.MAX_VALUE), is(true));

		assertThat(charSet, contains(Character.MIN_VALUE, Character.MAX_VALUE));

		assertThat(charSet.contains(Character.MIN_VALUE), is(true));
		assertThat(charSet.contains(Character.MAX_VALUE), is(true));

		assertThat(charSet.remove(Character.MIN_VALUE), is(true));
		assertThat(charSet.remove(Character.MAX_VALUE), is(true));

		assertThat(charSet, is(emptyIterable()));
	}

	@Test
	public void fuzz() {
		Character[] randomValues = new Character[1000];
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
			assertThat(empty.add(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		for (char randomValue : randomValues)
			assertThat(empty.add(randomValue), is(false));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAll(asList(randomValues)), is(true));

		for (char randomValue : randomValues)
			assertThat(empty.contains(randomValue), is(true));

		// toString
		Arrays.sort(randomValues);
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (char randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty.size(), is(0));

		for (char randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(false));
	}
}
