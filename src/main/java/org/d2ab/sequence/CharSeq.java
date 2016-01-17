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

import org.d2ab.function.chars.*;
import org.d2ab.iterable.chars.ChainingCharIterable;
import org.d2ab.iterable.chars.CharIterable;
import org.d2ab.iterator.chars.*;
import org.d2ab.iterator.ints.DelegatingIntIterator;
import org.d2ab.util.Arrayz;
import org.d2ab.util.primitive.OptionalChar;

import java.util.Arrays;
import java.util.Iterator;
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
	 * Create an empty {@code CharSequence} with no characters.
	 */
	static CharSeq empty() {
		return from(emptyIterator());
	}

	/**
	 * Create a {@code CharSequence} from an {@link Iterator} of {@code Character} values. Note that {@code
	 * CharSequence}s created from {@link Iterator}s cannot be passed over more than once. Further attempts will
	 * register the {@code CharSequence} as empty.
	 */
	static CharSeq from(Iterator<Character> iterator) {
		return from(CharIterator.from(iterator));
	}

	/**
	 * Create a {@code CharSequence} from a {@link CharIterator} of character values. Note that {@code
	 * CharSequence}s created from {@link CharIterator}s cannot be passed over more than once. Further attempts
	 * will
	 * register the {@code CharSequence} as empty.
	 */
	static CharSeq from(CharIterator iterator) {
		return () -> iterator;
	}

	/**
	 * Create a {@code CharSequence} from a {@link CharIterable}.
	 */
	static CharSeq from(CharIterable iterable) {
		return iterable::iterator;
	}

	static CharSeq from(CharSequence csq) {
		return () -> new CharSequenceCharIterator(csq);
	}

	/**
	 * Create a {@code CharSequence} with the given characters.
	 */
	static CharSeq of(char... cs) {
		return () -> new ArrayCharIterator(cs);
	}

	/**
	 * Create a {@code Sequence} from {@link Iterator}s of items supplied by the given {@link Supplier}. Every time
	 * the {@code Sequence} is to be iterated over, the {@link Supplier} is used to create the initial stream of
	 * elements. This is similar to creating a {@code Sequence} from an {@link Iterable}.
	 */
	static CharSeq from(Supplier<? extends CharIterator> iteratorSupplier) {
		return iteratorSupplier::get;
	}

	/**
	 * Create a {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created from {@link
	 * Stream}s cannot be passed over more than once. Further attempts will cause an {@link IllegalStateException}
	 * when the {@link Stream} is requested again.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 */
	static CharSeq from(Stream<Character> stream) {
		return from(stream::iterator);
	}

	/**
	 * Create a {@code CharSequence} from an {@link Iterable} of {@code Character} values.
	 */
	static CharSeq from(Iterable<Character> iterable) {
		return () -> CharIterator.from(iterable);
	}

	/**
	 * A {@code Sequence} of all the {@link Character} values starting at {@link Character#MIN_VALUE} and ending at
	 * {@link Character#MAX_VALUE}.
	 *
	 * @see #startingAt(char)
	 * @see #range(char, char)
	 * @see #until(char)
	 * @see #endingAt(char)
	 */
	static CharSeq all() {
		return startingAt((char) 0);
	}

	/**
	 * A {@code Sequence} of all the {@link Character} values starting at the given value and ending at {@link
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
	 * A {@code Sequence} of all the {@link Character} values between the given start and end positions, inclusive.
	 *
	 * @see #all()
	 * @see #startingAt(char)
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
		return () -> new ExclusiveTerminalCharIterator(terminal).backedBy(iterator());
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
		return () -> new InclusiveTerminalCharIterator(terminal).backedBy(iterator());
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
		return () -> new ExclusiveTerminalCharIterator(terminal).backedBy(iterator());
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
		return () -> new InclusiveTerminalCharIterator(terminal).backedBy(iterator());
	}

	default CharSeq map(CharUnaryOperator mapper) {
		return () -> new UnaryCharIterator() {
			@Override
			public char nextChar() {
				return mapper.applyAsChar(iterator.nextChar());
			}
		}.backedBy(iterator());
	}

	default Sequence<Character> box() {
		return toSequence(Character::valueOf);
	}

	default <T> Sequence<T> toSequence(CharFunction<T> mapper) {
		return () -> new Iterator<T>() {
			private final CharIterator iterator = iterator();

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				return mapper.apply(iterator.nextChar());
			}
		};
	}

	default CharSeq skip(long skip) {
		return () -> new SkippingCharIterator(skip).backedBy(iterator());
	}

	default CharSeq limit(long limit) {
		return () -> new LimitingCharIterator(limit).backedBy(iterator());
	}

	default CharSeq append(Iterable<Character> iterable) {
		return append(CharIterable.from(iterable));
	}

	default CharSeq append(CharIterable that) {
		return new ChainingCharIterable(this, that)::iterator;
	}

	default CharSeq append(CharIterator iterator) {
		return append(iterator.asIterable());
	}

	default CharSeq append(Iterator<Character> iterator) {
		return append(CharIterable.from(iterator));
	}

	default CharSeq append(char... characters) {
		return append(CharIterable.of(characters));
	}

	default CharSeq append(Stream<Character> stream) {
		return append(CharIterable.from(stream));
	}

	default CharSeq append(IntStream stream) {
		return append(CharIterable.from(stream));
	}

	default CharSeq filter(CharPredicate predicate) {
		return () -> new FilteringCharIterator(predicate).backedBy(iterator());
	}

	default <C> C collect(Supplier<? extends C> constructor, ObjCharConsumer<? super C> adder) {
		C result = constructor.get();
		forEachChar(c -> adder.accept(result, c));
		return result;
	}

	default String join(String delimiter) {
		return join("", delimiter, "");
	}

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

	default char reduce(char identity, CharBinaryOperator operator) {
		return reduce(identity, operator, iterator());
	}

	default char reduce(char identity, CharBinaryOperator operator, CharIterator iterator) {
		char result = identity;
		while (iterator.hasNext())
			result = operator.applyAsChar(result, iterator.nextChar());
		return result;
	}

	default OptionalChar first() {
		CharIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalChar.empty();

		return OptionalChar.of(iterator.nextChar());
	}

	default OptionalChar second() {
		CharIterator iterator = iterator();

		iterator.skip();
		if (!iterator.hasNext())
			return OptionalChar.empty();

		return OptionalChar.of(iterator.nextChar());
	}

	default OptionalChar third() {
		CharIterator iterator = iterator();

		iterator.skip();
		iterator.skip();
		if (!iterator.hasNext())
			return OptionalChar.empty();

		return OptionalChar.of(iterator.nextChar());
	}

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

	default CharSeq step(long step) {
		return () -> new SteppingCharIterator(step).backedBy(iterator());
	}

	default CharSeq distinct() {
		return () -> new DistinctCharIterator().backedBy(iterator());
	}

	default OptionalChar min() {
		return reduce((a, b) -> (a < b) ? a : b);
	}

	default OptionalChar reduce(CharBinaryOperator operator) {
		CharIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalChar.empty();

		char result = reduce(iterator.next(), operator, iterator);
		return OptionalChar.of(result);
	}

	default OptionalChar max() {
		return reduce((a, b) -> (a > b) ? a : b);
	}

	default long count() {
		long count = 0;
		for (CharIterator iterator = iterator(); iterator.hasNext(); iterator.nextChar()) {
			count++;
		}
		return count;
	}

	default boolean all(CharPredicate predicate) {
		for (CharIterator iterator = iterator(); iterator.hasNext(); ) {
			if (!predicate.test(iterator.nextChar()))
				return false;
		}
		return true;
	}

	default boolean none(CharPredicate predicate) {
		return !any(predicate);
	}

	default boolean any(CharPredicate predicate) {
		for (CharIterator iterator = iterator(); iterator.hasNext(); ) {
			if (predicate.test(iterator.nextChar()))
				return true;
		}
		return false;
	}

	default CharSeq peek(CharConsumer action) {
		return () -> new UnaryCharIterator() {
			@Override
			public char nextChar() {
				char next = iterator.nextChar();
				action.accept(next);
				return next;
			}
		}.backedBy(iterator());
	}

	default CharSeq sorted() {
		char[] array = toArray();
		Arrays.sort(array);
		return () -> CharIterator.of(array);
	}

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

	default CharSeq prefix(char... cs) {
		return () -> new ChainingCharIterator(CharIterable.of(cs), this);
	}

	default CharSeq suffix(char... cs) {
		return () -> new ChainingCharIterator(this, CharIterable.of(cs));
	}

	default CharSeq interleave(CharSeq that) {
		return () -> new InterleavingCharIterator(this, that);
	}

	default CharSeq reverse() {
		char[] array = toArray();
		for (int i = 0; i < (array.length / 2); i++) {
			Arrayz.swap(array, i, array.length - 1 - i);
		}
		return CharIterable.of(array)::iterator;
	}

	default String asString() {
		return new String(toArray());
	}

	default CharSeq mapBack(IntCharToCharBinaryFunction mapper) {
		return () -> new BackPeekingMappingCharIterator(mapper).backedBy(iterator());
	}

	default CharSeq mapForward(CharIntToCharBinaryFunction mapper) {
		return () -> new ForwardPeekingMappingCharIterator(mapper).backedBy(iterator());
	}

	default IntSeq toInts() {
		return () -> new DelegatingIntIterator<Character, CharIterator>() {
			@Override
			public int nextInt() {
				return iterator.nextChar();
			}
		}.backedBy(iterator());
	}

	default CharSeq repeat() {
		return () -> new RepeatingCharIterator(this, -1);
	}

	default CharSeq repeat(long times) {
		return () -> new RepeatingCharIterator(this, times);
	}
}
