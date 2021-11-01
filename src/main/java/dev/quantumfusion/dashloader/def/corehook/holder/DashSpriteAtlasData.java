package dev.quantumfusion.dashloader.def.corehook.holder;

import dev.quantumfusion.dashloader.core.common.ObjectObjectList;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.DashDataManager;
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

	public DashSpriteAtlasData(DashDataManager data, DashRegistryWriter writer) {
		atlases = new ObjectObjectList<>();
		var atlases = ((SpriteAtlasManagerAccessor) data.spriteAtlasManager.getMinecraftData()).getAtlases();
		var extraAtlases = data.getWriteContextData().extraAtlases;
		atlases.forEach((identifier, spriteAtlasTexture) -> addAtlas(data, writer, spriteAtlasTexture, 0));
		extraAtlases.forEach(spriteAtlasTexture -> addAtlas(data, writer, spriteAtlasTexture, 1));
	}

	private void addAtlas(DashDataManager data, DashRegistryWriter writer, SpriteAtlasTexture texture, int i) {
		this.atlases.put(new DashSpriteAtlasTexture(texture, data.getWriteContextData().atlasData.get(texture), writer), i);
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
