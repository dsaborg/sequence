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

import org.d2ab.primitive.chars.BaseCharIterator;
import org.d2ab.primitive.doubles.BaseDoubleIterator;
import org.d2ab.primitive.ints.BaseIntIterator;
import org.d2ab.primitive.longs.*;
import org.d2ab.utils.MoreArrays;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Iterator;
import java.util.OptionalLong;
import java.util.function.*;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyIterator;

/**
 * An {@link Iterable} sequence of {@code long} values with {@link Stream}-like operations for refining,
 * transforming and collating the list of longs.
 */
@FunctionalInterface
public interface Longs extends LongIterable {
	/**
	 * Create empty {@code Longs} with no contents.
	 */
	@Nonnull
	static Longs empty() {
		return from(emptyIterator());
	}

	/**
	 * Create an {@code Longs} from an {@link Iterator} of {@code Long} values. Note that {@code Longs} created from {@link Iterator}s
	 * cannot be passed over more than once. Further attempts will register the {@code Longs} as empty.
	 */
	@Nonnull
	static Longs from(@Nonnull Iterator<Long> iterator) {
		return from(LongIterator.from(iterator));
	}

	/**
	 * Create a {@code Longs} from a {@link LongIterator} of long values. Note that {@code
	 * Longs}s created from {@link LongIterator}s cannot be passed over more than once. Further attempts
	 * will
	 * register the {@code Longs} as empty.
	 */
	@Nonnull
	static Longs from(@Nonnull LongIterator iterator) {
		return () -> iterator;
	}

	/**
	 * Create a {@code Longs} from a {@link LongIterable}.
	 */
	@Nonnull
	static Longs from(@Nonnull LongIterable iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a {@code Longs} with the given longs.
	 */
	@Nonnull
	static Longs of(@Nonnull long... cs) {
		return () -> new ArrayLongIterator(cs);
	}

	/**
	 * Create a {@code Sequence} from {@link Iterator}s of items supplied by the given {@link Supplier}. Every time
	 * the {@code Sequence} is to be iterated over, the {@link Supplier} is used to create the initial stream of
	 * elements. This is similar to creating a {@code Sequence} from an {@link Iterable}.
	 */
	@Nonnull
	static Longs from(@Nonnull Supplier<? extends LongIterator> iteratorSupplier) {
		return iteratorSupplier::get;
	}

	/**
	 * Create a {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created from {@link
	 * Stream}s cannot be passed over more than once. Further attempts will cause an {@link IllegalStateException}
	 * when the {@link Stream} is requested again.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 */
	static Longs from(Stream<Long> stream) {
		return from(stream::iterator);
	}

	/**
	 * Create a {@code Longs} from an {@link Iterable} of {@code Long} values.
	 */
	@Nonnull
	static Longs from(@Nonnull Iterable<Long> iterable) {
		return () -> LongIterator.from(iterable);
	}

	/**
	 * A {@code Sequence} of all the positive {@link Long} values starting at {@code 1} and ending at {@link Long#MAX_VALUE}.
	 */
	static Longs positive() {
		return startingAt(1);
	}

	/**
	 * A {@code Sequence} of all the {@link Long} values starting at the given value and ending at {@link
	 * Long#MAX_VALUE}.
	 */
	static Longs startingAt(long start) {
		return range(start, Long.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Long} values between the given start and end positions, inclusive.
	 */
	static Longs range(long start, long end) {
		LongUnaryOperator next = (end > start) ? x -> x + 1 : x -> x - 1;
		return recurse(start, next).endingAt(end);
	}

	default Longs endingAt(long terminal) {
		return () -> new InclusiveTerminalLongIterator(iterator(), terminal);
	}

	static Longs recurse(long seed, LongUnaryOperator op) {
		return () -> new RecursiveLongIterator(seed, op);
	}

	/**
	 * A {@code Sequence} of all the negative {@link Long} values starting at {@code -1} and ending at {@link Long#MIN_VALUE}.
	 */
	static Longs negative() {
		return range(-1L, Long.MIN_VALUE);
	}

	@Nonnull
	default Longs map(@Nonnull LongUnaryOperator mapper) {
		return () -> new MappingLongIterator(iterator(), mapper);
	}

	@Nonnull
	default Longs skip(long skip) {
		return () -> new SkippingLongIterator(iterator(), skip);
	}

	@Nonnull
	default Longs limit(long limit) {
		return () -> new LimitingLongIterator(iterator(), limit);
	}

	@Nonnull
	default Longs append(@Nonnull Iterable<Long> iterable) {
		return append(LongIterable.from(iterable));
	}

	@Nonnull
	default Longs append(@Nonnull LongIterable that) {
		return new ChainingLongIterable(this, that)::iterator;
	}

	default Longs append(LongIterator iterator) {
		return append(iterator.asIterable());
	}

	default Longs append(Iterator<Long> iterator) {
		return append(LongIterable.from(iterator));
	}

	default Longs append(long... longs) {
		return append(LongIterable.of(longs));
	}

	default Longs append(Stream<Long> stream) {
		return append(LongIterable.from(stream));
	}

	default Longs append(LongStream stream) {
		return append(LongIterable.from(stream));
	}

	@Nonnull
	default Longs filter(@Nonnull LongPredicate predicate) {
		return () -> new FilteringLongIterator(iterator(), predicate);
	}

	default Longs until(long terminal) {
		return () -> new ExclusiveTerminalLongIterator(iterator(), terminal);
	}

	default <C> C collect(Supplier<? extends C> constructor, ObjLongConsumer<? super C> adder) {
		C result = constructor.get();
		forEachLong(x -> adder.accept(result, x));
		return result;
	}

	default String join(String delimiter) {
		return join("", delimiter, "");
	}

	default String join(String prefix, String delimiter, String suffix) {
		StringBuilder result = new StringBuilder(prefix);

		boolean started = false;
		for (LongIterator iterator = iterator(); iterator.hasNext(); started = true) {
			long each = iterator.nextLong();
			if (started)
				result.append(delimiter);
			result.append(each);
		}

		return result.append(suffix).toString();
	}

	default long reduce(long identity, LongBinaryOperator operator) {
		return reduce(identity, operator, iterator());
	}

	default long reduce(long identity, LongBinaryOperator operator, LongIterator iterator) {
		long result = identity;
		while (iterator.hasNext())
			result = operator.applyAsLong(result, iterator.nextLong());
		return result;
	}

	default OptionalLong first() {
		LongIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalLong.empty();

		return OptionalLong.of(iterator.nextLong());
	}

	default OptionalLong second() {
		LongIterator iterator = iterator();

		iterator.skipOne();
		if (!iterator.hasNext())
			return OptionalLong.empty();

		return OptionalLong.of(iterator.nextLong());
	}

	default OptionalLong third() {
		LongIterator iterator = iterator();

		iterator.skipOne();
		iterator.skipOne();
		if (!iterator.hasNext())
			return OptionalLong.empty();

		return OptionalLong.of(iterator.nextLong());
	}

	default OptionalLong last() {
		LongIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalLong.empty();

		long last;
		do {
			last = iterator.nextLong();
		} while (iterator.hasNext());

		return OptionalLong.of(last);
	}

	default Longs step(long step) {
		return () -> new SteppingLongIterator(iterator(), step);
	}

	default Longs distinct() {
		return () -> new DistinctLongIterator(iterator());
	}

	default OptionalLong min() {
		return reduce((a, b) -> (a < b) ? a : b);
	}

	default OptionalLong reduce(LongBinaryOperator operator) {
		LongIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalLong.empty();

		long result = reduce(iterator.next(), operator, iterator);
		return OptionalLong.of(result);
	}

	default OptionalLong max() {
		return reduce((a, b) -> (a > b) ? a : b);
	}

	default long count() {
		long count = 0;
		for (LongIterator iterator = iterator(); iterator.hasNext(); iterator.nextLong()) {
			count++;
		}
		return count;
	}

	default boolean all(LongPredicate predicate) {
		for (LongIterator iterator = iterator(); iterator.hasNext(); ) {
			if (!predicate.test(iterator.nextLong()))
				return false;
		}
		return true;
	}

	default boolean none(LongPredicate predicate) {
		return !any(predicate);
	}

	default boolean any(LongPredicate predicate) {
		for (LongIterator iterator = iterator(); iterator.hasNext(); ) {
			if (predicate.test(iterator.nextLong()))
				return true;
		}
		return false;
	}

	default Longs peek(LongConsumer action) {
		return () -> new PeekingLongIterator(iterator(), action);
	}

	default Longs sorted() {
		long[] array = toArray();
		Arrays.sort(array);
		return () -> LongIterator.of(array);
	}

	default long[] toArray() {
		long[] work = new long[10];

		int index = 0;
		LongIterator iterator = iterator();
		while (iterator.hasNext()) {
			if (work.length < (index + 1)) {
				int newCapacity = work.length + (work.length >> 1);
				long[] newLongs = new long[newCapacity];
				System.arraycopy(work, 0, newLongs, 0, work.length);
				work = newLongs;
			}
			work[index++] = iterator.nextLong();
		}

		if (work.length == index) {
			return work; // Not very likely
		}

		long[] result = new long[index];
		System.arraycopy(work, 0, result, 0, index);
		return result;
	}

	default Longs prefix(long... cs) {
		return () -> new ChainingLongIterator(LongIterable.of(cs), this);
	}

	default Longs suffix(long... cs) {
		return () -> new ChainingLongIterator(this, LongIterable.of(cs));
	}

	default Longs interleave(Longs that) {
		return () -> new InterleavingLongIterator(this, that);
	}

	default Longs reverse() {
		long[] array = toArray();
		for (int i = 0; i < (array.length / 2); i++) {
			MoreArrays.swap(array, i, array.length - 1 - i);
		}
		return LongIterable.of(array)::iterator;
	}

	default Longs mapBack(BackPeekingLongFunction mapper) {
		return () -> new BackPeekingLongIterator(iterator(), mapper);
	}

	default Longs mapForward(ForwardPeekingLongFunction mapper) {
		return () -> new ForwardPeekingLongIterator(iterator(), mapper);
	}

	default Chars toChars() {
		return () -> new BaseCharIterator<Long, LongIterator>(iterator()) {
			@Override
			public char nextChar() {
				return (char) iterator.nextLong();
			}
		};
	}

	default Ints toInts() {
		return () -> new BaseIntIterator<Long, LongIterator>(iterator()) {
			@Override
			public int nextInt() {
				return (int) iterator.nextLong();
			}
		};
	}

	default Doubles toDoubles() {
		return () -> new BaseDoubleIterator<Long, LongIterator>(iterator()) {
			@Override
			public double nextDouble() {
				return iterator.nextLong();
			}
		};
	}

	default Chars toChars(LongToCharFunction mapper) {
		return () -> new BaseCharIterator<Long, LongIterator>(iterator()) {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.nextLong());
			}
		};
	}

	default Ints toInts(LongToIntFunction mapper) {
		return () -> new BaseIntIterator<Long, LongIterator>(iterator()) {
			@Override
			public int nextInt() {
				return mapper.applyAsInt(iterator.nextLong());
			}
		};
	}

	default Doubles toDoubles(LongToDoubleFunction mapper) {
		return () -> new BaseDoubleIterator<Long, LongIterator>(iterator()) {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextLong());
			}
		};
	}
}
