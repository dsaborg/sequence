package org.d2ab.test;

import org.d2ab.iterator.ints.IntIterator;

public interface StrictIntIterator extends IntIterator {
	static IntIterator from(IntIterator iterator) {
		return new IntIterator() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public int nextInt() {
				return iterator.nextInt();
			}

			@Override
			public Integer next() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void remove() {
				iterator.remove();
			}
		};
	}

	static IntIterator of(int... values) {
		return from(IntIterator.of(values));
	}
}
