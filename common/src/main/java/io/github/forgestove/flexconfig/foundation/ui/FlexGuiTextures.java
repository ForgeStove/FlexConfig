package io.github.forgestove.flexconfig.foundation.ui;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.api.CategoryIcon;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public enum FlexGuiTextures {
	ICON_ON("icon/on"),
	ICON_OFF("icon/off"),
	ICON_RESET("icon/reset"),
	ICON_UNDO("icon/undo"),
	ICON_LOCK("icon/lock"),
	ICON_UNLOCK("icon/unlock"),
	ICON_CATEGORY_CONTROLS("icon/category/controls"),
	ICON_CATEGORY_LANGUAGE("icon/category/language"),
	ICON_CATEGORY_MULTIPLAYER("icon/category/multiplayer"),
	ICON_CATEGORY_GENERAL("icon/category/general"),
	ICON_CATEGORY_FRIENDS("icon/category/friends"),
	ICON_CATEGORY_SERVER("icon/category/server"),
	ICON_CATEGORY_ADVANCED("icon/category/advanced"),
	ICON_CATEGORY_CHEATS("icon/category/cheats"),
	ICON_CATEGORY_EXPERIMENTAL("icon/category/experimental"),
	ICON_CATEGORY_BEHAVIOUR("icon/category/behaviour"),
	ICON_CATEGORY_RESOURCE("icon/category/resource"),
	ICON_CATEGORY_DEV_TAB("icon/category/dev-tab"),
	ICON_CATEGORY_ALEX("icon/category/alex");
	public final ResourceLocation id, texture;
	FlexGuiTextures(String path) {
		id = FlexConfig.getFlexRes(path);
		texture = FlexConfig.getFlexRes("textures/gui/sprites/" + path + ".png");
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
	@Nullable
	public static FlexGuiTextures of(CategoryIcon icon) {
		return switch (icon) {
			case CONTROLS -> ICON_CATEGORY_CONTROLS;
			case LANGUAGE -> ICON_CATEGORY_LANGUAGE;
			case MULTIPLAYER -> ICON_CATEGORY_MULTIPLAYER;
			case GENERAL -> ICON_CATEGORY_GENERAL;
			case FRIENDS -> ICON_CATEGORY_FRIENDS;
			case SERVER -> ICON_CATEGORY_SERVER;
			case ADVANCED -> ICON_CATEGORY_ADVANCED;
			case CHEATS -> ICON_CATEGORY_CHEATS;
			case EXPERIMENTAL -> ICON_CATEGORY_EXPERIMENTAL;
			case BEHAVIOUR -> ICON_CATEGORY_BEHAVIOUR;
			case RESOURCE -> ICON_CATEGORY_RESOURCE;
			case DEV_TAB -> ICON_CATEGORY_DEV_TAB;
			case ALEX -> ICON_CATEGORY_ALEX;
			case NONE -> null;
		};
	}
	public void render(GuiGraphics gui, int x, int y) {
		gui.blitSprite(id, x, y, width(), height());
	}
}
