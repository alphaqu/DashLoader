package net.oskarstrom.dashloader.def.model;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.BuiltinBakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.mixin.accessor.BuiltinBakedModelAccessor;
import net.oskarstrom.dashloader.def.model.components.DashModelOverrideList;
import net.oskarstrom.dashloader.def.model.components.DashModelTransformation;

@Data
@DashObject(BuiltinBakedModel.class)
public class DashBuiltinBakedModel implements DashModel {
	@DataNullable
	public final DashModelTransformation transformation;
	public final DashModelOverrideList itemPropertyOverrides;
	public final int spritePointer;
	public final boolean sideLit;

	public DashBuiltinBakedModel(DashModelTransformation transformation, DashModelOverrideList itemPropertyOverrides, int spritePointer, boolean sideLit) {
		this.transformation = transformation;
		this.itemPropertyOverrides = itemPropertyOverrides;
		this.spritePointer = spritePointer;
		this.sideLit = sideLit;
	}


	public DashBuiltinBakedModel(BuiltinBakedModel builtinBakedModel, DashRegistry registry) {
		BuiltinBakedModelAccessor access = ((BuiltinBakedModelAccessor) builtinBakedModel);
		final ModelTransformation transformation = access.getTransformation();
		this.transformation = DashModelTransformation.createDashOrReturnNullIfDefault(transformation);
		itemPropertyOverrides = new DashModelOverrideList(access.getItemPropertyOverrides(), registry);
		spritePointer = registry.add(access.getSprite());
		sideLit = access.getSideLit();
	}


	@Override
	public BuiltinBakedModel toUndash(DashExportHandler handler) {
		Sprite sprite = handler.get(spritePointer);
		return new BuiltinBakedModel(DashModelTransformation.toUndashOrDefault(transformation), itemPropertyOverrides.toUndash(handler), sprite, sideLit);
	}

	@Override
	public void apply(DashExportHandler handler) {
		itemPropertyOverrides.applyOverrides(handler);
	}

}
