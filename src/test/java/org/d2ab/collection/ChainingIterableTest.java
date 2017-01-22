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

package org.d2ab.collection;

import org.junit.Test;

import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ChainingIterableTest {
	private final SizedIterable<String> empty = ChainingIterable.empty();

	private final SizedIterable<String> abc = ChainingIterable.concat(asList("a", "b", "c"));

	private final SizedIterable<String> abc_def =
			ChainingIterable.concat(asList("a", "b", "c"), asList("d", "e", "f"));

	private final SizedIterable<String> abc_def_ghi =
			ChainingIterable.concat(asList("a", "b", "c"), asList("d", "e", "f"), asList("g", "h", "i"));

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void one() {
		twice(() -> assertThat(abc, contains("a", "b", "c")));
	}

	@Test
	public void two() {
		twice(() -> assertThat(abc_def, contains("a", "b", "c", "d", "e", "f")));
	}

	@Test
	public void three() {
		twice(() -> assertThat(abc_def_ghi, contains("a", "b", "c", "d", "e", "f", "g", "h", "i")));
	}

	@Test
	public void lazy() {
		@SuppressWarnings("unchecked")
		Iterable<String> chainingIterable = ChainingIterable.concat(Iterables.of("a", "b", "c"), () -> {
			throw new IllegalStateException(); // Not thrown yet, until below when iterator is requested
		});

		Iterator<String> iterator = chainingIterable.iterator();
		assertThat(iterator.next(), is("a"));
		assertThat(iterator.next(), is("b"));
		assertThat(iterator.next(), is("c"));

		// Exception not thrown until iterator is encountered
		expecting(IllegalStateException.class, iterator::hasNext);
	}

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(abc.size(), is(3));
		assertThat(abc_def.size(), is(6));
		assertThat(abc_def_ghi.size(), is(9));

		SizedIterable<Integer> maxIntegerSize = new SizedIterable<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int size() {
				return Integer.MAX_VALUE;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}
		};

		SizedIterable concatMaxIntegerSize = ChainingIterable.concat(maxIntegerSize);
		assertThat(concatMaxIntegerSize.size(), is(Integer.MAX_VALUE));

		SizedIterable concatMaxIntegerSizePlusOne = ChainingIterable.concat(maxIntegerSize, Iterables.of(1));
		expecting(IllegalStateException.class, concatMaxIntegerSizePlusOne::size);
	}

	@Test
	public void isEmpty() {
		assertThat(empty.isEmpty(), is(true));
		assertThat(abc.isEmpty(), is(false));
		assertThat(abc_def.isEmpty(), is(false));
		assertThat(abc_def_ghi.isEmpty(), is(false));

		SizedIterable<Integer> empty = new SizedIterable<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int size() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isEmpty() {
				return true;
			}
		};

		SizedIterable<Integer> nonEmpty = new SizedIterable<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int size() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isEmpty() {
				return false;
			}
		};

		SizedIterable concatEmptyEmpty = ChainingIterable.concat(empty, empty);
		assertThat(concatEmptyEmpty.isEmpty(), is(true));

		SizedIterable concatEmptyNonEmpty = ChainingIterable.concat(empty, nonEmpty);
		assertThat(concatEmptyNonEmpty.isEmpty(), is(false));

		SizedIterable concatNonEmptyEmpty = ChainingIterable.concat(nonEmpty, empty);
		assertThat(concatNonEmptyEmpty.isEmpty(), is(false));

		SizedIterable concatNonEmptyNonEmpty = ChainingIterable.concat(nonEmpty, nonEmpty);
		assertThat(concatNonEmptyNonEmpty.isEmpty(), is(false));
	}
}
