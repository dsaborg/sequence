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

package org.d2ab.sequence.iterator;

import org.d2ab.collection.iterator.DelegatingReferenceIterator;
import org.d2ab.function.ObjIntFunction;

import java.util.Iterator;

/**
 * An iterator mapping elements with the index of the current element.
 */
public class IndexingMappingIterator<T, U> extends DelegatingReferenceIterator<T, U> {
	private final ObjIntFunction<? super T, ? extends U> mapper;
	private int index;

	public IndexingMappingIterator(Iterator<T> iterator, ObjIntFunction<? super T, ? extends U> mapper) {
		super(iterator);
		this.mapper = mapper;
	}

	@Override
	public U next() {
		return mapper.apply(iterator.next(), index++);
	}
}
