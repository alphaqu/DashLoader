package dev.quantumfusion.dashloader.def.data.font;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import net.minecraft.client.font.Font;

public interface DashFont extends Dashable<Font> {
	Font export(DashRegistryReader exportHandler);
}

