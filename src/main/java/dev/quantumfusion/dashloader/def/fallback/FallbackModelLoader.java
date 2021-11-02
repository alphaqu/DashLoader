package dev.quantumfusion.dashloader.def.fallback;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;

public class FallbackModelLoader extends ModelLoader {
	public FallbackModelLoader(ResourceManager resourceManager, BlockColors blockColors, Profiler profiler, int i) {
		super(resourceManager, blockColors, profiler, i);
	}



}
