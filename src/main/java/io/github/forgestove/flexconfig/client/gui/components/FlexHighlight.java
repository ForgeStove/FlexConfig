package io.github.forgestove.flexconfig.client.gui.components;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntUnaryOperator;
/** 列表悬停行的高亮：位置平滑跟随、淡入淡出；只负责绘制，不参与命中。 */
public class FlexHighlight extends AbstractWidget {
	private static final float FADE_SPEED = 0.25F;
	private static final float POSITION_SPEED = 0.75F;
	private final int itemHeight;
	private final HoveredIndex hoveredIndex;
	private final IntUnaryOperator rowTopGetter;
	private float y = -1;
	private float targetY = -1;
	private float alpha;
	private int lastIndex = -1;
	public FlexHighlight(int itemHeight, HoveredIndex hoveredIndex, IntUnaryOperator rowTopGetter) {
		super(0, 0, 0, 0, Component.empty());
		this.itemHeight = itemHeight;
		this.hoveredIndex = hoveredIndex;
		this.rowTopGetter = rowTopGetter;
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		var alphaInt = (int) (alpha * 48); // 最大透明度 48 (0x30)
		var color = alphaInt << 24 | 0xFFFFFF;
		var top = (int) y - 1;
		var bottom = top + itemHeight;
		var visibleBottom = getY() + getHeight();
		if (top < getY()) top = getY();
		if (bottom > visibleBottom) bottom = visibleBottom;
		if (top < bottom) gui.fill(getX(), top, getX() + getWidth(), bottom, color);
		// 两种速度按 1-(1-SPEED)^delta 折算，帧率变化不影响淡入淡出与位置趋近
		var fadeProgress = 1.0f - (float) Math.pow(1.0 - FADE_SPEED, delta);
		var index = hoveredIndex.index(mouseX, mouseY);
		if (index >= 0) {
			var entryTop = rowTopGetter.applyAsInt(index);
			targetY = entryTop;
			alpha = Mth.lerp(fadeProgress, alpha, 0.95F);
			// 首次悬停没有可插值的历史位置，直接就位
			if (y < 0 || lastIndex < 0) y = entryTop;
			lastIndex = index;
		} else {
			alpha = Mth.lerp(fadeProgress, alpha, 0.0F);
			lastIndex = -1;
		}
		if (!(targetY >= 0) || !(y >= 0)) return;
		var positionProgress = 1.0f - (float) Math.pow(1.0 - POSITION_SPEED, delta);
		y = Mth.lerp(positionProgress, y, targetY);
		// 差值不足 1px 时直接取目标，避免无限逼近
		if (Math.abs(y - targetY) < 1.0f) y = targetY;
	}
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return false;
	}
	/** 只负责绘制：命中一律交给下层控件。 */
	@Override
	protected boolean clicked(double mouseX, double mouseY) {
		return false;
	}
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {}
	@FunctionalInterface
	public interface HoveredIndex {
		int index(double mouseX, double mouseY);
	}
}
