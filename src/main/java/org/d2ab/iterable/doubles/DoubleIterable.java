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

package org.d2ab.iterable.doubles;

import org.d2ab.iterator.doubles.ArrayDoubleIterator;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.util.Pair;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

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

	/**
	 * Converts a container of some kind into a possibly once-only {@link DoubleIterable}.
	 *
	 * @param container the non-null container to turn into an {@link DoubleIterable}, can be one of {@link Iterable},
	 *                  {@link DoubleIterable}, {@link Iterator}, {@link DoubleIterator}, {@link Stream} of {@link
	 *                  Double}s, {@link DoubleStream}, array of {@link Double}s, or double array.
	 *
	 * @return the container as a {@link DoubleIterable}.
	 *
	 * @throws ClassCastException if the container is not one of the permitted classes.
	 */
	@SuppressWarnings("unchecked")
	static DoubleIterable from(Object container) {
		requireNonNull(container);

		if (container instanceof DoubleIterable)
			return (DoubleIterable) container;
		else if (container instanceof Iterable)
			return from((Iterable<Double>) container);
		else if (container instanceof DoubleIterator)
			return from((DoubleIterator) container);
		else if (container instanceof Stream)
			return from((Iterator<Double>) container);
		else if (container instanceof DoubleStream)
			return from((DoubleStream) container);
		else if (container instanceof double[])
			return of((double[]) container);
		else if (container instanceof Double[])
			return from((Double[]) container);
		else if (container instanceof Pair)
			return from(((Pair<Double, Double>) container)::iterator);
		else
			throw new ClassCastException("Required an Iterable, DoubleIterable, Iterator, DoubleIterator, array of " +
			                             "Double, double array, Stream of Double, or DoubleStream but got: " +
			                             container.getClass());
	}

	static DoubleIterable from(Iterable<Double> iterable) {
		return () -> DoubleIterator.from(iterable);
	}

	static DoubleIterable from(DoubleIterator iterator) {
		return iterator.asIterable();
	}

	static DoubleIterable from(Iterator<Double> iterator) {
		return () -> DoubleIterator.from(iterator);
	}

	static DoubleIterable from(DoubleStream doubleStream) {
		return from(DoubleIterator.from(doubleStream.iterator()));
	}

	static DoubleIterable of(double... doubles) {
		return () -> new ArrayDoubleIterator(doubles);
	}

	static DoubleIterable from(Double... doubles) {
		return from(Arrays.asList(doubles));
	}

	static DoubleIterable from(Stream<Double> stream) {
		return from(stream.iterator());
	}
}
