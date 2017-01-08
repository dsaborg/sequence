package org.d2ab.util;

import java.util.Stack;

public abstract class Strict {
	Strict() {
	}

	private static final String STRICT_PROPERTY = "org.d2ab.sequence.strict";

	public static boolean LENIENT = !Boolean.getBoolean(STRICT_PROPERTY);

	private static final Stack<Boolean> state = new Stack<>();

	public static void unset() {
		state.push(LENIENT);
		LENIENT = true;
	}

	public static void reset() {
		LENIENT = state.pop();
	}

	public static void check() {
		assert LENIENT : "Strict checking enabled";
	}
}
