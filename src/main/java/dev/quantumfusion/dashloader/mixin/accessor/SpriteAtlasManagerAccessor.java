package dev.quantumfusion.dashloader.mixin.accessor;

import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SpriteAtlasManager.class)
public interface SpriteAtlasManagerAccessor {

	@Accessor("atlases")
	Map<Identifier, SpriteAtlasTexture> getAtlases();

}
