package dev.notalpha.dashloader.client.model;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.client.model.components.BakedQuadCollection;
import dev.notalpha.dashloader.client.model.components.DashModelOverrideList;
import dev.notalpha.dashloader.client.model.components.DashModelTransformation;
import dev.notalpha.dashloader.io.data.collection.ObjectObjectList;
import dev.notalpha.dashloader.mixin.accessor.BasicBakedModelAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.render.model.BuiltinBakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class DashBasicBakedModel implements DashObject<BasicBakedModel> {
	public final int quads;
	public final ObjectObjectList<Direction, Integer> faceQuads;
	public final boolean usesAo;
	public final boolean hasDepth;
	public final boolean isSideLit;
	@DataNullable
	public final DashModelTransformation transformation;
	public final DashModelOverrideList itemPropertyOverrides;
	public final int spritePointer;

	public DashBasicBakedModel(int quads,
							   ObjectObjectList<Direction, Integer> faceQuads,
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


	public DashBasicBakedModel(BasicBakedModel basicBakedModel, RegistryWriter writer) {
		BasicBakedModelAccessor access = ((BasicBakedModelAccessor) basicBakedModel);
		basicBakedModel.getQuads(null, null, Random.create());
		this.quads = writer.add(new BakedQuadCollection(access.getQuads()));

		this.faceQuads = new ObjectObjectList<>();
		access.getFaceQuads().forEach((direction, bakedQuads) -> {
			this.faceQuads.put(direction, writer.add(new BakedQuadCollection(bakedQuads)));
		});

		this.itemPropertyOverrides = new DashModelOverrideList(access.getItemPropertyOverrides(), writer);
		this.usesAo = access.getUsesAo();
		this.hasDepth = access.getHasDepth();
		this.isSideLit = access.getIsSideLit();
		this.transformation = DashModelTransformation.createDashOrReturnNullIfDefault(access.getTransformation());
		this.spritePointer = writer.add(access.getSprite());
	}


	@Override
	public BasicBakedModel export(final RegistryReader reader) {
		final Sprite sprite = reader.get(this.spritePointer);

		BakedQuadCollection collection = reader.get(this.quads);
		var quadsOut = collection.quads;

		var faceQuadsOut = new HashMap<Direction, List<BakedQuad>>();
		for (var entry : this.faceQuads.list()) {
			BakedQuadCollection collectionEntry = reader.get(entry.value());
			faceQuadsOut.put(entry.key(), collectionEntry.quads);
		}

		return new BasicBakedModel(quadsOut, faceQuadsOut, this.usesAo, this.isSideLit, this.hasDepth, sprite, DashModelTransformation.exportOrDefault(this.transformation), this.itemPropertyOverrides.export(reader));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashBasicBakedModel that = (DashBasicBakedModel) o;

		if (quads != that.quads) return false;
		if (usesAo != that.usesAo) return false;
		if (hasDepth != that.hasDepth) return false;
		if (isSideLit != that.isSideLit) return false;
		if (spritePointer != that.spritePointer) return false;
		if (!faceQuads.equals(that.faceQuads)) return false;
		if (!Objects.equals(transformation, that.transformation))
			return false;
		return itemPropertyOverrides.equals(that.itemPropertyOverrides);
	}

	@Override
	public int hashCode() {
		int result = quads;
		result = 31 * result + faceQuads.hashCode();
		result = 31 * result + (usesAo ? 1 : 0);
		result = 31 * result + (hasDepth ? 1 : 0);
		result = 31 * result + (isSideLit ? 1 : 0);
		result = 31 * result + (transformation != null ? transformation.hashCode() : 0);
		result = 31 * result + itemPropertyOverrides.hashCode();
		result = 31 * result + spritePointer;
		return result;
	}
}
