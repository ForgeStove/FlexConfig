package io.github.forgestove.flexconfig.client.ui.row;
import io.github.forgestove.flexconfig.client.ui.core.*;
import io.github.forgestove.flexconfig.client.ui.model.RowContext;
import io.github.forgestove.flexconfig.client.ui.widget.UiControl;
import org.jetbrains.annotations.Nullable;
import java.util.List;
/** 一行配置：布局和绘制每帧进行，命中用上一帧末或当帧开头写入的 bounds。 */
public abstract class ConfigRow implements RowTransition {
	protected final RowContext ctx;
	protected int depth;
	private UiBounds bounds = UiBounds.EMPTY;
	private final UiTween reveal = new UiTween(1);
	private final UiTween heightAnim = new UiTween(1);
	private boolean exiting;
	protected ConfigRow(RowContext ctx) {
		this.ctx = ctx;
	}
	public @Nullable String configPath() {
		return null;
	}
	public @Nullable Object identity() {
		return configPath();
	}
	public void reveal(float delayMs) {
		reveal.start(0, 1, UiMotion.CONTENT_ROWS_IN.durationMs(), UiMotion.CONTENT_ROWS_IN.easing(), delayMs);
	}
	public void updateReveal(float delta) {
		reveal.update(delta);
		heightAnim.update(delta);
	}
	public float revealValue() {
		return reveal.value();
	}
	@Override
	public void onEnter() {}
	@Override
	public void onExit() {}
	@Override
	public boolean exited() {
		return true;
	}
	protected boolean heightSettled() {
		return heightAnim.target() == 0 && heightAnim.done();
	}
	public void expand() {
		expandFrom(0);
	}
	public void expandFrom(float value) {
		heightAnim.start(value, 1, UiMotion.CONTENT_ROWS_IN);
	}
	public void collapse() {
		exiting = true;
		heightAnim.start(1, 0, UiMotion.CONTENT_ROWS_IN);
	}
	public boolean exiting() {
		return exiting;
	}
	public float heightScale() {
		return heightAnim.value();
	}
	public int displayHeight(int width) {
		return Math.max(0, Math.round(heightFor(width) * heightAnim.value()));
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
		if (exiting) return false;
		for (var control : controls()) if (control.mouseClicked(ui, mouseX, mouseY, button)) return true;
		return false;
	}
	public boolean mouseReleased(UiContext ui, double mouseX, double mouseY, int button) {
		if (exiting) return false;
		for (var control : controls()) if (control.mouseReleased(ui, mouseX, mouseY, button)) return true;
		return false;
	}
	public boolean keyPressed(UiContext ui, int keyCode, int scanCode, int modifiers) {
		if (exiting) return false;
		for (var control : controls()) if (control.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		return false;
	}
	public boolean charTyped(UiContext ui, char codePoint) {
		if (exiting) return false;
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
