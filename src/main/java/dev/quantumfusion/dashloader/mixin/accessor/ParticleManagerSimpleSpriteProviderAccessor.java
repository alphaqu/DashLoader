package dev.quantumfusion.dashloader.mixin.accessor;

import java.util.List;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ParticleManager.SimpleSpriteProvider.class)
public interface ParticleManagerSimpleSpriteProviderAccessor {

	@Accessor
	List<Sprite> getSprites();

	@Accessor
	void setSprites(List<Sprite> sprites);
}
