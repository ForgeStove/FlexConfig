package io.github.forgestove.flexconfig.client.ui.screen;
import io.github.forgestove.flexconfig.client.ui.core.*;
import io.github.forgestove.flexconfig.client.ui.widget.*;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.function.Supplier;
/** 左侧栏顶部的操作区：完成按钮和状态文字。 */
public class FooterView {
	private static final int BUTTON_HEIGHT = 18;
	private final UiButton done;
	private final Supplier<Component> status;
	private UiBounds bounds = UiBounds.EMPTY;
	public FooterView(UiButton done, Supplier<Component> status) {
		this.done = done;
		this.status = status;
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
		var text = status.get();
		if (text != null) ui.painter.text(ui.painter.trim(text.getString(), bounds.width()), bounds.x(), done.bounds().bottom() + 4, UiTheme.TEXT_3);
		done.render(ui);
	}
	public boolean mouseClicked(UiContext ui, double mouseX, double mouseY, int button) {
		return done.mouseClicked(ui, mouseX, mouseY, button);
	}
}
