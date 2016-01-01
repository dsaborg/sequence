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

public interface Pair<T, U> extends Entry<T, U> {
	static <K, V> Pair<K, V> from(Entry<? extends K, ? extends V> entry) {
		return new Base<K, V>() {
			@Override
			public K getFirst() {
				return entry.getKey();
			}

			@Override
			public V getSecond() {
				return entry.getValue();
			}
		};
	}

	static <T> Pair<T, T> unary(@Nullable T item) {
		return new Base<T, T>() {
			@Override
			public T getFirst() {
				return item;
			}

			@Override
			public T getSecond() {
				return item;
			}
		};
	}

	@Override
	default T getKey() {
		return getFirst();
	}

	T getFirst();

	@Override
	default U getValue() {
		return getSecond();
	}

	U getSecond();

	@Override
	default U setValue(U value) {
		throw new UnsupportedOperationException();
	}

	default Pair<U, T> swapped() {
		return new Base<U, T>() {
			@Override
			public U getFirst() {
				return Pair.this.getSecond();
			}

			@Override
			public T getSecond() {
				return Pair.this.getFirst();
			}
		};
	}

	default <V> Pair<V, U> withFirst(V first) {
		return new Base<V, U>() {
			@Override
			public V getFirst() {
				return first;
			}

			@Override
			public U getSecond() {
				return Pair.this.getSecond();
			}
		};
	}

	default <V> Pair<T, V> withSecond(V second) {
		return new Base<T, V>() {
			@Override
			public T getFirst() {
				return Pair.this.getFirst();
			}

			@Override
			public V getSecond() {
				return second;
			}
		};
	}

	default <V> Pair<V, T> shiftRight(V replacement) {
		return new Base<V, T>() {
			@Override
			public V getFirst() {
				return replacement;
			}

			@Override
			public T getSecond() {
				return Pair.this.getFirst();
			}
		};
	}

	default <V> Pair<U, V> shiftLeft(V replacement) {
		return new Base<U, V>() {
			@Override
			public U getFirst() {
				return Pair.this.getSecond();
			}

			@Override
			public V getSecond() {
				return replacement;
			}
		};
	}

	@Nonnull
	default <V, W> Pair<V, W> map(@Nonnull Function<? super T, ? extends V> firstMapper,
	                              @Nonnull Function<? super U, ? extends W> secondMapper) {
		return of(firstMapper.apply(getFirst()), secondMapper.apply(getSecond()));
	}

	static <A, B> Pair<A, B> of(@Nullable A first, @Nullable B sceond) {
		return new Base<A, B>() {
			@Override
			public A getFirst() {
				return first;
			}

			@Override
			public B getSecond() {
				return sceond;
			}
		};
	}

	@Nonnull
	default <V, W> Pair<V, W> map(@Nonnull BiFunction<? super T, ? super U, ? extends Pair<V, W>> mapper) {
		return mapper.apply(getFirst(), getSecond());
	}

	default <R> R apply(@Nonnull BiFunction<? super T, ? super U, ? extends R> function) {
		return function.apply(getFirst(), getSecond());
	}

	default boolean test(@Nonnull Predicate<? super T> firstPredicate, @Nonnull Predicate<? super U> secondPredicate) {
		return firstPredicate.test(getFirst()) && secondPredicate.test(getSecond());
	}

	default boolean test(@Nonnull BiPredicate<? super T, ? super U> predicate) {
		return predicate.test(getFirst(), getSecond());
	}

	default Map<T, U> putInto(@Nonnull Map<T, U> map) {
		map.put(getFirst(), getSecond());
		return map;
	}

	default <R> Iterator<R> iterator() {
		return new PairIterator(this);
	}

	abstract class Base<T, U> implements Pair<T, U> {
		@Override
		public int hashCode() {
			int result = getFirst() != null ? getFirst().hashCode() : 0;
			result = 31 * result + (getSecond() != null ? getSecond().hashCode() : 0);
			return result;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || !(o instanceof Pair))
				return false;

			Pair<?, ?> that = (Pair<?, ?>) o;

			return (getFirst() != null ? getFirst().equals(that.getFirst()) : that.getFirst() == null) &&
			       (getSecond() != null ? getSecond().equals(that.getSecond()) : that.getSecond() == null);
		}

		@Override
		public String toString() {
			return "(" + getFirst() + ',' + getSecond() + ')';
		}
	}

	class PairIterator<T extends R, U extends R, R> implements Iterator<R> {
		private final Pair<T, U> pair;
		int index;

		public PairIterator(Pair<T, U> pair) {
			this.pair = pair;
		}

		@Override
		public boolean hasNext() {
			return index < 2;
		}

		@Override
		public R next() {
			if (!hasNext())
				throw new NoSuchElementException();
			switch (++index) {
				case 1:
					return pair.getFirst();
				case 2:
					return pair.getSecond();
				default:
					// Can't happen due to above check
					throw new IllegalStateException();
			}
		}
	}
}
