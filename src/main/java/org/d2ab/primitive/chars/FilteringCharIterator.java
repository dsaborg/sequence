/*
 * Copyright 2015 Daniel Skogquist Åborg
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
package org.d2ab.primitive.chars;

import java.util.NoSuchElementException;

public class FilteringCharIterator implements CharIterator {
	private final CharIterator iterator;
	private final CharPredicate predicate;
	char nextValue;
	private boolean foundNext;

	public FilteringCharIterator(CharIterator iterator, CharPredicate predicate) {
		this.iterator = iterator;
		this.predicate = predicate;
	}

	@Override
	public char nextChar() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		foundNext = false;
		return nextValue;
	}

	@Override
	public boolean hasNext() {
		if (foundNext) { // already checked
			return true;
		}

		do { // find next matching, bail out if EOF
			foundNext = iterator.hasNext();
			if (!foundNext)
				return false;
			nextValue = iterator.nextChar();
		} while (!predicate.test(nextValue));

		// found matching value
		return true;
	}
}
