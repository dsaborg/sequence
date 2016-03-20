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

import java.util.function.Supplier;

/**
 * Represents a supplier of {@code char}-valued results.  This is the {@code char}-producing primitive specialization of
 * {@link Supplier}.
 * <p>
 * <p>There is no requirement that a distinct result be returned each time the supplier is invoked.
 * <p>
 * <p>This is a functional interface whose functional method is {@link #getAsChar()}.
 *
 * @see Supplier
 * @since 1.8
 */
@FunctionalInterface
public interface CharSupplier {
	/**
	 * Gets a result.
	 *
	 * @return a result
	 */
	char getAsChar();
}
