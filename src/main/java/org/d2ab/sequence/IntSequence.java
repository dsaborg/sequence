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

import org.d2ab.collection.IntList;
import org.d2ab.function.chars.IntToCharFunction;
import org.d2ab.function.ints.IntBiConsumer;
import org.d2ab.function.ints.IntBiPredicate;
import org.d2ab.iterable.ints.ChainingIntIterable;
import org.d2ab.iterable.ints.IntIterable;
import org.d2ab.iterator.IterationException;
import org.d2ab.iterator.Iterators;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.iterator.ints.*;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.util.Arrayz;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyIterator;

/**
 * An {@link Iterable} sequence of {@code int} values with {@link Stream}-like operations for refining,
 * transforming and collating the list of ints.
 */
@FunctionalInterface
public interface IntSequence extends IntList {
	/**
	 * Create empty {@code IntSequence} with no contents.
	 */
	static IntSequence empty() {
		return once(emptyIterator());
	}

	/**
	 * Create an {@code IntSequence} with the given ints.
	 */
	static IntSequence of(int... is) {
		return () -> IntIterator.of(is);
	}

	/**
	 * Create an {@code IntSequence} from a {@link IntIterable}.
	 *
	 * @see #cache(IntIterable)
	 */
	static IntSequence from(IntIterable iterable) {
		return iterable::iterator;
	}

	/**
	 * Create an {@code IntSequence} from an {@link Iterable} of {@code Integer} values.
	 *
	 * @see #cache(Iterable)
	 */
	static IntSequence from(Iterable<Integer> iterable) {
		return from(IntIterable.from(iterable));
	}

	/**
	 * Create a once-only {@code IntSequence} from a {@link PrimitiveIterator.OfInt}. Note that {@code IntSequence}s
	 * created from {@link PrimitiveIterator.OfInt} cannot be passed over more than once. Further attempts will
	 * register the {@code IntSequence} as empty.
	 *
	 * @see #cache(PrimitiveIterator.OfInt)
	 * @since 1.1
	 */
	static IntSequence once(PrimitiveIterator.OfInt iterator) {
		return from(IntIterable.once(iterator));
	}

	/**
	 * Create a once-only {@code IntSequence} from an {@link Iterator} of {@code Integer} values. Note that
	 * {@code IntSequence} created from {@link Iterator}s cannot be passed over more than once. Further attempts will
	 * register the {@code IntSequence} as empty.
	 *
	 * @see #cache(Iterator)
	 * @since 1.1
	 */
	static IntSequence once(Iterator<Integer> iterator) {
		return once(IntIterator.from(iterator));
	}

	/**
	 * Create a once-only {@code IntSequence} from an {@link IntStream} of items. Note that {@code IntSequences}
	 * created from {@link IntStream}s cannot be passed over more than once. Further attempts will register the
	 * {@code IntSequence} as empty.
	 *
	 * @throws IllegalStateException if the {@link IntStream} is exhausted.
	 * @see #cache(IntStream)
	 * @since 1.1
	 */
	static IntSequence once(IntStream stream) {
		return once(stream.iterator());
	}

	/**
	 * Create a only-only {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created
	 * from{@link Stream}s cannot be passed over more than once. Further attempts will register the {@code IntSequence}
	 * as empty.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 * @see #cache(Stream)
	 * @since 1.1
	 */
	static IntSequence once(Stream<Integer> stream) {
		return once(stream.iterator());
	}

	/**
	 * Create an {@code IntSequence} from an {@link InputStream} which iterates over the bytes provided in the
	 * input stream as ints. The {@link InputStream} must support {@link InputStream#reset} or the {@code IntSequence}
	 * will only be available to iterate over once. The {@link InputStream} will be reset in between iterations,
	 * if possible. If an {@link IOException} occurs during iteration, an {@link IterationException} will be thrown.
	 * The {@link InputStream} will not be closed by the {@code IntSequence} when iteration finishes, it must be closed
	 * externally when iteration is finished.
	 *
	 * @since 1.1
	 */
	static IntSequence read(InputStream inputStream) {
		return IntIterable.read(inputStream)::iterator;
	}

	/**
	 * Create an {@code IntSequence} from a cached copy of an {@link IntIterable}.
	 *
	 * @see #cache(Iterable)
	 * @see #cache(IntStream)
	 * @see #cache(Stream)
	 * @see #cache(PrimitiveIterator.OfInt)
	 * @see #cache(Iterator)
	 * @see #from(IntIterable)
	 * @since 1.1
	 */
	static IntSequence cache(IntIterable iterable) {
		return cache(iterable.iterator());
	}

	/**
	 * Create an {@code IntSequence} from a cached copy of a {@link PrimitiveIterator.OfInt}.
	 *
	 * @see #cache(Iterator)
	 * @see #cache(IntStream)
	 * @see #cache(Stream)
	 * @see #cache(IntIterable)
	 * @see #cache(Iterable)
	 * @see #once(PrimitiveIterator.OfInt)
	 * @since 1.1
	 */
	static IntSequence cache(PrimitiveIterator.OfInt iterator) {
		int[] cache = new int[10];
		int position = 0;
		while (iterator.hasNext()) {
			int next = iterator.nextInt();
			if (position == cache.length)
				cache = Arrays.copyOf(cache, cache.length * 2);
			cache[position++] = next;
		}
		if (cache.length > position)
			cache = Arrays.copyOf(cache, position);
		return of(cache);
	}

	/**
	 * Create an {@code IntSequence} from a cached copy of an {@link Iterator} of {@link Integer}s.
	 *
	 * @see #cache(PrimitiveIterator.OfInt)
	 * @see #cache(IntStream)
	 * @see #cache(Stream)
	 * @see #cache(IntIterable)
	 * @see #cache(Iterable)
	 * @see #once(Iterator)
	 * @since 1.1
	 */
	static IntSequence cache(Iterator<Integer> iterator) {
		return cache(IntIterator.from(iterator));
	}

	/**
	 * Create an {@code IntSequence} from a cached copy of an {@link IntStream}.
	 *
	 * @see #cache(Stream)
	 * @see #cache(IntIterable)
	 * @see #cache(Iterable)
	 * @see #cache(PrimitiveIterator.OfInt)
	 * @see #cache(Iterator)
	 * @see #once(IntStream)
	 * @since 1.1
	 */
	static IntSequence cache(IntStream stream) {
		return cache(stream.iterator());
	}

	/**
	 * Create an {@code IntSequence} from a cached copy of a {@link Stream} of {@link Integer}s.
	 *
	 * @see #cache(IntStream)
	 * @see #cache(IntIterable)
	 * @see #cache(Iterable)
	 * @see #cache(PrimitiveIterator.OfInt)
	 * @see #cache(Iterator)
	 * @see #once(Stream)
	 * @since 1.1
	 */
	static IntSequence cache(Stream<Integer> stream) {
		return cache(stream.iterator());
	}

	/**
	 * Create an {@code IntSequence} from a cached copy of an {@link Iterable} of {@code Integer} values.
	 *
	 * @see #cache(IntIterable)
	 * @see #cache(IntStream)
	 * @see #cache(Stream)
	 * @see #cache(PrimitiveIterator.OfInt)
	 * @see #cache(Iterator)
	 * @see #from(Iterable)
	 * @since 1.1
	 */
	static IntSequence cache(Iterable<Integer> iterable) {
		return cache(iterable.iterator());
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
	 * @return an {@code IntSequence} that is generated from the given supplier and thus never terminates.
	 *
	 * @see #recurse(int, IntUnaryOperator)
	 * @see #endingAt(int)
	 * @see #until(int)
	 */
	static IntSequence generate(IntSupplier supplier) {
		return once((InfiniteIntIterator) supplier::getAsInt);
	}

	/**
	 * @return an {@code IntSequence} where each {@link #iterator()} is generated by polling for a supplier and then
	 * using it to generate the sequence of {@code ints}. The sequence never terminates.
	 *
	 * @see #recurse(int, IntUnaryOperator)
	 * @see #endingAt(int)
	 * @see #until(int)
	 */
	static IntSequence multiGenerate(Supplier<? extends IntSupplier> supplierSupplier) {
		return () -> {
			IntSupplier intSupplier = supplierSupplier.get();
			return (InfiniteIntIterator) intSupplier::getAsInt;
		};
	}

	/**
	 * @return an {@code IntSequence} of random ints that never terminates. Each run of this {@code IntSequence}'s
	 * {@link #iterator()} will produce a new random sequence of ints. This method is equivalent to
	 * {@code random(Random::new)}.
	 *
	 * @see #random(Supplier)
	 * @see Random#nextInt()
	 * @since 1.2
	 */
	static IntSequence random() {
		return random(Random::new);
	}

	/**
	 * @return an {@code IntSequence} of random ints that never terminates. The given supplier is used to produce the
	 * instance of {@link Random} that is used, one for each new {@link #iterator()}.
	 *
	 * @see #random()
	 * @see Random#nextInt()
	 * @since 1.2
	 */
	static IntSequence random(Supplier<? extends Random> randomSupplier) {
		return multiGenerate(() -> {
			Random random = randomSupplier.get();
			return random::nextInt;
		});
	}

	/**
	 * @return an {@code IntSequence} of random ints between {@code 0}, inclusive, and the upper bound, exclusive,
	 * that never terminates. Each run of this {@code IntSequence}'s {@link #iterator()} will produce a new random
	 * sequence of ints. This method is equivalent to {@code random(Random::new, upper}.
	 *
	 * @see #random(Supplier, int)
	 * @see Random#nextInt(int)
	 * @since 1.2
	 */
	static IntSequence random(int upper) {
		return random(0, upper);
	}

	/**
	 * @return an {@code IntSequence} of random ints between {@code 0}, inclusive, and the upper bound, exclusive,
	 * that never terminates. The given supplier is used to produce the instance of {@link Random} that is used, one
	 * for each new {@link #iterator()}.
	 *
	 * @see #random(int)
	 * @see Random#nextInt(int)
	 * @since 1.2
	 */
	static IntSequence random(Supplier<? extends Random> randomSupplier, int upper) {
		return random(randomSupplier, 0, upper);
	}

	/**
	 * @return an {@code IntSequence} of random ints between the lower bound, inclusive, and upper bound, exclusive,
	 * that never terminates. Each run of this {@code IntSequence}'s {@link #iterator()} will produce a new random
	 * sequence of ints. This method is equivalent to {@code random(Random::new, lower, upper}.
	 *
	 * @see #random(Supplier, int, int)
	 * @see Random#nextInt(int)
	 * @since 1.2
	 */
	static IntSequence random(int lower, int upper) {
		return random(Random::new, lower, upper);
	}

	/**
	 * @return an {@code IntSequence} of random ints between the lower bound, inclusive, and upper bound, exclusive,
	 * that never terminates. The given supplier is used to produce the instance of {@link Random} that is used, one
	 * for each new {@link #iterator()}.
	 *
	 * @see #random(int, int)
	 * @see Random#nextInt(int)
	 * @since 1.2
	 */
	static IntSequence random(Supplier<? extends Random> randomSupplier, int lower, int upper) {
		return multiGenerate(() -> {
			Random random = randomSupplier.get();
			int bound = upper - lower;
			return () -> random.nextInt(bound) + lower;
		});
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
	 * Begin this {@code IntSequence} just after the given element is encountered, not including the element in the
	 * {@code IntSequence}.
	 *
	 * @see #startingAfter(IntPredicate)
	 * @see #startingFrom(int)
	 * @since 1.1
	 */
	default IntSequence startingAfter(int element) {
		return () -> new ExclusiveStartingIntIterator(iterator(), element);
	}

	/**
	 * Begin this {@code IntSequence} when the given element is encountered, including the element as the first element
	 * in the {@code IntSequence}.
	 *
	 * @see #startingFrom(IntPredicate)
	 * @see #startingAfter(int)
	 * @since 1.1
	 */
	default IntSequence startingFrom(int element) {
		return () -> new InclusiveStartingIntIterator(iterator(), element);
	}

	/**
	 * Begin this {@code IntSequence} just after the given predicate is satisfied, not including the element that
	 * satisfies the predicate in the {@code IntSequence}.
	 *
	 * @see #startingAfter(int)
	 * @see #startingFrom(IntPredicate)
	 * @since 1.1
	 */
	default IntSequence startingAfter(IntPredicate predicate) {
		return () -> new ExclusiveStartingIntIterator(iterator(), predicate);
	}

	/**
	 * Begin this {@code IntSequence} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the first element in the {@code IntSequence}.
	 *
	 * @see #startingFrom(int)
	 * @see #startingAfter(IntPredicate)
	 * @since 1.1
	 */
	default IntSequence startingFrom(IntPredicate predicate) {
		return () -> new InclusiveStartingIntIterator(iterator(), predicate);
	}

	/**
	 * Map the values in this {@code IntSequence} sequence to another set of values specified by the given {@code
	 * mapper} function.
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
	 * Map the values in this {@code IntSequence} sequence to another set of values specified by the given {@code
	 * mapper} function, while providing the current index to the mapper.
	 *
	 * @since 1.2
	 */
	default IntSequence mapIndexed(IntBinaryOperator mapper) {
		return () -> new UnaryIntIterator(iterator()) {
			private int index;

			@Override
			public int nextInt() {
				return mapper.applyAsInt(iterator.nextInt(), index++);
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
		return () -> Iterators.from(iterator(), mapper);
	}

	/**
	 * Skip a set number of {@code ints} in this {@code IntSequence}.
	 */
	default IntSequence skip(int skip) {
		return () -> new SkippingIntIterator(iterator(), skip);
	}

	/**
	 * Skip a set number of {@code ints} at the end of this {@code IntSequence}.
	 *
	 * @since 1.1
	 */
	default IntSequence skipTail(int skip) {
		if (skip == 0)
			return this;

		return () -> new TailSkippingIntIterator(iterator(), skip);
	}

	/**
	 * Limit the maximum number of {@code ints} returned by this {@code IntSequence}.
	 */
	default IntSequence limit(int limit) {
		return () -> new LimitingIntIterator(iterator(), limit);
	}

	/**
	 * Append the given {@code ints} to the end of this {@code IntSequence}.
	 */
	default IntSequence append(int... ints) {
		return append(IntIterable.of(ints));
	}

	/**
	 * Append the {@code ints} in the given {@link IntIterable} to the end of this {@code IntSequence}.
	 */
	default IntSequence append(IntIterable that) {
		return new ChainingIntIterable(this, that)::iterator;
	}

	/**
	 * Append the {@link Integer}s in the given {@link Iterable} to the end of this {@code IntSequence}.
	 */
	default IntSequence append(Iterable<Integer> iterable) {
		return append(IntIterable.from(iterable));
	}

	/**
	 * Append the {@code ints} in the given {@link IntIterator} to the end of this {@code IntSequence}.
	 * <p>
	 * The appended {@code ints} will only be available on the first traversal of the resulting {@code IntSequence}.
	 *
	 * @see #cache(PrimitiveIterator.OfInt)
	 */
	default IntSequence append(PrimitiveIterator.OfInt iterator) {
		return append(IntIterable.once(iterator));
	}

	/**
	 * Append the {@link Integer}s in the given {@link Iterator} to the end of this {@code IntSequence}.
	 * <p>
	 * The appended {@link Integer}s will only be available on the first traversal of the resulting
	 * {@code IntSequence}.
	 *
	 * @see #cache(Iterator)
	 */
	default IntSequence append(Iterator<Integer> iterator) {
		return append(IntIterator.from(iterator));
	}

	/**
	 * Append the {@code int} values of the given {@link IntStream} to the end of this {@code IntSequence}.
	 * <p>
	 * The appended {@code ints} will only be available on the first traversal of the resulting {@code IntSequence}.
	 *
	 * @see #cache(IntStream)
	 */
	default IntSequence append(IntStream stream) {
		return append(stream.iterator());
	}

	/**
	 * Append the {@link Integer}s in the given {@link Stream} to the end of this {@code IntSequence}.
	 * <p>
	 * The appended {@link Integer}s will only be available on the first traversal of the resulting
	 * {@code IntSequence}.
	 *
	 * @see #cache(Stream)
	 */
	default IntSequence append(Stream<Integer> stream) {
		return append(stream.iterator());
	}

	/**
	 * Filter the elements in this {@code IntSequence}, keeping only the elements that match the given
	 * {@link IntPredicate}.
	 */
	default IntSequence filter(IntPredicate predicate) {
		return () -> new FilteringIntIterator(iterator(), predicate);
	}

	/**
	 * Filter the elements in this {@code IntSequence}, keeping only the elements that match the given
	 * {@link IntBiPredicate}, which is passed each {@code double} together with its index in the sequence.
	 *
	 * @since 1.2
	 */
	default IntSequence filterIndexed(IntBiPredicate predicate) {
		return () -> new IndexedFilteringIntIterator(iterator(), predicate);
	}

	/**
	 * Filter this {@code IntSequence} to another sequence of ints while peeking at the previous value in the
	 * sequence.
	 * <p>
	 * The predicate has access to the previous int and the current int in the iteration. If the current int is
	 * the first value in the sequence, and there is no previous value, the provided replacement value is used as
	 * the first previous value.
	 */
	default IntSequence filterBack(int firstPrevious, IntBiPredicate predicate) {
		return () -> new BackPeekingFilteringIntIterator(iterator(), firstPrevious, predicate);
	}

	/**
	 * Filter this {@code IntSequence} to another sequence of ints while peeking at the next int in the sequence.
	 * <p>
	 * The predicate has access to the current int and the next int in the iteration. If the current int is
	 * the last value in the sequence, and there is no next value, the provided replacement value is used as
	 * the last next value.
	 */
	default IntSequence filterForward(int lastNext, IntBiPredicate predicate) {
		return () -> new ForwardPeekingFilteringIntIterator(iterator(), lastNext, predicate);
	}

	/**
	 * @return an {@code IntSequence} containing only the {@code ints} found in the given target array.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default IntSequence including(int... elements) {
		return filter(e -> Arrayz.contains(elements, e));
	}

	/**
	 * @return an {@code IntSequence} containing only the {@code ints} not found in the given target array.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default IntSequence excluding(int... elements) {
		return filter(e -> !Arrayz.contains(elements, e));
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

		int result = iterator.reduce(iterator.nextInt(), operator);
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
		return at(0);
	}

	/**
	 * @return the second int of this {@code IntSequence} or an empty {@link OptionalInt} if there are less than two
	 * ints in the {@code IntSequence}.
	 */
	default OptionalInt second() {
		return at(1);
	}

	/**
	 * @return the third int of this {@code IntSequence} or an empty {@link OptionalInt} if there are less than
	 * three ints in the {@code IntSequence}.
	 */
	default OptionalInt third() {
		return at(2);
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
		do
			last = iterator.nextInt(); while (iterator.hasNext());

		return OptionalInt.of(last);
	}

	/**
	 * @return the {@code int} at the given index, or an empty {@link OptionalInt} if the {@code IntSequence} is
	 * smaller than the index.
	 *
	 * @since 1.2
	 */
	default OptionalInt at(int index) {
		IntIterator iterator = iterator();
		iterator.skip(index);

		if (!iterator.hasNext())
			return OptionalInt.empty();

		return OptionalInt.of(iterator.nextInt());
	}

	/**
	 * @return the first int of those in this {@code IntSequence} matching the given predicate, or an empty
	 * {@link OptionalInt} if there are no matching ints in the {@code IntSequence}.
	 *
	 * @see #filter(IntPredicate)
	 * @see #at(int, IntPredicate)
	 * @since 1.2
	 */
	default OptionalInt first(IntPredicate predicate) {
		return at(0, predicate);
	}

	/**
	 * @return the second int of those in this {@code IntSequence} matching the given predicate, or an empty
	 * {@link OptionalInt} if there are less than two matching ints in the {@code IntSequence}.
	 *
	 * @see #filter(IntPredicate)
	 * @see #at(int, IntPredicate)
	 * @since 1.2
	 */
	default OptionalInt second(IntPredicate predicate) {
		return at(1, predicate);
	}

	/**
	 * @return the third int of those in this {@code IntSequence} matching the given predicate, or an empty
	 * {@link OptionalInt} if there are less than three matching ints in the {@code IntSequence}.
	 *
	 * @see #filter(IntPredicate)
	 * @see #at(int, IntPredicate)
	 * @since 1.2
	 */
	default OptionalInt third(IntPredicate predicate) {
		return at(2, predicate);
	}

	/**
	 * @return the last int of those in this {@code IntSequence} matching the given predicate, or an empty
	 * {@link OptionalInt} if there are no matching ints in the {@code IntSequence}.
	 *
	 * @see #filter(IntPredicate)
	 * @see #at(int, IntPredicate)
	 * @since 1.2
	 */
	default OptionalInt last(IntPredicate predicate) {
		return filter(predicate).last();
	}

	/**
	 * @return the {@code int} at the given index out of ints matching the given predicate, or an empty
	 * {@link OptionalInt} if the matching {@code IntSequence} is smaller than the index.
	 *
	 * @see #filter(IntPredicate)
	 * @since 1.2
	 */
	default OptionalInt at(int index, IntPredicate predicate) {
		return filter(predicate).at(index);
	}

	/**
	 * Skip x number of steps in between each invocation of the iterator of this {@code IntSequence}.
	 */
	default IntSequence step(int step) {
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
	 *
	 * @since 1.2
	 */
	@Override
	default int size() {
		return iterator().size();
	}

	/**
	 * @return an unsized {@link Spliterator.OfInt} for this {@code IntSequence}.
	 *
	 * @since 2.0
	 */
	@Override
	default Spliterator.OfInt spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), 0);
	}

	/**
	 * @return true if all ints in this {@code IntSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean all(IntPredicate predicate) {
		for (IntIterator iterator = iterator(); iterator.hasNext(); )
			if (!predicate.test(iterator.nextInt()))
				return false;

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
		for (IntIterator iterator = iterator(); iterator.hasNext(); )
			if (predicate.test(iterator.nextInt()))
				return true;

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
	 * Allow the given {@link IntBiConsumer} to see each element together with its index in this {@code IntSequence}
	 * as it is traversed.
	 *
	 * @since 1.2.2
	 */
	default IntSequence peekIndexed(IntBiConsumer action) {
		return () -> new UnaryIntIterator(iterator()) {
			private int index;

			@Override
			public int nextInt() {
				int next = iterator.nextInt();
				action.accept(next, index++);
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
		return () -> {
			int[] array = toIntArray();
			Arrays.sort(array);
			return IntIterator.of(array);
		};
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
	 * Interleave the elements in this {@code IntSequence} with those of the given {@code IntIterable}, stopping when
	 * either sequence finishes.
	 */
	default IntSequence interleave(IntIterable that) {
		return () -> new InterleavingIntIterator(this, that);
	}

	/**
	 * @return an {@code IntSequence} which iterates over this {@code IntSequence} in reverse order.
	 *
	 * @see #sorted()
	 */
	default IntSequence reverse() {
		return () -> IntIterator.of(Arrayz.reverse(toIntArray()));
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
		return () -> CharIterator.from(iterator());
	}

	/**
	 * Convert this sequence of ints to a sequence of longs.
	 */
	default LongSequence toLongs() {
		return () -> LongIterator.from(iterator());
	}

	/**
	 * Convert this sequence of ints to a sequence of doubles corresponding to the cast double value of each int.
	 */
	default DoubleSequence toDoubles() {
		return () -> DoubleIterator.from(iterator());
	}

	/**
	 * Convert this sequence of ints to a sequence of chars using the given converter function.
	 */
	default CharSeq toChars(IntToCharFunction mapper) {
		return () -> CharIterator.from(iterator(), mapper);
	}

	/**
	 * Convert this sequence of ints to a sequence of longs using the given converter function.
	 */
	default LongSequence toLongs(IntToLongFunction mapper) {
		return () -> LongIterator.from(iterator(), mapper);
	}

	/**
	 * Convert this sequence of ints to a sequence of doubles using the given converter function.
	 */
	default DoubleSequence toDoubles(IntToDoubleFunction mapper) {
		return () -> DoubleIterator.from(iterator(), mapper);
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
	default IntSequence repeat(int times) {
		return () -> new RepeatingIntIterator(this, times);
	}

	/**
	 * Window the elements of this {@code IntSequence} into a sequence of {@code IntSequence}s of elements, each with
	 * the size of the given window. The first item in each list is the second item in the previous list. The final
	 * {@code IntSequence} may be shorter than the window. This is equivalent to {@code window(window, 1)}.
	 */
	default Sequence<IntSequence> window(int window) {
		return window(window, 1);
	}

	/**
	 * Window the elements of this {@code IntSequence} into a sequence of {@code IntSequence}s of elements, each with
	 * the size of the given window, stepping {@code step} elements between each window. If the given step is less than
	 * the window size, the windows will overlap each other.
	 */
	default Sequence<IntSequence> window(int window, int step) {
		return () -> new WindowingIntIterator(iterator(), window, step);
	}

	/**
	 * Batch the elements of this {@code IntSequence} into a sequence of {@code IntSequence}s of distinct elements,
	 * each with the given batch size. This is equivalent to {@code window(size, size)}.
	 */
	default Sequence<IntSequence> batch(int size) {
		return window(size, size);
	}

	/**
	 * Batch the elements of this {@code IntSequence} into a sequence of {@code IntSequence}s of distinct elements,
	 * where the given predicate determines where to split the lists of partitioned elements. The predicate is given
	 * the current and next item in the iteration, and if it returns true a partition is created between the elements.
	 */
	default Sequence<IntSequence> batch(IntBiPredicate predicate) {
		return () -> new PredicatePartitioningIntIterator<>(iterator(), predicate);
	}

	/**
	 * Split the {@code ints} of this {@code IntSequence} into a sequence of {@code IntSequence}s of distinct elements,
	 * around the given {@code int}. The elements around which the sequence is split are not included in the result.
	 *
	 * @since 1.1
	 */
	default Sequence<IntSequence> split(int element) {
		return () -> new SplittingIntIterator(iterator(), element);
	}

	/**
	 * Split the {@code ints} of this {@code IntSequence} into a sequence of {@code IntSequence}s of distinct
	 * elements, where the given predicate determines which {@code ints} to split the partitioned elements around. The
	 * {@code ints} matching the predicate are not included in the result.
	 *
	 * @since 1.1
	 */
	default Sequence<IntSequence> split(IntPredicate predicate) {
		return () -> new SplittingIntIterator(iterator(), predicate);
	}

	/**
	 * Perform the given action for each {@code int} in this {@code IntSequence}, with the index of each element passed
	 * as the second parameter in the action.
	 *
	 * @since 1.2
	 */
	default void forEachIntIndexed(IntBiConsumer action) {
		int index = 0;
		for (IntIterator iterator = iterator(); iterator.hasNext(); )
			action.accept(iterator.nextInt(), index++);
	}
}
