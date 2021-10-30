package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.dashloader.def.mixin.accessor.ModelOverrideListInlinedCondition;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.json.ModelOverrideList;
@Data
public record DashModelOverrideListInlinedCondition(int index, float threshold) {
	public DashModelOverrideListInlinedCondition(ModelOverrideList.InlinedCondition inlinedCondition) {
		this(inlinedCondition.index, inlinedCondition.threshold);
	}

	public ModelOverrideList.InlinedCondition export() {
		return ModelOverrideListInlinedCondition.newModelOverrideListInlinedCondition(index, threshold);
	}
}
