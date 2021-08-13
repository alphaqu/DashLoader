package net.oskarstrom.dashloader.def.api;

import net.oskarstrom.dashloader.def.blockstate.property.DashProperty;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashPropertyValue;
import net.oskarstrom.dashloader.def.font.DashFont;
import net.oskarstrom.dashloader.def.model.DashModel;
import net.oskarstrom.dashloader.def.model.predicates.DashPredicate;

public enum DashDataType {
	MODEL("Model", "models", DashModel.class, true),
	PROPERTY("Property", "properties", DashProperty.class, true),
	PROPERTY_VALUE("Property Value", "values", DashPropertyValue.class, true),
	PREDICATE("Predicate", "predicates", DashPredicate.class, true),
	FONT("Font", "fonts", DashFont.class, true),
	DATA("Data", "data", DashDataClass.class, false),
	DEFAULT("something went wrong", "omegakek", null, true);

	public String name;
	//serializers
	public String internalName;
	public Class<?> factoryInterface;
	public boolean requiresTargetObject;


	DashDataType(String type, String internalName, Class<?> factoryInterface, boolean requiresTargetObject) {
		this.name = type;
		this.internalName = internalName;
		this.factoryInterface = factoryInterface;
		this.requiresTargetObject = requiresTargetObject;
	}

	@Override
	public String toString() {
		return internalName;
	}
}
