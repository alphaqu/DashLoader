package dev.notalpha.dashloader.client.identifier;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.mixin.accessor.IdentifierAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.quantumfusion.hyphen.scan.annotations.DataFixedArraySize;
import net.minecraft.util.Identifier;

public final class DashIdentifier implements DashObject<Identifier> {
	public final String namespace;
	public final String path;

	public DashIdentifier(String namespace, String path) {
		this.namespace = namespace;
		this.path = path;
	}

	public DashIdentifier(Identifier identifier) {
		this.namespace = identifier.getNamespace();
		this.path = identifier.getPath();
	}

	@Override
	public Identifier export(RegistryReader exportHandler) {
		return IdentifierAccessor.init(this.namespace, this.path, null);
	}
}
