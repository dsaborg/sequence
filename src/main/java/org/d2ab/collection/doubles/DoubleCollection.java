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

import org.d2ab.collection.Collectionz;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;

/**
 * A primitive specialization of {@link Collection} for {code double} values. Supplements all {@link Double}-valued
 * methods with corresponding {@code double}-valued methods.
 */
public interface DoubleCollection extends Collection<Double>, DoubleIterable {
	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default void clear() {
		iterator().removeAll();
	}

	@Override
	default Double[] toArray() {
		return toArray(new Double[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return Collectionz.toArray(this, a);
	}

	/**
	 * Collect the {@code doubles} in this {@code DoubleCollection} into an {@code double}-array.
	 */
	default double[] toDoubleArray() {
		return new ArrayDoubleList(this).toDoubleArray();
	}

	@Override
	default boolean add(Double l) {
		return addDouble(l);
	}

	default boolean addDouble(double l) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean contains(Object o) {
		return o instanceof Double && containsDoubleExactly((double) o);
	}

	@Override
	default boolean remove(Object o) {
		return o instanceof Double && removeDoubleExactly((double) o);
	}

	@Override
	default boolean addAll(Collection<? extends Double> c) {
		return Collectionz.addAll(this, c);
	}

	default boolean addAllDoubles(double... is) {
		boolean changed = false;
		for (double i : is)
			changed |= addDouble(i);
		return changed;
	}

	default boolean addAllDoubles(DoubleCollection c) {
		if (c.isEmpty())
			return false;

		c.forEachDouble(this::addDouble);
		return true;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return Collectionz.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return Collectionz.removeAll(this, c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return Collectionz.retainAll(this, c);
	}

	@Override
	default boolean removeIf(Predicate<? super Double> filter) {
		return removeDoublesIf(filter::test);
	}

	@Override
	default Spliterator.OfDouble spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.NONNULL);
	}
}
