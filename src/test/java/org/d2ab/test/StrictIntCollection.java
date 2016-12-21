package org.d2ab.test;

import org.d2ab.collection.ints.IntCollection;
import org.d2ab.collection.ints.IntList;
import org.d2ab.iterator.ints.IntIterator;

import java.util.Collection;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface StrictIntCollection extends IntCollection {
	static IntCollection of(int... values) {
		return from(IntList.of(values));
	}

	static IntCollection from(IntCollection collection) {
		return new Base() {
			@Override
			public IntIterator iterator() {
				return StrictIntIterator.from(collection.iterator());
			}

			@Override
			public int size() {
				return collection.size();
			}

			@Override
			public boolean addInt(int x) {
				return collection.addInt(x);
			}

			@Override
			public boolean addAllInts(int... xs) {
				return collection.addAllInts(xs);
			}

			@Override
			public boolean addAllInts(IntCollection xs) {
				return collection.addAllInts(xs);
			}

			@Override
			public Integer[] toArray() {
				throw new UnsupportedOperationException();
			}

			@Override
			public <T> T[] toArray(T[] a) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Spliterator.OfInt spliterator() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Stream<Integer> stream() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Stream<Integer> parallelStream() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean add(Integer x) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean contains(Object o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean remove(Object o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean addAll(Collection<? extends Integer> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean containsAll(Collection<?> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean removeIf(Predicate<? super Integer> filter) {
				throw new UnsupportedOperationException();
			}
		};
	}
}
