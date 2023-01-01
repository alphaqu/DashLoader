package dev.notalpha.dashloader.minecraft.sprite;

import dev.notalpha.dashloader.io.data.collection.IntIntList;
import dev.notalpha.dashloader.registry.RegistryFactory;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.quantumfusion.taski.builtin.StepTask;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.concurrent.CompletableFuture;


public final class DashStitchResult {
	public final int width;
	public final int height;
	public final int mipLevel;
	public final int missing;
	public final IntIntList regions;

	public DashStitchResult(int width, int height, int mipLevel, int missing, IntIntList regions) {
		this.width = width;
		this.height = height;
		this.mipLevel = mipLevel;
		this.missing = missing;
		this.regions = regions;
	}

	public DashStitchResult(SpriteLoader.StitchResult stitchResult, RegistryFactory writer, StepTask task) {
		this.width = stitchResult.width();
		this.height = stitchResult.height();
		this.mipLevel = stitchResult.mipLevel();
		this.missing = writer.add(stitchResult.missing());

		this.regions = new IntIntList();
		stitchResult.regions().forEach((identifier, sprite) -> {
			this.regions.put(writer.add(identifier), writer.add(sprite));
			task.next();
		});
	}

	public SpriteLoader.StitchResult export(RegistryReader reader) {
		Map<Identifier, Sprite> regions = new Object2ObjectOpenHashMap<>();
		this.regions.forEach((key, value) -> {
			regions.put(reader.get(key), reader.get(value));
		});

		return new SpriteLoader.StitchResult(
				this.width,
				this.height,
				this.mipLevel,
				reader.get(this.missing),
				regions,
				CompletableFuture.runAsync(
						() ->{
							throw new RuntimeException("Cached object not yet finalized");
						}
				)
		);
	}
}
