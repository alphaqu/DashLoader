package net.oskarstrom.dashloader.def.model.components;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
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
		final ModelOverrideList.InlinedCondition[] conditions = ((ModelOverrideListBakedOverrideAccessor) override).getConditions();
		this.conditions = new DashModelOverrideListInlinedCondition[conditions.length];
		for (int i = 0; i < conditions.length; i++) {
			this.conditions[i] = new DashModelOverrideListInlinedCondition(conditions[i]);
		}
		final BakedModel model = ((ModelOverrideListBakedOverrideAccessor) override).getModel();
		this.model = model == null ? null : registry.models.register(model);
	}

	public ModelOverrideList.BakedOverride toUndash(DashRegistry registry) {
		final ModelOverrideList.InlinedCondition[] conditions = new ModelOverrideList.InlinedCondition[this.conditions.length];
		for (int i = 0; i < this.conditions.length; i++) {
			conditions[i] = this.conditions[i].toUndash();
		}
		return ModelOverrideListBakedOverrideAccessor.newModelOverrideListBakedOverride(conditions, model == null ? null : registry.models.getObject(model));
	}
}
