package net.oskarstrom.dashloader.def.common;


import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeFixedSize;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.def.mixin.accessor.ModelIdentifierAccessor;


public class DashModelIdentifier implements DashIdentifierInterface {
	@Serialize(order = 0)
	public final String[] strings;

	public DashModelIdentifier(@Deserialize("strings") String[] strings) {
		this.strings = strings;
	}

	public DashModelIdentifier(ModelIdentifier identifier) {
		strings = new String[3];
		strings[0] = identifier.getNamespace();
		strings[1] = identifier.getPath();
		strings[2] = identifier.getVariant();
	}

	@Override
	public Identifier toUndash(DashRegistry registry) {
		return ModelIdentifierAccessor.init(strings);
	}
}
