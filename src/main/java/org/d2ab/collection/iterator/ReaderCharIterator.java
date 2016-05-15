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

package org.d2ab.collection.iterator;

import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;

/**
 * A {@link CharIterator} over the {@code chars} in a {@link Reader}.
 */
public class ReaderCharIterator implements CharIterator {
	private Reader reader;
	private int next;
	private boolean hasNext;

	public ReaderCharIterator(Reader reader) {
		this.reader = reader;
	}

	@Override
	public boolean hasNext() {
		if (!hasNext) {
			try {
				next = reader.read();
			} catch (IOException e) {
				throw new IterationException(e);
			}

			hasNext = true;
		}

		return next != -1;
	}

	@Override
	public char nextChar() {
		if (!hasNext())
			throw new NoSuchElementException();

		hasNext = false;
		return (char) next;
	}
}
