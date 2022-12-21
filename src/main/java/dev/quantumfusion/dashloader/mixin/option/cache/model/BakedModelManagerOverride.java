package dev.quantumfusion.dashloader.mixin.option.cache.model;

import dev.quantumfusion.dashloader.DashDataManager;
import dev.quantumfusion.dashloader.DashLoader;
import java.util.Map;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static dev.quantumfusion.dashloader.DashLoader.DL;

@Mixin(value = BakedModelManager.class, priority = 69420)
public abstract class BakedModelManagerOverride {
	@Shadow
	private Map<Identifier, BakedModel> models;

	@Inject(method = "upload",
			at = @At(value = "TAIL")
	)

	private void yankAssets(BakedModelManager.BakingResult bakingResult, Profiler profiler, CallbackInfo ci) {
		if (DL.isWrite()) {
			final DashDataManager data = DL.getData();
			DashLoader.LOG.info("Yanking Minecraft Assets");
			data.bakedModels.setMinecraftData(this.models);
		}
	}

}
