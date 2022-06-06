package dev.quantumfusion.dashloader.data.font;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import net.minecraft.client.font.Font;

@DashObject(Font.class)
public interface DashFont extends Dashable<Font> {
	Font export(RegistryReader exportHandler);
}

