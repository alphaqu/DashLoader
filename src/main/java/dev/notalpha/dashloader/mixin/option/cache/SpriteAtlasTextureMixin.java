package dev.notalpha.dashloader.mixin.option.cache;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.atlas.AtlasModule;
import net.minecraft.client.texture.*;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.digest.DigestUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Mixin(SpriteAtlasTexture.class)
public abstract class SpriteAtlasTextureMixin extends AbstractTexture {
	@Final
	@Shadow
	private Identifier id;
	@Shadow
	private int mipLevel;
	@Shadow
	private int width;
	@Shadow
	private int height;

	@Inject(method = "upload", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;<init>()V", ordinal = 1, remap = false))
	private void uploadCached(SpriteLoader.StitchResult stitchResult, CallbackInfo ci, @Share("cached") LocalRef<Boolean> cached) {
		cached.set(false);
		AtlasModule.ATLASES.visit(CacheStatus.LOAD, map -> {
			var tasks = map.get(this.id.toUnderscoreSeparatedString());
			if (tasks == null) return;

			try {
				// mipLevel should never be greater than tasks.size()
				if (mipLevel + 1 != tasks.size()) {
					closeAll(tasks);
					return;
				}

				// The texture was allocated for the size vanilla decided on. If that
				// does not match the cached image, uploading it would leave the rest
				// of the texture (and all mip levels) with the contents of the
				// previously loaded pack. Build the atlas vanilla instead.
				var base = tasks.get(0).get();
				if (base == null || base.getWidth() != stitchResult.width() || base.getHeight() != stitchResult.height()) {
					DashLoader.LOG.warn("Cached atlas for {} does not fit the texture ({}x{} cached, {}x{} needed), re-uploading vanilla.",
							this.id, base == null ? 0 : base.getWidth(), base == null ? 0 : base.getHeight(), stitchResult.width(), stitchResult.height());
					closeAll(tasks);
					return;
				}

				cached.set(true);
				bindTexture();

				for (int i = 0; i < tasks.size(); i++) {
					var img = tasks.get(i).get();
					img.upload(i, 0, 0, true);
				}
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private static void closeAll(List<FutureTask<NativeImage>> tasks) {
		for (var task : tasks) {
			try {
				var image = task.get();
				if (image != null) {
					image.close();
				}
			} catch (InterruptedException | ExecutionException ignored) {
			}
		}
	}

	@WrapWithCondition(method = "upload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/Sprite;upload()V"))
	private boolean shouldBuildAtlas(Sprite instance, @Share("cached") LocalRef<Boolean> cached) {
		return !cached.get();
	}

	@Inject(method = "upload", at = @At("TAIL"))
	private void saveAtlas(SpriteLoader.StitchResult stitchResult, CallbackInfo ci, @Share("cached") LocalRef<Boolean> cached) throws IOException {
		if (cached.get()) {
			return;
		}

		var stringId = this.id.toUnderscoreSeparatedString();

		AtlasModule.ATLASES.visit(CacheStatus.SAVE, map -> {
			map.put(stringId, null); // just for the string
		});

		var atlasFolder = AtlasModule.getAtlasFolder();
		Files.createDirectories(atlasFolder);

		GlStateManager._bindTexture(this.getGlId());

		for (int i = 0; i <= this.mipLevel; i++) {
			try (NativeImage nativeImage = new NativeImage(width >> i, height >> i, false)) {
				nativeImage.loadFromTextureImage(i, false);
				var path = atlasFolder.resolve(DigestUtils.md5Hex(stringId + i).toUpperCase());
				nativeImage.writeTo(path);
			}
		}
	}
}
