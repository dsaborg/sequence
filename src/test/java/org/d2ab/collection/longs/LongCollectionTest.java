package org.d2ab.collection.longs;

import org.d2ab.collection.Arrayz;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.test.StrictLongIterator;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class LongCollectionTest {
	LongList backingEmpty = LongList.create();
	LongCollection empty = new LongCollection.Base() {
		@Override
		public LongIterator iterator() {
			return StrictLongIterator.from(backingEmpty.iterator());
		}

		@Override
		public int size() {
			return backingEmpty.size();
		}

		@Override
		public boolean addLong(long x) {
			return backingEmpty.addLong(x);
		}
	};

	LongList backing = LongList.create(1, 2, 3, 4, 5);
	LongCollection collection = new LongCollection.Base() {
		@Override
		public LongIterator iterator() {
			return StrictLongIterator.from(backing.iterator());
		}

		@Override
		public int size() {
			return backing.size();
		}

		@Override
		public boolean addLong(long x) {
			return backing.addLong(x);
		}
	};

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
	public void addLong() {
		assertThat(empty.addLong(17), is(true));
		assertThat(empty, containsLongs(17));

		assertThat(collection.addLong(17), is(true));
		assertThat(collection, containsLongs(1, 2, 3, 4, 5, 17));
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

		expecting(UnsupportedOperationException.class, () -> def.addLong(17));
	}

	@Test
	public void containsLong() {
		assertThat(empty.containsLong(17), is(false));

		assertThat(collection.containsLong(17), is(false));

		for (long x = 1; x <= 5; x++)
			assertThat(collection.containsLong(x), is(true));
	}

	@Test
	public void removeLong() {
		assertThat(empty.removeLong(17), is(false));

		assertThat(collection.removeLong(17), is(false));

		for (long x = 1; x <= 5; x++)
			assertThat(collection.removeLong(x), is(true));
		assertThat(collection, is(emptyIterable()));
	}

	@Test
	public void toLongArray() {
		assertArrayEquals(new long[0], empty.toLongArray());
		assertArrayEquals(new long[]{1L, 2L, 3L, 4L, 5L}, collection.toLongArray());
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(collection.toString(), is("[1, 2, 3, 4, 5]"));
	}

	@Test
	public void addAllLongArray() {
		assertThat(empty.addAllLongs(), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllLongs(1, 2, 3), is(true));
		assertThat(empty, containsLongs(1, 2, 3));

		assertThat(collection.addAllLongs(6, 7, 8), is(true));
		assertThat(collection, containsLongs(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void addAllLongCollection() {
		assertThat(empty.addAllLongs(LongList.create()), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(empty.addAllLongs(LongList.create(1, 2, 3)), is(true));
		assertThat(empty, containsLongs(1, 2, 3));

		assertThat(collection.addAllLongs(LongList.create(6, 7, 8)), is(true));
		assertThat(collection, containsLongs(1, 2, 3, 4, 5, 6, 7, 8));
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
	public void longStream() {
		assertThat(empty.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
				is(emptyIterable()));

		assertThat(collection.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
				containsLongs(1, 2, 3, 4, 5));
	}

	@Test
	public void parallelLongStream() {
		assertThat(empty.parallelLongStream()
						.collect(LongList::create, LongList::addLong, LongList::addAllLongs),
				is(emptyIterable()));

		assertThat(collection.parallelLongStream()
						.collect(LongList::create, LongList::addLong, LongList::addAllLongs),
				containsLongs(1, 2, 3, 4, 5));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(collection.sequence(), containsLongs(1, 2, 3, 4, 5));
	}

	@Test
	public void removeAllLongArray() {
		assertThat(empty.removeAllLongs(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAllLongs(1, 2, 3), is(true));
		assertThat(collection, containsLongs(4, 5));
	}

	@Test
	public void removeAllLongCollection() {
		assertThat(empty.removeAll(LongList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAll(LongList.create(1, 2, 3)), is(true));
		assertThat(collection, containsLongs(4, 5));
	}

	@Test
	public void retainAllLongArray() {
		assertThat(empty.retainAllLongs(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAllLongs(1, 2, 3), is(true));
		assertThat(collection, containsLongs(1, 2, 3));
	}

	@Test
	public void retainAllLongCollection() {
		assertThat(empty.retainAll(LongList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAll(LongList.create(1, 2, 3)), is(true));
		assertThat(collection, containsLongs(1, 2, 3));
	}

	@Test
	public void removeLongsIf() {
		assertThat(empty.removeLongsIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeLongsIf(x -> x > 3), is(true));
		assertThat(collection, containsLongs(1, 2, 3));
	}

	@Test
	public void containsAllLongArray() {
		assertThat(empty.containsAllLongs(), is(true));
		assertThat(empty.containsAllLongs(1, 2, 3), is(false));

		assertThat(collection.containsAllLongs(), is(true));
		assertThat(collection.containsAllLongs(1, 2, 3), is(true));
		assertThat(collection.containsAllLongs(1, 2, 3, 17), is(false));
	}

	@Test
	public void containsAllLongCollection() {
		assertThat(empty.containsAll(LongList.create()), is(true));
		assertThat(empty.containsAll(LongList.create(1, 2, 3)), is(false));

		assertThat(collection.containsAll(LongList.create()), is(true));
		assertThat(collection.containsAll(LongList.create(1, 2, 3)), is(true));
		assertThat(collection.containsAll(LongList.create(1, 2, 3, 17)), is(false));
	}

	@Test
	public void forEachLong() {
		empty.forEachLong(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicLong value = new AtomicLong(1);
		collection.forEachLong(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(6L));
	}

	@Test
	public void addBoxed() {
		assertThat(empty.add(17L), is(true));
		assertThat(empty, containsLongs(17));

		assertThat(collection.add(17L), is(true));
		assertThat(collection, containsLongs(1, 2, 3, 4, 5, 17));
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
		assertThat(empty.remove(17), is(false));

		assertThat(collection.remove(17), is(false));
		assertThat(collection.remove(new Object()), is(false));

		for (long x = 1; x <= 5; x++)
			assertThat(collection.remove(x), is(true));
		assertThat(collection, is(emptyIterable()));
	}

	@Test
	public void addAllBoxed() {
		assertThat(empty.addAll(asList(1L, 2L, 3L)), is(true));
		assertThat(empty, containsLongs(1, 2, 3));

		assertThat(collection.addAll(asList(6L, 7L, 8L)), is(true));
		assertThat(collection, containsLongs(1, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void removeAllBoxed() {
		assertThat(empty.removeAll(asList(1L, 2L, 3L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeAll(asList(1L, 2L, 3L)), is(true));
		assertThat(collection, containsLongs(4, 5));
	}

	@Test
	public void retainAllBoxed() {
		assertThat(empty.retainAll(asList(1L, 2L, 3L)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.retainAll(asList(1L, 2L, 3L)), is(true));
		assertThat(collection, containsLongs(1, 2, 3));
	}

	@Test
	public void removeIfBoxed() {
		assertThat(empty.removeIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(collection.removeIf(x -> x > 3), is(true));
		assertThat(collection, containsLongs(1, 2, 3));
	}

	@Test
	public void containsLongCollection() {
		assertThat(empty.containsAll(asList(1L, 2L, 3L)), is(false));
		assertThat(collection.containsAll(asList(1L, 2L, 3L)), is(true));
		assertThat(collection.containsAll(asList(1L, 2L, 3L, 17L)), is(false));
	}

	@Test
	public void forEachBoxed() {
		empty.forEach(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicLong value = new AtomicLong(1);
		collection.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(6L));
	}

	@Test
	public void fuzz() {
		long[] randomValues = new long[1000];
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
			assertThat(empty.addLong(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAllLongs(randomValues), is(true));

		for (long randomValue : randomValues)
			assertThat(empty.containsLong(randomValue), is(true));

		// toString
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (long randomValue : randomValues)
			assertThat(empty.removeLong(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty, is(emptyIterable()));

		for (long randomValue : randomValues)
			assertThat(empty.removeLong(randomValue), is(false));
	}

	public static class BoxingTest {
		LongList backingEmpty = LongList.create();
		LongCollection empty = new LongCollection.Base() {
			@Override
			public LongIterator iterator() {
				return backingEmpty.iterator();
			}

			@Override
			public int size() {
				return backingEmpty.size();
			}

			@Override
			public boolean addLong(long x) {
				return backingEmpty.addLong(x);
			}
		};

		LongList backing = LongList.create(1, 2, 3, 4, 5);
		LongCollection collection = new LongCollection.Base() {
			@Override
			public LongIterator iterator() {
				return backing.iterator();
			}

			@Override
			public int size() {
				return backing.size();
			}

			@Override
			public boolean addLong(long x) {
				return backing.addLong(x);
			}
		};

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
	}
}
