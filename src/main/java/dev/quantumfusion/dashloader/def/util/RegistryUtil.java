package dev.quantumfusion.dashloader.def.util;

import net.minecraft.client.render.model.json.MultipartModelSelector;

public class RegistryUtil {
	public static MultipartModelSelector preparePredicate(final MultipartModelSelector selector) {
		if (selector == MultipartModelSelector.TRUE || selector == MultipartModelSelector.FALSE) {
			return new BooleanSelector(selector);
		}
		return selector;
	}
}
