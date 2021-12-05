package dev.quantumfusion.dashloader.def.util.mixins;

import net.minecraft.state.property.Property;

public interface StateDuck<O, S> {

	void setPropertiesMap(Property<?>[] propertiesMap);

	void setValuesMap(Comparable<?>[][] valuesMap);

	void setFastWithTable(Object[][] fastWithTable);
}
