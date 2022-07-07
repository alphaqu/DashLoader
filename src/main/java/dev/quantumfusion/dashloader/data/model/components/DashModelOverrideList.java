package dev.quantumfusion.dashloader.data.model.components;

import dev.quantumfusion.dashloader.mixin.accessor.ModelOverrideListAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.util.Identifier;

public final class DashModelOverrideList {
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

		for (int i = 0; i < overrides.length; i++) {
			this.overrides[i] = new DashModelOverrideListBakedOverride(overrides[i], writer);
		}

		for (int i = 0; i < conditionTypes.length; i++) {
			this.conditionTypes[i] = writer.add(conditionTypes[i]);
		}
	}

	public ModelOverrideList export(RegistryReader reader) {
		this.toApply = ModelOverrideListAccessor.newModelOverrideList();

		var conditionTypesOut = new Identifier[this.conditionTypes.length];
		for (int i = 0; i < this.conditionTypes.length; i++) {
			conditionTypesOut[i] = reader.get(this.conditionTypes[i]);
		}

		((ModelOverrideListAccessor) this.toApply).setConditionTypes(conditionTypesOut);
		return this.toApply;
	}

	public void applyOverrides(RegistryReader reader) {
		var overridesOut = new ModelOverrideList.BakedOverride[this.overrides.length];
		for (int i = 0; i < this.overrides.length; i++) {
			overridesOut[i] = this.overrides[i].export(reader);
		}

		((ModelOverrideListAccessor) this.toApply).setOverrides(overridesOut);
	}
}
