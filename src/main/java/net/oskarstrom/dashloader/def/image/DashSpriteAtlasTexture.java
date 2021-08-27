package net.oskarstrom.dashloader.def.image;

import net.oskarstrom.dashloader.def.mixin.accessor.AbstractTextureAccessor;
import net.oskarstrom.dashloader.def.mixin.accessor.SpriteAccessor;
import net.oskarstrom.dashloader.def.mixin.accessor.SpriteAtlasTextureAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.api.data.Pointer2PointerMap;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.registry.Pointer;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.util.UnsafeHelper;

import java.util.*;

public class DashSpriteAtlasTexture {
	@Serialize(order = 0)
	public final int id;
	@Serialize(order = 1)
	public final int maxTextureSize;
	@Serialize(order = 2)
	public Pointer2PointerMap sprites;
	@Serialize(order = 3)
	public boolean bilinear;
	@Serialize(order = 4)
	public boolean mipmap;
	@Serialize(order = 5)
	public DashSpriteAtlasTextureData data;


	public DashSpriteAtlasTexture(@Deserialize("id") int id,
								  @Deserialize("maxTextureSize") int maxTextureSize,
								  @Deserialize("sprites") Pointer2PointerMap sprites,
								  @Deserialize("bilinear") boolean bilinear,
								  @Deserialize("mipmap") boolean mipmap,
								  @Deserialize("data") DashSpriteAtlasTextureData data) {
		this.id = id;
		this.maxTextureSize = maxTextureSize;
		this.sprites = sprites;
		this.bilinear = bilinear;
		this.mipmap = mipmap;
		this.data = data;
	}

	public DashSpriteAtlasTexture(SpriteAtlasTexture spriteAtlasTexture, DashSpriteAtlasTextureData data, DashRegistry registry) {
		SpriteAtlasTextureAccessor spriteTextureAccess = ((SpriteAtlasTextureAccessor) spriteAtlasTexture);
		this.data = data;
		sprites = new Pointer2PointerMap();
		spriteTextureAccess.getSprites().forEach((identifier, sprite) -> sprites.add(Pointer2PointerMap.Entry.of(registry.add(identifier), registry.add(sprite))));
		id = registry.add(spriteAtlasTexture.getId());
		maxTextureSize = spriteTextureAccess.getMaxTextureSize();
		bilinear = ((AbstractTextureAccessor) spriteAtlasTexture).getBilinear();
		mipmap = ((AbstractTextureAccessor) spriteAtlasTexture).getMipmap();
	}

	public SpriteAtlasTexture toUndash(DashRegistry registry) {
		final SpriteAtlasTexture spriteAtlasTexture = UnsafeHelper.allocateInstance(SpriteAtlasTexture.class);
		final AbstractTextureAccessor access = ((AbstractTextureAccessor) spriteAtlasTexture);
		access.setBilinear(bilinear);
		access.setMipmap(mipmap);
		final SpriteAtlasTextureAccessor spriteAtlasTextureAccessor = ((SpriteAtlasTextureAccessor) spriteAtlasTexture);
		final Map<Identifier, Sprite> out = new HashMap<>(sprites.size());
		sprites.forEach((entry) -> out.put(registry.get(entry.key), loadSprite(entry.value, registry, spriteAtlasTexture)));
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
		spriteAtlasTextureAccessor.setId(registry.get(id));
		spriteAtlasTextureAccessor.setMaxTextureSize(maxTextureSize);
		DashLoader.getVanillaData().addAtlasData(spriteAtlasTexture, data);
		return spriteAtlasTexture;
	}

	private Sprite loadSprite(int spritePointer, DashRegistry registry, SpriteAtlasTexture spriteAtlasTexture) {
		Sprite sprite = registry.get(spritePointer);
		((SpriteAccessor) sprite).setAtlas(spriteAtlasTexture);
		return sprite;
	}


}
