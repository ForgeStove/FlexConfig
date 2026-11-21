package io.github.forgestove.flexconfig.foundation.client.screen;
import io.github.forgestove.flexconfig.foundation.client.*;
import io.github.forgestove.flexconfig.foundation.ui.ConfigInput.*;
import io.github.forgestove.flexconfig.foundation.ui.UiView;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.*;
/** Screen 适配器：把原版生命周期、绘制和输入原样转发给平台无关的 {@link UiView}。 */
public class MinecraftScreen extends Screen {
	private @Nullable UiView view;
	private final UiText uiTitle;
	protected MinecraftScreen(UiText title) {
		super(Translation.toComponent(title));
		uiTitle = title;
	}
	/** 子类构造视图后再接入，视图需要适配器实例作为宿主。 */
	protected final void setView(@Nullable UiView view) {
		this.view = view;
	}
	protected final UiText uiTitle() {
		return uiTitle;
	}
	@Override
	protected void init() {
		if (view != null) view.init();
	}
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		if (view == null) return;
		renderBackground(graphics, mouseX, mouseY, delta);
		view.frame(new MinecraftRenderTarget(graphics), mouseX, mouseY, seconds());
	}
	private static float seconds() {
		return ClientUtil.mc.getTimer().getRealtimeDeltaTicks() / 20F;
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return view != null && view.input(new MouseDown(mouseX, mouseY, button));
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return view != null && view.input(new MouseUp(mouseX, mouseY, button));
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return view != null && view.input(new MouseDrag(mouseX, mouseY, button, dragX, dragY));
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return view != null && view.input(new Scroll(mouseX, mouseY, scrollX, scrollY));
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return view != null && view.input(new Key(keyCode, scanCode, modifiers));
	}
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		return view != null && view.input(new Char(codePoint, modifiers));
	}
	@Override
	public void onClose() {
		if (view != null) view.onClose();
	}
	@Override
	public void removed() {
		if (view != null) view.onRemoved();
	}
	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		super.resize(minecraft, width, height);
		if (view != null) view.resize();
	}
}
