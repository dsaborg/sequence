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

import org.d2ab.collection.chars.BitCharSet;
import org.d2ab.collection.chars.CharSet;

import java.util.NoSuchElementException;

public class DistinctCharIterator extends DelegatingUnaryCharIterator {
	private final CharSet seen = new BitCharSet();

	private char next;
	private boolean hasNext;

	public DistinctCharIterator(CharIterator iterator) {
		super(iterator);
	}

	@Override
	public char nextChar() {
		if (!hasNext())
			throw new NoSuchElementException();

		hasNext = false;
		return next;
	}

	@Override
	public boolean hasNext() {
		if (hasNext)
			return true;

		while (!hasNext && iterator.hasNext()) {
			char maybeNext = iterator.nextChar();
			if (hasNext = seen.addChar(maybeNext))
				next = maybeNext;
		}

		return hasNext;
	}
}
