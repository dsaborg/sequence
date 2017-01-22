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

import org.d2ab.collection.Iterables;
import org.d2ab.collection.chars.CharIterable;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ChainingCharIterator extends DelegatingUnaryCharIterator {
	private final Iterator<CharIterable> iterables;

	public ChainingCharIterator(CharIterable... iterables) {
		this(Iterables.of(iterables));
	}

	public ChainingCharIterator(Iterable<CharIterable> iterables) {
		super(CharIterator.EMPTY);
		this.iterables = iterables.iterator();
	}

	@Override
	public boolean hasNext() {
		while (!iterator.hasNext() && iterables.hasNext())
			iterator = iterables.next().iterator();
		return iterator.hasNext();
	}

	@Override
	public char nextChar() {
		if (!hasNext())
			throw new NoSuchElementException();

		return iterator.nextChar();
	}
}
