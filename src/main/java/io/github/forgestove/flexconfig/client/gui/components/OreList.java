package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
/**
 * 一列可滚动的文字项：悬停铺灰底、当前项铺主题绿，滚动是平滑的、也支持拖动。
 * <p>
 * 只管画和拾取，选中后要做什么（关弹窗、跳界面）由调用方决定，所以枚举下拉弹窗和「其他模组」整屏列表共用它。
 * <p>
 * 想要一个能直接塞进布局的控件壳，用 {@link OreListWidget}。
 */
public final class OreList<T> {
	/** 行高、行间距、行内左右留白。 */
	public static final int ROW_H = 14, ROW_GAP = 1, ITEM_H = ROW_H + ROW_GAP, CELL_PAD = 5;
	/** 悬停 / 当前项配色（当前项对齐基岩列表的选中绿）；空结果提示压暗一档。 */
	private static final int ROW_HOVER = 0xFF56585B, ROW_SELECTED = 0xFF3C8527, TEXT = 0xFFFFFFFF, TEXT_EMPTY = 0xFFAAAAAA;
	private final List<T> options;
	private final Function<T, Component> display;
	/** 当前项 —— 列表里画成选中的那一项；返回 {@code null} 即没有当前项，常用于「选完就跳走」的列表。 */
	private final Supplier<T> current;
	/** 过滤后一条不剩时画在首行位置的灰字；为 {@code null} 就什么都不画。 */
	@Nullable private final Component emptyMessage;
	private final OreScrollbar scrollbar = new OreScrollbar();
	/** 过滤后的当前列表；没过滤时恒等于 {@link #options}。 */
	private List<T> filtered;
	private int x, y, width, height;
	/** 一屏能放下的行数，由 {@link #height} 算出。 */
	private int visible;
	public OreList(List<T> options, Function<T, Component> display, Supplier<T> current) {
		this(options, display, current, null);
	}
	public OreList(List<T> options, Function<T, Component> display, Supplier<T> current, @Nullable Component emptyMessage) {
		this.options = options;
		this.display = display;
		this.current = current;
		this.emptyMessage = emptyMessage;
		filtered = options;
		scrollbar.step(ITEM_H);
	}
	/** 按搜索词过滤（空词即全部），并回到顶部。 */
	public void filter(String query) {
		var key = query.trim().toLowerCase(Locale.ROOT);
		filtered = key.isEmpty()
			? options
			: options.stream().filter(option -> display.apply(option).getString().toLowerCase(Locale.ROOT).contains(key)).toList();
		scrollbar.seek(0);
	}
	/** 需要的宽度：按整组文案算，不随过滤结果收缩，否则搜出两三条时列表会忽宽忽窄。 */
	public int width(Font font) {
		var widest = 0;
		for (var option : options) widest = Math.max(widest, font.width(display.apply(option)));
		return widest + CELL_PAD * 2;
	}
	/** 需要的可见行数（受 {@code maxHeight} 限制）；顺带记下这次算出的值。 */
	public int height(int maxHeight) {
		visible = Math.clamp(filtered.size(), 1, Math.max(1, maxHeight / ITEM_H));
		return visible;
	}
	/** 一次算完并给出像素高：省得调用方再复刻一遍「行数 × 行高」这笔账。 */
	public int pixelHeight(int maxHeight) {
		return height(maxHeight) * ITEM_H;
	}
	/** 有内容超出可视区 —— 此时才画滚动条，也才该出手型光标。 */
	public boolean scrolling() {
		return filtered.size() > visible;
	}
	/** 过滤后一条不剩（此时 {@link #render} 改画 {@link #emptyMessage}）。 */
	public boolean isEmpty() {
		return filtered.isEmpty();
	}
	/** 键盘确认的落点：当前项在就直接给它，否则退回首条；列表为空返回 {@code null}。 */
	public @Nullable T defaultOption() {
		var option = current.get();
		return option != null && filtered.contains(option) ? option : filtered.isEmpty() ? null : filtered.getFirst();
	}
	/** 把当前项摆到列表中间，打开下拉时用。 */
	public void centerCurrent() {
		var index = options.indexOf(current.get());
		if (index > 0) scrollbar.seek(index * ITEM_H - visible * ITEM_H / 2.0);
	}
	/** 滚动条的宽度，由滑块精灵决定（随资源包变）。 */
	public int scrollbarWidth() {
		return FlexGuiTextures.SCROLLBAR_TOP.width();
	}
	/** 摆到指定矩形里；高要先用 {@link #height} 问过。 */
	public void area(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		scrollbar.area(x + width - scrollbarWidth(), y, height, filtered.size() * ITEM_H);
	}
	/** 每帧调用：喂当前鼠标坐标（拖动落位）、推进平滑，然后画行与滚动条。 */
	public void render(GuiGraphics gui, Font font, int mouseX, int mouseY) {
		scrollbar.dragTo(mouseY);
		scrollbar.update();
		var scroll = scrollbar.scroll();
		var currentValue = current.get();
		gui.enableScissor(x, y, x + width, y + height);
		if (filtered.isEmpty()) renderEmpty(gui, font, scroll);
		else for (var i = 0; i < filtered.size(); i++) {
			var rowY = (int) Math.round(y + i * ITEM_H - scroll);
			if (rowY + ROW_H < y || rowY > y + height) continue;
			var option = filtered.get(i);
			if (Objects.equals(option, currentValue)) gui.fill(x, rowY, x + width, rowY + ROW_H, ROW_SELECTED);
			else if (isOver(mouseX, mouseY, rowY)) gui.fill(x, rowY, x + width, rowY + ROW_H, ROW_HOVER);
			gui.drawString(font, display.apply(option).getVisualOrderText(), x + CELL_PAD, rowY + (ROW_H - 8) / 2, TEXT, true);
		}
		gui.disableScissor();
		scrollbar.render(gui, mouseX, mouseY);
	}
	/** 空结果提示：画在首行的位置上，水平居中。 */
	private void renderEmpty(GuiGraphics gui, Font font, double scroll) {
		if (emptyMessage == null) return;
		var rowY = (int) Math.round(y - scroll);
		if (rowY + ROW_H < y || rowY > y + height) return;
		gui.drawString(font, emptyMessage.getVisualOrderText(),
			x + (width - font.width(emptyMessage)) / 2, rowY + (ROW_H - 8) / 2, TEXT_EMPTY, true);
	}
	/** 点在某一行上就返回该行，点在滚动条或空白处返回 {@code null}。 */
	public @Nullable T mousePressed(double mouseX, double mouseY) {
		if (scrollbar.mousePressed(mouseX, mouseY)) return null;
		var index = rowAt(mouseX, mouseY);
		return index < 0 ? null : filtered.get(index);
	}
	public void release() {
		scrollbar.release();
	}
	public void wheel(double amount) {
		scrollbar.wheel(amount);
	}
	/** 鼠标是否落在列表区里（调用方据此决定要不要接手滚轮、要不要关掉整个界面）。 */
	public boolean hovering(double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
	/** 鼠标是否正落在某一行上（列表区内、行与行之间那条 1px 缝不算）。 */
	public boolean hasRowAt(double mouseX, double mouseY) {
		var index = rowAt(mouseX, mouseY);
		if (index < 0) return false;
		var rowTop = y + index * ITEM_H - (int) Math.round(scrollbar.scroll());
		return mouseY >= rowTop && mouseY < rowTop + ROW_H;
	}
	/** @return 鼠标所在行号；不在列表区内返回 -1。 */
	private int rowAt(double mouseX, double mouseY) {
		if (!hovering(mouseX, mouseY)) return -1;
		var index = (int) ((mouseY - y + scrollbar.scroll()) / ITEM_H);
		return index >= 0 && index < filtered.size() ? index : -1;
	}
	private boolean isOver(double mouseX, double mouseY, int rowY) {
		return mouseX >= x && mouseX < x + width && mouseY >= rowY && mouseY < rowY + ROW_H;
	}
}
