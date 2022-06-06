package dev.quantumfusion.dashloader.mixin.option.cache.sprite;

import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.data.image.DashSpriteAtlasTextureData;
import dev.quantumfusion.dashloader.fallback.sprite.FakeTextureStitcher;
import dev.quantumfusion.dashloader.mixin.accessor.SpriteAccessor;
import dev.quantumfusion.dashloader.util.mixins.SpriteAtlasTextureDuck;
import dev.quantumfusion.dashloader.util.mixins.SpriteInfoDuck;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.MutablePair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

@Mixin(SpriteAtlasTexture.class)
public class SpriteAtlasTextureMixin implements SpriteAtlasTextureDuck {
	private boolean dashLoaded = false;
	private DashSpriteAtlasTextureData data;
	private Map<Identifier, MutablePair<Sprite, Sprite.Info>> cachedSprites;


	@Inject(
			method = "upload(Lnet/minecraft/client/texture/SpriteAtlasTexture$Data;)V",
			at = @At(value = "HEAD")
	)
	private void saveAtlasInfo(SpriteAtlasTexture.Data data, CallbackInfo ci) {
		if (DashLoader.isWrite()) {
			DashLoader.getData().getWriteContextData().atlasData.put((SpriteAtlasTexture) (Object) this, new DashSpriteAtlasTextureData(data));
		}
	}

	@Redirect(
			method = "stitch",
			at = @At(value = "NEW", target = "Lnet/minecraft/client/texture/TextureStitcher;<init>")
	)
	private TextureStitcher textureStitcher(int maxWidth, int maxHeight, int mipLevel) {
		// If its not cached then upload the sprite. In a cached state everything gets uploaded at once.
		if (this.dashLoaded) {
			return new FakeTextureStitcher(this.data.width(), this.data.height(), this.data.mipLevel(), this.cachedSprites);
		}
		return new TextureStitcher(maxWidth, maxHeight, mipLevel);
	}

	@Inject(
			method = "method_18160*",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void loadSpriteInfo(Identifier identifier, ResourceManager resourceManager, Queue<Sprite.Info> queue, CallbackInfo ci) {
		if (this.dashLoaded) {
			MutablePair<Sprite, Sprite.Info> entry = this.cachedSprites.get(identifier);
			if (entry != null) {
				Sprite cached = entry.left;
				// lets hope animations are not important
				Sprite.Info info = new Sprite.Info(identifier, cached.getWidth(), cached.getHeight(), AnimationResourceMetadata.EMPTY);
				((SpriteInfoDuck) (Object) info).setCached(cached);
				entry.right = info;

				// Add to minecraft queue
				queue.add(info);
				ci.cancel();
			} else {
				DashLoader.LOGGER.warn("Could not find cached sprite {}. This may cause huge visual issues and/or NPE.", identifier);
			}
		}
	}


	@Inject(
			method = "loadSprite*",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void loadSprite(ResourceManager container, Sprite.Info info, int atlasWidth, int atlasHeight, int maxLevel, int x, int y, CallbackInfoReturnable<Sprite> cir) {
		Sprite cached = ((SpriteInfoDuck) (Object) info).getCached();
		if (cached != null) {
			final SpriteAccessor access = (SpriteAccessor) cached;
			access.setAtlas((SpriteAtlasTexture) (Object) this);
			access.setId(info.getId());
			cir.setReturnValue(cached);
		}
	}

	@Inject(
			method = "clear",
			at = @At(value = "TAIL")
	)
	private void clearAtlas(CallbackInfo ci) {
		this.dashLoaded = false;
		this.data = null;
		this.cachedSprites = null;
	}


	@Override
	public void dashLoaded(DashSpriteAtlasTextureData data, Map<Identifier, Sprite> sprites) {
		this.dashLoaded = true;
		this.data = data;
		this.cachedSprites = new HashMap<>();
		sprites.forEach((identifier, sprite) -> this.cachedSprites.put(identifier, new MutablePair<>(sprite, null)));
	}

	@Override
	public Map<Identifier, MutablePair<Sprite, Sprite.Info>> getCachedSprites() {
		return this.cachedSprites;
	}
}
