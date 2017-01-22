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

import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MappedCollectionTest {
	private Collection<Integer> original = new ArrayDeque<>(Lists.of(1, 2, 3, 4, 5));
	private Collection<String> mapped = MappedCollection.from(original, Object::toString);

	private Collection<Integer> originalEmpty = new ArrayDeque<>();
	private Collection<String> mappedEmpty = MappedCollection.from(originalEmpty, Object::toString);

	@Test
	public void size() {
		assertThat(mappedEmpty.size(), is(0));
		assertThat(mapped.size(), is(5));
	}

	@Test
	public void isEmpty() {
		assertThat(mappedEmpty.isEmpty(), is(true));
		assertThat(mapped.isEmpty(), is(false));
	}

	@Test
	public void containsElement() {
		assertThat(mappedEmpty.contains("2"), is(false));

		for (int i = 1; i <= 5; i++)
			assertThat(mapped.contains(String.valueOf(i)), is(true));

		assertThat(mapped.contains("17"), is(false));
	}

	@Test
	public void iterator() {
		assertThat(mappedEmpty, is(emptyIterable()));
		assertThat(mapped, contains("1", "2", "3", "4", "5"));
	}

	@Test
	public void iteratorRemove() {
		Iterator<String> iterator = mapped.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.remove();

		assertThat(mapped, contains("1", "4", "5"));
		assertThat(original, contains(1, 4, 5));
	}

	@Test
	public void toArray() {
		assertThat(mappedEmpty.toArray(), is(emptyArray()));
		assertThat(mapped.toArray(), is(arrayContaining("1", "2", "3", "4", "5")));
	}

	@Test
	public void toArrayOfType() {
		assertThat(mappedEmpty.toArray(new String[0]), is(emptyArray()));
		assertThat(mapped.toArray(new String[5]), is(arrayContaining("1", "2", "3", "4", "5")));
	}

	@Test
	public void add() {
		expecting(UnsupportedOperationException.class, () -> mappedEmpty.add("17"));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> mapped.add("17"));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void remove() {
		assertThat(mappedEmpty.remove("17"), is(false));

		assertThat(mapped.remove("2"), is(true));
		assertThat(mapped, contains("1", "3", "4", "5"));
		assertThat(original, contains(1, 3, 4, 5));

		assertThat(mapped.remove("17"), is(false));
		assertThat(mapped, contains("1", "3", "4", "5"));
		assertThat(original, contains(1, 3, 4, 5));
	}

	@Test
	public void containsAll() {
		assertThat(mappedEmpty.containsAll(Lists.of("17", "18")), is(false));

		assertThat(mapped.containsAll(Lists.of("1", "2")), is(true));
		assertThat(mapped.containsAll(Lists.of("1", "17")), is(false));
	}

	@Test
	public void addAll() {
		expecting(UnsupportedOperationException.class, () -> mappedEmpty.addAll(Lists.of("1", "2")));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> mapped.addAll(Lists.of("1", "2")));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void removeAll() {
		assertThat(mappedEmpty.removeAll(Lists.of("1", "2")), is(false));
		assertThat(mappedEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		assertThat(mapped.removeAll(Lists.of()), is(false));
		assertThat(mapped, contains("1", "2", "3", "4", "5"));
		assertThat(original, contains(1, 2, 3, 4, 5));

		assertThat(mapped.removeAll(Lists.of("1", "2", "3")), is(true));
		assertThat(mapped, contains("4", "5"));
		assertThat(original, contains(4, 5));
	}

	@Test
	public void retainAll() {
		assertThat(mappedEmpty.retainAll(Lists.of(1, 2)), is(false));
		assertThat(mappedEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		assertThat(mapped.retainAll(Lists.of("1", "2", "3")), is(true));
		assertThat(mapped, contains("1", "2", "3"));
		assertThat(original, contains(1, 2, 3));
	}

	@Test
	public void clear() {
		mappedEmpty.clear();
		assertThat(mappedEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		mapped.clear();
		assertThat(mapped, is(emptyIterable()));
		assertThat(original, is(emptyIterable()));
	}

	@Test
	public void iteratorRemoveAll() {
		Iterator<String> iterator = mapped.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.next(), is(String.valueOf(i + 1)));
			iterator.remove();
			i++;
		}
		assertThat(i, is(5));

		assertThat(mapped, is(emptyIterable()));
		assertThat(original, is(emptyIterable()));
	}

	@Test
	public void stream() {
		assertThat(mappedEmpty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(mapped.stream().collect(Collectors.toList()), contains("1", "2", "3", "4", "5"));
	}

	@Test
	public void parallelStream() {
		assertThat(mappedEmpty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(mapped.parallelStream().collect(Collectors.toList()), contains("1", "2", "3", "4", "5"));
	}

	@Test
	public void removeIf() {
		mappedEmpty.removeIf(x -> x.equals("1") || x.equals("2"));
		assertThat(mappedEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		mapped.removeIf(x -> x.equals("1") || x.equals("2"));
		assertThat(mapped, contains("3", "4", "5"));
		assertThat(original, contains(3, 4, 5));
	}

	@Test
	public void forEach() {
		mappedEmpty.forEach(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger value = new AtomicInteger(1);
		mapped.forEach(x -> assertThat(x, is(String.valueOf(value.getAndIncrement()))));
		assertThat(value.get(), is(6));
	}
}
