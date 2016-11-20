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

package org.d2ab.collection.doubles;

import java.util.Comparator;

/**
 * A primitive specialization of {@link Comparator} for {@code double} values.
 */
@FunctionalInterface
public interface DoubleComparator extends Comparator<Double> {
	@Override
	default int compare(Double d1, Double d2) {
		return compareDoubles(d1, d2);
	}

	int compareDoubles(double d1, double d2);

	static DoubleComparator withPrecision(double precision) {
		return (d1, d2) -> {
			if (equals(d1, d2, precision))
				return 0;

			return Double.compare(d1, d2);
		};
	}

	static boolean equals(double d1, double d2, double precision) {
		return Math.abs(d1 - d2) <= precision;
	}
}
