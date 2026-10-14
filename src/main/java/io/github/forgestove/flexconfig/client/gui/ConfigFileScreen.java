package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.gui.components.OreButton;
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
	/** 文件按钮之间的间距。 */
	private static final int SPACING = 4;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final FrameLayout centerFrame = new FrameLayout();
	private final String modId;
	private final List<ConfigHandler<?, ?>> handlers;
	@Nullable private final Screen parent;
	public ConfigFileScreen(String modId, List<ConfigHandler<?, ?>> handlers) {
		super(ClientUtil.modName(modId));
		this.modId = modId;
		this.handlers = handlers;
		parent = ClientUtil.mc.screen;
	}
	@Override
	protected void init() {
		layout.addTitleHeader(title, font);
		var files = LinearLayout.vertical().spacing(SPACING);
		handlers.forEach(handler -> files.addChild(fileButton(handler)));
		centerFrame.addChild(files);
		layout.addToContents(centerFrame);
		layout.addToFooter(OreButton.of(Translation.BACK_LABEL, b -> onClose()).width(OreButton.DEFAULT_WIDTH).build());
		layout.visitWidgets(this::addRenderableWidget);
		repositionElements();
	}
	/** 一份配置文件一行；{@link FlexConfigScreenFactory#available} 为假时置灰并说明。 */
	private OreButton fileButton(ConfigHandler<?, ?> handler) {
		var available = FlexConfigScreenFactory.available(handler);
		var button = OreButton.of(label(handler), b -> open(handler)).width(OreButton.DEFAULT_WIDTH).build();
		button.active = available;
		button.setTooltip(available
			? Tooltip.create(Component.literal(handler.getFileName()))
			: Tooltip.create(Translation.tooltip(Translation.SERVER_CONFIG_UNAVAILABLE, Translation.SERVER_CONFIG_UNAVAILABLE_TOOLTIP)));
		return button;
	}
	@Override
	public void onClose() {
		ClientUtil.mc.setScreen(parent);
	}
	@Override
	protected void repositionElements() {
		centerFrame.setMinHeight(layout.getContentHeight());   // 内容区高同样随窗口尺寸变
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
	@Override
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		super.render(gui, mouseX, mouseY, delta);
		ClientUtil.renderSeparators(gui, width, height, layout.getHeaderHeight(), layout.getFooterHeight());
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) onClose();
		return false;
	}
}
