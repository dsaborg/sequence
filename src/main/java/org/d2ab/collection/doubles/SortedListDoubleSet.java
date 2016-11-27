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

import org.d2ab.collection.SparseBitSet;
import org.d2ab.iterator.doubles.DoubleIterator;

import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An implementation of {@link DoubleSortedSet} backed by two {@link SparseBitSet}s for positive and negative values.
 */
public class SortedListDoubleSet implements DoubleSortedSet {
	private final DoubleList values = DoubleList.create();

	public SortedListDoubleSet(double... xs) {
		addAllDoubles(xs);
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public DoubleIterator iterator() {
		return values.iterator();
	}

	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}

	@Override
	public void clear() {
		values.clear();
	}

	@Override
	public boolean addDouble(double x) {
		int index = values.binarySearchExactly(x);
		if (index >= 0)
			return false;

		values.addDoubleAt(-index - 1, x);
		return true;
	}

	@Override
	public boolean removeDoubleExactly(double x) {
		int index = values.binarySearchExactly(x);
		if (index < 0)
			return false;

		values.removeDoubleAt(index);
		return true;
	}

	@Override
	public boolean containsDoubleExactly(double x) {
		return values.binarySearchExactly(x) >= 0;
	}

	@Override
	public double firstDouble() {
		if (isEmpty())
			throw new NoSuchElementException();

		return values.get(0);
	}

	@Override
	public double lastDouble() {
		if (isEmpty())
			throw new NoSuchElementException();

		return values.get(values.size() - 1);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(size() * 5); // heuristic
		builder.append("[");

		boolean started = false;
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); ) {
			if (started)
				builder.append(", ");
			else
				started = true;
			builder.append(iterator.nextDouble());
		}

		builder.append("]");
		return builder.toString();
	}

	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Set))
			return false;

		Set<?> that = (Set<?>) o;
		return size() == that.size() && containsAll(that);
	}

	public int hashCode() {
		int hashCode = 0;
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); )
			hashCode += Double.hashCode(iterator.nextDouble());
		return hashCode;
	}
}
