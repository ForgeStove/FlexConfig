package io.github.forgestove.flexconfig.foundation.ui;
import io.github.forgestove.flexconfig.api.FileKey;
import io.github.forgestove.flexconfig.api.menu.Accent;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import io.github.forgestove.flexconfig.foundation.tree.RootConfigNode;
import io.github.forgestove.flexconfig.foundation.ui.ConfigInput.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.*;

import java.util.Set;
/** 屏幕适配层：只处理 Screen 的打开、关闭和坐标转发，界面编排在 {@link ConfigShell}。 */
public class ConfigScreen<C, V> extends Screen implements ConfigHost {
	public final RootConfigNode root;
	private final Screen parent;
	private final ConfigShell<C, V> shell;
	public ConfigScreen(ConfigHandler<C, V> handler) {
		super(handler.getConfigTree().getTitle());
		root = handler.getConfigTree();
		parent = ClientUtil.mc.screen;
		shell = new ConfigShell<>(this, handler);
	}
	@Override
	protected void init() {
		shell.init();
	}
	@Override
	protected void repositionElements() {
		shell.resize();
	}
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		if (shell.closed()) return;
		renderBackground(graphics, mouseX, mouseY, delta);
		shell.frame(graphics, mouseX, mouseY, delta);
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return shell.input(new MouseDown(mouseX, mouseY, button));
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return shell.input(new MouseUp(mouseX, mouseY, button));
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return shell.input(new MouseDrag(mouseX, mouseY, button, dragX, dragY));
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return shell.input(new Scroll(mouseX, mouseY, scrollX, scrollY));
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return shell.input(new Key(keyCode, scanCode, modifiers));
	}
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		return shell.input(new Char(codePoint, modifiers));
	}
	@Override
	public void onClose() {
		shell.onClose();
	}
	@Override
	public void removed() {
		shell.onRemoved();
	}
	public void refresh() {
		shell.refresh();
	}
	public Set<FileKey> editedFiles() {
		return shell.editedFiles();
	}
	public Accent accent() {
		return shell.accent();
	}
	@Override
	public int guiWidth() {
		return width;
	}
	@Override
	public int guiHeight() {
		return height;
	}
	@Override
	public boolean shiftDown() {
		return hasShiftDown();
	}
	@Override
	public Screen parentScreen() {
		return parent;
	}
	@Override
	public void setScreen(@Nullable Screen screen) {
		ClientUtil.mc.setScreen(screen);
	}
	@Override
	public void copyToClipboard(String text) {
		ClientUtil.mc.keyboardHandler.setClipboard(text);
	}
}
