package dev.notalpha.dashloader.client.model;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.CachingData;
import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.cache.Cache;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.collection.IntIntList;
import dev.notalpha.dashloader.api.collection.IntObjectList;
import dev.notalpha.dashloader.api.collection.ObjectIntList;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.client.Dazy;
import dev.notalpha.dashloader.client.model.components.DashModelBakeSettings;
import dev.notalpha.dashloader.client.model.fallback.MultiUnbakedBakedModel;
import dev.notalpha.dashloader.client.model.fallback.UnbakedBakedGroupableModel;
import dev.notalpha.dashloader.client.model.fallback.UnbakedBakedModel;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.config.Option;
import dev.notalpha.dashloader.mixin.accessor.ModelLoaderAccessor;
import dev.notalpha.taski.builtin.StepTask;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelModule implements DashModule<ModelModule.Data> {
	public static final CachingData<HashMap<ModelBaker.BakedModelCacheKey, BakedModel>> BAKED_MODEL_PARTS = new CachingData<>(CacheStatus.SAVE);

	public static final CachingData<HashMap<ModelIdentifier, BakedModel>> BLOCK_MODELS_SAVE = new CachingData<>(CacheStatus.SAVE);
	public static final CachingData<Map<ModelIdentifier, BlockStatesLoader.BlockModel>> RAW_BLOCK_MODELS = new CachingData<>(CacheStatus.SAVE);

//	public static final CachingData<HashMap<Identifier, ItemModel>> ITEM_MODELS_SAVE = new CachingData<>(CacheStatus.SAVE);
//	public static final CachingData<HashMap<Identifier, ItemAsset.Properties>> ITEM_PROPERTIES = new CachingData<>(CacheStatus.SAVE);

	public static final CachingData<HashMap<Identifier, UnbakedModel>> MODEL_PARTS = new CachingData<>(CacheStatus.LOAD);
	public static final CachingData<ArrayList<Identifier>> MISSING_MODEL_PARTS = new CachingData<>(CacheStatus.LOAD);

	public static final CachingData<Map<ModelIdentifier, BlockStatesLoader.BlockModel>> BLOCK_MODELS = new CachingData<>(CacheStatus.LOAD);
	public static final CachingData<ArrayList<Identifier>> MISSING_BLOCK_MODELS = new CachingData<>(CacheStatus.LOAD);

//	public static final CachingData<HashMap<Identifier, ItemAsset>> ITEM_MODELS = new CachingData<>(CacheStatus.LOAD);
//	public static final CachingData<ArrayList<Identifier>> MISSING_ITEM_MODELS = new CachingData<>(CacheStatus.LOAD);


	public static final CachingData<HashMap<MultipartUnbakedModel, Pair<List<MultipartModelComponent>, StateManager<Block, BlockState>>>> MULTIPART_PREDICATES = new CachingData<>(CacheStatus.SAVE);
	public static final CachingData<HashMap<BakedModel, MultipartUnbakedModel>> UNBAKED_TO_BAKED_MULTIPART_MODELS = new CachingData<>(CacheStatus.SAVE);

	public static StateManager<Block, BlockState> getStateManager(Identifier identifier) {
		StateManager<Block, BlockState> staticDef = ModelLoaderAccessor.getStaticDefinitions().get(identifier);
		if (staticDef != null) {
			return staticDef;
		} else {
			return Registries.BLOCK.get(identifier).getStateManager();
		}
	}

	@NotNull
	public static Identifier getStateManagerIdentifier(StateManager<Block, BlockState> stateManager) {
		// Static definitions like itemframes.
		for (Map.Entry<Identifier, StateManager<Block, BlockState>> entry : ModelLoaderAccessor.getStaticDefinitions().entrySet()) {
			if (entry.getValue() == stateManager) {
				return entry.getKey();
			}
		}

		return Registries.BLOCK.getId(stateManager.getOwner());
	}

	@Override
	public void reset(Cache cache) {
		BAKED_MODEL_PARTS.reset(cache, new HashMap<>());

		BLOCK_MODELS_SAVE.reset(cache, new HashMap<>());
		RAW_BLOCK_MODELS.reset(cache, new HashMap<>());

//		ITEM_MODELS_SAVE.reset(cache, new HashMap<>());
//		ITEM_PROPERTIES.reset(cache, new HashMap<>());

		MODEL_PARTS.reset(cache, new HashMap<>());
		MISSING_MODEL_PARTS.reset(cache, new ArrayList<>());

		BLOCK_MODELS.reset(cache, new HashMap<>());
		MISSING_BLOCK_MODELS.reset(cache, new ArrayList<>());

//		ITEM_MODELS.reset(cache, new HashMap<>());
//		MISSING_ITEM_MODELS.reset(cache, new ArrayList<>());


		MULTIPART_PREDICATES.reset(cache, new HashMap<>());
		UNBAKED_TO_BAKED_MULTIPART_MODELS.reset(cache, new HashMap<>());
	}

	@Override
	public Data save(RegistryWriter factory, StepTask task) {
		var bakedModelCache = BAKED_MODEL_PARTS.get(CacheStatus.SAVE);
		var blockModels = BLOCK_MODELS_SAVE.get(CacheStatus.SAVE);
		var rawBlockModels = RAW_BLOCK_MODELS.get(CacheStatus.SAVE);
//		var itemModels = ITEM_MODELS_SAVE.get(CacheStatus.SAVE);
//		var itemProperties = ITEM_PROPERTIES.get(CacheStatus.SAVE);


		if (bakedModelCache == null || blockModels == null || rawBlockModels == null) {
			return null;
		}

		// split models that have been baked with multiple vs one setting
		var modelPartsRemap = new Int2ObjectOpenHashMap<ObjectIntList<DashModelBakeSettings.BakeSettings>>(bakedModelCache.size());
		var outModelPartsVariants = new IntObjectList<IntIntList>(new ArrayList<>(bakedModelCache.size()));
		var outModelParts = new IntIntList(new ArrayList<>(bakedModelCache.size()));

		var missingModelParts = new IntArrayList();

		var outBlockModels = new IntObjectList<IntIntList.IntInt>(new ArrayList<>(blockModels.size()));
		var missingBlockModels = new IntArrayList();

//		var outItemModels = new IntObjectList<IntIntList.IntInt>(new ArrayList<>(itemModels.size()));
//		var missingItemModels = new IntArrayList();

		task.doForEach(bakedModelCache, (key, model) -> {
			if (model == null) return;

			try {
				var regId = factory.add(model);

				modelPartsRemap.compute(factory.add(key.id()), (id, models) -> {
					if (models == null) models = new ObjectIntList<>();
					// don't add bake settings to the registry yet, might not need
					models.put(new DashModelBakeSettings.BakeSettings(key.transformation(), key.isUvLocked()), regId);

					return models;
				});
			} catch (RuntimeException e) {
				// NB: nested factory.add calls (e.g. multipart parts with modded model impls
				// like Refined Storage cables, see #121) surface as wrapped RuntimeExceptions,
				// so a narrow catch would abort the whole save. Skip + vanilla fallback instead.
				if (missingModelParts.size() < 3) {
					DashLoader.LOG.warn("Skipping uncacheable model part {} ({}): {}", key.id(), model.getClass().getName(), e.getMessage());
				}
				missingModelParts.add(factory.add(key.id()));
			}
		});

		final int[] num = {0};

		modelPartsRemap.forEach((id, models) -> {
			var list = models.list();
			num[0] += list.size();

			if (list.size() == 1) { // can ignore bake settings
				outModelParts.put(id, list.getFirst().value());
			} else {
				var data = new IntIntList(new ArrayList<>(list.size()));
				list.forEach(entry -> {
					data.put(factory.add(entry.key()), entry.value());
				});
				outModelPartsVariants.put(id, data);
			}
		});

		DashLoader.LOG.info("saved {}/{} model parts", num, bakedModelCache.size());
		DashLoader.LOG.info("missing {} model parts", missingModelParts.size());

		task.doForEach(blockModels, (modelId, model) -> {
			if (model == null) return;

			try {
				var regId = factory.add(model);
				var blockModel = new IntIntList.IntInt(factory.add(rawBlockModels.get(modelId).state()), regId);
				outBlockModels.put(factory.add(modelId), blockModel);
			} catch (RuntimeException e) {
				if (missingBlockModels.size() < 3) {
					DashLoader.LOG.warn("Skipping uncacheable block model {} ({}): {}", modelId.id(), model.getClass().getName(), e.getMessage());
				}
				missingBlockModels.add(factory.add(modelId.id()));
			}
		});

		DashLoader.LOG.info("saved {}/{} block models", outBlockModels.list().size(), blockModels.size());
		DashLoader.LOG.info("missing {} block models", missingBlockModels.size());

//		task.doForEach(itemModels, (id, model) -> {
//			if (model == null) return;
//
//			try {
//				var regId = factory.add(model); // TODO: ItemModel caching. fyi there's 6 variants and more to add
//				var itemAsset = new IntIntList.IntInt(factory.add(itemProperties.getOrDefault(id, ItemAsset.Properties.DEFAULT)), regId);
//				outItemModels.put(factory.add(id), itemAsset);
//			} catch (RegistryAddException ignored) {
//				missingItemModels.add(factory.add(id));
//			}
//		});
//
//		DashLoader.LOG.info("saved {}/{} item models", outItemModels.list().size(), itemModels.size());
//		DashLoader.LOG.info("missing {} item models", missingItemModels.size());

		return new Data(
				outModelPartsVariants,
				outModelParts,
				missingModelParts.toIntArray(),
				outBlockModels,
				missingBlockModels.toIntArray()
//				outItemModels,
//				missingItemModels.toIntArray()
		);
	}

	@Override
	public void load(Data data, RegistryReader reader, StepTask task) {
		var modelParts = new HashMap<Identifier, UnbakedModel>(data.modelParts.list().size() + data.modelPartsVariants.list().size());
		var missingModelParts = new ArrayList<Identifier>(data.missingModelParts.length);

		var blockModels = new HashMap<ModelIdentifier, BlockStatesLoader.BlockModel>(data.blockModels.list().size());
		var missingBlockModels = new ArrayList<Identifier>(data.missingBlockModels.length);
//		var itemModels = new HashMap<Identifier, ItemAsset>(data.itemModels.list().size());
//		var missingItemModels = new ArrayList<Identifier>(data.missingItemModels.length);

		data.modelPartsVariants.forEach((id, entry) -> {
			var thing = new HashMap<DashModelBakeSettings.BakeSettings, Dazy<? extends BakedModel>>();
			entry.forEach((settings, model) -> {
				thing.put(reader.get(settings), reader.get(model));
			});
			modelParts.put(reader.get(id), new MultiUnbakedBakedModel(thing));
		});

		data.modelParts.forEach((id, model) -> {
			modelParts.put(reader.get(id), new UnbakedBakedModel(reader.get(model)));
		});

		for (int id : data.missingModelParts) {
			missingModelParts.add(reader.get(id));
		}

		data.blockModels.forEach((id, blockModel) -> {
			BlockState blockState = reader.get(blockModel.key());
			var dazyModel = new UnbakedBakedGroupableModel(reader.get(blockModel.value()));
			blockModels.put(reader.get(id), new BlockStatesLoader.BlockModel(blockState, dazyModel));
		});

		for (int id : data.missingBlockModels) {
			missingBlockModels.add(reader.get(id));
		}

//		data.itemModels.forEach((id, itemAsset) -> {
//			ItemAsset.Properties properties = reader.get(itemAsset.value());
//			var model = reader.get(itemAsset.key());
//			itemModels.put(reader.get(id), new ItemAsset(model, properties));
//		});

		MODEL_PARTS.set(CacheStatus.LOAD, modelParts);
		MISSING_MODEL_PARTS.set(CacheStatus.LOAD, missingModelParts);
		BLOCK_MODELS.set(CacheStatus.LOAD, blockModels);
		MISSING_BLOCK_MODELS.set(CacheStatus.LOAD, missingBlockModels);
//		ITEM_MODELS.set(CacheStatus.IDLE, itemModels);
//		MISSING_ITEM_MODELS.set(CacheStatus.IDLE, missingItemModels);
	}

	@Override
	public Class<Data> getDataClass() {
		return Data.class;
	}

	@Override
	public float taskWeight() {
		return 1000;
	}

	@Override
	public boolean isActive() {
		return ConfigHandler.optionActive(Option.CACHE_MODEL_LOADER);
	}

	public static final class Data {
		public final IntObjectList<IntIntList> modelPartsVariants;
		public final IntIntList modelParts;
		public final int[] missingModelParts;
		public final IntObjectList<IntIntList.IntInt> blockModels;
		public final int[] missingBlockModels;
//		public final IntObjectList<IntIntList.IntInt> itemModels;
//		public final int[] missingItemModels;

		public Data(
				IntObjectList<IntIntList> modelPartsVariants,
				IntIntList modelParts,
				int[] missingModelParts,
				IntObjectList<IntIntList.IntInt> blockModels,
				int[] missingBlockModels
//				IntObjectList<IntIntList.IntInt> itemModels,
//				int[] missingItemModels
		) {
			this.modelPartsVariants = modelPartsVariants;
			this.modelParts = modelParts;
			this.missingModelParts = missingModelParts;
			this.blockModels = blockModels;
			this.missingBlockModels = missingBlockModels;
//			this.itemModels = itemModels;
//			this.missingItemModels = missingItemModels;
		}
	}
}
