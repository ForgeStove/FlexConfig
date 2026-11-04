package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.ConfigTranslation;
import io.github.forgestove.flexconfig.ElementType;
import io.github.forgestove.flexconfig.FieldAccess;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.*;
import io.github.forgestove.flexconfig.client.gui.api.TabLifecycle;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import io.github.forgestove.flexconfig.tree.ValueConfigNode.ValueReader;
import io.github.forgestove.flexconfig.tree.ValueConfigNode.ValueWriter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;
/** 集合的标题行：负责增删元素，元素本身是紧随其后的独立条目。 */
public class CollectionValueConfigEntry<C, E> extends ValueConfigEntry<C, List<E>> {
	private static final Component ADD_LABEL = Component.literal("+");
	private final Button addButton;
	public static <C, E> List<ConfigEntry> create(ConfigCategoryTab<C, List<E>> tab, ValueConfigNode<C, List<E>> node) {
		var entries = new ArrayList<ConfigEntry>();
		var header = new CollectionValueConfigEntry<>(tab, node);
		entries.add(header);
		header.appendElements(entries, 0);
		return entries;
	}
	public CollectionValueConfigEntry(ConfigCategoryTab<C, List<E>> tab, ValueConfigNode<C, List<E>> node) {
		super(tab, node);
		addButton = Button.builder(ADD_LABEL, b -> addElement()).size(SIZE, HEIGHT).build();
		addButton.setTooltip(Tooltip.create(ADD_LABEL));
		children.add(addButton);
	}
	public static @Nullable Object defaultValueOf(Class<?> type) {
		type = FieldAccess.boxed(type);
		if (type == Boolean.class) return false;
		if (type == Integer.class) return 0;
		if (type == Long.class) return 0L;
		if (type == Float.class) return 0f;
		if (type == Double.class) return 0d;
		if (type == Byte.class) return (byte) 0;
		if (type == Short.class) return (short) 0;
		if (type == Character.class) return '\0';
		if (type == String.class) return "";
		if (type.isEnum()) {
			var constants = type.getEnumConstants();
			return constants.length == 0 ? null : constants[0];
		}
		return FieldAccess.newInstance(type);
	}
	public List<E> values() {
		var value = node.getEditingValue(tab.config);
		if (value != null) return value;
		var empty = new ArrayList<E>();
		node.setEditingValue(empty);
		return empty;
	}
	public @Nullable ElementType elementShape() {
		return node.getElementType();
	}
	/** 列表选项的翻译键前缀，元素键在其后追加 {@code .<元素类名>}。 */
	public String optionKey() {
		return "%s.config.option.%s".formatted(tab.screen.root.modId(), node.getPath());
	}
	/** 与当前索引对齐的默认元素；越界时按类型构造空值。 */
	public @Nullable Object defaultElement(int index) {
		var defaults = node.getDefaultValue();
		if (defaults != null && index < defaults.size()) return FieldAccess.deepCopy(defaults.get(index));
		var shape = elementShape();
		// 嵌套集合没有统一的「空值类型」，越界时由调用方给空表/空数组
		return shape == null || shape.isCollection() ? null : defaultValueOf(shape.raw());
	}
	@SuppressWarnings("unchecked")
	private E asElement(Object value) {
		return (E) value;
	}
	private void addElement() {
		var shape = elementShape();
		if (shape == null || isReadOnly()) return;
		var template = defaultElement(0);
		var element = shape.isCollection()
			? newNestedElement(shape)
			: shape.raw().isInstance(template) ? FieldAccess.deepCopy(template) : defaultValueOf(shape.raw());
		if (element == null) return;
		var list = values();
		list.add(asElement(element));
		node.setEditingValue(list);
		tab.rebuildEntries();
	}
	/** 新元素本身是集合：列表用空表，数组按组件类型造空数组。 */
	private Object newNestedElement(ElementType shape) {
		return shape.isArray() ? FieldAccess.listToArray(shape.raw().getComponentType(), List.of()) : new ArrayList<>();
	}
	public void removeElement(int index) {
		if (isReadOnly()) return;
		var list = values();
		if (index < 0 || index >= list.size()) return;
		tab.screen.getHandler().shiftElementKeybinds(node.getPath(), index);
		list.remove(index);
		node.setEditingValue(list);
		tab.rebuildEntries();
	}
	/** 单个简单元素的编辑节点；值直接读写集合本身，保存时再由列表节点整体写回配置。 */
	@SuppressWarnings("unchecked")
	public ValueConfigNode<C, E> elementNode(int index, Class<?> elementType) {
		var list = values();
		var baseline = index < list.size() ? FieldAccess.deepCopy(list.get(index)) : null;
		return ValueConfigNode.<C, E>builder()
			.valueType((Class<? extends E>) FieldAccess.boxed(elementType))
			.name("%s[%d]".formatted(node.getName(), index))
			.path("%s[%d]".formatted(node.getPath(), index))
			.title(node.getTitle().copy().append(" #" + (index + 1)))
			.defaultValue((E) defaultElement(index))
			.valueReader(ignored -> index < list.size() ? list.get(index) : null)
			.valueWriter((ignored, value) -> {
				if (index < list.size()) list.set(index, value);
			})
			.writeThrough(true)
			.element(true)
			.lockPath(node.getLockPath())
			.baselineValue(baseline)
			.build();
	}
	/** 嵌套集合元素的节点：值用 List 视图，数组元素通过回写列表同步到原数组。 */
	private ValueConfigNode<C, Object> nestedElementNode(int index, ElementType shape) {
		var list = values();
		var raw = shape.raw();
		var array = raw.isArray();
		Object defaultValue = defaultElement(index);
		if (defaultValue != null && defaultValue.getClass().isArray()) defaultValue = FieldAccess.arrayToList(defaultValue);
		ValueReader<C, Object> reader;
		ValueWriter<C, Object> writer;
		Object baseline;
		if (array) {
			var componentType = raw.getComponentType();
			var holder = new WriteBackList[1];
			var cached = new WriteBackList(
				FieldAccess.arrayToList(list.get(index)),
				() -> list.set(index, asElement(FieldAccess.listToArray(componentType, holder[0])))
			);
			holder[0] = cached;
			reader = ignored -> cached;
			writer = (ignored, value) -> cached.replaceContents(value instanceof List<?> values ? values : List.of());
			baseline = FieldAccess.deepCopy(cached);
		} else {
			reader = ignored -> list.get(index);
			writer = (ignored, value) -> list.set(index, asElement(value));
			baseline = FieldAccess.deepCopy(list.get(index));
		}
		if (defaultValue == null) defaultValue = new ArrayList<>();
		return ValueConfigNode.<C, Object>builder()
			.valueType(List.class)
			.name("%s[%d]".formatted(node.getName(), index))
			.path("%s[%d]".formatted(node.getPath(), index))
			.title(node.getTitle().copy().append(" #" + (index + 1)))
			.defaultValue(defaultValue)
			.elementType(shape.element())
			.writeThrough(true)
			.element(true)
			.lockPath(node.getLockPath())
			.baselineValue(baseline)
			.valueReader(reader)
			.valueWriter(writer)
			.build();
	}
	/** 依次生成所有元素行：简单值一行、对象元素可展开、嵌套集合再生成一组内部条目。 */
	public void appendElements(List<ConfigEntry> out, int depth) {
		var shape = elementShape();
		if (shape == null) return;
		var list = values();
		for (var i = 0; i < list.size(); i++) {
			var position = i;
			if (shape.isCollection()) {
				var inner = new ArrayList<>(EntryTypeRegistry.createValueEntries(tab, nestedElementNode(i, shape)));
				for (var entry : inner) {
					if (entry instanceof TabLifecycle lifecycle) lifecycle.onAttachedToTab(tab);
					entry.setIndent(entry.getIndent() + (depth + 1) * INDENT_PX);
				}
				if (!inner.isEmpty()) addDeleteButton(inner.get(0), i);
				out.addAll(inner);
				continue;
			}
			if (shape.isSimple()) {
				for (var entry : EntryTypeRegistry.createValueEntries(tab, elementNode(i, shape.raw()))) {
					if (entry instanceof TabLifecycle lifecycle) lifecycle.onAttachedToTab(tab);
					entry.setIndent(entry.getIndent() + (depth + 1) * INDENT_PX);
					addDeleteButton(entry, i);
					out.add(entry);
				}
				continue;
			}
			var element = list.get(i);
			var elementKey = "%s.%s".formatted(optionKey(), element.getClass().getSimpleName());
			var elementPath = "%s[%d]".formatted(node.getPath(), i);
			var group = new GroupConfigEntry<>(
				tab,
				element,
				defaultElement(i),
				ConfigTranslation.translatable(elementKey),
				elementPath,
				elementKey,
				node.getLockPath(),
				(depth + 1) * INDENT_PX,
				() -> removeElement(position),
				() -> !isReadOnly()
			);
			out.add(group);
			if (group.isExpanded()) group.appendChildren(out, depth + 2);
		}
	}
	private void addDeleteButton(ConfigEntry entry, int index) {
		if (!(entry instanceof ValueConfigEntry<?, ?> valueEntry)) return;
		var deleteButton = Button.builder(Component.literal("-"), b -> removeElement(index)).size(SIZE, HEIGHT).build();
		deleteButton.setTooltip(Tooltip.create(Component.literal("-")));
		valueEntry.children.add(deleteButton);
		valueEntry.extraWidgets.add(deleteButton);
	}
	@Override
	public void resetToDefault() {
		tab.screen.getHandler().clearElementKeybinds(node.getPath());
		super.resetToDefault();
		tab.rebuildEntries();
	}
	@Override
	public void resetToActive() {
		tab.screen.getHandler().clearElementKeybinds(node.getPath());
		super.resetToActive();
		tab.rebuildEntries();
	}
	@Override
	public void refresh() {
		super.refresh();
		addButton.active = !isReadOnly() && elementShape() != null;
	}
	@Override
	public void render(
		GuiGraphics gui,
		int index,
		int y,
		int x,
		int width,
		int height,
		int mouseX,
		int mouseY,
		boolean hovering,
		float delta
	) {
		addButton.active = !isReadOnly() && elementShape() != null;
		renderGui(gui, y, x, width, mouseX, mouseY, delta, lockButton, undoButton, resetButton, addButton);
		var count = Component.literal(String.valueOf(values().size())).withStyle(ChatFormatting.GRAY);
		var countX = x + width - SIZE - GAP - ClientUtil.mc.font.width(count) - GAP;
		if (countX > x + getIndent() + 40) gui.drawString(ClientUtil.mc.font, count, countX, y + 6, -1, false);
	}
}
