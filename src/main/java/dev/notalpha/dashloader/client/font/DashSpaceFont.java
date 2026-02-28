package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import com.mojang.blaze3d.font.SpaceProvider;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.HashMap;
import java.util.Map;

public final class DashSpaceFont implements DashObject<SpaceProvider, SpaceProvider> {
public final int[] ints;
public final float[] floats;

public DashSpaceFont(int[] ints, float[] floats) {
this.ints = ints;
this.floats = floats;
}

public DashSpaceFont(SpaceProvider font) {
IntSet glyphs = font.getSupportedGlyphs();
this.ints = new int[glyphs.size()];
this.floats = new float[glyphs.size()];
int i = 0;
for (Integer providedGlyph : glyphs) {
var glyph = font.getGlyph(providedGlyph);
assert glyph != null;
this.ints[i] = providedGlyph;
this.floats[i] = glyph.info().getAdvance();
i++;
}
}

@Override
public SpaceProvider export(RegistryReader exportHandler) {
Map<Integer, Float> map = new HashMap<>(this.ints.length);
for (int i = 0; i < this.ints.length; i++) {
	map.put(this.ints[i], this.floats[i]);
}
return new SpaceProvider(map);
}
}
