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

package org.d2ab.collection.longs;

import org.d2ab.collection.Arrayz;
import org.d2ab.iterator.longs.ArrayLongIterator;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.sequence.LongSequence;
import org.d2ab.util.Strict;

import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

@FunctionalInterface
public interface LongIterable extends Iterable<Long> {
	static LongIterable of(long... longs) {
		return () -> new ArrayLongIterator(longs);
	}

	static LongIterable from(Iterable<Long> iterable) {
		if (iterable instanceof LongIterable)
			return (LongIterable) iterable;

		return () -> LongIterator.from(iterable.iterator());
	}

	static LongIterable once(LongIterator iterator) {
		return () -> iterator;
	}

	static LongIterable once(PrimitiveIterator.OfLong iterator) {
		return once(LongIterator.from(iterator));
	}

	@Override
	LongIterator iterator();

	/**
	 * Performs the given action for each {@code long} in this iterable.
	 */
	@Override
	default void forEach(Consumer<? super Long> consumer) {
		assert Strict.LENIENT : "LongIterable.forEach(Consumer)";

		forEachLong(consumer::accept);
	}

	/**
	 * Performs the given action for each {@code long} in this iterable.
	 */
	default void forEachLong(LongConsumer consumer) {
		iterator().forEachRemaining(consumer);
	}

	default LongStream longStream() {
		return StreamSupport.longStream(spliterator(), false);
	}

	default LongStream parallelLongStream() {
		return StreamSupport.longStream(spliterator(), true);
	}

	default Spliterator.OfLong spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.NONNULL);
	}

	/**
	 * @return a {@link LongSequence} over the {@code long} values in this {@code LongIterable}.
	 */
	default LongSequence sequence() {
		return LongSequence.from(this);
	}

	default boolean isEmpty() {
		return iterator().isEmpty();
	}

	default void clear() {
		iterator().removeAll();
	}

	default boolean containsLong(long x) {
		return iterator().contains(x);
	}

	default boolean removeLong(long x) {
		for (LongIterator iterator = iterator(); iterator.hasNext(); )
			if (iterator.nextLong() == x) {
				iterator.remove();
				return true;
			}

		return false;
	}

	default boolean containsAllLongs(long... xs) {
		for (long x : xs)
			if (!containsLong(x))
				return false;

		return true;
	}

	default boolean containsAllLongs(LongIterable c) {
		for (LongIterator iterator = c.iterator(); iterator.hasNext(); )
			if (!containsLong(iterator.nextLong()))
				return false;

		return true;
	}

	/**
	 * @return true if this {@code LongIterable} contains any of the given {@code longs}, false otherwise.
	 */
	default boolean containsAnyLongs(long... xs) {
		LongIterator iterator = iterator();
		while (iterator.hasNext())
			if (Arrayz.contains(xs, iterator.nextLong()))
				return true;

		return false;
	}

	/**
	 * @return true if this {@code LongIterable} contains any of the {@code longs} in the given {@code LongIterable},
	 * false otherwise.
	 */
	default boolean containsAnyLongs(LongIterable xs) {
		LongIterator iterator = iterator();
		while (iterator.hasNext())
			if (xs.containsLong(iterator.nextLong()))
				return true;

		return false;
	}

	default boolean removeAllLongs(LongIterable c) {
		boolean modified = false;
		for (LongIterator iterator = iterator(); iterator.hasNext(); ) {
			if (c.containsLong(iterator.nextLong())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	default boolean retainAllLongs(LongIterable c) {
		return removeLongsIf(x -> !c.containsLong(x));
	}

	default boolean removeAllLongs(long... xs) {
		return removeLongsIf(x -> Arrayz.contains(xs, x));
	}

	default boolean retainAllLongs(long... xs) {
		return removeLongsIf(x -> !Arrayz.contains(xs, x));
	}

	default boolean removeLongsIf(LongPredicate filter) {
		boolean modified = false;
		for (LongIterator iterator = iterator(); iterator.hasNext(); ) {
			if (filter.test(iterator.nextLong())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}
}
