package org.d2ab.function;

import java.util.Comparator;
import java.util.function.BinaryOperator;

/**
 * Utility methods for {@link BinaryOperator}.
 */
public class BinaryOperators {
	BinaryOperators() {
		// for test coverage
	}

	@SuppressWarnings("unchecked")
	public static <T> BinaryOperator<T> firstMinBy(Comparator<? super T> comparator) {
		if (comparator == null)
			return (a, b) -> ((Comparable) a).compareTo(b) <= 0 ? a : b;
		else
			return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
	}

	@SuppressWarnings("unchecked")
	public static <T> BinaryOperator<T> firstMaxBy(Comparator<? super T> comparator) {
		if (comparator == null)
			return (a, b) -> ((Comparable) a).compareTo(b) >= 0 ? a : b;
		else
			return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
	}

	@SuppressWarnings("unchecked")
	public static <T> BinaryOperator<T> lastMinBy(Comparator<? super T> comparator) {
		if (comparator == null)
			return (a, b) -> ((Comparable) a).compareTo(b) < 0 ? a : b;
		else
			return (a, b) -> comparator.compare(a, b) < 0 ? a : b;
	}

	@SuppressWarnings("unchecked")
	public static <T> BinaryOperator<T> lastMaxBy(Comparator<? super T> comparator) {
		if (comparator == null)
			return (a, b) -> ((Comparable) a).compareTo(b) > 0 ? a : b;
		else
			return (a, b) -> comparator.compare(a, b) > 0 ? a : b;
	}
}
