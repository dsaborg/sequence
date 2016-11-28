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

import org.d2ab.function.CharPredicate;

import java.util.NoSuchElementException;

public class ExclusiveStartingCharIterator extends DelegatingUnaryCharIterator {
	private final CharPredicate predicate;

	private boolean started;

	public ExclusiveStartingCharIterator(CharIterator iterator, char element) {
		this(iterator, c -> c == element);
	}

	public ExclusiveStartingCharIterator(CharIterator iterator, CharPredicate predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (!started) {
			while (iterator.hasNext())
				if (predicate.test(iterator.nextChar()))
					break;
			started = true;
		}
		return super.hasNext();
	}

	@Override
	public char nextChar() {
		if (!hasNext())
			throw new NoSuchElementException();

		return iterator.nextChar();
	}
}
