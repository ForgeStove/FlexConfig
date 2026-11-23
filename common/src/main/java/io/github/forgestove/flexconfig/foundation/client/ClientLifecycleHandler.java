package io.github.forgestove.flexconfig.foundation.client;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.foundation.*;
import io.github.forgestove.flexconfig.foundation.client.screen.ConfigScreen;
import io.github.forgestove.flexconfig.platform.FlexConfigServices;
import net.minecraft.client.resources.language.I18n;

import java.util.*;
public class ClientLifecycleHandler {
	private static final Map<String, Boolean> PREVIOUS_STATES = new HashMap<>();
	private static int watchTicks;
	private static String scannedLanguage;
	public static void tick() {
		checkTranslations();
		watchConfigFiles();
		if (ClientUtil.mc.screen != null) return;
		var window = ClientUtil.mc.getWindow().getWindow();
		ConfigStore.getRegisteredFileKeys().forEach(key -> ConfigStore.getHandler(key).tickTriggerKeybinds(window, PREVIOUS_STATES));
	}
	private static void checkTranslations() {
		if (!FlexConfigServices.PLATFORM.isDevelopmentEnvironment()) return;
		if (!I18n.exists("menu.singleplayer")) return;
		var language = ClientUtil.mc.getLanguageManager().getSelected();
		if (language.equals(scannedLanguage)) return;
		scannedLanguage = language;
		ConfigStore.getRegisteredFileKeys().forEach(key -> TranslationChecker.check(ConfigStore.getRootConfigNode(key)));
		TranslationChecker.checkExternalSources();
	}
	private static void watchConfigFiles() {
		if (++watchTicks % ConfigWatchService.INTERVAL_TICKS != 0) return;
		var editing = ClientUtil.mc.screen instanceof ConfigScreen<?, ?> screen ? screen.editedFiles() : Set.<FileKey>of();
		for (var key : ConfigWatchService.poll(editing, ConfigType.CLIENT, ConfigType.COMMON)) {
			ConfigStore.getHandler(key).reloadAndCopy();
			ClientLockManager.reapply(key);
		}
	}
	public static void onLoggingOut() {
		ClientServerConfigCache.clear();
		ClientLockManager.clear();
		ConfigStore.applyWorldRestart();
	}
}
