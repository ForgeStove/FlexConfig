package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.gui.*;
import io.github.forgestove.flexconfig.client.gui.components.*;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;
public final class ColorValueConfigEntry<C> extends ValueConfigEntry<C, Integer> {
	public static final Pattern HEX_PATTERN = Pattern.compile("[0-9A-Fa-f]*");
	public final FlexEditBox inputField;
	public final FlexColorPickerButton previewWidget;
	public final boolean hasAlpha;
	public ColorValueConfigEntry(ConfigCategoryTab<C, Integer> tab, ValueConfigNode<C, Integer> node) {
		super(tab, node);
		hasAlpha = node.colorHasAlpha();
		previewWidget = new FlexColorPickerButton(0, 0, SIZE, SIZE, hasAlpha, this::getValue, this::openColorPicker);
		previewWidget.setTooltip(Tooltip.create(Translation.COLOR_PICKER_TOOLTIP));
		inputField = new FlexEditBox(ClientUtil.mc.font, 0, 0, WIDTH - SIZE - GAP, HEIGHT, this.node.getTitle());
		inputField.setMaxLength(getMaxLength());
		inputField.setValue(formatColor(getValue()));
		inputField.setFilter(s -> HEX_PATTERN.matcher(s).matches());
		inputField.setResponder(this::onInputChange);
		children.add(previewWidget);
		children.add(inputField);
	}
	private int getMaxLength() {
		return hasAlpha ? 8 : 6;
	}
	private String formatColor(int color) {
		return String.format(hasAlpha ? "%08X" : "%06X", color);
	}
	private void onInputChange(String value) {
		var maxLength = getMaxLength();
		if (value.length() != maxLength) return;
		try {
			setValue(Integer.parseUnsignedInt(value, 16));
		} catch (NumberFormatException ignored) {}
	}
	private void openColorPicker() {
		var mc = ClientUtil.mc;
		var screen = new ColorPickerScreen(mc.screen, getValue(), hasAlpha, this::accept);
		mc.setScreen(screen);
	}
	private void accept(int newColor) {
		setValue(newColor);
		inputField.setValue(formatColor(newColor));
	}
	@Override
	public void refresh() {
		var maxLength = getMaxLength();
		var isValid = node.validate(tab.config) == null && inputField.getValue().length() == maxLength;
		if (isValid) {
			var valueStr = formatColor(getValue());
			if (!inputField.isFocused() && !inputField.getValue().equalsIgnoreCase(valueStr)) inputField.setValue(valueStr);
			inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY));
		} else inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY.withColor(ChatFormatting.RED)));
		super.refresh();
	}
	@Override
	public void render(
		@NotNull GuiGraphics gui,
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
		var readOnly = isReadOnly();
		previewWidget.active = !readOnly;
		inputField.active = !readOnly;
		renderGui(gui, y, x, width, mouseX, mouseY, delta, lockButton, undoButton, resetButton, inputField, previewWidget);
	}
}
