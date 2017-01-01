package org.d2ab.collection.ints;

import org.d2ab.collection.Arrayz;
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class IntCollectionBoxingTest extends BaseBoxingTest {
	Collection<Integer> empty = IntCollection.Base.create();
	Collection<Integer> collection = IntCollection.Base.create(1, 2, 3, 4, 5);

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
		assertThat(empty.add(17), is(true));
		assertThat(empty, contains(17));

		assertThat(collection.add(17), is(true));
		assertThat(collection, contains(1, 2, 3, 4, 5, 17));
	}

	@Test
	public void boxedContains() {
		assertThat(empty.contains(17), is(false));

		assertThat(collection.contains(17), is(false));

		for (int x = 1; x <= 5; x++)
			assertThat(collection.contains(x), is(true));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17), is(false));

		assertThat(collection.remove(17), is(false));

		for (int x = 1; x <= 5; x++)
			assertThat(collection.remove(x), is(true));
		assertThat(collection, is(emptyIterable()));
	}

	@Test
	public void toArray() {
		assertArrayEquals(new Integer[0], empty.toArray());
		assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, collection.toArray());
	}

	@Test
	public void toArrayWithType() {
		assertArrayEquals(new Integer[0], empty.toArray(new Integer[0]));
		assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, collection.toArray(new Integer[0]));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(collection.toString(), is("[1, 2, 3, 4, 5]"));
	}

	@Test
	public void addAllIntCollection() {
		assertThat(empty.addAll(IntList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(IntList.create(1, 2, 3)), is(true));
		assertThat(empty, contains(1, 2, 3));

		assertThat(collection.addAll(IntList.create(6, 7, 8)), is(true));
		assertThat(collection, contains(1, 2, 3, 4, 5, 6, 7, 8));
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
	public void removeAllIntCollection() {
		assertThat(empty.removeAll(IntList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAll(IntList.create(1, 2, 3)), is(true));
		assertThat(collection, contains(4, 5));
	}

	@Test
	public void retainAllIntCollection() {
		assertThat(empty.retainAll(IntList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAll(IntList.create(1, 2, 3)), is(true));
		assertThat(collection, contains(1, 2, 3));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeIf(x -> x > 3), is(true));
		assertThat(collection, contains(1, 2, 3));
	}

	@Test
	public void containsAllIntCollection() {
		assertThat(empty.containsAll(IntList.create()), is(true));
		assertThat(empty.containsAll(IntList.create(1, 2, 3)), is(false));

		assertThat(collection.containsAll(IntList.create()), is(true));
		assertThat(collection.containsAll(IntList.create(1, 2, 3)), is(true));
		assertThat(collection.containsAll(IntList.create(1, 2, 3, 17)), is(false));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		collection.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(6));
	}

	@Test
	public void containsIntCollection() {
		assertThat(empty.containsAll(asList(1, 2, 3)), is(false));
		assertThat(collection.containsAll(asList(1, 2, 3)), is(true));
		assertThat(collection.containsAll(asList(1, 2, 3, 17)), is(false));
	}

	@Test
	public void fuzz() {
		Integer[] randomValues = new Integer[1000];
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
			assertThat(empty.add(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAll(asList(randomValues)), is(true));

		for (int randomValue : randomValues)
			assertThat(empty.contains(randomValue), is(true));

		// toString
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (int randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty, is(emptyIterable()));

		for (int randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(false));
	}
}
