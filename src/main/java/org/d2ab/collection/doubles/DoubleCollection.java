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

import org.d2ab.collection.PrimitiveCollections;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.util.Strict;

import java.util.Collection;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;

/**
 * A primitive specialization of {@link Collection} for {code double} values. Supplements all {@link Double}-valued
 * methods with corresponding {@code double}-valued methods.
 */
public interface DoubleCollection extends Collection<Double>, DoubleIterable {
	// TODO: Extract out relevant parts to IterableDoubleCollection

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
		return PrimitiveCollections.toArray(this, a);
	}

	/**
	 * Collect the {@code doubles} in this {@code DoubleCollection} into a {@code double}-array.
	 */
	default double[] toDoubleArray() {
		return new ArrayDoubleList(this).toDoubleArray();
	}

	/**
	 * @return a {@link DoubleList} view of this {@code DoubleCollection}, which is updated as the {@code
	 * DoubleSequence} changes. The list does not implement {@link RandomAccess} and is best accessed in sequence.
	 *
	 * @since 2.2
	 */
	default DoubleList asList() {
		return CollectionDoubleList.from(this);
	}

	@Override
	default boolean add(Double x) {
		return DoubleCollections.add(this, x);
	}

	/**
	 * @see #addDouble(double, double)
	 * @since 2.1
	 */
	default boolean addDoubleExactly(double x) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see #addDoubleExactly(double)
	 * @since 2.1
	 */
	default boolean addDouble(double x, double precision) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean contains(Object o) {
		return DoubleCollections.contains(this, o);
	}

	@Override
	default boolean remove(Object o) {
		return DoubleCollections.remove(this, o);
	}

	@Override
	default boolean addAll(Collection<? extends Double> c) {
		return DoubleCollections.addAll(this, c);
	}

	default boolean addAllDoubles(double... xs) {
		boolean modified = false;
		for (double x : xs)
			modified |= addDoubleExactly(x);
		return modified;
	}

	default boolean addAllDoubles(DoubleCollection c) {
		boolean modified = false;
		for (DoubleIterator iterator = c.iterator(); iterator.hasNext(); )
			modified |= addDoubleExactly(iterator.nextDouble());
		return modified;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return DoubleCollections.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return DoubleCollections.removeAll(this, c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return DoubleCollections.retainAll(this, c);
	}

	@Override
	default boolean removeIf(Predicate<? super Double> filter) {
		Strict.check();

		return removeDoublesIf(filter::test);
	}

	@Override
	default Spliterator.OfDouble spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.NONNULL);
	}

	/**
	 * Base class for {@link DoubleCollection} implementations.
	 */
	abstract class Base implements DoubleCollection {
		public static DoubleCollection create(double... doubles) {
			return from(DoubleList.create(doubles));
		}

		public static DoubleCollection from(final DoubleCollection collection) {
			return new DoubleCollection.Base() {
				@Override
				public DoubleIterator iterator() {
					return collection.iterator();
				}

				@Override
				public int size() {
					return collection.size();
				}

				@Override
				public boolean addDoubleExactly(double x) {
					return collection.addDoubleExactly(x);
				}
			};
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder(size() * 5); // heuristic
			builder.append("[");

			boolean tail = false;
			for (DoubleIterator iterator = iterator(); iterator.hasNext(); ) {
				if (tail)
					builder.append(", ");
				else
					tail = true;
				builder.append(iterator.nextDouble());
			}

			builder.append("]");
			return builder.toString();
		}
	}
}
