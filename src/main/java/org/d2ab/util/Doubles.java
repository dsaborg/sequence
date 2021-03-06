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

package org.d2ab.util;

public abstract class Doubles {
	Doubles() {
	}

	public static boolean eq(double l, double r, double precision) {
		return Math.abs(l - r) <= precision;
	}

	public static boolean ge(double l, double r, double precision) {
		return l - r >= -precision;
	}

	public static boolean lt(double l, double r, double precision) {
		return l - r < precision;
	}
}
