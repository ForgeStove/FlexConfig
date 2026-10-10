package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
/**
 * 全模组 GUI 纹理的登记表 —— 只有精灵名，**尺寸与 UV 都归资源文件管**。
 * <p>
 * 纹理是一张 sheet（{@code assets/flexconfig/textures/gui/toggle.png}），各块由
 * {@code assets/minecraft/atlases/gui.json} 里的 {@code unstitch} 源切成命名精灵；尺寸也不再写死，
 * {@link #width()} 直接问 gui 图集（region 改多大就是多大），绘制走 {@code blitSprite}。于是：
 * <ul>
 * <li><b>换画风</b>：资源包覆盖那张 sheet（保持布局即可）</li>
 * <li><b>换尺寸/布局</b>：改 atlas JSON 的 region —— sheet 与 JSON 都由 {@code tools/GenOreUiTextures.java}
 * 用同一组常量生成，改排布改脚本重跑</li>
 * </ul>
 * ⚠️ 尺寸既然交给资源，把开关改得比配置行还高就会溢出（行高属于配置界面布局，不随纹理变）。
 */
public enum AllGuiTextures {
	// 开关：轨道三态 + 滑钮四态
	TOGGLE_TRACK_OFF("toggle/track_off"),
	TOGGLE_TRACK_ON("toggle/track_on"),
	TOGGLE_TRACK_DISABLED("toggle/track_disabled"),
	TOGGLE_THUMB("toggle/thumb"),
	TOGGLE_THUMB_HOVER("toggle/thumb_hover"),
	TOGGLE_THUMB_PRESSED("toggle/thumb_pressed"),
	TOGGLE_THUMB_DISABLED("toggle/thumb_disabled"),
	TOGGLE_FOCUS_RING("toggle/focus_ring");
	private final ResourceLocation sprite;
	AllGuiTextures(String path) {
		sprite = ResourceLocation.fromNamespaceAndPath("flexconfig", path);
	}
	private @NotNull TextureAtlasSprite sprite() {
		return ClientUtil.mc.getGuiSprites().getSprite(sprite);
	}
	/** 实际宽度（由 atlas region 决定，随资源包变）。 */
	@SuppressWarnings("resource")
	public int width() {
		return sprite().contents().width();
	}
	/** 实际高度（由 atlas region 决定，随资源包变）。 */
	@SuppressWarnings("resource")
	public int height() {
		return sprite().contents().height();
	}
	/** 按精灵名与自身尺寸绘制到 (x, y)：UV 由图集算，尺寸由图集读，代码不持有任何坐标或像素值。 */
	public void render(@NotNull GuiGraphics gui, int x, int y) {
		gui.blitSprite(sprite, x, y, width(), height());
	}
}
