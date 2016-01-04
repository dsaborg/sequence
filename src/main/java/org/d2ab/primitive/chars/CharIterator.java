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
package org.d2ab.primitive.chars;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;

/**
 * An Iterator specialized for {@code int} values.
 *
 * @since 1.8
 */
public interface CharIterator extends PrimitiveIterator<Character, CharConsumer> {
	static CharIterator of(char... cs) {
		return new ArrayCharIterator(cs);
	}

	static CharIterator from(Iterable<Character> iterable) {
		return from(iterable.iterator());
	}

	static CharIterator from(Iterator<Character> iterator) {
		return new CharIterator() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public char nextChar() {
				return iterator.next();
			}
		};
	}

	static CharIterator from(OfInt iterator) {
		return new CharIterator() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public char nextChar() {
				return (char) iterator.nextInt();
			}
		};
	}

	default void skipOne() {
		skip(1);
	}

	default void skip(long steps) {
		long count = 0;
		while ((count++ < steps) && hasNext()) {
			nextChar();
		}
	}

	/**
	 * Returns the next {@code char} element in the iteration.
	 *
	 * @return the next {@code char} element in the iteration
	 *
	 * @throws NoSuchElementException if the iteration has no more elements
	 */
	char nextChar();

	default CharIterable asIterable() {
		return () -> this;
	}

	/**
	 * Performs the given action for each remaining element until all elements have been processed or the action throws
	 * an exception.  Actions are performed in the order of iteration, if that order is specified. Exceptions thrown by
	 * the action are relayed to the caller.
	 *
	 * @param action The action to be performed for each element
	 *
	 * @throws NullPointerException if the specified action is null
	 * @implSpec <p>The default implementation behaves as if:
	 * <pre>{@code
	 *     while (hasNext())
	 *         action.accept(nextChar());
	 * }</pre>
	 */
	@Override
	default void forEachRemaining(CharConsumer action) {
		Objects.requireNonNull(action);
		while (hasNext())
			action.accept(nextChar());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec The default implementation boxes the result of calling {@link #nextChar()}, and returns that boxed
	 * result.
	 */
	@Override
	default Character next() {
		return nextChar();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec If the action is an instance of {@code CharConsumer} then it is cast to {@code CharConsumer} and
	 * passed
	 * to {@link #forEachRemaining}; otherwise the action is adapted to an instance of {@code CharConsumer}, by boxing
	 * the argument of {@code CharConsumer}, and then passed to {@link #forEachRemaining}.
	 */
	@Override
	default void forEachRemaining(Consumer<? super Character> action) {
		if (action instanceof CharConsumer) {
			forEachRemaining((CharConsumer) action);
		} else {
			// The method reference action::accept is never null
			Objects.requireNonNull(action);
			forEachRemaining((CharConsumer) action::accept);
		}
	}
}
