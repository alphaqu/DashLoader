package dev.quantumfusion.dashloader.mixin.accessor;

import java.util.Map;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FontManager.class)
public interface FontManagerAccessor {

	@Accessor
	TextureManager getTextureManager();

	@Accessor
	Map<Identifier, FontStorage> getFontStorages();

}
