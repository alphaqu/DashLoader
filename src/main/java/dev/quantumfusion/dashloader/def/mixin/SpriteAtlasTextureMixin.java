package dev.quantumfusion.dashloader.def.mixin;

import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.image.DashSpriteAtlasTextureData;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteAtlasTexture.class)
public class SpriteAtlasTextureMixin {

	@Inject(
			method = "upload(Lnet/minecraft/client/texture/SpriteAtlasTexture$Data;)V",
			at = @At(value = "HEAD")
	)
	private void saveAtlasInfo(SpriteAtlasTexture.Data data, CallbackInfo ci) {
		if (DashLoader.isWrite()) {
			DashLoader.getData().getWriteContextData().atlasData.put((SpriteAtlasTexture) (Object) this, new DashSpriteAtlasTextureData(data));
		}
	}
}
