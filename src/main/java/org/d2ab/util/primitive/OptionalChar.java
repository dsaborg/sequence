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

package org.d2ab.util.primitive;

import org.d2ab.function.chars.CharConsumer;
import org.d2ab.function.chars.CharSupplier;

import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.function.Supplier;

/**
 * A wrapper for {@code char} values that may or may not be present. Adapted from {@link OptionalInt} and the like.
 */
public final class OptionalChar {
	private static final int CACHE_THRESHOLD = 0x100;
	private static final OptionalChar[] CACHE = buildCache(CACHE_THRESHOLD);

	private static final OptionalChar EMPTY = new OptionalChar();

	private final boolean present;
	private final char value;

	private OptionalChar() {
		this.present = false;
		this.value = 0;
	}

	private OptionalChar(char c) {
		this.present = true;
		this.value = c;
	}

	private static OptionalChar[] buildCache(int threshold) {
		OptionalChar[] cache = new OptionalChar[threshold];
		for (int i = 0; i < threshold; i++) {
			cache[i] = new OptionalChar((char) i);
		}
		return cache;
	}

	/**
	 * Return an empty {@code OptionalChar}.
	 */
	public static OptionalChar empty() {
		return EMPTY;
	}

	/**
	 * Return an {@code OptionalChar} with the given {@code char} value.
	 */
	public static OptionalChar of(char c) {
		return c < CACHE_THRESHOLD ? CACHE[(int) c] : new OptionalChar(c);
	}

	/**
	 * Get the {@code char} value of this {@code OptionalChar}.
	 */
	public char getAsChar() {
		if (!present)
			throw new NoSuchElementException();

		return value;
	}

	/**
	 * @return true if this {@code OptionalChar} has a value present.
	 */
	public boolean isPresent() {
		return present;
	}

	/**
	 * Performs the given action if this {@code OptionalChar} has a value present.
	 */
	public void ifPresent(CharConsumer consumer) {
		if (present)
			consumer.accept(value);
	}

	/**
	 * @return the value of this {@code OptionalChar} if present, otherwise the given {@code char} value.
	 */
	public char orElse(char c) {
		return present ? value : c;
	}

	/**
	 * @return the value of this {@code OptionalChar} if present, otherwise a value from the given {@code char}
	 * supplier.
	 */
	public char orElseGet(CharSupplier supplier) {
		return present ? value : supplier.getAsChar();
	}

	/**
	 * @return the value of this {@code OptionalChar} if present, otherwise throws an exception from the given
	 * supplier.
	 */
	public <T extends Throwable> char orElseThrow(Supplier<T> supplier) throws T {
		if (!present)
			throw supplier.get();

		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (getClass() != o.getClass())
			return false;

		OptionalChar that = (OptionalChar) o;
		return present == that.present && value == that.value;
	}

	@Override
	public int hashCode() {
		int result = (present ? 1 : 0);
		result = 31 * result + (int) value;
		return result;
	}

	@Override
	public String toString() {
		return present ? "OptionalChar[" + value + "]" : "OptionalChar.empty";
	}
}
