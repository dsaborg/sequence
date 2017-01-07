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

package org.d2ab.iterator;

import org.d2ab.collection.Iterables;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class IteratorsTest {
	private final Iterator<Object> empty = Iterators.empty();
	private final Iterator<Integer> iterator = Iterators.of(1, 2, 3, 4, 5);

	@Test
	public void constructor() {
		new Iterators() {
			// code coverage
		};
	}

	@Test
	public void empty() {
		assertThat(empty.hasNext(), is(false));
		expecting(NoSuchElementException.class, empty::next);
	}

	@Test
	public void of() {
		Iterator<Integer> none = Iterators.of();
		assertThat(Iterables.once(none), is(emptyIterable()));
		assertThat(none.hasNext(), is(false));
		expecting(NoSuchElementException.class, none::next);

		Iterator<Integer> one = Iterators.of(1);
		assertThat(Iterables.once(one), contains(1));
		assertThat(one.hasNext(), is(false));
		expecting(NoSuchElementException.class, one::next);

		Iterator<Integer> two = Iterators.of(1, 2);
		assertThat(Iterables.once(two), contains(1, 2));
		assertThat(two.hasNext(), is(false));
		expecting(NoSuchElementException.class, two::next);

		assertThat(Iterables.once(iterator), contains(1, 2, 3, 4, 5));
		assertThat(iterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, iterator::next);
	}

	@Test
	public void size() {
		assertThat(Iterators.size(empty), is(0));
		assertThat(Iterators.size(iterator), is(5));
	}

	@Test
	public void bigSize() {
		assertThat(Iterators.size(iterator, it -> {
			assertThat(it, is(sameInstance(iterator)));
			return (long) Integer.MAX_VALUE;
		}), is(Integer.MAX_VALUE));

		expecting(IllegalStateException.class, () ->
				Iterators.size(iterator,
				               it -> {
					               assertThat(it, is(sameInstance(iterator)));
					               return Integer.MAX_VALUE + 1L;
				               }));
	}

	@Test
	public void skip() {
		assertThat(Iterators.skip(empty), is(false));

		assertThat(iterator.next(), is(1));
		assertThat(Iterators.skip(iterator), is(true));
		assertThat(iterator.next(), is(3));
	}

	@Test
	public void toList() {
		assertThat(Iterators.toList(iterator), contains(1, 2, 3, 4, 5));
	}
}