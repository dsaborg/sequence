/*
 * Copyright 2015 Daniel Skogquist Åborg
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
package org.d2ab.sequence;

import org.d2ab.collection.Maps;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
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
				return value.addAndGet(1);
			}

			@Override
			public Integer getValue() {
				return value.addAndGet(1);
			}

			@Override
			public Integer setValue(Integer value) {
				throw new UnsupportedOperationException();
			}
		});

		assertThat(pairFromEntry.getLeft(), is(1));
		assertThat(pairFromEntry.getRight(), is(2));

		// Test assignment is pass-through
		assertThat(pairFromEntry.getLeft(), is(3));
		assertThat(pairFromEntry.getRight(), is(4));

		assertThat(pairFromEntry.toString(), is("(5, 6)"));
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
		assertThat(pair.hashCode(), is(pair.hashCode()));
	}

	@Test
	public void hashCodeAcrossTypes() {
		Pair<Integer, String> pairFromEntry = Pair.from(Maps.entry(1, "2"));
		assertThat(pair.hashCode(), is(pairFromEntry.hashCode()));
		assertThat(pair.hashCode(), is(not(Pair.from(Maps.entry(1, "3")).hashCode())));
		assertThat(pair.hashCode(), is(not(Pair.from(Maps.entry(3, "2")).hashCode())));
		assertThat(pair.hashCode(), is(not(Pair.from(Maps.entry(3, "4")).hashCode())));

		Pair<Integer, Integer> unaryPair = Pair.unary(1);
		assertThat(Pair.of(1, 1).hashCode(), is(unaryPair.hashCode()));
		assertThat(Pair.of(1, 2).hashCode(), is(not(unaryPair.hashCode())));
		assertThat(Pair.of(2, 1).hashCode(), is(not(unaryPair.hashCode())));
		assertThat(Pair.of(2, 2).hashCode(), is(not(unaryPair.hashCode())));
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
	public void put() {
		Map<Integer, String> map = new HashMap<>();
		pair.putInto(map);
		assertThat(map.get(1), is("2"));
	}

	@Test
	public void swapped() {
		assertThat(pair.swap(), is(Pair.of("2", 1)));
	}

	@Test
	public void shiftLeft() {
		assertThat(pair.shiftLeft(17), is(Pair.of("2", 17)));
	}

	@Test
	public void shiftRight() {
		assertThat(pair.shiftRight("17"), is(Pair.of("17", 1)));
	}

	@Test
	public void withFirst() {
		assertThat(pair.withLeft(17), is(Pair.of(17, "2")));
	}

	@Test
	public void withSecond() {
		assertThat(pair.withRight("17"), is(Pair.of(1, "17")));
	}
}
