package org.d2ab.collection.longs;

import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.util.Strict;

import java.util.Collection;

/**
 * Utility methods for {@link LongCollection} instances.
 */
public abstract class LongCollections {
	LongCollections() {
	}

	public static boolean add(Long x, LongCollection xs) {
		Strict.check();

		return xs.addLong(x);
	}

	public static boolean contains(Object o, LongCollection xs) {
		Strict.check();

		return o instanceof Long && xs.containsLong((long) o);
	}

	public static boolean remove(Object o, LongCollection xs) {
		Strict.check();

		return o instanceof Long && xs.removeLong((long) o);
	}

	public static boolean addAll(LongCollection xs, Collection<? extends Long> c) {
		if (c instanceof LongCollection)
			return xs.addAllLongs((LongCollection) c);

		Strict.check();

		boolean modified = false;
		for (long x : c)
			modified |= xs.addLong(x);
		return modified;
	}

	public static boolean containsAll(LongCollection xs, Collection<?> c) {
		if (c instanceof LongIterable)
			return xs.containsAllLongs((LongIterable) c);

		Strict.check();

		for (Object x : c)
			if (!xs.contains(x))
				return false;

		return true;
	}

	public static boolean removeAll(LongCollection xs, Collection<?> c) {
		if (c instanceof LongIterable)
			return xs.removeAllLongs((LongIterable) c);

		Strict.check();

		boolean modified = false;
		for (LongIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			if (c.contains(iterator.nextLong())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	public static boolean retainAll(LongCollection xs, Collection<?> c) {
		if (c instanceof LongIterable)
			return xs.retainAllLongs((LongIterable) c);

		Strict.check();

		boolean modified = false;
		for (LongIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			if (!c.contains(iterator.nextLong())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}
}
