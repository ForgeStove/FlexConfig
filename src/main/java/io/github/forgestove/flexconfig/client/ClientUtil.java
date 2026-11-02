package io.github.forgestove.flexconfig.client;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.platform.Platform;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
public class ClientUtil {
	public static final Minecraft mc = Minecraft.getInstance();
	public static Component modName(String modId) {
		var name = Platform.get().modDisplayName(modId);
		return Component.literal(name != null ? name : modId);
	}
	/** 头部与底部的分隔线：原版由列表控件（{@code AbstractSelectionList#renderListSeparators}）绘制，此处没有原版列表。 */
	public static void renderSeparators(GuiGraphics gui, int width, int height, int headerHeight, int footerHeight) {
		renderSeparator(gui, true, width, headerHeight - 2);
		renderFooterSeparator(gui, width, height, footerHeight);
	}
	public static void renderFooterSeparator(GuiGraphics gui, int width, int height, int footerHeight) {
		renderSeparator(gui, false, width, height - footerHeight - 2);
	}
	private static void renderSeparator(GuiGraphics gui, boolean header, int width, int y) {
		var inWorld = mc.level != null;
		var path = header ? inWorld ? "textures/gui/inworld_header_separator.png" : "textures/gui/header_separator.png"
			: inWorld ? "textures/gui/inworld_footer_separator.png" : "textures/gui/footer_separator.png";
		RenderSystem.enableBlend();
		gui.blit(FlexConfig.getRes(path), 0, y, 0F, 0F, width, 2, 32, 2);
		RenderSystem.disableBlend();
	}
	/**
	 * {@code KeyMapping} 在 vanilla 中没有公开的取值途径（字段为 private），NeoForge 注入了 {@code getKey()}、
	 * Fabric 需借助 Fabric API，故差异下沉到各自源集的 {@code KeybindsLoader}。
	 */
	public static Key boundKey(KeyMapping mapping) {
		return KeybindsLoader.boundKey(mapping);
	}
}
