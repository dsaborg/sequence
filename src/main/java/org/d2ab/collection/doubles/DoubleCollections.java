package org.d2ab.collection.doubles;

import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.util.Strict;

import java.util.Collection;

/**
 * Utility methods for {@link DoubleCollection} instances.
 */
public abstract class DoubleCollections {
	DoubleCollections() {
	}

	public static boolean add(DoubleCollection xs, Double x) {
		Strict.check();

		return xs.addDoubleExactly(x);
	}

	public static boolean contains(DoubleCollection xs, Object o) {
		Strict.check();

		return o instanceof Double && xs.containsDoubleExactly((double) o);
	}

	public static boolean remove(DoubleCollection xs, Object o) {
		Strict.check();

		return o instanceof Double && xs.removeDoubleExactly((double) o);
	}

	public static boolean addAll(DoubleCollection xs, Collection<? extends Double> c) {
		if (c instanceof DoubleCollection)
			return xs.addAllDoubles((DoubleCollection) c);

		Strict.check();

		boolean modified = false;
		for (double x : c)
			modified |= xs.addDoubleExactly(x);
		return modified;
	}

	public static boolean containsAll(DoubleCollection xs, Collection<?> c) {
		if (c instanceof DoubleIterable)
			return xs.containsAllDoublesExactly((DoubleIterable) c);

		Strict.check();

		for (Object x : c)
			if (!xs.contains(x))
				return false;

		return true;
	}

	public static boolean retainAll(DoubleCollection xs, Collection<?> c) {
		if (c instanceof DoubleIterable)
			return xs.retainAllDoublesExactly((DoubleIterable) c);

		Strict.check();

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
		if (c instanceof DoubleIterable)
			return xs.removeAllDoublesExactly((DoubleIterable) c);

		Strict.check();

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
