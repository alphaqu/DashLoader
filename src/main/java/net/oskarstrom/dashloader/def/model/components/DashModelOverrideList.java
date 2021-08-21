package net.oskarstrom.dashloader.def.model.components;

import net.oskarstrom.dashloader.def.mixin.accessor.ModelOverrideListAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.registry.Pointer;
import net.oskarstrom.dashloader.core.util.DashHelper;

public class DashModelOverrideList {
	@Serialize(order = 0)
	public final DashModelOverrideListBakedOverride[] overrides;
	@Serialize(order = 1)
	public final Pointer[] conditionTypes; //identifiers

	ModelOverrideList toApply;

	public DashModelOverrideList(@Deserialize("overrides") DashModelOverrideListBakedOverride[] overrides,
								 @Deserialize("conditionTypes") Pointer[] conditionTypes) {
		this.overrides = overrides;
		this.conditionTypes = conditionTypes;
	}

	public DashModelOverrideList(ModelOverrideList modelOverrideList, DashRegistry registry) {
		final ModelOverrideList.BakedOverride[] overrides = ((ModelOverrideListAccessor) modelOverrideList).getOverrides();
		final Identifier[] conditionTypes = ((ModelOverrideListAccessor) modelOverrideList).getConditionTypes();

		this.overrides = DashHelper.convertArrays(overrides, bakedOverride -> new DashModelOverrideListBakedOverride(bakedOverride, registry));
		this.conditionTypes = DashHelper.convertArrays(conditionTypes, registry::add);

	}

	public ModelOverrideList toUndash(DashRegistry registry) {
		toApply = ModelOverrideListAccessor.newModelOverrideList();

		final Identifier[] identifiers = DashHelper.convertArrays(conditionTypes, registry::get);
		((ModelOverrideListAccessor) toApply).setConditionTypes(identifiers);

		return toApply;
	}

	public void applyOverrides(DashRegistry registry) {
		final var bakedOverrides = DashHelper.convertArrays(this.overrides, override -> override.toUndash(registry));
		((ModelOverrideListAccessor) toApply).setOverrides(bakedOverrides);
	}
}
