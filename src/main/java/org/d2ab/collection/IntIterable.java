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

package org.d2ab.collection;

import org.d2ab.iterator.IterationException;
import org.d2ab.iterator.ints.ArrayIntIterator;
import org.d2ab.iterator.ints.InputStreamIntIterator;
import org.d2ab.iterator.ints.IntIterator;

import java.io.IOException;
import java.io.InputStream;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

import static java.util.Arrays.asList;

@FunctionalInterface
public interface IntIterable extends Iterable<Integer> {
	static IntIterable of(int... integers) {
		return () -> new ArrayIntIterator(integers);
	}

	static IntIterable from(Integer... integers) {
		return from(asList(integers));
	}

	static IntIterable from(Iterable<Integer> iterable) {
		if (iterable instanceof IntIterable)
			return (IntIterable) iterable;

		return () -> IntIterator.from(iterable.iterator());
	}

	static IntIterable once(IntIterator iterator) {
		return () -> iterator;
	}

	static IntIterable once(PrimitiveIterator.OfInt iterator) {
		return once(IntIterator.from(iterator));
	}

	/**
	 * Create an {@code IntIterable} from an {@link InputStream} which iterates over the bytes provided in the
	 * input stream as ints. The {@link InputStream} must support {@link InputStream#reset} or the {@code IntIterable}
	 * will only be available to iterate over once. The {@link InputStream} will be reset in between iterations,
	 * if possible. If an {@link IOException} occurs during iteration, an {@link IterationException} will be thrown.
	 * The {@link InputStream} will not be closed by the {@code IntIterable} when iteration finishes, it must be closed
	 * externally when iteration is finished.
	 *
	 * @since 1.1
	 */
	static IntIterable read(InputStream inputStream) {
		return new IntIterable() {
			boolean started;

			@Override
			public IntIterator iterator() {
				if (started)
					try {
						inputStream.reset();
					} catch (IOException e) {
						// do nothing, let input stream exhaust itself
					}
				else
					started = true;

				return new InputStreamIntIterator(inputStream);
			}
		};
	}

	@Override
	IntIterator iterator();

	/**
	 * Performs the given action for each {@code int} in this iterable.
	 */
	default void forEachInt(IntConsumer consumer) {
		iterator().forEachRemaining(consumer);
	}

	/**
	 * Performs the given action for each {@code int} in this iterable.
	 */
	@Override
	default void forEach(Consumer<? super Integer> consumer) {
		iterator().forEachRemaining(consumer);
	}

	default void clear() {
		iterator().removeAll();
	}

	default boolean containsAll(IntCollection c) {
		for (int i : c)
			if (!containsInt(i))
				return false;

		return true;
	}

	default boolean removeAll(IntCollection c) {
		return removeIntsIf(c::containsInt);
	}

	default boolean retainAll(IntCollection c) {
		return removeIntsIf(i -> !c.containsInt(i));
	}

	default boolean containsAll(int... is) {
		for (int i : is)
			if (!containsInt(i))
				return false;

		return true;
	}

	default boolean removeAll(int... is) {
		return removeIntsIf(i -> Arrayz.contains(is, i));
	}

	default boolean retainAll(int... is) {
		return removeIntsIf(i -> !Arrayz.contains(is, i));
	}

	default boolean removeIntsIf(IntPredicate filter) {
		boolean changed = false;
		for (IntIterator iterator = iterator(); iterator.hasNext(); ) {
			if (filter.test(iterator.nextInt())) {
				iterator.remove();
				changed = true;
			}
		}
		return changed;
	}

	default Spliterator.OfInt spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.NONNULL);
	}

	default boolean containsInt(int i) {
		return iterator().contains(i);
	}

	default boolean removeInt(int i) {
		IntIterator iterator = iterator();
		while (iterator.hasNext())
			if (iterator.nextInt() == i) {
				iterator.remove();
				return true;
			}

		return false;
	}

	/**
	 * Collect the {@code ints} in this {@code IntIterable} into an {@code int}-array.
	 */
	default int[] toIntArray() {
		return iterator().toArray();
	}

	/**
	 * @return this {@code IntIterable} as an {@link InputStream}. Mark and reset is supported, by re-traversing
	 * the iterator to the mark position. Ints outside of the allowed range {@code 0} to {@code 255} will result in
	 * an {@link IOException} being thrown during traversal.
	 *
	 * @since 1.2
	 */
	default InputStream asInputStream() {
		return new IntIterableInputStream(this);
	}

	/**
	 * @return true if this {@code IntIterable} contains any of the given {@code ints}, false otherwise.
	 *
	 * @since 1.2
	 */
	default boolean containsAny(int... items) {
		IntIterator iterator = iterator();
		while (iterator.hasNext())
			if (Arrayz.contains(items, iterator.nextInt()))
				return true;

		return false;
	}
}
