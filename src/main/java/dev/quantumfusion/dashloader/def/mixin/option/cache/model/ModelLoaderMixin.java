package dev.quantumfusion.dashloader.def.mixin.option.cache.model;

import com.mojang.datafixers.util.Pair;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.fallback.model.MissingDashModel;
import dev.quantumfusion.dashloader.def.fallback.model.UnbakedBakedModel;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(value = ModelLoader.class, priority = 69420)
public class ModelLoaderMixin {

	@Shadow
	@Final
	private Map<Identifier, UnbakedModel> unbakedModels;

	@Mutable
	@Shadow
	@Final
	private Object2IntMap<BlockState> stateLookup;


	@Inject(
			method = "<init>(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiler/Profiler;I)V",
			at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", args = "ldc=missing_model", shift = At.Shift.AFTER)
	)
	private void injectLoadedModels(ResourceManager resourceManager, BlockColors blockColors, Profiler profiler, int i, CallbackInfo ci) {
		if (DashLoader.isRead()) {
			var data = DashLoader.getData();
			var dashModels = data.bakedModels.getCacheResultData();
			DashLoader.LOGGER.info("Injecting {} Cached Models", dashModels.size());
			dashModels.forEach((identifier, bakedModel) -> {
				if (!(bakedModel instanceof MissingDashModel))
					this.unbakedModels.put(identifier, new UnbakedBakedModel(bakedModel));
			});

			this.stateLookup = data.modelStateLookup.getCacheResultData();
		}
	}

	@Inject(
			method = "<init>(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiler/Profiler;I)V",
			at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=stitching"),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onFinishAddingModels(ResourceManager resourceManager, BlockColors blockColors, Profiler profiler, int mipmap, CallbackInfo ci,
			Set<Pair<String, String>> thing,
			Set<SpriteIdentifier> thing2,
			Map<Identifier, List<SpriteIdentifier>> map) {
		if (DashLoader.isRead()) map.clear();
	}

}
