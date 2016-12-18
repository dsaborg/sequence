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

package org.d2ab.collection.ints;

import org.d2ab.iterator.ints.ExclusiveTerminalIntIterator;
import org.d2ab.iterator.ints.InclusiveStartingIntIterator;
import org.d2ab.iterator.ints.IntIterator;

import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A primitive specialization of {@link SortedSet} for {@code int} values.
 */
public interface IntSortedSet extends SortedSet<Integer>, IntSet {
	/**
	 * @return a new empty mutable {@code IntSortedSet}.
	 *
	 * @since 2.1
	 */
	static IntSortedSet create() {
		return new BitIntSet();
	}

	/**
	 * @return a new mutable {@code IntSortedSet} with a copy of the given elements.
	 *
	 * @since 2.1
	 */
	static IntSortedSet create(int... xs) {
		return new BitIntSet(xs);
	}

	@Override
	default IntComparator comparator() {
		return null;
	}

	@Override
	default IntSortedSet subSet(Integer from, Integer to) {
		return subSet((int) from, (int) to);
	}

	default IntSortedSet subSet(int from, int to) {
		return new SubSet(this) {
			@Override
			public IntIterator iterator() {
				return untilExcluded(fromIncluded(IntSortedSet.this.iterator()));
			}

			@Override
			protected boolean included(int x) {
				return x >= from && x < to;
			}
		};
	}

	@Override
	default IntSortedSet headSet(Integer to) {
		return headSet((int) to);
	}

	default IntSortedSet headSet(int to) {
		return new SubSet(this) {
			@Override
			public IntIterator iterator() {
				return untilExcluded(IntSortedSet.this.iterator());
			}

			@Override
			public int firstInt() {
				return IntSortedSet.this.firstInt();
			}

			@Override
			protected boolean included(int x) {
				return x < to;
			}
		};
	}

	@Override
	default IntSortedSet tailSet(Integer from) {
		return tailSet((int) from);
	}

	default IntSortedSet tailSet(int from) {
		return new SubSet(this) {
			@Override
			public IntIterator iterator() {
				return fromIncluded(IntSortedSet.this.iterator());
			}

			@Override
			public int lastInt() {
				return IntSortedSet.this.lastInt();
			}

			@Override
			protected boolean included(int x) {
				return x >= from;
			}
		};
	}

	@Override
	default Integer first() {
		return firstInt();
	}

	default int firstInt() {
		return iterator().nextInt();
	}

	@Override
	default Integer last() {
		return lastInt();
	}

	default int lastInt() {
		IntIterator iterator = iterator();
		int last;
		do
			last = iterator.nextInt();
		while (iterator.hasNext());
		return last;
	}

	@Override
	default Spliterator.OfInt spliterator() {
		return Spliterators.spliterator(iterator(), size(),
		                                Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED |
		                                Spliterator.NONNULL);
	}

	abstract class SubSet extends IntSet.Base implements IntSortedSet {
		private final IntSortedSet set;

		public SubSet(IntSortedSet set) {
			this.set = set;
		}

		@Override
		public int size() {
			return iterator().count();
		}

		@Override
		public boolean containsInt(int x) {
			return included(x) && set.containsInt(x);
		}

		@Override
		public boolean removeInt(int x) {
			return included(x) && set.removeInt(x);
		}

		@Override
		public boolean addInt(int x) {
			if (excluded(x))
				throw new IllegalArgumentException(String.valueOf(x));

			return set.addInt(x);
		}

		protected IntIterator untilExcluded(IntIterator iterator) {
			return new ExclusiveTerminalIntIterator(iterator, this::excluded);
		}

		protected IntIterator fromIncluded(IntIterator iterator) {
			return new InclusiveStartingIntIterator(iterator, this::included);
		}

		protected abstract boolean included(int x);

		protected boolean excluded(int x) {
			return !included(x);
		}
	}
}
