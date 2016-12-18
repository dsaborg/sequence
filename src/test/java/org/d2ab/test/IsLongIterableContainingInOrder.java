/*
 * BSD License
 *
 * Copyright (c) 2000-2015 www.hamcrest.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce
 * the above copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Hamcrest nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package org.d2ab.test;

import org.d2ab.collection.longs.LongIterable;
import org.d2ab.iterator.longs.LongIterator;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.internal.NullSafety;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.IsEqual.equalTo;

public class IsLongIterableContainingInOrder extends TypeSafeDiagnosingMatcher<LongIterable> {
	private final List<Matcher<? super Long>> matchers;

	public IsLongIterableContainingInOrder(List<Matcher<? super Long>> matchers) {
		this.matchers = matchers;
	}

	@Override
	protected boolean matchesSafely(LongIterable iterable, Description mismatchDescription) {
		final LongMatchSeries matchSeries = new LongMatchSeries(matchers, mismatchDescription);
		for (LongIterator iterator = iterable.iterator(); iterator.hasNext(); ) {
			long item = iterator.nextLong();
			if (!matchSeries.matches(item)) {
				return false;
			}
		}

		return matchSeries.isFinished();
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("long iterable containing ").appendList("[", ", ", "]", matchers);
	}

	private static class LongMatchSeries {
		private final List<Matcher<? super Long>> matchers;
		private final Description mismatchDescription;
		private int nextMatchIx = 0;

		public LongMatchSeries(List<Matcher<? super Long>> matchers, Description mismatchDescription) {
			this.mismatchDescription = mismatchDescription;
			if (matchers.isEmpty()) {
				throw new IllegalArgumentException("Should specify at least one expected long");
			}
			this.matchers = matchers;
		}

		public boolean matches(long item) {
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

		private boolean isMatched(long item) {
			final Matcher<? super Long> matcher = matchers.get(nextMatchIx);
			if (!matcher.matches(item)) {
				describeMismatch(matcher, item);
				return false;
			}
			nextMatchIx++;
			return true;
		}

		private void describeMismatch(Matcher<? super Long> matcher, long item) {
			mismatchDescription.appendText("item " + nextMatchIx + ": ");
			matcher.describeMismatch(item, mismatchDescription);
		}
	}

	/**
	 * Creates a matcher for {@link LongIterable}s that matches when a single pass over the
	 * examined {@link LongIterable} yields a series of items, each logically equal to the
	 * corresponding item in the specified items.  For a positive match, the examined iterable
	 * must be of the same length as the number of specified items.
	 *
	 * @param items the items that must equal the items provided by an examined {@link LongIterable}
	 */
	public static Matcher<LongIterable> containsLongs(long... items) {
		List<Matcher<? super Long>> matchers = new ArrayList<>();
		for (long item : items) {
			matchers.add(equalTo(item));
		}

		return containsLongs(matchers);
	}

	/**
	 * Creates a matcher for {@link LongIterable}s that matches when a single pass over the
	 * examined {@link LongIterable} yields a single item that satisfies the specified matcher.
	 * For a positive match, the examined iterable must only yield one item.
	 *
	 * @param itemMatcher the matcher that must be satisfied by the single item provided by an examined {@link
	 *                    LongIterable}
	 */
	@SuppressWarnings("unchecked")
	public static Matcher<LongIterable> containsLongs(final Matcher<? super Long> itemMatcher) {
		return containsLongs(new ArrayList<>(singletonList(itemMatcher)));
	}

	/**
	 * Creates a matcher for {@link LongIterable}s that matches when a single pass over the
	 * examined {@link LongIterable} yields a series of items, each satisfying the corresponding
	 * matcher in the specified matchers.  For a positive match, the examined iterable
	 * must be of the same length as the number of specified matchers.
	 *
	 * @param itemMatchers the matchers that must be satisfied by the items provided by an examined {@link Iterable}
	 */
	@SafeVarargs
	public static Matcher<LongIterable> containsLongs(Matcher<? super Long>... itemMatchers) {
		// required for JDK 1.6
		final List<Matcher<? super Long>> nullSafeWithExplicitTypeMatchers = NullSafety.nullSafe(itemMatchers);
		return containsLongs(nullSafeWithExplicitTypeMatchers);
	}

	/**
	 * Creates a matcher for {@link LongIterable}s that matches when a single pass over the
	 * examined {@link LongIterable} yields a series of items, each satisfying the corresponding
	 * matcher in the specified list of matchers.  For a positive match, the examined iterable
	 * must be of the same length as the specified list of matchers.
	 *
	 * @param itemMatchers a list of matchers, each of which must be satisfied by the corresponding item provided by an
	 *                     examined {@link LongIterable}
	 */
	public static Matcher<LongIterable> containsLongs(List<Matcher<? super Long>> itemMatchers) {
		return new IsLongIterableContainingInOrder(itemMatchers);
	}
}
