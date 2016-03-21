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

package org.d2ab.iterator.longs;

import org.d2ab.function.ints.IntBiPredicate;
import org.d2ab.function.longs.LongBiPredicate;
import org.d2ab.iterator.DelegatingIterator;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.sequence.IntSequence;
import org.d2ab.sequence.LongSequence;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * A {@link CharIterator} that can batch up another iterator by comparing two items in sequence and deciding whether
 * to split up in a batch on those items.
 */
public class PredicatePartitioningLongIterator<T> extends DelegatingIterator<Long, LongIterator, LongSequence> {
	private final LongBiPredicate predicate;
	private long next;
	private boolean hasNext;

	public PredicatePartitioningLongIterator(LongIterator iterator, LongBiPredicate predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (!hasNext && super.hasNext()) {
			next = iterator.nextLong();
			hasNext = true;
		}
		return hasNext;
	}

	@Override
	public LongSequence next() {
		if (!hasNext())
			throw new NoSuchElementException();

		long[] buffer = new long[3];
		int size = 0;
		do {
			if (buffer.length == size)
				buffer = Arrays.copyOf(buffer, buffer.length * 2);
			buffer[size++] = next;

			hasNext = iterator.hasNext();
			if (!hasNext)
				break;
			long following = iterator.nextLong();
			boolean split = predicate.test(next, following);
			next = following;
			if (split)
				break;
		} while (hasNext);
		if (buffer.length > size)
			buffer = Arrays.copyOf(buffer, size);
		return LongSequence.of(buffer);
	}
}
