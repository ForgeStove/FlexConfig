package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.ClientUtil;
import org.lwjgl.glfw.GLFW;

import java.util.*;
/**
 * 鼠标光标：可交互组件在渲染时记下想要的光标，由各加载器在一帧绘制完之后统一下发。渲染过程中即时下发的话，
 * 一帧内会先设成箭头再设成手型，鼠标静止时表现为反复闪烁。GLFW 光标句柄按需创建并缓存，仅在状态变化时
 * 调用 {@code glfwSetCursor}。
 */
public class OreCursor {
	private static final Map<Integer, Long> CURSORS = new HashMap<>();
	/** 已下发的光标；0 为默认箭头，-1 表示尚未下发。 */
	private static int current = -1, requested;
	/** 一帧开始时清空请求；该帧无组件请求即回落成默认箭头。 */
	public static void reset() {
		requested = 0;
	}
	public static void hand() {
		requested = GLFW.GLFW_HAND_CURSOR;
	}
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
