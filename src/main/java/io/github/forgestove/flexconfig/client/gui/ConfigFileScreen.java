package io.github.forgestove.flexconfig.client.gui;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.gui.components.OreButton;
import io.github.forgestove.flexconfig.client.gui.popup.OreListPopupScreen;
import io.github.forgestove.flexconfig.platform.Platform;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;
import org.lwjgl.glfw.GLFW;

import java.util.*;
/**
 * 一个模组有多份配置文件时的入口界面：列出全部配置文件，选中的那一份才进入 {@link ConfigScreen}。
 * <p>
 * 只有一份配置的模组不会走到这里（{@code FlexConfigScreenFactory} 直接打开它），所以单文件模组的观感不受影响。
 */
public final class ConfigFileScreen extends Screen {
	private static final int BUTTON_WIDTH = 200;
	/** 文件按钮之间的间距。 */
	private static final int SPACING = 4;
	/** 底栏右侧按钮与屏幕边缘的留白。 */
	private static final int FOOTER_MARGIN = 4;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	/** 撑满内容区的空框：布局默认把内容贴在头部下方 30px，撑满后子控件才是真正垂直居中。 */
	private final FrameLayout centerFrame = new FrameLayout();
	private final String modId;
	private final List<ConfigHandler<?, ?>> handlers;
	@Nullable private final Screen parent;
	public ConfigFileScreen(String modId, List<ConfigHandler<?, ?>> handlers) {
		super(displayName(modId));
		this.modId = modId;
		this.handlers = handlers;
		parent = ClientUtil.mc.screen;
	}
	private static Component displayName(String modId) {
		var name = Platform.get().modDisplayName(modId);
		return Component.literal(name != null ? name : modId);
	}
	@Override
	protected void init() {
		layout.addTitleHeader(title, font);
		var files = LinearLayout.vertical().spacing(SPACING);
		handlers.forEach(handler -> files.addChild(fileButton(handler)));
		centerFrame.addChild(files);
		layout.addToContents(centerFrame);
		layout.addToFooter(OreButton.of(Translation.BACK_LABEL, b -> onClose()).width(BUTTON_WIDTH).build());
		layout.addToFooter(othersButton(), s -> s.alignHorizontallyRight().paddingRight(FOOTER_MARGIN));
		layout.visitWidgets(this::addRenderableWidget);
		repositionElements();
	}
	/** 一份配置文件一行；{@link FlexConfigScreenFactory#available} 为假时置灰并说明。 */
	private OreButton fileButton(ConfigHandler<?, ?> handler) {
		var available = FlexConfigScreenFactory.available(handler);
		var button = OreButton.of(label(handler), b -> open(handler)).width(BUTTON_WIDTH).build();
		button.active = available;
		button.setTooltip(available
			? Tooltip.create(Component.literal(handler.getFileName()))
			: Tooltip.create(Translation.tooltip(Translation.SERVER_CONFIG_UNAVAILABLE, Translation.SERVER_CONFIG_UNAVAILABLE_TOOLTIP)));
		return button;
	}
	private OreButton othersButton() {
		var others = otherMods();
		var button = OreButton.of(Translation.OTHER_MODS_LABEL, b -> openModPicker(others)).width(BUTTON_WIDTH).build();
		button.active = !others.isEmpty();
		if (others.isEmpty()) button.setTooltip(Tooltip.create(Translation.OTHER_MODS_NONE));
		return button;
	}
	@Override
	public void onClose() {
		ClientUtil.mc.setScreen(parent);
	}
	@Override
	protected void repositionElements() {
		centerFrame.setMinHeight(layout.getContentHeight());   // 内容区高随窗口尺寸变，居中框要跟着
		layout.arrangeElements();
	}
	/** 有翻译就用翻译，否则退回文件名。 */
	private Component label(ConfigHandler<?, ?> handler) {
		var key = "%s.config.file.%s".formatted(modId, handler.getType().extension());
		return Component.translatableWithFallback(key, handler.getFileName());
	}
	private void open(ConfigHandler<?, ?> handler) {
		ClientUtil.mc.setScreen(new ConfigScreen<>(handler));
	}
	private List<String> otherMods() {
		return ConfigRegistry.getRegisteredFileKeys()
			.stream()
			.map(FileKey::modId)
			.distinct()
			.sorted(Comparator.comparing(id -> displayName(id).getString(), String.CASE_INSENSITIVE_ORDER))
			.toList();
	}
	private void openModPicker(List<String> modIds) {
		ClientUtil.mc.setScreen(new OreListPopupScreen<>(
			this,
			Translation.OTHER_MODS_TITLE,
			modIds,
			ConfigFileScreen::displayName,
			() -> null,
			this::openMod,
			true
		));
	}
	private void openMod(String modId) {
		ClientUtil.mc.setScreen(FlexConfigScreenFactory.createConfigScreen(modId));
	}
	@Override
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		super.render(gui, mouseX, mouseY, delta);
		var inWorld = ClientUtil.mc.level != null;
		var header = inWorld ? INWORLD_HEADER_SEPARATOR : HEADER_SEPARATOR;
		var footer = inWorld ? INWORLD_FOOTER_SEPARATOR : FOOTER_SEPARATOR;
		RenderSystem.enableBlend();
		gui.blit(header, 0, layout.getHeaderHeight() - 2, 0F, 0F, width, 2, 32, 2);
		gui.blit(footer, 0, height - layout.getFooterHeight(), 0F, 0F, width, 2, 32, 2);
		RenderSystem.disableBlend();
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) onClose();
		return false;
	}

}
