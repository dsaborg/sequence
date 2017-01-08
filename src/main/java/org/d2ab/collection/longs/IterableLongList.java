package org.d2ab.collection.longs;

import org.d2ab.iterator.longs.LongIterator;

import java.util.Iterator;

/**
 * Superinterface for {@link LongList} implementation supporting only regular {@link Iterator} access.
 */
public interface IterableLongList extends LongList {
	@Override
	LongIterator iterator();

	@Override
	default LongListIterator listIterator(int index) {
		return LongListIterator.forwardOnly(iterator(), index);
	}
}
