package dev.quantumfusion.dashloader.minecraft.model.predicates;

import dev.quantumfusion.dashloader.api.Dashable;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import net.minecraft.block.BlockState;

import java.util.function.Predicate;

@DashObject(Predicate.class)
public interface DashPredicate extends Dashable<Predicate<BlockState>> {
	Predicate<BlockState> export(RegistryReader exportHandler);
}
