package net.oskarstrom.dashloader.def.model;

import net.oskarstrom.dashloader.def.api.DashObject;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashDirectionValue;
import net.oskarstrom.dashloader.def.mixin.accessor.BasicBakedModelAccessor;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.oskarstrom.dashloader.api.data.PairMap;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.registry.Pointer;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.model.components.DashBakedQuad;
import net.oskarstrom.dashloader.def.model.components.DashModelOverrideList;
import net.oskarstrom.dashloader.def.model.components.DashModelTransformation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@DashObject(BasicBakedModel.class)
public class DashBasicBakedModel implements DashModel {
	@Serialize(order = 0)
	public final List<DashBakedQuad> quads;
	@Serialize(order = 1)
	public final PairMap<DashDirectionValue, Collection<Pointer>> faceQuads;
	@Serialize(order = 2)
	public final boolean usesAo;
	@Serialize(order = 3)
	public final boolean hasDepth;
	@Serialize(order = 4)
	public final boolean isSideLit;
	@Serialize(order = 5)
	@SerializeNullable
	public final DashModelTransformation transformation;
	@Serialize(order = 6)
	public final DashModelOverrideList itemPropertyOverrides;
	@Serialize(order = 7)
	public final Pointer spritePointer;


	public DashBasicBakedModel(@Deserialize("quads") List<DashBakedQuad> quads,
							   @Deserialize("faceQuads") PairMap<DashDirectionValue, Collection<Pointer>> faceQuads,
							   @Deserialize("usesAo") boolean usesAo,
							   @Deserialize("hasDepth") boolean hasDepth,
							   @Deserialize("isSideLit") boolean isSideLit,
							   @Deserialize("transformation") DashModelTransformation transformation,
							   @Deserialize("itemPropertyOverrides") DashModelOverrideList itemPropertyOverrides,
							   @Deserialize("spritePointer") Pointer spritePointer) {
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
		quads = DashHelper.convertCollection(access.getQuads(),quad -> new DashBakedQuad(quad,registry));
		final Map<Direction, List<BakedQuad>> faceQuads = access.getFaceQuads();
		this.faceQuads = new PairMap<>(DashHelper.convertMapToCollection(
				faceQuads,
				(entry) -> {
					final List<BakedQuad> value = entry.getValue();
					final Collection<Pointer> right = DashHelper.convertCollection(value, registry::add);
					return PairMap.Entry.of(new DashDirectionValue(entry.getKey()), right);
				}));
		itemPropertyOverrides = new DashModelOverrideList(access.getItemPropertyOverrides(), registry);
		usesAo = access.getUsesAo();
		hasDepth = access.getHasDepth();
		isSideLit = access.getIsSideLit();
		this.transformation = DashModelTransformation.createDashOrReturnNullIfDefault(access.getTransformation());
		spritePointer = registry.add(access.getSprite());
	}


	@Override
	public BasicBakedModel toUndash(final DashRegistry registry) {
		final Sprite sprite = registry.get(spritePointer);
		final List<BakedQuad> quadsOut = DashHelper.convertCollection(quads, bakedQuad -> bakedQuad.toUndash(registry));
		final Map<Direction, List<BakedQuad>> faceQuadsOut = DashHelper.convertCollectionToMap(faceQuads.getData(), (entry) ->
				Pair.of(entry.getKey().toUndash(registry), DashHelper.convertCollection(entry.getValue(), registry::get)));

		return new BasicBakedModel(quadsOut, faceQuadsOut, usesAo, isSideLit, hasDepth, sprite, DashModelTransformation.toUndashOrDefault(transformation), itemPropertyOverrides.toUndash(registry));
	}

	@Override
	public void apply(DashRegistry registry) {
		itemPropertyOverrides.applyOverrides(registry);
	}

}
