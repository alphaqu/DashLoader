package dev.quantumfusion.dashloader.minecraft.font;

import dev.quantumfusion.dashloader.api.Dashable;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import net.minecraft.client.font.Font;

@DashObject(Font.class)
public interface DashFont extends Dashable<Font> {
	Font export(RegistryReader exportHandler);
}

