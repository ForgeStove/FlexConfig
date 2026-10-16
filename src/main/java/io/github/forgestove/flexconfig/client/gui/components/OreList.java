package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
/** 负责绘制与命中：选中之后做什么（关弹窗、跳界面）由调用方决定。 */
public final class OreList<T> {
	public static final int DEFAULT_ROW_H = 14;
	private static final int ROW_HOVER = 0xFF56585B, TEXT = 0xFFFFFFFF;
	private static final int ICON_GAP = 4;
	private static final int ROW_GAP = 1, CELL_PAD = 5;
	private final List<T> options;
	private final Function<T, Component> display;
	/** 当前项；返回 {@code null} 表示无当前项。 */
	private final Supplier<T> current;
	private final OreScrollbar scrollbar = new OreScrollbar();
	private int rowH = DEFAULT_ROW_H;
	private int x, y, width, height;
	/** 按下时所在的那一项，松手即清空。 */
	@Nullable private T armed;
	private int visible;
	public OreList(List<T> options, Function<T, Component> display, Supplier<T> current) {
		this.options = options;
		this.display = display;
		this.current = current;
		scrollbar.step(itemHeight());
	}
	private static FlexGuiTextures mark(boolean selected, boolean hovered, boolean pressed) {
		if (selected) return pressed
			? FlexGuiTextures.CHECKBOX_ON_PRESSED
			: hovered ? FlexGuiTextures.CHECKBOX_ON_HOVER : FlexGuiTextures.CHECKBOX_ON;
		return pressed ? FlexGuiTextures.CHECKBOX_OFF_PRESSED : hovered ? FlexGuiTextures.CHECKBOX_OFF_HOVER :
			FlexGuiTextures.CHECKBOX_OFF;
	}
	/** 行是自绘的，不像原版按钮那样自动播音，需由调用方播一次点击音效。 */
	public static void playClick() {
		ClientUtil.mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
	public int width(Font font) {
		var widest = 0;
		for (var option : options) widest = Math.max(widest, font.width(display.apply(option)));
		return widest + CELL_PAD * 2 + markWidth() + ICON_GAP;
	}
	/** 标记宽由精灵决定，随资源包变。 */
	private int markWidth() {
		return FlexGuiTextures.CHECKBOX_ON.width();
	}
	/** 一次算完即得像素高，省去调用方再算一次行数乘行高。 */
	public int pixelHeight(int maxHeight) {
		return height(maxHeight) * itemHeight();
	}
	public int naturalHeight() {
		return options.size() * itemHeight();
	}
	/** 受 {@code maxHeight} 限制的可见行数，同时记下该值。 */
	public int height(int maxHeight) {
		visible = Math.clamp(options.size(), 1, Math.max(1, maxHeight / itemHeight()));
		return visible;
	}
	/** 内容超出可视区；此时才画滚动条、才出手型光标。 */
	public boolean scrolling() {
		return options.size() > visible;
	}
	public void centerCurrent() {
		var index = options.indexOf(current.get());
		if (index > 0) scrollbar.seek(index * itemHeight() - visible * itemHeight() / 2.0);
	}
	/** 设行高，同时同步滚轮步长。 */
	public void rowHeight(int rowH) {
		this.rowH = rowH;
		scrollbar.step(itemHeight());
	}
	public int itemHeight() {
		return rowH + ROW_GAP;
	}
	/** 摆到指定矩形；高须先经 {@link #height} 求得，否则 {@link #scrolling} 的判据不成立。 */
	public void area(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		scrollbar.area(x + width - scrollbarWidth(), y, height, options.size() * itemHeight());
	}
	public int scrollbarWidth() {
		return FlexGuiTextures.SCROLLBAR.width();
	}
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
	public void mousePressed(double mouseX, double mouseY) {
		if (scrollbar.mousePressed(mouseX, mouseY)) return;
		armed = optionAt(mouseX, mouseY);
	}
	public @Nullable T armed() {
		return armed;
	}
	public @Nullable T optionAt(double mouseX, double mouseY) {
		var index = rowAt(mouseX, mouseY);
		return index < 0 ? null : options.get(index);
	}
	private int rowAt(double mouseX, double mouseY) {
		if (!hovering(mouseX, mouseY)) return -1;
		var index = (int) ((mouseY - y + scrollbar.scroll()) / itemHeight());
		return index >= 0 && index < options.size() ? index : -1;
	}
	/** 鼠标是否落在列表区内；调用方据此决定是否接管滚轮或关闭界面。 */
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
