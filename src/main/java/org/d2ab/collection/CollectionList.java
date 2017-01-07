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

import org.d2ab.iterator.ForwardListIterator;
import org.d2ab.iterator.Iterators;

import java.util.*;
import java.util.function.Predicate;

/**
 * A {@link List} view of a {@link Collection}, reflecting changes to the underlying {@link Collection}. The list does
 * not implement {@link RandomAccess}, and is best accessed in sequence. The list supports removal by object and add
 * at end operations, but not setting or indexed remove/add operations. These operations are supported only if
 * {@link #listIterator(int)} is overridden with a {@link ListIterator} that supports add and set.
 * The default {@link ListIterator} supports forward traversal only.
 *
 * @since 2.1
 */
public class CollectionList<T> extends AbstractSequentialList<T> {
	private final Collection<T> collection;

	public CollectionList(Collection<T> collection) {
		this.collection = collection;
	}

	@Override
	public int size() {
		return collection.size();
	}

	@Override
	public boolean isEmpty() {
		return collection.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return collection.iterator();
	}

	@Override
	public boolean contains(Object o) {
		return collection.contains(o);
	}

	@Override
	public Object[] toArray() {
		return collection.toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		return collection.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return collection.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return collection.containsAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return collection.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return collection.retainAll(c);
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		return collection.removeIf(filter);
	}

	@Override
	public void clear() {
		collection.clear();
	}

	@Override
	public int lastIndexOf(Object o) {
		int lastIndex = -1;
		int index = 0;
		for (T each : this) {
			if (Objects.equals(o, each))
				lastIndex = index;
			index++;
		}
		return lastIndex;
	}

	public ListIterator<T> listIterator(int index) {
		ListIterator<T> listIterator = new ForwardListIterator<>(iterator());

		int skipped = Iterators.skip(listIterator, index);
		if (skipped < index)
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + skipped);

		return listIterator;
	}
}
