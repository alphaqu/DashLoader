package dev.quantumfusion.dashloader.def.data.model.predicates;

import dev.quantumfusion.dashloader.core.api.annotation.DashDependencies;
import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
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

	public DashAndPredicate(List<DashPredicate> selectors) {
		this.selectors = selectors;
	}

	public DashAndPredicate(AndMultipartModelSelector selector, DashRegistryWriter writer) {
		AndMultipartModelSelectorAccessor access = ((AndMultipartModelSelectorAccessor) selector);
		selectors = new ArrayList<>();
		for (MultipartModelSelector accessSelector : access.getSelectors())
			selectors.add(DashPredicateCreator.create(accessSelector, writer));

	}

	@Override
	public Predicate<BlockState> export(DashRegistryReader handler) {
		List<Predicate<BlockState>> selectorsOut = new ArrayList<>();
		for (DashPredicate accessSelector : selectors)
			selectorsOut.add(accessSelector.export(handler));

		return (blockState) -> {
			for (Predicate<BlockState> blockStatePredicate : selectorsOut)
				if (!blockStatePredicate.test(blockState)) return false;
			return true;
		};
	}
}
