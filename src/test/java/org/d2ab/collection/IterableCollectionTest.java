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

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class IterableCollectionTest {
	private final IterableCollection<Integer> empty = IterableCollection.empty();
	private final IterableCollection<Integer> single = IterableCollection.of(1);
	private final IterableCollection<Integer> regular = IterableCollection.of(1, 2, 3, 4, 5);
	private final IterableCollection<Integer> mutable = IterableCollection.from(new ArrayList<>(asList(1, 2, 3, 4,
	                                                                                                   5)));
	@Test
	public void iteration() {
		assertThat(empty, is(emptyIterable()));
		assertThat(single, contains(1));
		assertThat(regular, contains(1, 2, 3, 4, 5));
		assertThat(mutable, contains(1, 2, 3, 4, 5));
	}

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(single.size(), is(1));
		assertThat(regular.size(), is(5));
		assertThat(mutable.size(), is(5));
	}

	@Test
	public void isEmpty() {
		assertThat(empty.isEmpty(), is(true));
		assertThat(single.isEmpty(), is(false));
		assertThat(regular.isEmpty(), is(false));
		assertThat(mutable.isEmpty(), is(false));
	}

	@Test
	public void containsObject() {
		assertThat(empty.contains(17), is(false));

		assertThat(single.contains(17), is(false));
		assertThat(single.contains(1), is(true));

		assertThat(regular.contains(17), is(false));
		for (int i = 1; i <= 5; i++)
			assertThat(regular.contains(i), is(true));

		assertThat(mutable.contains(17), is(false));
		for (int i = 1; i <= 5; i++)
			assertThat(mutable.contains(i), is(true));
	}

	@Test
	public void toArray() {
		assertThat(empty.toArray(), is(emptyArray()));
		assertThat(single.toArray(), is(arrayContaining(1)));
		assertThat(regular.toArray(), is(arrayContaining(1, 2, 3, 4, 5)));
		assertThat(mutable.toArray(), is(arrayContaining(1, 2, 3, 4, 5)));
	}

	@Test
	public void toArrayOfType() {
		assertThat(empty.toArray(new Integer[0]), is(emptyArray()));
		assertThat(single.toArray(new Integer[0]), is(arrayContaining(1)));
		assertThat(regular.toArray(new Integer[0]), is(arrayContaining(1, 2, 3, 4, 5)));
		assertThat(mutable.toArray(new Integer[0]), is(arrayContaining(1, 2, 3, 4, 5)));
	}

	@Test
	public void add() {
		expecting(UnsupportedOperationException.class, () -> empty.add(17));
		expecting(UnsupportedOperationException.class, () -> single.add(17));
		expecting(UnsupportedOperationException.class, () -> regular.add(17));
		expecting(UnsupportedOperationException.class, () -> mutable.add(17));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17), is(false));

		expecting(UnsupportedOperationException.class, () -> single.remove(1));
		expecting(UnsupportedOperationException.class, () -> regular.remove(1));

		assertThat(mutable.remove(1), is(true));
		assertThat(mutable, contains(2, 3, 4, 5));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(asList(17, 18, 19)), is(false));

		assertThat(single.containsAll(asList(1)), is(true));
		assertThat(single.containsAll(asList(1, 2)), is(false));

		assertThat(regular.containsAll(asList(1, 2, 3, 4, 5)), is(true));
		assertThat(regular.containsAll(asList(1, 2, 3, 4, 5, 6)), is(false));

		assertThat(mutable.containsAll(asList(1, 2, 3, 4, 5)), is(true));
		assertThat(mutable.containsAll(asList(1, 2, 3, 4, 5, 6)), is(false));
	}

	@Test
	public void addAll() {
		expecting(UnsupportedOperationException.class, () -> empty.addAll(asList(17, 18, 19)));
		expecting(UnsupportedOperationException.class, () -> single.addAll(asList(17, 18, 19)));
		expecting(UnsupportedOperationException.class, () -> regular.addAll(asList(17, 18, 19)));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(asList(1, 2)), is(false));

		expecting(UnsupportedOperationException.class, () -> single.removeAll(asList(1, 2)));
		expecting(UnsupportedOperationException.class, () -> regular.removeAll(asList(1, 2)));

		assertThat(mutable.removeAll(asList(17, 18)), is(false));
		assertThat(mutable, contains(1, 2, 3, 4, 5));

		assertThat(mutable.removeAll(asList(1, 2)), is(true));
		assertThat(mutable, contains(3, 4, 5));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(asList(1, 2)), is(false));

		assertThat(single.retainAll(asList(1)), is(false));
		assertThat(regular.retainAll(asList(1, 2, 3, 4, 5)), is(false));

		expecting(UnsupportedOperationException.class, () -> single.retainAll(asList(2)));
		expecting(UnsupportedOperationException.class, () -> regular.retainAll(asList(6)));

		assertThat(mutable.retainAll(asList(1, 2, 3, 4, 5)), is(false));
		assertThat(mutable, contains(1, 2, 3, 4, 5));

		assertThat(mutable.retainAll(asList(1, 2)), is(true));
		assertThat(mutable, contains(1, 2));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, single::clear);
		expecting(UnsupportedOperationException.class, regular::clear);

		mutable.clear();
		assertThat(mutable, is(emptyIterable()));
	}
}