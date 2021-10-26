package net.oskarstrom.dashloader.def.image;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.data.IntIntList;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.mixin.accessor.AbstractTextureAccessor;
import net.oskarstrom.dashloader.def.mixin.accessor.SpriteAccessor;
import net.oskarstrom.dashloader.def.mixin.accessor.SpriteAtlasTextureAccessor;
import net.oskarstrom.dashloader.def.util.UnsafeHelper;

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

	public DashSpriteAtlasTexture(SpriteAtlasTexture spriteAtlasTexture, DashSpriteAtlasTextureData data, DashRegistry registry) {
		this(spriteAtlasTexture, (SpriteAtlasTextureAccessor) spriteAtlasTexture, data, registry);
	}

	private DashSpriteAtlasTexture(SpriteAtlasTexture spriteAtlasTexture, SpriteAtlasTextureAccessor spriteTextureAccess, DashSpriteAtlasTextureData data, DashRegistry registry) {
		this(
				registry.add(spriteAtlasTexture.getId()),
				spriteTextureAccess.getMaxTextureSize(),
				new IntIntList(new ArrayList<>()),
				((AbstractTextureAccessor) spriteAtlasTexture).getBilinear(),
				((AbstractTextureAccessor) spriteAtlasTexture).getMipmap(),
				data);

		spriteTextureAccess.getSprites().forEach((identifier, sprite) -> sprites.put(registry.add(identifier), registry.add(sprite)));
	}

	@Override
	public SpriteAtlasTexture toUndash(DashExportHandler exportHandler) {
		final SpriteAtlasTexture spriteAtlasTexture = UnsafeHelper.allocateInstance(SpriteAtlasTexture.class);
		final AbstractTextureAccessor access = ((AbstractTextureAccessor) spriteAtlasTexture);
		access.setBilinear(bilinear);
		access.setMipmap(mipmap);
		final SpriteAtlasTextureAccessor spriteAtlasTextureAccessor = ((SpriteAtlasTextureAccessor) spriteAtlasTexture);
		final Map<Identifier, Sprite> out = new HashMap<>(sprites.list().size());
		sprites.forEach((key, value) -> out.put(exportHandler.get(key), loadSprite(value, exportHandler, spriteAtlasTexture)));
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
		spriteAtlasTextureAccessor.setId(exportHandler.get(id));
		spriteAtlasTextureAccessor.setMaxTextureSize(maxTextureSize);
		DashLoader.getVanillaData().addAtlasData(spriteAtlasTexture, data);
		return spriteAtlasTexture;
	}

	private Sprite loadSprite(int spritePointer, DashExportHandler exportHandler, SpriteAtlasTexture spriteAtlasTexture) {
		Sprite sprite = exportHandler.get(spritePointer);
		((SpriteAccessor) sprite).setAtlas(spriteAtlasTexture);
		return sprite;
	}
}
