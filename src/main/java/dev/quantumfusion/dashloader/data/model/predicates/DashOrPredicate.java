package dev.quantumfusion.dashloader.data.model.predicates;

import dev.quantumfusion.dashloader.api.DashDependencies;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.mixin.accessor.OrMultipartModelSelectorAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;


@DashObject(OrMultipartModelSelector.class)
@DashDependencies(DashSimplePredicate.class)
public final class DashOrPredicate implements DashPredicate {
	public final List<DashPredicate> selectors;
	public final int identifier;

	public DashOrPredicate(List<DashPredicate> selectors, int identifier) {
		this.selectors = selectors;
		this.identifier = identifier;
	}

	public DashOrPredicate(OrMultipartModelSelector selector, RegistryWriter writer) {
		OrMultipartModelSelectorAccessor access = ((OrMultipartModelSelectorAccessor) selector);
		this.identifier = writer.add(DashSimplePredicate.getStateManagerIdentifier(selector));

		this.selectors = new ArrayList<>();
		for (MultipartModelSelector accessSelector : access.getSelectors()) {
			this.selectors.add(DashPredicateCreator.create(accessSelector, writer));
		}

	}

	@Override
	public Predicate<BlockState> export(RegistryReader handler) {
		final ArrayList<MultipartModelSelector> selectors = new ArrayList<>();
		for (DashPredicate accessSelector : this.selectors) {
			final Predicate<BlockState> export = accessSelector.export(handler);
			selectors.add((stateStateManager) -> export);
		}

		return new OrMultipartModelSelector(selectors).getPredicate(DashSimplePredicate.getStateManager(handler.get(this.identifier)));
	}
}
