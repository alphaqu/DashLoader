package dev.quantumfusion.dashloader.data;

import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.mixin.accessor.IdentifierAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.hyphen.scan.annotations.DataFixedArraySize;
import net.minecraft.util.Identifier;

@DashObject(Identifier.class)
public class DashIdentifier implements DashIdentifierInterface {
	public final String @DataFixedArraySize(2) [] strings;

	public DashIdentifier(String[] strings) {
		this.strings = strings;
	}

	public DashIdentifier(Identifier identifier) {
		this.strings = new String[2];
		this.strings[0] = identifier.getNamespace();
		this.strings[1] = identifier.getPath();
	}

	@Override
	public Identifier export(RegistryReader exportHandler) {
		return IdentifierAccessor.init(this.strings);
	}
}
