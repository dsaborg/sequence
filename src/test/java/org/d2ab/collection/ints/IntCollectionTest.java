package org.d2ab.collection.ints;

import org.d2ab.collection.Arrayz;
import org.d2ab.collection.chars.CharCollection;
import org.d2ab.iterator.ints.IntIterator;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class IntCollectionTest {
	IntCollection empty = IntCollection.Base.create();
	IntCollection collection = IntCollection.Base.create(1, 2, 3, 4, 5);

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
	public void addInt() {
		assertThat(empty.addInt(17), is(true));
		assertThat(empty, containsInts(17));

		assertThat(collection.addInt(17), is(true));
		assertThat(collection, containsInts(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void addIntDefault() {
		IntCollection def = new IntCollection.Base() {
			@Override
			public IntIterator iterator() {
				return null;
			}

			@Override
			public int size() {
				return 0;
			}
		};

		expecting(UnsupportedOperationException.class, () -> def.addInt(17));
	}

	@Test
	public void containsInt() {
		assertThat(empty.containsInt(17), is(false));

		assertThat(collection.containsInt(17), is(false));

		for (int x = 1; x <= 5; x++)
			assertThat(collection.containsInt(x), is(true));
	}

	@Test
	public void removeInt() {
		assertThat(empty.removeInt(17), is(false));

		assertThat(collection.removeInt(17), is(false));

		for (int x = 1; x <= 5; x++)
			assertThat(collection.removeInt(x), is(true));
		assertThat(collection, is(emptyIterable()));
	}

	@Test
	public void toIntArray() {
		assertArrayEquals(new int[0], empty.toIntArray());
		assertArrayEquals(new int[]{1, 2, 3, 4, 5}, collection.toIntArray());
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(collection.toString(), is("[1, 2, 3, 4, 5]"));
	}

	@Test
	public void addAllIntArray() {
		assertThat(empty.addAllInts(), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllInts(1, 2, 3), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(collection.addAllInts(6, 7, 8), is(true));
		assertThat(collection, containsInts(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllIntCollection() {
		assertThat(empty.addAllInts(IntList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllInts(IntList.create(1, 2, 3)), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(collection.addAllInts(IntList.create(6, 7, 8)), is(true));
		assertThat(collection, containsInts(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(collection.stream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(collection.parallelStream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5));
	}

	@Test
	public void intStream() {
		assertThat(empty.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));

		assertThat(collection.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts(1, 2, 3, 4, 5));
	}

	@Test
	public void parallelIntStream() {
		assertThat(empty.parallelIntStream()
		                .collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));

		assertThat(collection.parallelIntStream()
		                     .collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts(1, 2, 3, 4, 5));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(collection.sequence(), containsInts(1, 2, 3, 4, 5));
	}

	@Test
	public void removeAllIntArray() {
		assertThat(empty.removeAllInts(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAllInts(1, 2, 3), is(true));
		assertThat(collection, containsInts(4, 5));
	}

	@Test
	public void removeAllIntsIntCollection() {
		assertThat(empty.removeAllInts(IntList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAllInts(IntList.create(1, 2, 3)), is(true));
		assertThat(collection, containsInts(4, 5));
	}

	@Test
	public void retainAllIntArray() {
		assertThat(empty.retainAllInts(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAllInts(1, 2, 3), is(true));
		assertThat(collection, containsInts(1, 2, 3));
	}

	@Test
	public void retainAllIntsIntCollection() {
		assertThat(empty.retainAllInts(IntList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAllInts(IntList.create(1, 2, 3)), is(true));
		assertThat(collection, containsInts(1, 2, 3));
	}

	@Test
	public void removeIntsIf() {
		assertThat(empty.removeIntsIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeIntsIf(x -> x > 3), is(true));
		assertThat(collection, containsInts(1, 2, 3));
	}

	@Test
	public void containsAllIntArray() {
		assertThat(empty.containsAllInts(), is(true));
		assertThat(empty.containsAllInts(1, 2, 3), is(false));

		assertThat(collection.containsAllInts(), is(true));
		assertThat(collection.containsAllInts(1, 2, 3), is(true));
		assertThat(collection.containsAllInts(1, 2, 3, 17), is(false));
	}

	@Test
	public void containsAllIntsIntCollection() {
		assertThat(empty.containsAllInts(IntList.create()), is(true));
		assertThat(empty.containsAllInts(IntList.create(1, 2, 3)), is(false));

		assertThat(collection.containsAllInts(IntList.create()), is(true));
		assertThat(collection.containsAllInts(IntList.create(1, 2, 3)), is(true));
		assertThat(collection.containsAllInts(IntList.create(1, 2, 3, 17)), is(false));
	}

	@Test
	public void forEachInt() {
		empty.forEachInt(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		collection.forEachInt(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(6));
	}

	@Test
	public void asChars() {
		CharCollection emptyAsChars = empty.asChars();
		twice(() -> assertThat(emptyAsChars, is(emptyIterable())));
		assertThat(emptyAsChars.size(), is(0));

		IntCollection collection = IntCollection.Base.create('a', 'b', 'c', 'd', 'e');

		CharCollection collectionAsChars = collection.asChars();
		twice(() -> assertThat(collectionAsChars, containsChars('a', 'b', 'c', 'd', 'e')));
		assertThat(collectionAsChars.size(), is(5));
	}

	@Test
	public void fuzz() {
		int[] randomValues = new int[1000];
		Random random = new Random();
		for (int i = 0; i < randomValues.length; i++) {
			int randomValue;
			do
				randomValue = random.nextInt();
			while (Arrayz.contains(randomValues, randomValue));
			randomValues[i] = randomValue;
		}

		// Adding
		for (int randomValue : randomValues)
			assertThat(empty.addInt(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAllInts(randomValues), is(true));

		for (int randomValue : randomValues)
			assertThat(empty.containsInt(randomValue), is(true));

		// toString
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (int randomValue : randomValues)
			assertThat(empty.removeInt(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty, is(emptyIterable()));

		for (int randomValue : randomValues)
			assertThat(empty.removeInt(randomValue), is(false));
	}
}
