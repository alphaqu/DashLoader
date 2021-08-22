package net.oskarstrom.dashloader.def.common;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeFixedSize;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.def.mixin.accessor.IdentifierAccessor;

public class DashIdentifier implements DashIdentifierInterface {
	@Serialize(order = 0)
	@SerializeFixedSize(2)
	public final String[] strings;

	public DashIdentifier(@Deserialize("strings") String[] strings) {
		this.strings = strings;
	}

	public DashIdentifier(Identifier identifier) {
		strings = new String[2];
		strings[0] = identifier.getNamespace();
		strings[1] = identifier.getPath();
	}

	@Override
	public Identifier toUndash(DashRegistry registry) {
		return IdentifierAccessor.init(strings);
	}
}
