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
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BitCharSetBoxingTest extends BaseBoxingTest {
	private final SortedSet<Character> empty = new BitCharSet();
	private final SortedSet<Character> set = new BitCharSet('a', 'b', 'c', 'd', 'e');

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
	public void equalsHashCodeAgainstTreeSet() {
		Set<Character> larger = new TreeSet<>(asList('a', 'b', 'c', 'd', 'e', 'q'));
		assertThat(set, is(not(equalTo(larger))));
		assertThat(set.hashCode(), is(not(larger.hashCode())));

		Set<Character> smaller = new TreeSet<>(asList('a', 'b', 'c', 'd'));
		assertThat(set, is(not(equalTo(smaller))));
		assertThat(set.hashCode(), is(not(smaller.hashCode())));

		Set<Character> dissimilar = new TreeSet<>(asList('a', 'b', 'c', 'd', 'f'));
		assertThat(set, is(not(equalTo(dissimilar))));
		assertThat(set.hashCode(), is(not(dissimilar.hashCode())));

		Set<Character> same = new TreeSet<>(asList('a', 'b', 'c', 'd', 'e'));
		assertThat(set, is(equalTo(same)));
		assertThat(set.hashCode(), is(same.hashCode()));
	}

	@Test
	public void subSet() {
		SortedSet<Character> subSet = set.subSet('b', 'e');
		assertThat(subSet, contains('b', 'c', 'd'));
		assertThat(subSet.size(), is(3));
		assertThat(subSet.first(), is('b'));
		assertThat(subSet.last(), is('d'));
		assertThat(subSet.contains('b'), is(true));
		assertThat(subSet.contains('e'), is(false));
		assertThat(subSet.toString(), is("[b, c, d]"));

		Set<Character> equivalentSet = new HashSet<>(asList('b', 'c', 'd'));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(subSet.remove('b'), is(true));
		assertThat(subSet, contains('c', 'd'));
		assertThat(subSet.size(), is(2));
		assertThat(set, contains('a', 'c', 'd', 'e'));

		assertThat(subSet.remove('b'), is(false));
		assertThat(subSet, contains('c', 'd'));
		assertThat(subSet.size(), is(2));
		assertThat(set, contains('a', 'c', 'd', 'e'));

		assertThat(subSet.add('b'), is(true));
		assertThat(subSet, contains('b', 'c', 'd'));
		assertThat(subSet.size(), is(3));
		assertThat(set, contains('a', 'b', 'c', 'd', 'e'));

		assertThat(subSet.add('b'), is(false));
		assertThat(subSet, contains('b', 'c', 'd'));
		assertThat(subSet.size(), is(3));
		assertThat(set, contains('a', 'b', 'c', 'd', 'e'));

		expecting(IllegalArgumentException.class, () -> subSet.add('f'));
		assertThat(subSet, contains('b', 'c', 'd'));
		assertThat(subSet.size(), is(3));
		assertThat(set, contains('a', 'b', 'c', 'd', 'e'));

		subSet.clear();
		assertThat(subSet, is(emptyIterable()));
		assertThat(subSet.size(), is(0));
		assertThat(set, contains('a', 'e'));
	}

	@Test
	public void sparseSubSet() {
		BitCharSet set = new BitCharSet('b', 'd', 'f', 'h');
		SortedSet<Character> subSet = set.subSet('c', 'g');
		assertThat(subSet, contains('d', 'f'));
		assertThat(subSet.size(), is(2));
		assertThat(subSet.first(), is('d'));
		assertThat(subSet.last(), is('f'));
		assertThat(subSet.contains('d'), is(true));
		assertThat(subSet.contains('h'), is(false));
		assertThat(subSet.toString(), is("[d, f]"));

		Set<Character> equivalentSet = new HashSet<>(asList('d', 'f'));
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void headSet() {
		SortedSet<Character> headSet = set.headSet('d');
		assertThat(headSet, contains('a', 'b', 'c'));
		assertThat(headSet.size(), is(3));
		assertThat(headSet.first(), is('a'));
		assertThat(headSet.last(), is('c'));
		assertThat(headSet.contains('b'), is(true));
		assertThat(headSet.contains('d'), is(false));
		assertThat(headSet.toString(), is("[a, b, c]"));

		Set<Character> equivalentSet = new HashSet<>(asList('a', 'b', 'c'));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(headSet.remove('b'), is(true));
		assertThat(headSet, contains('a', 'c'));
		assertThat(headSet.size(), is(2));
		assertThat(set, contains('a', 'c', 'd', 'e'));

		assertThat(headSet.remove('b'), is(false));
		assertThat(headSet, contains('a', 'c'));
		assertThat(headSet.size(), is(2));
		assertThat(set, contains('a', 'c', 'd', 'e'));

		assertThat(headSet.add('b'), is(true));
		assertThat(headSet, contains('a', 'b', 'c'));
		assertThat(headSet.size(), is(3));
		assertThat(set, contains('a', 'b', 'c', 'd', 'e'));

		assertThat(headSet.add('b'), is(false));
		assertThat(headSet, contains('a', 'b', 'c'));
		assertThat(headSet.size(), is(3));
		assertThat(set, contains('a', 'b', 'c', 'd', 'e'));

		expecting(IllegalArgumentException.class, () -> headSet.add('q'));
		assertThat(headSet, contains('a', 'b', 'c'));
		assertThat(headSet.size(), is(3));
		assertThat(set, contains('a', 'b', 'c', 'd', 'e'));

		headSet.clear();
		assertThat(headSet, is(emptyIterable()));
		assertThat(headSet.size(), is(0));
		assertThat(set, contains('d', 'e'));
	}

	@Test
	public void sparseHeadSet() {
		BitCharSet set = new BitCharSet('b', 'd', 'f', 'h');
		SortedSet<Character> headSet = set.headSet('e');
		assertThat(headSet, contains('b', 'd'));
		assertThat(headSet.size(), is(2));
		assertThat(headSet.first(), is('b'));
		assertThat(headSet.last(), is('d'));
		assertThat(headSet.contains('b'), is(true));
		assertThat(headSet.contains('f'), is(false));
		assertThat(headSet.toString(), is("[b, d]"));

		Set<Character> equivalentSet = new HashSet<>(asList('b', 'd'));
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void tailSet() {
		SortedSet<Character> tailSet = set.tailSet('c');
		assertThat(tailSet, contains('c', 'd', 'e'));
		assertThat(tailSet.size(), is(3));
		assertThat(tailSet.first(), is('c'));
		assertThat(tailSet.last(), is('e'));
		assertThat(tailSet.contains('d'), is(true));
		assertThat(tailSet.contains('a'), is(false));
		assertThat(tailSet.toString(), is("[c, d, e]"));

		Set<Character> equivalentSet = new HashSet<>(asList('c', 'd', 'e'));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(tailSet.remove('d'), is(true));
		assertThat(tailSet, contains('c', 'e'));
		assertThat(tailSet.size(), is(2));
		assertThat(set, contains('a', 'b', 'c', 'e'));

		assertThat(tailSet.remove('d'), is(false));
		assertThat(tailSet, contains('c', 'e'));
		assertThat(tailSet.size(), is(2));
		assertThat(set, contains('a', 'b', 'c', 'e'));

		assertThat(tailSet.add('q'), is(true));
		assertThat(tailSet, contains('c', 'e', 'q'));
		assertThat(tailSet.size(), is(3));
		assertThat(set, contains('a', 'b', 'c', 'e', 'q'));

		assertThat(tailSet.add('q'), is(false));
		assertThat(tailSet, contains('c', 'e', 'q'));
		assertThat(tailSet.size(), is(3));
		assertThat(set, contains('a', 'b', 'c', 'e', 'q'));

		expecting(IllegalArgumentException.class, () -> tailSet.add('a'));
		assertThat(tailSet, contains('c', 'e', 'q'));
		assertThat(tailSet.size(), is(3));
		assertThat(set, contains('a', 'b', 'c', 'e', 'q'));

		assertThat(set.add('d'), is(true));
		assertThat(tailSet, contains('c', 'd', 'e', 'q'));
		assertThat(tailSet.size(), is(4));
		assertThat(set, contains('a', 'b', 'c', 'd', 'e', 'q'));

		tailSet.clear();
		assertThat(tailSet, is(emptyIterable()));
		assertThat(tailSet.size(), is(0));
		assertThat(set, contains('a', 'b'));
	}

	@Test
	public void sparseTailSet() {
		BitCharSet set = new BitCharSet('b', 'd', 'f', 'h');
		SortedSet<Character> tailSet = set.tailSet('e');
		assertThat(tailSet, contains('f', 'h'));
		assertThat(tailSet.size(), is(2));
		assertThat(tailSet.first(), is('f'));
		assertThat(tailSet.last(), is('h'));
		assertThat(tailSet.contains('f'), is(true));
		assertThat(tailSet.contains('d'), is(false));
		assertThat(tailSet.toString(), is("[f, h]"));

		Set<Character> equivalentSet = new HashSet<>(asList('f', 'h'));
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void addAllCharCollection() {
		assertThat(empty.addAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(empty, contains('a', 'b', 'c'));

		assertThat(set.addAll(CharList.create('c', 'd', 'e', 'f', 'g')), is(true));
		assertThat(set, contains('a', 'b', 'c', 'd', 'e', 'f', 'g'));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(set.stream().collect(Collectors.toList()), contains('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(set.parallelStream().collect(Collectors.toList()), contains('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void first() {
		expecting(NoSuchElementException.class, empty::first);
		assertThat(set.first(), is('a'));
	}

	@Test
	public void last() {
		expecting(NoSuchElementException.class, empty::last);
		assertThat(set.last(), is('e'));
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
	public void containsAllCharCollection() {
		assertThat(empty.containsAll(CharList.create('a', 'b', 'c')), is(false));
		assertThat(set.containsAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(set.containsAll(CharList.create('a', 'b', 'c', 'q')), is(false));
	}

	@Test
	public void containsAllCollection() {
		assertThat(empty.containsAll(asList('a', 'b', 'c')), is(false));
		assertThat(set.containsAll(asList('a', 'b', 'c')), is(true));
		assertThat(set.containsAll(asList('a', 'b', 'c', 'q')), is(false));
	}

	@Test
	public void addAll() {
		assertThat(empty.addAll(asList('a', 'b', 'c')), is(true));
		assertThat(empty, contains('a', 'b', 'c'));

		assertThat(set.addAll(asList('c', 'd', 'e', 'f', 'g')), is(true));
		assertThat(set, contains('a', 'b', 'c', 'd', 'e', 'f', 'g'));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(asList('a', 'b', 'c')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(asList('a', 'b', 'c')), is(true));
		assertThat(set, contains('d', 'e'));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(asList('a', 'b', 'c')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(asList('a', 'b', 'c')), is(true));
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
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger('a');
		set.forEach(x -> assertThat(x, is((char) value.getAndIncrement())));
		assertThat(value.get(), is((int) 'f'));
	}

	@Test
	public void boundaries() {
		assertThat(empty.add(Character.MIN_VALUE), is(true));
		assertThat(empty.add(Character.MAX_VALUE), is(true));

		assertThat(empty, contains(Character.MIN_VALUE, Character.MAX_VALUE));

		assertThat(empty.contains(Character.MIN_VALUE), is(true));
		assertThat(empty.contains(Character.MAX_VALUE), is(true));

		assertThat(empty.remove(Character.MIN_VALUE), is(true));
		assertThat(empty.remove(Character.MAX_VALUE), is(true));

		assertThat(empty, is(emptyIterable()));
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
