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
	private long first;
	private boolean firstFound;
	private boolean firstEmitted;

	public InclusiveStartingLongIterator(LongIterator iterator, long element) {
		this(iterator, i -> i == element);
	}

	public InclusiveStartingLongIterator(LongIterator iterator, LongPredicate predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (!started) {
			while (iterator.hasNext() && !firstFound) {
				long candidateFirst = iterator.nextLong();
				if (predicate.test(candidateFirst)) {
					firstFound = true;
					first = candidateFirst;
				}
			}
			started = true;
			return firstFound;
		} else {
			if (!firstEmitted)
				return firstFound;
			else
				return iterator.hasNext();
		}
	}

	@Override
	public long nextLong() {
		if (!hasNext())
			throw new NoSuchElementException();

		if (!firstEmitted) {
			firstEmitted = true;
			return first;
		} else {
			return iterator.nextLong();
		}
	}
}
