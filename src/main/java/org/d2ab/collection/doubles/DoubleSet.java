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

import java.util.Collection;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A primitive specialization of {@link Set} for {@code double} values.
 */
public interface DoubleSet extends Set<Double>, DoubleCollection {
	/**
	 * @return a new empty mutable {@code DoubleSet}.
	 *
	 * @since 2.1
	 */
	static DoubleSet create() {
		return new RawDoubleSet();
	}

	/**
	 * @return a new mutable {@code DoubleSet} with a copy of the given elements.
	 *
	 * @since 2.1
	 */
	static DoubleSet create(double... xs) {
		return new RawDoubleSet(xs);
	}

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

	@Override
	default boolean add(Double x) {
		return DoubleCollections.add(this, x);
	}

	@Override
	default boolean addDouble(double x, double precision) {
		return !containsDouble(x, precision) && addDoubleExactly(x);
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
	default Spliterator.OfDouble spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.DISTINCT);
	}

	/**
	 * Base class for {@link DoubleSet} implementations.
	 */
	abstract class Base extends DoubleCollection.Base implements DoubleSet {
		public static DoubleSet create(double... doubles) {
			return from(DoubleSortedSet.create(doubles));
		}

		public static DoubleSet from(final DoubleCollection collection) {
			return new DoubleSet.Base() {
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

		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof Set))
				return false;

			if (o instanceof DoubleSet) {
				DoubleSet that = (DoubleSet) o;
				return size() == that.size() && containsAllDoublesExactly(that);
			} else {
				Set<?> that = (Set<?>) o;
				return size() == that.size() && containsAll(that);
			}
		}

		public int hashCode() {
			int hashCode = 0;
			for (DoubleIterator iterator = iterator(); iterator.hasNext(); )
				hashCode += Double.hashCode(iterator.nextDouble());
			return hashCode;
		}
	}
}
