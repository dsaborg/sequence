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

package org.d2ab.sequence;

import org.d2ab.collection.*;
import org.d2ab.function.ObjIntFunction;
import org.d2ab.function.ObjIntPredicate;
import org.d2ab.function.ToCharFunction;
import org.d2ab.iterator.*;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.util.Pair;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static org.d2ab.collection.SizedIterable.SizeType.*;
import static org.d2ab.function.BinaryOperators.firstMaxBy;
import static org.d2ab.function.BinaryOperators.firstMinBy;
import static org.d2ab.util.Preconditions.*;

/**
 * An {@link Iterable} sequence of elements with {@link Stream}-like operations for refining, transforming and collating
 * the list of elements.
 * <p>
 * {@code Sequence} is fundamentally built on {@link Iterator}, however it implements {@link Collection} on a
 * best-effort basis, providing native mutation and querying operations supplied by {@link Collection} to the best
 * degree possible based on the backing storage of the {@code Sequence} and the combination of operations applied on it.
 * Be aware that this means that some methods on {@link Collection} has degraded performance similar to that of
 * {@link LinkedList}. In addition, {@link #size()} may degrade to {@code O(n)} performance under certain combinations
 * of operations where knowing the size in advance is not possible. Take caution when using the {@code Sequence} as a
 * {@link Collection}.
 */
@FunctionalInterface
public interface Sequence<T> extends IterableCollection<T> {
	Sequence EMPTY = new Sequence() {
		@Override
		public Iterator iterator() {
			return Iterators.empty();
		}

		@Override
		public SizeType sizeType() {
			return FIXED;
		}

		@Override
		public int size() {
			return 0;
		}
	};

	/**
	 * @return an empty immutable {@code Sequence} with no items.
	 *
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 */
	@SuppressWarnings("unchecked")
	static <T> Sequence<T> empty() {
		return EMPTY;
	}

	/**
	 * @return an immutable {@code Sequence} with one item.
	 *
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 */
	static <T> Sequence<T> of(T item) {
		return ListSequence.of(item);
	}

	/**
	 * @return an immutable {@code Sequence} with the given items.
	 *
	 * @see #of(Object)
	 * @see #from(Iterable)
	 */
	@SafeVarargs
	static <T> Sequence<T> of(T... items) {
		return ListSequence.of(items);
	}

	/**
	 * Create a {@code Sequence} backed by an {@link Iterable} of items. Mutation is supported to whatever extent
	 * supported by the given {@link Iterable} and the combination of operators applied to the resulting {@code
	 * Sequence}.
	 *
	 * @see #from(SizedIterable)
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #once(Iterator)
	 */
	static <T> Sequence<T> from(Iterable<T> iterable) {
		requireNonNull(iterable, "iterable");

		if (iterable instanceof List)
			return ListSequence.from((List<T>) iterable);

		if (iterable instanceof SizedIterable)
			return from((SizedIterable<T>) iterable);

		if (iterable instanceof Collection)
			return CollectionSequence.from((Collection<T>) iterable);

		return iterable::iterator;
	}

	/**
	 * Create a {@code Sequence} backed by a {@link SizedIterable} of items. Mutation is supported to whatever extent
	 * supported by the given {@link SizedIterable} and the combination of operators applied to the resulting {@code
	 * Sequence}.
	 *
	 * @see #from(Iterable)
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #once(Iterator)
	 */
	static <T> Sequence<T> from(SizedIterable<T> sizedIterable) {
		requireNonNull(sizedIterable, "sizedIterable");

		return new Sequence<T>() {
			@Override
			public Iterator<T> iterator() {
				return sizedIterable.iterator();
			}

			@Override
			public int size() {
				return sizedIterable.size();
			}

			@Override
			public SizeType sizeType() {
				return sizedIterable.sizeType();
			}

			@Override
			public boolean isEmpty() {
				return sizedIterable.isEmpty();
			}
		};
	}

	/**
	 * Create a {@code Sequence} of {@link Entry} key/value items from a {@link Map} of items. The resulting
	 * {@code Sequence} can be mapped using {@link Pair} items, which implement {@link Entry} and can thus be
	 * processed as part of the {@code Sequence}'s transformation steps.
	 *
	 * @see #of
	 * @see #from(Iterable)
	 */
	static <K, V> Sequence<Entry<K, V>> from(Map<K, V> map) {
		requireNonNull(map, "map");

		return from(map.entrySet());
	}

	/**
	 * Create a concatenated {@code Sequence} from several {@link Iterable}s which are concatenated together to form
	 * the stream of items in the {@code Sequence}.
	 *
	 * @see #concat(Iterable)
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 * @since 1.1.1
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	static <T> Sequence<T> concat(Iterable<T>... iterables) {
		requireNonNull(iterables, "iterables");
		for (Iterable<T> iterable : iterables)
			requireNonNull(iterable, "each iterable");

		if (Arrayz.all(iterables, List.class::isInstance))
			return ListSequence.concat(Arrays.copyOf(iterables, iterables.length, List[].class));

		if (Arrayz.all(iterables, Collection.class::isInstance))
			return CollectionSequence.concat(Arrays.copyOf(iterables, iterables.length, Collection[].class));

		return from(ChainingIterable.concat(iterables));
	}

	/**
	 * Create a concatenated {@code Sequence} from several {@link Iterable}s which are concatenated together to form
	 * the stream of items in the {@code Sequence}.
	 *
	 * @see #concat(Iterable[])
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 * @since 1.1.1
	 */
	@SuppressWarnings("unchecked")
	static <T> Sequence<T> concat(Iterable<Iterable<T>> iterables) {
		requireNonNull(iterables, "iterables");
		for (Iterable<T> iterable : iterables)
			requireNonNull(iterable, "each iterable");

		if (Iterables.all(iterables, List.class::isInstance))
			if (iterables instanceof List)
				return ListSequence.concat((List) iterables);
			else if (iterables instanceof Collection)
				return ListSequence.concat(Collectionz.asList((Collection) iterables));
			else
				return ListSequence.concat(Iterables.asList((Iterable) iterables));

		if (Iterables.all(iterables, Collection.class::isInstance))
			if (iterables instanceof Collection)
				return CollectionSequence.concat((Collection) iterables);
			else
				return CollectionSequence.concat(Iterables.asList((Iterable) iterables));

		return from(ChainingIterable.concat(iterables));
	}

	/**
	 * Create a one-pass-only {@code Sequence} from an {@link Iterator} of items. Note that {@code Sequences} created
	 * from {@link Iterator}s will be exhausted when the given iterator has been passed over. Further attempts will
	 * register the {@code Sequence} as empty. If the sequence is terminated partway through iteration, further
	 * calls to {@link #iterator()} will pick up where the previous iterator left off. If {@link #iterator()} calls
	 * are interleaved, calls to the given iterator will be interleaved.
	 *
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 * @see #cache(Iterator)
	 * @since 1.1
	 */
	static <T> Sequence<T> once(Iterator<T> iterator) {
		requireNonNull(iterator, "iterator");

		return from(Iterables.once(iterator));
	}

	/**
	 * Create a one-pass-only {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created
	 * from {@link Stream}s will be exhausted when the given stream has been passed over. Further attempts will
	 * register the {@code Sequence} as empty. If the sequence is terminated partway through iteration, further
	 * calls to {@link #iterator()} will pick up where the previous iterator left off. If {@link #iterator()} calls
	 * are interleaved, calls to the given stream will be interleaved.
	 *
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 * @see #once(Iterator)
	 * @see #cache(Stream)
	 * @since 1.1
	 */
	static <T> Sequence<T> once(Stream<T> stream) {
		requireNonNull(stream, "stream");

		return once(stream.iterator());
	}

	/**
	 * Create a {@code Sequence} with a cached copy of an {@link Iterable} of items.
	 *
	 * @see #cache(Iterator)
	 * @see #cache(Stream)
	 * @see #from(Iterable)
	 * @since 1.1
	 */
	static <T> Sequence<T> cache(Iterable<T> iterable) {
		requireNonNull(iterable, "iterable");

		return from(Iterables.toList(iterable));
	}

	/**
	 * Create a {@code Sequence} with a cached copy of an {@link Iterator} of items.
	 *
	 * @see #cache(Iterable)
	 * @see #cache(Stream)
	 * @see #once(Iterator)
	 * @since 1.1
	 */
	static <T> Sequence<T> cache(Iterator<T> iterator) {
		requireNonNull(iterator, "iterator");

		return from(Iterators.toList(iterator));
	}

	/**
	 * Create a {@code Sequence} with a cached copy of a {@link Stream} of items.
	 *
	 * @see #cache(Iterable)
	 * @see #cache(Iterator)
	 * @see #once(Stream)
	 * @since 1.1
	 */
	static <T> Sequence<T> cache(Stream<T> stream) {
		requireNonNull(stream, "stream");

		return from(stream.collect(Collectors.toList()));
	}

	/**
	 * @return a new empty mutable {@code Sequence}.
	 */
	static <T> Sequence<T> create() {
		return ListSequence.create();
	}

	/**
	 * @return a new empty mutable {@code Sequence} with the given initial capacity.
	 */
	static <T> Sequence<T> withCapacity(int capacity) {
		requireAtLeastZero(capacity, "capacity");

		return ListSequence.withCapacity(capacity);
	}

	/**
	 * @return a new mutable {@code Sequence} initialized with the given elements.
	 */
	@SafeVarargs
	static <T> Sequence<T> createOf(T... items) {
		requireNonNull(items, "items");

		return ListSequence.createOf(items);
	}

	/**
	 * @return a new mutable {@code Sequence} initialized with the elements in the given {@link Collection}.
	 */
	static <T> Sequence<T> createFrom(Collection<? extends T> collection) {
		requireNonNull(collection, "collection");

		return ListSequence.createFrom(collection);
	}

	/**
	 * @return a new mutable {@code Sequence} initialized with the elements in the given {@link Iterable}.
	 */
	@SuppressWarnings("unchecked")
	static <T> Sequence<T> createFrom(Iterable<? extends T> iterable) {
		requireNonNull(iterable, "iterable");

		if (iterable instanceof Collection)
			return createFrom((Collection<T>) iterable);

		return ListSequence.createFrom(iterable);
	}

	/**
	 * @return a new mutable {@code Sequence} initialized with the remaining elements in the given {@link Iterator}.
	 */
	static <T> Sequence<T> createFrom(Iterator<? extends T> iterator) {
		requireNonNull(iterator, "iterator");

		return ListSequence.createFrom(iterator);
	}

	/**
	 * A {@code Sequence} of all the positive {@link Integer} numbers starting at {@code 1} and ending at {@link
	 * Integer#MAX_VALUE} inclusive.
	 *
	 * @see #intsFromZero()
	 * @see #intsFrom(int)
	 * @see #range(int, int)
	 * @deprecated Use {@link IntSequence#positive()} instead.
	 */
	@Deprecated
	static Sequence<Integer> ints() {
		return range(1, Integer.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the positive {@link Integer} numbers starting at {@code 0} and ending at {@link
	 * Integer#MAX_VALUE} inclusive.
	 *
	 * @see #ints()
	 * @see #intsFrom(int)
	 * @see #range(int, int)
	 * @deprecated Use {@link IntSequence#positiveFromZero()} instead.
	 */
	@Deprecated
	static Sequence<Integer> intsFromZero() {
		return range(0, Integer.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Integer} numbers starting at the given start and ending at {@link
	 * Integer#MAX_VALUE} inclusive.
	 * <p>
	 * The start value may be negative, in which case the sequence will continue towards positive numbers and
	 * eventually {@link Integer#MAX_VALUE}.
	 *
	 * @see #ints()
	 * @see #range(int, int)
	 * @since 1.1
	 * @deprecated Use {@link IntSequence#increasingFrom(int)} instead.
	 */
	@Deprecated
	static Sequence<Integer> intsFrom(int start) {
		return range(start, Integer.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Integer} numbers between the given start and end positions, inclusive.
	 * If the end index is less than the start index, the resulting {@code Sequence} will be counting down from the
	 * start to the end.
	 *
	 * @see #ints()
	 * @see #intsFromZero()
	 * @see #intsFrom(int)
	 * @deprecated Use {@link IntSequence#range(int, int)} instead.
	 */
	@Deprecated
	static Sequence<Integer> range(int start, int end) {
		UnaryOperator<Integer> next = (end > start) ? i -> ++i : i -> --i;
		return recurse(start, next).endingAt(end);
	}

	/**
	 * A {@code Sequence} of all the positive {@link Long} numbers starting at {@code 1} and ending at {@link
	 * Long#MAX_VALUE} inclusive.
	 *
	 * @see #longsFromZero()
	 * @see #longsFrom(long)
	 * @see #range(long, long)
	 * @deprecated Use {@link LongSequence#positive()} instead.
	 */
	@Deprecated
	static Sequence<Long> longs() {
		return range(1, Long.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the positive {@link Long} numbers starting at {@code 0} and ending at {@link
	 * Long#MAX_VALUE} inclusive.
	 *
	 * @see #longs()
	 * @see #longsFrom(long)
	 * @see #range(long, long)
	 * @deprecated Use {@link LongSequence#positiveFromZero()} instead.
	 */
	@Deprecated
	static Sequence<Long> longsFromZero() {
		return range(0, Long.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Long} numbers starting at the given value and ending at {@link
	 * Long#MAX_VALUE} inclusive.
	 * <p>
	 * The start value may be negative, in which case the sequence will continue towards positive numbers and
	 * eventually {@link Long#MAX_VALUE}.
	 *
	 * @see #longs()
	 * @see #longsFromZero()
	 * @see #range(long, long)
	 * @since 1.1
	 * @deprecated Use {@link LongSequence#increasingFrom(long)} instead.
	 */
	@Deprecated
	static Sequence<Long> longsFrom(long start) {
		return range(start, Long.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Long} numbers between the given start and end positions, inclusive.
	 * If the end index is less than the start index, the resulting {@code Sequence} will be counting down from the
	 * start to the end.
	 *
	 * @see #longs()
	 * @see #longsFromZero()
	 * @see #longsFrom(long)
	 * @deprecated Use {@link LongSequence#range(long, long)} instead.
	 */
	@Deprecated
	static Sequence<Long> range(long start, long end) {
		UnaryOperator<Long> next = (end > start) ? i -> ++i : i -> --i;
		return recurse(start, next).endingAt(end);
	}

	/**
	 * A {@code Sequence} of all the {@link Character} values starting at {@link Character#MIN_VALUE} and ending at
	 * {@link Character#MAX_VALUE} inclusive.
	 *
	 * @see #charsFrom(char)
	 * @see #range(char, char)
	 * @deprecated Use {@link CharSeq#all()} instead.
	 */
	@Deprecated
	static Sequence<Character> chars() {
		return range(Character.MIN_VALUE, Character.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Character} values starting at the given value and ending at {@link
	 * Character#MAX_VALUE} inclusive.
	 *
	 * @see #chars()
	 * @see #range(char, char)
	 * @since 1.1
	 * @deprecated Use {@link CharSeq#startingAt(char)} instead.
	 */
	@Deprecated
	static Sequence<Character> charsFrom(char start) {
		return range(start, Character.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Character} values between the given start and end positions, inclusive.
	 * If the end index is less than the start index, the resulting {@code Sequence} will be counting down from the
	 * start to the end.
	 *
	 * @see #chars()
	 * @see #charsFrom(char)
	 * @deprecated Use {@link CharSeq#range(char, char)} instead.
	 */
	@Deprecated
	static Sequence<Character> range(char start, char end) {
		UnaryOperator<Character> next = (end > start) ? c -> (char) (c + 1) : c -> (char) (c - 1);
		return recurse(start, next).endingAt(end);
	}

	/**
	 * @return an infinite {@code Sequence} generated by repeatedly calling the given supplier. The returned {@code
	 * Sequence} never terminates naturally. If {@link #iterator()} is called multiple times, further iterators will
	 * pick up where the previous iterator left off in the given supplier. If iterator calls are interleaved, calls to
	 * the supplier will be interleaved.
	 *
	 * @see #multiGenerate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #endingAt(Object)
	 * @see #until(Object)
	 */
	static <T> Sequence<T> generate(Supplier<? extends T> supplier) {
		requireNonNull(supplier, "supplier");

		return new Sequence<T>() {
			@Override
			public Iterator<T> iterator() {
				return (InfiniteIterator<T>) supplier::get;
			}

			@Override
			public SizeType sizeType() {
				return INFINITE;
			}
		};
	}

	/**
	 * @return an infinite {@code Sequence} where each {@link #iterator()} is generated by polling for a {@link
	 * Supplier} and then using it to generate the sequence of elements. The sequence never terminates.
	 *
	 * @see #generate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #endingAt(Object)
	 * @see #until(Object)
	 */
	static <T> Sequence<T> multiGenerate(Supplier<? extends Supplier<? extends T>> multiSupplier) {
		requireNonNull(multiSupplier, "multiSupplier");

		return new Sequence<T>() {
			@Override
			public Iterator<T> iterator() {
				Supplier<? extends T> supplier = requireNonNull(multiSupplier.get(), "multiSupplier.get()");
				return (InfiniteIterator<T>) supplier::get;
			}

			@Override
			public SizeType sizeType() {
				return INFINITE;
			}
		};
	}

	/**
	 * Returns a {@code Sequence} produced by recursively applying the given operation to the given seed, which forms
	 * the first element of the sequence, the second being {@code f(seed)}, the third [@code f(f(seed))} and so on.
	 * The returned {@code Sequence} never terminates naturally.
	 *
	 * @return a {@code Sequence} produced by recursively applying the given operation to the given seed
	 *
	 * @see #recurse(Object, Function, Function)
	 * @see #multiGenerate(Supplier)
	 * @see #endingAt(Object)
	 * @see #until(Object)
	 */
	static <T> Sequence<T> recurse(T seed, UnaryOperator<T> f) {
		requireNonNull(f, "f");

		return new Sequence<T>() {
			@Override
			public Iterator<T> iterator() {
				return new RecursiveIterator<>(seed, f);
			}

			@Override
			public SizeType sizeType() {
				return INFINITE;
			}
		};
	}

	/**
	 * Returns a {@code Sequence} produced by recursively applying the given mapper {@code f} and incrementer
	 * {@code g} operations to the given seed, the first element being {@code f(seed)}, the second being
	 * {@code f(g(f(seed)))}, the third {@code f(g(f(g(f(seed)))))} and so on. The returned {@code Sequence} never
	 * terminates naturally.
	 *
	 * @param f a mapper function for producing elements that are to be included in the sequence, the first being
	 *          f(seed)
	 * @param g an incrementer function for producing the next unmapped element to be included in the sequence, applied
	 *          to the first mapped element f(seed) to produce the second unmapped value
	 *
	 * @return a {@code Sequence} produced by recursively applying the given mapper and incrementer operations to the
	 * given seed
	 *
	 * @see #recurse(Object, UnaryOperator)
	 * @see #endingAt(Object)
	 * @see #until(Object)
	 */
	static <T, S> Sequence<S> recurse(T seed, Function<? super T, ? extends S> f, Function<? super S, ? extends T> g) {
		requireNonNull(f, "f");
		requireNonNull(g, "g");

		return recurse(f.apply(seed), f.compose(g)::apply);
	}

	/**
	 * @return an immutable view of this {@code Sequence}.
	 */
	default Sequence<T> immutable() {
		return new EquivalentSizeSequence<>(this, ImmutableIterator::new);
	}

	/**
	 * Terminate this {@code Sequence} just before the given element is encountered, not including the element in the
	 * {@code Sequence}.
	 *
	 * @see #untilNull()
	 * @see #until(Predicate)
	 * @see #endingAt(Object)
	 * @see #multiGenerate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #recurse(Object, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> until(T terminal) {
		return () -> new ExclusiveTerminalIterator<>(iterator(), terminal);
	}

	/**
	 * Terminate this {@code Sequence} when the given element is encountered, including the element as the last element
	 * in the {@code Sequence}.
	 *
	 * @see #endingAtNull
	 * @see #endingAt(Predicate)
	 * @see #until(Object)
	 * @see #multiGenerate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #recurse(Object, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> endingAt(T terminal) {
		return () -> new InclusiveTerminalIterator<>(iterator(), terminal);
	}

	/**
	 * Terminate this {@code Sequence} just before a null element is encountered, not including the null in the
	 * {@code Sequence}.
	 *
	 * @see #until(Object)
	 * @see #until(Predicate)
	 * @see #endingAtNull
	 * @see #multiGenerate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #recurse(Object, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> untilNull() {
		return () -> new ExclusiveTerminalIterator<>(iterator(), (T) null);
	}

	/**
	 * Terminate this {@code Sequence} when a null element is encountered, including the null as the last element
	 * in the {@code Sequence}.
	 *
	 * @see #endingAt(Object)
	 * @see #endingAt(Predicate)
	 * @see #untilNull
	 * @see #multiGenerate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #recurse(Object, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> endingAtNull() {
		return () -> new InclusiveTerminalIterator<>(iterator(), (T) null);
	}

	/**
	 * Terminate this {@code Sequence} just before the given predicate is satisfied, not including the element that
	 * satisfies the predicate in the {@code Sequence}.
	 *
	 * @see #until(Object)
	 * @see #untilNull()
	 * @see #endingAt(Predicate)
	 * @see #multiGenerate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #recurse(Object, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> until(Predicate<T> terminal) {
		requireNonNull(terminal, "terminal");

		return () -> new ExclusiveTerminalIterator<>(iterator(), terminal);
	}

	/**
	 * Terminate this {@code Sequence} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the last element in the {@code Sequence}.
	 *
	 * @see #endingAt(Object)
	 * @see #endingAtNull()
	 * @see #until(Predicate)
	 * @see #multiGenerate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #recurse(Object, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> endingAt(Predicate<T> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new InclusiveTerminalIterator<>(iterator(), predicate);
	}

	/**
	 * Begin this {@code Sequence} just after the given element is encountered, not including the element in the
	 * {@code Sequence}.
	 *
	 * @see #startingAfter(Predicate)
	 * @see #startingFrom(Object)
	 * @since 1.1
	 */
	default Sequence<T> startingAfter(T element) {
		return () -> new ExclusiveStartingIterator<>(iterator(), element);
	}

	/**
	 * Begin this {@code Sequence} when the given element is encountered, including the element as the first element
	 * in the {@code Sequence}.
	 *
	 * @see #startingFrom(Predicate)
	 * @see #startingAfter(Object)
	 * @since 1.1
	 */
	default Sequence<T> startingFrom(T element) {
		return () -> new InclusiveStartingIterator<>(iterator(), element);
	}

	/**
	 * Begin this {@code Sequence} just after the given predicate is satisfied, not including the element that
	 * satisfies the predicate in the {@code Sequence}.
	 *
	 * @see #startingAfter(Object)
	 * @see #startingFrom(Predicate)
	 * @since 1.1
	 */
	default Sequence<T> startingAfter(Predicate<? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new ExclusiveStartingIterator<>(iterator(), predicate);
	}

	/**
	 * Begin this {@code Sequence} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the first element in the {@code Sequence}.
	 *
	 * @see #startingFrom(Object)
	 * @see #startingAfter(Predicate)
	 * @since 1.1
	 */
	default Sequence<T> startingFrom(Predicate<? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new InclusiveStartingIterator<>(iterator(), predicate);
	}

	/**
	 * Map the values in this {@code Sequence} to another set of values specified by the given {@code mapper} function.
	 *
	 * @see #biMap(Function, Function)
	 * @see #mapIndexed(ObjIntFunction)
	 * @see #mapBack(BiFunction)
	 * @see #mapForward(BiFunction)
	 * @see #flatten()
	 * @see #flatten(Function)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 */
	default <U> Sequence<U> map(Function<? super T, ? extends U> mapper) {
		requireNonNull(mapper, "mapper");

		return new EquivalentSizeSequence<>(this, it -> new MappingIterator<>(it, mapper));
	}

	/**
	 * Map the values in this {@code Sequence} to another set of values specified by the given {@code mapper}
	 * functions, allowing for backwards mapping using {@code backMapper} so elements can be added to underlying
	 * {@link Collection}s after being mapped.
	 *
	 * @see #map(Function)
	 */
	default <U> Sequence<U> biMap(Function<? super T, ? extends U> mapper,
	                              Function<? super U, ? extends T> backMapper) {
		requireNonNull(mapper, "mapper");
		requireNonNull(backMapper, "backMapper");

		return new EquivalentSizeSequence<>(this, it -> new MappingIterator<>(it, mapper));
	}

	/**
	 * Cast the values in this {@code Sequence} to the given {@link Class}.
	 *
	 * @see #map(Function)
	 */
	@SuppressWarnings({"unchecked", "unused"})
	default <U> Sequence<U> cast(Class<U> clazz) {
		return (Sequence<U>) this;
	}

	/**
	 * Map the values in this {@code Sequence} to another set of values specified by the given {@code mapper} function.
	 * In addition to the current element, the mapper has access to the index of each element.
	 *
	 * @see #map(Function)
	 * @see #mapBack(BiFunction)
	 * @see #mapForward(BiFunction)
	 * @see #flatten()
	 * @see #flatten(Function)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @since 1.2
	 */
	default <U> Sequence<U> mapIndexed(ObjIntFunction<? super T, ? extends U> mapper) {
		requireNonNull(mapper, "mapper");

		return new EquivalentSizeSequence<>(this, it -> new IndexingMappingIterator<>(it, mapper));
	}

	/**
	 * Map this {@code Sequence} to another sequence while peeking at the previous element in the iteration.
	 * <p>
	 * The mapper has access to the previous element and the next element in the iteration. {@code null} is provided
	 * as the first previous value when the next element is the first value in the sequence.
	 *
	 * @see #mapBack(Object, BiFunction)
	 */
	default <U> Sequence<U> mapBack(BiFunction<? super T, ? super T, ? extends U> mapper) {
		requireNonNull(mapper, "mapper");

		return mapBack(null, mapper);
	}

	/**
	 * Map this {@code Sequence} to another sequence while peeking at the following element in the iteration.
	 * <p>
	 * The mapper has access to the next element and the following element in the iteration. {@code null} is
	 * provided as the last following value when the next element is the last value in the sequence.
	 *
	 * @see #mapForward(Object, BiFunction)
	 */
	default <U> Sequence<U> mapForward(BiFunction<? super T, ? super T, ? extends U> mapper) {
		requireNonNull(mapper, "mapper");

		return mapForward(null, mapper);
	}

	/**
	 * Map this {@code Sequence} to another sequence while peeking at the previous element in the iteration.
	 * <p>
	 * The mapper has access to the previous element and the next element in the iteration. The given replacement value
	 * is provided as a prefix to the sequence for the first value in the sequence.
	 *
	 * @see #mapBack(BiFunction)
	 */
	default <U> Sequence<U> mapBack(T replacement, BiFunction<? super T, ? super T, ? extends U> mapper) {
		requireNonNull(mapper, "mapper");

		return new EquivalentSizeSequence<>(this, it -> new BackPeekingMappingIterator<T, U>(it, replacement) {
			@Override
			protected U map(T previous, T next) {
				return mapper.apply(previous, next);
			}
		});
	}

	/**
	 * Map this {@code Sequence} to another sequence while peeking at the following element in the iteration.
	 * <p>
	 * The mapper has access to the next element and the following element in the iteration. The given replacement
	 * value is provided as a suffix to the sequence for the last value in the sequence.
	 *
	 * @see #mapForward(BiFunction)
	 */
	default <U> Sequence<U> mapForward(T replacement, BiFunction<? super T, ? super T, ? extends U> mapper) {
		requireNonNull(mapper, "mapper");

		return new EquivalentSizeSequence<>(this, it -> new ForwardPeekingMappingIterator<T, U>(it, replacement) {
			@Override
			protected T mapFollowing(boolean hasFollowing, T following) {
				return following;
			}

			@Override
			protected U mapNext(T next, T following) {
				return mapper.apply(next, following);
			}
		});
	}

	/**
	 * Skip a set number of steps in this {@code Sequence}.
	 */
	default Sequence<T> skip(int skip) {
		requireAtLeastZero(skip, "skip");

		if (skip == 0)
			return this;

		return new Sequence<T>() {
			@Override
			public Iterator<T> iterator() {
				return new SkippingIterator<>(Sequence.this.iterator(), skip);
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public int size() {
				return Math.max(0, Sequence.this.size() - skip);
			}
		};
	}

	/**
	 * Skip a set number of steps at the end of this {@code Sequence}.
	 *
	 * @since 1.1
	 */
	default Sequence<T> skipTail(int skip) {
		requireAtLeastZero(skip, "skip");

		if (skip == 0)
			return this;

		return new Sequence<T>() {
			@Override
			public Iterator<T> iterator() {
				return new TailSkippingIterator<>(Sequence.this.iterator(), skip);
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public int size() {
				return Math.max(0, Sequence.this.size() - skip);
			}
		};
	}

	/**
	 * Limit the maximum number of results returned by this {@code Sequence}.
	 */
	default Sequence<T> limit(int limit) {
		requireAtLeastZero(limit, "limit");

		if (limit == 0)
			return empty();

		return new Sequence<T>() {
			@Override
			public Iterator<T> iterator() {
				return new LimitingIterator<>(Sequence.this.iterator(), limit);
			}

			@Override
			public int size() {
				return Sequence.this.sizeType().limitedSize(Sequence.this, this, limit);
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType().limited();
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Limit the results returned by this {@code Sequence} to the last {@code limit} items.
	 *
	 * @since 2.3
	 */
	default Sequence<T> limitTail(int limit) {
		requireAtLeastZero(limit, "limit");

		if (limit == 0)
			return empty();

		requireFinite(this, "Infinite Sequence");

		return new Sequence<T>() {
			@Override
			public Iterator<T> iterator() {
				return new TailLimitingIterator<>(Sequence.this.iterator(), limit);
			}

			@Override
			public int size() {
				return Sequence.this.sizeType().limitedSize(Sequence.this, this, limit);
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType().limited();
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Append the given elements to the end of this {@code Sequence}.
	 */
	@SuppressWarnings("unchecked")
	default Sequence<T> append(T... items) {
		requireNonNull(items, "items");

		return append(Iterables.of(items));
	}

	/**
	 * Append the elements of the given {@link Iterable} to the end of this {@code Sequence}.
	 *
	 * @see #cache(Iterable)
	 */
	default Sequence<T> append(Iterable<T> iterable) {
		requireNonNull(iterable, "iterable");

		return from(ChainingIterable.concat(this, iterable));
	}

	/**
	 * Append the elements of the given {@link Iterator} to the end of this {@code Sequence}.
	 * <p>
	 * The appended elements will only be available on the first traversal of the resulting {@code Sequence}.
	 *
	 * @see #cache(Iterator)
	 */
	default Sequence<T> append(Iterator<T> iterator) {
		requireNonNull(iterator, "iterator");

		return append(Iterables.once(iterator));
	}

	/**
	 * Append the elements of the given {@link Stream} to the end of this {@code Sequence}.
	 * <p>
	 * The appended elements will only be available on the first traversal of the resulting {@code Sequence}.
	 *
	 * @see #cache(Stream)
	 */
	default Sequence<T> append(Stream<T> stream) {
		requireNonNull(stream, "stream");

		return append(stream.iterator());
	}

	/**
	 * Filter the elements in this {@code Sequence}, keeping only the elements that match the given {@link Predicate}.
	 * <p>
	 * This method does not guarantee the order in which items are tested against the predicate, items may be tested in
	 * arbitrary order rather tha iteration order.
	 */
	default Sequence<T> filter(Predicate<? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new FilteringIterator<>(iterator(), predicate);
	}

	/**
	 * Filter the elements in this {@code Sequence}, keeping only the elements that match the given
	 * {@link ObjIntPredicate}, which is passed the current element and its index in the sequence.
	 *
	 * @since 1.2
	 */
	default Sequence<T> filterIndexed(ObjIntPredicate<? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new IndexedFilteringIterator<>(iterator(), predicate);
	}

	/**
	 * Filter the elements in this {@code Sequence}, keeping only the elements are instances of the given
	 * {@link Class}.
	 * <p>
	 * This method does not guarantee the order in which items are tested against the class, items may be tested in
	 * arbitrary order rather tha iteration order.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default <U> Sequence<U> filter(Class<U> targetClass) {
		requireNonNull(targetClass, "targetClass");

		return (Sequence<U>) filter(targetClass::isInstance);
	}

	/**
	 * Filter the elements in this {@code Sequence} while peeking at the previous element in the iteration, keeping
	 * only the elements that match the given {@link BiPredicate}.
	 * <p>
	 * The predicate has access to the previous element and the next element in the iteration. {@code null} is provided
	 * as a prefix to the sequence for first value in the sequence.
	 *
	 * @see #filterBack(Object, BiPredicate)
	 */
	default Sequence<T> filterBack(BiPredicate<? super T, ? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return filterBack(null, predicate);
	}

	/**
	 * Filter the elements in this {@code Sequence} while peeking at the previous element in the iteration, keeping
	 * only the elements that match the given {@link BiPredicate}.
	 * <p>
	 * The predicate has access to the previous element and the next element in the iteration. The given replacement
	 * value is provided as a prefix to the sequence for the first value in the sequence.
	 *
	 * @see #filterBack(BiPredicate)
	 */
	default Sequence<T> filterBack(T replacement, BiPredicate<? super T, ? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new BackPeekingFilteringIterator<>(iterator(), replacement, predicate);
	}

	/**
	 * Filter the elements in this {@code Sequence} while peeking at the next element in the iteration, keeping
	 * only the elements that match the given {@link BiPredicate}.
	 * <p>
	 * The predicate has access to the current element and the next element in the iteration. {@code null} is provided
	 * as a suffix to the sequence for the last value in the sequence.
	 *
	 * @see #filterForward(Object, BiPredicate)
	 */
	default Sequence<T> filterForward(BiPredicate<? super T, ? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return filterForward(null, predicate);
	}

	/**
	 * Filter the elements in this {@code Sequence} while peeking at the next element in the iteration, keeping
	 * only the elements that match the given {@link BiPredicate}.
	 * <p>
	 * The predicate has access to the current element and the next element in the iteration. The given replacement
	 * value is provided as a suffix to the sequence for the last value in the sequence.
	 *
	 * @see #filterForward(BiPredicate)
	 */
	default Sequence<T> filterForward(T replacement, BiPredicate<? super T, ? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new ForwardPeekingFilteringIterator<>(iterator(), replacement, predicate);
	}

	/**
	 * @return a {@code Sequence} containing only the items found in the given target array.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default Sequence<T> including(T... items) {
		requireNonNull(items, "items");

		return filter(e -> Arrayz.contains(items, e));
	}

	/**
	 * @return a {@code Sequence} containing only the items found in the given target iterable.
	 *
	 * @since 1.2
	 */
	default Sequence<T> including(Iterable<? extends T> items) {
		requireNonNull(items, "items");

		return filter(e -> Iterables.contains(items, e));
	}

	/**
	 * @return a {@code Sequence} containing only the items not found in the given target array.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default Sequence<T> excluding(T... items) {
		requireNonNull(items, "items");

		return filter(e -> !Arrayz.contains(items, e));
	}

	/**
	 * @return a {@code Sequence} containing only the items not found in the given target iterable.
	 *
	 * @since 1.2
	 */
	default Sequence<T> excluding(Iterable<? extends T> items) {
		requireNonNull(items, "items");

		return filter(e -> !Iterables.contains(items, e));
	}

	/**
	 * Flatten the elements in this {@code Sequence} according to the given mapper {@link Function}. The resulting
	 * {@code Sequence} contains the elements that is the result of applying the mapper {@link Function} to each
	 * element, appended together inline as a single {@code Sequence}.
	 *
	 * @see #flatten()
	 * @see #map(Function)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 */
	// TODO: Add flattenIterator, flattenArray, etc
	default <U> Sequence<U> flatten(Function<? super T, ? extends Iterable<U>> mapper) {
		requireNonNull(mapper, "mapper");

		return from(ChainingIterable.flatten(this, mapper));
	}

	/**
	 * Flatten the elements in this {@code Sequence}. The resulting {@code Sequence} contains the elements that is the
	 * result of flattening each element, inline. Allowed elements that can be flattened are {@link Iterator},
	 * {@link Iterable}, {@code object array}, {@link Pair}, {@link Entry} and {@link Stream}. Elements of another type
	 * will result in a {@link ClassCastException}.
	 *
	 * @throws ClassCastException if a non-collection element is encountered in the {@code Sequence}.
	 * @see #flatten(Function)
	 * @see #map(Function)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 */
	default <U> Sequence<U> flatten() {
		return from(ChainingIterable.flatten(this, Iterables::from));
	}

	/**
	 * Collect the elements in this {@code Sequence} into an {@code Object}-array.
	 */
	default Object[] toArray() {
		return toArray(Object[]::new);
	}

	/**
	 * Collect the elements in this {@code Sequence} into an array of the type determined by the given array
	 * constructor.
	 */
	default <A> A[] toArray(IntFunction<A[]> constructor) {
		requireNonNull(constructor, "constructor");

		int size = sizeIfKnown();
		if (size == -1)
			return Iterators.toArray(iterator(), constructor);

		return Iterators.toArray(iterator(), constructor, size);
	}

	/**
	 * Collect the elements in this {@code Sequence} into a {@link List}.
	 */
	default List<T> toList() {
		if (!sizeType().known())
			return toList(ArrayList::new);

		return new ArrayList<>(this);
	}

	/**
	 * Collect the elements in this {@code Sequence} into a {@link List} of the type determined by the given
	 * constructor.
	 */
	default List<T> toList(Supplier<? extends List<T>> constructor) {
		requireNonNull(constructor, "constructor");

		return toCollection(constructor);
	}

	/**
	 * Collect the elements in this {@code Sequence} into a {@link Set}.
	 */
	default Set<T> toSet() {
		if (!sizeType().known())
			return toSet(HashSet::new);

		return new HashSet<>(this);
	}

	/**
	 * Collect the elements in this {@code Sequence} into a {@link Set} of the type determined by the given
	 * constructor.
	 */
	default <S extends Set<T>> S toSet(Supplier<? extends S> constructor) {
		requireNonNull(constructor, "constructor");

		return toCollection(constructor);
	}

	/**
	 * Collect the elements in this {@code Sequence} into a {@link SortedSet}.
	 */
	default SortedSet<T> toSortedSet() {
		if (!sizeType().known())
			return toSet(TreeSet::new);

		return new TreeSet<>(this);
	}

	/**
	 * Collect the elements in this {@code Sequence} into a {@link SortedSet} ordered by the given {@code comparator},
	 * which may be null to indicate natural ordering.
	 */
	default SortedSet<T> toSortedSet(Comparator<? super T> comparator) {
		return collectInto(new TreeSet<>(comparator));
	}

	/**
	 * Convert this {@code Sequence} of {@link Map.Entry} values into a {@link Map}.
	 *
	 * @throws ClassCastException if this {@code Sequence} is not of {@link Map.Entry}.
	 */
	default <K, V> Map<K, V> toMap() {
		return toMap(HashMap::new);
	}

	/**
	 * Convert this {@code Sequence} of {@link Map.Entry} values into a {@link Map} of the type determined by the given
	 * constructor.
	 *
	 * @throws ClassCastException if this {@code Sequence} is not of {@link Map.Entry}.
	 */
	default <M extends Map<K, V>, K, V> M toMap(Supplier<? extends M> constructor) {
		requireNonNull(constructor, "constructor");

		@SuppressWarnings("unchecked")
		Sequence<Entry<K, V>> entries = (Sequence<Entry<K, V>>) this;

		M result = constructor.get();
		for (Entry<K, V> t : entries)
			result.put(t.getKey(), t.getValue());

		return result;
	}

	/**
	 * Convert this {@code Sequence} of into a {@link Map}, using the given key mapper {@link Function} and value
	 * mapper {@link Function} to convert each element into a {@link Map} entry. If the same key occurs more than once,
	 * the key is remapped in the resulting map to the latter corresponding value.
	 */
	default <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper,
	                               Function<? super T, ? extends V> valueMapper) {
		requireNonNull(keyMapper, "keyMapper");
		requireNonNull(valueMapper, "valueMapper");

		return toMap(HashMap::new, keyMapper, valueMapper);
	}

	/**
	 * Convert this {@code Sequence} of into a {@link Map} of the type determined by the given constructor, using the
	 * given key mapper {@link Function} and value mapper {@link Function} to convert each element into a {@link Map}
	 * entry. If the same key occurs more than once, the key is remapped in the resulting map to the latter
	 * corresponding value.
	 */
	default <M extends Map<K, V>, K, V> M toMap(Supplier<? extends M> constructor,
	                                            Function<? super T, ? extends K> keyMapper,
	                                            Function<? super T, ? extends V> valueMapper) {
		requireNonNull(constructor, "constructor");
		requireNonNull(keyMapper, "keyMapper");
		requireNonNull(valueMapper, "valueMapper");

		return collect(constructor,
		               (result, element) -> result.put(keyMapper.apply(element), valueMapper.apply(element)));
	}

	/**
	 * Convert this {@code Sequence} of into a {@link Map}, using the given key mapper {@link Function} and value
	 * mapper {@link Function} to convert each element into a {@link Map} entry, and using the given {@code merger}
	 * {@link BiFunction} to merge values in the map, according to {@link Map#merge(Object, Object, BiFunction)}.
	 */
	default <K, V> Map<K, V> toMergedMap(Function<? super T, ? extends K> keyMapper,
	                                     Function<? super T, ? extends V> valueMapper,
	                                     BiFunction<? super V, ? super V, ? extends V> merger) {
		requireNonNull(keyMapper, "keyMapper");
		requireNonNull(valueMapper, "valueMapper");
		requireNonNull(merger, "merger");

		return toMergedMap(HashMap::new, keyMapper, valueMapper, merger);
	}

	/**
	 * Convert this {@code Sequence} of into a {@link Map} of the type determined by the given constructor, using the
	 * given key mapper {@link Function} and value mapper {@link Function} to convert each element into a {@link Map}
	 * entry, and using the given {@code merger} {@link BiFunction} to merge values in the map, according to
	 * {@link Map#merge(Object, Object, BiFunction)}.
	 */
	default <M extends Map<K, V>, K, V> M toMergedMap(Supplier<? extends M> constructor,
	                                                  Function<? super T, ? extends K> keyMapper,
	                                                  Function<? super T, ? extends V> valueMapper,
	                                                  BiFunction<? super V, ? super V, ? extends V> merger) {
		requireNonNull(constructor, "constructor");
		requireNonNull(keyMapper, "keyMapper");
		requireNonNull(valueMapper, "valueMapper");
		requireNonNull(merger, "merger");

		return collect(constructor,
		               (result, element) -> result.merge(keyMapper.apply(element), valueMapper.apply(element),
		                                                 merger));
	}

	/**
	 * Performs a "group by" operation on the elements in this sequence, grouping elements according to a
	 * classification function and returning the results in a {@link Map}.
	 *
	 * @since 2.3
	 */
	default <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
		return groupBy(classifier, HashMap::new);
	}

	/**
	 * Performs a "group by" operation on the elements in this sequence, grouping elements according to a
	 * classification function and returning the results in a {@link Map} whose type is determined by the given {@code
	 * constructor}.
	 *
	 * @since 2.3
	 */
	default <M extends Map<K, List<T>>, K> M groupBy(Function<? super T, ? extends K> classifier,
	                                                 Supplier<? extends M> constructor) {
		return groupBy(classifier, constructor, ArrayList::new);
	}

	/**
	 * Performs a "group by" operation on the elements in this sequence, grouping elements according to a
	 * classification function and returning the results in a {@link Map} whose type is determined by the given {@code
	 * constructor}, using the given {@code groupConstructor} to create the target {@link Collection} of the grouped
	 * values.
	 *
	 * @since 2.3
	 */
	default <M extends Map<K, C>, C extends Collection<T>, K> M groupBy(Function<? super T, ? extends K> classifier,
	                                                                    Supplier<? extends M> mapConstructor,
	                                                                    Supplier<C> groupConstructor) {
		return groupBy(classifier, mapConstructor, Collectors.toCollection(groupConstructor));
	}

	/**
	 * Performs a "group by" operation on the elements in this sequence, grouping elements according to a
	 * classification function and returning the results in a {@link Map} whose type is determined by the given {@code
	 * constructor}, using the given group {@link Collector} to collect the grouped values.
	 *
	 * @since 2.3
	 */
	default <M extends Map<K, C>, C, K, A> M groupBy(Function<? super T, ? extends K> classifier,
	                                                 Supplier<? extends M> mapConstructor,
	                                                 Collector<? super T, A, C> groupCollector) {
		Supplier<? extends A> groupConstructor = groupCollector.supplier();
		BiConsumer<? super A, ? super T> groupAccumulator = groupCollector.accumulator();

		@SuppressWarnings("unchecked")
		Map<K, A> result = (Map<K, A>) mapConstructor.get();
		for (T t : this)
			groupAccumulator.accept(result.computeIfAbsent(classifier.apply(t), k -> groupConstructor.get()), t);

		if (!groupCollector.characteristics().contains(IDENTITY_FINISH)) {
			@SuppressWarnings("unchecked")
			Function<? super A, ? extends A> groupFinisher = (Function<? super A, ? extends A>) groupCollector
					.finisher();
			result.replaceAll((k, v) -> groupFinisher.apply(v));
		}

		@SuppressWarnings("unchecked")
		M castResult = (M) result;

		return castResult;
	}

	/**
	 * Convert this {@code Sequence} of {@link Map.Entry} into a {@link SortedMap}.
	 *
	 * @throws ClassCastException if this {@code Sequence} is not of {@link Map.Entry}.
	 */
	default <K, V> SortedMap<K, V> toSortedMap() {
		return toMap(TreeMap::new);
	}

	/**
	 * Convert this {@code Sequence} into a {@link SortedMap}, using the given key mapper {@link Function} and value
	 * mapper {@link Function} to convert each element into a {@link SortedMap} entry.
	 */
	default <K, V> SortedMap<K, V> toSortedMap(Function<? super T, ? extends K> keyMapper,
	                                           Function<? super T, ? extends V> valueMapper) {
		requireNonNull(keyMapper, "keyMapper");
		requireNonNull(valueMapper, "valueMapper");

		return toMap(TreeMap::new, keyMapper, valueMapper);
	}

	/**
	 * Convert this {@code Sequence} of {@link Map.Entry} into a {@link SortedMap} ordered by the given comparator.
	 *
	 * @throws ClassCastException if this {@code Sequence} is not of {@link Map.Entry}.
	 */
	default <K, V> SortedMap<K, V> toSortedMap(Comparator<? super K> comparator) {
		return toMap(() -> new TreeMap<K, V>(comparator));
	}

	/**
	 * Convert this {@code Sequence} into a {@link SortedMap} ordered by the given comparator, using the given key
	 * mapper {@link Function} and value mapper {@link Function} to convert each element into a {@link SortedMap}
	 * entry.
	 */
	default <K, V> SortedMap<K, V> toSortedMap(Comparator<? super K> comparator,
	                                           Function<? super T, ? extends K> keyMapper,
	                                           Function<? super T, ? extends V> valueMapper) {
		requireNonNull(keyMapper, "keyMapper");
		requireNonNull(valueMapper, "valueMapper");

		return toMap(() -> new TreeMap<K, V>(comparator), keyMapper, valueMapper);
	}

	/**
	 * Collect this {@code Sequence} into a {@link Collection} instance created by the given constructor.
	 */
	default <U extends Collection<T>> U toCollection(Supplier<? extends U> constructor) {
		requireNonNull(constructor, "constructor");

		return collectInto(requireNonNull(constructor.get(), "constructor.get()"));
	}

	/**
	 * Collect this {@code Sequence} into an arbitrary container using the given {@link Collector}.
	 */
	default <R, A> R collect(Collector<T, A, R> collector) {
		requireNonNull(collector, "collector");

		A container = collect(collector.supplier(), collector.accumulator());
		return collector.finisher().apply(container);
	}

	/**
	 * Collect this {@code Sequence} into an arbitrary container using the given constructor and adder.
	 */
	default <C> C collect(Supplier<? extends C> constructor, BiConsumer<? super C, ? super T> adder) {
		requireNonNull(constructor, "constructor");
		requireNonNull(adder, "adder");

		return collectInto(constructor.get(), adder);
	}

	/**
	 * Collect this {@code Sequence} into the given {@link Collection}.
	 */
	default <U extends Collection<T>> U collectInto(U collection) {
		requireNonNull(collection, "collection");

		requireFinite(this, "Infinite Sequence");

		if (sizeType().known())
			collection.addAll(this);
		else
			for (T t : this)
				//noinspection UseBulkOperation
				collection.add(t);

		return collection;
	}

	/**
	 * Collect this {@code Sequence} into the given container, using the given adder.
	 */
	default <C> C collectInto(C result, BiConsumer<? super C, ? super T> adder) {
		requireNonNull(result, "result");
		requireNonNull(adder, "adder");

		requireFinite(this, "Infinite Sequence");

		for (T t : this)
			adder.accept(result, t);

		return result;
	}

	/**
	 * @return a {@link List} view of this {@code Sequence}, which is updated in real time as the backing store of the
	 * {@code Sequence} changes. The list does not implement {@link RandomAccess} and is best accessed in sequence. The
	 * list supports {@link List#add} only if the {@code Sequence} is backed by a list itself, otherwise it supports
	 * removal only.
	 *
	 * @since 1.2
	 */
	default List<T> asList() {
		return Iterables.asList(this);
	}

	/**
	 * Join this {@code Sequence} into a string.
	 *
	 * @since 1.2
	 */
	default String join() {
		return join("");
	}

	/**
	 * Join this {@code Sequence} into a string separated by the given delimiter.
	 */
	default String join(String delimiter) {
		requireNonNull(delimiter, "delimiter");

		return join("", delimiter, "");
	}

	/**
	 * Join this {@code Sequence} into a string separated by the given delimiter, with the given prefix and suffix.
	 */
	default String join(String prefix, String delimiter, String suffix) {
		requireNonNull(prefix, "prefix");
		requireNonNull(delimiter, "delimiter");
		requireNonNull(suffix, "suffix");

		requireFinite(this, "Infinite Sequence");

		StringBuilder result = new StringBuilder();
		result.append(prefix);
		boolean started = false;
		for (T each : this) {
			if (started)
				result.append(delimiter);
			else
				started = true;
			result.append(each);
		}
		result.append(suffix);
		return result.toString();
	}

	/**
	 * Reduce this {@code Sequence} into a single element by iteratively applying the given binary operator to
	 * the current result and each element in this sequence.
	 */
	default Optional<T> reduce(BinaryOperator<T> operator) {
		requireNonNull(operator, "operator");

		requireFinite(this, "Infinite Sequence");

		return Iterators.reduce(iterator(), operator);
	}

	/**
	 * Reduce this {@code Sequence} into a single element by iteratively applying the given binary operator to
	 * the current result and each element in this sequence, starting with the given identity as the initial result.
	 */
	default T reduce(T identity, BinaryOperator<T> operator) {
		requireNonNull(operator, "operator");

		requireFinite(this, "Infinite Sequence");

		return Iterators.reduce(iterator(), identity, operator);
	}

	/**
	 * Fold this {@code Sequence} into a single result by iteratively applying the given binary operator to the current
	 * result and each element in this sequence, starting with the given identity as the initial result.
	 */
	default <U> U fold(U identity, BiFunction<? super U, ? super T, ? extends U> operator) {
		requireNonNull(operator, "operator");

		requireFinite(this, "Infinite Sequence");

		return Iterators.fold(iterator(), identity, operator);
	}

	/**
	 * @return an arbitrary element in this {@code Sequence}, or an empty {@link Optional} if there are no elements in
	 * the {@code Sequence}.
	 *
	 * @since 2.4
	 */
	default Optional<T> arbitrary() {
		return first();
	}

	/**
	 * @return the first element of this {@code Sequence}, or an empty {@link Optional} if there are no elements in the
	 * {@code Sequence}.
	 */
	default Optional<T> first() {
		requireAtLeastZero(0, "index");

		return Iterators.first(iterator());
	}

	/**
	 * @return the last element of this {@code Sequence}, or an empty {@link Optional} if there are no elements in the
	 * {@code Sequence}.
	 */
	default Optional<T> last() {
		requireFinite(this, "Infinite Sequence");

		return Iterators.last(iterator());
	}

	/**
	 * @return the element at the given index, or an empty {@link Optional} if the {@code Sequence} is smaller than the
	 * index.
	 *
	 * @since 1.2
	 */
	default Optional<T> at(int index) {
		requireAtLeastZero(index, "index");

		if (index == 0)
			return first();

		int size = sizeIfKnown();
		if (size != -1 && index == size - 1)
			return last();

		return Iterators.get(iterator(), index);
	}

	/**
	 * @return an arbitrary element in this {@code Sequence} matching the given {@link Predicate}, or an empty
	 * {@link Optional} if there are no matching elements in the {@code Sequence}.
	 * <p>
	 * This method does not guarantee the order in which items are tested against the predicate, items may be tested in
	 * arbitrary order rather tha iteration order.
	 *
	 * @since 2.4
	 */
	default Optional<T> arbitrary(Predicate<? super T> predicate) {
		return first(predicate);
	}

	/**
	 * @return the first element of this {@code Sequence} that matches the given predicate, or an empty
	 * {@link Optional} if there are no matching elements in the {@code Sequence}.
	 * <p>
	 * This method does not guarantee the order in which items are tested against the predicate, items may be tested in
	 * arbitrary order rather tha iteration order.
	 *
	 * @since 1.2
	 */
	default Optional<T> first(Predicate<? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return filter(predicate).first();
	}

	/**
	 * @return the last element of this {@code Sequence} the matches the given predicate, or an empty {@link Optional}
	 * if there are no matching elements in the {@code Sequence}.
	 * <p>
	 * This method does not guarantee the order in which items are tested against the predicate, items may be tested in
	 * arbitrary order rather tha iteration order.
	 *
	 * @since 1.2
	 */
	default Optional<T> last(Predicate<? super T> predicate) {
		requireNonNull(predicate, "predicate");

		requireFinite(this, "Infinite Sequence");

		return filter(predicate).last();
	}

	/**
	 * @return the element at the given index out of the elements matching the given predicate, or an empty {@link
	 * Optional} if the {@code Sequence} of matching elements is smaller than the index.
	 *
	 * @since 1.2
	 */
	default Optional<T> at(int index, Predicate<? super T> predicate) {
		requireAtLeastZero(index, "index");
		requireNonNull(predicate, "predicate");

		return filter(predicate).at(index);
	}

	/**
	 * @return an arbitrary element in this {@code Sequence} that is an instance of the given {@link Class}, or an
	 * empty {@link Optional} if there are no matching elements in the {@code Sequence}.
	 * <p>
	 * This method does not guarantee the order in which items are tested against the class, items may be tested in
	 * arbitrary order rather tha iteration order.
	 *
	 * @since 2.4
	 */
	default <U> Optional<U> arbitrary(Class<U> targetClass) {
		return first(targetClass);
	}

	/**
	 * @return the first element of this {@code Sequence} that is an instance of the given {@link Class}, or an empty
	 * {@link Optional} if there are no matching elements in the {@code Sequence}.
	 * <p>
	 * This method does not guarantee the order in which items are tested against the class, items may be tested in
	 * arbitrary order rather tha iteration order.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default <U> Optional<U> first(Class<U> targetClass) {
		requireNonNull(targetClass, "targetClass");

		return (Optional<U>) filter(targetClass::isInstance).first();
	}

	/**
	 * @return the last element of this {@code Sequence} that is an instance of the given {@link Class}, or an empty
	 * {@link Optional} if there are no matching elements in the {@code Sequence}.
	 * <p>
	 * This method does not guarantee the order in which items are tested against the class, items may be tested in
	 * arbitrary order rather tha iteration order.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default <U> Optional<U> last(Class<U> targetClass) {
		requireNonNull(targetClass, "targetClass");

		requireFinite(this, "Infinite Sequence");

		return (Optional<U>) filter(targetClass::isInstance).last();
	}

	/**
	 * @return the element at the given index out of the elements that are instances of the given {@link Class}, or an
	 * empty {@link Optional} if the {@code Sequence} of matching elements is smaller than the index.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default <U> Optional<U> at(int index, Class<? extends U> targetClass) {
		requireAtLeastZero(index, "index");
		requireNonNull(targetClass, "targetClass");

		return (Optional<U>) filter(targetClass::isInstance).at(index);
	}

	/**
	 * @return an arbitrary element in this {@code Sequence}, or an empty {@link Optional} if there are no elements in
	 * the {@code Sequence}.
	 *
	 * @since 2.4
	 */
	default Optional<T> removeArbitrary() {
		return removeFirst();
	}

	/**
	 * @return the first element of this {@code Sequence}, or an empty {@link Optional} if there are no elements in the
	 * {@code Sequence}.
	 */
	default Optional<T> removeFirst() {
		return Iterators.removeFirst(iterator());
	}

	/**
	 * @return the last element of this {@code Sequence}, or an empty {@link Optional} if there are no elements in the
	 * {@code Sequence}.
	 */
	default Optional<T> removeLast() {
		requireFinite(this, "Infinite Sequence");

		return Iterators.removeLast(iterator());
	}

	/**
	 * @return the element at the given index, or an empty {@link Optional} if the {@code Sequence} is smaller than the
	 * index.
	 *
	 * @since 1.2
	 */
	default Optional<T> removeAt(int index) {
		requireAtLeastZero(index, "index");

		return Iterators.removeAt(iterator(), index);
	}

	/**
	 * @return an arbitrary element in this {@code Sequence} matching the given {@link Predicate}, or an empty
	 * {@link Optional} if there are no matching elements in the {@code Sequence}.
	 * <p>
	 * This method does not guarantee the order in which items are tested against the predicate, items may be tested in
	 * arbitrary order rather tha iteration order.
	 *
	 * @since 2.4
	 */
	default Optional<T> removeArbitrary(Predicate<? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return filter(predicate).removeArbitrary();
	}

	/**
	 * @return the first element of this {@code Sequence} that matches the given predicate, or an empty
	 * {@link Optional} if there are no matching elements in the {@code Sequence}.
	 * <p>
	 * This method does not guarantee the order in which items are tested against the predicate, items may be tested in
	 * arbitrary order rather tha iteration order.
	 *
	 * @since 1.2
	 */
	default Optional<T> removeFirst(Predicate<? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return filter(predicate).removeFirst();
	}

	/**
	 * @return the last element of this {@code Sequence} the matches the given predicate, or an empty {@link Optional}
	 * if there are no matching elements in the {@code Sequence}.
	 * <p>
	 * This method does not guarantee the order in which items are tested against the predicate, items may be tested in
	 * arbitrary order rather tha iteration order.
	 *
	 * @since 1.2
	 */
	default Optional<T> removeLast(Predicate<? super T> predicate) {
		requireNonNull(predicate, "predicate");

		requireFinite(this, "Infinite Sequence");

		return filter(predicate).removeLast();
	}

	/**
	 * @return the element at the given index out of the elements matching the given predicate, or an empty {@link
	 * Optional} if the {@code Sequence} of matching elements is smaller than the index.
	 *
	 * @since 1.2
	 */
	default Optional<T> removeAt(int index, Predicate<? super T> predicate) {
		requireAtLeastZero(index, "index");
		requireNonNull(predicate, "predicate");

		return filter(predicate).removeAt(index);
	}

	/**
	 * @return an arbitrary element in this {@code Sequence} that is an instance of the given {@link Class}, or an
	 * empty {@link Optional} if there are no matching elements in the {@code Sequence}.
	 * <p>
	 * This method does not guarantee the order in which items are tested against the class, items may be tested in
	 * arbitrary order rather tha iteration order.
	 *
	 * @since 2.4
	 */
	@SuppressWarnings("unchecked")
	default <U> Optional<U> removeArbitrary(Class<U> targetClass) {
		requireNonNull(targetClass, "targetClass");

		return filter(targetClass).removeArbitrary();
	}

	/**
	 * @return the first element of this {@code Sequence} that is an instance of the given {@link Class}, or an empty
	 * {@link Optional} if there are no matching elements in the {@code Sequence}.
	 * <p>
	 * This method does not guarantee the order in which items are tested against the class, items may be tested in
	 * arbitrary order rather tha iteration order.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default <U> Optional<U> removeFirst(Class<U> targetClass) {
		requireNonNull(targetClass, "targetClass");

		return filter(targetClass).removeFirst();
	}

	/**
	 * @return the last element of this {@code Sequence} that is an instance of the given {@link Class}, or an empty
	 * {@link Optional} if there are no matching elements in the {@code Sequence}.
	 * <p>
	 * This method does not guarantee the order in which items are tested against the class, items may be tested in
	 * arbitrary order rather tha iteration order.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default <U> Optional<U> removeLast(Class<U> targetClass) {
		requireNonNull(targetClass, "targetClass");

		requireFinite(this, "Infinite Sequence");

		return filter(targetClass).removeLast();
	}

	/**
	 * @return the element at the given index out of the elements that are instances of the given {@link Class}, or an
	 * empty {@link Optional} if the {@code Sequence} of matching elements is smaller than the index.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default <U> Optional<U> removeAt(int index, Class<U> targetClass) {
		requireAtLeastZero(index, "index");
		requireNonNull(targetClass, "targetClass");

		return filter(targetClass).removeAt(index);
	}

	/**
	 * Pair the elements of this {@code Sequence} into a sequence of overlapping {@link Entry} elements. Each entry
	 * overlaps the value item with the key item of the next entry. If there is only one item in the sequence, the
	 * first entry returned has that item as a key and null as the value.
	 */
	default Sequence<Entry<T, T>> entries() {
		return new Sequence<Entry<T, T>>() {
			@Override
			public Iterator<Entry<T, T>> iterator() {
				return new PairingIterator<T, Entry<T, T>>(Sequence.this.iterator(), 1) {
					@Override
					protected Entry<T, T> pair(T first, T second) {
						return Maps.entry(first, second);
					}
				};
			}

			@Override
			public int size() {
				int originalSize = Sequence.this.size();
				return originalSize == 0 ? 0 : Math.max(1, originalSize - 1);
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Pair the elements of this {@code Sequence} into a sequence of {@link Pair} elements. Each pair overlaps the
	 * second item with the first item of the next pair. If there is only one item in the list, the first pair returned
	 * has a null as the second item.
	 */
	default Sequence<Pair<T, T>> pairs() {
		return new Sequence<Pair<T, T>>() {
			@Override
			public Iterator<Pair<T, T>> iterator() {
				return new PairingIterator<T, Pair<T, T>>(Sequence.this.iterator(), 1) {
					@Override
					protected Pair<T, T> pair(T first, T second) {
						return Pair.of(first, second);
					}
				};
			}

			@Override
			public int size() {
				int originalSize = Sequence.this.size();
				return originalSize == 0 ? 0 : Math.max(1, originalSize - 1);
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Pair the elements of this {@code Sequence} into a sequence of {@link Entry} elements. Each entry is adjacent to
	 * the next entry. If there is an uneven amount of items in the list, the final entry returned has a null as the
	 * value item.
	 */
	default Sequence<Entry<T, T>> adjacentEntries() {
		return new Sequence<Entry<T, T>>() {
			@Override
			public Iterator<Entry<T, T>> iterator() {
				return new PairingIterator<T, Entry<T, T>>(Sequence.this.iterator(), 2) {
					@Override
					protected Entry<T, T> pair(T first, T second) {
						return Maps.entry(first, second);
					}
				};
			}

			@Override
			public int size() {
				return (Sequence.this.size() + 1) / 2;
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Pair the elements of this {@code Sequence} into a sequence of {@link Pair} elements. Each pair is adjacent to
	 * the next pair. If there is an uneven amount of items in the list, the final pair returned has a null as the
	 * second item.
	 */
	default Sequence<Pair<T, T>> adjacentPairs() {
		return new Sequence<Pair<T, T>>() {
			@Override
			public Iterator<Pair<T, T>> iterator() {
				return new PairingIterator<T, Pair<T, T>>(Sequence.this.iterator(), 2) {
					@Override
					protected Pair<T, T> pair(T first, T second) {
						return Pair.of(first, second);
					}
				};
			}

			@Override
			public int size() {
				return (Sequence.this.size() + 1) / 2;
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Converts a {@code Sequence} of {@link Pair}s of items into a {@link BiSequence}. Note the sequence must be of
	 * {@link Pair} or a {@link ClassCastException} will occur when traversal is attempted.
	 */
	@SuppressWarnings("unchecked")
	default <L, R> BiSequence<L, R> toBiSequence() {
		Sequence<Pair<L, R>> pairSequence = (Sequence<Pair<L, R>>) this;
		return new BiSequence<L, R>() {
			@Override
			public Iterator<Pair<L, R>> iterator() {
				return pairSequence.iterator();
			}

			@Override
			public int size() {
				return pairSequence.size();
			}

			@Override
			public SizeType sizeType() {
				return pairSequence.sizeType();
			}

			@Override
			public boolean isEmpty() {
				return pairSequence.isEmpty();
			}
		};
	}

	/**
	 * Converts a {@code Sequence} of {@link Map.Entry} items into an {@link EntrySequence}. Note the sequence must be
	 * of {@link Map.Entry} or a {@link ClassCastException} will occur when traversal is attempted.
	 */
	@SuppressWarnings("unchecked")
	default <K, V> EntrySequence<K, V> toEntrySequence() {
		Sequence<Entry<K, V>> entrySequence = (Sequence<Entry<K, V>>) this;
		return new EntrySequence<K, V>() {
			@Override
			public Iterator<Entry<K, V>> iterator() {
				return entrySequence.iterator();
			}

			@Override
			public int size() {
				return entrySequence.size();
			}

			@Override
			public SizeType sizeType() {
				return entrySequence.sizeType();
			}

			@Override
			public boolean isEmpty() {
				return entrySequence.isEmpty();
			}
		};
	}

	/**
	 * Window the elements of this {@code Sequence} into a sequence of {@code Sequence}s of elements, each with the
	 * size of the given window. The first item in each sequence is the second item in the previous sequence. The final
	 * sequence may be shorter than the window. This method is equivalent to {@code window(window, 1)}.
	 */
	default Sequence<Sequence<T>> window(int window) {
		requireAtLeastOne(window, "window");

		return window(window, 1);
	}

	/**
	 * Window the elements of this {@code Sequence} into a sequence of {@code Sequence}s of elements, each with the
	 * size of the given window, stepping {@code step} elements between each window. If the given step is less than the
	 * window size, the windows will overlap each other. If the step is larger than the window size, elements will be
	 * skipped in between windows.
	 */
	default Sequence<Sequence<T>> window(int window, int step) {
		requireAtLeastOne(window, "window");
		requireAtLeastOne(step, "step");

		return new Sequence<Sequence<T>>() {
			@Override
			public Iterator<Sequence<T>> iterator() {
				return new WindowingIterator<T, Sequence<T>>(Sequence.this.iterator(), window, step) {
					@Override
					protected Sequence<T> toSequence(List<T> list) {
						return ListSequence.from(list);
					}
				};
			}

			@Override
			public int size() {
				if (step == 1) {
					int originalSize = Sequence.this.size();
					return originalSize == 0 ? 0 : Math.max(1, originalSize - window + 1);
				} else if (step == window) {
					return (Sequence.this.size() + step - 1) / step;
				} else {
					// TODO: Add size pass-through
					return SizedIterable.size(this);
				}
			}

			@Override
			public SizeType sizeType() {
				if (step == 1 || step == window)
					return Sequence.this.sizeType();
				else
					return UNAVAILABLE;
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Batch the elements of this {@code Sequence} into a sequence of {@code Sequence}s of distinct elements, each with
	 * the given batch size. This method is equivalent to {@code window(size, size)}.
	 */
	default Sequence<Sequence<T>> batch(int size) {
		requireAtLeastOne(size, "size");

		return window(size, size);
	}

	/**
	 * Batch the elements of this {@code Sequence} into a sequence of {@code Sequence}s of distinct elements, where the
	 * given predicate determines where to split the lists of partitioned elements. The predicate is given the current
	 * and next item in the iteration, and if it returns true a partition is created between the elements.
	 */
	default Sequence<Sequence<T>> batch(BiPredicate<? super T, ? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return new Sequence<Sequence<T>>() {
			@Override
			public Iterator<Sequence<T>> iterator() {
				return new PredicatePartitioningIterator<T, Sequence<T>>(Sequence.this.iterator(), predicate) {
					@Override
					protected Sequence<T> toSequence(List<T> list) {
						return ListSequence.from(list);
					}
				};
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Split the elements of this {@code Sequence} into a sequence of {@code Sequence}s of distinct elements, around
	 * the given element. The elements around which the sequence is split are not included in the result.
	 *
	 * @since 1.1
	 */
	default Sequence<Sequence<T>> split(T element) {
		return new Sequence<Sequence<T>>() {
			@Override
			public Iterator<Sequence<T>> iterator() {
				return new SplittingIterator<T, Sequence<T>>(Sequence.this.iterator(), element) {
					@Override
					protected Sequence<T> toSequence(List<T> list) {
						return ListSequence.from(list);
					}
				};
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Split the elements of this {@code Sequence} into a sequence of {@code Sequence}s of distinct elements, where the
	 * given predicate determines which elements to split the partitioned elements around. The elements matching the
	 * predicate are not included in the result.
	 *
	 * @since 1.1
	 */
	default Sequence<Sequence<T>> split(Predicate<? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return new Sequence<Sequence<T>>() {
			@Override
			public Iterator<Sequence<T>> iterator() {
				return new SplittingIterator<T, Sequence<T>>(Sequence.this.iterator(), predicate) {
					@Override
					protected Sequence<T> toSequence(List<T> list) {
						return ListSequence.from(list);
					}
				};
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Skip x number of steps in between each invocation of the iterator of this {@code Sequence}.
	 */
	default Sequence<T> step(int step) {
		requireAtLeastOne(step, "step");

		return new Sequence<T>() {
			@Override
			public Iterator<T> iterator() {
				return new SteppingIterator<>(Sequence.this.iterator(), step);
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public int size() {
				return (Sequence.this.size() + step - 1) / step;
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * @return a {@code Sequence} where each item in this {@code Sequence} occurs only once, the first time it is
	 * encountered.
	 */
	default Sequence<T> distinct() {
		return new Sequence<T>() {
			@Override
			public Iterator<T> iterator() {
				return new DistinctIterator<>(Sequence.this.iterator());
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * @return this {@code Sequence} sorted according to the natural order. Must be a (@code Sequence} of {@link
	 * Comparable} or a {@link ClassCastException} is thrown during traversal.
	 */
	@SuppressWarnings("unchecked")
	default Sequence<T> sorted() {
		return sorted(null);
	}

	/**
	 * @return this {@code Sequence} sorted according to the given {@link Comparator}, or natural order if the
	 * comparator is {@code null}.
	 */
	default Sequence<T> sorted(Comparator<? super T> comparator) {
		return new SortedSequence<>(this, comparator);
	}

	/**
	 * @return the minimal element in this {@code Sequence} according to their natural order. Elements in the sequence
	 * must all implement {@link Comparable} or a {@link ClassCastException} will be thrown at traversal.
	 * <p>
	 * If more than one element compare as the minimum according to the natural comparator, an arbitrary choice will
	 * be returned out of the possible minimums.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default Optional<T> min() {
		return min((Comparator) naturalOrder());
	}

	/**
	 * @return the maximum element in this {@code Sequence} according to their natural order. Elements in the sequence
	 * must all implement {@link Comparable} or a {@link ClassCastException} will be thrown at traversal.
	 * <p>
	 * If more than one element compare as the maximum according to the natural comparator, an arbitrary choice will
	 * be returned out of the possible maximums.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default Optional<T> max() {
		return max((Comparator) naturalOrder());
	}

	/**
	 * @return the minimal element in this {@code Sequence} according to the given {@link Comparator}.
	 * <p>
	 * If more than one element compare as the minimum according to the given {@code comparator}, an arbitrary choice
	 * will be returned out of the possible minimums.
	 */
	default Optional<T> min(Comparator<? super T> comparator) {
		requireNonNull(comparator, "comparator");

		return reduce(firstMinBy(comparator));
	}

	/**
	 * @return the maximum element in this {@code Sequence} according to the given {@link Comparator}.
	 * <p>
	 * If more than one element compare as the maximum according to the given {@code comparator}, an arbitrary choice
	 * will be returned out of the possible maximums.
	 */
	default Optional<T> max(Comparator<? super T> comparator) {
		requireNonNull(comparator, "comparator");

		return reduce(firstMaxBy(comparator));
	}

	/**
	 * @return true if all elements in this {@code Sequence} satisfy the given predicate, false otherwise.
	 */
	default boolean all(Predicate<? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return Iterables.all(this, predicate);
	}

	/**
	 * @return true if no elements in this {@code Sequence} satisfy the given predicate, false otherwise.
	 */
	default boolean none(Predicate<? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return Iterables.none(this, predicate);
	}

	/**
	 * @return true if any element in this {@code Sequence} satisfies the given predicate, false otherwise.
	 */
	default boolean any(Predicate<? super T> predicate) {
		requireNonNull(predicate, "predicate");

		return Iterables.any(this, predicate);
	}

	/**
	 * @return true if all elements in this {@code Sequence} are instances of the given {@link Class}, false otherwise.
	 *
	 * @since 1.2
	 */
	default boolean all(Class<?> target) {
		requireNonNull(target, "target");

		return all(target::isInstance);
	}

	/**
	 * @return true if no elements in this {@code Sequence} are instances of the given {@link Class}, false otherwise.
	 *
	 * @since 1.2
	 */
	default boolean none(Class<?> targetClass) {
		requireNonNull(targetClass, "targetClass");

		return none(targetClass::isInstance);
	}

	/**
	 * @return true if any element in this {@code Sequence} is an instance of the given {@link Class}, false otherwise.
	 *
	 * @since 1.2
	 */
	default boolean any(Class<?> target) {
		requireNonNull(target, "target");

		return any(target::isInstance);
	}

	/**
	 * Allow the given {@link Consumer} to see each element in this {@code Sequence} as it is traversed.
	 */
	default Sequence<T> peek(Consumer<? super T> action) {
		requireNonNull(action, "action");

		return new EquivalentSizeSequence<>(this, it -> new PeekingIterator<>(it, action));
	}

	/**
	 * Allow the given {@link ObjIntConsumer} to see each element with its index as this {@code Sequence} is
	 * traversed.
	 *
	 * @since 1.2.2
	 */
	default Sequence<T> peekIndexed(ObjIntConsumer<? super T> action) {
		requireNonNull(action, "action");

		return new EquivalentSizeSequence<>(this, it -> new IndexPeekingIterator<>(it, action));
	}

	/**
	 * Allow the given {@link BiConsumer} to see each and its following element in this {@code Sequence} as it is
	 * traversed. In the last iteration, the following item will be null.
	 *
	 * @see #peekForward(Object, BiConsumer)
	 */
	default Sequence<T> peekForward(BiConsumer<? super T, ? super T> action) {
		requireNonNull(action, "action");

		return peekForward(null, action);
	}

	/**
	 * Allow the given {@link BiConsumer} to see each and its previous element in this {@code Sequence} as it is
	 * traversed. In the first iteration, the previous item will be null.
	 *
	 * @see #peekBack(Object, BiConsumer)
	 */
	default Sequence<T> peekBack(BiConsumer<? super T, ? super T> action) {
		requireNonNull(action, "action");

		return peekBack(null, action);
	}

	/**
	 * Allow the given {@link BiConsumer} to see each and its following element in this {@code Sequence} as it is
	 * traversed. In the last iteration, the following item will have the given replacement value.
	 *
	 * @see #peekForward(BiConsumer)
	 */
	default Sequence<T> peekForward(T replacement, BiConsumer<? super T, ? super T> action) {
		requireNonNull(action, "action");

		return new EquivalentSizeSequence<>(this, it -> new ForwardPeekingMappingIterator<T, T>(it, replacement) {
			@Override
			protected T mapNext(T next, T following) {
				action.accept(next, following);
				return next;
			}

			@Override
			protected T mapFollowing(boolean hasFollowing, T following) {
				return following;
			}
		});
	}

	/**
	 * Allow the given {@link BiConsumer} to see each and its previous element in this {@code Sequence} as it is
	 * traversed. In the first iteration, the previous item will have the given replacement value.
	 *
	 * @see #peekBack(BiConsumer)
	 */
	default Sequence<T> peekBack(T replacement, BiConsumer<? super T, ? super T> action) {
		requireNonNull(action, "action");

		return new EquivalentSizeSequence<>(this, it -> new BackPeekingMappingIterator<T, T>(it, replacement) {
			@Override
			protected T map(T previous, T next) {
				action.accept(previous, next);
				return next;
			}
		});
	}

	/**
	 * Delimit each element in this {@code Sequence} with the given delimiter element.
	 */
	@SuppressWarnings("unchecked")
	default <U, V> Sequence<U> delimit(V delimiter) {
		return new Sequence<U>() {
			@Override
			public Iterator<U> iterator() {
				return new DelimitingIterator<>((Iterator<U>) Sequence.this.iterator(), Optional.empty(),
				                                Optional.of(delimiter),
				                                Optional.empty());
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public int size() {
				return Math.max(0, Sequence.this.size() * 2 - 1);
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Delimit the elements in this {@code Sequence} with the given delimiter, prefix and suffix elements.
	 */
	@SuppressWarnings("unchecked")
	default <U, V> Sequence<U> delimit(V prefix, V delimiter, V suffix) {
		return new Sequence<U>() {
			@Override
			public Iterator<U> iterator() {
				return new DelimitingIterator<>((Iterator<U>) Sequence.this.iterator(), Optional.of(prefix),
				                                Optional.of(delimiter),
				                                Optional.of(suffix));
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public int size() {
				return Math.max(0, Sequence.this.size() * 2 - 1) + 2;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}
		};
	}

	/**
	 * Prefix the elements in this {@code Sequence} with the given prefix element.
	 */
	@SuppressWarnings("unchecked")
	default <U, V> Sequence<U> prefix(V prefix) {
		return new Sequence<U>() {
			@Override
			public Iterator<U> iterator() {
				return new DelimitingIterator<>((Iterator<U>) Sequence.this.iterator(), Optional.of(prefix),
				                                Optional.empty(),
				                                Optional.empty());
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public int size() {
				return Sequence.this.size() + 1;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}
		};
	}

	/**
	 * Suffix the elements in this {@code Sequence} with the given suffix element.
	 */
	@SuppressWarnings("unchecked")
	default <U, V> Sequence<U> suffix(V suffix) {
		return new Sequence<U>() {
			@Override
			public Iterator<U> iterator() {
				return new DelimitingIterator<>((Iterator<U>) Sequence.this.iterator(), Optional.empty(),
				                                Optional.empty(),
				                                Optional.of(suffix));
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public int size() {
				return Sequence.this.size() + 1;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}
		};
	}

	/**
	 * Interleave the elements in this {@code Sequence} with those of the given {@link Iterable}, inserting {@code
	 * null} values if either sequence finishes before the other. The result is a {@code Sequence} of pairs of items,
	 * the left entry coming from this sequence and the right entry from the given target {@link Iterable}.
	 */
	default <U> Sequence<Pair<T, U>> interleave(Iterable<U> targetIterable) {
		requireNonNull(targetIterable, "targetIterable");

		return new Sequence<Pair<T, U>>() {
			@Override
			public Iterator<Pair<T, U>> iterator() {
				return new InterleavingPairingIterator<>(Sequence.this.iterator(), targetIterable.iterator());
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType().concat(Iterables.sizeType(targetIterable));
			}

			@Override
			public int size() {
				return Math.max(Sequence.this.size(), Iterables.size(targetIterable));
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty() && Iterables.isEmpty(targetIterable);
			}
		};
	}

	/**
	 * Interleave the elements in this {@code Sequence} with those of the given {@link Iterable}, stopping when either
	 * stream of items finishes. The result is a {@code Sequence} of pairs of items, the left entry coming from this
	 * sequence and the right entry from the given target {@link Iterable}.
	 */
	default <U> Sequence<Pair<T, U>> interleaveShort(Iterable<U> targetIterable) {
		requireNonNull(targetIterable, "targetIterable");

		return new Sequence<Pair<T, U>>() {
			@Override
			public Iterator<Pair<T, U>> iterator() {
				return new ShortInterleavingPairingIterator<>(Sequence.this.iterator(), targetIterable.iterator());
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType().intersect(Iterables.sizeType(targetIterable));
			}

			@Override
			public int size() {
				return Math.min(Sequence.this.size(), Iterables.size(targetIterable));
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty() || Iterables.isEmpty(targetIterable);
			}
		};
	}

	/**
	 * @return a {@code Sequence} which iterates over this {@code Sequence} in reverse order.
	 */
	default Sequence<T> reverse() {
		return new ReverseSequence<>(this);
	}

	/**
	 * @return a {@code Sequence} which iterates over this {@code Sequence} in random order. No guarantees are made as
	 * to the predictability of the resulting order of elements other than that it will be as random as allowed by
	 * {@link Random}. If stable random order is required, use {@link #shuffle(Supplier)} with a fixed seed
	 * {@link Random} instance supplied.
	 *
	 * @see #shuffle(Random)
	 * @see #shuffle(Supplier)
	 */
	default Sequence<T> shuffle() {
		return new ShuffledSequence<T>(this);
	}

	/**
	 * @return a {@code Sequence} which iterates over this {@code Sequence} in random order as determined by the given
	 * {@link Random} generator. No guarantees are made as to the predictability of the resulting order of elements
	 * other than that it will be as random as allowed by the given {@link Random} generator. If stable random order is
	 * required, use {@link #shuffle(Supplier)} with a fixed seed {@link Random} instance supplied.
	 *
	 * @see #shuffle()
	 * @see #shuffle(Supplier)
	 */
	default Sequence<T> shuffle(Random random) {
		requireNonNull(random, "random");

		return new ShuffledSequence<T>(this, random);
	}

	/**
	 * @return a {@code Sequence} which iterates over this {@code Sequence} in random order as determined by the given
	 * random generator. A new instance of {@link Random} is created by the given supplier at the start of each
	 * iteration.
	 *
	 * @see #shuffle()
	 * @see #shuffle(Random)
	 *
	 * @since 1.2
	 */
	default Sequence<T> shuffle(Supplier<? extends Random> randomSupplier) {
		requireNonNull(randomSupplier, "randomSupplier");

		return new StableShuffledSequence<T>(this, randomSupplier);
	}

	/**
	 * Convert this {@code Sequence} to a {@link CharSeq} using the given mapper function to map each element to a
	 * {@code char}.
	 *
	 * @see #toInts(ToIntFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @see #flatten()
	 */
	default CharSeq toChars(ToCharFunction<? super T> mapper) {
		requireNonNull(mapper, "mapper");

		return new CharSeq() {
			@Override
			public CharIterator iterator() {
				return CharIterator.from(Sequence.this.iterator(), mapper);
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public int size() {
				return Sequence.this.size();
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Convert this {@code Sequence} to an {@link IntSequence} using the given mapper function to map each element
	 * to an {@code int}.
	 *
	 * @see #toChars(ToCharFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @see #flatten()
	 */
	default IntSequence toInts(ToIntFunction<? super T> mapper) {
		requireNonNull(mapper, "mapper");

		return new IntSequence() {
			@Override
			public IntIterator iterator() {
				return IntIterator.from(Sequence.this.iterator(), mapper);
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public int size() {
				return Sequence.this.size();
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Convert this {@code Sequence} to a {@link LongSequence} using the given mapper function to map each element to a
	 * {@code long}.
	 *
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @see #flatten()
	 */
	default LongSequence toLongs(ToLongFunction<? super T> mapper) {
		requireNonNull(mapper, "mapper");

		return new LongSequence() {
			@Override
			public LongIterator iterator() {
				return LongIterator.from(Sequence.this.iterator(), mapper);
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public int size() {
				return Sequence.this.size();
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Convert this {@code Sequence} to a {@link DoubleSequence} using the given mapper function to map each element
	 * to a {@code double}.
	 *
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @see #flatten()
	 */
	default DoubleSequence toDoubles(ToDoubleFunction<? super T> mapper) {
		requireNonNull(mapper, "mapper");

		return new DoubleSequence() {
			@Override
			public DoubleIterator iterator() {
				return DoubleIterator.from(Sequence.this.iterator(), mapper);
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public int size() {
				return Sequence.this.size();
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Repeat this {@code Sequence} forever, producing a sequence that never terminates unless the original sequence is
	 * empty in which case the resulting sequence is also empty, or the original sequence at some point returns an
	 * empty {@link Iterator} in which case the repeated sequence terminates.
	 */
	default Sequence<T> repeat() {
		return new Sequence<T>() {
			@Override
			public Iterator<T> iterator() {
				return new RepeatingIterator<>(Sequence.this, -1);
			}

			@Override
			public int size() {
				switch (Sequence.this.sizeType()) {
					case FIXED:
						if (Sequence.this.isEmpty())
							return 0;
					default:
						return SizedIterable.size(this);
				}
			}

			@Override
			public SizeType sizeType() {
				switch (Sequence.this.sizeType()) {
					case FIXED:
						if (Sequence.this.isEmpty())
							return FIXED;
					case INFINITE:
						return INFINITE;
					default:
						return UNAVAILABLE;
				}
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Repeat this {@code Sequence} the given number of times.
	 */
	default Sequence<T> repeat(int times) {
		requireAtLeastZero(times, "times");

		if (times == 0)
			return empty();

		return new Sequence<T>() {
			@Override
			public Iterator<T> iterator() {
				return new RepeatingIterator<>(Sequence.this, times);
			}

			@Override
			public int size() {
				switch (Sequence.this.sizeType()) {
					case FIXED:
						return Sequence.this.size() * times;
					default:
						return SizedIterable.size(this);
				}
			}

			@Override
			public SizeType sizeType() {
				switch (Sequence.this.sizeType()) {
					case FIXED:
						return FIXED;
					case INFINITE:
						return INFINITE;
					default:
						return UNAVAILABLE;
				}
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Tests each pair of items in the sequence and swaps any two items which match the given {@code determiner}
	 * {@link Predicate}.
	 */
	default Sequence<T> swap(BiPredicate<? super T, ? super T> determiner) {
		requireNonNull(determiner, "determiner");

		return new EquivalentSizeSequence<>(this, it -> new SwappingIterator<>(it, determiner));
	}

	/**
	 * @return a {@link BiSequence} of this sequence paired up with the index of each element.
	 */
	default BiSequence<Integer, T> index() {
		return new BiSequence<Integer, T>() {
			@Override
			public Iterator<Pair<Integer, T>> iterator() {
				return new DelegatingMappingIterator<T, Pair<Integer, T>>(Sequence.this.iterator()) {
					private int index;

					@Override
					public Pair<Integer, T> next() {
						return Pair.of(index++, iterator.next());
					}
				};
			}

			@Override
			public SizeType sizeType() {
				return Sequence.this.sizeType();
			}

			@Override
			public int size() {
				return Sequence.this.size();
			}

			@Override
			public boolean isEmpty() {
				return Sequence.this.isEmpty();
			}
		};
	}

	/**
	 * Perform the given action for each element in this {@code Sequence}, with the index of each element passed as the
	 * second parameter in the action.
	 *
	 * @since 1.2
	 */
	default void forEachIndexed(ObjIntConsumer<? super T> action) {
		requireNonNull(action, "action");

		int index = 0;
		for (T each : this)
			action.accept(each, index++);
	}
}
