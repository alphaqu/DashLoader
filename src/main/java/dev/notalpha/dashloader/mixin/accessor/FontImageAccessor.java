package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.client.font.UnicodeTextureFont;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(UnicodeTextureFont.FontImage.class)
public interface FontImageAccessor {
	@Accessor
	NativeImage getImage();

	@Invoker("<init>")
	static UnicodeTextureFont.FontImage create(byte[] sizes, NativeImage image) {
		throw new AssertionError();
	}

}
