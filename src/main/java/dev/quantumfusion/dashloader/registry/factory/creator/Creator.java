package dev.quantumfusion.dashloader.registry.factory.creator;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.RegistryWriter;

public interface Creator<R, D extends Dashable<R>> {
	D create(R raw, RegistryWriter writer) throws Throwable;
}
