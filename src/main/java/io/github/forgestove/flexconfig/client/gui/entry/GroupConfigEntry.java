package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.client.gui.EntryTypeRegistry;
import io.github.forgestove.flexconfig.client.gui.api.TabLifecycle;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import io.github.forgestove.flexconfig.tree.ValueConfigNode.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.*;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BooleanSupplier;
/** 可折叠的对象分组：点标题展开字段，{@code @Category} 子对象递归成下一级分组。 */
public class GroupConfigEntry<C> extends ConfigEntry {
	private static final Set<Object> expandedElements = Collections.newSetFromMap(new WeakHashMap<>());
	private static final String EXPANDED_PREFIX = "- ";
	private static final String COLLAPSED_PREFIX = "+ ";
	private final ConfigCategoryTab<C, ?> tab;
	private final Object target;
	@Nullable private final Object defaults;
	private final Component label;
	private final String path;
	private final String keyPrefix;
	private final String lockPath;
	@Nullable private final BooleanSupplier canDelete;
	private final boolean expanded;
	private final ClickWidget clickWidget;
	@Nullable private final Button deleteButton;
	public GroupConfigEntry(
		ConfigCategoryTab<C, ?> tab,
		Object target,
		@Nullable Object defaults,
		Component label,
		String path,
		String keyPrefix,
		String lockPath,
		int indent,
		@Nullable Runnable onDelete,
		@Nullable BooleanSupplier canDelete
	) {
		this.tab = tab;
		this.target = target;
		this.defaults = defaults;
		this.label = label;
		this.path = path;
		this.keyPrefix = keyPrefix;
		this.lockPath = lockPath;
		this.canDelete = canDelete;
		setIndent(indent);
		expanded = expandedElements.contains(target);
		clickWidget = new ClickWidget();
		deleteButton = onDelete == null ? null : Button.builder(Component.literal("×"), b -> onDelete.run()).size(SIZE, HEIGHT).build();
		if (deleteButton != null) deleteButton.setTooltip(Tooltip.create(Component.literal("×")));
	}
	public boolean isExpanded() {
		return expanded;
	}
	private void toggle() {
		if (expanded) expandedElements.remove(target);
		else expandedElements.add(target);
		tab.rebuildEntries();
	}
	/** 按目标对象的字段生成子条目，{@code @Category} 字段继续递归成子分组。 */
	public void appendChildren(List<ConfigEntry> out, int depth) {
		for (var field : FieldAccess.collectOrderedFields(target.getClass())) {
			if (!ConfigCondition.evaluate(field)) continue;
			if (field.isAnnotationPresent(Category.class)) {
				var childTarget = FieldAccess.getFieldValue(field, target);
				if (childTarget == null) {
					childTarget = FieldAccess.newInstance(field.getType());
					if (childTarget == null) continue;
					FieldAccess.setFieldValue(field, target, childTarget);
				}
				var childDefaults = defaults == null ? null : FieldAccess.getFieldValue(field, defaults);
				var childKey = "%s.%s".formatted(keyPrefix, field.getName());
				var child = new GroupConfigEntry<>(
					tab,
					childTarget,
					childDefaults,
					ConfigTranslation.translatable(childKey),
					"%s.%s".formatted(path, field.getName()),
					childKey,
					lockPath,
					depth * INDENT_PX,
					null,
					null
				);
				out.add(child);
				if (child.isExpanded()) child.appendChildren(out, depth + 1);
				continue;
			}
			for (var entry : EntryTypeRegistry.createValueEntries(tab, fieldNode(field))) {
				if (entry instanceof TabLifecycle lifecycle) lifecycle.onAttachedToTab(tab);
				entry.setIndent(entry.getIndent() + depth * INDENT_PX);
				out.add(entry);
			}
		}
	}
	/** 字段类型只在运行期可知，节点统一按 {@code Object} 构造，条目再按 valueType 分派。 */
	private ValueConfigNode<C, Object> fieldNode(Field field) {
		var rawType = field.getType();
		var isArray = rawType.isArray();
		var valueType = isArray ? List.class : FieldAccess.boxed(rawType);
		var elementType = FieldAccess.elementShape(field);
		Object defaultValue = defaults == null ? null : FieldAccess.getFieldValue(field, defaults);
		if (isArray) defaultValue = FieldAccess.arrayToList(defaultValue);
		Object baseline = FieldAccess.getFieldValue(field, target);
		if (isArray) baseline = FieldAccess.arrayToList(baseline);
		var fieldKey = "%s.%s".formatted(keyPrefix, field.getName());
		ValueReader<C, Object> reader;
		ValueWriter<C, Object> writer;
		if (isArray) {
			var componentType = rawType.getComponentType();
			var holder = new WriteBackList[1];
			var cached = new WriteBackList(
				FieldAccess.arrayToList(FieldAccess.getFieldValue(field, target)),
				() -> FieldAccess.setFieldValue(field, target, FieldAccess.listToArray(componentType, holder[0]))
			);
			holder[0] = cached;
			reader = ignored -> cached;
			writer = (ignored, value) -> cached.replaceContents(value instanceof List<?> values ? values : List.of());
		} else {
			reader = ignored -> FieldAccess.getFieldValue(field, target);
			writer = (ignored, value) -> FieldAccess.setFieldValue(field, target, value);
		}
		return ValueConfigNode.<C, Object>builder()
			.valueType(valueType)
			.field(field)
			.name(field.getName())
			.path("%s.%s".formatted(path, field.getName()))
			.title(ConfigTranslation.translatable(fieldKey))
			.tooltip(ConfigTranslation.translatable(fieldKey + ".tooltip"))
			.defaultValue(defaultValue)
			.elementType(elementType)
			.colorValue(field.getAnnotation(ColorValue.class))
			.sliderRange(FieldValidators.sliderRange(field))
			.validator(FieldValidators.validatorFor(valueType, field))
			.writeThrough(true)
			.element(true)
			.lockPath(lockPath)
			.baselineValue(FieldAccess.deepCopy(baseline))
			.valueReader(reader)
			.valueWriter(writer)
			.build();
	}
	@NotNull
	@Override
	public List<? extends GuiEventListener> children() {
		return deleteButton == null ? List.of(clickWidget) : List.of(clickWidget, deleteButton);
	}
	@NotNull
	@Override
	public List<? extends NarratableEntry> narratables() {
		return deleteButton == null ? List.of(clickWidget) : List.of(clickWidget, deleteButton);
	}
	@Override
	public void render(
		GuiGraphics gui,
		int index,
		int y,
		int x,
		int entryWidth,
		int entryHeight,
		int mouseX,
		int mouseY,
		boolean hovered,
		float delta
	) {
		var indent = getIndent();
		var font = ClientUtil.mc.font;
		var prefix = expanded ? EXPANDED_PREFIX : COLLAPSED_PREFIX;
		gui.drawString(font, prefix, x + indent, y + 5, 0xAAAAAA, true);
		gui.drawString(font, label.getVisualOrderText(), x + indent + font.width(prefix), y + 5, -1, true);
		var clickWidth = entryWidth;
		if (deleteButton != null) {
			deleteButton.active = canDelete == null || canDelete.getAsBoolean();
			deleteButton.setPosition(x + entryWidth - SIZE, y);
			deleteButton.render(gui, mouseX, mouseY, delta);
			clickWidth = entryWidth - SIZE - GAP;
		}
		clickWidget.setRectangle(clickWidth, entryHeight, x, y);
	}
	private class ClickWidget extends AbstractWidget {
		private ClickWidget() {
			super(0, 0, 0, 0, Component.empty());
		}
		@Override
		protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {}
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button != 0 || !isMouseOver(mouseX, mouseY)) return false;
			ClientUtil.mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			toggle();
			return true;
		}
		@NotNull
		@Override
		public NarrationPriority narrationPriority() {
			return NarrationPriority.HOVERED;
		}
		@Override
		public void updateWidgetNarration(@NotNull NarrationElementOutput output) {
			output.add(NarratedElementType.TITLE, label);
		}
	}
}
