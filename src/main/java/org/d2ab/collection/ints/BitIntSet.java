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

package org.d2ab.collection.ints;

import org.d2ab.collection.SparseBitSet;
import org.d2ab.iterator.ints.DelegatingTransformingIntIterator;
import org.d2ab.iterator.ints.IntIterator;

import java.util.PrimitiveIterator;

/**
 * An implementation of {@link IntSortedSet} backed by two {@link SparseBitSet}s for positive and negative values.
 */
public class BitIntSet extends IntSet.Base implements IntSortedSet {
	private final SparseBitSet values = new SparseBitSet();

	public BitIntSet() {
	}

	public BitIntSet(int... xs) {
		addAllInts(xs);
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public IntIterator iterator() {
		return new DelegatingTransformingIntIterator<Long, PrimitiveIterator.OfLong>(values.iterator()) {
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
	public boolean addInt(int x) {
		return values.set((long) x - Integer.MIN_VALUE);
	}

	@Override
	public boolean removeInt(int x) {
		return values.clear((long) x - Integer.MIN_VALUE);
	}

	@Override
	public boolean containsInt(int x) {
		return values.get((long) x - Integer.MIN_VALUE);
	}

	@Override
	public int firstInt() {
		return (int) (values.firstLong() + Integer.MIN_VALUE);
	}

	@Override
	public int lastInt() {
		return (int) (values.lastLong() + Integer.MIN_VALUE);
	}
}
