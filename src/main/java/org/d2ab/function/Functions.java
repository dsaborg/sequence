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

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Helper methods related to {@link Function} and its related functional interfaces.
 */
public class Functions {
	private Functions() {
	}

	public static <T, U> UnaryOperator<U> composeAsUnaryOperator(Function<? super T, ? extends U> f,
	                                                             Function<? super U, ? extends T> g) {
		return asUnaryOperator(f.compose(g));
	}

	public static <U> UnaryOperator<U> asUnaryOperator(Function<? super U, ? extends U> function) {
		return function::apply;
	}
}
