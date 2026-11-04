package io.github.forgestove.flexconfig.client.gui.entry;

import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;

import java.util.regex.Pattern;
public class ShortValueConfigEntry<C> extends GenericValueConfigEntry<C, Short> {
	private static final Pattern SHORT_PATTERN = Pattern.compile("-?\\d*");
	public ShortValueConfigEntry(ConfigCategoryTab<C, Short> tab, ValueConfigNode<C, Short> node) {
		super(tab, node, s -> isZero(s) ? (short) 0 : Short.parseShort(s), s -> isZero(s) || SHORT_PATTERN.matcher(s).matches());
	}
}
