package io.github.forgestove.flexconfig.client.ui.model;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
/** 值行的校验、变更和锁定状态。 */
public class ValueRowState {
	public @Nullable Component validationError;
	public boolean changed, locked, readOnly, hasPendingLock, canLock, showLock;
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void refresh(ValueConfigNode<?, ?> node, RowContext ctx) {
		var file = ctx.fileKey();
		var path = node.getLockPath();
		var pending = ClientLockManager.getPendingLock(file, path);
		locked = pending != null ? pending : ClientLockManager.isLocked(file, path);
		readOnly = locked || !ctx.editable();
		hasPendingLock = Boolean.TRUE.equals(pending);
		validationError = ((ValueConfigNode) node).validate(ctx.config());
		changed = hasPendingLock || !readOnly && !((ValueConfigNode) node).isActiveValue(ctx.config());
		canLock = ClientUtil.mc.player != null && ClientUtil.mc.player.hasPermissions(2);
		showLock = shouldShowLockButton(node, ctx);
	}
	private boolean shouldShowLockButton(ValueConfigNode<?, ?> node, RowContext ctx) {
		if (!ctx.lockable() || node.isElement()) return false;
		if (node.getValueType() == List.class && node.getElementType() != null && node.getElementType().isCollection()) return false;
		if (ClientUtil.mc.player == null) return false;
		if (ClientUtil.mc.isSingleplayer()) return false;
		return ClientLockManager.hasReceivedSync();
	}
}
