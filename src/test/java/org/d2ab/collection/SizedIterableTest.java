package org.d2ab.collection;

import org.d2ab.iterator.Iterators;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.d2ab.collection.SizedIterable.SizeType.*;
import static org.d2ab.test.HasSizeCharacteristics.*;
import static org.d2ab.test.Tests.expecting;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class SizedIterableTest {
	@Test
	public void fromSizedIterable() {
		SizedIterable<Integer> originalEmpty = new SizedIterable<Integer>() {
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
		};
		SizedIterable<Integer> empty = SizedIterable.from(originalEmpty);
		assertThat(empty, is(sameInstance(originalEmpty)));

		SizedIterable<Integer> originalSized = new SizedIterable<Integer>() {
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
		};
		SizedIterable<Integer> regular = SizedIterable.from(originalSized);
		assertThat(regular, is(sameInstance(originalSized)));
	}

	@Test
	public void fromCollectionAsIterable() {
		SizedIterable<Integer> empty = SizedIterable.from((Iterable<Integer>) Collections.<Integer>emptyList());
		assertThat(empty, is(emptySizedIterable()));

		SizedIterable<Integer> regular = SizedIterable.from((Iterable<Integer>) asList(1, 2, 3, 4, 5));
		assertThat(regular, containsSized(1, 2, 3, 4, 5));
	}

	@Test
	public void fromIterable() {
		SizedIterable<Integer> empty = SizedIterable.from(Iterables.<Integer>empty()::iterator);
		assertThat(empty, is(emptyUnsizedIterable()));

		SizedIterable<Integer> regular = SizedIterable.from(Iterables.of(1, 2, 3, 4, 5)::iterator);
		assertThat(regular, containsUnsized(1, 2, 3, 4, 5));
	}

	@Test
	public void defaultSizeAvailableSize() {
		SizedIterable<Integer> iterable = new SizedIterable<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				throw new AssertionError();
			}

			@Override
			public SizeType sizeType() {
				return AVAILABLE;
			}
		};

		expecting(IllegalStateException.class, iterable::size);
	}

	@Test
	public void defaultSizeUnavailableSize() {
		SizedIterable<Integer> iterable = new SizedIterable<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				return Iterators.of(1, 2, 3, 4, 5);
			}

			@Override
			public SizeType sizeType() {
				return UNAVAILABLE;
			}
		};

		assertThat(iterable.size(), is(5));
	}

	@Test
	public void defaultSizeInfiniteSize() {
		SizedIterable<Integer> iterable = new SizedIterable<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				throw new AssertionError();
			}

			@Override
			public SizeType sizeType() {
				return INFINITE;
			}
		};

		expecting(UnsupportedOperationException.class, iterable::size);
	}
}