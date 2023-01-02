package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.registry.RegistryReader;
import net.minecraft.client.font.BlankFont;

public final class DashBlankFont implements DashObject<BlankFont> {
	@Override
	public BlankFont export(RegistryReader exportHandler) {
		return new BlankFont();
	}
}
