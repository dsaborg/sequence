package org.d2ab.sequence;

import java.util.Iterator;

public class Iterators {
	private Iterators() {
	}

	public static void skipOne(Iterator<?> iterator) {
		skip(1, iterator);
	}

	public static void skip(int steps, Iterator<?> iterator) {
		for (int count = 0; count < steps && iterator.hasNext(); count++) {
			iterator.next();
		}
	}
}
