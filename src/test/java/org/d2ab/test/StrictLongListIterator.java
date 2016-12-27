package org.d2ab.test;

import org.d2ab.collection.longs.LongListIterator;

public class StrictLongListIterator extends StrictLongIterator implements LongListIterator {
	private final LongListIterator listIterator;

	public static LongListIterator from(LongListIterator listIterator) {
		return new StrictLongListIterator(listIterator);
	}

	public StrictLongListIterator(LongListIterator listIterator) {
		super(listIterator);
		this.listIterator = listIterator;
	}

	@Override
	public boolean hasPrevious() {
		return listIterator.hasPrevious();
	}

	@Override
	public long previousLong() {
		return listIterator.previousLong();
	}

	@Override
	public Long previous() {
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
	public void set(long x) {
		listIterator.set(x);
	}

	@Override
	public void set(Long x) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(long x) {
		listIterator.add(x);
	}

	@Override
	public void add(Long x) {
		throw new UnsupportedOperationException();
	}
}
