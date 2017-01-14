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

package org.d2ab.collection.longs;

import org.d2ab.collection.Arrayz;
import org.d2ab.iterator.longs.LongIterator;
import org.junit.Test;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class LongSetTest {
	private final LongSet empty = LongSet.Base.create();
	private final LongSet set = LongSet.Base.create(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(set.size(), is(10));
	}

	@Test
	public void bigSize() {
		assertThat(new BitLongSet() {
			@Override
			protected long bitCount() {
				return Integer.MAX_VALUE;
			}
		}.size(), is(Integer.MAX_VALUE));

		expecting(IllegalStateException.class, () -> new BitLongSet() {
			@Override
			protected long bitCount() {
				return Integer.MAX_VALUE + 1L;
			}
		}.size());
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void iteratorFailFastPositives() {
		LongIterator it1 = set.iterator();
		assertThat(it1.hasNext(), is(true));
		assertThat(set.addLong(17), is(true));
		expecting(ConcurrentModificationException.class, it1::nextLong);

		LongIterator it2 = set.iterator();
		assertThat(it2.hasNext(), is(true));
		assertThat(set.removeLong(17), is(true));
		expecting(ConcurrentModificationException.class, it2::nextLong);
	}

	@Test
	public void iteratorFailFastNegatives() {
		LongIterator it1 = set.iterator();
		assertThat(it1.hasNext(), is(true));
		assertThat(set.addLong(-17), is(true));
		expecting(ConcurrentModificationException.class, it1::nextLong);

		LongIterator it2 = set.iterator();
		assertThat(it2.hasNext(), is(true));
		assertThat(set.removeLong(-17), is(true));
		expecting(ConcurrentModificationException.class, it2::nextLong);
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
	public void addLong() {
		empty.addLong(17);
		assertThat(empty, containsLongs(17));

		set.addLong(17);
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
	}

	@Test
	public void containsLong() {
		assertThat(empty.containsLong(17), is(false));

		assertThat(set.containsLong(17), is(false));
		for (long x = -5; x <= 4; x++)
			assertThat(set.containsLong(x), is(true));
	}

	@Test
	public void removeLong() {
		assertThat(empty.removeLong(17), is(false));

		assertThat(set.removeLong(17), is(false));
		for (long x = -5; x <= 4; x++)
			assertThat(set.removeLong(x), is(true));
		assertThat(set.isEmpty(), is(true));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(set.toString(), is("[-5, -4, -3, -2, -1, 0, 1, 2, 3, 4]"));
	}

	@Test
	public void equalsEdgeCases() {
		assertThat(set, is(equalTo(set)));
		assertThat(set, is(not(equalTo(null))));
		assertThat(set, is(not(equalTo(new Object()))));
	}

	@Test
	public void equalsHashCodeAgainstBitLongSet() {
		LongSet larger = new BitLongSet(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17);
		assertThat(set, is(not(equalTo(larger))));
		assertThat(set.hashCode(), is(not(larger.hashCode())));

		LongSet smaller = new BitLongSet(-5, -4, -3, -2, -1, 0, 1, 2, 3);
		assertThat(set, is(not(equalTo(smaller))));
		assertThat(set.hashCode(), is(not(smaller.hashCode())));

		LongSet dissimilar = new BitLongSet(-5, -4, -3, -2, -1, 0, 1, 2, 3, 5);
		assertThat(set, is(not(equalTo(dissimilar))));
		assertThat(set.hashCode(), is(not(dissimilar.hashCode())));

		LongSet same = new BitLongSet(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);
		assertThat(set, is(equalTo(same)));
		assertThat(set.hashCode(), is(same.hashCode()));
	}

	@Test
	public void addAllLongArray() {
		assertThat(empty.addAllLongs(1, 2, 3), is(true));
		assertThat(empty, containsLongs(1, 2, 3));

		assertThat(set.addAllLongs(3, 4, 5, 6, 7), is(true));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void addAllLongCollection() {
		assertThat(empty.addAllLongs(LongList.create(1, 2, 3)), is(true));
		assertThat(empty, containsLongs(1, 2, 3));

		assertThat(set.addAllLongs(LongList.create(3, 4, 5, 6, 7)), is(true));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(set.stream().collect(Collectors.toList()), contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(set.parallelStream().collect(Collectors.toList()),
		           contains(-5L, -4L, -3L, -2L, -1L, 0L, 1L, 2L, 3L, 4L));
	}

	@Test
	public void longStream() {
		assertThat(empty.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           is(emptyIterable()));

		assertThat(set.longStream().collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void parallelLongStream() {
		assertThat(empty.parallelLongStream()
		                .collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           is(emptyIterable()));

		assertThat(set.parallelLongStream()
		              .collect(LongList::create, LongList::addLong, LongList::addAllLongs),
		           containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(set.sequence(), containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void iteratorRemoveAll() {
		LongIterator iterator = set.iterator();
		long value = -5;
		while (iterator.hasNext()) {
			assertThat(iterator.nextLong(), is(value));
			iterator.remove();
			value++;
		}
		assertThat(value, is(5L));
		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void removeAllLongArray() {
		assertThat(empty.removeAllLongs(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAllLongs(1, 2, 3), is(true));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void removeAllLongCollection() {
		assertThat(empty.removeAll(LongList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(LongList.create(1, 2, 3)), is(true));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void retainAllLongArray() {
		assertThat(empty.retainAllLongs(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAllLongs(1, 2, 3), is(true));
		assertThat(set, containsLongs(1, 2, 3));
	}

	@Test
	public void retainAllLongCollection() {
		assertThat(empty.retainAll(LongList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(LongList.create(1, 2, 3)), is(true));
		assertThat(set, containsLongs(1, 2, 3));
	}

	@Test
	public void removeLongsIf() {
		assertThat(empty.removeLongsIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeLongsIf(x -> x > 3), is(true));
		assertThat(set, containsLongs(-5, -4, -3, -2, -1, 0, 1, 2, 3));
	}

	@Test
	public void containsAllLongArray() {
		assertThat(empty.containsAllLongs(1, 2, 3), is(false));
		assertThat(set.containsAllLongs(1, 2, 3), is(true));
		assertThat(set.containsAllLongs(1, 2, 3, 17), is(false));
	}

	@Test
	public void containsAllLongCollection() {
		assertThat(empty.containsAll(LongList.create(1, 2, 3)), is(false));
		assertThat(set.containsAll(LongList.create(1, 2, 3)), is(true));
		assertThat(set.containsAll(LongList.create(1, 2, 3, 17)), is(false));
	}

	@Test
	public void forEachLong() {
		empty.forEachLong(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicLong value = new AtomicLong(-5);
		set.forEachLong(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(5L));
	}

	@Test
	public void boundaries() {
		BitLongSet set = new BitLongSet();
		assertThat(set.addLong(Long.MIN_VALUE), is(true));
		assertThat(set.addLong(0), is(true));
		assertThat(set.addLong(Long.MAX_VALUE), is(true));

		assertThat(set, containsLongs(Long.MIN_VALUE, 0, Long.MAX_VALUE));

		assertThat(set.containsLong(Long.MIN_VALUE), is(true));
		assertThat(set.containsLong(0), is(true));
		assertThat(set.containsLong(Long.MAX_VALUE), is(true));

		assertThat(set.removeLong(Long.MIN_VALUE), is(true));
		assertThat(set.removeLong(0), is(true));
		assertThat(set.removeLong(Long.MAX_VALUE), is(true));

		assertThat(set, is(emptyIterable()));
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

		for (long randomValue : randomValues)
			assertThat(empty.addLong(randomValue), is(false));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAllLongs(randomValues), is(true));

		for (long randomValue : randomValues)
			assertThat(empty.containsLong(randomValue), is(true));

		// toString
		Arrays.sort(randomValues);
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (long randomValue : randomValues)
			assertThat(empty.removeLong(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty.size(), is(0));

		for (long randomValue : randomValues)
			assertThat(empty.removeLong(randomValue), is(false));
	}
}
