package net.oskarstrom.dashloader.def.font;

import net.minecraft.client.font.Font;
import net.oskarstrom.dashloader.api.Dashable;
import net.oskarstrom.dashloader.api.registry.DashRegistry;

public interface DashFont extends Dashable<Font> {
	Font toUndash(DashRegistry registry);

}

