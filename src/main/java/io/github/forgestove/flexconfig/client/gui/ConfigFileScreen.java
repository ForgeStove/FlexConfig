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
	/** 底栏两个按钮之间的间距（与 {@link ConfigScreen} 的底栏一致）。 */
	private static final int FOOTER_SPACING = 8;
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
		var buttonWidth = width / 5;
		var files = LinearLayout.vertical().spacing(SPACING);
		handlers.forEach(handler -> files.addChild(fileButton(handler, buttonWidth)));
		centerFrame.addChild(files);
		layout.addToContents(centerFrame);
		var footer = LinearLayout.horizontal().spacing(FOOTER_SPACING);
		footer.addChild(OreButton.of(Translation.BACK_LABEL, b -> onClose()).width(buttonWidth).build());
		footer.addChild(othersButton(buttonWidth));
		layout.addToFooter(footer);
		layout.visitWidgets(this::addRenderableWidget);
		repositionElements();
	}
	/** 一份配置文件一行；{@link FlexConfigScreenFactory#available} 为假时置灰并说明。 */
	private OreButton fileButton(ConfigHandler<?, ?> handler, int width) {
		var available = FlexConfigScreenFactory.available(handler);
		var button = OreButton.of(label(handler), b -> open(handler)).width(width).build();
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
	private OreButton othersButton(int width) {
		var others = otherMods();
		var button = OreButton.of(Translation.OTHER_MODS_LABEL, b -> openModList(others)).width(width).build();
		button.active = !others.isEmpty();
		if (others.isEmpty()) button.setTooltip(Tooltip.create(Translation.OTHER_MODS_NONE));
		return button;
	}
	@Override
	protected void repositionElements() {
		var buttonWidth = width / 5;
		layout.visitWidgets(widget -> {
			if (widget instanceof OreButton button) button.setWidth(buttonWidth);
		});
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
	private List<String> otherMods() {
		return ConfigRegistry.getRegisteredFileKeys()
			.stream()
			.map(FileKey::modId)
			.distinct()
			.sorted(Comparator.comparing(id -> ClientUtil.modName(id).getString(), String.CASE_INSENSITIVE_ORDER))
			.toList();
	}
	private void openModList(List<String> modIds) {
		ClientUtil.mc.setScreen(new ConfigModListScreen(this, modIds));
	}
	@Override
	public void renderBackground(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		ClientUtil.renderBands(gui, width, height, layout.getHeaderHeight(), layout.getFooterHeight());
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
