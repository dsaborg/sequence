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

package org.d2ab.sequence;

import org.d2ab.function.QuaternaryFunction;
import org.d2ab.util.Pair;

import java.util.Map;
import java.util.function.*;

/**
 * Helper methods for {@link Sequence} instances.
 */
public class SequenceFunctions {
	private SequenceFunctions() {
	}

	/**
	 * @return the given doubly bi-valued function converted to a pair-based binary operator.
	 */
	public static <L, R> BinaryOperator<Pair<L, R>> asPairBinaryOperator(QuaternaryFunction<L, R, L, R, Pair<L, R>>
			                                                                     f) {
		return (p1, p2) -> f.apply(p1.getLeft(), p1.getRight(), p2.getLeft(), p2.getRight());
	}

	/**
	 * @return the given bi-valued function converted to a pair-based function.
	 */
	public static <L, R, T> Function<Pair<L, R>, T> asPairFunction(BiFunction<? super L, ? super R, ? extends T> f) {
		return p -> f.apply(p.getLeft(), p.getRight());
	}

	/**
	 * @return the given bi-valued predicate converted to a pair-based predicate.
	 */
	public static <L, R> Predicate<Pair<L, R>> asPairPredicate(BiPredicate<? super L, ? super R> predicate) {
		return p -> predicate.test(p.getLeft(), p.getRight());
	}

	/**
	 * @return the given bi-valued consumer converted to a pair-based consumer.
	 */
	public static <L, R> Consumer<Pair<L, R>> asPairConsumer(BiConsumer<? super L, ? super R> action) {
		return p -> action.accept(p.getLeft(), p.getRight());
	}

	public static <K, V> BinaryOperator<Map.Entry<K, V>> asEntryBinaryOperator(
			QuaternaryFunction<? super K, ? super V, ? super
					K, ? super V, ? extends Map.Entry<K, V>> f) {
		return (e1, e2) -> f.apply(e1.getKey(), e1.getValue(), e2.getKey(), e2.getValue());
	}

	public static <K, V, R> Function<Map.Entry<K, V>, R> asEntryFunction(
			BiFunction<? super K, ? super V, ? extends R> mapper) {
		return entry -> mapper.apply(entry.getKey(), entry.getValue());
	}

	public static <K, V> Predicate<Map.Entry<K, V>> asEntryPredicate(BiPredicate<? super K, ? super V> predicate) {
		return entry -> predicate.test(entry.getKey(), entry.getValue());
	}

	public static <K, V> Consumer<Map.Entry<K, V>> asEntryConsumer(BiConsumer<? super K, ? super V> action) {
		return entry -> action.accept(entry.getKey(), entry.getValue());
	}
}
