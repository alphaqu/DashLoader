package dev.quantumfusion.dashloader.def.data.blockstate.property.value;

public class DashPropertyValueManager {

	public static Object prepare(Comparable<?> comparable) {
		if (comparable instanceof Enum e ) {
			return new EnumValueWrapped(e);
		}
		return comparable;
	}
}
