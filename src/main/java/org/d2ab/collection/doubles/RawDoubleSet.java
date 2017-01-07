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
import org.d2ab.iterator.doubles.DelegatingTransformingDoubleIterator;
import org.d2ab.iterator.doubles.DoubleIterator;

import java.util.PrimitiveIterator;

/**
 * An implementation of {@link DoubleSet} backed by a {@link BitLongSet}s for raw double values.
 */
public class RawDoubleSet extends DoubleSet.Base {
	private final LongSet values = new BitLongSet();

	public RawDoubleSet() {
	}

	public RawDoubleSet(double... xs) {
		addAllDoubles(xs);
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public DoubleIterator iterator() {
		return new DelegatingTransformingDoubleIterator<Long, PrimitiveIterator.OfLong>(values.iterator()) {
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
	public boolean addDoubleExactly(double x) {
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
}
