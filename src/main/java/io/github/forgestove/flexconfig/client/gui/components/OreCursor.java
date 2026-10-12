package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.ClientUtil;
import org.lwjgl.glfw.GLFW;

import java.util.*;
/**
 * 鼠标光标：可交互的组件在渲染时把想要的光标记下来，由各加载器在**一帧绘制完之后**统一下发。
 * <p>
 * 组件在渲染过程中请求，故此处只记录；若随请求即时下发，一帧内会先设成箭头再设成手型，
 * 鼠标静止时表现为反复闪烁。GLFW 光标句柄按需创建并缓存，且只在状态变化时调 {@code glfwSetCursor}。
 */
public final class OreCursor {
	/** 已创建的 GLFW 标准光标，键是 {@code GLFW_*_CURSOR}。 */
	private static final Map<Integer, Long> CURSORS = new HashMap<>();
	/** 已下发的光标；0 为默认箭头，-1 表示尚未下发。 */
	private static int current = -1, requested;
	/** 开始渲染一帧；该帧无组件请求时会回收成默认箭头。 */
	public static void reset() {
		requested = 0;
	}
	/** 可点击 / 可拖动的组件。 */
	public static void hand() {
		requested = GLFW.GLFW_HAND_CURSOR;
	}
	/** 输入框。 */
	public static void iBeam() {
		requested = GLFW.GLFW_IBEAM_CURSOR;
	}
	/** 下发本帧的光标，须在绘制完本帧控件之后调用。 */
	public static void apply() {
		if (requested == current) return;
		current = requested;
		var handle = requested == 0 ? 0L : CURSORS.computeIfAbsent(requested, GLFW::glfwCreateStandardCursor);
		GLFW.glfwSetCursor(ClientUtil.mc.getWindow().getWindow(), handle);
	}
}
