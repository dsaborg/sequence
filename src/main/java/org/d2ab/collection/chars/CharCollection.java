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

import org.d2ab.collection.PrimitiveCollections;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.util.Strict;

import java.util.Collection;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;

/**
 * A primitive specialization of {@link Collection} for {code char} values. Supplements all {@link Character}-valued
 * methods with corresponding {@code char}-valued methods.
 */
public interface CharCollection extends Collection<Character>, CharIterable {
	// TODO: Extract out relevant parts to IterableCharCollection

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
		return PrimitiveCollections.toArray(this, a);
	}

	/**
	 * Collect the {@code chars} in this {@code CharCollection} into an {@code char}-array.
	 */
	default char[] toCharArray() {
		return new ArrayCharList(this).toCharArray();
	}

	/**
	 * @return a {@link CharList} view of this {@code CharCollection}, which is updated in real time as the {@code
	 * CharCollection} changes. The list does not implement {@link RandomAccess} and is best accessed in sequence.
	 *
	 * @since 2.2
	 */
	default CharList asList() {
		return CharList.Base.from(this);
	}

	@Override
	default boolean add(Character x) {
		return CharCollections.add(this, x);
	}

	default boolean addChar(char x) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean contains(Object o) {
		return CharCollections.contains(this, o);
	}

	@Override
	default boolean remove(Object o) {
		return CharCollections.remove(this, o);
	}

	@Override
	default boolean addAll(Collection<? extends Character> c) {
		return CharCollections.addAll(this, c);
	}

	default boolean addAllChars(char... xs) {
		boolean modified = false;
		for (char x : xs)
			modified |= addChar(x);
		return modified;
	}

	default boolean addAllChars(CharCollection xs) {
		boolean modified = false;
		for (CharIterator iterator = xs.iterator(); iterator.hasNext(); )
			modified |= addChar(iterator.nextChar());
		return modified;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return CharCollections.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return CharCollections.removeAll(this, c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return CharCollections.retainAll(this, c);
	}

	@Override
	default boolean removeIf(Predicate<? super Character> filter) {
		Strict.check();

		return removeCharsIf(filter::test);
	}

	@Override
	default Spliterator.OfInt intSpliterator() {
		return Spliterators.spliterator(intIterator(), size(), Spliterator.NONNULL);
	}

	/**
	 * Base class for {@link CharCollection} implementations.
	 */
	abstract class Base implements CharCollection {
		public static CharCollection create(char... chars) {
			return from(CharList.create(chars));
		}

		public static CharCollection from(final CharCollection collection) {
			return new CharCollection.Base() {
				@Override
				public CharIterator iterator() {
					return collection.iterator();
				}

				@Override
				public int size() {
					return collection.size();
				}

				@Override
				public boolean addChar(char x) {
					return collection.addChar(x);
				}
			};
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder(size() * 5); // heuristic
			builder.append("[");

			boolean tail = false;
			for (CharIterator iterator = iterator(); iterator.hasNext(); ) {
				if (tail)
					builder.append(", ");
				else
					tail = true;
				builder.append(iterator.nextChar());
			}

			builder.append("]");
			return builder.toString();
		}
	}
}
