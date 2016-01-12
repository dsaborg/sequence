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

import org.d2ab.collection.Tracker;

import java.util.NoSuchElementException;

public class DistinctCharIterator extends UnaryCharIterator {
	private static final int THRESHOLD = 256;

	private final Tracker seen = new Tracker(THRESHOLD);

	private char next;
	private boolean hasNext;

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
			char next = iterator.nextChar();
			hasNext = seen.add(next);
			if (hasNext)
				this.next = next;
		}

		return hasNext;
	}
}
