package dev.notalpha.dashloader.minecraft.model;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.registry.RegistryWriter;
import dev.notalpha.dashloader.util.RegistryUtil;
import dev.notalpha.dashloader.util.UnsafeHelper;
import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.io.data.collection.IntIntList;
import dev.notalpha.dashloader.io.data.collection.IntObjectList;
import dev.notalpha.dashloader.mixin.accessor.MultipartBakedModelAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.MultipartBakedModel;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@DashObject(MultipartBakedModel.class)
public class DashMultipartBakedModel implements DashModel {
	//identifier baked model
	public final IntIntList components;
	//public final boolean ambientOcclusion;
	//public final boolean depthGui;
	//public final boolean sideLit;
	//public final int sprite;
	//@DataNullable
	//public final DashModelTransformation transformations;
	// a bit too expensive to compute
	//public final DashModelOverrideList itemPropertyOverrides;

	public final IntObjectList<byte[]> stateCache;

	public DashMultipartBakedModel(IntIntList components, IntObjectList<byte[]> stateCache) {
		this.components = components;
		this.stateCache = stateCache;
	}

	public DashMultipartBakedModel(MultipartBakedModel model, RegistryWriter writer) {
		var access = ((MultipartBakedModelAccessor) model);
		var accessComponents = access.getComponents();
		int size = accessComponents.size();
		this.components = new IntIntList(new ArrayList<>(size));

		var stateManagers = ModelCacheHandler.STATE_MANAGERS.get(DashLoader.Status.SAVE);
		var selectors = ModelCacheHandler.MULTIPART_PREDICATES.get(DashLoader.Status.SAVE).get(model);

		for (int i = 0; i < size; i++) {
			var right = accessComponents.get(i).getRight();
			var selector = selectors.getKey().get(i);
			stateManagers.put(selector, selectors.getValue());
			this.components.put(writer.add(RegistryUtil.preparePredicate(selector)), writer.add(right));
		}

		this.stateCache = new IntObjectList<>();
		access.getStateCache().forEach((blockState, bitSet) -> this.stateCache.put(writer.add(blockState), bitSet.toByteArray()));
	}

	@Override
	public MultipartBakedModel export(RegistryReader reader) {
		MultipartBakedModel model = UnsafeHelper.allocateInstance(MultipartBakedModel.class);
		var access = (MultipartBakedModelAccessor) model;

		Map<BlockState, BitSet> stateCacheOut = new Reference2ObjectOpenHashMap<>(this.stateCache.list().size());
		this.stateCache.forEach((blockstate, bitSet) -> stateCacheOut.put(reader.get(blockstate), BitSet.valueOf(bitSet)));
		access.setStateCache(stateCacheOut);

		List<Pair<Predicate<BlockState>, BakedModel>> componentsOut = new ArrayList<>(this.components.list().size());
		this.components.forEach((key, value) -> componentsOut.add(Pair.of(reader.get(key), reader.get(value))));

		var bakedModel = componentsOut.iterator().next().getRight();
		access.setComponents(componentsOut);
		access.setAmbientOcclusion(bakedModel.useAmbientOcclusion());
		access.setDepthGui(bakedModel.hasDepth());
		access.setSideLit(bakedModel.isSideLit());
		access.setSprite(bakedModel.getParticleSprite());
		access.setTransformations(bakedModel.getTransformation());
		access.setItemPropertyOverrides(bakedModel.getOverrides());
		return model;
	}

	///@Override
	///public void postExport(RegistryReader reader) {
	///	var access = ((MultipartBakedModelAccessor) this.toApply);
///
	///	List<Pair<Predicate<BlockState>, BakedModel>> componentsOut = new ArrayList<>();
	///	this.components.forEach((key, value) -> componentsOut.add(Pair.of(reader.get(key), reader.get(value))));
///
	///	var bakedModel = componentsOut.iterator().next().getRight();
	///	access.setComponents(componentsOut);
	///	access.setAmbientOcclusion(bakedModel.useAmbientOcclusion());
	///	access.setDepthGui(bakedModel.hasDepth());
	///	access.setSideLit(bakedModel.isSideLit());
	///	access.setSprite(bakedModel.getParticleSprite());
	///	access.setTransformations(bakedModel.getTransformation());
	///	access.setItemPropertyOverrides(bakedModel.getOverrides());
	///}
}

