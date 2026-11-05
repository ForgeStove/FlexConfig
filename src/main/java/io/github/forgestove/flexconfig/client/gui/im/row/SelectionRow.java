package io.github.forgestove.flexconfig.client.gui.im.row;
import io.github.forgestove.flexconfig.client.gui.im.core.UiTheme;
import io.github.forgestove.flexconfig.client.gui.im.model.RowContext;
import io.github.forgestove.flexconfig.client.gui.im.widget.UiDropdown;
import io.github.forgestove.flexconfig.client.gui.im.widget.UiDropdown.Option;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
/** 任意候选集的下拉行，供自定义类型提供器使用。 */
@SuppressWarnings("unused")
public class SelectionRow extends ValueRow {
	public SelectionRow(RowContext ctx, ValueConfigNode<?, ?> node, List<?> candidates, Function<Object, Component> text) {
		super(ctx, node);
		var options = new ArrayList<Option>();
		for (var candidate : candidates) options.add(new Option(candidate, text.apply(candidate)));
		var dropdown = new UiDropdown(options, this::value, this::setValue, d -> ctx.screen().openDropdown(d));
		actionBar.add(dropdown, UiTheme.CONTROL_WIDTH);
	}
}
