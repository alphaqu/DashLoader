package dev.quantumfusion.dashloader.def.data.font;

import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.font.BlankFont;

@Data
@DashObject(BlankFont.class)
public class DashBlankFont implements DashFont {
	@Override
	public BlankFont export(DashRegistryReader exportHandler) {
		return new BlankFont();
	}
}
