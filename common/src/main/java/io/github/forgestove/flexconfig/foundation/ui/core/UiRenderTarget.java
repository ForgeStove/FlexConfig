package io.github.forgestove.flexconfig.foundation.ui.core;
/**
 * 界面绘制需要的底层图形操作。GuiGraphics 等具体图形库类型只出现在实现类中，
 * 界面代码通过这个接口绘制，不直接依赖渲染库。
 */
public interface UiRenderTarget {
	void pushPose();
	void popPose();
	void translate(float x, float y, float z);
	void scale(float x, float y, float z);
	void fill(int x1, int y1, int x2, int y2, int color);
	void fillGradient(int x1, int y1, int x2, int y2, int top, int bottom);
	void blit(
		UiTexture texture,
		int x,
		int y,
		int width,
		int height,
		float u,
		float v,
		int uWidth,
		int vHeight,
		int texWidth,
		int texHeight
	);
	void text(UiText text, int x, int y, int color, boolean shadow);
	void enableScissor(int x1, int y1, int x2, int y2);
	void disableScissor();
	void flush();
	void setShaderColor(float red, float green, float blue, float alpha);
}
