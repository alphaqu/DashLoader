package dev.notalpha.dashloader.client.identifier;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.mixin.accessor.IdentifierAccessor;
import net.minecraft.resources.ResourceLocation;

public final class DashIdentifier implements DashObject<ResourceLocation, ResourceLocation> {
	public final String namespace;
	public final String path;

	public DashIdentifier(String namespace, String path) {
		this.namespace = namespace;
		this.path = path;
	}

	public DashIdentifier(ResourceLocation identifier) {
		this.namespace = identifier.getNamespace();
		this.path = identifier.getPath();
	}

	@Override
	public ResourceLocation export(RegistryReader exportHandler) {
		return IdentifierAccessor.init(this.namespace, this.path);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashIdentifier that = (DashIdentifier) o;

		if (!namespace.equals(that.namespace)) return false;
		return path.equals(that.path);
	}

	@Override
	public int hashCode() {
		int result = namespace.hashCode();
		result = 31 * result + path.hashCode();
		return result;
	}
}
