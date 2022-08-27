package dev.quantumfusion.dashloader.mixin.option.misc;

import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
	/**
	 * @author notequalalpha
	 * @reason Save time by not checking sounds.
	 */
	@SuppressWarnings("SameReturnValue")
	@Overwrite
	public static boolean isSoundResourcePresent(Sound sound, Identifier identifier, ResourceManager resourceManager) {
		return true;
	}
}
