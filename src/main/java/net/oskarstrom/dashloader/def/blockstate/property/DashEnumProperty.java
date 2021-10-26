package net.oskarstrom.dashloader.def.blockstate.property;

import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.util.ClassHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@DashObject(EnumProperty.class)
public class DashEnumProperty implements DashProperty {
	public final List<String> values;
	public final String className;
	public final String name;
	public transient Class<?> type;

	public DashEnumProperty(
			List<String> values,
			String className,
			String name) {
		this.values = values;
		this.className = className;
		this.name = name;
	}


	public DashEnumProperty(EnumProperty property) {
		this(getValues(property), property.getType().getName(), property.getName());
	}

	@NotNull
	private static List<String> getValues(EnumProperty property) {
		final List<String> values = new ArrayList<>();
		property.getValues().forEach(valuee -> values.add(valuee.toString()));
		return values;
	}

	@Override
	public EnumProperty<?> toUndash(DashExportHandler exportHandler) {
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
