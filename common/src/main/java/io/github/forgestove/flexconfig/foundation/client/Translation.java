package io.github.forgestove.flexconfig.foundation.client;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.*;
/** 界面文本。对界面返回 UiText；树和 API 仍然使用 Component，二者的转换集中在 from 和 toComponent。 */
public class Translation {
	static {
		UiTexts.install(MinecraftTextEngine.INSTANCE);
	}
	public static final UiText ON_LABEL = UiText.literal("✔");
	public static final UiText OFF_LABEL = UiText.literal("❌");
	public static final UiText RESET_TOOLTIP = UiText.translatable("controls.reset");
	public static final UiText UNDO_TOOLTIP = UiText.translatable("flexconfig.undo.tooltip");
	public static final UiText KEYBINDS_LABEL = UiText.translatable("flexconfig.keybinds");
	public static final UiText CANCEL_LABEL = UiText.translatable("gui.cancel");
	public static final UiText BACK_LABEL = UiText.translatable("gui.back");
	public static final UiText SAVE_UNAVAILABLE = UiText.translatable("flexconfig.save_unavailable");
	public static final UiText SAVE_UNAVAILABLE_INVALID = UiText.translatable("flexconfig.save_unavailable.invalid");
	public static final UiText SAVE_UNAVAILABLE_READ_ONLY = UiText.translatable("flexconfig.save_unavailable.read_only");
	public static final UiText SERVER_CONFIG_UNAVAILABLE = UiText.translatable("flexconfig.server_config.unavailable");
	public static final UiText SERVER_CONFIG_UNAVAILABLE_TOOLTIP = UiText.translatable("flexconfig.server_config.unavailable.tooltip");
	public static final UiText QUIT_CONFIRM_LABEL = UiText.translatable("flexconfig.quit.confirm");
	public static final UiText QUIT_CONFIRM_TITLE = UiText.translatable("flexconfig.quit.confirm.title");
	public static final UiText QUIT_CONFIRM_WARNING = UiText.translatable("flexconfig.quit.confirm.warning");
	public static final UiText RESTART_REQUIRED_LABEL = UiText.translatable("flexconfig.restart.game.text");
	public static final UiText RESTART_REQUIRED_TITLE = UiText.translatable("flexconfig.restart.title");
	public static final UiText RESTART_REQUIRED_WORLD = UiText.translatable("flexconfig.restart.server.text");
	public static final UiText RETURN_TO_MENU = UiText.translatable("menu.returnToMenu");
	public static final UiText QUIT_GAME = UiText.translatable("menu.quit");
	public static final UiText IGNORE_RESTART_LABEL = UiText.translatable("flexconfig.restart.return");
	public static final UiText COLOR_PICKER_TOOLTIP = UiText.translatable("flexconfig.color_picker.tooltip");
	public static final UiText VALIDATOR_MIN = UiText.translatable("flexconfig.validator.min");
	public static final UiText VALIDATOR_MAX = UiText.translatable("flexconfig.validator.max");
	public static final UiText UNSUPPORTED_TYPE = UiText.translatable("flexconfig.unsupported_type");
	public static final UiText MULTIPLE_ERRORS = UiText.translatable("flexconfig.validator.multiple_errors");
	public static final UiText UNLOCK_TOOLTIP = UiText.translatable("flexconfig.unlock.tooltip");
	public static final UiText LOCKED_TOOLTIP = UiText.translatable("flexconfig.locked.tooltip");
	public static final UiText CLOSE_TOOLTIP = UiText.translatable("flexconfig.close");
	public static final UiText DONE_LABEL = UiText.translatable("flexconfig.done");
	public static final UiText PAGE_RESET = UiText.translatable("flexconfig.page.reset");
	public static final UiText ROW_COPY = UiText.translatable("flexconfig.row.copy");
	public static final UiText SEARCH_CLEAR = UiText.translatable("flexconfig.search.clear");
	public static final UiText SEARCH_MENU = UiText.translatable("flexconfig.search.menu");
	public static final UiText SEARCH_NO_CHANGES = UiText.translatable("flexconfig.search.no_changes");
	public static final UiText SEARCH_PLACEHOLDER = UiText.translatable("flexconfig.search.placeholder");
	public static final UiText TOAST_UNDONE = UiText.translatable("flexconfig.toast.undone");
	public static final UiText TOOLTIP_CHANGED = UiText.translatable("flexconfig.tooltip.changed");
	public static final UiText WARN_CHEAT_TOOLTIP = UiText.translatable("flexconfig.warn.cheat.tooltip");
	public static UiText pageResetToast(int count) {
		return UiText.translatable("flexconfig.toast.page_reset", count);
	}
	public static UiText searchResults(int count) {
		return UiText.translatable("flexconfig.search.results", count);
	}
	public static UiText searchNoMatch(String query) {
		return UiText.translatable("flexconfig.search.no_match", query);
	}
	public static UiText undoChanges(int count) {
		return UiText.translatable("flexconfig.undo_changes", count);
	}
	public static UiText searchChanged(int count) {
		return UiText.translatable("flexconfig.search.changed", count);
	}
	public static UiText resetTo(String defaultText) {
		return UiText.translatable("flexconfig.row.reset_to", defaultText);
	}
	public static UiText tooltipDefault(String value) {
		return UiText.translatable("flexconfig.tooltip.default", value);
	}
	public static @Nullable String getString(String key) {
		return I18n.exists(key) ? I18n.get(key) : null;
	}
	/** 树和 API 返回 Component，进入界面前转成 UiText。 */
	public static @Nullable UiText from(@Nullable Component component) {
		return UiText.from(component);
	}
	/** 交给原版界面前转回 Component。 */
	public static Component toComponent(UiText text) {
		return MinecraftTextEngine.toComponent(text);
	}
	public static @Nullable UiTexture texture(@Nullable ResourceLocation id) {
		return id == null ? null : new MinecraftTexture(id);
	}
	/** 多行 tooltip：加粗标题 + 灰色说明；{@code \n} 由字体换行处理。 */
	public static @NotNull UiText tooltip(@NotNull UiText title, @NotNull UiText detail) {
		return title.copy().withStyle(UiStyle.BOLD).append("\n").append(detail.copy().withStyle(UiStyle.GRAY));
	}
}
