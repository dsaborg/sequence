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

package org.d2ab.collection.ints;

import org.d2ab.collection.Arrayz;
import org.d2ab.iterator.IterationException;
import org.d2ab.iterator.ints.ArrayIntIterator;
import org.d2ab.iterator.ints.InputStreamIntIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.sequence.IntSequence;

import java.io.IOException;
import java.io.InputStream;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

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
	@Override
	default void forEach(Consumer<? super Integer> consumer) {
		iterator().forEachRemaining(consumer);
	}

	/**
	 * Performs the given action for each {@code int} in this iterable.
	 */
	default void forEachInt(IntConsumer consumer) {
		iterator().forEachRemaining(consumer);
	}

	default IntStream intStream() {
		return StreamSupport.intStream(spliterator(), false);
	}

	default IntStream parallelIntStream() {
		return StreamSupport.intStream(spliterator(), true);
	}

	default Spliterator.OfInt spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.NONNULL);
	}

	/**
	 * @return an {@link IntSequence} over the {@code int} values in this {@code IntIterable}.
	 */
	default IntSequence sequence() {
		return IntSequence.from(this);
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

	default boolean isEmpty() {
		return iterator().isEmpty();
	}

	default void clear() {
		iterator().removeAll();
	}

	default boolean containsInt(int x) {
		return iterator().contains(x);
	}

	default boolean removeInt(int x) {
		for (IntIterator iterator = iterator(); iterator.hasNext(); )
			if (iterator.nextInt() == x) {
				iterator.remove();
				return true;
			}

		return false;
	}

	default boolean containsAllInts(int... xs) {
		for (int x : xs)
			if (!containsInt(x))
				return false;

		return true;
	}

	default boolean containsAllInts(IntIterable c) {
		for (int x : c)
			if (!containsInt(x))
				return false;

		return true;
	}

	/**
	 * @return true if this {@code IntIterable} contains any of the given {@code ints}, false otherwise.
	 */
	default boolean containsAnyInts(int... xs) {
		IntIterator iterator = iterator();
		while (iterator.hasNext())
			if (Arrayz.contains(xs, iterator.nextInt()))
				return true;

		return false;
	}

	/**
	 * @return true if this {@code IntIterable} contains any of the {@code ints} in the given {@code IntIterable},
	 * false otherwise.
	 */
	default boolean containsAnyInts(IntIterable xs) {
		IntIterator iterator = iterator();
		while (iterator.hasNext())
			if (xs.containsInt(iterator.nextInt()))
				return true;

		return false;
	}

	default boolean removeAllInts(int... xs) {
		return removeIntsIf(x -> Arrayz.contains(xs, x));
	}

	default boolean removeAllInts(IntIterable xs) {
		return removeIntsIf(xs::containsInt);
	}

	default boolean retainAllInts(int... xs) {
		return removeIntsIf(x -> !Arrayz.contains(xs, x));
	}

	default boolean retainAllInts(IntIterable xs) {
		return removeIntsIf(x -> !xs.containsInt(x));
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
}
