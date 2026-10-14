package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.util.function.*;
/**
 * {@link OreList} 的控件外壳：把「无 render / 无事件的纯绘制列表」接进原版布局与事件分发。
 * <p>
 * 控件矩形就是列表矩形，几何只有 {@link #syncArea()} 一个来源，所以鼠标命中范围和画出来的列表永远一致；
 * 想让列表多高，调用方只需 {@code setHeight(list.pixelHeight(可用高))}。
 * <p>
 * 点空白与滚轮**不吞事件**（相应返回 {@code false}）：列表不是模态的，下面的控件该拿到的点击不能被它拦掉。
 * <p>
 * 光标也在这里请求：只有真的落在某一行上，或者有溢出时落在滚动条上，才出手型。列表里的空白处保持箭头。
 */
public final class OreListWidget<T> extends AbstractWidget {
	private final OreList<T> list;
	/** 点中一行时的动作。 */
	private final Consumer<T> onPick;
	/** 列表可用高（px）；{@link #setHeight} 也写这里，故两者恒等。 */
	private int maxHeight;
	public OreListWidget(@NotNull OreList<T> list, @NotNull Consumer<T> onPick) {
		super(0, 0, 0, 0, Component.empty());
		this.list = list;
		this.onPick = onPick;
	}
	/** 转给 {@link OreList#filter}；列表在壳里，搜索框只需拿到这个方法引用。 */
	public void filter(String query) {
		list.filter(query);
	}
	/** 当前可见行里被选中的那个，键盘确认用；没有可选项时为 {@code null}。 */
	public @Nullable T defaultOption() {
		return list.defaultOption();
	}
	/** 这么高的可用区能放下多少像素高；配合 {@link #setHeight} 用。 */
	public int pixelHeight(int maxHeight) {
		return list.pixelHeight(maxHeight);
	}
	/** 把列表摆到控件当前的矩形上。布局每次改完位置都要调一次。 */
	public void syncArea() {
		list.area(getX(), getY(), getWidth(), maxHeight);
	}
	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		maxHeight = height;
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		list.render(gui, ClientUtil.mc.font, mouseX, mouseY, partialTick);
		if (wantsHandCursor(mouseX, mouseY)) OreCursor.hand();
	}
	/** 落在某一行上，或有溢出时落在滚动条上。 */
	private boolean wantsHandCursor(double mouseX, double mouseY) {
		if (!list.hovering(mouseX, mouseY)) return false;
		if (list.hasRowAt(mouseX, mouseY)) return true;
		return list.scrolling() && mouseX >= getX() + getWidth() - list.scrollbarWidth();
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!active || !visible || !isValidClickButton(button)) return false;
		var picked = list.mousePressed(mouseX, mouseY);
		if (picked == null) return false;   // 点空白不拦截，下层控件照常收到
		onPick.accept(picked);
		return true;
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (!list.hovering(mouseX, mouseY)) return false;
		list.wheel(vertical);
		return true;
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		list.release();
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {}
}
