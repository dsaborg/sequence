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

package org.d2ab.collection.doubles;

import org.d2ab.iterator.doubles.ArrayDoubleIterator;
import org.d2ab.iterator.doubles.DoubleIterator;

import java.util.PrimitiveIterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

import static java.util.Arrays.asList;

@FunctionalInterface
public interface DoubleIterable extends Iterable<Double> {
	@Override
	DoubleIterator iterator();

	/**
	 * Performs the given action for each {@code double} in this iterable.
	 */
	@Override
	default void forEach(Consumer<? super Double> consumer) {
		forEachDouble((consumer instanceof DoubleConsumer) ? (DoubleConsumer) consumer : consumer::accept);
	}

	/**
	 * Performs the given action for each {@code double} in this iterable.
	 */
	default void forEachDouble(DoubleConsumer consumer) {
		DoubleIterator iterator = iterator();
		while (iterator.hasNext())
			consumer.accept(iterator.nextDouble());
	}

	static DoubleIterable of(double... doubles) {
		return () -> new ArrayDoubleIterator(doubles);
	}

	static DoubleIterable from(Double... doubles) {
		return from(asList(doubles));
	}

	static DoubleIterable from(Iterable<Double> iterable) {
		if (iterable instanceof DoubleIterable)
			return (DoubleIterable) iterable;

		return () -> DoubleIterator.from(iterable.iterator());
	}

	static DoubleIterable once(DoubleIterator iterator) {
		return () -> iterator;
	}

	static DoubleIterable once(PrimitiveIterator.OfDouble iterator) {
		return once(DoubleIterator.from(iterator));
	}
}
