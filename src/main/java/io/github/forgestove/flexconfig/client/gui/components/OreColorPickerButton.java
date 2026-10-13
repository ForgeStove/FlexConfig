package io.github.forgestove.flexconfig.client.gui.components;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.util.function.IntSupplier;
public final class OreColorPickerButton extends OreAbstractButton {
	private static final int BORDER = 0xFF1E1E1F, BORDER_HOVER = 0xFFFFFFFF;
	private static final int CHECKER_LIGHT = 0xFFD0D1D4, CHECKER_DARK = 0xFF8C8D90, CHECKER_SIZE = 4;
	private final IntSupplier colorSupplier;
	private final boolean hasAlpha;
	@Nullable private final Runnable action;
	public OreColorPickerButton(
		int x,
		int y,
		int width,
		int height,
		boolean hasAlpha,
		IntSupplier colorSupplier,
		@Nullable Runnable action
	) {
		super(x, y, width, height, Component.empty());
		this.colorSupplier = colorSupplier;
		this.hasAlpha = hasAlpha;
		this.action = action;
		active = action != null;
	}
	@Override
	public void onPress() {
		if (action != null) action.run();
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		var color = colorSupplier.getAsInt();
		if (!hasAlpha) color |= 0xFF000000;
		if (hasAlpha) renderCheckerboard(gui);
		gui.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
		gui.renderOutline(getX(), getY(), getWidth(), getHeight(), isHovered() ? BORDER_HOVER : BORDER);
		renderCursor();
	}
	private void renderCheckerboard(@NotNull GuiGraphics gui) {
		for (var y = 0; y < getHeight(); y += CHECKER_SIZE)
			for (var x = 0; x < getWidth(); x += CHECKER_SIZE) {
				var light = (x / CHECKER_SIZE + y / CHECKER_SIZE) % 2 == 0;
				gui.fill(
					getX() + x,
					getY() + y,
					getX() + Math.min(x + CHECKER_SIZE, getWidth()),
					getY() + Math.min(y + CHECKER_SIZE, getHeight()),
					light ? CHECKER_LIGHT : CHECKER_DARK
				);
			}
	}
}
