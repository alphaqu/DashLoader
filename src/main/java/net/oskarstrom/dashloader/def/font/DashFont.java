package net.oskarstrom.dashloader.def.font;

import net.minecraft.client.font.Font;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;

public interface DashFont extends Dashable<Font> {
	Font toUndash(DashExportHandler exportHandler);

}

