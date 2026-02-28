package dev.notalpha.dashloader.client;

import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import org.jetbrains.annotations.Nullable;

// its lazy, but dash! Used for resolution of sprites.
public abstract class Dazy<V> {
	@Nullable
	private transient V loaded;

	protected abstract V resolve(Function<Material, TextureAtlasSprite> spriteLoader);

	public V get(Function<Material, TextureAtlasSprite> spriteLoader) {
		if (loaded != null) {
			return loaded;
		}

		loaded = resolve(spriteLoader);
		return loaded;
	}
}
