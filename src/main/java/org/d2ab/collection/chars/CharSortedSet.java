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

import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.chars.ExclusiveTerminalCharIterator;
import org.d2ab.iterator.chars.InclusiveStartingCharIterator;

import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A primitive specialization of {@link SortedSet} for {@code char} values.
 */
public interface CharSortedSet extends SortedSet<Character>, CharSet {
	/**
	 * @return a new empty mutable {@code CharSortedSet}.
	 *
	 * @since 2.1
	 */
	static CharSortedSet create() {
		return new BitCharSet();
	}

	/**
	 * @return a new mutable {@code CharSortedSet} with a copy of the given elements.
	 *
	 * @since 2.1
	 */
	static CharSortedSet create(char... xs) {
		return new BitCharSet(xs);
	}

	@Override
	default CharComparator comparator() {
		return null;
	}

	@Override
	default CharSortedSet subSet(Character from, Character to) {
		return subSet((char) from, (char) to);
	}

	default CharSortedSet subSet(char from, char to) {
		return new SubSet(this) {
			@Override
			public CharIterator iterator() {
				return untilExcluded(fromIncluded(CharSortedSet.this.iterator()));
			}

			@Override
			protected boolean included(char x) {
				return x >= from && x < to;
			}
		};
	}

	@Override
	default CharSortedSet headSet(Character to) {
		return headSet((char) to);
	}

	default CharSortedSet headSet(char to) {
		return new SubSet(this) {
			@Override
			public CharIterator iterator() {
				return untilExcluded(CharSortedSet.this.iterator());
			}

			@Override
			public char firstChar() {
				return CharSortedSet.this.firstChar();
			}

			@Override
			protected boolean included(char x) {
				return x < to;
			}
		};
	}

	@Override
	default CharSortedSet tailSet(Character from) {
		return tailSet((char) from);
	}

	default CharSortedSet tailSet(char from) {
		return new SubSet(this) {
			@Override
			public CharIterator iterator() {
				return fromIncluded(CharSortedSet.this.iterator());
			}

			@Override
			public char lastChar() {
				return CharSortedSet.this.lastChar();
			}

			@Override
			protected boolean included(char x) {
				return x >= from;
			}
		};
	}

	@Override
	default Character first() {
		return firstChar();
	}

	default char firstChar() {
		return iterator().nextChar();
	}

	@Override
	default Character last() {
		return lastChar();
	}

	default char lastChar() {
		CharIterator iterator = iterator();
		char last;
		do
			last = iterator.nextChar();
		while (iterator.hasNext());
		return last;
	}

	@Override
	default Spliterator.OfInt intSpliterator() {
		return Spliterators.spliterator(intIterator(), size(),
		                                Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED |
		                                Spliterator.NONNULL);
	}

	abstract class SubSet extends CharSet.Base implements CharSortedSet {
		private CharSortedSet set;

		public SubSet(CharSortedSet set) {
			this.set = set;
		}

		@Override
		public int size() {
			return iterator().count();
		}

		@Override
		public boolean containsChar(char x) {
			return included(x) && set.containsChar(x);
		}

		@Override
		public boolean removeChar(char x) {
			return included(x) && set.removeChar(x);
		}

		@Override
		public boolean addChar(char x) {
			if (excluded(x))
				throw new IllegalArgumentException(String.valueOf(x));

			return set.addChar(x);
		}

		protected CharIterator untilExcluded(CharIterator iterator) {
			return new ExclusiveTerminalCharIterator(iterator, this::excluded);
		}

		protected CharIterator fromIncluded(CharIterator iterator) {
			return new InclusiveStartingCharIterator(iterator, this::included);
		}

		protected abstract boolean included(char x);

		protected boolean excluded(char x) {
			return !included(x);
		}
	}
}
