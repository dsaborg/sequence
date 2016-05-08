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

import org.d2ab.function.chars.LongToCharFunction;
import org.d2ab.function.longs.LongBiConsumer;
import org.d2ab.function.longs.LongBiPredicate;
import org.d2ab.iterable.Iterables;
import org.d2ab.iterable.longs.ChainingLongIterable;
import org.d2ab.iterable.longs.LongIterable;
import org.d2ab.iterator.Iterators;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.iterator.longs.*;
import org.d2ab.util.Arrayz;

import java.util.*;
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
		return once(emptyIterator());
	}

	/**
	 * Create a {@code LongSequence} with the given longs.
	 */
	static LongSequence of(long... ls) {
		return () -> new ArrayLongIterator(ls);
	}

	/**
	 * Create a {@code LongSequence} from a {@link LongIterable}.
	 *
	 * @see #cache(LongIterable)
	 */
	static LongSequence from(LongIterable iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a {@code LongSequence} from an {@link Iterable} of {@code Long} values.
	 *
	 * @see #cache(Iterable)
	 */
	static LongSequence from(Iterable<Long> iterable) {
		return from(LongIterable.from(iterable));
	}

	/**
	 * Create a once-only {@code LongSequence} from a {@link PrimitiveIterator.OfLong} of long values. Note that {@code
	 * LongSequence}s created from {@link PrimitiveIterator.OfLong}s cannot be passed over more than once. Further
	 * attempts will register the {@code LongSequence} as empty.
	 *
	 * @see #cache(PrimitiveIterator.OfLong)
	 * @since 1.1
	 */
	static LongSequence once(PrimitiveIterator.OfLong iterator) {
		return from(LongIterable.once(iterator));
	}

	/**
	 * Create a once-only {@code LongSequence} from an {@link Iterator} of {@code Long} values. Note that
	 * {@code LongSequence}s created from {@link Iterator}s cannot be passed over more than once. Further attempts will
	 * register the {@code LongSequence} as empty.
	 *
	 * @see #cache(Iterator)
	 * @since 1.1
	 */
	static LongSequence once(Iterator<Long> iterator) {
		return once(LongIterator.from(iterator));
	}

	/**
	 * Create a once-only @code Sequence} from a {@link LongStream} of items. Note that {@code Sequences} created from
	 * {@link LongStream}s cannot be passed over more than once. Further attempts will register the
	 * {@code LongSequence} as empty.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 * @see #cache(LongStream)
	 * @since 1.1
	 */
	static LongSequence once(LongStream stream) {
		return once(stream.iterator());
	}

	/**
	 * Create a once-only {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created from
	 * {@link Stream}s cannot be passed over more than once. Further attempts will register the {@code LongSequence} as
	 * empty.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 * @see #cache(Stream)
	 * @since 1.1
	 */
	static LongSequence once(Stream<Long> stream) {
		return once(stream.iterator());
	}

	/**
	 * Create a once-only {@code LongSequence} from a {@link PrimitiveIterator.OfLong} of long values. Note that {@code
	 * LongSequence}s created from {@link PrimitiveIterator.OfLong}s cannot be passed over more than once. Further
	 * attempts will register the {@code LongSequence} as empty.
	 *
	 * @see #cache(PrimitiveIterator.OfLong)
	 * @deprecated Use {@link #once(PrimitiveIterator.OfLong)} instead.
	 */
	@Deprecated
	static LongSequence from(PrimitiveIterator.OfLong iterator) {
		return once(iterator);
	}

	/**
	 * Create a once-only {@code LongSequence} from an {@link Iterator} of {@code Long} values. Note that
	 * {@code LongSequence}s created from {@link Iterator}s cannot be passed over more than once. Further attempts will
	 * register the {@code LongSequence} as empty.
	 *
	 * @see #cache(Iterator)
	 * @deprecated Use {@link #once(Iterator)} instead.
	 */
	@Deprecated
	static LongSequence from(Iterator<Long> iterator) {
		return once(iterator);
	}

	/**
	 * Create a once-only @code Sequence} from a {@link LongStream} of items. Note that {@code Sequences} created from
	 * {@link LongStream}s cannot be passed over more than once. Further attempts will register the
	 * {@code LongSequence} as empty.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 * @see #cache(LongStream)
	 * @deprecated Use {@link #once(LongStream)} instead.
	 */
	@Deprecated
	static LongSequence from(LongStream stream) {
		return once(stream);
	}

	/**
	 * Create a once-only {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created from
	 * {@link Stream}s cannot be passed over more than once. Further attempts will register the {@code LongSequence} as
	 * empty.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 * @see #cache(Stream)
	 * @deprecated Use {@link #once(Stream)} instead.
	 */
	@Deprecated
	static LongSequence from(Stream<Long> stream) {
		return once(stream);
	}

	/**
	 * Create a {@code LongSequence} from a cached copy of a {@link PrimitiveIterator.OfLong}.
	 *
	 * @see #cache(Iterator)
	 * @see #cache(LongStream)
	 * @see #cache(Stream)
	 * @see #cache(LongIterable)
	 * @see #cache(Iterable)
	 * @see #once(PrimitiveIterator.OfLong)
	 * @since 1.1
	 */
	static LongSequence cache(PrimitiveIterator.OfLong iterator) {
		long[] cache = new long[10];
		int position = 0;
		while (iterator.hasNext()) {
			long next = iterator.nextLong();
			if (position == cache.length)
				cache = Arrays.copyOf(cache, cache.length * 2);
			cache[position++] = next;
		}
		if (cache.length > position)
			cache = Arrays.copyOf(cache, position);
		return of(cache);
	}

	/**
	 * Create a {@code LongSequence} from a cached copy of an {@link Iterator} of {@link Long}s.
	 *
	 * @see #cache(PrimitiveIterator.OfLong)
	 * @see #cache(LongStream)
	 * @see #cache(Stream)
	 * @see #cache(LongIterable)
	 * @see #cache(Iterable)
	 * @see #once(Iterator)
	 * @since 1.1
	 */
	static LongSequence cache(Iterator<Long> iterator) {
		return cache(LongIterator.from(iterator));
	}

	/**
	 * Create a {@code LongSequence} from a cached copy of a {@link LongStream}.
	 *
	 * @see #cache(Stream)
	 * @see #cache(LongIterable)
	 * @see #cache(Iterable)
	 * @see #cache(PrimitiveIterator.OfLong)
	 * @see #cache(Iterator)
	 * @see #once(LongStream)
	 * @since 1.1
	 */
	static LongSequence cache(LongStream stream) {
		return cache(stream.iterator());
	}

	/**
	 * Create a {@code LongSequence} from a cached copy of a {@link Stream} of {@link Long}s.
	 *
	 * @see #cache(LongStream)
	 * @see #cache(LongIterable)
	 * @see #cache(Iterable)
	 * @see #cache(PrimitiveIterator.OfLong)
	 * @see #cache(Iterator)
	 * @see #once(Stream)
	 * @since 1.1
	 */
	static LongSequence cache(Stream<Long> stream) {
		return cache(stream.iterator());
	}

	/**
	 * Create a {@code LongSequence} from a cached copy of an {@link LongIterable}.
	 *
	 * @see #cache(Iterable)
	 * @see #cache(LongStream)
	 * @see #cache(Stream)
	 * @see #cache(PrimitiveIterator.OfLong)
	 * @see #cache(Iterator)
	 * @see #from(LongIterable)
	 * @since 1.1
	 */
	static LongSequence cache(LongIterable iterable) {
		return cache(iterable.iterator());
	}

	/**
	 * Create a {@code LongSequence} from a cached copy of an {@link Iterable} of {@code Long} values.
	 *
	 * @see #cache(LongIterable)
	 * @see #cache(LongStream)
	 * @see #cache(Stream)
	 * @see #cache(PrimitiveIterator.OfLong)
	 * @see #cache(Iterator)
	 * @see #from(Iterable)
	 * @since 1.1
	 */
	static LongSequence cache(Iterable<Long> iterable) {
		return cache(iterable.iterator());
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
	 * @return a {@code LongSequence} where each {@link #iterator()} is generated by polling for a supplier and then
	 * using it to generate the sequence of {@code longs}. The sequence never terminates.
	 *
	 * @see #recurse(long, LongUnaryOperator)
	 * @see #endingAt(long)
	 * @see #until(long)
	 */
	static LongSequence multiGenerate(Supplier<? extends LongSupplier> supplierSupplier) {
		return () -> {
			LongSupplier longSupplier = supplierSupplier.get();
			return (InfiniteLongIterator) longSupplier::getAsLong;
		};
	}

	/**
	 * @return a {@code LongSequence} of random longs that never terminates. Each run of this {@code LongSequence}'s
	 * {@link #iterator()} will produce a new random sequence of longs. This method is equivalent to {@code random
	 * (Random::new)}.
	 *
	 * @see #random(Supplier)
	 * @see Random#nextLong()
	 * @since 1.2
	 */
	static LongSequence random() {
		return random(Random::new);
	}

	/**
	 * @return a {@code LongSequence} of random longs that never terminates. The given supplier is used to produce the
	 * instance of {@link Random} that is used, one for each new {@link #iterator()}.
	 *
	 * @see #random()
	 * @see Random#nextLong()
	 * @since 1.2
	 */
	static LongSequence random(Supplier<? extends Random> randomSupplier) {
		return multiGenerate(() -> {
			Random random = randomSupplier.get();
			return random::nextLong;
		});
	}

	/**
	 * @return a {@code LongSequence} of random longs between {@code 0}, inclusive, and the upper bound, exclusive,
	 * that never terminates. Each run of this {@code LongSequence}'s {@link #iterator()} will produce a new random
	 * sequence of longs. This method is equivalent to {@code random(Random::new, upper}.
	 *
	 * @see #random(Supplier, long)
	 * @see Random#nextDouble()
	 * @since 1.2
	 */
	static LongSequence random(long upper) {
		return random(0, upper);
	}

	/**
	 * @return a {@code LongSequence} of random longs between {@code 0}, inclusive, and the upper bound, exclusive,
	 * that never terminates. The given supplier is used to produce the instance of {@link Random} that is used, one
	 * for each new {@link #iterator()}.
	 *
	 * @see #random(long)
	 * @see Random#nextDouble()
	 * @since 1.2
	 */
	static LongSequence random(Supplier<? extends Random> randomSupplier, long upper) {
		return random(randomSupplier, 0, upper);
	}

	/**
	 * @return a {@code LongSequence} of random longs between the lower bound, inclusive, and upper bound, exclusive,
	 * that never terminates. Each run of this {@code LongSequence}'s {@link #iterator()} will produce a new random
	 * sequence of longs. This method is equivalent to {@code random(Random::new, lower, upper}.
	 *
	 * @see #random(Supplier, long, long)
	 * @see Random#nextDouble()
	 * @since 1.2
	 */
	static LongSequence random(long lower, long upper) {
		return random(Random::new, lower, upper);
	}

	/**
	 * @return a {@code LongSequence} of random longs between the lower bound, inclusive, and upper bound, exclusive,
	 * that never terminates. The given supplier is used to produce the instance of {@link Random} that is used, one
	 * for each new {@link #iterator()}.
	 *
	 * @see #random(long, long)
	 * @see Random#nextDouble()
	 * @since 1.2
	 */
	static LongSequence random(Supplier<? extends Random> randomSupplier, long lower, long upper) {
		return multiGenerate(() -> {
			Random random = randomSupplier.get();
			long bound = upper - lower;
			return () -> (long) (random.nextDouble() * bound) + lower;
		});
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
	 * Begin this {@code LongSequence} just after the given element is encountered, not including the element in the
	 * {@code LongSequence}.
	 *
	 * @see #startingAfter(LongPredicate)
	 * @see #startingFrom(long)
	 * @since 1.1
	 */
	default LongSequence startingAfter(long element) {
		return () -> new ExclusiveStartingLongIterator(iterator(), element);
	}

	/**
	 * Begin this {@code LongSequence} when the given element is encountered, including the element as the first
	 * element
	 * in the {@code LongSequence}.
	 *
	 * @see #startingFrom(LongPredicate)
	 * @see #startingAfter(long)
	 * @since 1.1
	 */
	default LongSequence startingFrom(long element) {
		return () -> new InclusiveStartingLongIterator(iterator(), element);
	}

	/**
	 * Begin this {@code LongSequence} just after the given predicate is satisfied, not including the element that
	 * satisfies the predicate in the {@code LongSequence}.
	 *
	 * @see #startingAfter(long)
	 * @see #startingFrom(LongPredicate)
	 * @since 1.1
	 */
	default LongSequence startingAfter(LongPredicate predicate) {
		return () -> new ExclusiveStartingLongIterator(iterator(), predicate);
	}

	/**
	 * Begin this {@code LongSequence} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the first element in the {@code LongSequence}.
	 *
	 * @see #startingFrom(long)
	 * @see #startingAfter(LongPredicate)
	 * @since 1.1
	 */
	default LongSequence startingFrom(LongPredicate predicate) {
		return () -> new InclusiveStartingLongIterator(iterator(), predicate);
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
	 * Map the {@code longs} in this {@code LongSequence} to another set of {@code longs} specified by the given
	 * {@code mapper} function, while providing the current index to the mapper.
	 */
	default LongSequence mapIndexed(LongBinaryOperator mapper) {
		return () -> new UnaryLongIterator(iterator()) {
			private long index;

			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.nextLong(), index++);
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
		return () -> Iterators.from(iterator(), mapper);
	}

	/**
	 * Skip a set number of {@code longs} in this {@code LongSequence}.
	 */
	default LongSequence skip(long skip) {
		return () -> new SkippingLongIterator(iterator(), skip);
	}

	/**
	 * Skip a set number of {@code longs} at the end of this {@code LongSequence}.
	 *
	 * @since 1.1
	 */
	default LongSequence skipTail(int skip) {
		if (skip == 0)
			return this;

		return () -> new TailSkippingLongIterator(iterator(), skip);
	}

	/**
	 * Limit the maximum number of {@code longs} returned by this {@code LongSequence}.
	 */
	default LongSequence limit(long limit) {
		return () -> new LimitingLongIterator(iterator(), limit);
	}

	/**
	 * Append the given {@code longs} to the end of this {@code LongSequence}.
	 */
	default LongSequence append(long... longs) {
		return append(LongIterable.of(longs));
	}

	/**
	 * Append the {@code longs} in the given {@link LongIterable} to the end of this {@code LongSequence}.
	 */
	default LongSequence append(LongIterable iterable) {
		return new ChainingLongIterable(this, iterable)::iterator;
	}

	/**
	 * Append the {@link Long}s in the given {@link Iterable} to the end of this {@code LongSequence}.
	 */
	default LongSequence append(Iterable<Long> iterable) {
		return append(LongIterable.from(iterable));
	}

	/**
	 * Append the {@code longs} in the given {@link PrimitiveIterator.OfLong} to the end of this {@code LongSequence}.
	 * <p>
	 * The appended {@code longs} will only be available on the first traversal of the resulting {@code LongSequence}.
	 */
	default LongSequence append(PrimitiveIterator.OfLong iterator) {
		return append(LongIterable.once(iterator));
	}

	/**
	 * Append the {@link Long}s in the given {@link Iterator} to the end of this {@code LongSequence}.
	 * <p>
	 * The appended {@link Long}s will only be available on the first traversal of the resulting {@code LongSequence}.
	 */
	default LongSequence append(Iterator<Long> iterator) {
		return append(LongIterator.from(iterator));
	}

	/**
	 * Append the {@code long} values of the given {@link LongStream} to the end of this {@code LongSequence}.
	 * <p>
	 * The appended {@code longs} will only be available on the first traversal of the resulting {@code LongSequence}.
	 */
	default LongSequence append(LongStream stream) {
		return append(stream.iterator());
	}

	/**
	 * Append the {@link Long}s in the given {@link Stream} to the end of this {@code LongSequence}.
	 * <p>
	 * The appended {@link Long}s will only be available on the first traversal of the resulting {@code LongSequence}.
	 */
	default LongSequence append(Stream<Long> stream) {
		return append(stream.iterator());
	}

	/**
	 * Filter the elements in this {@code LongSequence}, keeping only the elements that match the given
	 * {@link LongPredicate}.
	 */
	default LongSequence filter(LongPredicate predicate) {
		return () -> new FilteringLongIterator(iterator(), predicate);
	}

	/**
	 * Filter this {@code LongSequence} to another sequence of longs while peeking at the previous value in the
	 * sequence.
	 * <p>
	 * The predicate has access to the previous long and the current long in the iteration. If the current long is
	 * the first value in the sequence, and there is no previous value, the provided replacement value is used as
	 * the first previous value.
	 */
	default LongSequence filterBack(long firstPrevious, LongBiPredicate predicate) {
		return () -> new BackPeekingFilteringLongIterator(iterator(), firstPrevious, predicate);
	}

	/**
	 * Filter this {@code LongSequence} to another sequence of longs while peeking at the next long in the sequence.
	 * <p>
	 * The predicate has access to the current long and the next long in the iteration. If the current long is
	 * the last value in the sequence, and there is no next value, the provided replacement value is used as
	 * the last next value.
	 */
	default LongSequence filterForward(long lastNext, LongBiPredicate predicate) {
		return () -> new ForwardPeekingFilteringLongIterator(iterator(), lastNext, predicate);
	}

	/**
	 * @return an {@code LongSequence} containing only the {@code longs} found in the given target array.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default LongSequence including(long... elements) {
		return filter(e -> Arrayz.contains(elements, e));
	}

	/**
	 * @return an {@code LongSequence} containing only the {@code longs} not found in the given target array.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default LongSequence excluding(long... elements) {
		return filter(e -> !Arrayz.contains(elements, e));
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

		long result = iterator.reduce(iterator.nextLong(), operator);
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
		return at(0);
	}

	/**
	 * @return the second long of this {@code LongSequence} or an empty {@link OptionalLong} if there are less than two
	 * longs in the {@code LongSequence}.
	 */
	default OptionalLong second() {
		return at(1);
	}

	/**
	 * @return the third long of this {@code LongSequence} or an empty {@link OptionalLong} if there are less than
	 * three longs in the {@code LongSequence}.
	 */
	default OptionalLong third() {
		return at(2);
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
		do
			last = iterator.nextLong(); while (iterator.hasNext());

		return OptionalLong.of(last);
	}

	/**
	 * @return the {@code long} at the given index, or an empty {@link OptionalLong} if the {@code LongSequence} is
	 * smaller than the index.
	 *
	 * @since 1.2
	 */
	default OptionalLong at(long index) {
		LongIterator iterator = iterator();
		iterator.skip(index);

		if (!iterator.hasNext())
			return OptionalLong.empty();

		return OptionalLong.of(iterator.nextLong());
	}

	/**
	 * @return the first long of those in this {@code LongSequence} matching the given predicate, or an empty
	 * {@link OptionalLong} if there are no matching longs in the {@code LongSequence}.
	 *
	 * @see #filter(LongPredicate)
	 * @see #at(long, LongPredicate)
	 * @since 1.2
	 */
	default OptionalLong first(LongPredicate predicate) {
		return at(0, predicate);
	}

	/**
	 * @return the second long of those in this {@code LongSequence} matching the given predicate, or an empty
	 * {@link OptionalLong} if there are less than two matching longs in the {@code LongSequence}.
	 *
	 * @see #filter(LongPredicate)
	 * @see #at(long, LongPredicate)
	 * @since 1.2
	 */
	default OptionalLong second(LongPredicate predicate) {
		return at(1, predicate);
	}

	/**
	 * @return the third long of those in this {@code LongSequence} matching the given predicate, or an empty
	 * {@link OptionalLong} if there are less than three matching longs in the {@code LongSequence}.
	 *
	 * @see #filter(LongPredicate)
	 * @see #at(long, LongPredicate)
	 * @since 1.2
	 */
	default OptionalLong third(LongPredicate predicate) {
		return at(2, predicate);
	}

	/**
	 * @return the last long of those in this {@code LongSequence} matching the given predicate, or an empty
	 * {@link OptionalLong} if there are no matching longs in the {@code LongSequence}.
	 *
	 * @see #filter(LongPredicate)
	 * @see #at(long, LongPredicate)
	 * @since 1.2
	 */
	default OptionalLong last(LongPredicate predicate) {
		return filter(predicate).last();
	}

	/**
	 * @return the {@code long} at the given index out of longs matching the given predicate, or an empty
	 * {@link OptionalLong} if the matching {@code LongSequence} is smaller than the index.
	 *
	 * @see #filter(LongPredicate)
	 * @since 1.2
	 */
	default OptionalLong at(long index, LongPredicate predicate) {
		return filter(predicate).at(index);
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
	 *
	 * @since 1.2
	 */
	default long size() {
		return iterator().count();
	}

	/**
	 * @return the count of longs in this {@code LongSequence}.
	 *
	 * @deprecated Use {@link #size()} instead.
	 */
	@Deprecated
	default long count() {
		return size();
	}

	/**
	 * @return true if all longs in this {@code LongSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean all(LongPredicate predicate) {
		for (LongIterator iterator = iterator(); iterator.hasNext(); )
			if (!predicate.test(iterator.nextLong()))
				return false;
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
		for (LongIterator iterator = iterator(); iterator.hasNext(); )
			if (predicate.test(iterator.nextLong()))
				return true;
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
		return () -> {
			long[] array = toArray();
			Arrays.sort(array);
			return LongIterator.of(array);
		};
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
	 * Interleave the elements in this {@code LongSequence} with those of the given {@code LongIterable}, stopping when
	 * either sequence finishes.
	 */
	default LongSequence interleave(LongIterable that) {
		return () -> new InterleavingLongIterator(this, that);
	}

	/**
	 * @return a {@code LongSequence} which iterates over this {@code LongSequence} in reverse order.
	 *
	 * @see #sorted()
	 */
	default LongSequence reverse() {
		return () -> LongIterator.of(Arrayz.reverse(toArray()));
	}

	/**
	 * Convert this sequence of longs to a sequence of chars corresponding to the downcast char value of each long.
	 */
	default CharSeq toChars() {
		return () -> CharIterator.from(iterator());
	}

	/**
	 * Convert this sequence of longs to a sequence of ints corresponding to the downcast integer value of each long.
	 */
	default IntSequence toInts() {
		return () -> IntIterator.from(iterator());
	}

	/**
	 * Convert this sequence of longs to a sequence of doubles corresponding to the cast double value of each long.
	 */
	default DoubleSequence toDoubles() {
		return () -> DoubleIterator.from(iterator());
	}

	/**
	 * Convert this sequence of longs to a sequence of chars using the given converter function.
	 */
	default CharSeq toChars(LongToCharFunction mapper) {
		return () -> CharIterator.from(iterator(), mapper);
	}

	/**
	 * Convert this sequence of longs to a sequence of ints using the given converter function.
	 */
	default IntSequence toInts(LongToIntFunction mapper) {
		return () -> IntIterator.from(iterator(), mapper);
	}

	/**
	 * Convert this sequence of longs to a sequence of doubles using the given converter function.
	 */
	default DoubleSequence toDoubles(LongToDoubleFunction mapper) {
		return () -> DoubleIterator.from(iterator(), mapper);
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

	/**
	 * Window the elements of this {@code LongSequence} into a sequence of {@code LongSequence}s of elements, each with
	 * the size of the given window. The first item in each list is the second item in the previous list. The final
	 * {@code LongSequence} may be shorter than the window. This is equivalent to {@code window(window, 1)}.
	 */
	default Sequence<LongSequence> window(int window) {
		return window(window, 1);
	}

	/**
	 * Window the elements of this {@code LongSequence} into a sequence of {@code LongSequence}s of elements, each with
	 * the size of the given window, stepping {@code step} elements between each window. If the given step is less than
	 * the window size, the windows will overlap each other.
	 */
	default Sequence<LongSequence> window(int window, int step) {
		return () -> new WindowingLongIterator(iterator(), window, step);
	}

	/**
	 * Batch the elements of this {@code LongSequence} into a sequence of {@code LongSequence}s of distinct elements,
	 * each with the given batch size. This is equivalent to {@code window(size, size)}.
	 */
	default Sequence<LongSequence> batch(int size) {
		return window(size, size);
	}

	/**
	 * Batch the elements of this {@code LongSequence} into a sequence of {@code LongSequence}s of distinct elements,
	 * where the given predicate determines where to split the lists of partitioned elements. The predicate is given
	 * the current and next item in the iteration, and if it returns true a partition is created between the elements.
	 */
	default Sequence<LongSequence> batch(LongBiPredicate predicate) {
		return () -> new PredicatePartitioningLongIterator<>(iterator(), predicate);
	}

	/**
	 * Split the {@code ints} of this {@code IntSequence} into a sequence of {@code IntSequence}s of distinct elements,
	 * around the given {@code int}. The elements around which the sequence is split are not included in the result.
	 *
	 * @since 1.1
	 */
	default Sequence<LongSequence> split(long element) {
		return () -> new SplittingLongIterator(iterator(), element);
	}

	/**
	 * Split the {@code longs} of this {@code LongSequence} into a sequence of {@code LongSequence}s of distinct
	 * elements, where the given predicate determines which {@code longs} to split the partitioned elements around. The
	 * {@code longs} matching the predicate are not included in the result.
	 *
	 * @since 1.1
	 */
	default Sequence<LongSequence> split(LongPredicate predicate) {
		return () -> new SplittingLongIterator(iterator(), predicate);
	}

	/**
	 * Remove all elements matched by this sequence using {@link Iterator#remove()}.
	 *
	 * @since 1.2
	 */
	default void clear() {
		Iterables.removeAll(this);
	}

	/**
	 * Remove all elements matched by this sequence using {@link Iterator#remove()}.
	 *
	 * @deprecated Use {@link #clear()} instead.
	 */
	@Deprecated
	default void removeAll() {
		clear();
	}

	/**
	 * @return true if this {@code LongSequence} is empty, false otherwise.
	 *
	 * @since 1.1
	 */
	default boolean isEmpty() {
		return !iterator().hasNext();
	}

	/**
	 * @return true if this {@code LongSequence} contains the given {@code long}, false otherwise.
	 *
	 * @since 1.2
	 */
	default boolean contains(long l) {
		return iterator().contains(l);
	}

	/**
	 * @return true if this {@code LongSequence} contains all of the given {@code longs}, false otherwise.
	 *
	 * @since 1.2
	 */
	default boolean containsAll(long... items) {
		for (long item : items)
			if (!iterator().contains(item))
				return false;

		return true;
	}

	/**
	 * @return true if this {@code LongSequence} contains any of the given {@code longs}, false otherwise.
	 *
	 * @since 1.2
	 */
	default boolean containsAny(long... items) {
		for (LongIterator iterator = iterator(); iterator.hasNext(); )
			if (Arrayz.contains(items, iterator.nextLong()))
				return true;

		return false;
	}

	/**
	 * Perform the given action for each {@code long} in this {@code LongSequence}, with the index of each element
	 * passed as the second parameter in the action.
	 *
	 * @since 1.2
	 */
	default void forEachLongIndexed(LongBiConsumer action) {
		long index = 0;
		for (LongIterator iterator = iterator(); iterator.hasNext(); )
			action.accept(iterator.nextLong(), index++);
	}
}
