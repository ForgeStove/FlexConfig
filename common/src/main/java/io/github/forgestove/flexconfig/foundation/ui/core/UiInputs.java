package io.github.forgestove.flexconfig.foundation.ui.core;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import org.lwjgl.glfw.GLFW;
public class UiInputs {
	public static boolean shiftDown() {
		var window = ClientUtil.mc.getWindow().getWindow();
		return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
	}
	public static boolean controlDown() {
		var window = ClientUtil.mc.getWindow().getWindow();
		return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(
			window,
			GLFW.GLFW_KEY_RIGHT_CONTROL
		);
	}
}
