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

import org.d2ab.util.Classes;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

import static org.d2ab.collection.SizedIterable.SizeType.AVAILABLE;
import static org.d2ab.collection.SizedIterable.SizeType.FIXED;

/**
 * Utility methods for {@link Collection} instances.
 */
public abstract class Collectionz {
	private static final Set<String> FIXED_SIZE_COLLECTION_CLASS_NAMES = new HashSet<>(Lists.of(
			"java.util.Arrays$ArrayList",
			"java.util.Collections$EmptyList",
			"java.util.Collections$EmptySet",
			"java.util.Collections$SingletonList",
			"java.util.Collections$SingletonSet"));

	@SuppressWarnings("unchecked")
	private static final Optional<Class<? extends Collection>> UNMODIFIABLE_COLLECTION_CLASS =
			Classes.classByName("java.util.Collections$UnmodifiableCollection");

	private static final Optional<Field> UNMODIFIABLE_COLLECTION_FIELD = UNMODIFIABLE_COLLECTION_CLASS
			.flatMap(cls -> Classes.accessibleField(cls, "c"));

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

	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Collection<?> collection, T[] array) {
		int size = collection.size();
		Function<T[], T[]> allocator = a -> a.length >= size ? a : (T[]) Array.newInstance(a.getClass().getComponentType(), size);
		return toArray(collection.iterator(), array, allocator);
	}


	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Iterator<?> iterator, T[] array) {
		return toArray(iterator, array, Function.identity());
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Iterator<?> iterator, T[] array, Function<T[], T[]> allocator) {
		T[] a = allocator.apply(array);

		int cursor = 0;
		while (iterator.hasNext()) {
			if (cursor == a.length)
				a = Arrays.copyOf(a, a.length + (a.length >> 1) + 1);

			a[cursor++] = (T) iterator.next();
		}

		// size overreported and original array fits actual size?
		if (a != array && array.length >= cursor)
			System.arraycopy(a, 0, a = array, 0, cursor);

		// actual size less than expected
		if (a.length > cursor)
			if (a == array)
				a[cursor] = null;
			else
				a = Arrays.copyOf(a, cursor);

		return a;
	}

	public static SizedIterable.SizeType sizeType(Collection<?> collection) {
		return unwrap(collection).map(Collectionz::sizeType).orElseGet(
				() -> FIXED_SIZE_COLLECTION_CLASS_NAMES.contains(collection.getClass().getName()) ? FIXED : AVAILABLE);
	}

	@SuppressWarnings("unchecked")
	private static Optional<Collection<?>> unwrap(Collection<?> collection) {
		if (UNMODIFIABLE_COLLECTION_CLASS.map(cls -> cls.isInstance(collection)).orElse(false))
			return UNMODIFIABLE_COLLECTION_FIELD.flatMap(fld -> Classes.getValue(fld, collection));

		return Optional.empty();
	}
}
