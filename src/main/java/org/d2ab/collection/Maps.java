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
package org.d2ab.collection;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Utility methods for {@link Map} instances
 */
public class Maps {
	@Nonnull
	public static <K, V> Entry<K, V> entry(K key, V value) {
		return new Entry<K, V>() {
			@Override
			public K getKey() {
				return key;
			}

			@Override
			public V getValue() {
				return value;
			}

			@Override
			public V setValue(V value) {
				throw new UnsupportedOperationException();
			}
		};
	}

	public static <K, V> Builder<K, V> builder(IntFunction<Map<K, V>> constructor, int initialCapacity) {
		return new Builder<>(() -> constructor.apply(initialCapacity));
	}

	public static <K, V> Builder<K, V> put(K key, V value) {
		return Maps.<K, V>builder().put(key, value);
	}

	public static <K, V> Builder<K, V> builder() {
		return builder(HashMap::new);
	}

	public static <K, V> Builder<K, V> builder(Supplier<Map<K, V>> constructor) {
		return new Builder<>(constructor);
	}

	public static class Builder<K, V> {
		private Supplier<Map<K, V>> constructor;
		private Map<K, V> map;

		private Builder(Supplier<Map<K, V>> constructor) {
			this.constructor = constructor;
		}

		public Builder<K, V> put(K key, V value) {
			if (map == null)
				map = constructor.get();
			map.put(key, value);
			return this;
		}

		public Map<K, V> build() {
			Map<K, V> result = map;
			map = null;
			return result;
		}
	}
}
