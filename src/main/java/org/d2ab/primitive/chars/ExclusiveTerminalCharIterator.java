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
import java.util.Objects;

public class ExclusiveTerminalCharIterator implements CharIterator {
	private final CharIterator iterator;
	private final char terminal;
	private char next;
	private boolean gotNext;

	public ExclusiveTerminalCharIterator(CharIterator iterator, char terminal) {
		this.iterator = iterator;
		this.terminal = terminal;
	}

	@Override
	public char nextChar() {
		if (!hasNext())
			throw new NoSuchElementException();

		gotNext = false;
		return next;
	}

	@Override
	public boolean hasNext() {
		if (!gotNext && iterator.hasNext()) {
			next = iterator.next();
			gotNext = true;
		}
		return gotNext && !Objects.equals(next, terminal);
	}
}
