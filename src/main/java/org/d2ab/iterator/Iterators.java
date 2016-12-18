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

package org.d2ab.iterator;

import org.d2ab.function.CharFunction;
import org.d2ab.iterator.chars.CharIterator;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.DoubleFunction;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

/**
 * Utility methods for {@link Iterator} instances.
 */
public class Iterators {
	private static final Iterator EMPTY = new Iterator() {
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public Object next() {
			throw new NoSuchElementException();
		}
	};

	private Iterators() {
	}

	/**
	 * @return an empty {@link Iterator}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Iterator<T> empty() {
		return EMPTY;
	}

	/**
	 * @return an {@link Iterator} containing the given items.
	 */
	@SafeVarargs
	public static <T> Iterator<T> of(T... items) {
		return new ArrayIterator<>(items);
	}

	/**
	 * @return an {@link Iterator} over the items in the given {@link CharIterator}, mapped to objects using the given
	 * {@link CharFunction}.
	 */
	public static <T> Iterator<T> from(CharIterator iterator, CharFunction<T> mapper) {
		return new DelegatingTransformingIterator<Character, CharIterator, T>(iterator) {
			@Override
			public T next() {
				return mapper.apply(iterator.nextChar());
			}
		};
	}

	/**
	 * @return an {@link Iterator} over the items in the given {@link PrimitiveIterator.OfInt}, mapped to objects using
	 * the given {@link IntFunction}.
	 */
	public static <T> Iterator<T> from(PrimitiveIterator.OfInt iterator, IntFunction<T> mapper) {
		return new DelegatingTransformingIterator<Integer, PrimitiveIterator.OfInt, T>(iterator) {
			@Override
			public T next() {
				return mapper.apply(iterator.nextInt());
			}
		};
	}

	/**
	 * @return an {@link Iterator} over the items in the given {@link PrimitiveIterator.OfDouble}, mapped to objects
	 * using the given {@link DoubleFunction}.
	 */
	public static <T> Iterator<T> from(PrimitiveIterator.OfDouble iterator, DoubleFunction<T> mapper) {
		return new DelegatingTransformingIterator<Double, PrimitiveIterator.OfDouble, T>(iterator) {
			@Override
			public T next() {
				return mapper.apply(iterator.nextDouble());
			}
		};
	}

	/**
	 * @return an {@link Iterator} over the items in the given {@link PrimitiveIterator.OfLong}, mapped to objects
	 * using the given {@link LongFunction}.
	 */
	public static <T> Iterator<T> from(PrimitiveIterator.OfLong iterator, LongFunction<T> mapper) {
		return new DelegatingTransformingIterator<Long, PrimitiveIterator.OfLong, T>(iterator) {
			@Override
			public T next() {
				return mapper.apply(iterator.nextLong());
			}
		};
	}

	/**
	 * Skip one step in the given {@link Iterator}.
	 *
	 * @return true if there was an element to skip over.
	 */
	public static boolean skip(Iterator<?> iterator) {
		if (iterator.hasNext()) {
			iterator.next();
			return true;
		}

		return false;
	}

	/**
	 * Skip the given number of steps in the given {@link Iterator}.
	 *
	 * @return the actual number of steps skipped, if iterator terminated early.
	 */
	public static int skip(Iterator<?> iterator, int steps) {
		int count = 0;
		while (count < steps && iterator.hasNext()) {
			iterator.next();
			count++;
		}
		return count;
	}

	/**
	 * Reduce the given iterator into a single element by iteratively applying the given binary operator to
	 * the current result and each element in this sequence. Returns an empty optional if the sequence is empty,
	 * or the result if it's not.
	 */
	public static <T> Optional<T> reduce(Iterator<? extends T> iterator, BinaryOperator<T> operator) {
		if (!iterator.hasNext())
			return Optional.empty();

		T identity = iterator.next();
		if (!iterator.hasNext())
			return Optional.of(identity);

		T result = reduce(iterator, identity, operator);
		return Optional.of(result);
	}

	/**
	 * Reduce the given iterator into a single element by iteratively applying the given binary operator to
	 * the current result and each element in this sequence, starting with the given identity as the initial result.
	 */
	public static <T> T reduce(Iterator<? extends T> iterator, T identity, BinaryOperator<T> operator) {
		T result = identity;
		while (iterator.hasNext())
			result = operator.apply(result, iterator.next());
		return result;
	}

	/**
	 * @return the element at the given index, or an empty {@link Optional} if the {@link Iterator} contains fewer
	 * items than the index.
	 */
	public static <T> Optional<T> get(Iterator<? extends T> iterator, int index) {
		skip(iterator, index);
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	/**
	 * @return the last element in the given {@link Iterator} or an empty {@link Optional} if there are no elements in
	 * the {@link Iterator}.
	 */
	public static <T> Optional<T> last(Iterator<? extends T> iterator) {
		if (!iterator.hasNext())
			return Optional.empty();

		T last;
		do
			last = iterator.next(); while (iterator.hasNext());

		return Optional.of(last);
	}

	/**
	 * Collect the given {@link Iterator} into a {@link List}.
	 */
	public static <T> List<T> toList(Iterator<? extends T> iterator) {
		List<T> list = new ArrayList<>();
		iterator.forEachRemaining(list::add);
		return list;
	}

	/**
	 * @return the count of elements remaining in the given {@link Iterator}.
	 */
	public static int count(Iterator<?> iterator) {
		long count = 0;
		while (iterator.hasNext()) {
			iterator.next();
			count++;
		}

		if (count > Integer.MAX_VALUE)
			throw new IllegalStateException("count > Integer.MAX_VALUE: " + count);

		return (int) count;
	}

	/**
	 * @return an unmodifiable view of an {@link Iterator} retrieved from the given {@link Iterable}.
	 */
	public static <T> Iterator<T> unmodifiable(Iterable<? extends T> iterable) {
		return unmodifiable(iterable.iterator());
	}

	/**
	 * @return an unmodifiable view of the given {@link Iterator}.
	 */
	public static <T> Iterator<T> unmodifiable(Iterator<? extends T> iterator) {
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				return iterator.next();
			}
		};
	}

	/**
	 * @return true if any object in the given {@link Iterator} is equal to the given object, false otherwise.
	 *
	 * @since 2.0
	 */
	public static <T> boolean contains(Iterator<? extends T> iterator, T object) {
		while (iterator.hasNext())
			if (Objects.equals(object, iterator.next()))
				return true;

		return false;
	}
}
