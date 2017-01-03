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
import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.util.Strict;

import java.util.Collection;
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
		assert Strict.LENIENT : "DoubleCollection.toArray()";

		return toArray(new Double[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		assert Strict.LENIENT : "DoubleCollection.toArray(Object[])";

		return Collectionz.toArray(this, a);
	}

	/**
	 * Collect the {@code doubles} in this {@code DoubleCollection} into an {@code double}-array.
	 */
	default double[] toDoubleArray() {
		return new ArrayDoubleList(this).toDoubleArray();
	}

	@Override
	default boolean add(Double x) {
		assert Strict.LENIENT : "DoubleCollection.add(Double)";

		return addDoubleExactly(x);
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
		assert Strict.LENIENT : "DoubleCollection.contains(Object)";

		return o instanceof Double && containsDoubleExactly((double) o);
	}

	@Override
	default boolean remove(Object o) {
		assert Strict.LENIENT : "DoubleCollection.remove(Object)";

		return o instanceof Double && removeDoubleExactly((double) o);
	}

	@Override
	default boolean addAll(Collection<? extends Double> c) {
		if (c instanceof DoubleCollection)
			return addAllDoubles((DoubleCollection) c);

		assert Strict.LENIENT : "DoubleCollection.addAll(Collection)";

		return Collectionz.addAll(this, c);
	}

	default boolean addAllDoubles(double... xs) {
		boolean changed = false;
		for (double x : xs)
			changed |= addDoubleExactly(x);
		return changed;
	}

	default boolean addAllDoubles(DoubleCollection c) {
		if (c.isEmpty())
			return false;

		c.forEachDouble(this::addDoubleExactly);
		return true;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		if (c instanceof DoubleCollection)
			return containsAllDoublesExactly((DoubleCollection) c);

		assert Strict.LENIENT : "DoubleCollection.containsAll(Collection)";

		return Collectionz.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		if (c instanceof DoubleCollection)
			return removeAllDoublesExactly((DoubleCollection) c);

		assert Strict.LENIENT : "DoubleCollection.removeAll(Collection)";

		return Collectionz.removeAll(this, c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		if (c instanceof DoubleCollection)
			return retainAllDoublesExactly((DoubleCollection) c);

		assert Strict.LENIENT : "DoubleCollection.retainAll(Collection)";

		return Collectionz.retainAll(this, c);
	}

	@Override
	default boolean removeIf(Predicate<? super Double> filter) {
		assert Strict.LENIENT : "DoubleCollection.removeIf(Predicate)";

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
			return create(DoubleList.create(doubles));
		}

		public static DoubleCollection create(final DoubleCollection collection) {
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
