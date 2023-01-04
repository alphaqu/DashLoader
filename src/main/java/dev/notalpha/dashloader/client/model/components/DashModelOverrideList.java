package dev.notalpha.dashloader.client.model.components;

import dev.notalpha.dashloader.mixin.accessor.ModelOverrideListAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.util.Identifier;

import java.util.Arrays;

public final class DashModelOverrideList {
	public final DashModelOverrideListBakedOverride[] overrides;
	public final int[] conditionTypes; //identifiers

	public DashModelOverrideList(DashModelOverrideListBakedOverride[] overrides, int[] conditionTypes) {
		this.overrides = overrides;
		this.conditionTypes = conditionTypes;
	}

	public DashModelOverrideList(ModelOverrideList modelOverrideList, RegistryWriter writer) {
		final ModelOverrideList.BakedOverride[] overrides = ((ModelOverrideListAccessor) modelOverrideList).getOverrides();
		final Identifier[] conditionTypes = ((ModelOverrideListAccessor) modelOverrideList).getConditionTypes();

		this.overrides = new DashModelOverrideListBakedOverride[overrides.length];
		this.conditionTypes = new int[conditionTypes.length];

		for (int i = 0; i < overrides.length; i++) {
			this.overrides[i] = new DashModelOverrideListBakedOverride(overrides[i], writer);
		}

		for (int i = 0; i < conditionTypes.length; i++) {
			this.conditionTypes[i] = writer.add(conditionTypes[i]);
		}
	}

	public ModelOverrideList export(RegistryReader reader) {
		var out = ModelOverrideListAccessor.newModelOverrideList();
		ModelOverrideListAccessor access = (ModelOverrideListAccessor) out;

		var conditionTypesOut = new Identifier[this.conditionTypes.length];
		for (int i = 0; i < this.conditionTypes.length; i++) {
			conditionTypesOut[i] = reader.get(this.conditionTypes[i]);
		}

		var overridesOut = new ModelOverrideList.BakedOverride[this.overrides.length];
		for (int i = 0; i < this.overrides.length; i++) {
			overridesOut[i] = this.overrides[i].export(reader);
		}

		access.setConditionTypes(conditionTypesOut);
		access.setOverrides(overridesOut);
		return out;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashModelOverrideList that = (DashModelOverrideList) o;

		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(overrides, that.overrides)) return false;
		return Arrays.equals(conditionTypes, that.conditionTypes);
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(overrides);
		result = 31 * result + Arrays.hashCode(conditionTypes);
		return result;
	}
}
