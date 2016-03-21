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

import org.d2ab.function.QuaternaryFunction;
import org.d2ab.iterable.ChainingIterable;
import org.d2ab.iterable.Iterables;
import org.d2ab.iterator.*;
import org.d2ab.util.Entries;

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
public interface EntrySequence<K, V> extends Iterable<Entry<K, V>> {
	static <K, V> EntrySequence<K, V> of(Entry<K, V> item) {
		return from(Collections.singleton(item));
	}

	@SafeVarargs
	static <K, V> EntrySequence<K, V> of(Entry<K, V>... items) {
		return from(asList(items));
	}

	static <K, V> EntrySequence<K, V> ofEntry(K left, V right) {
		return of(Entries.of(left, right));
	}

	@SuppressWarnings("unchecked")
	static <K, V> EntrySequence<K, V> ofEntries(Object... os) {
		if (os.length % 2 != 0)
			throw new IllegalArgumentException("Expected an even set of objects, but got: " + os.length);

		List<Entry<K, V>> entries = new ArrayList<>();
		for (int i = 0; i < os.length; i += 2)
			entries.add(Entries.of((K) os[i], (V) os[i + 1]));
		return from(entries);
	}

	@SafeVarargs
	static <K, V> EntrySequence<K, V> from(Iterable<? extends Entry<K, V>>... iterables) {
		return () -> new ChainingIterator<>(iterables);
	}

	static <K, V> EntrySequence<K, V> from(Iterable<Entry<K, V>> iterable) {
		return iterable::iterator;
	}

	static <K, V> EntrySequence<K, V> empty() {
		return from(emptyIterator());
	}

	static <K, V> EntrySequence<K, V> from(Iterator<Entry<K, V>> iterator) {
		return () -> iterator;
	}

	static <K, V> EntrySequence<K, V> from(Stream<Entry<K, V>> stream) {
		return stream::iterator;
	}

	static <K, V> EntrySequence<K, V> from(Supplier<? extends Iterator<Entry<K, V>>> iteratorSupplier) {
		return iteratorSupplier::get;
	}

	static <K, V> EntrySequence<K, V> recurse(K keySeed, V valueSeed,
	                                          BiFunction<K, V, ? extends Entry<K, V>> op) {
		return () -> new RecursiveIterator<>(Entries.of(keySeed, valueSeed), Entries.asUnaryOperator(op));
	}

	static <K, V, KK, VV> EntrySequence<KK, VV> recurse(K keySeed, V valueSeed,
	                                                    BiFunction<? super K, ? super V, ? extends Entry<KK, VV>> f,
	                                                    BiFunction<? super KK, ? super VV, ? extends Entry<K, V>> g) {
		return () -> new RecursiveIterator<>(f.apply(keySeed, valueSeed), Entries.asUnaryOperator(f, g));
	}

	static <K, V> EntrySequence<K, V> from(Map<K, V> map) {
		return map.entrySet()::iterator;
	}

	default <KK, VV> EntrySequence<KK, VV> map(BiFunction<? super K, ? super V, ? extends Entry<KK, VV>> mapper) {
		return map(Entries.asFunction(mapper));
	}

	default <KK, VV> EntrySequence<KK, VV> map(Function<? super Entry<K, V>, ? extends Entry<KK, VV>> mapper) {
		return () -> new MappingIterator<>(iterator(), mapper);
	}

	default <KK, VV> EntrySequence<KK, VV> map(Function<? super K, ? extends KK> keyMapper,
	                                           Function<? super V, ? extends VV> valueMapper) {
		return map(Entries.asFunction(keyMapper, valueMapper));
	}

	default EntrySequence<K, V> skip(int skip) {
		return () -> new SkippingIterator<>(iterator(), skip);
	}

	default EntrySequence<K, V> limit(int limit) {
		return () -> new LimitingIterator<>(iterator(), limit);
	}

	@SuppressWarnings("unchecked")
	default EntrySequence<K, V> then(EntrySequence<K, V> then) {
		return () -> new ChainingIterator<>(this, then);
	}

	default EntrySequence<K, V> filter(BiPredicate<? super K, ? super V> predicate) {
		return filter(Entries.asPredicate(predicate));
	}

	default EntrySequence<K, V> filter(Predicate<? super Entry<K, V>> predicate) {
		return () -> new FilteringIterator<>(iterator(), predicate);
	}

	default <KK, VV> EntrySequence<KK, VV> flatten(
			BiFunction<? super K, ? super V, ? extends Iterable<Entry<KK, VV>>> mapper) {
		return flatten(Entries.asFunction(mapper));
	}

	default <KK, VV> EntrySequence<KK, VV> flatten(
			Function<? super Entry<K, V>, ? extends Iterable<Entry<KK, VV>>> mapper) {
		ChainingIterable<Entry<KK, VV>> result = new ChainingIterable<>();
		toSequence(mapper).forEach(result::append);
		return result::iterator;
	}

	default EntrySequence<K, V> until(Entry<K, V> terminal) {
		return () -> new ExclusiveTerminalIterator<>(iterator(), terminal);
	}

	default EntrySequence<K, V> endingAt(Entry<K, V> terminal) {
		return () -> new InclusiveTerminalIterator<>(iterator(), terminal);
	}

	default EntrySequence<K, V> until(K key, V value) {
		return until(Entries.of(key, value));
	}

	default EntrySequence<K, V> endingAt(K key, V value) {
		return endingAt(Entries.of(key, value));
	}

	default EntrySequence<K, V> until(BiPredicate<? super K, ? super V> terminal) {
		return until(Entries.asPredicate(terminal));
	}

	default EntrySequence<K, V> endingAt(BiPredicate<? super K, ? super V> terminal) {
		return endingAt(Entries.asPredicate(terminal));
	}

	default EntrySequence<K, V> until(Predicate<? super Entry<K, V>> terminal) {
		return () -> new ExclusiveTerminalIterator<>(iterator(), terminal);
	}

	default EntrySequence<K, V> endingAt(Predicate<? super Entry<K, V>> terminal) {
		return () -> new InclusiveTerminalIterator<>(iterator(), terminal);
	}

	default Entry<K, V>[] toArray() {
		return toArray(Entry[]::new);
	}

	default Entry<K, V>[] toArray(IntFunction<Entry<K, V>[]> constructor) {
		List list = toList();
		@SuppressWarnings("unchecked")
		Entry<K, V>[] array = (Entry<K, V>[]) list.toArray(constructor.apply(list.size()));
		return array;
	}

	default List<Entry<K, V>> toList() {
		return toList(ArrayList::new);
	}

	default List<Entry<K, V>> toList(Supplier<List<Entry<K, V>>> constructor) {
		return toCollection(constructor);
	}

	default Set<Entry<K, V>> toSet() {
		return toSet(HashSet::new);
	}

	default <S extends Set<Entry<K, V>>> S toSet(Supplier<? extends S> constructor) {
		return toCollection(constructor);
	}

	default SortedSet<Entry<K, V>> toSortedSet() {
		return toSet(TreeSet::new);
	}

	default Map<K, V> toMap() {
		return toMap(HashMap::new);
	}

	default <M extends Map<K, V>> M toMap(Supplier<? extends M> constructor) {
		M result = constructor.get();
		forEach(each -> Entries.put(result, each));
		return result;
	}

	default SortedMap<K, V> toSortedMap() {
		return toMap(TreeMap::new);
	}

	default <C extends Collection<Entry<K, V>>> C toCollection(Supplier<? extends C> constructor) {
		return collect(constructor, Collection::add);
	}

	default <C> C collect(Supplier<? extends C> constructor, BiConsumer<? super C, ? super Entry<K, V>> adder) {
		C result = constructor.get();
		forEach(each -> adder.accept(result, each));
		return result;
	}

	default <S, R> S collect(Collector<Entry<K, V>, R, S> collector) {
		R result = collector.supplier().get();
		BiConsumer<R, Entry<K, V>> accumulator = collector.accumulator();
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
		for (Entry<K, V> each : this) {
			if (first)
				first = false;
			else
				result.append(delimiter);
			result.append(each);
		}
		result.append(suffix);
		return result.toString();
	}

	default Optional<Entry<K, V>> reduce(BinaryOperator<Entry<K, V>> operator) {
		Iterator<Entry<K, V>> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		Entry<K, V> result = reduce(iterator.next(), operator, iterator);
		return Optional.of(result);
	}

	default Optional<Entry<K, V>> reduce(QuaternaryFunction<K, V, K, V, Entry<K, V>> operator) {
		Iterator<Entry<K, V>> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		Entry<K, V> result = reduce(iterator.next(), Entries.asBinaryOperator(operator), iterator);
		return Optional.of(result);
	}

	default Entry<K, V> reduce(Entry<K, V> identity, BinaryOperator<Entry<K, V>> operator) {
		return reduce(identity, operator, iterator());
	}

	default Entry<K, V> reduce(K key, V value, QuaternaryFunction<K, V, K, V, Entry<K, V>> operator) {
		return reduce(Entries.of(key, value), Entries.asBinaryOperator(operator), iterator());
	}

	default Entry<K, V> reduce(Entry<K, V> identity, BinaryOperator<Entry<K, V>> operator,
	                           Iterator<Entry<K, V>> iterator) {
		Entry<K, V> result = identity;
		while (iterator.hasNext())
			result = operator.apply(result, iterator.next());
		return result;
	}

	default Optional<Entry<K, V>> first() {
		Iterator<Entry<K, V>> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	default Optional<Entry<K, V>> second() {
		Iterator<Entry<K, V>> iterator = iterator();

		Iterators.skip(iterator);
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	default Optional<Entry<K, V>> third() {
		Iterator<Entry<K, V>> iterator = iterator();

		Iterators.skip(iterator, 2);
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	default Optional<Entry<K, V>> last() {
		Iterator<Entry<K, V>> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		Entry<K, V> last;
		do {
			last = iterator.next();
		} while (iterator.hasNext());

		return Optional.of(last);
	}

	default Sequence<EntrySequence<K, V>> window(int window) {
		return window(window, 1);
	}

	default Sequence<EntrySequence<K, V>> window(int window, int step) {
		return () -> new WindowingIterator<Entry<K, V>, EntrySequence<K, V>>(iterator(), window, step) {
			@Override
			protected EntrySequence<K, V> toSequence(List<Entry<K, V>> list) {
				return EntrySequence.from(list);
			}
		};
	}

	default Sequence<EntrySequence<K, V>> batch(int size) {
		return window(size, size);
	}

	default EntrySequence<K, V> step(int step) {
		return () -> new SteppingIterator<>(iterator(), step);
	}

	default EntrySequence<K, V> distinct() {
		return () -> new DistinctIterator<>(iterator());
	}

	default EntrySequence<K, V> sorted() {
		return () -> new SortingIterator<>(iterator());
	}

	default EntrySequence<K, V> sorted(Comparator<? super Entry<? extends K, ? extends V>> comparator) {
		return () -> new SortingIterator<>(iterator(), comparator);
	}

	default Optional<Entry<K, V>> min(Comparator<? super Entry<? extends K, ? extends V>> comparator) {
		return reduce(BinaryOperator.minBy(comparator));
	}

	default Optional<Entry<K, V>> max(Comparator<? super Entry<? extends K, ? extends V>> comparator) {
		return reduce(BinaryOperator.maxBy(comparator));
	}

	default int count() {
		int count = 0;
		for (Entry<K, V> ignored : this)
			count++;
		return count;
	}

	default boolean all(BiPredicate<? super K, ? super V> biPredicate) {
		Predicate<? super Entry<K, V>> predicate = Entries.asPredicate(biPredicate);
		for (Entry<K, V> each : this)
			if (!predicate.test(each))
				return false;
		return true;
	}

	default boolean none(BiPredicate<? super K, ? super V> predicate) {
		return !any(predicate);
	}

	default boolean any(BiPredicate<? super K, ? super V> biPredicate) {
		Predicate<? super Entry<K, V>> predicate = Entries.asPredicate(biPredicate);
		for (Entry<K, V> each : this)
			if (predicate.test(each))
				return true;
		return false;
	}

	default EntrySequence<K, V> peek(BiConsumer<K, V> action) {
		Consumer<? super Entry<K, V>> consumer = Entries.asConsumer(action);
		return () -> new PeekingIterator<>(iterator(), consumer);
	}

	default EntrySequence<K, V> append(Iterator<? extends Entry<K, V>> iterator) {
		return append(Iterables.from(iterator));
	}

	default EntrySequence<K, V> append(Iterable<? extends Entry<K, V>> that) {
		@SuppressWarnings("unchecked")
		Iterable<Entry<K, V>> chainingSequence = new ChainingIterable<>(this, that);
		return chainingSequence::iterator;
	}

	@SuppressWarnings("unchecked")
	default EntrySequence<K, V> append(Entry<K, V>... entries) {
		return append(Iterables.from(entries));
	}

	@SuppressWarnings("unchecked")
	default EntrySequence<K, V> appendEntry(K key, V value) {
		return append(Entries.of(key, value));
	}

	default EntrySequence<K, V> append(Stream<Entry<K, V>> stream) {
		return append(Iterables.from(stream));
	}

	default Sequence<Entry<K, V>> toSequence() {
		return Sequence.from(this);
	}

	default <T> Sequence<T> toSequence(BiFunction<? super K, ? super V, ? extends T> mapper) {
		return toSequence(Entries.asFunction(mapper));
	}

	default <T> Sequence<T> toSequence(Function<? super Entry<K, V>, ? extends T> mapper) {
		return () -> new MappingIterator<>(iterator(), mapper);
	}

	default EntrySequence<K, V> repeat() {
		return () -> new RepeatingIterator<>(this, -1);
	}

	default EntrySequence<K, V> repeat(long times) {
		return () -> new RepeatingIterator<>(this, times);
	}

	default EntrySequence<K, V> reverse() {
		return () -> new ReverseIterator<>(iterator());
	}

	default EntrySequence<K, V> shuffle() {
		List<Entry<K, V>> list = toList();
		Collections.shuffle(list);
		return from(list);
	}

	default EntrySequence<K, V> shuffle(Random md) {
		List<Entry<K, V>> list = toList();
		Collections.shuffle(list, md);
		return from(list);
	}
}
