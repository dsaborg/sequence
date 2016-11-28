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

import org.d2ab.collection.longs.LongList;
import org.d2ab.iterator.DelegatingTransformingIterator;
import org.d2ab.sequence.LongSequence;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.LongPredicate;

/**
 * An {@link Iterator} that can batch up another iterator by comparing two items in sequence and deciding whether
 * to split up in a batch on those items.
 */
public class SplittingLongIterator extends DelegatingTransformingIterator<Long, LongIterator, LongSequence> {
	private final LongPredicate predicate;

	public SplittingLongIterator(LongIterator iterator, long element) {
		this(iterator, l -> l == element);
	}

	public SplittingLongIterator(LongIterator iterator, LongPredicate predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public LongSequence next() {
		if (!hasNext())
			throw new NoSuchElementException();

		LongList buffer = LongList.create();
		while (iterator.hasNext()) {
			long next = iterator.nextLong();
			if (predicate.test(next))
				break;
			buffer.addLong(next);
		}

		return LongSequence.from(buffer);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
