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
		assert Strict.LENIENT : "CharCollection.toArray()";

		return toArray(new Character[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		assert Strict.LENIENT : "CharCollection.toArray(Object[])";

		return Collectionz.toArray(this, a);
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
		assert Strict.LENIENT : "CharCollection.add(Character)";

		return addChar(x);
	}

	default boolean addChar(char x) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean contains(Object o) {
		assert Strict.LENIENT : "CharCollection.contains(Object)";

		return o instanceof Character && containsChar((char) o);
	}

	@Override
	default boolean remove(Object o) {
		assert Strict.LENIENT : "CharCollection.remove(Character)";

		return o instanceof Character && removeChar((char) o);
	}

	@Override
	default boolean addAll(Collection<? extends Character> c) {
		if (c instanceof CharCollection)
			return addAllChars((CharCollection) c);

		assert Strict.LENIENT : "CharCollection.addAll(Collection)";

		return Collectionz.addAll(this, c);
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
		if (c instanceof CharCollection)
			return containsAllChars((CharCollection) c);

		assert Strict.LENIENT : "CharCollection.containsAll(Collection)";

		return Collectionz.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		if (c instanceof CharCollection)
			return removeAllChars((CharCollection) c);

		assert Strict.LENIENT : "CharCollection.removeAll(Collection)";

		return Collectionz.removeAll(this, c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		if (c instanceof CharCollection)
			return retainAllChars((CharCollection) c);

		assert Strict.LENIENT : "CharCollection.retainAll(Collection)";

		return Collectionz.retainAll(this, c);
	}

	@Override
	default boolean removeIf(Predicate<? super Character> filter) {
		assert Strict.LENIENT : "CharCollection.removeIf(Predicate)";

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
