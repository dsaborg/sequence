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

import org.d2ab.function.longs.BackPeekingLongFunction;
import org.d2ab.function.longs.ForwardPeekingLongFunction;
import org.d2ab.function.longs.LongToCharFunction;
import org.d2ab.iterable.longs.ChainingLongIterable;
import org.d2ab.iterable.longs.LongIterable;
import org.d2ab.iterator.DelegatingIterator;
import org.d2ab.iterator.chars.DelegatingCharIterator;
import org.d2ab.iterator.doubles.DelegatingDoubleIterator;
import org.d2ab.iterator.ints.DelegatingIntIterator;
import org.d2ab.iterator.longs.*;
import org.d2ab.util.Arrayz;

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
public interface LongSequence extends LongIterable {
	/**
	 * Create empty {@code LongSequence} with no contents.
	 */
	static LongSequence empty() {
		return from(emptyIterator());
	}

	/**
	 * Create an {@code LongSequence} from an {@link Iterator} of {@code Long} values. Note that {@code LongSequence}
	 * created
	 * from
	 * {@link Iterator}s cannot be passed over more than once. Further attempts will register the {@code LongSequence} as
	 * empty.
	 */
	static LongSequence from(Iterator<Long> iterator) {
		return from(LongIterator.from(iterator));
	}

	/**
	 * Create a {@code LongSequence} from a {@link LongIterator} of long values. Note that {@code
	 * LongSequence}s created from {@link LongIterator}s cannot be passed over more than once. Further attempts
	 * will
	 * register the {@code LongSequence} as empty.
	 */
	static LongSequence from(LongIterator iterator) {
		return () -> iterator;
	}

	/**
	 * Create a {@code LongSequence} from a {@link LongIterable}.
	 */
	static LongSequence from(LongIterable iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a {@code LongSequence} with the given longs.
	 */
	static LongSequence of(long... cs) {
		return () -> new ArrayLongIterator(cs);
	}

	/**
	 * Create a {@code Sequence} from {@link Iterator}s of items supplied by the given {@link Supplier}. Every time
	 * the {@code Sequence} is to be iterated over, the {@link Supplier} is used to create the initial stream of
	 * elements. This is similar to creating a {@code Sequence} from an {@link Iterable}.
	 */
	static LongSequence from(Supplier<? extends LongIterator> iteratorSupplier) {
		return iteratorSupplier::get;
	}

	/**
	 * Create a {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created from {@link
	 * Stream}s cannot be passed over more than once. Further attempts will cause an {@link IllegalStateException}
	 * when the {@link Stream} is requested again.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 */
	static LongSequence from(Stream<Long> stream) {
		return from(stream::iterator);
	}

	/**
	 * Create a {@code LongSequence} from an {@link Iterable} of {@code Long} values.
	 */
	static LongSequence from(Iterable<Long> iterable) {
		return () -> LongIterator.from(iterable);
	}

	/**
	 * A {@code Sequence} of all the positive {@link Long} values starting at {@code 1} and ending at
	 * {@link Long#MAX_VALUE}.
	 *
	 * @see #negative()
	 * @see #startingAt(long)
	 * @see #range(long, long)
	 */
	static LongSequence positive() {
		return startingAt(1);
	}

	/**
	 * A {@code Sequence} of all the negative {@link Long} values starting at {@code -1} and ending at
	 * {@link Long#MIN_VALUE}.
	 *
	 * @see #positive()
	 * @see #startingAt(long)
	 * @see #range(long, long)
	 */
	static LongSequence negative() {
		return range(-1L, Long.MIN_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Long} values starting at the given value and ending at {@link
	 * Long#MAX_VALUE}.
	 */
	static LongSequence startingAt(long start) {
		return range(start, Long.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Long} values between the given start and end positions, inclusive.
	 */
	static LongSequence range(long start, long end) {
		LongUnaryOperator next = (end > start) ? x -> ++x : x -> --x;
		return recurse(start, next).endingAt(end);
	}

	static LongSequence recurse(long seed, LongUnaryOperator op) {
		return () -> new InfiniteLongIterator() {
			private long previous;
			private boolean hasPrevious;

			@Override
			public long nextLong() {
				previous = hasPrevious ? op.applyAsLong(previous) : seed;
				hasPrevious = true;
				return previous;
			}
		};
	}

	/**
	 * @return a sequence of {@code IntSequence} that is generated from the given supplier and thus never terminates.
	 *
	 * @see #recurse(long, LongUnaryOperator)
	 * @see #endingAt(long)
	 * @see #until(long)
	 */
	static LongSequence generate(LongSupplier supplier) {
		return () -> (InfiniteLongIterator) supplier::getAsLong;
	}

	/**
	 * Terminate this {@code LongSequence} sequence before the given element, with the previous element as the last
	 * element in this {@code LongSequence} sequence.
	 *
	 * @see #until(LongPredicate)
	 * @see #endingAt(long)
	 * @see #generate(LongSupplier)
	 * @see #recurse(long, LongUnaryOperator)
	 */
	default LongSequence until(long terminal) {
		return () -> new ExclusiveTerminalLongIterator(terminal).backedBy(iterator());
	}

	/**
	 * Terminate this {@code LongSequence} sequence at the given element, including it as the last element in this
	 * {@code
	 * LongSequence} sequence.
	 *
	 * @see #endingAt(LongPredicate)
	 * @see #until(long)
	 * @see #generate(LongSupplier)
	 * @see #recurse(long, LongUnaryOperator)
	 */
	default LongSequence endingAt(long terminal) {
		return () -> new InclusiveTerminalLongIterator(terminal).backedBy(iterator());
	}

	/**
	 * Terminate this {@code LongSequence} sequence before the element that satisfies the given predicate, with the
	 * previous
	 * element as the last element in this {@code LongSequence} sequence.
	 *
	 * @see #until(long)
	 * @see #endingAt(long)
	 * @see #generate(LongSupplier)
	 * @see #recurse(long, LongUnaryOperator)
	 */
	default LongSequence until(LongPredicate terminal) {
		return () -> new ExclusiveTerminalLongIterator(terminal).backedBy(iterator());
	}

	/**
	 * Terminate this {@code LongSequence} sequence at the element that satisfies the given predicate, including the
	 * element as the last element in this {@code LongSequence} sequence.
	 *
	 * @see #endingAt(long)
	 * @see #until(long)
	 * @see #generate(LongSupplier)
	 * @see #recurse(long, LongUnaryOperator)
	 */
	default LongSequence endingAt(LongPredicate terminal) {
		return () -> new InclusiveTerminalLongIterator(terminal).backedBy(iterator());
	}

	/**
	 * Map the {@code longs} in this {@code LongSequence} to another set of {@code longs} specified by the given
	 * {@code mapper} function.
	 */
	default LongSequence map(LongUnaryOperator mapper) {
		return () -> new UnaryLongIterator() {
			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.nextLong());
			}
		}.backedBy(iterator());
	}

	default LongSequence mapBack(BackPeekingLongFunction mapper) {
		return () -> new BackPeekingLongIterator(mapper).backedBy(iterator());
	}

	default LongSequence mapForward(ForwardPeekingLongFunction mapper) {
		return () -> new ForwardPeekingLongIterator(mapper).backedBy(iterator());
	}

	/**
	 * Map the {@code longs} in this {@code LongSequence} to their boxed {@link Long} counterparts.
	 */
	default Sequence<Long> box() {
		return toSequence(Long::valueOf);
	}

	/**
	 * Map the {@code longs} in this {@code LongSequence} to a {@link Sequence} of values.
	 */
	default <T> Sequence<T> toSequence(LongFunction<T> mapper) {
		return () -> new DelegatingIterator<Long, LongIterator, T, Iterator<T>>() {
			@Override
			public T next() {
				return mapper.apply(iterator.nextLong());
			}
		}.backedBy(iterator());
	}

	/**
	 * Skip a set number of {@code longs} in this {@code LongSequence}.
	 */
	default LongSequence skip(long skip) {
		return () -> new SkippingLongIterator(skip).backedBy(iterator());
	}

	/**
	 * Limit the maximum number of {@code longs} returned by this {@code LongSequence}.
	 */
	default LongSequence limit(long limit) {
		return () -> new LimitingLongIterator(limit).backedBy(iterator());
	}

	/**
	 * Append the {@link Long}s in the given {@link Iterable} to the end of this {@code LongSequence}.
	 */
	default LongSequence append(Iterable<Long> iterable) {
		return append(LongIterable.from(iterable));
	}

	/**
	 * Append the {@code longs} in the given {@link LongIterable} to the end of this {@code LongSequence}.
	 */
	default LongSequence append(LongIterable that) {
		return new ChainingLongIterable(this, that)::iterator;
	}

	/**
	 * Append the {@code longs} in the given {@link LongIterator} to the end of this {@code LongSequence}.
	 * <p>
	 * The appended {@code longs} will only be available on the first traversal of the resulting {@code LongSequence}.
	 */
	default LongSequence append(LongIterator iterator) {
		return append(iterator.asIterable());
	}

	/**
	 * Append the {@link Long}s in the given {@link Iterator} to the end of this {@code LongSequence}.
	 * <p>
	 * The appended {@link Long}s will only be available on the first traversal of the resulting {@code LongSequence}.
	 */
	default LongSequence append(Iterator<Long> iterator) {
		return append(LongIterable.from(iterator));
	}

	/**
	 * Append the given {@code longs} to the end of this {@code LongSequence}.
	 */
	default LongSequence append(long... longs) {
		return append(LongIterable.of(longs));
	}

	/**
	 * Append the {@link Long}s in the given {@link Stream} to the end of this {@code LongSequence}.
	 * <p>
	 * The appended
	 * {@link Long}s will only be available on the first traversal of the resulting {@code LongSequence}.
	 * Further traversals will result in {@link IllegalStateException} being thrown.
	 */
	default LongSequence append(Stream<Long> stream) {
		return append(LongIterable.from(stream));
	}

	/**
	 * Append the {@code long} values of the given {@link LongStream} to the end of this {@code LongSequence}.
	 * <p>
	 * The appended {@code longs} will only be available on the first traversal of the resulting {@code LongSequence}.
	 * Further traversals will result in {@link IllegalStateException} being thrown.
	 */
	default LongSequence append(LongStream stream) {
		return append(LongIterable.from(stream));
	}

	/**
	 * Filter the elements in this {@code LongSequence}, keeping only the elements that match the given
	 * {@link LongPredicate}.
	 */
	default LongSequence filter(LongPredicate predicate) {
		return () -> new FilteringLongIterator(predicate).backedBy(iterator());
	}

	/**
	 * Collect this {@code LongSequence} into an arbitrary container using the given constructor and adder.
	 */
	default <C> C collect(Supplier<? extends C> constructor, ObjLongConsumer<? super C> adder) {
		return collectInto(constructor.get(), adder);
	}

	/**
	 * Collect this {@code LongSequence} into the given container using the given adder.
	 */
	default <C> C collectInto(C result, ObjLongConsumer<? super C> adder) {
		forEachLong(l -> adder.accept(result, l));
		return result;
	}

	/**
	 * Join this {@code LongSequence} into a string separated by the given delimiter.
	 */
	default String join(String delimiter) {
		return join("", delimiter, "");
	}

	/**
	 * Join this {@code LongSequence} into a string separated by the given delimiter, with the given prefix and suffix.
	 */
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

	/**
	 * Reduce this {@code LongSequence} into a single {@code long} by iteratively applying the given binary operator to
	 * the current result and each {@code long} in the sequence.
	 */
	default OptionalLong reduce(LongBinaryOperator operator) {
		LongIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalLong.empty();

		long result = iterator.reduce(iterator.next(), operator);
		return OptionalLong.of(result);
	}

	/**
	 * Reduce this {@code LongSequence} into a single {@code long} by iteratively applying the given binary operator to
	 * the current result and each {@code long} in the sequence, starting with the given identity as the initial result.
	 */
	default long reduce(long identity, LongBinaryOperator operator) {
		return iterator().reduce(identity, operator);
	}

	/**
	 * @return the first long of this {@code LongSequence} or an empty {@link OptionalLong} if there are no
	 * longs in the {@code LongSequence}.
	 */
	default OptionalLong first() {
		LongIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalLong.empty();

		return OptionalLong.of(iterator.nextLong());
	}

	/**
	 * @return the second long of this {@code LongSequence} or an empty {@link OptionalLong} if there are less than two
	 * longs in the {@code LongSequence}.
	 */
	default OptionalLong second() {
		LongIterator iterator = iterator();

		iterator.skip();
		if (!iterator.hasNext())
			return OptionalLong.empty();

		return OptionalLong.of(iterator.nextLong());
	}

	/**
	 * @return the third long of this {@code LongSequence} or an empty {@link OptionalLong} if there are less than
	 * three longs in the {@code LongSequence}.
	 */
	default OptionalLong third() {
		LongIterator iterator = iterator();

		iterator.skip();
		iterator.skip();
		if (!iterator.hasNext())
			return OptionalLong.empty();

		return OptionalLong.of(iterator.nextLong());
	}

	/**
	 * @return the last long of this {@code LongSequence} or an empty {@link OptionalLong} if there are no
	 * longs in the {@code LongSequence}.
	 */
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

	/**
	 * Skip x number of steps in between each invocation of the iterator of this {@code LongSequence}.
	 */
	default LongSequence step(long step) {
		return () -> new SteppingLongIterator(step).backedBy(iterator());
	}

	/**
	 * @return a {@code LongSequence} where each item occurs only once, the first time it is encountered.
	 */
	default LongSequence distinct() {
		return () -> new DistinctLongIterator().backedBy(iterator());
	}

	default OptionalLong min() {
		return reduce((a, b) -> (a < b) ? a : b);
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

	default LongSequence peek(LongConsumer action) {
		return () -> new UnaryLongIterator() {
			@Override
			public long nextLong() {
				long next = iterator.nextLong();
				action.accept(next);
				return next;
			}
		}.backedBy(iterator());
	}

	default LongSequence sorted() {
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
			return work; // Not very likely, but still
		}

		long[] result = new long[index];
		System.arraycopy(work, 0, result, 0, index);
		return result;
	}

	default LongSequence prefix(long... cs) {
		return () -> new ChainingLongIterator(LongIterable.of(cs), this);
	}

	default LongSequence suffix(long... cs) {
		return () -> new ChainingLongIterator(this, LongIterable.of(cs));
	}

	default LongSequence interleave(LongSequence that) {
		return () -> new InterleavingLongIterator(this, that);
	}

	default LongSequence reverse() {
		long[] array = toArray();
		for (int i = 0; i < (array.length / 2); i++) {
			Arrayz.swap(array, i, array.length - 1 - i);
		}
		return LongIterable.of(array)::iterator;
	}

	default CharSeq toChars() {
		return () -> new DelegatingCharIterator<Long, LongIterator>() {
			@Override
			public char nextChar() {
				return (char) iterator.nextLong();
			}
		}.backedBy(iterator());
	}

	default IntSequence toInts() {
		return () -> new DelegatingIntIterator<Long, LongIterator>() {
			@Override
			public int nextInt() {
				return (int) iterator.nextLong();
			}
		}.backedBy(iterator());
	}

	default DoubleSequence toDoubles() {
		return () -> new DelegatingDoubleIterator<Long, LongIterator>() {
			@Override
			public double nextDouble() {
				return iterator.nextLong();
			}
		}.backedBy(iterator());
	}

	default CharSeq toChars(LongToCharFunction mapper) {
		return () -> new DelegatingCharIterator<Long, LongIterator>() {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.nextLong());
			}
		}.backedBy(iterator());
	}

	default IntSequence toInts(LongToIntFunction mapper) {
		return () -> new DelegatingIntIterator<Long, LongIterator>() {
			@Override
			public int nextInt() {
				return mapper.applyAsInt(iterator.nextLong());
			}
		}.backedBy(iterator());
	}

	default DoubleSequence toDoubles(LongToDoubleFunction mapper) {
		return () -> new DelegatingDoubleIterator<Long, LongIterator>() {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextLong());
			}
		}.backedBy(iterator());
	}

	default LongSequence repeat() {
		return () -> new RepeatingLongIterator(this, -1);
	}

	default LongSequence repeat(long times) {
		return () -> new RepeatingLongIterator(this, times);
	}
}
