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

import org.d2ab.function.QuaternaryFunction;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;

import static java.util.Comparator.comparing;
import static org.d2ab.util.Comparators.naturalOrderNullsFirst;

/**
 * A general purpose pair of two objects, "{@code left}" and "{@code right}". Pairs implement {@link Map.Entry} where
 * the key is the "left" side and the value is the "right" side.
 *
 * @param <L> the type of the "left" side of the pair.
 * @param <R> the type of the "right" side of the pair.
 */
public abstract class Pair<L, R> implements Entry<L, R>, Comparable<Pair<L, R>> {
	@SuppressWarnings("unchecked")
	private static final Comparator<Pair> COMPARATOR =
			comparing((Function<Pair, Object>) Pair::getLeft, naturalOrderNullsFirst()).thenComparing(
					(Function<Pair, Object>) Pair::getRight, naturalOrderNullsFirst());

	/**
	 * @return a {@code Pair} of the two objects given.
	 */
	public static <L, R> Pair<L, R> of(L left, R right) {
		return new Pair<L, R>() {
			@Override
			public L getLeft() {
				return left;
			}

			@Override
			public R getRight() {
				return right;
			}
		};
	}

	/**
	 * @return a {@code Pair} that delegates to the given {@link Map.Entry}, key being left and value being right.
	 */
	public static <K, V> Pair<K, V> from(Entry<? extends K, ? extends V> entry) {
		return new Pair<K, V>() {
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

	/**
	 * @return a unary {@code Pair} where both objects are the same.
	 */
	public static <T> Pair<T, T> unary(T item) {
		return new Pair<T, T>() {
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

	/**
	 * @return the given bi-valued function converted to a pair-based unary operator.
	 */
	public static <K, V> UnaryOperator<Pair<K, V>> asUnaryOperator(
			BiFunction<? super K, ? super V, ? extends Pair<K, V>> op) {
		return entry -> op.apply(entry.getLeft(), entry.getRight());
	}

	/**
	 * @return the given doubly bi-valued function converted to a pair-based binary operator.
	 */
	public static <K, V> BinaryOperator<Pair<K, V>> asBinaryOperator(QuaternaryFunction<K, V, K, V, Pair<K, V>> f) {
		return (e1, e2) -> f.apply(e1.getLeft(), e1.getRight(), e2.getLeft(), e2.getRight());
	}

	/**
	 * @return the given bi-valued function converted to a pair-based function.
	 */
	public static <K, V, R> Function<Pair<K, V>, R> asFunction(BiFunction<? super K, ? super V, ? extends R> mapper) {
		return entry -> mapper.apply(entry.getLeft(), entry.getRight());
	}

	/**
	 * @return the given bi-valued predicate converted to a pair-based predicate.
	 */
	public static <K, V> Predicate<Pair<K, V>> asPredicate(BiPredicate<? super K, ? super V> predicate) {
		return entry -> predicate.test(entry.getLeft(), entry.getRight());
	}

	/**
	 * @return the given bi-valued consumer converted to a pair-based consumer.
	 */
	public static <K, V> Consumer<Pair<K, V>> asConsumer(BiConsumer<? super K, ? super V> action) {
		return entry -> action.accept(entry.getLeft(), entry.getRight());
	}

	/**
	 * @return the "left" component of the {@code Pair}.
	 */
	public abstract L getLeft();

	/**
	 * @return the "right" component of the {@code Pair}.
	 */
	public abstract R getRight();

	/**
	 * @return the "left" component of the {@code Pair}.
	 */
	@Override
	public L getKey() {
		return getLeft();
	}

	/**
	 * @return the "right" component of the {@code Pair}.
	 */
	@Override
	public R getValue() {
		return getRight();
	}

	/**
	 * This operation is not supported and throws {@link UnsupportedOperationException}.
	 */
	@Override
	public R setValue(R value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return a {@code Pair} with the "left" and "right" components of this {@code Pair} swapped.
	 */
	public Pair<R, L> swap() {
		return new Pair<R, L>() {
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

	/**
	 * @return a {@code Pair} where the "left" component is replaced with the given value.
	 */
	public <LL> Pair<LL, R> withLeft(LL left) {
		return new Pair<LL, R>() {
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

	/**
	 * @return a {@code Pair} where the "right" component is replaced with the given value.
	 */
	public <RR> Pair<L, RR> withRight(RR right) {
		return new Pair<L, RR>() {
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

	/**
	 * @return a {@code Pair} where the "left" component is shifted to the "right" component and replaced with the
	 * given value.
	 */
	public <LL> Pair<LL, L> shiftRight(LL replacement) {
		return new Pair<LL, L>() {
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

	/**
	 * @return a {@code Pair} where the "right" component is shifted to the "left" component and replaced with the
	 * given value.
	 */
	public <RR> Pair<R, RR> shiftLeft(RR replacement) {
		return new Pair<R, RR>() {
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

	/**
	 * @return a {@code Pair} mapped from this {@code Pair} using the given mappers.
	 */
	public <LL, RR> Pair<LL, RR> map(Function<? super L, ? extends LL> leftMapper,
	                                 Function<? super R, ? extends RR> rightMapper) {
		return new Pair<LL, RR>() {
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

	/**
	 * @return a {@code Pair} mapped from this {@code Pair} using the given mapper.
	 */
	public <LL, RR> Pair<LL, RR> map(BiFunction<? super L, ? super R, ? extends Pair<LL, RR>> mapper) {
		return mapper.apply(getLeft(), getRight());
	}

	/**
	 * @return a the result of applying the given {@link BiFunction} to the "left" and "right" components of this
	 * {@code Pair}.
	 */
	public <T> T apply(BiFunction<? super L, ? super R, ? extends T> function) {
		return function.apply(getLeft(), getRight());
	}

	/**
	 * @return the result of testing the given {@link Predicate}s on the "left" and "right" components of this
	 * {@code Pair}.
	 */
	public boolean test(Predicate<? super L> leftPredicate, Predicate<? super R> rightPredicate) {
		return leftPredicate.test(getLeft()) && rightPredicate.test(getRight());
	}

	/**
	 * @return the result of testing the given {@link BiPredicate}s on the "left" and "right" components of this
	 * {@code Pair}.
	 */
	public boolean test(BiPredicate<? super L, ? super R> predicate) {
		return predicate.test(getLeft(), getRight());
	}

	public Map<L, R> put(Map<L, R> map) {
		map.put(getLeft(), getRight());
		return map;
	}

	/**
	 * @return an iterator over the components of this {@code Pair}, containing exactly two elements.
	 */
	public <T> Iterator<T> iterator() {
		return new PairIterator<>();
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
		return Objects.equals(getLeft(), that.getLeft()) && Objects.equals(getRight(), that.getRight());
	}

	@Override
	public String toString() {
		return "(" + format(getLeft()) + ", " + format(getRight()) + ')';
	}

	private static String format(Object o) {
		if (o instanceof String)
			return '"' + (String) o + '"';

		return String.valueOf(o);
	}

	@Override
	public int compareTo(Pair<L, R> that) {
		return COMPARATOR.compare(this, that);
	}

	private class PairIterator<T> implements Iterator<T> {
		int index;

		@Override
		public boolean hasNext() {
			return index < 2;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T next() {
			if (!hasNext())
				throw new NoSuchElementException();

			switch (++index) {
				case 1:
					return (T) getLeft();
				case 2:
					return (T) getRight();
				default:
					// Can't happen due to above check
					throw new IllegalStateException();
			}
		}
	}
}
