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

import org.d2ab.iterator.FilteringIterator;
import org.d2ab.iterator.Iterators;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

import static org.d2ab.collection.SizedIterable.SizeType.UNKNOWN;

/**
 * A {@link Collection} that provides a filtered view of another {@link Collection}. All operations are supported
 * except variations of {@link Collection#add}.
 *
 * @since 2.1
 */
public class FilteredCollection<T> extends AbstractCollection<T> implements SizedIterable<T> {
	private final Collection<T> collection;
	private final Predicate<? super T> predicate;

	private FilteredCollection(Collection<T> collection, Predicate<? super T> predicate) {
		this.collection = collection;
		this.predicate = predicate;
	}

	public static <T> Collection<T> from(Collection<T> collection, Predicate<? super T> predicate) {
		return new FilteredCollection<>(collection, predicate);
	}

	@Override
	public Iterator<T> iterator() {
		return new FilteringIterator<>(collection.iterator(), predicate);
	}

	@Override
	public SizeType sizeType() {
		return UNKNOWN;
	}

	@Override
	public int size() {
		return Iterators.size(iterator());
	}

	@Override
	public boolean add(T t) {
		if (!predicate.test(t))
			throw new IllegalArgumentException(String.valueOf(t));

		return collection.add(t);
	}
}
