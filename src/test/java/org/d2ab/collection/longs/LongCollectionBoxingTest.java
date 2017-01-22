package org.d2ab.collection.longs;

import org.d2ab.collection.Arrayz;
import org.d2ab.collection.Lists;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.test.BaseBoxingTest;
import org.junit.Test;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class LongCollectionBoxingTest extends BaseBoxingTest {
	private final Collection<Long> empty = LongCollection.Base.create();
	private final Collection<Long> collection = LongCollection.Base.create(1, 2, 3, 4, 5);

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
		assertThat(empty.add(17L), is(true));
		assertThat(empty, contains(17L));

		assertThat(collection.add(17L), is(true));
		assertThat(collection, contains(1L, 2L, 3L, 4L, 5L, 17L));
	}

	@Test
	public void addLongDefault() {
		LongCollection def = new LongCollection.Base() {
			@Override
			public LongIterator iterator() {
				return null;
			}

			@Override
			public int size() {
				return 0;
			}
		};

		expecting(UnsupportedOperationException.class, () -> def.add(17L));
	}

	@Test
	public void boxedContains() {
		assertThat(empty.contains(17L), is(false));

		assertThat(collection.contains(17L), is(false));

		for (long x = 1; x <= 5; x++)
			assertThat(collection.contains(x), is(true));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17L), is(false));

		assertThat(collection.remove(17L), is(false));

		for (long x = 1; x <= 5; x++)
			assertThat(collection.remove(x), is(true));
		assertThat(collection, is(emptyIterable()));
	}

	@Test
	public void toArray() {
		assertArrayEquals(new Long[0], empty.toArray());
		assertArrayEquals(new Long[]{1L, 2L, 3L, 4L, 5L}, collection.toArray());
	}

	@Test
	public void toArrayWithType() {
		assertArrayEquals(new Long[0], empty.toArray(new Long[0]));
		assertArrayEquals(new Long[]{1L, 2L, 3L, 4L, 5L}, collection.toArray(new Long[0]));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(collection.toString(), is("[1, 2, 3, 4, 5]"));
	}

	@Test
	public void addAllLongCollection() {
		assertThat(empty.addAll(LongList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAll(LongList.create(1L, 2L, 3L)), is(true));
		assertThat(empty, contains(1L, 2L, 3L));

		assertThat(collection.addAll(LongList.create(6L, 7L, 8L)), is(true));
		assertThat(collection, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(collection.stream().collect(Collectors.toList()), contains(1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(collection.parallelStream().collect(Collectors.toList()), contains(1L, 2L, 3L, 4L, 5L));
	}

	@Test
	public void removeAllLongCollection() {
		assertThat(empty.removeAll(LongList.create(1L, 2L, 3L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAll(LongList.create(1L, 2L, 3L)), is(true));
		assertThat(collection, contains(4L, 5L));
	}

	@Test
	public void retainAllLongCollection() {
		assertThat(empty.retainAll(LongList.create(1L, 2L, 3L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAll(LongList.create(1L, 2L, 3L)), is(true));
		assertThat(collection, contains(1L, 2L, 3L));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeIf(x -> x > 3), is(true));
		assertThat(collection, contains(1L, 2L, 3L));
	}

	@Test
	public void containsAllLongCollection() {
		assertThat(empty.containsAll(LongList.create()), is(true));
		assertThat(empty.containsAll(LongList.create(1L, 2L, 3L)), is(false));

		assertThat(collection.containsAll(LongList.create()), is(true));
		assertThat(collection.containsAll(LongList.create(1L, 2L, 3L)), is(true));
		assertThat(collection.containsAll(LongList.create(1L, 2L, 3L, 17L)), is(false));
	}

	@Test
	public void forEach() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicLong value = new AtomicLong(1);
		collection.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(6L));
	}

	@Test
	public void addBoxed() {
		assertThat(empty.add(17L), is(true));
		assertThat(empty, contains(17L));

		assertThat(collection.add(17L), is(true));
		assertThat(collection, contains(1L, 2L, 3L, 4L, 5L, 17L));
	}

	@Test
	public void containsBoxed() {
		assertThat(empty.contains(17L), is(false));

		assertThat(collection.contains(17L), is(false));
		assertThat(collection.contains(new Object()), is(false));

		for (long x = 1; x <= 5; x++)
			assertThat(collection.contains(x), is(true));
	}

	@Test
	public void removeBoxed() {
		assertThat(empty.remove(17L), is(false));

		assertThat(collection.remove(17L), is(false));
		assertThat(collection.remove(new Object()), is(false));

		for (long x = 1; x <= 5; x++)
			assertThat(collection.remove(x), is(true));
		assertThat(collection, is(emptyIterable()));
	}

	@Test
	public void addAllBoxed() {
		assertThat(empty.addAll(Lists.of(1L, 2L, 3L)), is(true));
		assertThat(empty, contains(1L, 2L, 3L));

		assertThat(collection.addAll(Lists.of(6L, 7L, 8L)), is(true));
		assertThat(collection, contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
	}

	@Test
	public void removeAllBoxed() {
		assertThat(empty.removeAll(Lists.of(1L, 2L, 3L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAll(Lists.of(1L, 2L, 3L)), is(true));
		assertThat(collection, contains(4L, 5L));
	}

	@Test
	public void retainAllBoxed() {
		assertThat(empty.retainAll(Lists.of(1L, 2L, 3L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAll(Lists.of(1L, 2L, 3L)), is(true));
		assertThat(collection, contains(1L, 2L, 3L));
	}

	@Test
	public void removeIfBoxed() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeIf(x -> x > 3), is(true));
		assertThat(collection, contains(1L, 2L, 3L));
	}

	@Test
	public void containsAllCollection() {
		assertThat(empty.containsAll(Lists.of(1L, 2L, 3L)), is(false));
		assertThat(collection.containsAll(Lists.of(1L, 2L, 3L)), is(true));
		assertThat(collection.containsAll(Lists.of(1L, 2L, 3L, 17L)), is(false));
	}

	@Test
	public void fuzz() {
		Long[] randomValues = new Long[1000];
		Random random = new Random();
		for (int i = 0; i < randomValues.length; i++) {
			long randomValue;
			do
				randomValue = random.nextLong();
			while (Arrayz.contains(randomValues, randomValue));
			randomValues[i] = randomValue;
		}

		// Adding
		for (long randomValue : randomValues)
			assertThat(empty.add(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAll(Lists.of(randomValues)), is(true));

		for (long randomValue : randomValues)
			assertThat(empty.contains(randomValue), is(true));

		// toString
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (long randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty, is(emptyIterable()));

		for (long randomValue : randomValues)
			assertThat(empty.remove(randomValue), is(false));
	}
}
