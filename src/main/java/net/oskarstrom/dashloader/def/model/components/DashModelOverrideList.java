package net.oskarstrom.dashloader.def.model.components;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.mixin.accessor.ModelOverrideListAccessor;

public class DashModelOverrideList {
	@Serialize(order = 0)
	public final DashModelOverrideListBakedOverride[] overrides;
	@Serialize(order = 1)
	public final Integer[] conditionTypes; //identifiers

	ModelOverrideList toApply;

	public DashModelOverrideList(@Deserialize("overrides") DashModelOverrideListBakedOverride[] overrides,
								 @Deserialize("conditionTypes") Integer[] conditionTypes) {
		this.overrides = overrides;
		this.conditionTypes = conditionTypes;
	}

	public DashModelOverrideList(ModelOverrideList modelOverrideList, DashRegistry registry) {
		final ModelOverrideList.BakedOverride[] overrides = ((ModelOverrideListAccessor) modelOverrideList).getOverrides();
		final Identifier[] conditionTypes = ((ModelOverrideListAccessor) modelOverrideList).getConditionTypes();

		this.overrides = DashHelper.convertArrays(overrides, DashModelOverrideListBakedOverride[]::new, bakedOverride -> new DashModelOverrideListBakedOverride(bakedOverride, registry));
		this.conditionTypes = DashHelper.convertArrays(conditionTypes, Integer[]::new, registry::add);

	}

	public ModelOverrideList toUndash(DashExportHandler exportHandler) {
		toApply = ModelOverrideListAccessor.newModelOverrideList();

		final Identifier[] identifiers = DashHelper.convertArrays(conditionTypes, Identifier[]::new, registry::get);
		((ModelOverrideListAccessor) toApply).setConditionTypes(identifiers);

		return toApply;
	}

	public void applyOverrides(DashRegistry registry) {
		ModelOverrideList.BakedOverride[] bakedOverrides = DashHelper.convertArrays(this.overrides, ModelOverrideList.BakedOverride[]::new, override -> override.toUndash(registry));
		((ModelOverrideListAccessor) toApply).setOverrides(bakedOverrides);
	}
}
