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

package org.d2ab.collection.longs;

import org.d2ab.iterator.longs.ExclusiveTerminalLongIterator;
import org.d2ab.iterator.longs.InclusiveStartingLongIterator;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.util.Strict;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A primitive specialization of {@link SortedSet} for {@code long} values.
 */
public interface LongSortedSet extends SortedSet<Long>, LongSet {
	/**
	 * @return a new empty mutable {@code LongSortedSet}.
	 *
	 * @since 2.1
	 */
	static LongSortedSet create() {
		return new BitLongSet();
	}

	/**
	 * @return a new mutable {@code LongSortedSet} with a copy of the given elements.
	 *
	 * @since 2.1
	 */
	static LongSortedSet create(long... xs) {
		return new BitLongSet(xs);
	}

	@Override
	default Comparator<Long> comparator() {
		return null;
	}

	@Override
	default LongSortedSet subSet(Long from, Long to) {
		Strict.check();

		return subSet((long) from, (long) to);
	}

	default LongSortedSet subSet(long from, long to) {
		return new LongSortedSet.SubSet(this) {
			@Override
			public LongIterator iterator() {
				return untilExcluded(fromIncluded(LongSortedSet.this.iterator()));
			}

			@Override
			protected boolean included(long x) {
				return x >= from && x < to;
			}
		};
	}

	@Override
	default LongSortedSet headSet(Long to) {
		Strict.check();

		return headSet((long) to);
	}

	default LongSortedSet headSet(long to) {
		return new LongSortedSet.SubSet(this) {
			@Override
			public LongIterator iterator() {
				return untilExcluded(LongSortedSet.this.iterator());
			}

			@Override
			public long firstLong() {
				return LongSortedSet.this.firstLong();
			}

			@Override
			protected boolean included(long x) {
				return x < to;
			}
		};
	}

	@Override
	default LongSortedSet tailSet(Long from) {
		Strict.check();

		return tailSet((long) from);
	}

	default LongSortedSet tailSet(long from) {
		return new LongSortedSet.SubSet(this) {
			@Override
			public LongIterator iterator() {
				return fromIncluded(LongSortedSet.this.iterator());
			}

			@Override
			public long lastLong() {
				return LongSortedSet.this.lastLong();
			}

			@Override
			protected boolean included(long x) {
				return x >= from;
			}
		};
	}

	@Override
	default Long first() {
		Strict.check();

		return firstLong();
	}

	default long firstLong() {
		return iterator().nextLong();
	}

	@Override
	default Long last() {
		Strict.check();

		return lastLong();
	}

	default long lastLong() {
		LongIterator iterator = iterator();
		long last;
		do
			last = iterator.nextLong();
		while (iterator.hasNext());
		return last;
	}

	@Override
	default Spliterator.OfLong spliterator() {
		return Spliterators.spliterator(iterator(), size(),
		                                Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED |
		                                Spliterator.NONNULL);
	}

	abstract class Base extends LongSet.Base implements LongSortedSet {
		public static LongSortedSet create(long... longs) {
			return from(LongSortedSet.create(longs));
		}

		public static LongSortedSet from(final LongCollection collection) {
			return new LongSortedSet.Base() {
				@Override
				public LongIterator iterator() {
					return collection.iterator();
				}

				@Override
				public int size() {
					return collection.size();
				}

				@Override
				public boolean addLong(long x) {
					return collection.addLong(x);
				}
			};
		}
	}

	abstract class SubSet extends Base {
		private final LongSortedSet set;

		public SubSet(LongSortedSet set) {
			this.set = set;
		}

		@Override
		public int size() {
			return iterator().size();
		}

		@Override
		public boolean containsLong(long x) {
			return included(x) && set.containsLong(x);
		}

		@Override
		public boolean removeLong(long x) {
			return included(x) && set.removeLong(x);
		}

		@Override
		public boolean addLong(long x) {
			if (excluded(x))
				throw new IllegalArgumentException(String.valueOf(x));

			return set.addLong(x);
		}

		protected LongIterator untilExcluded(LongIterator iterator) {
			return new ExclusiveTerminalLongIterator(iterator, this::excluded);
		}

		protected LongIterator fromIncluded(LongIterator iterator) {
			return new InclusiveStartingLongIterator(iterator, this::included);
		}

		protected abstract boolean included(long x);

		protected boolean excluded(long x) {
			return !included(x);
		}
	}
}
