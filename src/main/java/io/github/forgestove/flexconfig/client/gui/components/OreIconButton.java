package io.github.forgestove.flexconfig.client.gui.components;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor.ARGB32;
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
		var color = textColor();
		RenderSystem.setShaderColor(ARGB32.red(color) / 255F, ARGB32.green(color) / 255F, ARGB32.blue(color) / 255F, 1F);
		icon.render(gui, getX() + (getWidth() - icon.width()) / 2, centeredY(boxY, icon.height()));
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
}
