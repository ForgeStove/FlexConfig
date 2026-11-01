package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.ResourceLocation;
public enum FlexGuiTextures {
	ICON_ON("icon/on"),
	ICON_OFF("icon/off"),
	ICON_RESET("icon/reset"),
	ICON_UNDO("icon/undo"),
	ICON_LOCK("icon/lock"),
	ICON_UNLOCK("icon/unlock");
	private final ResourceLocation id;
	/** 1.20.1 没有 gui sprites 图集，尺寸只能从贴图文件本身读，首次使用时缓存。 */
	private int texWidth = -1, texHeight = -1;
	FlexGuiTextures(String path) {
		id = FlexConfig.getFlexRes("textures/gui/sprites/" + path + ".png");
	}
	private void loadSize() {
		if (texWidth > 0) return;
		try (var image = NativeImage.read(ClientUtil.mc.getResourceManager().getResource(id).orElseThrow().open())) {
			texWidth = image.getWidth();
			texHeight = image.getHeight();
		} catch (Exception e) {
			texWidth = texHeight = 16;
		}
	}
	public int width() {
		loadSize();
		return texWidth;
	}
	public int height() {
		loadSize();
		return texHeight;
	}
	public void render(GuiGraphics gui, int x, int y) {
		gui.blit(id, x, y, 0, 0, width(), height(), width(), height());
	}
}
