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

package org.d2ab.collection;

import org.d2ab.iterator.ints.DelegatingIntIterator;
import org.d2ab.iterator.ints.IntIterator;

import java.util.PrimitiveIterator;
import java.util.Set;

/**
 * An implementation of {@link IntSortedSet} backed by two {@link SparseBitSet}s for positive and negative values.
 */
public class BitIntSet implements IntSortedSet {
	private final SparseBitSet values = new SparseBitSet();

	public BitIntSet(int... is) {
		addAll(is);
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public IntIterator iterator() {
		return new DelegatingIntIterator<Long, PrimitiveIterator.OfLong>(values.iterator()) {
			@Override
			public int nextInt() {
				return (int) (iterator.nextLong() + Integer.MIN_VALUE);
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
	public boolean addInt(int i) {
		return values.set((long) i - Integer.MIN_VALUE);
	}

	@Override
	public boolean removeInt(int i) {
		return values.clear((long) i - Integer.MIN_VALUE);
	}

	@Override
	public boolean containsInt(int i) {
		return values.get((long) i - Integer.MIN_VALUE);
	}

	@Override
	public int firstInt() {
		return (int) (values.firstLong() + Integer.MIN_VALUE);
	}

	@Override
	public int lastInt() {
		return (int) (values.lastLong() + Integer.MIN_VALUE);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(size() * 5); // heuristic
		builder.append("[");

		boolean started = false;
		IntIterator iterator = iterator();
		while (iterator.hasNext()) {
			if (started)
				builder.append(", ");
			else
				started = true;
			builder.append(iterator.nextInt());
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
		for (IntIterator iterator = iterator(); iterator.hasNext(); )
			hashCode += Integer.hashCode(iterator.nextInt());
		return hashCode;
	}
}
