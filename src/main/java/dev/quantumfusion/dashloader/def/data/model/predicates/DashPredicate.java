package dev.quantumfusion.dashloader.def.data.model.predicates;

import net.minecraft.block.BlockState;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;

import java.util.function.Predicate;

public interface DashPredicate extends Dashable<Predicate<BlockState>> {
	Predicate<BlockState> toUndash(DashExportHandler exportHandler);

}
