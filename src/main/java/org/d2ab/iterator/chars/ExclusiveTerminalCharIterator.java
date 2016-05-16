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

public class ExclusiveTerminalCharIterator extends UnaryCharIterator {
	private final CharPredicate terminal;
	private char next;
	private boolean hasNext;

	public ExclusiveTerminalCharIterator(CharIterator iterator, char terminal) {
		this(iterator, c -> c == terminal);
	}

	public ExclusiveTerminalCharIterator(CharIterator iterator, CharPredicate terminal) {
		super(iterator);
		this.terminal = terminal;
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
		if (!hasNext && iterator.hasNext()) {
			next = iterator.next();
			hasNext = true;
		}
		return hasNext && !terminal.test(next);
	}
}
