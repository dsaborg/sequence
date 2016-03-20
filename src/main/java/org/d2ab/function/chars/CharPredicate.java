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

package org.d2ab.function.chars;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * A specialization of {@link Predicate} for {@code char} values. Adapter from {@link IntPredicate} and the like.
 */
@FunctionalInterface
public interface CharPredicate {
	/**
	 * Test this predicate against the given {@code char}.
	 */
	boolean test(char c);

	/**
	 * Negate this predicate, returning a predicate that always returns the opposite values.
	 */
	default CharPredicate negate() {
		return x -> !test(x);
	}

	/**
	 * Combine this predicate with another predicate using "{@code and}" boolean logic.
	 */
	default CharPredicate and(CharPredicate predicate) {
		return x -> test(x) && predicate.test(x);
	}

	/**
	 * Combine this predicate with another predicate using "{@code or}" boolean logic.
	 */
	default CharPredicate or(CharPredicate predicate) {
		return x -> test(x) || predicate.test(x);
	}
}
