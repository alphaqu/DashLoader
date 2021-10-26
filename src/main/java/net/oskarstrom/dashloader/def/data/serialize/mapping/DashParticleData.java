package net.oskarstrom.dashloader.def.data.serialize.mapping;

import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.image.DashSpriteAtlasTexture;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.data.Pointer2ObjectMap;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.registry.Pointer;
import net.oskarstrom.dashloader.def.data.VanillaData;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashParticleData implements Dashable<Pair<Map<Identifier, List<Sprite>>, SpriteAtlasTexture>> {

	@Serialize(order = 0)
	public final Pointer2ObjectMap<List<Integer>> particles;

	@Serialize(order = 1)
	public final DashSpriteAtlasTexture atlasTexture;

	public DashParticleData(@Deserialize("particles") Pointer2ObjectMap<List<Integer>> particles,
							@Deserialize("atlasTexture") DashSpriteAtlasTexture atlasTexture) {
		this.particles = particles;
		this.atlasTexture = atlasTexture;
	}

	public DashParticleData(VanillaData data, DashRegistry registry, DashLoader.TaskHandler taskHandler) {
		this.particles = new Pointer2ObjectMap<>();
		final Map<Identifier, List<Sprite>> particles = data.getParticles();
		taskHandler.setSubtasks(particles.size() + 1);
		particles.forEach((identifier, spriteList) -> {
			List<Integer> out = new ArrayList<>();
			spriteList.forEach(sprite -> out.add(registry.add(sprite)));
			this.particles.add(Pointer2ObjectMap.Entry.of(registry.add(identifier), out));
			taskHandler.completedSubTask();
		});
		final SpriteAtlasTexture particleAtlas = data.getParticleAtlas();
		atlasTexture = new DashSpriteAtlasTexture(particleAtlas, data.getAtlasData(particleAtlas), registry);
		taskHandler.completedSubTask();

	}


	public Pair<Map<Identifier, List<Sprite>>, SpriteAtlasTexture> toUndash(DashExportHandler exportHandler) {
		Map<Identifier, List<Sprite>> out = new HashMap<>();
		particles.forEach((entry) -> {
			List<Sprite> outInner = new ArrayList<>();
			entry.value.forEach(pntr -> outInner.add(exportHandler.get(pntr)));
			out.put(exportHandler.get(entry.key), outInner);
		});
		return Pair.of(out, atlasTexture.toUndash(exportHandler));
	}

}
