package net.oskarstrom.dashloader.def.model.components;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.registry.Pointer;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.mixin.accessor.ModelOverrideListBakedOverrideAccessor;
import org.jetbrains.annotations.Nullable;

public class DashModelOverrideListBakedOverride {
	@Serialize(order = 0)
	public final DashModelOverrideListInlinedCondition[] conditions;
	@Nullable
	@Serialize(order = 1)
	@SerializeNullable
	public final Integer model; // temp

	public DashModelOverrideListBakedOverride(@Deserialize("conditions") DashModelOverrideListInlinedCondition[] conditions,
											  @Deserialize("model") @Nullable Integer model) {
		this.conditions = conditions;
		this.model = model;
	}


	public DashModelOverrideListBakedOverride(ModelOverrideList.BakedOverride override, DashRegistry registry) {
		final ModelOverrideListBakedOverrideAccessor access = (ModelOverrideListBakedOverrideAccessor) override;

		this.conditions = DashHelper.convertArrays(access.getConditions(), DashModelOverrideListInlinedCondition[]::new, DashModelOverrideListInlinedCondition::new);
		this.model = DashHelper.nullable(access.getModel(), registry::add);

	}

	public ModelOverrideList.BakedOverride toUndash(DashRegistry registry) {
		final var conditions = DashHelper.convertArrays(this.conditions, ModelOverrideList.InlinedCondition[]::new, DashModelOverrideListInlinedCondition::toUndash);
		return ModelOverrideListBakedOverrideAccessor.newModelOverrideListBakedOverride(conditions, DashHelper.nullable(model, registry::get));
	}
}
