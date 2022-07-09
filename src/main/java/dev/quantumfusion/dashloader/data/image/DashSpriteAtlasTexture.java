package dev.quantumfusion.dashloader.data.image;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.data.common.IntIntList;
import dev.quantumfusion.dashloader.mixin.accessor.AbstractTextureAccessor;
import dev.quantumfusion.dashloader.mixin.accessor.SpriteAccessor;
import dev.quantumfusion.dashloader.mixin.accessor.SpriteAtlasTextureAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.util.mixins.SpriteAtlasTextureDuck;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import static dev.quantumfusion.dashloader.DashLoader.DL;

public final class DashSpriteAtlasTexture implements Dashable<SpriteAtlasTexture> {
	public final int id;
	public final IntIntList sprites;
	public final boolean bilinear;
	public final boolean mipmap;
	public final DashSpriteAtlasTextureData data;

	public DashSpriteAtlasTexture(
			int id,
			IntIntList sprites,
			boolean bilinear,
			boolean mipmap,
			DashSpriteAtlasTextureData data
	) {
		this.id = id;
		this.sprites = sprites;
		this.bilinear = bilinear;
		this.mipmap = mipmap;
		this.data = data;
	}

	public DashSpriteAtlasTexture(SpriteAtlasTexture spriteAtlasTexture, RegistryWriter writer) {
		AbstractTextureAccessor access = (AbstractTextureAccessor) spriteAtlasTexture;

		this.id = writer.add(spriteAtlasTexture.getId());
		this.sprites = new IntIntList(new ArrayList<>());
		((SpriteAtlasTextureAccessor) spriteAtlasTexture).getSprites().forEach((identifier, sprite) -> this.sprites.put(writer.add(identifier), writer.add(sprite)));

		this.bilinear = access.getBilinear();
		this.mipmap = access.getMipmap();
		this.data = DL.getData().getWriteContextData().atlasData.get(spriteAtlasTexture);

	}

	@Override
	public SpriteAtlasTexture export(RegistryReader reader) {
		final SpriteAtlasTexture spriteAtlasTexture = new SpriteAtlasTexture(reader.get(this.id));
		final AbstractTextureAccessor access = ((AbstractTextureAccessor) spriteAtlasTexture);
		access.setBilinear(this.bilinear);
		access.setMipmap(this.mipmap);
		final Map<Identifier, Sprite> out = new HashMap<>(this.sprites.list().size());
		this.sprites.forEach((key, value) -> out.put(reader.get(key), this.loadSprite(value, reader, spriteAtlasTexture)));
		// Notify about its cached state.
		((SpriteAtlasTextureDuck) spriteAtlasTexture).dashLoaded(this.data, out);
		DL.getData().getReadContextData().atlasData.put(spriteAtlasTexture, this.data);
		return spriteAtlasTexture;
	}

	private Sprite loadSprite(int spritePointer, RegistryReader exportHandler, SpriteAtlasTexture spriteAtlasTexture) {
		Sprite sprite = exportHandler.get(spritePointer);
		((SpriteAccessor) sprite).setAtlas(spriteAtlasTexture);
		return sprite;
	}
}
