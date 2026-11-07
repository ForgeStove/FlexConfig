package io.github.forgestove.flexconfig.foundation.ui.core;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
/** 自绘文本提示：标题白色、正文灰色，优先贴在控件下方，空间不足时翻到上方。 */
public class UiTooltipRenderer {
	private static final int MAX_WIDTH = 192, PADDING = 4, GAP = 6;
	public static void render(
		UiPainter painter,
		Component text,
		UiBounds anchor,
		int mouseX,
		int mouseY,
		int screenWidth,
		int screenHeight,
		boolean avoidPointer
	) {
		var font = ClientUtil.mc.font;
		var lines = font.split(text, MAX_WIDTH);
		if (lines.isEmpty()) return;
		var width = 0;
		for (var line : lines) width = Math.max(width, font.width(line));
		width += PADDING * 2;
		var height = lines.size() * font.lineHeight + PADDING * 2 - 1;
		var hasAnchor = anchor.width() > 0 || anchor.height() > 0;
		var anchorX = hasAnchor ? anchor.x() : mouseX;
		var anchorY = hasAnchor ? anchor.y() : mouseY;
		var anchorBottom = hasAnchor ? anchor.bottom() : mouseY;
		var x = Mth.clamp(anchorX, 2, Math.max(2, screenWidth - width - 2));
		var y = anchorBottom + GAP;
		if (y + height > screenHeight - 2) y = anchorY - GAP - height;
		y = Mth.clamp(y, 2, Math.max(2, screenHeight - height - 2));
		if (avoidPointer && mouseX >= x - 2 && mouseX <= x + width + 2 && mouseY >= y - 2 && mouseY <= y + height + 2) {
			var below = mouseY + 12;
			var above = mouseY - 8 - height;
			y = below + height <= screenHeight - 2 ? below : Math.max(2, above);
		}
		painter.flush();
		painter.rect(x, y, width, height, UiTheme.WELL);
		painter.border(x, y, width, height, UiTheme.OUTLINE);
		var lineY = y + PADDING;
		for (var i = 0; i < lines.size(); i++) {
			painter.text(lines.get(i), x + PADDING, lineY, i == 0 ? UiTheme.TEXT : UiTheme.TEXT_2);
			lineY += font.lineHeight;
		}
	}
}
