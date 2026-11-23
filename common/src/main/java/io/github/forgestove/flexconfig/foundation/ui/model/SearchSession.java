package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.foundation.client.Translation;
import io.github.forgestove.flexconfig.foundation.ui.row.*;

import java.util.List;
public class SearchSession {
	private final RowContext ctx;
	private final Runnable clearAction;
	private String query = "";
	private boolean changedOnly;
	private List<ConfigRow> results = List.of();
	private boolean dirty;
	private int count;
	private int version = -1;
	/** {@code clearAction} 供空结果提示行使用，由界面负责清空搜索框和开关。 */
	public SearchSession(RowContext ctx, Runnable clearAction) {
		this.ctx = ctx;
		this.clearAction = clearAction;
	}
	public String query() {
		return query;
	}
	public boolean changedOnly() {
		return changedOnly;
	}
	public boolean filtering() {
		return !query.isEmpty() || changedOnly;
	}
	public List<ConfigRow> rows() {
		return filtering() ? results : List.of();
	}
	public int count() {
		return count;
	}
	public void setQuery(String query) {
		var next = query.trim();
		if (next.equals(this.query)) return;
		this.query = next;
		dirty = true;
	}
	public void setChangedOnly(boolean changedOnly) {
		if (this.changedOnly == changedOnly) return;
		this.changedOnly = changedOnly;
		dirty = true;
	}
	/** 只清查询词，保留「已修改」开关；界面重新初始化时沿用用户的筛选状态。 */
	public void clearQuery() {
		if (query.isEmpty()) return;
		query = "";
		dirty = true;
	}
	public void clear() {
		query = "";
		changedOnly = false;
		dirty = true;
	}
	/** 编辑后调用；只有筛选生效时才需要重算。 */
	public void markDirty() {
		if (filtering()) dirty = true;
	}
	public boolean needsRebuild(int modelVersion) {
		return dirty || version != modelVersion;
	}
	public void rebuild(ConfigUiModel model, int modelVersion) {
		SearchIndex.clearHighlights(model);
		if (!filtering()) {
			results = List.of();
			count = 0;
			version = modelVersion;
			dirty = false;
			return;
		}
		var found = SearchIndex.search(ctx, model, query, changedOnly);
		count = found.size();
		if (found.isEmpty()) {
			var message = query.isEmpty() ? Translation.SEARCH_NO_CHANGES : Translation.searchNoMatch(query);
			found.add(new SearchEmptyRow(ctx, message, clearAction));
		}
		results = found;
		version = modelVersion;
		dirty = false;
	}
}
