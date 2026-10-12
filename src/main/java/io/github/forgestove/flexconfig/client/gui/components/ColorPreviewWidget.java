package io.github.forgestove.flexconfig.client.gui.components;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;
/** 颜色预览色块：填满指定颜色 + Ore 描边；带 alpha 的颜色先垫一层棋盘格。 */
public final class ColorPreviewWidget extends AbstractWidget {
	private static final int BORDER = 0xFF1E1E1F;
	private static final int CHECKER_LIGHT = 0xFFD0D1D4, CHECKER_DARK = 0xFF8C8D90;
	private static final int CHECKER_SIZE = 4;
	private final IntSupplier colorSupplier;
	private final boolean hasAlpha;
	public ColorPreviewWidget(int x, int y, int width, int height, boolean hasAlpha, IntSupplier colorSupplier) {
		super(x, y, width, height, Component.empty());
		this.colorSupplier = colorSupplier;
		this.hasAlpha = hasAlpha;
		active = false;
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		var color = colorSupplier.getAsInt();
		if (!hasAlpha) color |= 0xFF000000;
		if (hasAlpha) renderCheckerboard(gui);
		gui.fill(getX(), getY(), getX() + width, getY() + height, color);
		gui.renderOutline(getX(), getY(), width, height, BORDER);
	}
	private void renderCheckerboard(@NotNull GuiGraphics gui) {
		for (var y = 0; y < height; y += CHECKER_SIZE)
			for (var x = 0; x < width; x += CHECKER_SIZE) {
				var light = (x / CHECKER_SIZE + y / CHECKER_SIZE) % 2 == 0;
				gui.fill(
					getX() + x,
					getY() + y,
					getX() + Math.min(x + CHECKER_SIZE, width),
					getY() + Math.min(y + CHECKER_SIZE, height),
					light ? CHECKER_LIGHT : CHECKER_DARK
				);
			}
	}
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
		defaultButtonNarrationText(narration);
	}
}
