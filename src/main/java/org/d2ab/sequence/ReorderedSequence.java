package org.d2ab.sequence;

import java.util.function.Predicate;

abstract class ReorderedSequence<T> extends StableReorderedSequence<T> {
	protected ReorderedSequence(Sequence<T> parent) {
		super(parent);
	}

	@Override
	public Sequence<T> filter(Predicate<? super T> predicate) {
		return withParent(parent.filter(predicate));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Sequence<U> filter(Class<U> targetClass) {
		return withParent((Sequence) parent.filter(targetClass));
	}
}
