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
import java.util.function.Function;

/**
 * A {@link Collection} that presents a mapped view of another {@link Collection}.
 */
public class BiMappedCollection<T, U> extends MappedCollection<T, U> implements SizedIterable<U> {
	private final Function<? super U, ? extends T> backMapper;

	public static <T, U> Collection<U> from(Collection<T> collection, Function<? super T, ? extends U> mapper,
	                                        Function<? super U, ? extends T> backMapper) {
		return new BiMappedCollection<>(collection, mapper, backMapper);
	}

	private BiMappedCollection(Collection<T> collection, Function<? super T, ? extends U> mapper,
	                           Function<? super U, ? extends T> backMapper) {
		super(collection, mapper);
		this.backMapper = backMapper;
	}

	@Override
	public boolean add(U u) {
		return collection.add(backMapper.apply(u));
	}
}
