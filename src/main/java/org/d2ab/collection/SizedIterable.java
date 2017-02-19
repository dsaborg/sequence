package org.d2ab.collection;

import org.d2ab.iterator.Iterators;

import java.util.Collection;
import java.util.Iterator;

import static java.lang.Math.min;
import static org.d2ab.collection.SizedIterable.SizeType.FIXED;

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
		return from(collection, Iterables.sizeType(collection));
	}

	static <T> SizedIterable<T> from(Collection<T> collection, SizeType sizeType) {
		return new SizedIterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return collection.iterator();
			}

			@Override
			public int size() {
				return collection.size();
			}

			@Override
			public SizeType sizeType() {
				return sizeType;
			}
		};
	}

	static int size(SizedIterable<?> iterable) {
		switch (iterable.sizeType()) {
			case INFINITE:
				throw new UnsupportedOperationException();
			case AVAILABLE:
				throw new IllegalStateException("SizeType.AVAILABLE; must override size()");
			case FIXED:
				throw new IllegalStateException("SizeType.FIXED; must override size()");
			case UNAVAILABLE:
			default:
				return Iterators.size(iterable.iterator());
		}
	}

	static boolean isEmpty(SizedIterable<?> iterable) {
		SizeType sizeType = iterable.sizeType();
		switch (sizeType) {
			case AVAILABLE:
			case FIXED:
				return iterable.size() == 0;
			case INFINITE:
				return false;
			case UNAVAILABLE:
			default:
				return !iterable.iterator().hasNext();
		}
	}

	/**
	 * The type of size this {@code SizedIterable} has, which provides the guarantees that can be assumed on the
	 * evaluation of the {@link #size()} and {@link #isEmpty()} methods.
	 * <p>
	 * If this method returns {@link SizeType#AVAILABLE}, {@link #size()} and {@link #isEmpty()} is guaranteed to
	 * evaluate correct results without having to traverse the {@code SizedIterable}.
	 * <p>
	 * If this method returns {@link SizeType#FIXED}, {@link #size()} and {@link #isEmpty()} is guaranteed to
	 * evaluate to the same result for the lifetime of the {@code SizedIterable} without having to traverse it.
	 * <p>
	 * If this method returns {@link SizeType#UNAVAILABLE}, {@link #size()} and {@link #isEmpty()} may require
	 * traversal of the {@code SizedIterable} to determine their results, and may thus be an {@code O(n)} operation.
	 * <p>
	 * If this method returns {@link SizeType#INFINITE}, {@link #size()} should always throw
	 * {@link UnsupportedOperationException} and {@link #isEmpty()} should always return false.
	 */
	default SizeType sizeType() {
		return SizeType.UNAVAILABLE;
	}

	default int size() {
		return size(this);
	}

	/**
	 * @return the size of this {@code SizedIterable} if known a-priori, or -1 if the size is not known.
	 */
	default int sizeIfKnown() {
		switch (sizeType()) {
			case AVAILABLE:
			case FIXED:
				return size();
			case UNAVAILABLE:
			case INFINITE:
			default:
				return -1;
		}
	}

	default boolean isEmpty() {
		return isEmpty(this);
	}

	enum SizeType {
		UNAVAILABLE {
			@Override
			public SizeType limited() {
				return UNAVAILABLE;
			}

			@Override
			public int limitedSize(SizedIterable<?> parent, SizedIterable<?> iterable, int limit) {
				return SizedIterable.size(iterable);
			}
		},
		AVAILABLE {
			@Override
			public SizeType limited() {
				return AVAILABLE;
			}

			@Override
			public int limitedSize(SizedIterable<?> parent, SizedIterable<?> iterable, int limit) {
				return min(parent.size(), limit);
			}
		},
		FIXED {
			@Override
			public SizeType limited() {
				return FIXED;
			}

			@Override
			public int limitedSize(SizedIterable<?> parent, SizedIterable<?> iterable, int limit) {
				return min(parent.size(), limit);
			}
		},
		INFINITE {
			@Override
			public SizeType limited() {
				return AVAILABLE;
			}

			@Override
			public int limitedSize(SizedIterable<?> parent, SizedIterable<?> iterable, int limit) {
				return limit;
			}
		};

		public SizeType concat(SizeType sizeType) {
			if (this == INFINITE || sizeType == INFINITE)
				return INFINITE;

			if (this == UNAVAILABLE || sizeType == UNAVAILABLE)
				return UNAVAILABLE;

			if (this == AVAILABLE || sizeType == AVAILABLE)
				return AVAILABLE;

			return FIXED; // both FIXED
		}

		public SizeType intersect(SizeType sizeType) {
			if (this == UNAVAILABLE || sizeType == UNAVAILABLE)
				return UNAVAILABLE;

			if (this == INFINITE)
				return sizeType;

			if (sizeType == INFINITE)
				return this;

			if (this == AVAILABLE || sizeType == AVAILABLE)
				return AVAILABLE;

			return FIXED; // both FIXED
		}

		public abstract SizeType limited();

		public abstract int limitedSize(SizedIterable<?> parent, SizedIterable<?> iterable, int limit);
	}

	abstract class FixedSizedIterable<T> implements SizedIterable<T> {
		public abstract int size();

		@Override
		public SizeType sizeType() {
			return FIXED;
		}
	}
}
