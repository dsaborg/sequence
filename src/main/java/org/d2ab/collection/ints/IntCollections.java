package org.d2ab.collection.ints;

import org.d2ab.iterator.ints.IntIterator;

import java.util.Collection;

/**
 * Utility methods for {@link IntCollection} instances.
 */
public abstract class IntCollections {
	IntCollections() {
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
}
