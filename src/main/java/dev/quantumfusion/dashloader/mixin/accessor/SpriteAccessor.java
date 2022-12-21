package dev.quantumfusion.dashloader.mixin.accessor;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Sprite.class)
public interface SpriteAccessor {
	@Accessor
	@Mutable
	void setAtlasId(Identifier atlasId);

	@Accessor
	@Mutable
	void setContents(SpriteContents contents);

	@Accessor("x")
	@Mutable
	void setX(int x);


	@Accessor("y")
	@Mutable
	void setY(int y);


	@Accessor
	@Mutable
	void setUMin(float uMin);

	@Accessor
	@Mutable
	void setUMax(float uMax);


	@Accessor
	@Mutable
	void setVMin(float vMin);


	@Accessor
	@Mutable
	void setVMax(float vMax);

}
