package org.d2ab.util;

import java.util.Stack;

public abstract class Strict {
	public static final String PROPERTY = "org.d2ab.sequence.strict";

	public static State state;

	Strict() {
	}

	static {
		init();
	}

	public static void init() {
		state = new State(!Boolean.getBoolean(PROPERTY));
	}

	public static boolean isLenient() {
		return state.isLenient();
	}

	public static void unset() {
		state.unset();
	}

	public static void reset() {
		state.reset();
	}

	public static void check() {
		assert isLenient() : "Strict checking enabled";
	}

	static final class State {
		private boolean lenient;
		private final Stack<Boolean> history = new Stack<>();

		public State(boolean lenient) {
			this.lenient = lenient;
		}

		public boolean isLenient() {
			return lenient;
		}

		public void unset() {
			history.push(lenient);
			lenient = true;
		}

		public void reset() {
			lenient = history.pop();
		}
	}
}
