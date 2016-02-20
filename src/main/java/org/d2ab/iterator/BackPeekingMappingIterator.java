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

import java.util.Iterator;
import java.util.function.BiFunction;

/**
 * An iterator that maps each element by looking at the current AND the previous element.
 */
public class BackPeekingMappingIterator<T, U> extends DelegatingReferenceIterator<T, U> {
	private final BiFunction<? super T, ? super T, ? extends U> mapper;
	private T previous;

	public BackPeekingMappingIterator(Iterator<T> iterator, BiFunction<? super T, ? super T, ? extends U> mapper) {
		super(iterator);
		this.mapper = mapper;
	}

	@Override
	public U next() {
		T next = iterator.next();

		U mapped = mapper.apply(previous, next);
		previous = next;
		return mapped;
	}
}
