package dev.notalpha.dashloader.client.sprite;

import dev.notalpha.dashloader.Cache;
import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.misc.UnsafeHelper;
import dev.notalpha.dashloader.mixin.accessor.SpriteAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.util.Identifier;

public class DashSprite implements DashObject<Sprite> {
	public final int atlasId;
	public final DashSpriteContents contents;

	public final int x;
	public final int y;

	public final int atlasWidth;
	public final int atlasHeight;

	public DashSprite(int atlasId, DashSpriteContents contents, int x, int y, int atlasWidth, int atlasHeight) {
		this.atlasId = atlasId;
		this.contents = contents;
		this.x = x;
		this.y = y;
		this.atlasWidth = atlasWidth;
		this.atlasHeight = atlasHeight;
	}

	public DashSprite(Sprite sprite, RegistryWriter writer) {
		this.atlasId = writer.add(sprite.getAtlasId());
		this.contents = new DashSpriteContents(sprite.getContents(), writer);

		// i dont wanna deal with atlases thank you.
		this.x = sprite.getX();
		this.y = sprite.getY();
		this.atlasWidth = Math.round(this.x / sprite.getMinU());
		this.atlasHeight = Math.round(this.y / sprite.getMinV());
	}

	@Override
	public Sprite export(final RegistryReader registry) {
		return SpriteAccessor.init(registry.get(this.atlasId), this.contents.export(registry), this.atlasWidth, this.atlasHeight, this.x, this.y);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashSprite that = (DashSprite) o;

		if (atlasId != that.atlasId) return false;
		if (x != that.x) return false;
		if (y != that.y) return false;
		if (atlasWidth != that.atlasWidth) return false;
		if (atlasHeight != that.atlasHeight) return false;
		return contents.equals(that.contents);
	}

	@Override
	public int hashCode() {
		int result = atlasId;
		result = 31 * result + contents.hashCode();
		result = 31 * result + x;
		result = 31 * result + y;
		result = 31 * result + atlasWidth;
		result = 31 * result + atlasHeight;
		return result;
	}
}
