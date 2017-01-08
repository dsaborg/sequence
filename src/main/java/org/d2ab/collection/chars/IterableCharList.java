package org.d2ab.collection.chars;

import org.d2ab.iterator.chars.CharIterator;

import java.util.Iterator;

/**
 * Superinterface for {@link CharList} implementation supporting only regular {@link Iterator} access.
 */
public interface IterableCharList extends CharList {
	@Override
	CharIterator iterator();

	@Override
	default CharListIterator listIterator(int index) {
		return CharListIterator.forwardOnly(iterator(), index);
	}
}
