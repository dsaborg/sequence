package org.d2ab.collection;

import org.d2ab.util.Strict;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Daniel on 2017-01-08.
 */
public abstract class PrimitiveCollections {
	PrimitiveCollections() {
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Collection<?> collection, T[] a) {
		Strict.check();

		int size = collection.size();
		if (a.length < size)
			a = Arrays.copyOf(a, size);

		int index = 0;
		for (Object o : collection)
			a[index++] = (T) o;

		if (a.length > size)
			a[size] = null;

		return a;
	}
}
