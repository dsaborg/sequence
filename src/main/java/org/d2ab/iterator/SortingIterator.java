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

import javax.annotation.Nullable;
import java.util.*;

public class SortingIterator<T> extends UnaryReferenceIterator<T> {
	private final Comparator<? super T> comparator;

	private Iterator<T> sortedIterator;

	public SortingIterator(Iterator<T> iterator) {
		this(iterator, naturalOrder());
	}

	public SortingIterator(Iterator<T> iterator, Comparator<? super T> comparator) {
		super(iterator);
		this.comparator = comparator;
	}

	private static <T> Comparator<? super T> naturalOrder() {
		@SuppressWarnings("unchecked")
		Comparator<? super T> comparator = (Comparator<? super T>) Comparator.naturalOrder();
		return comparator;
	}

	@Override
	public boolean hasNext() {
		if (sortedIterator == null) {
			List<T> elements = new ArrayList<>();
			while (iterator.hasNext())
				elements.add(iterator.next());
			elements.sort(comparator);
			sortedIterator = elements.iterator();
		}
		return sortedIterator.hasNext();
	}

	@Override
	@Nullable
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		return sortedIterator.next();
	}
}
