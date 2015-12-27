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

package org.d2ab.test;

import static org.junit.Assert.fail;

public class Tests {
	private Tests() {}

	public static void expecting(Class<? extends Throwable> exceptionClass, Runnable action) {
		try {
			action.run();
			fail("Expected " + exceptionClass.getName());
		} catch (Throwable t) {
			if (!exceptionClass.isInstance(t)) {
				throw t;
			}
		}
	}

	public static void twice(Runnable action) {
		action.run();
		action.run();
	}
}
