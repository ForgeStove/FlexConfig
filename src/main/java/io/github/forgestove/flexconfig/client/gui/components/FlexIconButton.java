package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
public class FlexIconButton extends Button {
	private FlexGuiTextures icon;
	public FlexIconButton(int width, int height, @NotNull FlexGuiTextures icon, @NotNull OnPress onPress) {
		super(0, 0, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
		this.icon = icon;
	}
	public void setIcon(@NotNull FlexGuiTextures icon) {
		this.icon = icon;
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		super.renderWidget(gui, mouseX, mouseY, partialTick);
		icon.render(gui, getX() + (getWidth() - icon.width()) / 2, getY() + (getHeight() - icon.height()) / 2);
	}
}
