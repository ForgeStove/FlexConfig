package io.github.forgestove.flexconfig.foundation.ui.screen;
import io.github.forgestove.flexconfig.foundation.client.Translation;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton;
import org.lwjgl.glfw.GLFW;
/** 确认对话框。保存标题、正文和确认后的动作，打开期间由 ConfigShell 拦截其余输入。 */
public final class DialogView {
	private static final int MAX_WIDTH = 300, PAD = 8, BUTTON_WIDTH = 70, BUTTON_HEIGHT = 18;
	private boolean open;
	private UiText title = UiText.empty(), body = UiText.empty();
	private Runnable action = () -> {};
	private final UiButton confirm, cancel;
	public DialogView() {
		confirm = new UiButton(Translation.QUIT_CONFIRM_LABEL.copy().withStyle(UiStyle.BOLD), UiButton.Style.PRIMARY, this::confirm);
		cancel = new UiButton(Translation.CANCEL_LABEL, UiButton.Style.SECONDARY, this::cancel);
	}
	public boolean open() {
		return open;
	}
	public void open(UiText title, UiText body, Runnable action) {
		this.title = title;
		this.body = body;
		this.action = action;
		open = true;
	}
	public void mouseDown(UiContext ui, UiPointerRouter pointer, double mouseX, double mouseY, int button) {
		if (pointer.press(ui, cancel, mouseX, mouseY, button)) return;
		pointer.press(ui, confirm, mouseX, mouseY, button);
	}
	public boolean keyPressed(int keyCode) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) cancel();
		else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) confirm();
		return true;
	}
	public void render(UiContext ui, int logicalWidth, int logicalHeight) {
		var painter = ui.painter;
		var contentWidth = Math.clamp(
			Math.max(painter.textWidthEmphasis(title.getString()), painter.textWidth(body)),
			BUTTON_WIDTH * 2 + UiTheme.GAP,
			MAX_WIDTH - PAD * 2
		);
		var lines = painter.split(body, contentWidth);
		var lineHeight = painter.lineHeight();
		var panelWidth = contentWidth + PAD * 2;
		var panelHeight = UiTheme.HEADER_HEIGHT + PAD + lines.size() * lineHeight + PAD + BUTTON_HEIGHT + PAD;
		var panel = new UiBounds((logicalWidth - panelWidth) / 2, (logicalHeight - panelHeight) / 2, panelWidth, panelHeight);
		painter.pushZ(300);
		painter.rect(0, 0, logicalWidth, logicalHeight, 0x99000000);
		painter.rect(panel.x() + 3, panel.y() + 3, panel.width(), panel.height(), UiTheme.SHADOW);
		painter.border(panel.x() - 1, panel.y() - 1, panel.width() + 2, panel.height() + 2, UiTheme.WINDOW_EDGE);
		painter.rect(panel.x(), panel.y(), panel.width(), UiTheme.HEADER_HEIGHT - 2, UiTheme.TITLEBAR);
		painter.rect(panel.x(), panel.y() + UiTheme.HEADER_HEIGHT - 2, panel.width(), 1, UiTheme.TITLEBAR_EDGE);
		painter.rect(panel.x(), panel.y() + UiTheme.HEADER_HEIGHT - 1, panel.width(), 1, UiTheme.WINDOW_EDGE);
		painter.rect(panel.x(), panel.y() + UiTheme.HEADER_HEIGHT, panel.width(), panel.height() - UiTheme.HEADER_HEIGHT, UiTheme.PANEL);
		painter.textEmphasis(title, panel.x() + PAD, panel.y() + 7, UiTheme.TITLE_INK);
		var textY = panel.y() + UiTheme.HEADER_HEIGHT + PAD;
		for (var line : lines) {
			painter.text(line, panel.x() + PAD, textY, UiTheme.TEXT);
			textY += lineHeight;
		}
		var buttonY = panel.bottom() - PAD - BUTTON_HEIGHT;
		cancel.setBounds(new UiBounds(panel.right() - PAD - BUTTON_WIDTH, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT));
		confirm.setBounds(new UiBounds(cancel.bounds().x() - UiTheme.GAP - BUTTON_WIDTH, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT));
		cancel.render(ui);
		confirm.render(ui);
		painter.popZ();
	}
	private void confirm() {
		if (!open) return;
		open = false;
		UiSound.click();
		action.run();
	}
	private void cancel() {
		if (!open) return;
		open = false;
		UiSound.click();
	}
}
