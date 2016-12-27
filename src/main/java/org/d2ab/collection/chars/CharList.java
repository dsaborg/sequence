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
import org.d2ab.function.CharUnaryOperator;
import org.d2ab.iterator.chars.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A primitive specialization of {@link List} for {@code char} values.
 */
public interface CharList extends List<Character>, CharCollection {
	// TODO: Extract out relevant parts to IterableCharList

	/**
	 * Returns an immutable {@code CharList} of the given elements. The returned {@code CharList}'s
	 * {@link CharListIterator} supports forward iteration only.
	 */
	static CharList of(char... xs) {
		return new CharList.Base() {
			@Override
			public CharIterator iterator() {
				return new ArrayCharIterator(xs);
			}

			@Override
			public int size() {
				return xs.length;
			}
		};
	}

	/**
	 * @return a new empty mutable {@code CharList}.
	 *
	 * @since 2.1
	 */
	static CharList create() {
		return ArrayCharList.create();
	}

	/**
	 * @return a new mutable {@code CharList} with a copy of the given elements.
	 *
	 * @since 2.1
	 */
	static CharList create(char... xs) {
		return ArrayCharList.create(xs);
	}

	/**
	 * @return an {@code CharList} initialized with the members of the given {@link CharIterator}.
	 */
	static CharList copy(CharIterator iterator) {
		CharList copy = create();
		while (iterator.hasNext())
			copy.addChar(iterator.nextChar());
		return copy;
	}

	default void clear() {
		iterator().removeAll();
	}

	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default boolean contains(Object o) {
		return o instanceof Character && containsChar((char) o);
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
	default boolean remove(Object o) {
		return o instanceof Character && removeChar((char) o);
	}

	@Override
	default boolean add(Character x) {
		return addChar(x);
	}

	@Override
	default boolean addAll(int index, Collection<? extends Character> c) {
		if (c.isEmpty())
			return false;

		CharListIterator listIterator = listIterator(index);
		for (char t : c)
			listIterator.add(t);

		return true;
	}

	default boolean addAllCharsAt(int index, char... xs) {
		if (xs.length == 0)
			return false;

		CharListIterator listIterator = listIterator(index);
		for (char x : xs)
			listIterator.add(x);

		return true;
	}

	default boolean addAllCharsAt(int index, CharCollection xs) {
		if (xs.isEmpty())
			return false;

		CharListIterator listIterator = listIterator(index);
		xs.forEachChar(listIterator::add);

		return true;
	}

	@Override
	default void replaceAll(UnaryOperator<Character> operator) {
		replaceAllChars(operator::apply);
	}

	default void replaceAllChars(CharUnaryOperator operator) {
		CharListIterator listIterator = listIterator();
		while (listIterator.hasNext())
			listIterator.set(operator.applyAsChar(listIterator.nextChar()));
	}

	@Override
	default void sort(Comparator<? super Character> c) {
		throw new UnsupportedOperationException();
	}

	default void sortChars() {
		throw new UnsupportedOperationException();
	}

	default int binarySearch(char x) {
		throw new UnsupportedOperationException();
	}

	@Override
	default CharList subList(int from, int to) {
		return new SubList(this, from, to);
	}

	@Override
	default boolean addAll(Collection<? extends Character> c) {
		return Collectionz.addAll(this, c);
	}

	@Override
	default boolean addChar(char x) {
		listIterator(size()).add(x);
		return true;
	}

	@Override
	default boolean addAllChars(char... xs) {
		if (xs.length == 0)
			return false;

		CharListIterator listIterator = listIterator(size());
		for (char x : xs)
			listIterator.add(x);

		return true;
	}

	@Override
	default boolean addAllChars(CharCollection xs) {
		if (xs.isEmpty())
			return false;

		CharListIterator listIterator = listIterator(size());
		xs.forEachChar(listIterator::add);

		return true;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return Collectionz.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return removeCharsIf(c::contains);
	}

	@Override
	default boolean removeIf(Predicate<? super Character> filter) {
		return removeCharsIf(filter::test);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return removeCharsIf(x -> !c.contains(x));
	}

	@Override
	default Character get(int index) {
		return getChar(index);
	}

	default char getChar(int index) {
		CharListIterator iterator = listIterator(index);
		if (!iterator.hasNext())
			throw new IndexOutOfBoundsException(String.valueOf(index));

		return iterator.nextChar();
	}

	@Override
	default Character set(int index, Character x) {
		return setChar(index, x);
	}

	default char setChar(int index, char x) {
		CharListIterator listIterator = listIterator(index);
		char previous = listIterator.nextChar();
		listIterator.set(x);
		return previous;
	}

	@Override
	default void add(int index, Character x) {
		addCharAt(index, x);
	}

	default void addCharAt(int index, char x) {
		listIterator(index).add(x);
	}

	@Override
	default Character remove(int index) {
		return removeCharAt(index);
	}

	default char removeCharAt(int index) {
		CharListIterator listIterator = listIterator(index);
		char previous = listIterator.nextChar();
		listIterator.remove();
		return previous;
	}

	@Override
	default int lastIndexOf(Object o) {
		return o instanceof Character ? lastIndexOfChar((char) o) : -1;
	}

	default int lastIndexOfChar(char x) {
		int lastIndex = -1;

		int index = 0;
		for (CharIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.nextChar() == x)
				lastIndex = index;

		return lastIndex;
	}

	@Override
	default int indexOf(Object o) {
		return o instanceof Character ? indexOfChar((char) o) : -1;
	}

	default int indexOfChar(char x) {
		int index = 0;
		for (CharIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.nextChar() == x)
				return index;

		return -1;
	}

	@Override
	default CharListIterator listIterator() {
		return listIterator(0);
	}

	@Override
	default CharListIterator listIterator(int index) {
		return CharListIterator.forwardOnly(iterator(), index);
	}

	@Override
	default Spliterator.OfInt intSpliterator() {
		return Spliterators.spliterator(intIterator(), size(), Spliterator.ORDERED | Spliterator.NONNULL);
	}

	/**
	 * Base class for {@link CharList} implementations.
	 */
	abstract class Base extends CharCollection.Base implements CharList {
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof List))
				return false;

			CharListIterator it = listIterator();
			if (o instanceof CharList) {
				CharListIterator that = ((CharList) o).listIterator();
				while (it.hasNext()) {
					if (!that.hasNext())
						return false;
					if (it.nextChar() != that.nextChar())
						return false;
				}
				return !that.hasNext();
			} else {
				ListIterator<?> that = ((List<?>) o).listIterator();
				while (it.hasNext()) {
					if (!that.hasNext())
						return false;
					char x = it.nextChar();
					Object y = that.next();
					if (!(y instanceof Character && x == (Character) y))
						return false;
				}
				return !that.hasNext();
			}
		}

		public int hashCode() {
			int hashCode = 1;
			for (CharIterator iterator = iterator(); iterator.hasNext(); )
				hashCode = 31 * hashCode + Character.hashCode(iterator.nextChar());
			return hashCode;
		}
	}

	class SubList implements CharList {
		private final CharList list;

		private int from;
		private int to;

		public SubList(CharList list, int from, int to) {
			if (from < 0)
				throw new ArrayIndexOutOfBoundsException(from);
			if (to > list.size())
				throw new ArrayIndexOutOfBoundsException(to);
			this.list = list;
			this.from = from;
			this.to = to;
		}

		public CharIterator iterator() {
			return new DelegatingUnaryCharIterator(
					new LimitingCharIterator(new SkippingCharIterator(list.iterator(), from), to - from)) {
				@Override
				public char nextChar() {
					return iterator.nextChar();
				}

				@Override
				public void remove() {
					super.remove();
					to--;
				}
			};
		}

		public int size() {
			return to - from;
		}
	}
}
