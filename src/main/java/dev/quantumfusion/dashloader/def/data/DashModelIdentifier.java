package dev.quantumfusion.dashloader.def.data;


import dev.quantumfusion.dashloader.def.mixin.accessor.ModelIdentifierAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeFixedSize;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;


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
	public Identifier toUndash(DashExportHandler exportHandler) {
		return ModelIdentifierAccessor.init(strings);
	}
}
