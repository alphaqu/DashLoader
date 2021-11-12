package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.mixin.accessor.ModelOverrideListAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.util.Identifier;

@Data
public class DashModelOverrideList {
	public final DashModelOverrideListBakedOverride[] overrides;
	public final Integer[] conditionTypes; //identifiers

	transient ModelOverrideList toApply;

	public DashModelOverrideList(DashModelOverrideListBakedOverride[] overrides, Integer[] conditionTypes) {
		this.overrides = overrides;
		this.conditionTypes = conditionTypes;
	}

	public DashModelOverrideList(ModelOverrideList modelOverrideList, RegistryWriter writer) {
		final ModelOverrideList.BakedOverride[] overrides = ((ModelOverrideListAccessor) modelOverrideList).getOverrides();
		final Identifier[] conditionTypes = ((ModelOverrideListAccessor) modelOverrideList).getConditionTypes();

		this.overrides = new DashModelOverrideListBakedOverride[overrides.length];
		this.conditionTypes = new Integer[conditionTypes.length];

		for (int i = 0; i < overrides.length; i++)
			this.overrides[i] = new DashModelOverrideListBakedOverride(overrides[i], writer);

		for (int i = 0; i < conditionTypes.length; i++)
			this.conditionTypes[i] = writer.add(conditionTypes[i]);
	}

	public ModelOverrideList export(RegistryReader reader) {
		toApply = ModelOverrideListAccessor.newModelOverrideList();

		var conditionTypesOut = new Identifier[conditionTypes.length];
		for (int i = 0; i < conditionTypes.length; i++)
			conditionTypesOut[i] = reader.get(conditionTypes[i]);

		((ModelOverrideListAccessor) toApply).setConditionTypes(conditionTypesOut);
		return toApply;
	}

	public void applyOverrides(RegistryReader reader) {
		var overridesOut = new ModelOverrideList.BakedOverride[overrides.length];
		for (int i = 0; i < overrides.length; i++)
			overridesOut[i] = this.overrides[i].export(reader);

		((ModelOverrideListAccessor) toApply).setOverrides(overridesOut);
	}
}
