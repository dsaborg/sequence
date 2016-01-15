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

package org.d2ab.iterable;

import org.d2ab.util.Pair;

import java.util.Iterator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public class Iterables {
	private Iterables() {
	}

	public static <T> Iterable<T> from(Iterator<T> iterator) {
		requireNonNull(iterator);
		return () -> iterator;
	}

	@SafeVarargs
	public static <T> Iterable<T> from(T... objects) {
		return asList(requireNonNull(objects));
	}

	public static <T> Iterable<T> from(Stream<T> stream) {
		return requireNonNull(stream)::iterator;
	}

	/**
	 * Converts a container of some kind into a possibly once-only {@link Iterable}.
	 *
	 * @param container the non-null container to turn into an {@link Iterable}, can be one of {@link Iterable}, {@link
	 *                  Iterator}, {@link Stream} or {@code Array}.
	 *
	 * @return the container as an iterable.
	 *
	 * @throws ClassCastException if the container is not one of {@link Iterable}, {@link Iterator}, {@link Stream} or
	 *                            {@code Array}
	 */
	@SuppressWarnings("unchecked")
	public static <T> Iterable<T> from(Object container) {
		requireNonNull(container);
		if (container instanceof Iterable)
			return (Iterable<T>) container;
		else if (container instanceof Iterator)
			return from((Iterator<T>) container);
		else if (container instanceof Stream)
			return from((Stream<T>) container);
		else if (container instanceof Object[])
			return from((T[]) container);
		else if (container instanceof Pair)
			return from((Iterable<T>) ((Pair<T, T>) container)::iterator);
		else
			throw new ClassCastException("Required an Iterable, Iterator, Array or Stream but got: " +
			                             container.getClass());
	}
}
