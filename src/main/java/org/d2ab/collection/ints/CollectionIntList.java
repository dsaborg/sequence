package org.d2ab.collection.ints;

import org.d2ab.iterator.ints.IntIterator;

import java.util.Collection;

/**
 * An {@link IntList} implementation backed by a {@link IntCollection}. Supports forward iteration only.
 */
public class CollectionIntList extends IntList.Base implements IterableIntList {
	private final IntCollection collection;

	public static IntList from(final IntCollection collection) {
		return new CollectionIntList(collection);
	}

	private CollectionIntList(IntCollection collection) {
		this.collection = collection;
	}

	@Override
	public IntIterator iterator() {
		return collection.iterator();
	}

	@Override
	public int size() {
		return collection.size();
	}

	@Override
	public boolean addInt(int x) {
		return collection.addInt(x);
	}

	@Override
	public boolean addAll(Collection<? extends Integer> c) {
		return collection.addAll(c);
	}

	public boolean addAllInts(int... xs) {
		return collection.addAllInts(xs);
	}

	public boolean addAllInts(IntCollection xs) {
		return collection.addAllInts(xs);
	}
}
