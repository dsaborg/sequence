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

package org.d2ab.collection.ints;

import java.util.Comparator;

/**
 * A primitive specialization of {@link Comparator} for {@code int} values.
 */
@FunctionalInterface
public interface IntComparator extends Comparator<Integer> {
	@Override
	default int compare(Integer x1, Integer x2) {
		return compare((int) x1, (int) x2);
	}

	int compare(int x1, int x2);
}
