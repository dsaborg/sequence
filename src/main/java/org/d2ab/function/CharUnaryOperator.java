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

package org.d2ab.function;

/**
 */
@FunctionalInterface
public interface CharUnaryOperator {
	/**
	 * Apply this operator to the given {@code char}.
	 */
	char applyAsChar(char c);

	/**
	 * @return a {@code CharUnaryOperator} that always returns the same result.
	 */
	static CharUnaryOperator identity() {
		return c -> c;
	}

	/**
	 * Compose this operator with another operator, applying this operator to the result of the given operator.
	 */
	default CharUnaryOperator compose(CharUnaryOperator operator) {
		return c -> applyAsChar(operator.applyAsChar(c));
	}

	/**
	 * Compose this operator with another operator, applying the given operator to the result of this operator.
	 */
	default CharUnaryOperator andThen(CharUnaryOperator operator) {
		return c -> operator.applyAsChar(applyAsChar(c));
	}
}
