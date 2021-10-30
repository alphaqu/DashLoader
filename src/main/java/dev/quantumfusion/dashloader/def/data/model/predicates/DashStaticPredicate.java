package dev.quantumfusion.dashloader.def.data.model.predicates;

import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.def.util.BooleanSelector;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.block.BlockState;

import java.util.function.Predicate;

@Data
@DashObject(BooleanSelector.class)
public class DashStaticPredicate implements DashPredicate {
	public final boolean value;

	public DashStaticPredicate(boolean value) {
		this.value = value;
	}


	public DashStaticPredicate(BooleanSelector multipartModelSelector) {
		value = multipartModelSelector.selector;
	}

	@Override
	public Predicate<BlockState> export(DashRegistryReader exportHandler) {
		return (blockState) -> value;
	}

}
