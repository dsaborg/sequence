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

package org.d2ab.primitive.longs;

import java.util.NoSuchElementException;

/**
 * An {@link LongIterator} that cycles the values of an {@link LongIterable} forever. This class repeatedly calls
 * {@link LongIterable#iterator()} to receive new values when the iterator ends, so it's possible to cause this
 * {@link LongIterator} to terminate by providing an empty {@link LongIterator}. If the {@link LongIterable} never
 * returns an empty {@link LongIterator}, this {@link LongIterator} will never terminate.
 */
public class RepeatingLongIterator extends UnaryLongIterator {
	private final LongIterable iterable;

	public RepeatingLongIterator(LongIterable iterable) {
		this.iterable = iterable;
	}

	@Override
	public boolean hasNext() {
		if (iterator == null || !iterator.hasNext()) {
			iterator = iterable.iterator();
		}
		return iterator.hasNext();
	}

	@Override
	public long nextLong() {
		if (!hasNext())
			throw new NoSuchElementException();

		return iterator.nextLong();
	}
}
