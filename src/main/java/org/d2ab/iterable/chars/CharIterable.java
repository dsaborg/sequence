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

package org.d2ab.iterable.chars;

import org.d2ab.function.chars.CharConsumer;
import org.d2ab.iterator.chars.ArrayCharIterator;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.util.Pair;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface CharIterable extends Iterable<Character> {
	@Override
	CharIterator iterator();

	/**
	 * Perform the given action for each {@code char} in this iterable.
	 */
	@Override
	default void forEach(Consumer<? super Character> consumer) {
		forEachChar((consumer instanceof CharConsumer) ? (CharConsumer) consumer : consumer::accept);
	}

	/**
	 * Perform the given action for each {@code char} in this iterable.
	 */
	default void forEachChar(CharConsumer consumer) {
		CharIterator iterator = iterator();
		while (iterator.hasNext())
			consumer.accept(iterator.nextChar());
	}

	/**
	 * Converts a container of some kind into a possibly once-only {@link CharIterable}.
	 *
	 * @param container the non-null container to turn into an {@link CharIterable}, can be one of {@link Iterable},
	 *                  {@link CharIterable}, {@link Iterator}, {@link CharIterator}, {@link Stream} of {@link
	 *                  Character}s, {@link IntStream}, array of {@link Character}s, or char array.
	 *
	 * @return the container as a {@link CharIterable}.
	 *
	 * @throws ClassCastException if the container is not one of the permitted classes.
	 */
	@SuppressWarnings("unchecked")
	static CharIterable from(Object container) {
		requireNonNull(container);

		if (container instanceof CharIterable)
			return (CharIterable) container;
		else if (container instanceof Iterable)
			return from((Iterable<Character>) container);
		else if (container instanceof CharIterator)
			return from((CharIterator) container);
		else if (container instanceof Stream)
			return from((Iterator<Character>) container);
		else if (container instanceof IntStream)
			return from((IntStream) container);
		else if (container instanceof char[])
			return of((char[]) container);
		else if (container instanceof Character[])
			return from((Character[]) container);
		else if (container instanceof Pair)
			return from(((Pair<Character, Character>) container)::iterator);
		else
			throw new ClassCastException("Required an Iterable, CharIterable, Iterator, CharIterator, array of " +
			                             "Character, char array, Stream of Character, or IntStream but got: " +
			                             container.getClass());
	}

	static CharIterable from(Iterable<Character> iterable) {
		return () -> CharIterator.from(iterable);
	}

	static CharIterable from(CharIterator iterator) {
		return iterator.asIterable();
	}

	static CharIterable from(Iterator<Character> iterator) {
		return () -> CharIterator.from(iterator);
	}

	static CharIterable from(IntStream charStream) {
		return from(CharIterator.from(charStream.iterator()));
	}

	static CharIterable of(char... characters) {
		return () -> new ArrayCharIterator(characters);
	}

	static CharIterable from(Character... characters) {
		return from(Arrays.asList(characters));
	}

	static CharIterable from(Stream<Character> stream) {
		return from(stream.iterator());
	}
}
