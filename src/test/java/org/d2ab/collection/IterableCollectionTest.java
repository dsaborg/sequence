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

import org.d2ab.iterator.Iterators;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static org.d2ab.collection.SizedIterable.SizeType.UNAVAILABLE;
import static org.d2ab.test.HasSizeCharacteristics.*;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class IterableCollectionTest {
	private final IterableCollection<Integer> empty = IterableCollection.empty();
	private final IterableCollection<Integer> single = IterableCollection.of(1);
	private final IterableCollection<Integer> fixed = IterableCollection.of(1, 2, 3, 4, 5);
	private final IterableCollection<Integer> mutable = IterableCollection.from(
			new ArrayList<>(Lists.of(1, 2, 3, 4, 5)));
	private final IterableCollection<Integer> sized = IterableCollection.from(
			unmodifiableCollection(new ArrayList<>(Lists.of(1, 2, 3, 4, 5))));
	private final IterableCollection<Integer> unsized = IterableCollection.from(
			Iterables.of(1, 2, 3, 4, 5)::iterator);

	@Test
	public void fromSizedIterableAsIterable() {
		IterableCollection<Integer> empty = IterableCollection.from((Iterable<Integer>) new SizedIterable<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int size() {
				return 0;
			}

			@Override
			public boolean isEmpty() {
				return true;
			}
		});
		assertThat(empty.size(), is(0));
		assertThat(empty.sizeType(), is(UNAVAILABLE));
		assertThat(empty.isEmpty(), is(true));

		IterableCollection<Integer> regular = IterableCollection.from((Iterable<Integer>) new SizedIterable<Integer>
				() {
			@Override
			public Iterator<Integer> iterator() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int size() {
				return 10;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}
		});
		assertThat(regular.size(), is(10));
		assertThat(regular.sizeType(), is(UNAVAILABLE));
		assertThat(regular.isEmpty(), is(false));
	}

	@Test
	public void fromCollectionAsIterable() {
		IterableCollection<Integer> empty = IterableCollection.from((Iterable<Integer>) new ArrayList<Integer>());
		assertThat(empty, is(emptySizedIterable()));

		IterableCollection<Integer> regular = IterableCollection.from(
				(Iterable<Integer>) new ArrayList<>(asList(1, 2, 3, 4, 5)));
		assertThat(regular, containsSized(1, 2, 3, 4, 5));
	}

	@Test
	public void fromFixedCollectionAsIterable() {
		IterableCollection<Integer> empty = IterableCollection.from(
				(Iterable<Integer>) Collections.<Integer>emptyList());
		assertThat(empty, is(emptyFixedIterable()));

		IterableCollection<Integer> regular = IterableCollection.from((Iterable<Integer>) asList(1, 2, 3, 4, 5));
		assertThat(regular, containsFixed(1, 2, 3, 4, 5));
	}

	@Test
	public void fromIterable() {
		IterableCollection<Integer> empty = IterableCollection.from(
				(Iterable<Integer>) Iterables.<Integer>empty()::iterator);
		assertThat(empty, is(emptyUnsizedIterable()));

		IterableCollection<Integer> regular = IterableCollection.from(
				(Iterable<Integer>) Iterables.of(1, 2, 3, 4, 5)::iterator);
		assertThat(regular, containsUnsized(1, 2, 3, 4, 5));
	}

	@Test
	public void iteration() {
		assertThat(empty, is(emptyFixedIterable()));
		expecting(NoSuchElementException.class, () -> empty.iterator().next());

		assertThat(single, containsFixed(1));
		Iterator<Integer> singleIterator = single.iterator();
		assertThat(singleIterator.hasNext(), is(true));
		assertThat(singleIterator.next(), is(1));
		assertThat(singleIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, singleIterator::next);

		assertThat(fixed, containsFixed(1, 2, 3, 4, 5));
		assertThat(mutable, containsSized(1, 2, 3, 4, 5));
		assertThat(sized, containsSized(1, 2, 3, 4, 5));
		assertThat(unsized, containsUnsized(1, 2, 3, 4, 5));
	}

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(single.size(), is(1));
		assertThat(fixed.size(), is(5));
		assertThat(mutable.size(), is(5));
		assertThat(sized.size(), is(5));
		assertThat(unsized.size(), is(5));
	}

	@Test
	public void isEmpty() {
		assertThat(empty.isEmpty(), is(true));
		assertThat(single.isEmpty(), is(false));
		assertThat(fixed.isEmpty(), is(false));
		assertThat(mutable.isEmpty(), is(false));
		assertThat(sized.isEmpty(), is(false));
		assertThat(unsized.isEmpty(), is(false));
	}

	@Test
	public void containsObject() {
		assertThat(empty.contains(17), is(false));

		assertThat(single.contains(17), is(false));
		assertThat(single.contains(1), is(true));

		assertThat(fixed.contains(17), is(false));
		for (int i = 1; i <= 5; i++)
			assertThat(fixed.contains(i), is(true));

		assertThat(mutable.contains(17), is(false));
		for (int i = 1; i <= 5; i++)
			assertThat(mutable.contains(i), is(true));

		assertThat(sized.contains(17), is(false));
		for (int i = 1; i <= 5; i++)
			assertThat(sized.contains(i), is(true));

		assertThat(unsized.contains(17), is(false));
		for (int i = 1; i <= 5; i++)
			assertThat(unsized.contains(i), is(true));
	}

	@Test
	public void toArray() {
		assertThat(empty.toArray(), is(emptyArray()));
		assertThat(single.toArray(), is(arrayContaining(1)));
		assertThat(fixed.toArray(), is(arrayContaining(1, 2, 3, 4, 5)));
		assertThat(mutable.toArray(), is(arrayContaining(1, 2, 3, 4, 5)));
		assertThat(sized.toArray(), is(arrayContaining(1, 2, 3, 4, 5)));
		assertThat(unsized.toArray(), is(arrayContaining(1, 2, 3, 4, 5)));
	}

	@Test
	public void toArrayWithEmptyArray() {
		assertThat(empty.toArray(new Integer[0]), is(emptyArray()));
		assertThat(single.toArray(new Integer[0]), is(arrayContaining(1)));
		assertThat(fixed.toArray(new Integer[0]), is(arrayContaining(1, 2, 3, 4, 5)));
		assertThat(mutable.toArray(new Integer[0]), is(arrayContaining(1, 2, 3, 4, 5)));
		assertThat(sized.toArray(new Integer[0]), is(arrayContaining(1, 2, 3, 4, 5)));
		assertThat(unsized.toArray(new Integer[0]), is(arrayContaining(1, 2, 3, 4, 5)));
	}

	@Test
	public void toArrayWithSmallerArray() {
		assertThat(empty.toArray(new Integer[0]), is(emptyArray()));
		assertThat(single.toArray(new Integer[0]), is(arrayContaining(1)));
		assertThat(fixed.toArray(new Integer[3]), is(arrayContaining(1, 2, 3, 4, 5)));
		assertThat(mutable.toArray(new Integer[3]), is(arrayContaining(1, 2, 3, 4, 5)));
		assertThat(sized.toArray(new Integer[3]), is(arrayContaining(1, 2, 3, 4, 5)));
		assertThat(unsized.toArray(new Integer[3]), is(arrayContaining(1, 2, 3, 4, 5)));
		assertThat(fixedOfSize(3, 1, 2, 3, 4, 5).toArray(new Integer[3]), is(arrayContaining(1, 2, 3, 4, 5)));
		assertThat(fixedOfSize(10, 1, 2, 3, 4, 5).toArray(new Integer[3]), is(arrayContaining(1, 2, 3, 4, 5)));
	}

	@Test
	public void toArrayWithLargerArray() {
		assertThat(empty.toArray(Arrayz.fill(new Integer[2], 17)), is(arrayContaining(null, 17)));
		assertThat(single.toArray(Arrayz.fill(new Integer[3], 17)), is(arrayContaining(1, null, 17)));
		assertThat(fixed.toArray(Arrayz.fill(new Integer[7], 17)), is(arrayContaining(1, 2, 3, 4, 5, null, 17)));
		assertThat(mutable.toArray(Arrayz.fill(new Integer[7], 17)), is(arrayContaining(1, 2, 3, 4, 5, null, 17)));
		assertThat(sized.toArray(Arrayz.fill(new Integer[7], 17)), is(arrayContaining(1, 2, 3, 4, 5, null, 17)));
		assertThat(unsized.toArray(Arrayz.fill(new Integer[7], 17)), is(arrayContaining(1, 2, 3, 4, 5, null, 17)));
		assertThat(fixedOfSize(3, 1, 2, 3, 4, 5).toArray(Arrayz.fill(new Integer[7], 17)), is(arrayContaining(1, 2, 3, 4, 5, null, 17)));
		assertThat(fixedOfSize(10, 1, 2, 3, 4, 5).toArray(Arrayz.fill(new Integer[7], 17)), is(arrayContaining(1, 2, 3, 4, 5, null, 17)));
	}

	public static IterableCollection<Integer> fixedOfSize(int size, Integer... items) {
		return new IterableCollection<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				return Iterators.of(items);
			}

			@Override
			public int size() {
				return size;
			}

			@Override
			public SizeType sizeType() {
				return SizeType.FIXED;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}
		};
	}

	@Test
	public void add() {
		expecting(UnsupportedOperationException.class, () -> empty.add(17));
		expecting(UnsupportedOperationException.class, () -> single.add(17));
		expecting(UnsupportedOperationException.class, () -> fixed.add(17));
		expecting(UnsupportedOperationException.class, () -> mutable.add(17));
		expecting(UnsupportedOperationException.class, () -> sized.add(17));
		expecting(UnsupportedOperationException.class, () -> unsized.add(17));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(17), is(false));

		expecting(UnsupportedOperationException.class, () -> single.remove(1));
		expecting(UnsupportedOperationException.class, () -> fixed.remove(1));

		assertThat(mutable.remove(1), is(true));
		assertThat(mutable, contains(2, 3, 4, 5));

		expecting(UnsupportedOperationException.class, () -> sized.remove(1));

		expecting(UnsupportedOperationException.class, () -> unsized.remove(1));
	}

	@Test
	public void containsAllCollection() {
		assertThat(empty.containsAll(Lists.of(17, 18, 19)), is(false));

		assertThat(single.containsAll(Lists.of(1)), is(true));
		assertThat(single.containsAll(Lists.of(1, 2)), is(false));

		assertThat(fixed.containsAll(Lists.of(1, 2, 3, 4, 5)), is(true));
		assertThat(fixed.containsAll(Lists.of(1, 2, 3, 4, 5, 6)), is(false));

		assertThat(mutable.containsAll(Lists.of(1, 2, 3, 4, 5)), is(true));
		assertThat(mutable.containsAll(Lists.of(1, 2, 3, 4, 5, 6)), is(false));

		assertThat(sized.containsAll(Lists.of(1, 2, 3, 4, 5)), is(true));
		assertThat(sized.containsAll(Lists.of(1, 2, 3, 4, 5, 6)), is(false));

		assertThat(unsized.containsAll(Lists.of(1, 2, 3, 4, 5)), is(true));
		assertThat(unsized.containsAll(Lists.of(1, 2, 3, 4, 5, 6)), is(false));
	}

	@Test
	public void addAll() {
		expecting(UnsupportedOperationException.class, () -> empty.addAll(Lists.of(17, 18, 19)));
		expecting(UnsupportedOperationException.class, () -> single.addAll(Lists.of(17, 18, 19)));
		expecting(UnsupportedOperationException.class, () -> fixed.addAll(Lists.of(17, 18, 19)));
		expecting(UnsupportedOperationException.class, () -> mutable.addAll(Lists.of(17, 18, 19)));
		expecting(UnsupportedOperationException.class, () -> sized.addAll(Lists.of(17, 18, 19)));
		expecting(UnsupportedOperationException.class, () -> unsized.addAll(Lists.of(17, 18, 19)));
	}

	@Test
	public void removeAll() {
		assertThat(empty.removeAll(Lists.of(1, 2)), is(false));

		expecting(UnsupportedOperationException.class, () -> single.removeAll(Lists.of(1, 2)));
		expecting(UnsupportedOperationException.class, () -> fixed.removeAll(Lists.of(1, 2)));

		assertThat(mutable.removeAll(Lists.of(17, 18)), is(false));
		assertThat(mutable, contains(1, 2, 3, 4, 5));

		assertThat(mutable.removeAll(Lists.of(1, 2)), is(true));
		assertThat(mutable, contains(3, 4, 5));

		expecting(UnsupportedOperationException.class, () -> sized.removeAll(Lists.of(1, 2)));

		expecting(UnsupportedOperationException.class, () -> unsized.removeAll(Lists.of(1, 2)));
	}

	@Test
	public void retainAll() {
		assertThat(empty.retainAll(Lists.of(1, 2)), is(false));

		assertThat(single.retainAll(Lists.of(1)), is(false));
		assertThat(fixed.retainAll(Lists.of(1, 2, 3, 4, 5)), is(false));

		expecting(UnsupportedOperationException.class, () -> single.retainAll(Lists.of(2)));
		expecting(UnsupportedOperationException.class, () -> fixed.retainAll(Lists.of(6)));

		assertThat(mutable.retainAll(Lists.of(1, 2, 3, 4, 5)), is(false));
		assertThat(mutable, contains(1, 2, 3, 4, 5));

		assertThat(mutable.retainAll(Lists.of(1, 2)), is(true));
		assertThat(mutable, contains(1, 2));

		expecting(UnsupportedOperationException.class, () -> sized.retainAll(Lists.of(6)));

		expecting(UnsupportedOperationException.class, () -> unsized.retainAll(Lists.of(6)));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x == 3), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(mutable.removeIf(x -> x == 3), is(true));
		twice(() -> assertThat(mutable, contains(1, 2, 4, 5)));
	}

	@Test
	public void retainIf() {
		assertThat(empty.retainIf(x -> x == 3), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(mutable.retainIf(x -> x == 3), is(true));
		twice(() -> assertThat(mutable, contains(3)));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, single::clear);
		expecting(UnsupportedOperationException.class, fixed::clear);

		mutable.clear();
		assertThat(mutable, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, sized::clear);

		expecting(UnsupportedOperationException.class, unsized::clear);
	}
}