package org.d2ab.collection;

import org.d2ab.util.Strict;

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

		return Collectionz.toArray(collection, a);
	}
}
