/*
 * Copyright 2016 Daniel Skogquist Åborg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.d2ab.collection;

import org.d2ab.iterator.ArrayIterator;
import org.d2ab.iterator.Iterators;
import org.d2ab.util.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Utility methods for {@link Iterable} instances.
 */
public interface Iterables {
	/**
	 * @return an unmodifiable empty {@link Iterable}.
	 */
	static <T> Iterable<T> empty() {
		return Iterators::empty;
	}

	/**
	 * @return an unmodifiable singleton {@link Iterable} containing the given object.
	 */
	static <T> Iterable<T> of(T object) {
		return () -> new SingletonIterator<>(object);
	}

	/**
	 * @return an unmodifiable {@link Iterable} containing the given objects.
	 */
	@SafeVarargs
	static <T> Iterable<T> of(T... objects) {
		return () -> new ArrayIterator<>(objects);
	}

	/**
	 * Create a one-pass-only {@code Iterable} from an {@link Iterator} of items. Note that {@code Iterables} created
	 * from {@link Iterator}s will be exhausted when the given iterator has been passed over. Further attempts will
	 * register the {@code Iterable} as empty. If the iterator is terminated partway through iteration, further
	 * calls to {@link Iterable#iterator()} will pick up where the previous iterator left off. If
	 * {@link Iterable#iterator()} calls are interleaved, calls to the given iterator will be interleaved.
	 */
	static <T> Iterable<T> once(Iterator<T> iterator) {
		return () -> iterator;
	}

	/**
	 * Converts a container of some kind into a possibly once-only {@link Iterable}.
	 *
	 * @param container the non-null container to turn into an {@link Iterable}, can be one of {@link Iterable}, {@link
	 *                  Iterator}, {@link Stream}, {@code Array}, {@link Pair} or {@link Map.Entry}.
	 *
	 * @return the container as an iterable.
	 *
	 * @throws ClassCastException if the container is not one of {@link Iterable}, {@link Iterator}, {@link Stream},
	 *                            {@code Array}, {@link Pair} or {@link Map.Entry}
	 */
	@SuppressWarnings("unchecked")
	static <T> Iterable<T> from(Object container) {
		if (container instanceof Iterable)
			return (Iterable<T>) container;
		else if (container instanceof Iterator)
			return once((Iterator<T>) container);
		else if (container instanceof Stream)
			return once(((Stream<T>) container).iterator());
		else if (container instanceof Object[])
			return of((T[]) container);
		else if (container instanceof Pair)
			return ((Pair<T, T>) container)::iterator;
		else if (container instanceof Map.Entry)
			return () -> Maps.iterator((Map.Entry<T, T>) container);
		else
			throw new ClassCastException(
					"Required an Iterable, Iterator, Array, Stream, Pair or Entry but got: " + container.getClass());
	}

	/**
	 * @return true if all elements in this {@code Sequence} satisfy the given predicate, false otherwise.
	 */
	static <T> boolean all(Iterable<T> iterable, Predicate<? super T> predicate) {
		for (T each : iterable)
			if (!predicate.test(each))
				return false;

		return true;
	}

	/**
	 * @return true if no elements in this {@code Sequence} satisfy the given predicate, false otherwise.
	 */
	static <T> boolean none(Iterable<T> iterable, Predicate<? super T> predicate) {
		return !any(iterable, predicate);
	}

	/**
	 * @return true if any element in this {@code Sequence} satisfies the given predicate, false otherwise.
	 */
	static <T> boolean any(Iterable<T> iterable, Predicate<? super T> predicate) {
		for (T each : iterable)
			if (predicate.test(each))
				return true;

		return false;
	}

	/**
	 * Remove all elements in the given {@link Iterable} using {@link Iterator#remove()}.
	 */
	static void clear(Iterable<?> iterable) {
		for (Iterator<?> iterator = iterable.iterator(); iterator.hasNext(); ) {
			iterator.next();
			iterator.remove();
		}
	}

	/**
	 * Remove all elements in the given {@link Iterable} found among the given items, using {@link Iterator#remove()}.
	 */
	static boolean removeAll(Iterable<?> iterable, Object... items) {
		boolean modified = false;
		for (Iterator<?> iterator = iterable.iterator(); iterator.hasNext(); )
			if (Arrayz.contains(items, iterator.next())) {
				iterator.remove();
				modified = true;
			}
		return modified;
	}

	/**
	 * Remove all elements in the given {@link Iterable} found in the second {@link Iterable},
	 * using {@link Iterator#remove()}.
	 */
	static boolean removeAll(Iterable<?> iterable, Iterable<?> items) {
		boolean modified = false;
		for (Iterator<?> iterator = iterable.iterator(); iterator.hasNext(); )
			if (contains(items, iterator.next())) {
				iterator.remove();
				modified = true;
			}
		return modified;
	}

	/**
	 * Remove all elements in the given {@link Iterable} found among the given items, using {@link Iterator#remove()}.
	 */
	static boolean retainAll(Iterable<?> iterable, Object... items) {
		boolean modified = false;
		for (Iterator<?> iterator = iterable.iterator(); iterator.hasNext(); )
			if (!Arrayz.contains(items, iterator.next())) {
				iterator.remove();
				modified = true;
			}
		return modified;
	}

	/**
	 * Remove all elements in the given {@link Iterable} found in the second {@link Iterable},
	 * using {@link Iterator#remove()}.
	 */
	static boolean retainAll(Iterable<?> iterable, Iterable<?> items) {
		boolean modified = false;
		for (Iterator<?> iterator = iterable.iterator(); iterator.hasNext(); )
			if (!contains(items, iterator.next())) {
				iterator.remove();
				modified = true;
			}
		return modified;
	}

	/**
	 * @return the given {@link Iterable} collected into a {@link List}.
	 */
	static <T> List<T> toList(Iterable<T> iterable) {
		if (iterable instanceof Collection) {
			return new ArrayList<>((Collection<T>) iterable);
		} else {
			return collect(iterable, ArrayList::new);
		}
	}

	/**
	 * @return the given {@link Iterable} collected into a {@link Collection} of the type determined by the given
	 * {@link Collection} constructor.
	 */
	static <T, C extends Collection<T>> C collect(Iterable<T> iterable, Supplier<C> supplier) {
		C collection = supplier.get();
		for (T t : iterable)
			collection.add(t);
		return collection;
	}

	/**
	 * Create a {@link List} view of the given {@link Iterable}, where changes in the underlying {@link Iterable} are
	 * reflected in the returned {@link List}. If a {@link List} is given it is returned unchanged. The list does not
	 * implement {@link RandomAccess} unless the given {@link Iterable} does, and is best accessed in sequence. The
	 * list does not support modification except the various removal operations, through {@link Iterator#remove()} only
	 * if implemented in the {@link Iterable}'s {@link Iterable#iterator()}.
	 *
	 * @since 1.2
	 */
	static <T> List<T> asList(Iterable<T> iterable) {
		return new IterableList<>(iterable);
	}

	/**
	 * @return true if any object in the given {@link Iterable} is equal to the given object, false otherwise.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	static <T> boolean contains(Iterable<? extends T> iterable, T object) {
		if (iterable instanceof Collection)
			return ((Collection<? extends T>) iterable).contains(object);

		for (T each : iterable)
			if (Objects.equals(each, object))
				return true;

		return false;
	}

	/**
	 * @return true if the given {@link Collection} contains all of the given items, false otherwise.
	 *
	 * @since 1.2
	 */
	@SafeVarargs
	static <T> boolean containsAll(Collection<?> collection, T... items) {
		for (T item : items)
			if (!collection.contains(item))
				return false;

		return true;
	}

	/**
	 * @return true if the given {@link Collection} contains any of the given items, false otherwise.
	 *
	 * @since 1.2
	 */
	@SafeVarargs
	static <T> boolean containsAny(Collection<?> collection, T... items) {
		for (Object item : items)
			if (collection.contains(item))
				return true;

		return false;
	}

	/**
	 * @return true if the given {@link Collection} contains any of the given items, false otherwise.
	 *
	 * @since 1.2
	 */
	static boolean containsAny(Collection<?> collection, Iterable<?> items) {
		for (Object item : items)
			if (collection.contains(item))
				return true;

		return false;
	}

	class SingletonIterator<T> implements Iterator<T> {
		private final T object;
		private boolean used;

		public SingletonIterator(T object) {
			this.object = object;
		}

		@Override
		public boolean hasNext() {
			return !used;
		}

		@Override
		public T next() {
			if (used)
				throw new NoSuchElementException();

			used = true;
			return object;
		}
	}
}
