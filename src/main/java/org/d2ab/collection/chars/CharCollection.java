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

package org.d2ab.collection.chars;

import org.d2ab.collection.Collectionz;
import org.d2ab.collection.ints.IntIterable;
import org.d2ab.iterator.ints.IntIterator;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;

/**
 * A primitive specialization of {@link Collection} for {code char} values. Supplements all {@link Character}-valued
 * methods with corresponding {@code char}-valued methods.
 */
public interface CharCollection extends Collection<Character>, CharIterable {
	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default void clear() {
		iterator().removeAll();
	}

	@Override
	default Character[] toArray() {
		return toArray(new Character[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return Collectionz.toArray(this, a);
	}

	/**
	 * Collect the {@code chars} in this {@code CharCollection} into an {@code char}-array.
	 */
	default char[] toCharArray() {
		return new ArrayCharList(this).toCharArray();
	}

	@Override
	default boolean add(Character i) {
		return addChar(i);
	}

	default boolean addChar(char i) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean contains(Object o) {
		return o instanceof Character && containsChar((char) o);
	}

	@Override
	default boolean remove(Object o) {
		return o instanceof Character && removeChar((char) o);
	}

	@Override
	default boolean addAll(Collection<? extends Character> c) {
		return Collectionz.addAll(this, c);
	}

	default boolean addAllChars(char... is) {
		boolean changed = false;
		for (char i : is)
			changed |= addChar(i);
		return changed;
	}

	default boolean addAllChars(CharCollection is) {
		if (is.isEmpty())
			return false;

		is.forEachChar(this::addChar);
		return true;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return Collectionz.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return Collectionz.removeAll(this, c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return Collectionz.retainAll(this, c);
	}

	@Override
	default boolean removeIf(Predicate<? super Character> filter) {
		return removeCharsIf(filter::test);
	}
}
