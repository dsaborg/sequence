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

package org.d2ab.collection;

import org.d2ab.iterator.MappingIterator;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

/**
 * A {@link Collection} that presents a mapped view of another {@link Collection}.
 */
public class MappedCollection<T, U> extends AbstractCollection<U> {
	protected final Collection<T> collection;
	private final Function<? super T, ? extends U> mapper;

	public static <T, U> Collection<U> from(Collection<T> collection, Function<? super T, ? extends U> mapper) {
		return new MappedCollection<>(collection, mapper);
	}

	protected MappedCollection(Collection<T> collection, Function<? super T, ? extends U> mapper) {
		this.collection = collection;
		this.mapper = mapper;
	}

	@Override
	public Iterator<U> iterator() {
		return new MappingIterator<>(collection.iterator(), mapper);
	}

	@Override
	public int size() {
		return collection.size();
	}
}
