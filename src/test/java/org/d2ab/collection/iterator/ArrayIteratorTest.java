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

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ArrayIteratorTest {
	@Test
	public void arrayIterator() {
		ArrayIterator<Integer> iterator = new ArrayIterator<>(1, 2, 3);

		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(1));

		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(2));

		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(3));

		assertThat(iterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, iterator::next);
	}
}