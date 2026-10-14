package io.github.forgestove.flexconfig.client;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;
public final class Translation {
	public static final Component ON_LABEL = Component.literal("✔");
	public static final Component OFF_LABEL = Component.literal("❌");
	public static final Component RESET_TOOLTIP = Component.translatable("controls.reset");
	public static final Component UNDO_TOOLTIP = Component.translatable("flexconfig.undo.tooltip");
	public static final Component KEYBINDS_LABEL = Component.translatable("flexconfig.keybinds");
	public static final Component CANCEL_LABEL = Component.translatable("gui.cancel");
	public static final Component BACK_LABEL = Component.translatable("gui.back");
	public static final Component QUIT_UNSAVED_LABEL = Component.translatable("flexconfig.quit.unsaved");
	/** 保存键文案恒定；能不能保存由按钮可用性与 tooltip 表达。 */
	public static final Component SAVE_CHANGES_LABEL = Component.translatable("flexconfig.save_changes");
	public static final Component SAVE_CHANGES_TOOLTIP = Component.translatable("flexconfig.save_changes.tooltip");
	public static final Component SAVE_UNAVAILABLE = Component.translatable("flexconfig.save_unavailable");
	public static final Component SAVE_UNAVAILABLE_NO_CHANGES = Component.translatable("flexconfig.save_unavailable.no_changes");
	public static final Component SAVE_UNAVAILABLE_INVALID = Component.translatable("flexconfig.save_unavailable.invalid");
	public static final Component SAVE_UNAVAILABLE_READ_ONLY = Component.translatable("flexconfig.save_unavailable.read_only");
	public static final Component SERVER_CONFIG_UNAVAILABLE = Component.translatable("flexconfig.server_config.unavailable");
	public static final Component SERVER_CONFIG_UNAVAILABLE_TOOLTIP
		= Component.translatable("flexconfig.server_config.unavailable.tooltip");
	public static final Component QUIT_CONFIRM_LABEL = Component.translatable("flexconfig.quit.confirm");
	public static final Component QUIT_CONFIRM_TITLE = Component.translatable("flexconfig.quit.confirm.title");
	public static final Component QUIT_CONFIRM_WARNING = Component.translatable("flexconfig.quit.confirm.warning");
	public static final Component RESTART_REQUIRED_LABEL = Component.translatable("flexconfig.restart.game.text");
	public static final Component RESTART_REQUIRED_TITLE = Component.translatable("flexconfig.restart.title");
	public static final Component RESTART_REQUIRED_WORLD = Component.translatable("flexconfig.restart.server.text");
	public static final Component RETURN_TO_MENU = Component.translatable("menu.returnToMenu");
	public static final Component QUIT_GAME = Component.translatable("menu.quit");
	public static final Component IGNORE_RESTART_LABEL = Component.translatable("flexconfig.restart.return");
	public static final Component COLOR_PICKER_TOOLTIP = Component.translatable("flexconfig.color_picker.tooltip");
	public static final Component VALIDATOR_MIN = Component.translatable("flexconfig.validator.min");
	public static final Component VALIDATOR_MAX = Component.translatable("flexconfig.validator.max");
	public static final Component UNSUPPORTED_TYPE = Component.translatable("flexconfig.unsupported_type");
	public static final Component MULTIPLE_ERRORS = Component.translatable("flexconfig.validator.multiple_errors");
	public static final Component UNLOCK_TOOLTIP = Component.translatable("flexconfig.unlock.tooltip");
	public static final Component LOCKED_TOOLTIP = Component.translatable("flexconfig.locked.tooltip");
	public static @Nullable String getString(String key) {
		return I18n.exists(key) ? I18n.get(key) : null;
	}
	/** 多行 tooltip：加粗标题 + 灰色说明；{@code \n} 由 {@code Tooltip.splitTooltip} → font.split 断行。 */
	public static @NotNull Component tooltip(@NotNull Component title, @NotNull Component detail) {
		return title.copy().withStyle(ChatFormatting.BOLD).append("\n").append(detail.copy().withStyle(ChatFormatting.GRAY));
	}
}
