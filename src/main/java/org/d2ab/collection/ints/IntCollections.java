package org.d2ab.collection.ints;

import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.util.Strict;

import java.util.Collection;

/**
 * Utility methods for {@link IntCollection} instances.
 */
public abstract class IntCollections {
	IntCollections() {
	}

	public static boolean add(IntCollection xs, Integer x) {
		Strict.check();

		return xs.addInt(x);
	}

	public static boolean contains(IntCollection xs, Object o) {
		Strict.check();

		return o instanceof Integer && xs.containsInt((int) o);
	}

	public static boolean remove(IntCollection xs, Object o) {
		Strict.check();

		return o instanceof Integer && xs.removeInt((int) o);
	}

	public static boolean addAll(IntCollection xs, Collection<? extends Integer> c) {
		if (c instanceof IntCollection)
			return xs.addAllInts((IntCollection) c);

		Strict.check();

		boolean modified = false;
		for (int x : c)
			modified |= xs.addInt(x);
		return modified;
	}

	public static boolean containsAll(IntCollection xs, Collection<?> c) {
		if (c instanceof IntIterable)
			return xs.containsAllInts((IntIterable) c);

		Strict.check();

		for (Object x : c)
			if (!xs.contains(x))
				return false;

		return true;
	}

	public static boolean removeAll(IntCollection xs, Collection<?> c) {
		if (c instanceof IntIterable)
			return xs.removeAllInts((IntIterable) c);

		Strict.check();

		boolean modified = false;
		for (IntIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			if (c.contains(iterator.nextInt())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	public static boolean retainAll(IntCollection xs, Collection<?> c) {
		if (c instanceof IntIterable)
			return xs.retainAllInts((IntIterable) c);

		Strict.check();

		boolean modified = false;
		for (IntIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			if (!c.contains(iterator.nextInt())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}
}
