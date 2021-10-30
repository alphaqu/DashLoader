package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
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

	public DashModelOverrideList(ModelOverrideList modelOverrideList, DashRegistryWriter writer) {
		final ModelOverrideList.BakedOverride[] overrides = ((ModelOverrideListAccessor) modelOverrideList).getOverrides();
		final Identifier[] conditionTypes = ((ModelOverrideListAccessor) modelOverrideList).getConditionTypes();

		this.overrides = new DashModelOverrideListBakedOverride[overrides.length];
		this.conditionTypes = new Integer[conditionTypes.length];

		for (int i = 0; i < overrides.length; i++)
			this.overrides[i] = new DashModelOverrideListBakedOverride(overrides[i], writer);

		for (int i = 0; i < conditionTypes.length; i++)
			this.conditionTypes[i] = writer.add(conditionTypes[i]);
	}

	public ModelOverrideList export(DashRegistryReader reader) {
		toApply = ModelOverrideListAccessor.newModelOverrideList();

		var conditionTypesOut = new Identifier[conditionTypes.length];
		for (int i = 0; i < conditionTypes.length; i++)
			conditionTypesOut[i] = reader.get(conditionTypes[i]);

		((ModelOverrideListAccessor) toApply).setConditionTypes(conditionTypesOut);
		return toApply;
	}

	public void applyOverrides(DashRegistryReader reader) {
		var overridesOut = new ModelOverrideList.BakedOverride[overrides.length];
		for (int i = 0; i < overrides.length; i++)
			overridesOut[i] = this.overrides[i].export(reader);

		((ModelOverrideListAccessor) toApply).setOverrides(overridesOut);
	}
}
