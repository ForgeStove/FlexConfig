package io.github.forgestove.flexconfig.foundation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
