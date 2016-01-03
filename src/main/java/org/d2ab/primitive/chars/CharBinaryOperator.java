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

import java.util.function.BinaryOperator;

/**
 * Represents an operation upon two {@code char}-valued operands and producing an {@code char}-valued result.   This is
 * the primitive type specialization of {@link BinaryOperator} for {@code char}.
 * <p>
 * <p>This is a <a href="package-summary.html">functional interface</a> whose functional method is {@link
 * #applyAsChar(char, char)}.
 *
 * @see BinaryOperator
 * @see CharUnaryOperator
 * @since 1.8
 */
@FunctionalInterface
public interface CharBinaryOperator {
	/**
	 * Applies this operator to the given operands.
	 *
	 * @param left  the first operand
	 * @param right the second operand
	 *
	 * @return the operator result
	 */
	char applyAsChar(char left, char right);
}
