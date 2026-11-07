package io.github.forgestove.flexconfig.foundation.ui.core;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
/** 弹簧/补间参数与位移常量。 */
public class UiMotion {
	public static boolean reducedMotion() {
		return ClientUtil.mc.options.screenEffectScale().get() <= 0D;
	}
	public record SpringSpec(float response, float damping) {}
	public record TweenSpec(float durationMs, UiEasing easing) {}
	public static final SpringSpec HOVER_IN = new SpringSpec(0.08F, 1F);
	public static final SpringSpec HOVER_OUT = new SpringSpec(0.16F, 1F);
	public static final SpringSpec CONTROL = new SpringSpec(0.18F, 0.82F);
	public static final SpringSpec SELECT = new SpringSpec(0.18F, 0.9F);
	public static final SpringSpec INDICATOR_LEAD = new SpringSpec(0.14F, 1F);
	public static final SpringSpec INDICATOR_TRAIL = new SpringSpec(0.24F, 1F);
	public static final SpringSpec THUMB = new SpringSpec(0.12F, 1F);
	public static final SpringSpec SEARCH_WIDTH = new SpringSpec(0.2F, 1F);
	public static final SpringSpec SCROLL = new SpringSpec(0.16F, 1F);
	public static final TweenSpec ENTER = new TweenSpec(220F, UiEasing.ENTER);
	public static final TweenSpec EXIT = new TweenSpec(110F, UiEasing.EXIT);
	public static final TweenSpec BACKDROP_IN = new TweenSpec(160F, UiEasing.ENTER);
	public static final TweenSpec CONTENT_ROWS_IN = new TweenSpec(180F, UiEasing.ENTER);
	public static final TweenSpec TAB_ROWS_IN = new TweenSpec(160F, UiEasing.ENTER);
	public static final float PAGE_SLIDE_IN = 24F, PAGE_SLIDE_OUT = 16F;
	public static final TweenSpec POPUP_IN = new TweenSpec(120F, UiEasing.ENTER);
	public static final TweenSpec POPUP_OUT = new TweenSpec(80F, UiEasing.EXIT);
	public static final TweenSpec TOOLTIP_FADE = new TweenSpec(100F, UiEasing.ENTER);
	public static final TweenSpec RESET_WASH = new TweenSpec(260F, UiEasing.ENTER);
	public static final TweenSpec DOT = new TweenSpec(120F, UiEasing.ENTER);
	public static final float RISE = 6F;
	public static final float CLOSE_DROP = 4F;
	public static final float CONTENT_ROWS_DELAY = 80F;
	public static final float ROW_STAGGER = 14F;
	public static final int ROW_STAGGER_MAX = 9;
	public static final float TOOLTIP_HOVER_DELAY = 450F;
	public static final float TOOLTIP_FOCUS_DELAY = 700F;
}
