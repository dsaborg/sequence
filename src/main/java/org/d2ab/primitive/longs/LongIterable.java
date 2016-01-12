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

package org.d2ab.primitive.longs;

import org.d2ab.sequence.Pair;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface LongIterable extends Iterable<Long> {
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
		requireNonNull(container);

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

	@Override
	LongIterator iterator();

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec If the action is an instance of {@code LongConsumer} then it is cast to {@code LongConsumer} and
	 * passed
	 * to {@link #forEachLong}; otherwise the action is adapted to an instance of {@code LongConsumer}, by boxing the
	 * argument of {@code LongConsumer}, and then passed to {@link #forEachLong}.
	 */
	@Override
	default void forEach(Consumer<? super Long> action) {
		requireNonNull(action);

		forEachLong((action instanceof LongConsumer) ? (LongConsumer) action : action::accept);
	}

	/**
	 * Performs the given action for each long in this {@code Iterable} until all elements have been processed or
	 * the action throws an exception. Actions are performed in the order of iteration, if that order is specified.
	 * Exceptions thrown by the action are relayed to the caller.
	 *
	 * @param action The action to be performed for each element
	 *
	 * @throws NullPointerException if the specified action is null
	 * @implSpec <p>The default implementation behaves as if:
	 * <pre>{@code
	 * LongIterator iterator = iterator();
	 * while (iterator.hasNext())
	 *     action.accept(iterator.nextLong());
	 * }</pre>
	 */
	default void forEachLong(LongConsumer action) {
		requireNonNull(action);

		LongIterator iterator = iterator();
		while (iterator.hasNext())
			action.accept(iterator.nextLong());
	}
}
