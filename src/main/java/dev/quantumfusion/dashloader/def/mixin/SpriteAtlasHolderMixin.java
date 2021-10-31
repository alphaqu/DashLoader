package dev.quantumfusion.dashloader.def.mixin;

import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.dataobject.MappingData;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(SpriteAtlasHolder.class)
public class SpriteAtlasHolderMixin {
	@Mutable
	@Shadow
	@Final
	private SpriteAtlasTexture atlas;

	@Inject(
			method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Lnet/minecraft/client/texture/SpriteAtlasTexture$Data;",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void prepareOverride(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<SpriteAtlasTexture.Data> cir) {
		final DashLoader loader = DashLoader.getInstance();
		if (loader.getStatus() == DashLoader.Status.LOADED) {
			if (loader.getMappings().getAtlas(this.atlas.getId()) != null) {
				cir.setReturnValue(null);
			}
		}
	}


	@Inject(
			method = "apply(Lnet/minecraft/client/texture/SpriteAtlasTexture$Data;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void applyOverride(SpriteAtlasTexture.Data data, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
		final DashLoader instance = DashLoader.getInstance();
		if (instance.getStatus() == DashLoader.Status.LOADED) {
			final MappingData mappings = instance.getMappings();
			if (mappings != null) {
				final SpriteAtlasTexture atlas = mappings.getAtlas(this.atlas.getId());
				if (atlas != null) {
					this.atlas = atlas;
					ci.cancel();
				}
			}
		}
	}

	@Inject(
			method = "apply(Lnet/minecraft/client/texture/SpriteAtlasTexture$Data;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
			at = @At(value = "TAIL"),
			cancellable = true
	)
	private void applyCreate(SpriteAtlasTexture.Data data, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
		if (DashLoader.getInstance().getStatus() == DashLoader.Status.LOADED) {
			ci.cancel();
		} else {
			DashLoader.getVanillaData().addExtraAtlasAssets(atlas);
		}
	}
}
