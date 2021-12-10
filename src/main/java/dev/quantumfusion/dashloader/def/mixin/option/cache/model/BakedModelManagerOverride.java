package dev.quantumfusion.dashloader.def.mixin.option.cache.model;

import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.api.option.ConfigHandler;
import dev.quantumfusion.dashloader.def.api.option.Option;
import dev.quantumfusion.dashloader.def.fallback.UnbakedBakedModel;
import dev.quantumfusion.dashloader.def.mixin.accessor.ModelLoaderAccessor;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(value = BakedModelManager.class, priority = 69420)
public abstract class BakedModelManagerOverride {
	@Shadow
	@Nullable
	private SpriteAtlasManager atlasManager;

	@Shadow
	private Map<Identifier, BakedModel> models;

	@Shadow
	private Object2IntMap<BlockState> stateLookup;

	@Inject(method = "apply*",
			at = @At(value = "RETURN"))
	private void applyStage(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
		if (DashLoader.isWrite() || !ConfigHandler.optionActive(Option.CACHE_MODEL_LOADER)) {
			final DashDataManager data = DashLoader.getData();
			data.spriteAtlasManager.setMinecraftData(atlasManager);
			data.bakedModels.setMinecraftData(models);
			data.modelStateLookup.setMinecraftData(stateLookup);
		}
	}

}
