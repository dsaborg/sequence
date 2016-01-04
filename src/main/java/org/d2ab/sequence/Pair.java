/*
 * Copyright 2015 Daniel Skogquist Åborg
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
package org.d2ab.sequence;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Pair<L, R> extends Entry<L, R> {
	static <T, U> Pair<T, U> of(@Nullable T left, @Nullable U right) {
		return new Base<T, U>() {
			@Override
			public T getLeft() {
				return left;
			}

			@Override
			public U getRight() {
				return right;
			}
		};
	}

	static <K, V> Pair<K, V> from(Entry<? extends K, ? extends V> entry) {
		return new Base<K, V>() {
			@Override
			public K getLeft() {
				return entry.getKey();
			}

			@Override
			public V getRight() {
				return entry.getValue();
			}
		};
	}

	static <T> Pair<T, T> unary(@Nullable T item) {
		return new Base<T, T>() {
			@Override
			public T getLeft() {
				return item;
			}

			@Override
			public T getRight() {
				return item;
			}
		};
	}

	@Override
	default L getKey() {
		return getLeft();
	}

	L getLeft();

	@Override
	default R getValue() {
		return getRight();
	}

	R getRight();

	@Override
	default R setValue(R value) {
		throw new UnsupportedOperationException();
	}

	default Pair<R, L> swap() {
		return new Base<R, L>() {
			@Override
			public R getLeft() {
				return Pair.this.getRight();
			}

			@Override
			public L getRight() {
				return Pair.this.getLeft();
			}
		};
	}

	default <LL> Pair<LL, R> withLeft(LL left) {
		return new Base<LL, R>() {
			@Override
			public LL getLeft() {
				return left;
			}

			@Override
			public R getRight() {
				return Pair.this.getRight();
			}
		};
	}

	default <RR> Pair<L, RR> withRight(RR right) {
		return new Base<L, RR>() {
			@Override
			public L getLeft() {
				return Pair.this.getLeft();
			}

			@Override
			public RR getRight() {
				return right;
			}
		};
	}

	default <LL> Pair<LL, L> shiftRight(LL replacement) {
		return new Base<LL, L>() {
			@Override
			public LL getLeft() {
				return replacement;
			}

			@Override
			public L getRight() {
				return Pair.this.getLeft();
			}
		};
	}

	default <RR> Pair<R, RR> shiftLeft(RR replacement) {
		return new Base<R, RR>() {
			@Override
			public R getLeft() {
				return Pair.this.getRight();
			}

			@Override
			public RR getRight() {
				return replacement;
			}
		};
	}

	@Nonnull
	default <LL, RR> Pair<LL, RR> map(@Nonnull Function<? super L, ? extends LL> leftMapper,
	                                  @Nonnull Function<? super R, ? extends RR> rightMapper) {
		return new Base<LL, RR>() {
			@Override
			public LL getLeft() {
				return leftMapper.apply(Pair.this.getLeft());
			}

			@Override
			public RR getRight() {
				return rightMapper.apply(Pair.this.getRight());
			}
		};
	}

	@Nonnull
	default <LL, RR> Pair<LL, RR> map(@Nonnull BiFunction<? super L, ? super R, ? extends Pair<LL, RR>> mapper) {
		return mapper.apply(getLeft(), getRight());
	}

	default <T> T apply(@Nonnull BiFunction<? super L, ? super R, ? extends T> function) {
		return function.apply(getLeft(), getRight());
	}

	default boolean test(@Nonnull Predicate<? super L> leftPredicate, @Nonnull Predicate<? super R> rightPredicate) {
		return leftPredicate.test(getLeft()) && rightPredicate.test(getRight());
	}

	default boolean test(@Nonnull BiPredicate<? super L, ? super R> predicate) {
		return predicate.test(getLeft(), getRight());
	}

	default Map<L, R> putInto(@Nonnull Map<L, R> map) {
		map.put(getLeft(), getRight());
		return map;
	}

	default <T> Iterator<T> iterator() {
		@SuppressWarnings("unchecked") PairIterator<?, ?, T> pairIterator = new PairIterator(this);
		return pairIterator;
	}

	abstract class Base<L, R> implements Pair<L, R> {
		@Override
		public int hashCode() {
			int result = (getLeft() != null) ? getLeft().hashCode() : 0;
			result = (31 * result) + ((getRight() != null) ? getRight().hashCode() : 0);
			return result;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Pair))
				return false;

			Pair<?, ?> that = (Pair<?, ?>) o;

			return ((getLeft() != null) ? getLeft().equals(that.getLeft()) : (that.getLeft() == null)) &&
			       ((getRight() != null) ? getRight().equals(that.getRight()) : (that.getRight() == null));
		}

		@Override
		public String toString() {
			return "(" + getLeft() + ',' + getRight() + ')';
		}
	}

	class PairIterator<L extends T, R extends T, T> implements Iterator<T> {
		private final Pair<L, R> pair;
		int index;

		public PairIterator(Pair<L, R> pair) {
			this.pair = pair;
		}

		@Override
		public boolean hasNext() {
			return index < 2;
		}

		@Override
		public T next() {
			if (!hasNext())
				throw new NoSuchElementException();
			switch (++index) {
				case 1:
					return pair.getLeft();
				case 2:
					return pair.getRight();
				default:
					// Can't happen due to above check
					throw new IllegalStateException();
			}
		}
	}
}
