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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class FilteredCollectionTest {
	private final Predicate<Integer> predicate = e -> e % 2 == 1;

	private final Collection<Integer> originalEmpty = new ArrayDeque<>();
	private final Collection<Integer> filteredEmpty = FilteredCollection.from(originalEmpty, predicate);

	private final Collection<Integer> original = new ArrayDeque<>(asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
	private final Collection<Integer> filtered = FilteredCollection.from(original, predicate);

	@Test
	public void size() {
		assertThat(filteredEmpty.size(), is(0));
		assertThat(filtered.size(), is(5));
	}

	@Test
	public void isEmpty() {
		assertThat(filteredEmpty.isEmpty(), is(true));
		assertThat(filtered.isEmpty(), is(false));
	}

	@Test
	public void containsElement() {
		assertThat(filteredEmpty.contains(2), is(false));

		for (int i = 1; i <= 9; i += 2)
			assertThat(filtered.contains(i), is(true));

		assertThat(filtered.contains(17), is(false));
	}

	@Test
	public void iterator() {
		assertThat(filteredEmpty, is(emptyIterable()));
		assertThat(filtered, contains(1, 3, 5, 7, 9));
	}

	@Test
	public void iteratorRemove() {
		Iterator<Integer> iterator = filtered.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.remove();

		assertThat(filtered, contains(1, 7, 9));
		assertThat(original, contains(1, 2, 4, 6, 7, 8, 9, 10));
	}

	@Test
	public void toArray() {
		assertThat(filteredEmpty.toArray(), is(emptyArray()));
		assertThat(filtered.toArray(), is(arrayContaining(1, 3, 5, 7, 9)));
	}

	@Test
	public void toArrayOfType() {
		assertThat(filteredEmpty.toArray(new Integer[0]), is(emptyArray()));
		assertThat(filtered.toArray(new Integer[5]), is(arrayContaining(1, 3, 5, 7, 9)));
	}

	@Test
	public void add() {
		assertThat(filteredEmpty.add(3), is(true));
		assertThat(filteredEmpty, contains(3));
		assertThat(originalEmpty, contains(3));

		expecting(IllegalArgumentException.class, () -> filteredEmpty.add(4));
		assertThat(filteredEmpty, contains(3));
		assertThat(originalEmpty, contains(3));

		assertThat(filtered.add(17), is(true));
		assertThat(filtered, contains(1, 3, 5, 7, 9, 17));
		assertThat(original, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 17));

		expecting(IllegalArgumentException.class, () -> filtered.add(18));
		assertThat(filtered, contains(1, 3, 5, 7, 9, 17));
		assertThat(original, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 17));
	}

	@Test
	public void remove() {
		assertThat(filteredEmpty.remove(17), is(false));

		assertThat(filtered.remove(1), is(true));
		assertThat(filtered, contains(3, 5, 7, 9));
		assertThat(original, contains(2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(filtered.remove(2), is(false));
		assertThat(filtered, contains(3, 5, 7, 9));
		assertThat(original, contains(2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(filtered.remove(17), is(false));
		assertThat(filtered, contains(3, 5, 7, 9));
		assertThat(original, contains(2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void containsAll() {
		assertThat(filteredEmpty.containsAll(asList(17, 18)), is(false));

		assertThat(filtered.containsAll(asList(2, 3)), is(false));
		assertThat(filtered.containsAll(asList(1, 3)), is(true));
		assertThat(filtered.containsAll(asList(1, 17)), is(false));
	}

	@Test
	public void addAll() {
		assertThat(filteredEmpty.addAll(asList(1, 3)), is(true));
		assertThat(filteredEmpty, contains(1, 3));
		assertThat(originalEmpty, contains(1, 3));

		expecting(IllegalArgumentException.class, () -> filteredEmpty.addAll(asList(1, 2)));
		assertThat(filteredEmpty, contains(1, 3, 1));
		assertThat(originalEmpty, contains(1, 3, 1));

		assertThat(filtered.addAll(asList(1, 3)), is(true));
		assertThat(filtered, contains(1, 3, 5, 7, 9, 1, 3));
		assertThat(original, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 3));

		expecting(IllegalArgumentException.class, () -> filtered.addAll(asList(1, 2)));
		assertThat(filtered, contains(1, 3, 5, 7, 9, 1, 3, 1));
		assertThat(original, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 3, 1));
	}

	@Test
	public void removeAll() {
		assertThat(filteredEmpty.removeAll(asList(1, 3)), is(false));
		assertThat(filteredEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		assertThat(filtered.removeAll(emptyList()), is(false));
		assertThat(original, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(filtered.removeAll(asList(1, 2, 3, 4, 5)), is(true));
		assertThat(filtered, contains(7, 9));
		assertThat(original, contains(2, 4, 6, 7, 8, 9, 10));
	}

	@Test
	public void retainAll() {
		assertThat(filteredEmpty.retainAll(asList(1, 2)), is(false));
		assertThat(filteredEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		assertThat(filtered.retainAll(asList(1, 2, 3)), is(true));
		assertThat(filtered, contains(1, 3));
		assertThat(original, contains(1, 2, 3, 4, 6, 8, 10));
	}

	@Test
	public void clear() {
		filteredEmpty.clear();
		assertThat(filteredEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		filtered.clear();
		assertThat(filtered, is(emptyIterable()));
		assertThat(original, contains(2, 4, 6, 8, 10));
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Integer> iterator = filtered.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is(i * 2 + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(5));

		assertThat(filtered, is(emptyIterable()));
		assertThat(original, contains(2, 4, 6, 8, 10));
	}

	@Test
	public void stream() {
		assertThat(filteredEmpty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(filtered.stream().collect(Collectors.toList()), contains(1, 3, 5, 7, 9));
	}

	@Test
	public void parallelStream() {
		assertThat(filteredEmpty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(filtered.parallelStream().collect(Collectors.toList()), contains(1, 3, 5, 7, 9));
	}

	@Test
	public void removeIf() {
		filteredEmpty.removeIf(x -> x == 1 || x == 2);
		assertThat(filteredEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		filtered.removeIf(x -> x == 1 || x == 2);
		assertThat(filtered, contains(3, 5, 7, 9));
		assertThat(original, contains(2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void forEach() {
		filteredEmpty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		filtered.forEach(x -> assertThat(x, is(value.getAndAdd(2))));
		assertThat(value.get(), is(11));
	}
}