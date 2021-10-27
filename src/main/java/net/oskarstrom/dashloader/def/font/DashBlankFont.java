package net.oskarstrom.dashloader.def.font;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.def.api.DashObject;
import net.minecraft.client.font.BlankFont;
import net.oskarstrom.dashloader.core.registry.DashRegistry;

@Data
@DashObject(BlankFont.class)
public class DashBlankFont implements DashFont {
	@Override
	public BlankFont toUndash(DashExportHandler exportHandler) {
		return new BlankFont();
	}
}
