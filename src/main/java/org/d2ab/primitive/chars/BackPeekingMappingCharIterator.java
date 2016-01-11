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

/**
 * An iterator over chars that also maps each element by looking at the current AND the previous element.
 */
public class BackPeekingMappingCharIterator extends UnaryCharIterator {
	private final IntCharToCharBinaryFunction mapper;
	private int previous = -1;

	public BackPeekingMappingCharIterator(IntCharToCharBinaryFunction mapper) {
		this.mapper = mapper;
	}

	@Override
	public char nextChar() {
		char next = iterator.nextChar();

		char result = mapper.applyAsIntAndChar(previous, next);

		previous = next;
		return result;
	}
}
