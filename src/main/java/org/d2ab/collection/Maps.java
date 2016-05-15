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

package org.d2ab.collection;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static java.util.Comparator.comparing;
import static org.d2ab.collection.Comparators.naturalOrderNullsFirst;

/**
 * Utility methods for {@link Map} instances
 */
public class Maps {
	@SuppressWarnings("unchecked")
	private static final Comparator<Entry> COMPARATOR =
			comparing((Function<Entry, Object>) Entry::getKey, naturalOrderNullsFirst()).thenComparing(
					(Function<Entry, Object>) Entry::getValue, naturalOrderNullsFirst());

	public static <K, V> Builder<K, V> builder(IntFunction<Map<K, V>> constructor, int initialCapacity) {
		return new Builder<>(() -> constructor.apply(initialCapacity));
	}

	public static <K, V> Builder<K, V> builder(K key, V value) {
		return Maps.<K, V>builder().put(key, value);
	}

	public static <K, V> Builder<K, V> builder() {
		return builder(HashMap::new);
	}

	public static <K, V> Builder<K, V> builder(Supplier<Map<K, V>> constructor) {
		return new Builder<>(constructor);
	}

	/**
	 * Creates a new {@link Entry} with the given key and value. Calling {@link Entry#setValue(Object)} on the
	 * entry will result in an {@link UnsupportedOperationException} being thrown.
	 */
	public static <K, V> Entry<K, V> entry(K key, V value) {
		return new EntryImpl<>(key, value);
	}

	public static <K, V> Map<K, V> put(Map<K, V> result, Entry<K, V> entry) {
		result.put(entry.getKey(), entry.getValue());
		return result;
	}

	public static <T> Iterator<T> iterator(Entry<? extends T, ? extends T> entry) {
		return new EntryIterator<>(entry);
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
			Map<K, V> result = map == null ? constructor.get() : map;
			map = null;
			return result;
		}
	}

	private static class EntryImpl<K, V> implements Entry<K, V>, Comparable<Entry<K, V>>, Serializable {
		private final K key;
		private final V value;

		private EntryImpl(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public int hashCode() {
			int result = (key != null) ? key.hashCode() : 0;
			result = (31 * result) + ((value != null) ? value.hashCode() : 0);
			return result;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof Entry))
				return false;

			Entry that = (Entry) o;
			return Objects.equals(key, that.getKey()) && Objects.equals(value, that.getValue());
		}

		@Override
		public String toString() {
			return "<" + key + ", " + value + '>';
		}

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

		@Override
		public int compareTo(Entry<K, V> that) {
			return COMPARATOR.compare(this, that);
		}
	}

	static class EntryIterator<T> implements Iterator<T> {
		private final Entry<? extends T, ? extends T> entry;
		int index;

		public EntryIterator(Entry<? extends T, ? extends T> entry) {
			this.entry = entry;
		}

		@Override
		public boolean hasNext() {
			return index < 2;
		}

		@Override
		public T next() {
			if (!hasNext())
				throw new NoSuchElementException();

			switch (++index) {
				case 1:
					return entry.getKey();
				case 2:
					return entry.getValue();
				default:
					// Can't happen due to above check
					throw new IllegalStateException();
			}
		}
	}
}
