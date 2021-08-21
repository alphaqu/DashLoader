package net.oskarstrom.dashloader.def.model;

import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.mixin.accessor.MultipartBakedModelAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.MultipartBakedModel;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.state.StateManager;
import net.minecraft.util.Util;
import net.oskarstrom.dashloader.api.data.Pointer2ObjectMap;
import net.oskarstrom.dashloader.api.data.Pointer2PointerMap;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.util.UnsafeHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@DashObject(MultipartBakedModel.class)
public class DashMultipartBakedModel implements DashModel {

	private static final Class<MultipartBakedModel> cls = MultipartBakedModel.class;
	//identifier baked model
	@Serialize(order = 0)
	public final Pointer2PointerMap components;
	@Serialize(order = 1)
	public final Pointer2ObjectMap<byte[]> stateCache;
	MultipartBakedModel toApply;

	public DashMultipartBakedModel(@Deserialize("components") Pointer2PointerMap components,
								   @Deserialize("stateCache") Pointer2ObjectMap<byte[]> stateCache) {
		this.components = components;
		this.stateCache = stateCache;
	}

	public DashMultipartBakedModel(MultipartBakedModel model, DashRegistry registry) {
		final Pair<List<MultipartModelSelector>, StateManager<Block, BlockState>> selectors = DashLoader.getVanillaData().getModelData(model);
		MultipartBakedModelAccessor access = ((MultipartBakedModelAccessor) model);
		List<Pair<Predicate<BlockState>, BakedModel>> accessComponents = access.getComponents();
		final int size = accessComponents.size();
		this.components = new Pointer2PointerMap(size);
		for (int i = 0; i < size; i++) {
			final BakedModel right = accessComponents.get(i).getRight();
			//TODO predicate registration
			components.put(registry.predicates.register(selectors.getKey().get(i), selectors.getValue()), registry.add(right));
		}
		final Map<BlockState, BitSet> stateCache = access.getStateCache();
		this.stateCache = DashHelper.convertMapToCollection(
				stateCache,
				new Pointer2ObjectMap<>(stateCache.size()),
				(entry) -> Pointer2ObjectMap.Entry.of(registry.add(entry.getKey()), entry.getValue().toByteArray()));
	}

	@Override
	public MultipartBakedModel toUndash(DashRegistry registry) {
		MultipartBakedModel model = UnsafeHelper.allocateInstance(cls);
		Map<BlockState, BitSet> stateCacheOut = new Object2ObjectOpenCustomHashMap<>(Util.identityHashStrategy());
		stateCache.forEach((entry) -> stateCacheOut.put(registry.get(entry.key), BitSet.valueOf(entry.value)));
		((MultipartBakedModelAccessor) model).setStateCache(stateCacheOut);
		toApply = model;
		return model;
	}

	@Override
	public void apply(DashRegistry registry) {
		List<Pair<Predicate<BlockState>, BakedModel>> componentsOut = new ArrayList<>();
		components.forEach((entry) -> componentsOut.add(Pair.of(registry.get(entry.key), registry.get(entry.value))));
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

	@Override
	public int getStage() {
		return 2;
	}
}
