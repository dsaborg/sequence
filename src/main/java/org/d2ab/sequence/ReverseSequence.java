package org.d2ab.sequence;

import org.d2ab.iterator.ReverseIterator;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * An implementation of {@link Sequence} which provides a reverse view of another {@link Sequence}. Provides
 * optimizations for certain operations.
 */
class ReverseSequence<T> extends ReorderedSequence<T> {
	ReverseSequence(Sequence<T> original) {
		super(original);
	}

	@Override
	public Iterator<T> iterator() {
		return new ReverseIterator<>(original.iterator());
	}

	@Override
	protected Sequence<T> newInstance(Sequence<T> sequence) {
		return new ReverseSequence<>(sequence);
	}

	@Override
	public Optional<T> first() {
		return original.last();
	}

	@Override
	public Optional<T> last() {
		return original.first();
	}

	@Override
	public Optional<T> first(Predicate<? super T> predicate) {
		return original.filter(predicate).last();
	}

	@Override
	public Optional<T> last(Predicate<? super T> predicate) {
		return original.filter(predicate).first();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Optional<U> first(Class<U> targetClass) {
		return original.filter(targetClass).last();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Optional<U> last(Class<U> targetClass) {
		return original.filter(targetClass).first();
	}

	@Override
	public Optional<T> at(int index) {
		int size = sizeIfKnown();
		if (size == -1)
			return super.at(index);

		return index >= size ? Optional.empty() : original.at(size - index - 1);
	}

	@Override
	public Optional<T> removeFirst() {
		return original.removeLast();
	}

	@Override
	public Optional<T> removeLast() {
		return original.removeFirst();
	}

	@Override
	public Optional<T> removeAt(int index) {
		int size = sizeIfKnown();
		if (size == -1)
			return super.removeAt(index);

		return index >= size ? Optional.empty() : original.removeAt(size - index - 1);
	}

	@Override
	public Optional<T> removeFirst(Predicate<? super T> predicate) {
		return original.filter(predicate).removeLast();
	}

	@Override
	public Optional<T> removeLast(Predicate<? super T> predicate) {
		return original.filter(predicate).removeFirst();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Optional<U> removeFirst(Class<U> targetClass) {
		return original.filter(targetClass).removeLast();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Optional<U> removeLast(Class<U> targetClass) {
		return original.filter(targetClass).removeFirst();
	}

	@Override
	public Sequence<T> reverse() {
		return original;
	}
}
