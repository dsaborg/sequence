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

import java.util.*;
import java.util.function.Function;

/**
 * A {@link List} that presents a mapped view of another {@link List}.
 */
public interface MappedList {
	static <T, U> List<U> from(List<T> list, Function<? super T, ? extends U> mapper) {
		if (list instanceof RandomAccess)
			return new RandomAccessList<>(list, mapper);
		else
			return new SequentialList<>(list, mapper);
	}

	class RandomAccessList<T, U> extends AbstractList<U> implements RandomAccess, SizedIterable<U> {
		private final List<T> list;
		private final SizeType sizeType;
		private final Function<? super T, ? extends U> mapper;

		public RandomAccessList(List<T> list, Function<? super T, ? extends U> mapper) {
			this.list = list;
			this.sizeType = Iterables.sizeType(list);
			this.mapper = mapper;
		}

		@Override
		public U get(int index) {
			return mapper.apply(list.get(index));
		}

		@Override
		public U remove(int index) {
			return mapper.apply(list.remove(index));
		}

		@Override
		public int size() {
			return list.size();
		}

		@Override
		public SizeType sizeType() {
			return sizeType;
		}
	}

	class SequentialList<T, U> extends AbstractSequentialList<U> implements SizedIterable<U> {
		private final List<T> list;
		private final SizeType sizeType;
		private final Function<? super T, ? extends U> mapper;

		public SequentialList(List<T> list, Function<? super T, ? extends U> mapper) {
			this.list = list;
			this.sizeType = Iterables.sizeType(list);
			this.mapper = mapper;
		}

		@Override
		public ListIterator<U> listIterator(int index) {
			ListIterator<T> listIterator = list.listIterator(index);
			return new ListIterator<U>() {
				@Override
				public boolean hasNext() {
					return listIterator.hasNext();
				}

				@Override
				public U next() {
					return mapper.apply(listIterator.next());
				}

				@Override
				public boolean hasPrevious() {
					return listIterator.hasPrevious();
				}

				@Override
				public U previous() {
					return mapper.apply(listIterator.previous());
				}

				@Override
				public int nextIndex() {
					return listIterator.nextIndex();
				}

				@Override
				public int previousIndex() {
					return listIterator.previousIndex();
				}

				@Override
				public void remove() {
					listIterator.remove();
				}

				@Override
				public void set(U u) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void add(U u) {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public int size() {
			return list.size();
		}

		@Override
		public SizeType sizeType() {
			return sizeType;
		}
	}
}
