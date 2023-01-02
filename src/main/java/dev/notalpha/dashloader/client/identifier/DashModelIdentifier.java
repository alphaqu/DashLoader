package dev.notalpha.dashloader.client.identifier;


import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.mixin.accessor.ModelIdentifierAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.quantumfusion.hyphen.scan.annotations.DataFixedArraySize;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

public final class DashModelIdentifier implements DashObject<ModelIdentifier> {
	public final String namespace;
	public final String path;
	public final String variant;

	public DashModelIdentifier(ModelIdentifier identifier) {
		this.namespace = identifier.getNamespace();
		this.path = identifier.getPath();
		this.variant = identifier.getVariant();
	}

	public DashModelIdentifier(String namespace, String path, String variant) {
		this.namespace = namespace;
		this.path = path;
		this.variant = variant;
	}

	@Override
	public ModelIdentifier export(RegistryReader exportHandler) {
		return ModelIdentifierAccessor.init(namespace, path, variant, null);
	}
}
