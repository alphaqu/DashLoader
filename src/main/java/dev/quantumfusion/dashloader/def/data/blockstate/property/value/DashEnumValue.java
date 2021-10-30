package dev.quantumfusion.dashloader.def.data.blockstate.property.value;

import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.def.util.ClassHelper;
import dev.quantumfusion.hyphen.scan.annotations.Data;

@Data
@DashObject(EnumValueWrapped.class)
public record DashEnumValue(String value, String enumClass) implements DashPropertyValue {

	public DashEnumValue(EnumValueWrapped enuum) {
		this(enuum.e().name(), enuum.e().getDeclaringClass().getName());
	}

	@Override
	public Enum<?> export(DashRegistryReader exportHandler) {
		return get();
	}


	public <T extends Enum<T>> T get() {
		final Class<T> enumClass = ClassHelper.castClass(ClassHelper.getClass(this.enumClass));
		return Enum.valueOf(enumClass, value);
	}
}
