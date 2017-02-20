package org.d2ab.util;

import org.d2ab.collection.Lists;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ClassesTest {
	@Test
	public void constructor() {
		new Classes() {
			// test coverage
		};
	}

	@Test
	public void classByName() {
		assertThat(Classes.classByName("java.util.List"), is(Optional.of(List.class)));
		assertThat(Classes.classByName("foo"), is(Optional.empty()));
	}

	@Test
	public void accessibleField() {
		Optional<Field> elementData = Classes.accessibleField(ArrayList.class, "elementData");
		assertThat(elementData.get().getType(), is(Object[].class));
		assertThat(elementData.get().isAccessible(), is(true));

		Optional<Field> foo = Classes.accessibleField(ArrayList.class, "foo");
		assertThat(foo, is(Optional.empty()));
	}

	@Test
	public void getValue() throws NoSuchFieldException {
		Field f = ArrayList.class.getDeclaredField("elementData");
		f.setAccessible(true);

		Optional<Object[]> elementData = Classes.getValue(f, new ArrayList<>(Lists.of(1, 2, 3, 4, 5)));
		assertThat(elementData.get(), is(arrayContaining(1, 2, 3, 4, 5)));

		Optional<Field> foo = Classes.getValue(f, "foo");
		assertThat(foo, is(Optional.empty()));
	}
}