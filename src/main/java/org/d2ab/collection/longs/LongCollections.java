package org.d2ab.collection.longs;

import org.d2ab.iterator.longs.LongIterator;

import java.util.Collection;

/**
 * Utility methods for {@link LongCollection} instances.
 */
public abstract class LongCollections {
	LongCollections() {
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
}
