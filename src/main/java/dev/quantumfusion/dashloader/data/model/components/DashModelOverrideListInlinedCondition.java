package dev.quantumfusion.dashloader.data.model.components;

import dev.quantumfusion.dashloader.mixin.accessor.ModelOverrideListInlinedCondition;
import net.minecraft.client.render.model.json.ModelOverrideList;

public record DashModelOverrideListInlinedCondition(int index, float threshold) {
	public DashModelOverrideListInlinedCondition(ModelOverrideList.InlinedCondition inlinedCondition) {
		this(inlinedCondition.index, inlinedCondition.threshold);
	}

	public ModelOverrideList.InlinedCondition export() {
		return ModelOverrideListInlinedCondition.newModelOverrideListInlinedCondition(this.index, this.threshold);
	}
}
