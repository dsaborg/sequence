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

import java.util.NoSuchElementException;
import java.util.function.LongPredicate;

public class InclusiveStartingLongIterator extends DelegatingUnaryLongIterator {
	private final LongPredicate predicate;

	private boolean started;
	private long next;
	private boolean hasNext;

	public InclusiveStartingLongIterator(LongIterator iterator, long element) {
		this(iterator, l -> l == element);
	}

	public InclusiveStartingLongIterator(LongIterator iterator, LongPredicate predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (!started) {
			while (iterator.hasNext()) {
				next = iterator.nextLong();
				if (predicate.test(next)) {
					hasNext = true;
					break;
				}
			}
			started = true;
		}
		return hasNext;
	}

	@Override
	public long nextLong() {
		if (!hasNext())
			throw new NoSuchElementException();

		long result = next;
		hasNext = iterator.hasNext();
		if (hasNext)
			next = iterator.nextLong();
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
