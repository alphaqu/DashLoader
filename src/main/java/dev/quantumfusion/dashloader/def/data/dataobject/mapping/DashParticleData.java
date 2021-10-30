package dev.quantumfusion.dashloader.def.data.dataobject.mapping;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.common.IntObjectList;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.VanillaData;
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

	public DashParticleData(VanillaData data, DashRegistryWriter writer, DashLoader.TaskHandler taskHandler) {
		this.particles = new IntObjectList<>();
		final Map<Identifier, List<Sprite>> particles = data.getParticles();
		taskHandler.setSubtasks(particles.size() + 1);
		particles.forEach((identifier, spriteList) -> {
			List<Integer> out = new ArrayList<>();
			spriteList.forEach(sprite -> out.add(writer.add(sprite)));
			this.particles.put(writer.add(identifier), out);
			taskHandler.completedSubTask();
		});
		final SpriteAtlasTexture particleAtlas = data.getParticleAtlas();
		atlasTexture = new DashSpriteAtlasTexture(particleAtlas, data.getAtlasData(particleAtlas), writer);
		taskHandler.completedSubTask();

	}


	public Pair<Map<Identifier, List<Sprite>>, SpriteAtlasTexture> export(DashRegistryReader reader) {
		Map<Identifier, List<Sprite>> out = new HashMap<>();
		particles.forEach((key, value) -> {
			List<Sprite> outInner = new ArrayList<>();
			value.forEach(pntr -> outInner.add(reader.get(pntr)));
			out.put(reader.get(key), outInner);
		});
		return Pair.of(out, atlasTexture.export(reader));
	}

}
