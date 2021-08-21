package net.oskarstrom.dashloader.def.font;

import net.oskarstrom.dashloader.def.api.DashObject;
import net.minecraft.client.font.BlankFont;
import net.oskarstrom.dashloader.api.registry.DashRegistry;

@DashObject(BlankFont.class)
public class DashBlankFont implements DashFont {
	@Override
	public BlankFont toUndash(DashRegistry registry) {
		return new BlankFont();
	}
}
