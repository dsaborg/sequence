package org.d2ab.util;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Created by Daniel on 2017-02-19.
 */
public abstract class Classes {
	Classes() {
	}

	@SuppressWarnings("unchecked")
	public static <C extends Class<?>> Optional<C> classByName(String className) {
		try {
			return Optional.of((C) Class.forName(className));
		} catch (ClassNotFoundException | RuntimeException e) {
			return Optional.empty();
		}
	}

	public static Optional<Field> accessibleField(Class<?> cls, String fieldName) {
		try {
			Field f = cls.getDeclaredField(fieldName);
			f.setAccessible(true);
			return Optional.of(f);
		} catch (NoSuchFieldException | RuntimeException e) {
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<T> getValue(Field field, Object object) {
		try {
			return Optional.of((T) field.get(object));
		} catch (IllegalAccessException | RuntimeException e) {
			return Optional.empty();
		}
	}
}
