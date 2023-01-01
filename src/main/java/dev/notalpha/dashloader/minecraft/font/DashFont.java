package dev.notalpha.dashloader.minecraft.font;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.Dashable;
import dev.notalpha.dashloader.registry.RegistryReader;
import net.minecraft.client.font.Font;

@DashObject(Font.class)
public interface DashFont extends Dashable<Font> {
	Font export(RegistryReader exportHandler);
}

