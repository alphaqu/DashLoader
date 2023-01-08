package dev.notalpha.dashloader.mixin.option.cache.sprite;

import dev.notalpha.dashloader.Cache;
import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.client.model.fallback.FakeTextureStitcher;
import dev.notalpha.dashloader.client.sprite.AtlasData;
import dev.notalpha.dashloader.client.sprite.SpriteModule;
import dev.notalpha.dashloader.misc.duck.SpriteInfoDuck;
import dev.notalpha.dashloader.mixin.accessor.SpriteAccessor;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(SpriteAtlasTexture.class)
public class SpriteAtlasTextureMixin {
	@Shadow
	@Final
	private Identifier id;
	@Nullable
	private AtlasData data;
	private Map<Identifier, MutablePair<Sprite, Sprite.Info>> cachedSprites;

	@Inject(
			method = "upload(Lnet/minecraft/client/texture/SpriteAtlasTexture$Data;)V",
			at = @At(value = "HEAD")
	)
	private void saveAtlasInfo(SpriteAtlasTexture.Data data, CallbackInfo ci) {
		SpriteModule.ATLASES.visit(Cache.Status.SAVE, map -> {
			map.put(this.id, new AtlasData((SpriteAtlasTexture) (Object) this, data));
		});
	}

	@Inject(
			method = "stitch",
			at = @At(value = "HEAD")
	)
	private void dashLoaderInject(ResourceManager resourceManager, Stream<Identifier> idStream, Profiler profiler, int mipmapLevel, CallbackInfoReturnable<SpriteAtlasTexture.Data> cir) {
		SpriteModule.ATLASES.visit(Cache.Status.LOAD, map -> {
			AtlasData atlasData = map.get(id);
			if (atlasData != null) {
				this.data = atlasData;
				this.cachedSprites = new HashMap<>();
				atlasData.sprites.forEach((identifier, sprite) -> {
					this.cachedSprites.put(identifier, new MutablePair<>(sprite, null));
				});
			}
		});
	}

	@Redirect(
			method = "stitch",
			at = @At(value = "NEW", target = "Lnet/minecraft/client/texture/TextureStitcher;<init>")
	)
	private TextureStitcher textureStitcher(int maxWidth, int maxHeight, int mipLevel) {
		// If its not cached then upload the sprite. In a cached state everything gets uploaded at once.
		if (this.data != null) {
			return new FakeTextureStitcher(this.data.width, this.data.height, this.data.maxLevel, this.cachedSprites);
		}
		return new TextureStitcher(maxWidth, maxHeight, mipLevel);
	}

	@Inject(
			method = "stitch",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/TextureStitcher;<init>(III)V", shift = At.Shift.BEFORE),
			locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private void injectSprites(ResourceManager resourceManager, Stream<Identifier> idStream, Profiler profiler, int mipmapLevel, CallbackInfoReturnable<SpriteAtlasTexture.Data> cir,
							   Set<Identifier> set) {
		this.cachedSprites.forEach((identifier, spriteInfoMutablePair) -> {
			set.add(identifier);
		});
	}

	@Inject(
			method = "method_18160*",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void loadSpriteInfo(Identifier identifier, ResourceManager resourceManager, Queue<Sprite.Info> queue, CallbackInfo ci) {
		if (this.data != null) {
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
				DashLoader.LOG.warn("Could not find cached sprite {}. This may cause huge visual issues and/or NPE.", identifier);
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
		this.data = null;
		this.cachedSprites = null;
	}
}
