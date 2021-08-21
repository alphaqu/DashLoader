package net.oskarstrom.dashloader.def.model.predicates;

import net.minecraft.block.BlockState;
import net.oskarstrom.dashloader.api.Dashable;
import net.oskarstrom.dashloader.api.registry.DashRegistry;

import java.util.function.Predicate;

public interface DashPredicate extends Dashable<Predicate<BlockState>> {
	Predicate<BlockState> toUndash(DashRegistry registry);

}
