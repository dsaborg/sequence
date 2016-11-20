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

package org.d2ab.collection.longs;

import org.d2ab.collection.SparseBitSet;
import org.d2ab.iterator.longs.ChainingLongIterator;
import org.d2ab.iterator.longs.LongIterator;

import java.util.Set;

/**
 * An implementation of {@link LongSortedSet} backed by two {@link SparseBitSet}s for positive and negative values.
 * This {@link LongSortedSet} covers all values between {@link Long#MIN_VALUE} and {@link Long#MAX_VALUE} inclusive.
 */
public class BitLongSet implements LongSortedSet {
	private final SparseBitSet positives = new SparseBitSet();
	private final SparseBitSet negatives = new SparseBitSet();

	public BitLongSet(long... ls) {
		addAllLongs(ls);
	}

	@Override
	public int size() {
		long bitCount = positives.bitCount() + negatives.bitCount();

		if (bitCount > Integer.MAX_VALUE)
			throw new IllegalStateException("size > Integer.MAX_VALUE: " + bitCount);

		return (int) bitCount;
	}

	@Override
	public LongIterator iterator() {
		return new ChainingLongIterator(() -> LongIterator.from(negatives.descendingIterator(), n -> -n - 1),
		                                () -> LongIterator.from(positives.iterator()));
	}

	@Override
	public boolean isEmpty() {
		return positives.isEmpty() && negatives.isEmpty();
	}

	@Override
	public void clear() {
		positives.clear();
		negatives.clear();
	}

	@Override
	public boolean addLong(long l) {
		if (l >= 0)
			return positives.set(l);
		else
			return negatives.set(-(l + 1));
	}

	@Override
	public boolean removeLong(long l) {
		if (l >= 0)
			return positives.clear(l);
		else
			return negatives.clear(-(l + 1));
	}

	@Override
	public boolean containsLong(long l) {
		if (l >= 0)
			return positives.get(l);
		else
			return negatives.get(-(l + 1));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(size() * 5); // heuristic
		builder.append("[");

		boolean started = false;
		LongIterator iterator = iterator();
		while (iterator.hasNext()) {
			if (started)
				builder.append(", ");
			else
				started = true;
			builder.append(iterator.nextLong());
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
		LongIterator iterator = iterator();
		while (iterator.hasNext())
			hashCode += Long.hashCode(iterator.nextLong());
		return hashCode;
	}
}
