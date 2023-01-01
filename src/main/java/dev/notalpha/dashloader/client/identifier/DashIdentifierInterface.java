package dev.notalpha.dashloader.client.identifier;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.Exportable;
import net.minecraft.util.Identifier;

@DashObject(Identifier.class)
public interface DashIdentifierInterface extends Exportable<Identifier> {
}
