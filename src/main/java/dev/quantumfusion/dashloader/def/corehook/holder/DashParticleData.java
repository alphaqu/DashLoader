package dev.quantumfusion.dashloader.def.corehook.holder;

import dev.quantumfusion.dashloader.core.DashLoaderCore;
import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.common.IntObjectList;
import dev.quantumfusion.dashloader.core.progress.task.CountTask;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.data.image.DashSpriteAtlasTexture;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DashParticleData implements Dashable<Pair<Map<Identifier, List<Sprite>>, SpriteAtlasTexture>> {
	public final IntObjectList<List<Integer>> particles;
	public final DashSpriteAtlasTexture atlasTexture;

	public DashParticleData(IntObjectList<List<Integer>> particles,
			DashSpriteAtlasTexture atlasTexture) {
		this.particles = particles;
		this.atlasTexture = atlasTexture;
	}

	public DashParticleData(DashDataManager data, RegistryWriter writer) {
		this.particles = new IntObjectList<>();
		final Map<Identifier, List<Sprite>> particles = data.particleSprites.getMinecraftData();

		CountTask task = new CountTask(particles.size());
		DashLoaderCore.PROGRESS.getCurrentContext().setSubtask(task);
		particles.forEach((identifier, spriteList) -> {
			List<Integer> out = new ArrayList<>();
			spriteList.forEach(sprite -> out.add(writer.add(sprite)));
			this.particles.put(writer.add(identifier), out);
			task.completedTask();
		});
		final SpriteAtlasTexture particleAtlas = data.particleAtlas.getMinecraftData();
		atlasTexture = new DashSpriteAtlasTexture(particleAtlas, data.getWriteContextData().atlasData.get(particleAtlas), writer);
	}


	public Pair<Map<Identifier, List<Sprite>>, SpriteAtlasTexture> export(RegistryReader reader) {
		Map<Identifier, List<Sprite>> out = new HashMap<>();
		particles.forEach((key, value) -> {
			List<Sprite> outInner = new ArrayList<>();
			value.forEach(pntr -> outInner.add(reader.get(pntr)));
			out.put(reader.get(key), outInner);
		});
		return Pair.of(out, atlasTexture.export(reader));
	}

}
