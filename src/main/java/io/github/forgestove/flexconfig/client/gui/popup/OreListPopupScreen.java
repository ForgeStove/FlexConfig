package io.github.forgestove.flexconfig.client.gui.popup;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import io.github.forgestove.flexconfig.client.gui.components.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.*;
/** 取值用的列表弹窗：一列可滚动的文字项，选中即回调，然后关掉自己回父界面。枚举下拉用它。 */
public class OreListPopupScreen<T> extends OrePopupScreen {
	private final OreList<T> list;
	private final Consumer<T> onSelect;
	public OreListPopupScreen(
		@NotNull Screen parent,
		@NotNull List<T> options,
		@NotNull Function<T, Component> display,
		@NotNull Supplier<T> current,
		@NotNull Consumer<T> onSelect
	) {
		super(parent, null);
		this.onSelect = onSelect;
		list = new OreList<>(options, display, current);
	}
	/** 行高。 */
	public void rowHeight(int rowHeight) {
		list.rowHeight(rowHeight);
	}
	/** 面板底用输入框那张图。 */
	@Override
	protected void drawPanel(@NotNull GuiGraphics gui) {
		FlexGuiTextures.FIELD_NORMAL.render(gui, panelX, panelY, panelWidth, panelHeight);
	}
	@Override
	protected int contentWidth() {
		list.height(maxListHeight());   // 先算可见行数，才知道要不要给滚动条留位
		return list.width(font) + (list.scrolling() ? list.scrollbarWidth() : 0);
	}
	@Override
	protected int contentHeight() {
		return list.pixelHeight(maxListHeight());
	}
	/** 列表可用高：下方放得下就向下弹，放不下就用上方空间，两侧都放不下取大的那侧。 */
	private int maxListHeight() {
		if (anchorBottom() < 0) return height - PAD * 2;
		var space = list.naturalHeight() <= spaceBelow() ? spaceBelow() : Math.max(spaceAbove(), list.itemHeight());
		return Math.clamp(space, list.itemHeight(), height - PAD * 2);
	}
	@Override
	protected void layoutContent() {
		list.area(contentX(), contentY(), contentW(), contentH());
	}
	@Override
	protected void init() {
		super.init();
		list.centerCurrent();
	}
	@Override
	protected void renderContent(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		list.render(gui, font, mouseX, mouseY, partialTick);
		if (list.hovering(mouseX, mouseY)) OreCursor.hand();
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isOutsidePanel(mouseX, mouseY)) return super.mouseClicked(mouseX, mouseY, button);
		if (button == 0) list.mousePressed(mouseX, mouseY);
		return true;
	}
	/** 松手才触发：松开时鼠标还得落在同一项上（同 {@link OreButton} 那套）。 */
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		var picked = list.armed();
		list.release();
		if (picked != null && picked.equals(list.optionAt(mouseX, mouseY))) {
			OreList.playClick();
			// 先关（回父界面）再执行选中回调：回调里可以直接跳到别的界面，不会被 onClose 覆盖
			onClose();
			onSelect.accept(picked);
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (list.hovering(mouseX, mouseY)) list.wheel(vertical);
		return true;
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) onClose();
		return false;
	}
}
