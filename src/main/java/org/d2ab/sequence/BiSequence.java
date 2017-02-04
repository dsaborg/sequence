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
import org.d2ab.function.*;
import org.d2ab.iterator.*;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.util.Pair;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.function.BinaryOperator.maxBy;
import static java.util.function.BinaryOperator.minBy;
import static org.d2ab.util.Preconditions.requireAtLeastOne;
import static org.d2ab.util.Preconditions.requireAtLeastZero;

/**
 * An {@link Iterable} sequence of {@link Pair}s with {@link Stream}-like operations for refining, transforming and
 * collating the list of {@link Pair}s.
 */
@FunctionalInterface
public interface BiSequence<L, R> extends IterableCollection<Pair<L, R>> {
	/**
	 * Create an empty {@code BiSequence} with no items.
	 *
	 * @see #of(Pair)
	 * @see #of(Pair...)
	 * @see #ofPair(Object, Object)
	 * @see #ofPairs(Object...)
	 * @see #from(Iterable)
	 */
	static <L, R> BiSequence<L, R> empty() {
		return Iterables.<Pair<L, R>>empty()::iterator;
	}

	/**
	 * Create a {@code BiSequence} with one {@link Pair}.
	 *
	 * @see #of(Pair...)
	 * @see #ofPair(Object, Object)
	 * @see #ofPairs(Object...)
	 * @see #from(Iterable)
	 */
	static <L, R> BiSequence<L, R> of(Pair<L, R> item) {
		return from(Collections.singleton(item));
	}

	/**
	 * Create a {@code BiSequence} with the given {@link Pair}s.
	 *
	 * @see #of(Pair)
	 * @see #ofPair(Object, Object)
	 * @see #ofPairs(Object...)
	 * @see #from(Iterable)
	 */
	@SafeVarargs
	static <L, R> BiSequence<L, R> of(Pair<L, R>... items) {
		requireNonNull(items, "items");

		return from(Lists.of(items));
	}

	/**
	 * Create a {@code BiSequence} with one {@link Pair} of the given left and right values.
	 *
	 * @see #ofPairs(Object...)
	 * @see #of(Pair)
	 * @see #of(Pair...)
	 * @see #from(Iterable)
	 */
	static <L, R> BiSequence<L, R> ofPair(L left, R right) {
		return of(Pair.of(left, right));
	}

	/**
	 * Create a {@code BiSequence} with {@link Pair}s of the given left and right values given in sequence in the input
	 * array.
	 *
	 * @throws IllegalArgumentException if the array of left and right values is not of even length.
	 * @see #ofPair(Object, Object)
	 * @see #of(Pair)
	 * @see #of(Pair...)
	 * @see #from(Iterable)
	 */
	@SuppressWarnings("unchecked")
	static <T, U> BiSequence<T, U> ofPairs(Object... items) {
		requireNonNull(items, "items");
		if (items.length % 2 != 0)
			throw new IllegalArgumentException("Expected an even number of items: " + items.length);

		List<Pair<T, U>> pairs = new ArrayList<>();
		for (int i = 0; i < items.length; i += 2)
			pairs.add(Pair.of((T) items[i], (U) items[i + 1]));
		return from(pairs);
	}

	/**
	 * Create a {@code BiSequence} from an {@link Iterable} of pairs.
	 *
	 * @see #of(Pair)
	 * @see #of(Pair...)
	 * @see #from(Iterable...)
	 * @see #cache(Iterable)
	 */
	static <L, R> BiSequence<L, R> from(Iterable<Pair<L, R>> iterable) {
		requireNonNull(iterable, "iterable");

		return iterable::iterator;
	}

	/**
	 * Create a concatenated {@code BiSequence} from several {@link Iterable}s of pairs which are concatenated together
	 * to form the stream of pairs in the {@code BiSequence}.
	 *
	 * @see #of(Pair)
	 * @see #of(Pair...)
	 * @see #from(Iterable)
	 * @deprecated Use {@link #concat(Iterable[])} instead.
	 */
	@SafeVarargs
	@Deprecated
	static <L, R> BiSequence<L, R> from(Iterable<Pair<L, R>>... iterables) {
		return concat(iterables);
	}

	/**
	 * Create a concatenated {@code BiSequence} from several {@link Iterable}s of pairs which are concatenated together
	 * to form the stream of pairs in the {@code BiSequence}.
	 *
	 * @see #of(Pair)
	 * @see #of(Pair...)
	 * @see #from(Iterable)
	 */
	@SafeVarargs
	static <L, R> BiSequence<L, R> concat(Iterable<Pair<L, R>>... iterables) {
		requireNonNull(iterables, "iterables");
		for (Iterable<Pair<L, R>> iterable : iterables)
			requireNonNull(iterable, "each iterable");

		return () -> new ChainingIterator<>(iterables);
	}

	/**
	 * Create a once-only {@code BiSequence} from an {@link Iterator} of pairs. Note that {@code BiSequence}s created
	 * from {@link Iterator}s cannot be passed over more than once. Further attempts will register the
	 * {@code BiSequence} as empty.
	 *
	 * @see #of(Pair)
	 * @see #of(Pair...)
	 * @see #from(Iterable)
	 * @see #cache(Iterator)
	 * @since 1.1
	 */
	static <L, R> BiSequence<L, R> once(Iterator<Pair<L, R>> iterator) {
		requireNonNull(iterator, "iterator");

		return from(Iterables.once(iterator));
	}

	/**
	 * Create a once-only {@code BiSequence} from a {@link Stream} of pairs. Note that {@code BiSequence}s created from
	 * {@link Stream}s cannot be passed over more than once. Further attempts will register the {@code BiSequence} as
	 * empty.
	 *
	 * @see #of(Pair)
	 * @see #of(Pair...)
	 * @see #from(Iterable)
	 * @see #once(Iterator)
	 * @see #cache(Stream)
	 * @since 1.1
	 */
	static <L, R> BiSequence<L, R> once(Stream<Pair<L, R>> stream) {
		requireNonNull(stream, "stream");

		return once(stream.iterator());
	}

	/**
	 * Create a {@code BiSequence} of {@link Map.Entry} key/value items from a {@link Map} of items. The resulting
	 * {@code BiSequence} can be mapped using {@link Pair} items, which implement {@link Map.Entry} and can thus be
	 * processed as part of the {@code BiSequence}'s transformation steps.
	 *
	 * @see #of
	 * @see #from(Iterable)
	 */
	static <K, V> BiSequence<K, V> from(Map<K, V> map) {
		requireNonNull(map, "map");

		return from(Sequence.from(map.entrySet()).map(Pair::from));
	}

	/**
	 * Create a {@code BiSequence} with a cached copy of an {@link Iterable} of pairs.
	 *
	 * @see #cache(Iterator)
	 * @see #cache(Stream)
	 * @see #from(Iterable)
	 * @since 1.1
	 */
	static <L, R> BiSequence<L, R> cache(Iterable<Pair<L, R>> iterable) {
		requireNonNull(iterable, "iterable");

		return from(Iterables.toList(iterable));
	}

	/**
	 * Create a {@code BiSequence} with a cached copy of an {@link Iterator} of pairs.
	 *
	 * @see #cache(Iterable)
	 * @see #cache(Stream)
	 * @see #once(Iterator)
	 * @since 1.1
	 */
	static <L, R> BiSequence<L, R> cache(Iterator<Pair<L, R>> iterator) {
		requireNonNull(iterator, "iterator");

		return from(Iterators.toList(iterator));
	}

	/**
	 * Create a {@code BiSequence} with a cached copy of a {@link Stream} of pairs.
	 *
	 * @see #cache(Iterable)
	 * @see #cache(Iterator)
	 * @see #once(Stream)
	 * @since 1.1
	 */
	static <L, R> BiSequence<L, R> cache(Stream<Pair<L, R>> stream) {
		requireNonNull(stream, "stream");

		return from(stream.collect(Collectors.toList()));
	}

	/**
	 * @return an infinite {@code BiSequence} generated by repeatedly calling the given supplier. The returned {@code
	 * BiSequence} never terminates naturally.
	 *
	 * @see #recurse(Pair, UnaryOperator)
	 * @see #endingAt(Pair)
	 * @see #until(Pair)
	 */
	static <L, R> BiSequence<L, R> generate(Supplier<Pair<L, R>> supplier) {
		requireNonNull(supplier, "supplier");

		return () -> (InfiniteIterator<Pair<L, R>>) supplier::get;
	}

	/**
	 * @return an infinite {@code BiSequence} where each {@link #iterator()} is generated by polling for a supplier and
	 * then using it to generate the sequence of pairs. The sequence never terminates.
	 *
	 * @see #generate(Supplier)
	 * @see #recurse(Pair, UnaryOperator)
	 * @see #endingAt(Pair)
	 * @see #until(Pair)
	 */
	static <L, R> BiSequence<L, R> multiGenerate(Supplier<? extends Supplier<? extends Pair<L, R>>> multiSupplier) {
		requireNonNull(multiSupplier, "multiSupplier");

		return () -> {
			Supplier<? extends Pair<L, R>> supplier = requireNonNull(
					multiSupplier.get(), "multiSupplier.get()");
			return (InfiniteIterator<Pair<L, R>>) supplier::get;
		};
	}

	/**
	 * Returns a {@code BiSequence} produced by recursively applying the given operation to the given seeds, which
	 * form the first element of the sequence, the second being {@code f(leftSeed, rightSeed)}, the third
	 * {@code f(f(leftSeed, rightSeed))} and so on. The returned {@code BiSequence} never terminates naturally.
	 *
	 * @return a {@code BiSequence} produced by recursively applying the given operation to the given seed
	 *
	 * @see #recurse(Pair, UnaryOperator)
	 * @see #generate(Supplier)
	 * @see #endingAt(Pair)
	 * @see #until(Pair)
	 */
	static <L, R> BiSequence<L, R> recurse(L leftSeed, R rightSeed,
	                                       BiFunction<? super L, ? super R, ? extends Pair<L, R>> operator) {
		requireNonNull(operator, "operator");

		return recurse(Pair.of(leftSeed, rightSeed), p -> operator.apply(p.getLeft(), p.getRight()));
	}

	/**
	 * Returns a {@code BiSequence} produced by recursively applying the given operation to the given seed, which
	 * form the first element of the sequence, the second being {@code f(seed)}, the third {@code f(f(seed))} and so
	 * on. The returned {@code BiSequence} never terminates naturally.
	 *
	 * @return a {@code BiSequence} produced by recursively applying the given operation to the given seed
	 *
	 * @see #recurse(Pair, UnaryOperator)
	 * @see #generate(Supplier)
	 * @see #endingAt(Pair)
	 * @see #until(Pair)
	 */
	static <L, R> BiSequence<L, R> recurse(Pair<L, R> seed, UnaryOperator<Pair<L, R>> operator) {
		requireNonNull(operator, "operator");

		return () -> new RecursiveIterator<>(seed, operator);
	}

	/**
	 * Returns a {@code BiSequence} produced by recursively applying the given mapper {@code f} and incrementer
	 * {@code g} operations to the given seeds, the first element being {@code f(leftSeed, rightSeed)}, the second
	 * being {@code f(g(f(leftSeed, rightSeed)))}, the third {@code f(g(f(g(f(leftSeed, rightSeed)))))} and so on.
	 * The returned {@code BiSequence} never terminates naturally.
	 *
	 * @param f a mapper function for producing elements that are to be included in the sequence, the first being
	 *          f(leftSeed, rightSeed)
	 * @param g an incrementer function for producing the next unmapped element to be included in the sequence, applied
	 *          to the first mapped element f(leftSeed, rightSeed) to produce the second unmapped value
	 *
	 * @return a {@code BiSequence} produced by recursively applying the given mapper and incrementer operations to the
	 * given seeds
	 *
	 * @see #recurse(Pair, UnaryOperator)
	 * @see #endingAt(Pair)
	 * @see #until(Pair)
	 */
	static <L, R, LL, RR> BiSequence<LL, RR> recurse(L leftSeed, R rightSeed,
	                                                 BiFunction<? super L, ? super R, ? extends Pair<LL, RR>> f,
	                                                 BiFunction<? super LL, ? super RR, ? extends Pair<L, R>> g) {
		requireNonNull(f, "f");
		requireNonNull(g, "g");

		Function<Pair<L, R>, Pair<LL, RR>> f1 = asPairFunction(f);
		Function<Pair<LL, RR>, Pair<L, R>> g1 = asPairFunction(g);
		return recurse(f.apply(leftSeed, rightSeed), f1.compose(g1)::apply);
	}

	/**
	 * @return the given doubly bi-valued function converted to a pair-based binary operator.
	 */
	static <L, R> BinaryOperator<Pair<L, R>> asPairBinaryOperator(QuaternaryFunction<L, R, L, R, Pair<L, R>> f) {
		return (p1, p2) -> f.apply(p1.getLeft(), p1.getRight(), p2.getLeft(), p2.getRight());
	}

	/**
	 * @return the given bi-valued function converted to a pair-based function.
	 */
	static <L, R, T> Function<Pair<L, R>, T> asPairFunction(BiFunction<? super L, ? super R, ? extends T> f) {
		return p -> f.apply(p.getLeft(), p.getRight());
	}

	/**
	 * @return the given bi-valued predicate converted to a pair-based predicate.
	 */
	static <L, R> Predicate<Pair<L, R>> asPairPredicate(BiPredicate<? super L, ? super R> predicate) {
		return p -> predicate.test(p.getLeft(), p.getRight());
	}

	/**
	 * @return the given bi-valued consumer converted to a pair-based consumer.
	 */
	static <L, R> Consumer<Pair<L, R>> asPairConsumer(BiConsumer<? super L, ? super R> action) {
		return p -> action.accept(p.getLeft(), p.getRight());
	}

	/**
	 * Map the pairs in this {@code BiSequence} to another set of pairs specified by the given {@code mapper}
	 * function.
	 *
	 * @see #map(Function)
	 * @see #map(Function, Function)
	 * @see #flatten(Function)
	 */
	default <LL, RR> BiSequence<LL, RR> map(BiFunction<? super L, ? super R, ? extends Pair<LL, RR>> mapper) {
		requireNonNull(mapper, "mapper");

		return map(asPairFunction(mapper));
	}

	/**
	 * Map the pairs in this {@code BiSequence} to another set of pairs specified by the given {@code mapper}
	 * function.
	 *
	 * @see #map(BiFunction)
	 * @see #map(Function, Function)
	 * @see #flatten(Function)
	 */
	default <LL, RR> BiSequence<LL, RR> map(Function<? super Pair<L, R>, ? extends Pair<LL, RR>> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> new MappingIterator<>(iterator(), mapper);
	}

	/**
	 * Map the pairs in this {@code BiSequence} to another set of pairs specified by the given {@code leftMapper}
	 * amd {@code rightMapper} functions.
	 *
	 * @see #map(BiFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 */
	default <LL, RR> BiSequence<LL, RR> map(Function<? super L, ? extends LL> leftMapper,
	                                        Function<? super R, ? extends RR> rightMapper) {
		requireNonNull(leftMapper, "leftMapper");
		requireNonNull(rightMapper, "rightMapper");

		return map(p -> p.map(leftMapper, rightMapper));
	}

	/**
	 * Map the pairs in this {@code BiSequence} to another set of pairs specified by the given {@code mapper} function.
	 * In addition to the current pair, the mapper has access to the index of each pair.
	 *
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @since 1.2
	 */
	default <LL, RR> BiSequence<LL, RR> mapIndexed(
			ObjIntFunction<? super Pair<L, R>, ? extends Pair<LL, RR>> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> new IndexingMappingIterator<>(iterator(), mapper);
	}

	/**
	 * Map the pairs in this {@code BiSequence} to another set of pairs specified by the given {@code mapper} function.
	 * In addition to the current pair, the mapper has access to the index of each pair.
	 *
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @since 1.2
	 */
	default <LL, RR> BiSequence<LL, RR> mapIndexed(
			ObjObjIntFunction<? super L, ? super R, ? extends Pair<LL, RR>> mapper) {
		requireNonNull(mapper, "mapper");

		return mapIndexed((p, i) -> mapper.apply(p.getLeft(), p.getRight(), i));
	}

	/**
	 * Map the left values of the pairs in this {@code BiSequence} to another set of left values specified by the given
	 * {@code mapper} function.
	 *
	 * @see #map(Function)
	 * @see #map(BiFunction)
	 * @see #map(Function, Function)
	 * @see #flatten(Function)
	 */
	default <LL> BiSequence<LL, R> mapLeft(Function<? super L, ? extends LL> mapper) {
		return map((l, r) -> Pair.of(mapper.apply(l), r));
	}

	/**
	 * Map the right values of the pairs in this {@code BiSequence} to another set of right values specified by the
	 * given {@code mapper} function.
	 *
	 * @see #map(Function)
	 * @see #map(BiFunction)
	 * @see #map(Function, Function)
	 * @see #flatten(Function)
	 */
	default <RR> BiSequence<L, RR> mapRight(Function<? super R, ? extends RR> mapper) {
		return map((l, r) -> Pair.of(l, mapper.apply(r)));
	}

	/**
	 * Skip a set number of steps in this {@code BiSequence}.
	 */
	default BiSequence<L, R> skip(int skip) {
		requireAtLeastZero(skip, "skip");

		if (skip == 0)
			return this;

		return () -> new SkippingIterator<>(iterator(), skip);
	}

	/**
	 * Skip a set number of steps at the end of this {@code BiSequence}.
	 *
	 * @since 1.1
	 */
	default BiSequence<L, R> skipTail(int skip) {
		requireAtLeastZero(skip, "skip");

		if (skip == 0)
			return this;

		return () -> new TailSkippingIterator<>(iterator(), skip);
	}

	/**
	 * Limit the maximum number of results returned by this {@code BiSequence}.
	 */
	default BiSequence<L, R> limit(int limit) {
		requireAtLeastZero(limit, "limit");

		if (limit == 0)
			return empty();

		return () -> new LimitingIterator<>(iterator(), limit);
	}

	/**
	 * Limit the results returned by this {@code BiSequence} to the last {@code limit} pairs.
	 *
	 * @since 2.3
	 */
	default BiSequence<L, R> limitTail(int limit) {
		requireAtLeastZero(limit, "limit");

		if (limit == 0)
			return empty();

		return () -> new TailLimitingIterator<>(iterator(), limit);
	}

	/**
	 * Filter the elements in this {@code BiSequence}, keeping only the elements that match the given
	 * {@link BiPredicate}.
	 */
	default BiSequence<L, R> filter(BiPredicate<? super L, ? super R> predicate) {
		requireNonNull(predicate, "predicate");

		return filter(asPairPredicate(predicate));
	}

	/**
	 * Filter the elements in this {@code BiSequence}, keeping only the pairs that match the given
	 * {@link Predicate}.
	 */
	default BiSequence<L, R> filter(Predicate<? super Pair<L, R>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new FilteringIterator<>(iterator(), predicate);
	}

	/**
	 * Filter the pairs in this {@code BiSequence}, keeping only the elements that match the given
	 * {@link ObjIntPredicate}, which is passed the current pair and its index in the sequence.
	 *
	 * @since 1.2
	 */
	default BiSequence<L, R> filterIndexed(ObjIntPredicate<? super Pair<L, R>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new IndexedFilteringIterator<>(iterator(), predicate);
	}

	/**
	 * Filter the pairs in this {@code BiSequence}, keeping only the elements that match the given
	 * {@link ObjObjIntPredicate}, which is passed the current pair and its index in the sequence.
	 *
	 * @since 1.2
	 */
	default BiSequence<L, R> filterIndexed(ObjObjIntPredicate<? super L, ? super R> predicate) {
		requireNonNull(predicate, "predicate");

		return filterIndexed((p, i) -> predicate.test(p.getLeft(), p.getRight(), i));
	}

	/**
	 * @return a {@code BiSequence} containing only the pairs found in the given target array.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default BiSequence<L, R> including(Pair<L, R>... pairs) {
		requireNonNull(pairs, "pairs");

		return filter(p -> Arrayz.contains(pairs, p));
	}

	/**
	 * @return a {@code BiSequence} containing only the pairs found in the given target iterable.
	 *
	 * @since 1.2
	 */
	default BiSequence<L, R> including(Iterable<? extends Pair<L, R>> pairs) {
		requireNonNull(pairs, "pairs");

		return filter(p -> Iterables.contains(pairs, p));
	}

	/**
	 * @return a {@code BiSequence} containing only the pairs not found in the given target array.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default BiSequence<L, R> excluding(Pair<L, R>... pairs) {
		requireNonNull(pairs, "pairs");

		return filter(p -> !Arrayz.contains(pairs, p));
	}

	/**
	 * @return a {@code BiSequence} containing only the pairs not found in the given target iterable.
	 *
	 * @since 1.2
	 */
	default BiSequence<L, R> excluding(Iterable<? extends Pair<L, R>> pairs) {
		requireNonNull(pairs, "pairs");

		return filter(p -> !Iterables.contains(pairs, p));
	}

	/**
	 * @return a {@link Sequence} of the {@link Pair}s in this {@code BiSequence} flattened into their left and right
	 * components and strung together.
	 */
	default <T> Sequence<T> flatten() {
		return toSequence().flatten(pair -> Iterables.fromPair((Pair) pair));
	}

	/**
	 * Flatten the elements in this {@code BiSequence} according to the given mapper {@link BiFunction}. The resulting
	 * {@code BiSequence} contains the elements that is the result of applying the mapper {@link BiFunction} to each
	 * element, appended together inline as a single {@code BiSequence}.
	 *
	 * @see #flatten(Function)
	 * @see #flattenLeft(Function)
	 * @see #flattenRight(Function)
	 * @see #map(BiFunction)
	 * @see #map(Function)
	 */
	default <LL, RR> BiSequence<LL, RR> flatten(
			BiFunction<? super L, ? super R, ? extends Iterable<Pair<LL, RR>>> mapper) {
		requireNonNull(mapper, "mapper");

		Function<? super Pair<L, R>, ? extends Iterable<Pair<LL, RR>>> function = asPairFunction(mapper);
		return flatten(function);
	}

	/**
	 * Flatten the elements in this {@code BiSequence} according to the given mapper {@link Function}. The resulting
	 * {@code BiSequence} contains the pairs that is the result of applying the mapper {@link Function} to each
	 * pair, appended together inline as a single {@code BiSequence}.
	 *
	 * @see #flatten(BiFunction)
	 * @see #flattenLeft(Function)
	 * @see #flattenRight(Function)
	 * @see #map(BiFunction)
	 * @see #map(Function)
	 */
	default <LL, RR> BiSequence<LL, RR> flatten(
			Function<? super Pair<L, R>, ? extends Iterable<Pair<LL, RR>>> mapper) {
		requireNonNull(mapper, "mapper");

		return ChainingIterable.concat(toSequence(mapper))::iterator;
	}

	/**
	 * Flatten the left side of each pair in this sequence, applying multiples of left values returned by the given
	 * mapper to the same right value of each pair.
	 *
	 * @see #flattenRight(Function)
	 * @see #flatten(Function)
	 * @see #flatten(BiFunction)
	 */
	default <LL> BiSequence<LL, R> flattenLeft(Function<? super Pair<L, R>, ? extends Iterable<LL>> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> new LeftFlatteningPairIterator<>(iterator(), mapper);
	}

	/**
	 * Flatten the right side of each pair in this sequence, applying multiples of right values returned by the given
	 * mapper to the same left value of each pair.
	 *
	 * @see #flattenLeft(Function)
	 * @see #flatten(Function)
	 * @see #flatten(BiFunction)
	 */
	default <RR> BiSequence<L, RR> flattenRight(Function<? super Pair<L, R>, ? extends Iterable<RR>> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> new RightFlatteningPairIterator<>(iterator(), mapper);
	}

	/**
	 * Terminate this {@code BiSequence} just before the given element is encountered, not including the element in the
	 * {@code BiSequence}.
	 *
	 * @see #until(Predicate)
	 * @see #endingAt(Pair)
	 * @see #generate(Supplier)
	 * @see #recurse(Pair, UnaryOperator)
	 * @see #repeat()
	 */
	default BiSequence<L, R> until(Pair<L, R> terminal) {
		return () -> new ExclusiveTerminalIterator<>(iterator(), terminal);
	}

	/**
	 * Terminate this {@code BiSequence} when the given element is encountered, including the element as the last
	 * element in the {@code BiSequence}.
	 *
	 * @see #endingAt(Predicate)
	 * @see #until(Pair)
	 * @see #generate(Supplier)
	 * @see #recurse(Pair, UnaryOperator)
	 * @see #repeat()
	 */
	default BiSequence<L, R> endingAt(Pair<L, R> terminal) {
		return () -> new InclusiveTerminalIterator<>(iterator(), terminal);
	}

	/**
	 * Terminate this {@code BiSequence} just before the pair with the given left and right components is encountered,
	 * not including the pair in the {@code BiSequence}.
	 *
	 * @see #until(Pair)
	 * @see #until(Predicate)
	 * @see #until(BiPredicate)
	 * @see #endingAt(Pair)
	 * @see #generate(Supplier)
	 * @see #recurse(Pair, UnaryOperator)
	 * @see #repeat()
	 */
	default BiSequence<L, R> until(L left, R right) {
		return until(Pair.of(left, right));
	}

	/**
	 * Terminate this {@code BiSequence} when the pair with the given left and right components is encountered,
	 * including the element as the last element in the {@code BiSequence}.
	 *
	 * @see #endingAt(Pair)
	 * @see #endingAt(Predicate)
	 * @see #endingAt(BiPredicate)
	 * @see #until(Pair)
	 * @see #generate(Supplier)
	 * @see #recurse(Pair, UnaryOperator)
	 * @see #repeat()
	 */
	default BiSequence<L, R> endingAt(L left, R right) {
		return endingAt(Pair.of(left, right));
	}

	/**
	 * Terminate this {@code BiSequence} just before the given predicate is satisfied, not including the element that
	 * satisfies the predicate in the {@code BiSequence}.
	 *
	 * @see #until(Predicate)
	 * @see #until(Object, Object)
	 * @see #until(Pair)
	 * @see #endingAt(BiPredicate)
	 * @see #generate(Supplier)
	 * @see #recurse(Pair, UnaryOperator)
	 * @see #repeat()
	 */
	default BiSequence<L, R> until(BiPredicate<? super L, ? super R> predicate) {
		requireNonNull(predicate, "predicate");

		return until(asPairPredicate(predicate));
	}

	/**
	 * Terminate this {@code BiSequence} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the last element in the {@code BiSequence}.
	 *
	 * @see #endingAt(Predicate)
	 * @see #endingAt(Object, Object)
	 * @see #endingAt(Pair)
	 * @see #until(BiPredicate)
	 * @see #generate(Supplier)
	 * @see #recurse(Pair, UnaryOperator)
	 * @see #repeat()
	 */
	default BiSequence<L, R> endingAt(BiPredicate<? super L, ? super R> predicate) {
		requireNonNull(predicate, "predicate");

		return endingAt(asPairPredicate(predicate));
	}

	/**
	 * Terminate this {@code BiSequence} just before the given predicate is satisfied, not including the element that
	 * satisfies the predicate in the {@code BiSequence}.
	 *
	 * @see #until(BiPredicate)
	 * @see #until(Pair)
	 * @see #endingAt(Predicate)
	 * @see #generate(Supplier)
	 * @see #recurse(Pair, UnaryOperator)
	 * @see #repeat()
	 */
	default BiSequence<L, R> until(Predicate<? super Pair<L, R>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new ExclusiveTerminalIterator<>(iterator(), predicate);
	}

	/**
	 * Terminate this {@code BiSequence} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the last element in the {@code BiSequence}.
	 *
	 * @see #endingAt(BiPredicate)
	 * @see #endingAt(Pair)
	 * @see #until(Predicate)
	 * @see #generate(Supplier)
	 * @see #recurse(Pair, UnaryOperator)
	 * @see #repeat()
	 */
	default BiSequence<L, R> endingAt(Predicate<? super Pair<L, R>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new InclusiveTerminalIterator<>(iterator(), predicate);
	}

	/**
	 * Begin this {@code BiSequence} just after the given pair is encountered, not including the pair in the
	 * {@code BiSequence}.
	 *
	 * @see #startingAfter(Predicate)
	 * @see #startingAfter(BiPredicate)
	 * @see #startingFrom(Pair)
	 * @since 1.1
	 */
	default BiSequence<L, R> startingAfter(Pair<L, R> element) {
		return () -> new ExclusiveStartingIterator<>(iterator(), element);
	}

	/**
	 * Begin this {@code BiSequence} when the given pair is encountered, including the pair as the first element
	 * in the {@code BiSequence}.
	 *
	 * @see #startingFrom(Predicate)
	 * @see #startingFrom(BiPredicate)
	 * @see #startingAfter(Pair)
	 * @since 1.1
	 */
	default BiSequence<L, R> startingFrom(Pair<L, R> element) {
		return () -> new InclusiveStartingIterator<>(iterator(), element);
	}

	/**
	 * Begin this {@code BiSequence} just after the given predicate is satisfied, not including the pair that
	 * satisfies the predicate in the {@code BiSequence}.
	 *
	 * @see #startingAfter(BiPredicate)
	 * @see #startingAfter(Pair)
	 * @see #startingFrom(Predicate)
	 * @since 1.1
	 */
	default BiSequence<L, R> startingAfter(Predicate<? super Pair<L, R>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new ExclusiveStartingIterator<>(iterator(), predicate);
	}

	/**
	 * Begin this {@code BiSequence} when the given predicate is satisfied, including the pair that satisfies
	 * the predicate as the first element in the {@code BiSequence}.
	 *
	 * @see #startingFrom(BiPredicate)
	 * @see #startingFrom(Pair)
	 * @see #startingAfter(Predicate)
	 * @since 1.1
	 */
	default BiSequence<L, R> startingFrom(Predicate<? super Pair<L, R>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new InclusiveStartingIterator<>(iterator(), predicate);
	}

	/**
	 * Begin this {@code BiSequence} just after the given predicate is satisfied, not including the pair that
	 * satisfies the predicate in the {@code BiSequence}.
	 *
	 * @see #startingAfter(Predicate)
	 * @see #startingAfter(Pair)
	 * @see #startingFrom(Predicate)
	 * @since 1.1
	 */
	default BiSequence<L, R> startingAfter(BiPredicate<? super L, ? super R> predicate) {
		requireNonNull(predicate, "predicate");

		return startingAfter(asPairPredicate(predicate));
	}

	/**
	 * Begin this {@code BiSequence} when the given predicate is satisfied, including the pair that satisfies
	 * the predicate as the first element in the {@code BiSequence}.
	 *
	 * @see #startingFrom(Predicate)
	 * @see #startingFrom(Pair)
	 * @see #startingAfter(Predicate)
	 * @since 1.1
	 */
	default BiSequence<L, R> startingFrom(BiPredicate<? super L, ? super R> predicate) {
		requireNonNull(predicate, "predicate");

		return startingFrom(asPairPredicate(predicate));
	}

	/**
	 * Collect the pairs in this {@code BiSequence} into an array.
	 */
	default Pair<L, R>[] toArray() {
		return toArray(Pair[]::new);
	}

	/**
	 * Collect the pairs in this {@code BiSequence} into an array of the type determined by the given array
	 * constructor.
	 */
	default Pair<L, R>[] toArray(IntFunction<Pair<L, R>[]> constructor) {
		requireNonNull(constructor, "constructor");

		List<Pair<L, R>> list = toList();
		return list.toArray(constructor.apply(list.size()));
	}

	/**
	 * Collect the pairs in this {@code BiSequence} into a {@link List}.
	 */
	default List<Pair<L, R>> toList() {
		return toList(ArrayList::new);
	}

	/**
	 * Collect the pairs in this {@code BiSequence} into a {@link List} of the type determined by the given
	 * constructor.
	 */
	default List<Pair<L, R>> toList(Supplier<List<Pair<L, R>>> constructor) {
		requireNonNull(constructor, "constructor");

		return toCollection(constructor);
	}

	/**
	 * Collect the pairs in this {@code BiSequence} into a {@link Set}.
	 */
	default Set<Pair<L, R>> toSet() {
		return toSet(HashSet::new);
	}

	/**
	 * Collect the pairs in this {@code BiSequence} into a {@link Set} of the type determined by the given
	 * constructor.
	 */
	default <S extends Set<Pair<L, R>>> S toSet(Supplier<? extends S> constructor) {
		requireNonNull(constructor, "constructor");

		return toCollection(constructor);
	}

	/**
	 * Collect the pairs in this {@code BiSequence} into a {@link SortedSet}.
	 */
	default SortedSet<Pair<L, R>> toSortedSet() {
		return toSet(TreeSet::new);
	}

	/**
	 * Collect the pairs in this {@code BiSequence} into a {@link Map}, using left values as keys and right values as
	 * values. If the same left value occurs more than once in the {@code BiSequence}, the key is remapped in the
	 * resulting map to the latter corresponding right value.
	 */
	default Map<L, R> toMap() {
		return toMap(HashMap::new);
	}

	/**
	 * Collect the pairs in this {@code BiSequence} into a {@link Map} of the type determined by the given
	 * constructor, using left values as keys and right values as values. If the same left value occurs more than once
	 * in the {@code BiSequence}, the key is remapped in the resulting map to the latter corresponding right value.
	 */
	default <M extends Map<L, R>> M toMap(Supplier<? extends M> constructor) {
		requireNonNull(constructor, "constructor");

		M result = constructor.get();
		for (Pair<L, R> each : this)
			result.put(each.getLeft(), each.getRight());

		return result;
	}

	/**
	 * Collect the pairs in this {@code BiSequence} into a {@link Map}, using the given {@code merger}
	 * {@link BiFunction} to merge values in the map, according to {@link Map#merge(Object, Object, BiFunction)}.
	 */
	default Map<L, R> toMergedMap(BiFunction<? super R, ? super R, ? extends R> merger) {
		return toMergedMap(HashMap::new, merger);
	}

	/**
	 * Collect the pairs in this {@code BiSequence} into a {@link Map} of the type determined by the given
	 * constructor. The given {@code merger} {@link BiFunction} is used to merge values in the map, according to
	 * {@link Map#merge(Object, Object, BiFunction)}.
	 */
	default <M extends Map<L, R>> M toMergedMap(Supplier<? extends M> constructor,
	                                            BiFunction<? super R, ? super R, ? extends R> merger) {
		requireNonNull(constructor, "constructor");
		requireNonNull(merger, "merger");

		M result = constructor.get();
		for (Pair<L, R> each : this)
			result.merge(each.getLeft(), each.getRight(), merger);

		return result;
	}

	/**
	 * Performs a "group by" operation on the pairs in this sequence, grouping right values according to their left
	 * value and returning the results in a {@link Map}.
	 *
	 * @since 2.3
	 */
	default Map<L, List<R>> toGroupedMap() {
		return toGroupedMap(HashMap::new);
	}

	/**
	 * Performs a "group by" operation on the pairs in this sequence, grouping right values according to their left
	 * value and returning the results in a {@link Map} whose type is determined by the given {@code constructor}.
	 *
	 * @since 2.3
	 */
	default <M extends Map<L, List<R>>> M toGroupedMap(Supplier<? extends M> constructor) {
		requireNonNull(constructor, "constructor");

		return toGroupedMap(constructor, ArrayList::new);
	}

	/**
	 * Performs a "group by" operation on the pairs in this sequence, grouping right values according to their left
	 * value and returning the results in a {@link Map} whose type is determined by the given {@code constructor},
	 * using the given {@code groupConstructor} to create the target {@link Collection} of the grouped values.
	 *
	 * @since 2.3
	 */
	default <M extends Map<L, C>, C extends Collection<R>> M toGroupedMap(
			Supplier<? extends M> mapConstructor, Supplier<C> groupConstructor) {
		requireNonNull(mapConstructor, "mapConstructor");
		requireNonNull(groupConstructor, "groupConstructor");

		return toGroupedMap(mapConstructor, Collectors.toCollection(groupConstructor));
	}

	/**
	 * Performs a "group by" operation on the pairs in this sequence, grouping right values according to their left
	 * value and returning the results in a {@link Map} whose type is determined by the given {@code constructor},
	 * using the given group {@link Collector} to collect the grouped values.
	 *
	 * @since 2.3
	 */
	default <M extends Map<L, C>, C, A> M toGroupedMap(
			Supplier<? extends M> mapConstructor, Collector<? super R, A, C> groupCollector) {
		requireNonNull(mapConstructor, "mapConstructor");
		requireNonNull(groupCollector, "groupCollector");

		Supplier<? extends A> groupConstructor = groupCollector.supplier();
		BiConsumer<? super A, ? super R> groupAccumulator = groupCollector.accumulator();

		@SuppressWarnings("unchecked")
		Map<L, A> result = (Map<L, A>) mapConstructor.get();
		for (Pair<L, R> pair : this)
			groupAccumulator.accept(result.computeIfAbsent(pair.getLeft(), k -> groupConstructor.get()),
			                        pair.getRight());

		if (!groupCollector.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
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
	 * Collect the pairs in this {@code BiSequence} into a {@link SortedMap}.
	 */
	default SortedMap<L, R> toSortedMap() {
		return toMap(TreeMap::new);
	}

	/**
	 * Collect this {@code BiSequence} into a {@link Collection} of the type determined by the given constructor.
	 */
	default <C extends Collection<Pair<L, R>>> C toCollection(Supplier<? extends C> constructor) {
		requireNonNull(constructor, "constructor");

		return collectInto(constructor.get());
	}

	/**
	 * Collect this {@code BiSequence} into an arbitrary container using the given constructor and adder.
	 */
	default <C> C collect(Supplier<? extends C> constructor, BiConsumer<? super C, ? super Pair<L, R>> adder) {
		requireNonNull(constructor, "constructor");
		requireNonNull(adder, "adder");

		return collectInto(constructor.get(), adder);
	}

	/**
	 * Collect this {@code BiSequence} into an arbitrary container using the given {@link Collector}.
	 */
	default <S, C> S collect(Collector<Pair<L, R>, C, S> collector) {
		requireNonNull(collector, "collector");

		C intermediary = collect(collector.supplier(), collector.accumulator());
		return collector.finisher().apply(intermediary);
	}

	/**
	 * Collect this {@code BiSequence} into the given {@link Collection}.
	 */
	default <U extends Collection<Pair<L, R>>> U collectInto(U collection) {
		requireNonNull(collection, "collection");

		for (Pair<L, R> t : this)
			collection.add(t);
		return collection;
	}

	/**
	 * Collect this {@code Sequence} into the given container, using the given adder.
	 */
	default <C> C collectInto(C result, BiConsumer<? super C, ? super Pair<L, R>> adder) {
		requireNonNull(result, "result");
		requireNonNull(adder, "adder");

		for (Pair<L, R> t : this)
			adder.accept(result, t);
		return result;
	}

	/**
	 * @return a {@link List} view of this {@code BiSequence}, which is updated in real time as the backing store of
	 * the
	 * {@code BiSequence} changes. The list does not implement {@link RandomAccess} and is best accessed in sequence.
	 * The list does not support {@link List#add}, only removal through {@link Iterator#remove}.
	 *
	 * @since 2.2
	 */
	default List<Pair<L, R>> asList() {
		return Iterables.asList(this);
	}

	/**
	 * Join this {@code BiSequence} into a string separated by the given delimiter.
	 */
	default String join(String delimiter) {
		requireNonNull(delimiter, "delimiter");

		return join("", delimiter, "");
	}

	/**
	 * Join this {@code BiSequence} into a string separated by the given delimiter, with the given prefix and suffix.
	 */
	default String join(String prefix, String delimiter, String suffix) {
		requireNonNull(prefix, "prefix");
		requireNonNull(delimiter, "delimiter");
		requireNonNull(suffix, "suffix");

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

	/**
	 * Reduce this {@code BiSequence} into a single element by iteratively applying the given binary operator to
	 * the current result and each pair in this sequence.
	 */
	default Optional<Pair<L, R>> reduce(BinaryOperator<Pair<L, R>> operator) {
		requireNonNull(operator, "operator");

		return Iterators.reduce(iterator(), operator);
	}

	/**
	 * Reduce this {@code BiSequence} into a single element by iteratively applying the given function to
	 * the current result and each element in this sequence. The function is passed the left and right components of
	 * the result pair, followed by the left and right components of the current pair, respectively.
	 */
	default Optional<Pair<L, R>> reduce(QuaternaryFunction<L, R, L, R, Pair<L, R>> operator) {
		requireNonNull(operator, "operator");

		return reduce(asPairBinaryOperator(operator));
	}

	/**
	 * Reduce this {@code BiSequence} into a single element by iteratively applying the given binary operator to
	 * the current result and each pair in this sequence, starting with the given identity as the initial result.
	 */
	default Pair<L, R> reduce(Pair<L, R> identity, BinaryOperator<Pair<L, R>> operator) {
		requireNonNull(operator, "operator");

		return Iterators.reduce(iterator(), identity, operator);
	}

	/**
	 * Reduce this {@code BiSequence} into a single element by iteratively applying the given binary operator to
	 * the current result and each entry in this sequence, starting with the given identity as the initial result.
	 * The function is passed the left and right components of the result, followed by the left and right components of
	 * the current entry, respectively.
	 */
	default Pair<L, R> reduce(L left, R right, QuaternaryFunction<L, R, L, R, Pair<L, R>> operator) {
		requireNonNull(operator, "operator");

		return reduce(Pair.of(left, right), asPairBinaryOperator(operator));
	}

	/**
	 * @return the first pair of this {@code BiSequence} or an empty {@link Optional} if there are no pairs in the
	 * {@code BiSequence}.
	 */
	default Optional<Pair<L, R>> first() {
		return at(0);
	}

	/**
	 * @return the last pair of this {@code BiSequence} or an empty {@link Optional} if there are no pairs in the
	 * {@code BiSequence}.
	 */
	default Optional<Pair<L, R>> last() {
		return Iterators.last(iterator());
	}

	/**
	 * @return the element at the given index, or an empty {@link Optional} if the {@code BiSequence} is smaller than
	 * the index.
	 *
	 * @since 1.2
	 */
	default Optional<Pair<L, R>> at(int index) {
		requireAtLeastZero(index, "index");

		return Iterators.get(iterator(), index);
	}

	/**
	 * @return the first pair of this {@code BiSequence} that matches the given predicate, or an empty {@link Optional}
	 * if there are no matching pairs in the {@code BiSequence}.
	 *
	 * @since 1.2
	 */
	default Optional<Pair<L, R>> first(Predicate<? super Pair<L, R>> predicate) {
		return at(0, predicate);
	}

	/**
	 * @return the last pair of this {@code BiSequence} the matches the given predicate, or an empty
	 * {@link Optional} if there are no matching pairs in the {@code BiSequence}.
	 *
	 * @since 1.2
	 */
	default Optional<Pair<L, R>> last(Predicate<? super Pair<L, R>> predicate) {
		return filter(predicate).last();
	}

	/**
	 * @return the pair at the given index out of the pairs matching the given predicate, or an empty {@link Optional}
	 * if the {@code BiSequence} of matching pairs is smaller than the index.
	 *
	 * @since 1.2
	 */
	default Optional<Pair<L, R>> at(int index, Predicate<? super Pair<L, R>> predicate) {
		requireAtLeastZero(index, "index");
		requireNonNull(predicate, "predicate");

		return filter(predicate).at(index);
	}

	/**
	 * @return the first pair of this {@code BiSequence} that matches the given predicate, or an empty {@link Optional}
	 * if there are no matching pairs in the {@code BiSequence}.
	 *
	 * @since 1.2
	 */
	default Optional<Pair<L, R>> first(BiPredicate<? super L, ? super R> predicate) {
		return at(0, predicate);
	}

	/**
	 * @return the last pair of this {@code BiSequence} the matches the given predicate, or an empty
	 * {@link Optional} if there are no matching pairs in the {@code BiSequence}.
	 *
	 * @since 1.2
	 */
	default Optional<Pair<L, R>> last(BiPredicate<? super L, ? super R> predicate) {
		return filter(predicate).last();
	}

	/**
	 * @return the pair at the given index out of the pairs matching the given predicate, or an empty {@link Optional}
	 * if the {@code BiSequence} of matching pairs is smaller than the index.
	 *
	 * @since 1.2
	 */
	default Optional<Pair<L, R>> at(int index, BiPredicate<? super L, ? super R> predicate) {
		requireAtLeastZero(index, "index");
		requireNonNull(predicate, "predicate");

		return filter(predicate).at(index);
	}

	/**
	 * Window the elements of this {@code BiSequence} into a {@link Sequence} of {@code BiSequence}s of pairs, each
	 * with the size of the given window. The first item in each sequence is the second item in the previous sequence.
	 * The final sequence may be shorter than the window. This method is equivalent to {@code window(window, 1)}.
	 */
	default Sequence<BiSequence<L, R>> window(int window) {
		requireAtLeastOne(window, "window");

		return window(window, 1);
	}

	/**
	 * Window the elements of this {@code BiSequence} into a sequence of {@code BiSequence}s of elements, each with the
	 * size of the given window, stepping {@code step} elements between each window. If the given step is less than the
	 * window size, the windows will overlap each other. If the step is larger than the window size, elements will be
	 * skipped in between windows.
	 */
	default Sequence<BiSequence<L, R>> window(int window, int step) {
		requireAtLeastOne(window, "window");
		requireAtLeastOne(step, "step");

		return () -> new WindowingIterator<Pair<L, R>, BiSequence<L, R>>(iterator(), window, step) {
			@Override
			protected BiSequence<L, R> toSequence(List<Pair<L, R>> list) {
				return BiSequence.from(list);
			}
		};
	}

	/**
	 * Batch the elements of this {@code BiSequence} into a sequence of {@code BiSequence}s of distinct elements, each
	 * with the given batch size. This method is equivalent to {@code window(size, size)}.
	 */
	default Sequence<BiSequence<L, R>> batch(int size) {
		requireAtLeastOne(size, "size");

		return window(size, size);
	}

	/**
	 * Batch the elements of this {@code BiSequence} into a sequence of {@link BiSequence}s of distinct elements, where
	 * the given predicate determines where to split the lists of partitioned elements. The predicate is given the
	 * current and next item in the iteration, and if it returns true a partition is created between the elements.
	 */
	default Sequence<BiSequence<L, R>> batch(BiPredicate<? super Pair<L, R>, ? super Pair<L, R>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new PredicatePartitioningIterator<Pair<L, R>, BiSequence<L, R>>(iterator(), predicate) {
			@Override
			protected BiSequence<L, R> toSequence(List<Pair<L, R>> list) {
				return BiSequence.from(list);
			}
		};
	}

	/**
	 * Batch the elements of this {@code BiSequence} into a sequence of {@link BiSequence}s of distinct elements,
	 * where the given predicate determines where to split the lists of partitioned elements. The predicate is given
	 * the left and right values of the current and next items in the iteration, and if it returns true a partition is
	 * created between the elements.
	 */
	default Sequence<BiSequence<L, R>> batch(
			QuaternaryPredicate<? super L, ? super R, ? super L, ? super R> predicate) {
		requireNonNull(predicate, "predicate");

		return batch((p1, p2) -> predicate.test(p1.getLeft(), p1.getRight(), p2.getLeft(), p2.getRight()));
	}

	/**
	 * Split the elements of this {@code BiSequence} into a sequence of {@code BiSequence}s of distinct elements,
	 * around the given element. The elements around which the sequence is split are not included in the result.
	 *
	 * @since 1.1
	 */
	default Sequence<BiSequence<L, R>> split(Pair<L, R> element) {
		return () -> new SplittingIterator<Pair<L, R>, BiSequence<L, R>>(iterator(), element) {
			@Override
			protected BiSequence<L, R> toSequence(List<Pair<L, R>> list) {
				return BiSequence.from(list);
			}
		};
	}

	/**
	 * Split the elements of this {@code BiSequence} into a sequence of {@code BiSequence}s of distinct elements,
	 * where the given predicate determines which elements to split the partitioned elements around. The elements
	 * matching the predicate are not included in the result.
	 *
	 * @since 1.1
	 */
	default Sequence<BiSequence<L, R>> split(Predicate<? super Pair<L, R>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new SplittingIterator<Pair<L, R>, BiSequence<L, R>>(iterator(), predicate) {
			@Override
			protected BiSequence<L, R> toSequence(List<Pair<L, R>> list) {
				return BiSequence.from(list);
			}
		};
	}

	/**
	 * Split the elements of this {@code BiSequence} into a sequence of {@code BiSequence}s of distinct elements,
	 * where the given predicate determines which elements to split the partitioned elements around. The elements
	 * matching the predicate are not included in the result.
	 *
	 * @since 1.1
	 */
	default Sequence<BiSequence<L, R>> split(BiPredicate<? super L, ? super R> predicate) {
		requireNonNull(predicate, "predicate");

		return split(asPairPredicate(predicate));
	}

	/**
	 * Skip x number of steps in between each invocation of the iterator of this {@code BiSequence}.
	 */
	default BiSequence<L, R> step(int step) {
		requireAtLeastOne(step, "step");

		return () -> new SteppingIterator<>(iterator(), step);
	}

	/**
	 * @return a {@code BiSequence} where each item in this {@code BiSequence} occurs only once, the first time it is
	 * encountered.
	 */
	default BiSequence<L, R> distinct() {
		return () -> new DistinctIterator<>(iterator());
	}

	/**
	 * @return this {@code BiSequence} sorted according to the natural order.
	 */
	default BiSequence<L, R> sorted() {
		return sorted(Pair.comparator());
	}

	/**
	 * @return this {@code BiSequence} sorted according to the given {@link Comparator}.
	 */
	default BiSequence<L, R> sorted(Comparator<? super Pair<? extends L, ? extends R>> comparator) {
		return () -> Iterators.unmodifiable(Lists.sort(toList(), comparator));
	}

	/**
	 * @return the minimal pair in this {@code BiSequence}.
	 *
	 * @since 1.2
	 */
	default Optional<Pair<L, R>> min() {
		return min(Comparator.naturalOrder());
	}

	/**
	 * @return the maximum pair in this {@code BiSequence}.
	 *
	 * @since 1.2
	 */
	default Optional<Pair<L, R>> max() {
		return max(Comparator.naturalOrder());
	}

	/**
	 * @return the minimal element in this {@code BiSequence} according to the given {@link Comparator}.
	 */
	default Optional<Pair<L, R>> min(Comparator<? super Pair<L, R>> comparator) {
		requireNonNull(comparator, "comparator");

		return reduce(minBy(comparator));
	}

	/**
	 * @return the maximum element in this {@code BiSequence} according to the given {@link Comparator}.
	 */
	default Optional<Pair<L, R>> max(Comparator<? super Pair<L, R>> comparator) {
		requireNonNull(comparator, "comparator");

		return reduce(maxBy(comparator));
	}

	/**
	 * @return true if all elements in this {@code BiSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean all(BiPredicate<? super L, ? super R> predicate) {
		requireNonNull(predicate, "predicate");

		return Iterables.all(this, asPairPredicate(predicate));
	}

	/**
	 * @return true if no elements in this {@code BiSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean none(BiPredicate<? super L, ? super R> predicate) {
		requireNonNull(predicate, "predicate");

		return Iterables.none(this, asPairPredicate(predicate));
	}

	/**
	 * @return true if any element in this {@code BiSequence} satisfies the given predicate, false otherwise.
	 */
	default boolean any(BiPredicate<? super L, ? super R> predicate) {
		requireNonNull(predicate, "predicate");

		return Iterables.any(this, asPairPredicate(predicate));
	}

	/**
	 * Allow the given {@link BiConsumer} to see the components of each pair in this {@code BiSequence} as it is
	 * traversed.
	 */
	default BiSequence<L, R> peek(BiConsumer<? super L, ? super R> action) {
		requireNonNull(action, "action");

		return peek(asPairConsumer(action));
	}

	/**
	 * Allow the given {@link Consumer} to see each pair in this {@code BiSequence} as it is traversed.
	 *
	 * @since 1.2.2
	 */
	default BiSequence<L, R> peek(Consumer<? super Pair<L, R>> action) {
		requireNonNull(action, "action");

		return () -> new PeekingIterator<>(iterator(), action);
	}

	/**
	 * Allow the given {@link ObjObjIntConsumer} to see the components of each pair with their index as this
	 * {@code BiSequence} is traversed.
	 *
	 * @since 1.2.2
	 */
	default BiSequence<L, R> peekIndexed(ObjObjIntConsumer<? super L, ? super R> action) {
		requireNonNull(action, "action");

		return peekIndexed((p, x) -> action.accept(p.getLeft(), p.getRight(), x));
	}

	/**
	 * Allow the given {@link ObjLongConsumer} to see each pair with its index as this {@code BiSequence} is
	 * traversed.
	 *
	 * @since 1.2.2
	 */
	default BiSequence<L, R> peekIndexed(ObjIntConsumer<? super Pair<L, R>> action) {
		requireNonNull(action, "action");

		return () -> new IndexPeekingIterator<>(iterator(), action);
	}

	/**
	 * Append the elements of the given {@link Iterator} to the end of this {@code BiSequence}.
	 * <p>
	 * The appended elements will only be available on the first traversal of the resulting {@code Sequence}.
	 */
	default BiSequence<L, R> append(Iterator<Pair<L, R>> iterator) {
		requireNonNull(iterator, "iterator");

		return append(Iterables.once(iterator));
	}

	/**
	 * Append the elements of the given {@link Iterable} to the end of this {@code BiSequence}.
	 */
	default BiSequence<L, R> append(Iterable<Pair<L, R>> iterable) {
		requireNonNull(iterable, "iterable");

		return ChainingIterable.concat(this, iterable)::iterator;
	}

	/**
	 * Append the given elements to the end of this {@code BiSequence}.
	 */
	@SuppressWarnings("unchecked")
	default BiSequence<L, R> append(Pair<L, R>... pairs) {
		requireNonNull(pairs, "pairs");

		return append(Iterables.of(pairs));
	}

	/**
	 * Append the given pair to the end of this {@code BiSequence}.
	 */
	@SuppressWarnings("unchecked")
	default BiSequence<L, R> appendPair(L left, R right) {
		return append(Pair.of(left, right));
	}

	/**
	 * Append the elements of the given {@link Stream} to the end of this {@code BiSequence}.
	 * <p>
	 * The appended elements will only be available on the first traversal of the resulting {@code BiSequence}.
	 */
	default BiSequence<L, R> append(Stream<Pair<L, R>> stream) {
		requireNonNull(stream, "stream");

		return append(stream.iterator());
	}

	/**
	 * Convert this {@code BiSequence} to a {@link Sequence} of {@link Pair}s.
	 */
	default Sequence<Pair<L, R>> toSequence() {
		return Sequence.from(this);
	}

	/**
	 * Convert this {@code BiSequence} to a {@link Sequence} where each item is generated by the given mapper.
	 */
	default <T> Sequence<T> toSequence(BiFunction<? super L, ? super R, ? extends T> mapper) {
		requireNonNull(mapper, "mapper");

		return toSequence(asPairFunction(mapper));
	}

	/**
	 * Convert this {@code BiSequence} to a {@link Sequence} where each item is generated by the given mapper.
	 */
	default <T> Sequence<T> toSequence(Function<? super Pair<L, R>, ? extends T> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> new MappingIterator<>(iterator(), mapper);
	}

	/**
	 * Convert this {@code BiSequence} to a {@link EntrySequence} of {@link Map.Entry} elements.
	 */
	@SuppressWarnings("unchecked")
	default EntrySequence<L, R> toEntrySequence() {
		return EntrySequence.from((Iterable) this);
	}

	/**
	 * Convert this {@code BiSequence} to a {@link CharSeq} using the given mapper function to map each pair to a
	 * {@code char}.
	 *
	 * @see #toSequence(BiFunction)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntBiFunction)
	 * @see #toLongs(ToLongBiFunction)
	 * @see #toDoubles(ToDoubleBiFunction)
	 * @see #map(BiFunction)
	 * @see #flatten(BiFunction)
	 * @since 1.1.1
	 */
	default CharSeq toChars(ToCharBiFunction<? super L, ? super R> mapper) {
		requireNonNull(mapper, "mapper");

		return toChars(p -> mapper.applyAsChar(p.getLeft(), p.getRight()));
	}

	/**
	 * Convert this {@code BiSequence} to an {@link IntSequence} using the given mapper function to map each pair
	 * to an {@code int}.
	 *
	 * @see #toSequence(BiFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toChars(ToCharBiFunction)
	 * @see #toLongs(ToLongBiFunction)
	 * @see #toDoubles(ToDoubleBiFunction)
	 * @see #map(BiFunction)
	 * @see #flatten(BiFunction)
	 * @since 1.1.1
	 */
	default IntSequence toInts(ToIntBiFunction<? super L, ? super R> mapper) {
		requireNonNull(mapper, "mapper");

		return toInts(p -> mapper.applyAsInt(p.getLeft(), p.getRight()));
	}

	/**
	 * Convert this {@code BiSequence} to a {@link LongSequence} using the given mapper function to map each pair to a
	 * {@code long}.
	 *
	 * @see #toSequence(BiFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toChars(ToCharBiFunction)
	 * @see #toInts(ToIntBiFunction)
	 * @see #toDoubles(ToDoubleBiFunction)
	 * @see #map(BiFunction)
	 * @see #flatten(BiFunction)
	 * @since 1.1.1
	 */
	default LongSequence toLongs(ToLongBiFunction<? super L, ? super R> mapper) {
		requireNonNull(mapper, "mapper");

		return toLongs(p -> mapper.applyAsLong(p.getLeft(), p.getRight()));
	}

	/**
	 * Convert this {@code BiSequence} to a {@link DoubleSequence} using the given mapper function to map each pair
	 * to a {@code double}.
	 *
	 * @see #toSequence(BiFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #toChars(ToCharBiFunction)
	 * @see #toInts(ToIntBiFunction)
	 * @see #toLongs(ToLongBiFunction)
	 * @see #map(BiFunction)
	 * @see #flatten(BiFunction)
	 * @since 1.1.1
	 */
	default DoubleSequence toDoubles(ToDoubleBiFunction<? super L, ? super R> mapper) {
		requireNonNull(mapper, "mapper");

		return toDoubles(p -> mapper.applyAsDouble(p.getLeft(), p.getRight()));
	}

	/**
	 * Convert this {@code BiSequence} to a {@link CharSeq} using the given mapper function to map each pair to a
	 * {@code char}.
	 *
	 * @see #toSequence(Function)
	 * @see #toChars(ToCharBiFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @since 1.1.1
	 */
	default CharSeq toChars(ToCharFunction<? super Pair<L, R>> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> CharIterator.from(iterator(), mapper);
	}

	/**
	 * Convert this {@code BiSequence} to an {@link IntSequence} using the given mapper function to map each pair
	 * to an {@code int}.
	 *
	 * @see #toSequence(Function)
	 * @see #toInts(ToIntBiFunction)
	 * @see #toChars(ToCharFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @since 1.1.1
	 */
	default IntSequence toInts(ToIntFunction<? super Pair<L, R>> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> IntIterator.from(iterator(), mapper);
	}

	/**
	 * Convert this {@code BiSequence} to a {@link LongSequence} using the given mapper function to map each pair to a
	 * {@code long}.
	 *
	 * @see #toSequence(Function)
	 * @see #toLongs(ToLongBiFunction)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @since 1.1.1
	 */
	default LongSequence toLongs(ToLongFunction<? super Pair<L, R>> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> LongIterator.from(iterator(), mapper);
	}

	/**
	 * Convert this {@code BiSequence} to a {@link DoubleSequence} using the given mapper function to map each pair
	 * to a {@code double}.
	 *
	 * @see #toSequence(Function)
	 * @see #toDoubles(ToDoubleBiFunction)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @since 1.1.1
	 */
	default DoubleSequence toDoubles(ToDoubleFunction<? super Pair<L, R>> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> DoubleIterator.from(iterator(), mapper);
	}

	/**
	 * Repeat this {@code BiSequence} forever, producing a sequence that never terminates unless the original sequence
	 * is empty in which case the resulting sequence is also empty.
	 */
	default BiSequence<L, R> repeat() {
		return () -> new RepeatingIterator<>(this, -1);
	}

	/**
	 * Repeat this {@code BiSequence} the given number of times.
	 */
	default BiSequence<L, R> repeat(int times) {
		requireAtLeastZero(times, "times");

		return () -> new RepeatingIterator<>(this, times);
	}

	/**
	 * @return a {@code BiSequence} which iterates over this {@code BiSequence} in reverse order.
	 */
	default BiSequence<L, R> reverse() {
		return () -> new ReverseIterator<>(iterator());
	}

	/**
	 * @return a {@code BiSequence} which iterates over this {@code BiSequence} in random order.
	 */
	default BiSequence<L, R> shuffle() {
		return () -> Iterators.unmodifiable(Lists.shuffle(toList()));
	}

	/**
	 * @return a {@code BiSequence} which iterates over this {@code BiSequence} in random order as determined by the
	 * given random generator.
	 */
	default BiSequence<L, R> shuffle(Random random) {
		requireNonNull(random, "random");

		return () -> Iterators.unmodifiable(Lists.shuffle(toList(), random));
	}

	/**
	 * @return a {@code BiSequence} which iterates over this {@code BiSequence} in random order as determined by the
	 * given random generator. A new instance of {@link Random} is created by the given supplier at the start of each
	 * iteration.
	 *
	 * @since 1.2
	 */
	default BiSequence<L, R> shuffle(Supplier<? extends Random> randomSupplier) {
		requireNonNull(randomSupplier, "randomSupplier");

		return () -> {
			Random random = requireNonNull(randomSupplier.get(), "randomSupplier.get()");
			return Iterators.unmodifiable(Lists.shuffle(toList(), random));
		};
	}

	/**
	 * @return true if this {@code BiSequence} contains the given pair, false otherwise.
	 *
	 * @since 1.2
	 */
	default boolean contains(Pair<L, R> pair) {
		return Iterables.contains(this, pair);
	}

	/**
	 * @return true if this {@code BiSequence} contains the given pair, false otherwise.
	 *
	 * @since 1.2
	 */
	default boolean contains(L left, R right) {
		for (Pair<L, R> each : this)
			if (Objects.equals(left, each.getLeft()) && Objects.equals(right, each.getRight()))
				return true;

		return false;
	}

	/**
	 * Perform the given action for each element in this {@code BiSequence}.
	 *
	 * @since 1.2
	 */
	default void forEach(BiConsumer<? super L, ? super R> action) {
		requireNonNull(action, "action");

		forEach(asPairConsumer(action));
	}
}
