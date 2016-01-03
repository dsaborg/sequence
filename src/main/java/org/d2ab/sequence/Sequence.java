/*
 * Copyright 2015 Daniel Skogquist Åborg
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

import org.d2ab.iterable.Iterables;
import org.d2ab.iterator.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.singleton;

/**
 * An {@link Iterable} sequence of elements with {@link Stream}-like operations for refining, transforming and collating
 * the list of elements.
 */
@FunctionalInterface
public interface Sequence<T> extends Iterable<T> {
	/**
	 * Create an empty {@code Sequence} with no items.
	 */
	@Nonnull
	static <T> Sequence<T> empty() {
		return from(emptyIterator());
	}

	/**
	 * Create a {@code Sequence} from an {@link Iterator} of items. Note that {@code Sequences} created from {@link
	 * Iterator}s cannot be passed over more than once. Further attempts will register the {@code Sequence} as empty.
	 */
	@Nonnull
	static <T> Sequence<T> from(@Nonnull Iterator<T> iterator) {
		return () -> iterator;
	}

	/**
	 * Create a {@code Sequence} with one item.
	 */
	@Nonnull
	static <T> Sequence<T> of(@Nullable T item) {
		return from(singleton(item));
	}

	/**
	 * Create a {@code Sequence} from an {@link Iterable} of items.
	 */
	@Nonnull
	static <T> Sequence<T> from(@Nonnull Iterable<T> iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a {@code Sequence} with the given items.
	 */
	@SafeVarargs
	@Nonnull
	static <T> Sequence<T> of(@Nonnull T... items) {
		return from(asList(items));
	}

	/**
	 * Create a concatenated {@code Sequence} from several {@link Iterable}s which are concatenated together to form
	 * the
	 * stream of items in the {@code Sequence}.
	 */
	@SafeVarargs
	@Nonnull
	static <T> Sequence<T> from(@Nonnull Iterable<T>... iterables) {
		return new ChainingIterable<>(iterables)::iterator;
	}

	/**
	 * Create a {@code Sequence} from {@link Iterator}s of items supplied by the given {@link Supplier}. Every time the
	 * {@code Sequence} is to be iterated over, the {@link Supplier} is used to create the initial stream of elements.
	 * This is similar to creating a {@code Sequence} from an {@link Iterable}.
	 */
	@Nonnull
	static <T> Sequence<T> from(@Nonnull Supplier<? extends Iterator<T>> iteratorSupplier) {
		return iteratorSupplier::get;
	}

	/**
	 * Create a {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created from {@link
	 * Stream}s cannot be passed over more than once. Further attempts will cause an {@link IllegalStateException} when
	 * the {@link Stream} is requested again.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 */
	static <T> Sequence<T> from(Stream<T> stream) {
		return stream::iterator;
	}

	static <K, V> Sequence<Pair<K, V>> from(Map<? extends K, ? extends V> map) {
		return from(map.entrySet()).map(Pair::from);
	}

	@Nonnull
	default <U> Sequence<U> map(@Nonnull Function<? super T, ? extends U> mapper) {
		return () -> new MappingIterator<>(iterator(), mapper);
	}

	/**
	 * A {@code Sequence} of all the positive {@link Integer} numbers starting at {@code 1} and ending at {@link
	 * Integer#MAX_VALUE}.
	 */
	static Sequence<Integer> ints() {
		return range(1, Integer.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Integer} numbers between the given start and end positions, inclusive.
	 */
	static Sequence<Integer> range(int start, int end) {
		UnaryOperator<Integer> next = (end > start) ? i -> i + 1 : i -> i - 1;
		return recurse(start, next).endingAt(end);
	}

	default Sequence<T> endingAt(T terminal) {
		return () -> new InclusiveTerminalIterator<>(iterator(), terminal);
	}

	static <T> Sequence<T> recurse(T seed, UnaryOperator<T> op) {
		return () -> new RecursiveIterator<>(seed, op);
	}

	static <T, S> Sequence<S> recurse(T seed, Function<? super T, ? extends S> f, Function<? super S, ? extends T> g) {
		return () -> new RecursiveIterator<>(f.apply(seed), f.compose(g)::apply);
	}

	/**
	 * A {@code Sequence} of all the {@link Integer} numbers starting at the given start and ending at {@link
	 * Integer#MAX_VALUE}.
	 * <p>
	 * The start value may be negative, in which case the sequence will continue towards positive numbers and
	 * eventually
	 * {@link Integer#MAX_VALUE}.
	 */
	static Sequence<Integer> ints(int start) {
		return range(start, Integer.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the positive {@link Long} numbers starting at {@code 1} and ending at {@link
	 * Long#MAX_VALUE}.
	 */
	static Sequence<Long> longs() {
		return range(1, Long.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Long} numbers between the given start and end positions, inclusive.
	 */
	static Sequence<Long> range(long start, long end) {
		UnaryOperator<Long> next = (end > start) ? i -> i + 1 : i -> i - 1;
		return recurse(start, next).endingAt(end);
	}

	/**
	 * A {@code Sequence} of all the {@link Long} numbers starting at the given value and ending at {@link
	 * Long#MAX_VALUE}.
	 * <p>
	 * The start value may be negative, in which case the sequence will continue towards positive numbers and
	 * eventually
	 * {@link Long#MAX_VALUE}.
	 */
	static Sequence<Long> longs(long start) {
		return range(start, Long.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Character} values starting at {@link Character#MIN_VALUE} and ending at
	 * {@link Character#MAX_VALUE}.
	 */
	static Sequence<Character> chars() {
		return range((char) 0, Character.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Character} values between the given start and end positions, inclusive.
	 */
	static Sequence<Character> range(char start, char end) {
		UnaryOperator<Character> next = (end > start) ? c -> (char) (c + 1) : c -> (char) (c - 1);
		return recurse(start, next).endingAt(end);
	}

	/**
	 * A {@code Sequence} of all the {@link Character} values starting at the given value and ending at {@link
	 * Character#MAX_VALUE}.
	 */
	static Sequence<Character> chars(char start) {
		return range(start, Character.MAX_VALUE);
	}

	@Nonnull
	default Sequence<T> skip(long skip) {
		return () -> new SkippingIterator<>(iterator(), skip);
	}

	@Nonnull
	default Sequence<T> limit(long limit) {
		return () -> new LimitingIterator<>(iterator(), limit);
	}

	default Sequence<T> append(Iterator<T> iterator) {
		return append(Iterables.from(iterator));
	}

	@Nonnull
	default Sequence<T> append(@Nonnull Iterable<T> that) {
		@SuppressWarnings("unchecked")
		Iterable<T> chainingSequence = new ChainingIterable<>(this, that);
		return chainingSequence::iterator;
	}

	@SuppressWarnings("unchecked")
	default Sequence<T> append(T... objects) {
		return append(Iterables.from(objects));
	}

	default Sequence<T> append(Stream<T> stream) {
		return append(Iterables.from(stream));
	}

	@Nonnull
	default Sequence<T> filter(@Nonnull Predicate<? super T> predicate) {
		return () -> new FilteringIterator<>(iterator(), predicate);
	}

	@Nonnull
	default <U> Sequence<U> flatMap(@Nonnull Function<? super T, ? extends Iterable<U>> mapper) {
		return ChainingIterable.flatMap(this, mapper)::iterator;
	}

	default <U> Sequence<U> flatten() {
		return ChainingIterable.<U>flatten(this)::iterator;
	}

	default Sequence<T> untilNull() {
		return () -> new ExclusiveTerminalIterator<>(iterator(), null);
	}

	default Sequence<T> until(T terminal) {
		return () -> new ExclusiveTerminalIterator<>(iterator(), terminal);
	}

	default Set<T> toSet() {
		return toSet(HashSet::new);
	}

	default <S extends Set<T>> S toSet(Supplier<? extends S> constructor) {
		return toCollection(constructor);
	}

	default <U extends Collection<T>> U toCollection(Supplier<? extends U> constructor) {
		return collect(constructor, Collection::add);
	}

	default <C> C collect(Supplier<? extends C> constructor, BiConsumer<? super C, ? super T> adder) {
		C result = constructor.get();
		forEach(each -> adder.accept(result, each));
		return result;
	}

	default SortedSet<T> toSortedSet() {
		return toSet(TreeSet::new);
	}

	/**
	 * Convert this {@code Sequence} of {@code Pairs} into a map, or throw {@code ClassCastException} if this {@code
	 * Sequence} is not of {@code Pair}.
	 */
	default <K, V> Map<K, V> toMap() {
		@SuppressWarnings("unchecked")
		Function<T, Pair<K, V>> mapper = (Function<T, Pair<K, V>>) Function.<Pair<K, V>>identity();
		return toMap(mapper);
	}

	default <K, V> Map<K, V> toMap(Function<? super T, ? extends Pair<K, V>> mapper) {
		Supplier<Map<K, V>> supplier = HashMap::new;
		return toMap(supplier, mapper);
	}

	default <M extends Map<K, V>, K, V> M toMap(Supplier<? extends M> constructor,
	                                            Function<? super T, ? extends Pair<K, V>> mapper) {
		M result = constructor.get();
		forEach(each -> mapper.apply(each).putInto(result));
		return result;
	}

	default <K, V> Map<K, V> toMap(Supplier<Map<K, V>> supplier) {
		@SuppressWarnings("unchecked")
		Function<T, Pair<K, V>> mapper = (Function<T, Pair<K, V>>) Function.<Pair<K, V>>identity();
		return toMap(supplier, mapper);
	}

	default <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper,
	                               Function<? super T, ? extends V> valueMapper) {
		return toMap(HashMap::new, keyMapper, valueMapper);
	}

	default <M extends Map<K, V>, K, V> M toMap(Supplier<? extends M> constructor,
	                                            Function<? super T, ? extends K> keyMapper,
	                                            Function<? super T, ? extends V> valueMapper) {
		M result = constructor.get();
		forEach(each -> {
			K key = keyMapper.apply(each);
			V value = valueMapper.apply(each);
			result.put(key, value);
		});
		return result;
	}

	default <K, V> SortedMap<K, V> toSortedMap(Function<? super T, ? extends K> keyMapper,
	                                           Function<? super T, ? extends V> valueMapper) {
		return toMap(TreeMap::new, keyMapper, valueMapper);
	}

	default <S, R> S collect(Collector<T, R, S> collector) {
		R result = collector.supplier().get();
		BiConsumer<R, T> accumulator = collector.accumulator();
		forEach(each -> accumulator.accept(result, each));
		return collector.finisher().apply(result);
	}

	default String join(String delimiter) {
		return join("", delimiter, "");
	}

	default String join(String prefix, String delimiter, String suffix) {
		StringBuilder result = new StringBuilder();
		result.append(prefix);
		boolean first = true;
		for (T each : this) {
			if (first)
				first = false;
			else
				result.append(delimiter);
			result.append(each);
		}
		result.append(suffix);
		return result.toString();
	}

	default T reduce(T identity, BinaryOperator<T> operator) {
		return reduce(identity, operator, iterator());
	}

	default T reduce(T identity, BinaryOperator<T> operator, Iterator<? extends T> iterator) {
		T result = identity;
		while (iterator.hasNext())
			result = operator.apply(result, iterator.next());
		return result;
	}

	default Optional<T> first() {
		Iterator<T> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	default Optional<T> second() {
		Iterator<T> iterator = iterator();

		Iterators.skipOne(iterator);
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	default Optional<T> third() {
		Iterator<T> iterator = iterator();

		Iterators.skipOne(iterator);
		Iterators.skipOne(iterator);
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	default Optional<T> last() {
		Iterator<T> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		T last;
		do {
			last = iterator.next();
		} while (iterator.hasNext());

		return Optional.of(last);
	}

	default Sequence<Pair<T, T>> pair() {
		return () -> new PairingIterator<>(iterator());
	}

	default Sequence<List<T>> partition(int window) {
		return () -> new PartitioningIterator<>(iterator(), window);
	}

	default Sequence<T> step(long step) {
		return () -> new SteppingIterator<>(iterator(), step);
	}

	default Sequence<T> distinct() {
		return () -> new DistinctIterator<>(iterator());
	}

	default <S extends Comparable<? super S>> Sequence<S> sorted() {
		return () -> {
			@SuppressWarnings("unchecked")
			Iterator<S> comparableIterator = (Iterator<S>) iterator();
			return new SortingIterator<>(comparableIterator);
		};
	}

	default Sequence<T> sorted(Comparator<? super T> comparator) {
		return () -> new SortingIterator<>(iterator(), comparator);
	}

	default Optional<T> min(Comparator<? super T> comparator) {
		return reduce(BinaryOperator.minBy(comparator));
	}

	default Optional<T> reduce(BinaryOperator<T> operator) {
		Iterator<T> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		T result = reduce(iterator.next(), operator, iterator);
		return Optional.of(result);
	}

	default Optional<T> max(Comparator<? super T> comparator) {
		return reduce(BinaryOperator.maxBy(comparator));
	}

	default long count() {
		long count = 0;
		for (Iterator iterator = iterator(); iterator.hasNext(); iterator.next()) {
			count++;
		}
		return count;
	}

	default Object[] toArray() {
		return toList().toArray();
	}

	default List<T> toList() {
		return toList(ArrayList::new);
	}

	default List<T> toList(Supplier<? extends List<T>> constructor) {
		return toCollection(constructor);
	}

	default <A> A[] toArray(IntFunction<? extends A[]> constructor) {
		List list = toList();
		@SuppressWarnings("unchecked")
		A[] array = (A[]) list.toArray(constructor.apply(list.size()));
		return array;
	}

	default Stream<T> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	default boolean all(Predicate<? super T> predicate) {
		for (T each : this) {
			if (!predicate.test(each))
				return false;
		}
		return true;
	}

	default boolean none(Predicate<? super T> predicate) {
		return !any(predicate);
	}

	default boolean any(Predicate<? super T> predicate) {
		for (T each : this) {
			if (predicate.test(each))
				return true;
		}
		return false;
	}

	default Sequence<T> peek(Consumer<? super T> action) {
		return () -> new PeekingIterator<>(iterator(), action);
	}

	default <U extends R, V extends R, R> Sequence<R> delimit(V delimiter) {
		return () -> {
			@SuppressWarnings("unchecked")
			Iterator<U> delimitedIterator = (Iterator<U>) iterator();
			return new DelimitingIterator<>(delimitedIterator, Optional.empty(), Optional.of(delimiter),
			                                Optional.empty());
		};
	}

	default <U extends R, V extends R, R> Sequence<R> delimit(V prefix, V delimiter, V suffix) {
		return () -> {
			@SuppressWarnings("unchecked")
			Iterator<U> delimitedIterator = (Iterator<U>) iterator();
			return new DelimitingIterator<>(delimitedIterator, Optional.of(prefix), Optional.of(delimiter),
			                                Optional.of(suffix));
		};
	}

	default <U extends R, V extends R, R> Sequence<R> prefix(V prefix) {
		return () -> {
			@SuppressWarnings("unchecked")
			Iterator<U> delimitedIterator = (Iterator<U>) iterator();
			return new DelimitingIterator<>(delimitedIterator, Optional.of(prefix), Optional.empty(), Optional.empty
					                                                                                                   ());
		};
	}

	default <U extends R, V extends R, R> Sequence<R> suffix(V suffix) {
		return () -> {
			@SuppressWarnings("unchecked")
			Iterator<U> delimitedIterator = (Iterator<U>) iterator();
			return new DelimitingIterator<>(delimitedIterator, Optional.empty(), Optional.empty(), Optional.of(suffix));
		};
	}

	default <U> Sequence<Pair<T, U>> interleave(Sequence<U> that) {
		return () -> new InterleavingPairingIterator<>(iterator(), that.iterator());
	}

	default Sequence<T> reverse() {
		return () -> new ReverseIterator<>(iterator());
	}

	default Sequence<T> shuffle() {
		List<T> list = toList();
		Collections.shuffle(list);
		return from(list);
	}

	default Sequence<T> shuffle(@Nonnull Random md) {
		List<T> list = toList();
		Collections.shuffle(list, md);
		return from(list);
	}
}
