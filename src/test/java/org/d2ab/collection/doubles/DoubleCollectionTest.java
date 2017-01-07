package org.d2ab.collection.doubles;

import org.d2ab.collection.Arrayz;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class DoubleCollectionTest {
	DoubleCollection empty = DoubleCollection.Base.create();
	DoubleCollection collection = DoubleCollection.Base.create(1, 2, 3, 4, 5);

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
	public void addDoubleExactly() {
		assertThat(empty.addDoubleExactly(17), is(true));
		assertThat(empty, containsDoubles(17));

		assertThat(collection.addDoubleExactly(17), is(true));
		assertThat(collection, containsDoubles(1, 2, 3, 4, 5, 17));
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

		expecting(UnsupportedOperationException.class, () -> def.addDoubleExactly(17));
		expecting(UnsupportedOperationException.class, () -> def.addDouble(17, 0));
	}

	@Test
	public void containsDoubleExactly() {
		assertThat(empty.containsDoubleExactly(17), is(false));

		assertThat(collection.containsDoubleExactly(17), is(false));

		for (double x = 1; x <= 5; x++)
			assertThat(collection.containsDoubleExactly(x), is(true));
	}

	@Test
	public void removeDoubleWithPrecision() {
		assertThat(empty.removeDouble(17, 0.5), is(false));

		assertThat(collection.removeDouble(17, 0.5), is(false));

		for (double x = 1; x <= 5; x++)
			assertThat(collection.removeDouble(x + 0.2, 0.5), is(true));
		assertThat(collection, is(emptyIterable()));
	}

	@Test
	public void removeDoubleExactly() {
		assertThat(empty.removeDoubleExactly(17), is(false));

		assertThat(collection.removeDoubleExactly(17), is(false));

		for (double x = 1; x <= 5; x++)
			assertThat(collection.removeDoubleExactly(x), is(true));
		assertThat(collection, is(emptyIterable()));
	}

	@Test
	public void toDoubleArray() {
		assertArrayEquals(new double[0], empty.toDoubleArray(), 0.0);
		assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0, 5.0}, collection.toDoubleArray(), 0.0);
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(collection.toString(), is("[1.0, 2.0, 3.0, 4.0, 5.0]"));
	}

	@Test
	public void addAllDoublesDoubleArray() {
		assertThat(empty.addAllDoubles(), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllDoubles(1, 2, 3), is(true));
		assertThat(empty, containsDoubles(1, 2, 3));

		assertThat(collection.addAllDoubles(6, 7, 8), is(true));
		assertThat(collection, containsDoubles(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllDoublesDoubleCollection() {
		assertThat(empty.addAllDoubles(DoubleList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllDoubles(DoubleList.create(1, 2, 3)), is(true));
		assertThat(empty, containsDoubles(1, 2, 3));

		assertThat(collection.addAllDoubles(DoubleList.create(6, 7, 8)), is(true));
		assertThat(collection, containsDoubles(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllDoubleCollection() {
		assertThat(empty.addAll(DoubleList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(DoubleList.create(1, 2, 3)), is(true));
		assertThat(empty, containsDoubles(1, 2, 3));

		assertThat(collection.addAll(DoubleList.create(6, 7, 8)), is(true));
		assertThat(collection, containsDoubles(1, 2, 3, 4, 5, 6, 7, 8));
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
	public void doubleStream() {
		assertThat(empty.doubleStream().collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           is(emptyIterable()));

		assertThat(collection.doubleStream().collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           containsDoubles(1, 2, 3, 4, 5));
	}

	@Test
	public void parallelDoubleStream() {
		assertThat(empty.parallelDoubleStream()
		                .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           is(emptyIterable()));

		assertThat(collection.parallelDoubleStream()
		                     .collect(DoubleList::create, DoubleList::addDoubleExactly, DoubleList::addAllDoubles),
		           containsDoubles(1, 2, 3, 4, 5));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(collection.sequence(), containsDoubles(1, 2, 3, 4, 5));
	}

	@Test
	public void removeAllDoublesArrayWithPrecision() {
		assertThat(empty.removeAllDoubles(new double[]{1.1, 2.1, 3.1}, 0.5), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAllDoubles(new double[]{1.1, 2.1, 3.1}, 0.5), is(true));
		assertThat(collection, containsDoubles(4, 5));
	}

	@Test
	public void removeAllDoublesExactlyArray() {
		assertThat(empty.removeAllDoublesExactly(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAllDoublesExactly(1, 2, 3), is(true));
		assertThat(collection, containsDoubles(4, 5));
	}

	@Test
	public void removeAllDoubleCollection() {
		assertThat(empty.removeAll(DoubleList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAll(DoubleList.create(1, 2, 3)), is(true));
		assertThat(collection, containsDoubles(4, 5));
	}

	@Test
	public void retainAllDoublesArrayWithPrecision() {
		assertThat(empty.retainAllDoubles(new double[]{1.1, 2.1, 3.1}, 0.5), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAllDoubles(new double[]{1.1, 2.1, 3.1}, 0.5), is(true));
		assertThat(collection, containsDoubles(1, 2, 3));
	}

	@Test
	public void retainAllDoublesExactlyArray() {
		assertThat(empty.retainAllDoublesExactly(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAllDoublesExactly(1, 2, 3), is(true));
		assertThat(collection, containsDoubles(1, 2, 3));
	}

	@Test
	public void retainAllDoubleCollection() {
		assertThat(empty.retainAll(DoubleList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAll(DoubleList.create(1, 2, 3)), is(true));
		assertThat(collection, containsDoubles(1, 2, 3));
	}

	@Test
	public void removeDoublesIf() {
		assertThat(empty.removeDoublesIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeDoublesIf(x -> x > 3), is(true));
		assertThat(collection, containsDoubles(1, 2, 3));
	}

	@Test
	public void containsAllDoubleArray() {
		assertThat(empty.containsAllDoublesExactly(), is(true));
		assertThat(empty.containsAllDoublesExactly(1, 2, 3), is(false));

		assertThat(collection.containsAllDoublesExactly(), is(true));
		assertThat(collection.containsAllDoublesExactly(1, 2, 3), is(true));
		assertThat(collection.containsAllDoublesExactly(1, 2, 3, 17), is(false));
	}

	@Test
	public void containsAllDoubleCollection() {
		assertThat(empty.containsAll(DoubleList.create()), is(true));
		assertThat(empty.containsAll(DoubleList.create(1, 2, 3)), is(false));

		assertThat(collection.containsAll(DoubleList.create()), is(true));
		assertThat(collection.containsAll(DoubleList.create(1, 2, 3)), is(true));
		assertThat(collection.containsAll(DoubleList.create(1, 2, 3, 17)), is(false));
	}

	@Test
	public void forEachDouble() {
		empty.forEachDouble(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		collection.forEachDouble(x -> assertThat(x, is((double) value.getAndIncrement())));
		assertThat(value.get(), is(6));
	}

	@Test
	public void fuzz() {
		double[] randomValues = new double[1000];
		Random random = new Random();
		for (int i = 0; i < randomValues.length; i++) {
			double randomValue;
			do
				randomValue = random.nextDouble();
			while (Arrayz.containsExactly(randomValues, randomValue));
			randomValues[i] = randomValue;
		}

		// Adding
		for (double randomValue : randomValues)
			assertThat(empty.addDoubleExactly(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAllDoublesExactly(randomValues), is(true));

		for (double randomValue : randomValues)
			assertThat(empty.containsDoubleExactly(randomValue), is(true));

		// toString
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (double randomValue : randomValues)
			assertThat(empty.removeDoubleExactly(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty, is(emptyIterable()));

		for (double randomValue : randomValues)
			assertThat(empty.removeDoubleExactly(randomValue), is(false));
	}
}
