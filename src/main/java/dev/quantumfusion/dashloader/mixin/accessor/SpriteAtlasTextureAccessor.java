package dev.quantumfusion.dashloader.mixin.accessor;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SpriteAtlasTexture.class)
public interface SpriteAtlasTextureAccessor {

	@Accessor
	Map<Identifier, Sprite> getSprites();

	@Accessor
	Identifier getId();
}
