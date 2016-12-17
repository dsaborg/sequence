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
import java.util.function.Consumer;

/**
 * A {@link List} view of an {@link Iterable}, reflecting changes to the underlying {@link Iterable}. The list does not
 * implement {@link RandomAccess}, and is best accessed in sequence. The list supports removal operations, by using
 * {@link Iterator#remove()} if implemented in the {@link Iterable}'s {@link Iterator}. Add and set operations are
 * supported only if {@link #listIterator(int)} is overridden with a {@link ListIterator} that supports add and set.
 * The default {@link ListIterator} supports forward traversal only.
 *
 * @since 1.2
 */
public class IterableList<T> extends AbstractSequentialList<T> {
	private final Iterable<T> iterable;

	public IterableList(Iterable<T> iterable) {
		this.iterable = iterable;
	}

	@Override
	public Iterator<T> iterator() {
		return iterable.iterator();
	}

	@Override
	public int size() {
		return Iterators.count(iterator());
	}

	@Override
	public boolean isEmpty() {
		return !iterator().hasNext();
	}

	@Override
	public Spliterator<T> spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), 0);
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		iterable.forEach(action);
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
