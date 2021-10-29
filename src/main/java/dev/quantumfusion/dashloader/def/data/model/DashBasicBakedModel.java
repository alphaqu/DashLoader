package dev.quantumfusion.dashloader.def.data.model;

import dev.quantumfusion.dashloader.def.data.blockstate.property.value.DashDirectionValue;
import dev.quantumfusion.dashloader.def.data.model.components.DashBakedQuad;
import dev.quantumfusion.dashloader.def.data.model.components.DashModelOverrideList;
import dev.quantumfusion.dashloader.def.data.model.components.DashModelTransformation;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.oskarstrom.dashloader.core.data.ObjectObjectList;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.core.annotations.DashObject;
import dev.quantumfusion.dashloader.def.mixin.accessor.BasicBakedModelAccessor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
@DashObject(BasicBakedModel.class)
public final class DashBasicBakedModel implements DashModel {
	public final List<DashBakedQuad> quads;
	public final ObjectObjectList<DashDirectionValue, Collection<Integer>> faceQuads;
	public final boolean usesAo;
	public final boolean hasDepth;
	public final boolean isSideLit;
	@DataNullable
	public final DashModelTransformation transformation;
	public final DashModelOverrideList itemPropertyOverrides;
	public final int spritePointer;

	public DashBasicBakedModel(List<DashBakedQuad> quads,
			ObjectObjectList<DashDirectionValue, Collection<Integer>> faceQuads,
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


	public DashBasicBakedModel(BasicBakedModel basicBakedModel, DashRegistry registry) {
		BasicBakedModelAccessor access = ((BasicBakedModelAccessor) basicBakedModel);
		this.quads = DashHelper.convertCollection(access.getQuads(), quad -> new DashBakedQuad(quad, registry));
		final Map<Direction, List<BakedQuad>> faceQuads = access.getFaceQuads();
		this.faceQuads = new ObjectObjectList<>(DashHelper.convertMapToCollection(
				faceQuads,
				(entry) -> {
					final List<BakedQuad> value = entry.getValue();
					final Collection<Integer> right = DashHelper.convertCollection(value, registry::add);
					return new ObjectObjectList.ObjectObjectEntry<>(new DashDirectionValue(entry.getKey()), right);
				}));
		this.itemPropertyOverrides = new DashModelOverrideList(access.getItemPropertyOverrides(), registry);
		this.usesAo = access.getUsesAo();
		this.hasDepth = access.getHasDepth();
		this.isSideLit = access.getIsSideLit();
		this.transformation = DashModelTransformation.createDashOrReturnNullIfDefault(access.getTransformation());
		spritePointer = registry.add(access.getSprite());
	}


	@Override
	public BasicBakedModel toUndash(final DashExportHandler registry) {
		final Sprite sprite = registry.get(spritePointer);
		final List<BakedQuad> quadsOut = DashHelper.convertCollection(quads, bakedQuad -> bakedQuad.toUndash(registry));
		final Map<Direction, List<BakedQuad>> faceQuadsOut = DashHelper.convertCollectionToMap(faceQuads.list(), (entry) ->
				Pair.of(entry.key().toUndash(registry), DashHelper.convertCollection(entry.value(), registry::get)));

		return new BasicBakedModel(quadsOut, faceQuadsOut, usesAo, isSideLit, hasDepth, sprite, DashModelTransformation.toUndashOrDefault(transformation), itemPropertyOverrides.toUndash(registry));
	}

	@Override
	public void apply(DashExportHandler registry) {
		itemPropertyOverrides.applyOverrides(registry);
	}
}
