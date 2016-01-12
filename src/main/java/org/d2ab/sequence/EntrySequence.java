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

import org.d2ab.iterable.ChainingIterable;
import org.d2ab.iterator.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyIterator;

/**
 * An {@link Iterable} sequence of elements with {@link Stream}-like operations for refining, transforming and
 * collating the list of elements.
 */
@FunctionalInterface
public interface EntrySequence<T, U> extends Iterable<Entry<T, U>> {
	@Nonnull
	static <T, U> EntrySequence<T, U> of(Pair<T, U> item) {
		return from(Collections.singleton(item));
	}

	@SafeVarargs
	@Nonnull
	static <T, U> EntrySequence<T, U> from(@Nonnull Iterable<? extends Entry<T, U>>... iterables) {
		return () -> new ChainingIterator<>(iterables);
	}

	@SafeVarargs
	@Nonnull
	static <T, U> EntrySequence<T, U> of(@Nonnull Entry<T, U>... items) {
		return from(asList(items));
	}

	@Nonnull
	static <T, U> EntrySequence<T, U> empty() {
		return from(emptyIterator());
	}

	@Nonnull
	static <T, U> EntrySequence<T, U> from(@Nonnull Iterator<Entry<T, U>> iterator) {
		return () -> iterator;
	}

	@Nonnull
	static <T, U> EntrySequence<T, U> from(@Nonnull Stream<Entry<T, U>> stream) {
		return stream::iterator;
	}

	@Nonnull
	static <T, U> EntrySequence<T, U> from(@Nonnull Supplier<? extends Iterator<Entry<T, U>>> iteratorSupplier) {
		return iteratorSupplier::get;
	}

	@Nonnull
	static <T, U> EntrySequence<T, U> recurse(@Nullable T firstSeed, @Nullable U secondSeed,
	                                          @Nonnull BiFunction<T, U, ? extends Entry<T, U>> op) {
		return () -> new RecursiveIterator<>(Pair.of(firstSeed, secondSeed), p -> op.apply(p.getKey(), p.getValue()));
	}

	@Nonnull
	static <T, U, V, W> EntrySequence<V, W> recurse(@Nullable T firstSeed, @Nullable U secondSeed,
	                                                @Nonnull BiFunction<T, U, Entry<V, W>> f,
	                                                @Nonnull BiFunction<V, W, Entry<T, U>> g) {
		return () -> new RecursiveIterator<>(f.apply(firstSeed, secondSeed), p -> {
			Entry<T, U> p2 = g.apply(p.getKey(), p.getValue());
			return f.apply(p2.getKey(), p2.getValue());
		});
	}

	@Nonnull
	static <K, V> EntrySequence<K, V> from(Map<K, V> map) {
		return map.entrySet()::iterator;
	}

	@Nonnull
	default <V, W> EntrySequence<V, W> map(@Nonnull BiFunction<? super T, ? super U, ? extends Entry<V, W>> mapper) {
		return map(e -> mapper.apply(e.getKey(), e.getValue()));
	}

	@Nonnull
	default <V, W> EntrySequence<V, W> map(Function<Entry<T, U>, Entry<V, W>> mapper) {
		return () -> new MappingIterator<>(mapper).backedBy(iterator());
	}

	@Nonnull
	default <V, W> EntrySequence<V, W> map(@Nonnull Function<? super T, ? extends V> keyMapper,
	                                       @Nonnull Function<? super U, ? extends W> valueMapper) {
		return map(e -> Pair.map(e, keyMapper, valueMapper));
	}

	@Nonnull
	default EntrySequence<T, U> skip(int skip) {
		return () -> new SkippingIterator<Entry<T, U>>(skip).backedBy(iterator());
	}

	@Nonnull
	default EntrySequence<T, U> limit(int limit) {
		return () -> new LimitingIterator<Entry<T, U>>(limit).backedBy(iterator());
	}

	@Nonnull
	default EntrySequence<T, U> then(@Nonnull EntrySequence<T, U> then) {
		return () -> new ChainingIterator<>(this, then);
	}

	@Nonnull
	default EntrySequence<T, U> filter(@Nonnull BiPredicate<? super T, ? super U> predicate) {
		return () -> new FilteringIterator<Entry<T, U>>(e -> Pair.test(e, predicate)).backedBy(iterator());
	}

	@Nonnull
	default <V, W> EntrySequence<V, W> flatMap(@Nonnull
	                                           BiFunction<? super T, ? super U, ? extends Iterable<Entry<V, W>>>
			                                               mapper) {
		ChainingIterable<Entry<V, W>> result = new ChainingIterable<>();
		forEach(e -> result.append(mapper.apply(e.getKey(), e.getValue())));
		return result::iterator;
	}

	default EntrySequence<T, U> until(Entry<T, U> terminal) {
		return () -> new ExclusiveTerminalIterator<>(terminal).backedBy(iterator());
	}

	default Set<Entry<T, U>> toSet() {
		return toSet(HashSet::new);
	}

	default <S extends Set<Entry<T, U>>> S toSet(Supplier<? extends S> constructor) {
		return toCollection(constructor);
	}

	default <C extends Collection<Entry<T, U>>> C toCollection(Supplier<? extends C> constructor) {
		return collect(constructor, Collection::add);
	}

	default <C> C collect(Supplier<? extends C> constructor, BiConsumer<? super C, ? super Entry<T, U>> adder) {
		C result = constructor.get();
		forEach(each -> adder.accept(result, each));
		return result;
	}

	default SortedSet<Entry<T, U>> toSortedSet() {
		return toSet(TreeSet::new);
	}

	default Map<T, U> toMap() {
		return toMap(HashMap::new);
	}

	default <M extends Map<T, U>> M toMap(Supplier<? extends M> constructor) {
		M result = constructor.get();
		forEach(each -> Pair.putEntry(result, each));
		return result;
	}

	default SortedMap<T, U> toSortedMap() {
		return toMap(TreeMap::new);
	}

	default <S, R> S collect(Collector<Entry<T, U>, R, S> collector) {
		R result = collector.supplier().get();
		BiConsumer<R, Entry<T, U>> accumulator = collector.accumulator();
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
		for (Entry<T, U> each : this) {
			if (first)
				first = false;
			else
				result.append(delimiter);
			result.append(each);
		}
		result.append(suffix);
		return result.toString();
	}

	default Entry<T, U> reduce(Entry<T, U> identity, BinaryOperator<Entry<T, U>> operator) {
		return reduce(identity, operator, iterator());
	}

	default Entry<T, U> reduce(Entry<T, U> identity, BinaryOperator<Entry<T, U>> operator,
	                           Iterator<Entry<T, U>> iterator) {
		Entry<T, U> result = identity;
		while (iterator.hasNext())
			result = operator.apply(result, iterator.next());
		return result;
	}

	default Optional<Entry<T, U>> first() {
		Iterator<Entry<T, U>> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	default Optional<Entry<T, U>> second() {
		Iterator<Entry<T, U>> iterator = iterator();

		Iterators.skip(iterator);
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	default Optional<Entry<T, U>> third() {
		Iterator<Entry<T, U>> iterator = iterator();

		Iterators.skip(iterator, 2);
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	default Optional<Entry<T, U>> last() {
		Iterator<Entry<T, U>> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		Entry<T, U> last;
		do {
			last = iterator.next();
		} while (iterator.hasNext());

		return Optional.of(last);
	}

	default Sequence<List<Entry<T, U>>> partition(int window) {
		return () -> new PartitioningIterator<Entry<T, U>>(window).backedBy(iterator());
	}

	default EntrySequence<T, U> step(int step) {
		return () -> new SteppingIterator<Entry<T, U>>(step).backedBy(iterator());
	}

	default EntrySequence<T, U> distinct() {
		return () -> new DistinctIterator<Entry<T, U>>().backedBy(iterator());
	}

	default EntrySequence<T, U> sorted() {
		return () -> new SortingIterator<Entry<T, U>>().backedBy(iterator());
	}

	default EntrySequence<T, U> sorted(Comparator<? super Entry<? extends T, ? extends U>> comparator) {
		return () -> new SortingIterator<Entry<T, U>>(comparator).backedBy(iterator());
	}

	default Optional<Entry<T, U>> min(Comparator<? super Entry<? extends T, ? extends U>> comparator) {
		return reduce(BinaryOperator.minBy(comparator));
	}

	default Optional<Entry<T, U>> reduce(BinaryOperator<Entry<T, U>> operator) {
		Iterator<Entry<T, U>> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		Entry<T, U> result = reduce(iterator.next(), operator, iterator);
		return Optional.of(result);
	}

	default Optional<Entry<T, U>> max(Comparator<? super Entry<? extends T, ? extends U>> comparator) {
		return reduce(BinaryOperator.maxBy(comparator));
	}

	default int count() {
		int count = 0;
		for (Entry<T, U> ignored : this) {
			count++;
		}
		return count;
	}

	default Entry<T, U>[] toArray() {
		return toArray(Entry[]::new);
	}

	default Entry<T, U>[] toArray(IntFunction<Entry<T, U>[]> constructor) {
		List list = toList();
		@SuppressWarnings("unchecked")
		Entry<T, U>[] array = (Entry<T, U>[]) list.toArray(constructor.apply(list.size()));
		return array;
	}

	default List<Entry<T, U>> toList() {
		return toList(ArrayList::new);
	}

	default List<Entry<T, U>> toList(Supplier<List<Entry<T, U>>> constructor) {
		return toCollection(constructor);
	}

	default boolean all(BiPredicate<? super T, ? super U> predicate) {
		for (Entry<T, U> each : this) {
			if (!predicate.test(each.getKey(), each.getValue()))
				return false;
		}
		return true;
	}

	default boolean none(BiPredicate<? super T, ? super U> predicate) {
		return !any(predicate);
	}

	default boolean any(BiPredicate<? super T, ? super U> predicate) {
		for (Entry<T, U> each : this) {
			if (predicate.test(each.getKey(), each.getValue()))
				return true;
		}
		return false;
	}

	default EntrySequence<T, U> peek(BiConsumer<T, U> action) {
		return () -> new PeekingIterator(Pair.consumer(action)).backedBy(iterator());
	}
}
