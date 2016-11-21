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

package org.d2ab.collection.chars;

import org.d2ab.collection.Arrayz;
import org.d2ab.function.CharConsumer;
import org.d2ab.function.CharPredicate;
import org.d2ab.iterator.IterationException;
import org.d2ab.iterator.chars.ArrayCharIterator;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.chars.ReaderCharIterator;
import org.d2ab.iterator.ints.IntIterator;

import java.io.IOException;
import java.io.Reader;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;

@FunctionalInterface
public interface CharIterable extends Iterable<Character> {
	/**
	 * Create a {@code CharIterable} from a {@link Reader} which iterates over the characters provided in the reader.
	 * The {@link Reader} must support {@link Reader#reset} or the {@code CharIterable} will only be available to
	 * iterate over once. The {@link Reader} will be reset in between iterations, if possible. If an
	 * {@link IOException} occurs during iteration, an {@link IterationException} will be thrown. The {@link Reader}
	 * will not be closed by the {@code CharIterable} when iteration finishes, it must be closed externally when
	 * iteration is finished.
	 *
	 * @since 1.2
	 */
	static CharIterable read(Reader reader) {
		return new CharIterable() {
			boolean started;

			@Override
			public CharIterator iterator() {
				if (started)
					try {
						reader.reset();
					} catch (IOException e) {
						// do nothing, let reader exhaust itself
					}
				else
					started = true;

				return new ReaderCharIterator(reader);
			}
		};
	}

	static CharIterable of(char... characters) {
		return () -> new ArrayCharIterator(characters);
	}

	static CharIterable from(Character... characters) {
		return from(asList(characters));
	}

	static CharIterable from(Iterable<Character> iterable) {
		return () -> CharIterator.from(iterable.iterator());
	}

	static CharIterable once(CharIterator iterator) {
		return () -> iterator;
	}

	@Override
	CharIterator iterator();

	/**
	 * @return an {@link IntIterator} over the characters in this {@code CharIterable} as {@code int} values.
	 */
	default IntIterator intIterator() {
		return IntIterator.from(iterator());
	}

	/**
	 * @return this {@code CharIterable} as a {@link Reader}. Mark and reset is supported, by re-traversing
	 * the iterator to the mark position.
	 *
	 * @since 1.2
	 */
	default Reader asReader() {
		return new CharIterableReader(this);
	}

	/**
	 * Perform the given action for each {@code char} in this iterable.
	 */
	default void forEachChar(CharConsumer consumer) {
		iterator().forEachRemaining(consumer);
	}

	/**
	 * Perform the given action for each {@code char} in this iterable.
	 */
	@Override
	default void forEach(Consumer<? super Character> consumer) {
		iterator().forEachRemaining(consumer);
	}

	default IntStream intStream() {
		return StreamSupport.intStream(intSpliterator(), false);
	}

	default IntStream parallelIntStream() {
		return StreamSupport.intStream(intSpliterator(), true);
	}

	default Spliterator.OfInt intSpliterator() {
		return Spliterators.spliteratorUnknownSize(intIterator(), Spliterator.NONNULL);
	}

	default boolean isEmpty() {
		return iterator().isEmpty();
	}

	default void clear() {
		iterator().removeAll();
	}

	default boolean containsChar(char x) {
		return iterator().contains(x);
	}

	default boolean removeChar(char x) {
		for (CharIterator iterator = iterator(); iterator.hasNext(); )
			if (iterator.nextChar() == x) {
				iterator.remove();
				return true;
			}

		return false;
	}

	default boolean containsAllChars(char... xs) {
		for (char x : xs)
			if (!containsChar(x))
				return false;

		return true;
	}

	default boolean containsAllChars(CharIterable c) {
		for (char x : c)
			if (!containsChar(x))
				return false;

		return true;
	}

	/**
	 * @return true if this {@code CharIterable} contains any of the given {@code chars}, false otherwise.
	 */
	default boolean containsAnyChars(char... xs) {
		CharIterator iterator = iterator();
		while (iterator.hasNext())
			if (Arrayz.contains(xs, iterator.nextChar()))
				return true;

		return false;
	}

	/**
	 * @return true if this {@code CharIterable} contains any of the {@code chars} in the given {@code CharIterable},
	 * false otherwise.
	 */
	default boolean containsAnyChars(CharIterable xs) {
		CharIterator iterator = iterator();
		while (iterator.hasNext())
			if (xs.containsChar(iterator.nextChar()))
				return true;

		return false;
	}

	default boolean removeAllChars(char... xs) {
		return removeCharsIf(x -> Arrayz.contains(xs, x));
	}

	default boolean removeAllChars(CharIterable xs) {
		return removeCharsIf(xs::containsChar);
	}

	default boolean retainAllChars(char... xs) {
		return removeCharsIf(x -> !Arrayz.contains(xs, x));
	}

	default boolean retainAllChars(CharIterable xs) {
		return removeCharsIf(x -> !xs.containsChar(x));
	}

	default boolean removeCharsIf(CharPredicate filter) {
		boolean changed = false;
		for (CharIterator iterator = iterator(); iterator.hasNext(); ) {
			if (filter.test(iterator.nextChar())) {
				iterator.remove();
				changed = true;
			}
		}
		return changed;
	}
}
