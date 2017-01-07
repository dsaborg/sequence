package org.d2ab.collection.doubles;

import org.d2ab.iterator.doubles.DoubleIterator;

import java.util.Collection;

/**
 * Utility methods for {@link DoubleCollection} instances.
 */
public abstract class DoubleCollections {
	DoubleCollections() {
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
}
