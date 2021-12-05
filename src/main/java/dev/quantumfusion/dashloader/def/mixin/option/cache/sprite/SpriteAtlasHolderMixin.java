package dev.quantumfusion.dashloader.def.mixin.option.cache.sprite;

import dev.quantumfusion.dashloader.def.DashDataManager.DashReadContextData.DashAtlasManager;
import dev.quantumfusion.dashloader.def.DashLoader;
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
		if (DashLoader.isRead()) {
			var dashAtlasManager = DashLoader.getData().getReadContextData().dashAtlasManager;
			if (dashAtlasManager.getAtlas(this.atlas.getId()) != null) {
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
		if (DashLoader.isRead()) {
			var dashAtlasManager = DashLoader.getData().getReadContextData().dashAtlasManager;
			final SpriteAtlasTexture atlas = dashAtlasManager.getAtlas(this.atlas.getId());
			if (atlas != null) {
				this.atlas = atlas;
				ci.cancel();
			}
		}
	}

	@Inject(
			method = "apply(Lnet/minecraft/client/texture/SpriteAtlasTexture$Data;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
			at = @At(value = "TAIL"),
			cancellable = true
	)
	private void applyCreate(SpriteAtlasTexture.Data data, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
		if (DashLoader.isRead()) {
			ci.cancel();
		} else {
			DashLoader.getData().getWriteContextData().extraAtlases.add(atlas);
		}
	}
}
