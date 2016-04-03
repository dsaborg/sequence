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

import org.d2ab.function.ints.IntBiPredicate;

import java.util.NoSuchElementException;

/**
 * Base class for iterators that map the next element by also peeking at the previous element.
 */
public class BackPeekingFilteringIntIterator extends DelegatingIntIterator<Integer, IntIterator> {
	private final IntBiPredicate predicate;

	private int next;
	private boolean hasNext;

	public BackPeekingFilteringIntIterator(IntIterator iterator, int firstPrevious, IntBiPredicate predicate) {
		super(iterator);
		this.next = firstPrevious;
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (hasNext) {
			// already checked
			return true;
		}

		int previous;
		do {
			// find next matching, bail out if EOF
			hasNext = iterator.hasNext();
			if (!hasNext)
				return false;
			previous = next;
			next = iterator.nextInt();
		} while (!predicate.test(previous, next));

		// found matching value
		return true;
	}

	@Override
	public int nextInt() {
		if (!hasNext())
			throw new NoSuchElementException();

		hasNext = false;
		return next;
	}
}
