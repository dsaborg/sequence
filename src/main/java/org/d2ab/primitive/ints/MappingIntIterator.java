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

package org.d2ab.primitive.ints;

import java.util.function.IntUnaryOperator;

/**
 * Created by danie_000 on 2016-01-03.
 */
public class MappingIntIterator implements IntIterator {
	private final IntIterator iterator;
	private final IntUnaryOperator mapper;

	public MappingIntIterator(IntIterator iterator, IntUnaryOperator mapper) {
		this.iterator = iterator;
		this.mapper = mapper;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public int nextInt() {
		return mapper.applyAsInt(iterator.nextInt());
	}
}
