package dev.quantumfusion.dashloader.def.data.model.predicates;

import dev.quantumfusion.dashloader.core.api.annotation.DashDependencies;
import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.mixin.accessor.OrMultipartModelSelectorAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


@Data
@DashObject(OrMultipartModelSelector.class)
@DashDependencies(DashSimplePredicate.class)
public class DashOrPredicate implements DashPredicate {
	public final List<DashPredicate> selectors;

	public DashOrPredicate(List<DashPredicate> selectors) {
		this.selectors = selectors;
	}

	public DashOrPredicate(OrMultipartModelSelector selector, DashRegistryWriter writer) {
		OrMultipartModelSelectorAccessor access = ((OrMultipartModelSelectorAccessor) selector);
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
				if (blockStatePredicate.test(blockState)) return true;
			return false;
		};
	}
}
