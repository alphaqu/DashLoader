package dev.notalpha.dashloader.minecraft.sprite;

import dev.notalpha.dashloader.api.Dashable;
import dev.notalpha.dashloader.io.data.collection.IntIntList;
import dev.notalpha.dashloader.mixin.accessor.AbstractTextureAccessor;
import dev.notalpha.dashloader.mixin.accessor.SpriteAtlasTextureAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class DashSpriteAtlasTexture implements Dashable<SpriteAtlasTexture> {
	public final int id;
	public final IntIntList sprites;
	public final boolean bilinear;
	public final boolean mipmap;

	public DashSpriteAtlasTexture(
			int id,
			IntIntList sprites,
			boolean bilinear,
			boolean mipmap
	) {
		this.id = id;
		this.sprites = sprites;
		this.bilinear = bilinear;
		this.mipmap = mipmap;
	}

	public DashSpriteAtlasTexture(SpriteAtlasTexture spriteAtlasTexture, RegistryWriter writer) {
		AbstractTextureAccessor access = (AbstractTextureAccessor) spriteAtlasTexture;

		this.id = writer.add(spriteAtlasTexture.getId());
		this.sprites = new IntIntList(new ArrayList<>());
		((SpriteAtlasTextureAccessor) spriteAtlasTexture).getSprites().forEach((identifier, sprite) -> this.sprites.put(writer.add(identifier), writer.add(sprite)));

		this.bilinear = access.getBilinear();
		this.mipmap = access.getMipmap();

	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public SpriteAtlasTexture export(RegistryReader reader) {
		final SpriteAtlasTexture spriteAtlasTexture = new SpriteAtlasTexture(reader.get(this.id));
		final AbstractTextureAccessor access = ((AbstractTextureAccessor) spriteAtlasTexture);
		access.setBilinear(this.bilinear);
		access.setMipmap(this.mipmap);
		final Map<Identifier, Sprite> out = new HashMap<>(this.sprites.list().size());
		this.sprites.forEach((key, value) -> out.put(reader.get(key), this.loadSprite(value, reader, spriteAtlasTexture)));
		// Notify about its cached state.
		return spriteAtlasTexture;
	}

	private Sprite loadSprite(int spritePointer, RegistryReader exportHandler, SpriteAtlasTexture spriteAtlasTexture) {
		Sprite sprite = exportHandler.get(spritePointer);
		return sprite;
	}
}
