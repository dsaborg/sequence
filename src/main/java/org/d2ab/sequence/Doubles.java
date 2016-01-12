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

import org.d2ab.primitive.doubles.*;
import org.d2ab.primitive.ints.IntIterator;
import org.d2ab.primitive.longs.LongIterator;
import org.d2ab.utils.MoreArrays;

import javax.annotation.Nonnull;
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
public interface Doubles extends DoubleIterable {
	/**
	 * Create empty {@code Doubles} with no contents.
	 */
	@Nonnull
	static Doubles empty() {
		return from(emptyIterator());
	}

	/**
	 * Create an {@code Doubles} from an
	 * {@link Iterator} of {@code Double} values. Note that {@code Doubles} created from {@link Iterator}s
	 * cannot be passed over more than once. Further attempts will register the {@code Doubles} as empty.
	 */
	@Nonnull
	static Doubles from(@Nonnull Iterator<Double> iterator) {
		return from(DoubleIterator.from(iterator));
	}

	/**
	 * Create a {@code Doubles} from a {@link DoubleIterator} of double values. Note that {@code
	 * Doubles}s created from {@link DoubleIterator}s cannot be passed over more than once. Further attempts
	 * will
	 * register the {@code Doubles} as empty.
	 */
	@Nonnull
	static Doubles from(@Nonnull DoubleIterator iterator) {
		return () -> iterator;
	}

	/**
	 * Create a {@code Doubles} from a {@link DoubleIterable}.
	 */
	@Nonnull
	static Doubles from(@Nonnull DoubleIterable iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a {@code Doubles} with the given doubles.
	 */
	@Nonnull
	static Doubles of(@Nonnull double... cs) {
		return () -> new ArrayDoubleIterator(cs);
	}

	/**
	 * Create a {@code Sequence} from {@link Iterator}s of items supplied by the given {@link Supplier}. Every time
	 * the {@code Sequence} is to be iterated over, the {@link Supplier} is used to create the initial stream of
	 * elements. This is similar to creating a {@code Sequence} from an {@link Iterable}.
	 */
	@Nonnull
	static Doubles from(@Nonnull Supplier<? extends DoubleIterator> iteratorSupplier) {
		return iteratorSupplier::get;
	}

	/**
	 * Create a {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created from {@link
	 * Stream}s cannot be passed over more than once. Further attempts will cause an {@link IllegalStateException}
	 * when the {@link Stream} is requested again.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 */
	static Doubles from(Stream<Double> stream) {
		return from(stream::iterator);
	}

	/**
	 * Create a {@code Doubles} from an {@link Iterable} of {@code Double} values.
	 */
	@Nonnull
	static Doubles from(@Nonnull Iterable<Double> iterable) {
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
	static Doubles positive() {
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
	static Doubles negative() {
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
	static Doubles startingAt(double start) {
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
	static Doubles range(double start, double end) {
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
	static Doubles range(double start, double end, double step) {
		double effectiveStep = end > start ? step : -step;
		return recurse(start, d -> d + effectiveStep).endingAt(end);
	}

	static Doubles recurse(double seed, DoubleUnaryOperator op) {
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
	 * @return a sequence of {@code Doubles} that is generated from the given supplier and thus never terminates.
	 *
	 * @see #recurse(double, DoubleUnaryOperator)
	 * @see #endingAt(double)
	 * @see #until(double)
	 */
	static Doubles generate(DoubleSupplier supplier) {
		return () -> (InfiniteDoubleIterator) supplier::getAsDouble;
	}

	/**
	 * Terminate this {@code Doubles} sequence before the given element, with the previous element as the last
	 * element in this {@code Doubles} sequence.
	 *
	 * @see #endingAt(double)
	 * @see #generate(DoubleSupplier)
	 * @see #recurse(double, DoubleUnaryOperator)
	 */
	default Doubles until(double terminal) {
		return () -> new ExclusiveTerminalDoubleIterator(terminal).backedBy(iterator());
	}

	/**
	 * Terminate this {@code Doubles} sequence at the given element, including it as the last element in this {@code
	 * Doubles} sequence.
	 *
	 * @see #until(double)
	 * @see #generate(DoubleSupplier)
	 * @see #recurse(double, DoubleUnaryOperator)
	 */
	default Doubles endingAt(double terminal) {
		return () -> new InclusiveTerminalDoubleIterator(terminal).backedBy(iterator());
	}

	@Nonnull
	default Doubles map(@Nonnull DoubleUnaryOperator mapper) {
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

	@Nonnull
	default <T> Sequence<T> toSequence(@Nonnull DoubleFunction<T> mapper) {
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

	@Nonnull
	default Doubles skip(double skip) {
		return () -> new SkippingDoubleIterator(skip).backedBy(iterator());
	}

	@Nonnull
	default Doubles limit(double limit) {
		return () -> new LimitingDoubleIterator(limit).backedBy(iterator());
	}

	@Nonnull
	default Doubles append(@Nonnull Iterable<Double> iterable) {
		return append(DoubleIterable.from(iterable));
	}

	@Nonnull
	default Doubles append(@Nonnull DoubleIterable that) {
		return new ChainingDoubleIterable(this, that)::iterator;
	}

	default Doubles append(DoubleIterator iterator) {
		return append(iterator.asIterable());
	}

	default Doubles append(Iterator<Double> iterator) {
		return append(DoubleIterable.from(iterator));
	}

	default Doubles append(double... doubles) {
		return append(DoubleIterable.of(doubles));
	}

	default Doubles append(Stream<Double> stream) {
		return append(DoubleIterable.from(stream));
	}

	default Doubles append(DoubleStream stream) {
		return append(DoubleIterable.from(stream));
	}

	@Nonnull
	default Doubles filter(@Nonnull DoublePredicate predicate) {
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

	default Doubles step(double step) {
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

	default Doubles peek(DoubleConsumer action) {
		return () -> new UnaryDoubleIterator() {
			@Override
			public double nextDouble() {
				double next = iterator.nextDouble();
				action.accept(next);
				return next;
			}
		}.backedBy(iterator());
	}

	default Doubles sorted() {
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

	default Doubles prefix(double... cs) {
		return () -> new ChainingDoubleIterator(DoubleIterable.of(cs), this);
	}

	default Doubles suffix(double... cs) {
		return () -> new ChainingDoubleIterator(this, DoubleIterable.of(cs));
	}

	default Doubles interleave(Doubles that) {
		return () -> new InterleavingDoubleIterator(this, that);
	}

	default Doubles reverse() {
		double[] array = toArray();
		for (int i = 0; i < (array.length / 2); i++) {
			MoreArrays.swap(array, i, array.length - 1 - i);
		}
		return DoubleIterable.of(array)::iterator;
	}

	default Doubles mapBack(BackPeekingDoubleFunction mapper) {
		return () -> new BackPeekingDoubleIterator(mapper).backedBy(iterator());
	}

	default Doubles mapForward(ForwardPeekingDoubleFunction mapper) {
		return () -> new ForwardPeekingDoubleIterator(mapper).backedBy(iterator());
	}

	default Ints toInts() {
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

	default Longs toLongs() {
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

	default Ints toRoundedInts() {
		return toInts(d -> (int) round(d));
	}

	default Ints toInts(DoubleToIntFunction mapper) {
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

	default Longs toRoundedLongs() {
		return toLongs(Math::round);
	}

	default Longs toLongs(DoubleToLongFunction mapper) {
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

	default Doubles repeat() {
		return () -> new RepeatingDoubleIterator(this);
	}
}
