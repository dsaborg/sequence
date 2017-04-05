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

import org.d2ab.iterator.ShufflingArrayIterator;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.*;

/**
 * Utility methods for {@link List}s.
 */
public abstract class Lists {
	private static final int SHUFFLE_THRESHOLD = 5;

	Lists() {
	}

	public static <T> List<T> of() {
		return emptyList();
	}

	public static <T> List<T> of(T item) {
		return singletonList(item);
	}

	@SafeVarargs
	public static <T> List<T> of(T... items) {
		return unmodifiableList(asList(items));
	}

	@SafeVarargs
	public static <T> List<T> create(T... items) {
		return new ArrayList<>(asList(items));
	}

	/**
	 * Shuffle the items in the {@link List} according to the given {@link Random} generator. Produces elements in the
	 * same order as {@link ShufflingArrayIterator} and {@link Arrayz#shuffle(Object[], Random)}}.
	 *
	 * @see ShufflingArrayIterator
	 * @see Arrayz#shuffle(Object[], Random)
	 *
	 * @return the given {@link List}, shuffled.
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> shuffle(List<T> list, Random random) {
		int size = list.size();

		if (size < SHUFFLE_THRESHOLD || list instanceof RandomAccess) {
			for (int i = 0, j = size; i < size - 1; i++, j--) {
				int k = random.nextInt(j) + i;
				if (k != i) {
					T temp = list.get(k);
					list.set(k, list.get(i));
					list.set(i, temp);
				}
			}
		} else {
			Object[] array = list.toArray(new Object[size]);

			Arrayz.shuffle(array, random);

			ListIterator<T> it = list.listIterator();
			for (int i = 0; i < size; i++) {
				it.next();
				it.set((T) array[i]);
			}
		}

		return list;
	}

	/**
	 * A pass-through version of {@link List#sort(Comparator)} sorted using
	 * {@link Comparator#naturalOrder()}.
	 *
	 * @return the given list sorted using {@link List#sort(Comparator)} with {@link Comparator#naturalOrder()}.
	 */
	public static <T> List<T> sort(List<T> list, Comparator<? super T> comparator) {
		list.sort(comparator);
		return list;
	}

	/**
	 * Reverse the given {@link List} in place.
	 *
	 * @return the given {@link List}, reversed.
	 */
	public static <T> List<T> reverse(List<T> list) {
		Collections.reverse(list);
		return list;
	}
}
