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

import java.util.*;
import java.util.function.Predicate;

public class DashMultipartBakedModel implements DashObject<MultipartBakedModel> {
	public final List<Component> components;

	public DashMultipartBakedModel(List<Component> components) {
		this.components = components;
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
	}

	@Override
	public MultipartBakedModel export(RegistryReader reader) {
		List<Pair<Predicate<BlockState>, BakedModel>> componentsOut = new ArrayList<>(this.components.size());
		this.components.forEach(component -> {
			BakedModel compModel = reader.get(component.model);
			Identifier compIdentifier = reader.get(component.identifier);
			MultipartModelSelector compSelector = reader.get(component.selector);
			Predicate<BlockState> predicate = compSelector.getPredicate(ModelModule.getStateManager(compIdentifier));
			componentsOut.add(Pair.of(predicate, compModel));
		});

		MultipartBakedModel multipartBakedModel = new MultipartBakedModel(componentsOut);
		MultipartBakedModelAccessor access = (MultipartBakedModelAccessor) multipartBakedModel;
		// Fixes race condition which strangely does not happen in vanilla a ton?
		access.setStateCache(Collections.synchronizedMap(access.getStateCache()));
		return multipartBakedModel;
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

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Component component = (Component) o;

			if (model != component.model) return false;
			if (selector != component.selector) return false;
			return identifier == component.identifier;
		}

		@Override
		public int hashCode() {
			int result = model;
			result = 31 * result + selector;
			result = 31 * result + identifier;
			return result;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashMultipartBakedModel that = (DashMultipartBakedModel) o;

		return components.equals(that.components);
	}

	@Override
	public int hashCode() {
		return components.hashCode();
	}
}

