package dev.quantumfusion.dashloader.def.data.model;

import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.mixin.accessor.MultipartBakedModelAccessor;
import dev.quantumfusion.dashloader.def.util.RegistryUtil;
import dev.quantumfusion.dashloader.def.util.UnsafeHelper;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.state.StateManager;
import net.minecraft.util.Util;

import net.oskarstrom.dashloader.core.annotations.Dependencies;
import net.oskarstrom.dashloader.core.data.IntIntList;
import net.oskarstrom.dashloader.core.data.IntObjectList;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.core.annotations.DashObject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Data
@DashObject(MultipartBakedModel.class)
@Dependencies({WeightedBakedModel.class})
public class DashMultipartBakedModel implements DashModel {
	private static final Class<MultipartBakedModel> cls = MultipartBakedModel.class;
	//identifier baked model
	public final IntIntList components;
	public final IntObjectList<byte[]> stateCache;
	transient MultipartBakedModel toApply;

	public DashMultipartBakedModel(IntIntList components, IntObjectList<byte[]> stateCache) {
		this.components = components;
		this.stateCache = stateCache;
	}

	public DashMultipartBakedModel(MultipartBakedModel model, DashRegistry registry) {
		final Pair<List<MultipartModelSelector>, StateManager<Block, BlockState>> selectors = DashLoader.getVanillaData().getModelData(model);
		MultipartBakedModelAccessor access = ((MultipartBakedModelAccessor) model);
		List<Pair<Predicate<BlockState>, BakedModel>> accessComponents = access.getComponents();
		final int size = accessComponents.size();
		this.components = new IntIntList(new ArrayList<>(size));
		for (int i = 0; i < size; i++) {
			final BakedModel right = accessComponents.get(i).getRight();

			final MultipartModelSelector selector = selectors.getKey().get(i);
			DashLoader.getVanillaData().stateManagers.put(selector, selectors.getValue());

			components.put(registry.add(RegistryUtil.preparePredicate(selector)), registry.add(right));
		}
		final Map<BlockState, BitSet> stateCache = access.getStateCache();
		this.stateCache = new IntObjectList<>(DashHelper.convertMapToCollection(
				stateCache,
				(entry) -> new IntObjectList.IntObjectEntry<>(registry.add(entry.getKey()), entry.getValue().toByteArray())));
	}

	@Override
	public MultipartBakedModel toUndash(DashExportHandler handler) {
		MultipartBakedModel model = UnsafeHelper.allocateInstance(cls);
		Map<BlockState, BitSet> stateCacheOut = new Object2ObjectOpenCustomHashMap<>(Util.identityHashStrategy());
		stateCache.forEach((key, value) -> stateCacheOut.put(handler.get(key), BitSet.valueOf(value)));
		((MultipartBakedModelAccessor) model).setStateCache(stateCacheOut);
		toApply = model;
		return model;
	}

	@Override
	public void apply(DashExportHandler handler) {
		List<Pair<Predicate<BlockState>, BakedModel>> componentsOut = new ArrayList<>();
		components.forEach((key, value) -> componentsOut.add(Pair.of(handler.get(key), handler.get(value))));
		MultipartBakedModelAccessor access = ((MultipartBakedModelAccessor) toApply);
		BakedModel bakedModel = (BakedModel) ((Pair) componentsOut.iterator().next()).getRight();
		access.setComponents(componentsOut);
		access.setAmbientOcclusion(bakedModel.useAmbientOcclusion());
		access.setDepthGui(bakedModel.hasDepth());
		access.setSideLit(bakedModel.isSideLit());
		access.setSprite(bakedModel.getParticleSprite());
		access.setTransformations(bakedModel.getTransformation());
		access.setItemPropertyOverrides(bakedModel.getOverrides());
	}
}

