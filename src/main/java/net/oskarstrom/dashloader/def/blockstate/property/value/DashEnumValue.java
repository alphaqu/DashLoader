package net.oskarstrom.dashloader.def.blockstate.property.value;

import net.oskarstrom.dashloader.def.api.DashObject;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.def.util.ClassHelper;

@DashObject(Enum.class)
public class DashEnumValue implements DashPropertyValue {
	@Serialize(order = 0)
	public final String value;

	@Serialize(order = 1)
	public final String enumClass;

	public DashEnumValue(@Deserialize("value") String value,
						 @Deserialize("enumClass") String enumClass) {
		this.value = value;
		this.enumClass = enumClass;
	}

	public DashEnumValue(Enum<?> enuum) {
		this(enuum.name(), enuum.getDeclaringClass().getName());
	}

	@Override
	public Enum<?> toUndash(DashRegistry registry) {
		return get();
	}


	public <T extends Enum<T>> T get() {
		final Class<T> enumClass = ClassHelper.castClass(ClassHelper.getClass(this.enumClass));
		return Enum.valueOf(enumClass, value);
	}
}
