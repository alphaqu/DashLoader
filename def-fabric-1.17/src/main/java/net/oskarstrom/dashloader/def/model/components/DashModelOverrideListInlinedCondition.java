package net.oskarstrom.dashloader.def.model.components;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.oskarstrom.dashloader.def.mixin.accessor.ModelOverrideListInlinedCondition;

public class DashModelOverrideListInlinedCondition {
	@Serialize(order = 0)
	public final int index;
	@Serialize(order = 1)
	public final float threshold;


	public DashModelOverrideListInlinedCondition(@Deserialize("index") int index, @Deserialize("threshold") float threshold) {
		this.index = index;
		this.threshold = threshold;
	}

	public DashModelOverrideListInlinedCondition(ModelOverrideList.InlinedCondition inlinedCondition) {
		index = inlinedCondition.index;
		threshold = inlinedCondition.threshold;
	}

	public ModelOverrideList.InlinedCondition toUndash() {
		return ModelOverrideListInlinedCondition.newModelOverrideListInlinedCondition(index, threshold);
	}
}
