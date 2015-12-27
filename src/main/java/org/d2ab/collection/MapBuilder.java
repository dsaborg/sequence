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

import java.util.HashMap;
import java.util.Map;

public class MapBuilder<K, V> {
	private Map<K, V> map = new HashMap<>();

	private MapBuilder() {
	}

	public static <K, V> MapBuilder<K, V> of(K key, V value) {
		return new MapBuilder<K, V>().put(key, value);
	}

	protected MapBuilder<K, V> put(K key, V value) {
		map.put(key, value);
		return this;
	}

	public MapBuilder<K, V> and(K key, V value) {
		return put(key, value);
	}

	public Map<K, V> build() {
		Map<K, V> result = map;
		map = new HashMap<>(result);
		return result;
	}
}
