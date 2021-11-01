package dev.quantumfusion.dashloader.def.data.model.predicates;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;

import java.util.function.Predicate;

@DashObject(Predicate.class)
public interface DashPredicate extends Dashable<Predicate<BlockState>> {
	Predicate<BlockState> export(DashRegistryReader exportHandler);
}
