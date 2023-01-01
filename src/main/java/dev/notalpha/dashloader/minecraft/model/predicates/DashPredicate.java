package dev.notalpha.dashloader.minecraft.model.predicates;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.Dashable;
import dev.notalpha.dashloader.registry.RegistryReader;
import net.minecraft.block.BlockState;

import java.util.function.Predicate;

@DashObject(Predicate.class)
public interface DashPredicate extends Dashable<Predicate<BlockState>> {
	Predicate<BlockState> export(RegistryReader exportHandler);
}
