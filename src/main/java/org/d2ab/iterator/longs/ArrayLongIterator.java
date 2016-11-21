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

package org.d2ab.iterator.longs;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An {@link Iterator} over an array of items.
 */
public class ArrayLongIterator implements LongIterator {
	private final long[] values;
	private final int offset;
	private final int size;

	private int index;

	public ArrayLongIterator(long... values) {
		this(values, values.length);
	}

	public ArrayLongIterator(long[] values, int size) {
		this(values, 0, size);
	}

	public ArrayLongIterator(long[] values, int offset, int size) {
		if (offset > values.length || offset < 0)
			throw new IndexOutOfBoundsException("offset: " + offset + ", length: " + values.length);
		if (offset + size > values.length || size < 0)
			throw new IndexOutOfBoundsException("size: " + size + ", length - offset: " + (values.length - offset));
		this.values = values;
		this.offset = offset;
		this.size = size;
	}

	@Override
	public boolean hasNext() {
		return index < size;
	}

	@Override
	public long nextLong() {
		if (!hasNext())
			throw new NoSuchElementException();

		return values[offset + index++];
	}
}
