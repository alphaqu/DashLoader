package dev.notalpha.dashloader.client.model.components;

import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.mixin.accessor.ModelOverrideListBakedOverrideAccessor;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public final class DashModelOverrideListBakedOverride {
	public final DashModelOverrideListInlinedCondition[] conditions;
	@DataNullable
	public final Integer model;

	public DashModelOverrideListBakedOverride(DashModelOverrideListInlinedCondition[] conditions, @Nullable Integer model) {
		this.conditions = conditions;
		this.model = model;
	}

	public DashModelOverrideListBakedOverride(ModelOverrideList.BakedOverride override, RegistryWriter writer) {
		final ModelOverrideList.InlinedCondition[] conditionsIn = ((ModelOverrideListBakedOverrideAccessor) override).getConditions();
		BakedModel bakedModel = ((ModelOverrideListBakedOverrideAccessor) override).getModel();
		this.model = bakedModel == null ? null : writer.add(bakedModel);

		this.conditions = new DashModelOverrideListInlinedCondition[conditionsIn.length];
		for (int i = 0; i < conditionsIn.length; i++) {
			this.conditions[i] = new DashModelOverrideListInlinedCondition(conditionsIn[i]);
		}
	}

	public ModelOverrideList.BakedOverride export(RegistryReader reader) {
		var conditionsOut = new ModelOverrideList.InlinedCondition[this.conditions.length];
		for (int i = 0; i < this.conditions.length; i++) {
			conditionsOut[i] = this.conditions[i].export();
		}

		return ModelOverrideListBakedOverrideAccessor.newModelOverrideListBakedOverride(conditionsOut, this.model == null ? null : reader.get(this.model));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashModelOverrideListBakedOverride that = (DashModelOverrideListBakedOverride) o;

		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(conditions, that.conditions)) return false;
		return Objects.equals(model, that.model);
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(conditions);
		result = 31 * result + (model != null ? model.hashCode() : 0);
		return result;
	}
}
