package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
/**
 * 全模组 GUI 纹理的登记表：只登记精灵名，贴图与切法都在资源文件里。
 * <p>
 * 贴图放 {@code textures/gui/sprites/**}，由 {@code minecraft:atlases/gui} 的 directory 源收录。
 * 盒子类精灵配同名 {@code .png.mcmeta} 写九宫格，给多少尺寸画多大；开关与图标按原尺寸画。
 */
public enum FlexGuiTextures {
	// 滑钮：开关与滑块共用的方形滑块，四态
	THUMB("thumb/normal"),
	THUMB_HOVER("thumb/hover"),
	THUMB_PRESSED("thumb/pressed"),
	THUMB_DISABLED("thumb/disabled"),
	// 开关：轨道常驻两段式，滑钮挡住哪半就显示另半
	TOGGLE_TRACK("toggle/track"),
	TOGGLE_TRACK_DISABLED("toggle/track_disabled"),
	// 标签页：选中 / 未选中 / 未选中悬停（选中与按下同一套盒子）
	TAB_ON("tab/on"),
	TAB_OFF("tab/off"),
	TAB_OFF_HOVER("tab/off_hover"),
	// 按钮：3 类型 ×{常态, 悬停, 按下} + 三种类型共用的禁用态
	BUTTON_PRIMARY("button/primary"),
	BUTTON_PRIMARY_HOVER("button/primary_hover"),
	BUTTON_PRIMARY_PRESSED("button/primary_pressed"),
	BUTTON_DESTRUCTIVE("button/destructive"),
	BUTTON_DESTRUCTIVE_HOVER("button/destructive_hover"),
	BUTTON_DESTRUCTIVE_PRESSED("button/destructive_pressed"),
	BUTTON_SECONDARY("button/secondary"),
	BUTTON_SECONDARY_HOVER("button/secondary_hover"),
	BUTTON_SECONDARY_PRESSED("button/secondary_pressed"),
	BUTTON_DISABLED("button/disabled"),
	// 输入框：常态 / 禁用
	FIELD_NORMAL("field/normal"),
	FIELD_DISABLED("field/disabled"),
	// 滚动条滑块（纵向拉伸）：常态 / 悬停；轨道是纯色细线，运行时直接 fill
	SCROLLBAR("scrollbar/thumb"),
	SCROLLBAR_HOVER("scrollbar/thumb_hover"),
	// 滑块：轨道（未填充 / 已填充），滑钮用上面共用的；聚焦环与分段暗线由控件画
	SLIDER_TRACK("slider/track"),
	SLIDER_TRACK_FILLED("slider/track_filled"),
	// 行内动作图标：重置 / 撤销 / 锁定 / 解锁
	ICON_RESET("icon/reset"),
	ICON_UNDO("icon/undo"),
	ICON_LOCK("icon/lock"),
	ICON_UNLOCK("icon/unlock");
	private final ResourceLocation sprite;
	FlexGuiTextures(String path) {
		sprite = ResourceLocation.fromNamespaceAndPath("flexconfig", path);
	}
	/** 按精灵自身尺寸绘制到 (x, y)。 */
	public void render(@NotNull GuiGraphics gui, int x, int y) {
		gui.blitSprite(sprite, x, y, width(), height());
	}
	/** 给多少尺寸画多大；九宫格精灵由引擎按元数据裁切，中段是纯色，平铺接缝看不出来。 */
	public void render(@NotNull GuiGraphics gui, int x, int y, int width, int height) {
		if (width <= 0 || height <= 0) return;
		gui.blitSprite(sprite, x, y, width, height);
	}
	/** 由资源文件决定，随资源包变。 */
	@SuppressWarnings("resource")
	public int width() {
		return sprite().contents().width();
	}
	/** 由资源文件决定，随资源包变。 */
	@SuppressWarnings("resource")
	public int height() {
		return sprite().contents().height();
	}
	private @NotNull TextureAtlasSprite sprite() {
		return ClientUtil.mc.getGuiSprites().getSprite(sprite);
	}
}
