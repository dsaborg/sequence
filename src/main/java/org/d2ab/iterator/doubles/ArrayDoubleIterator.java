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

package org.d2ab.iterator.doubles;

import java.util.Iterator;

/**
 * An {@link Iterator} over an array of items.
 */
public class ArrayDoubleIterator implements DoubleIterator {
	private double[] array;
	private int offset;
	private int size;
	private int index;

	public ArrayDoubleIterator(double... array) {
		this(array, array.length);
	}

	public ArrayDoubleIterator(double[] array, int size) {
		this(array, 0, size);
	}

	public ArrayDoubleIterator(double[] array, int offset, int size) {
		if (offset > array.length || offset < 0)
			throw new IndexOutOfBoundsException("offset: " + offset + ", length: " + array.length);
		if (offset + size > array.length || size < 0)
			throw new IndexOutOfBoundsException("size: " + size + ", length - offset: " + (array.length - offset));
		this.array = array;
		this.offset = offset;
		this.size = size;
	}

	@Override
	public boolean hasNext() {
		return index < size;
	}

	@Override
	public double nextDouble() {
		return array[offset + index++];
	}
}
