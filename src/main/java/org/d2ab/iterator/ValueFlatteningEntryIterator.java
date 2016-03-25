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

import org.d2ab.util.Entries;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ValueFlatteningEntryIterator<K, V, VV>
		extends DelegatingReferenceIterator<Map.Entry<K, V>, Map.Entry<K, VV>> {
	private final Function<? super Map.Entry<K, V>, ? extends Iterable<VV>> mapper;

	private Iterator<VV> valueIterator = Iterators.empty();
	private Map.Entry<K, V> entry;

	public ValueFlatteningEntryIterator(Iterator<Map.Entry<K, V>> iterator,
	                                    Function<? super Map.Entry<K, V>, ? extends Iterable<VV>> mapper) {
		super(iterator);
		this.mapper = mapper;
	}

	@Override
	public boolean hasNext() {
		while (!valueIterator.hasNext() && iterator.hasNext()) {
			entry = iterator.next();
			valueIterator = mapper.apply(entry).iterator();
		}
		return valueIterator.hasNext();
	}

	@Override
	public Map.Entry<K, VV> next() {
		if (!hasNext())
			throw new NoSuchElementException();

		return Entries.of(entry.getKey(), valueIterator.next());
	}
}
