package org.da2ab.sequence;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PairTest {
	private final Pair<Integer, Integer> pair = Pair.of(1, 2);

	@Test
	public void of() {
		assertThat(pair.first(), is(1));
		assertThat(pair.second(), is(2));
	}

	@Test
	public void checkToString() {
		assertThat(pair.toString(), is("(1,2)"));
	}

	@Test
	public void checkHashCode() {
		assertThat(pair.hashCode(), is(pair.hashCode()));
	}

	@Test
	public void checkEquals() {
		assertThat(pair.equals(Pair.of(1, 2)), is(true));
		assertThat(pair.equals(Pair.of(1, 3)), is(false));
		assertThat(pair.equals(Pair.of(3, 2)), is(false));
		assertThat(pair.equals(null), is(false));
		assertThat(pair.equals(new Object()), is(false));
	}

	@Test
	public void put() {
		Map<Integer, Integer> map = new HashMap<>();
		pair.put(map);
		assertThat(map.get(1), is(2));
	}
}
