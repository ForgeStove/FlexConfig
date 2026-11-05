package io.github.forgestove.flexconfig.client.gui.components;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.forgestove.flexconfig.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
/**
 * Cloth 式内联下拉：顶部是可输入的单元格，展开的菜单在本行下方绘制，由条目在 {@code lateRender}
 * 里最后画出来，因此能盖住后续条目；展开高度通过 {@code getMorePossibleHeight} 计入列表滚动范围。
 */
public class InlineDropdown<T> {
	private static final int ROW_HEIGHT = 20, SCROLLBAR_WIDTH = 6;
	private static final int BORDER = 0xFFFFFFFF, SURFACE = 0xFF000000;
	private static final int HOVER = 0xFF1E1E1E, TEXT = 0xFFFFFFFF, TEXT_DIM = 0xFF888888;
	private static final int HIGHLIGHT = 0xFFFF55, SELECTED = 0x55FF55;
	private static final ResourceLocation SCROLLER = ResourceLocation.withDefaultNamespace("widget/scroller");
	@Nullable private static InlineDropdown<?> openDropdown;
	private final TopCell topCell;
	private final List<T> options;
	private final Supplier<T> current;
	private final Function<T, Component> text;
	private final Consumer<T> onSelect;
	private final IntSupplier listTop;
	private final IntSupplier listBottom;
	private final List<T> filtered = new ArrayList<>();
	private boolean expanded;
	private boolean updating;
	private double scroll, target;
	private int highlighted = -1;
	private int menuX, menuY, menuWidth, menuHeight;
	public InlineDropdown(
		int width,
		int height,
		List<T> options,
		Supplier<T> current,
		Function<T, Component> text,
		Consumer<T> onSelect,
		IntSupplier listTop,
		IntSupplier listBottom
	) {
		this.options = options;
		this.current = current;
		this.text = text;
		this.onSelect = onSelect;
		this.listTop = listTop;
		this.listBottom = listBottom;
		topCell = new TopCell(this, width, height);
		topCell.setResponder(value -> onTextChanged());
		updateText();
	}
	public static boolean isAnyOpen() {
		return openDropdown != null;
	}
	public static void closeOpen() {
		if (openDropdown != null) openDropdown.closeDropdown();
	}
	public static void closeOpenIfOutside(double mouseX, double mouseY) {
		var open = openDropdown;
		if (open == null) return;
		if (open.isMouseOverMenu(mouseX, mouseY) || open.topCell.isMouseOver(mouseX, mouseY)) return;
		open.closeDropdown();
	}
	public TopCell topCell() {
		return topCell;
	}
	public void refresh() {
		if (!expanded) updateText();
	}
	/** 列表计算滚动范围用；未展开时没有额外高度。 */
	public int extraHeight() {
		return expanded ? menuHeight() : -1;
	}
	public boolean expanded() {
		return expanded;
	}
	public boolean isMouseOverMenu(double mouseX, double mouseY) {
		return expanded && mouseX >= menuX && mouseX < menuX + menuWidth && mouseY >= menuY && mouseY < menuY + menuHeight;
	}
	public boolean menuClicked(double mouseX, double mouseY, int button) {
		if (!isMouseOverMenu(mouseX, mouseY)) return false;
		var scrolling = filtered.size() * ROW_HEIGHT > menuHeight;
		if (button != 0 || scrolling && mouseX >= menuX + menuWidth - SCROLLBAR_WIDTH) return true;
		var row = (int) ((mouseY - menuY - 1 + scroll) / ROW_HEIGHT);
		if (row >= 0 && row < filtered.size()) select(filtered.get(row));
		return true;
	}
	public boolean menuScrolled(double mouseX, double mouseY, double vertical) {
		if (!isMouseOverMenu(mouseX, mouseY)) return false;
		target = Mth.clamp(target - vertical * ROW_HEIGHT * 2, 0, maxScroll());
		return true;
	}
	public void closeDropdown() {
		close(true);
		topCell.setFocused(false);
	}
	public void lateRender(GuiGraphics gui, int mouseX, int mouseY, float delta) {
		if (!expanded) return;
		menuHeight = menuHeight();
		var below = listBottom.getAsInt() - (topCell.getY() + topCell.getHeight()) - 2;
		var above = topCell.getY() - listTop.getAsInt() - 2;
		menuY = below >= menuHeight || below >= above ? topCell.getY() + topCell.getHeight() : topCell.getY() - menuHeight;
		var font = ClientUtil.mc.font;
		var widest = 0;
		for (var value : filtered) widest = Math.max(widest, font.width(text.apply(value)));
		var screenWidth = ClientUtil.mc.getWindow().getGuiScaledWidth();
		menuWidth = Mth.clamp(Math.max(topCell.getWidth(), widest + 12), topCell.getWidth(), Math.max(topCell.getWidth(), screenWidth - 4));
		menuX = Mth.clamp(topCell.getX() + topCell.getWidth() - menuWidth, 2, Math.max(2, screenWidth - menuWidth - 2));
		var max = maxScroll();
		scroll = Mth.clamp(scroll, 0, max);
		target = Mth.clamp(target, 0, max);
		scroll = Mth.lerp((float) Math.min(1.0, delta * 0.5), scroll, target);
		if (Math.abs(target - scroll) < 0.5) scroll = target;
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, 200);   // 菜单总是压在本行以下的内容之上
		gui.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, BORDER);
		gui.fill(menuX + 1, menuY + 1, menuX + menuWidth - 1, menuY + menuHeight - 1, SURFACE);
		var scrolling = filtered.size() * ROW_HEIGHT > menuHeight;
		var contentWidth = menuWidth - 2 - (scrolling ? SCROLLBAR_WIDTH : 0);
		var currentValue = current.get();
		gui.enableScissor(menuX + 1, menuY + 1, menuX + menuWidth - 1, menuY + menuHeight - 1);
		for (var i = 0; i < filtered.size(); i++) {
			var rowY = (int) (menuY + 1 + i * ROW_HEIGHT - scroll);
			if (rowY + ROW_HEIGHT < menuY + 1 || rowY > menuY + menuHeight - 1) continue;
			var hovered = mouseX >= menuX + 1 && mouseX < menuX + 1 + contentWidth && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
			if (hovered) gui.fill(menuX + 1, rowY, menuX + 1 + contentWidth, rowY + ROW_HEIGHT, HOVER);
			var value = filtered.get(i);
			var color = i == highlighted ? HIGHLIGHT : Objects.equals(value, currentValue) ? SELECTED : hovered ? TEXT : TEXT_DIM;
			var label = font.plainSubstrByWidth(text.apply(value).getString(), Math.max(1, contentWidth - 8));
			gui.drawString(font, label, menuX + 4, rowY + 6, color, false);
		}
		gui.disableScissor();
		if (scrolling) {
			var knobHeight = Math.max(10, (menuHeight - 2) * (menuHeight - 2) / (filtered.size() * ROW_HEIGHT));
			var knobY = menuY + 1 + (int) ((menuHeight - 2 - knobHeight) * (scroll / max));
			gui.blitSprite(SCROLLER, menuX + menuWidth - SCROLLBAR_WIDTH - 1, knobY, SCROLLBAR_WIDTH, knobHeight);
		}
		pose.popPose();
	}
	/** 菜单高度按列表上下两侧里较大的可用空间算，不再固定 7 行；下方放不下时自动向上展开。 */
	private int menuHeight() {
		var content = Math.max(ROW_HEIGHT, filtered.size() * ROW_HEIGHT);
		var below = listBottom.getAsInt() - (topCell.getY() + topCell.getHeight()) - 2;
		var above = topCell.getY() - listTop.getAsInt() - 2;
		var available = Math.max(ROW_HEIGHT, Math.max(below, above));
		var maxHeight = available / ROW_HEIGHT * ROW_HEIGHT;
		return Math.min(content, maxHeight);
	}
	private int maxScroll() {
		return Math.max(0, filtered.size() * ROW_HEIGHT - menuHeight);
	}
	private void open() {
		if (!topCell.active || expanded) return;
		if (openDropdown != null && openDropdown != this) openDropdown.closeDropdown();
		openDropdown = this;
		expanded = true;
		updating = true;
		try {
			topCell.setValue("");
		} finally {
			updating = false;
		}
		updateFiltered();
		highlighted = indexOf(filtered, current.get());
		var max = maxScroll();
		scroll = target = Mth.clamp((highlighted < 0 ? 0 : highlighted * ROW_HEIGHT) - (menuHeight - ROW_HEIGHT) / 2.0, 0, max);
	}
	private void close(boolean restore) {
		if (!expanded) return;
		expanded = false;
		if (openDropdown == this) openDropdown = null;
		scroll = target = 0;
		highlighted = -1;
		if (restore) updateText();
	}
	private void select(T value) {
		onSelect.accept(value);
		close(false);
		updateText();
		topCell.setFocused(false);
	}
	private void onTextChanged() {
		if (updating) return;
		if (!expanded) open();
		updateFiltered();
		highlighted = filtered.isEmpty() ? -1 : 0;
		scroll = target = 0;
	}
	private void updateFiltered() {
		var query = topCell.getValue().trim().toLowerCase(Locale.ROOT);
		filtered.clear();
		for (var value : options)
			if (query.isEmpty() || text.apply(value).getString().toLowerCase(Locale.ROOT).contains(query)) filtered.add(value);
	}
	private void updateText() {
		var value = current.get();
		updating = true;
		try {
			topCell.setValue(value == null ? "" : text.apply(value).getString());
		} finally {
			updating = false;
		}
	}
	private static <T> int indexOf(List<T> values, T value) {
		for (var i = 0; i < values.size(); i++) if (Objects.equals(values.get(i), value)) return i;
		return -1;
	}
	private void move(int direction) {
		if (filtered.isEmpty()) return;
		highlighted = Mth.clamp(
			highlighted < 0 ? direction > 0 ? 0 : filtered.size() - 1 : highlighted + direction,
			0,
			filtered.size() - 1
		);
		var top = highlighted * ROW_HEIGHT;
		var visible = menuHeight();
		if (top < target) target = top;
		else if (top + ROW_HEIGHT > target + visible) target = top + ROW_HEIGHT - visible;
		target = Mth.clamp(target, 0, maxScroll());
	}
	private void selectHighlighted() {
		if (filtered.isEmpty()) {
			close(true);
			return;
		}
		select(filtered.get(Math.max(highlighted, 0)));
	}
	public static class TopCell extends FlexEditBox {
		private final InlineDropdown<?> owner;
		private TopCell(InlineDropdown<?> owner, int width, int height) {
			super(ClientUtil.mc.font, 0, 0, width, height, Component.empty());
			this.owner = owner;
			setMaxLength(256);
		}
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (!active || button != 0) return false;
			var result = super.mouseClicked(mouseX, mouseY, button);
			if (result) owner.open();
			return result;
		}
		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			if (!active) return false;
			if (owner.expanded) {
				if (keyCode == InputConstants.KEY_DOWN) {
					owner.move(1);
					return true;
				}
				if (keyCode == InputConstants.KEY_UP) {
					owner.move(-1);
					return true;
				}
				if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
					owner.selectHighlighted();
					return true;
				}
				if (keyCode == InputConstants.KEY_ESCAPE) {
					owner.close(true);
					return true;
				}
			}
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
		@Override
		public boolean charTyped(char codePoint, int modifiers) {
			if (!active) return false;
			if (!owner.expanded) owner.open();
			return super.charTyped(codePoint, modifiers);
		}
	}
}
