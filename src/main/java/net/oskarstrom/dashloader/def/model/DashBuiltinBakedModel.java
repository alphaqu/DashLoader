package net.oskarstrom.dashloader.def.model;

import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.mixin.accessor.BuiltinBakedModelAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;
import net.minecraft.client.render.model.BuiltinBakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.registry.Pointer;
import net.oskarstrom.dashloader.def.model.components.DashModelOverrideList;
import net.oskarstrom.dashloader.def.model.components.DashModelTransformation;

@DashObject(BuiltinBakedModel.class)
public class DashBuiltinBakedModel implements DashModel {
	@Serialize(order = 0)
	@SerializeNullable
	public final DashModelTransformation transformation;
	@Serialize(order = 1)
	public final DashModelOverrideList itemPropertyOverrides;
	@Serialize(order = 2)
	public final int spritePointer;
	@Serialize(order = 3)
	public final boolean sideLit;

	public DashBuiltinBakedModel(
			@Deserialize("transformation") DashModelTransformation transformation,
			@Deserialize("itemPropertyOverrides") DashModelOverrideList itemPropertyOverrides,
			@Deserialize("spritePointer") int spritePointer,
			@Deserialize("sideLit") boolean sideLit) {
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
	public BuiltinBakedModel toUndash(DashRegistry registry) {
		Sprite sprite = registry.get(spritePointer);
		return new BuiltinBakedModel(DashModelTransformation.toUndashOrDefault(transformation), itemPropertyOverrides.toUndash(registry), sprite, sideLit);
	}

	@Override
	public void apply(DashRegistry registry) {
		itemPropertyOverrides.applyOverrides(registry);
	}

}
