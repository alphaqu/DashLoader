package dev.quantumfusion.dashloader.def.fallback;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * An unbaked model which holds a baked model, used for fallback to reuse cached models.
 */
public class UnbakedBakedModel implements UnbakedModel {
	private final BakedModel bakedModel;

	public UnbakedBakedModel(BakedModel bakedModel) {
		this.bakedModel = bakedModel;
	}

	@Override
	public Collection<Identifier> getModelDependencies() {
		return List.of();
	}

	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		return List.of();
	}

	@Nullable
	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		return bakedModel;
	}
}
