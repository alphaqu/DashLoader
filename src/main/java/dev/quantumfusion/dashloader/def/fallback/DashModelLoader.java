package dev.quantumfusion.dashloader.def.fallback;

import com.mojang.datafixers.util.Pair;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.mixin.accessor.BasicBakedModelAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.ModelLoaderAccessor;
import dev.quantumfusion.dashloader.def.util.UnsafeHelper;
import dev.quantumfusion.dashloader.def.util.mixins.MixinInvoker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

public class DashModelLoader extends ModelLoader {
	private ModelLoaderAccessor access;
	private SpriteAtlasManager manager;
	private Map<Identifier, BakedModel> models;
	private Map<Identifier, UnbakedModel> modelsToBake;
	private Map<Identifier, UnbakedModel> unbakedModels;

	private ResourceManager resourceManager;
	private BlockColors blockColors;
	private Profiler profiler;
	private int mipmapLevel;

	@Nullable
	private Field fabric_mlrLoaderInstance = null;


	// will never be called muahahhahha
	private DashModelLoader(ResourceManager resourceManager, BlockColors blockColors, Profiler profiler, int i) {
		super(resourceManager, blockColors, profiler, i);
	}

	private void init(SpriteAtlasManager manager, Map<Identifier, BakedModel> models, ResourceManager resourceManager, BlockColors blockColors, Profiler profiler, int mipmapLevel) {
		this.manager = manager;
		this.models = models;
		this.unbakedModels = new HashMap<>();
		this.modelsToBake = new HashMap<>();

		this.resourceManager = resourceManager;
		this.blockColors = blockColors;
		this.profiler = profiler;
		this.mipmapLevel = mipmapLevel;

		this.access = ((ModelLoaderAccessor) this);
		this.access.setSpriteAtlasManager(this.manager);
		this.access.setBakedModels(this.models);
		this.access.setUnbakedModels(this.unbakedModels);

		this.access.setResourceManager(this.resourceManager);
		this.access.setBlockColors(this.blockColors);
		this.access.setModelsToBake(this.modelsToBake);

		this.access.setStateLookup(DashLoader.getData().modelStateLookup.getCacheResultData());
		this.access.setVariantMapDeserializationContext(new ModelVariantMap.DeserializationContext());
		this.access.setBakedModelCache(new HashMap<>());
		this.access.setModelsToLoad(new HashSet<>());
		this.access.setSpriteAtlasData(new HashMap<>());
		this.access.invokeAddModel(MISSING_ID); // inits fabricapi things
		checkForFabricAPIField();
	}

	private void checkForFabricAPIField() {
		try {
			final Field fabric_mlrLoaderInstance = ModelLoader.class.getDeclaredField("fabric_mlrLoaderInstance");
			fabric_mlrLoaderInstance.setAccessible(true);
			DashLoader.LOGGER.info("Detected Fabric Model API. Calling addmodel");
			this.fabric_mlrLoaderInstance = fabric_mlrLoaderInstance;
		} catch (NoSuchFieldException e) {
			DashLoader.LOGGER.info("No Fabric Model API found");
		}
	}

	public static void bakeUnsupportedModels(ResourceManager resourceManager, SpriteAtlasManager manager, BlockColors blockColors, Map<Identifier, BakedModel> models, Profiler profiler, int mipmapLevel) {
		final DashModelLoader dashModelLoader = UnsafeHelper.allocateInstance(DashModelLoader.class);
		dashModelLoader.init(manager, models, resourceManager, blockColors, profiler, mipmapLevel);
		dashModelLoader.bakeUnsupportedModels();
	}

	public void bakeUnsupportedModels() {
		// adds all of our models that already exist, we do this so when models load they can reuse dash models
		models.forEach((identifier, bakedModel) -> {
			if (!(bakedModel instanceof MissingDashModel.MissingDashModelWrapper)) {
				unbakedModels.put(identifier, new BakedModelWrapper(bakedModel));
			}
		});

		final Set<Identifier> missingModels = new HashSet<>(DashLoader.getData().getReadContextData().missingModelsRead);


		final Map<BlockState, Identifier> blockstateModelsMissing = new HashMap<>();
		for (Block block : Registry.BLOCK) {
			block.getStateManager().getStates().forEach((blockState) -> {
				final ModelIdentifier modelId = BlockModels.getModelId(blockState);
				if (missingModels.contains(modelId)) {
					blockstateModelsMissing.put(blockState, modelId);
					missingModels.remove(modelId);
				}
			});
		}

		MixinInvoker.invokeMethod(ModelLoader.class, "afterStoreArgs", this, resourceManager, blockColors, profiler, mipmapLevel, null);

		final MethodHandle[] mixinMethods = MixinInvoker.getMixinMethods(ModelLoader.class, BlockState.class, CallbackInfo.class);
		blockstateModelsMissing.forEach((blockState, identifier) -> {
			MixinInvoker.invokeMixinMethods(mixinMethods, this, blockState, null);
			access.invokeAddModel((ModelIdentifier) identifier);
		});

		for (Identifier identifier : missingModels) {
			access.invokeAddModel((ModelIdentifier) identifier);
		}

		// invokes all mixins that are found matching <init> parameters
		MixinInvoker.invokeMethod(ModelLoader.class, "onFinishAddingModels", this, resourceManager, blockColors, profiler, mipmapLevel, null);

		// gets all texture dependencies as they may load more models
		var missingTextures = new HashSet<Pair<String, String>>();
		modelsToBake.forEach((identifier, unbakedModel) -> {
			if (unbakedModel != null) {
				unbakedModel.getTextureDependencies(this::getOrLoadModel, missingTextures);
			}
		});


		// prints missing textures if it finds them
		for (var missingTexture : missingTextures) {
			if (!missingTexture.getSecond().equals(MISSING_ID.toString())) {
				DashLoader.LOGGER.warn("Fallback system was not able to resolve texture: {} in {}", missingTexture.getFirst(), missingTexture.getSecond());
			}
		}

		// goes through all of the unbaked models and bakes them. We dont need to bake our wrappers as they already exist in the models map
		modelsToBake.forEach((identifier, unbakedModel) -> {
			if (unbakedModel != null) {
				models.put(identifier, bake(identifier, ModelRotation.X0_Y0));
			}
		});

		finish();
	}

	public void invokeMixins() {
		final MethodHandle[] mixinMethods = MixinInvoker.getMixinMethods(ModelLoader.class, ResourceManager.class, BlockColors.class, Profiler.class, int.class, CallbackInfo.class);
		MixinInvoker.invokeMixinMethods(mixinMethods, this, resourceManager, blockColors, profiler, mipmapLevel, new CallbackInfo("funny", true));
	}

	public void finish() {
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

	public static final class BakedModelWrapper implements UnbakedModel {
		public final BakedModel model;
		//public final Collection<SpriteIdentifier> textures = new HashSet<>();

		public BakedModelWrapper(BakedModel model) {
			this.model = model;
			//if (model instanceof BasicBakedModel bakedModel) {
			//	BasicBakedModelAccessor accessor = ((BasicBakedModelAccessor) bakedModel);
			//	addQuads(accessor.getQuads());
			//	accessor.getFaceQuads().forEach((direction, bakedQuads) -> {
			//		addQuads(bakedQuads);
			//	});
			//} else if (model instanceof BuiltinBakedModel bakedModel) {
			//	createSpriteIdentifier(bakedModel.getParticleSprite());
			//}
		}

		//private void addQuads(List<BakedQuad> bakedQuads) {
		//	for (BakedQuad quad : bakedQuads) {
		//		final Sprite sprite = quad.getSprite();
		//		textures.add(createSpriteIdentifier(sprite));
		//	}
		//}

		@NotNull
		private SpriteIdentifier createSpriteIdentifier(Sprite sprite) {
			return new SpriteIdentifier(sprite.getAtlas().getId(), sprite.getId());
		}

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

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj == null || obj.getClass() != this.getClass()) return false;
			var that = (BakedModelWrapper) obj;
			return Objects.equals(this.model, that.model);
		}

		@Override
		public int hashCode() {
			return Objects.hash(model);
		}
	}
}
