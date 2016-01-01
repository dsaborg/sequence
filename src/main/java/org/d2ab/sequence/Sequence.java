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
 * An {@link Iterable} sequence of elements with {@link Stream}-like operations for refining, transforming and
 * collating the list of elements.
 */
@FunctionalInterface
public interface Sequence<T> extends Iterable<T> {
	@Nonnull
	static <T> Sequence<T> of(@Nullable T item) {
		return from(singleton(item));
	}

	@Nonnull
	static <T> Sequence<T> from(@Nonnull Iterable<T> items) {
		return items::iterator;
	}

	@SafeVarargs
	@Nonnull
	static <T> Sequence<T> of(@Nonnull T... items) {
		return from(asList(items));
	}

	@Nonnull
	static <T> Sequence<T> empty() {
		return from(emptyIterator());
	}

	@Nonnull
	static <T> Sequence<T> from(@Nonnull Iterator<T> iterator) {
		return () -> iterator;
	}

	@SafeVarargs
	@Nonnull
	static <T> Sequence<T> from(@Nonnull Iterable<T>... iterables) {
		return new ChainingSequence<>(iterables);
	}

	@Nonnull
	static <T> Sequence<T> from(@Nonnull Supplier<? extends Iterator<T>> iteratorSupplier) {
		return iteratorSupplier::get;
	}

	static <T> Sequence<T> from(Stream<T> stream) {
		return stream::iterator;
	}

	static <T> Sequence<T> recurse(T seed, UnaryOperator<T> op) {
		return () -> new RecursiveIterator<>(seed, op);
	}

	static <T, S> Sequence<S> recurse(T seed, Function<? super T, ? extends S> f, Function<? super S, ? extends T> g) {
		return () -> new RecursiveIterator<>(f.apply(seed), f.compose(g)::apply);
	}

	static <K, V> Sequence<Pair<K, V>> from(Map<? extends K, ? extends V> map) {
		return from(map.entrySet()).map(Pair::from);
	}

	@Nonnull
	default <U> Sequence<U> map(@Nonnull Function<? super T, ? extends U> mapper) {
		return () -> new MappingIterator<>(iterator(), mapper);
	}

	@Nonnull
	default Sequence<T> skip(int skip) {
		return () -> new SkippingIterator<>(iterator(), skip);
	}

	@Nonnull
	default Sequence<T> limit(int limit) {
		return () -> new LimitingIterator<>(iterator(), limit);
	}

	default Sequence<T> append(Iterator<T> iterator) {
		return append(Iterables.from(iterator));
	}

	@Nonnull
	default Sequence<T> append(@Nonnull Iterable<T> that) {
		return new ChainingSequence<>(this, that);
	}

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
		return ChainingSequence.flatMap(this, mapper);
	}

	default <U> Sequence<U> flatten() {
		return ChainingSequence.flatten(this);
	}

	default Sequence<T> untilNull() {
		return () -> new TerminalIterator<>(iterator(), null);
	}

	default Sequence<T> until(T terminal) {
		return () -> new TerminalIterator<>(iterator(), terminal);
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

	default <K, V> Map<K, V> pairsToMap(Function<? super T, ? extends Pair<K, V>> mapper) {
		return pairsToMap(HashMap::new, mapper);
	}

	default <M extends Map<K, V>, K, V> M pairsToMap(Supplier<? extends M> constructor,
	                                                 Function<? super T, ? extends Pair<K, V>> mapper) {
		M result = constructor.get();
		forEach(each -> mapper.apply(each).putInto(result));
		return result;
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

	default Sequence<T> step(int step) {
		return () -> new SteppingIterator<>(iterator(), step);
	}

	default Sequence<T> distinct() {
		return () -> new DistinctIterator<>(iterator());
	}

	default <S extends Comparable<? super S>> Sequence<S> sorted() {
		return () -> new SortingIterator<>((Iterator<S>) iterator());
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

	default int count() {
		int count = 0;
		for (T ignored : this) {
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
		List result = toList();
		return (A[]) result.toArray(constructor.apply(result.size()));
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
		return () -> new PeekingIterator(iterator(), action);
	}

	default <U> Sequence<U> delimit(U delimiter) {
		return () -> new DelimitingIterator<>((Iterator<U>) iterator(), Optional.empty(), Optional.of(delimiter),
		                                      Optional.empty());
	}

	default <U> Sequence<U> delimit(U prefix, U delimiter, U suffix) {
		return () -> new DelimitingIterator<>((Iterator<U>) iterator(), Optional.of(prefix), Optional.of(delimiter),
		                                      Optional.of(suffix));
	}

	default <U> Sequence<U> prefix(U prefix) {
		return () -> new DelimitingIterator<>((Iterator<U>) iterator(), Optional.of(prefix), Optional.empty(),
		                                      Optional.empty());
	}

	default <U> Sequence<U> suffix(U suffix) {
		return () -> new DelimitingIterator<>((Iterator<U>) iterator(), Optional.empty(), Optional.empty(),
		                                      Optional.of(suffix));
	}

	default <U> Sequence<U> interleave(Sequence<? extends U> that) {
		return () -> new InterleavingIterator(this, that);
	}
}
