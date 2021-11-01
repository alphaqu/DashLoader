package dev.quantumfusion.dashloader.def.mixin.accessor;

import com.google.common.collect.Sets;
import dev.quantumfusion.hyphen.thr.HyphenException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AffineTransformation;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.Set;

@Mixin(ModelLoader.class)
public interface ModelLoaderAccessor {

	@Accessor
	void setSpriteAtlasManager(SpriteAtlasManager spriteAtlasManager);

	@Accessor
	@Mutable
	void setUnbakedModels(Map<Identifier, UnbakedModel> unbakedModels);

	@Accessor
	@Mutable
	void setModelsToLoad(Set<Identifier> modelsToLoad);

	@Accessor
	@Mutable
	void setBakedModelCache(Map<Triple<Identifier, AffineTransformation, Boolean>, BakedModel> bakedModelCache);


	@Accessor
	@Mutable
	void setModelsToBake(Map<Identifier, UnbakedModel> modelsToBake);

	@Accessor
	@Mutable
	void setResourceManager(ResourceManager resourceManager);

	@Accessor
	@Mutable
	void setBlockColors(BlockColors blockColors);

	@Accessor
	@Mutable
	void setStateLookup(Object2IntMap<BlockState> blockColors);

	@Accessor
	@Mutable
	void setVariantMapDeserializationContext(ModelVariantMap.DeserializationContext deserializationContext);

	@Invoker("addModel")
	void invokeAddModel(ModelIdentifier modelId);


	@Accessor("ITEM_FRAME_STATE_FACTORY")
	static StateManager<Block, BlockState> getTheItemFrameThing() {
		throw new HyphenException("froge", "your dad");
	}

	@Accessor("STATIC_DEFINITIONS")
	static Map<Identifier, StateManager<Block, BlockState>> getTheItemFrameThingMap() {
		throw new HyphenException("froge", "your dad");
	}



}
