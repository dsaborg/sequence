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

public class SkippingCharIterator extends UnaryCharIterator {
	private final long skip;

	private boolean skipped;

	public SkippingCharIterator(long skip) {
		this.skip = skip;
	}

	@Override
	public char nextChar() {
		if (!hasNext())
			throw new NoSuchElementException();

		return iterator.nextChar();
	}

	@Override
	public boolean hasNext() {
		if (!skipped) {
			iterator.skip(skip);
			skipped = true;
		}

		return iterator.hasNext();
	}
}
