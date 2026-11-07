package io.github.forgestove.flexconfig.foundation.ui;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.api.CategoryIcon;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.*;
public enum FlexGuiTextures {
	ICON_ON("icon/on"),
	ICON_OFF("icon/off"),
	ICON_RESET("icon/reset"),
	ICON_SEARCH("icon/search"),
	ICON_UNDO("icon/undo"),
	ICON_LOCK("icon/lock"),
	ICON_UNLOCK("icon/unlock"),
	ICON_CATEGORY_CONTROLS("icon/category/controls"),
	ICON_CATEGORY_LANGUAGE("icon/category/language"),
	ICON_CATEGORY_MULTIPLAYER("icon/category/multiplayer"),
	ICON_CATEGORY_FRIENDS("icon/category/friends"),
	ICON_CATEGORY_SERVER("icon/category/server"),
	ICON_CATEGORY_ADVANCED("icon/category/advanced"),
	ICON_CATEGORY_CHEATS("icon/category/cheats"),
	ICON_CATEGORY_EXPERIMENTAL("icon/category/experimental"),
	ICON_CATEGORY_BEHAVIOUR("icon/category/behaviour"),
	ICON_CATEGORY_DEV_TAB("icon/category/dev-tab"),
	ICON_CATEGORY_ALEX("icon/category/alex"),
	ICON_CATEGORY_ACCESSIBILITY("icon/category/accessibility"),
	ICON_CATEGORY_ACCOUNT("icon/category/account"),
	ICON_CATEGORY_CHEST("icon/category/chest"),
	ICON_CATEGORY_COMMAND_BLOCK("icon/category/command-block"),
	ICON_CATEGORY_DEBUG("icon/category/debug-icon"),
	ICON_CATEGORY_FEEDBACK("icon/category/feedback"),
	ICON_CATEGORY_INVITES("icon/category/invites"),
	ICON_CATEGORY_KEYBOARD_MOUSE("icon/category/keyboard-mouse"),
	ICON_CATEGORY_NEWS("icon/category/news"),
	ICON_CATEGORY_PAINTING("icon/category/painting"),
	ICON_CATEGORY_PARTY("icon/category/party"),
	ICON_CATEGORY_PREVIEW("icon/category/preview"),
	ICON_CATEGORY_REALMS("icon/category/realms"),
	ICON_CATEGORY_SHULKER_BOX("icon/category/shulker-box"),
	ICON_CATEGORY_SOUND_BLOCK("icon/category/sound-block"),
	ICON_CATEGORY_STORAGE("icon/category/storage"),
	ICON_CATEGORY_SUBSCRIPTIONS("icon/category/subscriptions"),
	ICON_CATEGORY_TOUCH("icon/category/touch"),
	ICON_CATEGORY_UI_MENU_SERVER_TAB("icon/category/ui-menu-servertab"),
	ICON_CATEGORY_UI_MENU_WORLDS_TAB("icon/category/ui-menu-worldstab"),
	ICON_CATEGORY_UI_MENU_WORLDS_TAB_ALT01("icon/category/ui-menu-worldstab-alt01"),
	ICON_CATEGORY_UI_MENU_WORLDS_TAB_ALT02("icon/category/ui-menu-worldstab-alt02"),
	ICON_CATEGORY_WORK_BENCH("icon/category/work-bench");
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
			case GENERAL, WORK_BENCH -> ICON_CATEGORY_WORK_BENCH;
			case FRIENDS -> ICON_CATEGORY_FRIENDS;
			case SERVER -> ICON_CATEGORY_SERVER;
			case ADVANCED -> ICON_CATEGORY_ADVANCED;
			case CHEATS -> ICON_CATEGORY_CHEATS;
			case EXPERIMENTAL -> ICON_CATEGORY_EXPERIMENTAL;
			case BEHAVIOUR -> ICON_CATEGORY_BEHAVIOUR;
			case RESOURCE, CHEST -> ICON_CATEGORY_CHEST;
			case DEV_TAB -> ICON_CATEGORY_DEV_TAB;
			case ALEX -> ICON_CATEGORY_ALEX;
			case ACCESSIBILITY -> ICON_CATEGORY_ACCESSIBILITY;
			case ACCOUNT -> ICON_CATEGORY_ACCOUNT;
			case COMMAND_BLOCK -> ICON_CATEGORY_COMMAND_BLOCK;
			case DEBUG -> ICON_CATEGORY_DEBUG;
			case FEEDBACK -> ICON_CATEGORY_FEEDBACK;
			case INVITES -> ICON_CATEGORY_INVITES;
			case KEYBOARD_MOUSE -> ICON_CATEGORY_KEYBOARD_MOUSE;
			case NEWS -> ICON_CATEGORY_NEWS;
			case PAINTING -> ICON_CATEGORY_PAINTING;
			case PARTY -> ICON_CATEGORY_PARTY;
			case PREVIEW -> ICON_CATEGORY_PREVIEW;
			case REALMS -> ICON_CATEGORY_REALMS;
			case SHULKER_BOX -> ICON_CATEGORY_SHULKER_BOX;
			case SOUND_BLOCK -> ICON_CATEGORY_SOUND_BLOCK;
			case STORAGE -> ICON_CATEGORY_STORAGE;
			case SUBSCRIPTIONS -> ICON_CATEGORY_SUBSCRIPTIONS;
			case TOUCH -> ICON_CATEGORY_TOUCH;
			case UI_MENU_SERVER_TAB -> ICON_CATEGORY_UI_MENU_SERVER_TAB;
			case WORLDS, UI_MENU_WORLDS_TAB -> ICON_CATEGORY_UI_MENU_WORLDS_TAB;
			case UI_MENU_WORLDS_TAB_ALT01 -> ICON_CATEGORY_UI_MENU_WORLDS_TAB_ALT01;
			case UI_MENU_WORLDS_TAB_ALT02 -> ICON_CATEGORY_UI_MENU_WORLDS_TAB_ALT02;
			case NONE -> null;
		};
	}
	public void render(GuiGraphics gui, int x, int y) {
		gui.blitSprite(id, x, y, width(), height());
	}
}
