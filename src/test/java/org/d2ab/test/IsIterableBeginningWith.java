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

package org.d2ab.test;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.internal.NullSafety;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.IsEqual.equalTo;

public class IsIterableBeginningWith<E> extends TypeSafeDiagnosingMatcher<Iterable<? extends E>> {
	private final List<Matcher<? super E>> matchers;

	public IsIterableBeginningWith(List<Matcher<? super E>> matchers) {
		this.matchers = matchers;
	}

	@Override
	protected boolean matchesSafely(Iterable<? extends E> iterable, Description mismatchDescription) {
		final MatchSeries<E> matchSeries = new MatchSeries<>(matchers, mismatchDescription);
		int limit = matchers.size();
		for (Iterator<? extends E> iterator = iterable.iterator(); limit-- > 0 && iterator.hasNext(); ) {
			E item = iterator.next();
			if (!matchSeries.matches(item)) {
				return false;
			}
		}

		return matchSeries.isFinished();
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("iterable beginning with ").appendList("[", ", ", "]", matchers);
	}

	private static class MatchSeries<F> {
		private final List<Matcher<? super F>> matchers;
		private final Description mismatchDescription;
		private int nextMatchIx = 0;

		public MatchSeries(List<Matcher<? super F>> matchers, Description mismatchDescription) {
			this.mismatchDescription = mismatchDescription;
			if (matchers.isEmpty()) {
				throw new IllegalArgumentException("Should specify at least one expected element");
			}
			this.matchers = matchers;
		}

		public boolean matches(F item) {
			if (matchers.size() <= nextMatchIx) {
				mismatchDescription.appendText("not matched: ").appendValue(item);
				return false;
			}

			return isMatched(item);
		}

		public boolean isFinished() {
			if (nextMatchIx < matchers.size()) {
				mismatchDescription.appendText("no item was ").appendDescriptionOf(matchers.get(nextMatchIx));
				return false;
			}
			return true;
		}

		private boolean isMatched(F item) {
			final Matcher<? super F> matcher = matchers.get(nextMatchIx);
			if (!matcher.matches(item)) {
				describeMismatch(matcher, item);
				return false;
			}
			nextMatchIx++;
			return true;
		}

		private void describeMismatch(Matcher<? super F> matcher, F item) {
			mismatchDescription.appendText("item " + nextMatchIx + ": ");
			matcher.describeMismatch(item, mismatchDescription);
		}
	}

	/**
	 * Creates a matcher for {@link Iterable}s that matches when a single pass over the
	 * beginning of the examined {@link Iterable} yields a series of items, each logically equal to the
	 * corresponding item in the specified items.
	 * For example:
	 * <pre>assertThat(Arrays.asList("foo", "bar", "xyzzy"), beginsWith("foo", "bar"))</pre>
	 *
	 * @param items the items that must equal the items provided by an examined {@link Iterable}
	 */
	@SafeVarargs
	public static <E> Matcher<Iterable<? extends E>> beginsWith(E... items) {
		List<Matcher<? super E>> matchers = new ArrayList<>();
		for (E item : items) {
			matchers.add(equalTo(item));
		}

		return beginsWith(matchers);
	}

	/**
	 * Creates a matcher for {@link Iterable}s that matches when a single pass over the
	 * beginning of the examined {@link Iterable} yields a first item that satisfies the
	 * specified matcher.
	 * For example:
	 * <pre>assertThat(Arrays.asList("foo", "bar"), beginsWith(equalTo("foo")))</pre>
	 *
	 * @param itemMatcher the matcher that must be satisfied by the first item provided by an examined {@link Iterable}
	 */
	@SuppressWarnings("unchecked")
	public static <E> Matcher<Iterable<? extends E>> beginsWith(final Matcher<? super E> itemMatcher) {
		return beginsWith(new ArrayList<>(singletonList(itemMatcher)));
	}

	/**
	 * Creates a matcher for {@link Iterable}s that matches when a single pass over the
	 * beginning of the examined {@link Iterable} yields a series of items, each
	 * satisfying the corresponding matcher in the specified matchers.
	 * For example:
	 * <pre>assertThat(Arrays.asList("foo", "bar", "xyzzt"), beginsWith(equalTo("foo"), equalTo("bar")))</pre>
	 *
	 * @param itemMatchers the matchers that must be satisfied by the items provided by an examined {@link Iterable}
	 */
	@SafeVarargs
	public static <E> Matcher<Iterable<? extends E>> beginsWith(Matcher<? super E>... itemMatchers) {
		// required for JDK 1.6
		final List<Matcher<? super E>> nullSafeWithExplicitTypeMatchers = NullSafety.nullSafe(itemMatchers);
		return beginsWith(nullSafeWithExplicitTypeMatchers);
	}

	/**
	 * Creates a matcher for {@link Iterable}s that matches when a single pass over the beginning of the examined
	 * {@link
	 * Iterable} yields a series of items, each satisfying the corresponding matcher in the specified list of matchers.
	 * For example:
	 * <pre>assertThat(Arrays.asList("foo", "bar", "xyzzt"), beginsWith(Arrays.asList(equalTo("foo"),
	 * equalTo("bar"))))</pre>
	 *
	 * @param itemMatchers a list of matchers, each of which must be satisfied by the corresponding item provided by an
	 *                     examined {@link Iterable}
	 */
	public static <E> Matcher<Iterable<? extends E>> beginsWith(List<Matcher<? super E>> itemMatchers) {
		return new IsIterableBeginningWith<>(itemMatchers);
	}
}
