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
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.SimpleMultipartModelSelector;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;


public final class DashSimplePredicate implements DashObject<SimpleMultipartModelSelector> {
	public final String key;
	public final String valueString;

	public DashSimplePredicate(String key, String valueString) {
		this.key = key;
		this.valueString = valueString;
	}

	public DashSimplePredicate(SimpleMultipartModelSelector simpleMultipartModelSelector) {
		var access = ((SimpleMultipartModelSelectorAccessor) simpleMultipartModelSelector);
		this.key = access.getKey();
		this.valueString = access.getValueString();
	}

	@Override
	public SimpleMultipartModelSelector export(RegistryReader handler) {
		return new SimpleMultipartModelSelector(this.key, this.valueString);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashSimplePredicate that = (DashSimplePredicate) o;

		if (!key.equals(that.key)) return false;
		return valueString.equals(that.valueString);
	}

	@Override
	public int hashCode() {
		int result = key.hashCode();
		result = 31 * result + valueString.hashCode();
		return result;
	}
}
