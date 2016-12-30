package org.d2ab.test;

import org.d2ab.collection.ints.IntListIterator;

public class StrictIntListIterator extends StrictIntIterator implements IntListIterator {
	private final IntListIterator listIterator;

	public static IntListIterator from(IntListIterator listIterator) {
		return new StrictIntListIterator(listIterator);
	}

	public StrictIntListIterator(IntListIterator listIterator) {
		super(listIterator);
		this.listIterator = listIterator;
	}

	@Override
	public boolean hasPrevious() {
		return listIterator.hasPrevious();
	}

	@Override
	public int previousInt() {
		return listIterator.previousInt();
	}

	@Override
	public Integer previous() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int nextIndex() {
		return listIterator.nextIndex();
	}

	@Override
	public int previousIndex() {
		return listIterator.previousIndex();
	}

	@Override
	public void set(int x) {
		listIterator.set(x);
	}

	@Override
	public void set(Integer x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int x) {
		listIterator.add(x);
	}

	@Override
	public void add(Integer x) {
		throw new UnsupportedOperationException();
	}
}
