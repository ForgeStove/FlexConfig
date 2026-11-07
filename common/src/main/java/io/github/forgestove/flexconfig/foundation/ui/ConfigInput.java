package io.github.forgestove.flexconfig.foundation.ui;
/** 适配层到界面编排层的输入事件，坐标保持 Screen 原始值。 */
public sealed interface ConfigInput {
	record MouseDown(double x, double y, int button) implements ConfigInput {}
	record MouseUp(double x, double y, int button) implements ConfigInput {}
	record MouseDrag(double x, double y, int button, double dragX, double dragY) implements ConfigInput {}
	record Scroll(double x, double y, double scrollX, double scrollY) implements ConfigInput {}
	record Key(int keyCode, int scanCode, int modifiers) implements ConfigInput {}
	record Char(char codePoint, int modifiers) implements ConfigInput {}
}
