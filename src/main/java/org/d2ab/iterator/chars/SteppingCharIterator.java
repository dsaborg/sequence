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

import java.util.NoSuchElementException;

public class SteppingCharIterator extends UnaryCharIterator {
	private final long step;

	private boolean hasNext;
	private char next;

	public SteppingCharIterator(CharIterator iterator, long step) {
		super(iterator);
		this.step = step;
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

		if (!iterator.hasNext())
			return false;

		next = iterator.nextChar();

		iterator.skip(step - 1);
		hasNext = true;

		return true;
	}
}
