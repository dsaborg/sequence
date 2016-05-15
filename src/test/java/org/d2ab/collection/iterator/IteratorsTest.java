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

package org.d2ab.collection.iterator;

import org.d2ab.collection.Iterables;
import org.junit.Test;

import static org.d2ab.test.Tests.twice;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;

public class IteratorsTest {
	@Test
	public void empty() {
		Iterable<Object> empty = Iterables.empty();
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void of() {
		Iterable<Integer> none = Iterables.of();
		twice(() -> assertThat(none, is(emptyIterable())));

		Iterable<Integer> one = Iterables.of(1);
		twice(() -> assertThat(one, contains(1)));

		Iterable<Integer> two = Iterables.of(1, 2);
		twice(() -> assertThat(two, contains(1, 2)));

		Iterable<Integer> five = Iterables.of(1, 2, 3, 4, 5);
		twice(() -> assertThat(five, contains(1, 2, 3, 4, 5)));
	}

	@Test
	public void toList() {
		assertThat(Iterators.toList(Iterators.of(1, 2, 3, 4, 5)), contains(1, 2, 3, 4, 5));
	}
}