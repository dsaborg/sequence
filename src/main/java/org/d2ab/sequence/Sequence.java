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

import org.d2ab.function.chars.ToCharFunction;
import org.d2ab.iterable.ChainingIterable;
import org.d2ab.iterable.Iterables;
import org.d2ab.iterator.*;
import org.d2ab.iterator.chars.DelegatingCharIterator;
import org.d2ab.iterator.doubles.DelegatingDoubleIterator;
import org.d2ab.iterator.ints.DelegatingIntIterator;
import org.d2ab.iterator.longs.DelegatingLongIterator;
import org.d2ab.util.Entries;
import org.d2ab.util.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.singleton;
import static java.util.function.BinaryOperator.maxBy;
import static java.util.function.BinaryOperator.minBy;

/**
 * An {@link Iterable} sequence of elements with {@link Stream}-like operations for refining, transforming and collating
 * the list of elements.
 */
@FunctionalInterface
public interface Sequence<T> extends Iterable<T> {
	/**
	 * Create an empty {@code Sequence} with no items.
	 *
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 */
	static <T> Sequence<T> empty() {
		return from(emptyIterator());
	}

	/**
	 * Create a {@code Sequence} from an {@link Iterator} of items. Note that {@code Sequences} created from {@link
	 * Iterator}s cannot be passed over more than once. Further attempts will register the {@code Sequence} as empty.
	 *
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 */
	static <T> Sequence<T> from(Iterator<T> iterator) {
		return () -> iterator;
	}

	/**
	 * Create a {@code Sequence} with one item.
	 *
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 */
	static <T> Sequence<T> of(@Nullable T item) {
		return from(singleton(item));
	}

	/**
	 * Create a {@code Sequence} from an {@link Iterable} of items.
	 *
	 * @see #of(Object)
	 * @see #of(Object...)
	 */
	static <T> Sequence<T> from(Iterable<T> iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a {@code Sequence} with the given items.
	 *
	 * @see #of(Object)
	 * @see #from(Iterable)
	 */
	@SafeVarargs
	static <T> Sequence<T> of(T... items) {
		return from(asList(items));
	}

	/**
	 * Create a concatenated {@code Sequence} from several {@link Iterable}s which are concatenated together to form
	 * the stream of items in the {@code Sequence}.
	 *
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 */
	@SafeVarargs
	static <T> Sequence<T> from(Iterable<T>... iterables) {
		return new ChainingIterable<>(iterables)::iterator;
	}

	/**
	 * Create a {@code Sequence} from {@link Iterator}s of items supplied by the given {@link Supplier}. Every time the
	 * {@code Sequence} is to be iterated over, the {@link Supplier} is used to create the initial stream of elements.
	 * This is similar to creating a {@code Sequence} from an {@link Iterable}.
	 *
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 * @see #from(Iterator)
	 */
	static <T> Sequence<T> from(Supplier<? extends Iterator<T>> iteratorSupplier) {
		return iteratorSupplier::get;
	}

	/**
	 * Create a {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created from {@link
	 * Stream}s cannot be passed over more than once. Further attempts will cause an {@link IllegalStateException} when
	 * the {@link Stream} is requested again.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 * @see #from(Iterator)
	 */
	static <T> Sequence<T> from(Stream<T> stream) {
		return stream::iterator;
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
		return from(map.entrySet());
	}

	/**
	 * A {@code Sequence} of all the positive {@link Integer} numbers starting at {@code 1} and ending at {@link
	 * Integer#MAX_VALUE} inclusive.
	 *
	 * @see #ints(int)
	 * @see #range(int, int)
	 */
	static Sequence<Integer> ints() {
		return range(1, Integer.MAX_VALUE);
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
	 */
	static Sequence<Integer> ints(int start) {
		return range(start, Integer.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Integer} numbers between the given start and end positions, inclusive.
	 * If the end index is less than the start index, the resulting {@code Sequence} will be counting down from the
	 * start to the end.
	 *
	 * @see #ints()
	 * @see #ints(int)
	 */
	static Sequence<Integer> range(int start, int end) {
		UnaryOperator<Integer> next = (end > start) ? i -> ++i : i -> --i;
		return recurse(start, next).endingAt(end);
	}

	/**
	 * A {@code Sequence} of all the positive {@link Long} numbers starting at {@code 1} and ending at {@link
	 * Long#MAX_VALUE} inclusive.
	 *
	 * @see #longs(long)
	 * @see #range(long, long)
	 */
	static Sequence<Long> longs() {
		return range(1, Long.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Long} numbers starting at the given value and ending at {@link
	 * Long#MAX_VALUE} inclusive.
	 * <p>
	 * The start value may be negative, in which case the sequence will continue towards positive numbers and
	 * eventually {@link Long#MAX_VALUE}.
	 *
	 * @see #longs()
	 * @see #range(long, long)
	 */
	static Sequence<Long> longs(long start) {
		return range(start, Long.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Long} numbers between the given start and end positions, inclusive.
	 * If the end index is less than the start index, the resulting {@code Sequence} will be counting down from the
	 * start to the end.
	 *
	 * @see #longs()
	 * @see #longs(long)
	 */
	static Sequence<Long> range(long start, long end) {
		UnaryOperator<Long> next = (end > start) ? i -> i + 1 : i -> i - 1;
		return recurse(start, next).endingAt(end);
	}

	/**
	 * A {@code Sequence} of all the {@link Character} values starting at {@link Character#MIN_VALUE} and ending at
	 * {@link Character#MAX_VALUE} inclusive.
	 *
	 * @see #chars(char)
	 * @see #range(char, char)
	 */
	static Sequence<Character> chars() {
		return range((char) 0, Character.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Character} values starting at the given value and ending at {@link
	 * Character#MAX_VALUE} inclusive.
	 *
	 * @see #chars()
	 * @see #range(char, char)
	 */
	static Sequence<Character> chars(char start) {
		return range(start, Character.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Character} values between the given start and end positions, inclusive.
	 * If the end index is less than the start index, the resulting {@code Sequence} will be counting down from the
	 * start to the end.
	 *
	 * @see #chars()
	 * @see #chars(char)
	 */
	static Sequence<Character> range(char start, char end) {
		UnaryOperator<Character> next = (end > start) ? c -> (char) (c + 1) : c -> (char) (c - 1);
		return recurse(start, next).endingAt(end);
	}

	/**
	 * @return an infinite {@code Sequence} generated by repeatedly calling the given supplier.
	 *
	 * @see #recurse(Object, UnaryOperator)
	 * @see #endingAt(Object)
	 * @see #until(Object)
	 */
	static <T> Sequence<T> generate(Supplier<T> supplier) {
		return () -> (InfiniteIterator<T>) supplier::get;
	}

	/**
	 * Returns a {@code Sequence} produced by recursively applying the given operation to the given seed, which forms
	 * the first element of the sequence, the second being f(seed), the third f(f(seed)) and so on. The returned
	 * {@code Sequence} never terminates naturally.
	 *
	 * @return a {@code Sequence} produced by recursively applying the given operation to the given seed
	 *
	 * @see #recurse(Object, Function, Function)
	 * @see #generate(Supplier)
	 * @see #endingAt(Object)
	 * @see #until(Object)
	 */
	static <T> Sequence<T> recurse(@Nullable T seed, UnaryOperator<T> f) {
		return () -> new RecursiveIterator<>(seed, f);
	}

	/**
	 * Returns a {@code Sequence} produced by recursively applying the given mapper {@code f} and incrementer
	 * {@code g} operations to the given seed, the first element being {@code f(seed)}, the second being
	 * {@code f(g(f(seed)))}, the third {@code f(g(f(g(f(seed)))))} and so on. The returned {@code Sequence} never
	 * terminates naturally.
	 *
	 * @param f a mapper function for producing elements that are to be included in the sequence, the first being
	 *          f(seed)
	 * @param g an incrementer function for producing the next unmapped element to be included in the sequence,
	 *          applied to the first mapped element f(seed) to produce the second unmapped value
	 *
	 * @return a {@code Sequence} produced by recursively applying the given mapper and incrementer operations to the
	 * given seed
	 *
	 * @see #recurse(Object, UnaryOperator)
	 * @see #endingAt(Object)
	 * @see #until(Object)
	 */
	static <T, S> Sequence<S> recurse(@Nullable T seed,
	                                  Function<? super T, ? extends S> f,
	                                  Function<? super S, ? extends T> g) {
		return () -> new RecursiveIterator<>(f.apply(seed), f.compose(g)::apply);
	}

	/**
	 * Terminate this {@code Sequence} just before the given element is encountered, not including the element in the
	 * {@code Sequence}.
	 *
	 * @see #untilNull()
	 * @see #until(Predicate)
	 * @see #endingAt(Object)
	 * @see #generate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #recurse(Object, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> until(@Nullable T terminal) {
		return () -> new ExclusiveTerminalIterator<>(terminal).backedBy(iterator());
	}

	/**
	 * Terminate this {@code Sequence} when the given element is encountered, including the element as the last element
	 * in the {@code Sequence}.
	 *
	 * @see #endingAtNull
	 * @see #endingAt(Predicate)
	 * @see #until(Object)
	 * @see #generate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #recurse(Object, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> endingAt(@Nullable T terminal) {
		return () -> new InclusiveTerminalIterator<>(terminal).backedBy(iterator());
	}

	/**
	 * Terminate this {@code Sequence} just before a null element is encountered, not including the null in the
	 * {@code Sequence}.
	 *
	 * @see #until(Object)
	 * @see #until(Predicate)
	 * @see #endingAtNull
	 * @see #generate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #recurse(Object, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> untilNull() {
		return () -> new ExclusiveTerminalIterator<>((T) null).backedBy(iterator());
	}

	/**
	 * Terminate this {@code Sequence} when a null element is encountered, including the null as the last element
	 * in the {@code Sequence}.
	 *
	 * @see #endingAt(Object)
	 * @see #endingAt(Predicate)
	 * @see #untilNull
	 * @see #generate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #recurse(Object, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> endingAtNull() {
		return () -> new InclusiveTerminalIterator<>((T) null).backedBy(iterator());
	}

	/**
	 * Terminate this {@code Sequence} just before the given predicate is satisfied, not including the element that
	 * satisfies the predicate in the {@code Sequence}.
	 *
	 * @see #until(Object)
	 * @see #untilNull()
	 * @see #endingAt(Predicate)
	 * @see #generate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #recurse(Object, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> until(Predicate<T> terminal) {
		return () -> new ExclusiveTerminalIterator<>(terminal).backedBy(iterator());
	}

	/**
	 * Terminate this {@code Sequence} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the last element in the {@code Sequence}.
	 *
	 * @see #endingAt(Object)
	 * @see #endingAtNull()
	 * @see #until(Predicate)
	 * @see #generate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #recurse(Object, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> endingAt(Predicate<T> terminal) {
		return () -> new InclusiveTerminalIterator<>(terminal).backedBy(iterator());
	}

	/**
	 * Map the values in this {@code Sequence} to another set of values specified by the given {@code mapper} function.
	 *
	 * @see #flatten()
	 * @see #flatten(Function)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 */
	default <U> Sequence<U> map(Function<? super T, ? extends U> mapper) {
		return () -> new MappingIterator<>(mapper).backedBy(iterator());
	}

	/**
	 * Skip a set number of steps in this {@code Sequence}.
	 */
	default Sequence<T> skip(long skip) {
		return () -> new SkippingIterator<T>(skip).backedBy(iterator());
	}

	/**
	 * Limit the maximum number of results returned by this {@code Sequence}.
	 */
	default Sequence<T> limit(long limit) {
		return () -> new LimitingIterator<T>(limit).backedBy(iterator());
	}

	/**
	 * Append the elements of the given {@link Iterator} to the end of this {@code Sequence}.
	 * <p>
	 * The appended elements will only be available on the first traversal of the resulting {@code Sequence}.
	 */
	default Sequence<T> append(Iterator<T> iterator) {
		return append(Iterables.from(iterator));
	}

	/**
	 * Append the elements of the given {@link Iterable} to the end of this {@code Sequence}.
	 */
	default Sequence<T> append(Iterable<T> that) {
		@SuppressWarnings("unchecked")
		Iterable<T> chainingSequence = new ChainingIterable<>(this, that);
		return chainingSequence::iterator;
	}

	/**
	 * Append the given elements to the end of this {@code Sequence}.
	 */
	@SuppressWarnings("unchecked")
	default Sequence<T> append(T... objects) {
		return append(Iterables.from(objects));
	}

	/**
	 * Append the elements of the given {@link Stream} to the end of this {@code Sequence}.
	 * <p>
	 * The resulting {@code Sequence} can only be traversed once, further attempts to traverse will results in a
	 * {@link IllegalStateException}.
	 */
	default Sequence<T> append(Stream<T> stream) {
		return append(Iterables.from(stream));
	}

	/**
	 * Filter the elements in this {@code Sequence}, keeping only the elements that match the given {@link Predicate}.
	 */
	default Sequence<T> filter(Predicate<? super T> predicate) {
		return () -> new FilteringIterator<>(predicate).backedBy(iterator());
	}

	/**
	 * Flatten the elements in this {@code Sequence} according to the given mapper {@link Function}. The resulting
	 * {@code Sequence} contains the elements that is the result of applying the mapper {@link Function} to each
	 * element, appended together inline as a {@code Sequence}.
	 *
	 * @see #flatten()
	 * @see #map(Function)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 */
	default <U> Sequence<U> flatten(Function<? super T, ? extends Iterable<U>> mapper) {
		return ChainingIterable.flatMap(this, mapper)::iterator;
	}

	/**
	 * Flatten the elements in this {@code Sequence}. The resulting {@code Sequence} contains the elements that is the
	 * result of flattening each element, inline. Allowed elements that can be flattened are {@link Iterator},
	 * {@link Iterable}, {@code array}, {@link Pair} and {@link Stream}. Elements of another type will result in a
	 * {@link ClassCastException}.
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
		return ChainingIterable.<U>flatten(this)::iterator;
	}

	/**
	 * Collect the elements in this {@code Sequence} into an array.
	 */
	default Object[] toArray() {
		return toList().toArray();
	}

	/**
	 * Collect the elements in this {@code Sequence} into an array of the type determined by the given array
	 * constructor.
	 */
	default <A> A[] toArray(IntFunction<? extends A[]> constructor) {
		List list = toList();
		@SuppressWarnings("unchecked")
		A[] array = (A[]) list.toArray(constructor.apply(list.size()));
		return array;
	}

	/**
	 * Collect the elements in this {@code Sequence} into a {@link List}.
	 */
	default List<T> toList() {
		return toList(ArrayList::new);
	}

	/**
	 * Collect the elements in this {@code Sequence} into a {@link List} of the type determined by the given
	 * constructor.
	 */
	default List<T> toList(Supplier<? extends List<T>> constructor) {
		return toCollection(constructor);
	}

	/**
	 * Collect the elements in this {@code Sequence} into a {@link Set}.
	 */
	default Set<T> toSet() {
		return toSet(HashSet::new);
	}

	/**
	 * Collect the elements in this {@code Sequence} into a {@link Set} of the type determined by the given
	 * constructor.
	 */
	default <S extends Set<T>> S toSet(Supplier<? extends S> constructor) {
		return toCollection(constructor);
	}

	/**
	 * Collect the elements in this {@code Sequence} into a {@link SortedSet}.
	 */
	default SortedSet<T> toSortedSet() {
		return toSet(TreeSet::new);
	}

	/**
	 * Convert this {@code Sequence} of {@link Map.Entry} values into a {@link Map}.
	 *
	 * @throws ClassCastException if this {@code Sequence} is not of {@link Map.Entry}.
	 */
	default <K, V> Map<K, V> toMap() {
		@SuppressWarnings("unchecked")
		Function<? super T, ? extends Entry<K, V>> mapper =
				(Function<? super T, ? extends Entry<K, V>>) Function.<Entry<K, V>>identity();
		return toMap(mapper);
	}

	/**
	 * Convert this {@code Sequence} of into a {@link Map}, using the given mapper {@link Function} to convert each
	 * element into a {@link Map.Entry}.
	 */
	default <K, V> Map<K, V> toMap(Function<? super T, ? extends Entry<K, V>> mapper) {
		Supplier<Map<K, V>> supplier = HashMap::new;
		return toMap(supplier, mapper);
	}

	/**
	 * Convert this {@code Sequence} of into a {@link Map} of the type determined by the given constructor, using the
	 * given mapper {@link Function} to convert each element into a {@link Map.Entry}.
	 */
	default <M extends Map<K, V>, K, V> M toMap(Supplier<? extends M> constructor, Function<? super T, ? extends
			                                                                                                   Entry<K, V>> mapper) {
		M result = constructor.get();
		forEach(each -> Entries.put(result, mapper.apply(each)));
		return result;
	}

	/**
	 * Convert this {@code Sequence} of {@link Map.Entry} values into a {@link Map} of the type determined by the given
	 * constructor.
	 *
	 * @throws ClassCastException if this {@code Sequence} is not of {@link Map.Entry}.
	 */
	default <K, V> Map<K, V> toMap(Supplier<Map<K, V>> supplier) {
		@SuppressWarnings("unchecked")
		Function<T, Entry<K, V>> mapper = (Function<T, Entry<K, V>>) Function.<Entry<K, V>>identity();
		return toMap(supplier, mapper);
	}

	/**
	 * Convert this {@code Sequence} of into a {@link Map}, using the given key mapper {@link Function} and value
	 * mapper
	 * {@link Function} to convert each element into a {@link Map} entry.
	 */
	default <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper,
	                               Function<? super T, ? extends V> valueMapper) {
		return toMap(HashMap::new, keyMapper, valueMapper);
	}

	/**
	 * Convert this {@code Sequence} of into a {@link Map} of the type determined by the given constructor, using the
	 * given key mapper {@link Function} and value mapper {@link Function} to convert each element into a {@link Map}
	 * entry.
	 */
	default <M extends Map<K, V>, K, V> M toMap(Supplier<? extends M> constructor,
	                                            Function<? super T, ? extends K> keyMapper,
	                                            Function<? super T, ? extends V> valueMapper) {
		M result = constructor.get();
		forEach(each -> result.put(keyMapper.apply(each), valueMapper.apply(each)));
		return result;
	}

	/**
	 * Convert this {@code Sequence} of {@link Map.Entry} into a {@link SortedMap}.
	 *
	 * @throws ClassCastException if this {@code Sequence} is not of {@link Map.Entry}.
	 */
	default <K, V> SortedMap<K, V> toSortedMap() {
		Supplier<? extends SortedMap<K, V>> supplier = TreeMap::new;
		@SuppressWarnings("unchecked")
		Function<? super T, ? extends Entry<K, V>> mapper =
				(Function<? super T, ? extends Entry<K, V>>) Function.<Entry<K, V>>identity();
		return toMap(supplier, mapper);
	}

	/**
	 * Convert this {@code Sequence} of into a {@link SortedMap}, using the given mapper {@link Function} to convert
	 * each
	 * element into a {@link Map.Entry}.
	 */
	default <K, V> SortedMap<K, V> toSortedMap(Function<? super T, ? extends Entry<K, V>> mapper) {
		Supplier<? extends SortedMap<K, V>> supplier = TreeMap::new;
		return toMap(supplier, mapper);
	}

	/**
	 * Convert this {@code Sequence} into a {@link SortedMap}, using the given key mapper {@link Function} and value
	 * mapper {@link Function} to convert each element into a {@link SortedMap} entry.
	 */
	default <K, V> SortedMap<K, V> toSortedMap(Function<? super T, ? extends K> keyMapper,
	                                           Function<? super T, ? extends V> valueMapper) {
		return toMap(TreeMap::new, keyMapper, valueMapper);
	}

	/**
	 * Collect this {@code Sequence} into a {@link Collection} of the type determined by the given constructor.
	 */
	default <U extends Collection<T>> U toCollection(Supplier<? extends U> constructor) {
		return collect(constructor, Collection::add);
	}

	/**
	 * Collect this {@code Sequence} into an arbitrary container using the given constructor and adder.
	 */
	default <C> C collect(Supplier<? extends C> constructor, BiConsumer<? super C, ? super T> adder) {
		return collectInto(constructor.get(), adder);
	}

	/**
	 * Collect this {@code Sequence} into an arbitrary container using the given {@link Collector}.
	 */
	default <R, A> R collect(Collector<T, A, R> collector) {
		A container = collect(collector.supplier(), collector.accumulator());
		return collector.finisher().apply(container);
	}

	/**
	 * Collect this {@code Sequence} into the given {@link Collection}.
	 */
	default <U extends Collection<T>> U collectInto(U collection) {
		return collectInto(collection, Collection::add);
	}

	/**
	 * Collect this {@code Sequence} into the given container, using the given adder.
	 */
	default <C> C collectInto(C result, BiConsumer<? super C, ? super T> adder) {
		forEach(each -> adder.accept(result, each));
		return result;
	}

	/**
	 * Join this {@code Sequence} into a string separated by the given delimiter.
	 */
	default String join(String delimiter) {
		return join("", delimiter, "");
	}

	/**
	 * Join this {@code Sequence} into a string separated by the given delimiter, with the given prefix and suffix.
	 */
	default String join(String prefix, String delimiter, String suffix) {
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
		return Iterators.reduce(iterator(), operator);
	}

	/**
	 * Reduce this {@code Sequence} into a single element by iteratively applying the given binary operator to
	 * the current result and each element in this sequence, starting with the given identity as the initial result.
	 */
	default T reduce(@Nullable T identity, BinaryOperator<T> operator) {
		return Iterators.reduce(iterator(), identity, operator);
	}

	/**
	 * @return the first element of this {@code Sequence} or an empty {@link Optional} if there are no elements in the
	 * {@code Sequence}.
	 */
	default Optional<T> first() {
		Iterator<T> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	/**
	 * @return the second element of this {@code Sequence} or an empty {@link Optional} if there is one or less
	 * elements in the {@code Sequence}.
	 */
	default Optional<T> second() {
		Iterator<T> iterator = iterator();

		Iterators.skip(iterator);
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	/**
	 * @return the third element of this {@code Sequence} or an empty {@link Optional} if there is two or less
	 * elements in the {@code Sequence}.
	 */
	default Optional<T> third() {
		Iterator<T> iterator = iterator();

		Iterators.skip(iterator, 2);
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	/**
	 * @return the last element of this {@code Sequence} or an empty {@link Optional} if there are no
	 * elements in the {@code Sequence}.
	 */
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

	/**
	 * Pair the elements of this {@link Sequence} into a sequence of {@link Pair} elements. Each pair overlaps the
	 * second item with the first item of the next pair. If there is only one item in the list, the first pair returned
	 * has a null as the second item.
	 */
	default Sequence<Entry<T, T>> entries() {
		return () -> new PairingIterator<T, Entry<T, T>>(1) {
			@Override
			protected Entry<T, T> pair(T first, @Nullable T second) {
				return Entries.of(first, second);
			}
		}.backedBy(iterator());
	}

	/**
	 * Pair the elements of this {@link Sequence} into a sequence of {@link Pair} elements. Each pair overlaps the
	 * second item with the first item of the next pair. If there is only one item in the list, the first pair returned
	 * has a null as the second item.
	 */
	default Sequence<Pair<T, T>> pairs() {
		return () -> new PairingIterator<T, Pair<T, T>>(1) {
			@Override
			protected Pair<T, T> pair(T first, @Nullable T second) {
				return Pair.of(first, second);
			}
		}.backedBy(iterator());
	}

	/**
	 * Pair the elements of this {@link Sequence} into a sequence of {@link Pair} elements. Each pair overlaps the
	 * second item with the first item of the next pair. If there is only one item in the list, the first pair returned
	 * has a null as the second item.
	 */
	default Sequence<Entry<T, T>> adjacentEntries() {
		return () -> new PairingIterator<T, Entry<T, T>>(2) {
			@Override
			protected Entry<T, T> pair(T first, @Nullable T second) {
				return Entries.of(first, second);
			}
		}.backedBy(iterator());
	}

	/**
	 * Pair the elements of this {@link Sequence} into a sequence of {@link Pair} elements. Each pair overlaps the
	 * second item with the first item of the next pair. If there is only one item in the list, the first pair returned
	 * has a null as the second item.
	 */
	default Sequence<Pair<T, T>> adjacentPairs() {
		return () -> new PairingIterator<T, Pair<T, T>>(2) {
			@Override
			protected Pair<T, T> pair(T first, @Nullable T second) {
				return Pair.of(first, second);
			}
		}.backedBy(iterator());
	}

	/**
	 * Converts a {@code Sequence} of {@link Pair}s of items into a {@link BiSequence}. Note the sequence must be of
	 * {@link Pair} or a {@link ClassCastException} will occur when traversal is attempted.
	 */
	default <L, R> BiSequence<L, R> toBiSequence() {
		@SuppressWarnings("unchecked")
		Sequence<Pair<L, R>> pairSequence = (Sequence<Pair<L, R>>) this;
		return BiSequence.from(pairSequence);
	}

	/**
	 * Converts a {@code Sequence} of {@link Map.Entry} items into an {@link EntrySequence}. Note the sequence must be
	 * of {@link Map.Entry} or a {@link ClassCastException} will occur when traversal is attempted.
	 */
	default <K, V> EntrySequence<K, V> toEntrySequence() {
		@SuppressWarnings("unchecked")
		Sequence<Entry<K, V>> entrySequence = (Sequence<Entry<K, V>>) this;
		return EntrySequence.from(entrySequence);
	}

	/**
	 * Partition the elements of this {@link Sequence} into a sequence of {@link List}s of elements, each with the size
	 * of the given window. The first item in each list is the second item in the previous list. The final list may
	 * be shorter than the window.
	 */
	default Sequence<List<T>> partition(int window) {
		return () -> new PartitioningIterator<T>(window).backedBy(iterator());
	}

	/**
	 * Skip x number of steps in between each invocation of the iterator of this {@code Sequence}.
	 */
	default Sequence<T> step(long step) {
		return () -> new SteppingIterator<T>(step).backedBy(iterator());
	}

	/**
	 * @return a {@code Sequence} where each item in this {@code Sequence} occurs only once, the first time it is
	 * encountered.
	 */
	default Sequence<T> distinct() {
		return () -> new DistinctIterator<T>().backedBy(iterator());
	}

	/**
	 * @return this {@code Sequence} sorted according to the natural order. Must be a (@code Sequence} of
	 * {@link Comparable} or a {@link ClassCastException} is thrown during traversal.
	 */
	default <S extends Comparable<? super S>> Sequence<S> sorted() {
		return () -> {
			@SuppressWarnings("unchecked")
			Iterator<S> comparableIterator = (Iterator<S>) iterator();
			return new SortingIterator<S>().backedBy(comparableIterator);
		};
	}

	/**
	 * @return this {@code Sequence} sorted according to the given {@link Comparator}.
	 */
	default Sequence<T> sorted(Comparator<? super T> comparator) {
		return () -> new SortingIterator<>(comparator).backedBy(iterator());
	}

	/**
	 * @return the minimal element in this {@code Sequence} according to the given {@link Comparator}.
	 */
	default Optional<T> min(Comparator<? super T> comparator) {
		return reduce(minBy(comparator));
	}

	/**
	 * @return the maximum element in this {@code Sequence} according to the given {@link Comparator}.
	 */
	default Optional<T> max(Comparator<? super T> comparator) {
		return reduce(maxBy(comparator));
	}

	/**
	 * @return the count of elements in this sequence.
	 */
	default long count() {
		long count = 0;
		for (Iterator iterator = iterator(); iterator.hasNext(); iterator.next())
			count++;
		return count;
	}

	/**
	 * @return this {@code Sequence} as a {@link Stream}.
	 */
	default Stream<T> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * @return true if all elements in this {@code Sequence} satisfy the given predicate, false otherwise.
	 */
	default boolean all(Predicate<? super T> predicate) {
		for (T each : this) {
			if (!predicate.test(each))
				return false;
		}
		return true;
	}

	/**
	 * @return true if no elements in this {@code Sequence} satisfy the given predicate, false otherwise.
	 */
	default boolean none(Predicate<? super T> predicate) {
		return !any(predicate);
	}

	/**
	 * @return true if any element in this {@code Sequence} satisfies the given predicate, false otherwise.
	 */
	default boolean any(Predicate<? super T> predicate) {
		for (T each : this) {
			if (predicate.test(each))
				return true;
		}
		return false;
	}

	/**
	 * Allow the given {@link Consumer} to see each element in this {@code Sequence} as it is traversed.
	 */
	default Sequence<T> peek(Consumer<? super T> action) {
		return () -> new PeekingIterator<>(action).backedBy(iterator());
	}

	/**
	 * Delimit each element in this {@code Sequence} with the given delimiter element.
	 */
	@SuppressWarnings("unchecked")
	default <V extends R, R> Sequence<R> delimit(V delimiter) {
		return () -> new DelimitingIterator(Optional.empty(), Optional.of(delimiter), Optional.empty()).backedBy(
				iterator());
	}

	/**
	 * Delimit the elements in this {@code Sequence} with the given delimiter, prefix and suffix elements.
	 */
	@SuppressWarnings("unchecked")
	default <V extends R, R> Sequence<R> delimit(V prefix, V delimiter, V suffix) {
		return () -> new DelimitingIterator(Optional.of(prefix), Optional.of(delimiter), Optional.of(suffix)).backedBy(
				iterator());
	}

	/**
	 * Prefix the elements in this {@code Sequence} with the given prefix element.
	 */
	@SuppressWarnings("unchecked")
	default <V extends R, R> Sequence<R> prefix(V prefix) {
		return () -> new DelimitingIterator(Optional.of(prefix), Optional.empty(), Optional.empty()).backedBy(
				iterator());
	}

	/**
	 * Suffix the elements in this {@code Sequence} with the given suffix element.
	 */
	@SuppressWarnings("unchecked")
	default <V extends R, R> Sequence<R> suffix(V suffix) {
		return () -> new DelimitingIterator(Optional.empty(), Optional.empty(), Optional.of(suffix)).backedBy(
				iterator());
	}

	/**
	 * Interleave the elements in this {@code Sequence} with those of the given {@code Sequence}, stopping when either
	 * sequence finishes. The result is a {@code Sequence} of pairs of items, the first of which come from this
	 * sequence and the second from the given sequence.
	 */
	default <U> Sequence<Pair<T, U>> interleave(Sequence<U> that) {
		return () -> new InterleavingPairingIterator<>(iterator(), that.iterator());
	}

	/**
	 * @return a {@code Sequence} which iterates over this {@code Sequence} in reverse order.
	 */
	default Sequence<T> reverse() {
		return () -> new ReverseIterator<T>().backedBy(iterator());
	}

	/**
	 * @return a {@code Sequence} which iterates over this {@code Sequence} in random order.
	 */
	default Sequence<T> shuffle() {
		List<T> list = toList();
		Collections.shuffle(list);
		return from(list);
	}

	/**
	 * @return a {@code Sequence} which iterates over this {@code Sequence} in random order as determined by the given
	 * random generator.
	 */
	default Sequence<T> shuffle(Random md) {
		List<T> list = toList();
		Collections.shuffle(list, md);
		return from(list);
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
	default CharSeq toChars(ToCharFunction<T> mapper) {
		return () -> new DelegatingCharIterator<T, Iterator<T>>() {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.next());
			}
		}.backedBy(iterator());
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
	default IntSequence toInts(ToIntFunction<T> mapper) {
		return () -> new DelegatingIntIterator<T, Iterator<T>>() {
			@Override
			public int nextInt() {
				return mapper.applyAsInt(iterator.next());
			}
		}.backedBy(iterator());
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
	default LongSequence toLongs(ToLongFunction<T> mapper) {
		return () -> new DelegatingLongIterator<T, Iterator<T>>() {
			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.next());
			}
		}.backedBy(iterator());
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
	default DoubleSequence toDoubles(ToDoubleFunction<T> mapper) {
		return () -> new DelegatingDoubleIterator<T, Iterator<T>>() {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.next());
			}
		}.backedBy(iterator());
	}

	/**
	 * Repeat this {@code Sequence} forever, producing a sequence that never terminates unless the original sequence is
	 * empty in which case the resulting sequence is also empty.
	 */
	default Sequence<T> repeat() {
		return () -> new RepeatingIterator<>(this, -1);
	}

	/**
	 * Repeat this {@code Sequence} the given number of times.
	 */
	default Sequence<T> repeat(long times) {
		return () -> new RepeatingIterator<>(this, times);
	}
}
