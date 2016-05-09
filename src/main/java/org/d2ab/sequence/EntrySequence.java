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

import org.d2ab.collection.Lists;
import org.d2ab.collection.Maps;
import org.d2ab.function.*;
import org.d2ab.function.chars.ToCharBiFunction;
import org.d2ab.function.chars.ToCharFunction;
import org.d2ab.iterable.ChainingIterable;
import org.d2ab.iterable.Iterables;
import org.d2ab.iterator.*;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.util.Arrayz;
import org.d2ab.util.Pair;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyIterator;

/**
 * An {@link Iterable} sequence of {@link Entry} elements with {@link Stream}-like operations for refining,
 * transforming and collating the list of {@link Entry} elements.
 */
@FunctionalInterface
public interface EntrySequence<K, V> extends Iterable<Entry<K, V>> {
	/**
	 * Create an empty {@code EntrySequence} with no items.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #ofEntry(Object, Object)
	 * @see #ofEntries(Object...)
	 * @see #from(Iterable)
	 */
	static <K, V> EntrySequence<K, V> empty() {
		return once(emptyIterator());
	}

	/**
	 * Create an {@code EntrySequence} with one {@link Entry}.
	 *
	 * @see #of(Entry...)
	 * @see #ofEntry(Object, Object)
	 * @see #ofEntries(Object...)
	 * @see #from(Iterable)
	 */
	static <K, V> EntrySequence<K, V> of(Entry<K, V> item) {
		return from(Collections.singleton(item));
	}

	/**
	 * Create an {@code EntrySequence} with the given {@link Entry} list.
	 *
	 * @see #of(Entry)
	 * @see #ofEntry(Object, Object)
	 * @see #ofEntries(Object...)
	 * @see #from(Iterable)
	 */
	@SafeVarargs
	static <K, V> EntrySequence<K, V> of(Entry<K, V>... items) {
		return from(asList(items));
	}

	/**
	 * Create an {@code EntrySequence} with one {@link Entry} of the given key and value.
	 *
	 * @see #ofEntries(Object...)
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 */
	static <K, V> EntrySequence<K, V> ofEntry(K left, V right) {
		return of(Maps.entry(left, right));
	}

	/**
	 * Create an {@code EntrySequence} with an {@link Entry} list created from the given keys and values in sequence in
	 * the input array.
	 *
	 * @throws IllegalArgumentException if the array of keys and values is not of even length.
	 * @see #ofEntry(Object, Object)
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 */
	@SuppressWarnings("unchecked")
	static <K, V> EntrySequence<K, V> ofEntries(Object... os) {
		if (os.length % 2 != 0)
			throw new IllegalArgumentException("Expected an even set of objects, but got: " + os.length);

		List<Entry<K, V>> entries = new ArrayList<>();
		for (int i = 0; i < os.length; i += 2)
			entries.add(Maps.entry((K) os[i], (V) os[i + 1]));
		return from(entries);
	}

	/**
	 * Create an {@code EntrySequence} from an {@link Iterable} of entries.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable...)
	 * @see #cache(Iterable)
	 */
	static <K, V> EntrySequence<K, V> from(Iterable<Entry<K, V>> iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a concatenated {@code EntrySequence} from several {@link Iterable}s of entries which are concatenated
	 * together to form the stream of entries in the {@code EntrySequence}.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 */
	@SafeVarargs
	static <K, V> EntrySequence<K, V> from(Iterable<Entry<K, V>>... iterables) {
		return () -> new ChainingIterator<>(iterables);
	}

	/**
	 * Create a once-only {@code EntrySequence} from an {@link Iterator} of entries. Note that {@code EntrySequence}s
	 * created from {@link Iterator}s cannot be passed over more than once. Further attempts will register the
	 * {@code EntrySequence} as empty.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 * @see #cache(Iterator)
	 * @since 1.1
	 */
	static <K, V> EntrySequence<K, V> once(Iterator<Entry<K, V>> iterator) {
		return from(Iterables.once(iterator));
	}

	/**
	 * Create a once-only {@code EntrySequence} from a {@link Stream} of entries. Note that {@code EntrySequence}s
	 * created from {@link Stream}s cannot be passed over more than once. Further attempts will register the
	 * {@code EntrySequence} as empty.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 * @see #once(Iterator)
	 * @see #cache(Stream)
	 * @since 1.1
	 */
	static <K, V> EntrySequence<K, V> once(Stream<Entry<K, V>> stream) {
		return once(stream.iterator());
	}

	/**
	 * Create a once-only {@code EntrySequence} from an {@link Iterator} of entries. Note that {@code EntrySequence}s
	 * created from {@link Iterator}s cannot be passed over more than once. Further attempts will register the
	 * {@code EntrySequence} as empty.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 * @see #cache(Iterator)
	 * @deprecated Use {@link #once(Iterator)} instead.
	 */
	@Deprecated
	static <K, V> EntrySequence<K, V> from(Iterator<Entry<K, V>> iterator) {
		return once(iterator);
	}

	/**
	 * Create a once-only {@code EntrySequence} from a {@link Stream} of entries. Note that {@code EntrySequence}s
	 * created from {@link Stream}s cannot be passed over more than once. Further attempts will register the
	 * {@code EntrySequence} as empty.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 * @see #once(Iterator)
	 * @see #cache(Stream)
	 * @deprecated Use {@link #once(Stream)} instead.
	 */
	@Deprecated
	static <K, V> EntrySequence<K, V> from(Stream<Entry<K, V>> stream) {
		return once(stream);
	}

	/**
	 * Create an {@code EntrySequence} of {@link Map.Entry} key/value items from a {@link Map} of items. The resulting
	 * {@code EntrySequence} can be mapped using {@link Pair} items, which implement {@link Map.Entry} and can thus be
	 * processed as part of the {@code EntrySequence}'s transformation steps.
	 *
	 * @see #of
	 * @see #from(Iterable)
	 */
	static <K, V> EntrySequence<K, V> from(Map<K, V> map) {
		return from(map.entrySet());
	}

	/**
	 * Create an {@code EntrySequence} with a cached copy of an {@link Iterable} of entries.
	 *
	 * @see #cache(Iterator)
	 * @see #cache(Stream)
	 * @see #from(Iterable)
	 * @since 1.1
	 */
	static <K, V> EntrySequence<K, V> cache(Iterable<Entry<K, V>> iterable) {
		return from(Iterables.toList(iterable));
	}

	/**
	 * Create an {@code EntrySequence} with a cached copy of an {@link Iterator} of entries.
	 *
	 * @see #cache(Iterable)
	 * @see #cache(Stream)
	 * @see #once(Iterator)
	 * @since 1.1
	 */
	static <K, V> EntrySequence<K, V> cache(Iterator<Entry<K, V>> iterator) {
		return from(Iterators.toList(iterator));
	}

	/**
	 * Create an {@code EntrySequence} with a cached copy of a {@link Stream} of entries.
	 *
	 * @see #cache(Iterable)
	 * @see #cache(Iterator)
	 * @see #once(Stream)
	 * @since 1.1
	 */
	static <K, V> EntrySequence<K, V> cache(Stream<Entry<K, V>> stream) {
		return from(stream.collect(Collectors.toList()));
	}

	/**
	 * @return an infinite {@code EntrySequence} generated by repeatedly calling the given supplier. The returned
	 * {@code EntrySequence} never terminates naturally.
	 *
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #recurse(Object, Object, BiFunction)
	 * @see #endingAt(Entry)
	 * @see #until(Entry)
	 */
	static <K, V> EntrySequence<K, V> generate(Supplier<Entry<K, V>> supplier) {
		return () -> (InfiniteIterator<Entry<K, V>>) supplier::get;
	}

	/**
	 * @return an infinite {@code EntrySequence} where each {@link #iterator()} is generated by polling for a supplier
	 * and then using it to generate the sequence of entries. The sequence never terminates.
	 *
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #endingAt(Entry)
	 * @see #until(Entry)
	 */
	static <K, V> EntrySequence<K, V> multiGenerate(Supplier<? extends Supplier<? extends Entry<K, V>>>
			                                                supplierSupplier) {
		return () -> {
			Supplier<? extends Entry<K, V>> supplier = supplierSupplier.get();
			return (InfiniteIterator<Entry<K, V>>) supplier::get;
		};
	}

	/**
	 * Returns an {@code EntrySequence} produced by recursively applying the given operation to the given seeds, which
	 * form the first element of the sequence, the second being {@code f(keySeed, valueSeed)}, the third
	 * {@code f(f(keySeed, valueSeed))} and so on. The returned {@code EntrySequence} never terminates naturally.
	 *
	 * @return an {@code EntrySequence} produced by recursively applying the given operation to the given seed
	 *
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #generate(Supplier)
	 * @see #endingAt(Entry)
	 * @see #until(Entry)
	 */
	static <K, V> EntrySequence<K, V> recurse(K keySeed, V valueSeed, BiFunction<K, V, ? extends Entry<K, V>> op) {
		return recurse(Maps.entry(keySeed, valueSeed), Maps.asUnaryOperator(op));
	}

	/**
	 * Returns an {@code EntrySequence} produced by recursively applying the given operation to the given seed, which
	 * form the first element of the sequence, the second being {@code f(seed)}, the third {@code f(f(seed))} and so
	 * on. The returned {@code EntrySequence} never terminates naturally.
	 *
	 * @return an {@code EntrySequence} produced by recursively applying the given operation to the given seed
	 *
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #recurse(Object, Object, BiFunction)
	 * @see #generate(Supplier)
	 * @see #endingAt(Entry)
	 * @see #until(Entry)
	 */
	static <K, V> EntrySequence<K, V> recurse(Entry<K, V> entry, UnaryOperator<Entry<K, V>> unaryOperator) {
		return () -> new RecursiveIterator<>(entry, unaryOperator);
	}

	/**
	 * Returns an {@code EntrySequence} produced by recursively applying the given mapper {@code f} and incrementer
	 * {@code g} operations to the given seeds, the first element being {@code f(keySeed, valueSeed)}, the second
	 * being {@code f(g(f(keySeed, valueSeed)))}, the third {@code f(g(f(g(f(keySeed, valueSeed)))))} and so on.
	 * The returned {@code EntrySequence} never terminates naturally.
	 *
	 * @param f a mapper function for producing elements that are to be included in the sequence, the first being
	 *          f(keySeed, valueSeed)
	 * @param g an incrementer function for producing the next unmapped element to be included in the sequence,
	 *          applied to the first mapped element f(keySeed, valueSeed) to produce the second unmapped value
	 *
	 * @return an {@code EntrySequence} produced by recursively applying the given mapper and incrementer operations
	 * to the
	 * given seeds
	 *
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #recurse(Object, Object, BiFunction)
	 * @see #endingAt(Entry)
	 * @see #until(Entry)
	 */
	static <K, V, KK, VV> EntrySequence<KK, VV> recurse(K keySeed, V valueSeed,
	                                                    BiFunction<? super K, ? super V, ? extends Entry<KK, VV>> f,
	                                                    BiFunction<? super KK, ? super VV, ? extends Entry<K, V>> g) {
		Function<Entry<K, V>, Entry<KK, VV>> f1 = Maps.asFunction(f);
		Function<Entry<KK, VV>, Entry<K, V>> g1 = Maps.asFunction(g);
		return recurse(f.apply(keySeed, valueSeed), f1.compose(g1)::apply);
	}

	/**
	 * Map the entries in this {@code EntrySequence} to another set of entries specified by the given {@code mapper}
	 * function.
	 *
	 * @see #map(Function)
	 * @see #map(Function, Function)
	 * @see #flatten(Function)
	 */
	default <KK, VV> EntrySequence<KK, VV> map(BiFunction<? super K, ? super V, ? extends Entry<KK, VV>> mapper) {
		return map(Maps.asFunction(mapper));
	}

	/**
	 * Map the entries in this {@code EntrySequence} to another set of entries specified by the given {@code mapper}
	 * function.
	 *
	 * @see #map(BiFunction)
	 * @see #map(Function, Function)
	 * @see #flatten(Function)
	 */
	default <KK, VV> EntrySequence<KK, VV> map(Function<? super Entry<K, V>, ? extends Entry<KK, VV>> mapper) {
		return () -> new MappingIterator<>(iterator(), mapper);
	}

	/**
	 * Map the entries in this {@code EntrySequence} to another set of entries specified by the given {@code keyMapper}
	 * amd {@code valueMapper} functions.
	 *
	 * @see #map(BiFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 */
	default <KK, VV> EntrySequence<KK, VV> map(Function<? super K, ? extends KK> keyMapper,
	                                           Function<? super V, ? extends VV> valueMapper) {
		return map(Maps.asFunction(keyMapper, valueMapper));
	}

	/**
	 * Map the entries in this {@code EntrySequence} to another set of entries specified by the given {@code mapper}
	 * function. In addition to the current entry, the mapper has access to the index of each entry.
	 *
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @since 1.2
	 */
	default <KK, VV> EntrySequence<KK, VV> mapIndexed(
			ObjLongFunction<? super Entry<K, V>, ? extends Entry<KK, VV>> mapper) {
		return () -> new IndexingMappingIterator<>(iterator(), mapper);
	}

	/**
	 * Map the entries in this {@code EntrySequence} to another set of entries specified by the given {@code mapper}
	 * function. In addition to the current entry, the mapper has access to the index of each entry.
	 *
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @since 1.2
	 */
	default <KK, VV> EntrySequence<KK, VV> mapIndexed(
			ObjObjLongFunction<? super K, ? super V, ? extends Entry<KK, VV>> mapper) {
		return mapIndexed((e, i) -> mapper.apply(e.getKey(), e.getValue(), i));
	}

	/**
	 * Skip a set number of steps in this {@code EntrySequence}.
	 */
	default EntrySequence<K, V> skip(int skip) {
		return () -> new SkippingIterator<>(iterator(), skip);
	}

	/**
	 * Skip a set number of steps at the end of this {@code EntrySequence}.
	 *
	 * @since 1.1
	 */
	default EntrySequence<K, V> skipTail(long skip) {
		if (skip == 0)
			return this;

		return () -> new TailSkippingIterator<>(iterator(), (int) skip);
	}

	/**
	 * Limit the maximum number of results returned by this {@code EntrySequence}.
	 */
	default EntrySequence<K, V> limit(int limit) {
		return () -> new LimitingIterator<>(iterator(), limit);
	}

	/**
	 * Filter the elements in this {@code EntrySequence}, keeping only the elements that match the given
	 * {@link BiPredicate}.
	 */
	default EntrySequence<K, V> filter(BiPredicate<? super K, ? super V> predicate) {
		return filter(Maps.asPredicate(predicate));
	}

	/**
	 * Filter the elements in this {@code EntrySequence}, keeping only the entries that match the given
	 * {@link Predicate}.
	 */
	default EntrySequence<K, V> filter(Predicate<? super Entry<K, V>> predicate) {
		return () -> new FilteringIterator<>(iterator(), predicate);
	}

	/**
	 * Filter the entries in this {@code EntrySequence}, keeping only the elements that match the given
	 * {@link ObjLongPredicate}, which is passed the current entry and its index in the sequence.
	 *
	 * @since 1.2
	 */
	default EntrySequence<K, V> filterIndexed(ObjLongPredicate<? super Entry<K, V>> predicate) {
		return () -> new IndexedFilteringIterator<>(iterator(), predicate);
	}

	/**
	 * Filter the entries in this {@code EntrySequence}, keeping only the elements that match the given
	 * {@link ObjLongPredicate}, which is passed the current entry and its index in the sequence.
	 *
	 * @since 1.2
	 */
	default EntrySequence<K, V> filterIndexed(ObjObjLongPredicate<? super K, ? super V> predicate) {
		return filterIndexed((e, i) -> predicate.test(e.getKey(), e.getValue(), i));
	}

	/**
	 * @return a {@code EntrySequence} containing only the entries found in the given target array.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default EntrySequence<K, V> including(Entry<K, V>... entries) {
		return filter(e -> Arrayz.contains(entries, e));
	}

	/**
	 * @return a {@code EntrySequence} containing only the entries found in the given target iterable.
	 *
	 * @since 1.2
	 */
	default EntrySequence<K, V> including(Iterable<? extends Entry<K, V>> entries) {
		return filter(e -> Iterables.contains(entries, e));
	}

	/**
	 * @return a {@code EntrySequence} containing only the entries not found in the given target array.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default EntrySequence<K, V> excluding(Entry<K, V>... entries) {
		return filter(e -> !Arrayz.contains(entries, e));
	}

	/**
	 * @return a {@code EntrySequence} containing only the entries not found in the given target iterable.
	 *
	 * @since 1.2
	 */
	default EntrySequence<K, V> excluding(Iterable<? extends Entry<K, V>> entries) {
		return filter(e -> !Iterables.contains(entries, e));
	}

	/**
	 * @return a {@link Sequence} of the {@link Entry} elements in this {@code EntrySequence} flattened into their
	 * key and value components strung together.
	 */
	default <T> Sequence<T> flatten() {
		return toSequence().flatten();
	}

	/**
	 * Flatten the elements in this {@code EntrySequence} according to the given mapper {@link BiFunction}. The
	 * resulting {@code EntrySequence} contains the elements that is the result of applying the mapper
	 * {@link BiFunction} to each element, appended together inline as a single {@code EntrySequence}.
	 *
	 * @see #flatten(Function)
	 * @see #flattenKeys(Function)
	 * @see #flattenValues(Function)
	 * @see #map(BiFunction)
	 * @see #map(Function)
	 */
	default <KK, VV> EntrySequence<KK, VV> flatten(
			BiFunction<? super K, ? super V, ? extends Iterable<Entry<KK, VV>>> mapper) {
		return flatten(Maps.asFunction(mapper));
	}

	/**
	 * Flatten the elements in this {@code EntrySequence} according to the given mapper {@link Function}. The
	 * resulting {@code EntrySequence} contains the entries that is the result of applying the mapper
	 * {@link Function} to each entry, appended together inline as a single {@code EntrySequence}.
	 *
	 * @see #flatten(BiFunction)
	 * @see #flattenKeys(Function)
	 * @see #flattenValues(Function)
	 * @see #map(BiFunction)
	 * @see #map(Function)
	 */
	default <KK, VV> EntrySequence<KK, VV> flatten(
			Function<? super Entry<K, V>, ? extends Iterable<Entry<KK, VV>>> mapper) {
		ChainingIterable<Entry<KK, VV>> result = new ChainingIterable<>();
		toSequence(mapper).forEach(result::append);
		return result::iterator;
	}

	/**
	 * Flatten the keys of each entry in this sequence, applying multiples of keys returned by the given
	 * mapper to the same value of each entry.
	 *
	 * @see #flattenValues(Function)
	 * @see #flatten(Function)
	 * @see #flatten(BiFunction)
	 */
	default <KK> EntrySequence<KK, V> flattenKeys(Function<? super Entry<K, V>, ? extends Iterable<KK>> mapper) {
		return () -> new KeyFlatteningEntryIterator<>(iterator(), mapper);
	}

	/**
	 * Flatten the values of each entry in this sequence, applying multiples of values returned by the given
	 * mapper to the same key of each entry.
	 *
	 * @see #flattenKeys(Function)
	 * @see #flatten(Function)
	 * @see #flatten(BiFunction)
	 */
	default <VV> EntrySequence<K, VV> flattenValues(Function<? super Entry<K, V>, ? extends Iterable<VV>> mapper) {
		return () -> new ValueFlatteningEntryIterator<>(iterator(), mapper);
	}

	/**
	 * Terminate this {@code EntrySequence} just before the given element is encountered, not including the element in
	 * the {@code EntrySequence}.
	 *
	 * @see #until(Predicate)
	 * @see #endingAt(Entry)
	 * @see #generate(Supplier)
	 * @see #recurse
	 * @see #repeat()
	 */
	default EntrySequence<K, V> until(Entry<K, V> terminal) {
		return () -> new ExclusiveTerminalIterator<>(iterator(), terminal);
	}

	/**
	 * Terminate this {@code EntrySequence} when the given element is encountered, including the element as the last
	 * element in the {@code EntrySequence}.
	 *
	 * @see #endingAt(Predicate)
	 * @see #until(Entry)
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #repeat()
	 */
	default EntrySequence<K, V> endingAt(Entry<K, V> terminal) {
		return () -> new InclusiveTerminalIterator<>(iterator(), terminal);
	}

	/**
	 * Terminate this {@code EntrySequence} just before the entry with the given key and value is encountered,
	 * not including the entry in the {@code EntrySequence}.
	 *
	 * @see #until(Entry)
	 * @see #until(Predicate)
	 * @see #until(BiPredicate)
	 * @see #endingAt(Entry)
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #repeat()
	 */
	default EntrySequence<K, V> until(K key, V value) {
		return until(Maps.entry(key, value));
	}

	/**
	 * Terminate this {@code EntrySequence} when the entry the given key and value is encountered,
	 * including the element as the last element in the {@code EntrySequence}.
	 *
	 * @see #endingAt(Entry)
	 * @see #endingAt(Predicate)
	 * @see #endingAt(BiPredicate)
	 * @see #until(Entry)
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #repeat()
	 */
	default EntrySequence<K, V> endingAt(K key, V value) {
		return endingAt(Maps.entry(key, value));
	}

	/**
	 * Terminate this {@code EntrySequence} just before the given predicate is satisfied, not including the element
	 * that
	 * satisfies the predicate in the {@code EntrySequence}.
	 *
	 * @see #until(Predicate)
	 * @see #until(Object, Object)
	 * @see #until(Entry)
	 * @see #endingAt(Predicate)
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #repeat()
	 */
	default EntrySequence<K, V> until(BiPredicate<? super K, ? super V> terminal) {
		return until(Maps.asPredicate(terminal));
	}

	/**
	 * Terminate this {@code EntrySequence} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the last element in the {@code EntrySequence}.
	 *
	 * @see #endingAt(Predicate)
	 * @see #endingAt(Object, Object)
	 * @see #endingAt(Entry)
	 * @see #until(Predicate)
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #repeat()
	 */
	default EntrySequence<K, V> endingAt(BiPredicate<? super K, ? super V> terminal) {
		return endingAt(Maps.asPredicate(terminal));
	}

	/**
	 * Terminate this {@code EntrySequence} just before the given predicate is satisfied, not including the element
	 * that
	 * satisfies the predicate in the {@code EntrySequence}.
	 *
	 * @see #until(BiPredicate)
	 * @see #until(Entry)
	 * @see #endingAt(Predicate)
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #repeat()
	 */
	default EntrySequence<K, V> until(Predicate<? super Entry<K, V>> terminal) {
		return () -> new ExclusiveTerminalIterator<>(iterator(), terminal);
	}

	/**
	 * Terminate this {@code EntrySequence} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the last element in the {@code EntrySequence}.
	 *
	 * @see #endingAt(BiPredicate)
	 * @see #endingAt(Entry)
	 * @see #until(Predicate)
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #repeat()
	 */
	default EntrySequence<K, V> endingAt(Predicate<? super Entry<K, V>> terminal) {
		return () -> new InclusiveTerminalIterator<>(iterator(), terminal);
	}

	/**
	 * Begin this {@code EntrySequence} just after the given Entry is encountered, not including the entry in the
	 * {@code EntrySequence}.
	 *
	 * @see #startingAfter(Predicate)
	 * @see #startingAfter(BiPredicate)
	 * @see #startingFrom(Entry)
	 * @since 1.1
	 */
	default EntrySequence<K, V> startingAfter(Entry<K, V> element) {
		return () -> new ExclusiveStartingIterator<>(iterator(), element);
	}

	/**
	 * Begin this {@code EntrySequence} when the given Entry is encountered, including the entry as the first element
	 * in the {@code EntrySequence}.
	 *
	 * @see #startingFrom(Predicate)
	 * @see #startingFrom(BiPredicate)
	 * @see #startingAfter(Entry)
	 * @since 1.1
	 */
	default EntrySequence<K, V> startingFrom(Entry<K, V> element) {
		return () -> new InclusiveStartingIterator<>(iterator(), element);
	}

	/**
	 * Begin this {@code EntrySequence} just after the given predicate is satisfied, not including the entry that
	 * satisfies the predicate in the {@code EntrySequence}.
	 *
	 * @see #startingAfter(BiPredicate)
	 * @see #startingAfter(Entry)
	 * @see #startingFrom(Predicate)
	 * @since 1.1
	 */
	default EntrySequence<K, V> startingAfter(Predicate<? super Entry<K, V>> predicate) {
		return () -> new ExclusiveStartingIterator<>(iterator(), predicate);
	}

	/**
	 * Begin this {@code EntrySequence} when the given predicate is satisfied, including the entry that satisfies
	 * the predicate as the first element in the {@code EntrySequence}.
	 *
	 * @see #startingFrom(BiPredicate)
	 * @see #startingFrom(Entry)
	 * @see #startingAfter(Predicate)
	 * @since 1.1
	 */
	default EntrySequence<K, V> startingFrom(Predicate<? super Entry<K, V>> predicate) {
		return () -> new InclusiveStartingIterator<>(iterator(), predicate);
	}

	/**
	 * Begin this {@code EntrySequence} just after the given predicate is satisfied, not including the entry that
	 * satisfies the predicate in the {@code EntrySequence}.
	 *
	 * @see #startingAfter(Predicate)
	 * @see #startingAfter(Entry)
	 * @see #startingFrom(Predicate)
	 * @since 1.1
	 */
	default EntrySequence<K, V> startingAfter(BiPredicate<? super K, ? super V> predicate) {
		return startingAfter(Maps.asPredicate(predicate));
	}

	/**
	 * Begin this {@code EntrySequence} when the given predicate is satisfied, including the entry that satisfies
	 * the predicate as the first element in the {@code EntrySequence}.
	 *
	 * @see #startingFrom(Predicate)
	 * @see #startingFrom(Entry)
	 * @see #startingAfter(Predicate)
	 * @since 1.1
	 */
	default EntrySequence<K, V> startingFrom(BiPredicate<? super K, ? super V> predicate) {
		return startingFrom(Maps.asPredicate(predicate));
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into an array.
	 */
	default Entry<K, V>[] toArray() {
		return toArray(Entry[]::new);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into an array of the type determined by the given array
	 * constructor.
	 */
	default Entry<K, V>[] toArray(IntFunction<Entry<K, V>[]> constructor) {
		List<Entry<K, V>> list = toList();
		@SuppressWarnings("unchecked")
		Entry<K, V>[] array = list.toArray(constructor.apply(list.size()));
		return array;
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link List}.
	 */
	default List<Entry<K, V>> toList() {
		return toList(ArrayList::new);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link List} of the type determined by the given
	 * constructor.
	 */
	default List<Entry<K, V>> toList(Supplier<List<Entry<K, V>>> constructor) {
		return toCollection(constructor);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link Set}.
	 */
	default Set<Entry<K, V>> toSet() {
		return toSet(HashSet::new);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link Set} of the type determined by the given
	 * constructor.
	 */
	default <S extends Set<Entry<K, V>>> S toSet(Supplier<? extends S> constructor) {
		return toCollection(constructor);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link SortedSet}.
	 */
	default SortedSet<Entry<K, V>> toSortedSet() {
		return toSet(TreeSet::new);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link Map}.
	 */
	default Map<K, V> toMap() {
		return toMap(HashMap::new);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link Map} of the type determined by the given
	 * constructor.
	 */
	default <M extends Map<K, V>> M toMap(Supplier<? extends M> constructor) {
		return collect(constructor, Maps::put);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link SortedMap}.
	 */
	default SortedMap<K, V> toSortedMap() {
		return toMap(TreeMap::new);
	}

	/**
	 * Collect this {@code EntrySequence} into a {@link Collection} of the type determined by the given constructor.
	 */
	default <C extends Collection<Entry<K, V>>> C toCollection(Supplier<? extends C> constructor) {
		return collect(constructor, Collection::add);
	}

	/**
	 * Collect this {@code EntrySequence} into an arbitrary container using the given constructor and adder.
	 */
	default <C> C collect(Supplier<? extends C> constructor, BiConsumer<? super C, ? super Entry<K, V>> adder) {
		return collectInto(constructor.get(), adder);
	}

	/**
	 * Collect this {@code EntrySequence} into an arbitrary container using the given {@link Collector}.
	 */
	default <S, R> S collect(Collector<Entry<K, V>, R, S> collector) {
		R intermediary = collect(collector.supplier(), collector.accumulator());
		return collector.finisher().apply(intermediary);
	}

	/**
	 * Collect this {@code EntrySequence} into the given {@link Collection}.
	 */
	default <U extends Collection<Entry<K, V>>> U collectInto(U collection) {
		return collectInto(collection, Collection::add);
	}

	/**
	 * Collect this {@code EntrySequence} into the given container, using the given adder.
	 */
	default <C> C collectInto(C result, BiConsumer<? super C, ? super Entry<K, V>> adder) {
		forEach(entry -> adder.accept(result, entry));
		return result;
	}

	/**
	 * Join this {@code EntrySequence} into a string separated by the given delimiter.
	 */
	default String join(String delimiter) {
		return join("", delimiter, "");
	}

	/**
	 * Join this {@code EntrySequence} into a string separated by the given delimiter, with the given prefix and
	 * suffix.
	 */
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

	/**
	 * Reduce this {@code EntrySequence} into a single element by iteratively applying the given binary operator to
	 * the current result and each entry in this sequence.
	 */
	default Optional<Entry<K, V>> reduce(BinaryOperator<Entry<K, V>> operator) {
		return Iterators.reduce(iterator(), operator);
	}

	/**
	 * Reduce this {@code EntrySequence} into a single element by iteratively applying the given function to
	 * the current result and each entry in this sequence. The function is passed the key and value of the result,
	 * followed by the keys and values of the current entry, respectively.
	 */
	default Optional<Entry<K, V>> reduce(QuaternaryFunction<K, V, K, V, Entry<K, V>> operator) {
		return reduce(Maps.asBinaryOperator(operator));
	}

	/**
	 * Reduce this {@code EntrySequence} into a single element by iteratively applying the given binary operator to
	 * the current result and each entry in this sequence, starting with the given identity as the initial result.
	 */
	default Entry<K, V> reduce(Entry<K, V> identity, BinaryOperator<Entry<K, V>> operator) {
		return Iterators.reduce(iterator(), identity, operator);
	}

	/**
	 * Reduce this {@code EntrySequence} into a single element by iteratively applying the given binary operator to
	 * the current result and each entry in this sequence, starting with the given identity as the initial result.
	 * The function is passed the key and value of the result, followed by the keys and values of the current entry,
	 * respectively.
	 */
	default Entry<K, V> reduce(K key, V value, QuaternaryFunction<K, V, K, V, Entry<K, V>> operator) {
		return reduce(Maps.entry(key, value), Maps.asBinaryOperator(operator));
	}

	/**
	 * @return the first entry of this {@code EntrySequence} or an empty {@link Optional} if there are no entries in
	 * the {@code EntrySequence}.
	 */
	default Optional<Entry<K, V>> first() {
		return get(0);
	}

	/**
	 * @return the second entry of this {@code EntrySequence} or an empty {@link Optional} if there are one or less
	 * entries in the {@code EntrySequence}.
	 */
	default Optional<Entry<K, V>> second() {
		return get(1);
	}

	/**
	 * @return the third entry of this {@code EntrySequence} or an empty {@link Optional} if there are two or less
	 * entries in the {@code EntrySequence}.
	 */
	default Optional<Entry<K, V>> third() {
		return get(2);
	}

	/**
	 * @return the element at the given index, or an empty {@link Optional} if the {@code EntrySequence} is smaller
	 * than the index.
	 */
	default Optional<Entry<K, V>> get(long index) {
		return Iterators.get(iterator(), index);
	}

	/**
	 * @return the last entry of this {@code EntrySequence} or an empty {@link Optional} if there are no entries in
	 * the {@code EntrySequence}.
	 */
	default Optional<Entry<K, V>> last() {
		return Iterators.last(iterator());
	}

	/**
	 * Window the elements of this {@code EntrySequence} into a {@link Sequence} of {@code EntrySequence}s of entrues,
	 * each with the size of the given window. The first item in each sequence is the second item in the previous
	 * sequence. The final sequence may be shorter than the window. This method is equivalent to
	 * {@code window(window, 1)}.
	 */
	default Sequence<EntrySequence<K, V>> window(int window) {
		return window(window, 1);
	}

	/**
	 * Window the elements of this {@code EntrySequence} into a sequence of {@code EntrySequence}s of elements, each
	 * with the size of the given window, stepping {@code step} elements between each window. If the given step is less
	 * than the window size, the windows will overlap each other. If the step is larger than the window size, elements
	 * will be skipped in between windows.
	 */
	default Sequence<EntrySequence<K, V>> window(int window, int step) {
		return () -> new WindowingIterator<Entry<K, V>, EntrySequence<K, V>>(iterator(), window, step) {
			@Override
			protected EntrySequence<K, V> toSequence(List<Entry<K, V>> list) {
				return EntrySequence.from(list);
			}
		};
	}

	/**
	 * Batch the elements of this {@code EntrySequence} into a sequence of {@code EntrySequence}s of distinct elements,
	 * each with the given batch size. This method is equivalent to {@code window(size, size)}.
	 */
	default Sequence<EntrySequence<K, V>> batch(int size) {
		return window(size, size);
	}

	/**
	 * Batch the elements of this {@code EntrySequence} into a sequence of {@link EntrySequence}s of distinct elements,
	 * where the given predicate determines where to split the lists of partitioned elements. The predicate is given
	 * the current and next item in the iteration, and if it returns true a partition is created between the elements.
	 */
	default Sequence<EntrySequence<K, V>> batch(BiPredicate<? super Entry<K, V>, ? super Entry<K, V>> predicate) {
		return () -> new PredicatePartitioningIterator<Entry<K, V>, EntrySequence<K, V>>(iterator(), predicate) {
			@Override
			protected EntrySequence<K, V> toSequence(List<Entry<K, V>> list) {
				return EntrySequence.from(list);
			}
		};
	}

	/**
	 * Batch the elements of this {@code EntrySequence} into a sequence of {@link EntrySequence}s of distinct elements,
	 * where the given predicate determines where to split the lists of partitioned elements. The predicate is given
	 * the keys and values of the current and next items in the iteration, and if it returns true a partition is
	 * created between the elements.
	 */
	default Sequence<EntrySequence<K, V>> batch(
			QuaternaryPredicate<? super K, ? super V, ? super K, ? super V> predicate) {
		return batch((e1, e2) -> predicate.test(e1.getKey(), e1.getValue(), e2.getKey(), e2.getValue()));
	}

	/**
	 * Split the elements of this {@code EntrySequence} into a sequence of {@code EntrySequence}s of distinct elements,
	 * around the given element. The elements around which the sequence is split are not included in the result.
	 */
	default Sequence<EntrySequence<K, V>> split(Entry<K, V> element) {
		return () -> new SplittingIterator<Entry<K, V>, EntrySequence<K, V>>(iterator(), element) {
			@Override
			protected EntrySequence<K, V> toSequence(List<Entry<K, V>> list) {
				return EntrySequence.from(list);
			}
		};
	}

	/**
	 * Split the elements of this {@code EntrySequence} into a sequence of {@code EntrySequence}s of distinct elements,
	 * where the given predicate determines which elements to split the partitioned elements around. The elements
	 * matching the predicate are not included in the result.
	 */
	default Sequence<EntrySequence<K, V>> split(Predicate<? super Entry<K, V>> predicate) {
		return () -> new SplittingIterator<Entry<K, V>, EntrySequence<K, V>>(iterator(), predicate) {
			@Override
			protected EntrySequence<K, V> toSequence(List<Entry<K, V>> list) {
				return EntrySequence.from(list);
			}
		};
	}

	/**
	 * Split the elements of this {@code EntrySequence} into a sequence of {@code EntrySequence}s of distinct elements,
	 * where the given predicate determines which elements to split the partitioned elements around. The elements
	 * matching the predicate are not included in the result.
	 */
	default Sequence<EntrySequence<K, V>> split(BiPredicate<? super K, ? super V> predicate) {
		return split(Maps.asPredicate(predicate));
	}

	/**
	 * Skip x number of steps in between each invocation of the iterator of this {@code EntrySequence}.
	 */
	default EntrySequence<K, V> step(int step) {
		return () -> new SteppingIterator<>(iterator(), step);
	}

	/**
	 * @return an {@code EntrySequence} where each item in this {@code EntrySequence} occurs only once, the first time
	 * it is encountered.
	 */
	default EntrySequence<K, V> distinct() {
		return () -> new DistinctIterator<>(iterator());
	}

	/**
	 * @return this {@code EntrySequence} sorted according to the natural order.
	 */
	default EntrySequence<K, V> sorted() {
		return () -> Iterators.unmodifiable(Lists.sort((List) toList()));
	}

	/**
	 * @return this {@code EntrySequence} sorted according to the given {@link Comparator}.
	 */
	default EntrySequence<K, V> sorted(Comparator<? super Entry<? extends K, ? extends V>> comparator) {
		return () -> Iterators.unmodifiable(Lists.sort(toList(), comparator));
	}

	/**
	 * @return the minimal element in this {@code EntrySequence} according to their natural order. The entries in the
	 * sequence must all implement {@link Comparable} or a {@link ClassCastException} will be thrown at runtime.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default Optional<Entry<K, V>> min() {
		return min((Comparator) Comparator.naturalOrder());
	}

	/**
	 * @return the maximum element in this {@code EntrySequence} according to their natural order. The entries in the
	 * sequence must all implement {@link Comparable} or a {@link ClassCastException} will be thrown at runtime.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default Optional<Entry<K, V>> max() {
		return max((Comparator) Comparator.naturalOrder());
	}

	/**
	 * @return the minimal element in this {@code EntrySequence} according to the given {@link Comparator}.
	 */
	default Optional<Entry<K, V>> min(Comparator<? super Entry<K, V>> comparator) {
		return reduce(BinaryOperator.minBy(comparator));
	}

	/**
	 * @return the maximum element in this {@code EntrySequence} according to the given {@link Comparator}.
	 */
	default Optional<Entry<K, V>> max(Comparator<? super Entry<K, V>> comparator) {
		return reduce(BinaryOperator.maxBy(comparator));
	}

	/**
	 * @return the count of elements in this {@code EntrySequence}.
	 *
	 * @since 1.2
	 */
	default long size() {
		return Iterables.count(this);
	}

	/**
	 * @return the count of elements in this {@code EntrySequence}.
	 *
	 * @deprecated Use {@link #size()} instead.
	 */
	@Deprecated
	default long count() {
		return size();
	}

	/**
	 * @return this {@code EntrySequence} as a {@link Stream} of entries.
	 */
	default Stream<Entry<K, V>> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * @return true if all elements in this {@code EntrySequence} satisfy the given predicate, false otherwise.
	 */
	default boolean all(BiPredicate<? super K, ? super V> biPredicate) {
		return Iterables.all(this, Maps.asPredicate(biPredicate));
	}

	/**
	 * @return true if no elements in this {@code EntrySequence} satisfy the given predicate, false otherwise.
	 */
	default boolean none(BiPredicate<? super K, ? super V> predicate) {
		return !any(predicate);
	}

	/**
	 * @return true if any element in this {@code EntrySequence} satisfies the given predicate, false otherwise.
	 */
	default boolean any(BiPredicate<? super K, ? super V> biPredicate) {
		return Iterables.any(this, Maps.asPredicate(biPredicate));
	}

	/**
	 * Allow the given {@link BiConsumer} to see the components of each entry in this {@code EntrySequence} as it is
	 * traversed.
	 */
	default EntrySequence<K, V> peek(BiConsumer<? super K, ? super V> action) {
		return peek(Maps.asConsumer(action));
	}

	/**
	 * Allow the given {@link Consumer} to see each entry in this {@code EntrySequence} as it is traversed.
	 *
	 * @since 1.2.2
	 */
	default EntrySequence<K, V> peek(Consumer<? super Entry<K, V>> consumer) {
		return () -> new PeekingIterator<>(iterator(), consumer);
	}

	/**
	 * Allow the given {@link ObjObjLongConsumer} to see the components of each entry with their index as this
	 * {@code EntrySequence} is traversed.
	 *
	 * @since 1.2.2
	 */
	default EntrySequence<K, V> peekIndexed(ObjObjLongConsumer<? super K, ? super V> action) {
		return peekIndexed((p, x) -> action.accept(p.getKey(), p.getValue(), x));
	}

	/**
	 * Allow the given {@link ObjLongConsumer} to see each entry with its index as this {@code EntrySequence} is
	 * traversed.
	 *
	 * @since 1.2.2
	 */
	default EntrySequence<K, V> peekIndexed(ObjLongConsumer<? super Entry<K, V>> action) {
		return () -> new IndexPeekingIterator<>(iterator(), action);
	}

	/**
	 * Append the elements of the given {@link Iterator} to the end of this {@code EntrySequence}.
	 * <p>
	 * The appended elements will only be available on the first traversal of the resulting {@code Sequence}.
	 */
	default EntrySequence<K, V> append(Iterator<? extends Entry<K, V>> iterator) {
		return append(Iterables.once(iterator));
	}

	/**
	 * Append the elements of the given {@link Iterable} to the end of this {@code EntrySequence}.
	 */
	default EntrySequence<K, V> append(Iterable<? extends Entry<K, V>> that) {
		@SuppressWarnings("unchecked")
		Iterable<Entry<K, V>> chainingSequence = new ChainingIterable<>(this, that);
		return chainingSequence::iterator;
	}

	/**
	 * Append the given elements to the end of this {@code EntrySequence}.
	 */
	@SuppressWarnings("unchecked")
	default EntrySequence<K, V> append(Entry<K, V>... entries) {
		return append(Iterables.of(entries));
	}

	/**
	 * Append the given entry to the end of this {@code EntrySequence}.
	 */
	@SuppressWarnings("unchecked")
	default EntrySequence<K, V> appendEntry(K key, V value) {
		return append(Maps.entry(key, value));
	}

	/**
	 * Append the elements of the given {@link Stream} to the end of this {@code EntrySequence}.
	 * <p>
	 * The appended elements will only be available on the first traversal of the resulting {@code EntrySequence}.
	 */
	default EntrySequence<K, V> append(Stream<Entry<K, V>> stream) {
		return append(stream.iterator());
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link Sequence} of {@link Entry} elements.
	 */
	default Sequence<Entry<K, V>> toSequence() {
		return Sequence.from(this);
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link Sequence} where each item is generated by the given mapper.
	 */
	default <T> Sequence<T> toSequence(BiFunction<? super K, ? super V, ? extends T> mapper) {
		return toSequence(Maps.asFunction(mapper));
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link Sequence} where each item is generated by the given mapper.
	 */
	default <T> Sequence<T> toSequence(Function<? super Entry<K, V>, ? extends T> mapper) {
		return () -> new MappingIterator<>(iterator(), mapper);
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link BiSequence} of {@link Pair} elements.
	 */
	default BiSequence<K, V> toBiSequence() {
		return BiSequence.from(Sequence.from(this).map(Pair::from));
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link CharSeq} using the given mapper function to map each entry to a
	 * {@code char}.
	 *
	 * @see #toSequence(BiFunction)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntBiFunction)
	 * @see #toLongs(ToLongBiFunction)
	 * @see #toDoubles(ToDoubleBiFunction)
	 * @see #map(BiFunction)
	 * @see #flatten(BiFunction)
	 *
	 * @since 1.1.1
	 */
	default CharSeq toChars(ToCharBiFunction<? super K, ? super V> mapper) {
		return toChars(e -> mapper.applyAsChar(e.getKey(), e.getValue()));
	}

	/**
	 * Convert this {@code EntrySequence} to an {@link IntSequence} using the given mapper function to map each entry
	 * to an {@code int}.
	 *
	 * @see #toSequence(BiFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toChars(ToCharBiFunction)
	 * @see #toLongs(ToLongBiFunction)
	 * @see #toDoubles(ToDoubleBiFunction)
	 * @see #map(BiFunction)
	 * @see #flatten(BiFunction)
	 *
	 * @since 1.1.1
	 */
	default IntSequence toInts(ToIntBiFunction<? super K, ? super V> mapper) {
		return toInts(e -> mapper.applyAsInt(e.getKey(), e.getValue()));
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link LongSequence} using the given mapper function to map each entry
	 * to a {@code long}.
	 *
	 * @see #toSequence(BiFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toChars(ToCharBiFunction)
	 * @see #toInts(ToIntBiFunction)
	 * @see #toDoubles(ToDoubleBiFunction)
	 * @see #map(BiFunction)
	 * @see #flatten(BiFunction)
	 *
	 * @since 1.1.1
	 */
	default LongSequence toLongs(ToLongBiFunction<? super K, ? super V> mapper) {
		return toLongs(e -> mapper.applyAsLong(e.getKey(), e.getValue()));
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link DoubleSequence} using the given mapper function to map each entry
	 * to a {@code double}.
	 *
	 * @see #toSequence(BiFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #toChars(ToCharBiFunction)
	 * @see #toInts(ToIntBiFunction)
	 * @see #toLongs(ToLongBiFunction)
	 * @see #map(BiFunction)
	 * @see #flatten(BiFunction)
	 *
	 * @since 1.1.1
	 */
	default DoubleSequence toDoubles(ToDoubleBiFunction<? super K, ? super V> mapper) {
		return toDoubles(e -> mapper.applyAsDouble(e.getKey(), e.getValue()));
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link CharSeq} using the given mapper function to map each entry to a
	 * {@code char}.
	 *
	 * @see #toSequence(Function)
	 * @see #toChars(ToCharBiFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 *
	 * @since 1.1.1
	 */
	default CharSeq toChars(ToCharFunction<? super Entry<K, V>> mapper) {
		return () -> CharIterator.from(iterator(), mapper);
	}

	/**
	 * Convert this {@code EntrySequence} to an {@link IntSequence} using the given mapper function to map each entry
	 * to an {@code int}.
	 *
	 * @see #toSequence(Function)
	 * @see #toInts(ToIntBiFunction)
	 * @see #toChars(ToCharFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 *
	 * @since 1.1.1
	 */
	default IntSequence toInts(ToIntFunction<? super Entry<K, V>> mapper) {
		return () -> IntIterator.from(iterator(), mapper);
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link LongSequence} using the given mapper function to map each entry
	 * to a {@code long}.
	 *
	 * @see #toSequence(Function)
	 * @see #toLongs(ToLongBiFunction)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 *
	 * @since 1.1.1
	 */
	default LongSequence toLongs(ToLongFunction<? super Entry<K, V>> mapper) {
		return () -> LongIterator.from(iterator(), mapper);
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link DoubleSequence} using the given mapper function to map each entry
	 * to a {@code double}.
	 *
	 * @see #toSequence(Function)
	 * @see #toDoubles(ToDoubleBiFunction)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 *
	 * @since 1.1.1
	 */
	default DoubleSequence toDoubles(ToDoubleFunction<? super Entry<K, V>> mapper) {
		return () -> DoubleIterator.from(iterator(), mapper);
	}

	/**
	 * Repeat this {@code EntrySequence} forever, producing a sequence that never terminates unless the original
	 * sequence is empty in which case the resulting sequence is also empty.
	 */
	default EntrySequence<K, V> repeat() {
		return () -> new RepeatingIterator<>(this, -1);
	}

	/**
	 * Repeat this {@code EntrySequence} the given number of times.
	 */
	default EntrySequence<K, V> repeat(long times) {
		return () -> new RepeatingIterator<>(this, times);
	}

	/**
	 * @return an {@code EntrySequence} which iterates over this {@code EntrySequence} in reverse order.
	 */
	default EntrySequence<K, V> reverse() {
		return () -> new ReverseIterator<>(iterator());
	}

	/**
	 * @return an {@code EntrySequence} which iterates over this {@code EntrySequence} in random order.
	 */
	default EntrySequence<K, V> shuffle() {
		return () -> Iterators.unmodifiable(Lists.shuffle(toList()));
	}

	/**
	 * @return an {@code EntrySequence} which iterates over this {@code EntrySequence} in random order as determined by
	 * the given random generator.
	 */
	default EntrySequence<K, V> shuffle(Random random) {
		return () -> Iterators.unmodifiable(Lists.shuffle(toList(), random));
	}

	/**
	 * @return an {@code EntrySequence} which iterates over this {@code EntrySequence} in random order as determined by
	 * the given random generator. A new instance of {@link Random} is created by the given supplier at the start of
	 * each iteration.
	 *
	 * @since 1.2
	 */
	default EntrySequence<K, V> shuffle(Supplier<? extends Random> randomSupplier) {
		return () -> Iterators.unmodifiable(Lists.shuffle(toList(), randomSupplier.get()));
	}

	/**
	 * Remove all elements matched by this sequence using {@link Iterator#remove()}.
	 *
	 * @since 1.2
	 */
	default void clear() {
		Iterables.removeAll(this);
	}

	/**
	 * Remove all elements matched by this sequence using {@link Iterator#remove()}.
	 *
	 * @deprecated Use {@link #clear()} instead.
	 */
	@Deprecated
	default void removeAll() {
		clear();
	}

	/**
	 * @return true if this {@code EntrySequence} is empty, false otherwise.
	 *
	 * @since 1.1
	 */
	default boolean isEmpty() {
		return !iterator().hasNext();
	}

	/**
	 * @return true if this {@code EntrySequence} contains the given entry, false otherwise.
	 *
	 * @since 1.2
	 */
	default boolean contains(Entry<K, V> entry) {
		return Iterables.contains(this, entry);
	}

	/**
	 * @return true if this {@code EntrySequence} contains the given pair, false otherwise.
	 *
	 * @since 1.2
	 */
	default boolean contains(K key, V value) {
		return any((k, v) -> Objects.equals(key, k) && Objects.equals(value, v));
	}

	/**
	 * @return true if this {@code EntrySequence} contains all of the given entries, false otherwise.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default boolean containsAll(Entry<K, V>... entries) {
		return Iterables.containsAll(this, entries);
	}

	/**
	 * @return true if this {@code EntrySequence} contains any of the given entries, false otherwise.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default boolean containsAny(Entry<K, V>... entries) {
		return Iterables.containsAny(this, entries);
	}

	/**
	 * @return true if this {@code EntrySequence} contains all of the given entries, false otherwise.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default boolean containsAll(Iterable<? extends Entry<K, V>> entries) {
		return Iterables.containsAll(this, entries);
	}

	/**
	 * @return true if this {@code EntrySequence} contains any of the given entries, false otherwise.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default boolean containsAny(Iterable<? extends Entry<K, V>> entries) {
		return Iterables.containsAny(this, entries);
	}

	/**
	 * Perform the given action for each element in this {@code EntrySequence}.
	 *
	 * @since 1.2
	 */
	default void forEach(BiConsumer<? super K, ? super V> action) {
		forEach(Maps.asConsumer(action));
	}
}
