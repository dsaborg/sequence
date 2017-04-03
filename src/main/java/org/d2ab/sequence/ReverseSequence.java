package org.d2ab.sequence;

import org.d2ab.iterator.ReverseArrayIterator;

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

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator() {
		return (Iterator<T>) new ReverseArrayIterator<>(parent.toArray());
	}

	@Override
	protected Sequence<T> withParent(Sequence<T> parent) {
		return new ReverseSequence<>(parent);
	}

	@Override
	public Optional<T> first() {
		return parent.last();
	}

	@Override
	public Optional<T> last() {
		return parent.first();
	}

	@Override
	public Optional<T> at(int index) {
		int size = sizeIfKnown();
		if (size == -1)
			return super.at(index);

		return index >= size ? Optional.empty() : parent.at(size - index - 1);
	}

	@Override
	public Optional<T> first(Predicate<? super T> predicate) {
		return parent.filter(predicate).last();
	}

	@Override
	public Optional<T> last(Predicate<? super T> predicate) {
		return parent.filter(predicate).first();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Optional<U> first(Class<U> targetClass) {
		return parent.filter(targetClass).last();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Optional<U> last(Class<U> targetClass) {
		return parent.filter(targetClass).first();
	}

	@Override
	public Optional<T> removeFirst() {
		return parent.removeLast();
	}

	@Override
	public Optional<T> removeLast() {
		return parent.removeFirst();
	}

	@Override
	public Optional<T> removeAt(int index) {
		int size = sizeIfKnown();
		if (size == -1)
			return super.removeAt(index);

		return index >= size ? Optional.empty() : parent.removeAt(size - index - 1);
	}

	@Override
	public Optional<T> removeFirst(Predicate<? super T> predicate) {
		return parent.filter(predicate).removeLast();
	}

	@Override
	public Optional<T> removeLast(Predicate<? super T> predicate) {
		return parent.filter(predicate).removeFirst();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Optional<U> removeFirst(Class<U> targetClass) {
		return parent.filter(targetClass).removeLast();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Optional<U> removeLast(Class<U> targetClass) {
		return parent.filter(targetClass).removeFirst();
	}

	@Override
	public Sequence<T> reverse() {
		return parent;
	}
}
