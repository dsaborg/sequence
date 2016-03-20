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

import org.d2ab.iterable.doubles.ChainingDoubleIterable;
import org.d2ab.iterable.doubles.DoubleIterable;
import org.d2ab.iterator.doubles.*;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.util.Arrayz;

import java.util.Arrays;
import java.util.Iterator;
import java.util.OptionalDouble;
import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static java.lang.Math.round;
import static java.util.Collections.emptyIterator;

/**
 * An {@link Iterable} sequence of {@code double} values with {@link Stream}-like operations for refining,
 * transforming and collating the list of doubles.
 */
@FunctionalInterface
public interface DoubleSequence extends DoubleIterable {
	/**
	 * Create empty {@code DoubleSequence} with no contents.
	 */
	static DoubleSequence empty() {
		return from(emptyIterator());
	}

	/**
	 * Create an {@code DoubleSequence} from an
	 * {@link Iterator} of {@code Double} values. Note that {@code DoubleSequence} created from {@link Iterator}s
	 * cannot be passed over more than once. Further attempts will register the {@code DoubleSequence} as empty.
	 */
	static DoubleSequence from(Iterator<Double> iterator) {
		return from(DoubleIterator.from(iterator));
	}

	/**
	 * Create a {@code DoubleSequence} from a {@link DoubleIterator} of double values. Note that {@code
	 * DoubleSequence}s created from {@link DoubleIterator}s cannot be passed over more than once. Further attempts
	 * will
	 * register the {@code DoubleSequence} as empty.
	 */
	static DoubleSequence from(DoubleIterator iterator) {
		return () -> iterator;
	}

	/**
	 * Create a {@code DoubleSequence} from a {@link DoubleIterable}.
	 */
	static DoubleSequence from(DoubleIterable iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a {@code DoubleSequence} with the given doubles.
	 */
	static DoubleSequence of(double... cs) {
		return () -> new ArrayDoubleIterator(cs);
	}

	/**
	 * Create a {@code Sequence} from {@link Iterator}s of items supplied by the given {@link Supplier}. Every time
	 * the {@code Sequence} is to be iterated over, the {@link Supplier} is used to create the initial stream of
	 * elements. This is similar to creating a {@code Sequence} from an {@link Iterable}.
	 */
	static DoubleSequence from(Supplier<? extends DoubleIterator> iteratorSupplier) {
		return iteratorSupplier::get;
	}

	/**
	 * Create a {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created from {@link
	 * Stream}s cannot be passed over more than once. Further attempts will cause an {@link IllegalStateException}
	 * when the {@link Stream} is requested again.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 */
	static DoubleSequence from(Stream<Double> stream) {
		return from(stream::iterator);
	}

	/**
	 * Create a {@code DoubleSequence} from an {@link Iterable} of {@code Double} values.
	 */
	static DoubleSequence from(Iterable<Double> iterable) {
		return () -> DoubleIterator.from(iterable);
	}

	/**
	 * A {@code Sequence} of all the {@link Double} values starting at the given value and ending at {@link
	 * Double#MAX_VALUE}.
	 *
	 * @see #range(double, double, double, double)
	 */
	static DoubleSequence steppingFrom(double start, double step) {
		return recurse(start, d -> d + step);
	}

	/**
	 * A {@code Sequence} of all the {@link Double} values between the given start and end positions, inclusive, using
	 * the given step between iterations and the given accuracy to check whether the end value has occurred.
	 *
	 * @throws IllegalArgumentException if {@code step < 0}
	 * @see #steppingFrom(double, double)
	 */
	static DoubleSequence range(double start, double end, double step, double accuracy) {
		if (step < 0)
			throw new IllegalArgumentException("Require step to be >= 0");
		return end > start ?
		       recurse(start, d -> d + step).until(d -> d - accuracy >= end) :
		       recurse(start, d -> d - step).until(d -> d + accuracy <= end);
	}

	static DoubleSequence recurse(double seed, DoubleUnaryOperator op) {
		return () -> new InfiniteDoubleIterator() {
			private double previous;
			private boolean hasPrevious;

			@Override
			public double nextDouble() {
				previous = hasPrevious ? op.applyAsDouble(previous) : seed;
				hasPrevious = true;
				return previous;
			}
		};
	}

	/**
	 * @return a sequence of {@code DoubleSequence} that is generated from the given supplier and thus never
	 * terminates.
	 *
	 * @see #recurse(double, DoubleUnaryOperator)
	 * @see #endingAt(double, double)
	 * @see #until(double, double)
	 */
	static DoubleSequence generate(DoubleSupplier supplier) {
		return () -> (InfiniteDoubleIterator) supplier::getAsDouble;
	}

	/**
	 * Terminate this {@code DoubleSequence} before the given element compared to the given accuracy, with the previous
	 * element as the last element in this {@code DoubleSequence}.
	 *
	 * @see #until(DoublePredicate)
	 * @see #endingAt(double, double)
	 * @see #generate(DoubleSupplier)
	 * @see #recurse(double, DoubleUnaryOperator)
	 */
	default DoubleSequence until(double terminal, double accuracy) {
		return () -> new ExclusiveTerminalDoubleIterator(iterator(), terminal, accuracy);
	}

	/**
	 * Terminate this {@code DoubleSequence} at the given element compared to the given accuracy, including it as the
	 * last element in this {@code DoubleSequence}.
	 *
	 * @see #endingAt(DoublePredicate)
	 * @see #until(double, double)
	 * @see #generate(DoubleSupplier)
	 * @see #recurse(double, DoubleUnaryOperator)
	 */
	default DoubleSequence endingAt(double terminal, double accuracy) {
		return () -> new InclusiveTerminalDoubleIterator(iterator(), terminal, accuracy);
	}

	/**
	 * Terminate this {@code DoubleSequence} before the element that satisfies the given predicate, with the
	 * previous element as the last element in this {@code DoubleSequence}.
	 *
	 * @see #until(double, double)
	 * @see #endingAt(double, double)
	 * @see #generate(DoubleSupplier)
	 * @see #recurse(double, DoubleUnaryOperator)
	 */
	default DoubleSequence until(DoublePredicate terminal) {
		return () -> new ExclusiveTerminalDoubleIterator(iterator(), terminal);
	}

	/**
	 * Terminate this {@code DoubleSequence} at the element that satisfies the given predicate, including the
	 * element as the last element in this {@code DoubleSequence}.
	 *
	 * @see #endingAt(double, double)
	 * @see #until(double, double)
	 * @see #generate(DoubleSupplier)
	 * @see #recurse(double, DoubleUnaryOperator)
	 */
	default DoubleSequence endingAt(DoublePredicate terminal) {
		return () -> new InclusiveTerminalDoubleIterator(iterator(), terminal);
	}

	/**
	 * Map the {@code doubles} in this {@code DoubleSequence} to another set of {@code doubles} specified by the given
	 * {@code mapper} function.
	 */
	default DoubleSequence map(DoubleUnaryOperator mapper) {
		return () -> new UnaryDoubleIterator(iterator()) {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextDouble());
			}
		};
	}

	/**
	 * Map the {@code doubles} in this {@code DoubleSequence} to their boxed {@link Double} counterparts.
	 */
	default Sequence<Double> box() {
		return toSequence(Double::valueOf);
	}

	/**
	 * Map the {@code doubles} in this {@code DoubleSequence} to a {@link Sequence} of values.
	 */
	default <T> Sequence<T> toSequence(DoubleFunction<T> mapper) {
		return () -> new Iterator<T>() {
			private final DoubleIterator iterator = iterator();

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				return mapper.apply(iterator.nextDouble());
			}
		};
	}

	/**
	 * Skip a set number of {@code doubles} in this {@code DoubleSequence}.
	 */
	default DoubleSequence skip(long skip) {
		return () -> new SkippingDoubleIterator(iterator(), skip);
	}

	/**
	 * Limit the maximum number of {@code doubles} returned by this {@code DoubleSequence}.
	 */
	default DoubleSequence limit(long limit) {
		return () -> new LimitingDoubleIterator(iterator(), limit);
	}

	/**
	 * Append the {@link Double}s in the given {@link Iterable} to the end of this {@code DoubleSequence}.
	 */
	default DoubleSequence append(Iterable<Double> iterable) {
		return append(DoubleIterable.from(iterable));
	}

	/**
	 * Append the {@code doubles} in the given {@link DoubleIterable} to the end of this {@code DoubleSequence}.
	 */
	default DoubleSequence append(DoubleIterable that) {
		return new ChainingDoubleIterable(this, that)::iterator;
	}

	/**
	 * Append the {@code doubles} in the given {@link DoubleIterator} to the end of this {@code DoubleSequence}.
	 * <p>
	 * The appended {@code doubles} will only be available on the first traversal of the resulting {@code
	 * DoubleSequence}.
	 */
	default DoubleSequence append(DoubleIterator iterator) {
		return append(iterator.asIterable());
	}

	/**
	 * Append the {@link Double}s in the given {@link Iterator} to the end of this {@code DoubleSequence}.
	 * <p>
	 * The appended {@link Double}s will only be available on the first traversal of the resulting
	 * {@code DoubleSequence}.
	 */
	default DoubleSequence append(Iterator<Double> iterator) {
		return append(DoubleIterable.from(iterator));
	}

	/**
	 * Append the given {@code doubles} to the end of this {@code DoubleSequence}.
	 */
	default DoubleSequence append(double... doubles) {
		return append(DoubleIterable.of(doubles));
	}

	/**
	 * Append the {@link Double}s in the given {@link Stream} to the end of this {@code DoubleSequence}.
	 * <p>
	 * The appended {@link Double}s will only be available on the first traversal of the resulting
	 * {@code DoubleSequence}. Further traversals will result in {@link IllegalStateException} being thrown.
	 */
	default DoubleSequence append(Stream<Double> stream) {
		return append(DoubleIterable.from(stream));
	}

	/**
	 * Append the {@code double} values of the given {@link DoubleStream} to the end of this {@code DoubleSequence}.
	 * <p>
	 * The appended {@code doubles} will only be available on the first traversal of the resulting
	 * {@code DoubleSequence}. Further traversals will result in {@link IllegalStateException} being thrown.
	 */
	default DoubleSequence append(DoubleStream stream) {
		return append(DoubleIterable.from(stream));
	}

	/**
	 * Filter the elements in this {@code DoubleSequence}, keeping only the elements that match the given
	 * {@link DoublePredicate}.
	 */
	default DoubleSequence filter(DoublePredicate predicate) {
		return () -> new FilteringDoubleIterator(iterator(), predicate);
	}

	/**
	 * Collect this {@code DoubleSequence} into an arbitrary container using the given constructor and adder.
	 */
	default <C> C collect(Supplier<? extends C> constructor, ObjDoubleConsumer<? super C> adder) {
		return collectInto(constructor.get(), adder);
	}

	/**
	 * Collect this {@code DoubleSequence} into the given container using the given adder.
	 */
	default <C> C collectInto(C result, ObjDoubleConsumer<? super C> adder) {
		forEachDouble(d -> adder.accept(result, d));
		return result;
	}

	/**
	 * Join this {@code DoubleSequence} into a string separated by the given delimiter.
	 */
	default String join(String delimiter) {
		return join("", delimiter, "");
	}

	/**
	 * Join this {@code DoubleSequence} into a string separated by the given delimiter, with the given prefix and
	 * suffix.
	 */
	default String join(String prefix, String delimiter, String suffix) {
		StringBuilder result = new StringBuilder(prefix);

		boolean started = false;
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); started = true) {
			double each = iterator.nextDouble();
			if (started)
				result.append(delimiter);
			result.append(each);
		}

		return result.append(suffix).toString();
	}

	/**
	 * Reduce this {@code DoubleSequence} into a single {@code double} by iteratively applying the given binary
	 * operator to the current result and each {@code double} in the sequence.
	 */
	default OptionalDouble reduce(DoubleBinaryOperator operator) {
		DoubleIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalDouble.empty();

		double result = iterator.reduce(iterator.next(), operator);
		return OptionalDouble.of(result);
	}

	/**
	 * Reduce this {@code DoubleSequence} into a single {@code double} by iteratively applying the given binary
	 * operator to the current result and each {@code double} in the sequence, starting with the given identity as the
	 * initial result.
	 */
	default double reduce(double identity, DoubleBinaryOperator operator) {
		return iterator().reduce(identity, operator);
	}

	/**
	 * @return the first double of this {@code DoubleSequence} or an empty {@link OptionalDouble} if there are no
	 * doubles in the {@code DoubleSequence}.
	 */
	default OptionalDouble first() {
		DoubleIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalDouble.empty();

		return OptionalDouble.of(iterator.nextDouble());
	}

	/**
	 * @return the second double of this {@code DoubleSequence} or an empty {@link OptionalDouble} if there are less
	 * than two doubles in the {@code DoubleSequence}.
	 */
	default OptionalDouble second() {
		DoubleIterator iterator = iterator();

		iterator.skip();
		if (!iterator.hasNext())
			return OptionalDouble.empty();

		return OptionalDouble.of(iterator.nextDouble());
	}

	/**
	 * @return the third double of this {@code DoubleSequence} or an empty {@link OptionalDouble} if there are less
	 * than three doubles in the {@code DoubleSequence}.
	 */
	default OptionalDouble third() {
		DoubleIterator iterator = iterator();

		iterator.skip();
		iterator.skip();
		if (!iterator.hasNext())
			return OptionalDouble.empty();

		return OptionalDouble.of(iterator.nextDouble());
	}

	/**
	 * @return the last double of this {@code DoubleSequence} or an empty {@link OptionalDouble} if there are no
	 * doubles in the {@code DoubleSequence}.
	 */
	default OptionalDouble last() {
		DoubleIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalDouble.empty();

		double last;
		do {
			last = iterator.nextDouble();
		} while (iterator.hasNext());

		return OptionalDouble.of(last);
	}

	/**
	 * Skip x number of steps in between each invocation of the iterator of this {@code DoubleSequence}.
	 */
	default DoubleSequence step(long step) {
		return () -> new SteppingDoubleIterator(iterator(), step);
	}

	/**
	 * @return the smallest double in this {@code DoubleSequence}.
	 */
	default OptionalDouble min() {
		return reduce((a, b) -> (a < b) ? a : b);
	}

	/**
	 * @return the greatest double in this {@code DoubleSequence}.
	 */
	default OptionalDouble max() {
		return reduce((a, b) -> (a > b) ? a : b);
	}

	/**
	 * @return the number of doubles in this {@code DoubleSequence}.
	 */
	default long count() {
		long count = 0;
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); iterator.nextDouble()) {
			count++;
		}
		return count;
	}

	/**
	 * @return true if all doubles in this {@code DoubleSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean all(DoublePredicate predicate) {
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); ) {
			if (!predicate.test(iterator.nextDouble()))
				return false;
		}
		return true;
	}

	/**
	 * @return true if no doubles in this {@code DoubleSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean none(DoublePredicate predicate) {
		return !any(predicate);
	}

	/**
	 * @return true if any double in this {@code DoubleSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean any(DoublePredicate predicate) {
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); ) {
			if (predicate.test(iterator.nextDouble()))
				return true;
		}
		return false;
	}

	/**
	 * Allow the given {@link DoubleConsumer} to see each element in this {@code DoubleSequence} as it is traversed.
	 */
	default DoubleSequence peek(DoubleConsumer action) {
		return () -> new UnaryDoubleIterator(iterator()) {
			@Override
			public double nextDouble() {
				double next = iterator.nextDouble();
				action.accept(next);
				return next;
			}
		};
	}

	/**
	 * @return this {@code DoubleSequence} sorted according to the natural order of the double values.
	 *
	 * @see #reverse()
	 */
	default DoubleSequence sorted() {
		double[] array = toArray();
		Arrays.sort(array);
		return () -> DoubleIterator.of(array);
	}

	/**
	 * Collect the doubles in this {@code DoubleSequence} into an array.
	 */
	default double[] toArray() {
		double[] work = new double[10];

		int index = 0;
		DoubleIterator iterator = iterator();
		while (iterator.hasNext()) {
			if (work.length < (index + 1)) {
				int newCapacity = work.length + (work.length >> 1);
				double[] newDoubles = new double[newCapacity];
				System.arraycopy(work, 0, newDoubles, 0, work.length);
				work = newDoubles;
			}
			work[index++] = iterator.nextDouble();
		}

		if (work.length == index) {
			return work; // Not very likely, but still
		}

		double[] result = new double[index];
		System.arraycopy(work, 0, result, 0, index);
		return result;
	}

	/**
	 * Prefix the doubles in this {@code DoubleSequence} with the given doubles.
	 */
	default DoubleSequence prefix(double... xs) {
		return () -> new ChainingDoubleIterator(DoubleIterable.of(xs), this);
	}

	/**
	 * Suffix the doubles in this {@code DoubleSequence} with the given doubles.
	 */
	default DoubleSequence suffix(double... xs) {
		return () -> new ChainingDoubleIterator(this, DoubleIterable.of(xs));
	}

	/**
	 * Interleave the elements in this {@code DoubleSequence} with those of the given {@code DoubleSequence}, stopping
	 * when either sequence finishes.
	 */
	default DoubleSequence interleave(DoubleSequence that) {
		return () -> new InterleavingDoubleIterator(this, that);
	}

	/**
	 * @return a {@code DoubleSequence} which iterates over this {@code DoubleSequence} in reverse order.
	 *
	 * @see #sorted()
	 */
	default DoubleSequence reverse() {
		double[] array = toArray();
		for (int i = 0; i < (array.length / 2); i++) {
			Arrayz.swap(array, i, array.length - 1 - i);
		}
		return of(array);
	}

	/**
	 * Map this {@code DoubleSequence} to another sequence of doubles while peeking at the previous value in the
	 * sequence.
	 * <p>
	 * The mapper has access to the previous double and the current double in the iteration. If the current double is
	 * the first value in the sequence, and there is no previous value, the provided replacement value is used as
	 * the first previous value.
	 */
	default DoubleSequence mapBack(double firstPrevious, DoubleBinaryOperator mapper) {
		return () -> new BackPeekingMappingDoubleIterator(iterator(), firstPrevious, mapper);
	}

	/**
	 * Map this {@code DoubleSequence} to another sequence of doubles while peeking at the next value in the
	 * sequence.
	 * <p>
	 * The mapper has access to the current double and the next double in the iteration. If the current double is
	 * the last value in the sequence, and there is no next value, the provided replacement value is used as
	 * the last next value.
	 */
	default DoubleSequence mapForward(double lastNext, DoubleBinaryOperator mapper) {
		return () -> new ForwardPeekingMappingDoubleIterator(iterator(), lastNext, mapper);
	}

	/**
	 * Convert this sequence of doubles to a sequence of ints corresponding to the downcast integer value of each
	 * double.
	 */
	default IntSequence toInts() {
		return () -> new IntIterator() {
			DoubleIterator iterator = iterator();

			@Override
			public int nextInt() {
				return (int) iterator.nextDouble();
			}

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}
		};
	}

	/**
	 * Convert this sequence of doubles to a sequence of longs corresponding to the downcast long value of each
	 * double.
	 */
	default LongSequence toLongs() {
		return () -> new LongIterator() {
			DoubleIterator iterator = iterator();

			@Override
			public long nextLong() {
				return (long) iterator.nextDouble();
			}

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}
		};
	}

	/**
	 * Convert this sequence of doubles to a sequence of ints corresponding to the downcast rounded int value of each
	 * double.
	 */
	default IntSequence toRoundedInts() {
		return toInts(d -> (int) round(d));
	}

	/**
	 * Convert this sequence of doubles to a sequence of ints using the given converter function.
	 */
	default IntSequence toInts(DoubleToIntFunction mapper) {
		return () -> new IntIterator() {
			DoubleIterator iterator = iterator();

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public int nextInt() {
				return mapper.applyAsInt(iterator.nextDouble());
			}
		};
	}

	/**
	 * Convert this sequence of doubles to a sequence of longs corresponding to the rounded long value of each
	 * double.
	 */
	default LongSequence toRoundedLongs() {
		return toLongs(Math::round);
	}

	/**
	 * Convert this sequence of doubles to a sequence of longs using the given converter function.
	 */
	default LongSequence toLongs(DoubleToLongFunction mapper) {
		return () -> new LongIterator() {
			DoubleIterator iterator = iterator();

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.nextDouble());
			}
		};
	}

	/**
	 * Repeat this sequence of characters doubles, looping back to the beginning when the iterator runs out of doubles.
	 * <p>
	 * The resulting sequence will never terminate if this sequence is non-empty.
	 */
	default DoubleSequence repeat() {
		return () -> new RepeatingDoubleIterator(this, -1);
	}

	/**
	 * Repeat this sequence of doubles x times, looping back to the beginning when the iterator runs out of doubles.
	 */
	default DoubleSequence repeat(long times) {
		return () -> new RepeatingDoubleIterator(this, times);
	}
}
