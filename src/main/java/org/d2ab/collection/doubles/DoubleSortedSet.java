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

package org.d2ab.collection.doubles;

import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.iterator.doubles.ExclusiveTerminalDoubleIterator;
import org.d2ab.iterator.doubles.InclusiveStartingDoubleIterator;

import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A primitive specialization of {@link SortedSet} for {@code double} values.
 */
public interface DoubleSortedSet extends SortedSet<Double>, DoubleSet {
	/**
	 * @return a new empty mutable {@code DoubleSortedSet}.
	 *
	 * @since 2.1
	 */
	static DoubleSortedSet create() {
		return new SortedListDoubleSet();
	}

	/**
	 * @return a new mutable {@code DoubleSortedSet} with a copy of the given elements.
	 *
	 * @since 2.1
	 */
	static DoubleSortedSet create(double... xs) {
		return new SortedListDoubleSet(xs);
	}

	@Override
	default DoubleComparator comparator() {
		return null;
	}

	@Override
	default DoubleSortedSet subSet(Double from, Double to) {
		return subSetExactly((double) from, (double) to);
	}

	default DoubleSortedSet subSetExactly(double from, double to) {
		return new DoubleSortedSet.SubSet(this) {
			@Override
			public DoubleIterator iterator() {
				return untilExcluded(fromIncluded(DoubleSortedSet.this.iterator()));
			}

			@Override
			protected boolean included(double x) {
				return x >= from && x < to;
			}
		};
	}

	default DoubleSortedSet subSet(double from, double to, double precision) {
		return new DoubleSortedSet.SubSet(this) {
			@Override
			public DoubleIterator iterator() {
				return untilExcluded(fromIncluded(DoubleSortedSet.this.iterator()));
			}

			@Override
			protected boolean included(double x) {
				return DoubleComparator.ge(x, from, precision) && DoubleComparator.lt(x, to, precision);
			}
		};
	}

	@Override
	default DoubleSortedSet headSet(Double to) {
		return headSetExactly((double) to);
	}

	default DoubleSortedSet headSetExactly(double to) {
		return new DoubleSortedSet.SubSet(this) {
			@Override
			public DoubleIterator iterator() {
				return untilExcluded(DoubleSortedSet.this.iterator());
			}

			@Override
			public double firstDouble() {
				return DoubleSortedSet.this.firstDouble();
			}

			@Override
			protected boolean included(double x) {
				return x < to;
			}
		};
	}

	default DoubleSortedSet headSet(double to, double precision) {
		return new DoubleSortedSet.SubSet(this) {
			@Override
			public DoubleIterator iterator() {
				return untilExcluded(DoubleSortedSet.this.iterator());
			}

			@Override
			public double firstDouble() {
				return DoubleSortedSet.this.firstDouble();
			}

			@Override
			protected boolean included(double x) {
				return DoubleComparator.lt(x, to, precision);
			}
		};
	}

	@Override
	default DoubleSortedSet tailSet(Double from) {
		return tailSetExactly((double) from);
	}

	default DoubleSortedSet tailSetExactly(double from) {
		return new DoubleSortedSet.SubSet(this) {
			@Override
			public DoubleIterator iterator() {
				return fromIncluded(DoubleSortedSet.this.iterator());
			}

			@Override
			public double lastDouble() {
				return DoubleSortedSet.this.lastDouble();
			}

			@Override
			protected boolean included(double x) {
				return x >= from;
			}
		};
	}

	default DoubleSortedSet tailSet(double from, double precision) {
		return new DoubleSortedSet.SubSet(this) {
			@Override
			public DoubleIterator iterator() {
				return fromIncluded(DoubleSortedSet.this.iterator());
			}

			@Override
			public double lastDouble() {
				return DoubleSortedSet.this.lastDouble();
			}

			@Override
			protected boolean included(double x) {
				return DoubleComparator.ge(x, from, precision);
			}
		};
	}

	@Override
	default Double first() {
		return firstDouble();
	}

	default double firstDouble() {
		return iterator().nextDouble();
	}

	@Override
	default Double last() {
		return lastDouble();
	}

	default double lastDouble() {
		DoubleIterator iterator = iterator();
		double last;
		do
			last = iterator.nextDouble();
		while (iterator.hasNext());
		return last;
	}

	@Override
	default Spliterator.OfDouble spliterator() {
		return Spliterators.spliterator(iterator(), size(),
		                                Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED |
		                                Spliterator.NONNULL);
	}

	abstract class SubSet extends DoubleSet.Base implements DoubleSortedSet {
		private DoubleSortedSet set;

		public SubSet(DoubleSortedSet set) {
			this.set = set;
		}

		@Override
		public int size() {
			return iterator().count();
		}

		@Override
		public boolean containsDoubleExactly(double x) {
			return included(x) && set.containsDoubleExactly(x);
		}

		@Override
		public boolean containsDouble(double x, double precision) {
			return included(x) && set.containsDouble(x, precision);
		}

		@Override
		public boolean removeDoubleExactly(double x) {
			return included(x) && set.removeDoubleExactly(x);
		}

		@Override
		public boolean removeDouble(double x, double precision) {
			return included(x) && set.removeDouble(x, precision);
		}

		@Override
		public boolean addDoubleExactly(double x) {
			if (excluded(x))
				throw new IllegalArgumentException(String.valueOf(x));

			return set.addDoubleExactly(x);
		}

		@Override
		public boolean addDouble(double x, double precision) {
			if (excluded(x))
				throw new IllegalArgumentException(String.valueOf(x));

			return set.addDouble(x, precision);
		}

		protected DoubleIterator untilExcluded(DoubleIterator iterator) {
			return new ExclusiveTerminalDoubleIterator(iterator, this::excluded);
		}

		protected DoubleIterator fromIncluded(DoubleIterator iterator) {
			return new InclusiveStartingDoubleIterator(iterator, this::included);
		}

		protected abstract boolean included(double x);

		protected boolean excluded(double x) {
			return !included(x);
		}
	}
}
