package io.github.forgestove.flexconfig;
import org.jetbrains.annotations.Nullable;

import java.util.List;
/** 集合元素类型；嵌套集合通过 {@link #element()} 递归描述。 */
public record ElementType(Class<?> raw, @Nullable ElementType element) {
	public boolean isList() {
		return List.class.isAssignableFrom(raw);
	}
	public boolean isArray() {
		return raw.isArray();
	}
	public boolean isCollection() {
		return isList() || isArray();
	}
	public boolean isSimple() {
		return FieldAccess.isSimpleType(raw);
	}
}
