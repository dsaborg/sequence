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

import org.d2ab.function.ints.BackPeekingIntFunction;
import org.d2ab.function.ints.ForwardPeekingIntFunction;
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
	 * {@link Iterator}s cannot be passed over more than once. Further attempts will register the {@code IntSequence} as
	 * empty.
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
	 * A {@code Sequence} of all the positive {@link Integer} values starting at {@code 1} and ending at
	 * {@link Integer#MAX_VALUE}.
	 *
	 * @see #negative()
	 * @see #startingAt(int)
	 * @see #range(int, int)
	 */
	static IntSequence positive() {
		return startingAt(1);
	}

	/**
	 * A {@code Sequence} of all the negative {@link Integer} values starting at {@code -1} and ending at
	 * {@link Integer#MIN_VALUE}.
	 */
	static IntSequence negative() {
		return range(-1, Integer.MIN_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Integer} values starting at the given value and ending at {@link
	 * Integer#MAX_VALUE}.
	 *
	 * @see #positive()
	 * @see #negative()
	 * @see #range(int, int)
	 */
	static IntSequence startingAt(int start) {
		return range(start, Integer.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Integer} values between the given start and end positions, inclusive.
	 *
	 * @see #positive()
	 * @see #negative()
	 * @see #startingAt(int)
	 */
	static IntSequence range(int start, int end) {
		IntUnaryOperator next = (end > start) ? x -> ++x : x -> --x;
		return recurse(start, next).endingAt(end);
	}

	/**
	 * Returns an {@code IntSequence} sequence produced by recursively applying the given operation to the given
	 * seed, which
	 * forms the first element of the sequence, the second being f(seed), the third f(f(seed)) and so on. The returned
	 * {@code IntSequence} sequence never terminates naturally.
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
		return () -> new ExclusiveTerminalIntIterator(terminal).backedBy(iterator());
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
		return () -> new InclusiveTerminalIntIterator(terminal).backedBy(iterator());
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
		return () -> new ExclusiveTerminalIntIterator(terminal).backedBy(iterator());
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
		return () -> new InclusiveTerminalIntIterator(terminal).backedBy(iterator());
	}

	/**
	 * Map the values in this {@code IntSequence} sequence to another set of values specified by the given {@code mapper}
	 * function.
	 */
	default IntSequence map(IntUnaryOperator mapper) {
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

	default IntSequence skip(long skip) {
		return () -> new SkippingIntIterator(skip).backedBy(iterator());
	}

	default IntSequence limit(long limit) {
		return () -> new LimitingIntIterator(limit).backedBy(iterator());
	}

	default IntSequence append(Iterable<Integer> iterable) {
		return append(IntIterable.from(iterable));
	}

	default IntSequence append(IntIterable that) {
		return new ChainingIntIterable(this, that)::iterator;
	}

	default IntSequence append(IntIterator iterator) {
		return append(iterator.asIterable());
	}

	default IntSequence append(Iterator<Integer> iterator) {
		return append(IntIterable.from(iterator));
	}

	default IntSequence append(int... ints) {
		return append(IntIterable.of(ints));
	}

	default IntSequence append(Stream<Integer> stream) {
		return append(IntIterable.from(stream));
	}

	default IntSequence append(IntStream stream) {
		return append(IntIterable.from(stream));
	}

	default IntSequence filter(IntPredicate predicate) {
		return () -> new FilteringIntIterator(predicate).backedBy(iterator());
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

	default IntSequence step(long step) {
		return () -> new SteppingIntIterator(step).backedBy(iterator());
	}

	default IntSequence distinct() {
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

	default IntSequence peek(IntConsumer action) {
		return () -> new UnaryIntIterator() {
			@Override
			public int nextInt() {
				int next = iterator.nextInt();
				action.accept(next);
				return next;
			}
		}.backedBy(iterator());
	}

	default IntSequence sorted() {
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

	default IntSequence prefix(int... cs) {
		return () -> new ChainingIntIterator(IntIterable.of(cs), this);
	}

	default IntSequence suffix(int... cs) {
		return () -> new ChainingIntIterator(this, IntIterable.of(cs));
	}

	default IntSequence interleave(IntSequence that) {
		return () -> new InterleavingIntIterator(this, that);
	}

	default IntSequence reverse() {
		int[] array = toArray();
		for (int i = 0; i < (array.length / 2); i++) {
			Arrayz.swap(array, i, array.length - 1 - i);
		}
		return IntIterable.of(array)::iterator;
	}

	default IntSequence mapBack(BackPeekingIntFunction mapper) {
		return () -> new BackPeekingIntIterator(mapper).backedBy(iterator());
	}

	default IntSequence mapForward(ForwardPeekingIntFunction mapper) {
		return () -> new ForwardPeekingIntIterator(mapper).backedBy(iterator());
	}

	default CharSeq toChars() {
		return () -> new DelegatingCharIterator<Integer, IntIterator>() {
			@Override
			public char nextChar() {
				return (char) iterator.nextInt();
			}
		}.backedBy(iterator());
	}

	default LongSequence toLongs() {
		return () -> new DelegatingLongIterator<Integer, IntIterator>() {
			@Override
			public long nextLong() {
				return iterator.nextInt();
			}
		}.backedBy(iterator());
	}

	default DoubleSequence toDoubles() {
		return () -> new DelegatingDoubleIterator<Integer, IntIterator>() {
			@Override
			public double nextDouble() {
				return iterator.nextInt();
			}
		}.backedBy(iterator());
	}

	default CharSeq toChars(IntToCharFunction mapper) {
		return () -> new DelegatingCharIterator<Integer, IntIterator>() {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.nextInt());
			}
		}.backedBy(iterator());
	}

	default LongSequence toLongs(IntToLongFunction mapper) {
		return () -> new DelegatingLongIterator<Integer, IntIterator>() {
			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.nextInt());
			}
		}.backedBy(iterator());
	}

	default DoubleSequence toDoubles(IntToDoubleFunction mapper) {
		return () -> new DelegatingDoubleIterator<Integer, IntIterator>() {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextInt());
			}
		}.backedBy(iterator());
	}

	default IntSequence repeat() {
		return () -> new RepeatingIntIterator(this, -1);
	}

	default IntSequence repeat(long times) {
		return () -> new RepeatingIntIterator(this, times);
	}
}
