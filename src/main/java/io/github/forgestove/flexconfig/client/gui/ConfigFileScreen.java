package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.ConfigHandler;
import io.github.forgestove.flexconfig.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;
import org.lwjgl.glfw.GLFW;

import java.util.List;
/** 单份配置的模组不经过本界面，由 {@link FlexConfigScreenFactory} 直接打开 {@link ConfigScreen}。 */
public final class ConfigFileScreen extends Screen {
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
	private Button fileButton(ConfigHandler<?, ?> handler) {
		var available = FlexConfigScreenFactory.available(handler);
		var button = Button.builder(label(handler), b -> open(handler)).width(Button.DEFAULT_WIDTH).build();
		button.active = available;
		button.setTooltip(available
			? Tooltip.create(Component.literal(handler.getFileName()))
			: Tooltip.create(Translation.tooltip(Translation.SERVER_CONFIG_UNAVAILABLE, Translation.SERVER_CONFIG_UNAVAILABLE_TOOLTIP)));
		return button;
	}
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
	@Override
	public void onClose() {
		ClientUtil.mc.setScreen(parent);
	}
	@Override
	protected void init() {
		layout.addTitleHeader(title, font);
		var files = LinearLayout.vertical().spacing(SPACING);
		handlers.forEach(handler -> files.addChild(fileButton(handler)));
		centerFrame.addChild(files);
		layout.addToContents(centerFrame);
		layout.addToFooter(Button.builder(Translation.BACK_LABEL, b -> onClose()).width(Button.DEFAULT_WIDTH).build());
		layout.visitWidgets(this::addRenderableWidget);
		repositionElements();
	}
	@Override
	protected void repositionElements() {
		centerFrame.setMinHeight(layout.getContentHeight());   // 内容区高同样随窗口尺寸变
		layout.arrangeElements();
	}
}
