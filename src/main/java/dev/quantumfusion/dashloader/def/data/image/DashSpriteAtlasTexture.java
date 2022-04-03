package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.common.IntIntList;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.mixin.accessor.AbstractTextureAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAtlasTextureAccessor;
import dev.quantumfusion.dashloader.def.util.mixins.SpriteAtlasTextureDuck;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
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

	public DashSpriteAtlasTexture(SpriteAtlasTexture spriteAtlasTexture, DashSpriteAtlasTextureData data, RegistryWriter writer) {
		AbstractTextureAccessor access = (AbstractTextureAccessor) spriteAtlasTexture;

		this.id = writer.add(spriteAtlasTexture.getId());
		this.sprites = new IntIntList(new ArrayList<>());
		((SpriteAtlasTextureAccessor) spriteAtlasTexture).getSprites().forEach((identifier, sprite) -> sprites.put(writer.add(identifier), writer.add(sprite)));

		this.bilinear = access.getBilinear();
		this.mipmap = access.getMipmap();
		this.data = data;

	}

	@Override
	public SpriteAtlasTexture export(RegistryReader reader) {
		final SpriteAtlasTexture spriteAtlasTexture = new SpriteAtlasTexture(reader.get(id));
		final AbstractTextureAccessor access = ((AbstractTextureAccessor) spriteAtlasTexture);
		access.setBilinear(bilinear);
		access.setMipmap(mipmap);
		final Map<Identifier, Sprite> out = new HashMap<>(sprites.list().size());
		sprites.forEach((key, value) -> out.put(reader.get(key), loadSprite(value, reader, spriteAtlasTexture)));
		// Notify about its cached state.
		((SpriteAtlasTextureDuck) spriteAtlasTexture).dashLoaded(data, out);
		DashLoader.getData().getReadContextData().atlasData.put(spriteAtlasTexture, data);
		return spriteAtlasTexture;
	}

	private Sprite loadSprite(int spritePointer, RegistryReader exportHandler, SpriteAtlasTexture spriteAtlasTexture) {
		Sprite sprite = exportHandler.get(spritePointer);
		((SpriteAccessor) sprite).setAtlas(spriteAtlasTexture);
		return sprite;
	}
}
