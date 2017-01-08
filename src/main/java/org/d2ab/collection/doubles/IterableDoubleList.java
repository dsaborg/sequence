package org.d2ab.collection.doubles;

import org.d2ab.iterator.doubles.DoubleIterator;

import java.util.Iterator;

/**
 * Superinterface for {@link DoubleList} implementation supporting only regular {@link Iterator} access.
 */
public interface IterableDoubleList extends DoubleList {
	@Override
	DoubleIterator iterator();

	@Override
	default DoubleListIterator listIterator(int index) {
		return DoubleListIterator.forwardOnly(iterator(), index);
	}
}
