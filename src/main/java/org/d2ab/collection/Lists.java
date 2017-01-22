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
import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.*;

/**
 * Utility methods for {@link List}s.
 */
public abstract class Lists {
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
	 * A pass-through version of {@link Collections#shuffle(List)}.
	 *
	 * @return the given list shuffled using {@link Collections#shuffle(List)}.
	 */
	public static <T> List<T> shuffle(List<T> list) {
		Collections.shuffle(list);
		return list;
	}

	/**
	 * A pass-through version of {@link Collections#shuffle(List, Random)}.
	 *
	 * @return the given list shuffled using {@link Collections#shuffle(List, Random)}.
	 */
	public static <T> List<T> shuffle(List<T> list, Random random) {
		Collections.shuffle(list, random);
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
	 * Swap the given items in the given {@link List}.
	 */
	public static <T> void swap(List<T> list, int i, int j) {
		T temp = list.get(i);
		list.set(i, list.get(j));
		list.set(j, temp);
	}

	/**
	 * Reverse the given {@link List} in place.
	 *
	 * @return the given {@link List}, reversed.
	 */
	public static <T> List<T> reverse(List<T> list) {
		for (int i = 0; i < list.size() / 2; i++)
			swap(list, i, list.size() - i - 1);
		return list;
	}
}
