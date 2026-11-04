package io.github.forgestove.flexconfig.tree;
import io.github.forgestove.flexconfig.ElementType;
import io.github.forgestove.flexconfig.FieldAccess;
import io.github.forgestove.flexconfig.api.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.lang.reflect.Field;
import java.util.*;
public class ValueConfigNode<C, V> implements ConfigNode<C> {
	private String name;
	private String path;
	private Component title;
	private Component tooltip;
	private Class<? extends V> valueType;
	private Field field;
	private ElementType elementType;
	private RestartType requiredRestart;
	private V pendingValue;
	private ColorValue colorValue;
	private SliderRange sliderRange;
	private ValueReader<C, V> valueReader;
	private ValueWriter<C, V> valueWriter;
	private ValueValidator<V> validator;
	private ConfigChangeCallback changeCallback;
	private V defaultValue;
	private V editingValue;
	private V baselineValue;
	/** 绑定在集合元素上：值直接读写目标对象，不保存独立编辑副本。 */
	private boolean writeThrough;
	/** 集合元素或元素字段：不参与锁定与快捷键绑定。 */
	private boolean element;
	private String lockPath;
	public static <C, V> Builder<C, V> builder() {
		return new Builder<>();
	}
	@NotNull
	public Class<? extends V> getValueType() {
		return valueType;
	}
	@Nullable
	public Field getField() {
		return field;
	}
	public @Nullable ElementType getElementType() {
		return elementType;
	}
	public boolean isElement() {
		return element;
	}
	/** 锁定状态挂在所属集合的路径上，元素自身没有独立锁。 */
	public String getLockPath() {
		return lockPath != null ? lockPath : path;
	}
	@NotNull
	public Component getTitle() {
		return title;
	}
	@NotNull
	public String getName() {
		return name;
	}
	@Nullable
	@Override
	public Component getTooltip() {
		return tooltip;
	}
	@Override
	public void resetToDefault() {
		setEditingValue(copyValue(getDefaultValue()));
	}
	@Override
	public void resetToActive(C config) {
		setEditingValue(copyValue(writeThrough ? baselineValue : currentBase(config)));
	}
	@Override
	public @Nullable RestartType restartType(C config) {
		return pendingValue != null ? requiredRestart : null;
	}
	@Override
	public boolean isDefaultValue(C config) {
		return FieldAccess.deepEquals(getDefaultValue(), getEditingValue(config));
	}
	@Override
	public boolean isActiveValue(C config) {
		return FieldAccess.deepEquals(writeThrough ? baselineValue : currentBase(config), getEditingValue(config));
	}
	@Override
	public @Nullable Component validate(C config) {
		return validator == null ? null : validator.validate(getEditingValue(config));
	}
	@Override
	public void copy(C from, C to) {
		setActiveValue(to, getActiveValue(from));
	}
	@Override
	public void copyForRuntime(C from, C to) {
		if (pendingValue != null) return;   // 待生效：运行时先保持旧值
		setActiveValue(to, getActiveValue(from));
	}
	@Override
	public void flushPending(C to) {
		if (pendingValue == null) return;
		setActiveValue(to, pendingValue);
	}
	@Override
	public void applyRestart(C saved, C active, RestartType restartType) {
		if (writeThrough || requiredRestart != restartType || pendingValue == null) return;
		var oldValue = getActiveValue(active);
		var newValue = getActiveValue(saved);
		pendingValue = null;
		if (Objects.equals(oldValue, newValue)) return;
		setActiveValue(active, newValue);
		if (changeCallback != null) changeCallback.onChange(oldValue, newValue);
	}
	@Override
	public void writeEditingToConfig(C config) {
		if (writeThrough) return;   // 已经直接写进集合元素
		var oldValue = getActiveValue(config);
		var newValue = getEditingValue(config);
		if (requiredRestart == null) {
			setActiveValue(config, newValue);
			if (changeCallback != null && !Objects.equals(oldValue, newValue)) changeCallback.onChange(oldValue, newValue);
			return;
		}
		pendingValue = Objects.equals(oldValue, newValue) ? null : newValue;
	}
	@Override
	public String getPath() {
		return path;
	}
	public void setEditingValue(V value) {
		if (writeThrough) {
			valueWriter.write(null, value);
			return;
		}
		editingValue = value;
	}
	public V getDefaultValue() {
		return defaultValue;
	}
	private V currentBase(C config) {
		return pendingValue != null ? pendingValue : getActiveValue(config);
	}
	public V getActiveValue(C config) {
		return valueReader.read(config);
	}
	public V getEditingValue(C config) {
		if (writeThrough) return valueReader.read(config);
		if (editingValue == null) editingValue = copyValue(currentBase(config));
		return editingValue;
	}
	private V copyValue(V value) {
		return FieldAccess.deepCopy(value);
	}
	public void setActiveValue(C config, V value) {
		valueWriter.write(config, value);
	}
	@Nullable
	public SliderRange getSliderRange() {
		return sliderRange;
	}
	public boolean colorHasAlpha() {
		return isColorValue() && colorValue.value();
	}
	public boolean isColorValue() {
		return colorValue != null;
	}
	@FunctionalInterface
	public interface ValueReader<S, V> {
		V read(S source);
	}
	@FunctionalInterface
	public interface ValueWriter<S, V> {
		void write(S source, V value);
	}
	public interface ValueValidator<V> {
		@Nullable Component validate(V value);
	}
	@FunctionalInterface
	public interface ConfigChangeCallback {
		void onChange(Object oldValue, Object newValue);
	}
	public record SliderRange(double min, double max) {}
	public static class Builder<C, V> {
		private ValueConfigNode<C, V> node;
		private Builder() {
			node = new ValueConfigNode<>();
		}
		public Builder<C, V> valueType(Class<? extends V> valueType) {
			node.valueType = valueType;
			return this;
		}
		public Builder<C, V> field(Field field) {
			node.field = field;
			return this;
		}
		public Builder<C, V> elementType(@Nullable ElementType elementType) {
			node.elementType = elementType;
			return this;
		}
		public Builder<C, V> name(String name) {
			node.name = name;
			return this;
		}
		public Builder<C, V> path(String path) {
			node.path = path;
			return this;
		}
		public Builder<C, V> title(Component title) {
			node.title = title;
			return this;
		}
		public Builder<C, V> tooltip(Component tooltip) {
			node.tooltip = tooltip;
			return this;
		}
		public Builder<C, V> defaultValue(V defaultValue) {
			node.defaultValue = defaultValue;
			return this;
		}
		public Builder<C, V> restartType(@Nullable RestartType requiredRestart) {
			node.requiredRestart = requiredRestart;
			return this;
		}
		public Builder<C, V> colorValue(ColorValue colorValue) {
			node.colorValue = colorValue;
			return this;
		}
		public Builder<C, V> valueReader(ValueReader<C, V> valueReader) {
			node.valueReader = valueReader;
			return this;
		}
		public Builder<C, V> valueWriter(ValueWriter<C, V> valueWriter) {
			node.valueWriter = valueWriter;
			return this;
		}
		public Builder<C, V> sliderRange(@Nullable SliderRange sliderRange) {
			node.sliderRange = sliderRange;
			return this;
		}
		public Builder<C, V> validator(ValueValidator<V> validator) {
			node.validator = validator;
			return this;
		}
		public Builder<C, V> changeCallback(ConfigChangeCallback changeCallback) {
			node.changeCallback = changeCallback;
			return this;
		}
		public Builder<C, V> writeThrough(boolean writeThrough) {
			node.writeThrough = writeThrough;
			return this;
		}
		public Builder<C, V> element(boolean element) {
			node.element = element;
			return this;
		}
		public Builder<C, V> lockPath(String lockPath) {
			node.lockPath = lockPath;
			return this;
		}
		public Builder<C, V> baselineValue(@Nullable V baselineValue) {
			node.baselineValue = baselineValue;
			return this;
		}
		public ValueConfigNode<C, V> build() {
			var n = node;
			List.of(n.name, n.path, n.valueType, n.title, n.valueReader, n.valueWriter).forEach(Objects::requireNonNull);
			node = null;
			return n;
		}
	}
}
