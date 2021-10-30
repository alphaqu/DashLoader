package dev.quantumfusion.dashloader.def.data;


import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.def.mixin.accessor.ModelIdentifierAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

@Data
@DashObject(ModelIdentifier.class)
public class DashModelIdentifier implements DashIdentifierInterface {
	public final String[] strings;

	public DashModelIdentifier(String[] strings) {
		this.strings = strings;
	}

	public DashModelIdentifier(ModelIdentifier identifier) {
		strings = new String[3];
		strings[0] = identifier.getNamespace();
		strings[1] = identifier.getPath();
		strings[2] = identifier.getVariant();
	}

	@Override
	public Identifier export(DashRegistryReader exportHandler) {
		return ModelIdentifierAccessor.init(strings);
	}
}
