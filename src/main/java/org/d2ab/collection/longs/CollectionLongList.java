package org.d2ab.collection.longs;

import org.d2ab.iterator.longs.LongIterator;

import java.util.Collection;

/**
 * An {@link LongList} implementation backed by a {@link LongCollection}. Supports forward iteration only.
 */
public class CollectionLongList extends LongList.Base implements IterableLongList {
	private final LongCollection collection;

	public static LongList from(final LongCollection collection) {
		return new CollectionLongList(collection);
	}

	private CollectionLongList(LongCollection collection) {
		this.collection = collection;
	}

	@Override
	public LongIterator iterator() {
		return collection.iterator();
	}

	@Override
	public int size() {
		return collection.size();
	}

	@Override
	public boolean addLong(long x) {
		return collection.addLong(x);
	}

	@Override
	public boolean addAll(Collection<? extends Long> c) {
		return collection.addAll(c);
	}

	@Override
	public boolean addAllLongs(long... xs) {
		return collection.addAllLongs(xs);
	}

	@Override
	public boolean addAllLongs(LongCollection xs) {
		return collection.addAllLongs(xs);
	}
}
