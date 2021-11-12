package dev.quantumfusion.dashloader.def.data;

import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.def.mixin.accessor.IdentifierAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataFixedArraySize;
import net.minecraft.util.Identifier;

@Data
@DashObject(Identifier.class)
public class DashIdentifier implements DashIdentifierInterface {
	public final String @DataFixedArraySize(2) [] strings;

	public DashIdentifier(String[] strings) {
		this.strings = strings;
	}

	public DashIdentifier(Identifier identifier) {
		strings = new String[2];
		strings[0] = identifier.getNamespace();
		strings[1] = identifier.getPath();
	}

	@Override
	public Identifier export(RegistryReader exportHandler) {
		return IdentifierAccessor.init(strings);
	}
}
