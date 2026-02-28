package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.collection.IntIntList;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.mixin.accessor.FilterMapAccessor;
import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.client.gui.font.FontOption;

import java.util.HashMap;
import java.util.Map;

public class DashFontFilterPair implements DashObject<GlyphProvider.Conditional, GlyphProvider.Conditional> {
public final int provider;
public final IntIntList filter;

public DashFontFilterPair(int provider, IntIntList filter) {
this.provider = provider;
this.filter = filter;
}

public DashFontFilterPair(GlyphProvider.Conditional fontFilterPair, RegistryWriter writer) {
this.provider = writer.add(fontFilterPair.provider());

filter = new IntIntList();
((FilterMapAccessor) fontFilterPair.filter()).getValues().forEach(
(key, value) -> filter.put(key.ordinal(), value ? 1 : 0));
}

@Override
public GlyphProvider.Conditional export(RegistryReader reader) {
Map<FontOption, Boolean> activeFilters = new HashMap<>();
filter.forEach((key, value) -> activeFilters.put(FontOption.values()[key], value == 1));
return new GlyphProvider.Conditional(reader.get(provider), new FontOption.Filter(activeFilters));
}
}
