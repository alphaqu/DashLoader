package dev.quantumfusion.dashloader.def.data.model.predicates;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import net.minecraft.block.BlockState;

import java.util.function.Predicate;

@DashObject(Predicate.class)
public interface DashPredicate extends Dashable<Predicate<BlockState>> {
	Predicate<BlockState> export(RegistryReader exportHandler);
}
