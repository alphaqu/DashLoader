package dev.notalpha.dashloader.client.model;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.CachingData;
import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.cache.Cache;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.collection.IntIntList;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.client.Dazy;
import dev.notalpha.dashloader.client.blockstate.DashBlockState;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.config.Option;
import dev.notalpha.taski.builtin.StepTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Model caching for the 1.21.5+ model system ({@link BlockStateModel}).
 * <p>
 * SAVE: {@link dev.notalpha.dashloader.mixin.option.cache.model.BakedModelManagerMixin} captures the
 * fully baked {@code Map<BlockState, BlockStateModel>} from {@code BakedModelManager.upload}.
 * Every model is stored by its resolved parts; weighted models keep all variants so vanilla
 * random picking is preserved. Multipart models resolve deterministically per state, so they
 * flatten into plain part lists, no selector caching needed.
 * <p>
 * LOAD: {@link dev.notalpha.dashloader.mixin.option.cache.model.BlockStatesLoaderMixin} shortcuts
 * {@code BlockStatesLoader.load} with {@link DashBlockStateModel.DashUnbakedGrouped} wrappers,
 * so the vanilla bake pipeline runs without any JSON parsing.
 */
public class ModelModule implements DashModule<ModelModule.Data> {
	public static final CachingData<HashMap<BlockState, BlockStateModel>> BLOCK_STATE_MODELS = new CachingData<>(CacheStatus.SAVE);
	public static final CachingData<HashMap<BlockState, BlockStateModel.UnbakedRoot>> BLOCK_STATE_UNBAKED = new CachingData<>(CacheStatus.LOAD);

	@Override
	public void reset(Cache cache) {
		BLOCK_STATE_MODELS.reset(cache, new HashMap<>());
		BLOCK_STATE_UNBAKED.reset(cache, new HashMap<>());
	}

	@Override
	public Data save(RegistryWriter factory, StepTask task) {
		var blockModels = BLOCK_STATE_MODELS.get(CacheStatus.SAVE);
		if (blockModels == null) {
			return null;
		}

		var outBlockModels = new IntIntList(new ArrayList<>(blockModels.size()));

		task.doForEach(blockModels, (state, model) -> {
			if (model == null) return;

			try {
				// factory.add dispatches on the runtime class (hierarchy-aware) and
				// constructs the DashObject itself - never pass DashObjects here.
				final int modelPtr = factory.add(model);
				outBlockModels.put(factory.add(state), modelPtr);
			} catch (RuntimeException e) {
				if (outBlockModels.list().size() < 3) {
					StringBuilder chain = new StringBuilder();
					Throwable t = e;
					while (t != null && chain.length() < 1000) {
						chain.append(t.getClass().getName()).append(": ").append(t.getMessage()).append(" <- ");
						t = t.getCause();
					}
					DashLoader.LOG.warn("Skipping uncacheable model {} -> {}: {}", state, model == null ? "null" : model.getClass().getName(), chain);
				}
				// states without resolvable models (e.g. missing) are filled by vanilla on LOAD
			}
		});

		DashLoader.LOG.info("saved {} blockstate models", outBlockModels.list().size());
		return new Data(outBlockModels);
	}

	@Override
	public void load(Data data, RegistryReader reader, StepTask task) {
		var blockModels = new HashMap<BlockState, BlockStateModel.UnbakedRoot>(data.blockModels.list().size());

		data.blockModels.forEach((statePtr, modelPtr) -> {
			BlockState state = reader.get(statePtr);
			Dazy<? extends BlockStateModel> model = reader.get(modelPtr);
			blockModels.put(state, new DashBlockStateModel.DashUnbakedGrouped(model));
		});

		BLOCK_STATE_UNBAKED.set(CacheStatus.LOAD, blockModels);
		DashLoader.LOG.info("loaded {} blockstate models", blockModels.size());
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

	@NotNull
	public static Map<BlockState, BlockStateModel.UnbakedRoot> getUnbakedForLoad() {
		var map = BLOCK_STATE_UNBAKED.get(CacheStatus.LOAD);
		return map == null ? Map.of() : map;
	}

	public static final class Data {
		public final IntIntList blockModels;

		public Data(IntIntList blockModels) {
			this.blockModels = blockModels;
		}
	}
}
