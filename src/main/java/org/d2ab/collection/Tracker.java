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

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks presence of items, for example used to calculate distinct entries. Uses a mix between a {@link Set} and a
 * {@link BitSet} for remembering values. Uses a {@link BitSet} up to a threshold value, then uses a regular
 * {@link Set}.
 */
public class Tracker {
	private final int threshold;
	private final Set<Long> high = new HashSet<>();
	private final BitSet low;

	public Tracker(int threshold) {
		this.threshold = threshold;
		low = new BitSet(threshold);
	}

	public boolean add(long l) {
		if (l < threshold)
			return BitSets.add(low, (int) l);
		else
			return high.add(l);
	}
}
