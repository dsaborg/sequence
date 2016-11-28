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

import org.d2ab.collection.longs.LongSet;
import org.d2ab.collection.longs.LongSortedSet;
import org.d2ab.iterator.longs.LongIterator;

import java.util.Arrays;
import java.util.BitSet;
import java.util.NoSuchElementException;

/**
 * A sparse bit set for storing occurrences of bits where a large amount of the stored bits are expected to be zero.
 * This implementation uses a simplistic indexing scheme which provides {@code log(n)} performance for accessing
 * individual bits. Memory usage is roughly proportional to {@link #bitCount}. The bit set can set any bit between
 * {@code 0} and {@link Long#MAX_VALUE}, inclusive. However, the maximum number of sparse 64-bit words in use by the
 * set bits in the bit set is limited by the int length limit of arrays.
 */
public class SparseBitSet extends LongSet.Base implements LongSortedSet {
	private long[] words;
	private long[] indices;
	private int size;

	/**
	 * Construct a {@code SparseBitSet}.
	 */
	public SparseBitSet() {
		this(10);
	}

	/**
	 * Construct a {@code SparseBitSet} with the given values.
	 */
	public SparseBitSet(long... values) {
		this(values.length);
		for (long value : values)
			set(value);
	}

	/**
	 * Construct a {@code SparseBitSet} with the given initial capacity.
	 */
	public SparseBitSet(int capacity) {
		this.words = new long[capacity];
		this.indices = new long[capacity];
	}

	/**
	 * Set the bit at index {@code i}.
	 *
	 * @return {@code true} if this bit set changed as a result of setting the bit, i.e. the bit was clear,
	 * {@code false} otherwise.
	 */
	public boolean set(long i) {
		if (i < 0)
			throw new IllegalArgumentException("i < 0: " + i);

		int wordIndex = findWord(i);
		if (wordIndex < 0) {
			insert(i, -(wordIndex + 1));
			return true;
		}

		long bit = 1L << (i & 63);
		boolean wasClear = (words[wordIndex] & bit) == 0;
		words[wordIndex] |= bit;
		return wasClear;
	}

	/**
	 * Clear the bit at index {@code i}.
	 *
	 * @return {@code true} if this bit set changed as a result of setting the bit, i.e. the bit was set,
	 * {@code false} otherwise.
	 */
	public boolean clear(long i) {
		if (i < 0)
			throw new IllegalArgumentException("i < 0: " + i);

		int wordIndex = findWord(i);
		if (wordIndex < 0)
			return false;

		long bitIndex = i & 63;
		boolean wasSet = (words[wordIndex] & 1L << bitIndex) != 0;
		removeBit(wordIndex, bitIndex);
		return wasSet;
	}

	/**
	 * Set the bit at index {@code i} to the given {@code value}.
	 *
	 * @return {@code true} if this bit set changed as a result of changing the bit, false otherwise.
	 */
	public boolean set(long i, boolean value) {
		return value ? set(i) : clear(i);
	}

	/**
	 * Get the bit at index {@code i}.
	 *
	 * @return {@code true} if the bit at index {@code i} is set, false otherwise.
	 */
	public boolean get(long i) {
		if (i < 0)
			throw new IllegalArgumentException("i < 0: " + i);

		int wordIndex = findWord(i);
		return wordIndex >= 0 && (words[wordIndex] & (1L << (i & 63))) != 0;
	}

	/**
	 * @return the number of set bits in this {@code SparseBitSet}.
	 */
	public long bitCount() {
		long bitCount = 0;
		for (int i = 0; i < size; i++)
			bitCount += Long.bitCount(words[i]);
		return bitCount;
	}

	/**
	 * @return the size of this {@code SparseBitSet}, i.e. the number of set bits.
	 *
	 * @see #bitCount()
	 */
	@Override
	public int size() {
		long bitCount = bitCount();

		if (bitCount > Integer.MAX_VALUE)
			throw new IllegalStateException("size > Integer.MAX_VALUE: " + bitCount);

		return (int) bitCount;
	}

	/**
	 * @return true if this {@code SparseBitSet} contains no set bits, false otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Reset this {@code SparseBitSet} to the empty state, i.e. with no bits set.
	 */
	@Override
	public void clear() {
		for (int i = 0; i < size; i++) {
			words[i] = 0;
			indices[i] = 0;
		}
		size = 0;
	}

	@Override
	public long firstLong() {
		if (isEmpty())
			throw new NoSuchElementException();

		long firstWord = words[0];

		int bitIndex = 0;
		while ((firstWord & (1L << bitIndex)) == 0)
			bitIndex++;

		return (indices[0] << 6) + bitIndex;
	}

	@Override
	public long lastLong() {
		if (isEmpty())
			throw new NoSuchElementException();

		long lastWord = words[size - 1];

		int bitIndex = 63;
		while ((lastWord & (1L << bitIndex)) == 0)
			bitIndex--;

		return (indices[size - 1] << 6) + bitIndex;
	}

	/**
	 * @return a {@link LongIterator} over the bits in this {@code SparseBitSet}, from lowest to highest.
	 * All {@link LongIterator} methods are constant time. {@link LongIterator#remove()} is supported.
	 */
	@Override
	public LongIterator iterator() {
		return new LongIterator() {
			private int wordIndex = 0;
			private int bitIndex = 0;
			private int lastWordIndex = -1;
			private int lastBitIndex = -1;

			@Override
			public boolean hasNext() {
				// find next set bit
				while (wordIndex < size && (words[wordIndex] & (1L << bitIndex)) == 0)
					step();

				return wordIndex < size;
			}

			private void step() {
				bitIndex++;
				if (bitIndex == 64) {
					bitIndex = 0;
					wordIndex++;
				}
			}

			@Override
			public long nextLong() {
				if (!hasNext())
					throw new NoSuchElementException();

				lastWordIndex = wordIndex;
				lastBitIndex = bitIndex;
				long next = (indices[wordIndex] << 6) + bitIndex;
				step();
				return next;
			}

			@Override
			public void remove() {
				if (lastWordIndex < 0)
					throw new IllegalStateException("Cannot remove before call to nextLong or after call to remove");

				int previousSize = size;
				removeBit(lastWordIndex, lastBitIndex);
				if (size < previousSize) {
					wordIndex = lastWordIndex;
					bitIndex = 0;
				}
				lastWordIndex = -1;
				lastBitIndex = -1;
			}
		};
	}

	/**
	 * @return a descending {@link LongIterator} over the bits in this {@code SparseBitSet}, from highest to lowest.
	 * All {@link LongIterator} methods are constant time. {@link LongIterator#remove()} is supported.
	 */
	public LongIterator descendingIterator() {
		return new LongIterator() {
			private int wordIndex = size - 1;
			private int bitIndex = 63;
			private int lastWordIndex = -1;
			private int lastBitIndex = -1;

			@Override
			public boolean hasNext() {
				// find next set bit
				while (wordIndex >= 0 && (words[wordIndex] & (1L << bitIndex)) == 0)
					step();

				return wordIndex >= 0;
			}

			private void step() {
				bitIndex--;
				if (bitIndex < 0) {
					bitIndex = 63;
					wordIndex--;
				}
			}

			@Override
			public long nextLong() {
				if (!hasNext())
					throw new NoSuchElementException();

				lastWordIndex = wordIndex;
				lastBitIndex = bitIndex;
				long next = (indices[wordIndex] << 6) + bitIndex;
				step();
				return next;
			}

			@Override
			public void remove() {
				if (lastWordIndex < 0)
					throw new IllegalStateException("Cannot remove before call to nextLong or after call to remove");

				long previousSize = size;
				removeBit(lastWordIndex, lastBitIndex);
				if (size < previousSize) {
					wordIndex = lastWordIndex;
					bitIndex = 63;
				}
				lastWordIndex = -1;
				lastBitIndex = -1;
			}
		};
	}

	/**
	 * @return a string representation of this {@code BitSet} in the same format as {@link BitSet#toString()}, e.g.
	 * the indices of all the set bits in ascending order surrounded by curly brackets {@code "{}"}.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(size * 10); // heuristic
		builder.append("{");

		boolean started = false;
		for (int i = 0; i < size; i++) {
			long word = words[i];
			long index = indices[i];
			for (long bitIndex = 0; bitIndex < 64; bitIndex++) {
				long bit = 1L << bitIndex;
				if ((word & bit) != 0) {
					if (started)
						builder.append(", ");
					else
						started = true;
					builder.append((index << 6) + bitIndex);
				}
			}
		}

		builder.append("}");
		return builder.toString();
	}

	private int findWord(long i) {
		return Arrays.binarySearch(indices, 0, size, i >> 6);
	}

	private void insert(long i, int insertionPoint) {
		if (words.length == size) {
			words = Arrays.copyOf(words, words.length + (words.length >> 1));
			indices = Arrays.copyOf(indices, indices.length + (words.length >> 1));
		}
		System.arraycopy(words, insertionPoint, words, insertionPoint + 1, size - insertionPoint);
		System.arraycopy(indices, insertionPoint, indices, insertionPoint + 1, size - insertionPoint);
		words[insertionPoint] = 1L << (i & 63);
		indices[insertionPoint] = i >> 6;
		size++;
	}

	private void removeBit(int wordIndex, long bitIndex) {
		words[wordIndex] &= ~(1L << bitIndex);
		if (words[wordIndex] == 0)
			removeWord(wordIndex);
	}

	private void removeWord(int index) {
		System.arraycopy(words, index + 1, words, index, size - index - 1);
		System.arraycopy(indices, index + 1, indices, index, size - index - 1);
		words[size - 1] = 0;
		indices[size - 1] = 0;
		size--;
	}
}
