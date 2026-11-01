package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
public enum FlexGuiTextures {
	ICON_ON("icon/on"),
	ICON_OFF("icon/off"),
	ICON_RESET("icon/reset"),
	ICON_UNDO("icon/undo"),
	ICON_LOCK("icon/lock"),
	ICON_UNLOCK("icon/unlock");
	public final ResourceLocation id;
	FlexGuiTextures(String path) {
		id = FlexConfig.getFlexRes(path);
	}
	private @NotNull SpriteContents getContents() {
		return ClientUtil.mc.getGuiSprites().getSprite(id).contents();
	}
	public int width() {
		return getContents().width();
	}
	public int height() {
		return getContents().height();
	}
	public void render(GuiGraphics gui, int x, int y) {
		gui.blitSprite(id, x, y, width(), height());
	}
}
