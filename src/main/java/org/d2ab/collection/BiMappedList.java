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
public abstract class BiMappedList {
	private BiMappedList() {
	}

	public static <T, U> List<U> from(List<T> list, Function<? super T, ? extends U> mapper,
	                                  Function<? super U, ? extends T> backMapper) {
		if (list instanceof RandomAccess)
			return new RandomAccessList<>(list, mapper, backMapper);
		else
			return new SequentialList<>(list, mapper, backMapper);
	}

	private static class RandomAccessList<T, U> extends AbstractList<U> implements RandomAccess {
		private final List<T> list;
		private final Function<? super T, ? extends U> mapper;
		private final Function<? super U, ? extends T> backMapper;

		public RandomAccessList(List<T> list, Function<? super T, ? extends U> mapper,
		                        Function<? super U, ? extends T> backMapper) {
			this.list = list;
			this.mapper = mapper;
			this.backMapper = backMapper;
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
		public U set(int index, U element) {
			return mapper.apply(list.set(index, backMapper.apply(element)));
		}

		@Override
		public void add(int index, U element) {
			list.add(index, backMapper.apply(element));
		}

		@Override
		public int size() {
			return list.size();
		}
	}

	private static class SequentialList<T, U> extends AbstractSequentialList<U> {
		private final List<T> list;
		private final Function<? super T, ? extends U> mapper;
		private final Function<? super U, ? extends T> backMapper;

		public SequentialList(List<T> list, Function<? super T, ? extends U> mapper,
		                      Function<? super U, ? extends T> backMapper) {
			this.list = list;
			this.mapper = mapper;
			this.backMapper = backMapper;
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
					listIterator.set(backMapper.apply(u));
				}

				@Override
				public void add(U u) {
					listIterator.add(backMapper.apply(u));
				}
			};
		}

		@Override
		public int size() {
			return list.size();
		}
	}
}
