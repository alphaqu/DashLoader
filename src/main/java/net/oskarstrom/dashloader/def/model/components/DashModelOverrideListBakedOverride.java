package net.oskarstrom.dashloader.def.model.components;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.mixin.accessor.ModelOverrideListBakedOverrideAccessor;
import org.jetbrains.annotations.Nullable;

@Data
public record DashModelOverrideListBakedOverride(DashModelOverrideListInlinedCondition[] conditions,
												 @DataNullable Integer model) {
	public DashModelOverrideListBakedOverride(DashModelOverrideListInlinedCondition[] conditions,
			@Nullable Integer model) {
		this.conditions = conditions;
		this.model = model;
	}

	public DashModelOverrideListBakedOverride(ModelOverrideList.BakedOverride override, DashRegistry registry) {
		this(DashHelper.convertArrays(
				((ModelOverrideListBakedOverrideAccessor) override).getConditions(), DashModelOverrideListInlinedCondition[]::new,
				DashModelOverrideListInlinedCondition::new), DashHelper.nullable(((ModelOverrideListBakedOverrideAccessor) override).getModel(), registry::add));

	}

	public ModelOverrideList.BakedOverride toUndash(DashExportHandler exportHandler) {
		final var conditions = DashHelper.convertArrays(this.conditions, ModelOverrideList.InlinedCondition[]::new, DashModelOverrideListInlinedCondition::toUndash);
		return ModelOverrideListBakedOverrideAccessor.newModelOverrideListBakedOverride(conditions, DashHelper.nullable(model, registry::get));
	}
}
