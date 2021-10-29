package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.util.Identifier;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;
import dev.quantumfusion.dashloader.def.mixin.accessor.ModelOverrideListAccessor;
@Data
public class DashModelOverrideList {
	public final DashModelOverrideListBakedOverride[] overrides;
	public final Integer[] conditionTypes; //identifiers

	transient ModelOverrideList toApply;

	public DashModelOverrideList(DashModelOverrideListBakedOverride[] overrides, Integer[] conditionTypes) {
		this.overrides = overrides;
		this.conditionTypes = conditionTypes;
	}

	public DashModelOverrideList(ModelOverrideList modelOverrideList, DashRegistry registry) {
		final ModelOverrideList.BakedOverride[] overrides = ((ModelOverrideListAccessor) modelOverrideList).getOverrides();
		final Identifier[] conditionTypes = ((ModelOverrideListAccessor) modelOverrideList).getConditionTypes();

		this.overrides = DashHelper.convertArrays(overrides, DashModelOverrideListBakedOverride[]::new, bakedOverride -> new DashModelOverrideListBakedOverride(bakedOverride, registry));
		this.conditionTypes = DashHelper.convertArrays(conditionTypes, Integer[]::new, registry::add);

	}

	public ModelOverrideList toUndash(DashExportHandler handler) {
		toApply = ModelOverrideListAccessor.newModelOverrideList();

		final Identifier[] identifiers = DashHelper.convertArrays(conditionTypes, Identifier[]::new, handler::get);
		((ModelOverrideListAccessor) toApply).setConditionTypes(identifiers);

		return toApply;
	}

	public void applyOverrides(DashExportHandler registry) {
		ModelOverrideList.BakedOverride[] bakedOverrides = DashHelper.convertArrays(this.overrides, ModelOverrideList.BakedOverride[]::new, override -> override.toUndash(registry));
		((ModelOverrideListAccessor) toApply).setOverrides(bakedOverrides);
	}
}
