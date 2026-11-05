package io.github.forgestove.flexconfig.client.gui.im.row;
import io.github.forgestove.flexconfig.client.ClientLockManager;
import io.github.forgestove.flexconfig.client.Translation;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import io.github.forgestove.flexconfig.client.gui.im.core.*;
import io.github.forgestove.flexconfig.client.gui.im.model.*;
import io.github.forgestove.flexconfig.client.gui.im.widget.*;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Locale;
/** 值行基类：标签、状态样式、reset/undo/lock 和行尾控件布局。 */
public abstract class ValueRow extends ConfigRow {
	protected final ValueConfigNode<Object, Object> node;
	protected final ValueRowState state = new ValueRowState();
	protected final UiActionBar actionBar = new UiActionBar();
	protected final UiIconButton reset, lock;
	private final Component label;
	private final @Nullable Component tooltip;
	private final UiAnim hoverAnim = new UiAnim(0, UiMotion.HOVER_IN);
	private final UiTween dot = new UiTween(0);
	private final UiTween wash = new UiTween(1);
	private boolean hoverWas;
	private String highlightQuery = "";
	private boolean controlBelow;
	@SuppressWarnings("unchecked")
	public ValueRow(RowContext ctx, ValueConfigNode<?, ?> node) {
		super(ctx);
		this.node = (ValueConfigNode<Object, Object>) node;
		label = node.getTitle().copy();
		tooltip = node.getTooltip();
		reset = new UiIconButton(FlexGuiTextures.ICON_RESET, this::resetToDefault);
		reset.tooltip(Translation.RESET_TOOLTIP);
		lock = new UiIconButton(FlexGuiTextures.ICON_UNLOCK, this::toggleLock);
		actionBar.add(lock, UiTheme.ACTION_SIZE).add(reset, UiTheme.ACTION_SIZE);
	}
	protected Object value() {
		return node.getEditingValue(ctx.config());
	}
	protected void setValue(Object value) {
		node.setEditingValue(value);
		ctx.refresh();
	}
	/** 拖动等高频路径：只写编辑值，不触发整页刷新。 */
	protected void writeValue(Object value) {
		node.setEditingValue(value);
	}
	/** 元素行等需要在行尾追加控件（例如删除按钮）。 */
	public void addAction(UiControl control, int width) {
		actionBar.add(control, width);
	}
	public ValueConfigNode<Object, Object> node() {
		return node;
	}
	@Override
	public void prepare(UiContext ui) {
		var hot = hovered(ui) || focusedWithin(ui);
		if (hot != hoverWas) {
			hoverAnim.retune(hot ? UiMotion.HOVER_IN : UiMotion.HOVER_OUT);
			hoverAnim.target(hot ? 1 : 0);
			hoverWas = hot;
		}
		hoverAnim.update(ui.delta);
		var dotTarget = isChanged() ? 1F : 0F;
		if (dot.target() != dotTarget) dot.start(dotTarget, UiMotion.DOT);
		dot.update(ui.delta);
		wash.update(ui.delta);
		reset.setVisible(hot && isChanged() && !state.readOnly);
		lock.setVisible(hot && state.showLock && allowLock());
	}
	public void wash() {
		wash.start(0, 1, UiMotion.RESET_WASH.durationMs(), UiMotion.RESET_WASH.easing(), 0);
	}
	public void setHighlight(String query) {
		highlightQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
	}
	public String valueText() {
		var value = value();
		return value == null ? "" : String.valueOf(value);
	}
	public boolean canReset() {
		return !state.readOnly;
	}
	private boolean focusedWithin(UiContext ui) {
		var current = ui.focus.current();
		if (current == null) return false;
		for (var control : controls()) if (control == current) return true;
		return false;
	}
	protected boolean allowLock() {
		return true;
	}
	protected boolean showDescription() {
		return true;
	}
	protected int titleTop() {
		return controlBelow ? bounds().y() + 5 : bounds().y() + (bounds().height() - 20) / 2 + 1;
	}
	@Override
	public int heightFor(int width) {
		controlBelow = needsControlBelow(width);
		return UiTheme.ROW_HEIGHT + (controlBelow ? UiTheme.CONTROL_HEIGHT + 6 : 0);
	}
	private boolean needsControlBelow(int width) {
		var used = actionBar.usedWidth();
		var available = width - UiTheme.INDENT - depth * UiTheme.INDENT - used - UiTheme.PADDING * 2;
		return used > width * 0.4F && available < 110;
	}
	@Override
	public void layout() {
		if (controlBelow) actionBar.layout(bounds().right() - UiTheme.PADDING, bounds().bottom() - UiTheme.CONTROL_HEIGHT - 4, UiTheme.CONTROL_HEIGHT);
		else actionBar.layout(bounds().right() - UiTheme.PADDING, bounds().y(), bounds().height());
	}
	@Override
	public void render(UiContext ui) {
		var painter = ui.painter;
		var hoverValue = (float) hoverAnim.get();
		if (hoverValue > 0.01F) painter.rect(bounds(), UiTheme.mulAlpha(UiTheme.ROW_HOVER, hoverValue));
		if (!wash.done()) {
			var t = wash.value();
			painter.rect(bounds().x(), bounds().y(), Math.round(bounds().width() * t), bounds().height(), UiTheme.mulAlpha(ui.accent().wash(), 1F - t));
		}
		painter.groove(bounds().x(), bounds().right(), bounds().bottom() - 2);
		var left = bounds().x() + UiTheme.INDENT + depth * UiTheme.INDENT;
		var dotSize = Math.round(2F * dot.value());
		if (dotSize > 0) {
			painter.rect(left, titleTop() + 3, dotSize, dotSize, ui.accent().base());
			left += 6;
		}
		var available = Math.max(20, bounds().width() - (left - bounds().x()) - (controlBelow ? 0 : actionBar.usedWidth()) - UiTheme.PADDING * 2);
		var title = painter.trim(label.getString(), available);
		var titleY = titleTop();
		painter.text(title, left, titleY, titleColor());
		if (!highlightQuery.isEmpty()) {
			var lower = title.toLowerCase(Locale.ROOT);
			var from = 0;
			while (from < lower.length()) {
				var at = lower.indexOf(highlightQuery, from);
				if (at < 0) break;
				var x0 = left + painter.textWidth(title.substring(0, at));
				var x1 = x0 + painter.textWidth(title.substring(at, at + highlightQuery.length()));
				painter.rect(x0, titleY + 9, x1 - x0, 1, ui.accent().base());
				from = at + highlightQuery.length();
			}
		}
		var description = tooltip == null || !showDescription() ? "" : tooltip.getString().replace('\n', ' ');
		if (!description.isEmpty()) painter.text(painter.trim(description, available), left, titleY + 11, UiTheme.TEXT_2);
		actionBar.render(ui);
		if (ui.tooltip() == null && hovered(ui)) {
			var text = tooltip();
			if (text != null) ui.tooltip(text, bounds());
		}
	}
	private int titleColor() {
		if (state.locked) return UiTheme.LOCKED;
		if (state.readOnly) return UiTheme.DISABLED;
		if (hasError()) return UiTheme.DANGER_LIGHT;
		return UiTheme.TEXT;
	}
	protected @Nullable Component tooltip() {
		if (tooltip == null && state.validationError == null) return null;
		var out = label.copy();
		if (tooltip != null) out.append(Component.literal("\n")).append(tooltip);
		if (state.validationError != null) out.append(Component.literal("\n")).append(state.validationError.copy().withStyle(ChatFormatting.RED));
		return out;
	}
	@Override
	public List<UiControl> controls() {
		return actionBar.controls();
	}
	@Override
	public void refresh() {
		state.refresh(node, ctx);
		reset.setEnabled(!state.readOnly && !node.isDefaultValue(ctx.config()));
		lock.setEnabled(state.showLock && state.canLock);
		lock.setIcon(state.locked ? FlexGuiTextures.ICON_LOCK : FlexGuiTextures.ICON_UNLOCK);
		lock.setTooltip(state.locked ? Translation.LOCKED_TOOLTIP : Translation.UNLOCK_TOOLTIP);
	}
	public void resetToDefault() {
		if (state.readOnly) return;
		if (!node.isElement()) ClientLockManager.clearPendingLock(ctx.fileKey(), node.getLockPath());
		node.resetToDefault();
		wash();
		ctx.refresh();
	}
	public void resetToActive() {
		if (!node.isElement()) ClientLockManager.clearPendingLock(ctx.fileKey(), node.getLockPath());
		node.resetToActive(ctx.config());
		ctx.refresh();
	}
	private void toggleLock() {
		ClientLockManager.setPendingLock(ctx.fileKey(), node.getLockPath(), !state.locked);
		ctx.refresh();
	}
	@Override
	public boolean hasError() {
		return state.validationError != null;
	}
	@Override
	public boolean isChanged() {
		return state.changed;
	}
}
