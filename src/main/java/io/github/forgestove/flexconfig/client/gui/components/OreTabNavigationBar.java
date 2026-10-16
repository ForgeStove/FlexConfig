package io.github.forgestove.flexconfig.client.gui.components;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.*;
import net.minecraft.client.gui.components.tabs.*;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.*;
import net.minecraft.client.gui.navigation.*;
import net.minecraft.client.gui.navigation.FocusNavigationEvent.TabNavigation;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.*;

import java.util.*;
/**
 * 替代原版 {@code TabNavigationBar}：原版构造为包私有且按钮由它内部创建，无法单独替换按钮。切换逻辑仍走
 * 原版 {@link TabManager}，这里只负责摆放、事件与键盘。
 */
public class OreTabNavigationBar extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
	private static final int MAX_WIDTH = 400;
	private static final int MARGIN_X = 14;
	/** 列表顶部分隔线占的高度，栏底要给它留出来，tab 才正好坐在线上。 */
	private static final int SEPARATOR_HEIGHT = 2;
	private final TabManager tabManager;
	private final List<Tab> tabs;
	private final List<OreTabButton> buttons = new ArrayList<>();
	private final LinearLayout layout = LinearLayout.horizontal();
	/** 本次按下落在的 tab，松手仍在其上才切换。 */
	@Nullable private OreTabButton armedTab;
	private int width;
	public OreTabNavigationBar(@NotNull TabManager tabManager, @NotNull List<? extends Tab> tabs, int width) {
		this.tabManager = tabManager;
		this.tabs = List.copyOf(tabs);
		this.width = width;
		layout.defaultCellSetting().alignHorizontallyCenter();
		for (var tab : this.tabs)
			buttons.add(layout.addChild(new OreTabButton(tabManager, tab, 0, OreTabButton.preferredHeight())));
	}
	/** 屏幕只转发 keyCode，原版栏同样是这个一参入口。 */
	public boolean keyPressed(int keyCode) {
		return keyPressed(keyCode, 0, 0);
	}
	private int nextTabIndex(int keyCode) {
		if (keyCode >= 49 && keyCode <= 57) return keyCode - 49;
		if (keyCode != 258) return -1;
		var step = Screen.hasShiftDown() ? -1 : 1;
		return Mth.positiveModulo(tabs.indexOf(tabManager.getCurrentTab()) + step, tabs.size());
	}
	public void selectTab(int index, boolean playSound) {
		if (isFocused()) setFocused(buttons.get(index));
		else tabManager.setCurrentTab(tabs.get(index), playSound);
	}
	/** 焦点落到某个 tab 按钮上即切换该 tab。 */
	@Override
	public void setFocused(@Nullable GuiEventListener listener) {
		super.setFocused(listener);
		if (listener instanceof OreTabButton button) tabManager.setCurrentTab(button.tab(), true);
	}
	public void setWidth(int width) {
		this.width = width;
	}
	@Override
	public @NotNull List<OreTabButton> children() {
		return buttons;
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		var hit = getChildAt(mouseX, mouseY);
		if (hit.isEmpty()) return false;
		var target = hit.get();
		if (!target.mouseClicked(mouseX, mouseY, button)) return false;
		armedTab = target instanceof OreTabButton tabButton ? tabButton : null;
		return true;
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		var target = armedTab;
		armedTab = null;
		if (target == null) return false;
		target.mouseReleased(mouseX, mouseY, button);
		if (target.isMouseOver(mouseX, mouseY)) tabManager.setCurrentTab(target.tab(), true);
		return true;
	}
	/** Ctrl+数字选第 n 个、Ctrl+Tab / Ctrl+Shift+Tab 前后循环。 */
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!Screen.hasControlDown()) return false;
		var index = nextTabIndex(keyCode);
		if (index == -1) return false;
		selectTab(Mth.clamp(index, 0, tabs.size() - 1), true);
		return true;
	}
	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		var current = getFocused();
		if (!focused && current != null) current.setFocused(false);
	}
	@Override
	public @Nullable ComponentPath nextFocusPath(@NotNull FocusNavigationEvent event) {
		var current = currentTabButton();
		if (!isFocused() && current != null) return ComponentPath.path(this, ComponentPath.leaf(current));
		return event instanceof TabNavigation ? null : super.nextFocusPath(event);
	}
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return getRectangle().containsPoint((int) mouseX, (int) mouseY);
	}
	@Override
	public @NotNull ScreenRectangle getRectangle() {
		var rect = layout.getRectangle();
		return new ScreenRectangle(rect.left(), rect.top(), rect.width(), rect.height() + SEPARATOR_HEIGHT);
	}
	public void arrangeElements() {
		var usable = Math.min(MAX_WIDTH, width) - 2 * MARGIN_X;
		var each = Mth.roundToward(usable / tabs.size(), 2);   // 同原版：等分、向下取偶
		buttons.forEach(button -> button.setWidth(each));
		layout.arrangeElements();
		layout.setX(Mth.roundToward((width - usable) / 2, 2));
		layout.setY(0);
	}
	private @Nullable OreTabButton currentTabButton() {
		for (var button : buttons) if (button.isSelected()) return button;
		return null;
	}
	@Override
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		OreTabButton focused = null;
		for (var button : buttons)
			if (button.isFocused()) focused = button;
			else button.render(gui, mouseX, mouseY, partialTick);
		// 聚焦的那个最后画：环向左右各外扩 1px，先画会被右邻盒子覆盖
		if (focused != null) focused.render(gui, mouseX, mouseY, partialTick);
	}
	@Override
	public @NotNull NarrationPriority narrationPriority() {
		return buttons.stream().map(AbstractWidget::narrationPriority).max(NarrationPriority::compareTo).orElse(NarrationPriority.NONE);
	}
	@Override
	public void updateNarration(@NotNull NarrationElementOutput narration) {
		buttons.forEach(button -> button.updateNarration(narration.nest()));
	}
}
