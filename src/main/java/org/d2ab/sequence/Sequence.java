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

import org.d2ab.collection.Maps;
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
		return ListSequence.empty();
	}

	/**
	 * Create a {@code Sequence} with one item.
	 *
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 */
	static <T> Sequence<T> of(T item) {
		return ListSequence.of(item);
	}

	/**
	 * Create a {@code Sequence} with the given items.
	 *
	 * @see #of(Object)
	 * @see #from(Iterable)
	 */
	@SafeVarargs
	static <T> Sequence<T> of(T... items) {
		return ListSequence.of(items);
	}

	/**
	 * Create a {@code Sequence} from an {@link Iterable} of items.
	 *
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #once(Iterator)
	 */
	static <T> Sequence<T> from(Iterable<T> iterable) {
		if (iterable instanceof List)
			return ListSequence.from((List<T>) iterable);

		return iterable::iterator;
	}

	/**
	 * Create a concatenated {@code Sequence} from several {@link Iterable}s which are concatenated together to form
	 * the stream of items in the {@code Sequence}.
	 *
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 *
	 * @since 1.1.1
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	static <T> Sequence<T> concat(Iterable<T>... iterables) {
		Sequence<Iterable<T>> iterableSequence = Sequence.of(iterables);
		if (iterableSequence.all(iterable -> iterable instanceof List))
			return ChainedListSequence.from(iterableSequence.map(iterable -> (List<T>) iterable).toList());

		return new ChainingIterable<>(iterables)::iterator;
	}

	/**
	 * Create a concatenated {@code Sequence} from several {@link Iterable}s which are concatenated together to form
	 * the stream of items in the {@code Sequence}.
	 *
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 *
	 * @since 1.1.1
	 */
	static <T> Sequence<T> concat(Iterable<Iterable<T>> iterables) {
		Sequence<Iterable<T>> iterableSequence = Sequence.from(iterables);
		if (iterableSequence.all(iterable -> iterable instanceof List))
			return ChainedListSequence.from(iterableSequence.map(iterable -> (List<T>) iterable).toList());

		return ChainingIterable.<T>flatten(iterables)::iterator;
	}

	/**
	 * Create a concatenated {@code Sequence} from several {@link Iterable}s which are concatenated together to form
	 * the stream of items in the {@code Sequence}.
	 *
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 *
	 * @deprecated Use {@link #concat(Iterable...)} instead.
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	@SafeVarargs
	static <T> Sequence<T> from(Iterable<T>... iterables) {
		return concat(iterables);
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
	 *
	 * @since 1.1
	 */
	static <T> Sequence<T> once(Iterator<T> iterator) {
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
	 *
	 * @since 1.1
	 */
	static <T> Sequence<T> once(Stream<T> stream) {
		return once(stream.iterator());
	}

	/**
	 * Create a once-only {@code Sequence} from an {@link Iterator} of items. Note that {@code Sequences} created from
	 * {@link Iterator}s cannot be passed over more than once. Further attempts will register the {@code Sequence} as
	 * empty.
	 *
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 * @see #cache(Iterator)
	 *
	 * @deprecated Use {@link Sequence#once(Iterator)} instead.
	 */
	@Deprecated
	static <T> Sequence<T> from(Iterator<T> iterator) {
		return once(iterator);
	}

	/**
	 * Create a once-only {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created from
	 * {@link Stream}s cannot be passed over more than once. Further attempts will register the {@code Sequence} as
	 * empty.
	 *
	 * @see #of(Object)
	 * @see #of(Object...)
	 * @see #from(Iterable)
	 * @see #once(Iterator)
	 * @see #cache(Stream)
	 *
	 * @deprecated Use {@link Sequence#once(Stream)} instead.
	 */
	@Deprecated
	static <T> Sequence<T> from(Stream<T> stream) {
		return once(stream);
	}

	/**
	 * Create a {@code Sequence} with a cached copy of an {@link Iterable} of items.
	 *
	 * @see #cache(Iterator)
	 * @see #cache(Stream)
	 * @see #from(Iterable)
	 *
	 * @since 1.1
	 */
	static <T> Sequence<T> cache(Iterable<T> iterable) {
		return from(Iterables.toList(iterable));
	}

	/**
	 * Create a {@code Sequence} with a cached copy of an {@link Iterator} of items.
	 *
	 * @see #cache(Iterable)
	 * @see #cache(Stream)
	 * @see #once(Iterator)
	 *
	 * @since 1.1
	 */
	static <T> Sequence<T> cache(Iterator<T> iterator) {
		return from(Iterators.toList(iterator));
	}

	/**
	 * Create a {@code Sequence} with a cached copy of a {@link Stream} of items.
	 *
	 * @see #cache(Iterable)
	 * @see #cache(Iterator)
	 * @see #once(Stream)
	 *
	 * @since 1.1
	 */
	static <T> Sequence<T> cache(Stream<T> stream) {
		return from(stream.collect(Collectors.toList()));
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
	 * @see #intsFromZero()
	 * @see #intsFrom(int)
	 * @see #range(int, int)
	 */
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
	 */
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
	 *
	 * @deprecated Use {@link #intsFrom(int)} instead.
	 */
	@Deprecated
	static Sequence<Integer> ints(int start) {
		return intsFrom(start);
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
	 *
	 * @since 1.1
	 */
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
	 */
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
	 */
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
	 */
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
	 *
	 * @deprecated Use {@link #longsFrom(long)} instead
	 */
	@Deprecated
	static Sequence<Long> longs(long start) {
		return longsFrom(start);
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
	 *
	 * @since 1.1
	 */
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
	 */
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
	 */
	static Sequence<Character> chars() {
		return range(Character.MIN_VALUE, Character.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Character} values starting at the given value and ending at {@link
	 * Character#MAX_VALUE} inclusive.
	 *
	 * @see #chars()
	 * @see #range(char, char)
	 *
	 * @deprecated Use {@link #charsFrom(char)} instead.
	 */
	@Deprecated
	static Sequence<Character> chars(char start) {
		return charsFrom(start);
	}

	/**
	 * A {@code Sequence} of all the {@link Character} values starting at the given value and ending at {@link
	 * Character#MAX_VALUE} inclusive.
	 *
	 * @see #chars()
	 * @see #range(char, char)
	 *
	 * @since 1.1
	 */
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
	 */
	static Sequence<Character> range(char start, char end) {
		UnaryOperator<Character> next = (end > start) ? c -> (char) (c + 1) : c -> (char) (c - 1);
		return recurse(start, next).endingAt(end);
	}

	/**
	 * @return an infinite {@code Sequence} generated by repeatedly calling the given supplier. The returned
	 * {@code Sequence} never terminates naturally. If {@link #iterator()} is called multiple times, further iterators
	 * will pick up where the previous iterator left off in the given supplier. If iterator calls are interleaved,
	 * calls to the supplier will be interleaved.
	 *
	 * @see #multiGenerate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #endingAt(Object)
	 * @see #until(Object)
	 */
	static <T> Sequence<T> generate(Supplier<? extends T> supplier) {
		return () -> (InfiniteIterator<T>) supplier::get;
	}

	/**
	 * @return an infinite {@code Sequence} where each {@link #iterator()} is generated by polling for a supplier and
	 * then using it to generate the sequence of elements. The sequence never terminates.
	 *
	 * @see #generate(Supplier)
	 * @see #recurse(Object, UnaryOperator)
	 * @see #endingAt(Object)
	 * @see #until(Object)
	 */
	static <T> Sequence<T> multiGenerate(Supplier<? extends Supplier<? extends T>> supplierSupplier) {
		return () -> {
			Supplier<? extends T> supplier = supplierSupplier.get();
			return (InfiniteIterator<T>) supplier::get;
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
	static <T, S> Sequence<S> recurse(T seed, Function<? super T, ? extends S> f, Function<? super S, ? extends T> g) {
		return () -> new RecursiveIterator<>(f.apply(seed), f.compose(g)::apply);
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
	default Sequence<T> endingAt(Predicate<T> terminal) {
		return () -> new InclusiveTerminalIterator<>(iterator(), terminal);
	}

	/**
	 * Begin this {@code Sequence} just after the given element is encountered, not including the element in the
	 * {@code Sequence}.
	 *
	 * @see #startingAfter(Predicate)
	 * @see #startingFrom(Object)
	 *
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
	 *
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
	 *
	 * @since 1.1
	 */
	default Sequence<T> startingAfter(Predicate<? super T> predicate) {
		return () -> new ExclusiveStartingIterator<>(iterator(), predicate);
	}

	/**
	 * Begin this {@code Sequence} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the first element in the {@code Sequence}.
	 *
	 * @see #startingFrom(Object)
	 * @see #startingAfter(Predicate)
	 *
	 * @since 1.1
	 */
	default Sequence<T> startingFrom(Predicate<? super T> predicate) {
		return () -> new InclusiveStartingIterator<>(iterator(), predicate);
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
		return () -> new MappingIterator<>(iterator(), mapper);
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
		return () -> new BackPeekingMappingIterator<T, U>(iterator(), replacement) {
			@Override
			protected U map(T previous, T next) {
				return mapper.apply(previous, next);
			}
		};
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
		return () -> new ForwardPeekingMappingIterator<T, U>(iterator(), replacement) {
			@Override
			protected T mapFollowing(boolean hasFollowing, T following) {
				return following;
			}

			@Override
			protected U mapNext(T next, T following) {
				return mapper.apply(next, following);
			}
		};
	}

	/**
	 * Skip a set number of steps in this {@code Sequence}.
	 */
	default Sequence<T> skip(long skip) {
		return () -> new SkippingIterator<>(iterator(), skip);
	}

	/**
	 * Skip a set number of steps at the end of this {@code Sequence}.
	 *
	 * @since 1.1
	 */
	default Sequence<T> skipTail(long skip) {
		if (skip == 0)
			return this;

		return () -> new TailSkippingIterator<>(iterator(), (int) skip);
	}

	/**
	 * Limit the maximum number of results returned by this {@code Sequence}.
	 */
	default Sequence<T> limit(long limit) {
		return () -> new LimitingIterator<>(iterator(), limit);
	}

	/**
	 * Append the given elements to the end of this {@code Sequence}.
	 */
	@SuppressWarnings("unchecked")
	default Sequence<T> append(T... items) {
		return append(Iterables.of(items));
	}

	/**
	 * Append the elements of the given {@link Iterable} to the end of this {@code Sequence}.
	 *
	 * @see #cache(Iterable)
	 */
	default Sequence<T> append(Iterable<T> iterable) {
		return new ChainingIterable<>(this, iterable)::iterator;
	}

	/**
	 * Append the elements of the given {@link Iterator} to the end of this {@code Sequence}.
	 * <p>
	 * The appended elements will only be available on the first traversal of the resulting {@code Sequence}.
	 *
	 * @see #cache(Iterator)
	 */
	default Sequence<T> append(Iterator<T> iterator) {
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
		return append(stream.iterator());
	}

	/**
	 * Filter the elements in this {@code Sequence}, keeping only the elements that match the given {@link Predicate}.
	 */
	default Sequence<T> filter(Predicate<? super T> predicate) {
		return () -> new FilteringIterator<>(iterator(), predicate);
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
		return () -> new ForwardPeekingFilteringIterator<>(iterator(), replacement, predicate);
	}

	/**
	 * @return a {@code Sequence} containing only the elements found in the given target array.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default Sequence<T> including(T... elements) {
		return filter(e -> Arrayz.contains(elements, e));
	}

	/**
	 * @return a {@code Sequence} containing only the elements found in the given target iterable.
	 *
	 * @since 1.2
	 */
	default Sequence<T> including(Iterable<? extends T> elements) {
		return filter(e -> Iterables.contains(elements, e));
	}

	/**
	 * @return a {@code Sequence} containing only the elements not found in the given target array.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default Sequence<T> excluding(T... elements) {
		return filter(e -> !Arrayz.contains(elements, e));
	}

	/**
	 * @return a {@code Sequence} containing only the elements not found in the given target iterable.
	 *
	 * @since 1.2
	 */
	default Sequence<T> excluding(Iterable<? extends T> elements) {
		return filter(e -> !Iterables.contains(elements, e));
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
	default <U> Sequence<U> flatten(Function<? super T, ? extends Iterable<U>> mapper) {
		return ChainingIterable.flatten(this, mapper)::iterator;
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
		List<T> list = toList();
		@SuppressWarnings("unchecked")
		A[] array = list.toArray(constructor.apply(list.size()));
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
		return toMap(HashMap::new);
	}

	/**
	 * Convert this {@code Sequence} of {@link Map.Entry} values into a {@link Map} of the type determined by the given
	 * constructor.
	 *
	 * @throws ClassCastException if this {@code Sequence} is not of {@link Map.Entry}.
	 */
	default <M extends Map<K, V>, K, V> M toMap(Supplier<? extends M> constructor) {
		@SuppressWarnings("unchecked")
		Sequence<Entry<K, V>> entrySequence = (Sequence<Entry<K, V>>) this;
		return entrySequence.collect(constructor, Maps::put);
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
		return collect(constructor,
		               (result, element) -> result.put(keyMapper.apply(element), valueMapper.apply(element)));
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
		return toMap(TreeMap::new, keyMapper, valueMapper);
	}

	/**
	 * Collect this {@code Sequence} into a {@link Collection} of the type determined by the given constructor.
	 */
	default <U extends Collection<T>> U toCollection(Supplier<? extends U> constructor) {
		return collectInto(constructor.get());
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
	default T reduce(T identity, BinaryOperator<T> operator) {
		return Iterators.reduce(iterator(), identity, operator);
	}

	/**
	 * @return the first element of this {@code Sequence} or an empty {@link Optional} if there are no elements in the
	 * {@code Sequence}.
	 */
	default Optional<T> first() {
		return get(0);
	}

	/**
	 * @return the second element of this {@code Sequence} or an empty {@link Optional} if there is one or less
	 * elements in the {@code Sequence}.
	 */
	default Optional<T> second() {
		return get(1);
	}

	/**
	 * @return the third element of this {@code Sequence} or an empty {@link Optional} if there is two or less
	 * elements in the {@code Sequence}.
	 */
	default Optional<T> third() {
		return get(2);
	}

	/**
	 * @return the element at the given index, or an empty {@link Optional} if the {@code Sequence} is smaller than
	 * the index.
	 */
	default Optional<T> get(long index) {
		return Iterators.get(iterator(), index);
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
		do
			last = iterator.next(); while (iterator.hasNext());

		return Optional.of(last);
	}

	/**
	 * Pair the elements of this {@code Sequence} into a sequence of overlapping {@link Entry} elements. Each entry
	 * overlaps the value item with the key item of the next entry. If there is only one item in the sequence, the
	 * first entry returned has that item as a key and null as the value.
	 */
	default Sequence<Entry<T, T>> entries() {
		return () -> new PairingIterator<T, Entry<T, T>>(iterator(), 1) {
			@Override
			protected Entry<T, T> pair(T first, T second) {
				return Maps.entry(first, second);
			}
		};
	}

	/**
	 * Pair the elements of this {@code Sequence} into a sequence of {@link Pair} elements. Each pair overlaps the
	 * second item with the first item of the next pair. If there is only one item in the list, the first pair returned
	 * has a null as the second item.
	 */
	default Sequence<Pair<T, T>> pairs() {
		return () -> new PairingIterator<T, Pair<T, T>>(iterator(), 1) {
			@Override
			protected Pair<T, T> pair(T first, T second) {
				return Pair.of(first, second);
			}
		};
	}

	/**
	 * Pair the elements of this {@code Sequence} into a sequence of {@link Entry} elements. Each entry is adjacent to
	 * the next entry. If there is an uneven amount of items in the list, the final entry returned has a null as the
	 * value item.
	 */
	default Sequence<Entry<T, T>> adjacentEntries() {
		return () -> new PairingIterator<T, Entry<T, T>>(iterator(), 2) {
			@Override
			protected Entry<T, T> pair(T first, T second) {
				return Maps.entry(first, second);
			}
		};
	}

	/**
	 * Pair the elements of this {@code Sequence} into a sequence of {@link Pair} elements. Each pair is adjacent to
	 * the next pair. If there is an uneven amount of items in the list, the final pair returned has a null as the
	 * second item.
	 */
	default Sequence<Pair<T, T>> adjacentPairs() {
		return () -> new PairingIterator<T, Pair<T, T>>(iterator(), 2) {
			@Override
			protected Pair<T, T> pair(T first, T second) {
				return Pair.of(first, second);
			}
		};
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
	 * Window the elements of this {@code Sequence} into a sequence of {@code Sequence}s of elements, each with the
	 * size of the given window. The first item in each sequence is the second item in the previous sequence. The final
	 * sequence may be shorter than the window. This method is equivalent to {@code window(window, 1)}.
	 */
	default Sequence<Sequence<T>> window(int window) {
		return window(window, 1);
	}

	/**
	 * Partition the elements of this {@code Sequence} into a sequence of {@code Sequence}s of elements, each with the
	 * size of the given window. The first item in each sequence is the second item in the previous sequence. The final
	 * sequence may be shorter than the window. This method is equivalent to {@code window(window)}.
	 *
	 * @deprecated Use {@link #window(int)} instead.
	 */
	@Deprecated
	default Sequence<Sequence<T>> partition(int window) {
		return window(window);
	}

	/**
	 * Window the elements of this {@code Sequence} into a sequence of {@code Sequence}s of elements, each with the
	 * size of the given window, stepping {@code step} elements between each window. If the given step is less than the
	 * window size, the windows will overlap each other. If the step is larger than the window size, elements will be
	 * skipped in between windows.
	 */
	default Sequence<Sequence<T>> window(int window, int step) {
		return () -> new WindowingIterator<T, Sequence<T>>(iterator(), window, step) {
			@Override
			protected Sequence<T> toSequence(List<T> list) {
				return ListSequence.from(list);
			}
		};
	}

	/**
	 * Batch the elements of this {@code Sequence} into a sequence of {@code Sequence}s of distinct elements, each with
	 * the given batch size. This method is equivalent to {@code window(size, size)}.
	 */
	default Sequence<Sequence<T>> batch(int size) {
		return window(size, size);
	}

	/**
	 * Batch the elements of this {@code Sequence} into a sequence of {@code Sequence}s of distinct elements, where the
	 * given predicate determines where to split the lists of partitioned elements. The predicate is given the current
	 * and next item in the iteration, and if it returns true a partition is created between the elements.
	 */
	default Sequence<Sequence<T>> batch(BiPredicate<? super T, ? super T> predicate) {
		return () -> new PredicatePartitioningIterator<T, Sequence<T>>(iterator(), predicate) {
			@Override
			protected Sequence<T> toSequence(List<T> list) {
				return ListSequence.from(list);
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
		return () -> new SplittingIterator<T, Sequence<T>>(iterator(), element) {
			@Override
			protected Sequence<T> toSequence(List<T> list) {
				return ListSequence.from(list);
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
		return () -> new SplittingIterator<T, Sequence<T>>(iterator(), predicate) {
			@Override
			protected Sequence<T> toSequence(List<T> list) {
				return ListSequence.from(list);
			}
		};
	}

	/**
	 * Skip x number of steps in between each invocation of the iterator of this {@code Sequence}.
	 */
	default Sequence<T> step(long step) {
		return () -> new SteppingIterator<>(iterator(), step);
	}

	/**
	 * @return a {@code Sequence} where each item in this {@code Sequence} occurs only once, the first time it is
	 * encountered.
	 */
	default Sequence<T> distinct() {
		return () -> new DistinctIterator<>(iterator());
	}

	/**
	 * @return this {@code Sequence} sorted according to the natural order. Must be a (@code Sequence} of
	 * {@link Comparable} or a {@link ClassCastException} is thrown during traversal.
	 */
	@SuppressWarnings("unchecked")
	default Sequence<T> sorted() {
		return () -> new SortingIterator<>(iterator());
	}

	/**
	 * @return this {@code Sequence} sorted according to the given {@link Comparator}.
	 */
	default Sequence<T> sorted(Comparator<? super T> comparator) {
		return () -> new SortingIterator<>(iterator(), comparator);
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
	 * @return the count of elements in this {@code Sequence}.
	 */
	default long count() {
		long count = 0;
		for (T ignored : this)
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
		return Iterables.all(this, predicate);
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
		return Iterables.any(this, predicate);
	}

	/**
	 * Allow the given {@link Consumer} to see each element in this {@code Sequence} as it is traversed.
	 */
	default Sequence<T> peek(Consumer<? super T> action) {
		return () -> new PeekingIterator<>(iterator(), action);
	}

	/**
	 * Allow the given {@link BiConsumer} to see each and its following element in this {@code Sequence} as it is
	 * traversed. In the last iteration, the following item will be null.
	 *
	 * @see #peekForward(Object, BiConsumer)
	 */
	default Sequence<T> peekForward(BiConsumer<? super T, ? super T> action) {
		return peekForward(null, action);
	}

	/**
	 * Allow the given {@link BiConsumer} to see each and its previous element in this {@code Sequence} as it is
	 * traversed. In the first iteration, the previous item will be null.
	 *
	 * @see #peekBack(Object, BiConsumer)
	 */
	default Sequence<T> peekBack(BiConsumer<? super T, ? super T> action) {
		return peekBack(null, action);
	}

	/**
	 * Allow the given {@link BiConsumer} to see each and its following element in this {@code Sequence} as it is
	 * traversed. In the last iteration, the following item will have the given replacement value.
	 *
	 * @see #peekForward(BiConsumer)
	 */
	default Sequence<T> peekForward(T replacement, BiConsumer<? super T, ? super T> action) {
		return () -> new ForwardPeekingMappingIterator<T, T>(iterator(), replacement) {
			@Override
			protected T mapNext(T next, T following) {
				action.accept(next, following);
				return next;
			}

			@Override
			protected T mapFollowing(boolean hasFollowing, T following) {
				return following;
			}
		};
	}

	/**
	 * Allow the given {@link BiConsumer} to see each and its previous element in this {@code Sequence} as it is
	 * traversed. In the first iteration, the previous item will have the given replacement value.
	 *
	 * @see #peekBack(BiConsumer)
	 */
	default Sequence<T> peekBack(T replacement, BiConsumer<? super T, ? super T> action) {
		return () -> new BackPeekingMappingIterator<T, T>(iterator(), replacement) {
			@Override
			protected T map(T previous, T next) {
				action.accept(previous, next);
				return next;
			}
		};
	}

	/**
	 * Delimit each element in this {@code Sequence} with the given delimiter element.
	 */
	@SuppressWarnings("unchecked")
	default <U, V> Sequence<U> delimit(V delimiter) {
		return () -> new DelimitingIterator<>((Iterator<U>) iterator(), Optional.empty(), Optional.of(delimiter),
		                                      Optional.empty());
	}

	/**
	 * Delimit the elements in this {@code Sequence} with the given delimiter, prefix and suffix elements.
	 */
	@SuppressWarnings("unchecked")
	default <U, V> Sequence<U> delimit(V prefix, V delimiter, V suffix) {
		return () -> new DelimitingIterator<>((Iterator<U>) iterator(), Optional.of(prefix), Optional.of(delimiter),
		                                      Optional.of(suffix));
	}

	/**
	 * Prefix the elements in this {@code Sequence} with the given prefix element.
	 */
	@SuppressWarnings("unchecked")
	default <U, V> Sequence<U> prefix(V prefix) {
		return () -> new DelimitingIterator<>((Iterator<U>) iterator(), Optional.of(prefix), Optional.empty(),
		                                      Optional.empty());
	}

	/**
	 * Suffix the elements in this {@code Sequence} with the given suffix element.
	 */
	@SuppressWarnings("unchecked")
	default <U, V> Sequence<U> suffix(V suffix) {
		return () -> new DelimitingIterator<>((Iterator<U>) iterator(), Optional.empty(), Optional.empty(),
		                                      Optional.of(suffix));
	}

	/**
	 * Interleave the elements in this {@code Sequence} with those of the given {@link Iterable}, stopping when either
	 * sequence finishes. The result is a {@code Sequence} of pairs of items, the left entry coming from this
	 * sequence and the right entry from the given iterable.
	 */
	default <U> Sequence<Pair<T, U>> interleave(Iterable<U> that) {
		return () -> new InterleavingPairingIterator<>(iterator(), that.iterator());
	}

	/**
	 * @return a {@code Sequence} which iterates over this {@code Sequence} in reverse order.
	 */
	default Sequence<T> reverse() {
		return () -> new ReverseIterator<>(iterator());
	}

	/**
	 * @return a {@code Sequence} which iterates over this {@code Sequence} in random order.
	 */
	default Sequence<T> shuffle() {
		return () -> {
			List<T> list = toList();
			Collections.shuffle(list);
			return list.iterator();
		};
	}

	/**
	 * @return a {@code Sequence} which iterates over this {@code Sequence} in random order as determined by the given
	 * random generator.
	 */
	default Sequence<T> shuffle(Random md) {
		return () -> {
			List<T> list = toList();
			Collections.shuffle(list, md);
			return list.iterator();
		};
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
		return () -> CharIterator.from(iterator(), mapper);
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
		return () -> IntIterator.from(iterator(), mapper);
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
		return () -> LongIterator.from(iterator(), mapper);
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
		return () -> DoubleIterator.from(iterator(), mapper);
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

	/**
	 * Tests each pair of items in the sequence and swaps any two items which match the given predicate.
	 */
	default Sequence<T> swap(BiPredicate<? super T, ? super T> swapper) {
		return () -> new SwappingIterator<>(iterator(), swapper);
	}

	/**
	 * @return a {@link BiSequence} of this sequence paired up with the index of each element.
	 */
	default BiSequence<Long, T> index() {
		return () -> new MappedReferenceIterator<T, Pair<Long, T>>(iterator()) {
			private long index;

			@Override
			public Pair<Long, T> next() {
				return Pair.of(index++, iterator.next());
			}
		};
	}

	/**
	 * Remove all elements matched by this sequence using {@link Iterator#remove()}.
	 */
	default void removeAll() {
		Iterables.removeAll(this);
	}

	/**
	 * @return true if this {@code Sequence} is empty, false otherwise.
	 *
	 * @since 1.1
	 */
	default boolean isEmpty() {
		return !iterator().hasNext();
	}

	/**
	 * @return true if this {@code Sequence} contains the given item, false otherwise.
	 *
	 * @since 1.2
	 */
	default boolean contains(T item) {
		return Iterables.contains(this, item);
	}

	/**
	 * @return true if this {@code Sequence} contains all of the given items, false otherwise.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default boolean containsAll(T... items) {
		return Iterables.containsAll(this, items);
	}

	/**
	 * @return true if this {@code Sequence} contains all of the given items, false otherwise.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default boolean containsAll(Iterable<? extends T> items) {
		return Iterables.containsAll(this, items);
	}

	/**
	 * @return true if this {@code Sequence} contains any of the given items, false otherwise.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default boolean containsAny(T... items) {
		return Iterables.containsAny(this, items);
	}

	/**
	 * @return true if this {@code Sequence} contains any of the given items, false otherwise.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default boolean containsAny(Iterable<? extends T> items) {
		return Iterables.containsAny(this, items);
	}
}
