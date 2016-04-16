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

import java.util.Arrays;
import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

	static CharIterable of(char... characters) {
		return () -> new ArrayCharIterator(characters);
	}

	static CharIterable from(Character... characters) {
		return from(Arrays.asList(characters));
	}

	static CharIterable from(Iterable<Character> iterable) {
		return () -> CharIterator.from(iterable);
	}

	static CharIterable from(CharIterator iterator) {
		return () -> iterator;
	}

	static CharIterable from(PrimitiveIterator.OfInt iterator) {
		return from(CharIterator.from(iterator));
	}

	static CharIterable from(Iterator<Character> iterator) {
		return from(CharIterator.from(iterator));
	}

	static CharIterable from(Stream<Character> stream) {
		return from(stream.iterator());
	}

	static CharIterable from(IntStream stream) {
		return from(stream.iterator());
	}
}
