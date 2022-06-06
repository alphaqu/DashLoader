package dev.quantumfusion.dashloader.data;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.api.DashObject;
import net.minecraft.util.Identifier;

@DashObject(Identifier.class)
public interface DashIdentifierInterface extends Dashable<Identifier> {
}
