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

import org.d2ab.function.ints.IntToCharFunction;
import org.d2ab.iterable.ints.ChainingIntIterable;
import org.d2ab.iterable.ints.IntIterable;
import org.d2ab.iterator.chars.DelegatingCharIterator;
import org.d2ab.iterator.doubles.DelegatingDoubleIterator;
import org.d2ab.iterator.ints.*;
import org.d2ab.iterator.longs.DelegatingLongIterator;
import org.d2ab.util.Arrayz;

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
public interface IntSequence extends IntIterable {
	/**
	 * Create empty {@code IntSequence} with no contents.
	 */
	static IntSequence empty() {
		return from(emptyIterator());
	}

	/**
	 * Create an {@code IntSequence} from an {@link Iterator} of {@code Integer} values. Note that {@code IntSequence}
	 * created
	 * from
	 * {@link Iterator}s cannot be passed over more than once. Further attempts will register the {@code
	 * IntSequence} as empty.
	 */
	static IntSequence from(Iterator<Integer> iterator) {
		return from(IntIterator.from(iterator));
	}

	/**
	 * Create a {@code IntSequence} from a {@link IntIterator} of int values. Note that {@code
	 * IntSequence}s created from {@link IntIterator}s cannot be passed over more than once. Further attempts
	 * will
	 * register the {@code IntSequence} as empty.
	 */
	static IntSequence from(IntIterator iterator) {
		return () -> iterator;
	}

	/**
	 * Create a {@code IntSequence} from a {@link IntIterable}.
	 */
	static IntSequence from(IntIterable iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a {@code IntSequence} with the given ints.
	 */
	static IntSequence of(int... cs) {
		return () -> new ArrayIntIterator(cs);
	}

	/**
	 * Create a {@code Sequence} from {@link Iterator}s of items supplied by the given {@link Supplier}. Every time
	 * the {@code Sequence} is to be iterated over, the {@link Supplier} is used to create the initial stream of
	 * elements. This is similar to creating a {@code Sequence} from an {@link Iterable}.
	 */
	static IntSequence from(Supplier<? extends IntIterator> iteratorSupplier) {
		return iteratorSupplier::get;
	}

	/**
	 * Create a {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created from {@link
	 * Stream}s cannot be passed over more than once. Further attempts will cause an {@link IllegalStateException}
	 * when the {@link Stream} is requested again.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 */
	static IntSequence from(Stream<Integer> stream) {
		return from(stream::iterator);
	}

	/**
	 * Create a {@code IntSequence} from an {@link Iterable} of {@code Integer} values.
	 */
	static IntSequence from(Iterable<Integer> iterable) {
		return () -> IntIterator.from(iterable);
	}

	/**
	 * An {@code IntSequence} of all the positive {@code int} values starting at {@code 1} and ending at
	 * {@link Integer#MAX_VALUE}.
	 *
	 * @see #positiveFromZero()
	 * @see #increasingFrom(int)
	 * @see #negative()
	 * @see #negativeFromZero()
	 * @see #decreasingFrom(int)
	 * @see #steppingFrom(int, int)
	 * @see #range(int, int)
	 * @see #range(int, int, int)
	 */
	static IntSequence positive() {
		return range(1, Integer.MAX_VALUE);
	}

	/**
	 * An {@code IntSequence} of all the positive {@code int} values starting at {@code 0} and ending at
	 * {@link Integer#MAX_VALUE}.
	 *
	 * @see #positive()
	 * @see #increasingFrom(int)
	 * @see #negative()
	 * @see #negativeFromZero()
	 * @see #decreasingFrom(int)
	 * @see #steppingFrom(int, int)
	 * @see #range(int, int)
	 * @see #range(int, int, int)
	 */
	static IntSequence positiveFromZero() {
		return range(0, Integer.MAX_VALUE);
	}

	/**
	 * An increasing {@code IntSequence} of {@code int} values starting at the given value. This sequence never
	 * terminates but will wrap to {@link Integer#MIN_VALUE} when passing {@link Integer#MAX_VALUE}.
	 *
	 * @see #decreasingFrom(int)
	 * @see #positive()
	 * @see #positiveFromZero()
	 * @see #negative()
	 * @see #negativeFromZero()
	 * @see #steppingFrom(int, int)
	 * @see #range(int, int)
	 * @see #range(int, int, int)
	 */
	static IntSequence increasingFrom(int start) {
		return steppingFrom(start, 1);
	}

	/**
	 * An {@code IntSequence} of all the negative {@code int} values starting at {@code -1} and ending at
	 * {@link Integer#MIN_VALUE}.
	 *
	 * @see #negativeFromZero()
	 * @see #increasingFrom(int)
	 * @see #positive()
	 * @see #positiveFromZero()
	 * @see #decreasingFrom(int)
	 * @see #steppingFrom(int, int)
	 * @see #range(int, int)
	 * @see #range(int, int, int)
	 */
	static IntSequence negative() {
		return range(-1, Integer.MIN_VALUE);
	}

	/**
	 * An {@code IntSequence} of all the negative {@code int} values starting at {@code 0} and ending at
	 * {@link Integer#MIN_VALUE}.
	 *
	 * @see #negative()
	 * @see #increasingFrom(int)
	 * @see #positive()
	 * @see #positiveFromZero()
	 * @see #decreasingFrom(int)
	 * @see #steppingFrom(int, int)
	 * @see #range(int, int)
	 * @see #range(int, int, int)
	 */
	static IntSequence negativeFromZero() {
		return range(0, Integer.MIN_VALUE);
	}

	/**
	 * A decreasing {@code Sequence} of {@code int} values starting at the given value. This sequence never
	 * terminates but will wrap to {@link Integer#MAX_VALUE} when passing {@link Integer#MIN_VALUE}.
	 *
	 * @see #increasingFrom(int)
	 * @see #positive()
	 * @see #positiveFromZero()
	 * @see #negative()
	 * @see #negativeFromZero()
	 * @see #steppingFrom(int, int)
	 * @see #range(int, int)
	 * @see #range(int, int, int)
	 */
	static IntSequence decreasingFrom(int start) {
		return steppingFrom(start, -1);
	}

	/**
	 * A {@code Sequence} of all the {@code int} values starting at the given value and stepping with the given
	 * step. Pass in a negative step to create a decreasing sequence. This sequence never terminates but will wrap when
	 * passing {@link Integer#MAX_VALUE} or {@link Integer#MIN_VALUE}.
	 *
	 * @see #range(int, int)
	 * @see #range(int, int, int)
	 * @see #increasingFrom(int)
	 * @see #decreasingFrom(int)
	 * @see #positive()
	 * @see #positiveFromZero()
	 * @see #negative()
	 * @see #negativeFromZero()
	 */
	static IntSequence steppingFrom(int start, int step) {
		return recurse(start, x -> x + step);
	}

	/**
	 * A {@code Sequence} of all the {@code int} values between the given start and end positions, inclusive.
	 *
	 * @see #range(int, int, int)
	 * @see #steppingFrom(int, int)
	 * @see #increasingFrom(int)
	 * @see #decreasingFrom(int)
	 * @see #positive()
	 * @see #positiveFromZero()
	 * @see #negative()
	 * @see #negativeFromZero()
	 */
	static IntSequence range(int start, int end) {
		return range(start, end, 1);
	}

	/**
	 * A {@code Sequence} of the {@code int} values between the given start and end positions, stepping with the
	 * given step. The step must be given as a positive value, even if the range is a decreasing range.
	 *
	 * @throws IllegalArgumentException if {@code step < 0}
	 * @see #range(int, int)
	 * @see #steppingFrom(int, int)
	 * @see #increasingFrom(int)
	 * @see #decreasingFrom(int)
	 * @see #positive()
	 * @see #positiveFromZero()
	 * @see #negative()
	 * @see #negativeFromZero()
	 */
	static IntSequence range(int start, int end, int step) {
		if (step < 0)
			throw new IllegalArgumentException("Require step >= 0");
		return end >= start ?
		       recurse(start, x -> x + step).endingAt(x -> (long) x + step > end) :
		       recurse(start, x -> x - step).endingAt(x -> (long) x - step < end);
	}

	/**
	 * Returns an {@code IntSequence} sequence produced by recursively applying the given operation to the given
	 * seed, which forms the first element of the sequence, the second being f(seed), the third f(f(seed)) and so on.
	 * The returned {@code IntSequence} sequence never terminates naturally.
	 *
	 * @return an {@code IntSequence} sequence produced by recursively applying the given operation to the given seed
	 *
	 * @see #generate(IntSupplier)
	 * @see #endingAt(int)
	 * @see #until(int)
	 */
	static IntSequence recurse(int seed, IntUnaryOperator op) {
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
	 * @return a sequence of {@code IntSequence} that is generated from the given supplier and thus never terminates.
	 *
	 * @see #recurse(int, IntUnaryOperator)
	 * @see #endingAt(int)
	 * @see #until(int)
	 */
	static IntSequence generate(IntSupplier supplier) {
		return () -> (InfiniteIntIterator) supplier::getAsInt;
	}

	/**
	 * Terminate this {@code IntSequence} sequence before the given element, with the previous element as the last
	 * element in this {@code IntSequence} sequence.
	 *
	 * @see #until(IntPredicate)
	 * @see #endingAt(int)
	 * @see #generate(IntSupplier)
	 * @see #recurse(int, IntUnaryOperator)
	 */
	default IntSequence until(int terminal) {
		return () -> new ExclusiveTerminalIntIterator(iterator(), terminal);
	}

	/**
	 * Terminate this {@code IntSequence} sequence at the given element, including it as the last element in this
	 * {@code
	 * IntSequence} sequence.
	 *
	 * @see #endingAt(IntPredicate)
	 * @see #until(int)
	 * @see #generate(IntSupplier)
	 * @see #recurse(int, IntUnaryOperator)
	 */
	default IntSequence endingAt(int terminal) {
		return () -> new InclusiveTerminalIntIterator(iterator(), terminal);
	}

	/**
	 * Terminate this {@code IntSequence} sequence before the element that satisfies the given predicate, with the
	 * previous
	 * element as the last element in this {@code IntSequence} sequence.
	 *
	 * @see #until(int)
	 * @see #endingAt(int)
	 * @see #generate(IntSupplier)
	 * @see #recurse(int, IntUnaryOperator)
	 */
	default IntSequence until(IntPredicate terminal) {
		return () -> new ExclusiveTerminalIntIterator(iterator(), terminal);
	}

	/**
	 * Terminate this {@code IntSequence} sequence at the element that satisfies the given predicate, including the
	 * element as the last element in this {@code IntSequence} sequence.
	 *
	 * @see #endingAt(int)
	 * @see #until(int)
	 * @see #generate(IntSupplier)
	 * @see #recurse(int, IntUnaryOperator)
	 */
	default IntSequence endingAt(IntPredicate terminal) {
		return () -> new InclusiveTerminalIntIterator(iterator(), terminal);
	}

	/**
	 * Map the values in this {@code IntSequence} sequence to another set of values specified by the given {@code
	 * mapper}
	 * function.
	 */
	default IntSequence map(IntUnaryOperator mapper) {
		return () -> new UnaryIntIterator(iterator()) {
			@Override
			public int nextInt() {
				return mapper.applyAsInt(iterator.nextInt());
			}
		};
	}

	/**
	 * Map the {@code ints} in this {@code IntSequence} to their boxed {@link Integer} counterparts.
	 */
	default Sequence<Integer> box() {
		return toSequence(Integer::valueOf);
	}

	/**
	 * Map the {@code ints} in this {@code IntSequence} to a {@link Sequence} of values.
	 */
	default <T> Sequence<T> toSequence(IntFunction<T> mapper) {
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

	/**
	 * Skip a set number of {@code ints} in this {@code IntSequence}.
	 */
	default IntSequence skip(long skip) {
		return () -> new SkippingIntIterator(iterator(), skip);
	}

	/**
	 * Limit the maximum number of {@code ints} returned by this {@code IntSequence}.
	 */
	default IntSequence limit(long limit) {
		return () -> new LimitingIntIterator(iterator(), limit);
	}

	/**
	 * Append the {@link Integer}s in the given {@link Iterable} to the end of this {@code IntSequence}.
	 */
	default IntSequence append(Iterable<Integer> iterable) {
		return append(IntIterable.from(iterable));
	}

	/**
	 * Append the {@code ints} in the given {@link IntIterable} to the end of this {@code IntSequence}.
	 */
	default IntSequence append(IntIterable that) {
		return new ChainingIntIterable(this, that)::iterator;
	}

	/**
	 * Append the {@code ints} in the given {@link IntIterator} to the end of this {@code IntSequence}.
	 * <p>
	 * The appended {@code ints} will only be available on the first traversal of the resulting {@code IntSequence}.
	 */
	default IntSequence append(IntIterator iterator) {
		return append(iterator.asIterable());
	}

	/**
	 * Append the {@link Integer}s in the given {@link Iterator} to the end of this {@code IntSequence}.
	 * <p>
	 * The appended
	 * {@link Integer}s will only be available on the first traversal of the resulting {@code IntSequence}.
	 */
	default IntSequence append(Iterator<Integer> iterator) {
		return append(IntIterable.from(iterator));
	}

	/**
	 * Append the given {@code ints} to the end of this {@code IntSequence}.
	 */
	default IntSequence append(int... ints) {
		return append(IntIterable.of(ints));
	}

	/**
	 * Append the {@link Integer}s in the given {@link Stream} to the end of this {@code IntSequence}.
	 * <p>
	 * The appended
	 * {@link Integer}s will only be available on the first traversal of the resulting {@code IntSequence}.
	 * Further traversals will result in {@link IllegalStateException} being thrown.
	 */
	default IntSequence append(Stream<Integer> stream) {
		return append(IntIterable.from(stream));
	}

	/**
	 * Append the {@code int} values of the given {@link IntStream} to the end of this {@code IntSequence}.
	 * <p>
	 * The appended {@code ints} will only be available on the first traversal of the resulting {@code IntSequence}.
	 * Further traversals will result in {@link IllegalStateException} being thrown.
	 */
	default IntSequence append(IntStream stream) {
		return append(IntIterable.from(stream));
	}

	/**
	 * Filter the elements in this {@code IntSequence}, keeping only the elements that match the given
	 * {@link IntPredicate}.
	 */
	default IntSequence filter(IntPredicate predicate) {
		return () -> new FilteringIntIterator(iterator(), predicate);
	}

	/**
	 * Collect this {@code IntSequence} into an arbitrary container using the given constructor and adder.
	 */
	default <C> C collect(Supplier<? extends C> constructor, ObjIntConsumer<? super C> adder) {
		return collectInto(constructor.get(), adder);
	}

	/**
	 * Collect this {@code IntSequence} into the given container using the given adder.
	 */
	default <C> C collectInto(C result, ObjIntConsumer<? super C> adder) {
		forEachInt(x -> adder.accept(result, x));
		return result;
	}

	/**
	 * Join this {@code IntSequence} into a string separated by the given delimiter.
	 */
	default String join(String delimiter) {
		return join("", delimiter, "");
	}

	/**
	 * Join this {@code IntSequence} into a string separated by the given delimiter, with the given prefix and suffix.
	 */
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

	/**
	 * Reduce this {@code IntSequence} into a single {@code int} by iteratively applying the given binary operator to
	 * the current result and each {@code int} in the sequence.
	 */
	default OptionalInt reduce(IntBinaryOperator operator) {
		IntIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalInt.empty();

		int result = iterator.reduce(iterator.next(), operator);
		return OptionalInt.of(result);
	}

	/**
	 * Reduce this {@code IntSequence} into a single {@code int} by iteratively applying the given binary operator to
	 * the current result and each {@code int} in the sequence, starting with the given identity as the initial result.
	 */
	default int reduce(int identity, IntBinaryOperator operator) {
		return iterator().reduce(identity, operator);
	}

	/**
	 * @return the first int of this {@code IntSequence} or an empty {@link OptionalInt} if there are no
	 * ints in the {@code IntSequence}.
	 */
	default OptionalInt first() {
		IntIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalInt.empty();

		return OptionalInt.of(iterator.nextInt());
	}

	/**
	 * @return the second int of this {@code IntSequence} or an empty {@link OptionalInt} if there are less than two
	 * ints in the {@code IntSequence}.
	 */
	default OptionalInt second() {
		IntIterator iterator = iterator();

		iterator.skip();
		if (!iterator.hasNext())
			return OptionalInt.empty();

		return OptionalInt.of(iterator.nextInt());
	}

	/**
	 * @return the third int of this {@code IntSequence} or an empty {@link OptionalInt} if there are less than
	 * three ints in the {@code IntSequence}.
	 */
	default OptionalInt third() {
		IntIterator iterator = iterator();

		iterator.skip();
		iterator.skip();
		if (!iterator.hasNext())
			return OptionalInt.empty();

		return OptionalInt.of(iterator.nextInt());
	}

	/**
	 * @return the last int of this {@code IntSequence} or an empty {@link OptionalInt} if there are no
	 * ints in the {@code IntSequence}.
	 */
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

	/**
	 * Skip x number of steps in between each invocation of the iterator of this {@code IntSequence}.
	 */
	default IntSequence step(long step) {
		return () -> new SteppingIntIterator(iterator(), step);
	}

	/**
	 * @return an {@code IntSequence} where each item occurs only once, the first time it is encountered.
	 */
	default IntSequence distinct() {
		return () -> new DistinctIntIterator(iterator());
	}

	/**
	 * @return the smallest int in this {@code IntSequence}.
	 */
	default OptionalInt min() {
		return reduce((a, b) -> (a < b) ? a : b);
	}

	/**
	 * @return the greatest int in this {@code IntSequence}.
	 */
	default OptionalInt max() {
		return reduce((a, b) -> (a > b) ? a : b);
	}

	/**
	 * @return the number of ints in this {@code IntSequence}.
	 */
	default long count() {
		long count = 0;
		for (IntIterator iterator = iterator(); iterator.hasNext(); iterator.nextInt()) {
			count++;
		}
		return count;
	}

	/**
	 * @return true if all ints in this {@code IntSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean all(IntPredicate predicate) {
		for (IntIterator iterator = iterator(); iterator.hasNext(); ) {
			if (!predicate.test(iterator.nextInt()))
				return false;
		}
		return true;
	}

	/**
	 * @return true if no ints in this {@code IntSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean none(IntPredicate predicate) {
		return !any(predicate);
	}

	/**
	 * @return true if any int in this {@code IntSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean any(IntPredicate predicate) {
		for (IntIterator iterator = iterator(); iterator.hasNext(); ) {
			if (predicate.test(iterator.nextInt()))
				return true;
		}
		return false;
	}

	/**
	 * Allow the given {@link IntConsumer} to see each element in this {@code IntSequence} as it is traversed.
	 */
	default IntSequence peek(IntConsumer action) {
		return () -> new UnaryIntIterator(iterator()) {
			@Override
			public int nextInt() {
				int next = iterator.nextInt();
				action.accept(next);
				return next;
			}
		};
	}

	/**
	 * @return this {@code IntSequence} sorted according to the natural order of the int values.
	 *
	 * @see #reverse()
	 */
	default IntSequence sorted() {
		int[] array = toArray();
		Arrays.sort(array);
		return () -> IntIterator.of(array);
	}

	/**
	 * Collect the ints in this {@code IntSequence} into an array.
	 */
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

	/**
	 * Prefix the ints in this {@code IntSequence} with the given ints.
	 */
	default IntSequence prefix(int... cs) {
		return () -> new ChainingIntIterator(IntIterable.of(cs), this);
	}

	/**
	 * Suffix the ints in this {@code IntSequence} with the given ints.
	 */
	default IntSequence suffix(int... cs) {
		return () -> new ChainingIntIterator(this, IntIterable.of(cs));
	}

	/**
	 * Interleave the elements in this {@code IntSequence} with those of the given {@code IntSequence}, stopping when
	 * either sequence finishes.
	 */
	default IntSequence interleave(IntSequence that) {
		return () -> new InterleavingIntIterator(this, that);
	}

	/**
	 * @return an {@code IntSequence} which iterates over this {@code IntSequence} in reverse order.
	 *
	 * @see #sorted()
	 */
	default IntSequence reverse() {
		int[] array = toArray();
		for (int i = 0; i < (array.length / 2); i++) {
			Arrayz.swap(array, i, array.length - 1 - i);
		}
		return of(array);
	}

	/**
	 * Map this {@code IntSequence} to another sequence of ints while peeking at the previous value in the
	 * sequence.
	 * <p>
	 * The mapper has access to the previous int and the current int in the iteration. If the current int is
	 * the first value in the sequence, and there is no previous value, the provided replacement value is used as
	 * the first previous value.
	 */
	default IntSequence mapBack(int firstPrevious, IntBinaryOperator mapper) {
		return () -> new BackPeekingMappingIntIterator(iterator(), firstPrevious, mapper);
	}

	/**
	 * Map this {@code IntSequence} to another sequence of ints while peeking at the next int in the sequence.
	 * <p>
	 * The mapper has access to the current int and the next int in the iteration. If the current int is
	 * the last value in the sequence, and there is no next value, the provided replacement value is used as
	 * the last next value.
	 */
	default IntSequence mapForward(int lastNext, IntBinaryOperator mapper) {
		return () -> new ForwardPeekingMappingIntIterator(iterator(), lastNext, mapper);
	}

	/**
	 * Convert this sequence of ints to a sequence of chars corresponding to the downcast char value of each int.
	 */
	default CharSeq toChars() {
		return () -> new DelegatingCharIterator<Integer, IntIterator>(iterator()) {
			@Override
			public char nextChar() {
				return (char) iterator.nextInt();
			}
		};
	}

	/**
	 * Convert this sequence of ints to a sequence of longs.
	 */
	default LongSequence toLongs() {
		return () -> new DelegatingLongIterator<Integer, IntIterator>(iterator()) {
			@Override
			public long nextLong() {
				return iterator.nextInt();
			}
		};
	}

	/**
	 * Convert this sequence of ints to a sequence of doubles corresponding to the cast double value of each int.
	 */
	default DoubleSequence toDoubles() {
		return () -> new DelegatingDoubleIterator<Integer, IntIterator>(iterator()) {
			@Override
			public double nextDouble() {
				return iterator.nextInt();
			}
		};
	}

	/**
	 * Convert this sequence of ints to a sequence of chars using the given converter function.
	 */
	default CharSeq toChars(IntToCharFunction mapper) {
		return () -> new DelegatingCharIterator<Integer, IntIterator>(iterator()) {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.nextInt());
			}
		};
	}

	/**
	 * Convert this sequence of ints to a sequence of longs using the given converter function.
	 */
	default LongSequence toLongs(IntToLongFunction mapper) {
		return () -> new DelegatingLongIterator<Integer, IntIterator>(iterator()) {
			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.nextInt());
			}
		};
	}

	/**
	 * Convert this sequence of ints to a sequence of doubles using the given converter function.
	 */
	default DoubleSequence toDoubles(IntToDoubleFunction mapper) {
		return () -> new DelegatingDoubleIterator<Integer, IntIterator>(iterator()) {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextInt());
			}
		};
	}

	/**
	 * Repeat this sequence of ints forever, looping back to the beginning when the iterator runs out of ints.
	 * <p>
	 * The resulting sequence will never terminate if this sequence is non-empty.
	 */
	default IntSequence repeat() {
		return () -> new RepeatingIntIterator(this, -1);
	}

	/**
	 * Repeat this sequence of ints x times, looping back to the beginning when the iterator runs out of ints.
	 */
	default IntSequence repeat(long times) {
		return () -> new RepeatingIntIterator(this, times);
	}
}
