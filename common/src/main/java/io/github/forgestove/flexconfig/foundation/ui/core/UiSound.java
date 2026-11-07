package io.github.forgestove.flexconfig.foundation.ui.core;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
public class UiSound {
	private static final float VOLUME = 0.25F;
	public static void click() {
		click(1F);
	}
	public static void click(float pitch) {
		ClientUtil.mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), pitch, VOLUME));
	}
}
