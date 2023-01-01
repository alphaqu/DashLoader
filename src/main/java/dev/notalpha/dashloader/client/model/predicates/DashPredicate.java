package dev.notalpha.dashloader.client.model.predicates;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.Exportable;
import dev.notalpha.dashloader.registry.RegistryReader;
import net.minecraft.block.BlockState;

import java.util.function.Predicate;

@DashObject(Predicate.class)
public interface DashPredicate extends Exportable<Predicate<BlockState>> {
	Predicate<BlockState> export(RegistryReader exportHandler);
}
