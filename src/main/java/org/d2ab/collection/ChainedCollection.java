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

import org.d2ab.iterator.ChainingIterator;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static java.util.Arrays.asList;

/**
 * A {@link Collection} of multiple {@link Collection}s strung together in a chain.
 */
public class ChainedCollection<T> extends AbstractCollection<T> {
	private final Collection<Collection<T>> collections;

	@SuppressWarnings("unchecked")
	public static <T> Collection<T> concat(Collection<T>... collections) {
		return concat(asList(collections));
	}

	public static <T> Collection<T> concat(Collection<Collection<T>> collections) {
		return new ChainedCollection<>(collections);
	}

	private ChainedCollection(Collection<Collection<T>> collections) {
		this.collections = collections;
	}

	@Override
	public Iterator<T> iterator() {
		return new ChainingIterator<>(collections);
	}

	@Override
	public int size() {
		int size = 0;
		for (Collection<T> collection : collections)
			size += collection.size();
		return size;
	}

	@Override
	public void clear() {
		for (Collection<T> c : collections)
			c.clear();
	}

	@Override
	public boolean isEmpty() {
		for (Collection<T> collection : collections)
			if (!collection.isEmpty())
				return false;

		return true;
	}

	@Override
	public boolean add(T t) {
		if (collections.isEmpty())
			collections.add(new ArrayList<>());

		Collection<T> target = null;
		for (Collection<T> collection : collections) {
			if (target == null || !collection.isEmpty())
				target = collection;
		}

		//noinspection ConstantConditions
		return target.add(t);
	}
}
