package dev.notalpha.dashloader.client.model;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.client.model.components.DashModelOverrideList;
import dev.notalpha.dashloader.client.model.components.DashModelTransformation;
import dev.notalpha.dashloader.mixin.accessor.BuiltinBakedModelAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BuiltinBakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;

import java.util.Objects;

public final class DashBuiltinBakedModel implements DashObject<BuiltinBakedModel> {
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashBuiltinBakedModel that = (DashBuiltinBakedModel) o;

		if (spritePointer != that.spritePointer) return false;
		if (sideLit != that.sideLit) return false;
		if (!Objects.equals(transformation, that.transformation))
			return false;
		return itemPropertyOverrides.equals(that.itemPropertyOverrides);
	}

	@Override
	public int hashCode() {
		int result = transformation != null ? transformation.hashCode() : 0;
		result = 31 * result + itemPropertyOverrides.hashCode();
		result = 31 * result + spritePointer;
		result = 31 * result + (sideLit ? 1 : 0);
		return result;
	}
}
