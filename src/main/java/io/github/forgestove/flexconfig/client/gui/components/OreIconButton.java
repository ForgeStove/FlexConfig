package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
/** 图标是白色蒙版，用 {@code setShaderColor} 按按钮的文字色着色，同一张图在深字与白字按钮上均可用。 */
public class OreIconButton extends OreButton {
	private FlexGuiTextures icon;
	public OreIconButton(int size, @NotNull FlexGuiTextures icon, @NotNull OnPress onPress) {
		super(size, Component.empty(), Style.SECONDARY, onPress);
		this.icon = icon;
	}
	public void setIcon(@NotNull FlexGuiTextures icon) {
		this.icon = icon;
	}
	@Override
	protected void renderContent(@NotNull GuiGraphics gui, int boxY) {
		icon.render(gui, getX() + (getWidth() - icon.width()) / 2, centeredY(boxY, icon.height()));
	}
}
