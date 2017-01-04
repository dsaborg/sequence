package org.d2ab.collection.chars;

import org.d2ab.collection.Arrayz;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class CharCollectionBoxingTest extends BaseBoxingTest {
	Collection<Character> empty = CharCollection.Base.create();
	Collection<Character> collection = CharCollection.Base.create('a', 'b', 'c', 'd', 'e');

	@Test
	public void isEmpty() {
		assertThat(empty.isEmpty(), is(true));
		assertThat(collection.isEmpty(), is(false));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty.isEmpty(), is(true));

		collection.clear();
		assertThat(collection.isEmpty(), is(true));
	}

	@Test
	public void add() {
		assertThat(empty.add('q'), is(true));
		assertThat(empty, contains('q'));

		assertThat(collection.add('q'), is(true));
		assertThat(collection, contains('a', 'b', 'c', 'd', 'e', 'q'));
	}

	@Test
	public void addCharDefault() {
		CharCollection def = new CharCollection.Base() {
			@Override
			public CharIterator iterator() {
				return null;
			}

			@Override
			public int size() {
				return 0;
			}
		};

		expecting(UnsupportedOperationException.class, () -> def.add('q'));
	}

	@Test
	public void boxedContains() {
		assertThat(empty.contains('q'), is(false));

		assertThat(collection.contains('q'), is(false));

		for (char x = 'a'; x <= 'e'; x++)
			assertThat(collection.contains(x), is(true));
	}

	@Test
	public void remove() {
		assertThat(empty.remove('q'), is(false));

		assertThat(collection.remove('q'), is(false));

		for (char x = 'a'; x <= 'e'; x++)
			assertThat(collection.remove(x), is(true));
		assertThat(collection, is(emptyIterable()));
	}

	@Test
	public void toArray() {
		assertArrayEquals(new Character[0], empty.toArray());
		assertArrayEquals(new Character[]{'a', 'b', 'c', 'd', 'e'}, collection.toArray());
	}

	@Test
	public void toArrayWithType() {
		assertArrayEquals(new Character[0], empty.toArray(new Character[0]));
		assertArrayEquals(new Character[]{'a', 'b', 'c', 'd', 'e'}, collection.toArray(new Character[0]));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(collection.toString(), is("[a, b, c, d, e]"));
	}

	@Test
	public void addAllCharCollection() {
		assertThat(empty.addAll(CharList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(empty, contains('a', 'b', 'c'));

		assertThat(collection.addAll(CharList.create('f', 'g', 'h')), is(true));
		assertThat(collection, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllCollection() {
		assertThat(empty.addAll(emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(asList('a', 'b', 'c')), is(true));
		assertThat(empty, contains('a', 'b', 'c'));

		assertThat(collection.addAll(asList('f', 'g', 'h')), is(true));
		assertThat(collection, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(collection.stream().collect(Collectors.toList()), contains('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(collection.parallelStream().collect(Collectors.toList()), contains('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void removeAllCharCollection() {
		assertThat(empty.removeAll(CharList.create('a', 'b', 'c')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(collection, contains('d', 'e'));
	}

	@Test
	public void removeAllCollection() {
		assertThat(empty.removeAll(asList('a', 'b', 'c')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAll(asList('a', 'b', 'c')), is(true));
		assertThat(collection, contains('d', 'e'));
	}

	@Test
	public void retainAllCharCollection() {
		assertThat(empty.retainAll(CharList.create('a', 'b', 'c')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(collection, contains('a', 'b', 'c'));
	}

	@Test
	public void retainAllCollection() {
		assertThat(empty.retainAll(asList('a', 'b', 'c')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAll(asList('a', 'b', 'c')), is(true));
		assertThat(collection, contains('a', 'b', 'c'));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 'c'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeIf(x -> x > 'c'), is(true));
		assertThat(collection, contains('a', 'b', 'c'));
	}

	@Test
	public void containsAllCharCollection() {
		assertThat(empty.containsAll(CharList.create()), is(true));
		assertThat(empty.containsAll(CharList.create('a', 'b', 'c')), is(false));

		assertThat(collection.containsAll(CharList.create()), is(true));
		assertThat(collection.containsAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(collection.containsAll(CharList.create('a', 'b', 'c', 'q')), is(false));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger('a');
		collection.forEach(x -> assertThat(x, is((char) value.getAndIncrement())));
		assertThat((char) value.get(), is('f'));
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

		// Containment checks
		assertThat(empty.containsAll(asList(randomValues)), is(true));

		for (char randomValue : randomValues)
			assertThat(empty.contains(randomValue), is(true));

		// toString
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (char randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty, is(emptyIterable()));

		for (char randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(false));
	}
}
