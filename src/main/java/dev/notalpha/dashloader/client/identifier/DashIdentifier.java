package dev.notalpha.dashloader.client.identifier;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.mixin.accessor.IdentifierAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.quantumfusion.hyphen.scan.annotations.DataFixedArraySize;
import net.minecraft.util.Identifier;

public final class DashIdentifier implements DashObject<Identifier> {
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
