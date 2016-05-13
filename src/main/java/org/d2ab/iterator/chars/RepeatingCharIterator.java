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

import org.d2ab.iterable.chars.CharIterable;

import java.util.NoSuchElementException;

/**
 * An {@link CharIterator} that cycles the values of an {@link CharIterable} forever. This class repeatedly calls
 * {@link CharIterable#iterator()} to receive new values when the iterator ends, so it's possible to cause this
 * {@link CharIterator} to terminate by providing an empty {@link CharIterator}. If the {@link CharIterable} never
 * returns an empty {@link CharIterator}, this {@link CharIterator} will never terminate.
 */
public class RepeatingCharIterator extends UnaryCharIterator {
	private final CharIterable iterable;
	private int times;

	public RepeatingCharIterator(CharIterable iterable, int times) {
		super(CharIterator.EMPTY);
		this.iterable = iterable;
		this.times = times;
	}

	@Override
	public boolean hasNext() {
		if (!iterator.hasNext() && times != 0) {
			if (times > 0)
				times--;

			iterator = iterable.iterator();
		}
		return iterator.hasNext();
	}

	@Override
	public char nextChar() {
		if (!hasNext())
			throw new NoSuchElementException();

		return iterator.nextChar();
	}
}
