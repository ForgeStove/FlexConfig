package io.github.forgestove.flexconfig.client;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.api.ConfigType;
import io.github.forgestove.flexconfig.client.gui.ConfigScreen;
import io.github.forgestove.flexconfig.client.gui.components.FlexEditBox;
import io.github.forgestove.flexconfig.platform.Platform;
import net.minecraft.client.resources.language.I18n;

import java.util.*;
/** 各入口由加载器侧的事件订阅转发至此。 */
public final class ClientLifecycleHandler {
	private static final Map<String, Boolean> PREVIOUS_STATES = new HashMap<>();
	private static int watchTicks;
	private static String scannedLanguage;
	public static void tick() {
		checkTranslations();
		watchConfigFiles();
		if (ClientUtil.mc.screen != null) return;
		var window = ClientUtil.mc.getWindow().getWindow();
		// 快捷键可能挂在模组的任意一份配置上，按文件遍历
		ConfigRegistry.getRegisteredFileKeys().forEach(key -> ConfigRegistry.getHandler(key).tickTriggerKeybinds(window, PREVIOUS_STATES));
	}
	/** 每种语言扫描一次（切换语言时重扫）。就绪探针取原版键：资源就绪前 {@code I18n.exists} 恒为 false，直接判断会误报。 */
	private static void checkTranslations() {
		if (!Platform.get().isDevelopmentEnvironment()) return;
		if (!I18n.exists("menu.singleplayer")) return;
		var language = ClientUtil.mc.getLanguageManager().getSelected();
		if (language.equals(scannedLanguage)) return;
		scannedLanguage = language;
		ConfigRegistry.getRegisteredFileKeys().forEach(key -> TranslationChecker.check(ConfigRegistry.getRootConfigNode(key)));
	}
	private static void watchConfigFiles() {
		if (++watchTicks % ConfigWatchService.INTERVAL_TICKS != 0) return;
		// 界面正在编辑的那份跳过，免得冲掉未保存的编辑
		var editing = ClientUtil.mc.screen instanceof ConfigScreen<?, ?> screen ? screen.editedFiles() : Set.<FileKey>of();
		for (var key : ConfigWatchService.poll(editing, ConfigType.CLIENT, ConfigType.COMMON)) {
			ConfigRegistry.getHandler(key).reloadAndCopy();
			ClientLockManager.reapply(key);
		}
	}
	public static void onLoggingOut() {
		ClientServerConfigCache.clear();
		ClientLockManager.clear();
		ConfigRegistry.applyWorldRestart();
	}
	public static void onScreenMousePressed(double mouseX, double mouseY) {
		FlexEditBox.onScreenMouseClicked(mouseX, mouseY);
	}
}
