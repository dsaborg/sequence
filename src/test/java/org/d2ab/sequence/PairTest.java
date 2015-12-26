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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PairTest {
	private final Pair<Integer, Integer> pair = Pair.of(1, 2);

	@Test
	public void of() {
		assertThat(pair.first(), is(1));
		assertThat(pair.second(), is(2));
	}

	@Test
	public void checkToString() {
		assertThat(pair.toString(), is("(1,2)"));
	}

	@Test
	public void checkHashCode() {
		assertThat(pair.hashCode(), is(pair.hashCode()));
	}

	@Test
	public void checkEquals() {
		assertThat(pair.equals(Pair.of(1, 2)), is(true));
		assertThat(pair.equals(Pair.of(1, 3)), is(false));
		assertThat(pair.equals(Pair.of(3, 2)), is(false));
		assertThat(pair.equals(null), is(false));
		assertThat(pair.equals(new Object()), is(false));
	}

	@Test
	public void put() {
		Map<Integer, Integer> map = new HashMap<>();
		pair.put(map);
		assertThat(map.get(1), is(2));
	}
}
