package dev.quantumfusion.dashloader.mixin.option.cache.model;

import dev.quantumfusion.dashloader.DashDataManager;
import dev.quantumfusion.dashloader.DashLoader;
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

import java.util.Map;

@Mixin(value = BakedModelManager.class, priority = 69420)
public abstract class BakedModelManagerOverride {
	@Shadow
	@Nullable
	private SpriteAtlasManager atlasManager;

	@Shadow
	private Map<Identifier, BakedModel> models;

	@Inject(method = "apply*",
			at = @At(value = "TAIL")
	)

	private void yankAssets(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
		if (DashLoader.isWrite()) {
			final DashDataManager data = DashLoader.getData();
			DashLoader.LOGGER.info("Yanking Minecraft Assets");
			data.spriteAtlasManager.setMinecraftData(this.atlasManager);
			data.bakedModels.setMinecraftData(this.models);
		}
	}

}
