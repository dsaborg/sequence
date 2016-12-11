package org.d2ab.test;

import org.d2ab.iterator.longs.LongIterator;

public interface StrictLongIterator extends LongIterator {
	static LongIterator from(LongIterator iterator) {
		return new LongIterator() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public long nextLong() {
				return iterator.nextLong();
			}

			@Override
			public Long next() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void remove() {
				iterator.remove();
			}
		};
	}

	static LongIterator of(long... values) {
		return from(LongIterator.of(values));
	}
}
