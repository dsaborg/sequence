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

package org.d2ab.util;

import org.d2ab.collection.Maps;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PairTest {
	private final Pair<Integer, String> pair = Pair.of(1, "2");

	@Test
	public void of() {
		assertThat(pair.getLeft(), is(1));
		assertThat(pair.getRight(), is("2"));
		assertThat(pair.toString(), is("(1, \"2\")"));
	}

	@Test
	public void fromEntry() {
		AtomicInteger value = new AtomicInteger();

		Pair<Integer, Integer> pairFromEntry = Pair.from(new Entry<Integer, Integer>() {
			@Override
			public Integer getKey() {
				return value.incrementAndGet();
			}

			@Override
			public Integer getValue() {
				return value.incrementAndGet();
			}

			@Override
			public Integer setValue(Integer value) {
				return value;
			}
		});

		assertThat(pairFromEntry.getLeft(), is(1));
		assertThat(pairFromEntry.getRight(), is(2));
		expecting(UnsupportedOperationException.class, () -> pairFromEntry.setValue(17));

		// Test assignment is pass-through
		assertThat(pairFromEntry.getLeft(), is(3));
		assertThat(pairFromEntry.getRight(), is(4));

		assertThat(pairFromEntry.toString(), is("(5, 6)"));
	}

	@Test
	public void fromCopiedEntry() {
		AtomicInteger value = new AtomicInteger();

		Pair<Integer, Integer> pairFromEntry = Pair.copy(new Entry<Integer, Integer>() {
			@Override
			public Integer getKey() {
				return value.incrementAndGet();
			}

			@Override
			public Integer getValue() {
				return value.incrementAndGet();
			}

			@Override
			public Integer setValue(Integer value) {
				return value;
			}
		});

		assertThat(pairFromEntry.getLeft(), is(1));
		assertThat(pairFromEntry.getRight(), is(2));
		expecting(UnsupportedOperationException.class, () -> pairFromEntry.setValue(17));

		// Test assignment is not pass-through
		assertThat(pairFromEntry.getLeft(), is(1));
		assertThat(pairFromEntry.getRight(), is(2));

		assertThat(pairFromEntry.toString(), is("(1, 2)"));
	}

	@Test
	public void unary() {
		Pair<Integer, Integer> unaryPair = Pair.unary(1);
		assertThat(unaryPair.getLeft(), is(1));
		assertThat(unaryPair.getRight(), is(1));
		assertThat(unaryPair.toString(), is("(1, 1)"));
	}

	@Test
	public void testHashCode() {
		assertThat(pair.hashCode(), Matchers.is(pair.hashCode()));
	}

	@Test
	public void hashCodeAcrossTypes() {
		Pair<Integer, String> pairFromEntry = Pair.from(Maps.entry(1, "2"));
		assertThat(pair.hashCode(), Matchers.is(pairFromEntry.hashCode()));
		assertThat(pair.hashCode(), is(Matchers.not(Pair.from(Maps.entry(1, "3")).hashCode())));
		assertThat(pair.hashCode(), is(Matchers.not(Pair.from(Maps.entry(3, "2")).hashCode())));
		assertThat(pair.hashCode(), is(Matchers.not(Pair.from(Maps.entry(3, "4")).hashCode())));

		Pair<Integer, Integer> unaryPair = Pair.unary(1);
		assertThat(Pair.of(1, 1).hashCode(), Matchers.is(unaryPair.hashCode()));
		assertThat(Pair.of(1, 2).hashCode(), is(Matchers.not(unaryPair.hashCode())));
		assertThat(Pair.of(2, 1).hashCode(), is(Matchers.not(unaryPair.hashCode())));
		assertThat(Pair.of(2, 2).hashCode(), is(Matchers.not(unaryPair.hashCode())));
	}

	@Test
	public void testEquals() {
		assertThat(pair.equals(Pair.of(1, "2")), is(true));
		assertThat(pair.equals(Pair.of(1, "3")), is(false));
		assertThat(pair.equals(Pair.of(3, "2")), is(false));
		assertThat(pair.equals(null), is(false));
		assertThat(pair.equals(new Object()), is(false));
	}

	@Test
	public void equalsAcrossTypes() {
		assertThat(pair.equals(Pair.from(Maps.entry(1, "2"))), is(true));
		assertThat(Pair.of(1, 1).equals(Pair.unary(1)), is(true));
	}

	@Test
	public void clonePairOf() {
		Pair<Integer, String> original = Pair.of(1, "2");
		Pair<Integer, String> clone = original.clone();
		assertThat(clone, is(equalTo(original)));
	}

	@Test
	public void clonePairFromMapsEntry() {
		Pair<Integer, String> original = Pair.from(Maps.entry(1, "2"));
		Pair<Integer, String> clone = original.clone();
		assertThat(clone, is(equalTo(original)));
	}

	@Test
	public void clonePairFromAnonymousEntry() {
		Pair<Integer, String> original = Pair.from(new Entry<Integer, String>() {
			@Override
			public Integer getKey() {
				return 1;
			}

			@Override
			public String getValue() {
				return "2";
			}

			@Override
			public String setValue(String value) {
				throw new UnsupportedOperationException();
			}
		});
		Pair<Integer, String> clone = original.clone();
		assertThat(clone, is(equalTo(original)));
	}

	@Test
	public void clonePairCopiedFromEntry() {
		Pair<Integer, String> original = Pair.copy(Maps.entry(1, "2"));
		Pair<Integer, String> clone = original.clone();
		assertThat(clone, is(equalTo(original)));
	}

	@Test
	public void cloneUnaryPair() {
		Pair<Integer, Integer> original = Pair.unary(1);
		Pair<Integer, Integer> clone = original.clone();
		assertThat(clone, is(equalTo(original)));
	}

	@Test
	public void serializationPairOf() throws IOException, ClassNotFoundException {
		Pair<Integer, String> original = Pair.of(1, "2");
		Pair<Integer, String> deserialized = serializeDeserialize(original);

		assertThat(deserialized, is(equalTo(original)));
	}

	@Test
	public void serializationPairFromEntry() throws IOException, ClassNotFoundException {
		Pair<Integer, String> original = Pair.from(Maps.entry(1, "2"));
		Pair<Integer, String> deserialized = serializeDeserialize(original);

		assertThat(deserialized, is(equalTo(original)));
	}

	@Test
	public void serializationPairCopiedFromEntry() throws IOException, ClassNotFoundException {
		Pair<Integer, String> original = Pair.copy(Maps.entry(1, "2"));
		Pair<Integer, String> deserialized = serializeDeserialize(original);

		assertThat(deserialized, is(equalTo(original)));
	}

	@Test
	public void serializationUnaryPair() throws IOException, ClassNotFoundException {
		Pair<Integer, Integer> original = Pair.unary(1);
		Pair<Integer, Integer> deserialized = serializeDeserialize(original);

		assertThat(deserialized, is(equalTo(original)));
	}

	@SuppressWarnings("unchecked")
	private static <L, R> Pair<L, R> serializeDeserialize(Pair<L, R> original)
			throws IOException, ClassNotFoundException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bytes);
		out.writeObject(original);

		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
		return (Pair<L, R>) in.readObject();
	}

	@Test
	public void put() {
		Map<Integer, String> map = new HashMap<>();
		pair.put(map);
		assertThat(map.get(1), is("2"));
	}

	@Test
	public void swapped() {
		assertThat(pair.swap(), Matchers.is(Pair.of("2", 1)));
	}

	@Test
	public void shiftLeft() {
		assertThat(pair.shiftLeft(17), Matchers.is(Pair.of("2", 17)));
	}

	@Test
	public void shiftRight() {
		assertThat(pair.shiftRight("17"), Matchers.is(Pair.of("17", 1)));
	}

	@Test
	public void withFirst() {
		assertThat(pair.withLeft(17), Matchers.is(Pair.of(17, "2")));
	}

	@Test
	public void withSecond() {
		assertThat(pair.withRight("17"), Matchers.is(Pair.of(1, "17")));
	}

	@Test
	public void apply() {
		assertThat(pair.apply((x, y) -> Pair.of(x + 1, y + "0")), is(Pair.of(2, "20")));
	}

	@Test
	public void test() {
		assertThat(pair.test(x -> {
			assertThat(x, is(1));
			return true;
		}, y -> {
			assertThat(y, is("2"));
			return true;
		}), is(true));

		assertThat(pair.test(x -> true, y -> true), is(true));
		assertThat(pair.test(x -> true, y -> false), is(false));
		assertThat(pair.test(x -> false, y -> true), is(false));
		assertThat(pair.test(x -> false, y -> false), is(false));
	}

	@Test
	public void iterator() {
		Iterator<Integer> iterator = Pair.of(1, 2).iterator();
		assertThat(iterator.hasNext(), CoreMatchers.is(true));
		assertThat(iterator.next(), CoreMatchers.is(1));
		assertThat(iterator.hasNext(), CoreMatchers.is(true));
		assertThat(iterator.next(), CoreMatchers.is(2));
		assertThat(iterator.hasNext(), CoreMatchers.is(false));
		expecting(NoSuchElementException.class, iterator::next);
	}
}
