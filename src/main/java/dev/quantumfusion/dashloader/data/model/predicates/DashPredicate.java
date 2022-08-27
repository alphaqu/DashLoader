package dev.quantumfusion.dashloader.data.model.predicates;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;

@DashObject(Predicate.class)
public interface DashPredicate extends Dashable<Predicate<BlockState>> {
	Predicate<BlockState> export(RegistryReader exportHandler);
}
