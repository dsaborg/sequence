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

import org.d2ab.collection.longs.BitLongSet;
import org.d2ab.collection.longs.LongSet;
import org.d2ab.iterator.doubles.DelegatingDoubleIterator;
import org.d2ab.iterator.doubles.DoubleIterator;

import java.util.PrimitiveIterator;
import java.util.Set;

/**
 * An implementation of {@link DoubleSet} backed by a {@link BitLongSet}s for raw double values.
 */
public class RawDoubleSet implements DoubleSet {
	private final LongSet values = new BitLongSet();

	public RawDoubleSet(double... xs) {
		addAllDoubles(xs);
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public DoubleIterator iterator() {
		return new DelegatingDoubleIterator<Long, PrimitiveIterator.OfLong>(values.iterator()) {
			@Override
			public double nextDouble() {
				return Double.longBitsToDouble(iterator.nextLong());
			}
		};
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
		return values.addLong(Double.doubleToLongBits(x));
	}

	@Override
	public boolean removeDoubleExactly(double x) {
		return values.removeLong(Double.doubleToLongBits(x));
	}

	@Override
	public boolean containsDoubleExactly(double x) {
		return values.containsLong(Double.doubleToLongBits(x));
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
