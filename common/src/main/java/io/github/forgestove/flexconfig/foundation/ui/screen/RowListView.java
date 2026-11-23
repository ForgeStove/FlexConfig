package io.github.forgestove.flexconfig.foundation.ui.screen;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.row.ConfigRow;
import io.github.forgestove.flexconfig.foundation.ui.widget.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;
public class RowListView extends UiScrollPane {
	private final Supplier<List<ConfigRow>> rows;
	private UiBounds content;
	public RowListView(UiBounds content, Supplier<List<ConfigRow>> rows) {
		this.content = content;
		this.rows = rows;
	}
	public void setContent(UiBounds content) {
		this.content = content;
	}
	public void updateContentHeight() {
		var height = 0;
		for (var row : rows.get()) height += row.displayHeight(content.width());
		setContentHeight(height);
	}
	@Override
	protected void renderContent(UiContext ui) {
		var y = content.y() - (int) Math.round(scroll());
		for (var row : rows.get()) {
			var height = row.heightFor(content.width());
			var shown = row.displayHeight(content.width());
			var bounds = new UiBounds(content.x(), y, content.width(), height);
			row.setBounds(bounds);
			row.updateReveal(ui.delta);
			if (bounds.intersects(content)) {
				row.prepare(ui);
				row.layout();
			}
			y += shown;
		}
		for (var row : rows.get()) {
			if (!row.bounds().intersects(content)) continue;
			var reveal = row.revealValue();
			var scale = row.heightScale();
			var shrink = scale < 0.999F;
			if (reveal < 1) ui.painter.pushAlpha(reveal);
			if (shrink) {
				ui.painter.pushAlpha(Math.max(0.01F, scale));
				ui.painter.scissorOn(new UiBounds(content.x(), row.bounds().y(), content.width(), row.displayHeight(content.width())));
			}
			row.render(ui);
			if (shrink) {
				ui.painter.scissorOff();
				ui.painter.popAlpha();
			}
			if (reveal < 1) ui.painter.popAlpha();
		}
	}
	/** 收集可见行的可聚焦控件，顺序与绘制一致。 */
	public void addFocusables(List<UiFocus.Target> out) {
		for (var row : rows.get()) {
			if (!row.bounds().intersects(content)) continue;
			for (var control : row.controls()) if (control.focusable()) out.add(control);
		}
	}
	public @Nullable ConfigRow rowAt(UiContext ui, double mouseY) {
		var y = content.y() - (int) Math.round(scroll());
		for (var row : rows.get()) {
			var height = row.heightFor(content.width());
			var shown = row.displayHeight(content.width());
			var bounds = new UiBounds(content.x(), y, content.width(), height);
			if (mouseY >= y && mouseY < y + shown) {
				row.setBounds(bounds);
				row.prepare(ui);
				row.layout();
				return row;
			}
			y += shown;
		}
		return null;
	}
	/** 键盘切换焦点时把目标控件滚进可视区。 */
	public void ensureVisible(UiControl target) {
		var y = content.y() - (int) Math.round(scroll());
		for (var row : rows.get()) {
			var height = row.displayHeight(content.width());
			for (var control : row.controls())
				if (control == target) {
					var bottom = y + height;
					if (y < content.y()) scrollBy(y - content.y());
					else if (bottom > content.bottom()) scrollBy(bottom - content.bottom());
					return;
				}
			y += height;
		}
	}
}
