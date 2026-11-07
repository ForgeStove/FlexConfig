package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.api.FileKey;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
import io.github.forgestove.flexconfig.foundation.ui.ConfigShell;
/** 行与配置界面之间的窄接口，行不直接依赖界面或 Screen 的私有状态。 */
public record RowContext(ConfigShell<?, ?> shell) {
	public ConfigHandler<?, ?> handler() {
		return shell.getHandler();
	}
	public Object config() {
		return shell.config();
	}
	public FileKey fileKey() {
		return new FileKey(shell.root.modId(), shell.root.fileName());
	}
	public boolean editable() {
		return shell.isEditable();
	}
	public boolean lockable() {
		return shell.isLockable();
	}
	public void refresh() {
		shell.refresh();
	}
	public void rebuildTab() {
		shell.rebuildActiveTab();
	}
	public void beginCapture(CaptureTarget target) {
		shell.beginCapture(target);
	}
	public void endCapture(CaptureTarget target) {
		shell.endCapture(target);
	}
	public void saveByKeybind() {
		shell.saveByKeybind();
	}
}
