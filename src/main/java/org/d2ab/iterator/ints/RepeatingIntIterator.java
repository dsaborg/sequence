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

package org.d2ab.iterator.ints;

import org.d2ab.iterable.ints.IntIterable;

import java.util.NoSuchElementException;

/**
 * An {@link IntIterator} that cycles the values of an {@link IntIterable} forever. This class repeatedly calls
 * {@link IntIterable#iterator()} to receive new values when the iterator ends, so it's possible to cause this
 * {@link IntIterator} to terminate by providing an empty {@link IntIterator}. If the {@link IntIterable} never
 * returns an empty {@link IntIterator}, this {@link IntIterator} will never terminate.
 */
public class RepeatingIntIterator extends UnaryIntIterator {
	private final IntIterable iterable;
	private long times;

	public RepeatingIntIterator(IntIterable iterable, long times) {
		this.iterable = iterable;
		this.times = times;
	}

	@Override
	public boolean hasNext() {
		if ((iterator == null || !iterator.hasNext()) && times != 0) {
			if (times > 0)
				times--;

			iterator = iterable.iterator();
		}
		return iterator != null && iterator.hasNext();
	}

	@Override
	public int nextInt() {
		if (!hasNext())
			throw new NoSuchElementException();

		return iterator.nextInt();
	}
}
