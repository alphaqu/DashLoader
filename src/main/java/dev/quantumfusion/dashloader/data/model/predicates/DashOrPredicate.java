package dev.quantumfusion.dashloader.data.model.predicates;

import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.mixin.accessor.OrMultipartModelSelectorAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


@DashObject(OrMultipartModelSelector.class)
public final class DashOrPredicate implements DashPredicate {
	public final List<Integer> selectors;
	public final int identifier;

	public DashOrPredicate(List<Integer> selectors, int identifier) {
		this.selectors = selectors;
		this.identifier = identifier;
	}

	public DashOrPredicate(OrMultipartModelSelector selector, RegistryWriter writer) {
		OrMultipartModelSelectorAccessor access = ((OrMultipartModelSelectorAccessor) selector);
		this.identifier = writer.add(DashSimplePredicate.getStateManagerIdentifier(selector));

		this.selectors = new ArrayList<>();
		for (MultipartModelSelector accessSelector : access.getSelectors()) {
			System.out.println("OR adding " + accessSelector.getClass().getSimpleName());
			this.selectors.add(writer.add(accessSelector));
		}

	}

	@Override
	public Predicate<BlockState> export(RegistryReader handler) {
		final ArrayList<MultipartModelSelector> selectors = new ArrayList<>();
		for (Integer accessSelector : this.selectors) {
			Predicate<BlockState> export = handler.get(accessSelector);
			selectors.add((stateStateManager) -> export);
		}

		return new OrMultipartModelSelector(selectors).getPredicate(DashSimplePredicate.getStateManager(handler.get(this.identifier)));
	}
}
