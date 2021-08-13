package net.oskarstrom.dashloader.def.blockstate.property;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.util.ClassHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@DashObject(EnumProperty.class)
public class DashEnumProperty implements DashProperty {
	@Serialize(order = 0)
	public final List<String> values;
	@Serialize(order = 1)
	public final String className;
	@Serialize(order = 2)
	public final String name;

	public Class<?> type;

	public DashEnumProperty(@Deserialize("values") List<String> values,
							@Deserialize("className") String className,
							@Deserialize("name") String name) {
		this.values = values;
		this.className = className;
		this.name = name;
	}

	public DashEnumProperty(EnumProperty property) {
		className = property.getType().getName();
		name = property.getName();
		values = new ArrayList<>();
		property.getValues().forEach(valuee -> values.add(valuee.toString()));
	}

	@Override
	public EnumProperty<?> toUndash(DashRegistry registry) {
		return get();
	}

	public <T extends Enum<T> & StringIdentifiable> EnumProperty<T> get() {
		type = ClassHelper.getClass(className);
		return EnumProperty.of(name, (Class<T>) type, Arrays.asList(((Class<T>) type).getEnumConstants()));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DashEnumProperty that = (DashEnumProperty) o;
		return Objects.equals(values, that.values) && Objects.equals(className, that.className) && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(values, className, name);
	}
}
