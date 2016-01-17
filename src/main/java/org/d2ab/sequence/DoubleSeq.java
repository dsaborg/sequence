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

import org.d2ab.function.doubles.BackPeekingDoubleFunction;
import org.d2ab.function.doubles.ForwardPeekingDoubleFunction;
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
public interface DoubleSeq extends DoubleIterable {
	/**
	 * Create empty {@code DoubleSeq} with no contents.
	 */
	static DoubleSeq empty() {
		return from(emptyIterator());
	}

	/**
	 * Create an {@code DoubleSeq} from an
	 * {@link Iterator} of {@code Double} values. Note that {@code DoubleSeq} created from {@link Iterator}s
	 * cannot be passed over more than once. Further attempts will register the {@code DoubleSeq} as empty.
	 */
	static DoubleSeq from(Iterator<Double> iterator) {
		return from(DoubleIterator.from(iterator));
	}

	/**
	 * Create a {@code DoubleSeq} from a {@link DoubleIterator} of double values. Note that {@code
	 * DoubleSeq}s created from {@link DoubleIterator}s cannot be passed over more than once. Further attempts
	 * will
	 * register the {@code DoubleSeq} as empty.
	 */
	static DoubleSeq from(DoubleIterator iterator) {
		return () -> iterator;
	}

	/**
	 * Create a {@code DoubleSeq} from a {@link DoubleIterable}.
	 */
	static DoubleSeq from(DoubleIterable iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a {@code DoubleSeq} with the given doubles.
	 */
	static DoubleSeq of(double... cs) {
		return () -> new ArrayDoubleIterator(cs);
	}

	/**
	 * Create a {@code Sequence} from {@link Iterator}s of items supplied by the given {@link Supplier}. Every time
	 * the {@code Sequence} is to be iterated over, the {@link Supplier} is used to create the initial stream of
	 * elements. This is similar to creating a {@code Sequence} from an {@link Iterable}.
	 */
	static DoubleSeq from(Supplier<? extends DoubleIterator> iteratorSupplier) {
		return iteratorSupplier::get;
	}

	/**
	 * Create a {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created from {@link
	 * Stream}s cannot be passed over more than once. Further attempts will cause an {@link IllegalStateException}
	 * when the {@link Stream} is requested again.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 */
	static DoubleSeq from(Stream<Double> stream) {
		return from(stream::iterator);
	}

	/**
	 * Create a {@code DoubleSeq} from an {@link Iterable} of {@code Double} values.
	 */
	static DoubleSeq from(Iterable<Double> iterable) {
		return () -> DoubleIterator.from(iterable);
	}

	/**
	 * A {@code Sequence} of all the positive {@link Double} values starting at {@code 1} and ending at
	 * {@link Double#MAX_VALUE}.
	 *
	 * @see #negative()
	 * @see #startingAt(double)
	 * @see #range(double, double)
	 * @see #range(double, double, double)
	 */
	static DoubleSeq positive() {
		return startingAt(1);
	}

	/**
	 * A {@code Sequence} of all the negative {@link Double} values starting at {@code -1} and ending at
	 * {@link Double#MIN_VALUE}.
	 *
	 * @see #positive()
	 * @see #startingAt(double)
	 * @see #range(double, double)
	 * @see #range(double, double, double)
	 */
	static DoubleSeq negative() {
		return range(-1L, Double.MIN_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Double} values starting at the given value and ending at {@link
	 * Double#MAX_VALUE}.
	 *
	 * @see #positive
	 * @see #negative()
	 * @see #range(double, double)
	 * @see #range(double, double, double)
	 */
	static DoubleSeq startingAt(double start) {
		return range(start, Double.MAX_VALUE);
	}

	/**
	 * A {@code Sequence} of all the {@link Double} values between the given start and end positions, inclusive.
	 *
	 * @see #positive
	 * @see #negative()
	 * @see #startingAt(double)
	 * @see #range(double, double, double)
	 */
	static DoubleSeq range(double start, double end) {
		return range(start, end, 1);
	}

	/**
	 * A {@code Sequence} of all the {@link Double} values between the given start and end positions, inclusive, using
	 * the given step between iterations.
	 *
	 * @see #positive
	 * @see #negative()
	 * @see #startingAt(double)
	 * @see #range(double, double)
	 */
	static DoubleSeq range(double start, double end, double step) {
		double effectiveStep = end > start ? step : -step;
		return recurse(start, d -> d + effectiveStep).endingAt(end);
	}

	static DoubleSeq recurse(double seed, DoubleUnaryOperator op) {
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
	 * @return a sequence of {@code DoubleSeq} that is generated from the given supplier and thus never terminates.
	 *
	 * @see #recurse(double, DoubleUnaryOperator)
	 * @see #endingAt(double)
	 * @see #until(double)
	 */
	static DoubleSeq generate(DoubleSupplier supplier) {
		return () -> (InfiniteDoubleIterator) supplier::getAsDouble;
	}

	/**
	 * Terminate this {@code DoubleSeq} sequence before the given element, with the previous element as the last
	 * element in this {@code DoubleSeq} sequence.
	 *
	 * @see #until(DoublePredicate)
	 * @see #endingAt(double)
	 * @see #generate(DoubleSupplier)
	 * @see #recurse(double, DoubleUnaryOperator)
	 */
	default DoubleSeq until(double terminal) {
		return () -> new ExclusiveTerminalDoubleIterator(terminal).backedBy(iterator());
	}

	/**
	 * Terminate this {@code DoubleSeq} sequence at the given element, including it as the last element in this {@code
	 * DoubleSeq} sequence.
	 *
	 * @see #endingAt(DoublePredicate)
	 * @see #until(double)
	 * @see #generate(DoubleSupplier)
	 * @see #recurse(double, DoubleUnaryOperator)
	 */
	default DoubleSeq endingAt(double terminal) {
		return () -> new InclusiveTerminalDoubleIterator(terminal).backedBy(iterator());
	}

	/**
	 * Terminate this {@code DoubleSeq} sequence before the element that satisfies the given predicate, with the
	 * previous
	 * element as the last element in this {@code DoubleSeq} sequence.
	 *
	 * @see #until(double)
	 * @see #endingAt(double)
	 * @see #generate(DoubleSupplier)
	 * @see #recurse(double, DoubleUnaryOperator)
	 */
	default DoubleSeq until(DoublePredicate terminal) {
		return () -> new ExclusiveTerminalDoubleIterator(terminal).backedBy(iterator());
	}

	/**
	 * Terminate this {@code DoubleSeq} sequence at the element that satisfies the given predicate, including the
	 * element as the last element in this {@code DoubleSeq} sequence.
	 *
	 * @see #endingAt(double)
	 * @see #until(double)
	 * @see #generate(DoubleSupplier)
	 * @see #recurse(double, DoubleUnaryOperator)
	 */
	default DoubleSeq endingAt(DoublePredicate terminal) {
		return () -> new InclusiveTerminalDoubleIterator(terminal).backedBy(iterator());
	}

	default DoubleSeq map(DoubleUnaryOperator mapper) {
		return () -> new UnaryDoubleIterator() {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextDouble());
			}
		}.backedBy(iterator());
	}

	default Sequence<Double> box() {
		return toSequence(Double::valueOf);
	}

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

	default DoubleSeq skip(double skip) {
		return () -> new SkippingDoubleIterator(skip).backedBy(iterator());
	}

	default DoubleSeq limit(double limit) {
		return () -> new LimitingDoubleIterator(limit).backedBy(iterator());
	}

	default DoubleSeq append(Iterable<Double> iterable) {
		return append(DoubleIterable.from(iterable));
	}

	default DoubleSeq append(DoubleIterable that) {
		return new ChainingDoubleIterable(this, that)::iterator;
	}

	default DoubleSeq append(DoubleIterator iterator) {
		return append(iterator.asIterable());
	}

	default DoubleSeq append(Iterator<Double> iterator) {
		return append(DoubleIterable.from(iterator));
	}

	default DoubleSeq append(double... doubles) {
		return append(DoubleIterable.of(doubles));
	}

	default DoubleSeq append(Stream<Double> stream) {
		return append(DoubleIterable.from(stream));
	}

	default DoubleSeq append(DoubleStream stream) {
		return append(DoubleIterable.from(stream));
	}

	default DoubleSeq filter(DoublePredicate predicate) {
		return () -> new FilteringDoubleIterator(predicate).backedBy(iterator());
	}

	default <C> C collect(Supplier<? extends C> constructor, ObjDoubleConsumer<? super C> adder) {
		C result = constructor.get();
		forEachDouble(c -> adder.accept(result, c));
		return result;
	}

	default String join(String delimiter) {
		return join("", delimiter, "");
	}

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

	default double reduce(double identity, DoubleBinaryOperator operator) {
		return reduce(identity, operator, iterator());
	}

	default double reduce(double identity, DoubleBinaryOperator operator, DoubleIterator iterator) {
		double result = identity;
		while (iterator.hasNext())
			result = operator.applyAsDouble(result, iterator.nextDouble());
		return result;
	}

	default OptionalDouble first() {
		DoubleIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalDouble.empty();

		return OptionalDouble.of(iterator.nextDouble());
	}

	default OptionalDouble second() {
		DoubleIterator iterator = iterator();

		iterator.skip();
		if (!iterator.hasNext())
			return OptionalDouble.empty();

		return OptionalDouble.of(iterator.nextDouble());
	}

	default OptionalDouble third() {
		DoubleIterator iterator = iterator();

		iterator.skip();
		iterator.skip();
		if (!iterator.hasNext())
			return OptionalDouble.empty();

		return OptionalDouble.of(iterator.nextDouble());
	}

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

	default DoubleSeq step(double step) {
		return () -> new SteppingDoubleIterator(step).backedBy(iterator());
	}

	default OptionalDouble min() {
		return reduce((a, b) -> (a < b) ? a : b);
	}

	default OptionalDouble reduce(DoubleBinaryOperator operator) {
		DoubleIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalDouble.empty();

		double result = reduce(iterator.next(), operator, iterator);
		return OptionalDouble.of(result);
	}

	default OptionalDouble max() {
		return reduce((a, b) -> (a > b) ? a : b);
	}

	default double count() {
		double count = 0;
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); iterator.nextDouble()) {
			count++;
		}
		return count;
	}

	default boolean all(DoublePredicate predicate) {
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); ) {
			if (!predicate.test(iterator.nextDouble()))
				return false;
		}
		return true;
	}

	default boolean none(DoublePredicate predicate) {
		return !any(predicate);
	}

	default boolean any(DoublePredicate predicate) {
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); ) {
			if (predicate.test(iterator.nextDouble()))
				return true;
		}
		return false;
	}

	default DoubleSeq peek(DoubleConsumer action) {
		return () -> new UnaryDoubleIterator() {
			@Override
			public double nextDouble() {
				double next = iterator.nextDouble();
				action.accept(next);
				return next;
			}
		}.backedBy(iterator());
	}

	default DoubleSeq sorted() {
		double[] array = toArray();
		Arrays.sort(array);
		return () -> DoubleIterator.of(array);
	}

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

	default DoubleSeq prefix(double... cs) {
		return () -> new ChainingDoubleIterator(DoubleIterable.of(cs), this);
	}

	default DoubleSeq suffix(double... cs) {
		return () -> new ChainingDoubleIterator(this, DoubleIterable.of(cs));
	}

	default DoubleSeq interleave(DoubleSeq that) {
		return () -> new InterleavingDoubleIterator(this, that);
	}

	default DoubleSeq reverse() {
		double[] array = toArray();
		for (int i = 0; i < (array.length / 2); i++) {
			Arrayz.swap(array, i, array.length - 1 - i);
		}
		return DoubleIterable.of(array)::iterator;
	}

	default DoubleSeq mapBack(BackPeekingDoubleFunction mapper) {
		return () -> new BackPeekingDoubleIterator(mapper).backedBy(iterator());
	}

	default DoubleSeq mapForward(ForwardPeekingDoubleFunction mapper) {
		return () -> new ForwardPeekingDoubleIterator(mapper).backedBy(iterator());
	}

	default IntSeq toInts() {
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

	default LongSeq toLongs() {
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

	default IntSeq toRoundedInts() {
		return toInts(d -> (int) round(d));
	}

	default IntSeq toInts(DoubleToIntFunction mapper) {
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

	default LongSeq toRoundedLongs() {
		return toLongs(Math::round);
	}

	default LongSeq toLongs(DoubleToLongFunction mapper) {
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

	default DoubleSeq repeat() {
		return () -> new RepeatingDoubleIterator(this, -1);
	}

	default DoubleSeq repeat(long times) {
		return () -> new RepeatingDoubleIterator(this, times);
	}
}
