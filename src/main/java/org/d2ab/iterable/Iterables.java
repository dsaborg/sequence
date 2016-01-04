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

import org.d2ab.sequence.Pair;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class Iterables {
	private Iterables() {
	}

	@Nonnull
	public static <T> Iterable<T> from(Iterator<T> iterator) {
		return () -> iterator;
	}

	@SafeVarargs
	@Nonnull
	public static <T> Iterable<T> from(T... objects) {
		return asList(objects);
	}

	@Nonnull
	public static <T> Iterable<T> from(Stream<T> stream) {
		return stream::iterator;
	}

	/**
	 * Converts a container of some kind into a possibly once-only {@link Iterable}.
	 *
	 * @param container the non-null container to turn into an {@link Iterable}, can be one of {@link Iterable},
	 *                  {@link Iterator}, {@link Stream} or {@code Array}.
	 *
	 * @return the container as an iterable.
	 *
	 * @throws ClassCastException if the container is not one of {@link Iterable}, {@link Iterator}, {@link Stream} or
	 *                            {@code Array}
	 */
	@SuppressWarnings("unchecked")
	public static <T> Iterable<T> from(Object container) {
		if (container == null)
			throw new NullPointerException();

		if (container instanceof Iterable)
			return (Iterable<T>) container;

		if (container instanceof Iterator)
			return from((Iterator<T>) container);

		if (container instanceof Stream)
			return from((Stream<T>) container);

		if (container instanceof Object[])
			return from((T[]) container);

		if (container instanceof Pair)
			return from((Iterable<T>) ((Pair<T, T>) container)::iterator);

		throw new ClassCastException("Required an Iterable, Iterator, Array or Stream but got: " +
		                             container.getClass());
	}
}
