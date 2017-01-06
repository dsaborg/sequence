package org.d2ab.collection.doubles;

import org.d2ab.collection.Arrayz;
import org.d2ab.iterator.doubles.DoubleIterator;
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

public class DoubleCollectionBoxingTest extends BaseBoxingTest {
	Collection<Double> empty = DoubleCollection.Base.create();
	Collection<Double> collection = DoubleCollection.Base.create(1, 2, 3, 4, 5);

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
		assertThat(empty.add(17.0), is(true));
		assertThat(empty, contains(17.0));

		assertThat(collection.add(17.0), is(true));
		assertThat(collection, contains(1.0, 2.0, 3.0, 4.0, 5.0, 17.0));
	}

	@Test
	public void addDoubleDefault() {
		DoubleCollection def = new DoubleCollection.Base() {
			@Override
			public DoubleIterator iterator() {
				return null;
			}

			@Override
			public int size() {
				return 0;
			}
		};

		expecting(UnsupportedOperationException.class, () -> def.add(17.0));
	}

	@Test
	public void boxedContains() {
		assertThat(empty.contains(17.0), is(false));

		assertThat(collection.contains(17.0), is(false));

		for (double x = 1; x <= 5; x++)
			assertThat(collection.contains(x), is(true));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17.0), is(false));

		assertThat(collection.remove(17.0), is(false));

		for (double x = 1; x <= 5; x++)
			assertThat(collection.remove(x), is(true));
		assertThat(collection, is(emptyIterable()));
	}

	@Test
	public void toArray() {
		assertArrayEquals(new Double[0], empty.toArray());
		assertArrayEquals(new Double[]{1.0, 2.0, 3.0, 4.0, 5.0}, collection.toArray());
	}

	@Test
	public void toArrayWithType() {
		assertArrayEquals(new Double[0], empty.toArray(new Double[0]));
		assertArrayEquals(new Double[]{1.0, 2.0, 3.0, 4.0, 5.0}, collection.toArray(new Double[0]));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(collection.toString(), is("[1.0, 2.0, 3.0, 4.0, 5.0]"));
	}

	@Test
	public void addAllDoubleCollection() {
		assertThat(empty.addAll(DoubleList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(DoubleList.create(1.0, 2.0, 3.0)), is(true));
		assertThat(empty, contains(1.0, 2.0, 3.0));

		assertThat(collection.addAll(DoubleList.create(6.0, 7.0, 8.0)), is(true));
		assertThat(collection, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
	}

	@Test
	public void addAllCollection() {
		assertThat(empty.addAll(emptyList()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(asList(1.0, 2.0, 3.0)), is(true));
		assertThat(empty, contains(1.0, 2.0, 3.0));

		assertThat(collection.addAll(asList(6.0, 7.0, 8.0)), is(true));
		assertThat(collection, contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(collection.stream().collect(Collectors.toList()), contains(1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(collection.parallelStream().collect(Collectors.toList()), contains(1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void removeAllDoubleCollection() {
		assertThat(empty.removeAll(DoubleList.create(1.0, 2.0, 3.0)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAll(DoubleList.create(1.0, 2.0, 3.0)), is(true));
		assertThat(collection, contains(4.0, 5.0));
	}

	@Test
	public void removeAllCollection() {
		assertThat(empty.removeAll(asList(1.0, 2.0, 3.0)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAll(asList(1.0, 2.0, 3.0)), is(true));
		assertThat(collection, contains(4.0, 5.0));
	}

	@Test
	public void retainAllDoubleCollection() {
		assertThat(empty.retainAll(DoubleList.create(1.0, 2.0, 3.0)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAll(DoubleList.create(1.0, 2.0, 3.0)), is(true));
		assertThat(collection, contains(1.0, 2.0, 3.0));
	}

	@Test
	public void retainAllCollection() {
		assertThat(empty.retainAll(asList(1.0, 2.0, 3.0)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAll(asList(1.0, 2.0, 3.0)), is(true));
		assertThat(collection, contains(1.0, 2.0, 3.0));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeIf(x -> x > 3), is(true));
		assertThat(collection, contains(1.0, 2.0, 3.0));
	}

	@Test
	public void containsAllDoubleCollection() {
		assertThat(empty.containsAll(DoubleList.create()), is(true));
		assertThat(empty.containsAll(DoubleList.create(1.0, 2.0, 3.0)), is(false));

		assertThat(collection.containsAll(DoubleList.create()), is(true));
		assertThat(collection.containsAll(DoubleList.create(1.0, 2.0, 3.0)), is(true));
		assertThat(collection.containsAll(DoubleList.create(1.0, 2.0, 3.0, 17.0)), is(false));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		collection.forEach(x -> assertThat(x, is((double) value.getAndIncrement())));
		assertThat(value.get(), is(6));
	}

	@Test
	public void fuzz() {
		Double[] randomValues = new Double[1000];
		Random random = new Random();
		for (int i = 0; i < randomValues.length; i++) {
			double randomValue;
			do
				randomValue = random.nextDouble();
			while (Arrayz.contains(randomValues, randomValue));
			randomValues[i] = randomValue;
		}

		// Adding
		for (double randomValue : randomValues)
			assertThat(empty.add(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAll(asList(randomValues)), is(true));

		for (double randomValue : randomValues)
			assertThat(empty.contains(randomValue), is(true));

		// toString
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (double randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty, is(emptyIterable()));

		for (double randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(false));
	}
}
