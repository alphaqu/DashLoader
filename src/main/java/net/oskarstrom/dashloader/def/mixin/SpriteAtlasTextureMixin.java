package net.oskarstrom.dashloader.def.mixin;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.image.DashSpriteAtlasTextureData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteAtlasTexture.class)
public class SpriteAtlasTextureMixin {

	@Inject(method = "upload(Lnet/minecraft/client/texture/SpriteAtlasTexture$Data;)V",
			at = @At(value = "HEAD"))
	private void saveAtlasInfo(SpriteAtlasTexture.Data data, CallbackInfo ci) {
		DashLoader.getVanillaData().addAtlasData((SpriteAtlasTexture) (Object) this, new DashSpriteAtlasTextureData(data));
	}
}
