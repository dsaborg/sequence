package org.d2ab.function;

/**
 * Primitive specialization of a function taking a reference argument and returning a {@code long} value.
 */
public interface ToLongFunction<T> {
	long applyAsLong(T t);
}
