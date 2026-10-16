package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.api.Order;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
public final class FieldAccess {
	/**
	 * 按 {@link Order @Order} 值排序，同值者保持声明顺序，无注解者视为 0。
	 * 配置树与序列化器须共用本方法，否则会出现「GUI 能看到但永不落盘」一类不一致。
	 */
	public static Field[] collectOrderedFields(Class<?> clazz) {
		var fields = clazz.getDeclaredFields();
		var indices = new HashMap<String, Integer>();
		for (var i = 0; i < fields.length; i++) indices.put(fields[i].getName(), i);
		Arrays.sort(
			fields, Comparator.comparingInt((Field f) -> {
				var order = f.getAnnotation(Order.class);
				return order != null ? order.value() : 0;
			}).thenComparingInt(f -> indices.get(f.getName()))
		);
		return fields;
	}
	/** 裸 {@code List} 或实参不是 {@code Class} 时返回 {@code null}。 */
	public static Class<?> elementType(Field field) {
		if (field.getGenericType() instanceof ParameterizedType parameterized
			&& parameterized.getActualTypeArguments()[0] instanceof Class<?> element) return boxed(element);
		return null;
	}
	/** 原始类型映射到包装类型，因为值类型注册表按包装类型索引。 */
	public static Class<?> boxed(Class<?> type) {
		if (type == int.class) return Integer.class;
		if (type == boolean.class) return Boolean.class;
		if (type == long.class) return Long.class;
		if (type == float.class) return Float.class;
		if (type == double.class) return Double.class;
		if (type == byte.class) return Byte.class;
		if (type == short.class) return Short.class;
		if (type == char.class) return Character.class;
		return type;
	}
	public static VarHandle varHandle(Field field) {
		try {
			return MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup()).unreflectVarHandle(field);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Cannot access field: " + field.getName(), e);
		}
	}
	public static Object getFieldValue(Field field, Object target) {
		try {
			field.setAccessible(true);
			return field.get(target);
		} catch (IllegalAccessException | InaccessibleObjectException | SecurityException e) {
			throw new IllegalArgumentException("Failed to access field: " + field.getName(), e);
		}
	}
	public static Object readField(VarHandle handle, Object target, Field field) {
		try {
			return handle.get(target);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Failed to read " + field.getName(), e);
		}
	}
	public static void writeField(VarHandle handle, Object target, Object value, Field field) {
		try {
			handle.set(target, value);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Failed to write " + field.getName(), e);
		}
	}
}
