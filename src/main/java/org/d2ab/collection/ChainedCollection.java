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

import java.util.*;

import static org.d2ab.collection.SizedIterable.SizeType.*;

/**
 * A {@link Collection} of multiple {@link Collection}s strung together in a chain.
 */
public class ChainedCollection<T> extends AbstractCollection<T> implements SizedIterable<T> {
	private final Collection<Collection<T>> collections;
	private final SizeType sizeType;

	@SuppressWarnings("unchecked")
	public static <T> Collection<T> concat(Collection<T>... collections) {
		return concat(Lists.of(collections));
	}

	public static <T> Collection<T> concat(Collection<Collection<T>> collections) {
		return new ChainedCollection<>(collections);
	}

	private ChainedCollection(Collection<Collection<T>> collections) {
		this.collections = collections;
		this.sizeType = Iterables.sizeType(collections);
	}

	@Override
	public Iterator<T> iterator() {
		return new ChainingIterator<>(collections);
	}

	@Override
	public int size() {
		if (sizeType == INFINITE)
			throw new UnsupportedOperationException();

		int size = 0;
		for (Collection<T> collection : collections)
			size += collection.size();
		return size;
	}

	@Override
	public SizeType sizeType() {
		if (sizeType == INFINITE)
			throw new UnsupportedOperationException();

		SizeType sizeType = this.sizeType != FIXED ? AVAILABLE : FIXED;

		for (Collection<T> collection : collections)
			sizeType = sizeType.concat(Iterables.sizeType(collection));

		return sizeType;
	}

	@Override
	public void clear() {
		if (sizeType == INFINITE)
			throw new UnsupportedOperationException();

		for (Collection<T> c : collections)
			c.clear();
	}

	@Override
	public boolean isEmpty() {
		if (sizeType == INFINITE)
			throw new UnsupportedOperationException();

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

	@Override
	public Spliterator<T> spliterator() {
		if (sizeType == INFINITE || sizeType() == INFINITE)
			return Spliterators.spliteratorUnknownSize(iterator(), 0);

		return Spliterators.spliterator(this, 0);
	}
}
