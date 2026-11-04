package io.github.forgestove.flexconfig.client.gui.entry;

import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;

import java.util.regex.Pattern;
public class ByteValueConfigEntry<C> extends GenericValueConfigEntry<C, Byte> {
	private static final Pattern BYTE_PATTERN = Pattern.compile("-?\\d*");
	public ByteValueConfigEntry(ConfigCategoryTab<C, Byte> tab, ValueConfigNode<C, Byte> node) {
		super(tab, node, s -> isZero(s) ? (byte) 0 : Byte.parseByte(s), s -> isZero(s) || BYTE_PATTERN.matcher(s).matches());
	}
}
