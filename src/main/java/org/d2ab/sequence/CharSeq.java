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

import org.d2ab.function.chars.*;
import org.d2ab.function.ints.CharToIntFunction;
import org.d2ab.iterable.Iterables;
import org.d2ab.iterable.chars.ChainingCharIterable;
import org.d2ab.iterable.chars.CharIterable;
import org.d2ab.iterator.IterationException;
import org.d2ab.iterator.Iterators;
import org.d2ab.iterator.chars.*;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.util.Arrayz;
import org.d2ab.util.primitive.OptionalChar;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyIterator;

/**
 * An {@link Iterable} sequence of {@code char} values with {@link Stream}-like operations for refining,
 * transforming and collating the list of characters.
 */
@FunctionalInterface
public interface CharSeq extends CharIterable {
	/**
	 * Create an empty {@code CharSeq} with no characters.
	 */
	static CharSeq empty() {
		return from(emptyIterator());
	}

	/**
	 * Create a {@code CharSeq} with the given characters.
	 */
	static CharSeq of(char... cs) {
		return () -> CharIterator.of(cs);
	}

	/**
	 * Create a {@code CharSeq} from a {@link CharSequence}.
	 */
	static CharSeq from(CharSequence csq) {
		return () -> new CharSequenceCharIterator(csq);
	}

	/**
	 * Create a {@code CharSeq} from a {@link CharIterator} of character values. Note that {@code
	 * CharSeq}s created from {@link CharIterator}s cannot be passed over more than once. Further attempts
	 * will register the {@code CharSeq} as empty.
	 *
	 * @see #cache(CharIterator)
	 */
	static CharSeq from(CharIterator iterator) {
		return () -> iterator;
	}

	/**
	 * Create a {@code CharSeq} from a {@link PrimitiveIterator.OfInt} of character values. Note that {@code
	 * CharSeq}s created from {@link PrimitiveIterator.OfInt}s cannot be passed over more than once. Further attempts
	 * will register the {@code CharSeq} as empty.
	 *
	 * @see #cache(PrimitiveIterator.OfInt)
	 */
	static CharSeq from(PrimitiveIterator.OfInt iterator) {
		return from(CharIterator.from(iterator));
	}

	/**
	 * Create a {@code CharSeq} from an {@link Iterator} of {@code Character} values. Note that {@code
	 * CharSeq}s created from {@link Iterator}s cannot be passed over more than once. Further attempts will
	 * register the {@code CharSeq} as empty.
	 *
	 * @see #cache(Iterator)
	 */
	static CharSeq from(Iterator<Character> iterator) {
		return from(CharIterator.from(iterator));
	}

	/**
	 * Create a {@code CharSeq} from a {@link CharIterable}.
	 *
	 * @see #cache(CharIterable)
	 */
	static CharSeq from(CharIterable iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a {@code CharSeq} from an {@link Iterable} of {@code Character} values.
	 *
	 * @see #cache(Iterable)
	 */
	static CharSeq from(Iterable<Character> iterable) {
		return () -> CharIterator.from(iterable);
	}

	/**
	 * Create a {@code CharSeq} from a {@link Stream} of items. Note that {@code CharSeq} created from {@link
	 * Stream}s cannot be passed over more than once. Further attempts will register the {@code CharSeq} as empty.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 * @see #cache(Stream)
	 */
	static CharSeq from(Stream<Character> stream) {
		return from(stream.iterator());
	}

	/**
	 * Create a {@code CharSeq} from an {@link IntStream} of char values. Note that {@code CharSeq} created from
	 * {@link IntStream}s cannot be passed over more than once. Further attempts will register the {@code CharSeq} as
	 * empty.
	 *
	 * @throws IllegalStateException if the {@link IntStream} is exhausted.
	 * @see #cache(IntStream)
	 */
	static CharSeq from(IntStream stream) {
		return from(CharIterator.from(stream.iterator()));
	}

	/**
	 * Create a {@code CharSeq} from a {@link Reader} which iterates over the characters provided in the reader.
	 * The {@link Reader} must support {@link Reader#reset} or the {@code CharSeq} will only be available to iterate
	 * over once. The {@link Reader} will be reset in between iterations, if possible. If an {@link IOException}
	 * occurs during iteration, an {@link IterationException} will be thrown. The {@link Reader} will not be closed
	 * by the {@code CharSeq} when iteration finishes, it must be closed externally when iteration is finished.
	 */
	static CharSeq from(Reader reader) {
		return new CharSeq() {
			boolean started;

			@Override
			public CharIterator iterator() {
				if (started)
					try {
						reader.reset();
					} catch (IOException e) {
						// do nothing, let reader exhaust itself
					}
				else
					started = true;

				return new ReaderCharIterator(reader);
			}
		};
	}

	/**
	 * Create a {@code CharSeq} from a cached copy of a {@link CharIterator}.
	 *
	 * @see #cache(Iterator)
	 * @see #cache(PrimitiveIterator.OfInt)
	 * @see #cache(IntStream)
	 * @see #cache(Stream)
	 * @see #cache(CharIterable)
	 * @see #cache(Iterable)
	 * @see #from(CharIterator)
	 */
	static CharSeq cache(CharIterator iterator) {
		char[] cache = new char[10];
		int position = 0;
		while (iterator.hasNext()) {
			char next = iterator.nextChar();
			if (position == cache.length)
				cache = Arrays.copyOf(cache, cache.length * 2);
			cache[position++] = next;
		}
		if (cache.length > position)
			cache = Arrays.copyOf(cache, position);
		return of(cache);
	}

	/**
	 * Create a {@code CharSeq} from a cached copy of an {@link PrimitiveIterator.OfInt} of char values.
	 *
	 * @see #cache(CharIterator)
	 * @see #cache(Iterator)
	 * @see #cache(IntStream)
	 * @see #cache(Stream)
	 * @see #cache(CharIterable)
	 * @see #cache(Iterable)
	 * @see #from(Iterator)
	 */
	static CharSeq cache(PrimitiveIterator.OfInt iterator) {
		return cache(CharIterator.from(iterator));
	}

	/**
	 * Create a {@code CharSeq} from a cached copy of an {@link Iterator} of {@link Character}s.
	 *
	 * @see #cache(CharIterator)
	 * @see #cache(PrimitiveIterator.OfInt)
	 * @see #cache(IntStream)
	 * @see #cache(Stream)
	 * @see #cache(CharIterable)
	 * @see #cache(Iterable)
	 * @see #from(Iterator)
	 */
	static CharSeq cache(Iterator<Character> iterator) {
		return cache(CharIterator.from(iterator));
	}

	/**
	 * Create a {@code CharSeq} from a cached copy of a {@link IntStream} of char values.
	 *
	 * @see #cache(Stream)
	 * @see #cache(CharIterable)
	 * @see #cache(Iterable)
	 * @see #cache(CharIterator)
	 * @see #cache(PrimitiveIterator.OfInt)
	 * @see #cache(Iterator)
	 * @see #from(IntStream)
	 */
	static CharSeq cache(IntStream stream) {
		return cache(CharIterator.from(stream.iterator()));
	}

	/**
	 * Create a {@code CharSeq} from a cached copy of a {@link Stream} of {@link Character}s.
	 *
	 * @see #cache(IntStream)
	 * @see #cache(CharIterable)
	 * @see #cache(Iterable)
	 * @see #cache(CharIterator)
	 * @see #cache(PrimitiveIterator.OfInt)
	 * @see #cache(Iterator)
	 * @see #from(Stream)
	 */
	static CharSeq cache(Stream<Character> stream) {
		return cache(stream.iterator());
	}

	/**
	 * Create a {@code CharSeq} from a cached copy of an {@link CharIterable}.
	 *
	 * @see #cache(Iterable)
	 * @see #cache(IntStream)
	 * @see #cache(Stream)
	 * @see #cache(CharIterator)
	 * @see #cache(PrimitiveIterator.OfInt)
	 * @see #cache(Iterator)
	 * @see #from(CharIterable)
	 */
	static CharSeq cache(CharIterable iterable) {
		return cache(iterable.iterator());
	}

	/**
	 * Create a {@code CharSeq} from a cached copy of an {@link Iterable} of {@code Character} values.
	 *
	 * @see #cache(CharIterable)
	 * @see #cache(IntStream)
	 * @see #cache(Stream)
	 * @see #cache(CharIterator)
	 * @see #cache(PrimitiveIterator.OfInt)
	 * @see #cache(Iterator)
	 * @see #from(Iterable)
	 */
	static CharSeq cache(Iterable<Character> iterable) {
		return cache(iterable.iterator());
	}

	/**
	 * A {@code CharSeq} of all the {@link Character} values starting at {@link Character#MIN_VALUE} and ending at
	 * {@link Character#MAX_VALUE}.
	 *
	 * @see #startingFrom(char)
	 * @see #range(char, char)
	 * @see #until(char)
	 * @see #endingAt(char)
	 */
	static CharSeq all() {
		return startingAt((char) 0);
	}

	/**
	 * A {@code CharSeq} of all the {@link Character} values starting at the given value and ending at {@link
	 * Character#MAX_VALUE}.
	 *
	 * @see #all()
	 * @see #range(char, char)
	 * @see #until(char)
	 * @see #endingAt(char)
	 */
	static CharSeq startingAt(char start) {
		return range(start, Character.MAX_VALUE);
	}

	/**
	 * A {@code CharSeq} of all the {@link Character} values between the given start and end positions, inclusive.
	 *
	 * @see #all()
	 * @see #startingFrom(char)
	 * @see #until(char)
	 * @see #endingAt(char)
	 */
	static CharSeq range(char start, char end) {
		CharUnaryOperator next = (end > start) ? c -> (char) ++c : c -> (char) --c;
		return recurse(start, next).endingAt(end);
	}

	/**
	 * Returns a {@code CharSeq} sequence produced by recursively applying the given operation to the given seed, which
	 * forms the first element of the sequence, the second being f(seed), the third f(f(seed)) and so on. The returned
	 * {@code CharSeq} sequence never terminates naturally.
	 *
	 * @return a {@code CharSeq} sequence produced by recursively applying the given operation to the given seed
	 *
	 * @see #generate(CharSupplier)
	 * @see #endingAt(char)
	 * @see #until(char)
	 */
	static CharSeq recurse(char seed, CharUnaryOperator op) {
		return () -> new InfiniteCharIterator() {
			private char previous;
			private boolean hasPrevious;

			@Override
			public char nextChar() {
				previous = hasPrevious ? op.applyAsChar(previous) : seed;
				hasPrevious = true;
				return previous;
			}
		};
	}

	/**
	 * @return a sequence of {@code CharSeq} that is generated from the given supplier and thus never terminates.
	 *
	 * @see #recurse(char, CharUnaryOperator)
	 * @see #endingAt(char)
	 * @see #until(char)
	 */
	static CharSeq generate(CharSupplier supplier) {
		return () -> (InfiniteCharIterator) supplier::getAsChar;
	}

	/**
	 * Terminate this {@code CharSeq} sequence before the given element, with the previous element as the last
	 * element in this {@code CharSeq} sequence.
	 *
	 * @see #until(CharPredicate)
	 * @see #endingAt(char)
	 * @see #generate(CharSupplier)
	 * @see #recurse(char, CharUnaryOperator)
	 */
	default CharSeq until(char terminal) {
		return () -> new ExclusiveTerminalCharIterator(iterator(), terminal);
	}

	/**
	 * Terminate this {@code CharSeq} sequence at the given element, including it as the last element in this {@code
	 * CharSeq} sequence.
	 *
	 * @see #endingAt(CharPredicate)
	 * @see #until(char)
	 * @see #generate(CharSupplier)
	 * @see #recurse(char, CharUnaryOperator)
	 */
	default CharSeq endingAt(char terminal) {
		return () -> new InclusiveTerminalCharIterator(iterator(), terminal);
	}

	/**
	 * Terminate this {@code CharSeq} sequence before the element that satisfies the given predicate, with the previous
	 * element as the last element in this {@code CharSeq} sequence.
	 *
	 * @see #until(char)
	 * @see #endingAt(char)
	 * @see #generate(CharSupplier)
	 * @see #recurse(char, CharUnaryOperator)
	 */
	default CharSeq until(CharPredicate terminal) {
		return () -> new ExclusiveTerminalCharIterator(iterator(), terminal);
	}

	/**
	 * Terminate this {@code CharSeq} sequence at the element that satisfies the given predicate, including the
	 * element as the last element in this {@code CharSeq} sequence.
	 *
	 * @see #endingAt(char)
	 * @see #until(char)
	 * @see #generate(CharSupplier)
	 * @see #recurse(char, CharUnaryOperator)
	 */
	default CharSeq endingAt(CharPredicate terminal) {
		return () -> new InclusiveTerminalCharIterator(iterator(), terminal);
	}

	/**
	 * Begin this {@code CharSeq} just after the given element is encountered, not including the element in the
	 * {@code CharSeq}.
	 *
	 * @see #startingAfter(CharPredicate)
	 * @see #startingFrom(char)
	 */
	default CharSeq startingAfter(char element) {
		return () -> new ExclusiveStartingCharIterator(iterator(), element);
	}

	/**
	 * Begin this {@code CharSeq} when the given element is encountered, including the element as the first element
	 * in the {@code CharSeq}.
	 *
	 * @see #startingFrom(CharPredicate)
	 * @see #startingAfter(char)
	 */
	default CharSeq startingFrom(char element) {
		return () -> new InclusiveStartingCharIterator(iterator(), element);
	}

	/**
	 * Begin this {@code CharSeq} just after the given predicate is satisfied, not including the element that
	 * satisfies the predicate in the {@code CharSeq}.
	 *
	 * @see #startingAfter(char)
	 * @see #startingFrom(CharPredicate)
	 */
	default CharSeq startingAfter(CharPredicate predicate) {
		return () -> new ExclusiveStartingCharIterator(iterator(), predicate);
	}

	/**
	 * Begin this {@code CharSeq} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the first element in the {@code CharSeq}.
	 *
	 * @see #startingFrom(char)
	 * @see #startingAfter(CharPredicate)
	 */
	default CharSeq startingFrom(CharPredicate predicate) {
		return () -> new InclusiveStartingCharIterator(iterator(), predicate);
	}

	/**
	 * Map the {@code chars} in this {@code CharSeq} to another set of {@code chars} specified by the given
	 * {@code mapper} function.
	 */
	default CharSeq map(CharUnaryOperator mapper) {
		return () -> new UnaryCharIterator(iterator()) {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.nextChar());
			}
		};
	}

	/**
	 * Map the {@code chars} in this {@code CharSeq} to their boxed {@link Character} counterparts.
	 */
	default Sequence<Character> box() {
		return toSequence(Character::valueOf);
	}

	/**
	 * Map the {@code chars} in this {@code CharSeq} to a {@link Sequence} of values.
	 */
	default <T> Sequence<T> toSequence(CharFunction<T> mapper) {
		return () -> Iterators.from(iterator(), mapper);
	}

	/**
	 * Skip a set number of {@code chars} in this {@code CharSeq}.
	 */
	default CharSeq skip(long skip) {
		return () -> new SkippingCharIterator(iterator(), skip);
	}

	/**
	 * Skip a set number of {@code chars} at the end of this {@code CharSequence}.
	 */
	default CharSeq skipTail(int skip) {
		if (skip == 0)
			return this;

		return () -> new TailSkippingCharIterator(iterator(), skip);
	}

	/**
	 * Limit the maximum number of {@code chars} returned by this {@code CharSeq}.
	 */
	default CharSeq limit(long limit) {
		return () -> new LimitingCharIterator(iterator(), limit);
	}

	/**
	 * Append the given {@code chars} to the end of this {@code CharSeq}.
	 */
	default CharSeq append(char... characters) {
		return append(CharIterable.of(characters));
	}

	/**
	 * Append the {@code chars} in the given {@link CharIterable} to the end of this {@code CharSeq}.
	 */
	default CharSeq append(CharIterable that) {
		return new ChainingCharIterable(this, that)::iterator;
	}

	/**
	 * Append the {@link Character}s in the given {@link Iterable} to the end of this {@code CharSeq}.
	 */
	default CharSeq append(Iterable<Character> iterable) {
		return append(CharIterable.from(iterable));
	}

	/**
	 * Append the {@code chars} in the given {@link CharIterator} to the end of this {@code CharSeq}.
	 * <p>
	 * The appended {@code chars} will only be available on the first traversal of the resulting {@code CharSeq}.
	 */
	default CharSeq append(CharIterator iterator) {
		return append(CharIterable.from(iterator));
	}

	/**
	 * Append the {@code chars} in the given {@link PrimitiveIterator.OfInt} to the end of this {@code CharSeq}.
	 * <p>
	 * The appended {@code chars} will only be available on the first traversal of the resulting {@code CharSeq}.
	 */
	default CharSeq append(PrimitiveIterator.OfInt iterator) {
		return append(CharIterator.from(iterator));
	}

	/**
	 * Append the {@link Character}s in the given {@link Iterator} to the end of this {@code CharSeq}.
	 * <p>
	 * The appended {@link Character}s will only be available on the first traversal of the resulting {@code CharSeq}.
	 */
	default CharSeq append(Iterator<Character> iterator) {
		return append(CharIterator.from(iterator));
	}

	/**
	 * Append the {@link Character}s in the given {@link Stream} to the end of this {@code CharSeq}.
	 * <p>
	 * The appended {@link Character}s will only be available on the first traversal of the resulting {@code CharSeq}.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 */
	default CharSeq append(Stream<Character> stream) {
		return append(CharIterable.from(stream));
	}

	/**
	 * Append the {@code char} values of the {@code ints} in the given {@link IntStream} to the end of this
	 * {@code CharSeq}.
	 * <p>
	 * The appended {@code chars} will only be available on the first traversal of the resulting {@code CharSeq}.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 */
	default CharSeq append(IntStream stream) {
		return append(CharIterable.from(stream));
	}

	/**
	 * Filter the elements in this {@code CharSeq}, keeping only the elements that match the given
	 * {@link CharPredicate}.
	 */
	default CharSeq filter(CharPredicate predicate) {
		return () -> new FilteringCharIterator(iterator(), predicate);
	}

	/**
	 * Filter this {@code CharSeq} to another sequence of chars while peeking at the previous value in the
	 * sequence.
	 * <p>
	 * The predicate has access to the previous char and the current char in the iteration. If the current char is
	 * the first value in the sequence, and there is no previous value, the provided replacement value is used as
	 * the first previous value.
	 */
	default CharSeq filterBack(char firstPrevious, CharBiPredicate predicate) {
		return () -> new BackPeekingFilteringCharIterator(iterator(), firstPrevious, predicate);
	}

	/**
	 * Filter this {@code CharSeq} to another sequence of chars while peeking at the next char in the sequence.
	 * <p>
	 * The predicate has access to the current char and the next char in the iteration. If the current char is
	 * the last value in the sequence, and there is no next value, the provided replacement value is used as
	 * the last next value.
	 */
	default CharSeq filterForward(char lastNext, CharBiPredicate predicate) {
		return () -> new ForwardPeekingFilteringCharIterator(iterator(), lastNext, predicate);
	}

	/**
	 * Collect this {@code CharSeq} into an arbitrary container using the given constructor and adder.
	 */
	default <C> C collect(Supplier<? extends C> constructor, ObjCharConsumer<? super C> adder) {
		return collectInto(constructor.get(), adder);
	}

	/**
	 * Collect this {@code CharSeq} into the given container using the given adder.
	 */
	default <C> C collectInto(C result, ObjCharConsumer<? super C> adder) {
		forEachChar(c -> adder.accept(result, c));
		return result;
	}

	/**
	 * Join this {@code CharSeq} into a string separated by the given delimiter.
	 */
	default String join(String delimiter) {
		return join("", delimiter, "");
	}

	/**
	 * Join this {@code CharSeq} into a string separated by the given delimiter, with the given prefix and suffix.
	 */
	default String join(String prefix, String delimiter, String suffix) {
		StringBuilder result = new StringBuilder(prefix);

		boolean started = false;
		for (CharIterator iterator = iterator(); iterator.hasNext(); started = true) {
			char each = iterator.nextChar();
			if (started)
				result.append(delimiter);
			result.append(each);
		}

		return result.append(suffix).toString();
	}

	/**
	 * Reduce this {@code CharSeq} into a single {@code char} by iteratively applying the given binary operator to
	 * the current result and each {@code char} in the sequence.
	 */
	default OptionalChar reduce(CharBinaryOperator operator) {
		CharIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalChar.empty();

		Character identity = iterator.nextChar();
		char result = iterator.reduce(identity, operator);
		return OptionalChar.of(result);
	}

	/**
	 * Reduce this {@code CharSeq} into a single {@code char} by iteratively applying the given binary operator to
	 * the current result and each {@code char} in the sequence, starting with the given identity as the initial
	 * result.
	 */
	default char reduce(char identity, CharBinaryOperator operator) {
		return iterator().reduce(identity, operator);
	}

	/**
	 * @return the first character of this {@code CharSeq} or an empty {@link OptionalChar} if there are no characters
	 * in the {@code CharSeq}.
	 */
	default OptionalChar first() {
		CharIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalChar.empty();

		return OptionalChar.of(iterator.nextChar());
	}

	/**
	 * @return the second character of this {@code CharSeq} or an empty {@link OptionalChar} if there are less than two
	 * characters in the {@code CharSeq}.
	 */
	default OptionalChar second() {
		CharIterator iterator = iterator();

		iterator.skip();
		if (!iterator.hasNext())
			return OptionalChar.empty();

		return OptionalChar.of(iterator.nextChar());
	}

	/**
	 * @return the third character of this {@code CharSeq} or an empty {@link OptionalChar} if there are less than
	 * three characters in the {@code CharSeq}.
	 */
	default OptionalChar third() {
		CharIterator iterator = iterator();

		iterator.skip();
		iterator.skip();
		if (!iterator.hasNext())
			return OptionalChar.empty();

		return OptionalChar.of(iterator.nextChar());
	}

	/**
	 * @return the last character of this {@code CharSeq} or an empty {@link OptionalChar} if there are no
	 * characters in the {@code CharSeq}.
	 */
	default OptionalChar last() {
		CharIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalChar.empty();

		char last;
		do {
			last = iterator.nextChar();
		} while (iterator.hasNext());

		return OptionalChar.of(last);
	}

	/**
	 * Skip x number of steps in between each invocation of the iterator of this {@code CharSeq}.
	 */
	default CharSeq step(long step) {
		return () -> new SteppingCharIterator(iterator(), step);
	}

	/**
	 * @return a {@code CharSeq} where each item occurs only once, the first time it is encountered.
	 */
	default CharSeq distinct() {
		return () -> new DistinctCharIterator(iterator());
	}

	/**
	 * @return the smallest character in this {@code CharSeq} according to their integer value.
	 */
	default OptionalChar min() {
		return reduce((a, b) -> (a < b) ? a : b);
	}

	/**
	 * @return the greatest character in this {@code CharSeq} according to their integer value.
	 */
	default OptionalChar max() {
		return reduce((a, b) -> (a > b) ? a : b);
	}

	/**
	 * @return the number of characters in this {@code CharSeq}.
	 */
	default long count() {
		long count = 0;
		for (CharIterator iterator = iterator(); iterator.hasNext(); iterator.nextChar()) {
			count++;
		}
		return count;
	}

	/**
	 * @return true if all characters in this {@code CharSeq} satisfy the given predicate, false otherwise.
	 */
	default boolean all(CharPredicate predicate) {
		for (CharIterator iterator = iterator(); iterator.hasNext(); ) {
			if (!predicate.test(iterator.nextChar()))
				return false;
		}
		return true;
	}

	/**
	 * @return true if no characters in this {@code CharSeq} satisfy the given predicate, false otherwise.
	 */
	default boolean none(CharPredicate predicate) {
		return !any(predicate);
	}

	/**
	 * @return true if any character in this {@code CharSeq} satisfy the given predicate, false otherwise.
	 */
	default boolean any(CharPredicate predicate) {
		for (CharIterator iterator = iterator(); iterator.hasNext(); ) {
			if (predicate.test(iterator.nextChar()))
				return true;
		}
		return false;
	}

	/**
	 * Allow the given {@link CharConsumer} to see each element in this {@code CharSeq} as it is traversed.
	 */
	default CharSeq peek(CharConsumer action) {
		return () -> new UnaryCharIterator(iterator()) {
			@Override
			public char nextChar() {
				char next = iterator.nextChar();
				action.accept(next);
				return next;
			}
		};
	}

	/**
	 * @return this {@code CharSeq} sorted according to the natural order of the characters' integer values.
	 *
	 * @see #reverse()
	 */
	default CharSeq sorted() {
		return () -> {
			char[] array = toArray();
			Arrays.sort(array);
			return CharIterator.of(array);
		};
	}

	/**
	 * Collect the characters in this {@code CharSeq} into an array.
	 */
	default char[] toArray() {
		char[] work = new char[10];

		int index = 0;
		CharIterator iterator = iterator();
		while (iterator.hasNext()) {
			if (work.length < (index + 1)) {
				int newCapacity = work.length + (work.length >> 1);
				char[] newChars = new char[newCapacity];
				System.arraycopy(work, 0, newChars, 0, work.length);
				work = newChars;
			}
			work[index++] = iterator.nextChar();
		}

		if (work.length == index) {
			return work; // Not very likely, but still
		}

		char[] result = new char[index];
		System.arraycopy(work, 0, result, 0, index);
		return result;
	}

	/**
	 * Prefix the characters in this {@code CharSeq} with the given characters.
	 */
	default CharSeq prefix(char... cs) {
		return () -> new ChainingCharIterator(CharIterable.of(cs), this);
	}

	/**
	 * Suffix the characters in this {@code CharSeq} with the given characters.
	 */
	default CharSeq suffix(char... cs) {
		return () -> new ChainingCharIterator(this, CharIterable.of(cs));
	}

	/**
	 * Interleave the elements in this {@code CharSeq} with those of the given {@code CharSeq}, stopping when either
	 * sequence finishes.
	 */
	default CharSeq interleave(CharSeq that) {
		return () -> new InterleavingCharIterator(this, that);
	}

	/**
	 * @return a {@code CharSeq} which iterates over this {@code CharSeq} in reverse order.
	 *
	 * @see #sorted()
	 */
	default CharSeq reverse() {
		return () -> CharIterator.of(Arrayz.reverse(toArray()));
	}

	/**
	 * @return this {@code CharSeq} concatenated as a string.
	 *
	 * @see #join(String)
	 * @see #join(String, String, String)
	 * @see #collect(Supplier, ObjCharConsumer)
	 */
	default String asString() {
		return new String(toArray());
	}

	/**
	 * Map this {@code CharSeq} to another sequence of characters while peeking at the previous character in the
	 * sequence.
	 * <p>
	 * The mapper has access to the previous char and the current char in the iteration. If the current char is
	 * the first value in the sequence, and there is no previous value, the provided replacement value is used as
	 * the first previous value.
	 */
	default CharSeq mapBack(char firstPrevious, CharBinaryOperator mapper) {
		return () -> new BackPeekingMappingCharIterator(iterator(), firstPrevious, mapper);
	}

	/**
	 * Map this {@code CharSeq} to another sequence of characters while peeking at the next character in the sequence.
	 * <p>
	 * The mapper has access to the current char and the next char in the iteration. If the current char is
	 * the last value in the sequence, and there is no next value, the provided replacement value is used as
	 * the last next value.
	 */
	default CharSeq mapForward(char lastNext, CharBinaryOperator mapper) {
		return () -> new ForwardPeekingMappingCharIterator(iterator(), lastNext, mapper);
	}

	/**
	 * Convert this sequence of characters to a sequence of ints corresponding to the integer value of each character.
	 */
	default IntSequence toInts() {
		return () -> IntIterator.from(iterator());
	}

	/**
	 * Convert this sequence of characters to a sequence of ints corresponding to the integer value of each character.
	 */
	default IntSequence toInts(CharToIntFunction mapper) {
		return () -> IntIterator.from(iterator(), mapper);
	}

	/**
	 * Repeat this sequence of characters forever, looping back to the beginning when the iterator runs out of chars.
	 * <p>
	 * The resulting sequence will never terminate if this sequence is non-empty.
	 */
	default CharSeq repeat() {
		return () -> new RepeatingCharIterator(this, -1);
	}

	/**
	 * Repeat this sequence of characters x times, looping back to the beginning when the iterator runs out of chars.
	 */
	default CharSeq repeat(long times) {
		return () -> new RepeatingCharIterator(this, times);
	}

	/**
	 * Window the elements of this {@code CharSeq} into a sequence of {@code CharSeq}s of elements, each with the size
	 * of the given window. The first item in each list is the second item in the previous list. The final list may
	 * be shorter than the window. This is equivalent to {@code window(window, 1)}.
	 */
	default Sequence<CharSeq> window(int window) {
		return window(window, 1);
	}

	/**
	 * Window the elements of this {@link Sequence} into a sequence of {@code CharSeq}s of elements, each with the size
	 * of the given window, stepping {@code step} elements between each window. If the given step is less than the
	 * window size, the windows will overlap each other.
	 */
	default Sequence<CharSeq> window(int window, int step) {
		return () -> new WindowingCharIterator(iterator(), window, step);
	}

	/**
	 * Batch the elements of this {@link Sequence} into a sequence of {@code CharSeq}s of distinct elements, each with
	 * the given batch size. This is equivalent to {@code window(size, size)}.
	 */
	default Sequence<CharSeq> batch(int size) {
		return window(size, size);
	}

	/**
	 * Batch the elements of this {@link Sequence} into a sequence of {@code CharSeq}s of distinct elements, where the
	 * given predicate determines where to split the lists of partitioned elements. The predicate is given the current
	 * and next item in the iteration, and if it returns true a partition is created between the elements.
	 */
	default Sequence<CharSeq> batch(CharBiPredicate predicate) {
		return () -> new PredicatePartitioningCharIterator<>(iterator(), predicate);
	}

	/**
	 * Split the {@code chars} of this {@code CharSeq} charo a sequence of {@code CharSeq}s of distinct elements, where
	 * the given predicate determines which {@code chars} to split the partitioned elements around. The {@code chars}
	 * matching the predicate are not included in the result.
	 */
	default Sequence<CharSeq> split(CharPredicate predicate) {
		return () -> new SplittingCharIterator(iterator(), predicate);
	}

	/**
	 * Remove all elements matched by this sequence using {@link Iterator#remove()}.
	 */
	default void removeAll() {
		Iterables.removeAll(this);
	}

	/**
	 * @return true if this {@code CharSeq} is empty, false otherwise.
	 */
	default boolean isEmpty() {
		return !iterator().hasNext();
	}
}
