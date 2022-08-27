package dev.quantumfusion.dashloader.fallback.model;

import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * An unbaked model which holds a baked model, used for fallback to reuse cached models.
 */
public class UnbakedBakedModel extends JsonUnbakedModel implements UnbakedModel {
	private final BakedModel bakedModel;

	public UnbakedBakedModel(BakedModel bakedModel, Identifier identifier) {
		super(null, List.of(), Map.of(), bakedModel.useAmbientOcclusion(), GuiLight.ITEM, ModelTransformation.NONE, List.of());
		this.id = identifier.toString();
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
		return this.bakedModel;
	}
}
