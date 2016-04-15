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

import org.d2ab.function.chars.CharFunction;
import org.d2ab.iterator.chars.CharIterator;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.DoubleFunction;
import java.util.function.IntFunction;

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

	@SuppressWarnings("unchecked")
	public static <T> Iterator<T> empty() {
		return EMPTY;
	}

	@SafeVarargs
	public static <T> Iterator<T> of(T... items) {
		return new ArrayIterator<>(items);
	}

	public static <T> Iterator<T> from(CharIterator iterator, CharFunction<T> mapper) {
		return new DelegatingIterator<Character, CharIterator, T>(iterator) {
			@Override
			public T next() {
				return mapper.apply(iterator.nextChar());
			}
		};
	}

	public static <T> Iterator<T> from(PrimitiveIterator.OfInt iterator, IntFunction<T> mapper) {
		return new DelegatingIterator<Integer, PrimitiveIterator.OfInt, T>(iterator) {
			@Override
			public T next() {
				return mapper.apply(iterator.nextInt());
			}
		};
	}

	public static <T> Iterator<T> from(PrimitiveIterator.OfDouble iterator, DoubleFunction<T> mapper) {
		return new DelegatingIterator<Double, PrimitiveIterator.OfDouble, T>(iterator) {
			@Override
			public T next() {
				return mapper.apply(iterator.nextDouble());
			}
		};
	}

	public static void skip(Iterator<?> iterator) {
		if (iterator.hasNext())
			iterator.next();
	}

	public static void skip(Iterator<?> iterator, long steps) {
		while (steps-- > 0 && iterator.hasNext())
			iterator.next();
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
	public static <T> Optional<T> get(Iterator<T> iterator, long index) {
		skip(iterator, index);
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	public static <T> List<T> toList(Iterator<T> iterator) {
		List<T> list = new ArrayList<>();
		iterator.forEachRemaining(list::add);
		return list;
	}
}
