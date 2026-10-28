package dev.notalpha.dashloader.mixin.option.cache.sprite.content;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.sprite.content.SpriteContentModule;
import dev.notalpha.dashloader.mixin.accessor.SpriteContentsAccessor;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/**
 * 1.21.5: SpriteOpener is a functional interface; the sprite loading logic lives in
 * the lambda returned by {@code SpriteOpener.create}. Wrap that opener so LOAD can
 * return cached {@link SpriteContents} (with fresh metadata) and SAVE can capture
 * freshly loaded contents. Replaces the old method_52851 injects.
 */
@Mixin(SpriteOpener.class)
public interface SpriteOpenerMixin {
	@Inject(
			method = "create",
			at = @At(value = "RETURN"),
			cancellable = true
	)
	private static void dashloaderWrap(Collection<ResourceMetadataSerializer<?>> metadatas, CallbackInfoReturnable<SpriteOpener> cir) {
		SpriteOpener original = cir.getReturnValue();
		cir.setReturnValue((Identifier id, Resource resource) -> {
			var loadData = SpriteContentModule.SOURCE.get(CacheStatus.LOAD);
			if (loadData != null) {
				SpriteContents cached = loadData.get(id);
				if (cached != null) {
					ResourceMetadata resourceMetadata;
					try {
						resourceMetadata = resource.getMetadata().copy(metadatas);
					} catch (Exception exception) {
						SpriteOpener.LOGGER.error("Unable to parse metadata from {}", id, exception);
						return null;
					}
					((SpriteContentsAccessor) cached).setMetadata(resourceMetadata);
					return cached;
				}
			}

			SpriteContents result = original.loadSprite(id, resource);

			var saveData = SpriteContentModule.SOURCE.get(CacheStatus.SAVE);
			if (saveData != null && result != null) {
				if (saveData.containsKey(id)) {
					SpriteContents existing = saveData.get(id);
					if (existing != null && !SpriteContentModule.sameContents(existing, result)) {
						// Same id, different texture (e.g. wither painting vs. wither
						// effect icon): force the vanilla mechanism by caching null.
						DashLoader.LOG.warn("Duplicate sprite {} with different contents, using vanilla loading.", id);
						saveData.put(id, null);
					}
					// Identical double open or already-poisoned id: keep as is.
				} else {
					saveData.put(id, result);
				}
			}
			return result;
		});
	}
}
