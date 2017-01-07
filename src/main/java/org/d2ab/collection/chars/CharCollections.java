package org.d2ab.collection.chars;

import org.d2ab.iterator.chars.CharIterator;

import java.util.Collection;

/**
 * Utility methods for {@link CharCollection} instances.
 */
public abstract class CharCollections {
	CharCollections() {
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
}
