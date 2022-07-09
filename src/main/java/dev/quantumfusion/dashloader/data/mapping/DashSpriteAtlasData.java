package dev.quantumfusion.dashloader.data.mapping;

import dev.quantumfusion.dashloader.DashDataManager;
import dev.quantumfusion.dashloader.data.common.ObjectObjectList;
import dev.quantumfusion.dashloader.data.image.DashSpriteAtlasTexture;
import dev.quantumfusion.dashloader.mixin.accessor.SpriteAtlasManagerAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.taski.builtin.StepTask;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class DashSpriteAtlasData {
	public final ObjectObjectList<DashSpriteAtlasTexture, Integer> atlases;

	public DashSpriteAtlasData(ObjectObjectList<DashSpriteAtlasTexture, Integer> atlases) {
		this.atlases = atlases;
	}

	public DashSpriteAtlasData(DashDataManager data, RegistryWriter writer, StepTask parent) {
		this.atlases = new ObjectObjectList<>();
		var atlases = ((SpriteAtlasManagerAccessor) data.spriteAtlasManager.getMinecraftData()).getAtlases();
		var extraAtlases = data.getWriteContextData().extraAtlases;

		parent.run(new StepTask("Atlas", atlases.size() + extraAtlases.size()), task -> {
			atlases.forEach((identifier, spriteAtlasTexture) -> {
				this.addAtlas(writer, spriteAtlasTexture, 0);
				task.next();
			});
			extraAtlases.forEach(spriteAtlasTexture -> {
				this.addAtlas(writer, spriteAtlasTexture, 1);
				task.next();
			});
		});
	}

	private void addAtlas(RegistryWriter writer, SpriteAtlasTexture texture, int i) {
		this.atlases.put(new DashSpriteAtlasTexture(texture, writer), i);
	}


	public Pair<SpriteAtlasManager, List<SpriteAtlasTexture>> export(RegistryReader exportHandler) {
		var out = new ArrayList<SpriteAtlasTexture>(this.atlases.list().size());
		var toRegister = new ArrayList<SpriteAtlasTexture>(this.atlases.list().size());
		this.atlases.forEach((key, value) -> {
			if (value == 0) {
				out.add(key.export(exportHandler));
			}
			toRegister.add(key.export(exportHandler));
		});
		return Pair.of(new SpriteAtlasManager(out), toRegister);
	}
}
