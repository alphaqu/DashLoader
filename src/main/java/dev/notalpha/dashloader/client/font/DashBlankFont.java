package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import net.minecraft.client.gui.font.AllMissingGlyphProvider;

public final class DashBlankFont implements DashObject<AllMissingGlyphProvider, AllMissingGlyphProvider> {
@Override
public AllMissingGlyphProvider export(RegistryReader exportHandler) {
return new AllMissingGlyphProvider();
}
}
