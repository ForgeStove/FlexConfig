package io.github.forgestove.flexconfig.foundation.ui.row;
import io.github.forgestove.flexconfig.api.ConfigTranslationKeys;
import io.github.forgestove.flexconfig.foundation.client.*;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.factory.Keybinds;
import io.github.forgestove.flexconfig.foundation.ui.model.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.*;
import org.jetbrains.annotations.Nullable;

import java.awt.Point;
import java.util.*;
public abstract class ValueRow extends ConfigRow {
	protected final ValueConfigNode node;
	protected final ValueRowState state = new ValueRowState();
	protected final UiActionBar actionBar = new UiActionBar();
	protected final UiActionBar buttons = new UiActionBar();
	protected final UiIconButton reset, lock;
	private final UiText label;
	private final @Nullable UiText tooltip;
	private final UiAnim hoverAnim = new UiAnim(0, UiMotion.HOVER_IN);
	private final UiTween dot = new UiTween(0);
	private final UiTween wash = new UiTween(1);
	private boolean hoverWas;
	private String highlightQuery = "";
	private boolean controlBelow;
	public ValueRow(RowContext ctx, ValueConfigNode node) {
		super(ctx);
		this.node = node;
		label = node.getTitle();
		tooltip = node.getTooltip();
		reset = new UiIconButton(FlexGuiTextures.ICON_RESET.id, this::resetToDefault);
		reset.tooltip(Translation.RESET_TOOLTIP);
		lock = new UiIconButton(FlexGuiTextures.ICON_UNLOCK.id, this::toggleLock);
		buttons.add(lock, UiTheme.ACTION_SIZE).add(reset, UiTheme.ACTION_SIZE);
	}
	private static boolean hasCustomToString(Class<?> type) {
		try {
			return type.getMethod("toString").getDeclaringClass() != Object.class;
		} catch (NoSuchMethodException ignored) {
			return false;
		}
	}
	protected Object value() {
		return node.getEditingValue(ctx.config());
	}
	protected void setValue(Object value) {
		node.setEditingValue(value);
		ctx.refresh();
	}
	protected void writeValue(Object value) {
		node.setEditingValue(value);
	}
	public void addAction(UiControl control, int width) {
		actionBar.add(control, width);
	}
	public ValueConfigNode node() {
		return node;
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
		return !state.readOnly && (
			this instanceof TriggerKeybindAware aware && aware.hasTriggerKeybind() || !node.isDefaultValue(ctx.config())
		);
	}
	public UiText resetLabel() {
		return Translation.resetTo(defaultText());
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
		if (controlBelow) return bounds().y() + 5;
		return bounds().y() + (bounds().height() - 20 - descriptionExtra(bounds().width())) / 2 + 1;
	}
	protected int titleRightInset() {
		return buttons.usedWidth() + UiTheme.GAP;
	}
	protected int descriptionLineCount(int width) {
		if (tooltip == null || !showDescription()) return 0;
		return UiTexts.split(tooltip, descriptionWidth(width)).size();
	}
	/** 描述文本的可用宽度；高度计算和绘制共用，避免换行行数与实际渲染不一致。 */
	private int descriptionWidth(int width) {
		var inset = UiTheme.INDENT + depth * UiTheme.INDENT + titleRightInset() + UiTheme.PADDING * 2;
		if (dot.value() > 0.01 || !isDefaultValue()) inset += 6;
		if (!controlBelow) inset += actionBar.usedWidth();
		return Math.max(20, width - inset);
	}
	protected int descriptionExtra(int width) {
		return Math.max(0, descriptionLineCount(width) - 1) * 12;
	}
	private boolean needsControlBelow(int width) {
		var used = actionBar.usedWidth();
		var available = width - UiTheme.INDENT - depth * UiTheme.INDENT - used - UiTheme.PADDING * 2;
		return used > width * 0.4F && available < 110;
	}
	private int titleColor() {
		if (state.locked) return UiTheme.LOCKED;
		if (state.readOnly) return UiTheme.DISABLED;
		if (hasError()) return UiTheme.DANGER_LIGHT;
		return UiTheme.TEXT;
	}
	protected @Nullable UiText tooltip() {
		// 描述已经画在行内，悬浮提示不再重复
		var out = label.copy();
		var hasBody = false;
		var defaultValue = defaultText();
		if (!defaultValue.isEmpty()) {
			var hint = Translation.tooltipDefault(defaultValue).copy().withStyle(UiStyle.GRAY);
			out = out.append("\n").append(hint);
			hasBody = true;
			if (!node.isDefaultValue(ctx.config()))
				out = out.append("\n").append(Translation.TOOLTIP_CHANGED.copy().withStyle(UiStyle.GRAY));
		}
		if (state.validationError != null) {
			out = out.append("\n").append(state.validationError.copy().withStyle(UiStyle.RED));
			hasBody = true;
		}
		return hasBody ? out : null;
	}
	protected String defaultText() {
		return displayValue(node.getDefaultValue());
	}
	/** 值的显示文本：集合递归、枚举用翻译、按键用显示名、布尔用开关标记。 */
	protected String displayValue(@Nullable Object value) {
		var keybindName = value == null ? null : Keybinds.support().displayValue(value);
		if (keybindName != null) return keybindName.getString();
		switch (value) {
			case null -> {
				return "";
			}
			case List<?> list -> {
				var parts = new ArrayList<String>(list.size());
				for (var element : list) parts.add(displayValue(element));
				return "[" + String.join(", ", parts) + "]";
			}
			case Enum<?> constant -> {
				return UiText.translatable(ConfigTranslationKeys.enumValue(ctx.modId(), constant)).getString();
			}
			case Boolean bool -> {
				return (bool ? Translation.ON_LABEL : Translation.OFF_LABEL).getString();
			}
			case Point point -> {
				return point.x + ", " + point.y;
			}
			default -> {
			}
		}
		// 没有自定义 toString 的对象只显示类型名，避免默认值提示里出现 Class@hash
		if (hasCustomToString(value.getClass())) try {
			return value.toString();
		} catch (RuntimeException ignored) {}
		return value.getClass().getSimpleName();
	}
	public void resetToDefault() {
		if (state.readOnly) return;
		writeDefault();
		wash();
		ctx.refresh();
	}
	public void resetToActive() {
		writeActive();
		ctx.refresh();
	}
	/** 只改节点、不刷新；集合行要等重建出新行之后再刷新。 */
	protected void writeDefault() {
		if (!node.isElement()) ClientLockManager.clearPendingLock(ctx.fileKey(), node.getLockPath());
		node.resetToDefault();
	}
	protected void writeActive() {
		if (!node.isElement()) ClientLockManager.clearPendingLock(ctx.fileKey(), node.getLockPath());
		node.resetToActive(ctx.config());
	}
	private void toggleLock() {
		ClientLockManager.setPendingLock(ctx.fileKey(), node.getLockPath(), !state.locked);
		ctx.refresh();
	}
	@Override
	public String configPath() {
		return node.getPath();
	}
	@Override
	public void onEnter() {
		expand();
	}
	@Override
	public void onExit() {
		collapse();
	}
	@Override
	public boolean exited() {
		return exiting() && heightSettled();
	}
	@Override
	public int heightFor(int width) {
		controlBelow = needsControlBelow(width);
		return UiTheme.ROW_HEIGHT + (controlBelow ? UiTheme.CONTROL_HEIGHT + 6 : 0) + descriptionExtra(width);
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
		var altered = !isDefaultValue();
		var dotTarget = altered ? 1F : 0F;
		if (dot.target() != dotTarget) dot.start(dotTarget, UiMotion.DOT);
		dot.update(ui.delta);
		wash.update(ui.delta);
		reset.setVisible(canReset());
		lock.setVisible(hot && state.showLock && allowLock());
	}
	@Override
	public void layout() {
		var right = bounds().right() - UiTheme.PADDING;
		if (controlBelow) actionBar.layout(right, bounds().bottom() - UiTheme.CONTROL_HEIGHT - 4, UiTheme.CONTROL_HEIGHT);
		else actionBar.layout(right, bounds().y(), bounds().height());
		var controlsOnTitle = !controlBelow && !actionBar.controls().isEmpty();
		buttons.layout(
			controlsOnTitle ? right - actionBar.usedWidth() - UiTheme.GAP : right,
			bounds().y() + (bounds().height() - UiTheme.CONTROL_HEIGHT) / 2,
			UiTheme.CONTROL_HEIGHT
		);
	}
	@Override
	public void render(UiContext ui) {
		var painter = ui.painter;
		var hoverValue = (float) hoverAnim.get();
		if (hoverValue > 0.01F) painter.rect(bounds(), UiTheme.mulAlpha(UiTheme.ROW_HOVER, hoverValue));
		if (!wash.done()) {
			var t = wash.value();
			painter.rect(
				bounds().x(),
				bounds().y(),
				Math.round(bounds().width() * t),
				bounds().height(),
				UiTheme.mulAlpha(ui.accent().wash(), 1F - t)
			);
		}
		painter.groove(bounds().x(), bounds().right(), bounds().bottom() - 2);
		var left = bounds().x() + UiTheme.INDENT + depth * UiTheme.INDENT;
		var dotSize = Math.round(2F * dot.value());
		if (dotSize > 0) {
			painter.rect(left, titleTop() + 3, dotSize, dotSize, ui.accent().bright());
			left += 6;
		}
		var available = descriptionWidth(bounds().width());
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
		if (tooltip != null && showDescription()) {
			var lineY = titleY + 11;
			for (var line : UiTexts.split(tooltip, available)) {
				painter.text(line, left, lineY, UiTheme.TEXT_2);
				lineY += 12;
			}
		}
		actionBar.render(ui);
		buttons.render(ui);
		if (!exiting() && hovered(ui)) ui.hover(this, this::tooltip, bounds());
	}
	@Override
	public List<UiControl> controls() {
		var controls = new ArrayList<>(actionBar.controls());
		controls.addAll(buttons.controls());
		return controls;
	}
	@Override
	public void refresh() {
		state.refresh(node, ctx);
		reset.setEnabled(canReset());
		lock.setEnabled(state.showLock && state.canLock);
		lock.setIcon((state.locked ? FlexGuiTextures.ICON_LOCK : FlexGuiTextures.ICON_UNLOCK).id);
		lock.setTooltip(state.locked ? Translation.LOCKED_TOOLTIP : Translation.UNLOCK_TOOLTIP);
	}
	@Override
	public boolean hasError() {
		return state.validationError != null;
	}
	@Override
	public boolean isChanged() {
		return state.changed;
	}
	@Override
	public boolean isDefaultValue() {
		return node.isDefaultValue(ctx.config());
	}
}
