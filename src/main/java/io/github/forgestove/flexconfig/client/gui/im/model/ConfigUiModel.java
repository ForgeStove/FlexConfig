package io.github.forgestove.flexconfig.client.gui.im.model;
import io.github.forgestove.flexconfig.tree.CategoryConfigNode;
import java.util.ArrayList;
import java.util.List;
public class ConfigUiModel {
	private final RowContext ctx;
	private final List<ConfigTabModel> tabs = new ArrayList<>();
	private int active;
	public ConfigUiModel(RowContext ctx) {
		this.ctx = ctx;
	}
	public void build(List<CategoryConfigNode<?>> categories) {
		var previous = active < tabs.size() ? tabs.get(active).node : null;
		tabs.clear();
		for (var category : categories) tabs.add(buildTab(category));
		active = 0;
		if (previous != null) for (var i = 0; i < tabs.size(); i++) if (tabs.get(i).node == previous) {
			active = i;
			break;
		}
	}
	private ConfigTabModel buildTab(CategoryConfigNode<?> category) {
		var tab = new ConfigTabModel(category);
		tab.build(ctx);
		return tab;
	}
	public List<ConfigTabModel> tabs() {
		return tabs;
	}
	public int activeIndex() {
		return active;
	}
	public void setActive(int index) {
		if (index >= 0 && index < tabs.size()) active = index;
	}
	public ConfigTabModel activeTab() {
		return tabs.isEmpty() ? null : tabs.get(active);
	}
	public void refreshAll() {
		for (var tab : tabs) tab.refresh();
	}
	public void rebuildActive() {
		var tab = activeTab();
		if (tab != null) tab.build(ctx);
	}
	public boolean hasError() {
		for (var tab : tabs) if (tab.hasError) return true;
		return false;
	}
	public int changedCount() {
		var count = 0;
		for (var tab : tabs) count += tab.changedCount;
		return count;
	}
}
