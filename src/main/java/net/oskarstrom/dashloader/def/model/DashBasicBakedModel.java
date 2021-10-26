package net.oskarstrom.dashloader.def.model;

import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.oskarstrom.dashloader.core.data.ObjectObjectList;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashDirectionValue;
import net.oskarstrom.dashloader.def.mixin.accessor.BasicBakedModelAccessor;
import net.oskarstrom.dashloader.def.model.components.DashBakedQuad;
import net.oskarstrom.dashloader.def.model.components.DashModelOverrideList;
import net.oskarstrom.dashloader.def.model.components.DashModelTransformation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@DashObject(BasicBakedModel.class)
public record DashBasicBakedModel(List<DashBakedQuad> quads,
								  ObjectObjectList<DashDirectionValue, Collection<Integer>> faceQuads,
								  boolean usesAo, boolean hasDepth, boolean isSideLit,
								  @DataNullable DashModelTransformation transformation,
								  DashModelOverrideList itemPropertyOverrides,
								  int spritePointer) implements DashModel {


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
		final Map<Direction, List<BakedQuad>> faceQuadsOut = DashHelper.convertCollectionToMap(faceQuads.data, (entry) ->
				Pair.of(entry.getKey().toUndash(registry), DashHelper.convertCollection(entry.getValue(), registry::get)));

		return new BasicBakedModel(quadsOut, faceQuadsOut, usesAo, isSideLit, hasDepth, sprite, DashModelTransformation.toUndashOrDefault(transformation), itemPropertyOverrides.toUndash(registry));
	}

	@Override
	public void apply(DashRegistry registry) {
		itemPropertyOverrides.applyOverrides(registry);
	}

}
