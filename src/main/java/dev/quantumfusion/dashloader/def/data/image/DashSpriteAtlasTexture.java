package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.common.IntIntList;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.mixin.accessor.AbstractTextureAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAtlasTextureAccessor;
import dev.quantumfusion.dashloader.def.util.UnsafeHelper;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.util.Identifier;

import java.util.*;

@Data
public record DashSpriteAtlasTexture(
		int id,
		int maxTextureSize,
		IntIntList sprites,
		boolean bilinear,
		boolean mipmap,
		DashSpriteAtlasTextureData data
) implements Dashable<SpriteAtlasTexture> {

	public DashSpriteAtlasTexture(SpriteAtlasTexture spriteAtlasTexture, DashSpriteAtlasTextureData data, DashRegistryWriter writer) {
		this(spriteAtlasTexture, (SpriteAtlasTextureAccessor) spriteAtlasTexture, data, writer);
	}

	private DashSpriteAtlasTexture(SpriteAtlasTexture spriteAtlasTexture, SpriteAtlasTextureAccessor spriteTextureAccess, DashSpriteAtlasTextureData data, DashRegistryWriter writer) {
		this(
				writer.add(spriteAtlasTexture.getId()),
				spriteTextureAccess.getMaxTextureSize(),
				new IntIntList(new ArrayList<>()),
				((AbstractTextureAccessor) spriteAtlasTexture).getBilinear(),
				((AbstractTextureAccessor) spriteAtlasTexture).getMipmap(),
				data);

		spriteTextureAccess.getSprites().forEach((identifier, sprite) -> sprites.put(writer.add(identifier), writer.add(sprite)));
	}

	@Override
	public SpriteAtlasTexture export(DashRegistryReader reader) {
		final SpriteAtlasTexture spriteAtlasTexture = UnsafeHelper.allocateInstance(SpriteAtlasTexture.class);
		final AbstractTextureAccessor access = ((AbstractTextureAccessor) spriteAtlasTexture);
		access.setBilinear(bilinear);
		access.setMipmap(mipmap);
		final SpriteAtlasTextureAccessor spriteAtlasTextureAccessor = ((SpriteAtlasTextureAccessor) spriteAtlasTexture);
		final Map<Identifier, Sprite> out = new HashMap<>(sprites.list().size());
		sprites.forEach((key, value) -> out.put(reader.get(key), loadSprite(value, reader, spriteAtlasTexture)));
		final List<TextureTickListener> outAnimatedSprites = new ArrayList<>();
		out.values().forEach(sprite -> {
			final TextureTickListener animation = sprite.getAnimation();
			if (animation != null) {
				outAnimatedSprites.add(animation);
			}
		});
		spriteAtlasTextureAccessor.setAnimatedSprites(outAnimatedSprites);
		spriteAtlasTextureAccessor.setSpritesToLoad(new HashSet<>());
		spriteAtlasTextureAccessor.setSprites(out);
		spriteAtlasTextureAccessor.setId(reader.get(id));
		spriteAtlasTextureAccessor.setMaxTextureSize(maxTextureSize);
		DashLoader.getData().getReadContextData().atlasData.put(spriteAtlasTexture, data);
		return spriteAtlasTexture;
	}

	private Sprite loadSprite(int spritePointer, DashRegistryReader exportHandler, SpriteAtlasTexture spriteAtlasTexture) {
		Sprite sprite = exportHandler.get(spritePointer);
		((SpriteAccessor) sprite).setAtlas(spriteAtlasTexture);
		return sprite;
	}
}
