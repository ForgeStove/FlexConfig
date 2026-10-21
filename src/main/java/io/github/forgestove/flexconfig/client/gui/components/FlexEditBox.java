package io.github.forgestove.flexconfig.client.gui.components;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
/** 记录当前聚焦实例，供界面点击空白处时统一失焦。 */
public class FlexEditBox extends EditBox {
	@Nullable private static FlexEditBox focusedInstance;
	public FlexEditBox(Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
	}
	public static void onScreenMouseClicked(double mouseX, double mouseY) {
		if (focusedInstance == null) return;
		if (!focusedInstance.isMouseOver(mouseX, mouseY)) focusedInstance.setFocused(false);
	}
	@Override
	public void setFocused(boolean focused) {
		if (focused && focusedInstance != this) {
			if (focusedInstance != null) focusedInstance.setFocused(false);
			focusedInstance = this;
		} else if (!focused && focusedInstance == this) focusedInstance = null;
		super.setFocused(focused);
	}
}
