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

import java.util.Arrays;
import java.util.Collection;

/**
 * Utility methods for {@link Collection} instances.
 */
public class Collectionz {
	private Collectionz() {
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Collection<?> collection, T[] a) {
		int size = collection.size();
		if (a.length < size)
			a = Arrays.copyOf(a, size);

		int index = 0;
		for (Object o : collection)
			a[index++] = (T) o;

		if (a.length > size)
			a[size] = null;

		return a;
	}

	@SuppressWarnings("unchecked")
	public static boolean containsAll(IntIterable integers, Collection<?> c) {
		if (c instanceof IntIterable)
			return integers.containsAllInts((IntIterable) c);

		for (int i : (Collection<? extends Integer>) c)
			if (!integers.containsInt(i))
				return false;

		return true;
	}

	public static boolean addAll(IntCollection integers, Collection<? extends Integer> c) {
		if (c instanceof IntCollection)
			return integers.addAllInts((IntCollection) c);

		if (c.isEmpty())
			return false;

		c.forEach(integers::addInt);
		return true;
	}

	public static boolean retainAll(IntCollection integers, Collection<?> c) {
		if (c instanceof IntIterable)
			return integers.retainAllInts((IntIterable) c);

		return integers.removeIntsIf(i -> !c.contains(i));
	}

	public static boolean removeAll(IntCollection integers, Collection<?> c) {
		if (c instanceof IntIterable)
			return integers.removeAllInts((IntIterable) c);

		return integers.removeIntsIf(c::contains);
	}

	@SuppressWarnings("unchecked")
	public static boolean containsAll(LongCollection longs, Collection<?> c) {
		if (c instanceof LongCollection)
			return longs.containsAllLongs((LongCollection) c);

		for (long i : (Collection<? extends Long>) c)
			if (!longs.containsLong(i))
				return false;

		return true;
	}

	public static boolean addAll(LongCollection longs, Collection<? extends Long> c) {
		if (c instanceof LongCollection)
			return longs.addAllLongs((LongCollection) c);

		if (c.isEmpty())
			return false;

		c.forEach(longs::addLong);
		return true;
	}

	public static boolean retainAll(LongCollection longs, Collection<?> c) {
		if (c instanceof LongCollection)
			return longs.retainAllLongs((LongCollection) c);

		return longs.removeLongsIf(i -> !c.contains(i));
	}

	public static boolean removeAll(LongCollection longs, Collection<?> c) {
		if (c instanceof LongCollection)
			return longs.removeAllLongs((LongCollection) c);

		return longs.removeLongsIf(c::contains);
	}
}
