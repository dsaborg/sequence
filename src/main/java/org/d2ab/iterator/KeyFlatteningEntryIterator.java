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

public class KeyFlatteningEntryIterator<
		K, V, KK> extends DelegatingReferenceIterator<Map.Entry<K, V>, Map.Entry<KK, V>> {
	private final Function<? super Map.Entry<K, V>, ? extends Iterable<KK>> mapper;

	private Iterator<KK> keyIterator = Iterators.empty();
	private Map.Entry<K, V> entry;

	public KeyFlatteningEntryIterator(Iterator<Map.Entry<K, V>> iterator,
	                                  Function<? super Map.Entry<K, V>, ? extends Iterable<KK>> mapper) {
		super(iterator);
		this.mapper = mapper;
	}

	@Override
	public boolean hasNext() {
		while (!keyIterator.hasNext() && iterator.hasNext()) {
			entry = iterator.next();
			keyIterator = mapper.apply(entry).iterator();
		}
		return keyIterator.hasNext();
	}

	@Override
	public Map.Entry<KK, V> next() {
		if (!hasNext())
			throw new NoSuchElementException();

		return Entries.of(keyIterator.next(), entry.getValue());
	}
}
