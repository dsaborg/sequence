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

import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;

import static java.util.Arrays.asList;

@FunctionalInterface
public interface LongIterable extends Iterable<Long> {
	static LongIterable of(long... longs) {
		return () -> new ArrayLongIterator(longs);
	}

	static LongIterable from(Long... longs) {
		return from(asList(longs));
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
		iterator().forEachRemaining(consumer);
	}

	/**
	 * Performs the given action for each {@code long} in this iterable.
	 */
	default void forEachLong(LongConsumer consumer) {
		iterator().forEachRemaining(consumer);
	}

	default Spliterator.OfLong spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.NONNULL);
	}

	default boolean isEmpty() {
		return !iterator().hasNext();
	}

	default void clear() {
		iterator().removeAll();
	}

	default boolean containsLong(long l) {
		return iterator().contains(l);
	}

	default boolean removeLong(long l) {
		for (LongIterator iterator = iterator(); iterator.hasNext(); )
			if (iterator.nextLong() == l) {
				iterator.remove();
				return true;
			}

		return false;
	}

	default boolean containsAllLongs(long... ls) {
		for (long l : ls)
			if (!containsLong(l))
				return false;

		return true;
	}

	default boolean containsAllLongs(LongIterable c) {
		for (long i : c)
			if (!containsLong(i))
				return false;

		return true;
	}

	/**
	 * @return true if this {@code LongIterable} contains any of the given {@code longs}, false otherwise.
	 */
	default boolean containsAnyLongs(long... is) {
		LongIterator iterator = iterator();
		while (iterator.hasNext())
			if (Arrayz.contains(is, iterator.nextLong()))
				return true;

		return false;
	}

	/**
	 * @return true if this {@code LongIterable} contains any of the {@code longs} in the given {@code LongIterable},
	 * false otherwise.
	 */
	default boolean containsAnyLongs(LongIterable is) {
		LongIterator iterator = iterator();
		while (iterator.hasNext())
			if (is.containsLong(iterator.nextLong()))
				return true;

		return false;
	}

	default boolean removeAllLongs(LongIterable c) {
		return removeLongsIf(c::containsLong);
	}

	default boolean retainAllLongs(LongIterable c) {
		return removeLongsIf(i -> !c.containsLong(i));
	}

	default boolean removeAllLongs(long... ls) {
		return removeLongsIf(i -> Arrayz.contains(ls, i));
	}

	default boolean retainAllLongs(long... ls) {
		return removeLongsIf(i -> !Arrayz.contains(ls, i));
	}

	default boolean removeLongsIf(LongPredicate filter) {
		boolean changed = false;
		for (LongIterator iterator = iterator(); iterator.hasNext(); ) {
			if (filter.test(iterator.nextLong())) {
				iterator.remove();
				changed = true;
			}
		}
		return changed;
	}
}
