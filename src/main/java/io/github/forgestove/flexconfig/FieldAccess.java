package io.github.forgestove.flexconfig;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.Order;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class FieldAccess {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Set<Field> WARNED_FIELDS = ConcurrentHashMap.newKeySet();
	/**
	 * 按 {@link Order @Order} 值排序，同值者保持声明顺序，无注解者视为 0。
	 * 配置树与序列化器须共用本方法，否则会出现「GUI 能看到但永不落盘」一类不一致。
	 */
	public static Field[] collectOrderedFields(Class<?> clazz) {
		var fields = Arrays.stream(clazz.getDeclaredFields())
			.filter(field -> !Modifier.isStatic(field.getModifiers()))
			.filter(field -> !Modifier.isTransient(field.getModifiers()))
			.filter(field -> !field.isSynthetic())
			.toArray(Field[]::new);
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
	/** 数组按组件类型、{@code List<T>} 按实参递归解析；裸 {@code List} 或无法解析时返回 {@code null}。 */
	public static @Nullable ElementType elementShape(Field field) {
		if (field.getType().isArray()) return shape(field.getType().getComponentType());
		if (field.getGenericType() instanceof ParameterizedType parameterized
			&& parameterized.getActualTypeArguments().length == 1) return shape(parameterized.getActualTypeArguments()[0]);
		return null;
	}
	private static @Nullable ElementType shape(Type type) {
		if (type instanceof Class<?> cls) {
			var boxedClass = boxed(cls);
			return new ElementType(boxedClass, boxedClass.isArray() ? shape(boxedClass.getComponentType()) : null);
		}
		if (type instanceof ParameterizedType parameterized && parameterized.getRawType() instanceof Class<?> raw)
			return new ElementType(raw,
				parameterized.getActualTypeArguments().length == 1 ? shape(parameterized.getActualTypeArguments()[0]) : null);
		if (type instanceof WildcardType wildcard && wildcard.getUpperBounds().length == 1) return shape(wildcard.getUpperBounds()[0]);
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
	/** 能直接映射成单个 TOML 值或单个编辑框的类型。 */
	public static boolean isSimpleType(Class<?> type) {
		type = boxed(type);
		return type.isEnum() || type == String.class || isImmutable(type);
	}
	public static List<Object> arrayToList(@Nullable Object array) {
		if (array == null) return new ArrayList<>();
		var length = Array.getLength(array);
		var list = new ArrayList<>(length);
		for (var i = 0; i < length; i++) list.add(Array.get(array, i));
		return list;
	}
	public static Object listToArray(Class<?> componentType, List<?> list) {
		var array = Array.newInstance(componentType, list.size());
		for (var i = 0; i < list.size(); i++) Array.set(array, i, list.get(i));
		return array;
	}
	private static @Nullable Object unsafe;
	private static @Nullable Method allocateInstance;
	static {
		try {
			var unsafeClass = Class.forName("sun.misc.Unsafe");
			var theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			unsafe = theUnsafe.get(null);
			allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
		} catch (ReflectiveOperationException | InaccessibleObjectException | SecurityException e) {
			unsafe = null;
			allocateInstance = null;
		}
	}
	/** 优先无参构造；没有时退回 {@code Unsafe.allocateInstance}，与 Cloth Config 的构造方式一致。 */
	public static @Nullable Object newInstance(Class<?> type) {
		try {
			var constructor = type.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
		} catch (ReflectiveOperationException | InaccessibleObjectException | SecurityException ignored) {}
		if (unsafe == null || allocateInstance == null) return null;
		try {
			return allocateInstance.invoke(unsafe, type);
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}
	public static @Nullable Object getFieldValue(Field field, @Nullable Object target) {
		try {
			field.setAccessible(true);
			return field.get(target);
		} catch (IllegalAccessException | InaccessibleObjectException e) {
			throw new IllegalArgumentException("Failed to access field: " + field.getName(), e);
		}
	}
	public static void setFieldValue(Field field, Object target, @Nullable Object value) {
		try {
			field.setAccessible(true);
			field.set(target, value);
		} catch (IllegalAccessException | InaccessibleObjectException e) {
			throw new IllegalArgumentException("Failed to write field: " + field.getName(), e);
		}
	}
	public static VarHandle varHandle(Field field) {
		try {
			field.setAccessible(true);
			return MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup()).unreflectVarHandle(field);
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
	/** 深拷贝集合与普通对象；无法实例化的类型保持原引用。 */
	@SuppressWarnings("unchecked")
	public static <T> @Nullable T deepCopy(@Nullable T value) {
		return (T) deepCopy(value, new IdentityHashMap<>());
	}
	private static @Nullable Object deepCopy(@Nullable Object value, IdentityHashMap<Object, Object> seen) {
		if (value == null) return null;
		var type = value.getClass();
		if (isImmutable(type)) return value;
		if (isDeeplyImmutable(type)) return value;
		var copied = seen.get(value);
		if (copied != null) return copied;
		if (type.isArray()) {
			var length = Array.getLength(value);
			var array = Array.newInstance(type.getComponentType(), length);
			seen.put(value, array);
			for (var i = 0; i < length; i++) Array.set(array, i, deepCopy(Array.get(value, i), seen));
			return array;
		}
		switch (value) {
			case List<?> list -> {
				var listCopy = new ArrayList<>(list.size());
				seen.put(value, listCopy);
				for (var element : list) listCopy.add(deepCopy(element, seen));
				return listCopy;
			}
			case Set<?> set -> {
				var setCopy = new LinkedHashSet<>(Math.max(16, set.size()));
				seen.put(value, setCopy);
				for (var element : set) setCopy.add(deepCopy(element, seen));
				return setCopy;
			}
			case Map<?, ?> map -> {
				var mapCopy = new LinkedHashMap<>(Math.max(16, map.size()));
				seen.put(value, mapCopy);
				for (var entry : map.entrySet()) mapCopy.put(deepCopy(entry.getKey(), seen), deepCopy(entry.getValue(), seen));
				return mapCopy;
			}
			default -> {}
		}
		var copy = newInstance(type);
		if (copy == null) return value;
		seen.put(value, copy);
		for (var field : collectOrderedFields(type)) try {
			writeFieldDirectly(field, copy, deepCopy(getFieldValue(field, value), seen));
		} catch (RuntimeException e) {
			LOGGER.warn("Failed to copy field '{}' of {}", field.getName(), type.getName(), e);
		}
		return copy;
	}
	/** 结构相等：集合逐元素、对象逐字段比较，避免深拷贝后 identity 比较误判为「已修改」。 */
	public static boolean deepEquals(@Nullable Object first, @Nullable Object second) {
		if (first == second) return true;
		if (first == null || second == null || !first.getClass().equals(second.getClass())) return false;
		if (first.getClass().isArray()) {
			var length = Array.getLength(first);
			if (length != Array.getLength(second)) return false;
			for (var i = 0; i < length; i++)
				if (!deepEquals(Array.get(first, i), Array.get(second, i))) return false;
			return true;
		}
		switch (first) {
			case List<?> firstList -> {
				if (!(second instanceof List<?> secondList) || firstList.size() != secondList.size()) return false;
				for (var i = 0; i < firstList.size(); i++)
					if (!deepEquals(firstList.get(i), secondList.get(i))) return false;
				return true;
			}
			case Set<?> firstSet -> {
				if (!(second instanceof Set<?> secondSet) || firstSet.size() != secondSet.size()) return false;
				var remaining = new ArrayList<>(secondSet);
				for (var element : firstSet) {
					var matched = false;
					for (var iterator = remaining.iterator(); iterator.hasNext(); )
						if (deepEquals(element, iterator.next())) {
							iterator.remove();
							matched = true;
							break;
						}
					if (!matched) return false;
				}
				return true;
			}
			case Map<?, ?> firstMap -> {
				if (!(second instanceof Map<?, ?> secondMap) || firstMap.size() != secondMap.size()) return false;
				for (var entry : firstMap.entrySet())
					if (!secondMap.containsKey(entry.getKey()) || !deepEquals(entry.getValue(), secondMap.get(entry.getKey()))) return false;
				return true;
			}
			default -> {}
		}
		if (isImmutable(first.getClass()) || isDeeplyImmutable(first.getClass()) || first.equals(second))
			return first.equals(second);
		for (var field : collectOrderedFields(first.getClass())) try {
			if (!deepEquals(getFieldValue(field, first), getFieldValue(field, second))) return false;
		} catch (RuntimeException e) {
			if (WARNED_FIELDS.add(field)) LOGGER.warn("Cannot compare field '{}', treating values as different", field.getName(), e);
			return false;
		}
		return true;
	}
	private static boolean isImmutable(Class<?> type) {
		return type.isPrimitive() || type.isEnum()
			|| type == String.class || type == Boolean.class || type == Character.class
			|| type == Byte.class || type == Short.class || type == Integer.class
			|| type == Long.class || type == Float.class || type == Double.class;
	}
	/**
	 * final 类且所有实例字段都是 final：以构造器创建的值不可变，直接复用引用。
	 * 反射无法安全改写 final 字段，深拷贝会造出字段为 null 的残次对象（如 {@code InputConstants.Key}）。
	 */
	private static boolean isDeeplyImmutable(Class<?> type) {
		if (!Modifier.isFinal(type.getModifiers())) return false;
		var hasInstanceField = false;
		for (var field : type.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) continue;
			hasInstanceField = true;
			if (!Modifier.isFinal(field.getModifiers())) return false;
		}
		return hasInstanceField;
	}
	private static void writeFieldDirectly(Field field, Object target, @Nullable Object value) {
		try {
			field.setAccessible(true);
			field.set(target, value);
		} catch (IllegalAccessException | InaccessibleObjectException e) {
			throw new IllegalArgumentException("Failed to write field: " + field.getName(), e);
		}
	}
}
