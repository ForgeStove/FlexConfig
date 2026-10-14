package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.FileKey;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.gui.*;
import io.github.forgestove.flexconfig.client.gui.components.OreIconButton;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.*;

import java.util.*;
public abstract class ValueConfigEntry<C, V> extends ConfigEntry {
	/**
	 * 值区拆两半时每半的宽度。
	 * <p>
	 * 值区总宽固定是 {@link #WIDTH}（<b>所有类型都要对齐</b>，否则整排左缘会参差），
	 * 半宽 = {@code (WIDTH - GAP) / 2} 已经扣掉中间那一个 {@link #GAP}。直接写 {@code WIDTH / 2} 会让这一行
	 * 比别的类型多出一个 GAP。
	 */
	public static final int HALF_VALUE_WIDTH = (WIDTH - GAP) / 2;
	public final OreIconButton resetButton;
	public final OreIconButton undoButton;
	public final OreIconButton lockButton;
	public final ConfigCategoryTab<C, V> tab;
	public final List<AbstractWidget> children = new ArrayList<>();
	public final ValueConfigNode<C, V> node;
	public final Component label;
	@Nullable public final List<FormattedCharSequence> tooltip;
	public List<FormattedCharSequence> tooltipWithError;
	@Nullable public Component validationError;
	public boolean hasChanged;
	public ValueConfigEntry(ConfigCategoryTab<C, V> tab, ValueConfigNode<C, V> node) {
		this.node = node;
		this.tab = tab;
		label = this.node.getTitle().copy().withStyle(ChatFormatting.WHITE);
		var font = ClientUtil.mc.font;
		tooltip = this.node.getTooltip() == null ? null : font.split(this.node.getTooltip(), 350);
		tooltipWithError = getTooltipWithError();
		resetButton = new OreIconButton(SIZE, FlexGuiTextures.ICON_RESET, b -> resetToDefault());
		resetButton.setTooltip(Tooltip.create(Translation.RESET_TOOLTIP));
		resetButton.active = !this.node.isDefaultValue(this.tab.config);
		undoButton = new OreIconButton(SIZE, FlexGuiTextures.ICON_UNDO, b -> resetToActive());
		undoButton.setTooltip(Tooltip.create(Translation.UNDO_TOOLTIP));
		undoButton.active = !this.node.isActiveValue(this.tab.config);
		lockButton = new OreIconButton(SIZE, FlexGuiTextures.ICON_UNLOCK, b -> onLockToggle());
		lockButton.setTooltip(Tooltip.create(Translation.UNLOCK_TOOLTIP));
		lockButton.active = canLock();
		lockButton.visible = shouldShowLockButton();
		children.add(resetButton);
		children.add(undoButton);
		children.add(lockButton);
	}
	public void resetToDefault() {
		if (isReadOnly()) return;
		var path = node.getPath();
		var pending = ClientLockManager.getPendingLock(fileKey(), path);
		if (pending != Boolean.FALSE) ClientLockManager.clearPendingLock(fileKey(), path);
		node.resetToDefault();
		tab.screen.refresh();
	}
	public void resetToActive() {
		var path = node.getPath();
		var pending = ClientLockManager.getPendingLock(fileKey(), path);
		if (pending != Boolean.FALSE) ClientLockManager.clearPendingLock(fileKey(), path);
		node.resetToActive(tab.config);
		tab.screen.refresh();
	}
	/** 切换待处理的锁定状态（延迟至保存时执行）。 */
	private void onLockToggle() {
		var path = node.getPath();
		var shouldLock = !isLocked(); // effective state, considering pending
		ClientLockManager.setPendingLock(fileKey(), path, shouldLock);
		tab.screen.refresh();
	}
	public V getValue() {
		return node.getEditingValue(tab.config);
	}
	public void setValue(V value) {
		node.setEditingValue(value);
		tab.screen.refresh();
	}
	@NotNull
	@Override
	public List<? extends GuiEventListener> children() {
		return children;
	}
	@NotNull
	@Override
	public List<? extends NarratableEntry> narratables() {
		return children;
	}
	@Override
	public List<FormattedCharSequence> getTooltip() {
		return hasError() ? tooltipWithError : tooltip;
	}
	@Override
	public boolean hasError() {
		return validationError != null;
	}
	@Override
	public void refresh() {
		var locked = isLocked();
		var readOnly = isReadOnly();
		var hasPendingLock = Boolean.TRUE.equals(ClientLockManager.getPendingLock(fileKey(), node.getPath()));
		resetButton.active = !readOnly && !node.isDefaultValue(tab.config);
		undoButton.active = !readOnly && !node.isActiveValue(tab.config);
		validationError = node.validate(tab.config);
		hasChanged = hasPendingLock || !readOnly && !node.isActiveValue(tab.config);
		tooltipWithError = getTooltipWithError();
		lockButton.visible = shouldShowLockButton();
		lockButton.active = lockButton.visible && canLock();
		if (locked) {
			lockButton.setIcon(FlexGuiTextures.ICON_LOCK);
			lockButton.setTooltip(Tooltip.create(Translation.LOCKED_TOOLTIP));
		} else {
			lockButton.setIcon(FlexGuiTextures.ICON_UNLOCK);
			lockButton.setTooltip(Tooltip.create(Translation.UNLOCK_TOOLTIP));
		}
	}
	/**
	 * 检查此条目的有效锁定状态。
	 * 首先考虑待处理的锁定操作，然后回退到服务器确认的锁定。
	 */
	public boolean isLocked() {
		var path = node.getPath();
		var pending = ClientLockManager.getPendingLock(fileKey(), path);
		if (pending != null) return pending;
		return ClientLockManager.isLocked(fileKey(), path);
	}
	/** 本条目的值是否不可修改：被锁定，或该配置文件在客户端本就不可编辑（远端服务器上的 SERVER 型）。 */
	public boolean isReadOnly() {
		return isLocked() || !tab.screen.isEditable();
	}
	/** 本条目所属的配置文件。 */
	private FileKey fileKey() {
		return new FileKey(tab.screen.root.modId(), tab.screen.root.fileName());
	}
	public List<FormattedCharSequence> getTooltipWithError() {
		if (!hasError()) return tooltip;
		var errorTooltip = new ArrayList<FormattedCharSequence>();
		if (tooltip != null) errorTooltip.addAll(tooltip);
		if (validationError != null) errorTooltip.add(validationError.copy().withStyle(ChatFormatting.RED).getVisualOrderText());
		return errorTooltip;
	}
	private boolean shouldShowLockButton() {
		if (!tab.screen.isLockable()) return false;   // SERVER 型不接受锁定
		if (ClientUtil.mc.player == null) return false;
		if (ClientUtil.mc.isSingleplayer()) return false;
		return ClientLockManager.hasReceivedSync();
	}
	private boolean canLock() {
		return ClientUtil.mc.player != null && ClientUtil.mc.player.hasPermissions(2);
	}
	public void renderGui(
		@NotNull GuiGraphics gui,
		int y,
		int x,
		int width,
		int mouseX,
		int mouseY,
		float delta,
		AbstractWidget... widgets
	) {
		var indent = getIndent();
		var label = ConfigCategoryTab.styleAsState(this.label, hasError(), hasChanged);
		if (isLocked()) label = label.copy().withStyle(ChatFormatting.GRAY, ChatFormatting.STRIKETHROUGH);
		else if (!tab.screen.isEditable()) label = label.copy().withStyle(ChatFormatting.GRAY);
		gui.drawString(ClientUtil.mc.font, label.getVisualOrderText(), x + indent, y + 5, -1, true);
		var right = x + width;
		for (var widget : widgets) {
			if (!widget.visible) continue;
			right -= widget.getWidth();
			widget.setPosition(right, y);
			right -= GAP;
		}
		for (var widget : widgets) if (widget.visible) widget.render(gui, mouseX, mouseY, delta);
	}
}
