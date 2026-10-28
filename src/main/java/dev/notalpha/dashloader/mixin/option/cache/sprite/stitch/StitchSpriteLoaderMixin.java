package dev.notalpha.dashloader.mixin.option.cache.sprite.stitch;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.sprite.stitch.DashTextureStitcher;
import dev.notalpha.dashloader.client.sprite.stitch.SpriteStitcherModule;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.resources.Identifier;

@Mixin(SpriteLoader.class)
public final class StitchSpriteLoaderMixin {
	@Shadow
	@Final
	private Identifier location;

	@WrapOperation(
			method = "stitch",
			at = @At(value = "NEW", target = "(IIII)Lnet/minecraft/client/renderer/texture/Stitcher;")
	)
	private Stitcher<?> dashloaderStitcherLoad(int maxWidth, int maxHeight, int mipLevel, int anisotropy, Operation<Stitcher<?>> original) {
		var map = SpriteStitcherModule.STITCHERS_LOAD.get(CacheStatus.LOAD);
		if (map != null) {
			var data = map.get(location);
			if (data != null) {
				if (data.matches(maxWidth, maxHeight, mipLevel, anisotropy)) {
					return new DashTextureStitcher<>(maxWidth, maxHeight, mipLevel, anisotropy, data);
				}
				// Stitch parameters changed (e.g. mipmap/anisotropy video settings):
				// cached packing is stale, stitch vanilla instead of corrupting the atlas.
				DashLoader.LOG.info("Stitch parameters changed for {}, re-stitching vanilla.", location);
			}
		}

		return original.call(maxWidth, maxHeight, mipLevel, anisotropy);
	}

	@WrapOperation(
			method = "stitch",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/Stitcher;stitch()V")
	)
	private void dashloaderStitcherSave(Stitcher<SpriteContents> instance, Operation<Void> original) {
		original.call(instance);
		SpriteStitcherModule.STITCHERS_SAVE.visit(CacheStatus.SAVE, map -> map.add(Pair.of(location, instance)));
	}
}
