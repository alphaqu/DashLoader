package dev.quantumfusion.dashloader.minecraft.model.components;

import dev.quantumfusion.dashloader.mixin.accessor.ModelOverrideListBakedOverrideAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.util.DashUtil;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.json.ModelOverrideList;
import org.jetbrains.annotations.Nullable;

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
		this.model = DashUtil.nullable(((ModelOverrideListBakedOverrideAccessor) override).getModel(), writer::add);

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

		return ModelOverrideListBakedOverrideAccessor.newModelOverrideListBakedOverride(conditionsOut, DashUtil.nullable(this.model, reader::get));
	}
}
