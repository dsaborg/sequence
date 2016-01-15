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
import org.d2ab.iterable.Iterables;
import org.d2ab.iterator.*;
import org.d2ab.primitive.chars.DelegatingCharIterator;
import org.d2ab.primitive.chars.ToCharFunction;
import org.d2ab.primitive.doubles.DelegatingDoubleIterator;
import org.d2ab.primitive.ints.DelegatingIntIterator;
import org.d2ab.primitive.longs.DelegatingLongIterator;

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

/**
 * An {@link Iterable} sequence of elements with {@link Stream}-like operations for refining, transforming and collating
 * the list of elements.
 */
@FunctionalInterface
public interface Sequence<T> extends Iterable<T> {
	/**
	 * Create an empty {@code Sequence} with no items.
	 *
	 * @see #of(T)
	 * @see #of(T...)
	 * @see #from(Iterable)
	 */
	static <T> Sequence<T> empty() {
		return from(emptyIterator());
	}

	/**
	 * Create a {@code Sequence} from an {@link Iterator} of items. Note that {@code Sequences} created from {@link
	 * Iterator}s cannot be passed over more than once. Further attempts will register the {@code Sequence} as empty.
	 *
	 * @see #of(T)
	 * @see #of(T...)
	 * @see #from(Iterable)
	 */
	static <T> Sequence<T> from(Iterator<T> iterator) {
		return () -> iterator;
	}

	/**
	 * Create a {@code Sequence} with one item.
	 *
	 * @see #of(T...)
	 * @see #from(Iterable)
	 */
	static <T> Sequence<T> of(@Nullable T item) {
		return from(singleton(item));
	}

	/**
	 * Create a {@code Sequence} from an {@link Iterable} of items.
	 *
	 * @see #of(T)
	 * @see #of(T...)
	 */
	static <T> Sequence<T> from(Iterable<T> iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a {@code Sequence} with the given items.
	 *
	 * @see #of(T)
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
	 * @see #of(T)
	 * @see #of(T...)
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
	 * @see #of(T)
	 * @see #of(T...)
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
	 * @see #of(T)
	 * @see #of(T...)
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
	 * @see #recurse(T, UnaryOperator)
	 * @see #endingAt(T)
	 * @see #until(T)
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
	 * @see #recurse(T, Function, Function)
	 * @see #generate(Supplier)
	 * @see #endingAt(T)
	 * @see #until(T)
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
	 * @see #recurse(T, UnaryOperator)
	 * @see #endingAt(T)
	 * @see #until(T)
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
	 * @see #endingAt(T)
	 * @see #generate(Supplier)
	 * @see #recurse(T, UnaryOperator)
	 * @see #recurse(T, Function, Function)
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
	 * @see #until(T)
	 * @see #generate(Supplier)
	 * @see #recurse(T, UnaryOperator)
	 * @see #recurse(T, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> endingAt(@Nullable T terminal) {
		return () -> new InclusiveTerminalIterator<>(terminal).backedBy(iterator());
	}

	/**
	 * Terminate this {@code Sequence} just before a null element is encountered, not including the null in the
	 * {@code Sequence}.
	 *
	 * @see #until(T)
	 * @see #until(Predicate)
	 * @see #endingAtNull
	 * @see #generate(Supplier)
	 * @see #recurse(T, UnaryOperator)
	 * @see #recurse(T, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> untilNull() {
		return () -> new ExclusiveTerminalIterator<>((T) null).backedBy(iterator());
	}

	/**
	 * Terminate this {@code Sequence} when a null element is encountered, including the null as the last element
	 * in the {@code Sequence}.
	 *
	 * @see #endingAt(T)
	 * @see #endingAt(Predicate)
	 * @see #untilNull
	 * @see #generate(Supplier)
	 * @see #recurse(T, UnaryOperator)
	 * @see #recurse(T, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> endingAtNull() {
		return () -> new InclusiveTerminalIterator<>((T) null).backedBy(iterator());
	}

	/**
	 * Terminate this {@code Sequence} just before the given predicate is satisfied, not including the element that
	 * satisfies the predicate in the {@code Sequence}.
	 *
	 * @see #until(T)
	 * @see #untilNull()
	 * @see #endingAt(Predicate)
	 * @see #generate(Supplier)
	 * @see #recurse(T, UnaryOperator)
	 * @see #recurse(T, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> until(Predicate<T> terminal) {
		return () -> new ExclusiveTerminalIterator<>(terminal).backedBy(iterator());
	}

	/**
	 * Terminate this {@code Sequence} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the last element in the {@code Sequence}.
	 *
	 * @see #endingAt(T)
	 * @see #endingAtNull()
	 * @see #until(Predicate)
	 * @see #generate(Supplier)
	 * @see #recurse(T, UnaryOperator)
	 * @see #recurse(T, Function, Function)
	 * @see #repeat()
	 */
	default Sequence<T> endingAt(Predicate<T> terminal) {
		return () -> new InclusiveTerminalIterator<>(terminal).backedBy(iterator());
	}

	/**
	 * Map the values in this sequence to another set of values specified by the given {@code mapper} function.
	 */
	default <U> Sequence<U> map(Function<? super T, ? extends U> mapper) {
		return () -> new MappingIterator<>(mapper).backedBy(iterator());
	}

	default Sequence<T> skip(long skip) {
		return () -> new SkippingIterator<T>(skip).backedBy(iterator());
	}

	default Sequence<T> limit(long limit) {
		return () -> new LimitingIterator<T>(limit).backedBy(iterator());
	}

	default Sequence<T> append(Iterator<T> iterator) {
		return append(Iterables.from(iterator));
	}

	default Sequence<T> append(Iterable<T> that) {
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

	default Sequence<T> filter(Predicate<? super T> predicate) {
		return () -> new FilteringIterator<>(predicate).backedBy(iterator());
	}

	default <U> Sequence<U> flatMap(Function<? super T, ? extends Iterable<U>> mapper) {
		return ChainingIterable.flatMap(this, mapper)::iterator;
	}

	default <U> Sequence<U> flatten() {
		return ChainingIterable.<U>flatten(this)::iterator;
	}

	default Object[] toArray() {
		return toList().toArray();
	}

	default <A> A[] toArray(IntFunction<? extends A[]> constructor) {
		List list = toList();
		@SuppressWarnings("unchecked")
		A[] array = (A[]) list.toArray(constructor.apply(list.size()));
		return array;
	}

	default List<T> toList() {
		return toList(ArrayList::new);
	}

	default List<T> toList(Supplier<? extends List<T>> constructor) {
		return toCollection(constructor);
	}

	default Set<T> toSet() {
		return toSet(HashSet::new);
	}

	default <S extends Set<T>> S toSet(Supplier<? extends S> constructor) {
		return toCollection(constructor);
	}

	default SortedSet<T> toSortedSet() {
		return toSet(TreeSet::new);
	}

	/**
	 * Convert this {@code Sequence} of {@link Entry} values into a map, or throw {@code ClassCastException} if this
	 * {@code Sequence} is not of {@link Entry}.
	 */
	default <K, V> Map<K, V> toMap() {
		@SuppressWarnings("unchecked")
		Function<? super T, ? extends Entry<K, V>> mapper = (Function<? super T, ? extends Entry<K, V>>) Function
				                                                                                                 .<Entry<K, V>>identity();
		return toMap(mapper);
	}

	default <K, V> Map<K, V> toMap(Function<? super T, ? extends Entry<K, V>> mapper) {
		Supplier<Map<K, V>> supplier = HashMap::new;
		return toMap(supplier, mapper);
	}

	default <M extends Map<K, V>, K, V> M toMap(Supplier<? extends M> constructor, Function<? super T, ? extends
			                                                                                                   Entry<K, V>> mapper) {
		M result = constructor.get();
		forEach(each -> Pair.put(result, mapper.apply(each)));
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

	default <U extends Collection<T>> U toCollection(Supplier<? extends U> constructor) {
		return collect(constructor, Collection::add);
	}

	default <C> C collect(Supplier<? extends C> constructor, BiConsumer<? super C, ? super T> adder) {
		C result = constructor.get();
		forEach(each -> adder.accept(result, each));
		return result;
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

	default T reduce(@Nullable T identity, BinaryOperator<T> operator) {
		return reduce(identity, operator, iterator());
	}

	default T reduce(@Nullable T identity, BinaryOperator<T> operator, Iterator<? extends T> iterator) {
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

		Iterators.skip(iterator);
		if (!iterator.hasNext())
			return Optional.empty();

		return Optional.of(iterator.next());
	}

	default Optional<T> third() {
		Iterator<T> iterator = iterator();

		Iterators.skip(iterator, 2);
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
		return () -> new PairingIterator<T>().backedBy(iterator());
	}

	default Sequence<List<T>> partition(int window) {
		return () -> new PartitioningIterator<T>(window).backedBy(iterator());
	}

	default Sequence<T> step(long step) {
		return () -> new SteppingIterator<T>(step).backedBy(iterator());
	}

	default Sequence<T> distinct() {
		return () -> new DistinctIterator<T>().backedBy(iterator());
	}

	default <S extends Comparable<? super S>> Sequence<S> sorted() {
		return () -> {
			@SuppressWarnings("unchecked")
			Iterator<S> comparableIterator = (Iterator<S>) iterator();
			return new SortingIterator<S>().backedBy(comparableIterator);
		};
	}

	default Sequence<T> sorted(Comparator<? super T> comparator) {
		return () -> new SortingIterator<>(comparator).backedBy(iterator());
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
		return () -> new PeekingIterator<>(action).backedBy(iterator());
	}

	@SuppressWarnings("unchecked")
	default <V extends R, R> Sequence<R> delimit(V delimiter) {
		return () -> new DelimitingIterator(Optional.empty(), Optional.of(delimiter), Optional.empty()).backedBy(
				iterator());
	}

	@SuppressWarnings("unchecked")
	default <V extends R, R> Sequence<R> delimit(V prefix, V delimiter, V suffix) {
		return () -> new DelimitingIterator(Optional.of(prefix), Optional.of(delimiter), Optional.of(suffix)).backedBy(
				iterator());
	}

	@SuppressWarnings("unchecked")
	default <V extends R, R> Sequence<R> prefix(V prefix) {
		return () -> new DelimitingIterator(Optional.of(prefix), Optional.empty(), Optional.empty()).backedBy(
				iterator());
	}

	@SuppressWarnings("unchecked")
	default <V extends R, R> Sequence<R> suffix(V suffix) {
		return () -> new DelimitingIterator(Optional.empty(), Optional.empty(), Optional.of(suffix)).backedBy(
				iterator());
	}

	default <U> Sequence<Pair<T, U>> interleave(Sequence<U> that) {
		return () -> new InterleavingPairingIterator<>(iterator(), that.iterator());
	}

	default Sequence<T> reverse() {
		return () -> new ReverseIterator<T>().backedBy(iterator());
	}

	default Sequence<T> shuffle() {
		List<T> list = toList();
		Collections.shuffle(list);
		return from(list);
	}

	default Sequence<T> shuffle(Random md) {
		List<T> list = toList();
		Collections.shuffle(list, md);
		return from(list);
	}

	default CharSeq mapToChar(ToCharFunction<T> mapper) {
		return () -> new DelegatingCharIterator<T, Iterator<T>>() {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.next());
			}
		}.backedBy(iterator());
	}

	default IntSeq mapToInt(ToIntFunction<T> mapper) {
		return () -> new DelegatingIntIterator<T, Iterator<T>>() {
			@Override
			public int nextInt() {
				return mapper.applyAsInt(iterator.next());
			}
		}.backedBy(iterator());
	}

	default LongSeq mapToLong(ToLongFunction<T> mapper) {
		return () -> new DelegatingLongIterator<T, Iterator<T>>() {
			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.next());
			}
		}.backedBy(iterator());
	}

	default DoubleSeq mapToDouble(ToDoubleFunction<T> mapper) {
		return () -> new DelegatingDoubleIterator<T, Iterator<T>>() {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.next());
			}
		}.backedBy(iterator());
	}

	default Sequence<T> repeat() {
		return () -> new RepeatingIterator<>(this);
	}
}
