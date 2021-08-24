package net.oskarstrom.dashloader.def.util.mixins;

import it.unimi.dsi.fastutil.ints.IntComparator;
import net.minecraft.client.font.FontManager;

import java.util.function.BiFunction;

public class MixinValues {
	public static final BiFunction<int[], int[], IntComparator> func = (ks, js) -> (IntComparator) (i, j) -> js[i] == js[j] ? Integer.compare(ks[i], ks[j]) : Integer.compare(js[i], js[j]);
	public static FontManager fontManager;
}
