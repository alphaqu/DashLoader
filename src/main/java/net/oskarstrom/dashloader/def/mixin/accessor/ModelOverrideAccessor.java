package net.oskarstrom.dashloader.def.mixin.accessor;

import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ModelOverride.class)
public interface ModelOverrideAccessor {
	@Accessor
	Identifier getModelId();

	@Accessor
	List<ModelOverride.Condition> getConditions();
}
