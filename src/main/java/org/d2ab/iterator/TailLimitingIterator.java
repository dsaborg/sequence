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

package org.d2ab.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator that limits the results to a set number of items at the end of another iterator.
 */
public class TailLimitingIterator<T> extends DelegatingUnaryIterator<T> {
	private final int limit;

	private boolean started;
	private Object[] buffer;
	private int offset;
	private int index;
	private int size;

	public TailLimitingIterator(Iterator<T> iterator, int limit) {
		super(iterator);
		this.limit = limit;
	}

	@Override
	public boolean hasNext() {
		if (!started) {
			buffer = new Object[limit];
			int i = 0;
			while (iterator.hasNext()) {
				buffer[i] = iterator.next();
				i = ++i % limit;
				if (size < limit)
					size++;
			}

			offset = i % size;
			started = true;
		}

		return index < size;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		return (T) buffer[(offset + index++) % limit];
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
