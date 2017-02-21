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

import org.d2ab.sequence.Sequence;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;
import static org.d2ab.collection.SizedIterable.SizeType.*;
import static org.d2ab.test.IsIterableBeginningWith.beginsWith;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ChainedCollectionTest {
	private Collection<Integer> chainedTotallyEmpty = ChainedCollection.concat(new ArrayDeque<Collection<Integer>>());

	private Collection<Integer> firstEmpty = new ArrayDeque<>();
	private Collection<Integer> secondEmpty = new ArrayDeque<>();
	private Collection<Integer> thirdEmpty = new ArrayDeque<>();
	@SuppressWarnings("unchecked")
	private Collection<Integer> chainedEmpty = ChainedCollection.concat(firstEmpty, secondEmpty, thirdEmpty);

	private Collection<Integer> first = new ArrayDeque<>(Lists.of(1, 2, 3));
	private Collection<Integer> second = new ArrayDeque<>(Lists.of(4, 5, 6));
	private Collection<Integer> third = new ArrayDeque<>(Lists.of(7, 8, 9, 10));
	@SuppressWarnings("unchecked")
	private Collection<Integer> chained = ChainedCollection.concat(first, second, third);

	@SuppressWarnings("unchecked")
	private Collection<Integer> fixed = ChainedCollection.concat(Lists.of(1, 2, 3), Lists.of(4, 5, 6),
	                                                             Lists.of(7, 8, 9, 10));

	@SuppressWarnings("unchecked")
	private Collection<Integer> infinite = ChainedCollection.concat(new ArrayDeque<>(Lists.of(17)),
	                                                                Sequence.recurse(1, x -> x + 1),
	                                                                new ArrayDeque<>(Lists.of(18)));

	private Collection<Integer> infiniteCollections = ChainedCollection.concat(
			Sequence.generate(() -> (Collection<Integer>) Lists.of(17)));

	@Test
	public void size() {
		assertThat(chainedTotallyEmpty.size(), is(0));
		assertThat(chainedEmpty.size(), is(0));
		assertThat(chained.size(), is(10));
		assertThat(fixed.size(), is(10));
		expecting(UnsupportedOperationException.class, infinite::size);
		expecting(UnsupportedOperationException.class, infiniteCollections::size);
	}

	@Test
	public void sizeType() {
		assertThat(Iterables.sizeType(chainedTotallyEmpty), is(AVAILABLE));
		assertThat(Iterables.sizeType(chainedEmpty), is(AVAILABLE));
		assertThat(Iterables.sizeType(chained), is(AVAILABLE));
		assertThat(Iterables.sizeType(fixed), is(FIXED));
		assertThat(Iterables.sizeType(infinite), is(INFINITE));
		expecting(UnsupportedOperationException.class, () -> Iterables.sizeType(infiniteCollections));
	}

	@Test
	public void isEmpty() {
		assertThat(chainedTotallyEmpty.isEmpty(), is(true));
		assertThat(chainedEmpty.isEmpty(), is(true));
		assertThat(chained.isEmpty(), is(false));
		assertThat(fixed.isEmpty(), is(false));
		assertThat(infinite.isEmpty(), is(false));
		expecting(UnsupportedOperationException.class, infiniteCollections::isEmpty);
	}

	@Test
	public void containsElement() {
		assertThat(chainedTotallyEmpty.contains(17), is(false));

		assertThat(chainedEmpty.contains(17), is(false));

		for (int i = 1; i <= 10; i++)
			assertThat(chained.contains(i), is(true));
		assertThat(chained.contains(17), is(false));

		for (int i = 1; i <= 10; i++)
			assertThat(fixed.contains(i), is(true));
		assertThat(fixed.contains(17), is(false));

		assertThat(infinite.contains(17), is(true));
		assertThat(infiniteCollections.contains(17), is(true));
	}

	@Test
	public void iterator() {
		assertThat(chainedTotallyEmpty, is(emptyIterable()));
		expecting(NoSuchElementException.class, () -> chainedTotallyEmpty.iterator().next());

		assertThat(chainedEmpty, is(emptyIterable()));
		expecting(NoSuchElementException.class, () -> chainedEmpty.iterator().next());

		assertThat(chained, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(fixed, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(infinite, beginsWith(17, 1, 2, 3, 4, 5));
		assertThat(infiniteCollections, beginsWith(17, 17, 17, 17));
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
		assertThat(chainedTotallyEmpty.toArray(), is(emptyArray()));

		assertThat(chainedEmpty.toArray(), is(emptyArray()));

		assertThat(chained.toArray(), is(arrayContaining(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));

		assertThat(fixed.toArray(), is(arrayContaining(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void toArrayOfType() {
		assertThat(chainedTotallyEmpty.toArray(new Integer[0]), is(emptyArray()));

		assertThat(chainedEmpty.toArray(new Integer[0]), is(emptyArray()));

		assertThat(chained.toArray(new Integer[10]), is(arrayContaining(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));

		assertThat(fixed.toArray(new Integer[10]), is(arrayContaining(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void add() {
		chainedTotallyEmpty.add(17);
		assertThat(chainedTotallyEmpty, contains(17));

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

		expecting(UnsupportedOperationException.class, () -> fixed.add(17));

		infinite.add(19);
		assertThat(infinite, beginsWith(17, 1, 2, 3, 4, 5));
	}

	@Test
	public void remove() {
		assertThat(chainedTotallyEmpty.remove(17), is(false));
		assertThat(chainedTotallyEmpty, is(emptyIterable()));

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

		expecting(UnsupportedOperationException.class, () -> fixed.remove(5));

		assertThat(infinite.remove(17), is(true));
		assertThat(infinite, beginsWith(1, 2, 3, 4, 5));
	}

	@Test
	public void containsAll() {
		assertThat(chainedTotallyEmpty.containsAll(Lists.of(17, 18)), is(false));

		assertThat(chainedEmpty.containsAll(Lists.of(17, 18)), is(false));

		assertThat(chained.containsAll(Lists.of(2, 3, 4)), is(true));
		assertThat(chained.containsAll(Lists.of(2, 3, 17)), is(false));

		assertThat(fixed.containsAll(Lists.of(2, 3, 4)), is(true));
		assertThat(fixed.containsAll(Lists.of(2, 3, 17)), is(false));

		assertThat(infinite.containsAll(Lists.of(17, 1, 2, 3)), is(true));
		assertThat(infiniteCollections.containsAll(Lists.of(17)), is(true));
	}

	@Test
	public void addAll() {
		assertThat(chainedTotallyEmpty.addAll(Lists.of(1, 2)), is(true));
		assertThat(chainedTotallyEmpty, contains(1, 2));

		assertThat(chainedEmpty.addAll(Lists.of(1, 2)), is(true));
		assertThat(chainedEmpty, contains(1, 2));
		assertThat(firstEmpty, contains(1, 2));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		assertThat(chained.addAll(Lists.of(17, 18)), is(true));
		assertThat(chained, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 17, 18));
		assertThat(first, contains(1, 2, 3));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10, 17, 18));

		expecting(UnsupportedOperationException.class, () -> fixed.addAll(Lists.of(17, 18)));

		assertThat(infinite.addAll(Lists.of(19, 20)), is(true));
		assertThat(infinite, beginsWith(17, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeAll() {
		assertThat(chainedTotallyEmpty.removeAll(Lists.of(1, 2)), is(false));
		assertThat(chainedTotallyEmpty, is(emptyIterable()));

		assertThat(chainedEmpty.removeAll(Lists.of(1, 2)), is(false));
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		assertThat(chained.removeAll(Lists.of()), is(false));
		assertThat(chained, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2, 3));
		assertThat(second, contains(4, 5, 6));
		assertThat(third, contains(7, 8, 9, 10));

		assertThat(chained.removeAll(Lists.of(3, 4, 5)), is(true));
		assertThat(chained, contains(1, 2, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 2));
		assertThat(second, contains(6));
		assertThat(third, contains(7, 8, 9, 10));

		expecting(UnsupportedOperationException.class, () -> fixed.removeAll(Lists.of(3, 4, 5)));
	}

	@Test
	public void retainAll() {
		assertThat(chainedTotallyEmpty.retainAll(Lists.of(1, 2)), is(false));
		assertThat(chainedTotallyEmpty, is(emptyIterable()));

		assertThat(chainedEmpty.retainAll(Lists.of(1, 2)), is(false));
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		assertThat(chained.retainAll(Lists.of(2, 3, 4)), is(true));
		assertThat(chained, contains(2, 3, 4));
		assertThat(first, contains(2, 3));
		assertThat(second, contains(4));
		assertThat(third, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> fixed.retainAll(Lists.of(2, 3, 4)));
	}

	@Test
	public void clear() {
		chainedTotallyEmpty.clear();
		assertThat(chainedTotallyEmpty, is(emptyIterable()));

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

		expecting(UnsupportedOperationException.class, fixed::clear);
		expecting(UnsupportedOperationException.class, infinite::clear);
		expecting(UnsupportedOperationException.class, infiniteCollections::clear);
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
		assertThat(chainedTotallyEmpty.stream().collect(toList()), is(emptyIterable()));

		assertThat(chainedEmpty.stream().collect(toList()), is(emptyIterable()));

		assertThat(chained.stream().collect(toList()), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(fixed.stream().collect(toList()), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(infinite.stream().limit(6).collect(toList()), contains(17, 1, 2, 3, 4, 5));

		assertThat(infiniteCollections.stream().limit(4).collect(toList()), contains(17, 17, 17, 17));
	}

	@Test
	public void parallelStream() {
		assertThat(chainedTotallyEmpty.parallelStream().collect(toList()), is(emptyIterable()));

		assertThat(chainedEmpty.parallelStream().collect(toList()), is(emptyIterable()));

		assertThat(chained.parallelStream().collect(toList()), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

		assertThat(fixed.parallelStream().collect(toList()), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
	}

	@Test
	public void removeIf() {
		chainedTotallyEmpty.removeIf(x -> x == 2 || x == 5);
		assertThat(chainedTotallyEmpty, is(emptyIterable()));

		chainedEmpty.removeIf(x -> x == 2 || x == 5);
		assertThat(chainedEmpty, is(emptyIterable()));
		assertThat(firstEmpty, is(emptyIterable()));
		assertThat(secondEmpty, is(emptyIterable()));
		assertThat(thirdEmpty, is(emptyIterable()));

		chained.removeIf(x -> x == 2 || x == 5);
		assertThat(chained, contains(1, 3, 4, 6, 7, 8, 9, 10));
		assertThat(first, contains(1, 3));
		assertThat(second, contains(4, 6));
		assertThat(third, contains(7, 8, 9, 10));

		expecting(UnsupportedOperationException.class, () -> fixed.removeIf(x -> x == 2 || x == 5));
	}

	@Test
	public void forEach() {
		chainedTotallyEmpty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		chainedEmpty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		chained.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(11));

		value.set(1);
		fixed.forEach(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(11));
	}
}
