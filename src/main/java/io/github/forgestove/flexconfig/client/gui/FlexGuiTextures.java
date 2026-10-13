package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
/**
 * 全模组 GUI 纹理的登记表：只有精灵名，尺寸与 UV 都归资源文件管（sheet + atlas 的 unstitch 源）。
 */
public enum FlexGuiTextures {
	TOGGLE_TRACK("toggle/track"),
	TOGGLE_TRACK_DISABLED("toggle/track_disabled"),
	TOGGLE_THUMB("toggle/thumb"),
	TOGGLE_THUMB_HOVER("toggle/thumb_hover"),
	TOGGLE_THUMB_PRESSED("toggle/thumb_pressed"),
	TOGGLE_THUMB_DISABLED("toggle/thumb_disabled"),
	// Tab：每状态三段切（左右端帽 + 可拉伸中段）
	TAB_ON_L("tab/on_l"),
	TAB_ON_M("tab/on_m"),
	TAB_ON_R("tab/on_r"),
	TAB_OFF_L("tab/off_l"),
	TAB_OFF_M("tab/off_m"),
	TAB_OFF_R("tab/off_r"),
	TAB_OFF_HOVER_L("tab/off_hover_l"),
	TAB_OFF_HOVER_M("tab/off_hover_m"),
	TAB_OFF_HOVER_R("tab/off_hover_r"),
	// 按钮：3 类型 ×{常态, 悬停, 按下} + 共用禁用态；三段切同 tab
	BUTTON_PRIMARY_L("button/primary_l"),
	BUTTON_PRIMARY_M("button/primary_m"),
	BUTTON_PRIMARY_R("button/primary_r"),
	BUTTON_PRIMARY_HOVER_L("button/primary_hover_l"),
	BUTTON_PRIMARY_HOVER_M("button/primary_hover_m"),
	BUTTON_PRIMARY_HOVER_R("button/primary_hover_r"),
	BUTTON_PRIMARY_PRESSED_L("button/primary_pressed_l"),
	BUTTON_PRIMARY_PRESSED_M("button/primary_pressed_m"),
	BUTTON_PRIMARY_PRESSED_R("button/primary_pressed_r"),
	BUTTON_DESTRUCTIVE_L("button/destructive_l"),
	BUTTON_DESTRUCTIVE_M("button/destructive_m"),
	BUTTON_DESTRUCTIVE_R("button/destructive_r"),
	BUTTON_DESTRUCTIVE_HOVER_L("button/destructive_hover_l"),
	BUTTON_DESTRUCTIVE_HOVER_M("button/destructive_hover_m"),
	BUTTON_DESTRUCTIVE_HOVER_R("button/destructive_hover_r"),
	BUTTON_DESTRUCTIVE_PRESSED_L("button/destructive_pressed_l"),
	BUTTON_DESTRUCTIVE_PRESSED_M("button/destructive_pressed_m"),
	BUTTON_DESTRUCTIVE_PRESSED_R("button/destructive_pressed_r"),
	BUTTON_SECONDARY_L("button/secondary_l"),
	BUTTON_SECONDARY_M("button/secondary_m"),
	BUTTON_SECONDARY_R("button/secondary_r"),
	BUTTON_SECONDARY_HOVER_L("button/secondary_hover_l"),
	BUTTON_SECONDARY_HOVER_M("button/secondary_hover_m"),
	BUTTON_SECONDARY_HOVER_R("button/secondary_hover_r"),
	BUTTON_SECONDARY_PRESSED_L("button/secondary_pressed_l"),
	BUTTON_SECONDARY_PRESSED_M("button/secondary_pressed_m"),
	BUTTON_SECONDARY_PRESSED_R("button/secondary_pressed_r"),
	BUTTON_DISABLED_L("button/disabled_l"),
	BUTTON_DISABLED_M("button/disabled_m"),
	BUTTON_DISABLED_R("button/disabled_r"),
	// 输入框：常态 / 禁用；横向只有 1px 描边，三段切的左右帽各 1px
	FIELD_NORMAL_L("field/normal_l"),
	FIELD_NORMAL_M("field/normal_m"),
	FIELD_NORMAL_R("field/normal_r"),
	FIELD_DISABLED_L("field/disabled_l"),
	FIELD_DISABLED_M("field/disabled_m"),
	FIELD_DISABLED_R("field/disabled_r"),
	// 滚动条：滑块三段切（上帽 / 可拉伸中段 / 下帽）；轨道是纯色细线，运行时直接 fill
	SCROLLBAR_TOP("scrollbar/top"),
	SCROLLBAR_MID("scrollbar/mid"),
	SCROLLBAR_BOTTOM("scrollbar/bottom"),
	// 行内动作图标：重置 / 撤销 / 锁定 / 解锁（都取自 icons.png 的 16px 格）
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
	/** 由 atlas region 决定，随资源包变。 */
	@SuppressWarnings("resource")
	public int width() {
		return sprite().contents().width();
	}
	/** 由 atlas region 决定，随资源包变。 */
	@SuppressWarnings("resource")
	public int height() {
		return sprite().contents().height();
	}
	private @NotNull TextureAtlasSprite sprite() {
		return ClientUtil.mc.getGuiSprites().getSprite(sprite);
	}
	/**
	 * 按尺寸绘制（**拉伸**）。
	 * <p>
	 * 中段/边这类精灵**沿拉伸方向是纯色**（生成器保证：1px 的描边/高光都落在九宫格的**角/边**上，不在被拉伸的中间单元里），
	 * 所以拉伸与平铺**逐像素一致** —— 而平铺（尤其九宫格那种 2px 单元）会让一帧产生上万次 {@code blitSprite}，直接掉帧。
	 */
	public void render(@NotNull GuiGraphics gui, int x, int y, int width, int height) {
		if (width <= 0 || height <= 0) return;
		gui.blitSprite(sprite, x, y, width, height);
	}
}
