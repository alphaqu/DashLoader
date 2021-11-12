package dev.quantumfusion.dashloader.def.data.model;

import dev.quantumfusion.dashloader.core.api.DashDependencies;
import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.data.image.DashSprite;
import dev.quantumfusion.dashloader.def.data.model.components.DashModelOverrideList;
import dev.quantumfusion.dashloader.def.data.model.components.DashModelTransformation;
import dev.quantumfusion.dashloader.def.mixin.accessor.BuiltinBakedModelAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.BuiltinBakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;

@Data
@DashObject(BuiltinBakedModel.class)
@DashDependencies(DashSprite.class)
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


	public DashBuiltinBakedModel(BuiltinBakedModel builtinBakedModel, RegistryWriter writer) {
		BuiltinBakedModelAccessor access = ((BuiltinBakedModelAccessor) builtinBakedModel);
		final ModelTransformation transformation = access.getTransformation();
		this.transformation = DashModelTransformation.createDashOrReturnNullIfDefault(transformation);
		itemPropertyOverrides = new DashModelOverrideList(access.getItemPropertyOverrides(), writer);
		spritePointer = writer.add(access.getSprite());
		sideLit = access.getSideLit();
	}


	@Override
	public BuiltinBakedModel export(RegistryReader reader) {
		Sprite sprite = reader.get(spritePointer);
		return new BuiltinBakedModel(DashModelTransformation.exportOrDefault(transformation), itemPropertyOverrides.export(reader), sprite, sideLit);
	}

	@Override
	public void postExport(RegistryReader reader) {
		itemPropertyOverrides.applyOverrides(reader);
	}

}
