package dev.quantumfusion.dashloader.def.data.font;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.annotations.DashObject;
import net.minecraft.client.font.BlankFont;

@Data
@DashObject(BlankFont.class)
public class DashBlankFont implements DashFont {
	@Override
	public BlankFont toUndash(DashExportHandler exportHandler) {
		return new BlankFont();
	}
}
