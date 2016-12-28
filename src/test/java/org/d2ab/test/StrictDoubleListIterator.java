package org.d2ab.test;

import org.d2ab.collection.doubles.DoubleListIterator;

public class StrictDoubleListIterator extends StrictDoubleIterator implements DoubleListIterator {
	private final DoubleListIterator iterator;

	public static DoubleListIterator of(double... values) {
		return from(DoubleListIterator.of(values));
	}

	public static DoubleListIterator from(DoubleListIterator iterator) {
		return new StrictDoubleListIterator(iterator);
	}

	public StrictDoubleListIterator(DoubleListIterator iterator) {
		super(iterator);
		this.iterator = iterator;
	}

	@Override
	public boolean hasPrevious() {
		return iterator.hasPrevious();
	}

	@Override
	public double previousDouble() {
		return iterator.previousDouble();
	}

	@Override
	public Double previous() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int nextIndex() {
		return iterator.nextIndex();
	}

	@Override
	public int previousIndex() {
		return iterator.previousIndex();
	}

	@Override
	public void set(double x) {
		iterator.set(x);
	}

	@Override
	public void set(Double x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(double x) {
		iterator.add(x);
	}

	@Override
	public void add(Double x) {
		throw new UnsupportedOperationException();
	}
}
