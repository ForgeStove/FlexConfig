package io.github.forgestove.flexconfig.foundation.client;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import static net.minecraft.network.chat.Component.*;
public class Translation {
	public static final Component ON_LABEL = literal("✔");
	public static final Component OFF_LABEL = literal("❌");
	public static final Component RESET_TOOLTIP = translatable("controls.reset");
	public static final Component UNDO_TOOLTIP = translatable("flexconfig.undo.tooltip");
	public static final Component KEYBINDS_LABEL = translatable("flexconfig.keybinds");
	public static final Component CANCEL_LABEL = translatable("gui.cancel");
	public static final Component BACK_LABEL = translatable("gui.back");
	public static final Component SAVE_UNAVAILABLE = translatable("flexconfig.save_unavailable");
	public static final Component SAVE_UNAVAILABLE_INVALID = translatable("flexconfig.save_unavailable.invalid");
	public static final Component SAVE_UNAVAILABLE_READ_ONLY = translatable("flexconfig.save_unavailable.read_only");
	public static final Component SERVER_CONFIG_UNAVAILABLE = translatable("flexconfig.server_config.unavailable");
	public static final Component SERVER_CONFIG_UNAVAILABLE_TOOLTIP = translatable("flexconfig.server_config.unavailable.tooltip");
	public static final Component QUIT_CONFIRM_LABEL = translatable("flexconfig.quit.confirm");
	public static final Component QUIT_CONFIRM_TITLE = translatable("flexconfig.quit.confirm.title");
	public static final Component QUIT_CONFIRM_WARNING = translatable("flexconfig.quit.confirm.warning");
	public static final Component RESTART_REQUIRED_LABEL = translatable("flexconfig.restart.game.text");
	public static final Component RESTART_REQUIRED_TITLE = translatable("flexconfig.restart.title");
	public static final Component RESTART_REQUIRED_WORLD = translatable("flexconfig.restart.server.text");
	public static final Component RETURN_TO_MENU = translatable("menu.returnToMenu");
	public static final Component QUIT_GAME = translatable("menu.quit");
	public static final Component IGNORE_RESTART_LABEL = translatable("flexconfig.restart.return");
	public static final Component COLOR_PICKER_TOOLTIP = translatable("flexconfig.color_picker.tooltip");
	public static final Component VALIDATOR_MIN = translatable("flexconfig.validator.min");
	public static final Component VALIDATOR_MAX = translatable("flexconfig.validator.max");
	public static final Component UNSUPPORTED_TYPE = translatable("flexconfig.unsupported_type");
	public static final Component MULTIPLE_ERRORS = translatable("flexconfig.validator.multiple_errors");
	public static final Component UNLOCK_TOOLTIP = translatable("flexconfig.unlock.tooltip");
	public static final Component LOCKED_TOOLTIP = translatable("flexconfig.locked.tooltip");
	public static final Component CLOSE_TOOLTIP = translatable("flexconfig.close");
	public static final Component DONE_LABEL = translatable("flexconfig.done");
	public static final Component PAGE_RESET = translatable("flexconfig.page.reset");
	public static final Component ROW_COPY = translatable("flexconfig.row.copy");
	public static final Component SEARCH_CLEAR = translatable("flexconfig.search.clear");
	public static final Component SEARCH_MENU = translatable("flexconfig.search.menu");
	public static final Component SEARCH_NO_CHANGES = translatable("flexconfig.search.no_changes");
	public static final Component SEARCH_PLACEHOLDER = translatable("flexconfig.search.placeholder");
	public static final Component TOAST_UNDONE = translatable("flexconfig.toast.undone");
	public static final Component TOOLTIP_CHANGED = translatable("flexconfig.tooltip.changed");
	public static final Component WARN_CHEAT_TOOLTIP = translatable("flexconfig.warn.cheat.tooltip");
	public static Component pageResetToast(int count) {
		return translatable("flexconfig.toast.page_reset", count);
	}
	public static Component searchResults(int count) {
		return translatable("flexconfig.search.results", count);
	}
	public static Component searchNoMatch(String query) {
		return translatable("flexconfig.search.no_match", query);
	}
	public static Component undoChanges(int count) {
		return translatable("flexconfig.undo_changes", count);
	}
	public static Component searchChanged(int count) {
		return translatable("flexconfig.search.changed", count);
	}
	public static Component resetTo(String defaultText) {
		return translatable("flexconfig.row.reset_to", defaultText);
	}
	public static Component tooltipDefault(String value) {
		return translatable("flexconfig.tooltip.default", value);
	}
	public static @Nullable String getString(String key) {
		return I18n.exists(key) ? I18n.get(key) : null;
	}
	/** 多行 tooltip：加粗标题 + 灰色说明；{@code \n} 由 {@code Tooltip.splitTooltip} → font.split 断行。 */
	public static @NotNull Component tooltip(@NotNull Component title, @NotNull Component detail) {
		return title.copy().withStyle(ChatFormatting.BOLD).append("\n").append(detail.copy().withStyle(ChatFormatting.GRAY));
	}
}
