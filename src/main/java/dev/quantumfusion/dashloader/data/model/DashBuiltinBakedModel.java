package dev.quantumfusion.dashloader.data.model;

import dev.quantumfusion.dashloader.api.DashDependencies;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.data.image.DashSprite;
import dev.quantumfusion.dashloader.data.model.components.DashModelOverrideList;
import dev.quantumfusion.dashloader.data.model.components.DashModelTransformation;
import dev.quantumfusion.dashloader.mixin.accessor.BuiltinBakedModelAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.BuiltinBakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;

@DashObject(BuiltinBakedModel.class)
@DashDependencies(DashSprite.class)
public final class DashBuiltinBakedModel implements DashModel {
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
		this.itemPropertyOverrides = new DashModelOverrideList(access.getItemPropertyOverrides(), writer);
		this.spritePointer = writer.add(access.getSprite());
		this.sideLit = access.getSideLit();
	}


	@Override
	public BuiltinBakedModel export(RegistryReader reader) {
		Sprite sprite = reader.get(this.spritePointer);
		return new BuiltinBakedModel(DashModelTransformation.exportOrDefault(this.transformation), this.itemPropertyOverrides.export(reader), sprite, this.sideLit);
	}

	@Override
	public void postExport(RegistryReader reader) {
		this.itemPropertyOverrides.applyOverrides(reader);
	}
}
