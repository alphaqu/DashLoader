package net.oskarstrom.dashloader.def.api;

import net.oskarstrom.dashloader.def.blockstate.DashBlockState;
import net.oskarstrom.dashloader.def.blockstate.property.DashProperty;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashPropertyValue;
import net.oskarstrom.dashloader.def.font.DashFont;
import net.oskarstrom.dashloader.def.model.DashModel;
import net.oskarstrom.dashloader.def.model.predicates.DashPredicate;

public enum DashDataType {
	//factories
	MODEL("Model", "models", DashModel.class, true),
	PROPERTY("Property", "properties", DashProperty.class, true),
	PROPERTY_VALUE("Property Value", "values", DashPropertyValue.class, true),
	PREDICATE("Predicate", "predicates", DashPredicate.class, true),
	FONT("Font", "fonts", DashFont.class, true),

	//default
	DATA("Data", "data", DashDataClass.class, false),
	BLOCKSTATE("Blockstate", "blockstate", DashBlockState.class, false),
	IDENTIFIER("Blockstate", "blockstate", DashBlockState.class, false),
	SPRITE("Blockstate", "blockstate", DashBlockState.class, false),
	BAKEDQUAD("Blockstate", "blockstate", DashBlockState.class, false),
	//misc
	DEFAULT("something went wrong", "omegakek", null, true);

	public String name;
	//serializers
	public String internalName;
	public Class<?> clazz;
	public boolean requiresTargetObject;


	DashDataType(String type, String internalName, Class<?> clazz, boolean requiresTargetObject) {
		this.name = type;
		this.internalName = internalName;
		this.clazz = clazz;
		this.requiresTargetObject = requiresTargetObject;
	}

	@Override
	public String toString() {
		return internalName;
	}
}
