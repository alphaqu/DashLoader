package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.core.util.DashUtil;
import dev.quantumfusion.dashloader.def.mixin.accessor.ModelOverrideListBakedOverrideAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.json.ModelOverrideList;
import org.jetbrains.annotations.Nullable;

@Data
public final class DashModelOverrideListBakedOverride {
	public final DashModelOverrideListInlinedCondition[] conditions;
	@DataNullable
	public final Integer model;

	public DashModelOverrideListBakedOverride(DashModelOverrideListInlinedCondition[] conditions, @Nullable Integer model) {
		this.conditions = conditions;
		this.model = model;
	}

	public DashModelOverrideListBakedOverride(ModelOverrideList.BakedOverride override, DashRegistryWriter writer) {
		final ModelOverrideList.InlinedCondition[] conditionsIn = ((ModelOverrideListBakedOverrideAccessor) override).getConditions();
		this.model = DashUtil.nullable(((ModelOverrideListBakedOverrideAccessor) override).getModel(), writer::add);

		this.conditions = new DashModelOverrideListInlinedCondition[conditionsIn.length];
		for (int i = 0; i < conditionsIn.length; i++)
			this.conditions[i] = new DashModelOverrideListInlinedCondition(conditionsIn[i]);
	}

	public ModelOverrideList.BakedOverride export(DashRegistryReader reader) {
		var conditionsOut = new ModelOverrideList.InlinedCondition[conditions.length];
		for (int i = 0; i < conditions.length; i++)
			conditionsOut[i] = conditions[i].export();

		return ModelOverrideListBakedOverrideAccessor.newModelOverrideListBakedOverride(conditionsOut, DashUtil.nullable(model, reader::get));
	}
}
