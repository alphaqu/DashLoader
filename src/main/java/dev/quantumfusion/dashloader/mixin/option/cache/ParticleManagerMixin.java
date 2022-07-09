package dev.quantumfusion.dashloader.mixin.option.cache;

import dev.quantumfusion.dashloader.mixin.accessor.ParticleManagerSimpleSpriteProviderAccessor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static dev.quantumfusion.dashloader.DashLoader.DL;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

	@Shadow
	@Final
	private Map<Identifier, ParticleManager.SimpleSpriteProvider> spriteAwareFactories;
	@Mutable
	@Shadow
	@Final
	private SpriteAtlasTexture particleAtlasTexture;

	@Inject(
			method = "reload(Lnet/minecraft/resource/ResourceReloader$Synchronizer;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;Lnet/minecraft/util/profiler/Profiler;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
			at = @At(value = "HEAD")
	)
	private void reloadParticlesFast(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		// Atlas injection
		if (DL.isRead()) {
			var data = DL.getData().getReadContextData();
			var atlas = data.dashAtlasManager.getAtlas(SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
			if (atlas != null) {
				this.particleAtlasTexture = atlas;
			}
		}
	}

	@Inject(
			method = "method_18831",
			at = @At(value = "TAIL")
	)
	private void cacheParticles(Profiler profiler, Map map, SpriteAtlasTexture.Data data, CallbackInfo ci) {
		if (DL.isWrite()) {
			var particles = new HashMap<Identifier, List<Sprite>>();
			this.spriteAwareFactories.forEach((identifier, simpleSpriteProvider) -> {
				var access = (ParticleManagerSimpleSpriteProviderAccessor) simpleSpriteProvider;
				particles.put(identifier, access.getSprites());
			});
			DL.getData().particleSprites.setMinecraftData(particles);
			DL.getData().particleAtlas.setMinecraftData(this.particleAtlasTexture);
		}
	}

}
