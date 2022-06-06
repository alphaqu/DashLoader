package dev.quantumfusion.dashloader.mixin.option.cache;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import dev.quantumfusion.dashloader.mixin.accessor.ParticleManagerSimpleSpriteProviderAccessor;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
	@Shadow
	@Final
	private Map<ParticleTextureSheet, Queue<Particle>> particles;
	@Shadow
	@Final
	private TextureManager textureManager;

	@Shadow
	protected abstract void loadTextureList(ResourceManager resourceManager, Identifier id, Map<Identifier, List<Identifier>> result);

	// TODO remake this
	// TODO still remake this
	@Inject(
			method = "reload(Lnet/minecraft/resource/ResourceReloader$Synchronizer;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;Lnet/minecraft/util/profiler/Profiler;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void reloadParticlesFast(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {


		var data = DL.getData();
		var particleSprites = data.particleSprites;
		if (false && particleSprites.dataAvailable() && DL.isRead()) {
			// DASHLOADER THINGS

			final Runnable runnable = () -> particleSprites.getCacheResultData().forEach(
					(identifier, sprites) -> this.spriteAwareFactories.get(identifier).setSprites(sprites)
			);
			cir.setReturnValue(CompletableFuture.runAsync(runnable).thenCompose(synchronizer::whenPrepared));
			throw new RuntimeException("");
		} else {
			Map<Identifier, List<Identifier>> map = Maps.newConcurrentMap();
			CompletableFuture<?>[] completableFutures = Registry.PARTICLE_TYPE.getIds().stream().map(
					(identifier) -> CompletableFuture.runAsync(
							() -> this.loadTextureList(manager, identifier, map), prepareExecutor)
			).toArray(CompletableFuture<?>[]::new);

			CompletableFuture<?> stitchTask = CompletableFuture.allOf(completableFutures).thenApplyAsync((void_) -> {
				prepareProfiler.startTick();
				prepareProfiler.push("stitching");
				SpriteAtlasTexture.Data spriteData = this.particleAtlasTexture.stitch(manager, map.values().stream().flatMap(Collection::stream), prepareProfiler, 0);
				prepareProfiler.pop();
				prepareProfiler.endTick();
				return spriteData;
			}, prepareExecutor);
			cir.setReturnValue(stitchTask.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((spriteData) -> {
				this.particles.clear();
				applyProfiler.startTick();
				applyProfiler.push("upload");
				this.particleAtlasTexture.upload((SpriteAtlasTexture.Data) spriteData);
				applyProfiler.swap("bindSpriteSets");
				Sprite sprite = this.particleAtlasTexture.getSprite(MissingSprite.getMissingSpriteId());
				map.forEach((identifier, spritesAssets) -> {
					ImmutableList<Sprite> spriteList;
					if (spritesAssets.isEmpty()) {
						spriteList = ImmutableList.of(sprite);
					} else {
						Stream<Identifier> spriteStream = spritesAssets.stream();
						SpriteAtlasTexture spriteAtlasTexture = this.particleAtlasTexture;
						spriteList = spriteStream.map(spriteAtlasTexture::getSprite).collect(ImmutableList.toImmutableList());

					}
					ImmutableList<Sprite> immutableList = spriteList;
					((ParticleManagerSimpleSpriteProviderAccessor) this.spriteAwareFactories.get(identifier)).setSprites(immutableList);
				});
				applyProfiler.pop();
				applyProfiler.endTick();

			}, applyExecutor));
		}
		cir.cancel();
	}

	@Inject(
			method = "reload(Lnet/minecraft/resource/ResourceReloader$Synchronizer;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;Lnet/minecraft/util/profiler/Profiler;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
			at = @At(value = "TAIL"),
			cancellable = true
	)
	private void cacheParticles(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		if (DL.isWrite()) {
			var particles = new HashMap<Identifier, List<Sprite>>();
			this.spriteAwareFactories.forEach(
					(identifier, simpleSpriteProvider) -> particles.put(identifier, ((ParticleManagerSimpleSpriteProviderAccessor) simpleSpriteProvider).getSprites()));
			DL.getData().particleSprites.setMinecraftData(particles);
			DL.getData().particleAtlas.setMinecraftData(this.particleAtlasTexture);
		}
	}

}
