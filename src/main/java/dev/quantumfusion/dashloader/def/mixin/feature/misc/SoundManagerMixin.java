package dev.quantumfusion.dashloader.def.mixin.feature.misc;

import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
	/**
	 * Save time by not checking sounds.
	 *
	 * @author notequalalpha
	 */
	@Overwrite
	public static boolean isSoundResourcePresent(Sound sound, Identifier identifier, ResourceManager resourceManager) {
		return true;
	}
}
