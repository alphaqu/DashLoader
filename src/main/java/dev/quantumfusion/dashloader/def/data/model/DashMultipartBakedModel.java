package dev.quantumfusion.dashloader.def.data.model;

import dev.quantumfusion.dashloader.core.api.annotation.DashDependencies;
import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.common.IntIntList;
import dev.quantumfusion.dashloader.core.common.IntObjectList;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.model.predicates.DashAndPredicate;
import dev.quantumfusion.dashloader.def.mixin.accessor.MultipartBakedModelAccessor;
import dev.quantumfusion.dashloader.def.util.RegistryUtil;
import dev.quantumfusion.dashloader.def.util.UnsafeHelper;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.MultipartBakedModel;
import net.minecraft.util.Util;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Data
@DashObject(MultipartBakedModel.class)
@DashDependencies({DashWeightedBakedModel.class, DashAndPredicate.class})
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

	public DashMultipartBakedModel(MultipartBakedModel model, DashRegistryWriter writer) {
		var selectors = DashLoader.getData().getWriteContextData().multipartPredicates.get(model);
		var access = ((MultipartBakedModelAccessor) model);
		var accessComponents = access.getComponents();
		int size = accessComponents.size();

		this.components = new IntIntList(new ArrayList<>(size));
		for (int i = 0; i < size; i++) {
			var right = accessComponents.get(i).getRight();
			var selector = selectors.getKey().get(i);
			DashLoader.getData().getWriteContextData().stateManagers.put(selector, selectors.getValue());
			components.put(writer.add(RegistryUtil.preparePredicate(selector)), writer.add(right));
		}
		this.stateCache = new IntObjectList<>();
		access.getStateCache().forEach((blockState, bitSet) -> this.stateCache.put(writer.add(blockState), bitSet.toByteArray()));
	}

	@Override
	public MultipartBakedModel export(DashRegistryReader reader) {
		MultipartBakedModel model = UnsafeHelper.allocateInstance(cls);

		Map<BlockState, BitSet> stateCacheOut = new Object2ObjectOpenCustomHashMap<>(Util.identityHashStrategy());
		stateCache.forEach((blockstate, bitSet) -> stateCacheOut.put(reader.get(blockstate), BitSet.valueOf(bitSet)));
		((MultipartBakedModelAccessor) model).setStateCache(stateCacheOut);

		toApply = model;
		return model;
	}

	@Override
	public void apply(DashRegistryReader handler) {
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

