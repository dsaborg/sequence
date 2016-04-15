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

package org.d2ab.iterable;

import org.d2ab.collection.Maps;
import org.d2ab.util.Pair;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class Iterables {
	private Iterables() {
	}

	public static <T> Iterable<T> from(Iterator<T> iterator) {
		return () -> iterator;
	}

	@SafeVarargs
	public static <T> Iterable<T> of(T... objects) {
		return asList(objects);
	}

	public static <T> Iterable<T> from(Stream<T> stream) {
		return stream::iterator;
	}

	/**
	 * Converts a container of some kind into a possibly once-only {@link Iterable}.
	 *
	 * @param container the non-null container to turn into an {@link Iterable}, can be one of {@link Iterable}, {@link
	 *                  Iterator}, {@link Stream}, {@code Array}, {@link Pair} or {@link Map.Entry}.
	 *
	 * @return the container as an iterable.
	 *
	 * @throws ClassCastException if the container is not one of {@link Iterable}, {@link Iterator}, {@link Stream},
	 *                            {@code Array}, {@link Pair} or {@link Map.Entry}
	 */
	@SuppressWarnings("unchecked")
	public static <T> Iterable<T> from(Object container) {
		if (container instanceof Iterable)
			return (Iterable<T>) container;
		else if (container instanceof Iterator)
			return from((Iterator<T>) container);
		else if (container instanceof Stream)
			return from((Stream<T>) container);
		else if (container instanceof Object[])
			return of((T[]) container);
		else if (container instanceof Pair)
			return ((Pair<T, T>) container)::iterator;
		else if (container instanceof Map.Entry)
			return () -> Maps.iterator((Map.Entry<T, T>) container);
		else
			throw new ClassCastException(
					"Required an Iterable, Iterator, Array, Stream, Pair or Entry but got: " + container.getClass());
	}

	/**
	 * @return true if all elements in this {@code Sequence} satisfy the given predicate, false otherwise.
	 */
	public static <T> boolean all(Iterable<T> iterable, Predicate<? super T> predicate) {
		for (T each : iterable) {
			if (!predicate.test(each))
				return false;
		}
		return true;
	}

	/**
	 * @return true if no elements in this {@code Sequence} satisfy the given predicate, false otherwise.
	 */
	public static <T> boolean none(Iterable<T> iterable, Predicate<? super T> predicate) {
		return !any(iterable, predicate);
	}

	/**
	 * @return true if any element in this {@code Sequence} satisfies the given predicate, false otherwise.
	 */
	public static <T> boolean any(Iterable<T> iterable, Predicate<? super T> predicate) {
		for (T each : iterable) {
			if (predicate.test(each))
				return true;
		}
		return false;
	}

	public static <T> void removeAll(Iterable<T> iterable) {
		for (Iterator<T> iterator = iterable.iterator(); iterator.hasNext(); ) {
			iterator.next();
			iterator.remove();
		}
	}
}
