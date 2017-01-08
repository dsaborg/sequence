package org.d2ab.collection.doubles;

import org.d2ab.iterator.doubles.DoubleIterator;

import java.util.Collection;

/**
 * An {@link DoubleList} implementation backed by a {@link DoubleCollection}. Supports forward iteration only.
 */
public class CollectionDoubleList extends DoubleList.Base implements IterableDoubleList {
	private final DoubleCollection collection;

	public static DoubleList from(final DoubleCollection collection) {
		return new CollectionDoubleList(collection);
	}

	private CollectionDoubleList(DoubleCollection collection) {
		this.collection = collection;
	}

	@Override
	public DoubleIterator iterator() {
		return collection.iterator();
	}

	@Override
	public int size() {
		return collection.size();
	}

	@Override
	public boolean addDouble(double x, double precision) {
		return collection.addDouble(x, precision);
	}

	@Override
	public boolean addDoubleExactly(double x) {
		return collection.addDoubleExactly(x);
	}

	@Override
	public boolean addAll(Collection<? extends Double> c) {
		return collection.addAll(c);
	}

	@Override
	public boolean addAllDoubles(double... xs) {
		return collection.addAllDoubles(xs);
	}

	@Override
	public boolean addAllDoubles(DoubleCollection xs) {
		return collection.addAllDoubles(xs);
	}
}
