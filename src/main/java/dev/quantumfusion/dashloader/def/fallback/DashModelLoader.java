package dev.quantumfusion.dashloader.def.fallback;

import com.mojang.datafixers.util.Pair;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.mixin.accessor.ModelLoaderAccessor;
import dev.quantumfusion.dashloader.def.util.UnsafeHelper;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

public class DashModelLoader extends ModelLoader {
	private Map<Identifier, BakedModel> models;
	private Map<Identifier, UnbakedModel> modelsUnbaked;
	@Nullable
	private Field fabric_mlrLoaderInstance = null;

	// will never be called muahahhahha
	private DashModelLoader(ResourceManager resourceManager, BlockColors blockColors, Profiler profiler, int i) {
		super(resourceManager, blockColors, profiler, i);
	}

	private void constructor(ResourceManager resourceManager, SpriteAtlasManager manager, BlockColors blockColors, Map<Identifier, UnbakedModel> modelsUnbaked, Map<Identifier, BakedModel> models) {
		this.models = models;
		ModelLoaderAccessor access = ((ModelLoaderAccessor) this);
		access.setVariantMapDeserializationContext(new ModelVariantMap.DeserializationContext());
		access.setBlockColors(blockColors);
		access.setStateLookup(DashLoader.getData().modelStateLookup.getCacheResultData());
		access.setSpriteAtlasManager(manager);
		this.modelsUnbaked = modelsUnbaked;
		access.setUnbakedModels(this.modelsUnbaked);
		access.setResourceManager(resourceManager);
		access.setModelsToLoad(new HashSet<>());
		access.setModelsToBake(new HashMap<>());
		access.setBakedModelCache(new HashMap<>());
		try {
			final Field fabric_mlrLoaderInstance = ModelLoader.class.getDeclaredField("fabric_mlrLoaderInstance");
			fabric_mlrLoaderInstance.setAccessible(true);
			DashLoader.LOGGER.info("Detected Fabric Model API. Calling addmodel");
			this.fabric_mlrLoaderInstance = fabric_mlrLoaderInstance;
		} catch (NoSuchFieldException e) {
			DashLoader.LOGGER.info("No Fabric Model API found");
		}

		System.out.println(MISSING_ID);
		access.invokeAddModel(MISSING_ID);
	}

	public static void bakeUnsupportedModels(ResourceManager resourceManager, SpriteAtlasManager manager, BlockColors blockColors, Map<Identifier, BakedModel> models) {

		var modelsUnbaked = new HashMap<Identifier, UnbakedModel>();
		models.forEach((identifier, bakedModel) -> {
			if (!(bakedModel instanceof MissingDashModel.MissingDashModelWrapper)) {
				modelsUnbaked.put(identifier, new BakedModelWrapper(bakedModel));
			}
		});


		final DashModelLoader dashModelLoader = UnsafeHelper.allocateInstance(DashModelLoader.class);
		dashModelLoader.constructor(resourceManager, manager, blockColors, modelsUnbaked, models);
		dashModelLoader.bakeModels();


	}

	public void bakeModels() {
		List<UnbakedModel> unbakedModels = new ArrayList<>();
		for (Identifier identifier : DashLoader.getData().getReadContextData().missingModelsRead) {
			unbakedModels.add(getOrLoadModel(identifier));
		}

		final HashSet<Pair<String, String>> unresolvedTextureReferences = new HashSet<>();
		for (UnbakedModel unbakedModel : unbakedModels) {
			unbakedModel.getTextureDependencies(this::getOrLoadModel, unresolvedTextureReferences);
		}

		unresolvedTextureReferences.stream().filter((pair) -> !pair.getSecond().equals(MISSING_ID.toString())).forEach((pair) -> {
			DashLoader.LOGGER.warn("Unable to resolve texture reference: {} in {}", pair.getFirst(), pair.getSecond());
		});

		modelsUnbaked.forEach((identifier, unbakedModel) -> {
			if (!(unbakedModel instanceof DashModelLoader.BakedModelWrapper))
				models.put(identifier, bake(identifier, ModelRotation.X0_Y0));
		});

		if (fabric_mlrLoaderInstance != null) {
			try {
				DashLoader.LOGGER.info("Detected Fabric Model API. Calling finish() hook");
				fabric_mlrLoaderInstance.setAccessible(true);
				final Object o = fabric_mlrLoaderInstance.get(this);
				o.getClass().getMethod("finish").invoke(o);
			} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}


	private record BakedModelWrapper(BakedModel model) implements UnbakedModel {

		@Override
		public Collection<Identifier> getModelDependencies() {
			return List.of();
		}

		@Override
		public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
			return List.of();
		}

		@Nullable
		@Override
		public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
			return model;
		}
	}
}
