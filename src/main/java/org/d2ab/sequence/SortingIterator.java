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

package org.d2ab.sequence;

import java.util.*;

public class SortingIterator<T> implements Iterator<T> {
	private Iterator<T> iterator;
	private Comparator<? super T> comparator;
	private Iterator<T> sortedIterator;

	public SortingIterator(Iterator<T> iterator) {
		this(iterator, (Comparator<? super T>) Comparator.naturalOrder());
	}

	public SortingIterator(Iterator<T> iterator, Comparator<? super T> comparator) {
		this.iterator = iterator;
		this.comparator = comparator;
	}

	@Override
	public boolean hasNext() {
		if (sortedIterator == null) {
			List<T> elements = new ArrayList<T>();
			while (iterator.hasNext())
				elements.add(iterator.next());
			elements.sort(comparator);
			sortedIterator = elements.iterator();
		}
		return sortedIterator.hasNext();
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		return sortedIterator.next();
	}
}
