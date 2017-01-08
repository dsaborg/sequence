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

import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

/**
 * Utility methods for {@link Collection} instances.
 */
public abstract class Collectionz {
	Collectionz() {
	}

	/**
	 * @return a {@link List} view of the given {@link Collection}, reflecting changes to the underlying {@link
	 * Collection}. If a {@link List} is given it is returned unchanged. The list does not implement {@link
	 * RandomAccess}, and is best accessed in sequence.
	 */
	public static <T> List<T> asList(Collection<T> collection) {
		if (collection instanceof List)
			return (List<T>) collection;

		return new CollectionList<>(collection);
	}
}
