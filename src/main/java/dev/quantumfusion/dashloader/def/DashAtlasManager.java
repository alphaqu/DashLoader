package dev.quantumfusion.dashloader.def;

import dev.quantumfusion.dashloader.def.api.option.Option;
import dev.quantumfusion.dashloader.def.data.image.DashSpriteAtlasTextureData;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DashAtlasManager {
	private final DashDataManager.DashReadContextData readContextData;
	private final List<Pair<Option, Pair<SpriteAtlasTexture, DashSpriteAtlasTextureData>>> atlasesToRegister;

	public DashAtlasManager(DashDataManager.DashReadContextData readContextData) {
		this.readContextData = readContextData;
		this.atlasesToRegister = new ArrayList<>();
	}

	public void addAtlas(Option feature, SpriteAtlasTexture atlas) {
		var atlasData = Pair.of(atlas, readContextData.atlasData.get(atlas));
		atlasesToRegister.add(Pair.of(feature, atlasData));
	}

	public void consumeAtlases(Option feature, Consumer<Pair<SpriteAtlasTexture, DashSpriteAtlasTextureData>> consumer) {
		atlasesToRegister.forEach((pair) -> {
			if (pair.getLeft() == feature) {
				consumer.accept(pair.getRight());
			}
		});
	}

	@Nullable
	public SpriteAtlasTexture getAtlas(Identifier identifier) {
		for (Pair<Option, Pair<SpriteAtlasTexture, DashSpriteAtlasTextureData>> pair : atlasesToRegister) {
			final SpriteAtlasTexture atlas = pair.getRight().getLeft();
			if (identifier.equals(atlas.getId())) {
				return atlas;
			}
		}
		return null;
	}
}
