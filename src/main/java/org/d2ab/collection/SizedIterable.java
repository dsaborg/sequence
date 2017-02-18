package org.d2ab.collection;

import org.d2ab.iterator.Iterators;

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
			public SizeType limited() {
				return UNKNOWN;
			}

			@Override
			public int limitedSize(SizedIterable<?> parent, SizedIterable<?> iterable, int limit) {
				return SizedIterable.size(iterable);
			}
		},
		KNOWN {
			@Override
			public SizeType limited() {
				return KNOWN;
			}

			@Override
			public int limitedSize(SizedIterable<?> parent, SizedIterable<?> iterable, int limit) {
				return min(parent.size(), limit);
			}
		},
		INFINITE {
			@Override
			public SizeType limited() {
				return KNOWN;
			}

			@Override
			public int limitedSize(SizedIterable<?> parent, SizedIterable<?> iterable, int limit) {
				return limit;
			}
		};

		public SizeType concat(SizeType sizeType) {
			if (this == INFINITE || sizeType == INFINITE)
				return INFINITE;

			if (this == UNKNOWN || sizeType == UNKNOWN)
				return UNKNOWN;

			return KNOWN; // both KNOWN
		}

		public SizeType intersect(SizeType sizeType) {
			if (this == UNKNOWN || sizeType == UNKNOWN)
				return UNKNOWN;

			if (this == INFINITE)
				return sizeType;

			if (sizeType == INFINITE)
				return this;

			return KNOWN; // both KNOWN
		}

		public abstract SizeType limited();

		public abstract int limitedSize(SizedIterable<?> parent, SizedIterable<?> iterable, int limit);
	}

	abstract class KnownSizedIterable<T> implements SizedIterable<T> {
		public abstract int size();

		@Override
		public SizeType sizeType() {
			return KNOWN;
		}
	}
}
