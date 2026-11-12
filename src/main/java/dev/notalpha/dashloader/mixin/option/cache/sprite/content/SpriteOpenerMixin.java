package dev.notalpha.dashloader.mixin.option.cache.sprite.content;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.sprite.content.SpriteContentModule;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpriteResourceLoader.class)
public interface SpriteOpenerMixin {
	@ModifyReturnValue(
			method = "create",
			at = @At("RETURN")
	)
	private static SpriteResourceLoader dashloaderWrapLoader(SpriteResourceLoader original) {
		return (spriteLocation, resource) -> {
			var loadData = SpriteContentModule.SOURCE.get(CacheStatus.LOAD);
			if (loadData != null) {
				SpriteContents cached = loadData.get(spriteLocation);
				if (cached != null) {
					return cached;
				}
			}

			SpriteContents result = original.loadSprite(spriteLocation, resource);

			SpriteContentModule.SOURCE.visit(CacheStatus.SAVE, map -> {
				if (map.containsKey(spriteLocation)) {
					SpriteContents existing = map.get(spriteLocation);
					if (existing != null && !SpriteContentModule.sameContents(existing, result)) {
						// Same id, different texture (e.g. wither painting vs. wither
						// effect icon): force the vanilla mechanism by caching null.
						DashLoader.LOG.warn("Duplicate sprite {} with different contents, using vanilla loading.", spriteLocation);
						map.put(spriteLocation, null);
					}
					// Identical double open or already-poisoned id: keep as is.
				} else {
					map.put(spriteLocation, result);
				}
			});
			return result;
		};
	}
}
