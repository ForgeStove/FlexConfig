package io.github.forgestove.flexconfig.foundation.client.screen;
import io.github.forgestove.flexconfig.api.menu.Accent;
import io.github.forgestove.flexconfig.foundation.client.*;
import io.github.forgestove.flexconfig.foundation.ui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntConsumer;
/** 颜色选择器的 Screen 适配器，视图逻辑在 {@link ColorPickerView}。 */
public class ColorPickerScreen extends MinecraftScreen implements ConfigHost {
	private final Screen parent;
	public ColorPickerScreen(Screen parent, int initialColor, boolean hasAlpha, IntConsumer onColorSelected) {
		super(Translation.COLOR_PICKER_TOOLTIP);
		this.parent = parent;
		setView(new ColorPickerView(this, initialColor, hasAlpha, onColorSelected));
	}
	@Override
	public int guiWidth() {
		return width;
	}
	@Override
	public int guiHeight() {
		return height;
	}
	@Override
	public boolean shiftDown() {
		return hasShiftDown();
	}
	@Override
	public void close() {
		ClientUtil.mc.setScreen(parent);
	}
	@Override
	public Accent accent() {
		return parent instanceof ConfigScreen<?, ?> screen ? screen.accent() : Accent.DEFAULT;
	}
	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		parent.resize(minecraft, width, height);
		super.resize(minecraft, width, height);
	}
}
