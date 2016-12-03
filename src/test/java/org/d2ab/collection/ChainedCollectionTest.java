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
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ChainedCollectionTest {
	private Collection<Integer> firstEmpty = new ArrayDeque<>();
	private Collection<Integer> secondEmpty = new ArrayDeque<>();
	private Collection<Integer> thirdEmpty = new ArrayDeque<>();
	@SuppressWarnings("unchecked")
	private Collection<Integer> chainedEmpty = ChainedCollection.from(firstEmpty, secondEmpty, thirdEmpty);

	private Collection<Integer> first = new ArrayDeque<>(asList(1, 2, 3));
	private Collection<Integer> second = new ArrayDeque<>(asList(4, 5, 6));
	private Collection<Integer> third = new ArrayDeque<>(asList(7, 8, 9, 10));
	@SuppressWarnings("unchecked")
	private Collection<Integer> chained = ChainedCollection.from(first, second, third);

	@Test
	public void size() {
		assertThat(chainedEmpty.size(), is(0));
		assertThat(chained.size(), is(10));
	}

	@Test
	public void isEmpty() {
		assertThat(chainedEmpty.isEmpty(), is(true));
		assertThat(chained.isEmpty(), is(false));
	}

	@Test
	public void containsElement() {
		assertThat(chainedEmpty.contains(17), is(false));

		for (int i = 1; i <= 10; i++)
			assertThat(chained.contains(i), is(true));

		assertThat(chained.contains(17), is(false));
	}

	@Test
	public void iterator() {
		assertThat(chainedEmpty, is(emptyIterable()));

		assertThat(chained, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void iteratorRemove() {
		Iterator<Integer> iterator = chained.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.next();
		iterator.remove();

		assertThat(chained, contains(1, 3, 5, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 3));
		assertThat(second, contains(5, 6));
		assertThat(third, contains(7, 8, 9, 10));
	}

	@Test
	public void toArray() {
		assertThat(chainedEmpty.toArray(), is(emptyArray()));

		assertThat(chained.toArray(), is(arrayContaining(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void toArrayOfType() {
		assertThat(chainedEmpty.toArray(new Integer[0]), is(emptyArray()));

		assertThat(chained.toArray(new Integer[10]), is(arrayContaining(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void add() {
		chainedEmpty.add(17);
		assertThat(chainedEmpty, contains(17));
		assertThat(firstEmpty, contains(17));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		chained.add(17);
		assertThat(chained, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 17));
		assertThat(first, contains(1, 2, 3));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10, 17));
	}

	@Test
	public void remove() {
		assertThat(chainedEmpty.remove(17), is(false));
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		assertThat(chained.remove(17), is(false));
		assertThat(chained, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 3));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));

		assertThat(chained.remove(5), is(true));
		assertThat(chained, contains(1, 2, 3, 4, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 3));
		assertThat(second, contains(4, 6));
		assertThat(third, contains(7, 8, 9, 10));
	}

	@Test
	public void containsAll() {
		assertThat(chainedEmpty.containsAll(asList(17, 18)), is(false));

		assertThat(chained.containsAll(asList(2, 3, 4)), is(true));
		assertThat(chained.containsAll(asList(2, 3, 17)), is(false));
	}

	@Test
	public void addAll() {
		chainedEmpty.addAll(asList(1, 2));
		assertThat(chainedEmpty, contains(1, 2));
		assertThat(firstEmpty, contains(1, 2));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		chained.addAll(asList(17, 18));
		assertThat(chained, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 17, 18));
		assertThat(first, contains(1, 2, 3));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10, 17, 18));
	}

	@Test
	public void removeAll() {
		assertThat(chainedEmpty.removeAll(asList(1, 2)), is(false));
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		assertThat(chained.removeAll(emptyList()), is(false));
		assertThat(chained, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 3));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));

		assertThat(chained.removeAll(asList(3, 4, 5)), is(true));
		assertThat(chained, contains(1, 2, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2));
		assertThat(second, contains(6));
		assertThat(third, contains(7, 8, 9, 10));
	}

	@Test
	public void retainAll() {
		assertThat(chainedEmpty.retainAll(asList(1, 2)), is(false));
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		assertThat(chained.retainAll(asList(2, 3, 4)), is(true));
		assertThat(chained, contains(2, 3, 4));
		assertThat(first, contains(2, 3));
		assertThat(second, contains(4));
		assertThat(third, is(emptyIterable()));
	}

	@Test
	public void clear() {
		chainedEmpty.clear();
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		chained.clear();
		assertThat(chained, is(emptyIterable()));
		assertThat(first, is(emptyIterable()));
		assertThat(second, is(emptyIterable()));
		assertThat(third, is(emptyIterable()));
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<Integer> iterator = chained.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is(i + 1));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));

		assertThat(chained, is(emptyIterable()));
		assertThat(first, is(emptyIterable()));
		assertThat(second, is(emptyIterable()));
		assertThat(third, is(emptyIterable()));
	}

	@Test
	public void stream() {
		assertThat(chainedEmpty.stream().collect(Collectors.toList()), is(emptyIterable()));

		assertThat(chained.stream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void parallelStream() {
		assertThat(chainedEmpty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));

		assertThat(chained.parallelStream().collect(Collectors.toList()), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void removeIf() {
		chainedEmpty.removeIf(x -> x.equals(2) || x.equals(5));
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		chained.removeIf(x -> x.equals(2) || x.equals(5));
		assertThat(chained, contains(1, 3, 4, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 3));
		assertThat(second, contains(4, 6));
		assertThat(third, contains(7, 8, 9, 10));
	}

	@Test
	public void forEach() {
		chainedEmpty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		chained.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(11));
	}
}
