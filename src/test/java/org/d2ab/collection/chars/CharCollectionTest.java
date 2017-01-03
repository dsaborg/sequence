package org.d2ab.collection.chars;

import org.d2ab.collection.Arrayz;
import org.d2ab.iterator.chars.CharIterator;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class CharCollectionTest {
	CharCollection empty = CharCollection.Base.create();
	CharCollection collection = CharCollection.Base.create('a', 'b', 'c', 'd', 'e');

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
	public void addChar() {
		assertThat(empty.addChar('q'), is(true));
		assertThat(empty, containsChars('q'));

		assertThat(collection.addChar('q'), is(true));
		assertThat(collection, containsChars('a', 'b', 'c', 'd', 'e', 'q'));
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

		expecting(UnsupportedOperationException.class, () -> def.addChar('q'));
	}

	@Test
	public void containsChar() {
		assertThat(empty.containsChar('q'), is(false));

		assertThat(collection.containsChar('q'), is(false));

		for (char x = 'a'; x <= 'e'; x++)
			assertThat(collection.containsChar(x), is(true));
	}

	@Test
	public void removeChar() {
		assertThat(empty.removeChar('q'), is(false));

		assertThat(collection.removeChar('q'), is(false));

		for (char x = 'a'; x <= 'e'; x++)
			assertThat(collection.removeChar(x), is(true));
		assertThat(collection, is(emptyIterable()));
	}

	@Test
	public void toCharArray() {
		assertArrayEquals(new char[0], empty.toCharArray());
		assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e'}, collection.toCharArray());
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(collection.toString(), is("[a, b, c, d, e]"));
	}

	@Test
	public void addAllCharsCharArray() {
		assertThat(empty.addAllChars(), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllChars('a', 'b', 'c'), is(true));
		assertThat(empty, containsChars('a', 'b', 'c'));

		assertThat(collection.addAllChars('f', 'g', 'h'), is(true));
		assertThat(collection, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllCharsCharCollection() {
		assertThat(empty.addAllChars(CharList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllChars(CharList.create('a', 'b', 'c')), is(true));
		assertThat(empty, containsChars('a', 'b', 'c'));

		assertThat(collection.addAllChars(CharList.create('f', 'g', 'h')), is(true));
		assertThat(collection, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void addAllCharCollection() {
		assertThat(empty.addAll(CharList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(empty, containsChars('a', 'b', 'c'));

		assertThat(collection.addAll(CharList.create('f', 'g', 'h')), is(true));
		assertThat(collection, containsChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'));
	}

	@Test
	public void intStream() {
		assertThat(empty.intStream()
		                .mapToObj(x -> (char) x)
		                .collect(CharList::create, CharList::addChar, CharList::addAllChars),
		           is(emptyIterable()));

		assertThat(collection.intStream()
		                     .mapToObj(x -> (char) x)
		                     .collect(CharList::create, CharList::addChar, CharList::addAllChars),
		           containsChars('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void parallelIntStream() {
		assertThat(empty.parallelIntStream().mapToObj(x -> (char) x)
		                .collect(CharList::create, CharList::addChar, CharList::addAllChars),
		           is(emptyIterable()));

		assertThat(collection.parallelIntStream().mapToObj(x -> (char) x)
		                     .collect(CharList::create, CharList::addChar, CharList::addAllChars),
		           containsChars('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(collection.sequence(), containsChars('a', 'b', 'c', 'd', 'e'));
	}

	@Test
	public void removeAllCharArray() {
		assertThat(empty.removeAllChars('a', 'b', 'c'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAllChars('a', 'b', 'c'), is(true));
		assertThat(collection, containsChars('d', 'e'));
	}

	@Test
	public void removeAllCharCollection() {
		assertThat(empty.removeAll(CharList.create('a', 'b', 'c')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(collection, containsChars('d', 'e'));
	}

	@Test
	public void retainAllCharArray() {
		assertThat(empty.retainAllChars('a', 'b', 'c'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAllChars('a', 'b', 'c'), is(true));
		assertThat(collection, containsChars('a', 'b', 'c'));
	}

	@Test
	public void retainAllCharCollection() {
		assertThat(empty.retainAll(CharList.create('a', 'b', 'c')), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAll(CharList.create('a', 'b', 'c')), is(true));
		assertThat(collection, containsChars('a', 'b', 'c'));
	}

	@Test
	public void removeCharsIf() {
		assertThat(empty.removeCharsIf(x -> x > 'c'), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeCharsIf(x -> x > 'c'), is(true));
		assertThat(collection, containsChars('a', 'b', 'c'));
	}

	@Test
	public void containsAllCharArray() {
		assertThat(empty.containsAllChars(), is(true));
		assertThat(empty.containsAllChars('a', 'b', 'c'), is(false));

		assertThat(collection.containsAllChars(), is(true));
		assertThat(collection.containsAllChars('a', 'b', 'c'), is(true));
		assertThat(collection.containsAllChars('a', 'b', 'c', 'q'), is(false));
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
	public void forEachChar() {
		empty.forEachChar(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger('a');
		collection.forEachChar(x -> assertThat(x, is((char) value.getAndIncrement())));
		assertThat((char) value.get(), is('f'));
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

		// Containment checks
		assertThat(empty.containsAllChars(randomValues), is(true));

		for (char randomValue : randomValues)
			assertThat(empty.containsChar(randomValue), is(true));

		// toString
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (char randomValue : randomValues)
			assertThat(empty.removeChar(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty, is(emptyIterable()));

		for (char randomValue : randomValues)
			assertThat(empty.removeChar(randomValue), is(false));
	}

}
