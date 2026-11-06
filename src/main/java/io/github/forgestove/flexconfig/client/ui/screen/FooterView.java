package io.github.forgestove.flexconfig.client.ui.screen;
import io.github.forgestove.flexconfig.client.ui.core.*;
import io.github.forgestove.flexconfig.client.ui.widget.*;

import java.util.List;
/** 左侧栏顶部的操作区：完成按钮。 */
public class FooterView {
	private static final int BUTTON_HEIGHT = 18;
	private final UiButton done;
	private UiBounds bounds = UiBounds.EMPTY;
	public FooterView(UiButton done) {
		this.done = done;
	}
	public void setBounds(UiBounds bounds) {
		this.bounds = bounds;
	}
	public List<UiControl> controls() {
		return List.of(done);
	}
	public void layout() {
		done.setBounds(new UiBounds(bounds.x(), bounds.y(), bounds.width(), BUTTON_HEIGHT));
	}
	public void render(UiContext ui) {
		layout();
		done.render(ui);
	}
	public boolean mouseClicked(UiContext ui, double mouseX, double mouseY, int button) {
		return done.mouseClicked(ui, mouseX, mouseY, button);
	}
}
