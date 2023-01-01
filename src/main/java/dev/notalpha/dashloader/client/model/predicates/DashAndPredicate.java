package dev.notalpha.dashloader.client.model.predicates;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.mixin.accessor.AndMultipartModelSelectorAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.AndMultipartModelSelector;
import net.minecraft.client.render.model.json.MultipartModelSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@DashObject(AndMultipartModelSelector.class)
public final class DashAndPredicate implements DashPredicate {
	public final List<Integer> selectors;
	public final int identifier;

	public DashAndPredicate(List<Integer> selectors, int identifier) {
		this.selectors = selectors;
		this.identifier = identifier;
	}

	public DashAndPredicate(AndMultipartModelSelector selector, RegistryWriter writer) {
		AndMultipartModelSelectorAccessor access = ((AndMultipartModelSelectorAccessor) selector);
		this.identifier = writer.add(DashSimplePredicate.getStateManagerIdentifier(selector));

		this.selectors = new ArrayList<>();
		for (MultipartModelSelector accessSelector : access.getSelectors()) {
			this.selectors.add(writer.add(accessSelector));
		}
	}

	@Override
	public Predicate<BlockState> export(RegistryReader handler) {
		final ArrayList<MultipartModelSelector> selectors = new ArrayList<>();
		for (Integer accessSelector : this.selectors) {
			Predicate<BlockState> value = handler.get(accessSelector);
			selectors.add((stateStateManager) -> value);
		}

		return new AndMultipartModelSelector(selectors).getPredicate(DashSimplePredicate.getStateManager(handler.get(this.identifier)));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashAndPredicate that = (DashAndPredicate) o;

		if (identifier != that.identifier) return false;
		return selectors.equals(that.selectors);
	}

	@Override
	public int hashCode() {
		int result = selectors.hashCode();
		result = 31 * result + identifier;
		return result;
	}
}
