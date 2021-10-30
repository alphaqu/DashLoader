package dev.quantumfusion.dashloader.def.data.model.predicates;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import net.minecraft.block.BlockState;

import java.util.function.Predicate;

public interface DashPredicate extends Dashable<Predicate<BlockState>> {
	Predicate<BlockState> export(DashRegistryReader exportHandler);
}
