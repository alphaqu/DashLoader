package dev.quantumfusion.dashloader.def.data.dataobject.mapping;

import dev.quantumfusion.dashloader.core.common.ObjectObjectList;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.VanillaData;
import dev.quantumfusion.dashloader.def.data.image.DashSpriteAtlasTexture;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAtlasManagerAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

@Data
public class DashSpriteAtlasData {
	public final ObjectObjectList<DashSpriteAtlasTexture, Integer> atlases;

	public DashSpriteAtlasData(ObjectObjectList<DashSpriteAtlasTexture, Integer> atlases) {
		this.atlases = atlases;
	}

	public DashSpriteAtlasData(VanillaData data, DashRegistryWriter writer, DashLoader.TaskHandler taskHandler) {
		atlases = new ObjectObjectList<>();
		var atlases = ((SpriteAtlasManagerAccessor) data.getAtlasManager()).getAtlases();
		var extraAtlases = data.getExtraAtlases();
		taskHandler.setSubtasks(atlases.size() + extraAtlases.size());
		atlases.forEach((identifier, spriteAtlasTexture) -> addAtlas(data, writer, taskHandler, spriteAtlasTexture, 0));
		extraAtlases.forEach(spriteAtlasTexture -> addAtlas(data, writer, taskHandler, spriteAtlasTexture, 1));
	}

	private void addAtlas(VanillaData data, DashRegistryWriter writer, DashLoader.TaskHandler tasks, SpriteAtlasTexture texture, int i) {
		this.atlases.put(new DashSpriteAtlasTexture(texture, data.getAtlasData(texture), writer), i);
		tasks.completedSubTask();
	}


	public Pair<SpriteAtlasManager, List<SpriteAtlasTexture>> export(DashRegistryReader exportHandler) {
		var out = new ArrayList<SpriteAtlasTexture>(atlases.list().size());
		var toRegister = new ArrayList<SpriteAtlasTexture>(atlases.list().size());
		atlases.forEach((key, value) -> {
			if (value == 0) out.add(key.export(exportHandler));
			toRegister.add(key.export(exportHandler));
		});
		return Pair.of(new SpriteAtlasManager(out), toRegister);
	}
}
