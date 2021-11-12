package dev.quantumfusion.dashloader.def.data;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.api.DashObject;
import net.minecraft.util.Identifier;

import java.util.function.Predicate;

@DashObject(Identifier.class)
public interface DashIdentifierInterface extends Dashable<Identifier> {
}
