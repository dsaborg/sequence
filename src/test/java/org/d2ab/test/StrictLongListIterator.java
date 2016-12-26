package org.d2ab.test;

import org.d2ab.collection.longs.LongListIterator;

public interface StrictLongListIterator extends LongListIterator {
	static LongListIterator from(LongListIterator listIterator) {
		return new LongListIterator() {
			@Override
			public boolean hasNext() {
				return listIterator.hasNext();
			}

			@Override
			public long nextLong() {
				return listIterator.nextLong();
			}

			@Override
			public Long next() {
				throw new UnsupportedOperationException();
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

			@Override
			public void remove() {
				listIterator.remove();
			}
		};
	}
}
