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

import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;

/**
 * Specialization of the {@link BiConsumer} interface that takes a value of a given type and a char value. Adapted from
 * {@link ObjIntConsumer} and the like.
 */
@FunctionalInterface
public interface ObjCharConsumer<T> {
	/**
	 * Perform this action on the given value and the given character.
	 */
	void accept(T v, char c);
}
