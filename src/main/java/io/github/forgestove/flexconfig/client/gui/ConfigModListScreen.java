package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.FlexConfigScreenFactory;
import io.github.forgestove.flexconfig.client.Translation;
import io.github.forgestove.flexconfig.client.gui.components.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;
import org.lwjgl.glfw.GLFW;

import java.util.*;
/**
 * 「访问其他模组的配置」：列出所有注册了本库配置的模组，点一行进它的配置界面；带搜索框，Ctrl+F 可聚焦。
 * <p>
 * 几何只有一处：内容区高 = 屏高 − 页眉 − 页脚，列表再从中让出搜索框那一行。刻意不读
 * {@code HeaderAndFooterLayout.getContentHeight()} —— 它给的是内容容器自己的高，列表控件按它撑开就会盖住页脚。
 */
public final class ConfigModListScreen extends Screen {
	/** 搜索框高度、它与列表之间的间距、内容区左右留白。 */
	private static final int SEARCH_H = 16, SPACING = 4, MARGIN = 20;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final OreListWidget<String> listWidget;
	private final Screen parent;
	@Nullable private OreEditBox search;
	public ConfigModListScreen(@NotNull Screen parent, @NotNull List<String> modIds) {
		super(Translation.OTHER_MODS_TITLE);
		this.parent = parent;
		// 正在配置的那个模组标绿；页面是从它的界面里进来的，所以问当前界面是谁
		var list = new OreList<>(modIds, ClientUtil::modName, this::currentModId, Translation.OTHER_MODS_NO_MATCH);
		listWidget = new OreListWidget<>(list, modId -> ClientUtil.mc.setScreen(FlexConfigScreenFactory.createConfigScreen(modId)));
	}
	/** 当前界面正在配置的模组 id；不是从某个配置界面进来的就没有（于是没有行被标绿）。 */
	private @Nullable String currentModId() {
		return ClientUtil.mc.screen instanceof ConfigScreen<?, ?> screen ? screen.root.modId() : null;
	}
	@Override
	protected void init() {
		layout.addTitleHeader(title, font);
		search = new OreEditBox(font, 0, 0, 0, SEARCH_H, Component.empty());
		search.setHint(Translation.OTHER_MODS_SEARCH);
		search.setResponder(listWidget::filter);
		var content = LinearLayout.vertical().spacing(SPACING);
		content.addChild(search);
		content.addChild(listWidget);
		layout.addToContents(content);
		layout.addToFooter(OreButton.of(Translation.BACK_LABEL, b -> onClose()).width(width / 5).build());
		layout.visitWidgets(this::addRenderableWidget);
		// 先定列表高度，布局才量得到正确的内容高；排完再补一次位置（列表按矩形自己算命中，无需别的同步）
		listWidget.setHeight(listWidget.pixelHeight(contentHeight() - SEARCH_H - SPACING));
		repositionElements();
	}
	@Override
	protected void repositionElements() {
		var contentWidth = width - MARGIN * 2;
		if (search != null) search.setWidth(contentWidth);
		listWidget.setWidth(contentWidth);
		layout.arrangeElements();
		listWidget.syncArea();
	}
	/** 内容区（页眉与页脚之间）的可用高。 */
	private int contentHeight() {
		return height - layout.getHeaderHeight() - layout.getFooterHeight();
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
		if (search != null && hasControlDown() && keyCode == GLFW.GLFW_KEY_F) {
			setFocused(search);
			return true;
		}
		switch (keyCode) {
			case GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_KEY_ESCAPE -> onClose();
			case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> confirm(listWidget.defaultOption());
			default -> {
				return false;
			}
		}
		return true;
	}
	/** 键盘确认：打开某一行的配置界面；没有可选项就什么都不做。 */
	private void confirm(@Nullable String modId) {
		if (modId != null) ClientUtil.mc.setScreen(FlexConfigScreenFactory.createConfigScreen(modId));
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
	public void onClose() {
		ClientUtil.mc.setScreen(parent);
	}
}
