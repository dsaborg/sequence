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
 * An {@link Iterator} over an array of items iterating in reverse order.
 */
public class ReverseArrayIterator<T> implements Iterator<T> {
	private final T[] items;
	private int index;

	@SafeVarargs
	public ReverseArrayIterator(T... items) {
		this.items = items;
		this.index = items.length - 1;
	}

	@Override
	public boolean hasNext() {
		return index >= 0;
	}

	@Override
	public T next() {
		if (index < 0)
			throw new NoSuchElementException();

		return items[index--];
	}
}
