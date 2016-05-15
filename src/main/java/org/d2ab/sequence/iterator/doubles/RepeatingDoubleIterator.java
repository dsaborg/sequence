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

package org.d2ab.sequence.iterator.doubles;

import org.d2ab.collection.DoubleIterable;
import org.d2ab.collection.iterator.DoubleIterator;
import org.d2ab.collection.iterator.UnaryDoubleIterator;

import java.util.NoSuchElementException;

/**
 * An {@link DoubleIterator} that cycles the values of an {@link DoubleIterable} forever. This class repeatedly calls
 * {@link DoubleIterable#iterator()} to receive new values when the iterator ends, so it's possible to cause this
 * {@link DoubleIterator} to terminate by providing an empty {@link DoubleIterator}. If the {@link DoubleIterable} never
 * returns an empty {@link DoubleIterator}, this {@link DoubleIterator} will never terminate.
 */
public class RepeatingDoubleIterator extends UnaryDoubleIterator {
	private final DoubleIterable iterable;
	private int times;

	public RepeatingDoubleIterator(DoubleIterable iterable, int times) {
		super(DoubleIterator.EMPTY);
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
	public double nextDouble() {
		if (!hasNext())
			throw new NoSuchElementException();

		return iterator.nextDouble();
	}
}
