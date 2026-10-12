package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.client.gui.components.*;
import io.github.forgestove.flexconfig.client.gui.entry.ConfigEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.*;
public final class EnumDropdownScreen extends Screen {
	private static final int GAP = ConfigEntry.GAP, HEIGHT = ConfigEntry.HEIGHT, ITEM_H = HEIGHT + GAP;
	private final Enum<?>[] values;
	private final Supplier<Enum<?>> selectedSupplier;
	private final Consumer<Enum<?>> onSelect;
	private final Function<Enum<?>, Component> displayMapper;
	private final Screen parent;
	private final OreButton dropdownButton;
	/** 滚动条组件：滚动量、平滑、命中、拖动全在它里面，这里只喂几何与输入。 */
	private final OreScrollbar scrollbar = new OreScrollbar();
	private int maxVisibleOptions;
	public EnumDropdownScreen(
		Enum<?>[] values,
		Supplier<Enum<?>> selectedSupplier,
		Consumer<Enum<?>> onSelect,
		Function<Enum<?>, Component> displayMapper,
		Screen parent,
		OreButton dropdownButton
	) {
		super(Component.empty());
		this.values = values;
		this.selectedSupplier = selectedSupplier;
		this.onSelect = onSelect;
		this.displayMapper = displayMapper;
		this.parent = parent;
		this.dropdownButton = dropdownButton;
		scrollbar.step(ITEM_H);
		update();
	}
	private void update() {
		maxVisibleOptions = Math.min(values.length,
			(parent.height - dropdownButton.getY() - dropdownButton.getHeight() - HEIGHT) / ITEM_H);
		scrollbar.area(scrollbarX(), dropdownY() + GAP, viewportHeight(), values.length * ITEM_H);
		// 打开时把选中项摆到中间
		scrollbar.seek((Arrays.asList(values).indexOf(selectedSupplier.get()) - maxVisibleOptions / 2.0) * ITEM_H);
	}
	/** 可视区高，与 content 同口径（都是「项数 × 行高」），装得下时两者相等、滚动条不显示。 */
	private int viewportHeight() {
		return maxVisibleOptions * ITEM_H;
	}
	private int dropdownY() {
		return dropdownButton.getY() + dropdownButton.getHeight();
	}
	private int scrollbarX() {
		return dropdownButton.getX() + dropdownButton.getWidth() - scrollbarWidth() - GAP;
	}
	/** 滚动条宽度 = 滑块精灵宽（随资源包变），面板右侧要让出这么多。 */
	private int scrollbarWidth() {
		return FlexGuiTextures.SCROLLBAR_TOP.width();
	}
	@Override
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		scrollbar.dragTo(mouseY);   // 拖动落位：按每帧拿到的真实鼠标坐标推进，不用 mouseDragged
		scrollbar.update();
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, -100);
		// 将父屏幕绘制为背景
		parent.render(gui, -1, -1, delta);
		pose.popPose();
		var dropdownX = dropdownButton.getX();
		var dropdownY = dropdownY();
		var dropdownWidth = dropdownButton.getWidth();
		var dropdownHeight = maxVisibleOptions * ITEM_H;
		// 绘制下拉面板（外圈黑底=边框 + 内部灰色填充）
		gui.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + dropdownHeight + GAP, 0xFF000000);
		gui.fill(dropdownX + 1, dropdownY + 1, dropdownX + dropdownWidth - 1, dropdownY + dropdownHeight + GAP - 1, 0xFF2D2D2D);
		// 绘制可见选项（裁剪到面板内部区域，防止滚动时外溢）
		gui.enableScissor(dropdownX + 1, dropdownY + 1, dropdownX + dropdownWidth - 1, dropdownY + dropdownHeight + GAP - 1);
		var scroll = scrollbar.scroll();
		var firstIndex = Math.max(0, (int) Math.floor(scroll / ITEM_H));
		var lastIndex = Math.min(values.length - 1, (int) Math.ceil((scroll + viewportHeight()) / ITEM_H));
		for (var optionIndex = firstIndex; optionIndex <= lastIndex; optionIndex++) {
			var y = (int) Math.round(dropdownY + optionIndex * ITEM_H - scroll);
			if (y + HEIGHT < dropdownY || y > dropdownY + dropdownHeight + GAP) continue;
			var isSelected = values[optionIndex] == selectedSupplier.get();
			var isHovered = mouseX >= dropdownX && mouseX < dropdownX + dropdownWidth && mouseY >= y + GAP && mouseY < y + HEIGHT + GAP;
			if (isSelected) gui.fill(dropdownX + GAP, y + GAP, dropdownX + dropdownWidth - GAP, y + HEIGHT + GAP, 0xFF3366BB);
			else if (isHovered) gui.fill(dropdownX + GAP, y + GAP, dropdownX + dropdownWidth - GAP, y + HEIGHT + GAP, 0xFF404040);
			var color = isSelected ? 0xFFFFFFFF : isHovered ? 0xFFFFFF00 : 0xFFE0E0E0;
			gui.drawString(font, displayMapper.apply(values[optionIndex]), dropdownX + HEIGHT / 4, y + HEIGHT / 4 + GAP, color, false);
		}
		gui.disableScissor();
		scrollbar.render(gui, mouseX, mouseY);
	}
	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		parent.resize(minecraft, width, height);
		update();
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		assert minecraft != null;
		if (isOutsidePanel(mouseX, mouseY)) {
			minecraft.setScreen(parent);
			playClickSound();
			return true;
		}
		if (scrollbar.mousePressed(mouseX, mouseY)) return true;
		var dropdownY = dropdownY();
		var scroll = scrollbar.scroll();
		var firstIndex = Math.max(0, (int) Math.floor(scroll / ITEM_H));
		var lastIndex = Math.min(values.length - 1, (int) Math.ceil((scroll + viewportHeight()) / ITEM_H));
		var contentWidth = getContentWidth();
		for (var optionIndex = firstIndex; optionIndex <= lastIndex; optionIndex++) {
			var y = (int) Math.round(dropdownY + optionIndex * ITEM_H - scroll);
			if (mouseX < dropdownButton.getX() || mouseX >= dropdownButton.getX() + contentWidth) continue;
			if (!(mouseY >= y + GAP) || !(mouseY < y + HEIGHT + GAP)) continue;
			onSelect.accept(values[optionIndex]);
			minecraft.setScreen(parent);
			playClickSound();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	private boolean isOutsidePanel(double mouseX, double mouseY) {
		var panelX = dropdownButton.getX() - GAP;
		var panelY = dropdownY() - GAP;
		var panelW = dropdownButton.getWidth() + GAP;
		var panelH = maxVisibleOptions * ITEM_H + GAP * 2;
		return !(mouseX >= panelX) || !(mouseX <= panelX + panelW) || !(mouseY >= panelY) || !(mouseY <= panelY + panelH);
	}
	private void playClickSound() {
		assert minecraft != null;
		minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
	private int getContentWidth() {
		return scrollbar.shown() ? dropdownButton.getWidth() - scrollbarWidth() - GAP : dropdownButton.getWidth();
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		scrollbar.release();
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (isOutsidePanel(mouseX, mouseY)) return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
		scrollbar.wheel(vertical);
		return true;
	}
}
