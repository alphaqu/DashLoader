package net.oskarstrom.dashloader.def.data.serialize.mapping;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.api.data.PairMap;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.data.VanillaData;
import net.oskarstrom.dashloader.def.image.DashSpriteAtlasTexture;
import net.oskarstrom.dashloader.def.mixin.accessor.SpriteAtlasManagerAccessor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashSpriteAtlasData {
	@Serialize(order = 0)
	public final PairMap<DashSpriteAtlasTexture,Integer> atlases;

	public DashSpriteAtlasData(@Deserialize("atlases") PairMap<DashSpriteAtlasTexture,Integer> atlases) {
		this.atlases = atlases;
	}

	public DashSpriteAtlasData(VanillaData data, DashRegistry registry, DashLoader.TaskHandler taskHandler) {
		atlases = new PairMap<>();
		final Map<Identifier, SpriteAtlasTexture> atlases = ((SpriteAtlasManagerAccessor) data.getAtlasManager()).getAtlases();
		final List<SpriteAtlasTexture> extraAtlases = data.getExtraAtlases();
		taskHandler.setSubtasks(atlases.size() + extraAtlases.size());
		atlases.forEach((identifier, spriteAtlasTexture) -> {
			addAtlas(data, registry, taskHandler, spriteAtlasTexture, 0);
		});
		extraAtlases.forEach(spriteAtlasTexture -> {
			addAtlas(data, registry, taskHandler, spriteAtlasTexture, 1);
		});
	}

	private void addAtlas(VanillaData data, DashRegistry registry, DashLoader.TaskHandler taskHandler, SpriteAtlasTexture spriteAtlasTexture, int i) {
		this.atlases.add(PairMap.Entry.of(new DashSpriteAtlasTexture(spriteAtlasTexture, data.getAtlasData(spriteAtlasTexture), registry), i));
		taskHandler.completedSubTask();
	}


	public Pair<SpriteAtlasManager, List<SpriteAtlasTexture>> toUndash(DashRegistry loader) {
		ArrayList<SpriteAtlasTexture> out = new ArrayList<>(atlases.size());
		ArrayList<SpriteAtlasTexture> toRegister = new ArrayList<>(atlases.size());
		atlases.forEach((entry) -> {
			final DashSpriteAtlasTexture key = entry.key;
			if (entry.value == 0) {
				out.add(key.toUndash(loader));
			}
			toRegister.add(key.toUndash(loader));
		});
		return Pair.of(new SpriteAtlasManager(out), toRegister);
	}
}
