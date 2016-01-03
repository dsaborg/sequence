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

import org.d2ab.primitive.chars.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyIterator;

public class PrimitiveSequence {
	private PrimitiveSequence() {
	}

	/**
	 * An {@link Iterable} sequence of {@code char} values with {@link Stream}-like operations for refining,
	 * transforming and collating the list of characters.
	 */
	@FunctionalInterface
	public interface Chars extends CharIterable {
		/**
		 * Create an empty {@code CharSequence} with no characters.
		 */
		@Nonnull
		static Chars empty() {
			return from(emptyIterator());
		}

		/**
		 * Create a {@code CharSequence} from an {@link Iterator} of {@code Character} values. Note that {@code
		 * CharSequence}s created from {@link Iterator}s cannot be passed over more than once. Further attempts will
		 * register the {@code CharSequence} as empty.
		 */
		@Nonnull
		static Chars from(@Nonnull Iterator<Character> iterator) {
			return from(CharIterator.from(iterator));
		}

		/**
		 * Create a {@code CharSequence} from a {@link CharIterator} of character values. Note that {@code
		 * CharSequence}s created from {@link CharIterator}s cannot be passed over more than once. Further attempts
		 * will
		 * register the {@code CharSequence} as empty.
		 */
		@Nonnull
		static Chars from(@Nonnull CharIterator iterator) {
			return () -> iterator;
		}

		/**
		 * Create a {@code CharSequence} from a {@link CharIterable}.
		 */
		@Nonnull
		static Chars from(@Nonnull CharIterable iterable) {
			return iterable::iterator;
		}

		static Chars from(CharSequence csq) {
			return () -> new CharSequenceCharIterator(csq);
		}

		/**
		 * Create a {@code CharSequence} with the given characters.
		 */
		@Nonnull
		static Chars of(@Nonnull char... cs) {
			return () -> new ArrayCharIterator(cs);
		}

		/**
		 * Create a {@code Sequence} from {@link Iterator}s of items supplied by the given {@link Supplier}. Every time
		 * the {@code Sequence} is to be iterated over, the {@link Supplier} is used to create the initial stream of
		 * elements. This is similar to creating a {@code Sequence} from an {@link Iterable}.
		 */
		@Nonnull
		static Chars from(@Nonnull Supplier<? extends CharIterator> iteratorSupplier) {
			return iteratorSupplier::get;
		}

		/**
		 * Create a {@code Sequence} from a {@link Stream} of items. Note that {@code Sequences} created from {@link
		 * Stream}s cannot be passed over more than once. Further attempts will cause an {@link IllegalStateException}
		 * when the {@link Stream} is requested again.
		 *
		 * @throws IllegalStateException if the {@link Stream} is exhausted.
		 */
		static Chars from(Stream<Character> stream) {
			return from(stream::iterator);
		}

		/**
		 * Create a {@code CharSequence} from an {@link Iterable} of {@code Character} values.
		 */
		@Nonnull
		static Chars from(@Nonnull Iterable<Character> iterable) {
			return () -> CharIterator.from(iterable);
		}

		/**
		 * A {@code Sequence} of all the {@link Character} values starting at {@link Character#MIN_VALUE} and ending at
		 * {@link Character#MAX_VALUE}.
		 */
		static Chars all() {
			return range((char) 0, Character.MAX_VALUE);
		}

		/**
		 * A {@code Sequence} of all the {@link Character} values between the given start and end positions, inclusive.
		 */
		static Chars range(char start, char end) {
			CharUnaryOperator next = (end > start) ? c -> (char) (c + 1) : c -> (char) (c - 1);
			return recurse(start, next).endingAt(end);
		}

		default Chars endingAt(char terminal) {
			return () -> new InclusiveTerminalCharIterator(iterator(), terminal);
		}

		static Chars recurse(char seed, CharUnaryOperator op) {
			return () -> new RecursiveCharIterator(seed, op);
		}

		/**
		 * A {@code Sequence} of all the {@link Character} values starting at the given value and ending at {@link
		 * Character#MAX_VALUE}.
		 */
		static Chars startingAt(char start) {
			return range(start, Character.MAX_VALUE);
		}

		@Nonnull
		default Chars map(@Nonnull CharUnaryOperator mapper) {
			return () -> new MappingCharIterator(iterator(), mapper);
		}

		@Nonnull
		default Chars skip(long skip) {
			return () -> new SkippingCharIterator(iterator(), skip);
		}

		@Nonnull
		default Chars limit(long limit) {
			return () -> new LimitingCharIterator(iterator(), limit);
		}

		@Nonnull
		default Chars append(@Nonnull Iterable<Character> iterable) {
			return append(CharIterable.from(iterable));
		}

		@Nonnull
		default Chars append(@Nonnull CharIterable that) {
			return new ChainingChars(this, that);
		}

		default Chars append(CharIterator iterator) {
			return append(iterator.asIterable());
		}

		default Chars append(Iterator<Character> iterator) {
			return append(CharIterable.from(iterator));
		}

		default Chars append(char... characters) {
			return append(CharIterable.of(characters));
		}

		default Chars append(Stream<Character> stream) {
			return append(CharIterable.from(stream));
		}

		default Chars append(IntStream stream) {
			return append(CharIterable.from(stream));
		}

		@Nonnull
		default Chars filter(@Nonnull CharPredicate predicate) {
			return () -> new FilteringCharIterator(iterator(), predicate);
		}

		default Chars until(char terminal) {
			return () -> new ExclusiveTerminalCharIterator(iterator(), terminal);
		}

		default <C> C collect(Supplier<? extends C> constructor, ObjCharConsumer<? super C> adder) {
			C result = constructor.get();
			forEachChar((char each) -> adder.accept(result, each));
			return result;
		}

		default String join(String delimiter) {
			return join("", delimiter, "");
		}

		default String join(String prefix, String delimiter, String suffix) {
			StringBuilder result = new StringBuilder();
			result.append(prefix);
			boolean first = true;
			for (CharIterator iterator = iterator(); iterator.hasNext(); ) {
				char each = iterator.nextChar();
				if (first)
					first = false;
				else
					result.append(delimiter);
				result.append(each);
			}
			result.append(suffix);
			return result.toString();
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

			iterator.skipOne();
			if (!iterator.hasNext())
				return OptionalChar.empty();

			return OptionalChar.of(iterator.nextChar());
		}

		default OptionalChar third() {
			CharIterator iterator = iterator();

			iterator.skipOne();
			iterator.skipOne();
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

		default Chars step(long step) {
			return () -> new SteppingCharIterator(iterator(), step);
		}

		default Chars distinct() {
			return () -> new DistinctCharIterator(iterator());
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

		default Chars peek(CharConsumer action) {
			return () -> new PeekingCharIterator(iterator(), action);
		}

		default Chars sorted() {
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
				return work; // Not very likely
			}

			char[] result = new char[index];
			System.arraycopy(work, 0, result, 0, index);
			return result;
		}

		default Chars prefix(char... cs) {
			return () -> new ChainingCharIterator(CharIterable.of(cs), this);
		}

		default Chars suffix(char... cs) {
			return () -> new ChainingCharIterator(this, CharIterable.of(cs));
		}

		default Chars interleave(Chars that) {
			return () -> new InterleavingCharIterator(this, that);
		}

		default Chars reverse() {
			char[] array = toArray();
			for (int i = 0; i < (array.length / 2); i++) {
				swap(array, i, array.length - 1 - i);
			}
			return CharIterable.of(array)::iterator;
		}

		default void swap(char[] array, int i, int j) {
			char tempChar = array[i];
			array[i] = array[j];
			array[j] = tempChar;
		}

		default String asString() {
			return new String(toArray());
		}

		default Chars mapBack(IntCharToCharBinaryFunction mapper) {
			return () -> new BackPeekingMappingCharIterator(iterator(), mapper);
		}

		default Chars mapForward(CharIntToCharBinaryFunction mapper) {
			return () -> new ForwardPeekingMappingCharIterator(iterator(), mapper);
		}
	}

	public static class ChainingChars implements Chars {
		private final Collection<CharIterable> iterables = new ArrayList<>();

		public ChainingChars(@Nonnull CharIterable... iterables) {
			asList(iterables).forEach(e -> this.iterables.add(Objects.requireNonNull(e)));
		}

		@Override
		@Nonnull
		public ChainingChars append(@Nonnull CharIterable iterable) {
			iterables.add(iterable);
			return this;
		}

		@Override
		public CharIterator iterator() {
			return new ChainingCharIterator(iterables);
		}

		@Override
		public int hashCode() {
			return iterables.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if ((o == null) || (getClass() != o.getClass()))
				return false;

			ChainingChars that = (ChainingChars) o;

			return iterables.equals(that.iterables);
		}

		@Override
		public String toString() {
			return "ChainingChars" + iterables;
		}
	}
}
