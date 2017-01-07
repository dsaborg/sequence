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
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.iterator.longs.LongIterator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

/**
 * Utility methods for {@link Collection} instances.
 */
public abstract class Collectionz {
	Collectionz() {
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

	public static boolean containsAll(IntCollection xs, Collection<?> c) {
		if (c instanceof IntIterable)
			return xs.containsAllInts((IntIterable) c);

		for (Object x : c)
			if (!xs.contains(x))
				return false;

		return true;
	}

	public static boolean addAll(IntCollection xs, Collection<? extends Integer> c) {
		if (c instanceof IntCollection)
			return xs.addAllInts((IntCollection) c);

		boolean modified = false;
		for (int x : c)
			modified |= xs.addInt(x);
		return modified;
	}

	public static boolean retainAll(IntCollection xs, Collection<?> c) {
		if (c instanceof IntIterable)
			return xs.retainAllInts((IntIterable) c);

		boolean modified = false;
		for (IntIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			if (!c.contains(iterator.nextInt())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	public static boolean removeAll(IntCollection xs, Collection<?> c) {
		if (c instanceof IntIterable)
			return xs.removeAllInts((IntIterable) c);

		boolean modified = false;
		for (IntIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			if (c.contains(iterator.nextInt())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	public static boolean containsAll(LongCollection xs, Collection<?> c) {
		if (c instanceof LongCollection)
			return xs.containsAllLongs((LongCollection) c);

		for (Object x : c)
			if (!xs.contains(x))
				return false;

		return true;
	}

	public static boolean addAll(LongCollection xs, Collection<? extends Long> c) {
		if (c instanceof LongCollection)
			return xs.addAllLongs((LongCollection) c);

		boolean modified = false;
		for (long x : c)
			modified |= xs.addLong(x);
		return modified;
	}

	public static boolean retainAll(LongCollection xs, Collection<?> c) {
		if (c instanceof LongCollection)
			return xs.retainAllLongs((LongCollection) c);

		boolean modified = false;
		for (LongIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			if (!c.contains(iterator.nextLong())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	public static boolean removeAll(LongCollection xs, Collection<?> c) {
		if (c instanceof LongCollection)
			return xs.removeAllLongs((LongCollection) c);

		boolean modified = false;
		for (LongIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			if (c.contains(iterator.nextLong())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	public static boolean containsAll(CharCollection xs, Collection<?> c) {
		if (c instanceof CharIterable)
			return xs.containsAllChars((CharIterable) c);

		for (Object x : c)
			if (!xs.contains(x))
				return false;

		return true;
	}

	public static boolean addAll(CharCollection xs, Collection<? extends Character> c) {
		if (c instanceof CharCollection)
			return xs.addAllChars((CharCollection) c);

		boolean modified = false;
		for (Character x : c)
			modified |= xs.addChar(x);
		return modified;
	}

	public static boolean retainAll(CharCollection xs, Collection<?> c) {
		if (c instanceof CharIterable)
			return xs.retainAllChars((CharIterable) c);

		boolean modified = false;
		for (CharIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			if (!c.contains(iterator.nextChar())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	public static boolean removeAll(CharCollection xs, Collection<?> c) {
		if (c instanceof CharIterable)
			return xs.removeAllChars((CharIterable) c);

		boolean modified = false;
		for (CharIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			if (c.contains(iterator.nextChar())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	public static boolean containsAll(DoubleCollection xs, Collection<?> c) {
		if (c instanceof DoubleCollection)
			return xs.containsAllDoublesExactly((DoubleCollection) c);

		for (Object x : c)
			if (!xs.contains(x))
				return false;

		return true;
	}

	public static boolean addAll(DoubleCollection xs, Collection<? extends Double> c) {
		if (c instanceof DoubleCollection)
			return xs.addAllDoubles((DoubleCollection) c);

		boolean modified = false;
		for (double x : c)
			modified |= xs.addDoubleExactly(x);
		return modified;
	}

	public static boolean retainAll(DoubleCollection xs, Collection<?> c) {
		if (c instanceof DoubleCollection)
			return xs.retainAllDoublesExactly((DoubleCollection) c);

		boolean modified = false;
		for (DoubleIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			if (!c.contains(iterator.nextDouble())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	public static boolean removeAll(DoubleCollection xs, Collection<?> c) {
		if (c instanceof DoubleCollection)
			return xs.removeAllDoublesExactly((DoubleCollection) c);

		boolean modified = false;
		for (DoubleIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			if (c.contains(iterator.nextDouble())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	/**
	 * @return a {@link List} view of the given {@link Collection}, reflecting changes to the underlying {@link
	 * Collection}. If a {@link List} is given it is returned unchanged. The list does not implement {@link
	 * RandomAccess}, and is best accessed in sequence.
	 */
	public static <T> List<T> asList(Collection<T> collection) {
		if (collection instanceof List)
			return (List<T>) collection;

		return new CollectionList<>(collection);
	}
}
