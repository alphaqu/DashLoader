package dev.quantumfusion.dashloader.def.data.model.predicates;

import dev.quantumfusion.dashloader.core.api.DashDependencies;
import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.mixin.accessor.AndMultipartModelSelectorAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.AndMultipartModelSelector;
import net.minecraft.client.render.model.json.MultipartModelSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Data
@DashObject(AndMultipartModelSelector.class)
@DashDependencies(DashSimplePredicate.class)
public class DashAndPredicate implements DashPredicate {
	public final List<DashPredicate> selectors;
	public final int identifier;

	public DashAndPredicate(List<DashPredicate> selectors, int identifier) {
		this.selectors = selectors;
		this.identifier = identifier;
	}

	public DashAndPredicate(AndMultipartModelSelector selector, RegistryWriter writer) {
		AndMultipartModelSelectorAccessor access = ((AndMultipartModelSelectorAccessor) selector);
		this.identifier = writer.add(DashSimplePredicate.getStateManagerIdentifier(selector));

		selectors = new ArrayList<>();
		for (MultipartModelSelector accessSelector : access.getSelectors())
			selectors.add(DashPredicateCreator.create(accessSelector, writer));

	}

	@Override
	public Predicate<BlockState> export(RegistryReader handler) {
		final ArrayList<MultipartModelSelector> selectors = new ArrayList<>();
		for (DashPredicate accessSelector : this.selectors){
			final Predicate<BlockState> export = accessSelector.export(handler);
			selectors.add((stateStateManager) -> export);
		}

		return new AndMultipartModelSelector(selectors).getPredicate(DashSimplePredicate.getStateManager(handler.get(identifier)));
	}
}
