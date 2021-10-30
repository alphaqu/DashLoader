package dev.quantumfusion.dashloader.def.data.model;

import dev.quantumfusion.dashloader.core.api.annotation.DashDependencies;
import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.common.ObjectObjectList;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.data.image.DashSprite;
import dev.quantumfusion.dashloader.def.data.model.components.DashBakedQuad;
import dev.quantumfusion.dashloader.def.data.model.components.DashModelOverrideList;
import dev.quantumfusion.dashloader.def.data.model.components.DashModelTransformation;
import dev.quantumfusion.dashloader.def.mixin.accessor.BasicBakedModelAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
@DashObject(BasicBakedModel.class)
@DashDependencies({DashSprite.class, DashBakedQuad.class})
public final class DashBasicBakedModel implements DashModel {
	public final List<DashBakedQuad> quads;
	public final ObjectObjectList<Direction, List<Integer>> faceQuads;
	public final boolean usesAo;
	public final boolean hasDepth;
	public final boolean isSideLit;
	@DataNullable
	public final DashModelTransformation transformation;
	public final DashModelOverrideList itemPropertyOverrides;
	public final int spritePointer;

	public DashBasicBakedModel(List<DashBakedQuad> quads,
			ObjectObjectList<Direction, List<Integer>> faceQuads,
			boolean usesAo, boolean hasDepth, boolean isSideLit,
			DashModelTransformation transformation,
			DashModelOverrideList itemPropertyOverrides,
			int spritePointer) {
		this.quads = quads;
		this.faceQuads = faceQuads;
		this.usesAo = usesAo;
		this.hasDepth = hasDepth;
		this.isSideLit = isSideLit;
		this.transformation = transformation;
		this.itemPropertyOverrides = itemPropertyOverrides;
		this.spritePointer = spritePointer;
	}


	public DashBasicBakedModel(BasicBakedModel basicBakedModel, DashRegistryWriter writer) {
		BasicBakedModelAccessor access = ((BasicBakedModelAccessor) basicBakedModel);
		//DashHelper.convertCollection(access.getQuads(), quad -> new DashBakedQuad(quad, writer))
		this.quads = new ArrayList<>();
		for (var quad : access.getQuads()) quads.add(new DashBakedQuad(quad, writer));

		this.faceQuads = new ObjectObjectList<>();
		access.getFaceQuads().forEach((direction, bakedQuads) -> {
			var bakedQuadsOut = new ArrayList<Integer>();
			for (var bakedQuad : bakedQuads) writer.add(bakedQuad);
			this.faceQuads.put(direction, bakedQuadsOut);
		});

		this.itemPropertyOverrides = new DashModelOverrideList(access.getItemPropertyOverrides(), writer);
		this.usesAo = access.getUsesAo();
		this.hasDepth = access.getHasDepth();
		this.isSideLit = access.getIsSideLit();
		this.transformation = DashModelTransformation.createDashOrReturnNullIfDefault(access.getTransformation());
		spritePointer = writer.add(access.getSprite());
	}


	@Override
	public BasicBakedModel export(final DashRegistryReader reader) {
		final Sprite sprite = reader.get(spritePointer);

		var quadsOut = new ArrayList<BakedQuad>();
		for (DashBakedQuad quad : this.quads) quadsOut.add(quad.export(reader));

		var faceQuadsOut = new HashMap<Direction, List<BakedQuad>>();
		for (var entry : faceQuads.list()) {
			var out = new ArrayList<BakedQuad>();
			for (Integer integer : entry.value()) out.add(reader.get(integer));
			faceQuadsOut.put(entry.key(), out);
		}

		return new BasicBakedModel(quadsOut, faceQuadsOut, usesAo, isSideLit, hasDepth, sprite, DashModelTransformation.exportOrDefault(transformation), itemPropertyOverrides.export(reader));
	}

	@Override
	public void apply(DashRegistryReader registry) {
		itemPropertyOverrides.applyOverrides(registry);
	}
}
