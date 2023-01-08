package dev.notalpha.dashloader.misc.duck;

import net.minecraft.state.property.Property;

public interface StateDuck<O, S> {

	void setPropertiesMap(Property<?>[] propertiesMap);

	void setValuesMap(Comparable<?>[][] valuesMap);

	void setFastWithTable(Object[][] fastWithTable);
}
