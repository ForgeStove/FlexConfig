package io.github.forgestove.flexconfig.client.gui.im.screen;
import io.github.forgestove.flexconfig.client.gui.im.core.*;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
/** 底部居中的提示条，可带一个动作。 */
public class ToastLayer {
	private static final long STAY_MS = 3000;
	private final UiTween fade = new UiTween(0);
	private @Nullable Component text;
	private long shownAt;
	public void info(Component text) {
		this.text = text;
		shownAt = Util.getMillis();
		fade.start(fade.value(), 1, UiMotion.POPUP_IN);
	}
	public void update(float delta) {
		if (text == null) return;
		if (Util.getMillis() - shownAt > STAY_MS) fade.start(fade.value(), 0, UiMotion.TOOLTIP_FADE);
		fade.update(delta);
		if (fade.value() <= 0.01F && fade.target() == 0) text = null;
	}
	public void render(UiContext ui, int screenWidth, int screenHeight) {
		if (text == null) return;
		var painter = ui.painter;
		var boxWidth = painter.textWidth(text) + 16;
		var boxHeight = 22;
		var box = new UiBounds((screenWidth - boxWidth) / 2, screenHeight - 44, boxWidth, boxHeight);
		painter.pushAlpha(fade.value());
		painter.rect(box.x() + 2, box.y() + 2, box.width(), box.height(), UiTheme.SHADOW);
		painter.border(box, UiTheme.WINDOW_EDGE);
		painter.rect(box.x() + 1, box.y() + 1, box.width() - 2, box.height() - 2, UiTheme.SIDEBAR);
		painter.text(text, box.x() + 8, box.y() + (box.height() - painter.lineHeight()) / 2, UiTheme.TEXT);
		painter.popAlpha();
	}
}
