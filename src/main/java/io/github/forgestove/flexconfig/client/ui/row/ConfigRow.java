package io.github.forgestove.flexconfig.client.ui.row;
import io.github.forgestove.flexconfig.client.ui.core.*;
import io.github.forgestove.flexconfig.client.ui.model.RowContext;
import io.github.forgestove.flexconfig.client.ui.widget.UiControl;
import java.util.List;
/** 一行配置：布局和绘制每帧进行，命中用上一帧末或当帧开头写入的 bounds。 */
public abstract class ConfigRow {
	protected final RowContext ctx;
	protected int depth;
	private UiBounds bounds = UiBounds.EMPTY;
	private final UiTween reveal = new UiTween(1);
	protected ConfigRow(RowContext ctx) {
		this.ctx = ctx;
	}
	public void reveal(float delayMs) {
		reveal.start(0, 1, UiMotion.CONTENT_ROWS_IN.durationMs(), UiMotion.CONTENT_ROWS_IN.easing(), delayMs);
	}
	public void updateReveal(float delta) {
		reveal.update(delta);
	}
	public float revealValue() {
		return reveal.value();
	}
	public int depth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public UiBounds bounds() {
		return bounds;
	}
	public void setBounds(UiBounds bounds) {
		this.bounds = bounds;
	}
	public int height() {
		return UiTheme.ROW_HEIGHT + UiTheme.ROW_GAP;
	}
	public int heightFor(int width) {
		return height();
	}
	public boolean hovered(UiContext ui) {
		return bounds.contains(ui.mouseX, ui.mouseY);
	}
	public void prepare(UiContext ui) {}
	public void layout() {}
	public abstract void render(UiContext ui);
	public List<UiControl> controls() {
		return List.of();
	}
	public boolean mouseClicked(UiContext ui, double mouseX, double mouseY, int button) {
		for (var control : controls()) if (control.mouseClicked(ui, mouseX, mouseY, button)) return true;
		return false;
	}
	public boolean mouseReleased(UiContext ui, double mouseX, double mouseY, int button) {
		for (var control : controls()) if (control.mouseReleased(ui, mouseX, mouseY, button)) return true;
		return false;
	}
	public boolean keyPressed(UiContext ui, int keyCode, int scanCode, int modifiers) {
		for (var control : controls()) if (control.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		return false;
	}
	public boolean charTyped(UiContext ui, char codePoint) {
		for (var control : controls()) if (control.charTyped(ui, codePoint)) return true;
		return false;
	}
	public void refresh() {}
	public boolean hasError() {
		return false;
	}
	public boolean isChanged() {
		return false;
	}
}
