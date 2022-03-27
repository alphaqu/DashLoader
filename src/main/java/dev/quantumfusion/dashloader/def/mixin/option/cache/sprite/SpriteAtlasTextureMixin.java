package dev.quantumfusion.dashloader.def.mixin.option.cache.sprite;

import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.image.DashSpriteAtlasTexture;
import dev.quantumfusion.dashloader.def.data.image.DashSpriteAtlasTextureData;
import dev.quantumfusion.dashloader.def.fallback.sprite.FakeTextureStitcher;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAccessor;
import dev.quantumfusion.dashloader.def.util.mixins.SpriteAtlasTextureDuck;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(SpriteAtlasTexture.class)
public class SpriteAtlasTextureMixin implements SpriteAtlasTextureDuck {
	private boolean dashLoaded = false;
	private DashSpriteAtlasTextureData data;
	private Map<Identifier, Sprite> cachedSprites;


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
		if (dashLoaded) {
			return new FakeTextureStitcher(data.width(), data.height(), data.mipLevel());
		}
		return new TextureStitcher(maxWidth, maxHeight, mipLevel);
	}


	@Inject(
			method = "loadSprites(Lnet/minecraft/resource/ResourceManager;Ljava/util/Set;)Ljava/util/Collection;",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	// this method gets called when trying to get the sprite infos.
	// Lets hope no mod needs animation data at this exact stage.
	private void loadSprites(ResourceManager resourceManager, Set<Identifier> ids, CallbackInfoReturnable<Collection<Sprite.Info>> cir) {
		if (dashLoaded) {
			List<Sprite.Info> out = new ArrayList<>();
			cachedSprites.forEach((identifier, sprite) -> {
				final SpriteAccessor access = (SpriteAccessor) sprite;
				access.setAtlas((SpriteAtlasTexture) (Object) this);
				access.setId(identifier);
				ids.add(identifier);
				out.add(new Sprite.Info(identifier, sprite.getWidth(), sprite.getHeight(), AnimationResourceMetadata.EMPTY));
			});
			cir.setReturnValue(out);
		}
	}


	@Inject(
			method = "loadSprites(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/texture/TextureStitcher;I)Ljava/util/List;",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	// this method gets called when allocating sprites. Lets just return the existing ones if they are cached.
	private void loadSprites(ResourceManager resourceManager, TextureStitcher textureStitcher, int maxLevel, CallbackInfoReturnable<List<Sprite>> cir) {
		if (dashLoaded) {
			cir.setReturnValue(new ArrayList<>(cachedSprites.values()));
		}
	}

	@Inject(
			method = "clear",
			at = @At(value = "TAIL")
	)
	private void loadSprites(CallbackInfo ci) {
		this.dashLoaded = false;
		this.data = null;
		this.cachedSprites = null;
	}


	@Override
	public void dashLoaded(DashSpriteAtlasTextureData data, Map<Identifier, Sprite> sprites) {
		this.dashLoaded = true;
		this.data = data;
		this.cachedSprites = sprites;
	}
}
