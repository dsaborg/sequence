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

import org.d2ab.collection.chars.CharCollection;
import org.d2ab.collection.chars.CharIterable;
import org.d2ab.collection.doubles.DoubleCollection;
import org.d2ab.collection.ints.IntCollection;
import org.d2ab.collection.ints.IntIterable;
import org.d2ab.collection.longs.LongCollection;

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
	public static boolean containsAll(IntIterable xs, Collection<?> c) {
		if (c instanceof IntIterable)
			return xs.containsAllInts((IntIterable) c);

		for (int x : (Collection<? extends Integer>) c)
			if (!xs.containsInt(x))
				return false;

		return true;
	}

	public static boolean addAll(IntCollection xs, Collection<? extends Integer> c) {
		if (c instanceof IntCollection)
			return xs.addAllInts((IntCollection) c);

		if (c.isEmpty())
			return false;

		c.forEach(xs::addInt);
		return true;
	}

	public static boolean retainAll(IntCollection xs, Collection<?> c) {
		if (c instanceof IntIterable)
			return xs.retainAllInts((IntIterable) c);

		return xs.removeIntsIf(x -> !c.contains(x));
	}

	public static boolean removeAll(IntCollection xs, Collection<?> c) {
		if (c instanceof IntIterable)
			return xs.removeAllInts((IntIterable) c);

		return xs.removeIntsIf(c::contains);
	}

	@SuppressWarnings("unchecked")
	public static boolean containsAll(LongCollection xs, Collection<?> c) {
		if (c instanceof LongCollection)
			return xs.containsAllLongs((LongCollection) c);

		for (long x : (Collection<? extends Long>) c)
			if (!xs.containsLong(x))
				return false;

		return true;
	}

	public static boolean addAll(LongCollection xs, Collection<? extends Long> c) {
		if (c instanceof LongCollection)
			return xs.addAllLongs((LongCollection) c);

		if (c.isEmpty())
			return false;

		c.forEach(xs::addLong);
		return true;
	}

	public static boolean retainAll(LongCollection xs, Collection<?> c) {
		if (c instanceof LongCollection)
			return xs.retainAllLongs((LongCollection) c);

		return xs.removeLongsIf(x -> !c.contains(x));
	}

	public static boolean removeAll(LongCollection xs, Collection<?> c) {
		if (c instanceof LongCollection)
			return xs.removeAllLongs((LongCollection) c);

		return xs.removeLongsIf(c::contains);
	}

	@SuppressWarnings("unchecked")
	public static boolean containsAll(CharIterable xs, Collection<?> c) {
		if (c instanceof CharIterable)
			return xs.containsAllChars((CharIterable) c);

		for (char x : (Collection<? extends Character>) c)
			if (!xs.containsChar(x))
				return false;

		return true;
	}

	public static boolean addAll(CharCollection xs, Collection<? extends Character> c) {
		if (c instanceof CharCollection)
			return xs.addAllChars((CharCollection) c);

		if (c.isEmpty())
			return false;

		c.forEach(xs::addChar);
		return true;
	}

	public static boolean retainAll(CharCollection xs, Collection<?> c) {
		if (c instanceof CharIterable)
			return xs.retainAllChars((CharIterable) c);

		return xs.removeCharsIf(x -> !c.contains(x));
	}

	public static boolean removeAll(CharCollection xs, Collection<?> c) {
		if (c instanceof CharIterable)
			return xs.removeAllChars((CharIterable) c);

		return xs.removeCharsIf(c::contains);
	}

	@SuppressWarnings("unchecked")
	public static boolean containsAll(DoubleCollection xs, Collection<?> c) {
		if (c instanceof DoubleCollection)
			return xs.containsAllDoubles((DoubleCollection) c);

		for (double x : (Collection<? extends Double>) c)
			if (!xs.containsDouble(x))
				return false;

		return true;
	}

	public static boolean addAll(DoubleCollection xs, Collection<? extends Double> c) {
		if (c instanceof DoubleCollection)
			return xs.addAllDoubles((DoubleCollection) c);

		if (c.isEmpty())
			return false;

		c.forEach(xs::addDouble);
		return true;
	}

	public static boolean retainAll(DoubleCollection xs, Collection<?> c) {
		if (c instanceof DoubleCollection)
			return xs.retainAllDoubles((DoubleCollection) c);

		return xs.removeDoublesIf(x -> !c.contains(x));
	}

	public static boolean removeAll(DoubleCollection xs, Collection<?> c) {
		if (c instanceof DoubleCollection)
			return xs.removeAllDoubles((DoubleCollection) c);

		return xs.removeDoublesIf(c::contains);
	}
}
