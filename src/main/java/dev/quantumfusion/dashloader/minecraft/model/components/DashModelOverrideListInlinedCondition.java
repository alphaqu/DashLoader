package dev.quantumfusion.dashloader.minecraft.model.components;

import dev.quantumfusion.dashloader.mixin.accessor.ModelOverrideListInlinedCondition;
import net.minecraft.client.render.model.json.ModelOverrideList;

public final class DashModelOverrideListInlinedCondition {
	public final int index;
	public final float threshold;

	public DashModelOverrideListInlinedCondition(int index, float threshold) {
		this.index = index;
		this.threshold = threshold;
	}

	public DashModelOverrideListInlinedCondition(ModelOverrideList.InlinedCondition inlinedCondition) {
		this(inlinedCondition.index, inlinedCondition.threshold);
	}

	public ModelOverrideList.InlinedCondition export() {
		return ModelOverrideListInlinedCondition.newModelOverrideListInlinedCondition(this.index, this.threshold);
	}
}
