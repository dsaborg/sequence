package org.d2ab.collection;

/**
 * An {@link Iterable} which can also report the size of its contents.
 */
public interface SizedIterable<T> extends Iterable<T> {
	int size();

	boolean isEmpty();
}
