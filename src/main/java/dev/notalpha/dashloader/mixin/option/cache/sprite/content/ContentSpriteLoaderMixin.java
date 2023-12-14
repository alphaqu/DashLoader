package dev.notalpha.dashloader.mixin.option.cache.sprite.content;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.DashLoaderClient;
import dev.notalpha.dashloader.client.sprite.content.DashAtlasData;
import dev.notalpha.dashloader.client.sprite.content.DashSpriteOpener;
import dev.notalpha.dashloader.client.sprite.content.SpriteContentModule;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.HashMap;

@Mixin(SpriteLoader.class)
public class ContentSpriteLoaderMixin {

	@Shadow @Final private Identifier id;

	@Redirect(
			method = "load(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/Identifier;ILjava/util/concurrent/Executor;Ljava/util/Collection;)Ljava/util/concurrent/CompletableFuture;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/texture/SpriteOpener;create(Ljava/util/Collection;)Lnet/minecraft/client/texture/SpriteOpener;"
			)
	)
	public SpriteOpener loadOpenerInject(Collection<ResourceMetadataReader<?>> metadatas) {
		var inner = SpriteOpener.create(metadatas);
		var status = DashLoaderClient.CACHE.getStatus();
		var dashSpriteData = SpriteContentModule.ATLASES.get(status);
		if (dashSpriteData != null) {
			DashAtlasData dashAtlasData = dashSpriteData.get(id);
			if (dashAtlasData == null && status == CacheStatus.SAVE) {
				DashLoader.LOG.info("Creating atlas data {}", id);
				dashAtlasData = new DashAtlasData(id, new HashMap<>());
				dashSpriteData.put(id, dashAtlasData);
			}

			if (dashAtlasData != null) {
				return new DashSpriteOpener(inner, metadatas, dashAtlasData);
			}

			DashLoader.LOG.warn("Falling back atlas loading {}", id);
		}

		return inner;
	}
}
