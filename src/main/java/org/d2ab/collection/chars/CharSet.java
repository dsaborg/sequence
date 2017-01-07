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

import java.util.Collection;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A primitive specialization of {@link Set} for {@code char} values.
 */
public interface CharSet extends Set<Character>, CharCollection {
	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default void clear() {
		iterator().removeAll();
	}

	@Override
	default boolean add(Character x) {
		return addChar(x);
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
	default Character[] toArray() {
		return toArray(new Character[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return Collectionz.toArray(this, a);
	}

	@Override
	default boolean addAll(Collection<? extends Character> c) {
		return Collectionz.addAll(this, c);
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
	default Spliterator.OfInt intSpliterator() {
		return Spliterators.spliterator(intIterator(), size(), Spliterator.DISTINCT);
	}

	/**
	 * Base class for {@link CharSet} implementations.
	 */
	abstract class Base extends CharCollection.Base implements CharSet {
		public static CharSet create(char... chars) {
			return from(CharSortedSet.create(chars));
		}

		public static CharSet from(final CharCollection collection) {
			return new CharSet.Base() {
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

		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof Set))
				return false;

			Set<?> that = (Set<?>) o;
			return size() == that.size() && containsAll(that);
		}

		public int hashCode() {
			int hashCode = 0;
			for (CharIterator iterator = iterator(); iterator.hasNext(); )
				hashCode += Character.hashCode(iterator.nextChar());
			return hashCode;
		}
	}
}
