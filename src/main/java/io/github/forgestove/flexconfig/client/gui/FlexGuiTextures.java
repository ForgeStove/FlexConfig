package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
//? if >=1.20.2 {
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//?} else {
/*import com.mojang.blaze3d.platform.NativeImage;
*///?}
import net.minecraft.resources.ResourceLocation;
public enum FlexGuiTextures {
	ICON_ON("icon/on"),
	ICON_OFF("icon/off"),
	ICON_RESET("icon/reset"),
	ICON_UNDO("icon/undo"),
	ICON_LOCK("icon/lock"),
	ICON_UNLOCK("icon/unlock");
	private final ResourceLocation id;
	//? if <1.20.2 {
	/*/^* 1.20.1 没有 gui sprites 图集，尺寸只能从贴图文件本身读，首次使用时缓存。 ^/
	private int texWidth = -1, texHeight = -1;
	*///?}
	FlexGuiTextures(String path) {
		//? if >=1.20.2 {
		id = ClientUtil.getFlexRes(path);
		//?} else {
		/*id = ClientUtil.getFlexRes("textures/gui/sprites/" + path + ".png");
		*///?}
	}
	//? if >=1.20.2 {
	private TextureAtlasSprite getSprite() {
		return ClientUtil.mc.getGuiSprites().getSprite(id);
	}
	//?}
	//? if <1.20.2 {
	/*private void loadSize() {
		if (texWidth > 0) return;
		try (var image = NativeImage.read(ClientUtil.mc.getResourceManager().getResource(id).orElseThrow().open())) {
			texWidth = image.getWidth();
			texHeight = image.getHeight();
		} catch (Exception e) {
			texWidth = texHeight = 16;
		}
	}
	*///?}
	public int width() {
		//? if >=1.20.2 {
		return getSprite().contents().width();
		//?} else {
		/*loadSize();
		return texWidth;
		*///?}
	}
	public int height() {
		//? if >=1.20.2 {
		return getSprite().contents().height();
		//?} else {
		/*loadSize();
		return texHeight;
		*///?}
	}
	public void render(GuiGraphics gui, int x, int y) {
		//? if >=1.20.2 {
		gui.blitSprite(id, x, y, width(), height());
		//?} else {
		/*gui.blit(id, x, y, 0, 0, width(), height(), width(), height());
		*///?}
	}
}
