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

package org.d2ab.iterator.chars;

import org.d2ab.function.chars.CharBiPredicate;

import java.util.NoSuchElementException;

/**
 * Base class for iterators that map the next element by also peeking at the previous element.
 */
public class BackPeekingFilteringCharIterator extends DelegatingCharIterator<Character, CharIterator> {
	private final CharBiPredicate predicate;

	private char next;
	private boolean hasNext;

	public BackPeekingFilteringCharIterator(CharIterator iterator, char firstPrevious, CharBiPredicate predicate) {
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

		char previous;
		do {
			// find next matching, bail out if EOF
			hasNext = iterator.hasNext();
			if (!hasNext)
				return false;
			previous = next;
			next = iterator.nextChar();
		} while (!predicate.test(previous, next));

		// found matching value
		return true;
	}

	@Override
	public char nextChar() {
		if (!hasNext())
			throw new NoSuchElementException();

		hasNext = false;
		return next;
	}
}
