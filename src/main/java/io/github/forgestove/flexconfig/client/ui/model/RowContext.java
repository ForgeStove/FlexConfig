package io.github.forgestove.flexconfig.client.ui.model;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.client.ui.ConfigScreen;
/** 行与配置界面之间的窄接口，行不直接依赖界面的私有状态。 */
public record RowContext(ConfigScreen<?, ?> screen) {
	public ConfigHandler<?, ?> handler() {
		return screen.getHandler();
	}
	public Object config() {
		return screen.config();
	}
	public FileKey fileKey() {
		return new FileKey(screen.root.modId(), screen.root.fileName());
	}
	public boolean editable() {
		return screen.isEditable();
	}
	public boolean lockable() {
		return screen.isLockable();
	}
	public void refresh() {
		screen.refresh();
	}
	public void rebuildTab() {
		screen.rebuildActiveTab();
	}
	public void beginCapture(CaptureTarget target) {
		screen.beginCapture(target);
	}
	public void endCapture(CaptureTarget target) {
		screen.endCapture(target);
	}
	public void saveByKeybind() {
		screen.saveByKeybind();
	}
}
