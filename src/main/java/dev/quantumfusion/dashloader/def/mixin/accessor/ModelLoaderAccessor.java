package dev.quantumfusion.dashloader.def.mixin.accessor;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
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
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.profiler.Profiler;
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
	Map<Identifier, UnbakedModel> getModelsToBake();

	@Accessor
	@Mutable
	void setResourceManager(ResourceManager resourceManager);

	@Accessor("ITEM_FRAME_STATE_FACTORY")
	static StateManager<Block, BlockState> getTheItemFrameThing() {
		throw new HyphenException("froge", "your dad");
	}
}
