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

package org.d2ab.iterable.ints;

import org.d2ab.iterator.ints.ArrayIntIterator;
import org.d2ab.iterator.ints.IntIterator;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

@FunctionalInterface
public interface IntIterable extends Iterable<Integer> {
	@Override
	IntIterator iterator();

	/**
	 * Performs the given action for each {@code int} in this iterable.
	 */
	@Override
	default void forEach(Consumer<? super Integer> consumer) {
		forEachInt((consumer instanceof IntConsumer) ? (IntConsumer) consumer : consumer::accept);
	}

	/**
	 * Performs the given action for each {@code int} in this iterable.
	 */
	default void forEachInt(IntConsumer consumer) {
		IntIterator iterator = iterator();
		while (iterator.hasNext())
			consumer.accept(iterator.nextInt());
	}

	static IntIterable of(int... integers) {
		return () -> new ArrayIntIterator(integers);
	}

	static IntIterable from(Integer... integers) {
		return from(Arrays.asList(integers));
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
}
