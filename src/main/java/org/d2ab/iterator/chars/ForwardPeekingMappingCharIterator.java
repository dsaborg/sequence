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

package org.d2ab.iterator.chars;

import org.d2ab.function.chars.CharBinaryOperator;

import java.util.NoSuchElementException;

/**
 * An iterator over chars that also maps each element by looking at the current AND the next element.
 */
public class ForwardPeekingMappingCharIterator extends UnaryCharIterator {
	private final char lastNext;
	private final CharBinaryOperator mapper;

	private char current;
	private boolean hasCurrent;
	private boolean started;

	public ForwardPeekingMappingCharIterator(char lastNext, CharBinaryOperator mapper) {
		this.lastNext = lastNext;
		this.mapper = mapper;
	}

	@Override
	public boolean hasNext() {
		if (!started) {
			if (iterator.hasNext()) {
				current = iterator.next();
				hasCurrent = true;
			}
			started = true;
		}
		return hasCurrent;
	}

	@Override
	public char nextChar() {
		if (!hasNext())
			throw new NoSuchElementException();

		boolean hasNext = iterator.hasNext();
		char next = hasNext ? iterator.nextChar() : lastNext;

		char result = mapper.applyAsChar(current, next);
		current = next;
		hasCurrent = hasNext;
		return result;
	}
}
