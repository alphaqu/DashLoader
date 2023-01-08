package dev.notalpha.dashloader.client.sprite;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.io.data.collection.IntIntList;
import dev.notalpha.dashloader.mixin.accessor.SpriteAtlasTextureDataAccessor;
import dev.notalpha.dashloader.registry.RegistryFactory;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class DashAtlasData implements DashObject<AtlasData> {
	public final int width;
	public final int height;
	public final int maxLevel;
	public final int id;
	public final IntIntList sprites;

	public DashAtlasData(int width, int height, int maxLevel, int id, IntIntList sprites) {
		this.width = width;
		this.height = height;
		this.maxLevel = maxLevel;
		this.id = id;
		this.sprites = sprites;
	}

	public DashAtlasData(AtlasData data, RegistryWriter writer) {
		this.width = data.width;
		this.height = data.height;
		this.maxLevel = data.maxLevel;
		this.id = writer.add(data.id);

		this.sprites = new IntIntList();
		data.sprites.forEach((identifier, sprite) -> {
			this.sprites.put(writer.add(identifier), writer.add(sprite));
		});
	}

	@Override
	public AtlasData export(RegistryReader reader) {
		Map<Identifier, Sprite> sprites = new HashMap<>();
		this.sprites.forEach((identifier, sprite) -> {
			sprites.put(reader.get(identifier), reader.get(sprite));
		});
		return new AtlasData(this.width, this.height, this.maxLevel, reader.get(this.id), sprites);
	}
}
