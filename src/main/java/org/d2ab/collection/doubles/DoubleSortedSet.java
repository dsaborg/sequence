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

import org.d2ab.iterator.doubles.DoubleIterator;

import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A primitive specialization of {@link SortedSet} for {@code double} values.
 */
public interface DoubleSortedSet extends SortedSet<Double>, DoubleSet {
	@Override
	default DoubleComparator comparator() {
		return null;
	}

	@Override
	default DoubleSortedSet subSet(Double fromElement, Double toElement) {
		return subSet((double) fromElement, (double) toElement);
	}

	default DoubleSortedSet subSet(double fromElement, double toElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	default DoubleSortedSet headSet(Double toElement) {
		return headSet((double) toElement);
	}

	default DoubleSortedSet headSet(double toElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	default DoubleSortedSet tailSet(Double fromElement) {
		return tailSet((double) fromElement);
	}

	default DoubleSortedSet tailSet(double fromElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	default Double first() {
		return firstDouble();
	}

	default double firstDouble() {
		return iterator().nextDouble();
	}

	@Override
	default Double last() {
		return lastDouble();
	}

	default double lastDouble() {
		DoubleIterator iterator = iterator();
		double last;
		do
			last = iterator.nextDouble();
		while (iterator.hasNext());
		return last;
	}

	@Override
	default Spliterator.OfDouble spliterator() {
		return Spliterators.spliterator(iterator(), size(),
		                                Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED |
		                                Spliterator.NONNULL);
	}
}
