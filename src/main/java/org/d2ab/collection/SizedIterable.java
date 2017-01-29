package org.d2ab.collection;

import org.d2ab.iterator.Iterators;
import org.d2ab.sequence.Sequence;

import java.util.Collection;
import java.util.Iterator;

import static java.lang.Math.min;
import static org.d2ab.collection.SizedIterable.SizeType.KNOWN;

/**
 * An {@link Iterable} which can also report the size of its contents.
 */
public interface SizedIterable<T> extends Iterable<T> {
	static <T> SizedIterable<T> from(Iterable<T> iterable) {
		if (iterable instanceof SizedIterable)
			return ((SizedIterable<T>) iterable);
		if (iterable instanceof Collection)
			return from((Collection<T>) iterable);

		return iterable::iterator;
	}

	static <T> SizedIterable<T> from(Collection<T> collection) {
		return new KnownSizedIterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return collection.iterator();
			}

			@Override
			public int size() {
				return collection.size();
			}
		};
	}

	static int size(SizedIterable<?> iterable) {
		switch (iterable.sizeType()) {
			case INFINITE:
				throw new UnsupportedOperationException();
			case KNOWN:
				throw new IllegalStateException("SizeType.KNOWN; must override size()");
			case UNKNOWN:
			default:
				return Iterators.size(iterable.iterator());
		}
	}

	static boolean isEmpty(SizedIterable<?> iterable) {
		SizeType sizeType = iterable.sizeType();
		switch (sizeType) {
			case KNOWN:
				return iterable.size() == 0;
			case INFINITE:
				return false;
			case UNKNOWN:
			default:
				return !iterable.iterator().hasNext();
		}
	}

	/**
	 * The type of size this {@code SizedIterable} has, which provides the guarantees that can be assumed on the
	 * evaluation of the {@link #size()} and {@link #isEmpty()} methods.
	 * <p>
	 * If this method returns {@link SizeType#KNOWN}, {@link #size()} and {@link #isEmpty()} is guaranteed to evaluate
	 * correct results without having to traverse the {@code SizedIterable}.
	 * <p>
	 * If this method returns {@link SizeType#UNKNOWN}, {@link #size()} and {@link #isEmpty()} may require traversal
	 * of the {@code SizedIterable} to determine their results.
	 * <p>
	 * If this method returns {@link SizeType#INFINITE}, {@link #size()} will always throw
	 * {@link UnsupportedOperationException} and {@link #isEmpty()} will always return false.
	 */
	default SizeType sizeType() {
		return SizeType.UNKNOWN;
	}

	default int size() {
		return size(this);
	}

	default boolean isEmpty() {
		return isEmpty(this);
	}

	enum SizeType {
		UNKNOWN {
			@Override
			public SizeType concat(SizeType sizeType) {
				return UNKNOWN;
			}

			@Override
			public SizeType intersect(SizeType sizeType) {
				return UNKNOWN;
			}

			@Override
			public SizeType limited(int limit) {
				return UNKNOWN;
			}

			@Override
			public int limitedSize(Sequence<?> parent, Sequence<?> iterable, int limit) {
				return SizedIterable.size(iterable);
			}
		},
		KNOWN {
			@Override
			public SizeType concat(SizeType sizeType) {
				return sizeType;
			}

			@Override
			public SizeType intersect(SizeType sizeType) {
				return sizeType == UNKNOWN ? UNKNOWN : KNOWN;
			}

			@Override
			public SizeType limited(int limit) {
				return KNOWN;
			}

			@Override
			public int limitedSize(Sequence<?> parent, Sequence<?> iterable, int limit) {
				return min(parent.size(), limit);
			}
		},
		INFINITE {
			@Override
			public SizeType concat(SizeType sizeType) {
				return INFINITE;
			}

			@Override
			public SizeType intersect(SizeType sizeType) {
				return sizeType;
			}

			@Override
			public SizeType limited(int limit) {
				return KNOWN;
			}

			@Override
			public int limitedSize(Sequence<?> parent, Sequence<?> iterable, int limit) {
				return limit;
			}
		};

		public abstract SizeType concat(SizeType sizeType);

		public abstract SizeType intersect(SizeType sizeType);

		public abstract SizeType limited(int limit);

		public abstract int limitedSize(Sequence<?> parent, Sequence<?> iterable, int limit);
	}

	abstract class KnownSizedIterable<T> implements SizedIterable<T> {
		public abstract int size();

		@Override
		public SizeType sizeType() {
			return KNOWN;
		}
	}
}
