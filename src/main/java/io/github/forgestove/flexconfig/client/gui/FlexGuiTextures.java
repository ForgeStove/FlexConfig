package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
public enum FlexGuiTextures {
	ICON_ON("icon/on"),
	ICON_OFF("icon/off"),
	ICON_RESET("icon/reset"),
	ICON_UNDO("icon/undo"),
	ICON_LOCK("icon/lock"),
	ICON_UNLOCK("icon/unlock");
	private final ResourceLocation id;
	FlexGuiTextures(String path) {
		id = ResourceLocation.fromNamespaceAndPath("flexconfig", path);
	}
	private TextureAtlasSprite getSprite() {
		return ClientUtil.mc.getGuiSprites().getSprite(id);
	}
	private SpriteContents getContents() {
		return getSprite().contents();
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
