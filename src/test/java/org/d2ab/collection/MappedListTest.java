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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class MappedListTest {
	private final Supplier<List<Integer>> constructor;

	private List<Integer> originalEmpty;
	private List<String> mappedEmpty;

	private List<Integer> original;
	private List<String> mapped;

	public MappedListTest(Supplier<List<Integer>> constructor) {
		this.constructor = constructor;
	}

	@Before
	public void setUp() {
		originalEmpty = constructor.get();
		mappedEmpty = MappedList.from(originalEmpty, Object::toString);

		original = constructor.get();
		original.addAll(asList(1, 2, 3, 4, 5));
		mapped = MappedList.from(original, Object::toString);
	}

	@Parameters
	public static Object[][] parameters() {
		return new Supplier[][]{{ArrayList::new}, {LinkedList::new}};
	}

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
		assertThat(mappedEmpty.containsAll(asList("17", "18")), is(false));

		assertThat(mapped.containsAll(asList("1", "2")), is(true));
		assertThat(mapped.containsAll(asList("1", "17")), is(false));
	}

	@Test
	public void addAll() {
		expecting(UnsupportedOperationException.class, () -> mappedEmpty.addAll(asList("1", "2")));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> mapped.addAll(asList("1", "2")));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void addAllAtIndex() {
		expecting(UnsupportedOperationException.class, () -> mappedEmpty.addAll(0, asList("1", "2")));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> mapped.addAll(0, asList("1", "2")));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void removeAll() {
		assertThat(mappedEmpty.removeAll(asList("1", "2")), is(false));
		assertThat(mappedEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		assertThat(mapped.removeAll(asList()), is(false));
		assertThat(mapped, contains("1", "2", "3", "4", "5"));
		assertThat(original, contains(1, 2, 3, 4, 5));

		assertThat(mapped.removeAll(asList("1", "2", "3")), is(true));
		assertThat(mapped, contains("4", "5"));
		assertThat(original, contains(4, 5));
	}

	@Test
	public void retainAll() {
		assertThat(mappedEmpty.retainAll(asList(1, 2)), is(false));
		assertThat(mappedEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		assertThat(mapped.retainAll(asList("1", "2", "3")), is(true));
		assertThat(mapped, contains("1", "2", "3"));
		assertThat(original, contains(1, 2, 3));
	}

	@Test
	public void replaceAll() {
		mappedEmpty.replaceAll(x -> x + ".0");
		assertThat(mappedEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> mapped.replaceAll(x -> x + ".0"));
		assertThat(mapped, contains("1", "2", "3", "4", "5"));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void sort() {
		mappedEmpty.sort(Comparator.naturalOrder());
		assertThat(mappedEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> mapped.sort(Comparator.naturalOrder()));
		assertThat(mapped, contains("1", "2", "3", "4", "5"));
		assertThat(original, contains(1, 2, 3, 4, 5));
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
	public void testEquals() {
		assertThat(mappedEmpty.equals(asList()), is(true));
		assertThat(mappedEmpty.equals(asList("1", "2")), is(false));

		assertThat(mapped.equals(asList("1", "2", "3", "4", "5")), is(true));
		assertThat(mapped.equals(asList("1", "17", "3", "4", "5")), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(mappedEmpty.hashCode(), is(1));

		assertThat(mapped.hashCode(), is(75421906));
	}

	@Test
	public void get() {
		assertThat(mapped.get(0), is("1"));
		assertThat(mapped.get(2), is("3"));
		assertThat(mapped.get(4), is("5"));
	}

	@Test
	public void set() {
		expecting(UnsupportedOperationException.class, () -> mapped.set(2, "17"));
		assertThat(mapped, contains("1", "2", "3", "4", "5"));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void addAtIndex() {
		expecting(UnsupportedOperationException.class, () -> mappedEmpty.add(0, "3"));
		assertThat(originalEmpty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> mapped.add(0, "3"));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void indexOf() {
		assertThat(mappedEmpty.indexOf("17"), is(-1));

		assertThat(mapped.indexOf("3"), is(2));
	}

	@Test
	public void lastIndexOf() {
		assertThat(mappedEmpty.lastIndexOf("17"), is(-1));

		assertThat(mapped.lastIndexOf("3"), is(2));
		assertThat(mapped.lastIndexOf("17"), is(-1));
	}

	@Test
	public void listIteratorEmpty() {
		ListIterator<String> emptyIterator = mappedEmpty.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.hasPrevious(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(mappedEmpty, is(emptyIterable()));
		assertThat(originalEmpty, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		ListIterator<String> listIterator = mapped.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(false));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));
		assertThat(listIterator.next(), is("1"));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.next(), is("2"));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.next(), is("3"));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.previous(), is("3"));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.previous(), is("2"));

		expecting(UnsupportedOperationException.class, () -> listIterator.set("17"));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.next(), is("2"));

		expecting(UnsupportedOperationException.class, () -> listIterator.add("17"));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.hasPrevious(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.next(), is("3"));

		assertThat(mapped, contains("1", "2", "3", "4", "5"));
		assertThat(original, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		ListIterator<String> listIterator = mapped.listIterator();

		AtomicInteger i = new AtomicInteger();
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.next(), is(String.valueOf(i.get() + 1)));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(5));

			while (listIterator.hasPrevious()) {
				i.decrementAndGet();
				assertThat(listIterator.previous(), is(String.valueOf(i.get() + 1)));
				assertThat(listIterator.nextIndex(), is(i.get()));
				assertThat(listIterator.previousIndex(), is(i.get() - 1));
			}
			assertThat(i.get(), is(0));
		});
	}

	@Test
	public void subList() {
		List<String> emptySubList = mappedEmpty.subList(0, 0);
		assertThat(emptySubList, is(emptyIterable()));

		List<String> filteredSubList = mapped.subList(2, 4);
		assertThat(filteredSubList, contains("3", "4"));
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