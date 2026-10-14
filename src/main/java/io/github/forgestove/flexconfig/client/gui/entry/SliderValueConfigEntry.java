package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.client.gui.components.OreSlider;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.DoubleFunction;
/**
 * 带范围的数值条目：值区就是一个滑块（注解里写了 {@code slider = true} 才走这里）。
 * <p>
 * 拖动期间只写节点的编辑值，松手才 {@code refresh()}。拖一趟刷新几十次没必要。
 */
public final class SliderValueConfigEntry<C, N extends Number> extends ValueConfigEntry<C, N> {
	public final OreSlider slider;
	public SliderValueConfigEntry(ConfigCategoryTab<C, N> tab, ValueConfigNode<C, N> node, double step, DoubleFunction<N> fromDouble) {
		super(tab, node);
		var range = Objects.requireNonNull(node.getSliderRange(), "slider entry needs a slider range");
		slider = new OreSlider(
			WIDTH,
			HEIGHT,
			range.min(),
			range.max(),
			step,
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
		slider.dragTo(mouseX);   // 拖动按每帧真实鼠标坐标推进，不走 mouseDragged
		renderGui(gui, y, x, width, mouseX, mouseY, delta, lockButton, undoButton, resetButton, slider);
	}
}
