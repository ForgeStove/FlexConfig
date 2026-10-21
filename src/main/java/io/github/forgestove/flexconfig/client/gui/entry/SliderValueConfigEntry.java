package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.client.gui.components.FlexSliderButton;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.DoubleFunction;
/** 带范围的数值条目：注解里写了 {@code slider = true} 才走这里；拖动期间只写节点的编辑值，松手才 {@code refresh()}。 */
public final class SliderValueConfigEntry<C, N extends Number> extends ValueConfigEntry<C, N> {
	public final FlexSliderButton slider;
	public SliderValueConfigEntry(ConfigCategoryTab<C, N> tab, ValueConfigNode<C, N> node, DoubleFunction<N> fromDouble) {
		super(tab, node);
		var range = Objects.requireNonNull(node.getSliderRange(), "slider entry needs a slider range");
		slider = new FlexSliderButton(
			WIDTH,
			HEIGHT,
			range.min(),
			range.max(),
			() -> getValue().doubleValue(),
			value -> node.setEditingValue(fromDouble.apply(value)),
			tab.screen::refresh,
			value -> String.valueOf(fromDouble.apply(value))
		);
		children.add(slider);
	}
	@Override
	public void render(
		@NotNull GuiGraphics gui,
		int index,
		int y,
		int x,
		int width,
		int height,
		int mouseX,
		int mouseY,
		boolean hovering,
		float delta
	) {
		slider.active = !isReadOnly();
		renderGui(gui, y, x, width, mouseX, mouseY, delta, lockButton, undoButton, resetButton, slider);
	}
}
