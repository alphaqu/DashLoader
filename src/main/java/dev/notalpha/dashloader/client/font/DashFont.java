package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.Exportable;
import dev.notalpha.dashloader.registry.RegistryReader;
import net.minecraft.client.font.Font;

@DashObject(Font.class)
public interface DashFont extends Exportable<Font> {
	Font export(RegistryReader exportHandler);
}

