package org.d2ab.util;

import java.util.Stack;

public abstract class Strict {
	private static final String STRICT_PROPERTY = "org.d2ab.iterator.strict";

	public static boolean ENABLED = Boolean.getBoolean(STRICT_PROPERTY);

	private static Stack<Boolean> state = new Stack<>();

	public static void unset() {
		state.push(ENABLED);
		ENABLED = false;
	}

	public static void reset() {
		ENABLED = state.pop();
	}
}
