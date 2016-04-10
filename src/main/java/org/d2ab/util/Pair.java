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

package org.d2ab.util;

import org.d2ab.function.Functions;
import org.d2ab.function.QuaternaryFunction;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.function.*;

import static java.util.Comparator.comparing;
import static org.d2ab.util.Comparators.naturalOrderNullsFirst;

public interface Pair<L, R> extends Entry<L, R>, Comparable<Pair<L, R>> {
	static <T, U> Pair<T, U> of(T left, U right) {
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

	static <T> Pair<T, T> unary(T item) {
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

	static <K, V> UnaryOperator<Pair<K, V>> asUnaryOperator(BiFunction<? super K, ? super V, ? extends Pair<K, V>>
			                                                        op) {
		return entry -> op.apply(entry.getLeft(), entry.getRight());
	}

	static <K, V, KK, VV> UnaryOperator<Pair<KK, VV>> asUnaryOperator(BiFunction<? super K, ? super V, ? extends
			Pair<KK, VV>> f, BiFunction<? super KK, ? super VV, ? extends
			Pair<K, V>> g) {

		Function<? super Pair<K, V>, ? extends Pair<KK, VV>> f1 = asFunction(f);
		Function<? super Pair<KK, VV>, ? extends Pair<K, V>> g1 = asFunction(g);
		return Functions.toUnaryOperator(f1, g1);
	}

	static <K, V> BinaryOperator<Pair<K, V>> asBinaryOperator(QuaternaryFunction<K, V, K, V, Pair<K, V>> f) {
		return (e1, e2) -> f.apply(e1.getLeft(), e1.getRight(), e2.getLeft(), e2.getRight());
	}

	static <K, V, R> Function<? super Pair<K, V>, ? extends R> asFunction(BiFunction<? super K, ? super V, ? extends
			R> mapper) {
		return entry -> mapper.apply(entry.getLeft(), entry.getRight());
	}

	static <K, V> Predicate<? super Pair<K, V>> asPredicate(BiPredicate<? super K, ? super V> predicate) {
		return entry -> predicate.test(entry.getLeft(), entry.getRight());
	}

	static <K, V> Consumer<? super Pair<K, V>> asConsumer(BiConsumer<? super K, ? super V> action) {
		return entry -> action.accept(entry.getLeft(), entry.getRight());
	}

	L getLeft();

	R getRight();

	@Override
	default L getKey() {
		return getLeft();
	}

	@Override
	default R getValue() {
		return getRight();
	}

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

	default <LL, RR> Pair<LL, RR> map(Function<? super L, ? extends LL> leftMapper,
	                                  Function<? super R, ? extends RR> rightMapper) {
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

	default <LL, RR> Pair<LL, RR> map(BiFunction<? super L, ? super R, ? extends Pair<LL, RR>> mapper) {
		return mapper.apply(getLeft(), getRight());
	}

	default <T> T apply(BiFunction<? super L, ? super R, ? extends T> function) {
		return function.apply(getLeft(), getRight());
	}

	default boolean test(Predicate<? super L> leftPredicate, Predicate<? super R> rightPredicate) {
		return leftPredicate.test(getLeft()) && rightPredicate.test(getRight());
	}

	default boolean test(BiPredicate<? super L, ? super R> predicate) {
		return predicate.test(getLeft(), getRight());
	}

	default Map<L, R> put(Map<L, R> map) {
		map.put(getLeft(), getRight());
		return map;
	}

	default <T> Iterator<T> iterator() {
		@SuppressWarnings("unchecked")
		PairIterator<?, ?, T> pairIterator = new PairIterator(this);
		return pairIterator;
	}

	abstract class Base<L, R> implements Pair<L, R> {
		@SuppressWarnings("unchecked")
		private static final Comparator<Pair> COMPARATOR =
				comparing((Function<Pair, Object>) Pair::getLeft, naturalOrderNullsFirst()).thenComparing(
						(Function<Pair, Object>) Pair::getRight, naturalOrderNullsFirst());

		public static String format(Object o) {
			if (o instanceof String) {
				return '"' + (String) o + '"';
			}
			return String.valueOf(o);
		}

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
			return "(" + format(getLeft()) + ", " + format(getRight()) + ')';
		}

		@Override
		public int compareTo(Pair<L, R> that) {
			return COMPARATOR.compare(this, that);
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
