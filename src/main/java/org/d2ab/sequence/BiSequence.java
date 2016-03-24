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
import org.d2ab.function.QuaternaryPredicate;
import org.d2ab.iterable.ChainingIterable;
import org.d2ab.iterable.Iterables;
import org.d2ab.iterator.*;
import org.d2ab.util.Pair;

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
		return from(emptyIterator());
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
		return from(asList(items));
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
	static <T, U> BiSequence<T, U> ofPairs(Object... os) {
		if (os.length % 2 != 0)
			throw new IllegalArgumentException("Expected an even set of objects, but got: " + os.length);

		List<Pair<T, U>> pairs = new ArrayList<>();
		for (int i = 0; i < os.length; i += 2)
			pairs.add(Pair.of((T) os[i], (U) os[i + 1]));
		return from(pairs);
	}

	/**
	 * Create a {@code BiSequence} from an {@link Iterable} of pairs.
	 *
	 * @see #of(Pair)
	 * @see #of(Pair...)
	 * @see #from(Iterable...)
	 */
	static <L, R> BiSequence<L, R> from(Iterable<Pair<L, R>> iterable) {
		return iterable::iterator;
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
	static <L, R> BiSequence<L, R> from(Iterable<? extends Pair<L, R>>... iterables) {
		return () -> new ChainingIterator<>(iterables);
	}

	/**
	 * Create a {@code BiSequence} from an {@link Iterator} of pairs. Note that {@code BiSequence}s created from {@link
	 * Iterator}s cannot be passed over more than once. Further attempts will register the {@code BiSequence} as empty.
	 *
	 * @see #of(Pair)
	 * @see #of(Pair...)
	 * @see #from(Iterable)
	 */
	static <L, R> BiSequence<L, R> from(Iterator<Pair<L, R>> iterator) {
		return () -> iterator;
	}

	/**
	 * Create a {@code BiSequence} from a {@link Stream} of pairs. Note that {@code BiSequence}s created from
	 * {@link Stream}s cannot be passed over more than once. Further attempts will cause an
	 * {@link IllegalStateException} when the {@link Stream} is requested again.
	 *
	 * @see #of(Pair)
	 * @see #of(Pair...)
	 * @see #from(Iterable)
	 * @see #from(Iterator)
	 */
	static <L, R> BiSequence<L, R> from(Stream<Pair<L, R>> stream) {
		return stream::iterator;
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
		return Sequence.from(map.entrySet()).map(Pair::from)::iterator;
	}

	/**
	 * @return an infinite {@code BiSequence} generated by repeatedly calling the given supplier. The returned
	 * {@code BiSequence} never terminates naturally.
	 *
	 * @see #recurse(Pair, UnaryOperator)
	 * @see #endingAt(Pair)
	 * @see #until(Pair)
	 */
	static <L, R> BiSequence<L, R> generate(Supplier<Pair<L, R>> supplier) {
		return () -> (InfiniteIterator<Pair<L, R>>) supplier::get;
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
	                                       BiFunction<? super L, ? super R, ? extends Pair<L, R>> op) {
		return recurse(Pair.of(leftSeed, rightSeed), Pair.asUnaryOperator(op));
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
	static <L, R> BiSequence<L, R> recurse(Pair<L, R> seed, UnaryOperator<Pair<L, R>> op) {
		return () -> new RecursiveIterator<>(seed, op);
	}

	/**
	 * Returns a {@code BiSequence} produced by recursively applying the given mapper {@code f} and incrementer
	 * {@code g} operations to the given seeds, the first element being {@code f(leftSeed, rightSeed)}, the second
	 * being {@code f(g(f(leftSeed, rightSeed)))}, the third {@code f(g(f(g(f(leftSeed, rightSeed)))))} and so on.
	 * The returned {@code BiSequence} never terminates naturally.
	 *
	 * @param f a mapper function for producing elements that are to be included in the sequence, the first being
	 *          f(leftSeed, rightSeed)
	 * @param g an incrementer function for producing the next unmapped element to be included in the sequence,
	 *          applied to the first mapped element f(leftSeed, rightSeed) to produce the second unmapped value
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
		return recurse(f.apply(leftSeed, rightSeed), Pair.asUnaryOperator(f, g));
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
		return map(Pair.asFunction(mapper));
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
		return map(p -> p.map(leftMapper, rightMapper));
	}

	/**
	 * Skip a set number of steps in this {@code BiSequence}.
	 */
	default BiSequence<L, R> skip(int skip) {
		return () -> new SkippingIterator<>(iterator(), skip);
	}

	/**
	 * Limit the maximum number of results returned by this {@code BiSequence}.
	 */
	default BiSequence<L, R> limit(int limit) {
		return () -> new LimitingIterator<>(iterator(), limit);
	}

	/**
	 * Filter the elements in this {@code BiSequence}, keeping only the elements that match the given
	 * {@link BiPredicate}.
	 */
	default BiSequence<L, R> filter(BiPredicate<? super L, ? super R> predicate) {
		return filter(Pair.asPredicate(predicate));
	}

	/**
	 * Filter the elements in this {@code BiSequence}, keeping only the pairs that match the given
	 * {@link Predicate}.
	 */
	default BiSequence<L, R> filter(Predicate<? super Pair<L, R>> predicate) {
		return () -> new FilteringIterator<>(iterator(), predicate);
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
		return flatten(Pair.asFunction(mapper));
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
			Function<? super Pair<L, R>, ? extends Iterable<Pair<LL, RR>>> function) {
		ChainingIterable<Pair<LL, RR>> result = new ChainingIterable<>();
		toSequence(function).forEach(result::append);
		return result::iterator;
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
	default BiSequence<L, R> until(BiPredicate<? super L, ? super R> terminal) {
		return until(Pair.asPredicate(terminal));
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
	default BiSequence<L, R> endingAt(BiPredicate<? super L, ? super R> terminal) {
		return endingAt(Pair.asPredicate(terminal));
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
	default BiSequence<L, R> until(Predicate<? super Pair<L, R>> terminal) {
		return () -> new ExclusiveTerminalIterator<>(iterator(), terminal);
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
	default BiSequence<L, R> endingAt(Predicate<? super Pair<L, R>> terminal) {
		return () -> new InclusiveTerminalIterator<>(iterator(), terminal);
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
		List list = toList();
		@SuppressWarnings("unchecked")
		Pair<L, R>[] array = (Pair<L, R>[]) list.toArray(constructor.apply(list.size()));
		return array;
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
		return toCollection(constructor);
	}

	/**
	 * Collect the pairs in this {@code BiSequence} into a {@link SortedSet}.
	 */
	default SortedSet<Pair<L, R>> toSortedSet() {
		return toSet(TreeSet::new);
	}

	/**
	 * Collect the pairs in this {@code BiSequence} into a {@link Map}.
	 */
	default Map<L, R> toMap() {
		return toMap(HashMap::new);
	}

	/**
	 * Collect the pairs in this {@code BiSequence} into a {@link Map} of the type determined by the given
	 * constructor.
	 */
	default <M extends Map<L, R>> M toMap(Supplier<? extends M> constructor) {
		return collect(constructor, (result, pair) -> pair.put(result));
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
		return collect(constructor, Collection::add);
	}

	/**
	 * Collect this {@code BiSequence} into an arbitrary container using the given constructor and adder.
	 */
	default <C> C collect(Supplier<? extends C> constructor, BiConsumer<? super C, ? super Pair<L, R>> adder) {
		return collectInto(constructor.get(), adder);
	}

	/**
	 * Collect this {@code BiSequence} into an arbitrary container using the given {@link Collector}.
	 */
	default <S, C> S collect(Collector<Pair<L, R>, C, S> collector) {
		C intermediary = collect(collector.supplier(), collector.accumulator());
		return collector.finisher().apply(intermediary);
	}

	/**
	 * Collect this {@code BiSequence} into the given {@link Collection}.
	 */
	default <U extends Collection<Pair<L, R>>> U collectInto(U collection) {
		return collectInto(collection, Collection::add);
	}

	/**
	 * Collect this {@code Sequence} into the given container, using the given adder.
	 */
	default <C> C collectInto(C result, BiConsumer<? super C, ? super Pair<L, R>> adder) {
		forEach(pair -> adder.accept(result, pair));
		return result;
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

	default Sequence<BiSequence<L, R>> window(int window) {
		return window(window, 1);
	}

	default Sequence<BiSequence<L, R>> window(int window, int step) {
		return () -> new WindowingIterator<Pair<L, R>, BiSequence<L, R>>(iterator(), window, step) {
			@Override
			protected BiSequence<L, R> toSequence(List<Pair<L, R>> list) {
				return BiSequence.from(list);
			}
		};
	}

	default Sequence<BiSequence<L, R>> batch(int size) {
		return window(size, size);
	}

	/**
	 * Batch the elements of this {@code BiSequence} into a sequence of {@link BiSequence}s of distinct elements, where
	 * the given predicate determines where to split the lists of partitioned elements. The predicate is given the
	 * current and next item in the iteration, and if it returns true a partition is created between the elements.
	 */
	default Sequence<BiSequence<L, R>> batch(BiPredicate<? super Pair<L, R>, ? super Pair<L, R>> predicate) {
		return () -> new PredicatePartitioningIterator<Pair<L, R>, BiSequence<L, R>>(iterator(), predicate) {
			@Override
			protected BiSequence<L, R> toSequence(List<Pair<L, R>> list) {
				return BiSequence.from(list);
			}
		};
	}

	/**
	 * Batch the elements of this {@code EntrySequence} into a sequence of {@link EntrySequence}s of distinct elements,
	 * where the given predicate determines where to split the lists of partitioned elements. The predicate is given
	 * the left and right values of the current and next items in the iteration, and if it returns true a partition is
	 * created between the elements.
	 */
	default Sequence<BiSequence<L, R>> batch(
			QuaternaryPredicate<? super L, ? super R, ? super L, ? super R> predicate) {
		return batch((p1, p2) -> predicate.test(p1.getLeft(), p1.getRight(), p2.getLeft(), p2.getRight()));
	}

	default BiSequence<L, R> step(int step) {
		return () -> new SteppingIterator<>(iterator(), step);
	}

	default BiSequence<L, R> distinct() {
		return () -> new DistinctIterator<>(iterator());
	}

	default BiSequence<L, R> sorted() {
		return () -> new SortingIterator<>(iterator());
	}

	default BiSequence<L, R> sorted(Comparator<? super Pair<? extends L, ? extends R>> comparator) {
		return () -> new SortingIterator<>(iterator(), comparator);
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
		return () -> new PeekingIterator<>(iterator(), consumer);
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
		return () -> new MappingIterator<>(iterator(), mapper);
	}

	default BiSequence<L, R> repeat() {
		return () -> new RepeatingIterator<>(this, -1);
	}

	default BiSequence<L, R> repeat(long times) {
		return () -> new RepeatingIterator<>(this, times);
	}

	default BiSequence<L, R> reverse() {
		return () -> new ReverseIterator<>(iterator());
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
