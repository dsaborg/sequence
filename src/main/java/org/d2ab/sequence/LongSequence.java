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
	 * {@link Iterator}s cannot be passed over more than once. Further attempts will register the {@code
	 * LongSequence} as empty.
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
	 * A {@code Sequence} of all the positive {@code long} values starting at {@code 1} and ending at
	 * {@link Long#MAX_VALUE} inclusive.
	 *
	 * @see #positiveFromZero()
	 * @see #increasingFrom(long)
	 * @see #negative()
	 * @see #negativeFromZero()
	 * @see #decreasingFrom(long)
	 * @see #steppingFrom(long, long)
	 * @see #range(long, long)
	 * @see #range(long, long, long)
	 */
	static LongSequence positive() {
		return range(1, Long.MAX_VALUE);
	}

	/**
	 * A {@code LongSequence} of all the positive {@code long} values starting at {@code 0} and ending at
	 * {@link Long#MAX_VALUE} inclusive.
	 *
	 * @see #positive()
	 * @see #increasingFrom(long)
	 * @see #negative()
	 * @see #negativeFromZero()
	 * @see #decreasingFrom(long)
	 * @see #steppingFrom(long, long)
	 * @see #range(long, long)
	 * @see #range(long, long, long)
	 */
	static LongSequence positiveFromZero() {
		return range(0, Long.MAX_VALUE);
	}

	/**
	 * An increasing {@code LongSequence} of {@code long} values starting at the given value. This sequence never
	 * terminates but will wrap to {@link Long#MIN_VALUE} when passing {@link Long#MAX_VALUE}.
	 *
	 * @see #decreasingFrom(long)
	 * @see #positive()
	 * @see #positiveFromZero()
	 * @see #negative()
	 * @see #negativeFromZero()
	 * @see #steppingFrom(long, long)
	 * @see #range(long, long)
	 * @see #range(long, long, long)
	 */
	static LongSequence increasingFrom(long start) {
		return steppingFrom(start, 1);
	}

	/**
	 * A {@code Sequence} of all the negative {@code long} values starting at {@code -1} and ending at
	 * {@link Long#MIN_VALUE}.
	 *
	 * @see #negativeFromZero()
	 * @see #increasingFrom(long)
	 * @see #positive()
	 * @see #positiveFromZero()
	 * @see #decreasingFrom(long)
	 * @see #steppingFrom(long, long)
	 * @see #range(long, long)
	 * @see #range(long, long, long)
	 */
	static LongSequence negative() {
		return range(-1, Long.MIN_VALUE);
	}

	/**
	 * A {@code LongSequence} of all the negative {@code long} values starting at {@code 0} and ending at
	 * {@link Long#MIN_VALUE} inclusive.
	 *
	 * @see #negative()
	 * @see #increasingFrom(long)
	 * @see #positive()
	 * @see #positiveFromZero()
	 * @see #decreasingFrom(long)
	 * @see #steppingFrom(long, long)
	 * @see #range(long, long)
	 * @see #range(long, long, long)
	 */
	static LongSequence negativeFromZero() {
		return range(0, Long.MIN_VALUE);
	}

	/**
	 * A decreasing {@code LongSequence} of {@code long} values starting at the given value. This sequence never
	 * terminates but will wrap to {@link Long#MAX_VALUE} when passing {@link Long#MIN_VALUE}.
	 *
	 * @see #increasingFrom(long)
	 * @see #positive()
	 * @see #positiveFromZero()
	 * @see #negative()
	 * @see #negativeFromZero()
	 * @see #steppingFrom(long, long)
	 * @see #range(long, long)
	 * @see #range(long, long, long)
	 */
	static LongSequence decreasingFrom(long start) {
		return steppingFrom(start, -1);
	}

	/**
	 * A {@code LongSequence} of all the {@code long} values starting at the given value and stepping with the given
	 * step. Pass in a negative step to create a decreasing sequence. This sequence never terminates but will wrap when
	 * passing {@link Long#MAX_VALUE} or {@link Long#MIN_VALUE}.
	 *
	 * @see #range(long, long)
	 * @see #range(long, long, long)
	 * @see #increasingFrom(long)
	 * @see #decreasingFrom(long)
	 * @see #positive()
	 * @see #positiveFromZero()
	 * @see #negative()
	 * @see #negativeFromZero()
	 */
	static LongSequence steppingFrom(long start, long step) {
		return recurse(start, x -> x + step);
	}

	/**
	 * A {@code LongSequence} of all the {@code long} values between the given start and end positions, inclusive.
	 *
	 * @see #range(long, long, long)
	 * @see #steppingFrom(long, long)
	 * @see #increasingFrom(long)
	 * @see #decreasingFrom(long)
	 * @see #positive()
	 * @see #positiveFromZero()
	 * @see #negative()
	 * @see #negativeFromZero()
	 */
	static LongSequence range(long start, long end) {
		LongUnaryOperator next = (end > start) ? x -> ++x : x -> --x;
		return recurse(start, next).endingAt(end);
	}

	/**
	 * A {@code LongSequence} of the {@code long} values between the given start and end positions, stepping with
	 * the given step. The step must be given as a positive value, even if the range is a decreasing range.
	 *
	 * @throws IllegalArgumentException if {@code step < 0}
	 * @see #range(long, long)
	 * @see #steppingFrom(long, long)
	 * @see #increasingFrom(long)
	 * @see #decreasingFrom(long)
	 * @see #positive()
	 * @see #positiveFromZero()
	 * @see #negative()
	 * @see #negativeFromZero()
	 */
	static LongSequence range(long start, long end, long step) {
		if (step < 0)
			throw new IllegalArgumentException("Require step >= 0");
		return end >= start ?
		       recurse(start, x -> x + step).endingAt(x -> x + step > end || x > Long.MAX_VALUE - step) :
		       recurse(start, x -> x - step).endingAt(x -> x - step < end || x < Long.MIN_VALUE + step);
	}

	/**
	 * Returns a {@code LongSequence} sequence produced by recursively applying the given operation to the given
	 * seed, which forms the first element of the sequence, the second being f(seed), the third f(f(seed)) and so on.
	 * The returned {@code LongSequence} sequence never terminates naturally.
	 *
	 * @return a {@code LongSequence} sequence produced by recursively applying the given operation to the given seed
	 *
	 * @see #generate(LongSupplier)
	 * @see #endingAt(long)
	 * @see #until(long)
	 */
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
	 * @return a {@code LongSequence} that is generated from the given supplier and thus never terminates.
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
		return () -> new ExclusiveTerminalLongIterator(iterator(), terminal);
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
		return () -> new InclusiveTerminalLongIterator(iterator(), terminal);
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
		return () -> new ExclusiveTerminalLongIterator(iterator(), terminal);
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
		return () -> new InclusiveTerminalLongIterator(iterator(), terminal);
	}

	/**
	 * Map the {@code longs} in this {@code LongSequence} to another set of {@code longs} specified by the given
	 * {@code mapper} function.
	 */
	default LongSequence map(LongUnaryOperator mapper) {
		return () -> new UnaryLongIterator(iterator()) {
			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.nextLong());
			}
		};
	}

	/**
	 * Map this {@code LongSequence} to another sequence of longs while peeking at the previous long in the
	 * sequence.
	 * <p>
	 * The mapper has access to the previous long and the current long in the iteration. If the current long is
	 * the first value in the sequence, and there is no previous value, the provided replacement value is used as
	 * the first previous value.
	 */
	default LongSequence mapBack(long firstPrevious, LongBinaryOperator mapper) {
		return () -> new BackPeekingMappingLongIterator(iterator(), firstPrevious, mapper);
	}

	/**
	 * Map this {@code LongSequence} to another sequence of longs while peeking at the next long in the
	 * sequence.
	 * <p>
	 * The mapper has access to the current long and the next long in the iteration. If the current long is
	 * the last value in the sequence, and there is no next value, the provided replacement value is used as
	 * the last next value.
	 */
	default LongSequence mapForward(long lastNext, LongBinaryOperator mapper) {
		return () -> new ForwardPeekingMappingLongIterator(iterator(), lastNext, mapper);
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
		return () -> new DelegatingIterator<Long, LongIterator, T>(iterator()) {
			@Override
			public T next() {
				return mapper.apply(iterator.nextLong());
			}
		};
	}

	/**
	 * Skip a set number of {@code longs} in this {@code LongSequence}.
	 */
	default LongSequence skip(long skip) {
		return () -> new SkippingLongIterator(iterator(), skip);
	}

	/**
	 * Limit the maximum number of {@code longs} returned by this {@code LongSequence}.
	 */
	default LongSequence limit(long limit) {
		return () -> new LimitingLongIterator(iterator(), limit);
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
	 * The appended {@link Long}s will only be available on the first traversal of the resulting {@code LongSequence}.
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
		return () -> new FilteringLongIterator(iterator(), predicate);
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
	 * the current result and each {@code long} in the sequence, starting with the given identity as the initial
	 * result.
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
		return () -> new SteppingLongIterator(iterator(), step);
	}

	/**
	 * @return a {@code LongSequence} where each item occurs only once, the first time it is encountered.
	 */
	default LongSequence distinct() {
		return () -> new DistinctLongIterator(iterator());
	}

	/**
	 * @return the smallest long in this {@code LongSequence}.
	 */
	default OptionalLong min() {
		return reduce((a, b) -> (a < b) ? a : b);
	}

	/**
	 * @return the greatest long in this {@code LongSequence}.
	 */
	default OptionalLong max() {
		return reduce((a, b) -> (a > b) ? a : b);
	}

	/**
	 * @return the number of longs in this {@code LongSequence}.
	 */
	default long count() {
		long count = 0;
		for (LongIterator iterator = iterator(); iterator.hasNext(); iterator.nextLong()) {
			count++;
		}
		return count;
	}

	/**
	 * @return true if all longs in this {@code LongSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean all(LongPredicate predicate) {
		for (LongIterator iterator = iterator(); iterator.hasNext(); ) {
			if (!predicate.test(iterator.nextLong()))
				return false;
		}
		return true;
	}

	/**
	 * @return true if no longs in this {@code LongSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean none(LongPredicate predicate) {
		return !any(predicate);
	}

	/**
	 * @return true if any long in this {@code LongSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean any(LongPredicate predicate) {
		for (LongIterator iterator = iterator(); iterator.hasNext(); ) {
			if (predicate.test(iterator.nextLong()))
				return true;
		}
		return false;
	}

	/**
	 * Allow the given {@link LongConsumer} to see each element in this {@code LongSequence} as it is traversed.
	 */
	default LongSequence peek(LongConsumer action) {
		return () -> new UnaryLongIterator(iterator()) {
			@Override
			public long nextLong() {
				long next = iterator.nextLong();
				action.accept(next);
				return next;
			}
		};
	}

	/**
	 * @return this {@code LongSequence} sorted according to the natural order of the long values.
	 *
	 * @see #reverse()
	 */
	default LongSequence sorted() {
		long[] array = toArray();
		Arrays.sort(array);
		return () -> LongIterator.of(array);
	}

	/**
	 * Collect the longs in this {@code LongSequence} into an array.
	 */
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

	/**
	 * Prefix the longs in this {@code LongSequence} with the given longs.
	 */
	default LongSequence prefix(long... cs) {
		return () -> new ChainingLongIterator(LongIterable.of(cs), this);
	}

	/**
	 * Suffix the longs in this {@code LongSequence} with the given longs.
	 */
	default LongSequence suffix(long... cs) {
		return () -> new ChainingLongIterator(this, LongIterable.of(cs));
	}

	/**
	 * Interleave the elements in this {@code LongSequence} with those of the given {@code LongSequence}, stopping when
	 * either sequence finishes.
	 */
	default LongSequence interleave(LongSequence that) {
		return () -> new InterleavingLongIterator(this, that);
	}

	/**
	 * @return a {@code LongSequence} which iterates over this {@code LongSequence} in reverse order.
	 *
	 * @see #sorted()
	 */
	default LongSequence reverse() {
		long[] array = toArray();
		for (int i = 0; i < (array.length / 2); i++) {
			Arrayz.swap(array, i, array.length - 1 - i);
		}
		return of(array);
	}

	/**
	 * Convert this sequence of longs to a sequence of chars corresponding to the downcast char value of each long.
	 */
	default CharSeq toChars() {
		return () -> new DelegatingCharIterator<Long, LongIterator>(iterator()) {
			@Override
			public char nextChar() {
				return (char) iterator.nextLong();
			}
		};
	}

	/**
	 * Convert this sequence of longs to a sequence of ints corresponding to the downcast integer value of each long.
	 */
	default IntSequence toInts() {
		return () -> new DelegatingIntIterator<Long, LongIterator>(iterator()) {
			@Override
			public int nextInt() {
				return (int) iterator.nextLong();
			}
		};
	}

	/**
	 * Convert this sequence of longs to a sequence of doubles corresponding to the cast double value of each long.
	 */
	default DoubleSequence toDoubles() {
		return () -> new DelegatingDoubleIterator<Long, LongIterator>(iterator()) {
			@Override
			public double nextDouble() {
				return iterator.nextLong();
			}
		};
	}

	/**
	 * Convert this sequence of longs to a sequence of chars using the given converter function.
	 */
	default CharSeq toChars(LongToCharFunction mapper) {
		return () -> new DelegatingCharIterator<Long, LongIterator>(iterator()) {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.nextLong());
			}
		};
	}

	/**
	 * Convert this sequence of longs to a sequence of ints using the given converter function.
	 */
	default IntSequence toInts(LongToIntFunction mapper) {
		return () -> new DelegatingIntIterator<Long, LongIterator>(iterator()) {
			@Override
			public int nextInt() {
				return mapper.applyAsInt(iterator.nextLong());
			}
		};
	}

	/**
	 * Convert this sequence of longs to a sequence of doubles using the given converter function.
	 */
	default DoubleSequence toDoubles(LongToDoubleFunction mapper) {
		return () -> new DelegatingDoubleIterator<Long, LongIterator>(iterator()) {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextLong());
			}
		};
	}

	/**
	 * Repeat this sequence of longs forever, looping back to the beginning when the iterator runs out of longs.
	 * <p>
	 * The resulting sequence will never terminate if this sequence is non-empty.
	 */
	default LongSequence repeat() {
		return () -> new RepeatingLongIterator(this, -1);
	}

	/**
	 * Repeat this sequence of longs x times, looping back to the beginning when the iterator runs out of longs.
	 */
	default LongSequence repeat(long times) {
		return () -> new RepeatingLongIterator(this, times);
	}
}
