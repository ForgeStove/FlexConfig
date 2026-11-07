package io.github.forgestove.flexconfig.foundation.tree;
import io.github.forgestove.flexconfig.api.RestartType;
import io.github.forgestove.flexconfig.api.annotation.ColorValue;
import io.github.forgestove.flexconfig.foundation.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.lang.reflect.Field;
import java.util.*;
public class ValueConfigNode implements ConfigNode {
	private String name;
	private String path;
	private Component title;
	private Component tooltip;
	private Class<?> valueType;
	private Field field;
	private ElementType elementType;
	private RestartType requiredRestart;
	private Object pendingValue;
	private ColorValue colorValue;
	private SliderRange sliderRange;
	private ValueReader valueReader;
	private ValueWriter valueWriter;
	private ValueValidator validator;
	private ConfigChangeCallback changeCallback;
	private Object defaultValue;
	private Object editingValue;
	private Object baselineValue;
	/** 绑定在集合元素上：值直接读写目标对象，不保存独立编辑副本。 */
	private boolean writeThrough;
	/** 集合元素或元素字段：不参与锁定与快捷键绑定。 */
	private boolean element;
	private String lockPath;
	public static Builder builder() {
		return new Builder();
	}
	@NotNull
	public Class<?> getValueType() {
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
	public void resetToActive(Object config) {
		setEditingValue(copyValue(writeThrough ? baselineValue : currentBase(config)));
	}
	@Override
	public @Nullable RestartType restartType(Object config) {
		return pendingValue != null ? requiredRestart : null;
	}
	@Override
	public boolean isDefaultValue(Object config) {
		return FieldAccess.deepEquals(getDefaultValue(), getEditingValue(config));
	}
	@Override
	public boolean isActiveValue(Object config) {
		return FieldAccess.deepEquals(writeThrough ? baselineValue : currentBase(config), getEditingValue(config));
	}
	@Override
	public @Nullable Component validate(Object config) {
		return validator == null ? null : validator.validate(getEditingValue(config));
	}
	@Override
	public void copy(Object from, Object to) {
		setActiveValue(to, getActiveValue(from));
	}
	@Override
	public void copyForRuntime(Object from, Object to) {
		if (pendingValue != null) return;   // 待生效：运行时先保持旧值
		setActiveValue(to, getActiveValue(from));
	}
	@Override
	public void flushPending(Object to) {
		if (pendingValue == null) return;
		setActiveValue(to, pendingValue);
	}
	@Override
	public void applyRestart(Object saved, Object active, RestartType restartType) {
		if (writeThrough || requiredRestart != restartType || pendingValue == null) return;
		var oldValue = getActiveValue(active);
		var newValue = getActiveValue(saved);
		pendingValue = null;
		if (Objects.equals(oldValue, newValue)) return;
		setActiveValue(active, newValue);
		if (changeCallback != null) changeCallback.onChange(oldValue, newValue);
	}
	@Override
	public void writeEditingToConfig(Object config) {
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
	public void setEditingValue(Object value) {
		if (writeThrough) {
			valueWriter.write(null, value);
			return;
		}
		editingValue = value;
	}
	public Object getDefaultValue() {
		return defaultValue;
	}
	private Object currentBase(Object config) {
		return pendingValue != null ? pendingValue : getActiveValue(config);
	}
	public Object getActiveValue(Object config) {
		return valueReader.read(config);
	}
	public Object getEditingValue(Object config) {
		if (writeThrough) return valueReader.read(config);
		if (editingValue == null) editingValue = copyValue(currentBase(config));
		return editingValue;
	}
	private Object copyValue(Object value) {
		return FieldAccess.deepCopy(value);
	}
	public void setActiveValue(Object config, Object value) {
		valueWriter.write(config, value);
	}
	/** 集合元素视图：元素类型由 {@link ElementType} 在运行时描述，列表元素只能按 Object 操作。 */
	@SuppressWarnings("unchecked")
	public List<Object> getEditingElements(Object config) {
		return (List<Object>) getEditingValue(config);
	}
	public void setEditingElements(List<Object> elements) {
		setEditingValue(elements);
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
	public interface ValueReader {
		Object read(Object source);
	}
	@FunctionalInterface
	public interface ValueWriter {
		void write(Object source, Object value);
	}
	public interface ValueValidator {
		@Nullable Component validate(Object value);
	}
	@FunctionalInterface
	public interface ConfigChangeCallback {
		void onChange(Object oldValue, Object newValue);
	}
	public record SliderRange(double min, double max, double step) {
		public double stepOrAuto() {
			return step > 0 ? step : Math.max(0.01, (max - min) / 100);
		}
	}
	public static class Builder {
		private ValueConfigNode node;
		private Builder() {
			node = new ValueConfigNode();
		}
		public Builder valueType(Class<?> valueType) {
			node.valueType = valueType;
			return this;
		}
		public Builder field(Field field) {
			node.field = field;
			return this;
		}
		public Builder elementType(@Nullable ElementType elementType) {
			node.elementType = elementType;
			return this;
		}
		public Builder name(String name) {
			node.name = name;
			return this;
		}
		public Builder path(String path) {
			node.path = path;
			return this;
		}
		public Builder title(Component title) {
			node.title = title;
			return this;
		}
		public Builder tooltip(Component tooltip) {
			node.tooltip = tooltip;
			return this;
		}
		public Builder defaultValue(Object defaultValue) {
			node.defaultValue = defaultValue;
			return this;
		}
		public Builder restartType(@Nullable RestartType requiredRestart) {
			node.requiredRestart = requiredRestart;
			return this;
		}
		public Builder colorValue(ColorValue colorValue) {
			node.colorValue = colorValue;
			return this;
		}
		public Builder valueReader(ValueReader valueReader) {
			node.valueReader = valueReader;
			return this;
		}
		public Builder valueWriter(ValueWriter valueWriter) {
			node.valueWriter = valueWriter;
			return this;
		}
		public Builder sliderRange(@Nullable SliderRange sliderRange) {
			node.sliderRange = sliderRange;
			return this;
		}
		public Builder validator(ValueValidator validator) {
			node.validator = validator;
			return this;
		}
		public Builder changeCallback(ConfigChangeCallback changeCallback) {
			node.changeCallback = changeCallback;
			return this;
		}
		public Builder writeThrough(boolean writeThrough) {
			node.writeThrough = writeThrough;
			return this;
		}
		public Builder element(boolean element) {
			node.element = element;
			return this;
		}
		public Builder lockPath(String lockPath) {
			node.lockPath = lockPath;
			return this;
		}
		public Builder baselineValue(@Nullable Object baselineValue) {
			node.baselineValue = baselineValue;
			return this;
		}
		public ValueConfigNode build() {
			var n = node;
			List.of(n.name, n.path, n.valueType, n.title, n.valueReader, n.valueWriter).forEach(Objects::requireNonNull);
			node = null;
			return n;
		}
	}
}
