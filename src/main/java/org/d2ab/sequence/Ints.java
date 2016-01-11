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

import org.d2ab.primitive.chars.DelegatingCharIterator;
import org.d2ab.primitive.doubles.DelegatingDoubleIterator;
import org.d2ab.primitive.ints.*;
import org.d2ab.primitive.longs.DelegatingLongIterator;
import org.d2ab.utils.MoreArrays;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Iterator;
import java.util.OptionalInt;
import java.util.function.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyIterator;

/**
 * An {@link Iterable} sequence of {@code int} values with {@link Stream}-like operations for refining,
 * transforming and collating the list of ints.
 */
@FunctionalInterface
public interface Ints extends IntIterable {
	/**
	 * Create empty {@code Ints} with no contents.
	 */
	@Nonnull
	static Ints empty() {
		return from(emptyIterator());
	}

	/**
	 * Create an {@code Ints} from an {@link Iterator} of {@code Integer} values. Note that {@code Ints} created from
	 * {@link Iterator}s cannot be passed over more than once. Further attempts will register the {@code Ints} as
	 * empty.
	 */
	@Nonnull
	static Ints from(@Nonnull Iterator<Integer> iterator) {
		return from(IntIterator.from(iterator));
	}

	/**
	 * Create a {@code Ints} from a {@link IntIterator} of int values. Note that {@code
	 * Ints}s created from {@link IntIterator}s cannot be passed over more than once. Further attempts
	 * will
	 * register the {@code Ints} as empty.
	 */
	@Nonnull
	static Ints from(@Nonnull IntIterator iterator) {
		return () -> iterator;
	}

	/**
	 * Create a {@code Ints} from a {@link IntIterable}.
	 */
	@Nonnull
	static Ints from(@Nonnull IntIterable iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a {@code Ints} with the given ints.
	 */
	@Nonnull
	static Ints of(@Nonnull int... cs) {
		return () -> new ArrayIntIterator(cs);
	}

	/**
	 * Create a {@code Sequence} from {@link Iterator}s of items supplied by the given {@link Supplier}. Every time
	 * the {@code Sequence} is to be iterated over, the {@link Supplier} is used to create the initial stream of
	 * elements. This is similar to creating a {@code Sequence} from an {@link Iterable}.
	 */
	@Nonnull
	static Ints from(@Nonnull Supplier<? extends IntIterator> iteratorSupplier) {
		return iteratorSupplier::get;
	}

	/**
	 * Create a {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created from {@link
	 * Stream}s cannot be passed over more than once. Further attempts will cause an {@link IllegalStateException}
	 * when the {@link Stream} is requested again.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 */
	static Ints from(Stream<Integer> stream) {
		return from(stream::iterator);
	}

	/**
	 * Create a {@code Ints} from an {@link Iterable} of {@code Integer} values.
	 */
	@Nonnull
	static Ints from(@Nonnull Iterable<Integer> iterable) {
		return () -> IntIterator.from(iterable);
	}

	/**
	 * A {@code Sequence} of all the positive {@link Integer} values starting at {@code 1} and ending at
	 * {@link Integer#MAX_VALUE}.
	 */
	static Ints positive() {
		return startingAt(1);
	}

	/**
	 * A {@code Sequence} of all the {@link Integer} values starting at the given value and ending at {@link
	 * Integer#MAX_VALUE}.
	 */
	static Ints startingAt(int start) {
		return range(start, Integer.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Integer} values between the given start and end positions, inclusive.
	 */
	static Ints range(int start, int end) {
		IntUnaryOperator next = (end > start) ? x -> ++x : x -> --x;
		return recurse(start, next).endingAt(end);
	}

	static Ints recurse(int seed, IntUnaryOperator op) {
		return () -> new InfiniteIntIterator() {
			private int previous;
			private boolean hasPrevious;

			@Override
			public int nextInt() {
				previous = hasPrevious ? op.applyAsInt(previous) : seed;
				hasPrevious = true;
				return previous;
			}
		};
	}

	/**
	 * A {@code Sequence} of all the negative {@link Integer} values starting at {@code -1} and ending at
	 * {@link Integer#MIN_VALUE}.
	 */
	static Ints negative() {
		return range(-1, Integer.MIN_VALUE);
	}

	/**
	 * @return a sequence of {@code Ints} that is generated from the given supplier and thus never terminates.
	 */
	static Ints generate(IntSupplier supplier) {
		return () -> (InfiniteIntIterator) supplier::getAsInt;
	}

	default Ints endingAt(int terminal) {
		return () -> new InclusiveTerminalIntIterator(terminal).backedBy(iterator());
	}

	@Nonnull
	default Ints map(@Nonnull IntUnaryOperator mapper) {
		return () -> new UnaryIntIterator() {
			@Override
			public int nextInt() {
				return mapper.applyAsInt(iterator.nextInt());
			}
		}.backedBy(iterator());
	}

	default Sequence<Integer> box() {
		return toSequence(Integer::valueOf);
	}

	@Nonnull
	default <T> Sequence<T> toSequence(@Nonnull IntFunction<T> mapper) {
		return () -> new Iterator<T>() {
			private final IntIterator iterator = iterator();

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				return mapper.apply(iterator.nextInt());
			}
		};
	}

	@Nonnull
	default Ints skip(long skip) {
		return () -> new SkippingIntIterator(skip).backedBy(iterator());
	}

	@Nonnull
	default Ints limit(long limit) {
		return () -> new LimitingIntIterator(limit).backedBy(iterator());
	}

	@Nonnull
	default Ints append(@Nonnull Iterable<Integer> iterable) {
		return append(IntIterable.from(iterable));
	}

	@Nonnull
	default Ints append(@Nonnull IntIterable that) {
		return new ChainingIntIterable(this, that)::iterator;
	}

	default Ints append(IntIterator iterator) {
		return append(iterator.asIterable());
	}

	default Ints append(Iterator<Integer> iterator) {
		return append(IntIterable.from(iterator));
	}

	default Ints append(int... ints) {
		return append(IntIterable.of(ints));
	}

	default Ints append(Stream<Integer> stream) {
		return append(IntIterable.from(stream));
	}

	default Ints append(IntStream stream) {
		return append(IntIterable.from(stream));
	}

	@Nonnull
	default Ints filter(@Nonnull IntPredicate predicate) {
		return () -> new FilteringIntIterator(predicate).backedBy(iterator());
	}

	default Ints until(int terminal) {
		return () -> new ExclusiveTerminalIntIterator(terminal).backedBy(iterator());
	}

	default <C> C collect(Supplier<? extends C> constructor, ObjIntConsumer<? super C> adder) {
		C result = constructor.get();
		forEachInt(x -> adder.accept(result, x));
		return result;
	}

	default String join(String delimiter) {
		return join("", delimiter, "");
	}

	default String join(String prefix, String delimiter, String suffix) {
		StringBuilder result = new StringBuilder(prefix);

		boolean started = false;
		for (IntIterator iterator = iterator(); iterator.hasNext(); started = true) {
			int each = iterator.nextInt();
			if (started)
				result.append(delimiter);
			result.append(each);
		}

		return result.append(suffix).toString();
	}

	default int reduce(int identity, IntBinaryOperator operator) {
		return reduce(identity, operator, iterator());
	}

	default int reduce(int identity, IntBinaryOperator operator, IntIterator iterator) {
		int result = identity;
		while (iterator.hasNext())
			result = operator.applyAsInt(result, iterator.nextInt());
		return result;
	}

	default OptionalInt first() {
		IntIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalInt.empty();

		return OptionalInt.of(iterator.nextInt());
	}

	default OptionalInt second() {
		IntIterator iterator = iterator();

		iterator.skip();
		if (!iterator.hasNext())
			return OptionalInt.empty();

		return OptionalInt.of(iterator.nextInt());
	}

	default OptionalInt third() {
		IntIterator iterator = iterator();

		iterator.skip();
		iterator.skip();
		if (!iterator.hasNext())
			return OptionalInt.empty();

		return OptionalInt.of(iterator.nextInt());
	}

	default OptionalInt last() {
		IntIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalInt.empty();

		int last;
		do {
			last = iterator.nextInt();
		} while (iterator.hasNext());

		return OptionalInt.of(last);
	}

	default Ints step(long step) {
		return () -> new SteppingIntIterator(step).backedBy(iterator());
	}

	default Ints distinct() {
		return () -> new DistinctIntIterator().backedBy(iterator());
	}

	default OptionalInt min() {
		return reduce((a, b) -> (a < b) ? a : b);
	}

	default OptionalInt reduce(IntBinaryOperator operator) {
		IntIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalInt.empty();

		int result = reduce(iterator.next(), operator, iterator);
		return OptionalInt.of(result);
	}

	default OptionalInt max() {
		return reduce((a, b) -> (a > b) ? a : b);
	}

	default long count() {
		long count = 0;
		for (IntIterator iterator = iterator(); iterator.hasNext(); iterator.nextInt()) {
			count++;
		}
		return count;
	}

	default boolean all(IntPredicate predicate) {
		for (IntIterator iterator = iterator(); iterator.hasNext(); ) {
			if (!predicate.test(iterator.nextInt()))
				return false;
		}
		return true;
	}

	default boolean none(IntPredicate predicate) {
		return !any(predicate);
	}

	default boolean any(IntPredicate predicate) {
		for (IntIterator iterator = iterator(); iterator.hasNext(); ) {
			if (predicate.test(iterator.nextInt()))
				return true;
		}
		return false;
	}

	default Ints peek(IntConsumer action) {
		return () -> new UnaryIntIterator() {
			@Override
			public int nextInt() {
				int next = iterator.nextInt();
				action.accept(next);
				return next;
			}
		}.backedBy(iterator());
	}

	default Ints sorted() {
		int[] array = toArray();
		Arrays.sort(array);
		return () -> IntIterator.of(array);
	}

	default int[] toArray() {
		int[] work = new int[10];

		int index = 0;
		IntIterator iterator = iterator();
		while (iterator.hasNext()) {
			if (work.length < (index + 1)) {
				int newCapacity = work.length + (work.length >> 1);
				int[] newInts = new int[newCapacity];
				System.arraycopy(work, 0, newInts, 0, work.length);
				work = newInts;
			}
			work[index++] = iterator.nextInt();
		}

		if (work.length == index) {
			return work; // Not very likely, but still
		}

		int[] result = new int[index];
		System.arraycopy(work, 0, result, 0, index);
		return result;
	}

	default Ints prefix(int... cs) {
		return () -> new ChainingIntIterator(IntIterable.of(cs), this);
	}

	default Ints suffix(int... cs) {
		return () -> new ChainingIntIterator(this, IntIterable.of(cs));
	}

	default Ints interleave(Ints that) {
		return () -> new InterleavingIntIterator(this, that);
	}

	default Ints reverse() {
		int[] array = toArray();
		for (int i = 0; i < (array.length / 2); i++) {
			MoreArrays.swap(array, i, array.length - 1 - i);
		}
		return IntIterable.of(array)::iterator;
	}

	default Ints mapBack(BackPeekingIntFunction mapper) {
		return () -> new BackPeekingIntIterator(mapper).backedBy(iterator());
	}

	default Ints mapForward(ForwardPeekingIntFunction mapper) {
		return () -> new ForwardPeekingIntIterator(mapper).backedBy(iterator());
	}

	default Chars toChars() {
		return () -> new DelegatingCharIterator<Integer, IntIterator>() {
			@Override
			public char nextChar() {
				return (char) iterator.nextInt();
			}
		}.backedBy(iterator());
	}

	default Longs toLongs() {
		return () -> new DelegatingLongIterator<Integer, IntIterator>() {
			@Override
			public long nextLong() {
				return iterator.nextInt();
			}
		}.backedBy(iterator());
	}

	default Doubles toDoubles() {
		return () -> new DelegatingDoubleIterator<Integer, IntIterator>() {
			@Override
			public double nextDouble() {
				return iterator.nextInt();
			}
		}.backedBy(iterator());
	}

	default Chars toChars(IntToCharFunction mapper) {
		return () -> new DelegatingCharIterator<Integer, IntIterator>() {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.nextInt());
			}
		}.backedBy(iterator());
	}

	default Longs toLongs(IntToLongFunction mapper) {
		return () -> new DelegatingLongIterator<Integer, IntIterator>() {
			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.nextInt());
			}
		}.backedBy(iterator());
	}

	default Doubles toDoubles(IntToDoubleFunction mapper) {
		return () -> new DelegatingDoubleIterator<Integer, IntIterator>() {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextInt());
			}
		}.backedBy(iterator());
	}

	default Ints repeat() {
		return () -> new RepeatingIntIterator(this);
	}
}
