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

import org.d2ab.function.QuaternaryFunction;
import org.d2ab.iterable.ChainingIterable;
import org.d2ab.iterable.Iterables;
import org.d2ab.iterator.*;
import org.d2ab.util.Entries;
import org.d2ab.util.Pair;

import javax.annotation.Nullable;
import java.util.*;
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
public interface BiSequence<L, R> extends Iterable<Pair<L, R>> {
	static <L, R> BiSequence<L, R> of(Pair<L, R> item) {
		return from(Collections.singleton(item));
	}

	@SafeVarargs
	static <L, R> BiSequence<L, R> of(Pair<L, R>... items) {
		return from(asList(items));
	}

	static <L, R> BiSequence<L, R> ofPair(L left, R right) {
		return of(Pair.of(left, right));
	}

	static <L, R> BiSequence<L, R> from(Iterable<Pair<L, R>> iterable) {
		return iterable::iterator;
	}

	@SafeVarargs
	static <L, R> BiSequence<L, R> from(Iterable<? extends Pair<L, R>>... iterables) {
		return () -> new ChainingIterator<>(iterables);
	}

	static <L, R> BiSequence<L, R> empty() {
		return from(emptyIterator());
	}

	static <L, R> BiSequence<L, R> from(Iterator<Pair<L, R>> iterator) {
		return () -> iterator;
	}

	static <L, R> BiSequence<L, R> from(Stream<Pair<L, R>> stream) {
		return stream::iterator;
	}

	static <L, R> BiSequence<L, R> from(Supplier<? extends Iterator<Pair<L, R>>> iteratorSupplier) {
		return iteratorSupplier::get;
	}

	static <L, R> BiSequence<L, R> recurse(@Nullable L leftSeed,
	                                       @Nullable R rightSeed,
	                                       BiFunction<? super L, ? super R, ? extends Pair<L, R>> op) {
		return recurse(Pair.of(leftSeed, rightSeed), Pair.asUnaryOperator(op));
	}

	static <L, R> BiSequence<L, R> recurse(Pair<L, R> seed, UnaryOperator<Pair<L, R>> op) {
		return () -> new RecursiveIterator<>(seed, op);
	}

	static <L, R, LL, RR> BiSequence<LL, RR> recurse(@Nullable L leftSeed,
	                                                 @Nullable R rightSeed,
	                                                 BiFunction<? super L, ? super R, ? extends Pair<LL, RR>> f,
	                                                 BiFunction<? super LL, ? super RR, ? extends Pair<L, R>> g) {
		return recurse(f.apply(leftSeed, rightSeed), Pair.asUnaryOperator(f, g));
	}

	static <K, V> BiSequence<K, V> from(Map<K, V> map) {
		return Sequence.from(map.entrySet()).map(Pair::from)::iterator;
	}

	default <LL, RR> BiSequence<LL, RR> map(BiFunction<? super L, ? super R, ? extends Pair<LL, RR>> mapper) {
		return map(Pair.asFunction(mapper));
	}

	default <LL, RR> BiSequence<LL, RR> map(Function<? super Pair<L, R>, ? extends Pair<LL, RR>> mapper) {
		return () -> new MappingIterator<>(mapper).backedBy(iterator());
	}

	default <LL, RR> BiSequence<LL, RR> map(Function<? super L, ? extends LL> leftMapper,
	                                        Function<? super R, ? extends RR> rightMapper) {
		return map(e -> Pair.map(e, leftMapper, rightMapper));
	}

	default BiSequence<L, R> skip(int skip) {
		return () -> new SkippingIterator<Pair<L, R>>(skip).backedBy(iterator());
	}

	default BiSequence<L, R> limit(int limit) {
		return () -> new LimitingIterator<Pair<L, R>>(limit).backedBy(iterator());
	}

	@SuppressWarnings("unchecked")
	default BiSequence<L, R> then(BiSequence<L, R> then) {
		return () -> new ChainingIterator<>(this, then);
	}

	default BiSequence<L, R> filter(BiPredicate<? super L, ? super R> predicate) {
		return filter(Pair.asPredicate(predicate));
	}

	default BiSequence<L, R> filter(Predicate<? super Pair<L, R>> predicate) {
		return () -> new FilteringIterator<>(predicate).backedBy(iterator());
	}

	default <LL, RR> BiSequence<LL, RR> flatMap(BiFunction<? super L, ? super R, ? extends Iterable<Pair<LL, RR>>>
			                                            mapper) {
		ChainingIterable<Pair<LL, RR>> result = new ChainingIterable<>();
		Function<? super Pair<L, R>, ? extends Iterable<Pair<LL, RR>>> function = Pair.asFunction(mapper);
		Consumer<? super Iterable<Pair<LL, RR>>> append = result::append;
		Sequence.from(this).map(function).forEach(append);
		return result::iterator;
	}

	default BiSequence<L, R> until(Pair<L, R> terminal) {
		return () -> new ExclusiveTerminalIterator<>(terminal).backedBy(iterator());
	}

	default BiSequence<L, R> endingAt(Pair<L, R> terminal) {
		return () -> new InclusiveTerminalIterator<>(terminal).backedBy(iterator());
	}

	default BiSequence<L, R> until(L left, R right) {
		return until(Pair.of(left, right));
	}

	default BiSequence<L, R> endingAt(L left, R right) {
		return endingAt(Pair.of(left, right));
	}

	default BiSequence<L, R> until(BiPredicate<? super L, ? super R> terminal) {
		return until(Pair.asPredicate(terminal));
	}

	default BiSequence<L, R> endingAt(BiPredicate<? super L, ? super R> terminal) {
		return endingAt(Pair.asPredicate(terminal));
	}

	default BiSequence<L, R> until(Predicate<? super Pair<L, R>> terminal) {
		return () -> new ExclusiveTerminalIterator<>(terminal).backedBy(iterator());
	}

	default BiSequence<L, R> endingAt(Predicate<? super Pair<L, R>> terminal) {
		return () -> new InclusiveTerminalIterator<>(terminal).backedBy(iterator());
	}

	default Pair<L, R>[] toArray() {
		return toArray(Pair[]::new);
	}

	default Pair<L, R>[] toArray(IntFunction<Pair<L, R>[]> constructor) {
		List list = toList();
		@SuppressWarnings("unchecked")
		Pair<L, R>[] array = (Pair<L, R>[]) list.toArray(constructor.apply(list.size()));
		return array;
	}

	default List<Pair<L, R>> toList() {
		return toList(ArrayList::new);
	}

	default List<Pair<L, R>> toList(Supplier<List<Pair<L, R>>> constructor) {
		return toCollection(constructor);
	}

	default Set<Pair<L, R>> toSet() {
		return toSet(HashSet::new);
	}

	default <S extends Set<Pair<L, R>>> S toSet(Supplier<? extends S> constructor) {
		return toCollection(constructor);
	}

	default SortedSet<Pair<L, R>> toSortedSet() {
		return toSet(TreeSet::new);
	}

	default Map<L, R> toMap() {
		return toMap(HashMap::new);
	}

	default <M extends Map<L, R>> M toMap(Supplier<? extends M> constructor) {
		M result = constructor.get();
		forEach(each -> Entries.put(result, each));
		return result;
	}

	default SortedMap<L, R> toSortedMap() {
		return toMap(TreeMap::new);
	}

	default <C extends Collection<Pair<L, R>>> C toCollection(Supplier<? extends C> constructor) {
		return collect(constructor, Collection::add);
	}

	default <C> C collect(Supplier<? extends C> constructor, BiConsumer<? super C, ? super Pair<L, R>> adder) {
		C result = constructor.get();
		forEach(each -> adder.accept(result, each));
		return result;
	}

	default <S, C> S collect(Collector<Pair<L, R>, C, S> collector) {
		C result = collector.supplier().get();
		BiConsumer<C, Pair<L, R>> accumulator = collector.accumulator();
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
		for (Pair<L, R> each : this) {
			if (first)
				first = false;
			else
				result.append(delimiter);
			result.append(each);
		}
		result.append(suffix);
		return result.toString();
	}

	default Optional<Pair<L, R>> reduce(BinaryOperator<Pair<L, R>> operator) {
		Iterator<Pair<L, R>> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		Pair<L, R> result = reduce(iterator.next(), operator, iterator);
		return Optional.of(result);
	}

	default Optional<Pair<L, R>> reduce(QuaternaryFunction<L, R, L, R, Pair<L, R>> operator) {
		Iterator<Pair<L, R>> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		Pair<L, R> result = reduce(iterator.next(), Pair.asBinaryOperator(operator), iterator);
		return Optional.of(result);
	}

	default Pair<L, R> reduce(Pair<L, R> identity, BinaryOperator<Pair<L, R>> operator) {
		return reduce(identity, operator, iterator());
	}

	default Pair<L, R> reduce(L left, R right, QuaternaryFunction<L, R, L, R, Pair<L, R>> operator) {
		return reduce(Pair.of(left, right), Pair.asBinaryOperator(operator), iterator());
	}

	default Pair<L, R> reduce(Pair<L, R> identity, BinaryOperator<Pair<L, R>> operator, Iterator<Pair<L, R>>
			                                                                                    iterator) {
		Pair<L, R> result = identity;
		while (iterator.hasNext())
			result = operator.apply(result, iterator.next());
		return result;
	}

	default Optional<Pair<L, R>> first() {
		Iterator<Pair<L, R>> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	default Optional<Pair<L, R>> second() {
		Iterator<Pair<L, R>> iterator = iterator();

		Iterators.skip(iterator);
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	default Optional<Pair<L, R>> third() {
		Iterator<Pair<L, R>> iterator = iterator();

		Iterators.skip(iterator, 2);
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	default Optional<Pair<L, R>> last() {
		Iterator<Pair<L, R>> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		Pair<L, R> last;
		do {
			last = iterator.next();
		} while (iterator.hasNext());

		return Optional.of(last);
	}

	default Sequence<List<Pair<L, R>>> partition(int window) {
		return () -> new PartitioningIterator<Pair<L, R>>(window).backedBy(iterator());
	}

	default BiSequence<L, R> step(int step) {
		return () -> new SteppingIterator<Pair<L, R>>(step).backedBy(iterator());
	}

	default BiSequence<L, R> distinct() {
		return () -> new DistinctIterator<Pair<L, R>>().backedBy(iterator());
	}

	default BiSequence<L, R> sorted() {
		return () -> new SortingIterator<Pair<L, R>>().backedBy(iterator());
	}

	default BiSequence<L, R> sorted(Comparator<? super Pair<? extends L, ? extends R>> comparator) {
		return () -> new SortingIterator<Pair<L, R>>(comparator).backedBy(iterator());
	}

	default Optional<Pair<L, R>> min(Comparator<? super Pair<? extends L, ? extends R>> comparator) {
		return reduce(BinaryOperator.minBy(comparator));
	}

	default Optional<Pair<L, R>> max(Comparator<? super Pair<? extends L, ? extends R>> comparator) {
		return reduce(BinaryOperator.maxBy(comparator));
	}

	default int count() {
		int count = 0;
		for (Pair<L, R> ignored : this)
			count++;
		return count;
	}

	default boolean all(BiPredicate<? super L, ? super R> biPredicate) {
		Predicate<? super Pair<L, R>> predicate = Pair.asPredicate(biPredicate);
		for (Pair<L, R> each : this)
			if (!predicate.test(each))
				return false;
		return true;
	}

	default boolean none(BiPredicate<? super L, ? super R> predicate) {
		return !any(predicate);
	}

	default boolean any(BiPredicate<? super L, ? super R> biPredicate) {
		Predicate<? super Pair<L, R>> predicate = Pair.asPredicate(biPredicate);
		for (Pair<L, R> each : this)
			if (predicate.test(each))
				return true;
		return false;
	}

	default BiSequence<L, R> peek(BiConsumer<L, R> action) {
		Consumer<? super Pair<L, R>> consumer = Pair.asConsumer(action);
		return () -> new PeekingIterator<>(consumer).backedBy(iterator());
	}

	default BiSequence<L, R> append(Iterator<? extends Pair<L, R>> iterator) {
		return append(Iterables.from(iterator));
	}

	default BiSequence<L, R> append(Iterable<? extends Pair<L, R>> that) {
		@SuppressWarnings("unchecked")
		Iterable<Pair<L, R>> chainingSequence = new ChainingIterable<>(this, that);
		return chainingSequence::iterator;
	}

	@SuppressWarnings("unchecked")
	default BiSequence<L, R> append(Pair<L, R>... entries) {
		return append(Iterables.from(entries));
	}

	@SuppressWarnings("unchecked")
	default BiSequence<L, R> appendPair(L left, R right) {
		return append(Pair.of(left, right));
	}

	default BiSequence<L, R> append(Stream<Pair<L, R>> stream) {
		return append(Iterables.from(stream));
	}

	default Sequence<Pair<L, R>> toSequence() {
		return Sequence.from(this);
	}

	default <T> Sequence<T> toSequence(BiFunction<L, R, T> mapper) {
		return toSequence(Pair.asFunction(mapper));
	}

	default <T> Sequence<T> toSequence(Function<? super Pair<L, R>, ? extends T> mapper) {
		return () -> new MappingIterator<>(mapper).backedBy(iterator());
	}

	default BiSequence<L, R> repeat() {
		return () -> new RepeatingIterator<>(this, -1);
	}

	default BiSequence<L, R> repeat(long times) {
		return () -> new RepeatingIterator<>(this, times);
	}

	default BiSequence<L, R> reverse() {
		return () -> new ReverseIterator<Pair<L, R>>().backedBy(iterator());
	}

	default BiSequence<L, R> shuffle() {
		List<Pair<L, R>> list = toList();
		Collections.shuffle(list);
		return from(list);
	}

	default BiSequence<L, R> shuffle(Random md) {
		List<Pair<L, R>> list = toList();
		Collections.shuffle(list, md);
		return from(list);
	}
}
