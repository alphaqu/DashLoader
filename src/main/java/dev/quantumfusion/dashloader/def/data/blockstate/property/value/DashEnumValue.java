package dev.quantumfusion.dashloader.def.data.blockstate.property.value;

import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.annotations.DashObject;
import dev.quantumfusion.dashloader.def.util.ClassHelper;

@DashObject(Enum.class)
public record DashEnumValue(String value, String enumClass) implements DashPropertyValue {

	public DashEnumValue(Enum<?> enuum) {
		this(enuum.name(), enuum.getDeclaringClass().getName());
	}

	@Override
	public Enum<?> toUndash(DashExportHandler exportHandler) {
		return get();
	}


	public <T extends Enum<T>> T get() {
		final Class<T> enumClass = ClassHelper.castClass(ClassHelper.getClass(this.enumClass));
		return Enum.valueOf(enumClass, value);
	}
}
