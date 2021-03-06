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

package org.d2ab.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * An {@link Iterator} that delimits the items of another {@link Iterator} with a delimiter object.
 */
public class DelimitingIterator<U, V> extends DelegatingUnaryIterator<U> {
	private final Optional<? extends V> prefix;
	private final Optional<? extends V> delimiter;
	private final Optional<? extends V> suffix;

	private boolean delimiterNext;
	private boolean prefixDone;
	private boolean suffixDone;

	public DelimitingIterator(Iterator<U> iterator, Optional<? extends V> prefix, Optional<? extends V> delimiter,
	                          Optional<? extends V> suffix) {
		super(iterator);
		this.prefix = prefix;
		this.delimiter = delimiter;
		this.suffix = suffix;
	}

	@Override
	public boolean hasNext() {
		return !prefixDone && prefix.isPresent() ||
		       iterator.hasNext() ||
		       !suffixDone && suffix.isPresent();
	}

	@SuppressWarnings("unchecked")
	@Override
	public U next() {
		if (!prefixDone && prefix.isPresent()) {
			prefixDone = true;
			return (U) prefix.get();
		}

		if (iterator.hasNext()) {
			boolean sendDelimiter = delimiter.isPresent() && !(delimiterNext = !delimiterNext);
			return sendDelimiter ? (U) delimiter.get() : iterator.next();
		}

		if (!suffixDone && suffix.isPresent()) {
			suffixDone = true;
			return (U) suffix.get();
		}

		throw new NoSuchElementException();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
