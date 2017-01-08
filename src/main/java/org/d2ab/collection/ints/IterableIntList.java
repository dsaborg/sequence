package org.d2ab.collection.ints;

import org.d2ab.iterator.ints.IntIterator;

import java.util.Iterator;

/**
 * Superinterface for {@link IntList} implementation supporting only regular {@link Iterator} access.
 */
public interface IterableIntList extends IntList {
	@Override
	IntIterator iterator();

	@Override
	default IntListIterator listIterator(int index) {
		return IntListIterator.forwardOnly(iterator(), index);
	}
}
