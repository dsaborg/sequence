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
import org.d2ab.sequence.DoubleSequence;
import org.d2ab.util.Strict;

import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

import static org.d2ab.collection.Arrayz.contains;
import static org.d2ab.collection.Arrayz.containsExactly;
import static org.d2ab.util.Doubles.eq;

@FunctionalInterface
public interface DoubleIterable extends Iterable<Double> {
	static DoubleIterable of(double... doubles) {
		return () -> new ArrayDoubleIterator(doubles);
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

	@Override
	DoubleIterator iterator();

	/**
	 * Performs the given action for each {@code double} in this iterable.
	 */
	@Override
	default void forEach(Consumer<? super Double> consumer) {
		assert Strict.LENIENT : "DoubleIterable.forEach(Consumer)";

		forEachDouble(consumer::accept);
	}

	/**
	 * Performs the given action for each {@code double} in this iterable.
	 */
	default void forEachDouble(DoubleConsumer consumer) {
		DoubleIterator iterator = iterator();
		while (iterator.hasNext())
			consumer.accept(iterator.nextDouble());
	}

	default DoubleStream doubleStream() {
		return StreamSupport.doubleStream(spliterator(), false);
	}

	default DoubleStream parallelDoubleStream() {
		return StreamSupport.doubleStream(spliterator(), true);
	}

	default Spliterator.OfDouble spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.NONNULL);
	}

	/**
	 * @return a {@link DoubleSequence} over the {@code double} values in this {@code DoubleIterable}.
	 */
	default DoubleSequence sequence() {
		return DoubleSequence.from(this);
	}

	default boolean isEmpty() {
		return iterator().isEmpty();
	}

	default void clear() {
		iterator().removeAll();
	}

	default boolean containsDoubleExactly(double x) {
		return containsDouble(x, 0);
	}

	default boolean containsDouble(double x, double precision) {
		return iterator().contains(x, precision);
	}

	default boolean removeDoubleExactly(double x) {
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); )
			if (iterator.nextDouble() == x) {
				iterator.remove();
				return true;
			}

		return false;
	}

	default boolean removeDouble(double x, double precision) {
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); )
			if (eq(iterator.nextDouble(), x, precision)) {
				iterator.remove();
				return true;
			}

		return false;
	}

	default boolean containsAllDoublesExactly(double... xs) {
		for (double x : xs)
			if (!containsDoubleExactly(x))
				return false;

		return true;
	}

	default boolean containsAllDoubles(double[] xs, double precision) {
		for (double x : xs)
			if (!containsDouble(x, precision))
				return false;

		return true;
	}

	default boolean containsAllDoublesExactly(DoubleIterable c) {
		for (double x : c)
			if (!containsDoubleExactly(x))
				return false;

		return true;
	}

	default boolean containsAllDoubles(DoubleIterable c, double precision) {
		for (double x : c)
			if (!containsDouble(x, precision))
				return false;

		return true;
	}

	/**
	 * @return true if this {@code DoubleIterable} contains any of the given {@code doubles}, false otherwise.
	 */
	default boolean containsAnyDoublesExactly(double... xs) {
		DoubleIterator iterator = iterator();
		while (iterator.hasNext())
			if (containsExactly(xs, iterator.nextDouble()))
				return true;

		return false;
	}

	/**
	 * @return true if this {@code DoubleIterable} contains any of the given {@code doubles} to the given precision,
	 * false otherwise.
	 */
	default boolean containsAnyDoubles(double[] xs, double precision) {
		DoubleIterator iterator = iterator();
		while (iterator.hasNext())
			if (contains(xs, iterator.nextDouble(), precision))
				return true;

		return false;
	}

	/**
	 * @return true if this {@code DoubleIterable} contains any of the {@code doubles} in the given {@code
	 * DoubleIterable}, false otherwise.
	 */
	default boolean containsAnyDoublesExactly(DoubleIterable xs) {
		DoubleIterator iterator = iterator();
		while (iterator.hasNext())
			if (xs.containsDoubleExactly(iterator.nextDouble()))
				return true;

		return false;
	}

	/**
	 * @return true if this {@code DoubleIterable} contains any of the {@code doubles} in the given {@code
	 * DoubleIterable} to the given precision, false otherwise.
	 */
	default boolean containsAnyDoubles(DoubleIterable xs, double precision) {
		DoubleIterator iterator = iterator();
		while (iterator.hasNext())
			if (xs.containsDouble(iterator.nextDouble(), precision))
				return true;

		return false;
	}

	default boolean removeAllDoublesExactly(double... xs) {
		return removeDoublesIf(x -> containsExactly(xs, x));
	}

	default boolean removeAllDoubles(double[] xs, double precision) {
		return removeDoublesIf(x -> contains(xs, x, precision));
	}

	default boolean removeAllDoublesExactly(DoubleIterable c) {
		return removeDoublesIf(c::containsDoubleExactly);
	}

	default boolean removeAllDoubles(DoubleIterable c, double precision) {
		return removeDoublesIf(x -> c.containsDouble(x, precision));
	}

	default boolean retainAllDoublesExactly(double... xs) {
		return removeDoublesIf(x -> !containsExactly(xs, x));
	}

	default boolean retainAllDoubles(double[] xs, double precision) {
		return removeDoublesIf(x -> !contains(xs, x, precision));
	}

	default boolean retainAllDoublesExactly(DoubleIterable c) {
		return removeDoublesIf(x -> !c.containsDoubleExactly(x));
	}

	default boolean retainAllDoubles(DoubleIterable c, double precision) {
		return removeDoublesIf(x -> !c.containsDouble(x, precision));
	}

	default boolean removeDoublesIf(DoublePredicate filter) {
		boolean changed = false;
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); ) {
			if (filter.test(iterator.nextDouble())) {
				iterator.remove();
				changed = true;
			}
		}
		return changed;
	}
}
