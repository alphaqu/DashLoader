package dev.quantumfusion.dashloader.def.data.font;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import net.minecraft.client.font.Font;

@DashObject(Font.class)
public interface DashFont extends Dashable<Font> {
	Font export(RegistryReader exportHandler);
}

