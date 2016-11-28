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

package org.d2ab.iterator.ints;

import java.util.NoSuchElementException;
import java.util.function.IntPredicate;

public class InclusiveStartingIntIterator extends DelegatingIntIterator {
	private final IntPredicate predicate;

	private boolean started;
	private int first;
	private boolean firstFound;
	private boolean firstEmitted;

	public InclusiveStartingIntIterator(IntIterator iterator, int element) {
		this(iterator, i -> i == element);
	}

	public InclusiveStartingIntIterator(IntIterator iterator, IntPredicate predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (!started) {
			while (iterator.hasNext() && !firstFound) {
				int candidateFirst = iterator.nextInt();
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
	public int nextInt() {
		if (!hasNext())
			throw new NoSuchElementException();

		if (!firstEmitted) {
			firstEmitted = true;
			return first;
		} else {
			return iterator.nextInt();
		}
	}
}
