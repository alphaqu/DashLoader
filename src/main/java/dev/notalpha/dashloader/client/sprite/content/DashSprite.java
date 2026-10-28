package dev.notalpha.dashloader.client.sprite.content;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.client.Dazy;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;

public class DashSprite implements DashObject<TextureAtlasSprite, DashSprite.DazyImpl> {
	public final int id;

	public DashSprite(int id) {
		this.id = id;
	}

	public DashSprite(TextureAtlasSprite sprite, RegistryWriter writer) {
		this.id = writer.add(new SpriteId(sprite.atlasLocation(), sprite.contents().name()));
	}

	@Override
	public DazyImpl export(final RegistryReader registry) {
		return new DazyImpl(registry.get(id));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashSprite that = (DashSprite) o;

		return id == that.id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	public static class DazyImpl extends Dazy<TextureAtlasSprite> {
		public final SpriteId location;

		public DazyImpl(SpriteId location) {
			this.location = location;
		}

		@Override
		protected TextureAtlasSprite resolve(SpriteGetter spriteLoader) {
			return spriteLoader.get(location);
		}
	}
}
