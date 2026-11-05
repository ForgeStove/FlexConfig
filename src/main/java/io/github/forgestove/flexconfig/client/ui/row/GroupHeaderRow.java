package io.github.forgestove.flexconfig.client.ui.row;
import io.github.forgestove.flexconfig.client.ui.core.*;
import io.github.forgestove.flexconfig.client.ui.model.RowContext;
import io.github.forgestove.flexconfig.client.ui.widget.*;
import io.github.forgestove.flexconfig.client.ui.widget.UiButton.Style;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;
/** 对象元素的可展开标题行；展开状态按目标对象实例记忆。 */
public class GroupHeaderRow extends ConfigRow {
	private static final Set<Object> expandedTargets = Collections.newSetFromMap(new WeakHashMap<>());
	private final Component label;
	private final boolean expanded;
	private final Runnable toggle;
	private final @Nullable UiButton delete;
	private boolean pressed;
	public GroupHeaderRow(RowContext ctx, Object target, Component label, int depth, boolean canDelete, @Nullable Runnable onDelete, Runnable toggle) {
		super(ctx);
		this.label = label;
		this.depth = depth;
		this.toggle = toggle;
		expanded = expandedTargets.contains(target);
		delete = onDelete == null ? null : new UiButton(Component.literal("x"), Style.DANGER, onDelete);
		if (delete != null) {
			delete.setEnabled(canDelete);
			delete.setVisible(true);
		}
	}
	public boolean isExpanded() {
		return expanded;
	}
	public static void toggleExpanded(Object target) {
		if (!expandedTargets.remove(target)) expandedTargets.add(target);
	}
	@Override
	public void layout() {
		if (delete != null) {
			var height = Math.min(bounds().height(), UiTheme.CONTROL_HEIGHT);
			delete.setBounds(new UiBounds(bounds().right() - UiTheme.PADDING - UiTheme.ACTION_SIZE, bounds().y() + (bounds().height() - height) / 2, UiTheme.ACTION_SIZE, height));
		}
	}
	@Override
	public int height() {
		return UiTheme.GROUP_HEIGHT;
	}
	@Override
	public void render(UiContext ui) {
		var painter = ui.painter;
		if (pressed) painter.rect(bounds(), UiTheme.ROW_PRESS);
		var prefix = expanded ? "- " : "+ ";
		var text = painter.trim(prefix + label.getString().toUpperCase(), bounds().width() - UiTheme.PADDING * 2 - 18);
		var ty = bounds().bottom() - 13;
		painter.text(text, bounds().x() + UiTheme.INDENT + depth * UiTheme.INDENT, ty, UiTheme.TEXT_2);
		var ruleX = bounds().x() + UiTheme.INDENT + depth * UiTheme.INDENT + painter.textWidth(text) + 4;
		if (ruleX < bounds().right() - UiTheme.PADDING - 18) painter.groove(ruleX, bounds().right() - UiTheme.PADDING - 18, ty + 4);
		if (delete != null) delete.render(ui);
	}
	@Override
	public List<UiControl> controls() {
		return delete == null ? List.of() : List.of(delete);
	}
	@Override
	public boolean mouseClicked(UiContext ui, double mouseX, double mouseY, int button) {
		if (delete != null && delete.mouseClicked(ui, mouseX, mouseY, button)) return true;
		if (button != 0 || !hovered(ui)) return false;
		pressed = true;
		return true;
	}
	@Override
	public boolean mouseReleased(UiContext ui, double mouseX, double mouseY, int button) {
		if (!pressed) return false;
		pressed = false;
		if (button != 0 || !hovered(ui)) return true;
		UiSound.click();
		toggle.run();
		return true;
	}
	@Override
	public boolean keyPressed(UiContext ui, int keyCode, int scanCode, int modifiers) {
		return delete != null && delete.keyPressed(ui, keyCode, scanCode, modifiers);
	}
}
