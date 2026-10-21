package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
public enum FlexGuiTextures {
	// 滑钮
	THUMB("thumb/normal"),
	THUMB_HOVER("thumb/hover"),
	THUMB_PRESSED("thumb/pressed"),
	THUMB_DISABLED("thumb/disabled"),
	// 开关
	TOGGLE_TRACK("toggle/track"),
	TOGGLE_TRACK_DISABLED("toggle/track_disabled"),
	// 标签页
	TAB_ON("tab/on"),
	TAB_OFF("tab/off"),
	TAB_OFF_HOVER("tab/off_hover"),
	// 按钮
	BUTTON_PRIMARY("button/primary"),
	BUTTON_PRIMARY_HOVER("button/primary_hover"),
	BUTTON_PRIMARY_PRESSED("button/primary_pressed"),
	BUTTON_DESTRUCTIVE("button/destructive"),
	BUTTON_DESTRUCTIVE_HOVER("button/destructive_hover"),
	BUTTON_DESTRUCTIVE_PRESSED("button/destructive_pressed"),
	BUTTON_SECONDARY("button/secondary"),
	BUTTON_SECONDARY_HOVER("button/secondary_hover"),
	BUTTON_SECONDARY_PRESSED("button/secondary_pressed"),
	BUTTON_BLACK("button/black"),
	BUTTON_BLACK_HOVER("button/black_hover"),
	BUTTON_BLACK_PRESSED("button/black_pressed"),
	BUTTON_BLACK_DISABLED("button/black_disabled"),
	BUTTON_DISABLED("button/disabled"),
	// 输入框
	FIELD_NORMAL("field/normal"),
	FIELD_DISABLED("field/disabled"),
	// 滚动条滑块
	SCROLLBAR("scrollbar/thumb"),
	SCROLLBAR_HOVER("scrollbar/thumb_hover"),
	// 滑块
	SLIDER_TRACK("slider/track"),
	SLIDER_TRACK_FILLED("slider/track_filled"),
	// 图标
	ICON_RESET("icon/reset"),
	ICON_UNDO("icon/undo"),
	ICON_LOCK("icon/lock"),
	ICON_UNLOCK("icon/unlock"),
	// 列表选中标记
	CHECKBOX_ON("icon/checkbox_on"),
	CHECKBOX_ON_HOVER("icon/checkbox_on_hover"),
	CHECKBOX_ON_PRESSED("icon/checkbox_on_pressed"),
	CHECKBOX_OFF("icon/checkbox_off"),
	CHECKBOX_OFF_HOVER("icon/checkbox_off_hover"),
	CHECKBOX_OFF_PRESSED("icon/checkbox_off_pressed");
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
	public void render(GuiGraphics gui, int x, int y, int width, int height) {
		gui.blitSprite(id, x, y, width, height);
	}
}
