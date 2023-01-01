package dev.quantumfusion.dashloader.minecraft.model;

import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.io.data.collection.ObjectObjectList;
import dev.quantumfusion.dashloader.minecraft.model.components.BakedQuadCollection;
import dev.quantumfusion.dashloader.minecraft.model.components.DashModelOverrideList;
import dev.quantumfusion.dashloader.minecraft.model.components.DashModelTransformation;
import dev.quantumfusion.dashloader.mixin.accessor.BasicBakedModelAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// StagedDataChunk -> interface DashModel {BasicBakedModel, WeightedBakedModel, MultipartBakedModel}
// SimpleDataChunk -> class DashImage


//  class BasicBakedModel -> output DashModel
//  class WeightedBakedModel(BasicBakedModel) -> output DashModel
//  class MultipartBakedModel(WeightedBakedModel) -> output DashModel
//  class DashImage[] -> output DashImage

// 2 outputs [DashModel, DashImage]


@DashObject(BasicBakedModel.class)
public final class DashBasicBakedModel implements DashModel {
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
	public void postExport(RegistryReader reader) {
		this.itemPropertyOverrides.applyOverrides(reader);
	}
}
