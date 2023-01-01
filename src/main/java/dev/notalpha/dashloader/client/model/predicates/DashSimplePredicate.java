package dev.notalpha.dashloader.client.model.predicates;

import dev.notalpha.dashloader.Cache;
import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.client.blockstate.DashBlockState;
import dev.notalpha.dashloader.client.model.ModelModule;
import dev.notalpha.dashloader.mixin.accessor.ModelLoaderAccessor;
import dev.notalpha.dashloader.mixin.accessor.SimpleMultipartModelSelectorAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.SimpleMultipartModelSelector;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;


@DashObject(SimpleMultipartModelSelector.class)
public final class DashSimplePredicate implements DashPredicate {
	public final String key;
	public final String valueString;
	public final int identifier;

	public DashSimplePredicate(String key, String valueString, int identifier) {
		this.key = key;
		this.valueString = valueString;
		this.identifier = identifier;
	}

	public DashSimplePredicate(SimpleMultipartModelSelector simpleMultipartModelSelector, RegistryWriter writer) {
		var access = ((SimpleMultipartModelSelectorAccessor) simpleMultipartModelSelector);
		this.identifier = writer.add(getStateManagerIdentifier(simpleMultipartModelSelector));
		this.key = access.getKey();
		this.valueString = access.getValueString();
	}

	@Override
	public Predicate<BlockState> export(RegistryReader handler) {
		return new SimpleMultipartModelSelector(this.key, this.valueString).getPredicate(getStateManager(handler.get(this.identifier)));
	}

	@NotNull
	public static Identifier getStateManagerIdentifier(MultipartModelSelector multipartModelSelector) {
		StateManager<Block, BlockState> stateManager = ModelModule.STATE_MANAGERS.get(Cache.Status.SAVE).get(multipartModelSelector);
		Identifier identifier;
		if (stateManager == ModelLoaderAccessor.getTheItemFrameThing()) {
			identifier = DashBlockState.ITEM_FRAME;
		} else {
			identifier = Registries.BLOCK.getId(stateManager.getOwner());
		}
		return identifier;
	}

	public static StateManager<Block, BlockState> getStateManager(Identifier identifier) {
		if (identifier.equals(DashBlockState.ITEM_FRAME)) {
			return ModelLoaderAccessor.getTheItemFrameThing();
		} else {
			return Registries.BLOCK.get(identifier).getStateManager();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashSimplePredicate that = (DashSimplePredicate) o;

		if (identifier != that.identifier) return false;
		if (!key.equals(that.key)) return false;
		return valueString.equals(that.valueString);
	}

	@Override
	public int hashCode() {
		int result = key.hashCode();
		result = 31 * result + valueString.hashCode();
		result = 31 * result + identifier;
		return result;
	}
}
