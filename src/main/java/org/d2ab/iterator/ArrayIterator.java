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
package org.d2ab.iterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An {@link Iterator} over an array of items.
 */
public class ArrayIterator<T> implements Iterator<T> {
	private T[] items;
	private int index;

	@SafeVarargs
	public ArrayIterator(T... items) {
		this.items = items;
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(items);
		result = 31 * result + index;
		return result;
	}	@Override
	public boolean hasNext() {
		return index < items.length;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ArrayIterator<?> that = (ArrayIterator<?>) o;

		return index == that.index && Arrays.equals(items, that.items);
	}	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		return items[index++];
	}

	@Override
	public String toString() {
		return "ArrayIterator(" + Arrays.toString(items) + '@' + index + ')';
	}




}
