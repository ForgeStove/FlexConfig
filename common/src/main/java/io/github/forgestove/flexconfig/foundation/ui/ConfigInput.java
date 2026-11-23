package io.github.forgestove.flexconfig.foundation.ui;
public sealed interface ConfigInput {
	record MouseDown(double x, double y, int button) implements ConfigInput {}
	record MouseUp(double x, double y, int button) implements ConfigInput {}
	record MouseDrag(double x, double y, int button, double dragX, double dragY) implements ConfigInput {}
	record Scroll(double x, double y, double scrollX, double scrollY) implements ConfigInput {}
	record Key(int keyCode, int scanCode, int modifiers) implements ConfigInput {}
	record Char(char codePoint, int modifiers) implements ConfigInput {}
}
