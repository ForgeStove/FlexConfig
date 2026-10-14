package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
/**
 * 一列可滚动的文字项：悬停铺灰底，当前项左侧画选中标记，滚动是平滑的、也支持拖动。
 * <p>
 * 只管画和拾取，选中后要做什么（关弹窗、跳界面）由调用方决定。
 */
public final class OreList<T> {
	/** 悬停底色与文字色。 */
	private static final int ROW_HOVER = 0xFF56585B, TEXT = 0xFFFFFFFF;
	/** 选中标记与文字之间的间距。 */
	private static final int ICON_GAP = 4;
	/** 行间距、行内左右留白。 */
	private static final int ROW_GAP = 1, CELL_PAD = 5;
	/** 行高；可用 {@link #rowHeight} 改。 */
	public static final int DEFAULT_ROW_H = 14;
	private int rowH = DEFAULT_ROW_H;
	private final List<T> options;
	private final Function<T, Component> display;
	/** 当前项，在列表里画成选中。返回 {@code null} 表示没有当前项。 */
	private final Supplier<T> current;
	private final OreScrollbar scrollbar = new OreScrollbar();
	private int x, y, width, height;
	/** 按下时所在的那一项，松手即清；画按下态用它。 */
	@Nullable private T armed;
	/** 一屏能放下的行数，由 {@link #height} 算出。 */
	private int visible;
	public OreList(List<T> options, Function<T, Component> display, Supplier<T> current) {
		this.options = options;
		this.display = display;
		this.current = current;
		scrollbar.step(itemHeight());
	}
	/** 需要的宽度。 */
	public int width(Font font) {
		var widest = 0;
		for (var option : options) widest = Math.max(widest, font.width(display.apply(option)));
		return widest + CELL_PAD * 2 + markWidth() + ICON_GAP;
	}
	/** 选中标记的宽度（随资源包变）。 */
	private int markWidth() {
		return FlexGuiTextures.CHECKBOX_ON.width();
	}
	/** 选中标记：选中 / 未选中 × 常态 / 悬停 / 按下。 */
	private static FlexGuiTextures mark(boolean selected, boolean hovered, boolean pressed) {
		if (selected) return pressed ? FlexGuiTextures.CHECKBOX_ON_PRESSED : hovered ? FlexGuiTextures.CHECKBOX_ON_HOVER :
			FlexGuiTextures.CHECKBOX_ON;
		return pressed ? FlexGuiTextures.CHECKBOX_OFF_PRESSED : hovered ? FlexGuiTextures.CHECKBOX_OFF_HOVER :
			FlexGuiTextures.CHECKBOX_OFF;
	}
	/** 一次算完并给出像素高：省得调用方再复刻一遍「行数 × 行高」这笔账。 */
	public int pixelHeight(int maxHeight) {
		return height(maxHeight) * itemHeight();
	}
	/** 全部选项排下来要高多少，不受可视区限制。 */
	public int naturalHeight() {
		return options.size() * itemHeight();
	}
	/** 需要的可见行数（受 {@code maxHeight} 限制）；顺带记下这次算出的值。 */
	public int height(int maxHeight) {
		visible = Math.clamp(options.size(), 1, Math.max(1, maxHeight / itemHeight()));
		return visible;
	}
	/** 有内容超出可视区。只有这时才画滚动条，也才该出手型光标。 */
	public boolean scrolling() {
		return options.size() > visible;
	}
	/** 把当前项摆到列表中间，打开下拉时用。 */
	public void centerCurrent() {
		var index = options.indexOf(current.get());
		if (index > 0) scrollbar.seek(index * itemHeight() - visible * itemHeight() / 2.0);
	}
	/** 行高。改完顺带给滚轮同步步长。 */
	public void rowHeight(int rowH) {
		this.rowH = rowH;
		scrollbar.step(itemHeight());
	}
	/** 一行占的总高 = 行高 + 行间距。 */
	public int itemHeight() {
		return rowH + ROW_GAP;
	}
	/** 摆到指定矩形里；高要先用 {@link #height} 问过。 */
	public void area(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		scrollbar.area(x + width - scrollbarWidth(), y, height, options.size() * itemHeight());
	}
	/** 滚动条的宽度，由滑块精灵决定（随资源包变）。 */
	public int scrollbarWidth() {
		return FlexGuiTextures.SCROLLBAR.width();
	}
	/** 每帧调用：喂当前鼠标坐标（拖动落位）、推进平滑，然后画行与滚动条。 */
	public void render(GuiGraphics gui, Font font, int mouseX, int mouseY, float partialTick) {
		scrollbar.dragTo(mouseY);
		scrollbar.update();
		var scroll = scrollbar.scroll();
		var currentValue = current.get();
		gui.enableScissor(x, y, x + width, y + height);
		for (var i = 0; i < options.size(); i++) {
			var rowY = (int) Math.round(y + i * itemHeight() - scroll);
			if (rowY + rowH < y || rowY > y + height) continue;
			var option = options.get(i);
			var selected = Objects.equals(option, currentValue);
			var hovered = isOver(mouseX, mouseY, rowY);
			if (hovered) gui.fill(x, rowY, x + width, rowY + rowH, ROW_HOVER);
			var mark = mark(selected, hovered, Objects.equals(armed, option));
			mark.render(gui, x + CELL_PAD, rowY + (rowH - mark.height()) / 2);
			gui.drawString(
				font,
				display.apply(option).getVisualOrderText(),
				x + CELL_PAD + mark.width() + ICON_GAP,
				rowY + (rowH - 8) / 2,
				TEXT,
				true
			);
		}
		gui.disableScissor();
		scrollbar.render(gui, mouseX, mouseY, partialTick);
	}
	private boolean isOver(double mouseX, double mouseY, int rowY) {
		return mouseX >= x && mouseX < x + width && mouseY >= rowY && mouseY < rowY + rowH;
	}
	/** 按下：点在滚动条上就开始拖动，点在某一行上就记下那一项。 */
	public void mousePressed(double mouseX, double mouseY) {
		if (scrollbar.mousePressed(mouseX, mouseY)) return;
		armed = optionAt(mouseX, mouseY);
	}
	/** 按下时所在的那一项；没按下或按在空白处返回 {@code null}。 */
	public @Nullable T armed() {
		return armed;
	}
	/** 鼠标所在行的那一项，不改任何状态。 */
	public @Nullable T optionAt(double mouseX, double mouseY) {
		var index = rowAt(mouseX, mouseY);
		return index < 0 ? null : options.get(index);
	}
	/** 播一次按钮点击音效（行是自绘的，不像原版按钮那样自动播）。 */
	public static void playClick() {
		ClientUtil.mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
	/** @return 鼠标所在行号；不在列表区内返回 -1。 */
	private int rowAt(double mouseX, double mouseY) {
		if (!hovering(mouseX, mouseY)) return -1;
		var index = (int) ((mouseY - y + scrollbar.scroll()) / itemHeight());
		return index >= 0 && index < options.size() ? index : -1;
	}
	/** 鼠标是否落在列表区里（调用方据此决定要不要接手滚轮、要不要关掉整个界面）。 */
	public boolean hovering(double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
	public void release() {
		armed = null;
		scrollbar.release();
	}
	public void wheel(double amount) {
		scrollbar.wheel(amount);
	}
}
