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

package org.d2ab.iterable.longs;

import org.d2ab.iterator.longs.ArrayLongIterator;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.util.Pair;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@FunctionalInterface
public interface LongIterable extends Iterable<Long> {
	@Override
	LongIterator iterator();

	/**
	 * Performs the given action for each {@code long} in this iterable.
	 */
	@Override
	default void forEach(Consumer<? super Long> consumer) {
		forEachLong((consumer instanceof LongConsumer) ? (LongConsumer) consumer : consumer::accept);
	}

	/**
	 * Performs the given action for each {@code long} in this iterable.
	 */
	default void forEachLong(LongConsumer consumer) {
		LongIterator iterator = iterator();
		while (iterator.hasNext())
			consumer.accept(iterator.nextLong());
	}

	/**
	 * Converts a container of some kind into a possibly once-only {@link LongIterable}.
	 *
	 * @param container the non-null container to turn into an {@link LongIterable}, can be one of {@link Iterable},
	 *                  {@link LongIterable}, {@link Iterator}, {@link LongIterator}, {@link Stream} of {@link
	 *                  Long}s, {@link LongStream}, array of {@link Long}s, or long array.
	 *
	 * @return the container as a {@link LongIterable}.
	 *
	 * @throws ClassCastException if the container is not one of the permitted classes.
	 */
	@SuppressWarnings("unchecked")
	static LongIterable from(Object container) {
		if (container instanceof LongIterable)
			return (LongIterable) container;
		else if (container instanceof Iterable)
			return from((Iterable<Long>) container);
		else if (container instanceof LongIterator)
			return from((LongIterator) container);
		else if (container instanceof Stream)
			return from((Iterator<Long>) container);
		else if (container instanceof LongStream)
			return from((LongStream) container);
		else if (container instanceof long[])
			return of((long[]) container);
		else if (container instanceof Long[])
			return from((Long[]) container);
		else if (container instanceof Pair)
			return from(((Pair<Long, Long>) container)::iterator);
		else
			throw new ClassCastException("Required an Iterable, LongIterable, Iterator, LongIterator, array of " +
			                             "Long, long array, Stream of Long, or LongStream but got: " +
			                             container.getClass());
	}

	static LongIterable from(Iterable<Long> iterable) {
		return () -> LongIterator.from(iterable);
	}

	static LongIterable from(LongIterator iterator) {
		return iterator.asIterable();
	}

	static LongIterable from(Iterator<Long> iterator) {
		return () -> LongIterator.from(iterator);
	}

	static LongIterable from(LongStream longStream) {
		return from(LongIterator.from(longStream.iterator()));
	}

	static LongIterable of(long... longs) {
		return () -> new ArrayLongIterator(longs);
	}

	static LongIterable from(Long... longs) {
		return from(Arrays.asList(longs));
	}

	static LongIterable from(Stream<Long> stream) {
		return from(stream.iterator());
	}
}
