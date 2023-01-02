package dev.notalpha.dashloader.client.model;

import dev.notalpha.dashloader.Cache;
import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.client.model.predicates.BooleanSelector;
import dev.notalpha.dashloader.io.data.collection.IntObjectList;
import dev.notalpha.dashloader.misc.UnsafeHelper;
import dev.notalpha.dashloader.mixin.accessor.ModelLoaderAccessor;
import dev.notalpha.dashloader.mixin.accessor.MultipartBakedModelAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.MultipartBakedModel;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class DashMultipartBakedModel implements DashObject<MultipartBakedModel> {
	public final List<Component> components;
	public final IntObjectList<byte[]> stateCache;

	public DashMultipartBakedModel(List<Component> components, IntObjectList<byte[]> stateCache) {
		this.components = components;
		this.stateCache = stateCache;
	}

	public DashMultipartBakedModel(MultipartBakedModel model, RegistryWriter writer) {
		var access = ((MultipartBakedModelAccessor) model);
		var accessComponents = access.getComponents();
		int size = accessComponents.size();
		this.components = new ArrayList<>();

		var selectors = ModelModule.MULTIPART_PREDICATES.get(Cache.Status.SAVE).get(model);

		for (int i = 0; i < size; i++) {
			BakedModel componentModel = accessComponents.get(i).getRight();
			MultipartModelSelector selector = selectors.getKey().get(i);
			Identifier componentIdentifier = ModelModule.getStateManagerIdentifier(selectors.getRight());
			this.components.add(new Component(
					writer.add(componentModel),
					writer.add(selector),
					writer.add(componentIdentifier)
			));
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

		List<Pair<Predicate<BlockState>, BakedModel>> componentsOut = new ArrayList<>(this.components.size());
		this.components.forEach(component -> {
			BakedModel compModel = reader.get(component.model);
			Identifier compIdentifier = reader.get(component.identifier);
			MultipartModelSelector compSelector = reader.get(component.selector);
			Predicate<BlockState> predicate = compSelector.getPredicate(ModelModule.getStateManager(compIdentifier));
			componentsOut.add(Pair.of(predicate, compModel));
		});

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

	public static final class Component {
		public final int model;
		public final int selector;
		public final int identifier;


		public Component(int model, int selector, int identifier) {
			this.model = model;
			this.selector = selector;
			this.identifier = identifier;
		}
	}
}

